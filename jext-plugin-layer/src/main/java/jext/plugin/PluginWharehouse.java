/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.plugin;


import static java.util.stream.Collectors.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.jar.*;
import java.util.stream.Stream;

import jext.plugin.internal.*;
import static jext.plugin.internal.InternalUtil.*;
import org.slf4j.*;


public class PluginWharehouse {

    public enum Event { ADDED, REMOVED, ENABLED, DISABLED }
    public interface Listener extends BiConsumer<Event,List<PluginDescriptor>> {};


    protected static final Logger LOGGER = LoggerFactory.getLogger(PluginWharehouse.class);

    public static final String REGISTRY_FILE = "plugins";
    public static final String REPOSITORY_FOLDER = "repository";
    public static final String DROPINS_FOLDER = "dropins";


    public static PluginWharehouseBuilder builder() {
        return new PluginWharehouseBuilder();
    }

    private final Path repositoryFolder;
    private final PluginRegistryFile registryFile;
    private final RepositoryLayout repositoryLayout;
    private final List<Listener> listeners = new ArrayList<>();

    private ConcurrentHashMap<String,PluginDescriptor> enabledPlugins;
    private ConcurrentHashMap<String,PluginDescriptor> disabledPlugins;




    PluginWharehouse(
        Path wharehouseFolder,
        Class<? extends RepositoryLayout> repositoryLayoutClass,
        Properties repositoryLayoutProperties,
        boolean dropinsEnabled,
        boolean online
    ) throws IOException {

        createFolder(wharehouseFolder);

        this.repositoryFolder = createFolder(wharehouseFolder.resolve(REPOSITORY_FOLDER));
        this.repositoryLayout = InternalUtil.newRepositoryLayout(repositoryLayoutClass,repositoryFolder);
        this.repositoryLayout.setOnline(online);
        this.repositoryLayout.configure(repositoryLayoutProperties);

        this.registryFile = new PluginRegistryFile(wharehouseFolder.resolve(REGISTRY_FILE));
        obtainPluginDescriptors();

        if (dropinsEnabled) {
            var dropinsFolder = createFolder(wharehouseFolder.resolve(DROPINS_FOLDER));
            new DropinProcessor(dropinsFolder, this).processDropins();
        }
    }




    public Class<? extends RepositoryLayout> layoutType() {
        return this.repositoryLayout.getClass();
    }



    public synchronized Stream<PluginDescriptor> obtainPluginDescriptors() {
        if (this.enabledPlugins == null) {

            this.enabledPlugins = registryFile.lines()
                .filter(line->!line.startsWith("#"))
                .map(this::obtainPluginDescriptor)
                .flatMap(Optional::stream)
                .collect(toMap(PluginDescriptor::id, x->x, (x,y)->x, ConcurrentHashMap::new));

            this.disabledPlugins = registryFile.lines()
                .filter(line->line.startsWith("#"))
                .map(line->line.substring(1))
                .map(this::obtainPluginDescriptor)
                .flatMap(Optional::stream)
                .collect(toMap(PluginDescriptor::id, x->x, (x,y)->x, ConcurrentHashMap::new));
        }
        return this.enabledPlugins.values().stream();
    }


    public synchronized Optional<PluginDescriptor> getPlugin(String plugin) {
        return Optional.ofNullable(enabledPlugins.get(plugin));
    }


    public synchronized boolean containsPlugin(String plugin) {
        return this.enabledPlugins.containsKey(plugin) || this.disabledPlugins.containsKey(plugin);
    }

    public synchronized boolean isPluginEnabled(String plugin) {
        return !this.enabledPlugins.containsKey(plugin);
    }


    public synchronized List<PluginDescriptor> installPluginsFromRepository(List<String> requestedPlugins) {
        return processNewPlugins(requestedPlugins, requestedPlugins.stream()
            .parallel()
            .filter( it -> !containsPlugin(it) )
            .flatMap( it -> repositoryLayout.fetchArtifact(it).stream() )
        );
    }


    public synchronized List<PluginDescriptor> installPluginsFromZipFiles(List<Path> requestedPlugins) {
        return processNewPlugins(List.of(), requestedPlugins.stream()
             .map( it -> new DropinProcessor(it, this) )
             .flatMap( it -> it.processDropins().stream() )
        );
    }

    public synchronized List<PluginDescriptor> uninstallPlugins(List<String> requestedPlugins) {

        var uninstalledPlugins = requestedPlugins.stream()
            .filter(this::containsPlugin)
            .map(it -> repositoryLayout.deleteArtifact(it) ? it : null)
            .filter(Objects::nonNull)
            .collect(toList());

        var notUninstalledPlugins = requestedPlugins.stream()
            .filter( it -> !uninstalledPlugins.contains(it))
            .collect(toList());

        if (!notUninstalledPlugins.isEmpty()) {
            LOGGER.warn("The following plugins were not uninstalled: {}", notUninstalledPlugins);
        }

        var uninstallerPluginDescriptors = Stream.concat(
            uninstalledPlugins.stream().map(enabledPlugins::get),
            uninstalledPlugins.stream().map(disabledPlugins::get)
        ).filter(Objects::nonNull).collect(toList());

        notifyPluginChange(Event.REMOVED, uninstallerPluginDescriptors);
        LOGGER.info(
            "The following plugins have been uninstalled: {}",
            formatList(uninstalledPlugins)
        );
        return uninstallerPluginDescriptors;
    }


    public synchronized void clear() {
        try {
            enabledPlugins = null;
            disabledPlugins = null;
            registryFile.clear();
            InternalUtil.deleteSubdirectories(repositoryFolder, LOGGER);
        } catch (IOException e) {
            LOGGER.error("Cannot clear plugin wharehouse: {}", e.getMessage());
            LOGGER.debug(e.toString(), e);
            throw new PluginException(e);
        }
    }


    public synchronized void disablePlugins(List<String> plugins) {
        var descriptors = plugins.stream()
            .map(enabledPlugins::remove)
            .filter(Objects::nonNull)
            .collect(toList());
        descriptors.forEach( it -> disabledPlugins.put(it.id(), it));
        notifyPluginChange(Event.DISABLED, descriptors);
    }


    public synchronized void enablePlugins(List<String> plugins) {
        var descriptors = plugins.stream()
            .map(disabledPlugins::remove)
            .filter(Objects::nonNull)
            .collect(toList());
        descriptors.forEach( it -> enabledPlugins.put(it.id(), it));
        notifyPluginChange(Event.ENABLED, descriptors);
    }


    private List<PluginDescriptor> processNewPlugins(
        List<String> requestedPlugins,
        Stream<String> newPlugins
    ) {
        var installedPluginDescriptors = newPlugins
           .flatMap( it -> repositoryLayout.fetchArtifact(it).stream() )
           .flatMap( it -> obtainPluginDescriptor(it).stream() )
          .collect(toList());

        var installedPlugins = installedPluginDescriptors.stream()
            .map(PluginDescriptor::id)
            .collect(toList());


        var notInstalledPlugins = requestedPlugins.stream()
            .filter( it -> !installedPlugins.contains(it))
            .collect(toList());

        if (!notInstalledPlugins.isEmpty()) {
            LOGGER.warn("The following plugins were not installed: {}", notInstalledPlugins);
        }

        try {
            registryFile.add(installedPlugins);
            installedPluginDescriptors.forEach( it -> enabledPlugins.put(it.id(), it));
            notifyPluginChange(Event.ADDED, installedPluginDescriptors);
            LOGGER.info(
                "The following plugins have been installed: {}",
                formatList(installedPlugins)
            );
            return installedPluginDescriptors;
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }


    public boolean isEmpty() {
        return this.enabledPlugins.isEmpty() && this.disabledPlugins.isEmpty();
    }


    public boolean notHasEnabledPlugins() {
        return this.enabledPlugins.isEmpty();
    }


    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }


    void copyContentsTo(PluginWharehouse wharehouse) throws IOException {
        if (!this.layoutType().equals(wharehouse.layoutType())) {
            throw new PluginException("Cannot copy contents to a wharehouse using different layouts");
        }
        this.repositoryLayout.copyToOtherRepository(wharehouse.repositoryLayout);
        this.registryFile.append(wharehouse.registryFile);
    }



    private Optional<PluginDescriptor> obtainPluginDescriptor(String coordinates) {
        try {
            var pluginPath = repositoryLayout
            .obtainArtifactFromCoordinates(coordinates)
            .orElseThrow(()->new PluginException(
                "Artifact file {} defined in registry does not exist and could not be retrieved",
                coordinates
            ));
            var manifest = readManifest(pluginPath);
            validateManifest(manifest);
            List<Path> dependencies = obtainDependenciesFromManifest(manifest).stream()
                .map(repositoryLayout::obtainArtifactFromCoordinates)
                .flatMap(Optional::stream)
                .collect(toList());
            return Optional.of(new PluginDescriptor(manifest, pluginPath, dependencies));
        } catch (Exception e) {
            LOGGER.error(
                "Cannot resolve plugin {} from registry file: {}",
                coordinates,
                e.getMessage()
            );
            LOGGER.debug("",e);
            return Optional.empty();
        }
    }



    private Manifest readManifest (Path pluginPath) {
        try (var inputStream = new JarInputStream(Files.newInputStream(pluginPath))) {
            var manifest = inputStream.getManifest();
            if (manifest == null) {
                throw new PluginException("Plugin file {} defined in registry has no manifest", pluginPath);
            }
            return manifest;
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }



    private void validateManifest(Manifest manifest) {
        var requiredAttributes = List.of(
            ManifestAttributes.PLUGIN_NAME,
            ManifestAttributes.PLUGIN_VENDOR,
            ManifestAttributes.PLUGIN_VERSION
        );
        for (String attribute : requiredAttributes) {
            if (manifest.getMainAttributes().getValue(attribute) == null) {
                throw new PluginException(
                    "The Manifest must have the attribute {}",
                    attribute
                );
            }
        }
    }


    private List<String> obtainDependenciesFromManifest(Manifest manifest) {
        var dependencies = manifest.getMainAttributes()
            .getValue(ManifestAttributes.PLUGIN_DEPENDENCIES);
        if (dependencies == null) {
            return List.of();
        }
        return List.of(dependencies.replace(" ","").split(","));
    }


    private void notifyPluginChange(Event event, List<PluginDescriptor> plugins) {
        listeners.forEach( it -> it.accept(event,plugins) );
    }












}

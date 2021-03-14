/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.plugin;


import static java.util.stream.Collectors.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;

import jext.plugin.internal.*;
import org.slf4j.*;


public class PluginWharehouse {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PluginWharehouse.class);

    public static final String REGISTRY_FILE = "plugins";
    public static final String REPOSITORY_FOLDER = "repository";
    public static final String DROPINS_FOLDER = "dropins";


    public static PluginWharehouseBuilder builder() {
        return new PluginWharehouseBuilder();
    }

    private final PluginRegistryFile registryFile;
    private final RepositoryLayout repositoryLayout;

    private Map<String,PluginDescriptor> plugins;



    PluginWharehouse(
        Path wharehouseFolder,
        Class<? extends RepositoryLayout> repositoryLayoutClass,
        Properties repositoryLayoutProperties,
        boolean dropinsEnabled,
        boolean online
    ) throws IOException {

        createFolder(wharehouseFolder);

        var repositoryFolder = createFolder(wharehouseFolder.resolve(REPOSITORY_FOLDER));
        this.repositoryLayout = InternalUtil.newRepositoryLayout(repositoryLayoutClass,repositoryFolder);
        this.repositoryLayout.setOnline(online);
        this.repositoryLayout.configure(repositoryLayoutProperties);

        this.registryFile = new PluginRegistryFile(wharehouseFolder.resolve(REGISTRY_FILE));
        obtainPluginDescriptors();

        if (dropinsEnabled) {
            var dropinsFolder = createFolder(wharehouseFolder.resolve(DROPINS_FOLDER));
            new DropinProcessor(dropinsFolder, this).processDropinsFolder();
        }
    }


    public Class<? extends RepositoryLayout> layoutType() {
        return this.repositoryLayout.getClass();
    }



    public Stream<PluginDescriptor> obtainPluginDescriptors() {
        if (this.plugins == null) {
            this.plugins = registryFile.lines()
                .map(this::obtainPluginDescriptor)
                .flatMap(Optional::stream)
                .collect(toMap(PluginDescriptor::id, x->x));
        }
        return this.plugins.values().stream();
    }


    public boolean containsPlugin(String plugin) {
        return this.plugins.containsKey(plugin);
    }


    public boolean isEmpty() {
        return this.plugins.isEmpty();
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


    private static Path createFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        return folder;
    }














}

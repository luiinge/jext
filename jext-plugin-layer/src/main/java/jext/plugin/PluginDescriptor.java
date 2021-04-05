package jext.plugin;

import java.lang.module.*;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.*;
import jext.plugin.internal.ManifestAttributes;

public class PluginDescriptor {

    private final String id;
    private final Manifest manifest;
    private final ModuleFinder moduleFinder;
    private final ModuleReference pluginModuleReference;
    private final Set<ModuleReference> dependencyModuleReferences;




    public PluginDescriptor (Manifest manifest, Path pluginPath, List<Path> dependencyPaths)
    throws PluginException {
        this.manifest = manifest;
        this.id = ManifestAttributes.pluginId(manifest);
        var pluginModuleFinder = ModuleFinder.of(pluginPath);
        var dependencyModuleFinder = ModuleFinder.of(dependencyPaths.toArray(Path[]::new));
        this.moduleFinder = ModuleFinder.compose(pluginModuleFinder, dependencyModuleFinder);
        this.pluginModuleReference = pluginModuleFinder.findAll().stream()
            .findAny()
            .orElseThrow(()->new PluginException("Cannot find the module reference for plugin {}", id));
        this.dependencyModuleReferences = dependencyModuleFinder.findAll();
    }



    public String id() {
        return id;
    }


    public String vendor() {
        return ManifestAttributes.pluginVendor(manifest);
    }


    public String name() {
        return ManifestAttributes.pluginName(manifest);
    }


    public String version() {
        return ManifestAttributes.pluginVersion(manifest);
    }


    public Stream<ModuleReference> allModuleReferences() {
        var references = new ArrayList<>(dependencyModuleReferences);
        references.add(0, pluginModuleReference);
        return references.stream();
    }


    public Set<String> allModuleNames() {
        return allModuleReferences()
            .map(ModuleReference::descriptor)
            .map(ModuleDescriptor::name)
            .collect(Collectors.toUnmodifiableSet());
    }


    public ModuleReference pluginModuleReference() {
        return pluginModuleReference;
    }


    public Stream<ModuleReference> dependencyModuleReferences() {
        return dependencyModuleReferences.stream();
    }


    public ModuleLayer buildModuleLayer(
        ModuleLayer parentLayer,
        ClassLoader parentClassLoader
    ) {
        return parentLayer.defineModulesWithOneLoader(
            parentLayer.configuration().resolve(
                this.moduleFinder,
                ModuleFinder.of(),
                allModuleNames()
            ),
            parentClassLoader
        );
    }


    @Override
    public int hashCode() {
        return Objects.hash(id());
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PluginDescriptor)) {
            return false;
        }
        return Objects.equals(id(), ((PluginDescriptor)obj).id());
    }



}
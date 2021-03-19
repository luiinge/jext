package jext.plugin;

import java.io.*;
import java.util.*;
import jext.plugin.internal.PluginLayerModelSerializer;

public class PluginLayerModel {


    public static List<PluginLayerModel> fromInputStream(InputStream inputStream) throws IOException {
        return new PluginLayerModelSerializer().read(inputStream);
    }


    public static List<PluginLayerModel> fromReader(Reader reader) throws IOException {
        return new PluginLayerModelSerializer().read(reader);
    }


    public static PluginLayerModel of(String plugin) {
        return new PluginLayerModel(plugin, null, Set.of());
    }


    public static PluginLayerModel of(String plugin, String extendsPlugin) {
        return new PluginLayerModel(plugin, extendsPlugin, Set.of());
    }


    public static PluginLayerModel of(String plugin, Collection<String> runtimeDependencies) {
        return new PluginLayerModel(plugin, null, Set.copyOf(runtimeDependencies));
    }


    public static PluginLayerModel of(String plugin, String extendsPlugin, Collection<String> runtimeDependencies) {
        return new PluginLayerModel(plugin, extendsPlugin, Set.copyOf(runtimeDependencies));
    }


    private String plugin;
    private String extendsPlugin;
    private Set<String> runtimeDependencies = new HashSet<>();




    private PluginLayerModel (
        String plugin,
        String extendsPlugin,
        Set<String> runtimeDependencies
    ) {
        this.plugin = plugin;
        this.extendsPlugin = extendsPlugin;
        this.runtimeDependencies = runtimeDependencies;
    }


    public String plugin() {
        return plugin;
    }

    public Optional<String> extendsPlugin() {
        return Optional.ofNullable(extendsPlugin);
    }

    public Set<String> runtimeDependencies() {
        return Set.copyOf(runtimeDependencies);
    }



}

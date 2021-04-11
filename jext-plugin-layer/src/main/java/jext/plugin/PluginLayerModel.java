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
        return new PluginLayerModel(plugin, null, List.of());
    }


    public static PluginLayerModel of(String plugin, String extendsPlugin) {
        return new PluginLayerModel(plugin, extendsPlugin, List.of());
    }


    public static PluginLayerModel of(String plugin, Collection<String> runtimeDependencies) {
        return new PluginLayerModel(plugin, null, new ArrayList<>(runtimeDependencies));
    }


    public static PluginLayerModel of(String plugin, String extendsPlugin, Collection<String> runtimeDependencies) {
        return new PluginLayerModel(plugin, extendsPlugin, new ArrayList<>(runtimeDependencies));
    }


    private String plugin;
    private String extendsPlugin;
    private List<String> runtimeDependencies = new ArrayList<>();

    public PluginLayerModel () {}


    public PluginLayerModel (
        String plugin,
        String extendsPlugin,
        List<String> runtimeDependencies
    ) {
        this.plugin = Objects.requireNonNull(plugin);
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

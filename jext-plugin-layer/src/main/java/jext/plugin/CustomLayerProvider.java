/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.plugin;


import jext.ModuleLayerProvider;
import org.slf4j.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class CustomLayerProvider implements ModuleLayerProvider {

    private final ClassLoader parentClassLoader;
    private final Map<PluginDescriptor,ModuleLayer> layers = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final ModuleLayer bootLayer = ModuleLayer.boot();


    CustomLayerProvider(
        PluginWharehouse pluginWharehouse,
        ClassLoader parentClassLoader,
        List<PluginLayerModel> customLayers
    ) {
        this.parentClassLoader = parentClassLoader;
        pluginWharehouse.addListener(this::onPluginChange);

        pluginWharehouse.installPluginsFromRepository(
            customLayers.stream().flatMap(this::pluginsOfLayer).collect(toList())
        );




        pluginWharehouse.obtainPluginDescriptors().forEach(plugin->
            layers.put(plugin,plugin.buildModuleLayer(bootLayer,parentClassLoader))
        );
    }




    @Override
    public Stream<ModuleLayer> layers() {
        return layers.values().stream();
    }


    @Override
    public void addLayerModificationListener(Runnable listener) {
        listeners.add(listener);
    }


    private void onPluginChange(PluginWharehouse.Event event, List<PluginDescriptor> plugins) {
        switch(event) {
            case ADDED:
            case ENABLED:
                plugins.forEach( plugin ->
                                     layers.put(plugin,plugin.buildModuleLayer(bootLayer,parentClassLoader))
                );
                break;
            case REMOVED:
            case DISABLED:
                plugins.forEach(layers::remove);
                break;
            default:
        }
        listeners.forEach(Runnable::run);
    }



    private Stream<String> pluginsOfLayer(PluginLayerModel customLayer) {
        return Stream.concat(
            Stream.concat(
                Stream.of(customLayer.plugin()),
                customLayer.extendsPlugin().stream()
            ),
            customLayer.runtimeDependencies().stream()
        );
    }

}

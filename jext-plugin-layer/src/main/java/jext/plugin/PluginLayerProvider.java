/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.plugin;


import jext.ModuleLayerProvider;
import org.slf4j.*;

import java.util.*;
import java.util.stream.Stream;


public class PluginLayerProvider implements ModuleLayerProvider {

    private final ClassLoader parentClassLoader;
    private final Map<PluginDescriptor,ModuleLayer> layers = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final ModuleLayer bootLayer = ModuleLayer.boot();


    PluginLayerProvider(PluginWharehouse pluginWharehouse, ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        pluginWharehouse.addListener(this::onPluginChange);
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


}

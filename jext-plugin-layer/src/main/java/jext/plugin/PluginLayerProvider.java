/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.plugin;


import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.*;
import jext.ModuleLayerProvider;




public class PluginLayerProvider implements ModuleLayerProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PluginLayerProvider.class);

    private final PluginWharehouse pluginWharehouse;
    private final ClassLoader parentClassLoader;
    private final List<PluginDescriptor> plugins = new ArrayList<>();
    private final Map<PluginDescriptor,ModuleLayer> layers = new HashMap<>();


    PluginLayerProvider(PluginWharehouse pluginWharehouse, ClassLoader parentClassLoader) {
        this.pluginWharehouse = pluginWharehouse;
        this.parentClassLoader = parentClassLoader;
    }


    void discoverPluginModules() {
        pluginWharehouse.obtainPluginDescriptors().forEach(plugin-> {
            plugins.add(plugin);
            layers.put(plugin,plugin.buildModuleLayer(parentClassLoader));
        });
    }


    @Override
    public Stream<ModuleLayer> layers() {
        return layers.values().stream();
    }







}

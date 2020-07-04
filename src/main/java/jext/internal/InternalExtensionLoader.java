/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext.internal;


import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jext.ExtensionLoader;



public class InternalExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalExtensionLoader.class);

    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
        try {
            // dynamically declaration of 'use' directive, otherwise it will cause an error
            InternalExtensionLoader.class.getModule().addUses(type);
            return ServiceLoader.load(type, loader);
        } catch (ServiceConfigurationError e) {
            LOGGER.error("Error loading extension of type {}",type,e);
            return List.of();
        }
    }


    @Override
    public String toString() {
        return "Built-in extension loader";
    }
}

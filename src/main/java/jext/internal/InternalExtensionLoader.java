/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext.internal;


import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jext.ExtensionLoader;



public class InternalExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalExtensionLoader.class);

    private static final Map<Class<?>, Object> globalInstances = new ConcurrentHashMap<>();
    private static final Map<String, Map<Class<?>, Object>> instancesPerSession =
        new ConcurrentHashMap<>();
    private static final Set<Class<?>> withoutMetadata = ConcurrentHashMap.newKeySet();
    private static final Set<Class<?>> externallyManaged = ConcurrentHashMap.newKeySet();

    @Override
    public <T> List<T> load(Class<T> type, List<ClassLoader> classLoaders, String sessionID) {
        return classLoaders.stream()
           .flatMap(classLoader -> load(type,classLoader))
           .filter(this::filterPrototypesWithoutMetadata)
           .filter(this::filterExternallyManaged)
           .map(prototype -> instantiate(prototype, sessionID))
           .filter(Optional::isPresent)
           .map(Optional::get)
           .collect(Collectors.toList());
    }


    @Override
    public void invalidateSession(String sessionID) {
        instancesPerSession.remove(sessionID);
    }


    private boolean filterPrototypesWithoutMetadata(Object prototype) {
        Class<?> prototypeClass = prototype.getClass();
        if (withoutMetadata.contains(prototypeClass)) {
            return false;
        }
        var metadata = prototypeClass.getAnnotation(jext.Extension.class);
        if (metadata == null) {
            LOGGER.debug(
                "Class {} is not annotated with {} so it will be ignored",
                prototypeClass.getCanonicalName(),
                jext.Extension.class.getCanonicalName()
            );
            withoutMetadata.add(prototypeClass);
            return false;
        }
        return true;
    }



    private boolean filterExternallyManaged(Object prototype) {
        Class<?> prototypeClass = prototype.getClass();
        if (externallyManaged.contains(prototypeClass)) {
            return false;
        }
        var metadata = prototypeClass.getAnnotation(jext.Extension.class);
        if (metadata.externallyManaged()) {
            LOGGER.debug(
                "Class {} is externally managed and ignored by the internal extension loader",
                prototypeClass.getCanonicalName()
            );
            externallyManaged.add(prototypeClass);
            return false;
        }
        return true;
    }



    @SuppressWarnings("unchecked")
    private <T> Optional<T> instantiate (T prototype, String sessionID) {

        Class<?> prototypeClass = prototype.getClass();
        var metadata = prototypeClass.getAnnotation(jext.Extension.class);
        T instance = null;
        switch (metadata.scope()) {
            case LOCAL:
                instance = newInstance(prototypeClass);
                break;
            case SESSION:
                instance = (T) instancesPerSession
               .computeIfAbsent(sessionID, x-> new ConcurrentHashMap<>())
               .computeIfAbsent(prototypeClass, x -> newInstance(prototypeClass));
                break;
            case GLOBAL:
                instance = (T) globalInstances
               .computeIfAbsent(prototypeClass, x -> newInstance(prototypeClass));
                break;
            default:
                instance = prototype;
        }
        return Optional.ofNullable(instance);
    }


    @SuppressWarnings("unchecked")
    private <T> T newInstance(Class<?> type) {
        try {
            return (T) type.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.error(
                "Class {} cannot be instantiated, a public constructor with " +
                "zero arguments is required [error was: {}]",
                type.getCanonicalName(),
                e.toString()
            );
            return null;
        }
    }


    private <T> Stream<T> load(Class<T> type, ClassLoader classLoader) {
        try {
            // dynamically declaration of 'use' directive, otherwise it will cause an error
            InternalExtensionLoader.class.getModule().addUses(type);
            return ServiceLoader.load(type, classLoader).stream().map(ServiceLoader.Provider::get);
        } catch (ServiceConfigurationError e) {
            LOGGER.error("Error loading extension of type {}",type,e);
            return Stream.empty();
        }
    }


    @Override
    public String toString() {
        return "Built-in extension loader";
    }


}

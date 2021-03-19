package jext;

import jext.*;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.*;

class InjectionHandler {

    private static final Pattern GENERIC_NAME = Pattern.compile("[^<]+<([^>]+)>");
    private final Logger logger;
    private final ExtensionManager extensionManager;
    private final Map<Class<?>, Map<Class<?>,Object>> instances = new HashMap<>();

    InjectionHandler(ExtensionManager extensionManager, Logger logger) {
        this.extensionManager = extensionManager;
        this.logger = logger;
    }


    public <T,E> E injectExtensions(Class<T> extensionPoint, E extension) {
        addExtensionIfAbsent(extensionPoint, extension);
        Class<?> extensionClass = extension.getClass();
        Stream.<Class<?>>iterate(extensionClass, Objects::nonNull, Class::getSuperclass)
            .map(Class::getDeclaredFields)
            .flatMap(Stream::of)
            .filter(field -> field.isAnnotationPresent(Injected.class))
            .forEach(field -> tryInjectExtensions(extension, field));
        return extension;
    }


    private void addExtensionIfAbsent(Class<?> extensionPoint, Object extension) {
        instances
            .computeIfAbsent(extensionPoint, x->new HashMap<>())
            .putIfAbsent(extension.getClass(), extension);
    }


    private Collection<?> retrieveInjectableExtensions(Class<?> extensionPoint) {
        var extensions = instances.computeIfAbsent(extensionPoint, x->new HashMap<>());
        extensionManager
            .getExtensions(extensionPoint, type -> !extensions.containsKey(type), this)
            .forEach(extension -> extensions.putIfAbsent(extension.getClass(), extension));
        return extensions.values();
    }


    private <E> void tryInjectExtensions(E extension, Field field) {
        try {
            injectExtensionsInField(extension, field);
        } catch (RuntimeException | IllegalAccessException | ClassNotFoundException e) {
            logger.warn(
                "Cannot inject value into {}.{} : {}",
                extension.getClass().getCanonicalName(),
                field.getName(),
                e.getMessage()
            );
            logger.debug("{}", e, e);
            if (e instanceof InaccessibleObjectException) {
                logger.warn(
                    "Consider add the following to your module-info.java file:\n\topens {} to {};\n",
                    extension.getClass().getPackage().getName(),
                    ExtensionManager.class.getModule().getName()
                );
            }
        }
    }


    private <T,E> void injectExtensionsInField(E extension, Field field)
    throws IllegalAccessException, ClassNotFoundException {

        var fieldType = effectiveType(field);
        validateFieldType(fieldType);
        boolean collection = (fieldType != field.getType());
        Collection<?> injectableObjects = retrieveInjectableExtensions(fieldType);
        Object value = null;
        if (!collection) {
            value = injectableObjects.stream()
                .findFirst()
                .orElseThrow(()->nothingToInjectException(fieldType));
        } else if (field.getType() == List.class || field.getType() == Collection.class) {
            value = List.copyOf(injectableObjects);
        } else if (field.getType() == Set.class) {
            value = Set.copyOf(injectableObjects);
        }
        if (!field.canAccess(extension)) {
            field.setAccessible(true);
        }
        field.set(extension, value);
    }


    private void validateFieldType(Class<?> fieldType) {
        if (!fieldType.isAnnotationPresent(ExtensionPoint.class)) {
            throw new IllegalArgumentException(String.format(
                "Type %s is not annotated with %s",
                fieldType.getSimpleName(),
                ExtensionPoint.class.getSimpleName()
            ));
        }
    }


    private Class<?> effectiveType(Field field) throws ClassNotFoundException {
        Class<?> type = field.getType();
        if (type == List.class || type == Set.class || type == Collection.class) {
            var genericTypeMatcher = GENERIC_NAME.matcher(field.getGenericType().getTypeName());
            if (genericTypeMatcher.matches()) {
                type = Class.forName(genericTypeMatcher.group(1));
            } else {
                throw new ClassNotFoundException("Raw use of parametrized type "+type.getSimpleName());
            }
        }
        return type;
    }


    private IllegalArgumentException nothingToInjectException(Class<?> type) {
        return new IllegalArgumentException("There is no extension implementing "+type);
    }

}

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext.internal;


import java.util.List;
import java.util.function.Predicate;

import jext.Extension;
import jext.ExtensionLoader;
import jext.ExtensionPoint;


public class ExtensionLoadContext<T> {

    public static <T> ExtensionLoadContext<T> all(String sessionID, Class<T> extensionPoint) {
        return new ExtensionLoadContext<>(
            sessionID,
            extensionPoint,
            dataOf(extensionPoint),
            selectAll()
        );
    }


    public static <T> ExtensionLoadContext<T> satisfying(
        String sessionID,
        Class<T> extensionPoint,
        Predicate<T> condition
    ) {
        return new ExtensionLoadContext<>(
            sessionID,
            extensionPoint,
            dataOf(extensionPoint),
            condition
        );
    }


    public static <T> ExtensionLoadContext<T> satisfyingData(
        String sessionID,
        Class<T> extensionPoint,
        Predicate<Extension> condition
    ) {
        return new ExtensionLoadContext<>(
            sessionID,
            extensionPoint,
            dataOf(extensionPoint),
            conditionFromAnnotation(condition)
        );
    }


    private final Class<T> extensionPoint;
    private final ExtensionPoint extensionPointData;
    private final Predicate<T> condition;
    private final String sessionID;

    private List<ClassLoader> classLoaders;
    private ExtensionLoader extensionLoader;
    private boolean externallyManaged;


    private ExtensionLoadContext(
        String sessionID,
        Class<T> extensionPoint,
        ExtensionPoint extensionPointData,
        Predicate<T> condition
    ) {
        this.sessionID = sessionID;
        this.extensionPoint = extensionPoint;
        this.extensionPointData = extensionPointData;
        this.condition = condition;

    }





    public ExtensionLoadContext<T> withInternalLoader(
        List<ClassLoader> classLoaders,
        ExtensionLoader extensionLoader
    ) {
        var context = new ExtensionLoadContext<T>(
            sessionID,
            extensionPoint,
            extensionPointData,
            condition
        );
        context.classLoaders = classLoaders;
        context.extensionLoader = extensionLoader;
        context.externallyManaged = false;
        return context;
    }



    public ExtensionLoadContext<T> withExternalLoader(
        List<ClassLoader> classLoaders,
        ExtensionLoader extensionLoader
    ) {
        var context = new ExtensionLoadContext<T>(
            sessionID,
            extensionPoint,
            extensionPointData,
            condition
        );
        context.classLoaders = classLoaders;
        context.extensionLoader = extensionLoader;
        context.externallyManaged = true;
        return context;
    }


    public List<T> load() {
        return extensionLoader.load(extensionPoint, classLoaders, sessionID);
    }


    public Predicate<T> condition() {
        return condition;
    }


    public ExtensionPoint extensionPointData() {
        return extensionPointData;
    }


    public Class<T> extensionPoint() {
        return extensionPoint;
    }


    public boolean isExternallyManaged() {
        return externallyManaged;
    }



    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("[Extensions of type ").append(extensionPoint);
        if (externallyManaged) {
            string.append(" (externally managed) ");
        }
        if (extensionLoader != null) {
            string.append(" loaded by ").append(extensionLoader);
        }
        if (classLoaders != null) {
            string.append(" using class loaders ").append(classLoaders);
        }
        return string.append("]").toString();
    }


    private static <T> Predicate<T> selectAll() {
        return x -> true;
    }


    private static <T> Predicate<T> conditionFromAnnotation(Predicate<Extension> condition) {
        return extension -> condition.test(extension.getClass().getAnnotation(Extension.class));
    }


    private static <T> ExtensionPoint dataOf(Class<T> extensionPoint) {
        ExtensionPoint extensionPointData = extensionPoint.getAnnotation(ExtensionPoint.class);
        if (extensionPointData == null) {
            throw new IllegalArgumentException(
                extensionPoint + " must be annotated with @ExtensionPoint"
            );
        }
        return extensionPointData;
    }

}

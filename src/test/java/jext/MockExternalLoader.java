package jext;

import jext.internal.InternalExtensionLoader;

import java.util.*;
import java.util.stream.*;

public class MockExternalLoader implements ExtensionLoader {

    @Override
    public <T> List<T> load(Class<T> type, List<ClassLoader> classLoaders, String sessionID) {
        return classLoaders.stream().flatMap(classLoader -> {
            try {
                return ServiceLoader.load(type, classLoader).stream().map(ServiceLoader.Provider::get);
            } catch (RuntimeException e) {
                return Stream.empty();
            }
        })
       .filter(extension -> extension.getClass().getAnnotation(Extension.class).externallyManaged())
       .collect(Collectors.toList());
    }


    @Override
    public void invalidateSession(String sessionID) {
        // nothing
    }
}

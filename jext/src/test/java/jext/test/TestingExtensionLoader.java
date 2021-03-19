package jext.test;

import jext.*;

import java.util.*;

public class TestingExtensionLoader implements ExtensionLoader {

    @Override
    public <T> Optional<T> load(ServiceLoader.Provider<T> provider, Scope scope) {
        try {
            System.out.println("using external extension loader");
            return Optional.of(provider.type().getConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }
}

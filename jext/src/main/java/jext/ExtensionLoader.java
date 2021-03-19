package jext;

import java.util.Optional;
import java.util.ServiceLoader.Provider;

public interface ExtensionLoader {

    <T> Optional<T> load (Provider<T> provider, Scope scope);

}

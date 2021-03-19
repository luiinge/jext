/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext;


import static java.util.stream.Collectors.toSet;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.slf4j.*;
import jext.internal.BootLayerProvider;




public class ExtensionManager {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private final ModuleLayerProvider layerProvider;
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private final Set<Class<?>> invalidExtensions = ConcurrentHashMap.newKeySet();
    private final Set<Class<?>> validExtensions = ConcurrentHashMap.newKeySet();


    public ExtensionManager(ModuleLayerProvider layerProvider) {
        this.layerProvider = layerProvider;
    }


    public ExtensionManager() {
        this(new BootLayerProvider());
    }


    public <T> Optional<T> getExtension(Class<T> extensionPoint) {
        return getExtensions(extensionPoint).findFirst();
    }



    public <T> Stream<T> getExtensions(Class<T> extensionPoint) {

        addUseDirective(extensionPoint);
        validateAnnotatedWith(extensionPoint,ExtensionPoint.class);

        Set<Provider<T>> candidates =  layerProvider
            .layers()
            .map(layer -> ServiceLoader.load(layer, extensionPoint))
            .flatMap(ServiceLoader::stream)
            .filter(provider -> validateProvider(provider, extensionPoint))
            .collect(toSet());

        Set<String> overridenExtensions = candidates.stream()
            .map(this::extensionOf)
            .map(Extension::overrides)
            .filter(s->!s.isBlank())
            .collect(toSet());

        return candidates.stream()
            .filter(provider -> !isOverridenByOtherExtension(provider, overridenExtensions))
            .sorted(this::comparePriority)
            .map(this::instantiate)
            .flatMap(Optional::stream);
    }



    public void clear() {
        instances.clear();
        validExtensions.clear();
        invalidExtensions.clear();
    }



    private void addUseDirective(Class<?> type) {
        Module thisModule = ExtensionManager.class.getModule();
        try {
            // dynamically declaration of 'use' directive, otherwise it will cause an error
            thisModule.addUses(type);
        } catch (ServiceConfigurationError e) {
            LOGGER.error(
                "Cannot register 'use' directive of service {} into module {}",
                type,
                thisModule
            );
        }
    }


    private <T> Optional<T> instantiate(Provider<T> provider) {
        var extension = extensionOf(provider);
        ExtensionLoader loader = null;
        // this is the default value, but it is only the interface, not a real implementation
        if (extension.loadedWith() != ExtensionLoader.class) {
            loader = (ExtensionLoader) instances.computeIfAbsent(
                extension.loadedWith(),
                type->newInstance(type).orElse(null)
            );
        }
        if (loader == null) {
            loader = this::load;
        }
        return loader.load(provider,extension.scope());
    }





    @SuppressWarnings("unchecked")
    private <T> Optional<T> load (Provider<T> provider, Scope scope) {
        T prototype = (T) instances.computeIfAbsent(provider.type(), type->provider.get());
        return instantiate(prototype, scope);
    }



    @SuppressWarnings("unchecked")
    private <T> Optional<T> instantiate(T prototype, Scope scope) {
        if (scope == Scope.GLOBAL) {
            return Optional.of(prototype);
        } else if (scope == Scope.LOCAL) {
            return newInstance((Class<T>)prototype.getClass());
        }
        return Optional.empty();
    }



    private <T> Optional<T> newInstance(Class<T> type) {
        try {
            return Optional.of(type.getConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Cannot instantiate class {} : {}", type.getCanonicalName(), e.toString());
            LOGGER.debug("<cause was>", e);
        }
        return Optional.empty();
    }




    @SuppressWarnings("unchecked")
    private <T> boolean validateProvider(Provider<T> provider, Class<T> extensionPoint) {
        Class<T> extension = (Class<T>) provider.type();
        if (validExtensions.contains(extension)) {
            return true;
        }
        if (invalidExtensions.contains(extension)) {
            return false;
        }
        try {
            validateAnnotatedWith(extension, Extension.class);
            var extensionMetadata = extension.getAnnotation(Extension.class);
            var extensionPointMetadata = extensionPoint.getAnnotation(ExtensionPoint.class);
            validateExtensionMetadata(extensionMetadata,extensionPointMetadata);
            validExtensions.add(extension);
            return true;
        } catch (Exception e) {
            LOGGER.warn(
                "Extension {} implementing {} is not valid and it will be ignored",
                extension.getCanonicalName(),
                extensionPoint.getCanonicalName()
            );
            LOGGER.warn("Reason: {}", e.getMessage());
            LOGGER.debug("<cause was>",e);
            invalidExtensions.add(extension);
            return false;
        }
    }



    private void validateExtensionMetadata(
        Extension extensionMetadata,
        ExtensionPoint extensionPointMetadata
    ) throws IllegalArgumentException {
        var implementationVersion = SemanticVersion.of(extensionMetadata.extensionPointVersion());
        var specificationVersion = SemanticVersion.of(extensionPointMetadata.version());
        boolean compatible = specificationVersion.isCompatibleWith(implementationVersion);
        if (!compatible) {
            throw new IllegalArgumentException(String.format(
                "Extension point implementation version %s not compatible with specification version %s",
                implementationVersion,
                specificationVersion
            ));
        }
    }




    private void validateAnnotatedWith(Class<?> type, Class<? extends Annotation> annotation) {
        if (!type.isAnnotationPresent(annotation)) {
            throw new IllegalArgumentException(String.format(
                "Class %s not annotated with %s",
                type.getCanonicalName(),
                annotation.getCanonicalName()
            ));
        }
    }



    private <T> int comparePriority (Provider<T> providerA, Provider<T> providerB) {
        return extensionOf(providerA).priority().compareTo(extensionOf(providerB).priority());
    }



    private <T> boolean isOverridenByOtherExtension(
        Provider<T> provider,
        Set<String> overridenExtensions
    ) {
        var extension = extensionOf(provider);
        var implementationType = provider.type().getCanonicalName();
        return (extension.overridable() && overridenExtensions.contains(implementationType));
    }


    private <T> Extension extensionOf(Provider<T> provider) {
        return provider.type().getAnnotation(Extension.class);
    }

}

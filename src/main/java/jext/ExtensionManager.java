/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jext.internal.ExtensionLoadContext;
import jext.internal.ExtensionVersion;
import jext.internal.InternalExtensionLoader;


/**
 *  Object that provides operations in order to retrieve instances of
 *  classes annotated with {@link Extension}.
 *  <p>
 *  The intended purpose of this class is to be used as a singleton,
 *  but there is no actual constraint about that. Clients can create
 *  as many instances as they required, but being responsible of releasing
 *  references when they are no longer required (see {@link #clear()}).
 */
public class ExtensionManager {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);
    protected static final ExtensionLoader builtInExtensionLoader = new InternalExtensionLoader();

    protected final String sessionID = UUID.randomUUID().toString();
    protected final List<ClassLoader> classLoaders;
    protected final List<ExtensionLoader> extensionLoaders = extensionLoaders();
    protected final Map<Class<?>, Set<Class<?>>> invalidExtensions = new HashMap<>();
    protected final Map<Class<?>, Set<Class<?>>> validExtensions = new HashMap<>();
    protected final Map<Object, Extension> extensionMetadata = new HashMap<>();


    /**
     * Creates a new extension manager using the default class loader of the
     * current thread
     */
    public ExtensionManager() {
        this(Thread.currentThread().getContextClassLoader());
    }


    /**
     * Creates a new extension manager restricted to a specific set of class
     * loaders
     *
     * @param loaders The class loaders used for loading extension classes
     */
    public ExtensionManager(ClassLoader... loaders) {
        this.classLoaders = Arrays.asList(loaders);
    }


    /**
     * Creates a new extension manager restricted to a specific set of class
     * loaders
     *
     * @param loaders The class loaders used for loading extension classes
     */
    public ExtensionManager(Collection<ClassLoader> loaders) {
        this.classLoaders = new ArrayList<>(loaders);
    }


    /**
     * Get the extension annotated metadata for a given extension
     *
     * @param extension A extension instance
     * @return The extension metadata, or <code>null</code> if passed object is
     *         not an extension
     */
    public <T> Extension getExtensionMetadata(T extension) {
        return extensionMetadata.computeIfAbsent(
            extension,
            e -> e.getClass().getAnnotation(Extension.class)
        );
    }


    /**
     * Get all the extension annotated metadata for a given extension point
     *
     * @param extensionPoint A extension point
     * @return The extension metadata, or <code>null</code> if passed object is
     *         not an extension
     */
    public <T> Stream<Extension> getExtensionMetadata(Class<T> extensionPoint) {
        return getExtensions(extensionPoint).map(this::getExtensionMetadata);
    }


    /**
     * Retrieves an instance for the given extension point, if any exists. In the
     * case of existing multiple alternatives, the one with highest priority will
     * be used.
     *
     * @param extensionPoint The extension point type
     * @return An optional object either empty or wrapping the instance
     */
    public <T> Optional<T> getExtension(Class<T> extensionPoint) {
        return loadFirst(ExtensionLoadContext.all(sessionID,extensionPoint));
    }


    /**
     * Retrieves the instance for the given extension point that satisfies the
     * specified condition, if any exists. In the case of existing multiple
     * alternatives, the one with highest priority will be used.
     *
     * @param extensionPoint The extension point type
     * @param condition Only extensions satisfying this condition will be
     * returned
     * @return An optional object either empty or wrapping the instance
     */
    public <T> Optional<T> getExtensionThatSatisfy(
        Class<T> extensionPoint,
        Predicate<T> condition
    ) {
        return loadFirst(ExtensionLoadContext.satisfying(sessionID,extensionPoint, condition));
    }


    /**
     * Retrieves the instance for the given extension point that satisfies the
     * specified condition, if any exists. In the case of existing multiple
     * alternatives, the one with highest priority will be used.
     *
     * @param extensionPoint The extension point type
     * @param condition Only extensions which their metadata satisfies this
     * condition will be returned
     * @return An optional object either empty or wrapping the instance
     */
    public <T> Optional<T> getExtensionThatSatisfyMetadata(
        Class<T> extensionPoint,
        Predicate<Extension> condition
    ) {
        return loadFirst(ExtensionLoadContext.satisfyingData(sessionID,extensionPoint, condition));
    }



    /**
     * Retrieves the instance for the given extension point that satisfies the
     * given provider, name and version, if any exists. The retrieved extension may
     * be a higher but compatible version if exact version is not found.
     * <p>
     * In the case of existing multiple
     * alternatives, the one with highest priority will be used.
     *
     * @param extensionPoint The extension point type
     * @param provider The extension provider
     * @param name The extension name
     * @param version The minimal version
     * @return An optional object either empty or wrapping the instance
     */
    public <T> Optional<T> getExtensionThatSatisfyMetadata(
        Class<T> extensionPoint,
        String provider,
        String name,
        String version
    ) {
        return loadFirst(
            ExtensionLoadContext.satisfyingData(
                sessionID,
                extensionPoint,
                identifier(provider,name,version)
            )
        );
    }





    /**
     * Retrieves a priority-ordered list with all extensions for the given
     * extension point.
     *
     * @param extensionPoint The extension point type
     * @return A list with the extensions, empty if none was found
     */
    public <T> Stream<T> getExtensions(Class<T> extensionPoint) {
        return loadAll(ExtensionLoadContext.all(sessionID,extensionPoint));
    }


    /**
     * Retrieves a priority-ordered list with all then extensions for the given
     * extension point that satisfies the specified condition.
     *
     * @param extensionPoint The extension point type
     * @param condition Only extensions satisfying this condition will be returned
     * @return A list with the extensions, empty if none was found
     */
    public <T> Stream<T> getExtensionsThatSatisfy(Class<T> extensionPoint, Predicate<T> condition) {
        return loadAll(ExtensionLoadContext.satisfying(sessionID,extensionPoint, condition));
    }


    /**
     * Retrieves a priority-ordered list with all then extensions for the given
     * extension point that satisfies the specified condition.
     *
     * @param extensionPoint The extension point type
     * @param condition Only extensions which their metadata satisfies this
     * condition will be returned
     * @return A list with the extensions, empty if none was found
     */
    public <T> Stream<T> getExtensionsThatSatisfyMetadata(
        Class<T> extensionPoint,
        Predicate<Extension> condition
    ) {
        return loadAll(ExtensionLoadContext.satisfyingData(sessionID,extensionPoint, condition));
    }


    /**
     * Creates a new session of the extension manager. Each session
     * will handle extensions marked with the {@link ExtensionScope#SESSION}
     * scope in isolation, returning a singleton instance per session.
     * Other scopes will be treated normally.
     * <p>
     * Internally, each instance of <tt>ExtensionManager</tt> is considered
     * an independent session, so this method is equivalent to:
     * <code>
     *     new ExtensionManager(extensionManager.classLoaders())
     * </code>
     * <p>
     * <b>IMPORTANT:</b> Each session created should
     * invoke the method {@link #clear()} after being used. Otherwise,
     * session extension instances might remain permanently in memory.
     * @return A new extension manager that is isolated from the current
     * in the session scope
     */
    public ExtensionManager newSession() {
        return new ExtensionManager(classLoaders);
    }


    /**
     * Clear any cached or referenced extension instances. This
     * should be the last call prior to discard the manager.
     * <p>
     * If you are using one <tt>ExtensionManager</tt> object as a singleton,
     * usually there is no need to invoke this method. However, it is of major
     * relevance when controlling the lifecycle of several instances.
     * @see #newSession()
     */
    public void clear() {
        validExtensions.clear();
        invalidExtensions.clear();
        extensionMetadata.clear();
        builtInExtensionLoader.invalidateSession(sessionID);
        extensionLoaders.forEach(loader -> loader.invalidateSession(sessionID));
    }


    /**
     * @return An unmodifiable list with the class loaders used by this manager
     */
    public List<ClassLoader> classLoaders() {
        return Collections.unmodifiableList(classLoaders);
    }


    private <T> Stream<T> loadAll(ExtensionLoadContext<T> context) {
        return obtainValidExtensions(context).stream()
            .filter(context.condition())
            .sorted(sortByPriority());         
    }


    private <T> Optional<T> loadFirst(ExtensionLoadContext<T> context) {
        return obtainValidExtensions(context).stream()
            .filter(context.condition())
            .min(sortByPriority());         
    }

    
    

    private <T> List<T> obtainValidExtensions(ExtensionLoadContext<T> context) {

        this.validExtensions.putIfAbsent(context.extensionPoint(), new HashSet<>());
        this.invalidExtensions.putIfAbsent(context.extensionPoint(), new HashSet<>());

        List<T> collectedExtensions = new ArrayList<>();
        collectValidExtensions(
            context.withInternalLoader(classLoaders, builtInExtensionLoader),
            collectedExtensions
        );
        for (ExtensionLoader extensionLoader : extensionLoaders) {
            collectValidExtensions(
                context.withExternalLoader(classLoaders, extensionLoader),
                collectedExtensions
            );
        }
        removeOverridenExtensions(collectedExtensions);
        return collectedExtensions;
    }


    private <T> void collectValidExtensions(
        ExtensionLoadContext<T> context,
        List<T> collectedExtensions
    ) {
        Class<T> extensionPoint = context.extensionPoint();
        LOGGER.debug("{} :: Searching...", context);
        for (T extension : context.load()) {

            if (hasBeenInvalidated(extensionPoint, extension)) {
                LOGGER.debug(
                    "{} :: Found {} but ignored (it is marked as invalid)",
                    context,
                    extension
                );
                continue;
            }

            boolean valid = true;
            if (!hasBeenValidated(extensionPoint, extension)) {
                valid = validateExtension(context, extension);
            }
            if (valid) {
                LOGGER.debug("{} :: Found {}", context, extension);
                collectedExtensions.add(extension);
            } else {
                LOGGER.debug(
                    "{} :: Found {} but ignored (marked as invalid)",
                    context,
                    extension
                );
            }
        }
    }


    private <T> boolean validateExtension(ExtensionLoadContext<T> context, T extension) {

        Class<T> extensionPoint = context.extensionPoint();
        ExtensionPoint extensionPointData = context.extensionPointData();
        Extension extensionData = getExtensionMetadata(extension);

        // this should not happen, but there is no guarantee that external
        // service loaders provides non-externally managed extensions
        if (extensionData.externallyManaged() != context.isExternallyManaged()) {
            LOGGER.debug(
                "Class {} is{} externally managed and the extension loader is{}; ignored",
                extension.getClass(),
                extensionData.externallyManaged() ? "" : " not",
                context.isExternallyManaged() ? "" : " not"
            );
            this.invalidExtensions.get(extensionPoint).add(extension.getClass());
            return false;
        }

        if (!areCompatible(extensionPointData, extensionData)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(
                    "Extension point version of {} ({}) is not compatible with expected version {}",
                    id(extensionData),
                    extensionData.extensionPointVersion(),
                    extensionPointData.version()
                );
            }
            this.invalidExtensions.get(extensionPoint).add(extension.getClass());
            return false;
        }

        this.validExtensions.get(extensionPoint).add(extension.getClass());
        return true;
    }


    private <T> void removeOverridenExtensions(List<T> extensions) {

        List<T> overridableExtensions = extensions.stream()
            .filter(extension -> getExtensionMetadata(extension).overridable())
            .collect(Collectors.toList());

        Map<String, T> overridableExtensionClassNames = overridableExtensions.stream()
            .collect(
                Collectors.toMap(
                    extension -> extension.getClass().getCanonicalName(),
                    Function.identity()
                )
            );

        for (T extension : new ArrayList<>(extensions)) {
            Extension metadata = getExtensionMetadata(extension);
            T overridable = overridableExtensionClassNames.get(metadata.overrides());
            if (overridable != null) {
                extensions.remove(overridable);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(
                        "Extension {} overrides extension {}",
                        id(getExtensionMetadata(extension)),
                        id(getExtensionMetadata(overridable))
                    );
                }
            }
        }
    }


    private boolean areCompatible(ExtensionPoint extensionPointData, Extension extensionData) {
        ExtensionVersion extensionPointVersion = ExtensionVersion.of(extensionPointData.version());
        try {
            ExtensionVersion extensionDataPointVersion = ExtensionVersion.of(
                extensionData.extensionPointVersion()
            );
            return extensionDataPointVersion.isCompatibleWith(extensionPointVersion);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Bad extensionPointVersion in {}", id(extensionData));
            throw e;
        }
    }



    private int getExtensionPriority(Object extension) {
        return getExtensionMetadata(extension).priority();
    }
    

    private <T> boolean hasBeenValidated(Class<T> extensionPoint, T extension) {
        return validExtensions.get(extensionPoint).contains(extension.getClass());
    }


    private <T> boolean hasBeenInvalidated(Class<T> extensionPoint, T extension) {
        return invalidExtensions.get(extensionPoint).contains(extension.getClass());
    }


    private Comparator<Object> sortByPriority() {
        return Comparator.comparingInt(this::getExtensionPriority);
    }


    private static String id(Extension extension) {
        return extension.provider() + ":" + extension.name() + ":" + extension.version();
    }


    private static List<ExtensionLoader> extensionLoaders() {
        List<ExtensionLoader> loaders = new ArrayList<>();
        ServiceLoader.load(ExtensionLoader.class).forEach(loaders::add);
        return loaders;
    }


    private static Predicate<Extension> identifier(String provider, String name, String version) {
        return extension ->
            extension.provider().equalsIgnoreCase(provider) &&
            extension.name().equalsIgnoreCase(name) &&
            ExtensionVersion.of(extension.version()).isCompatibleWith(ExtensionVersion.of(version))
        ;
    }


}

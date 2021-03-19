/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext;


import java.lang.annotation.*;


/**
 * This annotation allows to mark a class as an extension managed by the
 * {@link PluginLayerProvider}.
 * <p>
 * Notice that any class not annotated with {@link Extension} will not be
 * managed in spite of implementing or extending the {@link ExtensionPoint}
 * class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extension {

    /**
     * The class name of the extension point that is extended.
     * <p>
     * If this field is not provided and the extension class implements directly
     * the extension point class, it will automatically infer the value as the
     * qualified name of the extension point class. Notice that, if the extension
     * point class uses generic parameters, the inference mechanism will not
     * work, so clients must provide the name of the class directly in those
     * cases.
     * </p>
     */
    String extensionPoint() default "";


    /**
     * The minimum version of the extension point that is extended in form of
     * {@code <majorVersion>.<minorVersion>} .
     * <p>
     * If an incompatible version is used (that is, the major part of the version
     * is different), the extension manager will emit a warning and prevent the
     * extension from loading.
     * </p>
     */
    String extensionPointVersion() default "1.0";



    /**
     * Priority used when extensions collide, the highest value have priority
     * over others.
     */
    Priority priority() default Priority.NORMAL;


    /**
     * Defines whether or not this extension can be overridden by other extension that
     * extends the implementation.
     */
    boolean overridable() default true;


    /**
     * The qualified class name of another extension that should be replaced by
     * this extension in case both of them are valid alternatives. It only has
     * effect if the {@link #overridable()} property of the extension to override
     * is set to <code>true</code>.
     */
    String overrides() default "";



    Scope scope() default Scope.GLOBAL;



    Class<? extends ExtensionLoader> loadedWith() default ExtensionLoader.class;

}

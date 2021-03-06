/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation allows to mark an interface or abstract class as an
 * extension point managed by the {@link ExtensionManager}.
 * <p>
 * In order to ensure compatibility between the extension point and its
 * extensions, it is important to maintain correctly the {@link #version()}
 * property. If you are intended to break backwards compatibility keeping the
 * same package and type name, increment the major part of the version in
 * order to avoid runtime errors. Otherwise, increment the minor part of the
 * version in order to state the previous methods are still valid.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtensionPoint {

    /**
     * The version of the extension point in form of
     * {@code <majorVersion>.<minorVersion>}
     */
    String version() default "1.0";


}

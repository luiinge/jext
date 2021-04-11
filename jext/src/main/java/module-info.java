/**
 * This modules allows you to define a <i>plug-in</i> architecture detecting the
 * Java classes that implements a specific interface. It mostly relies on the standard
 * extension mechanism provided by Java by means of the {@link java.util.ServiceLoader}
 * class, but it simplifies and enhances the usage.
 * <p>
 * Additional {@link jext.ExtensionLoaderX} instances can be provided if you require
 * alternative discovering methods, for example, a specific IoC manager.
 */
module jext {

    exports jext;


    requires org.slf4j;


}
/**
 * This modules allows you to define a <i>plug-in</i> architecture detecting the
 * Java classes that implements a specific interface. It mostly relies on the standard
 * extension mechanism provided by Java by means of the {@link java.util.ServiceLoader}
 * class, but it simplifies and enhances the usage.
 * <p>
 * Additional {@link jext.plugin.ExtensionLoaderX} instances can be provided if you require
 * alternative discovering methods, for example, a specific IoC manager.
 */
module jext.plugin {

    exports jext.plugin;

    requires jext;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires zip4j;
    requires maven.fetcher;

    opens jext.plugin to com.fasterxml.jackson.databind;
    opens jext.plugin.internal to com.fasterxml.jackson.databind;


}
/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


/**
 * The different strategies that can be used each time an extension is
 * requested using the {@link ExtensionManager}.
 */
public enum LoadStrategy {

    /** Keep a single instance */
    SINGLETON,

    /** Create a new instance each time */
    NEW,

    /** The behaviour is decided by the underline implementation */
    UNDEFINED
}

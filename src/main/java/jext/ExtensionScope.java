/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


/**
 * The different strategies that can be used each time an extension is
 * requested using the {@link ExtensionManager}.
 */
public enum ExtensionScope {

    /** Keep a single instance */
    GLOBAL,

    /** Create a new instance each time */
    LOCAL,

    /** Keep a single instance per session */
    SESSION
}

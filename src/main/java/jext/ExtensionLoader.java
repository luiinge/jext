/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import java.util.List;

/**
 * This interface allows third-party contributors to implement custom
 * mechanisms to retrieve extension instances, instead of using the Java
 * {@link java.util.ServiceLoader} approach.
 * <p>
 * This is specially suited for IoC injection frameworks that may manage
 * object instances in a wide range of different ways.
 */
public interface ExtensionLoader {

    /**
     * Given a expected type and a class loader, retrieves a collection
     * of instances of the type.
     * @param <T> The type of the extension point
     * @param type The type of the extension point
     * @param classLoaders The class loaders to be used
     * @param sessionID The string identifier of the extension manager session
     * @return An list with the retrieved instances. It cannot be null but can be empty.
     */
    <T> List<T> load(Class<T> type, List<ClassLoader> classLoaders, String sessionID);


    /**
     * Invalidate the given session, removing possible stored data from cache.
     * <p>
     * Clients can provide a void implementation if no session-related cache
     * is used.
     * @param sessionID The string identifier of the extension manager session
     */
    void invalidateSession(String sessionID);
}

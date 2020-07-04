/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


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
     * @param loader The class loader to be used
     * @return An iterable with the retrieved instances. It cannot be null but can be empty.
     */
    <T> Iterable<T> load(Class<T> type, ClassLoader loader);
}

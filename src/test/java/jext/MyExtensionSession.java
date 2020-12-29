/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;

@Extension(
    provider = "test",
    name = "session",
    version = "1.0.0",
    scope = ExtensionScope.SESSION
)
public class MyExtensionSession implements MyExtensionPoint {

}
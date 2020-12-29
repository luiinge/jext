/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;

@Extension(
    provider = "test",
    name = "local",
    version = "1.0.0",
    scope = ExtensionScope.LOCAL
)
public class MyExtensionLocal implements MyExtensionPoint {

}
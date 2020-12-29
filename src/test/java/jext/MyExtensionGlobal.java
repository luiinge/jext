/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;

@Extension(
    provider = "test",
    name = "global",
    version = "1.0.0",
    scope = ExtensionScope.GLOBAL
)
public class MyExtensionGlobal implements MyExtensionPoint {

}
/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;

import jext.ExtensionPoint;

@ExtensionPoint(version = "2.5")
public interface MyExtensionPointV2_5 {

    default String value() {
        return getClass().getSimpleName();
    }

}
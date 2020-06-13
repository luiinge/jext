/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;

import jext.ExtensionPoint;
import jext.LoadStrategy;

@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface ExtensionPointFresh {

}
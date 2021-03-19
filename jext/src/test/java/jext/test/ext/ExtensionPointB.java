package jext.test.ext;

import jext.ExtensionPoint;

@ExtensionPoint(version = "2")
public interface ExtensionPointB {

    String provideStuff();

}

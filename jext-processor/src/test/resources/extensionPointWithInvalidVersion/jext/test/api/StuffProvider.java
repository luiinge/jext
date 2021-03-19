package jext.test.api;

import jext.ExtensionPoint;

@ExtensionPoint(version = "A.0")
public interface StuffProvider {

    String provideStuff();

}

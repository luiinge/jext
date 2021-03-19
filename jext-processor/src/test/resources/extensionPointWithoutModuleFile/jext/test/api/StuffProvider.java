package jext.test.api;

import jext.ExtensionPoint;

@ExtensionPoint(version = "2.0")
public interface StuffProvider {

    String provideStuff();

}

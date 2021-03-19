package jext.test.api;

import jext.ExtensionPoint;

@ExtensionPoint
public interface StuffProvider {

    String provideStuff();

}

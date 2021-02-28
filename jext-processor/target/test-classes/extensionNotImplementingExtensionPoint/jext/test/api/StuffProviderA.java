package jext.test.api;

import jext.Extension;

@Extension(extensionPoint = "jext.test.api.StuffProvider")
public class StuffProviderA {

    public String provideStuff() {
        return "Stuff from provider A";
    }

}

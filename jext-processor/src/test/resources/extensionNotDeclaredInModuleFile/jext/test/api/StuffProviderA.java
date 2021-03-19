package jext.test.api;

import jext.Extension;

@Extension
public class StuffProviderA implements StuffProvider {

    public String provideStuff() {
        return "Stuff from provider A";
    }

}

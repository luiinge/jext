package jext.test.api;

import jext.Extension;

@Extension
public class StuffProviderB implements StuffProvider {

    public String provideStuff() {
        return "Stuff from provider B";
    }

}

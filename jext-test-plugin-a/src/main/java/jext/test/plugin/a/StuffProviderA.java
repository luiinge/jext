package jext.test.plugin.a;

import jext.Extension;
import jext.test.api.StuffProvider;

@Extension
public class StuffProviderA implements StuffProvider {

    @Override
    public String provideStuff() {
        return "I'm stuff from plugin A";
    }

}

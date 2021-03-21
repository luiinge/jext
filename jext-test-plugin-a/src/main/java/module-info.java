module jext.test.plugin.a {

    requires jext;
    requires jext.test.api;

    provides jext.test.api.StuffProvider with jext.test.plugin.a.StuffProviderA;


}
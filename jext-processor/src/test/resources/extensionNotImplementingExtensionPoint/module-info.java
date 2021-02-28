module jext.test.api {

    exports jext.test.api;

    requires jext;

    provides jext.test.api.StuffProvider with
        jext.test.api.StuffProviderB;

}
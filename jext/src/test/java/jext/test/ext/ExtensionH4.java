package jext.test.ext;

import jext.*;

@Extension
public class ExtensionH4 implements ExtensionPointH4 {

    @Injected
    private ExtensionPointH3 injectedExtension;

    public ExtensionPointH3 injectedExtension() {
        return injectedExtension;
    }
}

package jext.test.ext;

import jext.*;

@Extension
public class ExtensionH3 implements ExtensionPointH3 {

    @Injected
    private ExtensionPointH4 injectedExtension;

    public ExtensionPointH4 injectedExtension() {
        return injectedExtension;
    }
}

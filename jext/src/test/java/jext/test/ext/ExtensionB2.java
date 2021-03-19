package jext.test.ext;

import jext.Extension;

@Extension(extensionPointVersion = "2")
public class ExtensionB2 implements ExtensionPointB {

    @Override
    public String provideStuff() {
        return "MultipleExtensionB implementing MultipleExtensionPointV2";
    }
}

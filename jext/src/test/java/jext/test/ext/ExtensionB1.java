package jext.test.ext;

import jext.Extension;

@Extension(extensionPointVersion = "2")
public class ExtensionB1 implements ExtensionPointB {

    @Override
    public String provideStuff() {
        return "MultipleExtensionA implementing MultipleExtensionPointV2";
    }
}

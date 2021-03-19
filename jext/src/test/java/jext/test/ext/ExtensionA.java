package jext.test.ext;

import jext.*;

@Extension(extensionPointVersion = "2")
public class ExtensionA implements ExtensionPointA {

    @Override
    public String provideStuff() {
        return "I am MyExtensionA implementing MyExtensionPointV2";
    }
}

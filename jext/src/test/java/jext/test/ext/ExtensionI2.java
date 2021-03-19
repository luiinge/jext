package jext.test.ext;

import jext.*;

import java.util.List;

@Extension
public class ExtensionI2 implements ExtensionPointI {

    @Injected
    private List<ExtensionPointI> injectedList;


    public List<ExtensionPointI> injectedList() {
        return injectedList;
    }

}

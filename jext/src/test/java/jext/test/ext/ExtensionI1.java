package jext.test.ext;

import jext.*;

import java.util.*;

@Extension(priority = Priority.HIGHER)
public class ExtensionI1 implements ExtensionPointI {

    @Injected
    private List<ExtensionPointI> injectedList;


    public List<ExtensionPointI> injectedList() {
        return injectedList;
    }

}

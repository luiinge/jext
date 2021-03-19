package jext.test.ext;

import jext.*;

import java.util.*;

@Extension
public class ExtensionH1 implements ExtensionPointH1 {

    @Injected
    private ExtensionPointH2 injectedExtension;

    @Injected
    private List<ExtensionPointH5> injectedList;

    @Injected
    private Set<ExtensionPointH5> injectedSet;

    @Injected
    private Collection<ExtensionPointH5> injectedCollection;

    @Injected
    private List nonParametrizedList;


    private ExtensionPointH2 nonInjectedExtension;


    public ExtensionPointH2 injectedExtension() {
        return injectedExtension;
    }

    public ExtensionPointH2 nonInjectedExtension() {
        return nonInjectedExtension;
    }

    public List<ExtensionPointH5> injectedList() {
        return injectedList;
    }

    public Set<ExtensionPointH5> injectedSet() {
        return injectedSet;
    }

    public Collection<ExtensionPointH5> injectedCollection() {
        return injectedCollection;
    }

    public List nonParametrizedList() {
        return nonParametrizedList;
    }
}

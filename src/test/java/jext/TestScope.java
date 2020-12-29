/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class TestScope {

    private final ExtensionManager extensionManager = new ExtensionManager();


    @Test
    public void testLocalExtensionAlwaysGetANewInstance() {
        var call1 = getExtension(extensionManager,ExtensionScope.LOCAL).orElseThrow();
        var call2 = getExtension(extensionManager,ExtensionScope.LOCAL).orElseThrow();
        var call3 = getExtension(extensionManager,ExtensionScope.LOCAL).orElseThrow();
        assertThat(call1).isNotSameAs(call2).isNotSameAs(call3);
        assertThat(call2).isNotSameAs(call3);
    }

    @Test
    public void testGlobalExtensionAlwaysGetTheSameInstance() {
        var call1 = getExtension(extensionManager,ExtensionScope.GLOBAL).orElseThrow();
        var call2 = getExtension(extensionManager,ExtensionScope.GLOBAL).orElseThrow();
        var call3 = getExtension(extensionManager,ExtensionScope.GLOBAL).orElseThrow();
        assertThat(call1).isSameAs(call2).isSameAs(call3);
    }

    @Test
    public void testSessionExtensionOnlyGetANewInstanceWithinTheSameSession() {

        var session1 = extensionManager.newSession();
        var call1_1 = getExtension(session1,ExtensionScope.SESSION).orElseThrow();
        var call1_2 = getExtension(session1,ExtensionScope.SESSION).orElseThrow();
        var call1_3 = getExtension(session1,ExtensionScope.SESSION).orElseThrow();
        session1.clear();
        assertThat(call1_1).isSameAs(call1_2).isSameAs(call1_3);

        var session2 = extensionManager.newSession();
        var call2_1 = getExtension(session2,ExtensionScope.SESSION).orElseThrow();
        var call2_2 = getExtension(session2,ExtensionScope.SESSION).orElseThrow();
        var call2_3 = getExtension(session2,ExtensionScope.SESSION).orElseThrow();
        session2.clear();
        assertThat(call2_1).isSameAs(call2_2).isSameAs(call2_3);

        assertThat(call1_1).isNotSameAs(call2_1);
    }






    private Optional<MyExtensionPoint> getExtension(ExtensionManager extMgr, ExtensionScope scope) {
        return extMgr.getExtensionsThatSatisfyMetadata(
            MyExtensionPoint.class,
            metadata->metadata.scope()==scope
        ).findAny();
    }
}

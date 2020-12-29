/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.junit.BeforeClass;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


public class TestExtensionValidations {

    private final ExtensionManager extensionManager = new ExtensionManager();


    @Test
    public void testExtensionWithoutAnnotationCannotBeRetrieved() {
        assertThat(NotAnnotatedExtension.class.getAnnotation(Extension.class)).isNull();
        // repeat twice (testing invalid extension cache)
        for (int i=0; i<=2; i++) {
            assertThat(extensionManager.getExtensions(MyExtensionPoint.class))
                .extracting(Object::getClass)
                .noneMatch(NotAnnotatedExtension.class::equals);
        }
    }

    @Test
    public void testExternallyManagedExtensionCannotBeRetrievedByDefaultLoader() {
        assertThat(
            ExternallyManagedExtension.class.getAnnotation(Extension.class).externallyManaged()
        ).isTrue();
        // repeat twice (testing invalid extension cache)
        for (int i=0; i<=2; i++) {
            assertThat(extensionManager.getExtensions(MyExtensionPoint.class))
                .extracting(Object::getClass)
                .noneMatch(ExternallyManagedExtension.class::equals);
        }
    }


    @Test
    public void testOverridenExtensionCannotBeRetrieved() {
        assertThat(OverridableExtension.class.getAnnotation(Extension.class).overridable())
            .isTrue();
        assertThat(OverriderExtension.class.getAnnotation(Extension.class).overrides())
            .isEqualTo(OverridableExtension.class.getCanonicalName());
        // repeat twice (testing invalid extension cache)
        for (int i=0; i<=2; i++) {
            assertThat(extensionManager.getExtensions(MyExtensionPoint.class))
                .extracting(Object::getClass)
                .noneMatch(OverridableExtension.class::equals)
                .anyMatch(OverriderExtension.class::equals)
            ;
        }
    }
}

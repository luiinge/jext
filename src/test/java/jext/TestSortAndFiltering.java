/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import org.junit.*;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSortAndFiltering {

    private final ExtensionManager extensionManager = new ExtensionManager();

    @BeforeClass
    public static void preconditions() {
        assertThat(MyExtensionV2_5.class)
            .hasAnnotation(Extension.class);
        assertThat(MyExtensionV2_6.class)
            .hasAnnotation(Extension.class);
        assertThat(MyExtensionV2_5.class.getAnnotation(Extension.class).priority())
            .isLessThan(MyExtensionV2_6.class.getAnnotation(Extension.class).priority());
    }

    @Test
    public void testGetExtensionWithHighestPriority() {
        assertThat(extensionManager.getExtension(MyExtensionPointV2_5.class))
            .containsInstanceOf(MyExtensionV2_5.class);
    }


    @Test
    public void testGetExtensionsOrderedByPriority() {
        Stream<Class<?>> extensions = extensionManager.getExtensions(MyExtensionPointV2_5.class)
            .map(Object::getClass);
        assertThat(extensions)
            .containsExactly(MyExtensionV2_5.class, MyExtensionV2_6.class);
    }


    @Test
    public void testGetExtensionByMetadata() {
        var extension = extensionManager.getExtensionThatSatisfyMetadata(
            MyExtensionPoint.class,
            "test",
            "local",
            "1.0.0"
        );
        assertThat(extension).containsInstanceOf(MyExtensionLocal.class);
    }

}

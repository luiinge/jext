/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;


public class TestVersionCompatibility {

    private final ExtensionManager extensionManager = new ExtensionManager();

    @Test
    public void testVersion_1_0_NotCompatibleWithVersion_2_5() {
        assertThat(
            extensionManager.getExtensions(MyExtensionPointV2_5.class)
            .map(extensionManager::getExtensionMetadata)
            .map(Extension::extensionPointVersion)
        )
        .isNotEmpty()
        .noneMatch(
            version -> version.startsWith("1.")
        );
    }


    @Test
    public void testVersion_2_0_NotCompatibleWithVersion_2_5() {
        assertThat(
            extensionManager.getExtensions(MyExtensionPointV2_5.class)
                .map(extensionManager::getExtensionMetadata)
                .map(Extension::extensionPointVersion)
        )
        .isNotEmpty()
        .noneMatch(
            version -> version.startsWith("2.0")
        );
    }


    @Test
    public void testVersion_2_5_AndFurtherCompatibleWithVersion_2_5() {
        assertThat(
            extensionManager.getExtensions(MyExtensionPointV2_5.class)
                .map(extensionManager::getExtensionMetadata)
                .map(Extension::extensionPointVersion)
        )
        .isNotEmpty()
        .allMatch(
            version -> version.startsWith("2.5") || version.startsWith("2.6")
        );
    }


}

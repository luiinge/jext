/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import jext.internal.ExtensionVersion;


public class TestExtensionVersion {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion1() {
        ExtensionVersion.of("1.x.4");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion2() {
        ExtensionVersion.of("1.dog");
    }

    @Test
    public void testValidVersion() {
        assertThat(ExtensionVersion.of("1.2.cat")).isNotNull();
    }


    @Test
    public void testIsCompatible() {
        ExtensionVersion v1_5 =  ExtensionVersion.of("1.5");
        assertThat(v1_5.major()).isEqualTo(1);
        assertThat(v1_5.minor()).isEqualTo(5);
        assertThat(v1_5).hasToString("1.5");
        ExtensionVersion v2_1 =  ExtensionVersion.of("2.1.patch");
        ExtensionVersion v2_5 =  ExtensionVersion.of("2.5");
        assertThat(v1_5.isCompatibleWith(v2_1)).isFalse();
        assertThat(v2_1.isCompatibleWith(v1_5)).isFalse();
        assertThat(v2_1.isCompatibleWith(v2_5)).isFalse();
        assertThat(v2_5.isCompatibleWith(v2_1)).isTrue();
    }

}

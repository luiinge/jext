/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package jext.test;

import jext.*;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class TestSemanticVersion {

    @Test
    public void versionCanHaveOnlyMajorPart() {
        assertThat(SemanticVersion.of("1")).isNotNull();
    }


    @Test
    public void versionCanHaveOnlyMajorAndMinorParts() {
        assertThat(SemanticVersion.of("1.2")).isNotNull();
    }


    @Test
    public void versionCanHaveMajorMinorAndPatchParts() {
        assertThat(SemanticVersion.of("1.2.3")).isNotNull();
    }


    @Test
    public void versionCanHaveTextPacthPart() {
        assertThat(SemanticVersion.of("1.2.cat")).isNotNull();
    }


    @Test(expected = IllegalArgumentException.class)
    public void versionCannotHaveTextMajorPart() {
        SemanticVersion.of("whale.1.1");
    }


    @Test(expected = IllegalArgumentException.class)
    public void versionCannotHaveTextMinorPart() {
        SemanticVersion.of("1.dog.1");
    }


    @Test
    public void versionsAreNotCompatibleIfMajorPartDiffers() {
        SemanticVersion v1_5 =  SemanticVersion.of("1.5");
        SemanticVersion v2_1 =  SemanticVersion.of("2.1.patch");
        assertThat(v1_5.isCompatibleWith(v2_1)).isFalse();
        assertThat(v2_1.isCompatibleWith(v1_5)).isFalse();
    }


    @Test
    public void versionsWithSameMajorPartAreOnlyCompatibleBackwards() {
        SemanticVersion v2_0 =  SemanticVersion.of("2.0");
        SemanticVersion v2_1 =  SemanticVersion.of("2.1.patch");
        assertThat(v2_1.isCompatibleWith(v2_0)).isTrue();
        assertThat(v2_0.isCompatibleWith(v2_1)).isFalse();
    }

}

package jext.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import org.junit.Test;

public class TestPluginLayerFile {


    private static final ClassLoader classLoader = TestPluginLayerFile.class.getClassLoader();

    @Test
    public void parseFile() throws IOException {
        var layers = PluginLayerModel.fromInputStream(classLoader.getResourceAsStream("pluginLayers.json"));
        assertThat(layers).hasSize(3);
        assertThat(layers.get(0).plugin()).isEqualTo("a:p1:1");
        assertThat(layers.get(0).extendsPlugin()).isEmpty();
        assertThat(layers.get(0).runtimeDependencies()).isNotNull().isEmpty();
        assertThat(layers.get(1).plugin()).isEqualTo("a:p2:1");
        assertThat(layers.get(1).extendsPlugin()).isEmpty();
        assertThat(layers.get(1).runtimeDependencies()).isNotNull().isEmpty();
        assertThat(layers.get(2).plugin()).isEqualTo("a:p3:1");
        assertThat(layers.get(2).extendsPlugin()).contains("a:p1:1");
        assertThat(layers.get(2).runtimeDependencies()).containsExactlyInAnyOrder(
            "a:p4:1",
            "a:p5:1"
        );
    }

}
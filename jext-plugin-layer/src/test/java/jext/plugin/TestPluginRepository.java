package jext.plugin;


import java.io.IOException;
import java.nio.file.Path;
import org.junit.Test;

public class TestPluginRepository {


    @Test
    public void regularPluginIsDiscovered() throws IOException {
        var repository = PluginWharehouse.builder().atPath(Path.of("src/test/resources/plugins")).build();
        repository.obtainPluginDescriptors();

    }

}

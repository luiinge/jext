package jext.plugin.internal;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class PluginRegistryFile {

    private final Path registryFile;
    private final Set<String> lines = new HashSet<>();


    public PluginRegistryFile(Path registryFile) throws IOException {
        this.registryFile = registryFile;
        if (Files.exists(registryFile)) {
            this.lines.addAll(Files.readAllLines(registryFile));
        }
    }

    public Stream<String> lines() {
        return this.lines.stream();
    }


    public void add(String line) throws IOException {
        this.lines.add(line);
        write();
    }

    public void add(List<String> lines) throws IOException {
        this.lines.addAll(lines);
        write();
    }


    public void append(PluginRegistryFile other) throws IOException {
        this.lines.addAll(other.lines);
        write();
    }


    private void write() throws IOException {
        Files.write(registryFile, lines, StandardOpenOption.APPEND);
    }

    public void clear() throws IOException {
        this.lines.clear();
        write();
    }
}

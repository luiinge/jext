package jext.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class PluginWharehouseBuilder {

    private Path path;
    private Class<? extends RepositoryLayout> layoutClass;
    private Properties layoutProperties = new Properties();
    private boolean dropinsEnabled;
    private boolean online;

    public PluginWharehouseBuilder atPath(Path path) {
        this.path = path;
        return this;
    }


    public PluginWharehouseBuilder usingLayout(Class<? extends RepositoryLayout> layoutClass) {
        this.layoutClass = Objects.requireNonNull(layoutClass);
        return this;
    }


    public PluginWharehouseBuilder usingLayout(
        Class<? extends RepositoryLayout> layoutClass,
        Properties layoutProperties
    ) {
        this.layoutClass = Objects.requireNonNull(layoutClass);
        this.layoutProperties = Objects.requireNonNull(layoutProperties);
        return this;
    }


    public PluginWharehouseBuilder withDropinsEnabled(boolean dropinsEnabled) {
        this.dropinsEnabled = dropinsEnabled;
        return this;
    }


    public PluginWharehouseBuilder withDropinsEnabled() {
        return withDropinsEnabled(true);
    }


    public PluginWharehouseBuilder withDropinsDisabled() {
        return withDropinsEnabled(false);
    }


    public PluginWharehouseBuilder online(boolean online) {
        this.online = online;
        return this;
    }


    public PluginWharehouseBuilder online() {
        return online(true);
    }


    public PluginWharehouseBuilder offline() {
        return online(false);
    }


    public PluginWharehouse build() {
        try {
            Objects.requireNonNull(path, "Path cannot be null");
            Objects.requireNonNull(layoutClass, "Layout cannot be null");
            return new PluginWharehouse(path, layoutClass, layoutProperties, dropinsEnabled, online);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }
}

package jext.internal;

import java.util.stream.Stream;
import jext.ModuleLayerProvider;

public class BootLayerProvider implements ModuleLayerProvider {

    @Override
    public Stream<ModuleLayer> layers() {
        return Stream.of(ModuleLayer.boot());
    }

    @Override
    public void addLayerModificationListener(Runnable listener) {

    }

}

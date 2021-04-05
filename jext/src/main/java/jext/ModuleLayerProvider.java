package jext;

import java.util.List;
import java.util.stream.Stream;


public interface ModuleLayerProvider {

    Stream<ModuleLayer> layers();

    void addLayerModificationListener(Runnable listener);

}
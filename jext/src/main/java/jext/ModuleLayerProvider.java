package jext;

import java.util.stream.Stream;


public interface ModuleLayerProvider {

    Stream<ModuleLayer> layers();

}
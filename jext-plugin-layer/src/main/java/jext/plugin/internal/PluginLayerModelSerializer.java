package jext.plugin.internal;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jext.plugin.PluginLayerModel;

public class PluginLayerModelSerializer {

   private final ObjectMapper mapper = new ObjectMapper()
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);


    static class PluginLayerModelFile {
        int modelVersion;
        List<PluginLayerModel> pluginLayers;
    }


    @SuppressWarnings("unchecked")
    public List<PluginLayerModel> read(InputStream inputStream) throws IOException {
        return read(mapper.readValue(inputStream, Map.class));
    }


    @SuppressWarnings("unchecked")
    public List<PluginLayerModel> read(Reader reader) throws IOException {
        return read(mapper.readValue(reader, Map.class));
    }


    private List<PluginLayerModel> read(Map<?,Object> map) throws IOException {
        int modelVersion = (Integer)map.getOrDefault("modelVersion",Integer.valueOf(1));
        if (modelVersion == 1) {
            return mapper.convertValue(map, PluginLayerModelFile.class).pluginLayers;
        }
        throw new IOException("model version not implemented: "+modelVersion);
    }
}

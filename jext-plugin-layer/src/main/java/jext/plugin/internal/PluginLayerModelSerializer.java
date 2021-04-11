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


   public static class PluginLayerModelFile {
        int modelVersion;
        List<PluginLayerModel> pluginLayers;
   }


    @SuppressWarnings("unchecked")
    public List<PluginLayerModel> read(InputStream inputStream) throws IOException {
        return mapper.readValue(inputStream, PluginLayerModelFile.class).pluginLayers;
    }


    @SuppressWarnings("unchecked")
    public List<PluginLayerModel> read(Reader reader) throws IOException {
        return mapper.readValue(reader, PluginLayerModelFile.class).pluginLayers;
    }



}

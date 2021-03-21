package jext.plugin.internal;

import java.util.jar.Manifest;

public final class ManifestAttributes {

    public static final String PLUGIN_VENDOR = "Plugin-Vendor";
    public static final String PLUGIN_NAME = "Plugin-Name";
    public static final String PLUGIN_VERSION = "Plugin-Version";
    public static final String PLUGIN_DEPENDENCIES = "Plugin-Dependencies";


    public static String pluginVendor(Manifest manifest) {
        return manifest.getMainAttributes().getValue(PLUGIN_VENDOR);
    }

    public static String pluginName(Manifest manifest) {
        return manifest.getMainAttributes().getValue(PLUGIN_NAME);
    }

    public static String pluginVersion(Manifest manifest) {
        return manifest.getMainAttributes().getValue(PLUGIN_VERSION);
    }

    public static String[] pluginDependencies(Manifest manifest) {
       String dependencies = manifest.getMainAttributes().getValue(PLUGIN_DEPENDENCIES);
       return (dependencies == null || dependencies.isBlank() ?
           new String[0] :
           dependencies.stripLeading().stripTrailing().split(" ")
       );
    }


    public static String pluginId(Manifest manifest) {
        return pluginId(
            pluginVendor(manifest),
            pluginName(manifest),
            pluginVersion(manifest)
        );
    }


    public  static String pluginId(String vendor, String name, String version) {
        return vendor+":"+name+":"+version;
    }


    private ManifestAttributes() { /* avoid instantiation */ }




}

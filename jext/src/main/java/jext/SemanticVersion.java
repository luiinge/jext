/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package jext;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class SemanticVersion {

    public static SemanticVersion of(String version) {
        return new SemanticVersion(version);
    }

    public static boolean validate(String version) {
        try {
            of(version);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    private final int major;
    private final int minor;
    private final String patch;


    private SemanticVersion(String version) {
        var parts = Stream.of(version.split("\\.")).iterator();
        try {
            this.major = Integer.parseInt(parts.next());
            this.minor = parts.hasNext() ? Integer.parseInt(parts.next()) : 0;
            this.patch = parts.hasNext() ? parts.next() : "";
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IllegalArgumentException(
                "Not valid version number " + version + " (" + e.getMessage() + ")"
            );
        }
    }


    public int major() {
        return major;
    }


    public int minor() {
        return minor;
    }


    public String patch() {
        return patch;
    }

    public boolean isCompatibleWith(SemanticVersion otherVersion) {
        return (major == otherVersion.major && minor >= otherVersion.minor);
    }


    @Override
    public String toString() {
        return major + "." + minor;
    }

}
package jext.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public interface RepositoryLayout {

    void configure(Properties properties);

    Optional<String> fetchArtifact(String coordinates);

    void setOnline(boolean online);

    Path path();

    Optional<Path> obtainArtifactFromCoordinates(String coordinates);

    void copyToOtherRepository(RepositoryLayout otherRepository) throws IOException;

    boolean deleteArtifact(String coordinates);
}
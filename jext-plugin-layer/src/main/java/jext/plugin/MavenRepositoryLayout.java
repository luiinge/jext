    package jext.plugin;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

import maven.fetcher.*;
import org.slf4j.*;

public class MavenRepositoryLayout implements RepositoryLayout {


    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRepositoryLayout.class);
    private static final String COORDINATES_REGEX = "([\\w\\.\\-]+):([\\w\\-]+):([\\w\\.\\-]+)";
    private static final Pattern COORDINATES_PATTERN = Pattern.compile(COORDINATES_REGEX);

    private final MavenFetcher mavenFetcher;
    private final Path repositoryFolder;

    private boolean online;


    public MavenRepositoryLayout(Path repositoryFolder) {
        this.repositoryFolder = repositoryFolder;
        this.mavenFetcher = new MavenFetcher()
            .logger(LOGGER)
            .localRepositoryPath(repositoryFolder);
    }


    @Override
    public void configure(Properties properties) {
        this.mavenFetcher.configure(properties);
    }


    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }


    @Override
    public Path path() {
        return repositoryFolder;
    }


    @Override
    public Optional<Path> obtainArtifactFromCoordinates(String coordinates) {
        var matcher = coordinatesToPatternMatcher(coordinates);
        var artifactPath = coordinateMatcherToPath(matcher);
        if (!Files.exists(artifactPath) && online) {
            fetchArtifact(coordinates);
        }
        return Files.exists(artifactPath) ? Optional.of(artifactPath) : Optional.empty();
    }


    @Override
    public void copyToOtherRepository(RepositoryLayout otherRepository) throws IOException {
        try (var walker = Files.walk(repositoryFolder)) {
            walker.forEach(sourcePath -> {
                Path relativeSourcePath = repositoryFolder.relativize(sourcePath);
                Path targetPath = otherRepository.path().resolve(relativeSourcePath);
                try {
                    LOGGER.debug("Copying file {} to {} ...", sourcePath, targetPath);
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.error(
                        "There was a problem copying file {} to repository: {}",
                        targetPath,
                        e.getMessage()
                    );
                    LOGGER.debug("", e);
                }
            });
        }
    }




    private Path coordinateMatcherToPath(Matcher matcher) {
        String vendor = matcher.group(1);
        String id = matcher.group(2);
        String version = matcher.group(3);
        return Stream.of(vendor.split("\\."))
            .map(Path::of)
            .reduce(repositoryFolder,Path::resolve)
            .resolve(id)
            .resolve(version)
            .resolve(id+"-"+version+".jar");
    }


    private Matcher coordinatesToPatternMatcher (String coordinates) {
        var matcher = COORDINATES_PATTERN.matcher(coordinates);
        if (!matcher.matches()) {
            throw new PluginException("Plugin id in registry not valid: {}", coordinates);
        }
        return matcher;
    }


    @Override
    public void fetchArtifact(String coordinates) {
        this.mavenFetcher.fetchArtifacts(new MavenFetchRequest(coordinates));
    }
}

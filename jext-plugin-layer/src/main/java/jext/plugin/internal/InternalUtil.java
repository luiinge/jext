package jext.plugin.internal;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import jext.plugin.*;
import org.slf4j.Logger;

public class InternalUtil {

    private InternalUtil() { }

    public static RepositoryLayout newRepositoryLayout(
        Class<? extends RepositoryLayout> type,
        Path repositoryFolder
    ) {
        try {
            return type.getConstructor(Path.class).newInstance(repositoryFolder);
        } catch (ReflectiveOperationException e) {
            throw new PluginException(e);
        }
    }

    public static String formatList(List<? extends Object> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining("\n\t","\n\t","\n"));
    }


    public static Path createFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        return folder;
    }

    public static Boolean deleteDirectory(Path folder, Logger logger) {
        try (var walker = Files.walk(folder)) {
            return walker
               .sorted(Comparator.reverseOrder())
               .map(Path::toFile)
               .map(File::delete)
               .reduce((x,y)->x&&y)
               .orElse(false);
        } catch (IOException e) {
            logger.error("Problem deleting directory {} : {}", folder, e.getMessage());
            logger.debug(e.toString(), e);
            return false;
        }
    }


    public static void deleteSubdirectories(Path folder, Logger logger) throws IOException {
        try (var walker = Files.walk(folder, 1)) {
            walker.forEach( it -> deleteDirectory(it, logger));
        }
    }
}

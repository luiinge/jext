package jext.plugin.internal;

import java.nio.file.Path;
import jext.plugin.*;

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

}

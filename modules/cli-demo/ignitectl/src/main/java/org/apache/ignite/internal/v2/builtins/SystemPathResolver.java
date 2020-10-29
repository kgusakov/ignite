package org.apache.ignite.internal.v2.builtins;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import org.apache.ignite.internal.v2.IgniteCLIException;

public interface SystemPathResolver {

    /**
     * @return
     */
    String osgGlobalConfigPath();

    String osHomeDirectoryPath();

    String osCurrentDirPath();

    static String osIndependentPath(@NotNull String path, String... others) {
        Path startPath = FileSystems.getDefault().getPath(path);
        for (String p: others) {
            startPath = FileSystems.getDefault().getPath(startPath.toString(), FileSystems.getDefault().getPath(p).toString());
        }
        return startPath.toString();
    }

    /**
     *
     */
    class DefaultPathResolver implements SystemPathResolver {

        private final AppDirs appDirs = AppDirsFactory.getInstance();

        private static final String APP_NAME = "ignite";

        @Override public String osgGlobalConfigPath() {

            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("unix"))
                return "/etc/" + APP_NAME + "/";
            else if (osName.startsWith("windows"))
                // TODO: Support windows path through jna
                return null;
            else if (osName.startsWith("mac os"))
                return "/Library/App \\Support/" + APP_NAME + "/";
            else throw new IgniteCLIException("Unknown OS. Can't detect the appropriate config path");
        }

        @Override public String osHomeDirectoryPath() {
            return System.getProperty("user.home");
        }

        @Override public String osCurrentDirPath() {
            return System.getProperty("user.dir");
        }

    }
}

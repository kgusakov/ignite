package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
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

    static Path pathOf(@NotNull String path) {
        return FileSystems.getDefault().getPath(path);
    }

    static URL[] list(String path) {
        try {
            return Files.list(new File(path).toPath())
                .map(p -> {
                    try {
                        return p.toUri().toURL();
                    }
                    catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    class DefaultPathResolver implements SystemPathResolver {

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

package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Singleton;
import io.micronaut.core.annotation.Introspected;
import org.jetbrains.annotations.NotNull;
import org.apache.ignite.internal.v2.IgniteCLIException;

import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;

public interface SystemPathResolver {

    /**
     * @return
     */
    Path osgGlobalConfigPath();

    Path osHomeDirectoryPath();

    Path osCurrentDirPath();

    static URL[] list(Path path) {
        try {
            return Files.list(path)
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
    @Singleton
    @Introspected
    class DefaultPathResolver implements SystemPathResolver {

        private static final String APP_NAME = "ignite";

        @Override public Path osgGlobalConfigPath() {

            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("unix"))
                return pathOf("/etc/").resolve(APP_NAME);
            else if (osName.startsWith("windows"))
                // TODO: Support windows path through jna
                throw new RuntimeException("Windows support is not implemented yet");
            else if (osName.startsWith("mac os"))
                return pathOf("/Library/App \\Support/").resolve(APP_NAME);
            else throw new IgniteCLIException("Unknown OS. Can't detect the appropriate config path");
        }

        @Override public Path osHomeDirectoryPath() {
            return pathOf(System.getProperty("user.home"));
        }

        @Override public Path osCurrentDirPath() {
            return pathOf(System.getProperty("user.dir"));
        }

    }
}

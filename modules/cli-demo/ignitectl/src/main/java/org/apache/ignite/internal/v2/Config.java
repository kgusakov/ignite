package org.apache.ignite.internal.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;

public class Config {

    public final Path binDir;
    public final Path workDir;

    public Config(String binDir, String workDir) {
        this.binDir = pathOf(binDir);
        this.workDir = pathOf(workDir);
    }

    public Path cliDir(String version) {
        return binDir.resolve(version).resolve("cli");
    }

    public Path libsDir(String version) {
        return binDir.resolve(version).resolve("libs");
    }

    public static Config readConfigFile(File configFile) {
        try (InputStream inputStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            if ((properties.getProperty("bin") == null) || (properties.getProperty("work") == null))
                throw new IgniteCLIException("Config file has wrong format. " +
                    "It must contain correct paths to bin and work dirs");
            return new Config(properties.getProperty("bin"), properties.getProperty("work"));
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't read config file");
        }
    }

    public static Optional<File> searchConfigPath(SystemPathResolver pathResolver) {
        File cfgCurrentDir = pathResolver.osCurrentDirPath().resolve(".ignitecfg").toFile();
        if (cfgCurrentDir.exists())
            return Optional.of(cfgCurrentDir);

        File homeDirCfg = pathResolver.osHomeDirectoryPath().resolve(".ignitecfg").toFile();
        if (homeDirCfg.exists())
            return Optional.of(homeDirCfg);

        File globalDirCfg = pathResolver.osgGlobalConfigPath().resolve("ignite").resolve("ignitecfg").toFile();
        if (globalDirCfg.exists())
            return Optional.of(globalDirCfg);

        return Optional.empty();


    }
}

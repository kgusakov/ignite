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

public class Config {

    public final String binDir;
    public final String workDir;

    public Config(String binDir, String workDir) {
        this.binDir = binDir;
        this.workDir = workDir;
    }

    public Path cliDir(String version) {
        return FileSystems.getDefault().getPath(SystemPathResolver.osIndependentPath(binDir, version, "cli"));
    }

    public Path libsDir(String version) {
        return FileSystems.getDefault().getPath(SystemPathResolver.osIndependentPath(binDir, version, "libs"));
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
        File cfgCurrentDir = new File(SystemPathResolver.osIndependentPath(pathResolver.osCurrentDirPath(), ".ignitecfg"));
        if (cfgCurrentDir.exists())
            return Optional.of(cfgCurrentDir);

        File homeDirCfg = new File(SystemPathResolver.osIndependentPath(pathResolver.osHomeDirectoryPath(), ".ignitecfg"));
        if (homeDirCfg.exists())
            return Optional.of(homeDirCfg);

        File globalDirCfg = new File(SystemPathResolver.osIndependentPath(pathResolver.osgGlobalConfigPath(), "ignite", "ignitecfg"));
        if (globalDirCfg.exists())
            return Optional.of(globalDirCfg);

        return Optional.empty();


    }
}

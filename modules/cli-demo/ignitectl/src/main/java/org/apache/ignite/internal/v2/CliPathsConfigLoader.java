package org.apache.ignite.internal.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;

@Singleton
public class CliPathsConfigLoader {

    private final SystemPathResolver pathResolver;
    private final String version;

    @Inject
    public CliPathsConfigLoader(SystemPathResolver pathResolver,
        CliVersionInfo cliVersionInfo) {
        this.pathResolver = pathResolver;
        this.version = cliVersionInfo.version;
    }

    public Optional<IgnitePaths> loadIgnitePathsConfig() {
        return searchConfigPathsFile(pathResolver)
            .map(f -> CliPathsConfigLoader.readConfigFile(f, version));
    }

    public IgnitePaths loadIgnitePathsOrThrowError() {
        Optional<IgnitePaths> ignitePaths = loadIgnitePathsConfig();
        if (ignitePaths.isPresent())
            return ignitePaths.get();
        else
            throw new IgniteCLIException("To execute node module/node management commands you must run 'init' first");
    }

    public IgnitePaths loadIgnitePathsOrCreate() {
        Optional<IgnitePaths> ignitePaths = loadIgnitePathsConfig();
        if (ignitePaths.isPresent())
            return ignitePaths.get();
        else {

        }
            throw new IgniteCLIException("To execute node module/node management commands you must run 'init' first");
    }

    private  static Optional<File> searchConfigPathsFile(SystemPathResolver pathResolver) {
        File homeDirCfg = pathResolver.osHomeDirectoryPath().resolve(".ignitecfg").toFile();
        if (homeDirCfg.exists())
            return Optional.of(homeDirCfg);

        File globalDirCfg = pathResolver.osgGlobalConfigPath().resolve("ignite").resolve("ignitecfg").toFile();
        if (globalDirCfg.exists())
            return Optional.of(globalDirCfg);

        return Optional.empty();
    }

    private static IgnitePaths readConfigFile(File configFile, String version) {
        try (InputStream inputStream = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            if ((properties.getProperty("bin") == null) || (properties.getProperty("work") == null))
                throw new IgniteCLIException("Config file has wrong format. " +
                    "It must contain correct paths to bin and work dirs");
            return new IgnitePaths(pathOf(properties.getProperty("bin")),
                pathOf(properties.getProperty("work")), version);
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't read config file");
        }
    }
}

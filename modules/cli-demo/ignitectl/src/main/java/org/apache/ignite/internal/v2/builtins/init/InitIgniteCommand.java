package org.apache.ignite.internal.v2.builtins.init;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import javax.inject.Inject;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.CliVersionInfo;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import org.apache.ignite.internal.v2.builtins.module.ModuleManager;
import org.jetbrains.annotations.NotNull;

public class InitIgniteCommand extends AbstractCliCommand {

    private final SystemPathResolver pathResolver;
    private final CliVersionInfo cliVersionInfo;
    private final ModuleManager moduleManager;
    private final CliPathsConfigLoader cliPathsConfigLoader;

    @Inject
    public InitIgniteCommand(SystemPathResolver pathResolver, CliVersionInfo cliVersionInfo,
        ModuleManager moduleManager, CliPathsConfigLoader cliPathsConfigLoader) {
        this.pathResolver = pathResolver;
        this.cliVersionInfo = cliVersionInfo;
        this.moduleManager = moduleManager;
        this.cliPathsConfigLoader = cliPathsConfigLoader;
    }

    public void run() {
        moduleManager.setOut(out);
        Optional<IgnitePaths> ignitePathsOpt = cliPathsConfigLoader.loadIgnitePathsConfig();
        if (!ignitePathsOpt.isPresent()) {
            out.println("Init ignite directories...");
            IgnitePaths ignitePaths = initDirectories();
            out.println("Download and install current ignite version...");
            installIgnite(ignitePaths);
            out.println("Init default Ignite configs");
            initDefaultServerConfigs();
            out.println();
            out.println("Apache Ignite version " + cliVersionInfo.version + " sucessfully installed");
        } else {
            IgnitePaths cfg = ignitePathsOpt.get();
            out.println("Apache Ignite was initialized earlier\n" +
                "Configuration file: " + cliPathsConfigLoader.searchConfigPathsFile().get() + "\n" +
                "Ignite binaries dir: " + cfg.binDir + "\n" +
                "Ignite work dir: " + cfg.workDir);
        }
    }

    private void initDefaultServerConfigs() {
        Path serverCfgFile = cliPathsConfigLoader.loadIgnitePathsOrThrowError().serverDefaultConfigFile();
        try {
            Files.copy(InitIgniteCommand.class.getResourceAsStream("/default-config.xml"), serverCfgFile);
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't create default config file for server");
        }
    }

    private IgnitePaths initDirectories() {
        File cfgFile = initConfigFile();
        out.println("Configuration file initialized: " + cfgFile);
        IgnitePaths cfg = cliPathsConfigLoader.loadIgnitePathsOrThrowError();
        out.println("Ignite binaries dir: " + cfg.binDir);
        out.println("Ignite work dir: " + cfg.workDir);

        File igniteWork = cfg.workDir.toFile();
        if (!(igniteWork.exists() || igniteWork.mkdirs()))
            throw new IgniteCLIException("Can't create working directory: " + cfg.workDir);

        File igniteBin = cfg.libsDir().toFile();
        if (!(igniteBin.exists() || igniteBin.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for ignite modules: " + cfg.libsDir());

        File igniteBinCli = cfg.cliLibsDir().toFile();
        if (!(igniteBinCli.exists() || igniteBinCli.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for cli modules: " + cfg.cliLibsDir());
        
        File serverConfig = cfg.serverConfigDir().toFile();
        if (!(serverConfig.exists() || serverConfig.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for server configs: " + cfg.serverConfigDir());

        return cfg;
    }

    private void installIgnite(IgnitePaths ignitePaths) {
        moduleManager.addModule("_server", ignitePaths, false);
    }

    private File initConfigFile() {
        Path newCfgPath = pathResolver.osHomeDirectoryPath().resolve(".ignitecfg");
        File newCfgFile = newCfgPath.toFile();
        try {
            newCfgFile.createNewFile();
            Path binDir = pathResolver.osCurrentDirPath().resolve("ignite-bin");
            Path workDir = pathResolver.osCurrentDirPath().resolve("ignite-work");
            fillNewConfigFile(newCfgFile, binDir, workDir);
            return newCfgFile;
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't create configuration file in current directory: " + newCfgPath);
        }
    }

    private void fillNewConfigFile(File f, @NotNull Path binDir, @NotNull Path workDir) {
        try (FileWriter fileWriter = new FileWriter(f)) {
            Properties properties = new Properties();
            properties.setProperty("bin", binDir.toString());
            properties.setProperty("work", workDir.toString());
            properties.store(fileWriter, "");
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't write to ignitecfg file");
        }
    }
}

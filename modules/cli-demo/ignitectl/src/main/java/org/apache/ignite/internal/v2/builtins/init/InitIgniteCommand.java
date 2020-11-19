package org.apache.ignite.internal.v2.builtins.init;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
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
        if (!cliPathsConfigLoader.loadIgnitePathsConfig().isPresent()) {
            out.println("Init ignite directories...");
            IgnitePaths ignitePaths = initDirectories();
            out.println("Download and install current ignite version...");
            installIgnite(ignitePaths);
            out.println();
            out.println("Apache Ignite version " + cliVersionInfo.version + " sucessfully installed");
        } else
            out.println("Apache Ignite was initialized earlier");
    }

    private IgnitePaths initDirectories() {
        initConfigFile();
        IgnitePaths cfg = cliPathsConfigLoader.loadIgnitePathsOrThrowError();

        File igniteWork = cfg.workDir.toFile();
        if (!(igniteWork.exists() || igniteWork.mkdirs()))
            throw new IgniteCLIException("Can't create working directory: " + cfg.workDir);

        File igniteBin = cfg.libsDir().toFile();
        if (!(igniteBin.exists() || igniteBin.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for ignite modules: " + cfg.libsDir());

        File igniteBinCli = cfg.cliLibsDir().toFile();
        if (!(igniteBinCli.exists() || igniteBinCli.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for cli modules: " + cfg.cliLibsDir());

        return cfg;
    }

    private void installIgnite(IgnitePaths ignitePaths) {
        moduleManager.addModule("server", ignitePaths, false);
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

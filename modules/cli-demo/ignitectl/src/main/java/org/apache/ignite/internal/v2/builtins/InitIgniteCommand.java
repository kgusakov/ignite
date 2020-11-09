package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import javax.inject.Inject;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.Info;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.module.ModuleManager;
import org.apache.ignite.internal.v2.module.TransferListenerFactory;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(name = "init",
    description = "Init ignite directories and download current version of core artifacts")
public class InitIgniteCommand implements Runnable, IgniteCommand {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    private final SystemPathResolver pathResolver;
    private final Info info;
    private final MavenArtifactResolver mavenArtifactResolver;
    private final TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper;
    private final ModuleManager moduleManager;

    @Override public void run() {
        spec.commandLine().getOut().println("Init ignite directories...");
        Config config = initDirectories();
        spec.commandLine().getOut().println("Installing ignite core...");
        installIgnite(config);
        spec.commandLine().getOut().println("Download current ignite version...");
        spec.commandLine().getOut().println("Apache Ignite version " + info.version + " sucessfully installed");
    }

    @Inject
    public InitIgniteCommand(SystemPathResolver pathResolver, Info info,
        MavenArtifactResolver mavenArtifactResolver,
        TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper,
        ModuleManager moduleManager) {
        this.pathResolver = pathResolver;
        this.info = info;
        this.mavenArtifactResolver = mavenArtifactResolver;
        this.transferEventListenerWrapper = transferEventListenerWrapper;
        this.moduleManager = moduleManager;
    }

    private Config initDirectories() {
        File configFile = initConfigFile();
        Config cfg = Config.readConfigFile(configFile);

        File igniteWork = cfg.workDir.toFile();
        if (!(igniteWork.exists() || igniteWork.mkdirs()))
            throw new IgniteCLIException("Can't create working directory: " + cfg.workDir);


        File igniteBin =  cfg.libsDir(info.version).toFile();
        if (!(igniteBin.exists() || igniteBin.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for ignite modules: " + cfg.libsDir(info.version));

        File igniteBinCli = cfg.cliDir(info.version).toFile();
        if (!(igniteBinCli.exists() || igniteBinCli.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for cli modules: " + cfg.cliDir(info.version));

        return cfg;

    }

    private void installIgnite(Config config) {
        moduleManager.addModule("server", config, transferEventListenerWrapper.produceListener(spec.commandLine().getOut()));
    }

    private File initConfigFile() {
        Optional<File> configFile = Config.searchConfigPath(pathResolver);
        if (!configFile.isPresent()) {
            Path newCfgPath = pathResolver.osCurrentDirPath().resolve(".ignitecfg");
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
        else
            return configFile.get();
    }

    private void fillNewConfigFile(File f, @NotNull  Path binDir, @NotNull Path workDir) {
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

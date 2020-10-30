package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Info;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.IgniteCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "install",
    description = "Init ignite directories and download current version")
public class InitIgniteCommand implements Runnable, IgniteCommand {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    private final SystemPathResolver pathResolver;
    private final Info info;

    @Override public void run() {
        spec.commandLine().getOut().println("Init ignite directories...");
        Dirs dirs = initDirectories();
        spec.commandLine().getOut().println("Installing ignite core...");
        installIgnite(SystemPathResolver.osIndependentPath(dirs.binDir, info.version, "libs"));
        spec.commandLine().getOut().println("Download current ignite version...");
        spec.commandLine().getOut().println("Apache Ignite version " + info.version + " sucessfully installed");
    }

    public InitIgniteCommand() {
        pathResolver = new SystemPathResolver.DefaultPathResolver();
        info = new Info();
    }

    public InitIgniteCommand(SystemPathResolver pathResolver, Info info) {
        this.pathResolver = pathResolver;
        this.info = info;
    }

    private Dirs initDirectories() {
        File configFile = initConfigFile();
        Dirs dirs = readConfigFile(configFile);

        File igniteWork = new File(dirs.workDir);
        if (!(igniteWork.exists() || igniteWork.mkdirs()))
            throw new IgniteCLIException("Can't create working directory: " + dirs.workDir);


        String igniteBinPath = SystemPathResolver.osIndependentPath(dirs.binDir, info.version, "libs");
        File igniteBin = new File(igniteBinPath);
        if (!(igniteBin.exists() || igniteBin.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for ignite modules: " + igniteBinPath);

        String igniteBinCliPath = SystemPathResolver.osIndependentPath(dirs.binDir, info.version, "cli");
        File igniteBinCli = new File(igniteBinCliPath);
        if (!(igniteBinCli.exists() || igniteBinCli.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for cli modules: " + igniteBinCliPath);

        return dirs;

    }

    private void installIgnite(String path) {
        try {
            new MavenArtifactResolver(pathResolver).resolve(Paths.get(path), info.groupId, info.artifactId, info.version);
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't download core ignite artifact", e);
        }
    }

    private File initConfigFile() {
        Optional<File> configFile = searchConfigPath();
        if (!configFile.isPresent()) {
            String newCfgPath = SystemPathResolver.osIndependentPath(pathResolver.osCurrentDirPath(), ".ignitecfg");
            File newCfgFile = new File(newCfgPath);
            try {
                newCfgFile.createNewFile();
                String binDir = SystemPathResolver.osIndependentPath(pathResolver.osCurrentDirPath(), "ignite-bin");
                String workDir = SystemPathResolver.osIndependentPath(pathResolver.osCurrentDirPath(), "ignite-work");
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

    private Dirs readConfigFile(File configFile) {
        try {
            List<String> lines = Files.readAllLines(configFile.toPath());
            if (lines.size() != 2)
                throw new IgniteCLIException("Config file has wrong format. " +
                    "It must contain correct paths to bin and word dirs, newline separated");
            return new Dirs(lines.get(0), lines.get(1));
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't read config file");
        }
    }

    private void fillNewConfigFile(File f, String binDir, String workDir) {
        try {
            Files.write(f.toPath(), Arrays.asList(binDir, workDir));
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't write to ignitecfg file");
        }
    }

    private Optional<File> searchConfigPath() {
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

    private static class Dirs {
        public final String binDir;
        public final String workDir;

        public Dirs(String binDir, String workDir) {
            this.binDir = binDir;
            this.workDir = workDir;
        }
    }

}

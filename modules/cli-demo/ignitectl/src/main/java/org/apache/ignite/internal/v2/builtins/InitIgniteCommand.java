package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Const;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.IgniteCommand;
import org.apache.ignite.internal.v2.IgniteCtl;
import picocli.CommandLine;

import static org.apache.ignite.internal.v2.Const.VERSION;

@CommandLine.Command(name = "install",
    description = "Init ignite directories and download current version")
public class InitIgniteCommand implements Runnable, IgniteCommand {

    @CommandLine.ParentCommand
    private IgniteCtl parent;

    private final SystemPathResolver pathResolver;

    @Override public void run() {
        parent.out.println("Init ignite directories...");
        Dirs dirs = initDirectories();
        installIgnite(osIndependentPath(dirs.binDir, VERSION, "libs"));
        parent.out.println("Download current ignite version...");
        parent.out.println("Apache Ignite version " + VERSION + " sucessfully installed");
    }

    public InitIgniteCommand() {
        pathResolver = new SystemPathResolver.DefaultPathResolver();
    }

    public InitIgniteCommand(SystemPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    private Dirs initDirectories() {
        File configFile = initConfigFile();
        Dirs dirs = readConfigFile(configFile);

        File igniteWork = new File(dirs.workDir);
        if (!(igniteWork.exists() || igniteWork.mkdirs()))
            throw new IgniteCLIException("Can't create working directory: " + dirs.workDir);


        String igniteBinPath = osIndependentPath(dirs.binDir, VERSION, "libs");
        File igniteBin = new File(igniteBinPath);
        if (!(igniteBin.exists() || igniteBin.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for ignite modules: " + igniteBinPath);

        String igniteBinCliPath = osIndependentPath(dirs.binDir, VERSION, "cli");
        File igniteBinCli = new File(igniteBinCliPath);
        if (!(igniteBinCli.exists() || igniteBinCli.mkdirs()))
            throw new IgniteCLIException("Can't create a directory for cli modules: " + igniteBinCliPath);

        return dirs;

    }

    private void installIgnite(String path) {
        try {
            new MavenArtifactResolver().resolve(Paths.get(path), Const.IGNITE_GROUP_ID, Const.IGNITE_ARTIFACT_ID, VERSION);
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't download core ignite artifact", e);
        }
    }

    private File initConfigFile() {
        Optional<File> configFile = searchConfigPath();
        if (!configFile.isPresent()) {
            String newCfgPath = osIndependentPath(pathResolver.osCurrentDirPath(), ".ignitecfg");
            File newCfgFile = new File(newCfgPath);
            try {
                newCfgFile.createNewFile();
                String binDir = osIndependentPath(pathResolver.osCurrentDirPath(), "ignite-bin");
                String workDir = osIndependentPath(pathResolver.osCurrentDirPath(), "ignite-work");
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
        File cfgCurrentDir = new File(osIndependentPath(pathResolver.osCurrentDirPath(), ".ignitecfg"));
        if (cfgCurrentDir.exists())
            return Optional.of(cfgCurrentDir);

        File homeDirCfg = new File(osIndependentPath(pathResolver.osHomeDirectoryPath(), ".ignitecfg"));
        if (homeDirCfg.exists())
            return Optional.of(homeDirCfg);

        File globalDirCfg = new File(osIndependentPath(pathResolver.osgGlobalConfigPath(), "ignite", "ignitecfg"));
        if (globalDirCfg.exists())
            return Optional.of(globalDirCfg);

        return Optional.empty();

        
    }

    private static String osIndependentPath(@NotNull String path, String... others) {
        Path startPath = FileSystems.getDefault().getPath(path);
        for (String p: others) {
            startPath = FileSystems.getDefault().getPath(startPath.toString(), FileSystems.getDefault().getPath(p).toString());
        }
        return startPath.toString();
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

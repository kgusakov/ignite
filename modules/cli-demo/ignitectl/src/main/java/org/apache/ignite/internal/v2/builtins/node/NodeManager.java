package org.apache.ignite.internal.v2.builtins.node;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import org.apache.ignite.internal.v2.builtins.module.ModuleStorage;

@Singleton
public class NodeManager {

    public static final String MAIN_CLASS = "org.apache.ignite.startup.cmdline.CommandLineStartup";

    private final Info info;
    private final ModuleStorage moduleStorage;

    @Inject
    public NodeManager(
        Info info, ModuleStorage moduleStorage) {
        this.info = info;
        this.moduleStorage = moduleStorage;
    }

    public long start(String consistentId, Config config) {
        try {
            Path logFile = config.workDir.resolve(consistentId + ".log");
            if (Files.exists(logFile))
                Files.delete(logFile);

            Files.createFile(logFile);

            ProcessBuilder pb = new ProcessBuilder("java",
                "-DIGNITE_OVERRIDE_CONSISTENT_ID=" + consistentId,
                "-cp", classpath(config.libsDir(info.version)),
                MAIN_CLASS, "config/default-config.xml"
            )
                .redirectError(logFile.toFile())
                .redirectOutput(logFile.toFile());
            Process p = pb.start();
            createPidFile(consistentId, p.pid(), config);
            return p.pid();
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't load classpath", e);
        }
    }

    public String classpath(Path dir) throws IOException {
        return moduleStorage.listInstalled().modules.stream()
            .flatMap(m -> m.artifacts.stream())
            .map(m -> m.toAbsolutePath().toString())
            .collect(Collectors.joining(":"));
    }

    public void createPidFile(String consistentId, long pid, Config config) {
        Path dir = config.cliPidsDir();
        if (!Files.exists(dir)) {
            if (!dir.toFile().mkdirs())
                throw new IgniteCLIException("Can't create directory for storing the process pids: " + dir);
        }

        Path pidPath = dir.resolve(consistentId + "_" + System.currentTimeMillis() + ".pid");

        try (FileWriter fileWriter = new FileWriter(pidPath.toFile())) {
            fileWriter.write(String.valueOf(pid));
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't write pid file " + pidPath);
        }
    }

    public List<RunningNode> getRunningNodes(Config config) {
        Path dir = config.cliPidsDir();
        if (Files.exists(dir)) {
            try (Stream<Path> files = Files.find(dir, 1, (f, attrs) ->  f.getFileName().toString().endsWith(".pid"))) {
                    return files
                    .map(f -> {
                        long pid = 0;
                        try {
                            pid = Long.parseLong(Files.readAllLines(f).get(0));
                        }
                        catch (IOException e) {
                            throw new IgniteCLIException("Can't parse pid file " + f);
                        }
                        String filename = f.getFileName().toString();
                        if (filename.lastIndexOf("_") == -1)
                            return Optional.<RunningNode>empty();
                        else {
                            String consistentId = filename.substring(0, filename.lastIndexOf("_"));
                            return Optional.of(new RunningNode(pid, consistentId));
                        }

                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());
            }
            catch (IOException e) {
                throw new IgniteCLIException("Can't find directory with pid files for running nodes " + dir);
            }
        }
        else
            return Collections.emptyList();
    }

    public boolean stopWait(String consistentId, Config config) {
        Path dir = config.cliPidsDir();
        if (Files.exists(dir)) {
            try(Stream<Path> files = Files.find(dir, 1, (f, attrs) -> f.getFileName().toString().startsWith(consistentId))) {
                // TODO: dirty way to handle the problem
                return files.map(f -> {
                    try {
                        long pid = Long.parseLong(Files.readAllLines(f).get(0));
                        boolean result = stopWait(pid);
                        Files.delete(f);
                        return result;
                    }
                    catch (IOException e) {
                        throw new IgniteCLIException("Can't read pid file " + f);
                    }
                }).reduce((a, b) -> a && b).orElse(false);
            }
            catch (IOException e) {
                throw new IgniteCLIException("Can't open directory with pid files " + dir);
            }
        }
        else
            return false;
    }

    private boolean stopWait(long pid) {
        return ProcessHandle
            .of(pid)
            .map(ProcessHandle::destroy)
            .orElse(false);
    }

    public static class RunningNode {

        public final long pid;
        public final String consistentId;

        public RunningNode(long pid, String consistentId) {
            this.pid = pid;
            this.consistentId = consistentId;
        }
    }
}

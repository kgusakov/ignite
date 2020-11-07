/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import picocli.CommandLine;

import static org.apache.ignite.internal.v2.builtins.NodeCommand.NodeManager.MAIN_CLASS;
import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;

@CommandLine.Command(name = "node",
    description = "Node actions", subcommands = {
        NodeCommand.StartNodeCommand.class,
        NodeCommand.StopNodeCommand.class,
    NodeCommand.ListNodesCommand.class})
public class NodeCommand implements IgniteCommand, Runnable {

    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "start", description = "Start Ignite node")
    public static class StartNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        private final Info info;
        private final SystemPathResolver pathResolver;

        @Inject
        public StartNodeCommand(Info info, SystemPathResolver pathResolver) {
            this.info = info;
            this.pathResolver = pathResolver;
        }

        @CommandLine.Parameters(paramLabel = "consistent-id", description = "ConsistentId for new node")
        public String consistentId;

        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            Config config = Config.readConfigFile(configFile.get());
            long pid = NodeManager.start(config.libsDir(this.info.version), config.workDir, consistentId);

            spec.commandLine().getOut().println("Started ignite node with pid " + pid);
        }
    }

    @CommandLine.Command(name = "stop", description = "Stop Ignite node by consistentId")
    public static class StopNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Parameters(paramLabel = "pids", description = "pid of nodes to start")
        public List<Long> pids;

        @Override public void run() {
            pids.forEach(p -> {
                if (NodeManager.stopWait(p))
                    spec.commandLine().getOut().println("Node with pid " + p + " was stopped");
                else
                    spec.commandLine().getOut().println("Stop of node " + p + " was failed");
            });

        }
    }

    @CommandLine.Command(name = "list", description = "List current ignite nodes")
    public static class ListNodesCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Override public void run() {
            String pids = ProcessHandle.allProcesses().filter(
                p -> p
                    .info()
                    .arguments()
                    .map(args -> Arrays.stream(args).anyMatch(arg -> arg.contains(MAIN_CLASS)))
                    .orElse(false)
            ).map(ProcessHandle::pid)
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
            spec.commandLine().getOut().println(pids);

        }
    }

    public static class NodeManager {

        public static final String MAIN_CLASS = "org.apache.ignite.startup.cmdline.CommandLineStartup";

        public static long start(Path srvDir, Path workDir, String consistentId) {
            try {
                Path logFile = workDir.resolve(consistentId + ".log");
                if (Files.exists(logFile)) Files.delete(logFile);

                Files.createFile(logFile);

                ProcessBuilder pb = new ProcessBuilder("java",
                    "-DIGNITE_OVERRIDE_CONSISTENT_ID=" + consistentId,
                    "-cp", classpath(srvDir),
                    MAIN_CLASS, "config/default-config.xml"
                    )
                    .redirectError(logFile.toFile())
                    .redirectOutput(logFile.toFile());
                Process p = pb.start();
                return p.pid();
            }
            catch (IOException e) {
                throw new IgniteCLIException("Can't load classpath", e);
            }
        }

        public static String classpath(Path dir) throws IOException {
            long start = System.currentTimeMillis();
            String result = Files.walk(dir.toAbsolutePath())
                .filter(f -> f.toString().endsWith(".jar"))
                .map(f -> f.toAbsolutePath().toString())
                .collect(Collectors.joining(":"));
            System.out.println(System.currentTimeMillis() - start);
            return result;
        }

        public static boolean stopWait(long pid) {
            return ProcessHandle
                .of(pid)
                .map(ProcessHandle::destroy)
                .orElse(false);
        }
    }
}

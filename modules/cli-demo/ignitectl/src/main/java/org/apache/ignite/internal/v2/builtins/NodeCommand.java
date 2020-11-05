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
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import org.w3c.dom.Node;
import picocli.CommandLine;
import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;
import static java.lang.System.out;

@CommandLine.Command(name = "node",
    description = "Node actions", subcommands = {NodeCommand.StartNodeCommand.class, NodeCommand.StopNodeCommand.class})
public class NodeCommand implements IgniteCommand, Runnable {

    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "start", description = "Start Ignite node")
    public static class StartNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Parameters(paramLabel = "consistent-id", description = "ConsistentId for new node")
        public String consistentId;

        private final SystemPathResolver pathResolver;

        public StartNodeCommand() {
            pathResolver = new SystemPathResolver.DefaultPathResolver();
        }

        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            Config config = Config.readConfigFile(configFile.get());
            long pid = NodeManager.start(config.libsDir(new Info().version), consistentId);

            spec.commandLine().getOut().println("Started ignite node with pid " + pid);
        }
    }

    @CommandLine.Command(name = "stop", description = "Stop Ignite node by consistentId")
    public static class StopNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Parameters(paramLabel = "pid", description = "pid of node")
        public long pid;

        @Override public void run() {
            if (NodeManager.stopWait(pid))
                spec.commandLine().getOut().println("Node with pid " + pid + " was stopped");
            else
                spec.commandLine().getOut().println("Stop was failed");
        }
    }

    public static class NodeManager {

        private static final String MAIN_CLASS = "org.apache.ignite.startup.cmdline.CommandLineStartup";

        public static long start(Path srvDir, String consistentId) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java",
                    "-DIGNITE_OVERRIDE_CONSISTENT_ID=" + consistentId,
                    "-cp", classpath(srvDir),
                    MAIN_CLASS, "config/default-config.xml"
                    ).inheritIO();
                Process p = pb.start();
                return p.pid();
            }
            catch (IOException e) {
                throw new IgniteCLIException("Can't load classpath", e);
            }
        }

        public static String classpath(Path dir) throws IOException {
            return Files.walk(dir.toAbsolutePath())
                .filter(f -> f.toString().endsWith(".jar"))
                .map(f -> f.toAbsolutePath().toString())
                .collect(Collectors.joining(":"));
        }

        public static boolean stopWait(long pid) {
            return ProcessHandle
                .of(pid)
                .map(ProcessHandle::destroy)
                .orElse(false);
        }
    }
}

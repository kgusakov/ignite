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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import org.apache.ignite.internal.v2.builtins.node.NodeManager;
import picocli.CommandLine;

import static org.apache.ignite.internal.v2.builtins.node.NodeManager.MAIN_CLASS;

@CommandLine.Command(name = "node",
    description = "Node actions", subcommands = {
        NodeCommand.StartNodeCommand.class,
        NodeCommand.StopNodeCommand.class,
    NodeCommand.ListNodesCommand.class})
public class NodeCommand implements Runnable {

    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "start", description = "Start Ignite node")
    public static class StartNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        private final Info info;
        private final SystemPathResolver pathResolver;
        private final NodeManager nodeManager;

        @Inject
        public StartNodeCommand(Info info, SystemPathResolver pathResolver, NodeManager nodeManager) {
            this.info = info;
            this.pathResolver = pathResolver;
            this.nodeManager = nodeManager;
        }

        @CommandLine.Parameters(paramLabel = "consistent-id", description = "ConsistentId for new node")
        public String consistentId;

        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            Config config = Config.readConfigFile(configFile.get());
            long pid = nodeManager.start(consistentId, config);

            spec.commandLine().getOut().println("Started ignite node with pid " + pid);
        }
    }

    @CommandLine.Command(name = "stop", description = "Stop Ignite node by consistentId")
    public static class StopNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        private final NodeManager nodeManager;
        private final SystemPathResolver pathResolver;

        @Inject
        public StopNodeCommand(
            NodeManager nodeManager, SystemPathResolver resolver) {
            this.nodeManager = nodeManager;
            pathResolver = resolver;
        }

        @CommandLine.Parameters(paramLabel = "consistend-ids", description = "consistent ids of nodes to start")
        public List<String> pids;

        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            pids.forEach(p -> {
                if (nodeManager.stopWait(p, Config.readConfigFile(configFile.get())))
                    spec.commandLine().getOut().println("Node with consistent id " + p + " was stopped");
                else
                    spec.commandLine().getOut().println("Stop of node " + p + " was failed");
            });

        }
    }

    @CommandLine.Command(name = "list", description = "List current ignite nodes")
    public static class ListNodesCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        private final NodeManager nodeManager;
        private final SystemPathResolver pathResolver;

        @Inject
        public ListNodesCommand(NodeManager nodeManager,
            SystemPathResolver resolver) {
            this.nodeManager = nodeManager;
            pathResolver = resolver;
        }

        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");

            String pids = nodeManager.getRunningNodes(Config.readConfigFile(configFile.get())).stream()
                .map(rn -> rn.pid + "\t" + rn.consistentId)
                .collect(Collectors.joining("\n"));

            spec.commandLine().getOut().println(pids);

        }
    }

}

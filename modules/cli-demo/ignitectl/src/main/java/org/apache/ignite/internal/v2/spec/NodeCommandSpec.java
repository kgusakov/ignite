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

package org.apache.ignite.internal.v2.spec;

import java.util.List;
import javax.inject.Inject;
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.internal.v2.builtins.node.ListNodesCommand;
import org.apache.ignite.internal.v2.builtins.node.NodesClasspathCommand;
import org.apache.ignite.internal.v2.builtins.node.StartNodeCommand;
import org.apache.ignite.internal.v2.builtins.node.StopNodeCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "node",
    description = "start|stop|list local nodes", subcommands = {
        NodeCommandSpec.StartNodeCommandSpec.class,
        NodeCommandSpec.StopNodeCommandSpec.class,
        NodeCommandSpec.NodesClasspathCommandSpec.class,
        NodeCommandSpec.ListNodesCommandSpec.class})
public class NodeCommandSpec implements Runnable {

    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;


    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "start", description = "Start Ignite node")
    public static class StartNodeCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Inject
        ApplicationContext applicationContext;


        @CommandLine.Parameters(paramLabel = "consistent-id", description = "ConsistentId for new node")
        public String consistentId;

        @Override public void run() {
            StartNodeCommand startNodeCommand = applicationContext.createBean(StartNodeCommand.class);

            startNodeCommand.setOut(spec.commandLine().getOut());
            startNodeCommand.start(consistentId);
        }
    }

    @CommandLine.Command(name = "stop", description = "Stop Ignite node by consistentId")
    public static class StopNodeCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Inject
        private ApplicationContext applicationContext;

        @CommandLine.Parameters(paramLabel = "consistent-ids", description = "consistent ids of nodes to start")
        public List<String> pids;

        @Override public void run() {
            StopNodeCommand stopNodeCommand = applicationContext.createBean(StopNodeCommand.class);
            stopNodeCommand.setOut(spec.commandLine().getOut());
            stopNodeCommand.run(pids);

        }
    }

    @CommandLine.Command(name = "list", description = "List current ignite nodes")
    public static class ListNodesCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Inject
        private ApplicationContext applicationContext;

        @Override public void run() {
            ListNodesCommand listNodesCommand = applicationContext.createBean(ListNodesCommand.class);

            listNodesCommand.setOut(spec.commandLine().getOut());
            listNodesCommand.run();

        }
    }

    @CommandLine.Command(name = "classpath", description = "Show current classpath for new nodes")
    public static class NodesClasspathCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Inject
        private ApplicationContext applicationContext;

        @Override public void run() {
            NodesClasspathCommand classpathCommand = applicationContext.createBean(NodesClasspathCommand.class);

            classpathCommand.setOut(spec.commandLine().getOut());
            classpathCommand.run();

        }
    }

}

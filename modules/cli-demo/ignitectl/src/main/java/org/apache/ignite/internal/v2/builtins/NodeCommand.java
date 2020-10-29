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

import org.apache.ignite.internal.v2.IgniteCommand;
import org.apache.ignite.internal.v2.IgniteCtl;
import picocli.CommandLine;

@CommandLine.Command(name = "node", mixinStandardHelpOptions = true,
    description = "Node actions", subcommands = {NodeCommand.StartNodeCommand.class})
public class NodeCommand implements IgniteCommand {

    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "start", mixinStandardHelpOptions = true, description = "Start Ignite node")
    public static class StartNodeCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Override public void run() {
            spec.commandLine().getOut().println("start ignite node");
        }
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.v2;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.ServiceLoader;
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.builtins.BaselineCommand;
import org.apache.ignite.internal.v2.builtins.InitIgniteCommand;
import org.apache.ignite.internal.v2.builtins.ModuleCommand;
import org.apache.ignite.internal.v2.builtins.NodeCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import org.apache.ignite.internal.v2.module.ErrorHandler;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(name = "ignite", mixinStandardHelpOptions = true,
    footer = "\n2020 Copyright(C) Apache Software Foundation",
    headerHeading = "Control utility ignite is used to execute admin commands on cluster or run new local nodes.\n\n",
    commandListHeading = "\n\nCommands has the following syntax:\n",
    versionProvider = VersionProvider.class,
    subcommands = {
        NodeCommand.class,
        ModuleCommand.class,
        InitIgniteCommand.class,
        BaselineCommand.class,
    }
)
public class IgniteCli implements Runnable {
    public LineReaderImpl reader;
    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    public static void main(String... args) {
        ApplicationContext applicationContext = ApplicationContext.run();
        CommandLine.IFactory factory = applicationContext.createBean(CommandFactory.class);
        CommandLine cli = new CommandLine(IgniteCli.class, factory)
            .setExecutionExceptionHandler(new ErrorHandler())
            .addSubcommand(applicationContext.createBean(ShellCommand.class));

        loadSubcommands(cli,
            applicationContext.createBean(SystemPathResolver.class),
            applicationContext.createBean(Info.class));
        System.exit(cli.execute(args));
    }

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    public void setReader(LineReader reader){
        this.reader = (LineReaderImpl) reader;
    }

    public static void loadSubcommands(CommandLine commandLine, SystemPathResolver pathResolver, Info info) {
        Optional<File> configOpt = Config.searchConfigPath(pathResolver);
        if (configOpt.isPresent()) {
            Config cfg = Config.readConfigFile(configOpt.get());
            URL[] urls = SystemPathResolver.list(cfg.cliLibsDir(info.version));
            ClassLoader classLoader = new URLClassLoader(urls,
                IgniteCli.class.getClassLoader());
            ServiceLoader<IgniteCommand> loader = ServiceLoader.load(IgniteCommand.class, classLoader);
            loader.reload();
            for (IgniteCommand igniteCommand: loader) {
                commandLine.addSubcommand(igniteCommand);
            }

        }
        else {
            ServiceLoader<IgniteCommand> loader = ServiceLoader.load(IgniteCommand.class);
            loader.reload();
            for (IgniteCommand igniteCommand : loader) {
                commandLine.addSubcommand(igniteCommand);
            }

        }

    }
}
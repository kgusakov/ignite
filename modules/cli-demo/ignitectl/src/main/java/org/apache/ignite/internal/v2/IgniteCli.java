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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.ServiceLoader;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(name = "ignite", mixinStandardHelpOptions = true,
    description = "Control utility for Apache Ignite",
    versionProvider = VersionProvider.class)
public class IgniteCli implements Runnable {
    public LineReaderImpl reader;
    public @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    public static void main(String... args) {
        CommandLine cli = new CommandLine(new IgniteCli()).addSubcommand(ShellCommand.class);
        loadSubcommands(cli);
        System.exit(cli.execute(args));
    }

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    public void setReader(LineReader reader){
        this.reader = (LineReaderImpl) reader;
    }

    public static void loadSubcommands(CommandLine commandLine) {
        Optional<File> configOpt = Config.searchConfigPath(new SystemPathResolver.DefaultPathResolver());
        if (configOpt.isPresent()) {
            Config cfg = Config.readConfigFile(configOpt.get());
            URL[] urls = SystemPathResolver.list(SystemPathResolver.osIndependentPath(cfg.binDir, new Info().version, "cli/"));
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
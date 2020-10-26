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

import java.io.PrintWriter;
import java.util.ServiceLoader;
import org.apache.ignite.internal.v2.builtins.BaselineCommand;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(name = "ignitectl", mixinStandardHelpOptions = true,
    description = "Control utility for Apache Ignite",
    subcommands = {ShellCommand.class})
public class IgniteCtl implements Runnable {
    public LineReaderImpl reader;
    public PrintWriter out = new PrintWriter(System.out);

    public static void main(String... args) {
        CommandLine cli = new CommandLine(new IgniteCtl());
        loadSubcommands(cli);
        System.exit(cli.execute(args));
    }

    @Override public void run() {
        out.println(new CommandLine(this).getUsageMessage());
        out.flush();
    }

    public void setReader(LineReader reader){
        this.reader = (LineReaderImpl) reader;
        out = reader.getTerminal().writer();
    }

    public static void loadSubcommands(CommandLine commandLine) {
        for (IgniteCommand igniteCommand: ServiceLoader.load(IgniteCommand.class)) {
            commandLine.addSubcommand(igniteCommand);
        }
    }
}
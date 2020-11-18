package org.apache.ignite.internal.v2.spec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import javax.inject.Inject;
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.CliVersionInfo;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import org.apache.ignite.internal.v2.builtins.init.InitIgniteCommand;
import org.apache.ignite.internal.v2.builtins.module.ModuleManager;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command(name = "init",
    description = "Install Apache Ignite at the current directory")
public class InitIgniteCommandSpec implements Runnable, IgniteCommand {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    private ApplicationContext applicationContext;

    @Inject
    public InitIgniteCommandSpec(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override public void run() {
        InitIgniteCommand command = applicationContext.createBean(InitIgniteCommand.class);
        command.setOut(spec.commandLine().getOut());
        command.run();

    }

}

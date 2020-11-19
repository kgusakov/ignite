package org.apache.ignite.internal.v2.spec;

import javax.inject.Inject;
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.builtins.init.InitIgniteCommand;
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

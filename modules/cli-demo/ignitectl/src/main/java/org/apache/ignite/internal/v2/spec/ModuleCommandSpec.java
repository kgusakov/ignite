package org.apache.ignite.internal.v2.spec;

import javax.inject.Inject;
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.builtins.module.AddModuleCommand;
import org.apache.ignite.internal.v2.builtins.module.ListModuleCommand;
import org.apache.ignite.internal.v2.builtins.module.RemoveModuleCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "module",
    description = "Manage Ignite modules",
    subcommands = {
        ModuleCommandSpec.AddModuleCommandSpec.class,
        ModuleCommandSpec.RemoveModuleCommandSpec.class,
        ModuleCommandSpec.ListModuleCommandSpec.class})
public class ModuleCommandSpec implements IgniteCommand, Runnable {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }

    @CommandLine.Command(name = "add",
        description = "Add module to Ignite or cli tool")
    public static class AddModuleCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(names = {"--cli"},
            description = "set if you want to install cli module")
        public boolean cli;

        @CommandLine.Parameters(paramLabel = "module",
            description = "can be a 'builtin module name (see module list)'|'mvn:groupId:artifactId:version'")
        public String moduleName;

        @Inject
        ApplicationContext applicationContext;


        @Override public void run() {
            AddModuleCommand addModuleCommand = applicationContext.createBean(AddModuleCommand.class);
            addModuleCommand.setOut(spec.commandLine().getOut());

            addModuleCommand.addModule(moduleName, cli);
        }
    }

    @CommandLine.Command(name = "remove",
        description = "Remove Ignite or cli module by name")
    public static class RemoveModuleCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Parameters(paramLabel = "module",
            description = "can be a 'builtin module name (see module list)'|'mvn:groupId:artifactId:version'")
        public String moduleName;

        @Inject
        ApplicationContext applicationContext;


        @Override public void run() {
            RemoveModuleCommand removeModuleCommand = applicationContext.createBean(RemoveModuleCommand.class);
            removeModuleCommand.setOut(spec.commandLine().getOut());

            removeModuleCommand.removeModule(moduleName);
        }
    }

    @CommandLine.Command(name = "list",
        description = "List available builtin Apache Ignite modules")
    public static class ListModuleCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Inject
        ApplicationContext applicationContext;

        @Override public void run() {
            ListModuleCommand listModuleCommand = applicationContext.createBean(ListModuleCommand.class);
            listModuleCommand.setOut(spec.commandLine().getOut());

            listModuleCommand.list();
        }
    }

}

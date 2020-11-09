package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import com.mitchtalmadge.asciidata.table.ASCIITable;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import org.apache.ignite.internal.v2.module.ModuleManager;
import org.apache.ignite.internal.v2.module.TransferListenerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "module",
    description = "Module management commands",
    subcommands = {
        ModuleCommand.AddModuleCommand.class,
        ModuleCommand.ListModuleCommand.class})
public class ModuleCommand implements IgniteCommand, Runnable {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "add",
        description = "Add module to Ignite or cli tool")
    public static class AddModuleCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Parameters(paramLabel = "artifact", description = "maven coordinates to add groupId:artifactId[:version]")
        public String moduleName;

        private final MavenArtifactResolver mavenArtifactResolver;
        private final SystemPathResolver pathResolver;
        private final Info info;
        private final TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper;
        private final ModuleManager moduleManager;

        @Inject
        public AddModuleCommand(MavenArtifactResolver mavenArtifactResolver,
            SystemPathResolver pathResolver, Info info,
            TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper,
            ModuleManager moduleManager) {
            this.mavenArtifactResolver = mavenArtifactResolver;
            this.pathResolver = pathResolver;
            this.info = info;
            this.transferEventListenerWrapper = transferEventListenerWrapper;
            this.moduleManager = moduleManager;
        }


        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            Config config = Config.readConfigFile(configFile.get());

            moduleManager.addModule(moduleName, config,
                transferEventListenerWrapper.produceListener(spec.commandLine().getOut()));
        }
    }

    @CommandLine.Command(name = "list",
        description = "List available builtin Apache Ignite modules")
    public static class ListModuleCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        private final MavenArtifactResolver mavenArtifactResolver;
        private final SystemPathResolver pathResolver;
        private final Info info;

        @Inject
        public ListModuleCommand(MavenArtifactResolver mavenArtifactResolver, SystemPathResolver pathResolver, Info info) {
            this.mavenArtifactResolver = mavenArtifactResolver;
            this.pathResolver = pathResolver;
            this.info = info;
        }


        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            Config config = Config.readConfigFile(configFile.get());

            String[] headers = new String[] {"name", "description"};

            // TODO: ugly table should be changed
            String[][] answer = ModuleManager.readBuiltinModules()
                .stream()
                .map(m -> new String[] {m.name, m.description})
                .collect(Collectors.toList())
                .toArray(new String[][] {});

            spec.commandLine().getOut().println(ASCIITable.fromData(headers, answer).toString());
        }
    }

}

package org.apache.ignite.internal.v2.builtins.module;

import java.util.stream.Collectors;
import javax.inject.Inject;
import com.mitchtalmadge.asciidata.table.ASCIITable;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.Info;
import picocli.CommandLine;

@CommandLine.Command(name = "module",
    description = "Manage Ignite modules",
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

        @CommandLine.Option(names = {"--cli"},
            description = "set if you want to install cli module")
        public boolean cli;

        @CommandLine.Parameters(paramLabel = "module",
            description = "can be a 'builtin module name (see module list)'|'mvn:groupId:artifactId:version'")
        public String moduleName;

        private final MavenArtifactResolver mavenArtifactResolver;
        private final SystemPathResolver pathResolver;
        private final ModuleManager moduleManager;

        @Inject
        public AddModuleCommand(MavenArtifactResolver mavenArtifactResolver,
            SystemPathResolver pathResolver,
            ModuleManager moduleManager) {
            this.mavenArtifactResolver = mavenArtifactResolver;
            this.pathResolver = pathResolver;
            this.moduleManager = moduleManager;
        }


        @Override public void run() {
            Config config = Config.getConfigOrError(pathResolver);

            moduleManager.setOut(spec.commandLine().getOut());
            moduleManager.addModule(moduleName, config, cli);
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

package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import javax.inject.Inject;
import com.mitchtalmadge.asciidata.table.ASCIITable;
import com.mitchtalmadge.asciidata.table.formats.ASCIITableFormat;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
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
        public String mavenCoordinates;

        private final MavenArtifactResolver mavenArtifactResolver;
        private final SystemPathResolver pathResolver;
        private final Info info;
        private final TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper;

        @Inject
        public AddModuleCommand(MavenArtifactResolver mavenArtifactResolver,
            SystemPathResolver pathResolver, Info info,
            TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper) {
            this.mavenArtifactResolver = mavenArtifactResolver;
            this.pathResolver = pathResolver;
            this.info = info;
            this.transferEventListenerWrapper = transferEventListenerWrapper;
        }


        @Override public void run() {
            Optional<File> configFile = Config.searchConfigPath(pathResolver);
            if (!configFile.isPresent())
                throw new IgniteCLIException("Can't find config file. Looks like you should run 'init' command first");
            Config config = Config.readConfigFile(configFile.get());
            String[] coords = mavenCoordinates.split(":");

            String groupId;
            String artifactId;
            String version = info.version;
            if (coords.length == 2) {
                groupId = coords[0];
                artifactId = coords[1];
            }
            else if (coords.length == 3) {
                groupId = coords[0];
                artifactId = coords[1];
                version = coords[2];
            }
            else
                throw new IgniteCLIException("Incorrect maven coordinates");

            try {
                if (artifactId.endsWith("-all")) {
                    mavenArtifactResolver.resolveDeps(
                        config.libsDir(info.version),
                        config.cliDir(info.version),
                        groupId,
                        artifactId,
                        version,
                        transferEventListenerWrapper.produceListener(spec.commandLine().getOut()));
                }
                else if (artifactId.endsWith("-cli")) {
                    mavenArtifactResolver.resolve(
                        config.cliDir(info.version),
                        groupId,
                        artifactId,
                        version,
                        transferEventListenerWrapper.produceListener(spec.commandLine().getOut()));
                }
                else {
                    mavenArtifactResolver.resolve(
                        config.libsDir(info.version),
                        groupId,
                        artifactId,
                        version,
                        transferEventListenerWrapper.produceListener(spec.commandLine().getOut()));

                }
            } catch (IOException ex) {
                throw new IgniteCLIException("Can't retrieve needed module", ex);
            }
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
            String[][] answer = readBuiltinModules()
                .stream()
                .map(m -> new String[] {m.name, m.description})
                .collect(Collectors.toList())
                .toArray(new String[][] {});

            spec.commandLine().getOut().println(ASCIITable.fromData(headers, answer).toString());
        }
    }

    private static List<ModuleDescription> readBuiltinModules() {
        com.typesafe.config.ConfigObject config = ConfigFactory.load("modules.conf").getObject("modules");
        List<ModuleDescription> modules = new ArrayList<>();
        for (Map.Entry<String, ConfigValue> entry: config.entrySet()) {
            ConfigObject configObject = (ConfigObject) entry.getValue();
            modules.add(new ModuleDescription(
                entry.getKey(),
                configObject.toConfig().getString("description"),
                configObject.toConfig().getStringList("artifacts"),
                configObject.toConfig().getStringList("cli-artifacts")
            ));
        }
        return modules;
    }

    static class ModuleDescription {
        private final String name;
        private final String description;
        private final List<String> artifacts;
        private final List<String> cliArtifacts;

        public ModuleDescription(String name, String description, List<String> artifacts, List<String> cliArtifacts) {
            this.name = name;
            this.description = description;
            this.artifacts = artifacts;
            this.cliArtifacts = cliArtifacts;
        }

        public String toString() {
            return this.name + ":\t" + this.description;
        }
    }

}

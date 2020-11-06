package org.apache.ignite.internal.v2.builtins;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.inject.Inject;
import org.apache.ignite.cli.common.IgniteCommand;
import org.apache.ignite.internal.installer.MavenArtifactResolver;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.Info;
import picocli.CommandLine;

@CommandLine.Command(name = "module",
    description = "Module management commands",
    subcommands = {ModuleCommand.AddModuleCommand.class})
public class ModuleCommand implements IgniteCommand, Runnable {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "add",
        description = "Add module to Ignite or cli tool")
    public static class AddModuleCommand implements Runnable {

        @CommandLine.Parameters(paramLabel = "artifact", description = "maven coordinates to add groupId:artifactId[:version]")
        public String mavenCoordinates;

        private final MavenArtifactResolver mavenArtifactResolver;
        private final SystemPathResolver pathResolver;
        private final Info info;

        @Inject
        public AddModuleCommand(MavenArtifactResolver mavenArtifactResolver, SystemPathResolver pathResolver, Info info) {
            this.mavenArtifactResolver = mavenArtifactResolver;
            this.pathResolver = pathResolver;
            this.info = info;
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
                        version);
                }
                else if (artifactId.endsWith("-cli")) {
                    mavenArtifactResolver.resolve(
                        config.cliDir(info.version),
                        groupId,
                        artifactId,
                        version);
                }
                else {
                    mavenArtifactResolver.resolve(
                        config.libsDir(info.version),
                        groupId,
                        artifactId,
                        version);

                }
            } catch (IOException ex) {
                throw new IgniteCLIException("Can't retrieve needed module", ex);
            }
        }
    }

}

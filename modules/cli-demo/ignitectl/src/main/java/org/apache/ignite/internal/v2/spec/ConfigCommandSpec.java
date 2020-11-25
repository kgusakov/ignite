package org.apache.ignite.internal.v2.spec;

import org.apache.ignite.internal.v2.builtins.config.ConfigurationClient;
import picocli.CommandLine;

@CommandLine.Command(name = "config", mixinStandardHelpOptions = true,
    description = "Show/change node configurations",
    subcommands = {
        ConfigCommandSpec.GetConfigCommandSpec.class,
        ConfigCommandSpec.SetConfigCommandSpec.class
    })
public class ConfigCommandSpec implements Runnable {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "get", mixinStandardHelpOptions = true,
        description = "Get current cluster configs")
    public static class GetConfigCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Override public void run() {
            spec.commandLine().getOut().println(new ConfigurationClient().get());
        }
    }

    @CommandLine.Command(name = "set", mixinStandardHelpOptions = true,
        description = "Set current cluster configs. Config is expected as any valid Hocon")
    public static class SetConfigCommandSpec implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @CommandLine.Parameters(paramLabel = "hocon-string", description = "any text representation of hocon config")
        private String config;

        @Override public void run() {
            spec.commandLine().getOut().println(new ConfigurationClient().set(config));
        }
    }
}

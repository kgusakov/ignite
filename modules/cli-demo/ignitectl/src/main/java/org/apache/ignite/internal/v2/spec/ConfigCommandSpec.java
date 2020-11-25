package org.apache.ignite.internal.v2.spec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.apache.ignite.internal.v2.IgniteCLIException;
import org.apache.ignite.internal.v2.builtins.config.ConfigurationClient;
import picocli.CommandLine;

@CommandLine.Command(name = "config", mixinStandardHelpOptions = true,
    description = "Show/change node configurations",
    subcommands = {
        ConfigCommandSpec.GetConfigCommandSpec.class
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
}

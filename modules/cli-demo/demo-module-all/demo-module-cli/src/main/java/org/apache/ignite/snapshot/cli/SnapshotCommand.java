package org.apache.ignite.snapshot.cli;

import org.apache.ignite.cli.common.IgniteCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "snapshot",
    description = "Snapshots management",
    subcommands = {
        SnapshotCommand.CreateSnashotCommand.class,
        SnapshotCommand.CancelSnapshotCommand.class}
)
public class SnapshotCommand implements IgniteCommand, Runnable {

    @CommandLine.Spec CommandLine.Model.CommandSpec spec;

    @Override public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }

    @CommandLine.Command(
        name = "create",
        description = "Create snapshot"
    )
    public static class CreateSnashotCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Override public void run() {
            spec.commandLine().getOut().println("Create snapshot command was executed");
        }
    }


    @CommandLine.Command(
        name = "cancel",
        description = "Cancel snapshot"
    )
    public static class CancelSnapshotCommand implements Runnable {

        @CommandLine.Spec CommandLine.Model.CommandSpec spec;

        @Override public void run() {
            spec.commandLine().getOut().println("Cancel snapshot command was executed");
        }

    }
}

package org.apache.ignite.internal.v2.builtins.node;

import java.util.List;
import javax.inject.Inject;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class StopNodeCommand extends AbstractCliCommand {

    private final NodeManager nodeManager;
    private final CliPathsConfigLoader cliPathsConfigLoader;

    @Inject
    public StopNodeCommand(
        NodeManager nodeManager, CliPathsConfigLoader cliPathsConfigLoader) {
        this.nodeManager = nodeManager;
        this.cliPathsConfigLoader = cliPathsConfigLoader;
    }

    public void run(List<String> consistendIds) {
        IgnitePaths ignitePaths = cliPathsConfigLoader.loadIgnitePathsOrThrowError();
        consistendIds.forEach(p -> {
            if (nodeManager.stopWait(p, ignitePaths.cliPidsDir()))
                out.println("Node with consistent id " + p + " was stopped");
            else
                out.println("Stop of node " + p + " was failed");
        });

    }
}

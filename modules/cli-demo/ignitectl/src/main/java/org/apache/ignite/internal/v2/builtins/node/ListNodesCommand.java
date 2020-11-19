package org.apache.ignite.internal.v2.builtins.node;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class ListNodesCommand extends AbstractCliCommand {

    private final NodeManager nodeManager;
    private final CliPathsConfigLoader cliPathsConfigLoader;

    @Inject
    public ListNodesCommand(NodeManager nodeManager,
        CliPathsConfigLoader cliPathsConfigLoader) {
        this.nodeManager = nodeManager;
        this.cliPathsConfigLoader =cliPathsConfigLoader;
    }

    public void run() {
        List<String> pids = nodeManager
            .getRunningNodes(cliPathsConfigLoader.loadIgnitePathsOrThrowError().cliPidsDir())
            .stream()
            .map(rn -> rn.pid + "\t" + rn.consistentId)
            .collect(Collectors.toList());

        if (pids.isEmpty())
            out.println("No running nodes");
        else
            out.println(String.join("\n", pids));

    }
}

package org.apache.ignite.internal.v2.builtins.node;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class ListNodesCommand extends AbstractCliCommand {

    private final NodeManager nodeManager;
    private final SystemPathResolver pathResolver;

    @Inject
    public ListNodesCommand(NodeManager nodeManager,
        SystemPathResolver resolver) {
        this.nodeManager = nodeManager;
        pathResolver = resolver;
    }

    public void run() {
        Config config = Config.getConfigOrError(pathResolver);

        List<String> pids = nodeManager.getRunningNodes(config).stream()
            .map(rn -> rn.pid + "\t" + rn.consistentId)
            .collect(Collectors.toList());

        if (pids.isEmpty())
            out.println("No running nodes");
        else
            out.println(String.join("\n", pids));

    }
}

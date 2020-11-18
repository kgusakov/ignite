package org.apache.ignite.internal.v2.builtins.node;

import java.util.List;
import javax.inject.Inject;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class StopNodeCommand extends AbstractCliCommand {

    private final NodeManager nodeManager;
    private final SystemPathResolver pathResolver;

    @Inject
    public StopNodeCommand(
        NodeManager nodeManager, SystemPathResolver resolver) {
        this.nodeManager = nodeManager;
        pathResolver = resolver;
    }

    public void run(List<String> consistendIds) {
        Config config = Config.getConfigOrError(pathResolver);
        consistendIds.forEach(p -> {
            if (nodeManager.stopWait(p, config))
                out.println("Node with consistent id " + p + " was stopped");
            else
                out.println("Stop of node " + p + " was failed");
        });

    }
}

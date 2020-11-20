package org.apache.ignite.internal.v2.builtins.node;

import java.io.IOException;
import javax.inject.Inject;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.IgniteCLIException;

public class NodesClasspathCommand extends AbstractCliCommand {

    private final NodeManager nodeManager;

    @Inject
    public NodesClasspathCommand(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void run() {
        try {
            out.println(nodeManager.classpath());
        }
        catch (IOException e) {
            throw new IgniteCLIException("Can't get current classpath", e);
        }

    }
}

package org.apache.ignite.internal.v2.builtins.node;

import javax.inject.Inject;
import org.apache.ignite.internal.v2.CliVersionInfo;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import picocli.CommandLine;

public class StartNodeCommand extends AbstractCliCommand {

    private final CliVersionInfo cliVersionInfo;
    private final SystemPathResolver pathResolver;
    private final NodeManager nodeManager;

    @Inject
    public StartNodeCommand(CliVersionInfo cliVersionInfo, SystemPathResolver pathResolver, NodeManager nodeManager) {
        this.cliVersionInfo = cliVersionInfo;
        this.pathResolver = pathResolver;
        this.nodeManager = nodeManager;
    }

    public void start(String consistentId) {
        Config config = Config.getConfigOrError(pathResolver);
        long pid = nodeManager.start(consistentId, config);

        out.println("Started ignite node '" + consistentId + "'");
    }
}

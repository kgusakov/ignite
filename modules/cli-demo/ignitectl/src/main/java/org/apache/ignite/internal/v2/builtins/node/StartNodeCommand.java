package org.apache.ignite.internal.v2.builtins.node;

import javax.inject.Inject;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.CliVersionInfo;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class StartNodeCommand extends AbstractCliCommand {

    private final CliPathsConfigLoader cliPathsConfigLoader;
    private final NodeManager nodeManager;

    @Inject
    public StartNodeCommand(
        CliPathsConfigLoader cliPathsConfigLoader,
        NodeManager nodeManager) {
        this.cliPathsConfigLoader = cliPathsConfigLoader;
        this.nodeManager = nodeManager;
    }

    public void start(String consistentId) {
        IgnitePaths ignitePaths = cliPathsConfigLoader.loadIgnitePathsOrThrowError();
        long pid = nodeManager.start(consistentId, ignitePaths.workDir, ignitePaths.cliPidsDir());

        out.println("Started ignite node '" + consistentId + "'");
    }
}

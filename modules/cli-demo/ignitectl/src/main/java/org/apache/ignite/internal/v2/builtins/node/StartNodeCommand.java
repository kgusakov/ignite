package org.apache.ignite.internal.v2.builtins.node;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.CliVersionInfo;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

@Singleton
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
        NodeManager.RunningNode node = nodeManager.start(consistentId, ignitePaths.workDir, ignitePaths.cliPidsDir());

        out.println("Started ignite node.\nPID: " + node.pid +
            "\nConsistent Id: " + node.consistentId + "\nLog file: " + node.logFile);
    }
}

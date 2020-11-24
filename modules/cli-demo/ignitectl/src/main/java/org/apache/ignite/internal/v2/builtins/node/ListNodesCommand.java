package org.apache.ignite.internal.v2.builtins.node;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

@Singleton
public class ListNodesCommand extends AbstractCliCommand {

    private final NodeManager nodeManager;
    private final CliPathsConfigLoader cliPathsConfigLoader;

    @Inject
    public ListNodesCommand(NodeManager nodeManager,
        CliPathsConfigLoader cliPathsConfigLoader) {
        this.nodeManager = nodeManager;
        this.cliPathsConfigLoader = cliPathsConfigLoader;
    }

    public void run() {
        List<NodeManager.RunningNode> nodes = nodeManager
            .getRunningNodes(cliPathsConfigLoader.loadIgnitePathsOrThrowError().cliPidsDir());

        if (nodes.isEmpty())
            out.println("No running nodes");
        else {
            String table = AsciiTable.getTable(nodes, Arrays.asList(
                new Column().header("PID").dataAlign(HorizontalAlign.LEFT).with(n -> String.valueOf(n.pid)),
                new Column().header("Consistent Id").dataAlign(HorizontalAlign.LEFT).with(n -> n.consistentId)
            ));
            out.println(table);
        }
    }
}

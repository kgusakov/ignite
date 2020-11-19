package org.apache.ignite.internal.v2.builtins.module;

import java.util.stream.Collectors;
import javax.inject.Inject;
import com.mitchtalmadge.asciidata.table.ASCIITable;
import org.apache.ignite.internal.v2.CliVersionInfo;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class ListModuleCommand extends AbstractCliCommand {

    public void list() {
        String[] headers = new String[] {"name", "description"};

        // TODO: ugly table should be changed
        String[][] answer = ModuleManager.readBuiltinModules()
            .stream()
            .map(m -> new String[] {m.name, m.description})
            .collect(Collectors.toList())
            .toArray(new String[][] {});

        out.println(ASCIITable.fromData(headers, answer).toString());
    }
}

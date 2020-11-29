package org.apache.ignite.internal.v2.builtins.module;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.apache.ignite.internal.v2.AbstractCliCommand;

import static org.apache.ignite.internal.v2.builtins.module.ModuleManager.INTERNAL_MODULE_PREFIX;

@Singleton
public class ListModuleCommand extends AbstractCliCommand {

    public void list() {
        var builtinModules = ModuleManager.readBuiltinModules()
            .stream()
            .filter(m -> !m.name.startsWith(INTERNAL_MODULE_PREFIX));
        String table = AsciiTable.getTable(builtinModules.collect(Collectors.toList()), Arrays.asList(
            new Column().header("name").dataAlign(HorizontalAlign.LEFT).with(m -> m.name),
            new Column().header("description").dataAlign(HorizontalAlign.LEFT).with(m -> m.description)
        ));
        out.println(table);
    }
}

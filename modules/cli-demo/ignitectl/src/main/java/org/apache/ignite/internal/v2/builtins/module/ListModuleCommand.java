package org.apache.ignite.internal.v2.builtins.module;

import java.util.Arrays;
import javax.inject.Singleton;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.apache.ignite.internal.v2.AbstractCliCommand;

@Singleton
public class ListModuleCommand extends AbstractCliCommand {

    public void list() {
        String table = AsciiTable.getTable(ModuleManager.readBuiltinModules(), Arrays.asList(
            new Column().header("name").dataAlign(HorizontalAlign.LEFT).with(m -> m.name),
            new Column().header("description").dataAlign(HorizontalAlign.LEFT).with(m -> m.description)
        ));
        out.println(table);
    }
}

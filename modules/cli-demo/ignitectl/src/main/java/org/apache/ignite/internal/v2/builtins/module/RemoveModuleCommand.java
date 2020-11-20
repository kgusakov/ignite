package org.apache.ignite.internal.v2.builtins.module;

import javax.inject.Inject;
import org.apache.ignite.internal.v2.AbstractCliCommand;

public class RemoveModuleCommand extends AbstractCliCommand {
    private final ModuleManager moduleManager;

    @Inject
    public RemoveModuleCommand(
        ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }


    public void removeModule(String moduleName) {
        moduleManager.setOut(out);

        boolean removed = moduleManager.removeModule(moduleName);
        if (removed)
            out.println("Module " + moduleName + " was removed successfully");
        else
            out.println("Module " + moduleName + " is not found");

    }
}

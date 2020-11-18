package org.apache.ignite.internal.v2.builtins.module;

import javax.inject.Inject;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class AddModuleCommand extends AbstractCliCommand {

    private final SystemPathResolver pathResolver;
    private final ModuleManager moduleManager;

    @Inject
    public AddModuleCommand(
        SystemPathResolver pathResolver,
        ModuleManager moduleManager) {
        this.pathResolver = pathResolver;
        this.moduleManager = moduleManager;
    }


    public void addModule(String moduleName, boolean cli) {
        Config config = Config.getConfigOrError(pathResolver);

        moduleManager.setOut(out);
        moduleManager.addModule(moduleName, config, cli);
    }
}

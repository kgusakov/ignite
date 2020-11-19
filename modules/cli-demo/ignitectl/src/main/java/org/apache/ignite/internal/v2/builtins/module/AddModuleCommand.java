package org.apache.ignite.internal.v2.builtins.module;

import javax.inject.Inject;
import org.apache.ignite.internal.v2.CliPathsConfigLoader;
import org.apache.ignite.internal.v2.IgnitePaths;
import org.apache.ignite.internal.v2.AbstractCliCommand;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;

public class AddModuleCommand extends AbstractCliCommand {

    private final CliPathsConfigLoader cliPathsConfigLoader;
    private final ModuleManager moduleManager;

    @Inject
    public AddModuleCommand(
        CliPathsConfigLoader cliPathsConfigLoader,
        ModuleManager moduleManager) {
        this.cliPathsConfigLoader = cliPathsConfigLoader;
        this.moduleManager = moduleManager;
    }


    public void addModule(String moduleName, boolean cli) {
        moduleManager.setOut(out);
        moduleManager.addModule(moduleName, cliPathsConfigLoader.loadIgnitePathsOrThrowError(), cli);
    }
}

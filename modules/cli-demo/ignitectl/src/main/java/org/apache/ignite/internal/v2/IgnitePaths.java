package org.apache.ignite.internal.v2;

import java.nio.file.Path;

import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;

public class IgnitePaths {

    public static Path LIBS = pathOf("libs");
    public static Path CLI_LIBS = pathOf("cli");

    public final Path binDir;
    public final Path workDir;
    private final String version;

    public IgnitePaths(Path binDir, Path workDir, String version) {
        this.binDir = binDir;
        this.workDir = workDir;
        this.version = version;
    }


    public Path cliLibsDir() {
        return binDir.resolve(version).resolve("cli");
    }

    public Path libsDir() {
        return binDir.resolve(version).resolve("libs");
    }

    public Path cliPidsDir() {
        return workDir.resolve("cli").resolve("pids");
    }

    public Path installedModulesFile() {
        return workDir.resolve("modules.json");
    }



}

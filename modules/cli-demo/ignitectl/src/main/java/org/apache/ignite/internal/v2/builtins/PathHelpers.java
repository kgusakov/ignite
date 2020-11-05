package org.apache.ignite.internal.v2.builtins;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public interface PathHelpers {

    static Path pathOf(String path, String... paths) {
        return FileSystems.getDefault().getPath(path, paths);
    }
}

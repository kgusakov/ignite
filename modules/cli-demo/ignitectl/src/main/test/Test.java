/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.util.Arrays;
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.module.ModuleManager;
import org.apache.ignite.internal.v2.module.ModuleStorage;

public class Test {



    @org.junit.Test
    public void test() throws IOException, InterruptedException {
        System.out.println(FileSystems.getDefault().getPath("file://tmp/modules.json").toAbsolutePath().toString());
//        storage.saveModule(new ModuleStorage.ModuleDefinition(
//            "newModule",
//            Arrays.asList(FileSystems.getDefault().getPath("/tmp/artifact.jar")),
//            ModuleStorage.SourceType.File,
//            "file://file.jar"
//        ));



    }
}

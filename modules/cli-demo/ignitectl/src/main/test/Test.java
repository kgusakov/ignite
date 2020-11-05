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
import org.apache.ignite.internal.v2.builtins.NodeCommand;
import static org.apache.ignite.internal.v2.builtins.NodeCommand.NodeManager;
import static org.apache.ignite.internal.v2.builtins.PathHelpers.pathOf;

public class Test {

    @org.junit.Test
    public void test() throws IOException, InterruptedException {

        String srvDir =
            "/Users/kgusakov/Projects/gridgain_root/incubator-ignite/modules/cli-demo/ignitectl/target/tmp/ignite-bin/2.10.0-SNAPSHOT/libs/";
        long pid = NodeManager.start(pathOf(srvDir), "consistentName");

        Thread.sleep(60000);

        NodeCommand.NodeManager.stopWait(pid);

    }
}

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
import io.micronaut.context.ApplicationContext;
import org.apache.ignite.internal.v2.Config;
import org.apache.ignite.internal.v2.module.ModuleManager;

public class Test {



    @org.junit.Test
    public void test() throws IOException, InterruptedException {
        ApplicationContext applicationContext = ApplicationContext.run();
        ModuleManager moduleManager = applicationContext.getBean(ModuleManager.class);
        Config config = new Config("/tmp", "/tmp");
        TransferListenerFactory.TransferEventListenerWrapper transferEventListenerWrapper =
            applicationContext.getBean(TransferListenerFactory.TransferEventListenerWrapper.class);
        PrintWriter pw = new PrintWriter(System.out);
        moduleManager.addModule("server", config, transferEventListenerWrapper.produceListener(pw));

    }
}

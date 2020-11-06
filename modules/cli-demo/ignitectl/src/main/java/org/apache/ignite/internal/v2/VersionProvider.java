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

package org.apache.ignite.internal.v2;

import javax.inject.Inject;
import javax.inject.Singleton;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Introspected;
import picocli.CommandLine;

@Singleton
@Introspected
public class VersionProvider implements CommandLine.IVersionProvider {

    private final Info info;

    @Inject
    public VersionProvider(Info info) {
        this.info = info;
    }

    @Override public String[] getVersion() throws Exception {
        return new String[] {info.version};
    }
}

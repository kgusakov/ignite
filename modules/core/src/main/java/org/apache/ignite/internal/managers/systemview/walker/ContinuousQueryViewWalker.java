/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.managers.systemview.walker;

import java.util.UUID;
import org.apache.ignite.spi.systemview.view.ContinuousQueryView;
import org.apache.ignite.spi.systemview.view.SystemViewRowAttributeWalker;

/**
 * Generated by {@code org.apache.ignite.codegen.SystemViewRowAttributeWalkerGenerator}.
 * {@link ContinuousQueryView} attributes walker.
 * 
 * @see ContinuousQueryView
 */
public class ContinuousQueryViewWalker implements SystemViewRowAttributeWalker<ContinuousQueryView> {
    /** {@inheritDoc} */
    @Override public void visitAll(AttributeVisitor v) {
        v.accept(0, "cacheName", String.class);
        v.accept(1, "localListener", String.class);
        v.accept(2, "remoteFilter", String.class);
        v.accept(3, "remoteTransformer", String.class);
        v.accept(4, "localTransformedListener", String.class);
        v.accept(5, "lastSendTime", long.class);
        v.accept(6, "autoUnsubscribe", boolean.class);
        v.accept(7, "bufferSize", int.class);
        v.accept(8, "delayedRegister", boolean.class);
        v.accept(9, "interval", long.class);
        v.accept(10, "isEvents", boolean.class);
        v.accept(11, "isMessaging", boolean.class);
        v.accept(12, "isQuery", boolean.class);
        v.accept(13, "keepBinary", boolean.class);
        v.accept(14, "nodeId", UUID.class);
        v.accept(15, "notifyExisting", boolean.class);
        v.accept(16, "oldValueRequired", boolean.class);
        v.accept(17, "routineId", UUID.class);
        v.accept(18, "topic", String.class);
    }

    /** {@inheritDoc} */
    @Override public void visitAll(ContinuousQueryView row, AttributeWithValueVisitor v) {
        v.accept(0, "cacheName", String.class, row.cacheName());
        v.accept(1, "localListener", String.class, row.localListener());
        v.accept(2, "remoteFilter", String.class, row.remoteFilter());
        v.accept(3, "remoteTransformer", String.class, row.remoteTransformer());
        v.accept(4, "localTransformedListener", String.class, row.localTransformedListener());
        v.acceptLong(5, "lastSendTime", row.lastSendTime());
        v.acceptBoolean(6, "autoUnsubscribe", row.autoUnsubscribe());
        v.acceptInt(7, "bufferSize", row.bufferSize());
        v.acceptBoolean(8, "delayedRegister", row.delayedRegister());
        v.acceptLong(9, "interval", row.interval());
        v.acceptBoolean(10, "isEvents", row.isEvents());
        v.acceptBoolean(11, "isMessaging", row.isMessaging());
        v.acceptBoolean(12, "isQuery", row.isQuery());
        v.acceptBoolean(13, "keepBinary", row.keepBinary());
        v.accept(14, "nodeId", UUID.class, row.nodeId());
        v.acceptBoolean(15, "notifyExisting", row.notifyExisting());
        v.acceptBoolean(16, "oldValueRequired", row.oldValueRequired());
        v.accept(17, "routineId", UUID.class, row.routineId());
        v.accept(18, "topic", String.class, row.topic());
    }

    /** {@inheritDoc} */
    @Override public int count() {
        return 19;
    }
}

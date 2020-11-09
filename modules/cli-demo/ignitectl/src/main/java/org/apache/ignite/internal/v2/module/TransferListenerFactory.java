package org.apache.ignite.internal.v2.module;

import java.io.PrintWriter;
import javax.inject.Singleton;
import io.micronaut.context.annotation.Factory;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.plugins.repository.TransferListener;

@Factory
public class TransferListenerFactory {

    @Singleton
    TransferEventListenerWrapper defautTransferFunc() {
        return out -> evt -> {
            switch (evt.getEventType()) {
                case TransferEvent.TRANSFER_STARTED:
                    out.print(".");
                    break;
                case TransferEvent.TRANSFER_PROGRESS:
                    out.print(".");
                    break;
            }
            out.flush();
        };
    }

    public interface TransferEventListenerWrapper {
        TransferListener produceListener(PrintWriter out);
    }
}

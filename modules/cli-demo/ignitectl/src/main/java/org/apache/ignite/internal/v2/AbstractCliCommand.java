package org.apache.ignite.internal.v2;

import java.io.PrintWriter;

public abstract class AbstractCliCommand {
    protected PrintWriter out;

    public void setOut(PrintWriter out) {
        this.out = out;
    }
}

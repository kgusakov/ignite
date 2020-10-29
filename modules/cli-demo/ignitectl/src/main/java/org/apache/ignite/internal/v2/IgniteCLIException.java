package org.apache.ignite.internal.v2;

public class IgniteCLIException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public IgniteCLIException(String message) {
        super(message);
    }

    public IgniteCLIException(String message, Throwable cause) {
        super(message, cause);
    }
}

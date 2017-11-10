package io.blueocean.ath;

public class AcceptanceTestException extends RuntimeException {

    public AcceptanceTestException(String message, Throwable cause) {
        super(message);

        if (cause instanceof AcceptanceTestException) {
            initCause(cause.getCause());
        } else {
            initCause(cause);
        }
    }

}

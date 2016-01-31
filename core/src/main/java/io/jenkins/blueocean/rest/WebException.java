package io.jenkins.blueocean.rest;

/**
 * @author Vivek Pandey
 **/
public class WebException extends RuntimeException {
    public final int status;
    public final ErrorMessage errorMessage;

    public WebException(int status, String message) {
        super(message);
        this.status = status;
        this.errorMessage = new ErrorMessage(message);
    }

    public WebException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorMessage = new ErrorMessage(message);
    }

    public WebException(int status, ErrorMessage errorMessage, Throwable cause) {
        super(errorMessage.message, cause);
        this.status = status;
        this.errorMessage = errorMessage;
    }
}

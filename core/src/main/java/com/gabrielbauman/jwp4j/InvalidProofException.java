package com.gabrielbauman.jwp4j;

public class InvalidProofException extends JsonWebProofException {

    public InvalidProofException() {
        super();
    }

    public InvalidProofException(String message) {
        super(message);
    }

    public InvalidProofException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidProofException(Throwable cause) {
        super(cause);
    }

    protected InvalidProofException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

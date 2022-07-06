package com.gabrielbauman.jwp4j;

public class JsonWebProofException extends RuntimeException {

    public JsonWebProofException() {
        super();
    }

    public JsonWebProofException(String message) {
        super(message);
    }

    public JsonWebProofException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonWebProofException(Throwable cause) {
        super(cause);
    }

    protected JsonWebProofException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

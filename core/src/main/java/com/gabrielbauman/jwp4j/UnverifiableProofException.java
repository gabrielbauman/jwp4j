package com.gabrielbauman.jwp4j;

public class UnverifiableProofException extends JsonWebProofException {

    public UnverifiableProofException() {
        super();
    }

    public UnverifiableProofException(String message) {
        super(message);
    }

    public UnverifiableProofException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnverifiableProofException(Throwable cause) {
        super(cause);
    }

    protected UnverifiableProofException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

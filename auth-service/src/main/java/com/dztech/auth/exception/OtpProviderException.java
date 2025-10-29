package com.dztech.auth.exception;

public class OtpProviderException extends RuntimeException {
    public OtpProviderException(String message) {
        super(message);
    }

    public OtpProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}


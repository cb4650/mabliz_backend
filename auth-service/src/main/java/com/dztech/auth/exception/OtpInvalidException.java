package com.dztech.auth.exception;

public class OtpInvalidException extends RuntimeException {
    public OtpInvalidException(String message) {
        super(message);
    }

    public OtpInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}

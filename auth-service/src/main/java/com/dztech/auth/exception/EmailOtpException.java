package com.dztech.auth.exception;

public class EmailOtpException extends RuntimeException {
    public EmailOtpException(String message) {
        super(message);
    }

    public EmailOtpException(String message, Throwable cause) {
        super(message, cause);
    }
}

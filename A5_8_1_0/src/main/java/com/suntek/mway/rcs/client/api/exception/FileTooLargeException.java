package com.suntek.mway.rcs.client.api.exception;

public class FileTooLargeException extends Exception {
    private static final long serialVersionUID = 1;

    public FileTooLargeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FileTooLargeException(String detailMessage) {
        super(detailMessage);
    }

    public FileTooLargeException(Throwable throwable) {
        super(throwable);
    }
}

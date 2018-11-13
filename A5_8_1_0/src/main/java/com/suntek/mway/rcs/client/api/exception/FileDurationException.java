package com.suntek.mway.rcs.client.api.exception;

public class FileDurationException extends Exception {
    private static final long serialVersionUID = 1;

    public FileDurationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FileDurationException(String detailMessage) {
        super(detailMessage);
    }

    public FileDurationException(Throwable throwable) {
        super(throwable);
    }
}

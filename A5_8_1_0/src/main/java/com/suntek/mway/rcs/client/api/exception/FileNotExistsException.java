package com.suntek.mway.rcs.client.api.exception;

public class FileNotExistsException extends Exception {
    private static final long serialVersionUID = 1;

    public FileNotExistsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FileNotExistsException(String detailMessage) {
        super(detailMessage);
    }

    public FileNotExistsException(Throwable throwable) {
        super(throwable);
    }
}

package com.suntek.mway.rcs.client.api.exception;

public class FileSuffixException extends Exception {
    private static final long serialVersionUID = 1;

    public FileSuffixException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FileSuffixException(String detailMessage) {
        super(detailMessage);
    }

    public FileSuffixException(Throwable throwable) {
        super(throwable);
    }
}

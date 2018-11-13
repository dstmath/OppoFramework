package com.suntek.mway.rcs.client.api.exception;

public class InviteTooManyUserException extends Exception {
    private static final long serialVersionUID = 1;

    public InviteTooManyUserException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InviteTooManyUserException(String detailMessage) {
        super(detailMessage);
    }

    public InviteTooManyUserException(Throwable throwable) {
        super(throwable);
    }
}

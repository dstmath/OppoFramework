package com.suntek.mway.rcs.client.api.exception;

public class ServiceDisconnectedException extends Exception {
    private static final long serialVersionUID = 1;

    public ServiceDisconnectedException(String message) {
        super(message);
    }
}

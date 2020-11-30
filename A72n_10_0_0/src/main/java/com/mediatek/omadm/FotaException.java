package com.mediatek.omadm;

public class FotaException extends Exception {
    public FotaException() {
    }

    public FotaException(String msg) {
        super(msg);
    }

    public FotaException(Throwable reason) {
        super(reason);
    }

    public FotaException(String msg, Throwable reason) {
        super(msg, reason);
    }
}

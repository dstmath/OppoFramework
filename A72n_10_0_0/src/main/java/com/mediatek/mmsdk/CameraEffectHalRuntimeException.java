package com.mediatek.mmsdk;

public class CameraEffectHalRuntimeException extends RuntimeException {
    private Throwable mCause;
    private String mMessage;
    private final int mReason;

    public final int getReason() {
        return this.mReason;
    }

    public CameraEffectHalRuntimeException(int problem) {
        this.mReason = problem;
    }

    public CameraEffectHalRuntimeException(int problem, String msg) {
        super(msg);
        this.mReason = problem;
        this.mMessage = msg;
    }

    public CameraEffectHalRuntimeException(int problem, String msg, Throwable throwable) {
        super(msg, throwable);
        this.mReason = problem;
        this.mMessage = msg;
        this.mCause = throwable;
    }

    public CameraEffectHalRuntimeException(int problem, Throwable cause) {
        super(cause);
        this.mReason = problem;
        this.mCause = cause;
    }

    public CameraEffectHalException asChecked() {
        CameraEffectHalException e;
        Throwable th;
        String str = this.mMessage;
        if (str == null || (th = this.mCause) == null) {
            String str2 = this.mMessage;
            if (str2 != null) {
                e = new CameraEffectHalException(this.mReason, str2);
            } else {
                Throwable th2 = this.mCause;
                if (th2 != null) {
                    e = new CameraEffectHalException(this.mReason, th2);
                } else {
                    e = new CameraEffectHalException(this.mReason);
                }
            }
        } else {
            e = new CameraEffectHalException(this.mReason, str, th);
        }
        e.setStackTrace(getStackTrace());
        return e;
    }
}

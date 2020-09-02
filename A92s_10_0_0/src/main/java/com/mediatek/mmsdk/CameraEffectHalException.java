package com.mediatek.mmsdk;

import android.util.AndroidException;

public class CameraEffectHalException extends AndroidException {
    public static final int EFFECT_HAL_CLIENT_ERROR = 105;
    public static final int EFFECT_HAL_ERROR = 104;
    public static final int EFFECT_HAL_FACTORY_ERROR = 103;
    public static final int EFFECT_HAL_FEATUREMANAGER_ERROR = 102;
    public static final int EFFECT_HAL_IN_USE = 107;
    public static final int EFFECT_HAL_LISTENER_ERROR = 106;
    public static final int EFFECT_HAL_SERVICE_ERROR = 101;
    public static final int EFFECT_INITIAL_ERROR = 201;
    private final int mReason;

    public enum EffectHalError {
        EFFECT_HAL_SERVICE_ERROR,
        EFFECT_HAL_FEATUREMANAGER_ERROR,
        EFFECT_HAL_FACTORY_ERROR,
        EFFECT_HAL_ERROR,
        EFFECT_HAL_CLIENT_ERROR,
        EFFECT_HAL_LISTENER_ERROR,
        EFFECT_HAL_IN_USE
    }

    public enum EffectHalStatusError {
        EFFECT_INITIAL_ERROR
    }

    public final int getReason() {
        return this.mReason;
    }

    public CameraEffectHalException(int problem) {
        super(getDefaultMessage(problem));
        this.mReason = problem;
    }

    public CameraEffectHalException(int problem, String msg) {
        super(msg);
        this.mReason = problem;
    }

    public CameraEffectHalException(int problem, String msg, Throwable throwable) {
        super(msg, throwable);
        this.mReason = problem;
    }

    public CameraEffectHalException(int problem, Throwable throwable) {
        super(getDefaultMessage(problem), throwable);
        this.mReason = problem;
    }

    public static String getDefaultMessage(int problem) {
        if (problem != 107) {
            switch (problem) {
                case EFFECT_HAL_FEATUREMANAGER_ERROR /*{ENCODED_INT: 102}*/:
                    break;
                case 101:
                    break;
                case EFFECT_HAL_FACTORY_ERROR /*{ENCODED_INT: 103}*/:
                    break;
                default:
                    return "the problem type not in the camera hal,please add that in CameraEffectHalException ";
            }
            return "the problem type not in the camera hal,please add that in CameraEffectHalException ";
        }
        return "the problem type not in the camera hal,please add that in CameraEffectHalException ";
    }
}

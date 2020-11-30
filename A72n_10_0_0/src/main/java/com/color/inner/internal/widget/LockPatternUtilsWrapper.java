package com.color.inner.internal.widget;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;

public class LockPatternUtilsWrapper {
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    public static final String PASSWORD_TYPE_KEY = "lockscreen.password_type";
    private static final String TAG = "LockPatternUtilsWrapper";
    private LockPatternUtils mLockPatternUtils;

    public LockPatternUtilsWrapper(Context ctx) {
        this.mLockPatternUtils = new LockPatternUtils(ctx);
    }

    public LockPatternUtils getLockPatternUtils() {
        return this.mLockPatternUtils;
    }

    public boolean isLockScreenDisabled(int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.isLockScreenDisabled(userId);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean isSecure(int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.isSecure(userId);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean clearLock(byte[] savedCredential, int userHandle) {
        return clearLock(savedCredential, userHandle, false);
    }

    public boolean clearLock(byte[] savedCredential, int userHandle, boolean allowUntrustedChange) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.clearLock(savedCredential, userHandle, allowUntrustedChange);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean saveLockPassword(String password, String savedPassword, int requestedQuality, int userHandle) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.saveLockPassword(password, savedPassword, requestedQuality, userHandle);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean saveLockPassword(byte[] password, byte[] savedPassword, int requestedQuality, int userHandle) {
        return saveLockPassword(password, savedPassword, requestedQuality, userHandle, false);
    }

    public boolean saveLockPassword(byte[] password, byte[] savedPassword, int requestedQuality, int userHandle, boolean allowUntrustedChange) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.saveLockPassword(password, savedPassword, requestedQuality, userHandle, allowUntrustedChange);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public int getKeyguardStoredPasswordQuality(int userHandle) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.getKeyguardStoredPasswordQuality(userHandle);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public void reportSuccessfulPasswordAttempt(int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            lockPatternUtils.reportSuccessfulPasswordAttempt(userId);
            return;
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public long setLockoutAttemptDeadline(int userId, int timeoutMs) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.setLockoutAttemptDeadline(userId, timeoutMs);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean isVisiblePatternEnabled(int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.isVisiblePatternEnabled(userId);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean isTactileFeedbackEnabled() {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.isTactileFeedbackEnabled();
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            lockPatternUtils.setLockScreenDisabled(disable, userId);
            return;
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean saveLockPattern(byte[] pattern, byte[] savedPattern, int userId) {
        if (this.mLockPatternUtils != null) {
            return this.mLockPatternUtils.saveLockPattern(LockPatternUtils.byteArrayToPattern(pattern), savedPattern, userId);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public boolean saveLockPattern(byte[] pattern, byte[] savedPattern, int userId, boolean allowUntrustedChange) {
        if (this.mLockPatternUtils != null) {
            return this.mLockPatternUtils.saveLockPattern(LockPatternUtils.byteArrayToPattern(pattern), savedPattern, userId, allowUntrustedChange);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public byte[] verifyPattern(byte[] pattern, long challenge, int userId) throws RequestThrottledExceptionWrapper {
        if (this.mLockPatternUtils != null) {
            try {
                return this.mLockPatternUtils.verifyPattern(LockPatternUtils.byteArrayToPattern(pattern), challenge, userId);
            } catch (LockPatternUtils.RequestThrottledException e) {
                throw new RequestThrottledExceptionWrapper(e);
            }
        } else {
            throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
        }
    }

    public boolean isLockPasswordEnabled(int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.isLockPasswordEnabled(userId);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public long getLockoutAttemptDeadline(int userId) {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils != null) {
            return lockPatternUtils.getLockoutAttemptDeadline(userId);
        }
        throw new RuntimeException("have construct LockPatternUtilsWrapper with context parameter");
    }

    public static final class RequestThrottledExceptionWrapper extends Exception {
        private final LockPatternUtils.RequestThrottledException mCause;

        private RequestThrottledExceptionWrapper(LockPatternUtils.RequestThrottledException cause) {
            super((Throwable) cause);
            this.mCause = cause;
        }

        public int getTimeoutMs() {
            return this.mCause.getTimeoutMs();
        }
    }
}

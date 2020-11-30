package com.color.inner.internal.widget;

import android.os.AsyncTask;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;

public class LockPatternCheckerWrapper {
    private static final String TAG = "LockPatternCheckerWrapper";

    public interface OnVerifyCallbackWrapper {
        void onVerified(byte[] bArr, int i);
    }

    public interface OnCheckCallbackWrapper {
        void onChecked(boolean z, int i);

        default void onEarlyMatched() {
        }

        default void onCancelled() {
        }
    }

    public static AsyncTask<?, ?, ?> checkPassword(LockPatternUtilsWrapper utils, String password, int userId, OnCheckCallbackWrapper callback) {
        return checkPassword(utils, password != null ? password.getBytes() : null, userId, callback);
    }

    public static AsyncTask<?, ?, ?> checkPassword(LockPatternUtilsWrapper utils, byte[] passwordBytes, int userId, final OnCheckCallbackWrapper callback) {
        return LockPatternChecker.checkPassword(utils.getLockPatternUtils(), passwordBytes, userId, new LockPatternChecker.OnCheckCallback() {
            /* class com.color.inner.internal.widget.LockPatternCheckerWrapper.AnonymousClass1 */

            public void onEarlyMatched() {
                OnCheckCallbackWrapper.this.onEarlyMatched();
            }

            public void onChecked(boolean matched, int throttleTimeoutMs) {
                OnCheckCallbackWrapper.this.onChecked(matched, throttleTimeoutMs);
            }

            public void onCancelled() {
                OnCheckCallbackWrapper.this.onCancelled();
            }
        });
    }

    public static AsyncTask<?, ?, ?> checkPattern(LockPatternUtilsWrapper utils, byte[] bytes, int userId, final OnCheckCallbackWrapper callback) {
        return LockPatternChecker.checkPattern(utils.getLockPatternUtils(), LockPatternUtils.byteArrayToPattern(bytes), userId, new LockPatternChecker.OnCheckCallback() {
            /* class com.color.inner.internal.widget.LockPatternCheckerWrapper.AnonymousClass2 */

            public void onEarlyMatched() {
                OnCheckCallbackWrapper.this.onEarlyMatched();
            }

            public void onChecked(boolean matched, int throttleTimeoutMs) {
                OnCheckCallbackWrapper.this.onChecked(matched, throttleTimeoutMs);
            }

            public void onCancelled() {
                OnCheckCallbackWrapper.this.onCancelled();
            }
        });
    }

    public static AsyncTask<?, ?, ?> verifyPattern(LockPatternUtilsWrapper utils, byte[] bytes, long challenge, int userId, final OnVerifyCallbackWrapper callback) {
        return LockPatternChecker.verifyPattern(utils.getLockPatternUtils(), LockPatternUtils.byteArrayToPattern(bytes), challenge, userId, new LockPatternChecker.OnVerifyCallback() {
            /* class com.color.inner.internal.widget.LockPatternCheckerWrapper.AnonymousClass3 */

            public void onVerified(byte[] attestation, int throttleTimeoutMs) {
                OnVerifyCallbackWrapper.this.onVerified(attestation, throttleTimeoutMs);
            }
        });
    }

    public static AsyncTask<?, ?, ?> verifyTiedProfileChallenge(LockPatternUtilsWrapper utils, byte[] password, boolean isPattern, long challenge, int userId, final OnVerifyCallbackWrapper callback) {
        return LockPatternChecker.verifyTiedProfileChallenge(utils.getLockPatternUtils(), password, isPattern, challenge, userId, new LockPatternChecker.OnVerifyCallback() {
            /* class com.color.inner.internal.widget.LockPatternCheckerWrapper.AnonymousClass4 */

            public void onVerified(byte[] attestation, int throttleTimeoutMs) {
                OnVerifyCallbackWrapper.this.onVerified(attestation, throttleTimeoutMs);
            }
        });
    }
}

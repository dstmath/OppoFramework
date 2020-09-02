package com.android.server.locksettings;

import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.VerifyCredentialResponse;

public abstract class OppoBaseLockSettingsService extends ILockSettings.Stub {
    IOppoLockSettingsInner mOppoLockSettingsInner = null;

    public abstract LockPatternUtils getLockPatternUtils();

    public void resetTimeoutFlag(VerifyCredentialResponse verifyCredentialResponse) {
    }

    public IOppoLockSettingsInner getOppoLockSettingsInner() {
        return this.mOppoLockSettingsInner;
    }
}

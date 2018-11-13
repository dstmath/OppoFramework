package com.android.server.fingerprint.wakeup.state;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.power.FingerprintPowerManager;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.UnlockController.OrderedEvents;

public abstract class UnlockState {
    private String TAG = "FingerprintService.UnlockState";
    public long WAKEUP_SPEEDTIME_LIMIT_TO_FUSEING = 3;
    public Object mAuthInfoSyncLock = new Object();
    public int mAuthenticated = -1;
    public AuthenticatedInfo mAuthenticatedInfo = null;
    public Context mContext;
    private ExHandler mHandler;
    public IUnLocker mIUnLocker;
    public boolean mIsExitWakeUpMode = false;
    public boolean mIsScreenOff;
    public OrderedEvents mOrderedEvent = OrderedEvents.TOUCHUP;
    public boolean mState;
    public long mWakeupContinuouslyCount = 0;

    public abstract void finishAction(AuthenticatedInfo authenticatedInfo);

    public abstract void updateState();

    public UnlockState(Context context, IUnLocker unLocker, Looper looper) {
        this.mContext = context;
        this.mIUnLocker = unLocker;
        this.mHandler = new ExHandler(looper);
    }

    public void dispatchOrderedEvent(OrderedEvents event) {
        this.mOrderedEvent = event;
        updateState();
    }

    public void dispatchAuthenticated(AuthenticatedInfo authenticatedInfo) {
        synchronized (this.mAuthInfoSyncLock) {
            this.mAuthenticated = authenticatedInfo.fingerId;
            this.mAuthenticatedInfo = authenticatedInfo;
            updateState();
        }
    }

    public void dispatchScreenOff(boolean isScreenOff) {
        this.mIsScreenOff = isScreenOff;
        updateState();
    }

    public void resetAuthenticated() {
        LogUtil.d(this.TAG, "resetAuthenticated");
        synchronized (this.mAuthInfoSyncLock) {
            this.mAuthenticated = -1;
            this.mAuthenticatedInfo = null;
        }
    }

    public void notifyStateChanged(boolean state, final AuthenticatedInfo authenticatedInfo) {
        LogUtil.w(this.TAG, "mState = " + this.mState + ", isScreenOFF = " + getFPMS().isScreenOFF() + ", mIsScreenOff = " + this.mIsScreenOff + ", mAuthenticated = " + this.mAuthenticated + ", mOrderedEvent = " + this.mOrderedEvent);
        if (state) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    UnlockState.this.finishAction(authenticatedInfo);
                }
            });
        }
    }

    public FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }
}

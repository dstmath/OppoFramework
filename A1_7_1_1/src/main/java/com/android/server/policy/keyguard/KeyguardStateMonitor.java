package com.android.server.policy.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback.Stub;
import com.android.internal.widget.LockPatternUtils;
import java.io.PrintWriter;

public class KeyguardStateMonitor extends Stub {
    private static final String TAG = "KeyguardStateMonitor";
    private int mCurrentUserId;
    private volatile boolean mHasLockscreenWallpaper = false;
    private volatile boolean mInputRestricted = false;
    private volatile boolean mIsAnthTheftEnabled;
    private volatile boolean mIsShowing = false;
    private final LockPatternUtils mLockPatternUtils;
    private final OnShowingStateChangedCallback mOnShowingStateChangedCallback;
    private volatile boolean mSimSecure = true;
    private volatile boolean mTrusted = false;

    public interface OnShowingStateChangedCallback {
        void onShowingStateChanged(boolean z);
    }

    public KeyguardStateMonitor(Context context, IKeyguardService service, OnShowingStateChangedCallback showingStateChangedCallback) {
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mOnShowingStateChangedCallback = showingStateChangedCallback;
        try {
            service.addStateMonitorCallback(this);
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote Exception", e);
        }
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public boolean isSecure(int userId) {
        return (this.mLockPatternUtils.isSecure(userId) || this.mSimSecure) ? true : this.mIsAnthTheftEnabled;
    }

    public boolean isInputRestricted() {
        return this.mInputRestricted;
    }

    public boolean isTrusted() {
        return this.mTrusted;
    }

    public boolean hasLockscreenWallpaper() {
        return this.mHasLockscreenWallpaper;
    }

    public void onShowingStateChanged(boolean showing) {
        this.mIsShowing = showing;
        this.mOnShowingStateChangedCallback.onShowingStateChanged(showing);
    }

    public void onSimSecureStateChanged(boolean simSecure) {
        this.mSimSecure = simSecure;
    }

    public synchronized void setCurrentUser(int userId) {
        this.mCurrentUserId = userId;
    }

    private synchronized int getCurrentUser() {
        return this.mCurrentUserId;
    }

    public void onInputRestrictedStateChanged(boolean inputRestricted) {
        this.mInputRestricted = inputRestricted;
    }

    public void onTrustedChanged(boolean trusted) {
        this.mTrusted = trusted;
    }

    public void onHasLockscreenWallpaperChanged(boolean hasLockscreenWallpaper) {
        this.mHasLockscreenWallpaper = hasLockscreenWallpaper;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        prefix = prefix + "  ";
        pw.println(prefix + "mIsShowing=" + this.mIsShowing);
        pw.println(prefix + "mSimSecure=" + this.mSimSecure);
        pw.println(prefix + "mInputRestricted=" + this.mInputRestricted);
        pw.println(prefix + "mTrusted=" + this.mTrusted);
        pw.println(prefix + "mCurrentUserId=" + this.mCurrentUserId);
    }

    public void onAntiTheftStateChanged(boolean antiTheftEnabled) {
        this.mIsAnthTheftEnabled = antiTheftEnabled;
        Log.d(TAG, "Wenxiang:onAntiTheftStateChanged() - mIsAnthTheftEnabled = " + this.mIsAnthTheftEnabled);
    }
}

package com.android.server.policy;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import com.android.internal.policy.PhoneWindow;

public class OppoBasePhoneWindowManager implements OppoWindowManagerPolicyEx {
    static final int KEY_OFFSET_VALUE = 800;
    static final long POWER_KEY_WAKE_LOCK_TIME_OUT = 120000;
    protected IOppoPhoneWindowManagerInner mInner = null;

    enum AssistManagerLaunchMode {
        UNKNOWN,
        DEFAULT,
        LENS,
        WALKIE_TALKIE_START,
        WALKIE_TALKIE_STOP,
        PERSONAL_UPDATES
    }

    /* access modifiers changed from: package-private */
    public void extraWorkInCancelPendingPowerKeyAction() {
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyDown(KeyEvent event, boolean interactive) {
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceld) {
    }

    /* access modifiers changed from: package-private */
    public boolean getSpeechLongPressHandle() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void handlePowerKeyUpForWallet(boolean handled) {
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptPowerKeyForTelephone(KeyEvent event, boolean interactive) {
        return false;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean startPersonalAssistant(int source) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMenuLongPressed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void colorInterceptPowerKeyForAlarm() {
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptLongPowerPress() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptLongHomePress() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int colorUpdateConfigurationDependentBehaviors(int oldValue) {
        return oldValue;
    }

    /* access modifiers changed from: package-private */
    public boolean colorInterceptAppSwitchEventBeforeQueueing(KeyEvent event, boolean oldValue) {
        return oldValue;
    }

    /* access modifiers changed from: package-private */
    public void oppoHandleAssistLaunchMode(AssistManagerLaunchMode launchMode, Bundle args) {
    }

    /* access modifiers changed from: package-private */
    public boolean checkStartingWindowDrawable(Drawable originDrawable, boolean windowIsTranslucent) {
        return originDrawable != null;
    }

    /* access modifiers changed from: package-private */
    public void handleStartingWindow(PhoneWindow window) {
    }

    /* access modifiers changed from: package-private */
    public void handleStartingWindowAttrs(PhoneWindow window) {
    }

    /* access modifiers changed from: package-private */
    public boolean checkStartingWindowDrawable(Drawable originDrawable) {
        return originDrawable != null;
    }

    /* access modifiers changed from: package-private */
    public void handleStatusbarForStartingWindow(View view) {
    }
}

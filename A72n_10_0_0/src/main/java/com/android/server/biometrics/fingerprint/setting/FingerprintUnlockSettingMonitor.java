package com.android.server.biometrics.fingerprint.setting;

import android.content.Context;
import android.os.Looper;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;

public class FingerprintUnlockSettingMonitor {
    public String TAG = "FingerprintService.UnlockSettingMonitor";
    private Context mContext;
    private ExHandler mHandler;
    private Ilistener mListener;
    private String[] mSettings = {FingerprintSettings.BLACK_GESTURE_MAIN_SETTING, FingerprintSettings.BLACK_GESTURE_DOUBLE_CLICK_HOME_SETTING, "coloros_fingerprint_unlock_switch", FingerprintSettings.SIDE_FINGERPRINT_PRESS_TOUCH_MODE};
    private SettingsObserver[] mSettingsObservers;

    public FingerprintUnlockSettingMonitor(Context c, Ilistener listener, Looper l) {
        this.mContext = c;
        this.mListener = listener;
        this.mHandler = new ExHandler(l);
        initSettingMonitor();
    }

    private void initSettingMonitor() {
        this.mSettingsObservers = new SettingsObserver[this.mSettings.length];
        initSettingObserver();
    }

    private void initSettingObserver() {
        int i = 0;
        while (true) {
            String[] strArr = this.mSettings;
            if (i < strArr.length) {
                this.mSettingsObservers[i] = new SettingsObserver(this.mContext, this.mHandler, strArr[i]) {
                    /* class com.android.server.biometrics.fingerprint.setting.FingerprintUnlockSettingMonitor.AnonymousClass1 */

                    /* access modifiers changed from: package-private */
                    @Override // com.android.server.biometrics.fingerprint.setting.SettingsObserver
                    public void onUpdate(boolean on) {
                        String str = FingerprintUnlockSettingMonitor.this.TAG;
                        LogUtil.d(str, "mTarget = " + this.mTarget + ", on = " + on);
                        FingerprintUnlockSettingMonitor.this.mListener.onSettingChanged(this.mTarget, on);
                    }
                };
                i++;
            } else {
                return;
            }
        }
    }
}

package com.android.server.fingerprint.setting;

import android.content.Context;
import android.os.Looper;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;

public class FingerprintUnlockSettingMonitor {
    public String TAG = "FingerprintService.UnlockSettingMonitor";
    private Context mContext;
    private ExHandler mHandler;
    private Ilistener mListener;
    private String[] mSettings = new String[]{FingerprintSettings.BLACK_GESTURE_MAIN_SETTING, FingerprintSettings.BLACK_GESTURE_DOUBLE_CLICK_HOME_SETTING, FingerprintSettings.FINGERPRINT_UNLOCK_SWITCH};
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
        for (int i = 0; i < this.mSettings.length; i++) {
            this.mSettingsObservers[i] = new SettingsObserver(this.mContext, this.mHandler, this.mSettings[i]) {
                void onUpdate(boolean on) {
                    LogUtil.d(FingerprintUnlockSettingMonitor.this.TAG, "mTarget = " + this.mTarget + ", on = " + on);
                    FingerprintUnlockSettingMonitor.this.mListener.onSettingChanged(this.mTarget, on);
                }
            };
        }
    }
}

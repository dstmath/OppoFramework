package com.android.server.biometrics.face.setting;

import android.content.Context;
import android.os.Looper;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;

public class UnlockSettingMonitor {
    public String TAG = "FaceService.UnlockSettingMonitor";
    private Context mContext;
    private ExHandler mHandler;
    /* access modifiers changed from: private */
    public Ilistener mListener;
    private String[] mSettings = {FaceSettings.FACE_UNLOCK_SWITCH, FaceSettings.FACE_CLOSE_EYE_DETECT_SWITCH, "coloros_fingerprint_unlock_switch", "show_fingerprint_when_screen_off", FaceSettings.FACE_FINGERPRINT_COMBINATION_UNLOCK_SWITCH};
    private SettingsObserver[] mSettingsObservers;

    public UnlockSettingMonitor(Context c, Ilistener listener, Looper l) {
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
                    /* class com.android.server.biometrics.face.setting.UnlockSettingMonitor.AnonymousClass1 */

                    /* access modifiers changed from: package-private */
                    @Override // com.android.server.biometrics.face.setting.SettingsObserver
                    public void onUpdate(boolean on) {
                        String str = UnlockSettingMonitor.this.TAG;
                        LogUtil.d(str, "mTarget = " + this.mTarget + ", on = " + on);
                        UnlockSettingMonitor.this.mListener.onSettingChanged(this.mTarget, on);
                    }
                };
                i++;
            } else {
                return;
            }
        }
    }
}

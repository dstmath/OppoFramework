package com.android.server.face.setting;

import android.content.Context;
import android.os.Looper;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;

public class UnlockSettingMonitor {
    public String TAG = "FaceService.UnlockSettingMonitor";
    private Context mContext;
    private ExHandler mHandler;
    private Ilistener mListener;
    private String[] mSettings = new String[]{FaceSettings.FACE_UNLOCK_SWITCH, FaceSettings.FACE_CLOSE_EYE_DETECT_SWITCH};
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
        for (int i = 0; i < this.mSettings.length; i++) {
            this.mSettingsObservers[i] = new SettingsObserver(this.mContext, this.mHandler, this.mSettings[i]) {
                void onUpdate(boolean on) {
                    LogUtil.d(UnlockSettingMonitor.this.TAG, "mTarget = " + this.mTarget + ", on = " + on);
                    UnlockSettingMonitor.this.mListener.onSettingChanged(this.mTarget, on);
                }
            };
        }
    }
}

package com.android.server.biometrics.face.setting;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.android.server.biometrics.face.utils.LogUtil;

public abstract class SettingsObserver extends ContentObserver {
    private final String TAG = "FaceService.SettingsObserver";
    private Context mContext;
    public String mTarget;

    /* access modifiers changed from: package-private */
    public abstract void onUpdate(boolean z);

    public SettingsObserver(Context c, Handler handler, String target) {
        super(handler);
        this.mTarget = target;
        this.mContext = c;
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(this.mTarget), false, this, -1);
        update();
    }

    public void onChange(boolean selfChange) {
        update();
    }

    private void update() {
        try {
            onUpdate(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), this.mTarget, -2) != 0);
        } catch (Settings.SettingNotFoundException e) {
            LogUtil.d("FaceService.SettingsObserver", "SettingNotFoundException e:" + e.getMessage());
        }
    }
}

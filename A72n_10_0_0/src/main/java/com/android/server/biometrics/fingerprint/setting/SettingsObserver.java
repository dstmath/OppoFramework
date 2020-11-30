package com.android.server.biometrics.fingerprint.setting;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

public abstract class SettingsObserver extends ContentObserver {
    private final String TAG = "FingerprintService.SettingsObserver";
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
        boolean on = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), this.mTarget, 0) != 0) {
            on = true;
        }
        onUpdate(on);
    }
}

package com.android.server.face.setting;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Secure;

public abstract class SettingsObserver extends ContentObserver {
    private final String TAG = "FaceService.SettingsObserver";
    private Context mContext;
    public String mTarget;

    abstract void onUpdate(boolean z);

    public SettingsObserver(Context c, Handler handler, String target) {
        super(handler);
        this.mTarget = target;
        this.mContext = c;
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(this.mTarget), false, this, -1);
        update();
    }

    public void onChange(boolean selfChange) {
        update();
    }

    private void update() {
        onUpdate(Secure.getInt(this.mContext.getContentResolver(), this.mTarget, 0) != 0);
    }
}

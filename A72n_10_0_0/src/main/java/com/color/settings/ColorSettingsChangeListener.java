package com.color.settings;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;

public abstract class ColorSettingsChangeListener extends ContentObserver {
    public abstract void onSettingsChange(boolean z, String str, int i);

    public ColorSettingsChangeListener(Handler handler) {
        super(handler);
    }

    @Override // android.database.ContentObserver
    public final void onChange(boolean selfChange) {
    }

    @Override // android.database.ContentObserver
    public final void onChange(boolean selfChange, Uri uri) {
        filterUserId(selfChange, uri);
    }

    @Override // android.database.ContentObserver
    public final void onChange(boolean selfChange, Uri uri, int userId) {
        filterUserId(selfChange, uri);
    }

    private void filterUserId(boolean selfChange, Uri uri) {
        try {
            int id = Integer.valueOf(uri.getQueryParameter(ColorSettingsConfig.PARAMS_USER_ID)).intValue();
            if (ColorSettings.isSystemProcess()) {
                onSettingsChange(selfChange, uri.getPath(), id);
            } else if (id == UserHandle.myUserId()) {
                onSettingsChange(selfChange, uri.getPath(), id);
            } else {
                Log.w("CSListener", "filterUserId else selfChange=" + selfChange + " uri=" + uri.toString());
            }
        } catch (Exception e) {
            Log.e("CSListener", "filterUserId ERROR selfChange=" + selfChange + " uri=" + uri.toString(), e);
        }
    }
}

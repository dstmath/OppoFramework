package com.color.favorite;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.view.OppoWindowManager;
import com.color.direct.ColorDirectFindCmd;
import com.color.direct.ColorDirectFindCmds;
import com.color.direct.IColorDirectFindCallback;
import com.color.util.ColorLog;

public class ColorFavoriteHelper implements IColorFavoriteConstans {
    private static final String[] SELECTION_ARGS = {SETTING_KEY_ALL};
    private static final String SETTINGS_AUTHORITY = "com.coloros.favorite.settings.provider";
    private static final String SETTINGS_PATH = "settings";
    private static final Uri SETTINGS_URI = new Uri.Builder().scheme("content").authority(SETTINGS_AUTHORITY).path("settings").build();
    private static final String SETTING_KEY_ALL = "favorite_all";
    public static final String TAG = "ColorFavoriteHelper";

    public static void startCrawl(IColorDirectFindCallback callback) {
        try {
            ColorLog.d(TAG, "startCrawl");
            OppoWindowManager windowManager = new OppoWindowManager();
            ColorDirectFindCmd cmd = new ColorDirectFindCmd();
            cmd.putCommand(ColorDirectFindCmds.FIND_FAVORITE.name());
            cmd.setCallback(callback);
            windowManager.directFindCmd(cmd);
        } catch (RemoteException e) {
            ColorLog.wtf(TAG, e);
        } catch (Exception e2) {
            ColorLog.wtf(TAG, e2);
        }
    }

    public static void startSave(IColorDirectFindCallback callback) {
        try {
            ColorLog.d(TAG, "startSave");
            OppoWindowManager windowManager = new OppoWindowManager();
            ColorDirectFindCmd cmd = new ColorDirectFindCmd();
            cmd.putCommand(ColorDirectFindCmds.SAVE_FAVORITE.name());
            cmd.setCallback(callback);
            windowManager.directFindCmd(cmd);
        } catch (RemoteException e) {
            ColorLog.wtf(TAG, e);
        } catch (Exception e2) {
            ColorLog.wtf(TAG, e2);
        }
    }

    public static boolean isSettingOn(Context context) {
        return true;
    }
}

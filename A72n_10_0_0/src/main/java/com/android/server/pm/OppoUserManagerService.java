package com.android.server.pm;

import android.app.ActivityThread;
import android.common.ColorFrameworkFactory;
import android.common.OppoFeatureCache;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.util.Slog;
import com.android.server.notification.ZenModeHelper;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.io.File;

public class OppoUserManagerService extends UserManagerService {
    private static final String OPPO_USER_DATA_DIR = "/data/oppo/common/user";
    private static final String TAG = "UserManagerService";

    public OppoUserManagerService(Context context, PackageManagerService pm, OppoUserDataPreparer userDataPreparer, Object packagesLock) {
        super(context, pm, userDataPreparer, packagesLock);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.pm.OppoBaseUserManagerService
    public void prePareMultiAppUser(UserDataPreparer mUserDataPreparer, PackageManagerService mPm) {
        UserInfo userInfo;
        int storageFlags;
        if (exists(ZenModeHelper.OPPO_MULTI_USER_ID) && (userInfo = getUserInfo(ZenModeHelper.OPPO_MULTI_USER_ID)) != null) {
            if (StorageManager.isFileEncryptedNativeOrEmulated()) {
                storageFlags = 1;
            } else {
                storageFlags = 3;
            }
            mUserDataPreparer.prepareUserData(ZenModeHelper.OPPO_MULTI_USER_ID, userInfo.serialNumber, 3);
            mPm.reconcileAppsData(ZenModeHelper.OPPO_MULTI_USER_ID, storageFlags, true);
        }
    }

    @Override // com.android.server.pm.UserManagerService
    public Bundle getApplicationRestrictions(String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(73, Binder.getCallingUid());
        return super.getApplicationRestrictions(packageName);
    }

    @Override // com.android.server.pm.OppoBaseUserManagerService
    public void removeExtraConfigurationForUser(ContentResolver resolver, int userId) {
        String key;
        Settings.System.putIntForUser(resolver, "access_color_setting", -1, userId);
        if (userId == 0) {
            key = "persist.sys.themeflag";
        } else {
            key = "persist.sys.themeflag." + String.valueOf(userId);
        }
        SystemProperties.set(key, String.valueOf(0));
        if (ActivityThread.DEBUG_CONFIGURATION) {
            Log.d(TAG, "removeExtraConfigurationForUser " + userId);
        }
        ColorFrameworkFactory.getInstance().getColorThemeManager().onCleanupUserForTheme(userId);
        ColorFrameworkFactory.getInstance().getColorFontManager().onCleanupUserForFont(userId);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.pm.UserManagerService
    public void systemReady() {
        String str = "null";
        super.systemReady();
        File oppoUserDataRootDir = new File(OPPO_USER_DATA_DIR);
        try {
            if (!oppoUserDataRootDir.exists()) {
                Slog.e(TAG, "Create " + oppoUserDataRootDir.getPath());
                Os.mkdir(oppoUserDataRootDir.getPath(), 511);
            }
            Os.chmod(oppoUserDataRootDir.getPath(), 511);
        } catch (ErrnoException e) {
            Slog.e(TAG, e.getMessage() != null ? e.getMessage() : str);
            e.printStackTrace();
        }
        if (!oppoUserDataRootDir.exists()) {
            Slog.e(TAG, "Create " + oppoUserDataRootDir.getPath() + " failed.");
        }
        File oppoSystemDataDir = new File(OPPO_USER_DATA_DIR, "0");
        try {
            if (!oppoSystemDataDir.exists()) {
                Slog.e(TAG, "Create " + oppoSystemDataDir.getPath());
                Os.mkdir(oppoSystemDataDir.getPath(), 511);
            }
            Os.chmod(oppoSystemDataDir.getPath(), 511);
        } catch (ErrnoException e2) {
            if (e2.getMessage() != null) {
                str = e2.getMessage();
            }
            Slog.e(TAG, str);
            e2.printStackTrace();
        }
        if (!oppoSystemDataDir.exists()) {
            Slog.e(TAG, "Create " + oppoSystemDataDir.getPath() + " failed.");
        }
    }

    @Override // com.android.server.pm.OppoBaseUserManagerService
    public void onUserCreated(int userHandle) {
        Slog.d(TAG, "onUserCreated userId = " + userHandle);
        File oppoUserDataDir = new File(OPPO_USER_DATA_DIR, Integer.toString(userHandle));
        String path = oppoUserDataDir.getPath();
        if (!oppoUserDataDir.exists()) {
            try {
                Log.e(TAG, "path is " + path);
                Os.mkdir(path, 511);
                Os.chmod(path, 511);
            } catch (ErrnoException e) {
                Log.e(TAG, e.getMessage() != null ? e.getMessage() : "null");
                e.printStackTrace();
            }
        } else {
            Slog.d(TAG, "directory " + path + " has exists, not need create again.");
        }
        if (!oppoUserDataDir.exists()) {
            Slog.e(TAG, "Create " + oppoUserDataDir.getPath() + " failed.");
        }
    }

    @Override // com.android.server.pm.OppoBaseUserManagerService
    public void onUserRemoved(int userHandle) {
        Slog.d(TAG, "onUserRemoved userId = " + userHandle);
        File oppoUserDataDir = new File(OPPO_USER_DATA_DIR, Integer.toString(userHandle));
        if (oppoUserDataDir.exists()) {
            oppoUserDataDir.delete();
            if (oppoUserDataDir.exists()) {
                Slog.e(TAG, "Delete " + oppoUserDataDir.getPath() + " failed.");
            }
        }
    }
}

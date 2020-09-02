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
import android.util.Log;
import com.android.server.notification.ZenModeHelper;
import com.color.antivirus.IColorAntiVirusBehaviorManager;

public class OppoUserManagerService extends UserManagerService {
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
            Log.d("UserManagerService", "removeExtraConfigurationForUser " + userId);
        }
        ColorFrameworkFactory.getInstance().getColorThemeManager().onCleanupUserForTheme(userId);
        ColorFrameworkFactory.getInstance().getColorFontManager().onCleanupUserForFont(userId);
    }
}

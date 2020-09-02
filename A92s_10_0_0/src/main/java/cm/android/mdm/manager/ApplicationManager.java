package cm.android.mdm.manager;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Binder;
import android.provider.Settings;
import android.util.Log;
import cm.android.mdm.interfaces.IApplicationManager;
import cm.android.mdm.util.CustomizeServiceManager;
import cm.android.mdm.util.MethodSignature;
import cm.android.mdm.util.permission.DataBaseUtil;
import java.util.List;

public class ApplicationManager implements IApplicationManager {
    public static final int BIT_USER_CFG = 4;
    public static final char CUSTOMIZE_OVERLAY_MAIN_KEY_SEPARATOR = ':';
    public static final String CUSTOMIZE_OVERLAY_MENU_INVALID_LIST = "oppo_customize_overlay_menu_invalid_list";
    private static final String TAG = "ApplicationManager";
    private ActivityManager mAm = ((ActivityManager) this.mContext.getSystemService("activity"));
    private Context mContext;

    public ApplicationManager(Context context) {
        this.mContext = context;
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void addPersistentApp(List<String> packageNames) {
        if (packageNames != null && packageNames.size() > 0) {
            for (String packageName : packageNames) {
                CustomizeServiceManager.addProtectApplication(packageName);
            }
        }
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void removePersistentApp(List<String> packageNames) {
        if (packageNames != null && packageNames.size() > 0) {
            for (String packageName : packageNames) {
                CustomizeServiceManager.removeProtectApplication(packageName);
            }
        }
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public List<String> getPersistentApp() {
        return CustomizeServiceManager.getProtectApplicationList();
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void addDisallowedRunningApp(List<String> packageNames) {
        CustomizeServiceManager.addDisallowedRunningApp(packageNames);
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void removeDisallowedRunningApp(List<String> list) {
        if (this.mAm == null) {
        }
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void removeDisallowedRunningApp() {
        if (this.mAm == null) {
        }
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public List<String> getDisallowedRunningApp() {
        return this.mAm != null ? null : null;
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void killProcess(String packageName) {
        CustomizeServiceManager.killProcess(packageName);
    }

    @Override // cm.android.mdm.interfaces.IApplicationManager
    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(ApplicationManager.class);
    }

    /* JADX INFO: Multiple debug info for r5v1 int: [D('e' java.lang.Exception), D('val' int)] */
    @Override // cm.android.mdm.interfaces.IApplicationManager
    public void allowDrawOverlays(String packageName) {
        AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService("appops");
        ApplicationInfo appInfo = null;
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 4096);
            if (packageInfo != null) {
                appInfo = packageInfo.applicationInfo;
            }
        } catch (Exception e) {
        }
        if (appInfo != null) {
            appOps.setMode(24, appInfo.uid, appInfo.packageName, 0);
        } else {
            Log.d(TAG, "allowDrawOverlays: invalid pakageName");
        }
        String[] selectionArgs = {packageName};
        String[] projection = {DataBaseUtil.COLUMN_ALLOWED};
        ContentValues value = new ContentValues();
        Binder.clearCallingIdentity();
        Cursor cursor = null;
        int val = 0;
        try {
            Cursor cursor2 = this.mContext.getContentResolver().query(DataBaseUtil.URI_FLOAT_WINDOW, projection, "pkg_name=?", selectionArgs, null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                int columnIndex = cursor2.getColumnIndex(DataBaseUtil.COLUMN_ALLOWED);
                cursor2.moveToNext();
                val = cursor2.getInt(columnIndex);
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Exception e2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        value.put(DataBaseUtil.COLUMN_ALLOWED, Integer.valueOf(val | 4));
        this.mContext.getContentResolver().update(DataBaseUtil.URI_FLOAT_WINDOW, value, "pkg_name=?", selectionArgs);
        String packageNames = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), CUSTOMIZE_OVERLAY_MENU_INVALID_LIST, -2);
        StringBuilder disabeleMenuBuilder = new StringBuilder();
        if (packageNames == null) {
            disabeleMenuBuilder.append(packageName);
        } else if (packageNames.indexOf(packageName) == -1) {
            disabeleMenuBuilder.append(packageNames);
            disabeleMenuBuilder.append(CUSTOMIZE_OVERLAY_MAIN_KEY_SEPARATOR);
            disabeleMenuBuilder.append(packageName);
        } else {
            return;
        }
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), CUSTOMIZE_OVERLAY_MENU_INVALID_LIST, disabeleMenuBuilder.toString(), -2);
    }
}

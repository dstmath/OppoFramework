package com.color.multiapp;

import android.app.ColorActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ColorBaseResolveInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.ResolverActivity;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ColorMultiAppImpl extends ColorMultiAppDummy {
    private static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "ColorMultiAppImpl";
    private ColorActivityManager colorActivityManager;
    private AtomicBoolean mIsGet = new AtomicBoolean(false);
    private boolean mIsSupportCache = false;

    private synchronized boolean enforceActivityManager() {
        if (this.colorActivityManager == null) {
            IBinder ams = null;
            try {
                ams = ServiceManager.getService("activity");
            } catch (Exception e) {
                Log.e(TAG, "enforceActivityManager error, ams not ready yet!", e);
            }
            if (ams == null) {
                return false;
            }
            this.colorActivityManager = new ColorActivityManager();
        }
        return true;
    }

    public List<String> getAllowedMultiApp() {
        if (!isSupportMultiApp()) {
            return Collections.emptyList();
        }
        try {
            return this.colorActivityManager.getAllowedMultiApp();
        } catch (Exception e) {
            Log.e(TAG, "getAllowedMultiApp ", e);
            return null;
        }
    }

    public List<String> getCreatedMultiApp() {
        if (!isSupportMultiApp()) {
            return Collections.emptyList();
        }
        try {
            return this.colorActivityManager.getCreatedMultiApp();
        } catch (Exception e) {
            Log.e(TAG, "getCreatedMultiApp ", e);
            return null;
        }
    }

    public String getAliasMultiApp(String pkgName) {
        if (!isSupportMultiApp()) {
            return null;
        }
        try {
            return this.colorActivityManager.getAliasMultiApp(pkgName);
        } catch (Exception e) {
            Log.e(TAG, "getAliasMultiApp ", e);
            return null;
        }
    }

    public boolean isCreatedMultiApp(String pkgName) {
        List<String> createdMultiApp;
        if (!isSupportMultiApp() || pkgName == null || (createdMultiApp = getCreatedMultiApp()) == null) {
            return false;
        }
        return createdMultiApp.contains(pkgName);
    }

    public boolean isSupportMultiApp() {
        try {
            if (!this.mIsGet.get()) {
                if (!enforceActivityManager()) {
                    return false;
                }
                this.mIsSupportCache = this.colorActivityManager.getIsSupportMultiApp();
                this.mIsGet.set(true);
            }
            return this.mIsSupportCache;
        } catch (Exception e) {
            Log.e(TAG, "getAllowedMultiApp ", e);
            return false;
        }
    }

    public boolean isMultiAppUserId(int userId) {
        if (!isSupportMultiApp()) {
            return ColorMultiAppImpl.super.isMultiAppUserId(userId);
        }
        return userId == 999;
    }

    public boolean isMultiAppUri(Intent intent, String pkgName) {
        Uri output;
        if (!isSupportMultiApp()) {
            return ColorMultiAppImpl.super.isMultiAppUri(intent, pkgName);
        }
        if (intent == null || pkgName == null) {
            return false;
        }
        String targetData = intent.getDataString();
        String targetClip = null;
        boolean targetSend = false;
        boolean targetSendMultiple = false;
        boolean targetMedia = false;
        if (intent.getClipData() != null) {
            targetClip = intent.getClipData().toString();
        }
        String action = intent.getAction();
        if (action != null) {
            if ("android.intent.action.SEND".equals(action)) {
                Uri stream = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
                if (stream != null) {
                    targetSend = stream.toString().contains(pkgName);
                    if (stream.toString().contains("com.instagram")) {
                        targetSend = true;
                    }
                }
            } else if ("android.intent.action.SEND_MULTIPLE".equals(action)) {
                ArrayList<Uri> streams = intent.getParcelableArrayListExtra("android.intent.extra.STREAM");
                if (streams != null) {
                    int i = 0;
                    while (true) {
                        if (i >= streams.size()) {
                            break;
                        }
                        if (streams.get(i) != null) {
                            if (streams.get(i).toString().contains(pkgName)) {
                                targetSendMultiple = true;
                                break;
                            } else if (streams.get(i).toString().contains("com.instagram")) {
                                targetSendMultiple = true;
                                break;
                            }
                        }
                        i++;
                    }
                }
            } else if (("android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action) || "android.media.action.VIDEO_CAPTURE".equals(action)) && (output = (Uri) intent.getParcelableExtra("output")) != null) {
                targetMedia = output.toString().contains(pkgName);
            }
        }
        if (targetData != null && targetData.contains(pkgName)) {
            return true;
        }
        if ((targetClip == null || !targetClip.contains(pkgName)) && !targetSend && !targetSendMultiple && !targetMedia) {
            return false;
        }
        return true;
    }

    public int getCorrectUserId(int userId) {
        if (!isSupportMultiApp()) {
            return ColorMultiAppImpl.super.getCorrectUserId(userId);
        }
        if (userId == 999) {
            return 0;
        }
        return userId;
    }

    public List<ApplicationInfo> getInstalledApplications(IPackageManager packageManager, int flags, int userId) throws RemoteException {
        if (!isSupportMultiApp()) {
            return ColorMultiAppImpl.super.getInstalledApplications(packageManager, flags, userId);
        }
        if ((134217728 & flags) == 0) {
            return null;
        }
        if (DEBUG) {
            Log.d(TAG, " find cloned app: ");
        }
        List<ApplicationInfo> multiAppList = new ArrayList<>();
        ParceledListSlice<ApplicationInfo> sliceMore = packageManager.getInstalledApplications(128, 999);
        if (!(sliceMore == null || sliceMore.getList() == null || sliceMore.getList().isEmpty())) {
            List<String> createdList = getCreatedMultiApp();
            for (ApplicationInfo aInfo : sliceMore.getList()) {
                if (createdList.contains(aInfo.packageName)) {
                    multiAppList.add(aInfo);
                    if (DEBUG) {
                        Log.d(TAG, " multi app pkg= " + aInfo.packageName);
                    }
                }
            }
        }
        ParceledListSlice<ApplicationInfo> parceledList = packageManager.getInstalledApplications(flags, userId);
        if (!(parceledList == null || parceledList.getList() == null)) {
            multiAppList.addAll(parceledList.getList());
        }
        return multiAppList;
    }

    public ProviderInfo resolveContentProviderAsUser(IPackageManager packageManager, Context context, String name, int flags, int userId) throws RemoteException {
        String pkg;
        if (!isSupportMultiApp() || context == null || packageManager == null || (pkg = context.getPackageName()) == null || !isMultiAppUserId(userId) || !isCreatedMultiApp(pkg)) {
            return null;
        }
        Log.i(TAG, "multi app: resolveProvider is null!");
        return packageManager.resolveContentProvider(name, flags, 0);
    }

    public UserHandle getCorrectUserHandle(UserHandle user, String packageName) {
        if (!isSupportMultiApp() || user.getIdentifier() != 999 || packageName == null || isCreatedMultiApp(packageName)) {
            return user;
        }
        Slog.v(TAG, "createPackageContextAsUser(): user = 999 pkg = " + packageName);
        return new UserHandle(0);
    }

    public int fixApplicationInfo(int userId, PackageParser.Package pkg) {
        if (!isSupportMultiApp() || userId != 999 || pkg == null || pkg.applicationInfo == null || pkg.applicationInfo.packageName == null || isCreatedMultiApp(pkg.applicationInfo.packageName) || "com.android.permissioncontroller".equals(pkg.applicationInfo.packageName) || "com.coloros.securitypermission".equals(pkg.applicationInfo.packageName) || "com.google.android.permissioncontroller".equals(pkg.applicationInfo.packageName)) {
            return userId;
        }
        pkg.applicationInfo.uid = UserHandle.getUid(0, pkg.applicationInfo.uid);
        return 0;
    }

    public boolean addMultiAppInfo(Intent intent, List<ResolveInfo> from, List<ResolverActivity.ResolvedComponentInfo> into) {
        if (!isSupportMultiApp()) {
            return ColorMultiAppImpl.super.addMultiAppInfo(intent, from, into);
        }
        if (from == null || from.size() <= 0 || into == null || (intent.getFlags() & 1024) == 0) {
            return false;
        }
        ResolveInfo newInfo = from.get(0);
        ResolveInfo newInfo2 = new ResolveInfo(newInfo);
        Intent intent2 = new Intent(intent);
        intent2.addCategory("com.multiple.launcher");
        ColorBaseResolveInfo baseResolveInfo = (ColorBaseResolveInfo) ColorTypeCastingHelper.typeCasting(ColorBaseResolveInfo.class, newInfo2);
        if (baseResolveInfo != null) {
            baseResolveInfo.isMultiApp = true;
        }
        into.add(new ResolverActivity.ResolvedComponentInfo(new ComponentName(newInfo.activityInfo.packageName, newInfo.activityInfo.name), intent, newInfo));
        into.add(new ResolverActivity.ResolvedComponentInfo(new ComponentName(newInfo2.activityInfo.packageName, newInfo2.activityInfo.name), intent2, newInfo2));
        return true;
    }
}

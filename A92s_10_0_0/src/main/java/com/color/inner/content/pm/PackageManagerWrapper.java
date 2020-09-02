package com.color.inner.content.pm;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.OppoMirrorPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PackageManagerWrapper {
    public static final int COLOR_UNFREEZE_FLAG_NORMAL = 1;
    public static final int FLAG_PERMISSION_REVIEW_REQUIRED = 64;
    public static final int INSTALL_FAILED_INVALID_URI = -3;
    public static final int INSTALL_REPLACE_EXISTING = 2;
    public static final int STATE_COLOR_FREEZE_FREEZED = 2;
    public static final int STATE_OPPO_FREEZE_FREEZED = 2;
    private static final String TAG = "PackageManagerWrapper";

    public interface IPackageDataObserverWrapper {
        void onRemoveCompleted(String str, boolean z);
    }

    public interface IPackageDeleteObserverWrapper {
        void packageDeleted(String str, int i);
    }

    public interface IPackageStatsObserverWrapper {
        void onGetStatsCompleted(PackageStats packageStats, boolean z);
    }

    public static void deletePackageAsUser(Context ctx, String packageName, final IPackageDeleteObserverWrapper observer, int flags, int userId) {
        IPackageDeleteObserver.Stub mIPackageDeleteObserver = null;
        if (observer != null) {
            mIPackageDeleteObserver = new IPackageDeleteObserver.Stub() {
                /* class com.color.inner.content.pm.PackageManagerWrapper.AnonymousClass1 */

                public void packageDeleted(String packageName, int returnCode) {
                    IPackageDeleteObserverWrapper.this.packageDeleted(packageName, returnCode);
                }
            };
        }
        try {
            ctx.getPackageManager().deletePackageAsUser(packageName, mIPackageDeleteObserver, flags, userId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void deletePackage(Context ctx, String packageName, final IPackageDeleteObserverWrapper observer, int flags) {
        IPackageDeleteObserver.Stub mIPackageDeleteObserver = null;
        if (observer != null) {
            mIPackageDeleteObserver = new IPackageDeleteObserver.Stub() {
                /* class com.color.inner.content.pm.PackageManagerWrapper.AnonymousClass2 */

                public void packageDeleted(String packageName, int returnCode) {
                    IPackageDeleteObserverWrapper.this.packageDeleted(packageName, returnCode);
                }
            };
        }
        try {
            ctx.getPackageManager().deletePackage(packageName, mIPackageDeleteObserver, flags);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void grantRuntimePermission(PackageManager pm, String packageName, String permissionName, UserHandle user) {
        try {
            pm.grantRuntimePermission(packageName, permissionName, user);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int installExistingPackageAsUser(String packageName, int installReason, int userId) throws PackageManager.NameNotFoundException {
        try {
            int res = ActivityThread.getPackageManager().installExistingPackageAsUser(packageName, userId, 4194304, installReason, (List) null);
            if (res != -3) {
                return res;
            }
            throw new PackageManager.NameNotFoundException("Package " + packageName + " doesn't exist");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static List<ApplicationInfo> getInstalledApplicationsAsUser(PackageManager pm, int flags, int userId) {
        return pm.getInstalledApplicationsAsUser(flags, userId);
    }

    public static void deleteApplicationCacheFiles(Context ctx, String packageName, final IPackageDataObserverWrapper observerWrapper) {
        IPackageDataObserver.Stub mIPackageDataObserver = null;
        if (observerWrapper != null) {
            mIPackageDataObserver = new IPackageDataObserver.Stub() {
                /* class com.color.inner.content.pm.PackageManagerWrapper.AnonymousClass3 */

                public void onRemoveCompleted(String packageName, boolean succeeded) {
                    IPackageDataObserverWrapper.this.onRemoveCompleted(packageName, succeeded);
                }
            };
        }
        try {
            ctx.getPackageManager().deleteApplicationCacheFiles(packageName, mIPackageDataObserver);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void deleteApplicationCacheFilesAsUser(Context ctx, String packageName, int userId, final IPackageDataObserverWrapper observerWrapper) {
        IPackageDataObserver.Stub mIPackageDataObserver = null;
        if (observerWrapper != null) {
            mIPackageDataObserver = new IPackageDataObserver.Stub() {
                /* class com.color.inner.content.pm.PackageManagerWrapper.AnonymousClass4 */

                public void onRemoveCompleted(String packageName, boolean succeeded) {
                    IPackageDataObserverWrapper.this.onRemoveCompleted(packageName, succeeded);
                }
            };
        }
        try {
            ctx.getPackageManager().deleteApplicationCacheFilesAsUser(packageName, userId, mIPackageDataObserver);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    private PackageManagerWrapper() {
    }

    public static void getPackageSizeInfo(PackageManager pm, String packageName, final IPackageStatsObserverWrapper observer) {
        IPackageStatsObserver.Stub packageStatsObserver = null;
        if (observer != null) {
            try {
                packageStatsObserver = new IPackageStatsObserver.Stub() {
                    /* class com.color.inner.content.pm.PackageManagerWrapper.AnonymousClass5 */

                    public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
                        IPackageStatsObserverWrapper.this.onGetStatsCompleted(stats, succeeded);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        pm.getPackageSizeInfo(packageName, packageStatsObserver);
    }

    public static ComponentName getHomeActivities(PackageManager pm, List<ResolveInfo> outActivities) {
        try {
            return pm.getHomeActivities(outActivities);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static void clearCachedIconForActivity(PackageManager pm, ComponentName activityName) {
        try {
            OppoMirrorPackageManager.clearCachedIconForActivity.call(pm, new Object[]{activityName});
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static Drawable getUxIconDrawable(PackageManager pm, Drawable src, boolean isForegroundDrawable) {
        try {
            return (Drawable) OppoMirrorPackageManager.getUxIconDrawable.call(pm, new Object[]{src, Boolean.valueOf(isForegroundDrawable)});
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return src;
        }
    }

    public static Drawable getUxIconDrawable(PackageManager pm, String packageName, Drawable src, boolean isForegroundDrawable) {
        try {
            return (Drawable) callMethodByReflect(pm, "getUxIconDrawable", new Class[]{String.class, Drawable.class, Boolean.TYPE}, new Object[]{packageName, src, Boolean.valueOf(isForegroundDrawable)});
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return src;
        }
    }

    private static Object callMethodByReflect(Object object, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = object.getClass().getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }
}

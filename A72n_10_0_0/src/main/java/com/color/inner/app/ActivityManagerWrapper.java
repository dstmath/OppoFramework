package com.color.inner.app;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.pm.IPackageDataObserver;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;

public class ActivityManagerWrapper {
    private static final String TAG = "ActivityManagerWrapper";
    private static HashMap<IProcessObserverWrapper, IProcessObserver> mIProcessObserverMap = new HashMap<>();

    public interface IPackageDataObserverWrapper {
        void onRemoveCompleted(String str, boolean z);
    }

    public interface IProcessObserverWrapper {
        void onForegroundActivitiesChanged(int i, int i2, boolean z);

        void onForegroundServicesChanged(int i, int i2, int i3);

        void onProcessDied(int i, int i2);
    }

    public static void forceStopPackage(ActivityManager activityManager, String packageName) {
        try {
            activityManager.forceStopPackage(packageName);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static Configuration getConfiguration() {
        try {
            return ActivityManager.getService().getConfiguration();
        } catch (RemoteException e) {
            Log.w(TAG, "getConfiguration failed.");
            return null;
        }
    }

    public static boolean removeTask(int taskId) {
        try {
            return ActivityManager.getService().removeTask(taskId);
        } catch (RemoteException e) {
            Log.w(TAG, "removeTask failed.");
            return false;
        }
    }

    public static boolean clearApplicationUserData(ActivityManager activityManager, String packageName, final IPackageDataObserverWrapper observer) {
        IPackageDataObserver.Stub mIPackageDataObserver = null;
        if (observer != null) {
            try {
                mIPackageDataObserver = new IPackageDataObserver.Stub() {
                    /* class com.color.inner.app.ActivityManagerWrapper.AnonymousClass1 */

                    public void onRemoveCompleted(String packageName, boolean succeeded) {
                        IPackageDataObserverWrapper.this.onRemoveCompleted(packageName, succeeded);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }
        return activityManager.clearApplicationUserData(packageName, mIPackageDataObserver);
    }

    public static boolean clearApplicationUserData(String packageName, boolean keepState, final IPackageDataObserverWrapper observer, int userId) {
        IPackageDataObserver.Stub mIPackageDataObserver = null;
        if (observer != null) {
            try {
                mIPackageDataObserver = new IPackageDataObserver.Stub() {
                    /* class com.color.inner.app.ActivityManagerWrapper.AnonymousClass2 */

                    public void onRemoveCompleted(String packageName, boolean succeeded) {
                        IPackageDataObserverWrapper.this.onRemoveCompleted(packageName, succeeded);
                    }
                };
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }
        return ActivityManager.getService().clearApplicationUserData(packageName, keepState, mIPackageDataObserver, userId);
    }

    public static void setProcessLimit(int max) {
        try {
            ActivityManager.getService().setProcessLimit(max);
        } catch (Throwable th) {
            Log.w(TAG, "setProcessLimit failed.");
        }
    }

    public static long[] getProcessPss(int[] pids) {
        try {
            return ActivityManager.getService().getProcessPss(pids);
        } catch (RemoteException e) {
            Log.e(TAG, "getProcessPss failed.");
            return null;
        }
    }

    public static void resumeAppSwitches() {
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException e) {
            Log.e(TAG, "resumeAppSwitches failed.");
        }
    }

    public static boolean updateConfiguration(Configuration values) throws RemoteException {
        return ActivityManager.getService().updateConfiguration(values);
    }

    public static void registerProcessObserver(final IProcessObserverWrapper processObserverWrapper) {
        if (processObserverWrapper != null) {
            IProcessObserver.Stub mIProcessObserver = new IProcessObserver.Stub() {
                /* class com.color.inner.app.ActivityManagerWrapper.AnonymousClass3 */

                public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                    IProcessObserverWrapper.this.onForegroundActivitiesChanged(pid, uid, foregroundActivities);
                }

                public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
                    IProcessObserverWrapper.this.onForegroundServicesChanged(pid, uid, serviceTypes);
                }

                public void onProcessDied(int pid, int uid) {
                    IProcessObserverWrapper.this.onProcessDied(pid, uid);
                }
            };
            try {
                ActivityManager.getService().registerProcessObserver(mIProcessObserver);
                mIProcessObserverMap.put(processObserverWrapper, mIProcessObserver);
            } catch (Throwable th) {
                Log.e(TAG, "registerProcessObserver failed.");
            }
        }
    }

    public static void unregisterProcessObserver(IProcessObserverWrapper processObserverWrapper) {
        if (mIProcessObserverMap.get(processObserverWrapper) != null) {
            try {
                ActivityManager.getService().unregisterProcessObserver(mIProcessObserverMap.get(processObserverWrapper));
                mIProcessObserverMap.remove(processObserverWrapper);
            } catch (Throwable th) {
                Log.e(TAG, "unregisterProcessObserver failed.");
            }
        }
    }

    public static int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }
}

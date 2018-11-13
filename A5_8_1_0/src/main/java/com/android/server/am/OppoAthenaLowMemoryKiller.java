package com.android.server.am;

import android.app.ActivityManager.MemoryInfo;
import android.app.AppGlobals;
import android.app.IAthenaLMKCallback;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.storage.OppoDeviceStorageMonitorService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OppoAthenaLowMemoryKiller {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "Athena[LMK]";
    private static OppoAthenaLowMemoryKiller sInstance = null;
    private final ActivityManagerService mAms;
    private IAthenaLMKCallback mCallback = null;

    private class TokenWatcher implements DeathRecipient {
        IBinder mToken;

        TokenWatcher(IBinder token) {
            this.mToken = token;
        }

        IBinder getToken() {
            return this.mToken;
        }

        public void binderDied() {
            if (OppoAthenaLowMemoryKiller.DEBUG) {
                Log.v(OppoAthenaLowMemoryKiller.TAG, "binderDied(" + this.mToken + ")");
            }
            OppoAthenaLowMemoryKiller.this.mCallback = null;
            this.mToken = null;
        }
    }

    private OppoAthenaLowMemoryKiller(ActivityManagerService service) {
        this.mAms = service;
    }

    public static OppoAthenaLowMemoryKiller getInstance(ActivityManagerService service) {
        if (sInstance == null) {
            sInstance = new OppoAthenaLowMemoryKiller(service);
        }
        return sInstance;
    }

    public void registerCallback(IAthenaLMKCallback callback, IBinder token) {
        Log.d(TAG, "registerCallback");
        this.mCallback = callback;
        try {
            token.linkToDeath(new TokenWatcher(token), 0);
        } catch (RemoteException e) {
            Log.w(TAG, "caught remote exception in linkToDeath: ", e);
        }
    }

    private int getAvailMemory() {
        MemoryInfo mi = new MemoryInfo();
        this.mAms.getMemoryInfo(mi);
        return (int) (mi.availMem / OppoDeviceStorageMonitorService.MB_BYTES);
    }

    public int doLowMemoryScan(int thresholdMemSize, int thresholdOomAdj, List<String> whiteList, List<String> list) {
        long startTime = System.currentTimeMillis();
        Iterable lruList = null;
        try {
            if (!this.mAms.mLruProcesses.isEmpty()) {
                lruList = new ArrayList(this.mAms.mLruProcesses);
            }
            if (lruList == null) {
                return -1;
            }
            List<ProcessRecord> childList;
            HashMap<String, List<ProcessRecord>> candidateMap = new HashMap();
            for (ProcessRecord app : lruList) {
                if (app.uid > 10000 && app.curAdj >= thresholdOomAdj && app.info != null && (app.info.flags & 1) == 0) {
                    List list2 = null;
                    if (candidateMap.containsKey(app.info.packageName)) {
                        list2 = (List) candidateMap.get(app.info.packageName);
                    }
                    if (list2 == null) {
                        list2 = new ArrayList();
                    }
                    list2.add(app);
                    candidateMap.put(app.info.packageName, list2);
                }
            }
            for (ProcessRecord app2 : lruList) {
                if (app2.info != null && app2.curAdj > 200 && candidateMap.containsKey(app2.info.packageName)) {
                    childList = (List) candidateMap.get(app2.info.packageName);
                    if (childList == null) {
                        childList = new ArrayList();
                    }
                    childList.add(app2);
                    candidateMap.put(app2.info.packageName, childList);
                }
            }
            for (String packageName : candidateMap.keySet()) {
                if (whiteList == null || !whiteList.contains(packageName)) {
                    childList = (List) candidateMap.get(packageName);
                    if (childList != null) {
                        List<String> foregroundAppList = this.mAms.getAllTopPkgName();
                        if (foregroundAppList == null || !foregroundAppList.contains(packageName)) {
                            int userId = -1;
                            for (ProcessRecord app22 : childList) {
                                userId = app22.uid;
                                String reason = "Athena[LMK]:" + app22.adjType;
                                Log.w(TAG, "K [" + app22.pid + "] " + app22.processName + "(almk+" + app22.curAdj + ")");
                                app22.kill(reason, true);
                            }
                            if (this.mCallback != null) {
                                this.mCallback.onAppKilled(packageName);
                            }
                            AppGlobals.getPackageManager().setPackageStoppedState(packageName, true, userId);
                            Thread.sleep(1000);
                            if (getAvailMemory() >= thresholdMemSize) {
                                if (DEBUG) {
                                    Log.w(TAG, "done in advance");
                                }
                            }
                        } else if (DEBUG) {
                            Log.d(TAG, "detect fg: " + packageName);
                        }
                    } else {
                        continue;
                    }
                }
            }
            try {
                if (this.mCallback != null) {
                    this.mCallback.onFinish();
                }
            } catch (Exception e) {
                Log.w(TAG, "onFinish Exception " + e);
            }
            return 0;
        } catch (RemoteException e2) {
            e2.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

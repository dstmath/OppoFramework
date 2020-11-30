package com.oppo.os;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OppoUsbEnvironment extends Environment {
    private static final String DEFAULT_INTERNAL_PATH = "/storage/emulated/0";
    public static final int EXTERNAL = 2;
    public static final int INTERNAL = 1;
    private static final String MULTIAPP_INTERNAL_PATH = "/storage/emulated/999";
    public static final int NONE = -1;
    public static final int OTG = 3;
    private static final String TAG = "OppoUsbEnvironmentSys";
    private static String sExternalSdDir = null;
    private static boolean sInited = false;
    private static String sInternalSdDir = DEFAULT_INTERNAL_PATH;
    private static Object sLock = new Object();
    private static IStorageManager sMountService = null;
    private static ArrayList<String> sOtgPathes = new ArrayList<>();
    private static StorageEventListener sStorageListener = new StorageEventListener() {
        /* class com.oppo.os.OppoUsbEnvironment.AnonymousClass2 */

        @Override // android.os.storage.StorageEventListener
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            synchronized (OppoUsbEnvironment.sLock) {
                DiskInfo diskInfo = vol.getDisk();
                if (diskInfo != null) {
                    String path = vol.path;
                    if (oldState != 2 && newState == 2) {
                        if (diskInfo.isSd() && path != null) {
                            String unused = OppoUsbEnvironment.sExternalSdDir = path;
                            Log.d(OppoUsbEnvironment.TAG, "onVolumeStateChanged: sd mount. sExternalSdDir=" + OppoUsbEnvironment.sExternalSdDir);
                        }
                        if (diskInfo.isUsb() && path != null && !OppoUsbEnvironment.sOtgPathes.contains(path)) {
                            OppoUsbEnvironment.sOtgPathes.add(path);
                            Log.d(OppoUsbEnvironment.TAG, "onVolumeStateChanged: sOtgPathes.add=" + path);
                        }
                    } else if (newState != 2 && oldState == 2) {
                        if (diskInfo.isSd()) {
                            String unused2 = OppoUsbEnvironment.sExternalSdDir = null;
                            Log.d(OppoUsbEnvironment.TAG, "onVolumeStateChanged: sd unmount. sExternalSdDir=" + OppoUsbEnvironment.sExternalSdDir);
                        }
                        if (diskInfo.isUsb() && path != null && OppoUsbEnvironment.sOtgPathes.contains(path)) {
                            OppoUsbEnvironment.sOtgPathes.remove(path);
                            Log.d(OppoUsbEnvironment.TAG, "onVolumeStateChanged: sOtgPathes.remove=" + path);
                        }
                    }
                }
            }
        }
    };
    private static BroadcastReceiver sVolumeStateReceiver = new BroadcastReceiver() {
        /* class com.oppo.os.OppoUsbEnvironment.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (OppoUsbEnvironment.sLock) {
                String action = intent.getAction();
                int state = intent.getIntExtra(VolumeInfo.EXTRA_VOLUME_STATE, -1);
                String id = intent.getStringExtra(VolumeInfo.EXTRA_VOLUME_ID);
                if (state == 2 || state == 0) {
                    Log.d(OppoUsbEnvironment.TAG, "onReceive: action:" + action + ", state=" + state + ", id=" + id);
                    OppoUsbEnvironment.getVolumes();
                }
            }
        }
    };

    private static void update(Context context) {
        if (sMountService == null) {
            sMountService = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        }
        if (!sInited) {
            boolean hasPerm = true;
            sInited = true;
            getVolumes();
            Context contextApp = context.getApplicationContext();
            if (context.checkSelfPermission(Manifest.permission.WRITE_MEDIA_STORAGE) != 0) {
                hasPerm = false;
            }
            if (contextApp == null || !hasPerm) {
                Log.d(TAG, "update: hasPerm WRITE_MEDIA_STORAGE=" + hasPerm + ", contextApp=" + contextApp);
                StorageManager sm = (StorageManager) context.getSystemService("storage");
                if (sm != null) {
                    sm.registerListener(sStorageListener);
                    Log.d(TAG, "update: registerListener sStorageListener");
                    return;
                }
                return;
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(VolumeInfo.ACTION_VOLUME_STATE_CHANGED);
            contextApp.registerReceiver(sVolumeStateReceiver, filter);
            Log.d(TAG, "update: registerReceiver sVolumeStateReceiver");
        }
    }

    public static File getInternalSdDirectory(Context context) {
        String path;
        synchronized (sLock) {
            update(context);
            path = sInternalSdDir;
        }
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    public static File getExternalSdDirectory(Context context) {
        String path;
        synchronized (sLock) {
            update(context);
            path = sExternalSdDir;
        }
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    public static String getInternalSdState(Context context) {
        String path;
        StorageManager sm;
        synchronized (sLock) {
            update(context);
            path = sInternalSdDir;
        }
        if (path == null || (sm = (StorageManager) context.getSystemService("storage")) == null) {
            return "unknown";
        }
        return sm.getVolumeState(path);
    }

    public static String getExternalSdState(Context context) {
        String path;
        StorageManager sm;
        synchronized (sLock) {
            update(context);
            path = sExternalSdDir;
        }
        if (path == null || (sm = (StorageManager) context.getSystemService("storage")) == null) {
            return "unknown";
        }
        return sm.getVolumeState(path);
    }

    public static boolean isExternalSDRemoved(Context context) {
        String path;
        synchronized (sLock) {
            update(context);
            path = sExternalSdDir;
        }
        if (path == null) {
            return true;
        }
        String state = "unknown";
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (sm != null) {
            state = sm.getVolumeState(path);
        }
        Log.i(TAG, "isExternalSDRemoved: the state of volume is: " + state);
        return Environment.MEDIA_REMOVED.equals(state);
    }

    public static boolean isNestMounted() {
        boolean result;
        synchronized (sLock) {
            result = false;
            if (!(sInternalSdDir == null || sExternalSdDir == null || (!sInternalSdDir.startsWith(sExternalSdDir) && !sExternalSdDir.startsWith(sInternalSdDir)))) {
                result = true;
            }
        }
        return result;
    }

    public static List<String> getOtgPath(Context context) {
        synchronized (sLock) {
            update(context);
            if (sOtgPathes == null) {
                return null;
            }
            return (ArrayList) sOtgPathes.clone();
        }
    }

    public static boolean isVolumeMounted(Context context, String path) {
        synchronized (sLock) {
            update(context);
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (path == null || sm == null) {
            return false;
        }
        return Environment.MEDIA_MOUNTED.equals(sm.getVolumeState(path));
    }

    public static String getInternalPath(Context context) {
        String str;
        synchronized (sLock) {
            update(context);
            str = sInternalSdDir;
        }
        return str;
    }

    public static String getExternalPath(Context context) {
        String str;
        synchronized (sLock) {
            update(context);
            str = sExternalSdDir;
        }
        return str;
    }

    public static int getPathType(Context context, String path) {
        synchronized (sLock) {
            update(context);
            if (path == null) {
                return -1;
            }
            if (path.equals(sInternalSdDir)) {
                return 1;
            }
            if (path.equals(sExternalSdDir)) {
                return 2;
            }
            if (sOtgPathes == null || !sOtgPathes.contains(path)) {
                return -1;
            }
            return 3;
        }
    }

    /* access modifiers changed from: private */
    public static void getVolumes() {
        IStorageManager iStorageManager = sMountService;
        if (iStorageManager == null) {
            Log.e(TAG, "getVolumes: sMountService is null!!!");
            return;
        }
        try {
            VolumeInfo[] vols = iStorageManager.getVolumes(0);
            sExternalSdDir = null;
            sOtgPathes.clear();
            for (VolumeInfo vol : vols) {
                String path = vol.path;
                if (vol.type == 2) {
                    int userId = UserHandle.myUserId();
                    if (path != null) {
                        sInternalSdDir = path.concat("/").concat(Integer.toString(userId));
                        Log.d(TAG, "getVolumes: sInternalSdDir=" + sInternalSdDir);
                    }
                } else {
                    DiskInfo diskInfo = vol.getDisk();
                    if (diskInfo != null) {
                        if (diskInfo.isSd() && path != null) {
                            sExternalSdDir = path;
                            Log.d(TAG, "getVolumes: sExternalSdDir=" + sExternalSdDir);
                        }
                        if (diskInfo.isUsb() && path != null && !sOtgPathes.contains(path)) {
                            sOtgPathes.add(path);
                            Log.d(TAG, "getVolumes: sOtgPathes.add=" + path);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
        }
    }

    public static String getMultiappSdDirectory() {
        return MULTIAPP_INTERNAL_PATH;
    }
}

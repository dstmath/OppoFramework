package com.android.server.am;

import android.app.AppGlobals;
import android.content.pm.OppoPermissionManager;
import android.os.SystemProperties;
import android.util.Log;
import java.util.Map;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoPermissionCallback {
    private static final boolean DEBUG = false;
    private static final int MAX_CALLBACK_COUNT = 10;
    private static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    private static final String TAG = "OppoPermissionCallback";
    private static final long WAIT_TIME_LONG = 50000;
    private static final long WAIT_TIME_SHORT = 20000;
    private static Map<Integer, OppoPermissionCallback> mCallbackMap;
    public String permission;
    public int pid;
    public int res;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoPermissionCallback.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoPermissionCallback.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoPermissionCallback.<clinit>():void");
    }

    public int notifyApplication(String permission, int pid, int allowed, int token) {
        OppoPermissionCallback callback;
        if (DEBUG) {
            Log.d(TAG, "context notifyApplication, permission=" + permission + ", pid=" + pid + ", allowed=" + allowed + ", token=" + token);
        }
        synchronized (mCallbackMap) {
            callback = (OppoPermissionCallback) mCallbackMap.get(Integer.valueOf(token));
        }
        if (callback != null) {
            synchronized (callback) {
                callback.res = allowed;
                callback.notifyAll();
            }
        }
        return 1;
    }

    public String getPermission() {
        return this.permission;
    }

    public int getPid() {
        return this.pid;
    }

    public static ProcessRecord getProcessForPid(ActivityManagerService mService, int pid) {
        ProcessRecord rec;
        synchronized (mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int i = mService.mLruProcesses.size() - 1;
                while (i >= 0) {
                    rec = (ProcessRecord) mService.mLruProcesses.get(i);
                    if (rec.thread == null || rec.pid != pid) {
                        i--;
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                return null;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return rec;
    }

    /* JADX WARNING: Missing block: B:106:0x030c, code:
            if (r20.equals(com.android.server.am.OppoPermissionConstants.PERMISSION_SEND_SMS) != false) goto L_0x030e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int checkOppoPermission(String permission, int pid, int uid, ActivityManagerService mSelf) {
        ProcessRecord pr = getProcessForPid(mSelf, pid);
        String tmpPermission = permission;
        if (permission.startsWith(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
            permission = OppoPermissionConstants.PERMISSION_SEND_SMS;
        } else {
            if (permission.startsWith(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
                permission = OppoPermissionConstants.PERMISSION_CALL_PHONE;
            } else {
                if (permission.startsWith(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                    permission = PERMISSION_ACCESS_MEDIA_PROVIDER;
                } else {
                    if (permission.startsWith(OppoPermissionConstants.PERMISSION_CAMERA)) {
                        permission = OppoPermissionConstants.PERMISSION_CAMERA;
                    }
                }
            }
        }
        for (String writePermission : OppoPermissionManager.WRITE_PERMISSIONS) {
            if (permission.startsWith(writePermission)) {
                permission = writePermission;
            }
        }
        boolean isExpVersion = false;
        try {
            isExpVersion = AppGlobals.getPackageManager().hasSystemFeature("oppo.version.exp", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tmpPermission.equals("android.permission.CAMERA_MEDIA_RECORD")) {
            if (isExpVersion) {
                return 0;
            }
            tmpPermission = OppoPermissionConstants.PERMISSION_CAMERA;
        }
        if (!OppoPermissionManager.sInterceptingPermissions.contains(permission)) {
            return mSelf.checkPermission(permission, pid, uid);
        }
        if (SystemProperties.getBoolean("persist.sys.permission.enable", false) && pr != null && pr.info != null && (pr.info.flags & 1) == 0) {
            int result = 3;
            if (mCallbackMap.size() <= 10) {
                OppoPermissionCallback callback = new OppoPermissionCallback();
                Map map;
                try {
                    callback.permission = permission;
                    callback.pid = pid;
                    callback.res = 3;
                    synchronized (mCallbackMap) {
                        mCallbackMap.put(Integer.valueOf(callback.hashCode()), callback);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "checkOppoPermission, permission=" + permission + ", pid=" + pid + ", uid=" + uid + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                    }
                    synchronized (callback) {
                        int res = ((OppoActivityManagerService) mSelf).mPermissionInterceptPolicy.checkPermissionForProc(tmpPermission, pid, uid, callback.hashCode(), callback);
                        if (DEBUG) {
                            Log.d(TAG, "checkOppoPermission, checkPermissionForProc return res=" + res + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId() + " , m size " + mCallbackMap.size());
                        }
                        if (res == 1) {
                            result = -1;
                        } else if (res == 0) {
                            result = 0;
                        } else if (res == 2) {
                            if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                callback.wait(WAIT_TIME_LONG);
                            } else {
                                if (permission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                                    callback.wait(WAIT_TIME_LONG);
                                } else {
                                    callback.wait(WAIT_TIME_SHORT);
                                }
                            }
                            if (DEBUG) {
                                Log.d(TAG, "checkOppoPermission, notify continue callback.res=" + callback.res + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                            }
                            if (callback.res != 1) {
                                if (callback.res == 3) {
                                }
                                if (callback.res == 0) {
                                    result = 0;
                                }
                            }
                            result = -1;
                        }
                    }
                    if (DEBUG) {
                        Log.d(TAG, "checkOppoPermission, finally callback size=" + mCallbackMap.size() + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                    }
                    map = mCallbackMap;
                    synchronized (map) {
                        mCallbackMap.remove(Integer.valueOf(callback.hashCode()));
                    }
                } catch (Exception e2) {
                    try {
                        e2.printStackTrace();
                        if (DEBUG) {
                            Log.d(TAG, "checkOppoPermission, finally callback size=" + mCallbackMap.size() + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                        }
                        map = mCallbackMap;
                        synchronized (map) {
                            mCallbackMap.remove(Integer.valueOf(callback.hashCode()));
                        }
                    } catch (Throwable th) {
                        if (DEBUG) {
                            Log.d(TAG, "checkOppoPermission, finally callback size=" + mCallbackMap.size() + ", permission=" + callback.getPermission() + ", token=" + callback.hashCode() + ", Thread=" + Thread.currentThread().getId());
                        }
                        synchronized (mCallbackMap) {
                            mCallbackMap.remove(Integer.valueOf(callback.hashCode()));
                        }
                    }
                }
            }
            if (-1 == result) {
                return -1;
            }
            if (result == 0) {
                return 0;
            }
            if (OppoPermissionManager.OPPO_DEFINED_PERMISSIONS.contains(permission)) {
                return 0;
            }
            return mSelf.checkPermission(permission, pid, uid);
        } else if (OppoPermissionManager.OPPO_DEFINED_PERMISSIONS.contains(permission)) {
            return 0;
        } else {
            return mSelf.checkPermission(permission, pid, uid);
        }
    }
}

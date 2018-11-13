package com.android.internal.app;

import android.app.ActivityManager;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.wifi.IWifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;
import android.util.Slog;
import android.view.IWindowManager;
import com.android.server.LocationManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.ipomanager.ActivityManagerPlusConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class ShutdownManager {
    private static final String IPOWiFiEnable = "persist.sys.ipo.wifi";
    private static final String TAG = "ShutdownManager";
    private static int airplaneModeState;
    private static boolean doAudioUnmute;
    private static boolean isBSPPackage;
    static final String[] mHardCodePrebootKillList = null;
    static final String[] mHardCodeShutdownList = null;
    private static boolean mMerged;
    private static PowerManager mPowerManager;
    private static Handler sHandler;
    private static ShutdownManager sInstance;
    static ArrayList<String> sPrebootKillList;
    public static ArrayList<String> sShutdownWhiteList;
    private static int sdState;
    private static boolean setMusicMuted;
    private static int wifiState;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.app.ShutdownManager.<clinit>():void, dex:  in method: com.android.internal.app.ShutdownManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.app.ShutdownManager.<clinit>():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.app.ShutdownManager.<clinit>():void, dex:  in method: com.android.internal.app.ShutdownManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ShutdownManager.<clinit>():void");
    }

    public static native int GetMasterMute();

    public static native int GetStreamMute(int i);

    public static native int SetMasterMute(boolean z);

    public static native int SetStreamMute(int i, boolean z);

    private static void parseStringIntoArrary(String str, String str2, ArrayList<String> arrayList) {
        String[] split = str2.split(str);
        for (Object add : split) {
            arrayList.add(add);
        }
    }

    public static boolean addShutdownWhiteList(String str) {
        if (sShutdownWhiteList.contains(str)) {
            Slog.w(TAG, "duplicated whitelist: " + str);
            return false;
        }
        sShutdownWhiteList.add(str);
        Slog.v(TAG, "add whitelist: " + str);
        return true;
    }

    public static boolean removeShutdownWhiteList(String str) {
        if (!sShutdownWhiteList.contains(str)) {
            return false;
        }
        sShutdownWhiteList.remove(str);
        Slog.v(TAG, "remove whitelist: " + str);
        return true;
    }

    public static boolean inShutdownWhiteList(String str) {
        return str != null && sShutdownWhiteList.contains(str);
    }

    public static boolean addPrebootKillProcess(String str) {
        if (sPrebootKillList.contains(str)) {
            return false;
        }
        sPrebootKillList.add(str);
        Slog.v(TAG, "add PrebootKill: " + str);
        return true;
    }

    public static boolean removePrebootKillProcess(String str) {
        if (!sPrebootKillList.contains(str)) {
            return false;
        }
        sPrebootKillList.remove(str);
        Slog.v(TAG, "remove PrebootKill: " + str);
        return true;
    }

    public void ShutdownManager() {
    }

    public static ShutdownManager getInstance() {
        if (sInstance == null) {
            sInstance = new ShutdownManager();
        }
        return sInstance;
    }

    private void muteSystem(Context context) {
        if (!isBSPPackage) {
            if (1 != GetMasterMute()) {
                doAudioUnmute = true;
                SetMasterMute(true);
                return;
            }
            doAudioUnmute = false;
        }
    }

    private void unmuteSystem(Context context) {
        if (!isBSPPackage && doAudioUnmute) {
            SetMasterMute(false);
        }
    }

    public void recoverSystem(Context context) {
    }

    String getCurrentIME(Context context) {
        String string = Secure.getString(context.getContentResolver(), "default_input_method");
        if (string == null) {
            return null;
        }
        return string.substring(0, string.indexOf("/"));
    }

    public ArrayList<String> getAccessibilityServices(Context context) {
        if (Secure.getInt(context.getContentResolver(), "accessibility_enabled", 0) != 0) {
            String string = Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
            if (string == null || string.equals(IElsaManager.EMPTY_PACKAGE)) {
                Slog.i(TAG, "no accessibility services exist");
                return null;
            }
            SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(':');
            simpleStringSplitter.setString(string);
            ArrayList<String> arrayList = new ArrayList();
            while (simpleStringSplitter.hasNext()) {
                String next = simpleStringSplitter.next();
                if (next != null && next.length() > 0) {
                    ComponentName unflattenFromString = ComponentName.unflattenFromString(next);
                    arrayList.add(unflattenFromString.getPackageName());
                    Log.v(TAG, "AccessibilityService Package Name = " + unflattenFromString.getPackageName());
                }
            }
            return arrayList;
        }
        Slog.i(TAG, "accessibility is disabled");
        return null;
    }

    public static int prebootKillProcessListSize() {
        return sPrebootKillList.size();
    }

    public void prebootKillProcess(Context context) {
        IActivityManager asInterface = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        Iterator it = sPrebootKillList.iterator();
        while (it.hasNext()) {
            Slog.v(TAG, "PrebootKill = " + ((String) it.next()));
        }
        if (asInterface != null && sPrebootKillList.size() > 0) {
            try {
                for (RunningAppProcessInfo runningAppProcessInfo : asInterface.getRunningAppProcesses()) {
                    if (sPrebootKillList.contains(runningAppProcessInfo.processName)) {
                        Slog.i(TAG, "killProcess: " + runningAppProcessInfo.processName);
                        Process.killProcess(runningAppProcessInfo.pid);
                    }
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException: " + e);
            }
        }
    }

    public void forceStopKillPackages(Context context) {
        IActivityManager asInterface = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        IPackageManager asInterface2 = Stub.asInterface(ServiceManager.getService("package"));
        WallpaperManager instance = WallpaperManager.getInstance(context);
        Iterator it = sShutdownWhiteList.iterator();
        while (it.hasNext()) {
            Slog.v(TAG, "whitelist = " + ((String) it.next()));
        }
        if (asInterface2 != null && asInterface != null && instance != null) {
            try {
                String str;
                int i;
                int i2;
                int i3;
                int currentUser = ActivityManager.getCurrentUser();
                WallpaperInfo wallpaperInfo = instance.getWallpaperInfo();
                String packageName = wallpaperInfo == null ? null : wallpaperInfo.getPackageName();
                if (wallpaperInfo == null) {
                    str = null;
                } else {
                    str = wallpaperInfo.getServiceInfo().processName;
                }
                int packageUid = asInterface2.getPackageUid(packageName, DumpState.DUMP_INTENT_FILTER_VERIFIERS, currentUser);
                Slog.v(TAG, "Current Wallpaper = " + packageName + "(" + str + "), uid = " + packageUid);
                CharSequence currentIME = getCurrentIME(context);
                Slog.v(TAG, "Current IME: " + currentIME);
                for (RunningServiceInfo runningServiceInfo : asInterface.getServices(30, 0)) {
                    if (!(0 == runningServiceInfo.restarting || sShutdownWhiteList.contains(runningServiceInfo.service.getPackageName()) || runningServiceInfo.service.getPackageName().equals(packageName) || runningServiceInfo.service.getPackageName().equals(currentIME) || runningServiceInfo.service.getPackageName().contains(currentIME))) {
                        Slog.v(TAG, "force stop the scheduling service:" + runningServiceInfo.service.getPackageName());
                        asInterface.forceKillPackage(runningServiceInfo.service.getPackageName(), -1, "IPO_FORCEKILL");
                    }
                }
                List<RunningAppProcessInfo> runningAppProcesses = asInterface.getRunningAppProcesses();
                ArrayList accessibilityServices = getAccessibilityServices(context);
                ArrayList arrayList = new ArrayList();
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                List<ResolveInfo> list = asInterface2.queryIntentActivities(intent, null, 0, 0).getList();
                if (list.size() > 0) {
                    for (ResolveInfo resolveInfo : list) {
                        ComponentInfo componentInfo = resolveInfo.activityInfo == null ? resolveInfo.serviceInfo : resolveInfo.activityInfo;
                        if (componentInfo.processName != null) {
                            Slog.i(TAG, "home process: " + componentInfo.processName);
                            for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                                if (runningAppProcessInfo.processName.equals(componentInfo.processName)) {
                                    Slog.i(TAG, "found running home process shown in above log");
                                    runningAppProcesses.remove(runningAppProcessInfo);
                                    runningAppProcesses.add(0, runningAppProcessInfo);
                                    arrayList.add(runningAppProcessInfo.processName);
                                    break;
                                }
                            }
                        }
                        Slog.i(TAG, "query home process name fail!");
                    }
                } else {
                    Slog.i(TAG, "query home activity fail!");
                }
                for (RunningAppProcessInfo runningAppProcessInfo2 : runningAppProcesses) {
                    if (runningAppProcessInfo2.processName.contains(currentIME)) {
                        i = runningAppProcessInfo2.uid;
                        Slog.v(TAG, "Current IME uid: " + i);
                        i2 = i;
                        break;
                    }
                }
                i2 = -1;
                for (RunningAppProcessInfo runningAppProcessInfo22 : runningAppProcesses) {
                    Object i32;
                    Object obj = null;
                    Slog.v(TAG, "processName: " + runningAppProcessInfo22.processName + " pid: " + runningAppProcessInfo22.pid + " uid: " + runningAppProcessInfo22.uid);
                    if (sShutdownWhiteList.contains(runningAppProcessInfo22.processName) || runningAppProcessInfo22.processName.equals(str) || runningAppProcessInfo22.processName.contains(currentIME) || (runningAppProcessInfo22.processName.equals("com.google.android.apps.genie.geniewidget") && str != null && str.equals("com.google.android.apps.maps:MapsWallpaper"))) {
                        if (runningAppProcessInfo22.processName.contains(currentIME)) {
                            currentUser = 1;
                            i32 = null;
                        } else {
                            i32 = null;
                        }
                    } else if (runningAppProcessInfo22.uid == 1000) {
                        Slog.v(TAG, "process = " + runningAppProcessInfo22.processName);
                        i32 = 1;
                    } else if (runningAppProcessInfo22.uid != packageUid) {
                        if (i2 == -1 || runningAppProcessInfo22.uid != i2) {
                            String[] packagesForUid = asInterface2.getPackagesForUid(runningAppProcessInfo22.uid);
                            if (packagesForUid != null) {
                                i32 = packagesForUid.length;
                            } else {
                                i32 = 0;
                            }
                            for (int i4 = 0; i4 < i32; i4++) {
                                if (sShutdownWhiteList.contains(packagesForUid[i4])) {
                                    Slog.v(TAG, "uid-process = " + runningAppProcessInfo22.processName + ", whitelist item = " + packagesForUid[i4]);
                                    i32 = null;
                                    break;
                                }
                            }
                            i32 = 1;
                        } else if (runningAppProcessInfo22.processName.contains(currentIME)) {
                            i32 = 1;
                        } else {
                            Slog.i(TAG, "IME related process = " + runningAppProcessInfo22.processName);
                            i32 = null;
                            currentUser = 1;
                        }
                    } else if (runningAppProcessInfo22.processName.equals(str)) {
                        i32 = 1;
                    } else {
                        Slog.i(TAG, "wallpaper related process = " + runningAppProcessInfo22.processName);
                        i32 = null;
                        currentUser = 1;
                    }
                    if (i32 != null) {
                        i32 = 0;
                        while (i32 < runningAppProcessInfo22.pkgList.length) {
                            if (accessibilityServices != null && accessibilityServices.contains(runningAppProcessInfo22.pkgList[i32])) {
                                Slog.i(TAG, "skip accessibility service: " + runningAppProcessInfo22.pkgList[i32]);
                            } else {
                                Slog.i(TAG, "forceStopPackage: " + runningAppProcessInfo22.processName + " pid: " + runningAppProcessInfo22.pid);
                                asInterface.forceKillPackage(runningAppProcessInfo22.pkgList[i32], -1, "IPO_FORCEKILL");
                            }
                            i32++;
                        }
                    }
                    if (obj != null) {
                        Slog.i(TAG, "killProcess: " + runningAppProcessInfo22.processName);
                        Process.killProcess(runningAppProcessInfo22.pid);
                    }
                    if (ActivityManagerPlusConnection.inBooting()) {
                        Slog.w(TAG, "stop killing for IPO boot");
                        break;
                    }
                }
                if (!ActivityManagerPlusConnection.inBooting()) {
                    List list2 = asInterface.getRecentTasks(30, 15, -2).getList();
                    i = 0;
                    while (true) {
                        i32 = i;
                        if (i32 >= list2.size()) {
                            break;
                        }
                        if (((RecentTaskInfo) list2.get(i32)).baseActivity != null) {
                            String packageName2 = ((RecentTaskInfo) list2.get(i32)).baseActivity.getPackageName();
                            if (!sShutdownWhiteList.contains(packageName2)) {
                                Slog.i(TAG, "forceStopPackage: " + packageName2 + " in recentTaskList");
                                asInterface.forceKillPackage(packageName2, -1, "IPO_FORCEKILL");
                            }
                        }
                        i = i32 + 1;
                    }
                } else {
                    Slog.w(TAG, "Stop killing recentTaskList");
                }
                List<ProcessErrorStateInfo> processesInErrorState = asInterface.getProcessesInErrorState();
                if (processesInErrorState == null) {
                    Slog.i(TAG, "No process in error state");
                    return;
                }
                for (ProcessErrorStateInfo processErrorStateInfo : processesInErrorState) {
                    Slog.i(TAG, "killProcess " + processErrorStateInfo.processName + " in '" + processErrorStateInfo.shortMsg + " state");
                    Process.killProcess(processErrorStateInfo.pid);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException: " + e);
            }
        }
    }

    public void shutdown(Context context) {
        boolean z = true;
        muteSystem(context);
        Intent intent = new Intent("android.intent.action.black.mode");
        intent.putExtra("_black_mode", true);
        context.sendBroadcast(intent);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        mPowerManager = (PowerManager) context.getSystemService("power");
        mPowerManager.goToSleep(SystemClock.uptimeMillis());
        try {
            boolean z2 = System.getIntForUser(context.getContentResolver(), "accelerometer_rotation", 1, -2) != 0;
            IWindowManager asInterface = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (asInterface != null && z2) {
                asInterface.freezeRotation(0);
                System.putIntForUser(context.getContentResolver(), "accelerometer_rotation", 0, -2);
                System.putIntForUser(context.getContentResolver(), "accelerometer_rotation_restore", 1, -2);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "check Rotation: context object is null when get Rotation");
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        Slog.v(TAG, "start ipod");
        SystemProperties.set("ctl.start", "ipod");
        for (int i = 0; i < 5 && !SystemProperties.get("init.svc.ipod", null).equals("running"); i++) {
            Slog.v(TAG, "waiting ipod (" + i + ")");
            try {
                Thread.sleep(20);
            } catch (InterruptedException e3) {
                Slog.e(TAG, "interrupted while waiting ipod: " + e3);
            }
        }
        elapsedRealtime = (elapsedRealtime + 1500) - SystemClock.elapsedRealtime();
        if (elapsedRealtime > 0) {
            z = false;
        }
        if (!z) {
            try {
                Slog.v(TAG, "sleep " + elapsedRealtime + "ms for ipowin");
                Thread.sleep(elapsedRealtime);
            } catch (Throwable e4) {
                Log.e(TAG, "Thread sleep exception: ", e4);
            }
        }
    }

    public void enterShutdown(Context context) {
        if (!isBSPPackage && GetStreamMute(3) == 0) {
            setMusicMuted = true;
            SetStreamMute(3, true);
        }
        IActivityManager asInterface = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        Slog.i(TAG, "Force-stop GMap");
        try {
            asInterface.forceKillPackage("com.google.android.apps.maps", -1, "IPO_FORCEKILL");
        } catch (RemoteException e) {
            Slog.i(TAG, "RemoteExcepiton while forcekill google maps: " + e);
        }
    }

    public void finishShutdown(final Context context) {
        if (UserManager.supportsMultipleUsers()) {
            int currentUser = ActivityManager.getCurrentUser();
            Slog.i(TAG, "current userId: " + currentUser);
            if (currentUser != 0) {
                try {
                    ActivityManagerNative.getDefault().switchUser(0);
                } catch (Throwable e) {
                    Slog.e(TAG, "Couldn't switch user.", e);
                }
            }
        }
        sHandler.postDelayed(new Runnable() {
            public void run() {
                ShutdownManager.this.forceStopKillPackages(context);
            }
        }, 0);
        if (!isBSPPackage && setMusicMuted) {
            setMusicMuted = false;
            SetStreamMute(3, false);
        }
    }

    public void saveStates(Context context) {
        IWifiManager asInterface = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
        if (asInterface == null) {
            try {
                Slog.i(TAG, " can not get the IWifiManager binder");
            } catch (RemoteException e) {
                Slog.i(TAG, "Wi-Fi operation failed: " + e);
            }
        } else {
            asInterface.setWifiEnabledForIPO(false);
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            Slog.i(TAG, " can not get ConnectivityManager");
            return;
        }
        connectivityManager.stopTethering(0);
        Slog.i(TAG, " Turn off WIFI AP");
    }

    public void preRestoreStates(Context context) {
        unmuteSystem(context);
    }

    public void restoreStates(Context context) {
        IWifiManager asInterface = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
        if (asInterface == null) {
            try {
                Slog.i(TAG, " can not get the IWifiManager binder");
                return;
            } catch (RemoteException e) {
                Slog.i(TAG, "Wi-Fi operation failed: " + e);
                return;
            }
        }
        asInterface.setWifiEnabledForIPO(true);
    }

    /* JADX WARNING: Missing block: B:10:0x0030, code:
            r3.close();
            r0.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void copyFileTo(String str, String str2) {
        if (str != null && str2 != null) {
            ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
            StrictMode.allowThreadDiskWrites();
            try {
                File file = new File(str);
                File file2 = new File(str2);
                if (file.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    FileOutputStream fileOutputStream = new FileOutputStream(file2);
                    byte[] bArr = new byte[5120];
                    while (true) {
                        int read = fileInputStream.read(bArr);
                        if (read <= 0) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, read);
                    }
                    return;
                }
                Slog.d(TAG, str + " not exist...");
                StrictMode.setThreadPolicy(allowThreadDiskReads);
            } catch (FileNotFoundException e) {
                Slog.e(TAG, "file not found: " + e);
            } catch (IOException e2) {
                Slog.e(TAG, "IO exception: " + e2);
            } finally {
                StrictMode.setThreadPolicy(allowThreadDiskReads);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0057 A:{SYNTHETIC, Splitter: B:21:0x0057} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0089 A:{SYNTHETIC, Splitter: B:31:0x0089} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b1 A:{SYNTHETIC, Splitter: B:40:0x00b1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeStringToFile(String str, String str2) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (str != null) {
            File file = new File(str);
            ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
            StrictMode.allowThreadDiskWrites();
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
                try {
                    fileOutputStream.write(str2.getBytes());
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                            Slog.e(TAG, "IO exception: " + e3);
                        }
                    }
                    StrictMode.setThreadPolicy(allowThreadDiskReads);
                } catch (FileNotFoundException e4) {
                    e2 = e4;
                    try {
                        Slog.e(TAG, e2.toString());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e32) {
                                Slog.e(TAG, "IO exception: " + e32);
                            }
                        }
                        StrictMode.setThreadPolicy(allowThreadDiskReads);
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e5) {
                                Slog.e(TAG, "IO exception: " + e5);
                            }
                        }
                        StrictMode.setThreadPolicy(allowThreadDiskReads);
                        throw th;
                    }
                } catch (IOException e6) {
                    e32 = e6;
                    Slog.e(TAG, e32.toString());
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e322) {
                            Slog.e(TAG, "IO exception: " + e322);
                        }
                    }
                    StrictMode.setThreadPolicy(allowThreadDiskReads);
                }
            } catch (FileNotFoundException e7) {
                e2 = e7;
                fileOutputStream = null;
                Slog.e(TAG, e2.toString());
                if (fileOutputStream != null) {
                }
                StrictMode.setThreadPolicy(allowThreadDiskReads);
            } catch (IOException e8) {
                e322 = e8;
                fileOutputStream = null;
                Slog.e(TAG, e322.toString());
                if (fileOutputStream != null) {
                }
                StrictMode.setThreadPolicy(allowThreadDiskReads);
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = null;
                if (fileOutputStream != null) {
                }
                StrictMode.setThreadPolicy(allowThreadDiskReads);
                throw th;
            }
        }
    }

    public static void stopFtraceCapture() {
        if (SystemProperties.get("sys.shutdown.ftrace").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            Slog.d(TAG, "stop ftrace");
            writeStringToFile("/sys/kernel/debug/tracing/tracing_on", "0");
            Slog.d(TAG, "saving ftrace to /data/misc/shutdown_ftrace.txt");
            copyFileTo("/sys/kernel/debug/tracing/trace", "/data/misc/shutdown_ftrace.txt");
            Slog.d(TAG, "ftrace saving done, restart ftrace");
            writeStringToFile("/sys/kernel/debug/tracing/tracing_on", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
    }

    public static void startFtraceCapture() {
        if (SystemProperties.get("sys.shutdown.ftrace").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            Slog.d(TAG, "shutdown ftrace enabled!");
            String str = SystemProperties.get("sys.shutdown.ftrace.size");
            if (str.matches("^\\d+$")) {
                Slog.d(TAG, "buffer_size_kb = " + str);
            } else {
                Slog.d(TAG, "buffer_size_kb = " + str + ", restore to 11MB");
                str = "11256";
            }
            writeStringToFile("/sys/kernel/debug/tracing/buffer_size_kb", str);
            writeStringToFile("/sys/kernel/debug/tracing/trace", IElsaManager.EMPTY_PACKAGE);
        }
    }
}

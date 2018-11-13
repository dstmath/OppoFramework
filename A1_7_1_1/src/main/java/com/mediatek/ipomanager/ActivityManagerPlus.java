package com.mediatek.ipomanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import com.android.internal.app.ShutdownManager;
import com.android.internal.policy.PhoneWindow;
import com.android.server.LocationManagerService;
import com.android.server.usb.UsbAudioDevice;
import com.mediatek.am.AMEventHookAction;
import com.mediatek.am.AMEventHookData.BeforeSendBroadcast;
import com.mediatek.am.AMEventHookData.SystemReady;
import com.mediatek.am.AMEventHookData.SystemReady.Index;
import com.mediatek.am.AMEventHookResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public final class ActivityManagerPlus {
    private static final String TAG = "ActivityManagerPlus";
    private static View mIPOWin;
    private static ActivityManagerPlus sInstance;
    final ArrayList<String> mBoostDownloadingAppList;
    private Context mContext;
    final Handler mHandler;
    final HandlerThread mHandlerThread;
    private boolean mIPOAlarmBoot;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.ipomanager.ActivityManagerPlus.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.ipomanager.ActivityManagerPlus.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.ipomanager.ActivityManagerPlus.<clinit>():void");
    }

    public static ActivityManagerPlus getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ActivityManagerPlus(context);
        }
        return sInstance;
    }

    public static ActivityManagerPlus getInstance(SystemReady systemReady) {
        return getInstance((Context) systemReady.get(Index.context));
    }

    public AMEventHookResult filterBroadcast(BeforeSendBroadcast beforeSendBroadcast, AMEventHookResult aMEventHookResult) {
        if (!SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            return aMEventHookResult;
        }
        Intent intent = (Intent) beforeSendBroadcast.get(BeforeSendBroadcast.Index.intent);
        String action = intent.getAction();
        if (!"android.intent.action.BOOT_COMPLETED".equals(action) && !"android.intent.action.ACTION_SHUTDOWN".equals(action) && !"android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
            return aMEventHookResult;
        }
        if (intent.getIntExtra("_mode", 0) != 0) {
            List list;
            if ("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                list = (List) beforeSendBroadcast.get(BeforeSendBroadcast.Index.filterStaticList);
                List list2 = (List) beforeSendBroadcast.get(BeforeSendBroadcast.Index.filterDynamicList);
                Iterator it = ShutdownManager.sShutdownWhiteList.iterator();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    list.add(str);
                    list2.add(str);
                    Slog.i(TAG, "filterBroadcast:" + str);
                }
                aMEventHookResult.addAction(AMEventHookAction.AM_FilterStaticReceiver);
                aMEventHookResult.addAction(AMEventHookAction.AM_FilterRegisteredReceiver);
            } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                list = (List) beforeSendBroadcast.get(BeforeSendBroadcast.Index.filterDynamicList);
                Iterator it2 = ShutdownManager.sShutdownWhiteList.iterator();
                while (it2.hasNext()) {
                    action = (String) it2.next();
                    list.add(action);
                    Slog.i(TAG, "filterBroadcast:" + action);
                }
                aMEventHookResult.addAction(AMEventHookAction.AM_FilterRegisteredReceiver);
            }
            return aMEventHookResult;
        }
        Slog.i(TAG, "normal boot/shutdown");
        return aMEventHookResult;
    }

    private ActivityManagerPlus(Context context) {
        this.mHandlerThread = new HandlerThread("AMPlus", -2);
        this.mIPOAlarmBoot = false;
        this.mBoostDownloadingAppList = new ArrayList();
        Slog.i(TAG, "start ActivityManagerPlus");
        this.mContext = context;
        Slog.i(TAG, "support wl!");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        startHandler();
    }

    final void startHandler() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOST_DOWNLOADING");
        intentFilter.addAction("android.intent.action.ACTION_BOOT_IPO");
        intentFilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        intentFilter.addAction("android.intent.action.black.mode");
        intentFilter.addAction("android.intent.action.normal.boot");
        intentFilter.setPriority(1000);
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        Slog.i(TAG, "startHandler!");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(final Context context, Intent intent) {
                Slog.i(ActivityManagerPlus.TAG, "Receive: " + intent);
                final ShutdownManager instance = ShutdownManager.getInstance();
                String action = intent.getAction();
                if ("android.intent.action.BOOST_DOWNLOADING".equals(action)) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String string = extras.getString("package_name");
                        if (string != null) {
                            Boolean valueOf = Boolean.valueOf(extras.getBoolean("enabled", false));
                            int size = ActivityManagerPlus.this.mBoostDownloadingAppList.size();
                            int i = size - 1;
                            Boolean valueOf2 = Boolean.valueOf(false);
                            if (size != 0) {
                                while (i >= 0 && !((String) ActivityManagerPlus.this.mBoostDownloadingAppList.get(i)).equals(string)) {
                                    i--;
                                }
                                if (i >= 0) {
                                    valueOf2 = Boolean.valueOf(true);
                                } else {
                                    valueOf2 = Boolean.valueOf(false);
                                }
                            }
                            if (valueOf.booleanValue() && !valueOf2.booleanValue()) {
                                ActivityManagerPlus.this.mBoostDownloadingAppList.add(string);
                            } else if (!valueOf.booleanValue() && valueOf2.booleanValue()) {
                                ActivityManagerPlus.this.mBoostDownloadingAppList.remove(i);
                            }
                        }
                    }
                } else if ("android.intent.action.ACTION_PREBOOT_IPO".equals(action)) {
                    Slog.i(ActivityManagerPlus.TAG, "ipo PREBOOT_IPO");
                    Slog.i(ActivityManagerPlus.TAG, "re-launch launcher");
                    Intent intent2 = new Intent("android.intent.action.MAIN");
                    intent2.addCategory("android.intent.category.HOME");
                    intent2.setFlags(intent2.getFlags() | 268435456);
                    context.startActivity(intent2);
                    if (ShutdownManager.prebootKillProcessListSize() == 0) {
                        Slog.i(ActivityManagerPlus.TAG, "prebootKillProcess list empty, don't need to perform kill");
                    } else {
                        ActivityManagerPlus.this.mHandler.postDelayed(new Runnable(this) {
                            static final String C2K_PROPERTY = "ro.boot.opt_c2k_support";
                            static final String DUALTALK_PROPERTY = "persist.radio.multisim.config";
                            private static final long MAX_RADIO_ON_TIME = 180000;
                            static final String RADIOOFF2_PROPERTY = "ril.ipo.radiooff.2";
                            static final String RADIOOFF_PROPERTY = "ril.ipo.radiooff";
                            final boolean isDualTalkMode;
                            final /* synthetic */ AnonymousClass1 this$1;

                            private void waitRadioOn() {
                                int i = 1;
                                Slog.i(ActivityManagerPlus.TAG, "waiting for radio on");
                                long j = 0;
                                int i2;
                                do {
                                    boolean z;
                                    boolean z2 = SystemProperties.getInt(RADIOOFF_PROPERTY, 1) == 0;
                                    if (SystemProperties.getInt(RADIOOFF2_PROPERTY, 1) != 0) {
                                        z = false;
                                    } else {
                                        z = true;
                                    }
                                    Slog.i(ActivityManagerPlus.TAG, "DualTalkMode=" + this.isDualTalkMode + " radioOn=" + z2 + " radioOn=" + z);
                                    if ((!this.isDualTalkMode && z2) || (this.isDualTalkMode && z2 && z)) {
                                        Slog.i(ActivityManagerPlus.TAG, "radio on for " + (100 * j) + "ms");
                                        break;
                                    }
                                    try {
                                        Thread.sleep(100);
                                        j++;
                                        Slog.i(ActivityManagerPlus.TAG, " wait radio on for " + (100 * j) + "ms");
                                    } catch (InterruptedException e) {
                                    }
                                    if (100 * j < 180000) {
                                        i2 = 1;
                                        continue;
                                    } else {
                                        i2 = 0;
                                        continue;
                                    }
                                } while (i2 != 0);
                                if (j * 100 >= 180000) {
                                    i = 0;
                                }
                                if (i == 0) {
                                    Slog.i(ActivityManagerPlus.TAG, "timeout to wait radio on");
                                }
                            }

                            public void run() {
                                if (ActivityManagerPlus.this.isWifiOnlyDevice()) {
                                    Slog.i(ActivityManagerPlus.TAG, "wifi-only device, skip waiting for radio on");
                                } else {
                                    waitRadioOn();
                                }
                                instance.prebootKillProcess(context);
                            }
                        }, 500);
                    }
                    Slog.i(ActivityManagerPlus.TAG, "finished");
                    if (ActivityManagerPlus.isAlarmBoot()) {
                        ActivityManagerPlus.this.mIPOAlarmBoot = true;
                    }
                } else if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                    Slog.i(ActivityManagerPlus.TAG, "ipo BOOT_IPO");
                    ActivityManagerPlusConnection.getInstance(context).stopSocketServer();
                    if (!ActivityManagerPlus.isAlarmBoot()) {
                        instance.restoreStates(context);
                        ActivityManagerPlus.removeIPOWin();
                        Slog.i(ActivityManagerPlus.TAG, "PMS wakeup");
                        ((PowerManager) context.getSystemService("power")).wakeUp(SystemClock.uptimeMillis());
                        ActivityManagerPlus.ipoBootCompleted();
                    }
                } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                    Slog.i(ActivityManagerPlus.TAG, "handling SHUTDOWN_IPO finished");
                    ActivityManagerPlusConnection.getInstance(context).startSocketServer();
                } else if ("android.intent.action.black.mode".equals(action)) {
                    if (intent.getBooleanExtra("_black_mode", false)) {
                        ActivityManagerPlus.createIPOWin();
                    }
                } else if ("android.media.RINGER_MODE_CHANGED".equals(action)) {
                    SystemProperties.set("persist.sys.mute.state", Integer.toString(intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1)));
                } else if (ActivityManagerPlus.this.mIPOAlarmBoot && "android.intent.action.normal.boot".equals(action)) {
                    instance.restoreStates(context);
                    ActivityManagerPlus.this.mIPOAlarmBoot = false;
                }
            }
        }, intentFilter);
    }

    private boolean isWifiOnlyDevice() {
        return !((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
    }

    public static void createIPOWin() {
        Slog.i(TAG, "createIPOWin");
        if (sInstance == null || sInstance.mContext == null) {
            Slog.v(TAG, "ActivityManagerPlus not ready");
        } else if (mIPOWin == null) {
            PhoneWindow phoneWindow = new PhoneWindow(sInstance.mContext);
            phoneWindow.setType(2037);
            phoneWindow.setFlags(1024, 1024);
            phoneWindow.setLayout(-1, -1);
            phoneWindow.requestFeature(1);
            LayoutParams attributes = phoneWindow.getAttributes();
            attributes.setTitle("IPOWindow");
            attributes.flags = 1048;
            WindowManager windowManager = (WindowManager) sInstance.mContext.getSystemService("window");
            mIPOWin = phoneWindow.getDecorView();
            mIPOWin.setSystemUiVisibility(512);
            mIPOWin.setBackgroundColor(UsbAudioDevice.kAudioDeviceMetaMask);
            windowManager.addView(mIPOWin, attributes);
            SystemProperties.set("sys.ipowin.done", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        } else {
            Slog.v(TAG, "IPOWin already exist");
        }
    }

    public static void removeIPOWin() {
        Slog.i(TAG, "removeIPOWin");
        if (sInstance == null || sInstance.mContext == null) {
            Slog.v(TAG, "ActivityManagerPlus not ready");
        } else if (mIPOWin != null) {
            ((WindowManager) sInstance.mContext.getSystemService("window")).removeView(mIPOWin);
            mIPOWin = null;
            SystemProperties.set("sys.ipowin.done", "0");
        } else {
            Slog.i(TAG, "already removed, skip!");
        }
    }

    static UserManager getUserManager(Context context) {
        if (sInstance != null && sInstance.mContext != null) {
            return (UserManager) context.getSystemService("user");
        }
        Log.e(TAG, "ActivityManagerPlus not ready");
        return null;
    }

    public static void ipoBootCompleted() {
        if (sInstance == null || sInstance.mContext == null) {
            Log.e(TAG, "ActivityManagerPlus not ready");
            return;
        }
        UserManager userManager = getUserManager(sInstance.mContext);
        if (userManager != null) {
            int i;
            int i2;
            List users = userManager.getUsers();
            Intent intent = new Intent("android.intent.action.LOCKED_BOOT_COMPLETED", null);
            intent.addFlags(150994960);
            intent.putExtra("_mode", 1);
            for (i = 0; i < users.size(); i++) {
                i2 = ((UserInfo) users.get(i)).id;
                intent.putExtra("android.intent.extra.user_handle", i2);
                sInstance.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(i2), "android.permission.RECEIVE_BOOT_COMPLETED", -1, null, null, null, 0, null, null);
            }
            intent = new Intent("android.intent.action.BOOT_COMPLETED", null);
            intent.addFlags(150994960);
            intent.putExtra("_mode", 1);
            for (i = 0; i < users.size(); i++) {
                i2 = ((UserInfo) users.get(i)).id;
                intent.putExtra("android.intent.extra.user_handle", i2);
                sInstance.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(i2), "android.permission.RECEIVE_BOOT_COMPLETED", -1, null, null, null, 0, null, null);
            }
            return;
        }
        Log.e(TAG, "ActivityManagerPlus not ready");
    }

    private static boolean isAlarmBoot() {
        String str = SystemProperties.get("sys.boot.reason");
        if (str != null && str.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            return true;
        }
        return false;
    }
}

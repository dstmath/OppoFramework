package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.INetworkManagementEventObserver;
import android.net.InterfaceConfiguration;
import android.os.Binder;
import android.os.CommonTimeConfig;
import android.os.CommonTimeConfig.OnServerDiedListener;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.io.PrintWriter;

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
class CommonTimeManagementService extends Binder {
    private static final boolean ALLOW_WIFI = false;
    private static final String ALLOW_WIFI_PROP = "ro.common_time.allow_wifi";
    private static final boolean AUTO_DISABLE = false;
    private static final String AUTO_DISABLE_PROP = "ro.common_time.auto_disable";
    private static final byte BASE_SERVER_PRIO = (byte) 0;
    private static final InterfaceScoreRule[] IFACE_SCORE_RULES = null;
    private static final int NATIVE_SERVICE_RECONNECT_TIMEOUT = 5000;
    private static final int NO_INTERFACE_TIMEOUT = 0;
    private static final String NO_INTERFACE_TIMEOUT_PROP = "ro.common_time.no_iface_timeout";
    private static final String SERVER_PRIO_PROP = "ro.common_time.server_prio";
    private static final String TAG = null;
    private CommonTimeConfig mCTConfig;
    private OnServerDiedListener mCTServerDiedListener;
    private BroadcastReceiver mConnectivityMangerObserver;
    private final Context mContext;
    private String mCurIface;
    private boolean mDetectedAtStartup;
    private byte mEffectivePrio;
    private INetworkManagementEventObserver mIfaceObserver;
    private Object mLock;
    private INetworkManagementService mNetMgr;
    private Handler mNoInterfaceHandler;
    private Runnable mNoInterfaceRunnable;
    private Handler mReconnectHandler;
    private Runnable mReconnectRunnable;

    private static class InterfaceScoreRule {
        public final String mPrefix;
        public final byte mScore;

        public InterfaceScoreRule(String prefix, byte score) {
            this.mPrefix = prefix;
            this.mScore = score;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.CommonTimeManagementService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.CommonTimeManagementService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.CommonTimeManagementService.<clinit>():void");
    }

    public CommonTimeManagementService(Context context) {
        this.mReconnectHandler = new Handler();
        this.mNoInterfaceHandler = new Handler();
        this.mLock = new Object();
        this.mDetectedAtStartup = false;
        this.mEffectivePrio = BASE_SERVER_PRIO;
        this.mIfaceObserver = new BaseNetworkObserver() {
            public void interfaceStatusChanged(String iface, boolean up) {
                CommonTimeManagementService.this.reevaluateServiceState();
            }

            public void interfaceLinkStateChanged(String iface, boolean up) {
                CommonTimeManagementService.this.reevaluateServiceState();
            }

            public void interfaceAdded(String iface) {
                CommonTimeManagementService.this.reevaluateServiceState();
            }

            public void interfaceRemoved(String iface) {
                CommonTimeManagementService.this.reevaluateServiceState();
            }
        };
        this.mConnectivityMangerObserver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                CommonTimeManagementService.this.reevaluateServiceState();
            }
        };
        this.mCTServerDiedListener = new OnServerDiedListener() {
            public void onServerDied() {
                CommonTimeManagementService.this.scheduleTimeConfigReconnect();
            }
        };
        this.mReconnectRunnable = new Runnable() {
            public void run() {
                CommonTimeManagementService.this.connectToTimeConfig();
            }
        };
        this.mNoInterfaceRunnable = new Runnable() {
            public void run() {
                CommonTimeManagementService.this.handleNoInterfaceTimeout();
            }
        };
        this.mContext = context;
    }

    void systemRunning() {
        if (ServiceManager.checkService("common_time.config") == null) {
            Log.i(TAG, "No common time service detected on this platform.  Common time services will be unavailable.");
            return;
        }
        this.mDetectedAtStartup = true;
        this.mNetMgr = Stub.asInterface(ServiceManager.getService("network_management"));
        try {
            this.mNetMgr.registerObserver(this.mIfaceObserver);
        } catch (RemoteException e) {
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mConnectivityMangerObserver, filter);
        connectToTimeConfig();
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(Binder.getCallingPid());
            objArr[1] = Integer.valueOf(Binder.getCallingUid());
            pw.println(String.format("Permission Denial: can't dump CommonTimeManagement service from from pid=%d, uid=%d", objArr));
        } else if (this.mDetectedAtStartup) {
            synchronized (this.mLock) {
                String str;
                pw.println("Current Common Time Management Service Config:");
                String str2 = "  Native service     : %s";
                Object[] objArr2 = new Object[1];
                if (this.mCTConfig == null) {
                    str = "reconnecting";
                } else {
                    str = "alive";
                }
                objArr2[0] = str;
                pw.println(String.format(str2, objArr2));
                str2 = "  Bound interface    : %s";
                objArr2 = new Object[1];
                objArr2[0] = this.mCurIface == null ? "unbound" : this.mCurIface;
                pw.println(String.format(str2, objArr2));
                str2 = "  Allow WiFi         : %s";
                objArr2 = new Object[1];
                objArr2[0] = ALLOW_WIFI ? "yes" : "no";
                pw.println(String.format(str2, objArr2));
                str2 = "  Allow Auto Disable : %s";
                objArr2 = new Object[1];
                objArr2[0] = AUTO_DISABLE ? "yes" : "no";
                pw.println(String.format(str2, objArr2));
                Object[] objArr3 = new Object[1];
                objArr3[0] = Byte.valueOf(this.mEffectivePrio);
                pw.println(String.format("  Server Priority    : %d", objArr3));
                objArr3 = new Object[1];
                objArr3[0] = Integer.valueOf(NO_INTERFACE_TIMEOUT);
                pw.println(String.format("  No iface timeout   : %d", objArr3));
            }
        } else {
            pw.println("Native Common Time service was not detected at startup.  Service is unavailable");
        }
    }

    private void cleanupTimeConfig() {
        this.mReconnectHandler.removeCallbacks(this.mReconnectRunnable);
        this.mNoInterfaceHandler.removeCallbacks(this.mNoInterfaceRunnable);
        if (this.mCTConfig != null) {
            this.mCTConfig.release();
            this.mCTConfig = null;
        }
    }

    private void connectToTimeConfig() {
        cleanupTimeConfig();
        try {
            synchronized (this.mLock) {
                this.mCTConfig = new CommonTimeConfig();
                this.mCTConfig.setServerDiedListener(this.mCTServerDiedListener);
                this.mCurIface = this.mCTConfig.getInterfaceBinding();
                this.mCTConfig.setAutoDisable(AUTO_DISABLE);
                this.mCTConfig.setMasterElectionPriority(this.mEffectivePrio);
            }
            if (NO_INTERFACE_TIMEOUT >= 0) {
                this.mNoInterfaceHandler.postDelayed(this.mNoInterfaceRunnable, (long) NO_INTERFACE_TIMEOUT);
            }
            reevaluateServiceState();
        } catch (RemoteException e) {
            scheduleTimeConfigReconnect();
        }
    }

    private void scheduleTimeConfigReconnect() {
        cleanupTimeConfig();
        String str = TAG;
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(NATIVE_SERVICE_RECONNECT_TIMEOUT);
        Log.w(str, String.format("Native service died, will reconnect in %d mSec", objArr));
        this.mReconnectHandler.postDelayed(this.mReconnectRunnable, 5000);
    }

    private void handleNoInterfaceTimeout() {
        if (this.mCTConfig != null) {
            Log.i(TAG, "Timeout waiting for interface to come up.  Forcing networkless master mode.");
            if (-7 == this.mCTConfig.forceNetworklessMasterMode()) {
                scheduleTimeConfigReconnect();
            }
        }
    }

    private void reevaluateServiceState() {
        String bindIface = null;
        int bestScore = -1;
        try {
            String[] ifaceList = this.mNetMgr.listInterfaces();
            if (ifaceList != null) {
                for (String iface : ifaceList) {
                    byte thisScore = (byte) -1;
                    for (InterfaceScoreRule r : IFACE_SCORE_RULES) {
                        if (iface.contains(r.mPrefix)) {
                            thisScore = r.mScore;
                            break;
                        }
                    }
                    if (thisScore > bestScore) {
                        InterfaceConfiguration config = this.mNetMgr.getInterfaceConfig(iface);
                        if (config != null && config.isActive()) {
                            bindIface = iface;
                            byte bestScore2 = thisScore;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            bindIface = null;
        }
        boolean doRebind = true;
        synchronized (this.mLock) {
            String str;
            Object[] objArr;
            if (bindIface != null) {
                if (this.mCurIface == null) {
                    str = TAG;
                    objArr = new Object[1];
                    objArr[0] = bindIface;
                    Log.e(str, String.format("Binding common time service to %s.", objArr));
                    this.mCurIface = bindIface;
                }
            }
            if (bindIface == null) {
                if (this.mCurIface != null) {
                    Log.e(TAG, "Unbinding common time service.");
                    this.mCurIface = null;
                }
            }
            if (bindIface != null) {
                if (!(this.mCurIface == null || bindIface.equals(this.mCurIface))) {
                    str = TAG;
                    objArr = new Object[2];
                    objArr[0] = this.mCurIface;
                    objArr[1] = bindIface;
                    Log.e(str, String.format("Switching common time service binding from %s to %s.", objArr));
                    this.mCurIface = bindIface;
                }
            }
            doRebind = false;
        }
        if (doRebind && this.mCTConfig != null) {
            byte newPrio;
            if (bestScore > 0) {
                newPrio = (byte) (BASE_SERVER_PRIO * bestScore);
            } else {
                newPrio = BASE_SERVER_PRIO;
            }
            if (newPrio != this.mEffectivePrio) {
                this.mEffectivePrio = newPrio;
                this.mCTConfig.setMasterElectionPriority(this.mEffectivePrio);
            }
            if (this.mCTConfig.setNetworkBinding(this.mCurIface) != 0) {
                scheduleTimeConfigReconnect();
            } else if (NO_INTERFACE_TIMEOUT >= 0) {
                this.mNoInterfaceHandler.removeCallbacks(this.mNoInterfaceRunnable);
                if (this.mCurIface == null) {
                    this.mNoInterfaceHandler.postDelayed(this.mNoInterfaceRunnable, (long) NO_INTERFACE_TIMEOUT);
                }
            }
        }
    }
}

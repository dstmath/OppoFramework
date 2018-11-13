package com.mediatek.hdmi;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.android.server.LocationManagerService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.hdmi.IMtkHdmiManager.Stub;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public final class MtkHdmiManagerService extends Stub {
    private static final String ACTION_CLEARMOTION_DIMMED = "com.mediatek.clearmotion.DIMMED_UPDATE";
    private static final String ACTION_IPO_BOOT = "android.intent.action.ACTION_BOOT_IPO";
    private static final String ACTION_IPO_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final int AP_CFG_RDCL_FILE_HDCP_KEY_LID = 45;
    private static final int HDMI_COLOR_SPACE_DEFAULT = 0;
    private static final int HDMI_DEEP_COLOR_DEFAULT = 1;
    private static final int HDMI_ENABLE_STATUS_DEFAULT = 1;
    private static final int HDMI_VIDEO_RESOLUTION_DEFAULT = 100;
    private static final int HDMI_VIDEO_SCALE_DEFAULT = 0;
    private static final String KEY_CLEARMOTION_DIMMED = "sys.display.clearMotion.dimmed";
    private static final int MSG_CABLE_STATE = 2;
    private static final int MSG_DEINIT = 1;
    private static final int MSG_INIT = 0;
    private static final int MSG_USER_SWITCH = 3;
    private static final String TAG = "MtkHdmiService";
    private static String sHdmi;
    private static String sMhl;
    private static String sSlimPort;
    private BroadcastReceiver mActionReceiver;
    private AlertDialog mAudioOutputDialog;
    private int mAudioOutputMode;
    private boolean mCablePlugged;
    private boolean mCallComing;
    private boolean mCallRestore;
    private int mCapabilities;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private int[] mEdid;
    private HdmiHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHdVideoRestore;
    private int mHdmiColorSpace;
    private int mHdmiDeepColor;
    private boolean mHdmiEnabled;
    private HdmiObserver mHdmiObserver;
    private ContentObserver mHdmiSettingsObserver;
    private int mHdmiVideoResolution;
    private int mHdmiVideoScale;
    private boolean mInitialized;
    private boolean mIsHdVideoPlaying;
    private boolean mIsSmartBookPluggedIn;
    private PhoneStateListener mPhoneStateListener;
    private int[] mPreEdid;
    private TelephonyManager mTelephonyManager;
    private WakeLock mWakeLock;

    private static class FeatureOption {
        public static final boolean MTK_CLEARMOTION_SUPPORT = false;
        public static final boolean MTK_DRM_KEY_MNG_SUPPORT = false;
        public static final boolean MTK_ENABLE_HDMI_MULTI_CHANNEL = true;
        public static final boolean MTK_HDMI_4K_SUPPORT = false;
        public static final boolean MTK_HDMI_HDCP_SUPPORT = false;
        public static final boolean MTK_INTERNAL_HDMI_SUPPORT = false;
        public static final boolean MTK_INTERNAL_MHL_SUPPORT = false;
        public static final boolean MTK_MT8193_HDCP_SUPPORT = false;
        public static final boolean MTK_MT8193_HDMI_SUPPORT = false;
        public static final boolean MTK_SMARTBOOK_SUPPORT = false;
        public static final boolean MTK_TB6582_HDMI_SUPPORT = false;
        public static final boolean SHUTDOWN_REQUESTED = false;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.MtkHdmiManagerService.FeatureOption.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.MtkHdmiManagerService.FeatureOption.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.hdmi.MtkHdmiManagerService.FeatureOption.<clinit>():void");
        }

        private FeatureOption() {
        }

        private static boolean getValue(String key) {
            return SystemProperties.get(key).equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
    }

    private class HdmiHandler extends Handler {
        public HdmiHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = 1;
            MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "handleMessage: " + msg.what);
            if (MtkHdmiManagerService.this.mHandlerThread == null || !MtkHdmiManagerService.this.mHandlerThread.isAlive() || MtkHdmiManagerService.this.mHandlerThread.isInterrupted()) {
                MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "handler thread is error");
                return;
            }
            switch (msg.what) {
                case 0:
                    initHdmi(false);
                    if (!isRealyBootComplete()) {
                        deinitHdmi();
                        break;
                    }
                    MtkHdmiManagerService.this.mInitialized = true;
                    MtkHdmiManagerService mtkHdmiManagerService = MtkHdmiManagerService.this;
                    if (!MtkHdmiManagerService.this.mCablePlugged) {
                        i = 0;
                    }
                    mtkHdmiManagerService.hdmiCableStateChanged(i);
                    break;
                case 1:
                    MtkHdmiManagerService.this.mInitialized = false;
                    deinitHdmi();
                    break;
                case 2:
                    MtkHdmiManagerService.this.hdmiCableStateChanged(((Integer) msg.obj).intValue());
                    break;
                case 3:
                    deinitHdmi();
                    initHdmi(true);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

        private boolean isRealyBootComplete() {
            boolean bRet = false;
            String state = SystemProperties.get("ro.crypto.state");
            String decrypt = SystemProperties.get("vold.decrypt");
            String type = SystemProperties.get("ro.crypto.type");
            if ("unencrypted".equals(state)) {
                if (IElsaManager.EMPTY_PACKAGE.equals(decrypt)) {
                    bRet = true;
                }
            } else if ("unsupported".equals(state)) {
                if (IElsaManager.EMPTY_PACKAGE.equals(decrypt)) {
                    bRet = true;
                }
            } else if (!IElsaManager.EMPTY_PACKAGE.equals(state) && "encrypted".equals(state)) {
                if ("block".equals(type)) {
                    if ("trigger_restart_framework".equals(decrypt)) {
                        bRet = true;
                    }
                } else if ("file".equals(type)) {
                    bRet = true;
                }
            }
            MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "ro.crypto.state=" + state + " vold.decrypt=" + decrypt + " realBoot=" + bRet);
            return bRet;
        }

        private void deinitHdmi() {
            MtkHdmiManagerService.this.unregisterCallListener();
            MtkHdmiManagerService.this.enableHdmiImpl(false);
            if (MtkHdmiManagerService.this.isSignalOutputting()) {
                MtkHdmiManagerService.this.mCablePlugged = false;
                MtkHdmiManagerService.this.handleCablePlugged(false);
            }
        }

        private void initHdmi(boolean bSwitchUser) {
            MtkHdmiManagerService.this.loadHdmiSettings();
            MtkHdmiManagerService.this.enableHdmiImpl(MtkHdmiManagerService.this.mHdmiEnabled);
            if (bSwitchUser && MtkHdmiManagerService.this.mInitialized) {
                int i;
                MtkHdmiManagerService.this.handleCablePlugged(MtkHdmiManagerService.this.mCablePlugged);
                ContentResolver -get2 = MtkHdmiManagerService.this.mContentResolver;
                String str = "hdmi_cable_plugged";
                if (MtkHdmiManagerService.this.mCablePlugged) {
                    i = 1;
                } else {
                    i = 0;
                }
                System.putIntForUser(-get2, str, i, -2);
            }
            MtkHdmiManagerService.this.registerCallListener();
        }
    }

    private class HdmiObserver extends UEventObserver {
        private static final String HDMI_NAME_PATH = "/sys/class/switch/hdmi/name";
        private static final String HDMI_STATE_PATH = "/sys/class/switch/hdmi/state";
        private static final String HDMI_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/hdmi";
        private static final int MSG_HDMI = 10;
        private static final int MSG_OTG = 11;
        private static final String OTG_NAME_PATH = "/sys/class/switch/otg_state/name";
        private static final String OTG_STATE_PATH = "/sys/class/switch/otg_state/state";
        private static final String OTG_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/otg_state";
        private static final String TAG = "HdmiObserver";
        private final Context mContext;
        private final Handler mHandler;
        private String mHdmiName;
        private int mHdmiState;
        private String mOtgName;
        private int mPrevHdmiState;
        private final WakeLock mWakeLock;

        public HdmiObserver(Context context) {
            this.mHandler = new Handler(MtkHdmiManagerService.this.mHandler.getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 10:
                            HdmiObserver.this.sendIntents(msg.arg1, msg.arg2, (String) msg.obj);
                            break;
                        case 11:
                            HdmiObserver.this.handleOtgStateChanged(msg.arg1);
                            break;
                        default:
                            super.handleMessage(msg);
                            break;
                    }
                    HdmiObserver.this.mWakeLock.release();
                }
            };
            this.mContext = context;
            this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
            this.mWakeLock.setReferenceCounted(false);
            init();
        }

        public void startObserve() {
            startObserving(HDMI_UEVENT_MATCH);
            startObserving(OTG_UEVENT_MATCH);
        }

        public void stopObserve() {
            stopObserving();
        }

        public void onUEvent(UEvent event) {
            MtkHdmiManagerService.log(TAG, "HdmiObserver: onUEvent: " + event.toString());
            String name = event.get("SWITCH_NAME");
            int state = 0;
            try {
                state = Integer.parseInt(event.get("SWITCH_STATE"));
            } catch (NumberFormatException e) {
                Log.w(TAG, "HdmiObserver: Could not parse switch state from event " + event);
            }
            MtkHdmiManagerService.log(TAG, "HdmiObserver.onUEvent(), name=" + name + ", state=" + state);
            if (name.equals(this.mOtgName)) {
                updateOtgState(state);
            } else {
                update(name, state);
            }
        }

        private synchronized void init() {
            String newName = this.mHdmiName;
            int newState = this.mHdmiState;
            this.mPrevHdmiState = this.mHdmiState;
            try {
                update(getContentFromFile(HDMI_NAME_PATH), Integer.parseInt(getContentFromFile(HDMI_STATE_PATH)));
                initOtgState();
            } catch (NumberFormatException e) {
                Log.w(TAG, "HDMI state fail");
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:32:0x0104 A:{SYNTHETIC, Splitter: B:32:0x0104} */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x00c1 A:{SYNTHETIC, Splitter: B:24:0x00c1} */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x0080 A:{SYNTHETIC, Splitter: B:16:0x0080} */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x012d A:{SYNTHETIC, Splitter: B:38:0x012d} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private String getContentFromFile(String filePath) {
            IndexOutOfBoundsException e;
            Throwable th;
            char[] buffer = new char[1024];
            FileReader reader = null;
            String content = null;
            try {
                FileReader reader2 = new FileReader(filePath);
                try {
                    content = String.valueOf(buffer, 0, reader2.read(buffer, 0, buffer.length)).trim();
                    MtkHdmiManagerService.log(TAG, filePath + " content is " + content);
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e2) {
                            Log.w(TAG, "close reader fail: " + e2.getMessage());
                        }
                    }
                    reader = reader2;
                } catch (FileNotFoundException e3) {
                    reader = reader2;
                    Log.w(TAG, "can't find file " + filePath);
                    if (reader != null) {
                    }
                    return content;
                } catch (IOException e4) {
                    reader = reader2;
                    Log.w(TAG, "IO exception when read file " + filePath);
                    if (reader != null) {
                    }
                    return content;
                } catch (IndexOutOfBoundsException e5) {
                    e = e5;
                    reader = reader2;
                    try {
                        Log.w(TAG, "index exception: " + e.getMessage());
                        if (reader != null) {
                        }
                        return content;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e22) {
                                Log.w(TAG, "close reader fail: " + e22.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e6) {
                Log.w(TAG, "can't find file " + filePath);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e222) {
                        Log.w(TAG, "close reader fail: " + e222.getMessage());
                    }
                }
                return content;
            } catch (IOException e7) {
                Log.w(TAG, "IO exception when read file " + filePath);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2222) {
                        Log.w(TAG, "close reader fail: " + e2222.getMessage());
                    }
                }
                return content;
            } catch (IndexOutOfBoundsException e8) {
                e = e8;
                Log.w(TAG, "index exception: " + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e22222) {
                        Log.w(TAG, "close reader fail: " + e22222.getMessage());
                    }
                }
                return content;
            }
            return content;
        }

        /* JADX WARNING: Missing block: B:17:0x0046, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private synchronized void update(String newName, int newState) {
            MtkHdmiManagerService.log(TAG, "HDMIOberver.update(), oldState=" + this.mHdmiState + ", newState=" + newState);
            int hdmiState = newState;
            int newOrOld = newState | this.mHdmiState;
            if (FeatureOption.MTK_MT8193_HDMI_SUPPORT) {
                if (this.mHdmiState == newState && 3 != this.mHdmiState) {
                    return;
                }
            } else if (this.mHdmiState == newState || ((newOrOld - 1) & newOrOld) != 0) {
            }
            this.mHdmiName = newName;
            this.mPrevHdmiState = this.mHdmiState;
            this.mHdmiState = newState;
            this.mWakeLock.acquire();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10, this.mHdmiState, this.mPrevHdmiState, this.mHdmiName), 0);
        }

        private synchronized void sendIntents(int hdmiState, int prevHdmiState, String hdmiName) {
            sendIntent(1, hdmiState, prevHdmiState, hdmiName);
        }

        private void sendIntent(int hdmi, int hdmiState, int prevHdmiState, String hdmiName) {
            if ((hdmiState & hdmi) != (prevHdmiState & hdmi)) {
                Intent intent = new Intent("android.intent.action.HDMI_PLUG");
                intent.addFlags(1073741824);
                int state = 0;
                if ((hdmiState & hdmi) != 0) {
                    state = 1;
                }
                intent.putExtra("state", state);
                intent.putExtra("name", hdmiName);
                MtkHdmiManagerService.log(TAG, "HdmiObserver: Broadcast HDMI event, state: " + state + " name: " + hdmiName);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                MtkHdmiManagerService.this.mHandler.obtainMessage(2, Integer.valueOf(state)).sendToTarget();
            }
        }

        private void initOtgState() {
            this.mOtgName = getContentFromFile(OTG_NAME_PATH);
            try {
                int otgState = Integer.parseInt(getContentFromFile(OTG_STATE_PATH));
                Log.i(TAG, "HDMIObserver.initOtgState(), state=" + otgState + ", name=" + this.mOtgName);
                updateOtgState(otgState);
            } catch (NumberFormatException e) {
                Log.w(TAG, "OTG state fail");
            }
        }

        private void updateOtgState(int otgState) {
            Log.i(TAG, "HDMIObserver.updateOtgState(), otgState=" + otgState);
            this.mWakeLock.acquire();
            Message msg = this.mHandler.obtainMessage(11);
            msg.arg1 = otgState;
            this.mHandler.sendMessage(msg);
        }

        private void handleOtgStateChanged(int otgState) {
            Log.i(TAG, "HDMIObserver.handleOtgStateChanged(), otgState=" + otgState);
            Log.i(TAG, "notifyOtgState: " + MtkHdmiManagerService.this.nativeNotifyOtgState(otgState));
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.MtkHdmiManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.MtkHdmiManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.hdmi.MtkHdmiManagerService.<clinit>():void");
    }

    public native boolean nativeEnableAudio(boolean z);

    public native boolean nativeEnableCec(boolean z);

    public native boolean nativeEnableHdcp(boolean z);

    public native boolean nativeEnableHdmi(boolean z);

    public native boolean nativeEnableHdmiIpo(boolean z);

    public native boolean nativeEnableVideo(boolean z);

    public native int nativeGetCapabilities();

    public native char[] nativeGetCecAddr();

    public native int[] nativeGetCecCmd();

    public native int nativeGetDisplayType();

    public native int[] nativeGetEdid();

    public native boolean nativeHdmiPortraitEnable(boolean z);

    public native boolean nativeHdmiPowerEnable(boolean z);

    public native boolean nativeIsHdmiForceAwake();

    public native boolean nativeNeedSwDrmProtect();

    public native boolean nativeNotifyOtgState(int i);

    public native boolean nativeSetAudioConfig(int i);

    public native boolean nativeSetCecAddr(byte b, byte[] bArr, char c, char c2);

    public native boolean nativeSetCecCmd(byte b, byte b2, char c, byte[] bArr, int i, byte b3);

    public native boolean nativeSetDeepColor(int i, int i2);

    public native boolean nativeSetHdcpKey(byte[] bArr);

    public native boolean nativeSetHdmiDrmKey();

    public native boolean nativeSetVideoConfig(int i);

    private void handleCallStateChanged(int state) {
        log(TAG, "mCallComing: " + this.mCallComing + " mCallRestore: " + this.mCallRestore);
        if (state == 2) {
            this.mCallComing = true;
            if (isSignalOutputting()) {
                String contentStr = this.mContext.getResources().getString(134545616);
                int type = getDisplayType();
                if (type == 2) {
                    contentStr = contentStr.replaceAll(sHdmi, sMhl);
                } else if (type == 3) {
                    contentStr = contentStr.replaceAll(sHdmi, sSlimPort);
                }
                Toast.makeText(this.mContext, contentStr, 1).show();
                this.mCallRestore = true;
                enableHdmi(false);
                return;
            }
            return;
        }
        this.mCallComing = false;
        if (this.mCallRestore) {
            this.mCallRestore = false;
            enableHdmi(true);
        }
    }

    private void hdmiCableStateChanged(int state) {
        boolean z;
        if (state == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mCablePlugged = z;
        if (this.mInitialized) {
            int type = getDisplayType();
            String contentStr;
            if (this.mIsHdVideoPlaying && this.mCablePlugged) {
                if (type != 1) {
                    contentStr = this.mContext.getResources().getString(134545615);
                    if (type == 2) {
                        contentStr = contentStr.replaceAll(sHdmi, sMhl);
                    } else if (type == 3) {
                        contentStr = contentStr.replaceAll(sHdmi, sSlimPort);
                    }
                    log(TAG, "disable hdmi when play HD video");
                    Toast.makeText(this.mContext, contentStr, 1).show();
                    this.mHdVideoRestore = true;
                    log(TAG, "mIsHdVideoPlaying: " + this.mIsHdVideoPlaying + " mHdVideoRestore: " + this.mHdVideoRestore);
                    enableHdmi(false);
                    return;
                }
            } else if (this.mCallComing && this.mCablePlugged) {
                contentStr = this.mContext.getResources().getString(134545616);
                if (type == 2) {
                    contentStr = contentStr.replaceAll(sHdmi, sMhl);
                } else if (type == 3) {
                    contentStr = contentStr.replaceAll(sHdmi, sSlimPort);
                }
                log(TAG, "disable hdmi when call coming");
                Toast.makeText(this.mContext, contentStr, 1).show();
                this.mCallRestore = true;
                log(TAG, "mCallComing: " + this.mCallComing + " mCallRestore: " + this.mCallRestore);
                enableHdmi(false);
                return;
            }
            getCapabilities();
            handleCablePlugged(this.mCablePlugged);
            System.putIntForUser(this.mContentResolver, "hdmi_cable_plugged", state, -2);
        }
    }

    private void unregisterCallListener() {
        if (hasCapability(4) && this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
    }

    private void registerCallListener() {
        if (hasCapability(4)) {
            if (this.mTelephonyManager == null) {
                this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            }
            this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
            log(TAG, "register phone state change listener...");
        }
    }

    private void handleCablePlugged(boolean plugged) {
        boolean isShowNotification = false;
        updateClearMotionDimmed(plugged);
        if (plugged) {
            refreshEdid(plugged);
            if (FeatureOption.MTK_MT8193_HDMI_SUPPORT || FeatureOption.MTK_INTERNAL_HDMI_SUPPORT || FeatureOption.MTK_INTERNAL_MHL_SUPPORT) {
                setColorAndDeepImpl(this.mHdmiColorSpace, this.mHdmiDeepColor);
            }
            initVideoResolution(this.mHdmiVideoResolution, this.mHdmiVideoScale);
        } else {
            refreshEdid(plugged);
        }
        if (plugged && !this.mIsSmartBookPluggedIn) {
            isShowNotification = true;
        }
        handleNotification(isShowNotification);
        updateWakeLock(plugged, this.mHdmiEnabled);
        if (plugged) {
            handleMultiChannel();
        }
    }

    private boolean isSupportMultiChannel() {
        return getAudioParameter(120, 3) > 2;
    }

    private void handleMultiChannel() {
        if (isSupportMultiChannel()) {
            this.mAudioOutputMode = System.getIntForUser(this.mContentResolver, "hdmi_audio_output_mode", 0, -2);
            log(TAG, "current mode from setting provider : " + this.mAudioOutputMode);
            if (this.mAudioOutputDialog == null) {
                String title = this.mContext.getResources().getString(134545619);
                String stereo = this.mContext.getResources().getString(134545620);
                String multiChannel = this.mContext.getResources().getString(134545621);
                Builder title2 = new Builder(this.mContext).setTitle(title);
                CharSequence[] charSequenceArr = new String[2];
                charSequenceArr[0] = stereo;
                charSequenceArr[1] = multiChannel;
                this.mAudioOutputDialog = title2.setSingleChoiceItems(charSequenceArr, this.mAudioOutputMode, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean z = false;
                        MtkHdmiManagerService.this.mAudioOutputMode = which;
                        MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "mAudioOutputDialog clicked.. which: " + which);
                        MtkHdmiManagerService mtkHdmiManagerService = MtkHdmiManagerService.this;
                        if (which == 0) {
                            z = true;
                        }
                        mtkHdmiManagerService.setAudioParameters(z);
                        dialog.dismiss();
                        MtkHdmiManagerService.this.mAudioOutputDialog = null;
                    }
                }).setNegativeButton(17039360, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean z = false;
                        MtkHdmiManagerService mtkHdmiManagerService = MtkHdmiManagerService.this;
                        if (MtkHdmiManagerService.this.mAudioOutputMode == 0) {
                            z = true;
                        }
                        mtkHdmiManagerService.setAudioParameters(z);
                        dialog.dismiss();
                        MtkHdmiManagerService.this.mAudioOutputDialog = null;
                    }
                }).create();
                this.mAudioOutputDialog.setCancelable(false);
                this.mAudioOutputDialog.getWindow().setType(2003);
            }
            this.mAudioOutputDialog.show();
            return;
        }
        setAudioParameters(false);
    }

    private void setAudioParameters(boolean isStereoChecked) {
        int maxChannel = getAudioParameter(120, 3);
        if (isStereoChecked) {
            maxChannel = 2;
        }
        int maxSampleate = getAudioParameter(896, 7);
        int maxBitwidth = getAudioParameter(3072, 10);
        AudioSystem.setParameters("HDMI_channel=" + maxChannel);
        AudioSystem.setParameters("HDMI_maxsamplingrate=" + maxSampleate);
        AudioSystem.setParameters("HDMI_bitwidth=" + maxBitwidth);
        System.putIntForUser(this.mContentResolver, "hdmi_audio_output_mode", this.mAudioOutputMode, -2);
        log(TAG, "setAudioParameters mAudioOutputMode: " + this.mAudioOutputMode + " ,maxChannel: " + maxChannel + " ,maxSampleate: " + maxSampleate + " ,maxBitwidth: " + maxBitwidth);
    }

    public int getAudioParameter(int mask, int offsets) {
        int param = (this.mCapabilities & mask) >> offsets;
        log(TAG, "getAudioParameter() mask: " + mask + " ,offsets: " + offsets + " ,param: " + param + " ,mCapabilities: 0x" + Integer.toHexString(this.mCapabilities));
        return param;
    }

    private void updateClearMotionDimmed(boolean plugged) {
        if (FeatureOption.MTK_CLEARMOTION_SUPPORT) {
            SystemProperties.set(KEY_CLEARMOTION_DIMMED, plugged ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
            this.mContext.sendBroadcastAsUser(new Intent("com.mediatek.clearmotion.DIMMED_UPDATE"), UserHandle.ALL);
        }
    }

    private void refreshEdid(boolean plugged) {
        if (plugged) {
            int i;
            String str;
            Object[] objArr;
            this.mEdid = getResolutionMask();
            if (this.mEdid != null) {
                for (i = 0; i < this.mEdid.length; i++) {
                    str = TAG;
                    objArr = new Object[2];
                    objArr[0] = Integer.valueOf(i);
                    objArr[1] = Integer.valueOf(this.mEdid[i]);
                    log(str, String.format("mEdid[%d] = %d", objArr));
                }
            } else {
                log(TAG, "mEdid is null!");
            }
            if (this.mPreEdid != null) {
                for (i = 0; i < this.mPreEdid.length; i++) {
                    str = TAG;
                    objArr = new Object[2];
                    objArr[0] = Integer.valueOf(i);
                    objArr[1] = Integer.valueOf(this.mPreEdid[i]);
                    log(str, String.format("mPreEdid[%d] = %d", objArr));
                }
                return;
            }
            log(TAG, "mPreEdid is null!");
            return;
        }
        this.mPreEdid = this.mEdid;
    }

    private void handleNotification(boolean showNoti) {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        if (notificationManager == null) {
            Log.w(TAG, "Fail to get NotificationManager");
            return;
        }
        if (showNoti) {
            log(TAG, "Show notification now");
            Notification notification = new Notification();
            String titleStr = this.mContext.getResources().getString(134545613);
            String contentStr = this.mContext.getResources().getString(134545614);
            notification.icon = 134348892;
            int type = getDisplayType();
            if (type == 2) {
                titleStr = titleStr.replaceAll(sHdmi, sMhl);
                contentStr = contentStr.replaceAll(sHdmi, sMhl);
                notification.icon = 134348906;
            } else if (type == 3) {
                titleStr = titleStr.replaceAll(sHdmi, sSlimPort);
                contentStr = contentStr.replaceAll(sHdmi, sSlimPort);
                notification.icon = 134348912;
            }
            notification.tickerText = titleStr;
            notification.flags = 35;
            notification.setLatestEventInfo(this.mContext, titleStr, contentStr, PendingIntent.getActivityAsUser(this.mContext, 0, Intent.makeRestartActivityTask(new ComponentName("com.android.settings", "com.android.settings.HDMISettings")), 0, null, UserHandle.CURRENT));
            notificationManager.notifyAsUser(null, 134348892, notification, UserHandle.CURRENT);
        } else {
            log(TAG, "Clear notification now");
            notificationManager.cancelAsUser(null, 134348892, UserHandle.CURRENT);
        }
    }

    public MtkHdmiManagerService(Context context) {
        this.mWakeLock = null;
        this.mInitialized = false;
        this.mIsSmartBookPluggedIn = false;
        this.mIsHdVideoPlaying = false;
        this.mHdVideoRestore = false;
        this.mCallComing = false;
        this.mCallRestore = false;
        this.mTelephonyManager = null;
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, " Phone state changed, new state= " + state);
                MtkHdmiManagerService.this.handleCallStateChanged(state);
            }
        };
        this.mAudioOutputMode = 0;
        this.mActionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "receive: " + action);
                if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action) || "android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                    sendMsg(0);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    Log.d(MtkHdmiManagerService.TAG, "intent.getExtra_mode" + intent.getExtra("_mode"));
                    if (intent.getExtra("_mode") == null) {
                        Log.d(MtkHdmiManagerService.TAG, "SHUTDOWN_REQUESTED=" + FeatureOption.SHUTDOWN_REQUESTED);
                        if (FeatureOption.SHUTDOWN_REQUESTED) {
                            sendMsg(1);
                            return;
                        }
                        return;
                    }
                    sendMsg(1);
                } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                    sendMsg(1);
                } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    sendMsg(3);
                } else if ("android.intent.action.SMARTBOOK_PLUG".equals(action)) {
                    MtkHdmiManagerService.this.mIsSmartBookPluggedIn = intent.getBooleanExtra("state", false);
                    Log.d(MtkHdmiManagerService.TAG, "smartbook plug:" + MtkHdmiManagerService.this.mIsSmartBookPluggedIn);
                    MtkHdmiManagerService.this.handleNotification(false);
                }
            }

            private void sendMsg(int msgInit) {
                if (!MtkHdmiManagerService.this.mHandler.hasMessages(msgInit)) {
                    MtkHdmiManagerService.this.mHandler.sendEmptyMessage(msgInit);
                    MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "send msg: " + msgInit);
                }
            }
        };
        this.mHdmiSettingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                Log.d(MtkHdmiManagerService.TAG, "hdmiSettingsObserver onChanged: " + selfChange);
                MtkHdmiManagerService mtkHdmiManagerService = MtkHdmiManagerService.this;
                if (System.getIntForUser(MtkHdmiManagerService.this.mContentResolver, "hdmi_enable_status", 1, -2) != 1) {
                    z = false;
                }
                mtkHdmiManagerService.mHdmiEnabled = z;
                MtkHdmiManagerService.this.updateWakeLock(MtkHdmiManagerService.this.mCablePlugged, MtkHdmiManagerService.this.mHdmiEnabled);
            }
        };
        log(TAG, "MtkHdmiManagerService constructor");
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        initial();
    }

    private void initial() {
        if (this.mHandlerThread == null || !this.mHandlerThread.isAlive()) {
            this.mHandlerThread = new HandlerThread("HdmiService");
            this.mHandlerThread.start();
            this.mHandler = new HdmiHandler(this.mHandlerThread.getLooper());
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
            filter.addAction("android.intent.action.USER_SWITCHED");
            filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            filter.addAction("android.intent.action.ACTION_BOOT_IPO");
            if (FeatureOption.MTK_SMARTBOOK_SUPPORT) {
                filter.addAction("android.intent.action.SMARTBOOK_PLUG");
            }
            this.mContext.registerReceiverAsUser(this.mActionReceiver, UserHandle.ALL, filter, null, this.mHandler);
        }
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(536870922, "HDMI");
            this.mWakeLock.setReferenceCounted(false);
        }
        if (this.mHdmiObserver == null) {
            this.mHdmiObserver = new HdmiObserver(this.mContext);
            this.mHdmiObserver.startObserve();
        }
        if (FeatureOption.MTK_MT8193_HDCP_SUPPORT || FeatureOption.MTK_HDMI_HDCP_SUPPORT) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (FeatureOption.MTK_DRM_KEY_MNG_SUPPORT) {
                        MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "setDrmKey: " + MtkHdmiManagerService.this.setDrmKey());
                    } else {
                        MtkHdmiManagerService.log(MtkHdmiManagerService.TAG, "setHdcpKey: " + MtkHdmiManagerService.this.setHdcpKey());
                    }
                }
            });
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                MtkHdmiManagerService.this.getCapabilities();
                MtkHdmiManagerService.sHdmi = MtkHdmiManagerService.this.mContext.getResources().getString(134545617);
                MtkHdmiManagerService.sMhl = MtkHdmiManagerService.this.mContext.getResources().getString(134545618);
            }
        });
        observeSettings();
    }

    private void updateWakeLock(boolean plugged, boolean hdmiEnabled) {
        if (plugged && hdmiEnabled && nativeIsHdmiForceAwake()) {
            this.mWakeLock.acquire();
        } else {
            this.mWakeLock.release();
        }
    }

    private boolean setHdcpKey() {
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(ServiceManager.getService("NvRAMAgent"));
        if (agent != null) {
            try {
                log(TAG, "Read HDCP key from nvram");
                byte[] key = agent.readFile(45);
                for (int i = 0; i < 287; i++) {
                    String str = TAG;
                    Object[] objArr = new Object[2];
                    objArr[0] = Integer.valueOf(i);
                    objArr[1] = Byte.valueOf(key[i]);
                    log(str, String.format("HDCP key[%d] = %d", objArr));
                }
                if (key != null) {
                    return nativeSetHdcpKey(key);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "NvRAMAgent read file fail");
            }
        }
        return false;
    }

    private boolean setDrmKey() {
        boolean nativeSetHdmiDrmKey;
        synchronized (this) {
            nativeSetHdmiDrmKey = nativeSetHdmiDrmKey();
        }
        return nativeSetHdmiDrmKey;
    }

    private void loadHdmiSettings() {
        boolean z;
        if (System.getIntForUser(this.mContentResolver, "hdmi_enable_status", 1, -2) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mHdmiEnabled = z;
        this.mHdmiVideoResolution = System.getIntForUser(this.mContentResolver, "hdmi_video_resolution", 100, -2);
        this.mHdmiVideoScale = System.getIntForUser(this.mContentResolver, "hdmi_video_scale", 0, -2);
        this.mHdmiColorSpace = System.getIntForUser(this.mContentResolver, "hdmi_color_space", 0, -2);
        this.mHdmiDeepColor = System.getIntForUser(this.mContentResolver, "hdmi_deep_color", 1, -2);
        this.mIsHdVideoPlaying = false;
        this.mHdVideoRestore = false;
        this.mCallComing = false;
        this.mCallRestore = false;
    }

    private void observeSettings() {
        this.mContentResolver.registerContentObserver(System.getUriFor("hdmi_enable_status"), false, this.mHdmiSettingsObserver, -1);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.println("MTK HDMI MANAGER (dumpsys HDMI)");
        pw.println("HDMI mHdmiEnabled: " + this.mHdmiEnabled);
        pw.println("HDMI mHdmiVideoResolution: " + this.mHdmiVideoResolution);
        pw.println("HDMI mHdmiVideoScale: " + this.mHdmiVideoScale);
        pw.println("HDMI mHdmiColorSpace: " + this.mHdmiColorSpace);
        pw.println("HDMI mHdmiDeepColor: " + this.mHdmiDeepColor);
        pw.println("HDMI mCapabilities: " + this.mCapabilities);
        pw.println("HDMI mCablePlugged: " + this.mCablePlugged);
        pw.println("HDMI mEdid: " + Arrays.toString(this.mEdid));
        pw.println("HDMI mPreEdid: " + Arrays.toString(this.mPreEdid));
        pw.println("HDMI mInitialized: " + this.mInitialized);
        pw.println();
    }

    public boolean enableHdmi(boolean enabled) {
        log(TAG, "enableHdmi: " + enabled);
        boolean ret = false;
        if (enabled == this.mHdmiEnabled) {
            log(TAG, "mHdmiEnabled is the same: " + enabled);
        } else {
            ret = enableHdmiImpl(enabled);
            if (ret) {
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mHdmiEnabled = enabled;
                    System.putIntForUser(this.mContentResolver, "hdmi_enable_status", this.mHdmiEnabled ? 1 : 0, -2);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        return ret;
    }

    private boolean enableHdmiImpl(boolean enabled) {
        boolean nativeEnableHdmi;
        synchronized (this) {
            nativeEnableHdmi = nativeEnableHdmi(enabled);
        }
        return nativeEnableHdmi;
    }

    public int[] getResolutionMask() {
        int[] nativeGetEdid;
        log(TAG, "getResolutionMask");
        synchronized (this) {
            nativeGetEdid = nativeGetEdid();
        }
        return nativeGetEdid;
    }

    public boolean isSignalOutputting() {
        log(TAG, "isSignalOutputting");
        return this.mCablePlugged ? this.mHdmiEnabled : false;
    }

    public boolean setColorAndDeep(int color, int deep) {
        log(TAG, "setColorAndDeep: " + color + ", " + deep);
        boolean ret = setColorAndDeepImpl(color, deep);
        if (ret) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mHdmiColorSpace = color;
                this.mHdmiDeepColor = deep;
                System.putIntForUser(this.mContentResolver, "hdmi_color_space", color, -2);
                System.putIntForUser(this.mContentResolver, "hdmi_deep_color", deep, -2);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return ret;
    }

    private boolean setColorAndDeepImpl(int color, int deep) {
        boolean nativeSetDeepColor;
        synchronized (this) {
            nativeSetDeepColor = nativeSetDeepColor(color, deep);
        }
        return nativeSetDeepColor;
    }

    public boolean setVideoResolution(int resolution) {
        log(TAG, "setVideoResolution: " + resolution);
        int suitableResolution = resolution;
        if (resolution >= 100) {
            suitableResolution = getSuitableResolution(resolution);
        }
        if (suitableResolution == this.mHdmiVideoResolution) {
            log(TAG, "setVideoResolution is the same");
        }
        int finalResolution = suitableResolution >= 100 ? suitableResolution - 100 : suitableResolution;
        log(TAG, "final video resolution: " + finalResolution + " scale: " + this.mHdmiVideoScale);
        boolean ret = setVideoResolutionImpl(finalResolution, this.mHdmiVideoScale);
        if (ret) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mHdmiVideoResolution = suitableResolution;
                System.putIntForUser(this.mContentResolver, "hdmi_video_resolution", this.mHdmiVideoResolution, -2);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return ret;
    }

    private void initVideoResolution(int resolution, int scale) {
        log(TAG, "initVideoResolution: " + resolution + " scale: " + scale);
        if (isResolutionSupported(resolution)) {
            setVideoResolutionImpl(resolution, scale);
            return;
        }
        int suitableResolution = getSuitableResolution(resolution);
        int finalResolution = suitableResolution >= 100 ? suitableResolution - 100 : suitableResolution;
        log(TAG, "initVideoResolution final video resolution: " + finalResolution);
        if (setVideoResolutionImpl(finalResolution, scale)) {
            this.mHdmiVideoResolution = suitableResolution;
            System.putIntForUser(this.mContentResolver, "hdmi_video_resolution", this.mHdmiVideoResolution, -2);
        }
    }

    private boolean isResolutionSupported(int resolution) {
        log(TAG, "isResolutionSupported: " + resolution);
        if (resolution >= 100) {
            return false;
        }
        for (int res : getSupportedResolutions()) {
            if (res == resolution) {
                log(TAG, "resolution is supported");
                return true;
            }
        }
        return false;
    }

    private boolean setVideoResolutionImpl(int resolution, int scale) {
        boolean nativeSetVideoConfig;
        if (getDisplayType() == 1) {
            log(TAG, "revise resolution for SMB to 2");
            resolution = 2;
        }
        int param = (resolution & 255) | ((scale & 255) << 8);
        log(TAG, "set video resolution&scale: 0x" + Integer.toHexString(param));
        synchronized (this) {
            nativeSetVideoConfig = nativeSetVideoConfig(param);
        }
        return nativeSetVideoConfig;
    }

    private int getSuitableResolution(int videoResolution) {
        int length;
        int res;
        int i = 0;
        int[] supportedResolutions = getSupportedResolutions();
        ArrayList<Integer> resolutionList = new ArrayList();
        for (int res2 : supportedResolutions) {
            resolutionList.add(Integer.valueOf(res2));
        }
        if (needUpdate(videoResolution)) {
            log(TAG, "upate resolution");
            if (this.mEdid != null) {
                int index;
                int edidTemp = this.mEdid[0] | this.mEdid[1];
                if (FeatureOption.MTK_INTERNAL_HDMI_SUPPORT || FeatureOption.MTK_INTERNAL_MHL_SUPPORT) {
                    index = 1;
                } else if (FeatureOption.MTK_MT8193_HDMI_SUPPORT) {
                    index = 0;
                } else if (FeatureOption.MTK_TB6582_HDMI_SUPPORT) {
                    index = 2;
                } else {
                    index = 3;
                }
                int[] prefered = HdmiDef.getPreferedResolutions(index);
                length = prefered.length;
                while (i < length) {
                    res2 = prefered[i];
                    int act = res2;
                    if (res2 >= 100) {
                        act = res2 - 100;
                    }
                    if ((HdmiDef.sResolutionMask[act] & edidTemp) != 0 && resolutionList.contains(Integer.valueOf(act))) {
                        videoResolution = res2;
                        break;
                    }
                    i++;
                }
            }
        }
        log(TAG, "suiteable video resolution: " + videoResolution);
        return videoResolution;
    }

    private boolean needUpdate(int videoResolution) {
        log(TAG, "needUpdate: " + videoResolution);
        boolean needUpdate = true;
        if (this.mPreEdid != null && Arrays.equals(this.mEdid, this.mPreEdid)) {
            needUpdate = false;
        }
        if (videoResolution >= 100) {
            return true;
        }
        return needUpdate;
    }

    public boolean setVideoScale(int scale) {
        log(TAG, "setVideoScale: " + scale);
        boolean ret = false;
        if (scale >= 0 && scale <= 10) {
            ret = true;
        }
        if (ret) {
            this.mHdmiVideoScale = scale;
            int finalResolution = this.mHdmiVideoResolution >= 100 ? this.mHdmiVideoResolution - 100 : this.mHdmiVideoResolution;
            log(TAG, "set video resolution: " + finalResolution + " scale: " + this.mHdmiVideoScale);
            ret = setVideoResolutionImpl(finalResolution, this.mHdmiVideoScale);
            if (ret) {
                long ident = Binder.clearCallingIdentity();
                try {
                    System.putIntForUser(this.mContentResolver, "hdmi_video_scale", this.mHdmiVideoScale, -2);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        return ret;
    }

    public int[] getSupportedResolutions() {
        log(TAG, "getSupportedResolutions");
        return getSupportedResolutionsImpl();
    }

    private int[] getSupportedResolutionsImpl() {
        int i = 0;
        if (this.mEdid == null) {
            if (FeatureOption.MTK_TB6582_HDMI_SUPPORT) {
                return HdmiDef.getDefaultResolutions(2);
            }
            return HdmiDef.getDefaultResolutions(3);
        } else if (!FeatureOption.MTK_INTERNAL_HDMI_SUPPORT && !FeatureOption.MTK_INTERNAL_MHL_SUPPORT) {
            int[] resolutions;
            if (FeatureOption.MTK_MT8193_HDMI_SUPPORT) {
                resolutions = HdmiDef.getDefaultResolutions(0);
            } else {
                resolutions = HdmiDef.getAllResolutions();
            }
            int edidTemp = this.mEdid[0] | this.mEdid[1];
            ArrayList<Integer> list = new ArrayList();
            int length = resolutions.length;
            while (i < length) {
                int res = resolutions[i];
                try {
                    if (!((edidTemp & HdmiDef.sResolutionMask[res]) == 0 || list.contains(Integer.valueOf(res)))) {
                        list.add(Integer.valueOf(res));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.w(TAG, e.getMessage());
                }
                i++;
            }
            resolutions = new int[list.size()];
            for (int i2 = 0; i2 < list.size(); i2++) {
                resolutions[i2] = ((Integer) list.get(i2)).intValue();
            }
            log(TAG, "getSupportedResolutionsImpl: " + Arrays.toString(resolutions));
            return resolutions;
        } else if (FeatureOption.MTK_HDMI_4K_SUPPORT) {
            return HdmiDef.getDefaultResolutions(1);
        } else {
            return HdmiDef.getDefaultResolutions(4);
        }
    }

    public int getDisplayType() {
        int ret;
        log(TAG, "getDisplayType");
        synchronized (this) {
            ret = nativeGetDisplayType();
        }
        return ret;
    }

    /* JADX WARNING: Missing block: B:14:0x005c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyHdVideoState(boolean playing) {
        log(TAG, "notifyHdVideoState: " + playing);
        synchronized (this) {
            if (this.mIsHdVideoPlaying == playing) {
                return;
            }
            log(TAG, "mIsHdVideoPlaying: " + this.mIsHdVideoPlaying + " mNeedRestore: " + this.mHdVideoRestore);
            this.mIsHdVideoPlaying = playing;
            if (!this.mIsHdVideoPlaying && this.mHdVideoRestore) {
                this.mHdVideoRestore = false;
                enableHdmi(true);
            }
        }
    }

    public boolean enableHdmiPower(boolean enabled) {
        boolean ret;
        log(TAG, "enableHdmiPower");
        synchronized (this) {
            ret = nativeHdmiPowerEnable(enabled);
        }
        return ret;
    }

    public boolean needSwDrmProtect() {
        boolean ret;
        log(TAG, "needSwDrmProtect");
        synchronized (this) {
            ret = nativeNeedSwDrmProtect();
        }
        return ret;
    }

    public boolean hasCapability(int mask) {
        log(TAG, "hasCapability: " + mask);
        if ((this.mCapabilities & mask) != 0) {
            return true;
        }
        return false;
    }

    private void getCapabilities() {
        synchronized (this) {
            this.mCapabilities = nativeGetCapabilities();
        }
        log(TAG, "getCapabilities: 0x" + Integer.toHexString(this.mCapabilities));
    }

    private static void log(String tag, Object obj) {
        if (Log.isLoggable(tag, 4)) {
            Log.i(tag, obj.toString());
        }
    }
}

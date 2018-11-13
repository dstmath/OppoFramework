package com.mediatek.usp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.util.Preconditions;
import com.android.server.LocationManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.usp.IUspService.Stub;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class UspServiceImpl extends Stub {
    private static final String CUSTOM_PATH = "/custom/usp";
    private static final int MAX_AT_CMD_RESPONSE = 2048;
    private static final boolean MTK_C2K_SUPPORT = false;
    private static final int PROP_CFG_CTRL_FLAG_CONFIG_STATE_INVALID = 3;
    private static final int PROP_CFG_CTRL_FLAG_FIRST_SIM_ONLY_DONE = 2;
    private static final int PROP_CFG_CTRL_FLAG_NOT_FIRST_BOOT = 1;
    private static final int PROP_CFG_CTRL_FLAG_POPUP_HANDLED = 4;
    private static final String PROP_CXP_CONFIG_CTRL = "persist.mtk_usp_cfg_ctrl";
    private static final String PROP_GSM_SIM_OPERATOR_NUMERIC = "gsm.sim.operator.numeric";
    private static final String PROP_PERSIST_BOOTANIM_MNC = "persist.bootanim.mnc";
    private static final String SYSTEM_PATH = "/system/usp";
    private static final String USP_INFO_FILE = "usp-info.txt";
    private static final String VENDOR_PATH = "/vendor/usp";
    private static Map<String, String> sOperatorMapInfo;
    private final boolean DEBUG;
    private final String TAG;
    private final boolean TESTING_PURPOSE;
    private int mConfigState;
    private Context mContext;
    private AlertDialog mDialog;
    protected BroadcastReceiver mEnableDisableRespReceiver;
    protected BroadcastReceiver mIntentReceiver;
    private List<String> mPendingEnableDisableReq;
    private PackageManager mPm;
    private TaskHandler mTaskHandler;
    private MyHandler mUiHandler;

    private class MyHandler extends Handler {
        static final int FREEZE_FRAME = 1;
        static final int REBOOT_DIALOG = 0;

        /* synthetic */ MyHandler(UspServiceImpl this$0, MyHandler myHandler) {
            this();
        }

        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    UspServiceImpl.this.showWaitingScreen(((Boolean) msg.obj).booleanValue());
                    UspServiceImpl.this.freeze();
                    return;
                case 1:
                    if (UspServiceImpl.freezeFrame() < 0) {
                        Log.e("UspServiceImpl", "FREEZE FRAME FAILED...NOW WHAT TO DO...:(");
                        return;
                    } else {
                        Log.d("UspServiceImpl", "showWaitingScreen Freezed");
                        return;
                    }
                default:
                    Log.d("UspServiceImpl", "Wrong message reason");
                    return;
            }
        }
    }

    private class TaskHandler extends Handler {
        static final int EARLY_READ_FAILED = 3;
        static final int REBOOT_SYSTEM = 2;
        static final int START_CONFIG = 1;
        static final int START_FIRST_CONFIG = 0;

        public TaskHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.d("UspServiceImpl", "TaskHandler message:" + msg.what);
            switch (msg.what) {
                case 0:
                    UspServiceImpl.this.firstBootConfigure();
                    UspServiceImpl.this.setConfigCtrlFlag(1, true, null);
                    break;
                case 1:
                    if (!UspServiceImpl.this.mPendingEnableDisableReq.isEmpty()) {
                        UspServiceImpl.this.mPendingEnableDisableReq.clear();
                        UspServiceImpl.this.mContext.unregisterReceiver(UspServiceImpl.this.mEnableDisableRespReceiver);
                    }
                    UspServiceImpl.this.runningConfigurationTask((String) msg.obj);
                    break;
                case 2:
                    UspServiceImpl.this.rebootAndroidSystem();
                    break;
                case 3:
                    String mccMnc = UspServiceImpl.this.readMCCMNCFromProperty(true);
                    if (mccMnc.length() >= 5) {
                        UspServiceImpl.this.handleSwitchOperator(UspServiceImpl.this.getOperatorPackForSim(mccMnc));
                        break;
                    }
                    Log.d("UspServiceImpl", "Invalid mccMnc " + mccMnc);
                    UspServiceImpl.this.mTaskHandler.sendMessageDelayed(UspServiceImpl.this.mTaskHandler.obtainMessage(3), 500);
                    return;
                default:
                    Log.d("UspServiceImpl", "Wrong message reason");
                    break;
            }
        }
    }

    private class UspUserDialog implements OnClickListener {
        private String mOptr;

        UspUserDialog(String optr) {
            this.mOptr = optr;
        }

        void showDialog() {
            UspServiceImpl.this.mDialog = new Builder(UspServiceImpl.this.mContext).setMessage(new StringBuilder("[" + UspServiceImpl.this.getOperatorNameFromPack(this.mOptr) + "] " + Resources.getSystem().getString(134545453)).toString()).setPositiveButton(17039379, this).setNegativeButton(17039369, this).create();
            UspServiceImpl.this.mDialog.getWindow().setType(2010);
            LayoutParams attributes = UspServiceImpl.this.mDialog.getWindow().getAttributes();
            attributes.privateFlags |= 16;
            UspServiceImpl.this.mDialog.setCanceledOnTouchOutside(false);
            UspServiceImpl.this.mDialog.show();
            Log.d("UspServiceImpl", "showDialog " + UspServiceImpl.this.mDialog);
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            if (-1 == whichButton) {
                Log.d("UspServiceImpl", "Click for yes");
                UspServiceImpl.this.startConfiguringOpPack(this.mOptr, false);
            }
            UspServiceImpl.this.setConfigCtrlFlag(4, true, this.mOptr);
            UspServiceImpl.this.mDialog.dismiss();
            UspServiceImpl.this.mDialog = null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.usp.UspServiceImpl.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.usp.UspServiceImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.usp.UspServiceImpl.<clinit>():void");
    }

    public static native int freezeFrame();

    public static native int unfreezeFrame();

    public UspServiceImpl(Context context) {
        boolean z;
        this.TAG = "UspServiceImpl";
        if (Build.TYPE.equals("user")) {
            z = false;
        } else {
            z = true;
        }
        this.DEBUG = z;
        this.TESTING_PURPOSE = true;
        this.mConfigState = 0;
        this.mPendingEnableDisableReq = new ArrayList();
        this.mEnableDisableRespReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String[] data = intent.getData().toString().split(":");
                String packageName = data[data.length - 1];
                UspServiceImpl.this.mPendingEnableDisableReq.remove(packageName);
                Log.d("UspServiceImpl", "mEnableDisableRespReceiver, got response for package name=" + packageName);
                Log.d("UspServiceImpl", "Dump mPendingEnableDisableReq List of Size:" + UspServiceImpl.this.mPendingEnableDisableReq.size() + Arrays.toString(UspServiceImpl.this.mPendingEnableDisableReq.toArray()));
                if (UspServiceImpl.this.mPendingEnableDisableReq.isEmpty()) {
                    Log.d("UspServiceImpl", "mEnableDisableRespReceiver,mPendingEnableDisableReq empty So Calling rebootAndroidSystem");
                    UspServiceImpl.this.mContext.unregisterReceiver(UspServiceImpl.this.mEnableDisableRespReceiver);
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
            private static final String INTENT_KEY_ICC_STATE = "ss";
            private static final String INTENT_VALUE_ICC_LOADED = "LOADED";

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("UspServiceImpl", "BroadcastReceiver(), SIM state change, action=" + action);
                if (UspServiceImpl.this.mTaskHandler.hasMessages(3)) {
                    Log.d("UspServiceImpl", "removeMessages, TaskHandler.EARLY_READ_FAILED");
                    UspServiceImpl.this.mTaskHandler.removeMessages(3);
                }
                if (action != null && action.equals(ACTION_SIM_STATE_CHANGED)) {
                    String newState = intent.getStringExtra(INTENT_KEY_ICC_STATE);
                    Log.d("UspServiceImpl", "BroadcastReceiver(), SIM state change, new state=" + newState);
                    if (newState.equals(INTENT_VALUE_ICC_LOADED) && UspServiceImpl.this.mConfigState != 1) {
                        String mccMnc = UspServiceImpl.this.readMCCMNCFromProperty(false);
                        if (mccMnc.length() < 5) {
                            Log.d("UspServiceImpl", "Invalid mccMnc " + mccMnc);
                        } else {
                            UspServiceImpl.this.handleSwitchOperator(UspServiceImpl.this.getOperatorPackForSim(mccMnc));
                        }
                    }
                }
            }
        };
        Log.d("UspServiceImpl", "UspServiceImpl");
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        this.mUiHandler = new MyHandler(this, null);
        this.mTaskHandler = null;
        this.mDialog = null;
    }

    public void start() {
        Log.d("UspServiceImpl", "start");
        HandlerThread ht = new HandlerThread("HandlerThread");
        ht.start();
        this.mTaskHandler = new TaskHandler(ht.getLooper());
        if (getConfigCtrlFlag(3, null)) {
            String opPack = SystemProperties.get("persist.operator.optr");
            if (isOperatorValid(opPack)) {
                Log.d("UspServiceImpl", "start reconfiguring as last config was not complete");
                startConfiguringOpPack(opPack, true);
                return;
            }
            Log.d("UspServiceImpl", "Operator pack not valid: " + opPack);
            setConfigCtrlFlag(3, false, null);
        }
        if (!getConfigCtrlFlag(1, null)) {
            this.mTaskHandler.sendMessage(this.mTaskHandler.obtainMessage(0));
        }
        String mccMnc = readMCCMNCFromProperty(true);
        if (mccMnc.length() < 5) {
            Log.d("UspServiceImpl", "Invalid mccMnc: " + mccMnc + "scheduled after some time");
            this.mTaskHandler.sendMessageDelayed(this.mTaskHandler.obtainMessage(3), 500);
            return;
        }
        handleSwitchOperator(getOperatorPackForSim(mccMnc));
    }

    void firstBootConfigure() {
        Log.d("UspServiceImpl", "firstBootConfigure");
        String optr = SystemProperties.get("persist.operator.optr");
        if (optr == null || optr.length() <= 0) {
            Log.d("UspServiceImpl", "firstBootConfigure: OM config");
            enabledDisableApps("OM");
            return;
        }
        Log.d("UspServiceImpl", "firstBootConfigure: OP config" + optr);
        enabledDisableApps(optr);
        setProperties(optr);
    }

    boolean getConfigCtrlFlag(int prop, String optr) {
        boolean z = true;
        int propValue = SystemProperties.getInt(PROP_CXP_CONFIG_CTRL, 0);
        switch (prop) {
            case 1:
                if ((propValue & 1) != 1) {
                    z = false;
                }
                return z;
            case 2:
                if ((propValue & 2) != 2) {
                    z = false;
                }
                return z;
            case 3:
                if ((propValue & 4) != 4) {
                    z = false;
                }
                return z;
            case 4:
                int numStored = (-65536 & propValue) >> 16;
                try {
                    int numOptr = Integer.parseInt(optr.substring(2, optr.length()));
                    Log.d("UspServiceImpl", "saved: " + numStored + "cur: " + numOptr);
                    if (numOptr == numStored) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.d("UspServiceImpl", "getConfigCtrlFlag: 2" + e.toString());
                    break;
                }
                break;
        }
        return false;
    }

    synchronized void setConfigCtrlFlag(int prop, boolean flag, String optr) {
        int propValue = SystemProperties.getInt(PROP_CXP_CONFIG_CTRL, 0);
        switch (prop) {
            case 1:
                propValue &= -2;
                if (flag) {
                    propValue |= 1;
                    break;
                }
                break;
            case 2:
                propValue &= -3;
                if (flag) {
                    propValue |= 2;
                    break;
                }
                break;
            case 3:
                propValue &= -5;
                if (flag) {
                    propValue |= 4;
                    break;
                }
                break;
            case 4:
                if (optr != null) {
                    if (optr.length() >= 3) {
                        try {
                            propValue = (propValue & 65535) | (Integer.parseInt(optr.substring(2, optr.length())) << 16);
                            break;
                        } catch (NumberFormatException e) {
                            Log.d("UspServiceImpl", "setConfigCtrlFlag: 2" + e.toString());
                            break;
                        }
                    }
                }
                break;
        }
        SystemProperties.set(PROP_CXP_CONFIG_CTRL, IElsaManager.EMPTY_PACKAGE + propValue);
        return;
    }

    boolean isFirstValidSimConfigured() {
        String simSwitchMode = SystemProperties.get("ro.mtk_cxp_switch_mode");
        if (simSwitchMode != null && simSwitchMode.equals("2") && getConfigCtrlFlag(2, null)) {
            return true;
        }
        return false;
    }

    void handleSwitchOperator(String optr) {
        String simSwitchMode;
        if (isFirstValidSimConfigured()) {
            Log.d("UspServiceImpl", "isFirstValidSimConfigured: true");
        } else if (!isOperatorValid(optr)) {
            Log.d("UspServiceImpl", "Operator pack not valid: " + optr);
        } else if (optr.equals(getActiveOpPack())) {
            Log.d("UspServiceImpl", "same active operator: " + optr);
            simSwitchMode = SystemProperties.get("ro.mtk_cxp_switch_mode");
            if (simSwitchMode != null && simSwitchMode.equals("2")) {
                setConfigCtrlFlag(2, true, null);
                Log.d("UspServiceImpl", "set first valid sim configured");
            }
        } else {
            if (this.mConfigState != 1) {
                simSwitchMode = SystemProperties.get("ro.mtk_cxp_switch_mode");
                if (simSwitchMode != null && simSwitchMode.equals("2")) {
                    setConfigCtrlFlag(2, true, null);
                    Log.d("UspServiceImpl", "set first valid sim configured");
                    startConfiguringOpPack(optr, false);
                } else if (!getConfigCtrlFlag(4, optr)) {
                    if (this.mDialog == null || !this.mDialog.isShowing()) {
                        new UspUserDialog(optr).showDialog();
                    } else {
                        Log.d("UspServiceImpl", "configuration dialog already being displayed");
                    }
                }
            }
        }
    }

    public String getActiveOpPack() {
        return SystemProperties.get("persist.operator.optr");
    }

    public String getOpPackFromSimInfo(String mccMnc) {
        if (mccMnc == null || mccMnc.length() <= 0) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        return getOperatorPackForSim(mccMnc);
    }

    public void setOpPackActive(String opPack) {
        Log.i("UspServiceImpl", "setOpPackActive" + opPack);
        String simSwitchMode = SystemProperties.get("ro.mtk_cxp_switch_mode");
        if (simSwitchMode != null && simSwitchMode.equals("2")) {
            Log.d("UspServiceImpl", "First valid sim is enabled: ");
        } else if (!isOperatorValid(opPack)) {
            Log.d("UspServiceImpl", "Operator pack not valid: " + opPack);
        } else if (opPack.equals(getActiveOpPack())) {
            Log.d("UspServiceImpl", "same active operator: " + opPack);
        } else {
            if (this.mConfigState != 1) {
                startConfiguringOpPack(opPack, false);
            }
        }
    }

    boolean isOperatorValid(String optr) {
        if (optr == null || optr.length() <= 0) {
            Log.d("UspServiceImpl", "error in operator: " + optr);
            return false;
        } else if (getAllOpList().contains(optr)) {
            return true;
        } else {
            Log.d("UspServiceImpl", "Operator not found in all op pack list");
            return false;
        }
    }

    List<String> getAllOpList() {
        String ops = getRegionalOpPack();
        List<String> opList = new ArrayList();
        try {
            String[] opSplit = ops.split(" ");
            int count = 0;
            while (count < opSplit.length) {
                if (opSplit[count] != null && opSplit[count].length() > 0) {
                    opList.add(opSplit[count].substring(0, opSplit[count].indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT)));
                }
                count++;
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("UspServiceImpl", "illegal string passed to splitString: " + e.toString());
        }
        return opList;
    }

    public Map<String, String> getAllOpPackList() {
        String ops = getRegionalOpPack();
        Map<String, String> operatorMapInfo = new HashMap();
        try {
            String[] opSplit = ops.split(" ");
            int count = 0;
            while (count < opSplit.length) {
                if (opSplit[count] != null && opSplit[count].length() > 0) {
                    int firstUnderscoreIndex = opSplit[count].indexOf(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
                    operatorMapInfo.put(opSplit[count].substring(0, firstUnderscoreIndex), getOperatorNameFromPack(opSplit[count].substring(0, firstUnderscoreIndex)));
                }
                count++;
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("UspServiceImpl", "illegal string passed to splitString: " + e.toString());
        }
        return operatorMapInfo;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0066 A:{Splitter: B:1:0x0001, ExcHandler: android.content.res.Resources.NotFoundException (r0_0 'e' java.lang.RuntimeException)} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0066 A:{Splitter: B:1:0x0001, ExcHandler: android.content.res.Resources.NotFoundException (r0_0 'e' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:11:0x0066, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:12:0x0067, code:
            android.util.Log.e("UspServiceImpl", "getOperatorPackForSim Exception: " + r0.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getOperatorPackForSim(String mccMnc) {
        int i = 0;
        try {
            int mccMncNum = Integer.parseInt(mccMnc);
            String[] operatorList = Resources.getSystem().getStringArray(134479879);
            int length = operatorList.length;
            while (i < length) {
                String[] opSplit = operatorList[i].split("\\s*,\\s*");
                if (mccMncNum < Integer.parseInt(opSplit[0]) || mccMncNum > Integer.parseInt(opSplit[1])) {
                    i++;
                } else {
                    Log.d("UspServiceImpl", "getOperatorPackForSim optr: " + opSplit[2]);
                    return "OP" + opSplit[2];
                }
            }
        } catch (RuntimeException e) {
        }
        Log.d("UspServiceImpl", "getOperatorPackForSim optr NOT FOUND");
        return IElsaManager.EMPTY_PACKAGE;
    }

    private void startConfiguringOpPack(String opPack, boolean isReconfig) {
        Log.d("UspServiceImpl", "startConfiguringOpPack: " + opPack);
        this.mConfigState = 1;
        this.mUiHandler.sendMessage(this.mUiHandler.obtainMessage(0, Boolean.valueOf(isReconfig)));
        this.mTaskHandler.sendMessage(this.mTaskHandler.obtainMessage(1, opPack));
        this.mTaskHandler.sendMessageDelayed(this.mTaskHandler.obtainMessage(2, opPack), 500);
    }

    private void runningConfigurationTask(String opPack) {
        Log.d("UspServiceImpl", "runningConfigurationTask " + opPack);
        setConfigCtrlFlag(3, true, null);
        SystemProperties.set("gsm.ril.eboot", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        SystemProperties.set("cdma.ril.eboot", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        sendMdPowerOffCmd();
        setProperties(opPack);
        setMdSbpProperty(opPack);
        enabledDisableApps(opPack);
    }

    private String sendMdPowerOffCmd() {
        String atCmd = new String("AT+EPOF");
        try {
            final TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            byte[] rawData = atCmd.getBytes();
            byte[] cmdByte = new byte[(rawData.length + 1)];
            byte[] cmdResp = new byte[2048];
            System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
            cmdByte[cmdByte.length - 1] = (byte) 0;
            if (this.DEBUG) {
                Log.d("UspServiceImpl", "sendMdPowerOffCmd:" + atCmd);
            }
            if (MTK_C2K_SUPPORT) {
                final byte[] cmdRespMD3 = new byte[2048];
                new Thread() {
                    public void run() {
                        Log.d("UspServiceImpl", "sendMdPowerOffCmdMD3:" + telephonyManager.invokeOemRilRequestRaw("AT+EPOFDESTRILD:C2K".getBytes(), cmdRespMD3));
                    }
                }.start();
                int retMd3 = telephonyManager.invokeOemRilRequestRaw("AT+EFUN=0".getBytes(), cmdRespMD3);
            }
            int ret = telephonyManager.invokeOemRilRequestRaw(cmdByte, cmdResp);
            if (ret != -1) {
                cmdResp[ret] = (byte) 0;
                return new String(cmdResp);
            }
        } catch (NullPointerException ee) {
            ee.printStackTrace();
        }
        return IElsaManager.EMPTY_PACKAGE;
    }

    private void showWaitingScreen(boolean isReconfig) {
        AlertDialog dialog = new AlertDialog(this.mContext) {
            public boolean dispatchKeyEvent(KeyEvent event) {
                return true;
            }

            public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                return true;
            }

            public boolean dispatchTouchEvent(MotionEvent ev) {
                return true;
            }

            public boolean dispatchTrackballEvent(MotionEvent ev) {
                return true;
            }

            public boolean dispatchGenericMotionEvent(MotionEvent ev) {
                return true;
            }

            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                return true;
            }
        };
        if (isReconfig) {
            dialog.setMessage(this.mContext.getResources().getString(134545651));
        } else {
            dialog.setMessage(this.mContext.getResources().getString(134545650));
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setType(2010);
        LayoutParams attributes = dialog.getWindow().getAttributes();
        attributes.privateFlags |= 16;
        dialog.show();
        Log.d("UspServiceImpl", "showing WaitingScreen");
    }

    private void freeze() {
        this.mUiHandler.sendMessageDelayed(this.mUiHandler.obtainMessage(1), 500);
    }

    private void rebootAndroidSystem() {
        int i = 0;
        while (i < 25) {
            try {
                if (this.mPendingEnableDisableReq.isEmpty() && !this.mUiHandler.hasMessages(1)) {
                    Log.d("UspServiceImpl", "All Enable Disable completed before " + i + "th Sleep, Now Going to Reboot");
                    Log.d("UspServiceImpl", "Going to Reboot Android System");
                    break;
                }
                if (this.mUiHandler.hasMessages(1)) {
                    Log.d("UspServiceImpl", "FREEZE_FRAME still not handled, so wait for .5 sec");
                }
                Thread.sleep(500);
                if (i == 24) {
                    Log.e("UspServiceImpl", "Enable Disable May Have Not Completed");
                }
                i++;
            } catch (Exception e) {
                Log.d("UspServiceImpl", "when sleep exception happened");
            }
        }
        this.mConfigState = 2;
        SystemProperties.set("persist.mtk_usp_native_start", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public void unfreezeScreen() {
        if (getConfigCtrlFlag(3, null)) {
            Log.e("UspServiceImpl", "unfreezeScreen during configuration, so skipped");
            return;
        }
        if (unfreezeFrame() < 0) {
            Log.e("UspServiceImpl", "UNFREEZING FRAME FAILED.....WE ARE DEAD :(");
        }
    }

    private String readMCCMNCFromProperty(boolean isFromBootanim) {
        String value = readMCCMNCFromPropertyForTesting(isFromBootanim);
        Log.d("UspServiceImpl", "readMCCMNCFromPropertyForTesting " + value);
        return value;
    }

    private String readMCCMNCFromPropertyForTesting(boolean isFromBootanim) {
        String dummyMccMnc = SystemProperties.get("persist.simulate_cxp_sim");
        String mccMnc;
        if (isFromBootanim) {
            mccMnc = SystemProperties.get(PROP_PERSIST_BOOTANIM_MNC);
            if (!(mccMnc == null || mccMnc.length() <= 4 || mccMnc.equals("000000"))) {
                Log.d("UspServiceImpl", "read mcc mnc property from boot anim: bootanim-mnc");
                if (dummyMccMnc == null || dummyMccMnc.length() <= 4) {
                    dummyMccMnc = mccMnc;
                }
                return dummyMccMnc;
            }
        }
        mccMnc = SystemProperties.get(PROP_GSM_SIM_OPERATOR_NUMERIC);
        if (mccMnc != null && mccMnc.length() > 4) {
            Log.d("UspServiceImpl", "read mcc mnc property from sim-operator-mnc");
            if (dummyMccMnc == null || dummyMccMnc.length() <= 4) {
                dummyMccMnc = mccMnc;
            }
            return dummyMccMnc;
        }
        Log.d("UspServiceImpl", "failed to read mcc mnc from property");
        return IElsaManager.EMPTY_PACKAGE;
    }

    private String getOperatorNameFromPack(String optr) {
        String cxpPack = SystemProperties.get("ro.mtk_carrierexpress_pack");
        if (sOperatorMapInfo.containsKey(cxpPack + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + optr)) {
            Log.d("UspServiceImpl", "getOperatorNameFromPack for optr: " + cxpPack + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + optr);
            return (String) sOperatorMapInfo.get(cxpPack + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + optr);
        } else if (!sOperatorMapInfo.containsKey(optr)) {
            return new String("Unknown");
        } else {
            Log.d("UspServiceImpl", "getOperatorNameFromPack for optr: " + optr);
            return (String) sOperatorMapInfo.get(optr);
        }
    }

    private void setMdSbpProperty(String optr) {
        String val = new String(optr.substring(2, optr.length()));
        Log.d("UspServiceImpl", "setMdSbpProperty value: " + val);
        SystemProperties.set("persist.mtk_usp_md_sbp_code", val);
    }

    private void setProperties(String optr) {
        File customGlobalDir;
        if (new File(CUSTOM_PATH).exists()) {
            customGlobalDir = new File(CUSTOM_PATH);
        } else if (new File(VENDOR_PATH).exists()) {
            customGlobalDir = new File(VENDOR_PATH);
        } else {
            Log.e("UspServiceImpl", "none of custom/usp or vendor/usp exists");
            return;
        }
        List<String> opPropertyList = readFromFile(new File(customGlobalDir, "usp-content-" + optr + ".txt"), "[Property-start]", "[Property-end]");
        for (int i = 0; i < opPropertyList.size(); i++) {
            String key = getKey(((String) opPropertyList.get(i)).trim());
            String value = getValue(((String) opPropertyList.get(i)).trim());
            if (this.DEBUG) {
                Log.d("UspServiceImpl", "setting property " + key + "  TO  " + value);
            }
            set(this.mContext, key, value);
        }
    }

    private String getRegionalOpPack() {
        File customGlobalDir;
        Log.d("UspServiceImpl", "getRegionalOpPack ");
        if (new File(CUSTOM_PATH).exists()) {
            customGlobalDir = new File(CUSTOM_PATH);
        } else if (new File(VENDOR_PATH).exists()) {
            customGlobalDir = new File(VENDOR_PATH);
        } else {
            Log.e("UspServiceImpl", "none of custom/usp or vendor/usp exists");
            return IElsaManager.EMPTY_PACKAGE;
        }
        List<String> data = readFromFile(new File(customGlobalDir, USP_INFO_FILE));
        for (int i = 0; i < data.size(); i++) {
            String key = getKey(((String) data.get(i)).trim());
            Log.d("UspServiceImpl", "MTK_REGIONAL_OP_PACK = " + key);
            if (key.equals("MTK_REGIONAL_OP_PACK")) {
                String value = getValue(((String) data.get(i)).trim());
                Log.d("UspServiceImpl", "MTK_REGIONAL_OP_PACK = " + value);
                return value;
            }
        }
        return IElsaManager.EMPTY_PACKAGE;
    }

    private void set(Context context, String key, String val) throws IllegalArgumentException {
        try {
            SystemProperties.set(key, val);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
        }
    }

    private void enabledDisableApps(String optr) {
        String isInstSupport = SystemProperties.get("ro.mtk_carrierexpress_inst_sup");
        if (isInstSupport == null || !isInstSupport.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            File customGlobalDir;
            int i;
            if (new File(CUSTOM_PATH).exists()) {
                customGlobalDir = new File(CUSTOM_PATH);
            } else if (new File(VENDOR_PATH).exists()) {
                customGlobalDir = new File(VENDOR_PATH);
            } else {
                Log.e("UspServiceImpl", "none of custom/usp or vendor/usp exists");
                return;
            }
            String[] customGlobalFiles = customGlobalDir.list();
            String opFileName = "usp-content-" + optr + ".txt";
            File customAllFile = new File(customGlobalDir, "usp-packages-all.txt");
            File customOpFile = new File(customGlobalDir, opFileName);
            this.mPm = this.mContext.getPackageManager();
            List<String> allPackageList = readFromFile(customAllFile);
            Log.d("UspServiceImpl", "enabledDisableApps ALL File First content" + ((String) allPackageList.get(0)));
            List<String> opPackageList = readFromFile(customOpFile, "[Package-start]", "[Package-end]");
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageFilter.addDataScheme("package");
            this.mContext.registerReceiver(this.mEnableDisableRespReceiver, packageFilter);
            for (i = 0; i < allPackageList.size(); i++) {
                Log.d("UspServiceImpl", ((String) allPackageList.get(i)) + " not in OP File " + (!opPackageList.contains(allPackageList.get(i))) + " EnabledState: " + getPackageEnabledState((String) allPackageList.get(i), false));
                if (!opPackageList.contains(allPackageList.get(i))) {
                    if (getPackageEnabledState((String) allPackageList.get(i), false)) {
                        this.mPendingEnableDisableReq.add((String) allPackageList.get(i));
                        disableApps((String) allPackageList.get(i));
                    }
                }
            }
            for (i = 0; i < opPackageList.size(); i++) {
                Log.d("UspServiceImpl", ((String) opPackageList.get(i)) + " EnabledState: " + getPackageEnabledState((String) opPackageList.get(i), true));
                if (!getPackageEnabledState((String) opPackageList.get(i), true)) {
                    this.mPendingEnableDisableReq.add((String) opPackageList.get(i));
                    enableApps((String) opPackageList.get(i));
                }
            }
            return;
        }
        Log.d("UspServiceImpl", "Install/uninstall apk is enabled");
    }

    private boolean getPackageEnabledState(String packageName, boolean defaultState) {
        try {
            return this.mPm.getApplicationInfo(packageName, 0).enabled;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Log.e("UspServiceImpl", "getPackageEnabledState, packageNotFound: " + packageName);
            return defaultState;
        }
    }

    private String getKey(String toBeSplit) {
        String key = null;
        try {
            return toBeSplit.substring(0, toBeSplit.indexOf("="));
        } catch (IndexOutOfBoundsException e) {
            Log.e("UspServiceImpl", "illegal property string: " + e.toString());
            return key;
        }
    }

    private String getValue(String toBeSplit) {
        String value = null;
        try {
            return toBeSplit.substring(toBeSplit.indexOf("=") + 1, toBeSplit.length());
        } catch (IndexOutOfBoundsException e) {
            Log.e("UspServiceImpl", "illegal property string: " + e.toString());
            return value;
        }
    }

    private List<String> readFromFile(File customGlobalFile) {
        byte[] bytes = new byte[((int) customGlobalFile.length())];
        List<String> fileContents = new ArrayList();
        try {
            FileInputStream inputStream = new FileInputStream(customGlobalFile);
            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String str = IElsaManager.EMPTY_PACKAGE;
                while (true) {
                    str = bufferedReader.readLine();
                    if (str == null) {
                        break;
                    }
                    fileContents.add(str);
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("UspServiceImpl", "File not found: " + e.toString());
        } catch (IOException e2) {
            Log.e("UspServiceImpl", "Can not read file: " + e2.toString());
        }
        return fileContents;
    }

    private List<String> readFromFile(File customGlobalFile, String startTag, String endTag) {
        byte[] bytes = new byte[((int) customGlobalFile.length())];
        List<String> fileContents = new ArrayList();
        try {
            FileInputStream inputStream = new FileInputStream(customGlobalFile);
            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String receiveString = IElsaManager.EMPTY_PACKAGE;
                boolean isSect = false;
                while (true) {
                    receiveString = bufferedReader.readLine();
                    if (receiveString == null) {
                        break;
                    } else if (startTag.equals(receiveString)) {
                        isSect = true;
                    } else if (endTag.equals(receiveString)) {
                        break;
                    } else if (isSect) {
                        fileContents.add(receiveString);
                    }
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e("UspServiceImpl", "File not found: " + e.toString());
        } catch (IOException e2) {
            Log.e("UspServiceImpl", "Can not read file: " + e2.toString());
        }
        return fileContents;
    }

    private void enableApps(String appPackage) {
        Log.d("UspServiceImpl", "enablingApp :" + appPackage);
        try {
            this.mPm.setApplicationEnabledSetting(appPackage, 1, 1);
        } catch (IllegalArgumentException e) {
            Log.e("UspServiceImpl", "enabling illegal package: " + e.toString());
        }
    }

    private void disableApps(String appPackage) {
        Log.d("UspServiceImpl", "disablingApp :" + appPackage);
        try {
            this.mPm.setApplicationEnabledSetting(appPackage, 2, 1);
        } catch (IllegalArgumentException e) {
            Log.e("UspServiceImpl", "disabling illegal package: " + e.toString());
        }
    }
}

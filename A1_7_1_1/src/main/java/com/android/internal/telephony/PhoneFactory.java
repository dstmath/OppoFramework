package com.android.internal.telephony;

import android.content.Context;
import android.net.LocalServerSocket;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.telephony.dataconnection.TelephonyNetworkFactory;
import com.android.internal.telephony.imsphone.ImsPhoneFactory;
import com.android.internal.telephony.oem.rus.RusInitProcess;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.IndentingPrintWriter;
import com.mediatek.internal.telephony.dataconnection.DataSubSelector;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

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
public class PhoneFactory {
    static final boolean DBG = false;
    static final String LOG_TAG = "PhoneFactory";
    public static final int MAX_ACTIVE_PHONES = 1;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static final int SOCKET_OPEN_RETRY_MILLIS = 2000;
    private static CommandsInterface sCommandsInterface;
    private static CommandsInterface[] sCommandsInterfaces;
    private static Context sContext;
    private static DataSubSelector sDataSubSelector;
    private static final HashMap<String, LocalLog> sLocalLogs = null;
    static final Object sLockProxyPhones = null;
    private static boolean sMadeDefaults;
    private static Phone sPhone;
    private static PhoneNotifier sPhoneNotifier;
    private static PhoneSwitcher sPhoneSwitcher;
    private static Phone[] sPhones;
    private static ProxyController sProxyController;
    private static SubscriptionInfoUpdater sSubInfoRecordUpdater;
    private static SubscriptionMonitor sSubscriptionMonitor;
    private static TelephonyNetworkFactory[] sTelephonyNetworkFactories;
    private static UiccController sUiccController;
    private static IWorldPhone sWorldPhone;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneFactory.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.PhoneFactory.<clinit>():void");
    }

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    /* JADX WARNING: Missing block: B:12:?, code:
            sPhoneNotifier = new com.android.internal.telephony.DefaultPhoneNotifier();
            r22 = com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager.getDefault(r32);
            android.telephony.Rlog.i(LOG_TAG, "Cdma Subscription set to " + r22);
            r9 = android.telephony.TelephonyManager.getDefault().getPhoneCount();
            r27 = new int[r9];
            sPhones = new com.android.internal.telephony.Phone[r9];
            sCommandsInterfaces = new com.android.internal.telephony.RIL[r9];
            sTelephonyNetworkFactories = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory[r9];
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:13:0x005c, code:
            if (r6 >= r9) goto L_0x00b8;
     */
    /* JADX WARNING: Missing block: B:14:0x005e, code:
            r27[r6] = com.android.internal.telephony.RILConstants.PREFERRED_NETWORK_MODE;
            android.telephony.Rlog.i(LOG_TAG, "Network Mode set to " + java.lang.Integer.toString(r27[r6]));
            sCommandsInterfaces[r6] = new com.android.internal.telephony.RIL(r32, r27[r6], r22, java.lang.Integer.valueOf(r6));
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            android.telephony.Rlog.i(LOG_TAG, "Creating SubscriptionController");
            com.android.internal.telephony.SubscriptionController.init(r32, sCommandsInterfaces);
            com.mediatek.internal.telephony.RadioManager.init(r32, r9, sCommandsInterfaces);
            sUiccController = com.android.internal.telephony.uicc.UiccController.make(r32, sCommandsInterfaces);
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:32:0x00da, code:
            if (r6 >= r9) goto L_0x0140;
     */
    /* JADX WARNING: Missing block: B:33:0x00dc, code:
            r2 = null;
            r29 = android.telephony.TelephonyManager.getPhoneType(r27[r6]);
     */
    /* JADX WARNING: Missing block: B:34:0x00e6, code:
            if (r29 != 1) goto L_0x0128;
     */
    /* JADX WARNING: Missing block: B:35:0x00e8, code:
            r2 = new com.android.internal.telephony.GsmCdmaPhone(r32, sCommandsInterfaces[r6], sPhoneNotifier, r6, 1, com.android.internal.telephony.TelephonyComponentFactory.getInstance());
     */
    /* JADX WARNING: Missing block: B:36:0x00fa, code:
            android.telephony.Rlog.i(LOG_TAG, "Creating Phone with type = " + r29 + " sub = " + r6);
            sPhones[r6] = r2;
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:38:0x012b, code:
            if (r29 != 2) goto L_0x00fa;
     */
    /* JADX WARNING: Missing block: B:39:0x012d, code:
            r2 = new com.android.internal.telephony.GsmCdmaPhone(r32, sCommandsInterfaces[r6], sPhoneNotifier, r6, 6, com.android.internal.telephony.TelephonyComponentFactory.getInstance());
     */
    /* JADX WARNING: Missing block: B:40:0x0140, code:
            sPhone = sPhones[0];
            sCommandsInterface = sCommandsInterfaces[0];
            r23 = com.android.internal.telephony.SmsApplication.getDefaultSmsApplication(r32, true);
            r28 = "NONE";
     */
    /* JADX WARNING: Missing block: B:41:0x0158, code:
            if (r23 == null) goto L_0x015e;
     */
    /* JADX WARNING: Missing block: B:42:0x015a, code:
            r28 = r23.getPackageName();
     */
    /* JADX WARNING: Missing block: B:43:0x015e, code:
            android.telephony.Rlog.i(LOG_TAG, "defaultSmsApplication: " + r28);
            com.android.internal.telephony.SmsApplication.initSmsPackageMonitor(r32);
            sMadeDefaults = true;
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:44:0x0181, code:
            if (r6 >= r9) goto L_0x0191;
     */
    /* JADX WARNING: Missing block: B:45:0x0183, code:
            sPhones[r6].getServiceStateTracker().pollState();
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:46:0x0191, code:
            android.telephony.Rlog.i(LOG_TAG, "Creating SubInfoRecordUpdater ");
            sSubInfoRecordUpdater = new com.android.internal.telephony.SubscriptionInfoUpdater(r32, sPhones, sCommandsInterfaces);
            com.android.internal.telephony.SubscriptionController.getInstance().updatePhonesAvailability(sPhones);
            sDataSubSelector = new com.mediatek.internal.telephony.dataconnection.DataSubSelector(r32, r9);
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:47:0x01ba, code:
            if (r6 >= r9) goto L_0x01c6;
     */
    /* JADX WARNING: Missing block: B:48:0x01bc, code:
            sPhones[r6].startMonitoringImsService();
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:49:0x01c6, code:
            r13 = com.android.internal.telephony.ITelephonyRegistry.Stub.asInterface(android.os.ServiceManager.getService("telephony.registry"));
            r11 = com.android.internal.telephony.SubscriptionController.getInstance();
            sSubscriptionMonitor = new com.android.internal.telephony.SubscriptionMonitor(r13, sContext, r11, r9);
            sPhoneSwitcher = new com.android.internal.telephony.PhoneSwitcher(1, r9, sContext, r11, android.os.Looper.myLooper(), r13, sCommandsInterfaces, sPhones);
            sProxyController = com.android.internal.telephony.ProxyController.getInstance(r32, sPhones, sUiccController, sCommandsInterfaces, sPhoneSwitcher);
            sTelephonyNetworkFactories = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory[r9];
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:50:0x0205, code:
            if (r6 >= r9) goto L_0x0229;
     */
    /* JADX WARNING: Missing block: B:51:0x0207, code:
            sTelephonyNetworkFactories[r6] = new com.android.internal.telephony.dataconnection.TelephonyNetworkFactory(sPhoneSwitcher, r11, sSubscriptionMonitor, android.os.Looper.myLooper(), sContext, r6, sPhones[r6].mDcTracker);
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:52:0x0229, code:
            com.mediatek.internal.telephony.dataconnection.DataConnectionHelper.makeDataConnectionHelper(r32, sPhones, sPhoneSwitcher);
     */
    /* JADX WARNING: Missing block: B:53:0x0236, code:
            if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldModeSupport() == false) goto L_0x024f;
     */
    /* JADX WARNING: Missing block: B:55:0x023c, code:
            if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldPhoneSupport() == false) goto L_0x024f;
     */
    /* JADX WARNING: Missing block: B:56:0x023e, code:
            android.telephony.Rlog.i(LOG_TAG, "World mode support");
            com.mediatek.internal.telephony.worldphone.WorldMode.init();
     */
    /* JADX WARNING: Missing block: B:62:0x0253, code:
            if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldPhoneSupport() == false) goto L_0x0265;
     */
    /* JADX WARNING: Missing block: B:63:0x0255, code:
            android.telephony.Rlog.i(LOG_TAG, "World phone support");
            sWorldPhone = com.mediatek.internal.telephony.worldphone.WorldPhoneWrapper.getWorldPhoneInstance();
     */
    /* JADX WARNING: Missing block: B:64:0x0265, code:
            android.telephony.Rlog.i(LOG_TAG, "World phone not support");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void makeDefaultPhone(Context context) {
        synchronized (sLockProxyPhones) {
            if (!sMadeDefaults) {
                sContext = context;
                TelephonyDevController.create();
                int retryCount = 0;
                while (true) {
                    boolean hasException = false;
                    retryCount++;
                    try {
                        LocalServerSocket localServerSocket = new LocalServerSocket("com.android.internal.telephony");
                    } catch (IOException e) {
                        hasException = true;
                    }
                    if (!hasException) {
                        break;
                    } else if (retryCount > 3) {
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e2) {
                        }
                    }
                }
            }
        }
        RusInitProcess.execute(context);
    }

    public static Phone getDefaultPhone() {
        Phone phone;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                phone = sPhone;
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phone;
    }

    public static Phone getPhone(int phoneId) {
        Phone phone;
        String dbgInfo = UsimPBMemInfo.STRING_NOT_SET;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                if (phoneId == Integer.MAX_VALUE) {
                    if (DBG) {
                        dbgInfo = "phoneId == DEFAULT_PHONE_ID return sPhone";
                    }
                    phone = sPhone;
                } else {
                    if (DBG) {
                        dbgInfo = "phoneId != DEFAULT_PHONE_ID return sPhones[phoneId]";
                    }
                    phone = (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) ? null : sPhones[phoneId];
                }
                if (DBG) {
                    Rlog.d(LOG_TAG, "getPhone:- " + dbgInfo + " phoneId=" + phoneId + " phone=" + phone);
                }
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phone;
    }

    public static Phone[] getPhones() {
        Phone[] phoneArr;
        synchronized (sLockProxyPhones) {
            if (sMadeDefaults) {
                phoneArr = sPhones;
            } else {
                throw new IllegalStateException("Default phones haven't been made yet!");
            }
        }
        return phoneArr;
    }

    public static IWorldPhone getWorldPhone() {
        if (sWorldPhone == null) {
            Rlog.d(LOG_TAG, "sWorldPhone is null");
        }
        return sWorldPhone;
    }

    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }

    public static int calculatePreferredNetworkType(Context context, int phoneSubId) {
        int networkType = Global.getInt(context.getContentResolver(), "preferred_network_mode" + phoneSubId, RILConstants.PREFERRED_NETWORK_MODE);
        Rlog.d(LOG_TAG, "calculatePreferredNetworkType: phoneSubId = " + phoneSubId + " networkType = " + networkType);
        if (Global.getInt(context.getContentResolver(), "preferred_network_mode" + phoneSubId, -1) == -1) {
            Rlog.d(LOG_TAG, "check persist.radio.lte.chip : " + SystemProperties.get("persist.radio.lte.chip"));
            if (SystemProperties.get("persist.radio.lte.chip").equals("2")) {
                if (SystemProperties.get("ro.boot.opt_c2k_support").equals("1")) {
                    networkType = 7;
                } else {
                    networkType = 0;
                }
                Rlog.d(LOG_TAG, "REFERRED_NETWORK_MODE + " + phoneSubId + " don't have init value yet, force to " + networkType);
            }
        }
        return networkType;
    }

    public static int getDefaultSubscription() {
        return SubscriptionController.getInstance().getDefaultSubId();
    }

    public static boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(sContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
            Rlog.e(LOG_TAG, "Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        boolean prompt = value != 0;
        Rlog.d(LOG_TAG, "SMS Prompt option:" + prompt);
        return prompt;
    }

    public static Phone makeImsPhone(PhoneNotifier phoneNotifier, Phone defaultPhone) {
        return ImsPhoneFactory.makePhone(sContext, phoneNotifier, defaultPhone);
    }

    public static void addLocalLog(String key, int size) {
        synchronized (sLocalLogs) {
            if (sLocalLogs.containsKey(key)) {
                throw new IllegalArgumentException("key " + key + " already present");
            }
            sLocalLogs.put(key, new LocalLog(size));
        }
    }

    public static void localLog(String key, String log) {
        synchronized (sLocalLogs) {
            if (sLocalLogs.containsKey(key)) {
                ((LocalLog) sLocalLogs.get(key)).log(log);
            } else {
                throw new IllegalArgumentException("key " + key + " not found");
            }
        }
    }

    public static void dump(FileDescriptor fd, PrintWriter printwriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printwriter, "  ");
        pw.println("PhoneFactory:");
        pw.println(" sMadeDefaults=" + sMadeDefaults);
        sPhoneSwitcher.dump(fd, pw, args);
        pw.println();
        Phone[] phones = getPhones();
        for (int i = 0; i < phones.length; i++) {
            pw.increaseIndent();
            Phone phone = phones[i];
            try {
                phone.dump(fd, pw, args);
                pw.flush();
                pw.println("++++++++++++++++++++++++++++++++");
                sTelephonyNetworkFactories[i].dump(fd, pw, args);
                pw.flush();
                pw.println("++++++++++++++++++++++++++++++++");
                try {
                    ((IccCardProxy) phone.getIccCard()).dump(fd, pw, args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pw.flush();
                pw.decreaseIndent();
                pw.println("++++++++++++++++++++++++++++++++");
            } catch (Exception e2) {
                pw.println("Telephony DebugService: Could not get Phone[" + i + "] e=" + e2);
            }
        }
        pw.println("SubscriptionMonitor:");
        pw.increaseIndent();
        try {
            sSubscriptionMonitor.dump(fd, pw, args);
        } catch (Exception e22) {
            e22.printStackTrace();
        }
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("UiccController:");
        pw.increaseIndent();
        try {
            sUiccController.dump(fd, pw, args);
        } catch (Exception e222) {
            e222.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubscriptionController:");
        pw.increaseIndent();
        try {
            SubscriptionController.getInstance().dump(fd, pw, args);
        } catch (Exception e2222) {
            e2222.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("SubInfoRecordUpdater:");
        pw.increaseIndent();
        try {
            sSubInfoRecordUpdater.dump(fd, pw, args);
        } catch (Exception e22222) {
            e22222.printStackTrace();
        }
        pw.flush();
        pw.decreaseIndent();
        pw.println("++++++++++++++++++++++++++++++++");
        pw.println("LocalLogs:");
        pw.increaseIndent();
        synchronized (sLocalLogs) {
            for (String key : sLocalLogs.keySet()) {
                pw.println(key);
                pw.increaseIndent();
                ((LocalLog) sLocalLogs.get(key)).dump(fd, pw, args);
                pw.decreaseIndent();
            }
            pw.flush();
        }
        pw.decreaseIndent();
    }
}

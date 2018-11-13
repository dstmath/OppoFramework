package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager.Stub;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.OppoManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Downloads;
import android.system.ErrnoException;
import android.system.Os;
import android.telephony.ColorOSTelephonyManager;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.oppo.ota.OppoOtaUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BootReceiver extends BroadcastReceiver {
    private static final String BOOT_REASON_FILE = "/sys/power/app_boot";
    private static final char[] DIGITS = null;
    private static final String LAST_HEADER_FILE = "last-header.txt";
    private static final String LOG_FILES_FILE = "log-files.xml";
    private static final int LOG_SIZE = 0;
    private static final String OLD_UPDATER_CLASS = "com.google.android.systemupdater.SystemUpdateReceiver";
    private static final String OLD_UPDATER_PACKAGE = "com.google.android.systemupdater";
    private static final String TAG = "BootReceiver";
    private static final File TOMBSTONE_DIR = null;
    private static final String UNKNOW_REBOOT_PFF = "/sys/power/poff_reason";
    private static final String UNKNOW_REBOOT_PON = "/sys/power/pon_reason";
    private static final char[] UPPER_CASE_DIGITS = null;
    private static final File lastHeaderFile = null;
    private static final AtomicFile sFile = null;
    private static FileObserver sTombstoneObserver;
    public final int MSG_UPDATEIMEI;
    public final int MSG_UPDATESTATE;
    private Context mContext;
    Handler mGetStateHandler;
    private final String mLastExceptionProc;
    private final String mLastExceptionProperty;

    /* renamed from: com.android.server.BootReceiver$3 */
    class AnonymousClass3 extends FileObserver {
        final /* synthetic */ BootReceiver this$0;
        final /* synthetic */ DropBoxManager val$db;
        final /* synthetic */ String val$headers;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.BootReceiver.3.<init>(com.android.server.BootReceiver, java.lang.String, int, android.os.DropBoxManager, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass3(com.android.server.BootReceiver r1, java.lang.String r2, int r3, android.os.DropBoxManager r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.BootReceiver.3.<init>(com.android.server.BootReceiver, java.lang.String, int, android.os.DropBoxManager, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.BootReceiver.3.<init>(com.android.server.BootReceiver, java.lang.String, int, android.os.DropBoxManager, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.BootReceiver.3.onEvent(int, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onEvent(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.BootReceiver.3.onEvent(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.BootReceiver.3.onEvent(int, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.BootReceiver.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.BootReceiver.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BootReceiver.<clinit>():void");
    }

    public BootReceiver() {
        this.MSG_UPDATESTATE = 1001;
        this.MSG_UPDATEIMEI = 1002;
        this.mGetStateHandler = new Handler() {
            int mImeiCounter = 0;

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1001:
                        updatePhoneState(BootReceiver.this.mContext);
                        BootReceiver.this.writeSpecialNVtoData(BootReceiver.this.mContext);
                        return;
                    case 1002:
                        this.mImeiCounter++;
                        Slog.v(BootReceiver.TAG, "in handleMessage MSG_UPDATEIMEI " + this.mImeiCounter);
                        if (!updateIMEI(BootReceiver.this.mContext)) {
                            sendEmptyMessageDelayed(1002, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
                            return;
                        }
                        return;
                    default:
                        Slog.v(BootReceiver.TAG, "invalid msg");
                        return;
                }
            }

            private void updatePhoneState(Context context) {
                String device = Build.DEVICE;
                String androidVer = OppoManager.getVersionFOrAndroid();
                String IMEI = OppoManager.getIMEINums(context);
                String buildVer = OppoManager.getBuildVersion();
                String recDevice = OppoManager.readCriticalData(OppoManager.TYEP_DEVICE, 512);
                String recAndroidVer = OppoManager.readCriticalData(OppoManager.TYEP_Android_VER, 512);
                String recIMEI = OppoManager.readCriticalData(OppoManager.TYEP_PHONE_IMEI, 512);
                String recBuildVer = OppoManager.readCriticalData(OppoManager.TYEP_BUILD_VER, 512);
                Slog.v(BootReceiver.TAG, "record device is " + recDevice + " androidVer = " + recAndroidVer + " imei = " + recIMEI + " build = " + recBuildVer);
                if (!device.equals(recDevice)) {
                    Slog.v(BootReceiver.TAG, "device res = " + OppoManager.writeCriticalData(OppoManager.TYEP_DEVICE, device));
                }
                if (!androidVer.equals(recAndroidVer)) {
                    Slog.v(BootReceiver.TAG, "androidver res = " + OppoManager.writeCriticalData(OppoManager.TYEP_Android_VER, androidVer));
                }
                if (IMEI.equals("null")) {
                    sendEmptyMessageDelayed(1002, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
                } else if (!IMEI.equals(recIMEI)) {
                    Slog.v(BootReceiver.TAG, "imei res = " + OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI, IMEI));
                }
                if (!buildVer.equals(recBuildVer)) {
                    Slog.v(BootReceiver.TAG, "buildVer res = " + OppoManager.writeCriticalData(OppoManager.TYEP_BUILD_VER, buildVer));
                }
                String historyVer = OppoManager.readCriticalData(OppoManager.TYEP_BUILD_VER + 1024, 512);
                if (historyVer == null) {
                    return;
                }
                if (historyVer.equals("null") || historyVer.isEmpty()) {
                    Slog.v(BootReceiver.TAG, "record new vesion to history ");
                    OppoManager.writeCriticalData(OppoManager.TYEP_DEVICE + 1024, device);
                    OppoManager.writeCriticalData(OppoManager.TYEP_Android_VER + 1024, androidVer);
                    OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI + 1024, IMEI);
                    OppoManager.writeCriticalData(OppoManager.TYEP_BUILD_VER + 1024, buildVer);
                }
            }

            boolean updateIMEI(Context context) {
                String IMEI = OppoManager.getIMEINums(context);
                String recIMEI = OppoManager.readCriticalData(OppoManager.TYEP_PHONE_IMEI, 512);
                if (IMEI.equals("null") && this.mImeiCounter <= 5) {
                    return false;
                }
                if (this.mImeiCounter > 5) {
                    if (PhoneConstants.MVNO_TYPE_NONE.equals(recIMEI)) {
                        int res = OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI, IMEI);
                        Slog.v(BootReceiver.TAG, "imei record imie  " + IMEI);
                        this.mImeiCounter = 0;
                        return true;
                    }
                } else if (!("null".equals(IMEI) || IMEI.equals(recIMEI))) {
                    Slog.v(BootReceiver.TAG, "imei res = " + OppoManager.writeCriticalData(OppoManager.TYEP_PHONE_IMEI, IMEI));
                    this.mImeiCounter = 0;
                    return true;
                }
                return true;
            }
        };
        this.mLastExceptionProc = "/proc/sys/kernel/hung_task_oppo_kill";
        this.mLastExceptionProperty = "persist.hungtask.oppo.kill";
    }

    public void onReceive(final Context context, Intent intent) {
        if (!"trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"))) {
            this.mContext = context;
            new Thread() {
                public void run() {
                    Slog.v(BootReceiver.TAG, "send delayed message");
                    BootReceiver.this.mGetStateHandler.sendEmptyMessageDelayed(1001, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
                    BootReceiver.this.recordRootState();
                    if (OppoManager.incrementCriticalData(OppoManager.TYPE_REBOOT, BootReceiver.this.mContext.getResources().getString(R.string.type_issue_reboot)) == -2) {
                        Slog.e(BootReceiver.TAG, "increment reboot times failed!!");
                    }
                    BootReceiver.this.updateSerialNoNVLid();
                    String lastReboot = BootReceiver.this.isRebootExceptionFromBolckException();
                    if (lastReboot != null) {
                        OppoManager.writeLogToPartition(OppoManager.TYPE_REBOOT_FROM_BLOCKED, lastReboot, "ANDROID", "reboot_from_blocked", BootReceiver.this.mContext.getResources().getString(R.string.type_issue_reboot_blocked));
                    }
                    OppoOtaUtils.notifyOTAUpdateResult(context);
                    try {
                        BootReceiver.this.logBootEvents(context);
                    } catch (Exception e) {
                        Slog.e(BootReceiver.TAG, "Can't log boot events", e);
                    }
                    boolean onlyCore = false;
                    try {
                        onlyCore = Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
                    } catch (RemoteException e2) {
                    }
                    if (!onlyCore) {
                        try {
                            BootReceiver.this.removeOldUpdatePackages(context);
                        } catch (Exception e3) {
                            Slog.e(BootReceiver.TAG, "Can't remove old update packages", e3);
                        }
                    }
                    try {
                        OppoManager.syncCacheToEmmc();
                        Slog.v(BootReceiver.TAG, "syncCacheToEmmc");
                    } catch (Exception e32) {
                        Slog.e(BootReceiver.TAG, "sync criticallog failed e + " + e32.toString());
                    }
                }
            }.start();
        }
    }

    private void updateSerialNoNVLid() {
        String serialnum = SystemProperties.get("ro.serialno", " ");
        String SERIALNUM_FILENAME = "/data/nvram/APCFG/APRDEB/SERIAL_NUM";
        try {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(ServiceManager.getService("NvRAMAgent"));
            byte[] buff = null;
            try {
                buff = agent.readFileByName(SERIALNUM_FILENAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (buff != null) {
                String oldSerial = new String(buff, 0, 16);
                Slog.v(TAG, "updateSerialNoNVLid:" + oldSerial + " , serialnum:" + serialnum);
                if (!oldSerial.equals(serialnum)) {
                    Slog.v(TAG, "need updateSerialNoNVLid!");
                    char[] temp = serialnum.toCharArray();
                    for (int i = 0; i < 16; i++) {
                        buff[i] = (byte) temp[i];
                    }
                    buff[16] = (byte) 0;
                    try {
                        agent.writeFileByName(SERIALNUM_FILENAME, buff);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (Exception e22) {
            e22.printStackTrace();
        }
    }

    public static String bytesToHexString(byte[] bytes, boolean upperCase) {
        char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
        char[] buf = new char[(bytes.length * 2)];
        int c = 0;
        for (byte b : bytes) {
            int i = c + 1;
            buf[c] = digits[(b >> 4) & 15];
            c = i + 1;
            buf[i] = digits[b & 15];
        }
        return new String(buf);
    }

    private String readNvItem(String item, int type) {
        String result = "00";
        try {
            byte[] buff = null;
            try {
                buff = NvRAMAgent.Stub.asInterface(ServiceManager.getService("NvRAMAgent")).readFileByName(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (buff != null) {
                if (type == 0) {
                    result = new String(buff);
                } else if (type == 1) {
                    result = bytesToHexString(buff, true);
                } else {
                    StringBuffer buffer = new StringBuffer(bytesToHexString(buff, true).substring(0, 12));
                    for (int i = 10; i > 0; i -= 2) {
                        buffer = buffer.insert(i, ':');
                    }
                    result = buffer.toString();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        Slog.d(TAG, "readNvItem " + result);
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x01ac A:{SYNTHETIC, Splitter: B:32:0x01ac} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x01b1 A:{Catch:{ IOException -> 0x01b5 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01bd A:{SYNTHETIC, Splitter: B:40:0x01bd} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x01c2 A:{Catch:{ IOException -> 0x01c6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x01ac A:{SYNTHETIC, Splitter: B:32:0x01ac} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x01b1 A:{Catch:{ IOException -> 0x01b5 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01bd A:{SYNTHETIC, Splitter: B:40:0x01bd} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x01c2 A:{Catch:{ IOException -> 0x01c6 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeSpecialNVtoData(Context context) {
        IOException e;
        Throwable th;
        if (SystemProperties.getInt("oppo.device.firstboot", 0) == 0) {
            Slog.d(TAG, "writeSpecialNVtoData not firstboot return");
            return;
        }
        Slog.d(TAG, "writeSpecialNVtoData Entry");
        String productCmd = "/data/system/indicate";
        String carrierNameFull = readNvItem("/data/nvram/APCFG/APRDEB/CARRIER_VER", 0);
        String carrierName = carrierNameFull.substring(0, carrierNameFull.length() - 1);
        String mmiResult = readNvItem("/data/nvram/APCFG/APRDEB/ENG_RESULT", 1);
        String address = readNvItem("/data/nvram/APCFG/APRDEB/BT_Addr", 2);
        WifiInfo wifiInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
        String macAddress = wifiInfo == null ? "00" : wifiInfo.getMacAddress();
        String pcba = SystemProperties.get("gsm.serial", "null");
        ColorOSTelephonyManager telephonyManager = ColorOSTelephonyManager.getDefault(context);
        String imei_1 = telephonyManager.colorGetImei(0);
        String imei_2 = telephonyManager.colorGetImei(1);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            BufferedWriter bw2;
            FileWriter fw2 = new FileWriter(productCmd, true);
            try {
                bw2 = new BufferedWriter(fw2);
            } catch (IOException e2) {
                e = e2;
                fw = fw2;
                try {
                    e.printStackTrace();
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (fw != null) {
                        fw.close();
                    }
                    Os.chmod(productCmd, 438);
                    Slog.d(TAG, "writeSpecialNVtoData Exit");
                } catch (Throwable th2) {
                    th = th2;
                    if (bw != null) {
                    }
                    if (fw != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fw = fw2;
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                        throw th;
                    }
                }
                if (fw != null) {
                    fw.close();
                }
                throw th;
            }
            try {
                bw2.write("WIFI: " + macAddress);
                bw2.newLine();
                bw2.write("BT: " + address);
                bw2.newLine();
                bw2.write("PCBA: " + pcba);
                bw2.newLine();
                bw2.write("MMI: " + mmiResult);
                bw2.newLine();
                bw2.write("Carrier: " + carrierName);
                bw2.newLine();
                bw2.write("IMEI1: " + imei_1);
                bw2.newLine();
                bw2.write("IMEI2: " + imei_2);
                bw2.flush();
                if (bw2 != null) {
                    try {
                        bw2.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                if (fw2 != null) {
                    fw2.close();
                }
                fw = fw2;
            } catch (IOException e4) {
                e322 = e4;
                bw = bw2;
                fw = fw2;
                e322.printStackTrace();
                if (bw != null) {
                }
                if (fw != null) {
                }
                Os.chmod(productCmd, 438);
                Slog.d(TAG, "writeSpecialNVtoData Exit");
            } catch (Throwable th4) {
                th = th4;
                bw = bw2;
                fw = fw2;
                if (bw != null) {
                }
                if (fw != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e322 = e5;
            e322.printStackTrace();
            if (bw != null) {
            }
            if (fw != null) {
            }
            Os.chmod(productCmd, 438);
            Slog.d(TAG, "writeSpecialNVtoData Exit");
        }
        try {
            Os.chmod(productCmd, 438);
        } catch (ErrnoException e6) {
            e6.printStackTrace();
        }
        Slog.d(TAG, "writeSpecialNVtoData Exit");
    }

    private void removeOldUpdatePackages(Context context) {
        Downloads.removeAllDownloadsByPackage(context, OLD_UPDATER_PACKAGE, OLD_UPDATER_CLASS);
    }

    private String getPreviousBootHeaders() {
        try {
            return FileUtils.readTextFile(lastHeaderFile, 0, null);
        } catch (IOException e) {
            Slog.e(TAG, "Error reading " + lastHeaderFile, e);
            return null;
        }
    }

    private String getCurrentBootHeaders() throws IOException {
        return "Build: " + Build.FINGERPRINT + "\n" + "Hardware: " + Build.BOARD + "\n" + "Revision: " + SystemProperties.get("ro.revision", PhoneConstants.MVNO_TYPE_NONE) + "\n" + "Bootloader: " + Build.BOOTLOADER + "\n" + "Radio: " + Build.RADIO + "\n" + "Kernel: " + FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n") + "\n";
    }

    private String getBootHeadersToLogAndUpdate() throws IOException {
        String oldHeaders = getPreviousBootHeaders();
        String newHeaders = getCurrentBootHeaders();
        try {
            FileUtils.stringToFile(lastHeaderFile, newHeaders);
        } catch (IOException e) {
            Slog.e(TAG, "Error writing " + lastHeaderFile, e);
        }
        if (oldHeaders == null) {
            return "isPrevious: false\n" + newHeaders;
        }
        return "isPrevious: true\n" + oldHeaders;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void logBootEvents(android.content.Context r19) throws java.io.IOException {
        /*
        r18 = this;
        r6 = "dropbox";
        r0 = r19;
        r2 = r0.getSystemService(r6);
        r2 = (android.os.DropBoxManager) r2;
        r4 = r18.getBootHeadersToLogAndUpdate();
        r12 = readBootReason();
        r6 = "BootReceiver";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "Aha! Boot reason is ";
        r7 = r7.append(r8);
        r7 = r7.append(r12);
        r8 = "!!!";
        r7 = r7.append(r8);
        r7 = r7.toString();
        android.util.Slog.d(r6, r7);
        r16 = android.os.RecoverySystem.handleAftermath(r19);
        if (r16 == 0) goto L_0x0055;
    L_0x003a:
        if (r2 == 0) goto L_0x0055;
    L_0x003c:
        r6 = "SYSTEM_RECOVERY_LOG";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r7 = r7.append(r4);
        r0 = r16;
        r7 = r7.append(r0);
        r7 = r7.toString();
        r2.addText(r6, r7);
    L_0x0055:
        r5 = "";
        if (r12 == 0) goto L_0x0085;
    L_0x005a:
        r6 = new java.lang.StringBuilder;
        r7 = 512; // 0x200 float:7.175E-43 double:2.53E-321;
        r6.<init>(r7);
        r7 = "\n";
        r6 = r6.append(r7);
        r7 = "Boot info:\n";
        r6 = r6.append(r7);
        r7 = "Last boot reason: ";
        r6 = r6.append(r7);
        r6 = r6.append(r12);
        r7 = "\n";
        r6 = r6.append(r7);
        r5 = r6.toString();
    L_0x0085:
        r3 = readTimestamps();
        r6 = "ro.runtime.firstboot";
        r8 = 0;
        r6 = android.os.SystemProperties.getLong(r6, r8);
        r8 = 0;
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 != 0) goto L_0x0239;
    L_0x0098:
        r6 = android.os.storage.StorageManager.inCryptKeeperBounce();
        if (r6 == 0) goto L_0x01f9;
    L_0x009e:
        if (r2 == 0) goto L_0x00a6;
    L_0x00a0:
        r6 = "SYSTEM_BOOT";
        r2.addText(r6, r4);
    L_0x00a6:
        r6 = "kernel";
        r6 = r12.equals(r6);
        if (r6 != 0) goto L_0x00b8;
    L_0x00af:
        r6 = "modem";
        r6 = r12.equals(r6);
        if (r6 == 0) goto L_0x0119;
    L_0x00b8:
        r6 = "/proc/last_kmsg";
        r7 = LOG_SIZE;
        r7 = -r7;
        r8 = "SYSTEM_LAST_KMSG";
        addFileWithFootersToDropBox(r2, r3, r4, r5, r6, r7, r8);
        r6 = "/sys/fs/pstore/console-ramoops";
        r7 = LOG_SIZE;
        r7 = -r7;
        r8 = "SYSTEM_LAST_KMSG";
        addFileWithFootersToDropBox(r2, r3, r4, r5, r6, r7, r8);
        r9 = "/cache/recovery/log";
        r6 = LOG_SIZE;
        r10 = -r6;
        r11 = "SYSTEM_RECOVERY_LOG";
        r6 = r2;
        r7 = r3;
        r8 = r4;
        addFileToDropBox(r6, r7, r8, r9, r10, r11);
        r9 = "/cache/recovery/last_kmsg";
        r6 = LOG_SIZE;
        r10 = -r6;
        r11 = "SYSTEM_RECOVERY_KMSG";
        r6 = r2;
        r7 = r3;
        r8 = r4;
        addFileToDropBox(r6, r7, r8, r9, r10, r11);
        r6 = LOG_SIZE;
        r6 = -r6;
        r7 = "SYSTEM_AUDIT";
        addAuditErrorsToDropBox(r2, r3, r4, r6, r7);
        r6 = LOG_SIZE;
        r6 = -r6;
        r7 = "SYSTEM_FSCK";
        addFsckErrorsToDropBox(r2, r3, r4, r6, r7);
        r6 = android.os.OppoManager.TYPE_PANIC;
        r7 = "kernel_panic";
        r8 = "KERNEL";
        r9 = "panic";
        r10 = r19.getResources();
        r11 = 17040927; // 0x104061f float:2.4248963E-38 double:8.4193366E-317;
        r10 = r10.getString(r11);
        android.os.OppoManager.writeLogToPartition(r6, r7, r8, r9, r10);
    L_0x0119:
        r6 = "BootReceiver";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "persist.sys.oppo.reboot = ";
        r7 = r7.append(r8);
        r8 = "persist.sys.oppo.reboot";
        r9 = "";
        r8 = android.os.SystemProperties.get(r8, r9);
        r7 = r7.append(r8);
        r7 = r7.toString();
        android.util.Slog.v(r6, r7);
        r6 = "BootReceiver";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "persist.sys.oppo.fatal = ";
        r7 = r7.append(r8);
        r8 = "persist.sys.oppo.fatal";
        r9 = "";
        r8 = android.os.SystemProperties.get(r8, r9);
        r7 = r7.append(r8);
        r7 = r7.toString();
        android.util.Slog.v(r6, r7);
        r6 = "BootReceiver";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "oppo.device.firstboot = ";
        r7 = r7.append(r8);
        r8 = "oppo.device.firstboot";
        r9 = "";
        r8 = android.os.SystemProperties.get(r8, r9);
        r7 = r7.append(r8);
        r7 = r7.toString();
        android.util.Slog.v(r6, r7);
        r6 = "kernel";
        r6 = r12.equals(r6);
        if (r6 != 0) goto L_0x01b4;
    L_0x018e:
        r6 = "persist.sys.oppo.fatal";
        r7 = "";
        r6 = android.os.SystemProperties.get(r6, r7);
        r7 = "1";
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x01b4;
    L_0x01a1:
        r6 = "oppo.device.firstboot";
        r7 = "";
        r6 = android.os.SystemProperties.get(r6, r7);
        r7 = "1";
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x0209;
    L_0x01b4:
        r6 = "persist.sys.oppo.reboot";
        r7 = "";
        android.os.SystemProperties.set(r6, r7);
        r6 = "persist.sys.oppo.fatal";
        r7 = "";
        android.os.SystemProperties.set(r6, r7);
        r6 = "sys.oppo.junkclean";
        r7 = "1";
        android.os.SystemProperties.set(r6, r7);
    L_0x01cf:
        r6 = TOMBSTONE_DIR;
        r17 = r6.listFiles();
        r13 = 0;
    L_0x01d6:
        if (r17 == 0) goto L_0x025f;
    L_0x01d8:
        r0 = r17;
        r6 = r0.length;
        if (r13 >= r6) goto L_0x025f;
    L_0x01dd:
        r6 = r17[r13];
        r6 = r6.isFile();
        if (r6 == 0) goto L_0x01f6;
    L_0x01e5:
        r6 = r17[r13];
        r9 = r6.getPath();
        r10 = LOG_SIZE;
        r11 = "SYSTEM_TOMBSTONE";
        r6 = r2;
        r7 = r3;
        r8 = r4;
        addFileToDropBox(r6, r7, r8, r9, r10, r11);
    L_0x01f6:
        r13 = r13 + 1;
        goto L_0x01d6;
    L_0x01f9:
        r6 = java.lang.System.currentTimeMillis();
        r15 = java.lang.Long.toString(r6);
        r6 = "ro.runtime.firstboot";
        android.os.SystemProperties.set(r6, r15);
        goto L_0x009e;
    L_0x0209:
        if (r2 == 0) goto L_0x01b4;
    L_0x020b:
        r6 = "persist.sys.oppo.fb_upgraded";
        r7 = "";
        r6 = android.os.SystemProperties.get(r6, r7);
        r7 = "1";
        r6 = r6.equals(r7);
        if (r6 == 0) goto L_0x01b4;
    L_0x021e:
        r6 = android.os.OppoManager.TYPE_ANDROID_UNKNOWN_REBOOT;
        r7 = "unknown_reboot";
        r8 = "KERNEL";
        r9 = "panic";
        r10 = r19.getResources();
        r11 = 17040927; // 0x104061f float:2.4248963E-38 double:8.4193366E-317;
        r10 = r10.getString(r11);
        android.os.OppoManager.writeLogToPartition(r6, r7, r8, r9, r10);
        goto L_0x01b4;
    L_0x0239:
        if (r2 == 0) goto L_0x0241;
    L_0x023b:
        r6 = "SYSTEM_RESTART";
        r2.addText(r6, r4);
    L_0x0241:
        r14 = r18.isLastSystemServerRebootFormBolckException();
        if (r14 == 0) goto L_0x01cf;
    L_0x0247:
        r6 = android.os.OppoManager.TYPE_ANDROID_SYSTEM_REBOOT_FROM_BLOCKED;
        r7 = "ANDROID";
        r8 = "reboot_from_blocked";
        r9 = r19.getResources();
        r10 = 17040930; // 0x1040622 float:2.424897E-38 double:8.419338E-317;
        r9 = r9.getString(r10);
        android.os.OppoManager.writeLogToPartition(r6, r14, r7, r8, r9);
        goto L_0x01cf;
    L_0x025f:
        r0 = r18;
        r0.writeTimestamps(r3);
        r6 = new com.android.server.BootReceiver$3;
        r7 = TOMBSTONE_DIR;
        r8 = r7.getPath();
        r9 = 8;
        r7 = r18;
        r10 = r2;
        r11 = r4;
        r6.<init>(r7, r8, r9, r10, r11);
        sTombstoneObserver = r6;
        r6 = sTombstoneObserver;
        r6.startWatching();
        r6 = "persist.sys.oppo.fb_upgraded";
        r7 = "1";
        android.os.SystemProperties.set(r6, r7);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BootReceiver.logBootEvents(android.content.Context):void");
    }

    private static void addFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) throws IOException {
        addFileWithFootersToDropBox(db, timestamps, headers, PhoneConstants.MVNO_TYPE_NONE, filename, maxSize, tag);
    }

    private static void addFileWithFootersToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String footers, String filename, int maxSize, String tag) throws IOException {
        if (db != null && db.isTagEnabled(tag)) {
            File file = new File(filename);
            long fileTime = file.lastModified();
            if (fileTime > 0) {
                if (!timestamps.containsKey(filename) || ((Long) timestamps.get(filename)).longValue() != fileTime) {
                    timestamps.put(filename, Long.valueOf(fileTime));
                    Slog.i(TAG, "Copying " + filename + " to DropBox (" + tag + ")");
                    db.addText(tag, headers + FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n") + footers);
                }
            }
        }
    }

    private static void addAuditErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        if (db != null && db.isTagEnabled(tag)) {
            Slog.i(TAG, "Copying audit failures to DropBox");
            File file = new File("/proc/last_kmsg");
            long fileTime = file.lastModified();
            if (fileTime <= 0) {
                file = new File("/sys/fs/pstore/console-ramoops");
                fileTime = file.lastModified();
            }
            if (fileTime > 0) {
                if (!timestamps.containsKey(tag) || ((Long) timestamps.get(tag)).longValue() != fileTime) {
                    timestamps.put(tag, Long.valueOf(fileTime));
                    String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                    StringBuilder sb = new StringBuilder();
                    for (String line : log.split("\n")) {
                        if (line.contains("audit")) {
                            sb.append(line).append("\n");
                        }
                    }
                    Slog.i(TAG, "Copied " + sb.toString().length() + " worth of audits to DropBox");
                    db.addText(tag, headers + sb.toString());
                }
            }
        }
    }

    private static void addFsckErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        boolean upload_needed = false;
        if (db != null && db.isTagEnabled(tag)) {
            Slog.i(TAG, "Checking for fsck errors");
            File file = new File("/dev/fscklogs/log");
            if (file.lastModified() > 0) {
                String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                StringBuilder sb = new StringBuilder();
                for (String line : log.split("\n")) {
                    if (line.contains("FILE SYSTEM WAS MODIFIED")) {
                        upload_needed = true;
                        break;
                    }
                }
                if (upload_needed) {
                    addFileToDropBox(db, timestamps, headers, "/dev/fscklogs/log", maxSize, tag);
                }
                file.delete();
            }
        }
    }

    private static HashMap<String, Long> readTimestamps() {
        HashMap<String, Long> timestamps;
        boolean success;
        Throwable th;
        FileInputStream stream;
        Throwable th2;
        synchronized (sFile) {
            timestamps = new HashMap();
            success = false;
            th = null;
            stream = null;
            try {
                int type;
                stream = sFile.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new IllegalStateException("no start tag found");
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        success = true;
                    } else if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals("log")) {
                            timestamps.put(parser.getAttributeValue(null, "filename"), Long.valueOf(Long.valueOf(parser.getAttributeValue(null, "timestamp")).longValue()));
                        } else {
                            Slog.w(TAG, "Unknown tag: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
                success = true;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                if (th != null) {
                    throw th;
                } else {
                    if (1 == null) {
                        timestamps.clear();
                    }
                }
            } catch (Throwable th4) {
                Throwable th5 = th4;
                th4 = th2;
                th2 = th5;
            }
        }
        return timestamps;
        if (stream != null) {
            try {
                stream.close();
            } catch (Throwable th6) {
                if (th4 == null) {
                    th4 = th6;
                } else if (th4 != th6) {
                    th4.addSuppressed(th6);
                }
            }
        }
        if (th4 != null) {
            try {
                throw th4;
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "No existing last log timestamp file " + sFile.getBaseFile() + "; starting empty");
                if (!success) {
                    timestamps.clear();
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (!success) {
                    timestamps.clear();
                }
            } catch (IllegalStateException e3) {
                Slog.w(TAG, "Failed parsing " + e3);
                if (!success) {
                    timestamps.clear();
                }
            } catch (NullPointerException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (!success) {
                    timestamps.clear();
                }
            } catch (XmlPullParserException e5) {
                Slog.w(TAG, "Failed parsing " + e5);
                if (!success) {
                    timestamps.clear();
                }
            } catch (Throwable th7) {
                if (!success) {
                    timestamps.clear();
                }
            }
        } else {
            throw th2;
        }
    }

    private void writeTimestamps(HashMap<String, Long> timestamps) {
        synchronized (sFile) {
            try {
                FileOutputStream stream = sFile.startWrite();
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "log-files");
                    for (String filename : timestamps.keySet()) {
                        out.startTag(null, "log");
                        out.attribute(null, "filename", filename);
                        out.attribute(null, "timestamp", ((Long) timestamps.get(filename)).toString());
                        out.endTag(null, "log");
                    }
                    out.endTag(null, "log-files");
                    out.endDocument();
                    sFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to write timestamp file, using the backup: " + e);
                    sFile.failWrite(stream);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to write timestamp file: " + e2);
                return;
            }
        }
        return;
    }

    private static String readBootReason() {
        String res = PhoneConstants.MVNO_TYPE_NONE;
        try {
            FileInputStream fin = new FileInputStream(BOOT_REASON_FILE);
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer);
            res = new StringBuffer().append(new String(buffer)).toString().trim();
            fin.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }

    private String isLastSystemServerRebootFormBolckException() {
        Exception e;
        if (new File("/proc/sys/kernel/hung_task_oppo_kill").exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader("/proc/sys/kernel/hung_task_oppo_kill"));
                try {
                    String strSend = in.readLine();
                    if (in != null) {
                        in.close();
                    }
                    if (!strSend.trim().isEmpty()) {
                        return strSend;
                    }
                    BufferedReader bufferedReader = in;
                    return null;
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                return null;
            }
        }
        Slog.v(TAG, "reboot file is not exists");
        return null;
    }

    private String isRebootExceptionFromBolckException() {
        String strSend = SystemProperties.get("persist.hungtask.oppo.kill");
        if (strSend == null || strSend.isEmpty()) {
            return null;
        }
        return strSend;
    }

    private static String readUnknowRebootStatus() {
        String res = PhoneConstants.MVNO_TYPE_NONE;
        try {
            FileInputStream finPon = new FileInputStream(UNKNOW_REBOOT_PON);
            FileInputStream finPff = new FileInputStream(UNKNOW_REBOOT_PFF);
            byte[] bufferPon = new byte[finPon.available()];
            byte[] bufferPff = new byte[finPon.available()];
            finPon.read(bufferPon);
            finPff.read(bufferPff);
            res = new StringBuffer().append(new String(bufferPon) + " " + new String(bufferPff)).toString();
            finPon.close();
            finPff.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
    }

    void recordRootState() {
    }
}

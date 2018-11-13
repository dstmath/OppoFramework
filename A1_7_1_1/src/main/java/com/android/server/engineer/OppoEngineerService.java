package com.android.server.engineer;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.engineer.IOppoEngineerService.Stub;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Slog;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.notification.NotificationManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
public class OppoEngineerService extends SystemService {
    private static final String ATM_MODEM_MODE_NORMAL = "normal";
    private static final String ATM_MODEM_MODE_PROPERTY = "persist.atm.mdmode";
    private static final String BROADCAST_ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final String BUILD_RELEASE_TYPE_PROPERTY = "ro.build.release_type";
    private static final String CLEAR_ITEM_METHOD = "cleanItem";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = null;
    private static final String CRITICAL_LOG_CONFIG_FILTER_NAME = "criticallog_config";
    private static final int DISABLE_FLAG = 1;
    private static final int ENCRYPT_LENTH = 8;
    private static final String GET_SECRECY_STATE_METHOD = "getSecrecyState";
    private static final int HANDLE_ROM_UPDATE_MSG = 1000001;
    private static final int KEY1 = 12345678;
    private static final int KEY2 = 23131123;
    private static final String META_TST_SERVICE = "meta_tst";
    private static final int NVRAM_FACTORY_NUMBER_INFO_LENGTH = 16;
    private static final int OFFSET = 15730176;
    private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String OPPO_MANAGER_CLASS_NAME = "android.os.OppoManager";
    private static final String PARTION_PROTECT_FLAG_PATH = "/dev/block/platform/bootdevice/by-name/reserve4";
    private static final int PARTION_PROTECT_NOTIFICAITON_ID = 10000;
    private static final boolean PARTION_PROTECT_OFF = false;
    private static final boolean PARTION_PROTECT_ON = true;
    private static final String PCBA_PROPERTY = "gsm.serial";
    private static final String PCBA_UNKNOWN_DEFAULT = "UNKNOWN";
    private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final int SECRECY_ADB_TYPE = 4;
    private static final String SECRECY_INTERNAL_CLASS_NAME = "android.secrecy.SecrecyManagerInternal";
    private static final String SECRECY_SUPPORT_FEATURE = "oppo.secrecy.support";
    private static final String SYNC_CACHE_TO_EMMC_METHOD = "syncCacheToEmmc";
    public static final String TAG = "OppoEngineerService";
    private static final String USB_CHARGE_SWITCH_PROPERTY = "sys.usb.config.meta";
    private static final String WRITE_LOG_TO_PARTION_METHOD = "writeLogToPartition";
    private static final int WRITE_PROTECT_NEED_RESET_ISSUE_TYPE = 101;
    private static final String WRITE_PROTECT_RESET_CONFIG = "WriteProtectReset state=\"true\"";
    private static final int WRITE_PROTECT_RESET_DONE_ISSUE_TYPE = 102;
    private final Context mContext;
    private final OppoEngineerHandler mHandler;
    private final Object mLock;
    private final ServiceThread mServiceThread;
    private BroadcastReceiver mUpdateBroadcastReceiver;

    private class OppoEngineerHandler extends Handler {
        public OppoEngineerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OppoEngineerService.HANDLE_ROM_UPDATE_MSG /*1000001*/:
                    String update = OppoEngineerService.this.getDataFromProvider();
                    if (update != null && update.contains(OppoEngineerService.WRITE_PROTECT_RESET_CONFIG)) {
                        if (OppoEngineerService.this.resetWriteProtectState()) {
                            Slog.i(OppoEngineerService.TAG, "reset wp state success");
                            if (SystemProperties.getBoolean(OppoEngineerService.USB_CHARGE_SWITCH_PROPERTY, false)) {
                                SystemProperties.set(OppoEngineerService.USB_CHARGE_SWITCH_PROPERTY, "false");
                            }
                            OppoEngineerService oppoEngineerService = OppoEngineerService.this;
                            String str = OppoEngineerService.OPPO_MANAGER_CLASS_NAME;
                            String str2 = OppoEngineerService.WRITE_LOG_TO_PARTION_METHOD;
                            Class[] clsArr = new Class[5];
                            clsArr[0] = Integer.TYPE;
                            clsArr[1] = String.class;
                            clsArr[2] = String.class;
                            clsArr[3] = String.class;
                            clsArr[4] = String.class;
                            Object[] objArr = new Object[5];
                            objArr[0] = Integer.valueOf(102);
                            objArr[1] = "WriteProtectResetDone";
                            objArr[2] = "ANDROID";
                            objArr[3] = "WriteProtectIssue";
                            objArr[4] = "WriteProtectResetDone";
                            oppoEngineerService.invokeDeclaredMethod(null, str, str2, clsArr, objArr);
                            OppoEngineerService.this.mContext.unregisterReceiver(OppoEngineerService.this.mUpdateBroadcastReceiver);
                            return;
                        }
                        Slog.i(OppoEngineerService.TAG, "reset wp state fail");
                        OppoEngineerService.this.writeWpIssueToCriticalLog();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private final class OppoEngineerServiceWrapper extends Stub {
        /* synthetic */ OppoEngineerServiceWrapper(OppoEngineerService this$0, OppoEngineerServiceWrapper oppoEngineerServiceWrapper) {
            this();
        }

        private OppoEngineerServiceWrapper() {
        }

        public boolean getPartionProtectStatus() {
            return OppoEngineerService.this.getPartionProtectStatusLocked();
        }

        public boolean setPartionProtectEnable(boolean enable) {
            return OppoEngineerService.this.setPartionProtectEnableLocked(enable);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (Binder.getCallingUid() == 1000 || OppoEngineerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
                OppoEngineerService.this.dumpInternal(fd, pw, args);
            } else {
                pw.println("Permission Denial: can't dump engineer from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.engineer.OppoEngineerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.engineer.OppoEngineerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.engineer.OppoEngineerService.<clinit>():void");
    }

    public OppoEngineerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mUpdateBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Slog.d(OppoEngineerService.TAG, "onReceive intent = " + intent);
                if (intent != null) {
                    ArrayList<String> tmp = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                    if (tmp != null && tmp.contains("criticallog_config")) {
                        Slog.i(OppoEngineerService.TAG, "need reset wp state");
                        OppoEngineerService.this.mHandler.sendEmptyMessage(OppoEngineerService.HANDLE_ROM_UPDATE_MSG);
                    }
                }
            }
        };
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        this.mHandler = new OppoEngineerHandler(this.mServiceThread.getLooper());
    }

    public void onStart() {
        publishBinderService("engineer", new OppoEngineerServiceWrapper(this, null));
    }

    public void onBootPhase(int phase) {
        if (phase == 1000) {
            IntentFilter shutdownfilter = new IntentFilter();
            shutdownfilter.addAction("android.intent.action.ACTION_SHUTDOWN");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Slog.d(OppoEngineerService.TAG, "onReceive intent = " + intent);
                    if (!OppoEngineerService.ATM_MODEM_MODE_NORMAL.equals(SystemProperties.get(OppoEngineerService.ATM_MODEM_MODE_PROPERTY, OppoEngineerService.ATM_MODEM_MODE_NORMAL))) {
                        Slog.d(OppoEngineerService.TAG, "auto reset mdmode while shutdown");
                        SystemProperties.set(OppoEngineerService.ATM_MODEM_MODE_PROPERTY, OppoEngineerService.ATM_MODEM_MODE_NORMAL);
                    }
                }
            }, shutdownfilter);
            if (!getPartionProtectStatusLocked()) {
                Slog.i(TAG, "on PHASE_BOOT_COMPLETED");
                IntentFilter filter = new IntentFilter();
                filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
                this.mContext.registerReceiver(this.mUpdateBroadcastReceiver, filter);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        OppoEngineerService.this.addPartionProtectNotification(OppoEngineerService.this.mContext);
                        if (SystemProperties.getBoolean(OppoEngineerService.BUILD_RELEASE_TYPE_PROPERTY, false) && OppoEngineerService.this.isSecrecyEncryptState(OppoEngineerService.this.mContext, 4)) {
                            OppoEngineerService.this.writeWpIssueToCriticalLog();
                        }
                    }
                });
            }
            new Thread() {
                public void run() {
                    OppoEngineerService.this.updateOppoProductInfo();
                }
            }.start();
        }
    }

    private boolean isSecrecyEncryptState(Context context, int type) {
        if (!context.getPackageManager().hasSystemFeature(SECRECY_SUPPORT_FEATURE)) {
            return false;
        }
        Object secrecyManagerInternal = null;
        try {
            secrecyManagerInternal = -wrap1(Class.forName(SECRECY_INTERNAL_CLASS_NAME));
        } catch (ClassNotFoundException e) {
            Slog.i(TAG, "ClassNotFoundException found");
        }
        if (secrecyManagerInternal != null) {
            String str = SECRECY_INTERNAL_CLASS_NAME;
            String str2 = GET_SECRECY_STATE_METHOD;
            Class[] clsArr = new Class[1];
            clsArr[0] = Integer.TYPE;
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(type);
            Object result = invokeDeclaredMethod(secrecyManagerInternal, str, str2, clsArr, objArr);
            if (result != null) {
                return ((Boolean) result).booleanValue();
            }
            Slog.i(TAG, "result is null");
            return false;
        }
        Slog.i(TAG, "secrecyManagerInternal is null");
        return false;
    }

    private void writeWpIssueToCriticalLog() {
        Slog.i(TAG, "writeWpIssueToCriticalLog");
        String pcba = SystemProperties.get(PCBA_PROPERTY, PCBA_UNKNOWN_DEFAULT);
        StringBuilder criticalLogSb = new StringBuilder();
        criticalLogSb.append("[");
        criticalLogSb.append(pcba);
        criticalLogSb.append("]");
        criticalLogSb.append("[");
        criticalLogSb.append("WP OFF ISSUE");
        criticalLogSb.append("]");
        if (SystemProperties.getBoolean(USB_CHARGE_SWITCH_PROPERTY, false)) {
            criticalLogSb.append("[");
            criticalLogSb.append("USB Charge Disable");
            criticalLogSb.append("]");
        }
        if (!ATM_MODEM_MODE_NORMAL.equals(SystemProperties.get(ATM_MODEM_MODE_PROPERTY, ATM_MODEM_MODE_NORMAL))) {
            criticalLogSb.append("[");
            criticalLogSb.append("MODEM META MODE");
            criticalLogSb.append("]");
        }
        String criticalLog = criticalLogSb.toString();
        String str = OPPO_MANAGER_CLASS_NAME;
        String str2 = WRITE_LOG_TO_PARTION_METHOD;
        Class[] clsArr = new Class[5];
        clsArr[0] = Integer.TYPE;
        clsArr[1] = String.class;
        clsArr[2] = String.class;
        clsArr[3] = String.class;
        clsArr[4] = String.class;
        Object[] objArr = new Object[5];
        objArr[0] = Integer.valueOf(101);
        objArr[1] = criticalLog;
        objArr[2] = "ANDROID";
        objArr[3] = "WriteProtectIssue";
        objArr[4] = "WriteProtectNeedReset";
        invokeDeclaredMethod(null, str, str2, clsArr, objArr);
    }

    private boolean resetWriteProtectState() {
        if (getPartionProtectStatusLocked()) {
            return true;
        }
        if (!setPartionProtectEnableLocked(true) || !getPartionProtectStatusLocked()) {
            return false;
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                if (!OppoEngineerService.ATM_MODEM_MODE_NORMAL.equals(SystemProperties.get(OppoEngineerService.ATM_MODEM_MODE_PROPERTY, OppoEngineerService.ATM_MODEM_MODE_NORMAL))) {
                    Slog.i(OppoEngineerService.TAG, "current md mode is not normal, reset it to normal");
                    SystemProperties.set(OppoEngineerService.ATM_MODEM_MODE_PROPERTY, OppoEngineerService.ATM_MODEM_MODE_NORMAL);
                }
                if (!android.os.SystemService.isStopped(OppoEngineerService.META_TST_SERVICE)) {
                    android.os.SystemService.stop(OppoEngineerService.META_TST_SERVICE);
                }
                ((NotificationManager) OppoEngineerService.this.mContext.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME)).cancel(10000);
                OppoEngineerService.this.clearWpIssue();
            }
        });
        return true;
    }

    private void clearWpIssue() {
        String str = OPPO_MANAGER_CLASS_NAME;
        String str2 = CLEAR_ITEM_METHOD;
        Class[] clsArr = new Class[1];
        clsArr[0] = Integer.TYPE;
        Object[] objArr = new Object[1];
        objArr[0] = Integer.valueOf(101);
        invokeDeclaredMethod(null, str, str2, clsArr, objArr);
        str = OPPO_MANAGER_CLASS_NAME;
        str2 = CLEAR_ITEM_METHOD;
        clsArr = new Class[1];
        clsArr[0] = Integer.TYPE;
        objArr = new Object[1];
        objArr[0] = Integer.valueOf(1125);
        invokeDeclaredMethod(null, str, str2, clsArr, objArr);
        invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, SYNC_CACHE_TO_EMMC_METHOD, null, null);
    }

    private void updateOppoProductInfo() {
        Slog.i(TAG, "updateOppoProductInfo");
        String oppoProductInfoPath = "/data/nvram/APCFG/APRDEB/OPPO_PRODUCT_INFO";
        try {
            Object agent = getNvramAgentInstance();
            if (agent != null) {
                byte[] buff = null;
                try {
                    Class[] clsArr = new Class[1];
                    clsArr[0] = String.class;
                    Object[] objArr = new Object[1];
                    objArr[0] = oppoProductInfoPath;
                    Object data = invokeDeclaredMethod(agent, "com.android.server.NvRAMAgent", "readFileByName", clsArr, objArr);
                    if (data != null) {
                        buff = (byte[]) data;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (buff != null) {
                    if (buff.length >= 16) {
                        int noLength = 0;
                        for (int i = 0; i < 16; i++) {
                            if (buff[i] == (byte) 0) {
                                noLength = i;
                                break;
                            }
                            if (i == 15) {
                                noLength = 16;
                            }
                        }
                        if (noLength > 0) {
                            String factoryNumber = new String(buff, 0, noLength);
                            Slog.i(TAG, "updateOppoProductInfo factoryNumber:" + factoryNumber);
                            SystemProperties.set("oppo.eng.factory.no", factoryNumber);
                            return;
                        }
                        return;
                    }
                }
                Slog.d(TAG, "OPPO_PRODUCT_INFO empty");
                return;
            }
            Slog.d(TAG, "NvRAMAgent invalid");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public Object getNvramAgentInstance() throws ClassNotFoundException {
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = String.class;
            Method checkService = Class.forName("android.os.ServiceManager").getMethod("checkService", clsArr);
            Object[] objArr = new Object[1];
            objArr[0] = "NvRAMAgent";
            IBinder b = (IBinder) checkService.invoke(null, objArr);
            if (b == null) {
                return null;
            }
            Class[] clsArr2 = new Class[1];
            clsArr2[0] = IBinder.class;
            Method m = Class.forName("com.android.server.NvRAMAgent$Stub").getMethod("asInterface", clsArr2);
            Slog.v(TAG, "getMethod as interface");
            objArr = new Object[1];
            objArr[0] = b;
            return m.invoke(null, objArr);
        } catch (ClassNotFoundException e) {
            Slog.w(TAG, "getNvramAgentInstance failed ClassNotFoundException");
            return null;
        } catch (NoSuchMethodException e2) {
            Slog.w(TAG, "Getmethod failed NoSuchMethodException");
            return null;
        } catch (InvocationTargetException e3) {
            Slog.w(TAG, "Getmethod failed InvocationTargetException");
            return null;
        } catch (IllegalAccessException e4) {
            Slog.w(TAG, "Getmethod failed IllegalAccessException");
            return null;
        } catch (Exception e5) {
            Slog.w(TAG, "Getmethod failed Exception");
            return null;
        }
    }

    private Object invokeDeclaredMethod(Object target, String cls_name, String method_name, Class[] parameterTypes, Object[] args) {
        Slog.i(TAG, target + " invokeDeclaredMethod : " + cls_name + "." + method_name);
        Object result = null;
        try {
            Method method = Class.forName(cls_name).getDeclaredMethod(method_name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "ClassNotFoundException : " + e.getMessage());
            return result;
        } catch (NoSuchMethodException e2) {
            Log.i(TAG, "NoSuchMethodException : " + e2.getMessage());
            return result;
        } catch (IllegalAccessException e3) {
            Log.i(TAG, "IllegalAccessException : " + e3.getMessage());
            return result;
        } catch (InvocationTargetException e4) {
            Log.i(TAG, "InvocationTargetException : " + e4.getMessage());
            return result;
        } catch (SecurityException e5) {
            Log.i(TAG, "SecurityException : " + e5.getMessage());
            return result;
        } catch (Exception e6) {
            Log.e(TAG, "Exception : " + e6.getMessage());
            return result;
        }
    }

    private String getDataFromProvider() {
        Cursor cursor = null;
        String returnStr = null;
        String[] projection = new String[2];
        projection[0] = "version";
        projection[1] = COLUMN_NAME_2;
        try {
            if (this.mContext == null) {
                return null;
            }
            cursor = this.mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, projection, "filtername=\"criticallog_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int configVersion = cursor.getInt(versioncolumnIndex);
                returnStr = cursor.getString(xmlcolumnIndex);
                Slog.d(TAG, "White List updated, version = " + configVersion);
            }
            if (cursor != null) {
                cursor.close();
            }
            return returnStr;
        } catch (Exception e) {
            Slog.w(TAG, "We can not get white list data from provider, because of " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-h".equals(opt)) {
                pw.println("engineer service dump options:");
                return;
            } else if (!"-wp".equals(opt)) {
                pw.println("Unknown argument: " + opt + "; use -h for help");
                return;
            } else if (opti < args.length && "on".equals(args[opti])) {
                if (resetWriteProtectState()) {
                    pw.println("OK:enable wp success");
                } else {
                    pw.println("FAIL:enable wp fail");
                }
                return;
            } else if (opti != args.length) {
                pw.println("Unknown argument");
                return;
            } else if (getPartionProtectStatusLocked()) {
                pw.println("WP ON");
            } else {
                pw.println("WP OFF");
            }
        }
    }

    private int encrypt(int plainText, int key) {
        return plainText ^ key;
    }

    private int decrypt(int cipherText, int key) {
        return cipherText ^ key;
    }

    /* JADX WARNING: Missing block: B:31:0x009d, code:
            return r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getPartionProtectStatusLocked() {
        synchronized (this.mLock) {
            byte[] content;
            RandomAccessFile randomAccessFile;
            try {
                content = new byte[8];
                int keyInt = -1;
                if (new File(PARTION_PROTECT_FLAG_PATH).exists()) {
                    randomAccessFile = new RandomAccessFile(PARTION_PROTECT_FLAG_PATH, "r");
                    if (randomAccessFile != null) {
                        try {
                            Os.lseek(randomAccessFile.getFD(), 15730176, OsConstants.SEEK_SET);
                            Slog.d(TAG, "getPartionProtectStatusInternal readBytes count = " + randomAccessFile.read(content, 0, 8));
                            try {
                                randomAccessFile.close();
                            } catch (IOException e) {
                                Slog.d(TAG, "getPartionProtectStatusInternal IOException while close : " + e.getMessage());
                            }
                        } catch (IOException e2) {
                            Slog.d(TAG, "getPartionProtectStatusInternal IOException" + e2.getMessage());
                            content = null;
                            try {
                                randomAccessFile.close();
                            } catch (IOException e22) {
                                Slog.d(TAG, "getPartionProtectStatusInternal IOException while close : " + e22.getMessage());
                            }
                        } catch (ErrnoException e3) {
                            Slog.d(TAG, "getPartionProtectStatusInternal ErrnoException" + e3.getMessage());
                            content = null;
                            try {
                                randomAccessFile.close();
                            } catch (IOException e222) {
                                Slog.d(TAG, "getPartionProtectStatusInternal IOException while close : " + e222.getMessage());
                            }
                        } catch (Throwable th) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e2222) {
                                Slog.d(TAG, "getPartionProtectStatusInternal IOException while close : " + e2222.getMessage());
                            }
                            throw th;
                        }
                    }
                    if (content != null) {
                        if (content.length > 0) {
                            String keyString = new String(content);
                            Slog.i(TAG, "getPartionProtectStatusInternal : " + keyString);
                            if (!(keyString == null || keyString.isEmpty())) {
                                try {
                                    keyInt = Integer.parseInt(keyString);
                                } catch (NumberFormatException e4) {
                                    Slog.e(TAG, "getPartionProtectStatusInternal NumberFormatException:" + e4.getMessage());
                                }
                            }
                        }
                    }
                    boolean z;
                    if (decrypt(keyInt, KEY2) - KEY1 != 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                } else {
                    Slog.i(TAG, "getPartionProtectStatusInternal file not exists : /dev/block/platform/bootdevice/by-name/reserve4");
                    return true;
                }
            } catch (FileNotFoundException e5) {
                Slog.d(TAG, "getPartionProtectStatusInternal FileNotFoundException" + e5.getMessage());
                randomAccessFile = null;
                content = null;
            } catch (IllegalArgumentException e6) {
                Slog.d(TAG, "getPartionProtectStatusInternal IllegalArgumentException" + e6.getMessage());
                randomAccessFile = null;
                content = null;
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    private boolean setPartionProtectEnableLocked(boolean enable) {
        boolean ret;
        synchronized (this.mLock) {
            int keyInt = -1;
            if (!enable) {
                keyInt = 31232700;
            }
            RandomAccessFile randomAccessFile;
            try {
                String content = new Integer(keyInt).toString();
                ret = false;
                if (new File(PARTION_PROTECT_FLAG_PATH).exists()) {
                    randomAccessFile = new RandomAccessFile(PARTION_PROTECT_FLAG_PATH, "rws");
                    if (randomAccessFile != null) {
                        try {
                            Os.lseek(randomAccessFile.getFD(), 15730176, OsConstants.SEEK_SET);
                            randomAccessFile.write(content.getBytes(StandardCharsets.UTF_8));
                            ret = true;
                            try {
                                randomAccessFile.close();
                            } catch (IOException e) {
                                Log.d(TAG, "setPartionProtectEnableLocked IOException while close : " + e.getMessage());
                                ret = false;
                                return ret;
                            }
                        } catch (IOException e2) {
                            Log.d(TAG, "setPartionProtectEnableLocked IOException" + e2.getMessage());
                            ret = false;
                            try {
                                randomAccessFile.close();
                            } catch (IOException e22) {
                                Log.d(TAG, "setPartionProtectEnableLocked IOException while close : " + e22.getMessage());
                                ret = false;
                                return ret;
                            }
                            return ret;
                        } catch (ErrnoException e3) {
                            Log.d(TAG, "setPartionProtectEnableLocked ErrnoException" + e3.getMessage());
                            ret = false;
                            try {
                                randomAccessFile.close();
                            } catch (IOException e222) {
                                Log.d(TAG, "setPartionProtectEnableLocked IOException while close : " + e222.getMessage());
                                ret = false;
                                return ret;
                            }
                            return ret;
                        } catch (Throwable th) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e2222) {
                                Log.d(TAG, "setPartionProtectEnableLocked IOException while close : " + e2222.getMessage());
                            }
                            throw th;
                        }
                    }
                }
                Log.i(TAG, "setPartionProtectEnableLocked file not exists : /dev/block/platform/bootdevice/by-name/reserve4");
                return false;
            } catch (FileNotFoundException e4) {
                Log.d(TAG, "setPartionProtectEnableLocked FileNotFoundException" + e4.getMessage());
                randomAccessFile = null;
                ret = false;
            } catch (IllegalArgumentException e5) {
                Log.d(TAG, "setPartionProtectEnableLocked IllegalArgumentException" + e5.getMessage());
                randomAccessFile = null;
                ret = false;
            } catch (Throwable th2) {
                throw th2;
            }
        }
    }

    private void addPartionProtectNotification(Context context) {
        Slog.i(TAG, "addPartionProtectNotification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManagerService.NOTIFICATON_TITLE_NAME);
        Builder builder = new Builder(context);
        builder.setSmallIcon(17301651);
        builder.setAutoCancel(false);
        builder.setContentTitle(this.mContext.getString(17041018));
        builder.setContentText(this.mContext.getString(17041019));
        builder.setShowWhen(true);
        builder.setWhen(System.currentTimeMillis());
        Notification status = builder.build();
        status.flags = 34;
        status.priority = 2;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.wireless.NormalModeWarningPage"));
        intent.addFlags(268435456);
        status.contentIntent = PendingIntent.getActivityAsUser(context, 0, intent, 0, null, UserHandle.CURRENT);
        notificationManager.notify(10000, status);
    }
}

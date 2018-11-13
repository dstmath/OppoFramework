package com.android.server.policy;

import android.os.OppoManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.AgingCriticalEvent;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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
public class HwShutdownRecord {
    private static final File ENG_RESULT_FILE = null;
    private static final String ENG_RESULT_FILE_PATH = "/persist/engineermode/ENG_RESULT";
    public static final int ENG_RESULT_LENGTH = 128;
    private static final File ENG_RESULT_ROOT_FILE = null;
    private static final String ENG_RESULT_ROOT_PATH = "/persist/engineermode/";
    private static final int HARDWARE_RESET_RECORD_FLAG_INDEX = 77;
    private static final int MAX_BLOCK_BYTE = 512;
    private static final int SHUTDOWN_COUNT_BYTE = 16;
    private static final int SHUTDOWN_EACH_TIME_STR_BYTE = 14;
    private static final int SHUTDOWN_TIME_STR_LEN_BYTE = 4;
    private static final String STR_FORMAT = "%-16d%03d\n%s";
    private static final String STR_FORMAT_HEAD = "%-16d%03d\n";
    private static final String TAG = "HwShutdownRecord";
    private static HwShutdownRecord instance;
    private byte mHwResetCount;
    private boolean mHwShutdown;
    private int mHwShutdownCount;
    private String mHwShutdownTimeStr;
    private int mHwShutdownTimeStrLen;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.HwShutdownRecord.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.HwShutdownRecord.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.HwShutdownRecord.<clinit>():void");
    }

    public HwShutdownRecord() {
        this.mHwShutdownCount = -1;
        this.mHwShutdownTimeStrLen = 0;
        this.mHwShutdown = false;
        this.mHwResetCount = (byte) -1;
    }

    public static HwShutdownRecord getInstance() {
        if (instance == null) {
            instance = new HwShutdownRecord();
        }
        return instance;
    }

    public void recordHwShutdownFlag() {
        if (!this.mHwShutdown) {
            this.mHwShutdown = true;
            Log.i(TAG, "recordHwShutdownFlag");
            SystemProperties.set("persist.sys.oppo.longpwk", "yes");
            OppoManager.syncCacheToEmmc();
            LoadHwShutdownCountIfNeed();
            storeHwShutdownCount(false);
            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_POWERKEY_LONG_PRESSED, new String[0]);
            this.mHwResetCount = getProductLineTestFlagExtraByte(77);
            if (this.mHwResetCount < Byte.MIN_VALUE && this.mHwResetCount >= (byte) 0) {
                setProductLineTestFlagExtraByte(77, (byte) (this.mHwResetCount + 1));
            }
        }
    }

    public void cancelHwShutdownFlag() {
        if (this.mHwShutdown) {
            this.mHwShutdown = false;
            Log.i(TAG, "cancelHwShutdownFlag");
            SystemProperties.set("persist.sys.oppo.longpwk", IElsaManager.EMPTY_PACKAGE);
            storeHwShutdownCount(true);
            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_POWERKEY_LONGPRESSED_RELEASE, new String[0]);
            if (this.mHwResetCount >= (byte) 0) {
                setProductLineTestFlagExtraByte(77, this.mHwResetCount);
            }
        }
    }

    private int strToInt(String str, int startIndex, int endIndex) {
        String strSub = str.substring(startIndex, endIndex);
        if (strSub == null) {
            return 0;
        }
        strSub = strSub.trim();
        if (strSub == null || strSub.length() == 0) {
            return 0;
        }
        return Integer.parseInt(strSub);
    }

    private void LoadHwShutdownCountIfNeed() {
        try {
            String strData = OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, 16);
            this.mHwShutdownCount = strToInt(strData, 0, strData.length());
            try {
                this.mHwShutdownTimeStrLen = strToInt(OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, 20), 16, 19);
                this.mHwShutdownTimeStr = IElsaManager.EMPTY_PACKAGE;
                if (this.mHwShutdownTimeStrLen > 0) {
                    this.mHwShutdownTimeStr = OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, (this.mHwShutdownTimeStrLen + 16) + 4).substring(20);
                }
            } catch (Exception e) {
                Log.w(TAG, "LoadHwShutdownCountIfNeed read time region Exception,set default hardware shutdown time len to 0!");
                initHwShutdownRegion(this.mHwShutdownCount);
                this.mHwShutdownTimeStrLen = 0;
                this.mHwShutdownTimeStr = IElsaManager.EMPTY_PACKAGE;
            }
        } catch (Exception e2) {
            Log.w(TAG, "LoadHwShutdownCountIfNeed read mHwShutdownCount Exception,set default hardware shutdown count to 0!");
            initHwShutdownRegion(0);
            this.mHwShutdownCount = 0;
            this.mHwShutdownTimeStrLen = 0;
            this.mHwShutdownTimeStr = IElsaManager.EMPTY_PACKAGE;
        }
    }

    private void initHwShutdownRegion(int count) {
        String str = STR_FORMAT_HEAD;
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(count);
        objArr[1] = Integer.valueOf(0);
        OppoManager.writeCriticalData(OppoManager.TYPE_HW_SHUTDOWN, String.format(str, objArr));
    }

    private void storeHwShutdownCount(boolean cancelFlag) {
        int hwShutdownCount = this.mHwShutdownCount;
        int hwShutdownTimeStrLen = this.mHwShutdownTimeStrLen;
        String hwShutdownTimeStr = this.mHwShutdownTimeStr;
        if (!cancelFlag) {
            hwShutdownCount++;
            if (hwShutdownTimeStrLen + 14 <= 512) {
                hwShutdownTimeStrLen += 14;
                hwShutdownTimeStr = hwShutdownTimeStr + String.valueOf(System.currentTimeMillis()) + "\n";
            } else {
                hwShutdownTimeStr = (hwShutdownTimeStr + String.valueOf(System.currentTimeMillis()) + "\n").substring(14);
            }
        }
        OppoManager.cleanItem(OppoManager.TYPE_HW_SHUTDOWN);
        String str = STR_FORMAT;
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(hwShutdownCount);
        objArr[1] = Integer.valueOf(hwShutdownTimeStrLen);
        objArr[2] = hwShutdownTimeStr;
        OppoManager.writeCriticalData(OppoManager.TYPE_HW_SHUTDOWN, String.format(str, objArr));
    }

    private boolean setProductLineTestFlagExtraByte(int index, byte value) {
        boolean ret = false;
        byte[] data = null;
        Log.i(TAG, "setProductLineTestFlagExtraByte index = " + index + ", value = " + value);
        if (index < 0 || index >= 128) {
            Log.e(TAG, "index is invalid!");
            return false;
        } else if (ENG_RESULT_ROOT_FILE.exists() && ENG_RESULT_ROOT_FILE.isDirectory()) {
            RandomAccessFile randomAccessFile;
            if (!ENG_RESULT_FILE.exists()) {
                data = new byte[128];
                data[77] = value;
            }
            try {
                randomAccessFile = new RandomAccessFile(ENG_RESULT_FILE_PATH, "rws");
            } catch (FileNotFoundException e) {
                Log.d(TAG, "setProductLineTestFlagExtraByte FileNotFoundException" + e.getMessage());
                randomAccessFile = null;
                ret = false;
            } catch (IllegalArgumentException e2) {
                Log.d(TAG, "setProductLineTestFlagExtraByte IllegalArgumentException" + e2.getMessage());
                randomAccessFile = null;
                ret = false;
            }
            if (randomAccessFile != null) {
                if (data != null) {
                    try {
                        randomAccessFile.write(data);
                        if (ENG_RESULT_FILE.exists() && !ENG_RESULT_FILE.setReadable(true, false)) {
                            Log.e(TAG, "setReadable " + ENG_RESULT_FILE.getPath() + " failed!");
                        }
                    } catch (IOException e3) {
                        Log.d(TAG, "setProductLineTestFlagExtraByte IOException" + e3.getMessage());
                        ret = false;
                        return ret;
                    } finally {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e32) {
                            Log.d(TAG, "setProductLineTestFlagExtraByte IOException while close : " + e32.getMessage());
                            ret = false;
                        }
                    }
                } else {
                    randomAccessFile.seek((long) index);
                    randomAccessFile.writeByte(value);
                }
                ret = true;
                try {
                    randomAccessFile.close();
                } catch (IOException e322) {
                    Log.d(TAG, "setProductLineTestFlagExtraByte IOException while close : " + e322.getMessage());
                    ret = false;
                }
            }
            return ret;
        } else {
            Log.e(TAG, ENG_RESULT_ROOT_FILE + "is invalid!");
            return false;
        }
    }

    private byte getProductLineTestFlagExtraByte(int index) {
        byte result = (byte) 0;
        if (ENG_RESULT_ROOT_FILE.exists() && ENG_RESULT_ROOT_FILE.isDirectory()) {
            RandomAccessFile randomAccessFile;
            try {
                randomAccessFile = new RandomAccessFile(ENG_RESULT_FILE_PATH, "r");
            } catch (FileNotFoundException e) {
                Log.d(TAG, "getProductLineTestFlag FileNotFoundException" + e.getMessage());
                randomAccessFile = null;
            } catch (IllegalArgumentException e2) {
                Log.d(TAG, "getProductLineTestFlag IllegalArgumentException" + e2.getMessage());
                randomAccessFile = null;
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.seek((long) index);
                    result = randomAccessFile.readByte();
                    try {
                        randomAccessFile.close();
                    } catch (IOException e3) {
                        Log.d(TAG, "getProductLineTestFlag IOException while close : " + e3.getMessage());
                    }
                } catch (IOException e32) {
                    Log.d(TAG, "getProductLineTestFlag IOException" + e32.getMessage());
                    result = (byte) 0;
                    try {
                        randomAccessFile.close();
                    } catch (IOException e322) {
                        Log.d(TAG, "getProductLineTestFlag IOException while close : " + e322.getMessage());
                    }
                } catch (Throwable th) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e3222) {
                        Log.d(TAG, "getProductLineTestFlag IOException while close : " + e3222.getMessage());
                    }
                    throw th;
                }
            }
            Log.i(TAG, "getProductLineTestFlag result = " + result);
            return result;
        }
        Log.e(TAG, ENG_RESULT_ROOT_FILE + "is invalid!");
        return (byte) 0;
    }
}

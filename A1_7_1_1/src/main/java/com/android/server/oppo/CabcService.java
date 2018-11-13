package com.android.server.oppo;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.cabc.ICabcManager.Stub;
import com.android.server.LocationManagerService;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
public class CabcService extends Stub {
    private static boolean DEBUG = false;
    private static final String PROP_LOG_CABC = "persist.sys.assert.panic";
    private static final String TAG = "CabcService";
    private static final String cabc_node = "/sys/devices/virtual/mtk_disp_mgr/mtk_disp_mgr/LCM_CABC";
    private Context mContext;
    private int mInitModeFromDriver;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.CabcService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.oppo.CabcService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.oppo.CabcService.<clinit>():void");
    }

    public CabcService(Context context) {
        this.mContext = context;
        DEBUG = SystemProperties.getBoolean(PROP_LOG_CABC, false);
        this.mInitModeFromDriver = getMode();
    }

    public void setMode(int mode) {
        if (DEBUG) {
            Slog.d(TAG, "setMode mode = " + mode);
        }
        String modeStr = getModeStr(mode);
        if (modeStr == null) {
            if (DEBUG) {
                Slog.d(TAG, "setMode mode = " + mode + " failed! illegal param.");
            }
            return;
        }
        if (writeCabcNode(modeStr) && DEBUG) {
            Slog.d(TAG, "setMode mode = " + mode + " successful!");
        }
    }

    public int getMode() {
        return parseMode(getCurrentCabcMode());
    }

    private String getModeStr(int mode) {
        switch (mode) {
            case 0:
            case 1:
            case 2:
            case 3:
                return String.valueOf(mode);
            default:
                return null;
        }
    }

    public void closeCabc() {
        if (DEBUG) {
            Slog.d(TAG, "closeCabc.");
        }
        if (parseMode(getCurrentCabcMode()) != 0) {
            writeCabcNode(getModeStr(0));
        }
    }

    public void openCabc() {
        if (DEBUG) {
            Slog.d(TAG, "openCabc, mInitModeFromDriver is:" + this.mInitModeFromDriver);
        }
        writeCabcNode(getModeStr(this.mInitModeFromDriver));
    }

    private boolean writeCabcNode(String value) {
        IOException e;
        if (DEBUG) {
            Slog.d(TAG, "writeCabcNode, new mode is:" + value);
        }
        if (value == null || value.length() <= 0) {
            Slog.w(TAG, "writeCabcNode:mode unavailable!");
            return false;
        }
        try {
            FileWriter fr = new FileWriter(new File(cabc_node));
            try {
                fr.write(value);
                fr.close();
                if (DEBUG) {
                    Slog.d(TAG, "write cabc node succeed!");
                }
                return true;
            } catch (IOException e2) {
                e = e2;
                e.printStackTrace();
                Slog.e(TAG, "write cabc node failed!");
                return false;
            }
        } catch (IOException e3) {
            e = e3;
            e.printStackTrace();
            Slog.e(TAG, "write cabc node failed!");
            return false;
        }
    }

    private String getCurrentCabcMode() {
        File cabcFile = new File(cabc_node);
        char[] a = new char[10];
        String result = IElsaManager.EMPTY_PACKAGE;
        try {
            FileReader fr = new FileReader(cabcFile);
            fr.read(a);
            result = new String(a).trim();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
            Slog.e(TAG, "read cabc node failed!");
        }
        if (DEBUG) {
            Slog.d(TAG, "getCurrentCabcMode:" + result);
        }
        return result;
    }

    private int parseMode(String mode) {
        if (DEBUG) {
            Slog.d(TAG, "parseMode mode:" + mode);
        }
        if ("0".equalsIgnoreCase(mode)) {
            return 0;
        }
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equalsIgnoreCase(mode)) {
            return 1;
        }
        if ("2".equalsIgnoreCase(mode)) {
            return 2;
        }
        if ("3".equalsIgnoreCase(mode)) {
            return 3;
        }
        return 0;
    }
}

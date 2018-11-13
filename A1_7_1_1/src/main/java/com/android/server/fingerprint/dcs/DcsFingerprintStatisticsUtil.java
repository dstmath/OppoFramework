package com.android.server.fingerprint.dcs;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import oppo.util.OppoStatistics;

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
public class DcsFingerprintStatisticsUtil {
    public static final String EVENT_DOUBLE_HOME = "double_home_screen_on";
    public static final String EVENT_FINGER_ERROR = "fingerprint_identify_fail";
    public static final String EVENT_FINGER_OK = "fingerprint_identify_sucess";
    public static final String EVENT_MOVE_FAST = "fingerprint_move_fast";
    public static final String EVENT_SYNC_TEMPLATE = "sync_template_times";
    public static final String KEY_ACQUIRED_INFO = "acquireInfo";
    public static final String KEY_FINGER_ID = "fingerID";
    public static final String KEY_IMAGE_QUALITY = "imageInfo_quality";
    public static final String KEY_IMAGE_SCORE = "imageInfo_score";
    public static final String KEY_IMAGE_TYPE = "imageInfo_type";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final int MSG_ACQUIRED_INFO = 3;
    public static final int MSG_CLEAR_MAP = 5;
    public static final int MSG_DOUBLE_HOME = 4;
    public static final int MSG_FINGER_ID = 1;
    public static final int MSG_IMAGE_INFO = 2;
    public static final int MSG_SYNC_TEMPLATE = 6;
    public static final String SYSTEM_APP_TAG = "20120";
    private static Object mMutex;
    private static DcsFingerprintStatisticsUtil mSingleInstance;
    private final String TAG;
    private Context mContext;
    public Map<String, String> mFingerprintStatisticsMap;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private Looper mLooper;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil.<clinit>():void");
    }

    public DcsFingerprintStatisticsUtil(Context context) {
        this.TAG = "FingerprintService.DcsUtil";
        this.mFingerprintStatisticsMap = new ConcurrentHashMap();
        LogUtil.d("FingerprintService.DcsUtil", "DcsFingerprintStatisticsUtil construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("DcsFingerprintStatistics thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FingerprintService.DcsUtil", "mLooper null");
        }
        initHandler();
    }

    public static DcsFingerprintStatisticsUtil getDcsFingerprintStatisticsUtil(Context mContext) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new DcsFingerprintStatisticsUtil(mContext);
            }
        }
        return mSingleInstance;
    }

    public void stopDcs() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        DcsFingerprintStatisticsUtil.this.handleFingerId(msg);
                        break;
                    case 2:
                        DcsFingerprintStatisticsUtil.this.handleImageInfo(msg);
                        break;
                    case 3:
                        DcsFingerprintStatisticsUtil.this.handleAcquiredInfo(msg);
                        break;
                    case 4:
                        DcsFingerprintStatisticsUtil.this.handleDoubleHomeTimes(msg);
                        break;
                    case 5:
                        DcsFingerprintStatisticsUtil.this.handleClearMap();
                        break;
                    case 6:
                        DcsFingerprintStatisticsUtil.this.handleSyncTemplateTimes(msg);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void handleClearMap() {
        this.mFingerprintStatisticsMap.clear();
    }

    private void handleFingerId(Message msg) {
        this.mFingerprintStatisticsMap.put(KEY_FINGER_ID, Integer.toString(msg.arg1));
        if (msg.arg1 == 0) {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_FINGER_ERROR);
        } else {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_FINGER_OK);
        }
    }

    private void handleAcquiredInfo(Message msg) {
        this.mFingerprintStatisticsMap.put("packageName", (String) msg.obj);
        this.mFingerprintStatisticsMap.put(KEY_ACQUIRED_INFO, Integer.toString(msg.arg1));
        if (msg.arg1 == 5) {
            uploadDataToDcs(this.mFingerprintStatisticsMap, EVENT_MOVE_FAST);
        }
    }

    private void handleImageInfo(Message msg) {
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_TYPE, Integer.toString(msg.arg1));
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_QUALITY, Integer.toString(msg.arg2));
        this.mFingerprintStatisticsMap.put(KEY_IMAGE_SCORE, Integer.toString(((Integer) msg.obj).intValue()));
    }

    private void handleDoubleHomeTimes(Message msg) {
        uploadDataToDcs(null, EVENT_DOUBLE_HOME);
    }

    private void handleSyncTemplateTimes(Message msg) {
        uploadDataToDcs(null, EVENT_SYNC_TEMPLATE);
    }

    public void sendFingerId(int fingerId, String pkgName) {
        this.mHandler.obtainMessage(1, fingerId, 0, null).sendToTarget();
    }

    public void sendAcquiredInfo(int acquiredInfo, String pkgName) {
        this.mHandler.obtainMessage(3, acquiredInfo, 0, pkgName).sendToTarget();
    }

    public void sendImageInfo(int type, int quality, int match_score) {
        this.mHandler.obtainMessage(2, type, quality, Integer.valueOf(match_score)).sendToTarget();
    }

    public void sendDoubleHomeTimes() {
        this.mHandler.obtainMessage(4, 0, 0, null).sendToTarget();
    }

    public void sendSyncTemplateTimes() {
        this.mHandler.obtainMessage(6, 0, 0, null).sendToTarget();
    }

    public void clearMap() {
        this.mHandler.obtainMessage(5, 0, 0, null).sendToTarget();
    }

    private void uploadDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, SYSTEM_APP_TAG, eventId, data, false);
        handleClearMap();
    }
}

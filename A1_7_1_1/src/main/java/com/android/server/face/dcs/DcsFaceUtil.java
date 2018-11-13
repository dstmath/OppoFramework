package com.android.server.face.dcs;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
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
public class DcsFaceUtil {
    public static final String EVENT_FACE_FAIL = "face_identify_fail";
    public static final String EVENT_FACE_SUCESS = "face_identify_sucess";
    public static final String EVENT_HEALTH_TIMEOUT = "face_health_timeout";
    public static final String FACE_LOG_TAG = "facesystem";
    public static final String KEY_ACQUIRED_INFO = "acquireinfo";
    public static final String KEY_FACE_ID = "faceID";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final String KEY_TIMEOUT_FUNCTION = "functionName";
    public static final int MSG_ACQUIRED_INFO = 2;
    public static final int MSG_CLEAR_MAP = 3;
    public static final int MSG_FACE_ID = 1;
    public static final int MSG_HEALTH_TIMEOUT = 4;
    private static Object mMutex;
    private static DcsFaceUtil mSingleInstance;
    private final String TAG;
    private Context mContext;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private Looper mLooper;
    public Map<String, String> mStatisticsMap;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.dcs.DcsFaceUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.dcs.DcsFaceUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.dcs.DcsFaceUtil.<clinit>():void");
    }

    public DcsFaceUtil(Context context) {
        this.TAG = "FaceService.DcsUtil";
        this.mStatisticsMap = new ConcurrentHashMap();
        LogUtil.d("FaceService.DcsUtil", "DcsFaceUtil construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("DcsFaceUtil thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FaceService.DcsUtil", "DcsFaceUtil handler mLooper null");
        }
        initHandler();
    }

    public static DcsFaceUtil getDcsFaceUtil(Context mContext) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new DcsFaceUtil(mContext);
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
                        DcsFaceUtil.this.handleFaceId(msg);
                        break;
                    case 2:
                        DcsFaceUtil.this.handleAcquiredInfo(msg);
                        break;
                    case 3:
                        DcsFaceUtil.this.handleClearMap();
                        break;
                    case 4:
                        DcsFaceUtil.this.handleHealthTimeout(msg);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void sendFaceId(int faceId, String pkgName) {
        this.mHandler.obtainMessage(1, faceId, 0, null).sendToTarget();
    }

    private void handleFaceId(Message msg) {
        this.mStatisticsMap.put(KEY_FACE_ID, Integer.toString(msg.arg1));
        if (msg.arg1 == 0) {
            uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_FAIL);
        } else {
            uploadDataToDcs(this.mStatisticsMap, EVENT_FACE_SUCESS);
        }
    }

    public void sendAcquireInfo(int acquiredInfo, String pkgName) {
        this.mHandler.obtainMessage(2, acquiredInfo, 0, pkgName).sendToTarget();
    }

    private void handleAcquiredInfo(Message msg) {
    }

    public void clearMap() {
        this.mHandler.obtainMessage(3, 0, 0, null).sendToTarget();
    }

    private void handleClearMap() {
        this.mStatisticsMap.clear();
    }

    public void sendHealthTimeout(String funcName) {
        if (funcName != null) {
            this.mHandler.obtainMessage(4, 0, 0, funcName).sendToTarget();
        }
    }

    private void handleHealthTimeout(Message msg) {
        this.mStatisticsMap.put(KEY_TIMEOUT_FUNCTION, (String) msg.obj);
        uploadDataToDcs(this.mStatisticsMap, EVENT_HEALTH_TIMEOUT);
    }

    private void uploadDataToDcs(Map<String, String> data, String eventId) {
        OppoStatistics.onCommon(this.mContext, FACE_LOG_TAG, eventId, data, false);
        handleClearMap();
    }
}

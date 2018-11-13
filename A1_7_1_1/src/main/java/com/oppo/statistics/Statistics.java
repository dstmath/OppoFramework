package com.oppo.statistics;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.json.JSONObject;

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
public class Statistics {
    private static final String APP_ID = "appId";
    private static final String APP_NAME = "appName";
    private static final String APP_PACKAGE = "appPackage";
    private static final String APP_VERSION = "appVersion";
    private static final int COMMON = 1006;
    private static final String DATA_TYPE = "dataType";
    private static final String EVENT_ID = "eventID";
    private static final String LOG_MAP = "logMap";
    private static final String LOG_TAG = "logTag";
    private static final String SSOID = "ssoid";
    private static final String UPLOAD_NOW = "uploadNow";
    private static int appId;
    private static ExecutorService sSingleThreadExecutor;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.statistics.Statistics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.statistics.Statistics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.statistics.Statistics.<clinit>():void");
    }

    public static void onCommon(Context context, String logTag, String eventId, Map<String, String> logMap, boolean upLoadNow) {
        try {
            Log.d("Statistics", "onCommonWithoutJar logTag is " + logTag + ",logmap:" + logMap);
            if (logTag != null && !TextUtils.isEmpty(logTag)) {
                final Map<String, String> map = logMap;
                final String str = eventId;
                final boolean z = upLoadNow;
                final String str2 = logTag;
                final Context context2 = context;
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        if (!(map == null || map.isEmpty())) {
                            try {
                                for (String key : map.keySet()) {
                                    jsonObject.put(key, map.get(key));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
                        intent.putExtra(Statistics.APP_PACKAGE, "system");
                        intent.putExtra(Statistics.APP_NAME, "system");
                        intent.putExtra(Statistics.APP_VERSION, "system");
                        intent.putExtra("ssoid", "system");
                        intent.putExtra(Statistics.APP_ID, Statistics.appId);
                        intent.putExtra(Statistics.EVENT_ID, str);
                        intent.putExtra(Statistics.UPLOAD_NOW, z);
                        intent.putExtra(Statistics.LOG_TAG, str2);
                        intent.putExtra(Statistics.LOG_MAP, jsonObject.toString());
                        intent.putExtra(Statistics.DATA_TYPE, 1006);
                        context2.startService(intent);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

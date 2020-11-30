package com.oppo.statistics;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

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
    private static int appId = 20120;
    private static ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void onCommon(final Context context, final String logTag, final String eventId, final Map<String, String> logMap, final boolean upLoadNow) {
        try {
            Log.d("Statistics", "onCommonWithoutJar logTag is " + logTag + ",logmap:" + logMap);
            if (logTag != null && !TextUtils.isEmpty(logTag)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.Statistics.AnonymousClass1 */

                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        Map map = logMap;
                        if (map != null && !map.isEmpty()) {
                            try {
                                for (String key : logMap.keySet()) {
                                    jsonObject.put(key, logMap.get(key));
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
                        intent.putExtra(Statistics.EVENT_ID, eventId);
                        intent.putExtra(Statistics.UPLOAD_NOW, upLoadNow);
                        intent.putExtra(Statistics.LOG_TAG, logTag);
                        intent.putExtra(Statistics.LOG_MAP, jsonObject.toString());
                        intent.putExtra(Statistics.DATA_TYPE, 1006);
                        context.startService(intent);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package oppo.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

public class OppoStatistics {
    private static final String APP_ID = "appId";
    private static final String APP_NAME = "appName";
    private static final String APP_PACKAGE = "appPackage";
    private static final String APP_VERSION = "appVersion";
    private static final int COMMON = 1006;
    private static final int COMMON_LIST = 1010;
    private static final String DATA_TYPE = "dataType";
    private static final String EVENT_ID = "eventID";
    private static final String LOG_MAP = "logMap";
    private static final String LOG_TAG = "logTag";
    private static final String MAP_LIST = "mapList";
    private static final String SSOID = "ssoid";
    private static final String TAG = "OppoStatistics--";
    private static final String UPLOAD_NOW = "uploadNow";
    private static int appId = 20120;
    private static ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void onCommon(Context context, String logTag, String eventId, Map<String, String> logMap, boolean uploadNow) {
        if (context == null) {
            Slog.w("common_test", "context is null!");
            return;
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            Slog.d("common_test", "onCommon begin: logTag=" + logTag + ", eventId=" + eventId + ", logMap=" + logMap + ", uploadNow=" + uploadNow);
        }
        if (!TextUtils.isEmpty(logTag)) {
            Map<String, String> cloneMap;
            if (logMap != null) {
                cloneMap = new HashMap(logMap);
            } else {
                cloneMap = new HashMap();
            }
            final String str = eventId;
            final boolean z = uploadNow;
            final String str2 = logTag;
            final Context context2 = context;
            sSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
                    intent.putExtra(OppoStatistics.APP_PACKAGE, "system");
                    intent.putExtra(OppoStatistics.APP_NAME, "system");
                    intent.putExtra(OppoStatistics.APP_VERSION, "system");
                    intent.putExtra(OppoStatistics.SSOID, "system");
                    intent.putExtra(OppoStatistics.APP_ID, OppoStatistics.appId);
                    intent.putExtra(OppoStatistics.EVENT_ID, str);
                    intent.putExtra(OppoStatistics.UPLOAD_NOW, z);
                    intent.putExtra(OppoStatistics.LOG_TAG, str2);
                    intent.putExtra(OppoStatistics.LOG_MAP, OppoStatistics.getCommonObject(cloneMap).toString());
                    intent.putExtra(OppoStatistics.DATA_TYPE, 1006);
                    if (context2 != null) {
                        context2.startService(intent);
                    }
                    cloneMap.clear();
                }
            });
        }
    }

    public static void onCommon(Context context, String logTag, String eventID, List<Map<String, String>> mapList, boolean uploadNow) {
        if (context == null) {
            Slog.w("common_test", "context is null!");
            return;
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            Slog.d("common_list_test", "onCommon begin: logTag=" + logTag + ", eventID=" + eventID + ", mapList=" + mapList + ", uploadNow=" + uploadNow);
        }
        if (!TextUtils.isEmpty(logTag)) {
            List<Map<String, String>> cloneList;
            if (mapList != null) {
                cloneList = new ArrayList(mapList);
            } else {
                cloneList = new ArrayList();
            }
            final String str = eventID;
            final boolean z = uploadNow;
            final String str2 = logTag;
            final Context context2 = context;
            sSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    JSONArray jsonArray = new JSONArray();
                    for (Map<String, String> map : cloneList) {
                        jsonArray.put(OppoStatistics.getCommonObject(map));
                    }
                    String mapListStr = "";
                    try {
                        mapListStr = jsonArray.toString();
                        if (mapListStr.length() >= 50000) {
                            return;
                        }
                    } catch (OutOfMemoryError e) {
                        Slog.d("common_list_test", "onCommon--Error:" + e);
                    }
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
                    intent.putExtra(OppoStatistics.APP_PACKAGE, "system");
                    intent.putExtra(OppoStatistics.APP_NAME, "system");
                    intent.putExtra(OppoStatistics.APP_VERSION, "system");
                    intent.putExtra(OppoStatistics.SSOID, "system");
                    intent.putExtra(OppoStatistics.APP_ID, OppoStatistics.appId);
                    intent.putExtra(OppoStatistics.EVENT_ID, str);
                    intent.putExtra(OppoStatistics.UPLOAD_NOW, z);
                    intent.putExtra(OppoStatistics.LOG_TAG, str2);
                    intent.putExtra(OppoStatistics.MAP_LIST, mapListStr);
                    intent.putExtra(OppoStatistics.DATA_TYPE, OppoStatistics.COMMON_LIST);
                    if (context2 != null) {
                        context2.startService(intent);
                    }
                    cloneList.clear();
                }
            });
        }
    }

    public static void onCommonSync(Context context, String logTag, String eventId, Map<String, String> logMap, boolean upLoadNow) {
        if (context == null) {
            Slog.w("common_test", "context is null!");
            return;
        }
        if (logTag != null) {
            try {
                if ((TextUtils.isEmpty(logTag) ^ 1) != 0) {
                    sendData(context, logTag, eventId, logMap, upLoadNow);
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception: " + e);
            }
        }
    }

    private static JSONObject getCommonObject(Map<String, String> logMap) {
        JSONObject jsonObject = new JSONObject();
        if (!(logMap == null || (logMap.isEmpty() ^ 1) == 0)) {
            try {
                for (String key : logMap.keySet()) {
                    jsonObject.put(key, logMap.get(key));
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception: " + e);
            }
        }
        return jsonObject;
    }

    private static void sendData(Context context, String logTag, String eventId, Map<String, String> logMap, boolean upLoadNow) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
        intent.putExtra(APP_PACKAGE, "system");
        intent.putExtra(APP_NAME, "system");
        intent.putExtra(APP_VERSION, "system");
        intent.putExtra(SSOID, "system");
        intent.putExtra(APP_ID, appId);
        intent.putExtra(EVENT_ID, eventId);
        intent.putExtra(UPLOAD_NOW, upLoadNow);
        intent.putExtra(LOG_TAG, logTag);
        intent.putExtra(LOG_MAP, getCommonObject(logMap).toString());
        intent.putExtra(DATA_TYPE, 1006);
        context.startService(intent);
        if (logMap != null) {
            logMap.clear();
        }
    }
}

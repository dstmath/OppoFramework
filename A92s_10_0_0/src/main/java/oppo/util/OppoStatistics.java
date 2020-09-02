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
    private static final String ATOM_EVENTID = "unusual_frequence_info";
    private static final int ATOM_ID = 20185;
    private static final String ATOM_LOGTAG = "atomReport";
    private static final String ATOM_PACKAGE = "com.coloros.atom";
    private static final String ATOM_VERSION = "1.0.0";
    private static final String CALL_COUNT = "call_count";
    private static final int COMMON = 1006;
    private static final int COMMON_LIST = 1010;
    private static final int COUNT_LIMIT = 20;
    private static final String DATA_TYPE = "dataType";
    private static final String EVENT_ID = "eventID";
    public static final int FLAG_SEND_TO_ATOM = 2;
    public static final int FLAG_SEND_TO_DCS = 1;
    private static final String GAP_TIME = "gapTime";
    private static final long GAP_TIME_LIMIT = 10000;
    private static final String LOG_MAP = "logMap";
    private static final String LOG_TAG = "logTag";
    private static final String MAP_LIST = "mapList";
    private static final String PKG_NAME_ATOM = "com.coloros.deepthinker";
    private static final String PKG_NAME_DCS = "com.nearme.statistics.rom";
    private static final String SERVICE_NAME_ATOM = "com.coloros.atom.services.AtomReceiverService";
    private static final String SERVICE_NAME_DCS = "com.nearme.statistics.rom.service.ReceiverService";
    private static final String SSOID = "ssoid";
    private static final String SYSTEM = "system";
    private static final String TAG = "OppoStatistics--";
    private static final String UPLOAD_NOW = "uploadNow";
    /* access modifiers changed from: private */
    public static int sAppId = 20120;
    /* access modifiers changed from: private */
    public static int sCount = 0;
    private static ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();
    /* access modifiers changed from: private */
    public static long sStartTime = 0;

    static /* synthetic */ int access$308() {
        int i = sCount;
        sCount = i + 1;
        return i;
    }

    public static void onCommon(Context context, String logTag, String eventId, Map<String, String> logMap, boolean uploadNow) {
        onCommon(context, sAppId, logTag, eventId, logMap, uploadNow);
    }

    public static void onCommon(Context context, String appIdStr, String logTag, String eventId, Map<String, String> logMap, boolean uploadNow) {
        if (TextUtils.isEmpty(appIdStr)) {
            Slog.w(TAG, "onCommon: appId is null.");
            return;
        }
        int appId = -1;
        try {
            appId = Integer.valueOf(appIdStr).intValue();
        } catch (NumberFormatException e) {
            Slog.w(TAG, "onCommon: illegal appId=" + appIdStr);
        }
        if (appId != -1) {
            onCommon(context, appId, logTag, eventId, logMap, uploadNow);
        }
    }

    public static void onCommon(final Context context, final int appId, final String logTag, final String eventId, Map<String, String> logMap, final boolean uploadNow) {
        final Map<String, String> cloneMap;
        if (context == null) {
            Slog.w(TAG, "onCommon: context is null!");
            return;
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            Slog.d(TAG, "onCommon: appId=" + appId + ", logTag=" + logTag + ", eventId=" + eventId + ", logMap=" + logMap + ", uploadNow=" + uploadNow);
        }
        if (!TextUtils.isEmpty(logTag)) {
            if (logMap != null) {
                cloneMap = new HashMap<>(logMap);
            } else {
                cloneMap = new HashMap<>();
            }
            sSingleThreadExecutor.execute(new Runnable() {
                /* class oppo.util.OppoStatistics.AnonymousClass1 */

                public void run() {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(OppoStatistics.PKG_NAME_DCS, OppoStatistics.SERVICE_NAME_DCS));
                    intent.putExtra(OppoStatistics.APP_PACKAGE, "system");
                    intent.putExtra(OppoStatistics.APP_NAME, "system");
                    intent.putExtra(OppoStatistics.APP_VERSION, "system");
                    intent.putExtra(OppoStatistics.SSOID, "system");
                    intent.putExtra(OppoStatistics.APP_ID, appId);
                    intent.putExtra(OppoStatistics.EVENT_ID, eventId);
                    intent.putExtra(OppoStatistics.UPLOAD_NOW, uploadNow);
                    intent.putExtra(OppoStatistics.LOG_TAG, logTag);
                    intent.putExtra(OppoStatistics.LOG_MAP, OppoStatistics.getCommonObject(cloneMap).toString());
                    intent.putExtra(OppoStatistics.DATA_TYPE, 1006);
                    OppoStatistics.startDcsService(context, intent);
                    cloneMap.clear();
                }
            });
        }
    }

    public static void onCommon(final Context context, final String logTag, final String eventID, List<Map<String, String>> mapList, final boolean uploadNow) {
        final List<Map<String, String>> cloneList;
        if (context == null) {
            Slog.w(TAG, "onCommon: context is null!");
            return;
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            Slog.d(TAG, "onCommon: logTag=" + logTag + ", eventID=" + eventID + ", mapList=" + mapList + ", uploadNow=" + uploadNow);
        }
        if (!TextUtils.isEmpty(logTag)) {
            if (mapList != null) {
                cloneList = new ArrayList<>(mapList);
            } else {
                cloneList = new ArrayList<>();
            }
            sSingleThreadExecutor.execute(new Runnable() {
                /* class oppo.util.OppoStatistics.AnonymousClass2 */

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
                        Slog.d(OppoStatistics.TAG, "onCommon--Error:" + e);
                    }
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(OppoStatistics.PKG_NAME_DCS, OppoStatistics.SERVICE_NAME_DCS));
                    intent.putExtra(OppoStatistics.APP_PACKAGE, "system");
                    intent.putExtra(OppoStatistics.APP_NAME, "system");
                    intent.putExtra(OppoStatistics.APP_VERSION, "system");
                    intent.putExtra(OppoStatistics.SSOID, "system");
                    intent.putExtra(OppoStatistics.APP_ID, OppoStatistics.sAppId);
                    intent.putExtra(OppoStatistics.EVENT_ID, eventID);
                    intent.putExtra(OppoStatistics.UPLOAD_NOW, uploadNow);
                    intent.putExtra(OppoStatistics.LOG_TAG, logTag);
                    intent.putExtra(OppoStatistics.MAP_LIST, mapListStr);
                    intent.putExtra(OppoStatistics.DATA_TYPE, 1010);
                    OppoStatistics.startDcsService(context, intent);
                    cloneList.clear();
                }
            });
        }
    }

    public static void onCommon(final Context context, final String logTag, final String eventId, Map<String, String> logMap, final boolean uploadNow, int flagSendTo) {
        final Map<String, String> cloneMap;
        final Map<String, String> cloneMap2;
        if (context == null) {
            Slog.w(TAG, "onCommon: context is null!");
            return;
        }
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            Slog.d(TAG, "onCommon: logTag=" + logTag + ", eventId=" + eventId + ", logMap=" + logMap + ", uploadNow=" + uploadNow);
        }
        if (!TextUtils.isEmpty(logTag)) {
            if (logMap != null) {
                cloneMap = new HashMap<>(logMap);
            } else {
                cloneMap = new HashMap<>();
            }
            if ((flagSendTo & 1) == 1) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class oppo.util.OppoStatistics.AnonymousClass3 */

                    public void run() {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(OppoStatistics.PKG_NAME_DCS, OppoStatistics.SERVICE_NAME_DCS));
                        intent.putExtra(OppoStatistics.APP_PACKAGE, "system");
                        intent.putExtra(OppoStatistics.APP_NAME, "system");
                        intent.putExtra(OppoStatistics.APP_VERSION, "system");
                        intent.putExtra(OppoStatistics.SSOID, "system");
                        intent.putExtra(OppoStatistics.APP_ID, OppoStatistics.sAppId);
                        intent.putExtra(OppoStatistics.EVENT_ID, eventId);
                        intent.putExtra(OppoStatistics.UPLOAD_NOW, uploadNow);
                        intent.putExtra(OppoStatistics.LOG_TAG, logTag);
                        intent.putExtra(OppoStatistics.LOG_MAP, OppoStatistics.getCommonObject(cloneMap).toString());
                        intent.putExtra(OppoStatistics.DATA_TYPE, 1006);
                        OppoStatistics.startDcsService(context, intent);
                        cloneMap.clear();
                    }
                });
            }
            if (logMap != null) {
                cloneMap2 = new HashMap<>(logMap);
            } else {
                cloneMap2 = new HashMap<>();
            }
            if ((flagSendTo & 2) == 2) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class oppo.util.OppoStatistics.AnonymousClass4 */

                    public void run() {
                        if (OppoStatistics.sCount == 0) {
                            long unused = OppoStatistics.sStartTime = System.currentTimeMillis();
                        }
                        OppoStatistics.access$308();
                        Intent atomIntent = new Intent();
                        atomIntent.setComponent(new ComponentName(OppoStatistics.PKG_NAME_ATOM, OppoStatistics.SERVICE_NAME_ATOM));
                        atomIntent.putExtra(OppoStatistics.APP_ID, OppoStatistics.sAppId);
                        atomIntent.putExtra(OppoStatistics.APP_PACKAGE, "system");
                        atomIntent.putExtra(OppoStatistics.LOG_TAG, logTag);
                        atomIntent.putExtra(OppoStatistics.EVENT_ID, eventId);
                        atomIntent.putExtra(OppoStatistics.LOG_MAP, OppoStatistics.getCommonObject(cloneMap2).toString());
                        OppoStatistics.startDcsService(context, atomIntent);
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - OppoStatistics.sStartTime > 10000) {
                            if (OppoStatistics.sCount > 20) {
                                HashMap<String, String> errorMap = new HashMap<>();
                                errorMap.put(OppoStatistics.GAP_TIME, String.valueOf(currentTime - OppoStatistics.sStartTime));
                                errorMap.put(OppoStatistics.CALL_COUNT, String.valueOf(OppoStatistics.sCount));
                                errorMap.put(OppoStatistics.APP_PACKAGE, "system");
                                errorMap.put(OppoStatistics.LOG_TAG, logTag);
                                errorMap.put(OppoStatistics.EVENT_ID, eventId);
                                errorMap.put(OppoStatistics.LOG_MAP, OppoStatistics.getCommonObject(cloneMap2).toString());
                                String logmap = OppoStatistics.getCommonObject(errorMap).toString();
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(OppoStatistics.PKG_NAME_DCS, OppoStatistics.SERVICE_NAME_DCS));
                                intent.putExtra(OppoStatistics.APP_ID, OppoStatistics.ATOM_ID);
                                intent.putExtra(OppoStatistics.APP_VERSION, OppoStatistics.ATOM_VERSION);
                                intent.putExtra(OppoStatistics.APP_PACKAGE, OppoStatistics.ATOM_PACKAGE);
                                intent.putExtra(OppoStatistics.SSOID, "system");
                                intent.putExtra(OppoStatistics.LOG_TAG, OppoStatistics.ATOM_LOGTAG);
                                intent.putExtra(OppoStatistics.EVENT_ID, OppoStatistics.ATOM_EVENTID);
                                intent.putExtra(OppoStatistics.LOG_MAP, logmap);
                                OppoStatistics.startDcsService(context, intent);
                                int unused2 = OppoStatistics.sCount = 0;
                                Slog.w(OppoStatistics.TAG, "onCommon too frequently ");
                                return;
                            }
                            int unused3 = OppoStatistics.sCount = 0;
                        }
                        cloneMap2.clear();
                    }
                });
            }
        }
    }

    public static void onCommonSync(Context context, String logTag, String eventId, Map<String, String> logMap, boolean upLoadNow) {
        if (context == null) {
            Slog.w(TAG, "onCommon: context is null!");
        } else if (logTag != null) {
            try {
                if (!TextUtils.isEmpty(logTag)) {
                    sendData(context, logTag, eventId, logMap, upLoadNow);
                }
            } catch (Exception e) {
                Slog.w(TAG, "onCommonSync Exception: " + e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static JSONObject getCommonObject(Map<String, String> logMap) {
        JSONObject jsonObject = new JSONObject();
        if (logMap != null && !logMap.isEmpty()) {
            try {
                for (String key : logMap.keySet()) {
                    jsonObject.put(key, logMap.get(key));
                }
            } catch (Exception e) {
                Slog.w(TAG, "getCommonObject Exception: " + e);
            }
        }
        return jsonObject;
    }

    private static void sendData(Context context, String logTag, String eventId, Map<String, String> logMap, boolean upLoadNow) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PKG_NAME_DCS, SERVICE_NAME_DCS));
        intent.putExtra(APP_PACKAGE, "system");
        intent.putExtra(APP_NAME, "system");
        intent.putExtra(APP_VERSION, "system");
        intent.putExtra(SSOID, "system");
        intent.putExtra(APP_ID, sAppId);
        intent.putExtra(EVENT_ID, eventId);
        intent.putExtra(UPLOAD_NOW, upLoadNow);
        intent.putExtra(LOG_TAG, logTag);
        intent.putExtra(LOG_MAP, getCommonObject(logMap).toString());
        intent.putExtra(DATA_TYPE, 1006);
        startDcsService(context, intent);
        if (logMap != null) {
            logMap.clear();
        }
    }

    /* access modifiers changed from: private */
    public static void startDcsService(Context context, Intent intent) {
        if (context == null || intent == null) {
            Slog.w(TAG, "startDcsService failed, Params is null.");
            return;
        }
        try {
            context.startService(intent);
        } catch (Exception e) {
            Slog.w(TAG, "startDcsService Exception: " + e);
        }
    }
}

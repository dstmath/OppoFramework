package com.oppo.statistics;

import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.agent.CommonAgent;
import com.oppo.statistics.agent.DebugAgent;
import com.oppo.statistics.agent.OnEventAgent;
import com.oppo.statistics.agent.PageVisitAgent;
import com.oppo.statistics.storage.PreferenceHandler;
import com.oppo.statistics.util.AccountUtil;
import com.oppo.statistics.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class NearMeStatistics {
    private static final String CLIENT_START = "ClientStart";
    private static final Pattern EVENTID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-]{1,64}$");
    private static final String EVENT_COUNT = "eventCount";
    private static final String EVENT_DURATION = "duration";
    private static final String EVENT_ID = "eventid";
    private static final String KV_EVENT = "KVEvent";
    private static PageVisitAgent sPageVisitAgent = new PageVisitAgent();
    private static ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void setSessionTimeOut(Context context, int timeout) {
        LogUtil.d("NearMeStatistics", "setSession timeout is " + timeout);
        if (timeout > 0) {
            try {
                PreferenceHandler.setSessionTimeout(context, timeout);
            } catch (Throwable e) {
                LogUtil.e("NearMeStatistics", e);
            }
        }
    }

    public static void setSsoID(Context context, String ssoid) {
        LogUtil.d("NearMeStatistics", "setSsoid ssoid is " + ssoid);
        if (TextUtils.isEmpty(ssoid) || ssoid.equals("null")) {
            ssoid = AccountUtil.SSOID_DEFAULT;
        }
        try {
            PreferenceHandler.setSsoID(context, ssoid);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onCommon(Context context, String logTag, Map<String, String> logMap) {
        onCommon(context, logTag, "", logMap, false);
    }

    public static void onCommon(Context context, String logTag, Map<String, String> logMap, boolean uploadNow) {
        onCommon(context, logTag, "", logMap, uploadNow);
    }

    public static void onCommon(Context context, String logTag, String eventID, Map<String, String> logMap) {
        onCommon(context, logTag, eventID, logMap, false);
    }

    public static void onCommon(Context context, String logTag, String eventID, Map<String, String> logMap, boolean uploadNow) {
        try {
            LogUtil.d("NearMeStatistics", "onCommon logTag is " + logTag + ",eventID:" + ",logmap:" + logMap + ",uploadNow:" + uploadNow);
            if (logTag != null && (TextUtils.isEmpty(logTag) ^ 1) != 0) {
                final Context context2 = context;
                final String str = logTag;
                final String str2 = eventID;
                final Map<String, String> map = logMap;
                final boolean z = uploadNow;
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        CommonAgent.recordCommon(context2, str, str2, map, z);
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onSpecialAppStart(Context context, int appCode) {
        LogUtil.d("NearMeStatistics", "onSpecialAppStart appCode:" + appCode);
        onCommon(context, CLIENT_START, CLIENT_START, null, false);
    }

    public static void removeSsoID(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "removeSsoID");
            PreferenceHandler.setSsoID(context);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onUserAction(Context context, int actionCode) {
    }

    @Deprecated
    public static void onUserAction(Context context, int actionCode, int actionMount) {
    }

    @Deprecated
    public static void onEvent(Context context, String eventID, String eventTag, int eventCount, long duration) {
        Map logMap = new HashMap();
        logMap.put(EVENT_DURATION, String.valueOf(duration));
        logMap.put(EVENT_COUNT, String.valueOf(eventCount));
        if (eventTag == null) {
            logMap.put(EVENT_ID, "");
        } else {
            logMap.put(EVENT_ID, eventTag);
        }
        onCommon(context, KV_EVENT, eventID, logMap);
    }

    @Deprecated
    public static void onEvent(Context context, String eventID, int eventCount) {
        onEvent(context, eventID, "", eventCount, 0);
    }

    @Deprecated
    public static void onEvent(Context context, String eventID, String eventTag, int eventCount) {
        onEvent(context, eventID, eventTag, eventCount, 0);
    }

    @Deprecated
    public static void onEvent(Context context, String eventID, String eventTag) {
        onEvent(context, eventID, eventTag, 1, 0);
    }

    @Deprecated
    public static void onEvent(Context context, String eventID) {
        onEvent(context, eventID, "", 1, 0);
    }

    @Deprecated
    public static void onEventStart(final Context context, final String eventID, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onEventStart eventID:" + eventID + ",eventTag:" + eventTag);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onEventStart(context, eventID, eventTag);
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onEventStart(final Context context, final String eventID) {
        try {
            LogUtil.d("NearMeStatistics", "onEventStart eventID:" + eventID);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onEventStart(context, eventID, "");
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onEventEnd(final Context context, final String eventID, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onEventEnd eventID:" + eventID + ",eventTag:" + eventTag);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onEventEnd(context, eventID, eventTag);
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onEventEnd(final Context context, final String eventID) {
        try {
            LogUtil.d("NearMeStatistics", "onEventEnd eventID:" + eventID);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onEventEnd(context, eventID, "");
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onKVEvent(Context context, String eventID, Map<String, String> eventMap, long duration) {
        if (eventMap != null) {
            if (duration > 0) {
                eventMap.put(EVENT_DURATION, String.valueOf(duration));
            } else {
                eventMap.put(EVENT_DURATION, AccountUtil.SSOID_DEFAULT);
            }
            if (!eventMap.containsKey(EVENT_COUNT)) {
                eventMap.put(EVENT_COUNT, AccountUtil.SSOID_DEFAULT);
            }
            onCommon(context, KV_EVENT, eventID, eventMap, false);
            return;
        }
        Map<String, String> logMap = new HashMap();
        if (duration > 0) {
            logMap.put(EVENT_DURATION, String.valueOf(duration));
        } else {
            logMap.put(EVENT_DURATION, AccountUtil.SSOID_DEFAULT);
        }
        logMap.put(EVENT_COUNT, AccountUtil.SSOID_DEFAULT);
        onCommon(context, KV_EVENT, eventID, logMap, false);
    }

    @Deprecated
    public static void onKVEvent(Context context, String eventID, Map<String, String> eventMap) {
        if (eventMap != null) {
            if (!eventMap.containsKey(EVENT_DURATION)) {
                eventMap.put(EVENT_DURATION, AccountUtil.SSOID_DEFAULT);
            }
            if (!eventMap.containsKey(EVENT_COUNT)) {
                eventMap.put(EVENT_COUNT, AccountUtil.SSOID_DEFAULT);
            }
        }
        onCommon(context, KV_EVENT, eventID, eventMap, false);
    }

    @Deprecated
    public static void onDynamicEvent(Context context, int uploadMode, int statId, Map<String, String> infoMap, Map<String, String> eventMap) {
        try {
            LogUtil.d("NearMeStatistics", "onDynamicEvent uploadMode:" + uploadMode + ",statId:" + statId);
            final Context context2 = context;
            final int i = uploadMode;
            final int i2 = statId;
            final Map<String, String> map = infoMap;
            final Map<String, String> map2 = eventMap;
            sSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    OnEventAgent.onDynamicEvent(context2, i, i2, map, map2);
                }
            });
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onStaticEvent(Context context, int uploadMode, int statId, String setId, String setValue, String remark, Map<String, String> eventMap) {
        try {
            LogUtil.d("NearMeStatistics", "onStaticEvent uploadMode:" + uploadMode + ",statId:" + statId + ",setId:" + setId + ",setValue:" + setValue + ",remark:" + remark);
            final Context context2 = context;
            final int i = uploadMode;
            final int i2 = statId;
            final String str = setId;
            final String str2 = setValue;
            final String str3 = remark;
            final Map<String, String> map = eventMap;
            sSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    OnEventAgent.onStaticEvent(context2, i, i2, str, str2, str3, map);
                }
            });
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventStart(final Context context, final String eventID, final Map<String, String> eventMap, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventStart eventID:" + eventID + ",eventTag:" + eventTag + ",eventMap:" + eventMap);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onKVEventStart(context, eventID, eventMap, eventTag);
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventEnd(final Context context, final String eventID, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventEnd eventID:" + eventID + ",eventTag:" + eventTag);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onKVEventEnd(context, eventID, eventTag);
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventStart(final Context context, final String eventID, final Map<String, String> eventMap) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventStart eventID:" + eventID + ",eventMap:" + eventMap);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onKVEventStart(context, eventID, eventMap, "");
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventEnd(final Context context, final String eventID) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventEnd eventID:" + eventID);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        OnEventAgent.onKVEventEnd(context, eventID, "");
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onPause(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "onPause...");
            sPageVisitAgent.onPause(context);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onResume(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "onResume...");
            sPageVisitAgent.onResume(context);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onError(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "onError...");
            new StatisticsExceptionHandler(context).setStatisticsExceptionHandler();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onDebug(boolean isDebug) {
        try {
            LogUtil.setDebugs(isDebug);
            LogUtil.d("NearMeStatistics", "onDebug (no context) sdk and dcs isDebug:" + isDebug);
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onDebug(final Context context, final boolean isDebug) {
        try {
            LogUtil.setDebugs(isDebug);
            LogUtil.d("NearMeStatistics", "packageName:" + context.getPackageName() + ",isDebug:" + isDebug + ",isDebugMode:" + LogUtil.isDebugMode);
            if (LogUtil.isDebugMode) {
                sSingleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        DebugAgent.setDebug(context, isDebug);
                    }
                });
            }
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    private static boolean formatCheck(String eventID, String eventTag, int eventCount) {
        if (eventID == null) {
            LogUtil.e("NearMeStatistics", "EventID is null!");
            return false;
        } else if (!EVENTID_PATTERN.matcher(eventID).find()) {
            LogUtil.e("NearMeStatistics", "EventID format error!");
            return false;
        } else if (eventTag == null) {
            LogUtil.e("NearMeStatistics", "EventTag format error!");
            return false;
        } else if (eventCount <= 10000 && eventCount >= 1) {
            return true;
        } else {
            LogUtil.e("NearMeStatistics", "EventCount format error!");
            return false;
        }
    }
}

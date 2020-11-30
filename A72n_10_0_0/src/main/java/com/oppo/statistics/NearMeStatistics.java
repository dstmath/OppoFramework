package com.oppo.statistics;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.agent.AtomAgent;
import com.oppo.statistics.agent.CommonAgent;
import com.oppo.statistics.agent.DebugAgent;
import com.oppo.statistics.agent.OnEventAgent;
import com.oppo.statistics.agent.PageVisitAgent;
import com.oppo.statistics.agent.StaticPeriodDataRecord;
import com.oppo.statistics.data.SettingKeyBean;
import com.oppo.statistics.record.AppLifecycleCallbacks;
import com.oppo.statistics.storage.PreferenceHandler;
import com.oppo.statistics.util.AccountUtil;
import com.oppo.statistics.util.LogUtil;
import com.oppo.statistics.util.VersionUtil;
import java.util.HashMap;
import java.util.List;
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
    public static final int FLAG_SEND_TO_ATOM = 2;
    public static final int FLAG_SEND_TO_DCS = 1;
    private static final String KV_EVENT = "KVEvent";
    private static final int MAX_EVENT_COUNT = 10000;
    private static final int MIN_EVENT_COUNT = 1;
    private static Context sApplicationContext;
    private static PageVisitAgent sPageVisitAgent = new PageVisitAgent();
    private static ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public static void init(Context context) {
        if (context == null) {
            LogUtil.w("NearMeStatistics", "SDK init failed! context is null.");
            return;
        }
        sApplicationContext = context.getApplicationContext();
        ((Application) sApplicationContext).registerActivityLifecycleCallbacks(AppLifecycleCallbacks.getInstance());
    }

    public static void setSessionTimeOut(Context context, int timeout) {
        LogUtil.d("NearMeStatistics", "setSession timeout is " + timeout);
        if (timeout > 0) {
            try {
                PreferenceHandler.setSessionTimeout(context, timeout);
            } catch (Exception e) {
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
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onCommon(Context context, String logTag, String eventID, Map<String, String> logMap) {
        onCommon(context, "", logTag, eventID, logMap, false, 1);
    }

    @Deprecated
    public static void onCommon(Context context, String logTag, String eventID, Map<String, String> logMap, boolean uploadNow) {
        onCommon(context, "", logTag, eventID, logMap, uploadNow, 1);
    }

    public static void onCommon(Context context, String logTag, String eventID, Map<String, String> logMap, int flagSendTo) {
        onCommon(context, "", logTag, eventID, logMap, false, flagSendTo);
    }

    public static void onCommon(Context context, String appId, String logTag, String eventID, Map<String, String> logMap) {
        onCommon(context, appId, logTag, eventID, logMap, false, 1);
    }

    public static void onCommon(Context context, String appId, String logTag, String eventID, Map<String, String> logMap, boolean uploadNow) {
        onCommon(context, appId, logTag, eventID, logMap, uploadNow, 1);
    }

    public static void onCommon(final Context context, final String appId, final String logTag, final String eventID, final Map<String, String> logMap, final boolean uploadNow, int flagSendTo) {
        Exception e;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("onCommon logTag is ");
            sb.append(logTag);
            sb.append(",eventID:");
            sb.append(eventID);
            sb.append(",logmap:");
            sb.append(logMap);
            sb.append(",uploadNow:");
            try {
                sb.append(uploadNow);
                sb.append(",flagSendTo:");
                sb.append(flagSendTo);
                LogUtil.d("NearMeStatistics", sb.toString());
                if (!TextUtils.isEmpty(logTag)) {
                    if ((flagSendTo & 1) == 1) {
                        sSingleThreadExecutor.execute(new Runnable() {
                            /* class com.oppo.statistics.NearMeStatistics.AnonymousClass1 */

                            public void run() {
                                CommonAgent.recordCommon(context, appId, logTag, eventID, logMap, uploadNow);
                            }
                        });
                    }
                    if ((flagSendTo & 2) == 2) {
                        try {
                            sSingleThreadExecutor.execute(new Runnable() {
                                /* class com.oppo.statistics.NearMeStatistics.AnonymousClass2 */

                                public void run() {
                                    AtomAgent.recordAtomCommon(context, logTag, eventID, logMap);
                                }
                            });
                        } catch (Exception e2) {
                            e = e2;
                            LogUtil.e("NearMeStatistics", e);
                        }
                    }
                } else {
                    LogUtil.e("Send data failed! logTag is null.");
                }
            } catch (Exception e3) {
                e = e3;
                LogUtil.e("NearMeStatistics", e);
            }
        } catch (Exception e4) {
            e = e4;
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onStaticDataUpdate(final Context context, final String logTag, final String eventID, final Map<String, String> logMap) {
        try {
            LogUtil.d("NearMeStatistics", "onStaticDataUpdate logTag:" + logTag + ", eventID:, logmap:" + logMap);
            if (!TextUtils.isEmpty(logTag)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass3 */

                    public void run() {
                        StaticPeriodDataRecord.updateData(context, logTag, eventID, logMap);
                    }
                });
            } else {
                LogUtil.e("Send data failed! logTag is null.");
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onSettingKeyUpdate(final Context context, final String logTag, final String eventID, final List<SettingKeyBean> keys) {
        try {
            LogUtil.d("NearMeStatistics", "onSettingKeyUpdate logTag:" + logTag + ", eventID:, keys:" + keys);
            if (!TextUtils.isEmpty(logTag)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass4 */

                    public void run() {
                        StaticPeriodDataRecord.updateSettingKeyList(context, logTag, eventID, keys);
                    }
                });
            } else {
                LogUtil.e("Send data failed! logTag is null.");
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static boolean isSupportStaticData(Context context) {
        return VersionUtil.isSupportPeriodData(context);
    }

    @Deprecated
    public static void onSpecialAppStart(Context context, int appCode) {
        LogUtil.d("NearMeStatistics", "onSpecialAppStart appCode:" + appCode);
        onCommon(context, CLIENT_START, CLIENT_START, (Map<String, String>) null, false);
    }

    public static void removeSsoID(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "removeSsoID");
            PreferenceHandler.setSsoID(context);
        } catch (Exception e) {
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
        Map<String, String> logMap = new HashMap<>();
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
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass5 */

                    public void run() {
                        OnEventAgent.onEventStart(context, eventID, eventTag);
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onEventStart(final Context context, final String eventID) {
        try {
            LogUtil.d("NearMeStatistics", "onEventStart eventID:" + eventID);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass6 */

                    public void run() {
                        OnEventAgent.onEventStart(context, eventID, "");
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onEventEnd(final Context context, final String eventID, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onEventEnd eventID:" + eventID + ",eventTag:" + eventTag);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass7 */

                    public void run() {
                        OnEventAgent.onEventEnd(context, eventID, eventTag);
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onEventEnd(final Context context, final String eventID) {
        try {
            LogUtil.d("NearMeStatistics", "onEventEnd eventID:" + eventID);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass8 */

                    public void run() {
                        OnEventAgent.onEventEnd(context, eventID, "");
                    }
                });
            }
        } catch (Exception e) {
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
        Map<String, String> logMap = new HashMap<>();
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
    public static void onDynamicEvent(final Context context, final int uploadMode, final int statId, final Map<String, String> infoMap, final Map<String, String> eventMap) {
        try {
            LogUtil.d("NearMeStatistics", "onDynamicEvent uploadMode:" + uploadMode + ",statId:" + statId);
            sSingleThreadExecutor.execute(new Runnable() {
                /* class com.oppo.statistics.NearMeStatistics.AnonymousClass9 */

                public void run() {
                    OnEventAgent.onDynamicEvent(context, uploadMode, statId, infoMap, eventMap);
                }
            });
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    @Deprecated
    public static void onStaticEvent(final Context context, final int uploadMode, final int statId, final String setId, final String setValue, final String remark, final Map<String, String> eventMap) {
        Exception e;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("onStaticEvent uploadMode:");
            try {
                sb.append(uploadMode);
                sb.append(",statId:");
            } catch (Exception e2) {
                e = e2;
                LogUtil.e("NearMeStatistics", e);
            }
            try {
                sb.append(statId);
                sb.append(",setId:");
                try {
                    sb.append(setId);
                    sb.append(",setValue:");
                    try {
                        sb.append(setValue);
                        sb.append(",remark:");
                        try {
                            sb.append(remark);
                            LogUtil.d("NearMeStatistics", sb.toString());
                            sSingleThreadExecutor.execute(new Runnable() {
                                /* class com.oppo.statistics.NearMeStatistics.AnonymousClass10 */

                                public void run() {
                                    OnEventAgent.onStaticEvent(context, uploadMode, statId, setId, setValue, remark, eventMap);
                                }
                            });
                        } catch (Exception e3) {
                            e = e3;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        LogUtil.e("NearMeStatistics", e);
                    }
                } catch (Exception e5) {
                    e = e5;
                    LogUtil.e("NearMeStatistics", e);
                }
            } catch (Exception e6) {
                e = e6;
                LogUtil.e("NearMeStatistics", e);
            }
        } catch (Exception e7) {
            e = e7;
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventStart(final Context context, final String eventID, final Map<String, String> eventMap, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventStart eventID:" + eventID + ",eventTag:" + eventTag + ",eventMap:" + eventMap);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass11 */

                    public void run() {
                        OnEventAgent.onKVEventStart(context, eventID, eventMap, eventTag);
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventEnd(final Context context, final String eventID, final String eventTag) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventEnd eventID:" + eventID + ",eventTag:" + eventTag);
            if (formatCheck(eventID, eventTag, 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass12 */

                    public void run() {
                        OnEventAgent.onKVEventEnd(context, eventID, eventTag);
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventStart(final Context context, final String eventID, final Map<String, String> eventMap) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventStart eventID:" + eventID + ",eventMap:" + eventMap);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass13 */

                    public void run() {
                        OnEventAgent.onKVEventStart(context, eventID, eventMap, "");
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onKVEventEnd(final Context context, final String eventID) {
        try {
            LogUtil.d("NearMeStatistics", "onKVEventEnd eventID:" + eventID);
            if (formatCheck(eventID, "", 1)) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass14 */

                    public void run() {
                        OnEventAgent.onKVEventEnd(context, eventID, "");
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onPause(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "onPause...");
            sPageVisitAgent.onPause(context);
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onResume(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "onResume...");
            sPageVisitAgent.onResume(context);
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onError(Context context) {
        try {
            LogUtil.d("NearMeStatistics", "onError...");
            new StatisticsExceptionHandler(context).setStatisticsExceptionHandler();
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onDebug(boolean isDebug) {
        try {
            LogUtil.setDebugs(isDebug);
            LogUtil.d("NearMeStatistics", "onDebug (no context) sdk and dcs isDebug:" + isDebug);
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        }
    }

    public static void onDebug(final Context context, final boolean isDebug) {
        try {
            LogUtil.setDebugs(isDebug);
            LogUtil.d("NearMeStatistics", "packageName:" + context.getPackageName() + ",isDebug:" + isDebug + ",isDebugMode:" + LogUtil.isDebugMode);
            if (LogUtil.isDebugMode) {
                sSingleThreadExecutor.execute(new Runnable() {
                    /* class com.oppo.statistics.NearMeStatistics.AnonymousClass15 */

                    public void run() {
                        DebugAgent.setDebug(context, isDebug);
                    }
                });
            }
        } catch (Exception e) {
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
        } else if (eventCount <= MAX_EVENT_COUNT && eventCount >= 1) {
            return true;
        } else {
            LogUtil.e("NearMeStatistics", "EventCount format error!");
            return false;
        }
    }
}

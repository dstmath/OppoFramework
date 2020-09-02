package com.oppo.statistics.record;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.oppo.statistics.DataTypeConstants;
import com.oppo.statistics.data.AppLogBean;
import com.oppo.statistics.data.AppStartBean;
import com.oppo.statistics.data.CommonBean;
import com.oppo.statistics.data.DataConstants;
import com.oppo.statistics.data.DebugBean;
import com.oppo.statistics.data.DynamicEventBean;
import com.oppo.statistics.data.ExceptionBean;
import com.oppo.statistics.data.PageVisitBean;
import com.oppo.statistics.data.SpecialAppStartBean;
import com.oppo.statistics.data.StaticEventBean;
import com.oppo.statistics.data.StatisticBean;
import com.oppo.statistics.data.UserActionBean;
import com.oppo.statistics.util.AccountUtil;
import com.oppo.statistics.util.ApkInfoUtil;
import com.oppo.statistics.util.LogUtil;
import com.oppo.statistics.util.VersionUtil;

public class RecordHandler {
    private static final String ACTION_AMOUNT = "actionAmount";
    private static final String ACTION_CODE = "actionCode";
    private static final String ACTION_TIME = "actionTime";
    private static final String APP_ID = "appId";
    private static final String APP_NAME = "appName";
    private static final String APP_PACKAGE = "appPackage";
    private static final String APP_SESSION_ID = "statSId";
    private static final String APP_VERSION = "appVersion";
    private static final String DATA_TYPE = "dataType";
    private static final String DCS_PKG_NAME = "com.nearme.statistics.rom";
    private static final String DCS_SERVICE_NAME = "com.nearme.statistics.rom.service.ReceiverService";
    private static final String DEBUG = "debug";
    private static final String EVENT_BODY = "eventBody";
    private static final String EVENT_ID = "eventID";
    private static final String EVENT_TYPE = "eventType";
    private static final String EXCEPTION = "exception";
    private static final String EXCEPTION_COUNT = "count";
    private static final String EXCEPTION_TIME = "time";
    private static final String LOGIN_TIME = "loginTime";
    private static final String LOG_MAP = "logMap";
    private static final String LOG_TAG = "logTag";
    private static final String PAGE_VISIT_ACTIVIES = "activities";
    private static final String PAGE_VISIT_DURATION = "duration";
    private static final String PAGE_VISIT_TIME = "time";
    private static final String SSOID = "ssoid";
    private static final String TAG = RecordHandler.class.getSimpleName();
    private static final String UPLOAD_MODE = "uploadMode";
    private static final String UPLOAD_NOW = "uploadNow";

    public static void addTask(Context context, StatisticBean bean) {
        if (bean == null || context == null) {
            LogUtil.d("RecordHandler add Task error -- bean or context is null--" + bean + "," + context);
            return;
        }
        try {
            switch (bean.getDataType()) {
                case 1:
                    Intent intent = getStartServiceIntent(context);
                    intent.putExtra(LOGIN_TIME, ((AppStartBean) bean).getTime());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.APP_START);
                    startDcsService(context, intent);
                    return;
                case 2:
                    UserActionBean userActionBean = (UserActionBean) bean;
                    Intent intent2 = getStartServiceIntent(context);
                    intent2.putExtra(ACTION_AMOUNT, userActionBean.getActionAmount());
                    intent2.putExtra(ACTION_CODE, userActionBean.getActionCode());
                    intent2.putExtra(ACTION_TIME, userActionBean.getActionDate());
                    intent2.putExtra(DATA_TYPE, DataTypeConstants.USER_ACTION);
                    startDcsService(context, intent2);
                    return;
                case DataConstants.TYPE_PAGE_VISIT:
                    PageVisitBean pageVisitBean = (PageVisitBean) bean;
                    Intent intent3 = getStartServiceIntent(context);
                    intent3.putExtra("time", pageVisitBean.getTime());
                    intent3.putExtra(PAGE_VISIT_DURATION, pageVisitBean.getDuration());
                    intent3.putExtra(PAGE_VISIT_ACTIVIES, pageVisitBean.getActivities());
                    intent3.putExtra(DATA_TYPE, DataTypeConstants.PAGE_VISIT);
                    startDcsService(context, intent3);
                    return;
                case DataConstants.TYPE_APP_LOG:
                    AppLogBean appLogBean = (AppLogBean) bean;
                    Intent intent4 = getStartServiceIntent(context);
                    intent4.putExtra(EVENT_BODY, appLogBean.getBody());
                    intent4.putExtra(EVENT_TYPE, appLogBean.getType());
                    intent4.putExtra(DATA_TYPE, DataTypeConstants.APP_LOG);
                    startDcsService(context, intent4);
                    return;
                case DataConstants.TYPE_EXCEPTION:
                    ExceptionBean exceptionBean = (ExceptionBean) bean;
                    Intent intent5 = getStartServiceIntent(context);
                    intent5.putExtra(EXCEPTION, exceptionBean.getException());
                    intent5.putExtra(EXCEPTION_COUNT, exceptionBean.getCount());
                    intent5.putExtra("time", exceptionBean.getEventTime());
                    intent5.putExtra(DATA_TYPE, DataTypeConstants.EXCEPTION);
                    startDcsService(context, intent5);
                    return;
                case 6:
                case 8:
                default:
                    return;
                case DataConstants.TYPE_SPECIAL_APP_START:
                    SpecialAppStartBean appStartBean = (SpecialAppStartBean) bean;
                    Intent intent6 = getSpecialStartServiceIntent(appStartBean.getAppId(), context);
                    intent6.putExtra(LOGIN_TIME, appStartBean.getTime());
                    intent6.putExtra(DATA_TYPE, DataTypeConstants.APP_START);
                    startDcsService(context, intent6);
                    return;
                case DataConstants.TYPE_COMMON:
                    LogUtil.d("NearMeStatistics", "bean:" + bean.toString());
                    Intent intent7 = getCommonStartServiceIntent((CommonBean) bean, context);
                    intent7.putExtra(DATA_TYPE, DataTypeConstants.COMMON);
                    startDcsService(context, intent7);
                    return;
                case DataConstants.DYNAMIC_EVENT:
                    DynamicEventBean dynamicBean = (DynamicEventBean) bean;
                    Intent intent8 = getStartServiceIntent(context);
                    intent8.putExtra(EVENT_BODY, dynamicBean.getBody());
                    intent8.putExtra(UPLOAD_MODE, dynamicBean.getUploadMode());
                    intent8.putExtra(DATA_TYPE, DataTypeConstants.DYNAMIC_EVENT_TYPE);
                    startDcsService(context, intent8);
                    return;
                case DataConstants.STATIC_EVENT:
                    StaticEventBean staticBean = (StaticEventBean) bean;
                    Intent intent9 = getStartServiceIntent(context);
                    intent9.putExtra(EVENT_BODY, staticBean.getBody());
                    intent9.putExtra(UPLOAD_MODE, staticBean.getUploadMode());
                    intent9.putExtra(DATA_TYPE, DataTypeConstants.STATIC_EVENT_TYPE);
                    startDcsService(context, intent9);
                    return;
                case DataConstants.DEBUG:
                    Intent intent10 = getStartServiceIntent(context);
                    intent10.putExtra(DATA_TYPE, DataTypeConstants.DEBUG_TYPE);
                    intent10.putExtra(DEBUG, ((DebugBean) bean).getFlag());
                    startDcsService(context, intent10);
                    return;
                case DataConstants.TYPE_PERIOD_DATA:
                    LogUtil.d("NearMeStatistics", "bean:" + bean.toString());
                    Intent intent11 = getCommonStartServiceIntent((CommonBean) bean, context);
                    if (VersionUtil.isSupportPeriodData(context)) {
                        intent11.putExtra(DATA_TYPE, DataTypeConstants.PERIOD_DATA);
                    } else {
                        intent11.putExtra(DATA_TYPE, DataTypeConstants.COMMON);
                    }
                    startDcsService(context, intent11);
                    return;
                case DataConstants.TYPE_SETTING_KEY:
                    LogUtil.d("NearMeStatistics", "bean:" + bean.toString());
                    Intent intent12 = getCommonStartServiceIntent((CommonBean) bean, context);
                    if (VersionUtil.isSupportPeriodData(context)) {
                        intent12.putExtra(DATA_TYPE, DataTypeConstants.SETTING_KEY);
                        startDcsService(context, intent12);
                        return;
                    }
                    return;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
    }

    private static void startDcsService(Context context, Intent intent) {
        if (context == null) {
            new Exception("DataSendException: context is null.").printStackTrace();
        } else {
            context.startService(intent);
        }
    }

    private static Intent getStartServiceIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DCS_PKG_NAME, DCS_SERVICE_NAME));
        intent.putExtra(APP_ID, ApkInfoUtil.getAppCode(context));
        intent.putExtra(APP_VERSION, ApkInfoUtil.getVersionName(context));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(context));
        intent.putExtra(APP_NAME, ApkInfoUtil.getAppName(context));
        intent.putExtra("ssoid", AccountUtil.getSsoId(context));
        intent.putExtra(APP_SESSION_ID, StatIdManager.getInstance().getAppSessionId(context));
        return intent;
    }

    private static Intent getCommonStartServiceIntent(CommonBean commonBean, Context context) {
        int appId;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DCS_PKG_NAME, DCS_SERVICE_NAME));
        if (!TextUtils.isEmpty(commonBean.getAppId())) {
            try {
                appId = Integer.valueOf(commonBean.getAppId()).intValue();
            } catch (NumberFormatException e) {
                LogUtil.e("input appId is NumberFormatException, use appId in manifest.");
                appId = ApkInfoUtil.getAppCode(context);
            }
        } else {
            appId = ApkInfoUtil.getAppCode(context);
        }
        intent.putExtra(APP_ID, appId);
        intent.putExtra(APP_VERSION, ApkInfoUtil.getVersionName(context));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(context));
        intent.putExtra(APP_NAME, ApkInfoUtil.getAppName(context));
        intent.putExtra("ssoid", AccountUtil.getSsoId(context));
        intent.putExtra(UPLOAD_NOW, commonBean.getUploadNow());
        intent.putExtra(LOG_TAG, commonBean.getLogTag());
        intent.putExtra(EVENT_ID, commonBean.getEventID());
        intent.putExtra(LOG_MAP, commonBean.getLogMap());
        intent.putExtra(APP_SESSION_ID, StatIdManager.getInstance().getAppSessionId(context));
        return intent;
    }

    private static Intent getSpecialStartServiceIntent(int appID, Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DCS_PKG_NAME, DCS_SERVICE_NAME));
        intent.putExtra(APP_ID, appID);
        intent.putExtra(APP_VERSION, ApkInfoUtil.getVersionName(context));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(context));
        intent.putExtra(APP_NAME, ApkInfoUtil.getAppName(context));
        intent.putExtra("ssoid", AccountUtil.getSsoId(context));
        intent.putExtra(APP_SESSION_ID, StatIdManager.getInstance().getAppSessionId(context));
        return intent;
    }
}

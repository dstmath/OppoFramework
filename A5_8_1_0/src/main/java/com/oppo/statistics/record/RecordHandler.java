package com.oppo.statistics.record;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.UserHandle;
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

public class RecordHandler {
    private static final String ACTION_AMOUNT = "actionAmount";
    private static final String ACTION_CODE = "actionCode";
    private static final String ACTION_TIME = "actionTime";
    private static final String APP_ID = "appId";
    private static final String APP_NAME = "appName";
    private static final String APP_PACKAGE = "appPackage";
    private static final String APP_VERSION = "appVersion";
    private static final String DATA_TYPE = "dataType";
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
    private static Context mContext;

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void addTask(Context context, StatisticBean bean) {
        if (bean == null || context == null) {
            LogUtil.d("RecordHandler add Task error -- bean or context is null--" + bean + "," + context);
            return;
        }
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
        try {
            Intent intent;
            switch (bean.getDataType()) {
                case DataConstants.TYPE_APP_START /*1*/:
                    AppStartBean appStartBean = (AppStartBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(LOGIN_TIME, appStartBean.getTime());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.APP_START);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.TYPE_USER_ACTION /*2*/:
                    UserActionBean userActionBean = (UserActionBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(ACTION_AMOUNT, userActionBean.getActionAmount());
                    intent.putExtra(ACTION_CODE, userActionBean.getActionCode());
                    intent.putExtra(ACTION_TIME, userActionBean.getActionDate());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.USER_ACTION);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.TYPE_PAGE_VISIT /*3*/:
                    PageVisitBean pageVisitBean = (PageVisitBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra("time", pageVisitBean.getTime());
                    intent.putExtra(PAGE_VISIT_DURATION, pageVisitBean.getDuration());
                    intent.putExtra(PAGE_VISIT_ACTIVIES, pageVisitBean.getActivities());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.PAGE_VISIT);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.TYPE_APP_LOG /*4*/:
                    AppLogBean appLogBean = (AppLogBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(EVENT_BODY, appLogBean.getBody());
                    intent.putExtra(EVENT_TYPE, appLogBean.getType());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.APP_LOG);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.TYPE_EXCEPTION /*5*/:
                    ExceptionBean exceptionBean = (ExceptionBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(EXCEPTION, exceptionBean.getException());
                    intent.putExtra(EXCEPTION_COUNT, exceptionBean.getCount());
                    intent.putExtra("time", exceptionBean.getEventTime());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.EXCEPTION);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.TYPE_SPECIAL_APP_START /*7*/:
                    SpecialAppStartBean appStartBean2 = (SpecialAppStartBean) bean;
                    intent = getSpecialStartServiceIntent(appStartBean2.getAppId());
                    intent.putExtra(LOGIN_TIME, appStartBean2.getTime());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.APP_START);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.TYPE_COMMON /*9*/:
                    CommonBean commonBean = (CommonBean) bean;
                    LogUtil.d("NearMeStatistics", "bean:" + bean.toString());
                    intent = getCommonStartServiceIntent();
                    intent.putExtra(UPLOAD_NOW, commonBean.getUploadNow());
                    intent.putExtra(LOG_TAG, commonBean.getLogTag());
                    intent.putExtra(EVENT_ID, commonBean.getEventID());
                    intent.putExtra(LOG_MAP, commonBean.getLogMap());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.COMMON);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.DYNAMIC_EVENT /*10*/:
                    DynamicEventBean dynamicBean = (DynamicEventBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(EVENT_BODY, dynamicBean.getBody());
                    intent.putExtra(UPLOAD_MODE, dynamicBean.getUploadMode());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.DYNAMIC_EVENT_TYPE);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.STATIC_EVENT /*11*/:
                    StaticEventBean staticBean = (StaticEventBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(EVENT_BODY, staticBean.getBody());
                    intent.putExtra(UPLOAD_MODE, staticBean.getUploadMode());
                    intent.putExtra(DATA_TYPE, DataTypeConstants.STATIC_EVENT_TYPE);
                    startDcsService(mContext, intent);
                    break;
                case DataConstants.DEBUG /*12*/:
                    DebugBean staticBean2 = (DebugBean) bean;
                    intent = getStartServiceIntent();
                    intent.putExtra(DATA_TYPE, DataTypeConstants.DEBUG_TYPE);
                    intent.putExtra(DEBUG, staticBean2.getFlag());
                    startDcsService(mContext, intent);
                    break;
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, e);
        }
    }

    private static void startDcsService(Context context, Intent intent) {
        if (VERSION.SDK_INT >= 17) {
            context.startServiceAsUser(intent, UserHandle.OWNER);
        } else {
            context.startService(intent);
        }
    }

    private static Intent getStartServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
        intent.putExtra(APP_ID, ApkInfoUtil.getAppCode(mContext));
        intent.putExtra(APP_VERSION, ApkInfoUtil.getVersionName(mContext));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(mContext));
        intent.putExtra(APP_NAME, ApkInfoUtil.getAppName(mContext));
        intent.putExtra("ssoid", AccountUtil.getSsoId(mContext));
        return intent;
    }

    private static Intent getCommonStartServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
        intent.putExtra(APP_ID, ApkInfoUtil.getAppCode(mContext));
        intent.putExtra(APP_VERSION, ApkInfoUtil.getVersionName(mContext));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(mContext));
        intent.putExtra(APP_NAME, ApkInfoUtil.getAppName(mContext));
        intent.putExtra("ssoid", AccountUtil.getSsoId(mContext));
        return intent;
    }

    private static Intent getSpecialStartServiceIntent(int appID) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.nearme.statistics.rom", "com.nearme.statistics.rom.service.ReceiverService"));
        intent.putExtra(APP_ID, appID);
        intent.putExtra(APP_VERSION, ApkInfoUtil.getVersionName(mContext));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(mContext));
        intent.putExtra(APP_NAME, ApkInfoUtil.getAppName(mContext));
        intent.putExtra("ssoid", AccountUtil.getSsoId(mContext));
        return intent;
    }
}

package com.oppo.statistics.agent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.oppo.statistics.DataTypeConstants;
import com.oppo.statistics.data.CommonBean;
import com.oppo.statistics.data.StatisticBean;
import com.oppo.statistics.util.AccountUtil;
import com.oppo.statistics.util.ApkInfoUtil;
import com.oppo.statistics.util.LogUtil;
import java.util.HashMap;
import java.util.Map;

public class AtomAgent {
    private static final String APP_ID = "appId";
    private static final String APP_NAME = "appName";
    private static final String APP_PACKAGE = "appPackage";
    private static final String APP_VERSION = "appVersion";
    private static final int ATOM_ID = 20185;
    private static final String DATA_TYPE = "dataType";
    private static final String EVENT_ID = "eventID";
    private static final String KEY_CALLCOUNT = "call_count";
    private static final String KEY_GAPTIME = "gapTime";
    private static final String LOG_MAP = "logMap";
    private static final String LOG_TAG = "logTag";
    private static final int MAX_COUNT = 20;
    private static final long MAX_GAP_TIME = 10000;
    private static final String PKGNAME_ATOM = "com.coloros.deepthinker";
    private static final String PKGNAME_DCS = "com.nearme.statistics.rom";
    private static final String SERVICENAME_ATOM = "com.coloros.atom.services.AtomReceiverService";
    private static final String SERVICENAME_DCS = "com.nearme.statistics.rom.service.ReceiverService";
    private static final String SSOID = "ssoid";
    private static final String TAG = "AtomAgent";
    private static final String UPLOAD_NOW = "uploadNow";
    private static int sCount = 0;
    private static long startTime = 0;

    public static void recordAtomCommon(Context context, String logTag, String eventID, Map<String, String> logMap) {
        addTaskForAtom(context, new CommonBean(logTag, eventID, BaseAgent.map2JsonObject(logMap).toString()));
    }

    private static Intent getAtomServiceIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PKGNAME_ATOM, SERVICENAME_ATOM));
        intent.putExtra(APP_ID, ApkInfoUtil.getAppCode(context));
        intent.putExtra(APP_PACKAGE, ApkInfoUtil.getPackageName(context));
        return intent;
    }

    private static void addTaskForAtom(Context context, StatisticBean bean) {
        if (bean == null || context == null) {
            LogUtil.d("AtomAgent add Task error -- bean or context is null--" + bean + "," + context);
            return;
        }
        if (sCount == 0) {
            startTime = System.currentTimeMillis();
        }
        sCount++;
        CommonBean commonBean = (CommonBean) bean;
        Intent intent = getAtomServiceIntent(context);
        intent.putExtra(LOG_TAG, commonBean.getLogTag());
        intent.putExtra(EVENT_ID, commonBean.getEventID());
        intent.putExtra(LOG_MAP, commonBean.getLogMap());
        try {
            startService(context, intent);
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime <= MAX_GAP_TIME) {
                return;
            }
            if (sCount > MAX_COUNT) {
                HashMap<String, String> errorMap = new HashMap<>();
                errorMap.put(KEY_GAPTIME, String.valueOf(currentTime - startTime));
                errorMap.put(KEY_CALLCOUNT, String.valueOf(sCount));
                errorMap.put(APP_PACKAGE, ApkInfoUtil.getPackageName(context));
                errorMap.put(LOG_TAG, commonBean.getLogTag());
                errorMap.put(EVENT_ID, commonBean.getEventID());
                errorMap.put(LOG_MAP, commonBean.getLogMap());
                String logmap = BaseAgent.map2JsonObject(errorMap).toString();
                Intent intent2 = new Intent();
                intent2.setComponent(new ComponentName(PKGNAME_DCS, SERVICENAME_DCS));
                intent2.putExtra(APP_ID, ATOM_ID);
                intent2.putExtra(APP_VERSION, "1.0.0");
                intent2.putExtra(APP_NAME, "Atom");
                intent2.putExtra(APP_PACKAGE, "com.coloros.atom");
                intent2.putExtra("ssoid", AccountUtil.getSsoId(context));
                intent2.putExtra(UPLOAD_NOW, false);
                intent2.putExtra(DATA_TYPE, DataTypeConstants.COMMON);
                intent2.putExtra(LOG_TAG, "atomReport");
                intent2.putExtra(EVENT_ID, "unusual_frequence_info");
                intent2.putExtra(LOG_MAP, logmap);
                startService(context, intent2);
                sCount = 0;
                LogUtil.e("addTaskForAtom too frequently");
                return;
            }
            sCount = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startService(Context context, Intent intent) {
        context.startService(intent);
    }
}

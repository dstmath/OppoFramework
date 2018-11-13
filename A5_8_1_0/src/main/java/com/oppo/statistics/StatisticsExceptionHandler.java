package com.oppo.statistics;

import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.util.LogUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

public class StatisticsExceptionHandler implements UncaughtExceptionHandler {
    private static final String APP_EXCEPTION = "APPException";
    private static final String EVENT_COUNT = "eventCount";
    private static final String EXCEPTION_DESCRIPTION = "description";
    private Context mContext;
    private UncaughtExceptionHandler mHandler = Thread.getDefaultUncaughtExceptionHandler();

    public StatisticsExceptionHandler(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void setStatisticsExceptionHandler() {
        if (this != this.mHandler) {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        LogUtil.d("NearMeStatistics", "StatisticsExceptionHandler: get the uncaughtException.");
        String exception = getStackTrace(e);
        if (!TextUtils.isEmpty(exception)) {
            Map logMap = new HashMap();
            logMap.put(EVENT_COUNT, String.valueOf(1));
            logMap.put(EXCEPTION_DESCRIPTION, exception);
            NearMeStatistics.onCommon(this.mContext, APP_EXCEPTION, APP_EXCEPTION, logMap);
        }
        if (this.mHandler != null) {
            this.mHandler.uncaughtException(t, e);
        }
    }

    private String getStackTrace(Throwable throwable) {
        String result = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
            result = sw.toString();
        } catch (Throwable e) {
            LogUtil.e("NearMeStatistics", e);
        } finally {
            pw.close();
        }
        return result;
    }
}

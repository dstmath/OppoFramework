package com.oppo.statistics;

import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.agent.ExceptionAgent;
import com.oppo.statistics.data.ExceptionBean;
import com.oppo.statistics.util.LogUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;

public class StatisticsExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context mContext;
    private Thread.UncaughtExceptionHandler mHandler = Thread.getDefaultUncaughtExceptionHandler();

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
        long currentTime = System.currentTimeMillis();
        if (!TextUtils.isEmpty(exception)) {
            ExceptionBean bean = new ExceptionBean();
            bean.setCount(1);
            bean.setEventTime(currentTime);
            bean.setException(exception);
            ExceptionAgent.recordException(this.mContext, bean);
        }
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = this.mHandler;
        if (uncaughtExceptionHandler != null) {
            uncaughtExceptionHandler.uncaughtException(t, e);
        }
    }

    private String getStackTrace(Throwable throwable) {
        String result = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
            result = sw.toString();
        } catch (Exception e) {
            LogUtil.e("NearMeStatistics", e);
        } catch (Throwable th) {
            pw.close();
            throw th;
        }
        pw.close();
        return result;
    }
}

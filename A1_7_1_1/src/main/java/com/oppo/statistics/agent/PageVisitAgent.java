package com.oppo.statistics.agent;

import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.NearMeStatistics;
import com.oppo.statistics.data.PageVisitBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.storage.PreferenceHandler;
import com.oppo.statistics.util.LogUtil;
import com.oppo.statistics.util.TimeInfoUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;

public class PageVisitAgent {
    private static final String CLIENT_START = "ClientStart";
    private static final int PAGE_VISIT_MAX_COUNT = 10;
    private static final int PAUSE = 1;
    private static final int RESUME = 0;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private final class HandlePageVisitRunnable implements Runnable {
        private String mClassName;
        private Context mContext;
        private long mCurrentTimeMills;
        private int mType;

        public HandlePageVisitRunnable(Context context, String className, long timeMillis, int type) {
            this.mContext = context;
            this.mClassName = className;
            this.mCurrentTimeMills = timeMillis;
            this.mType = type;
        }

        public void run() {
            switch (this.mType) {
                case 0:
                    PageVisitAgent.recordResume(this.mContext, this.mClassName, this.mCurrentTimeMills);
                    return;
                case 1:
                    PageVisitAgent.recordPause(this.mContext, this.mClassName, this.mCurrentTimeMills);
                    return;
                default:
                    return;
            }
        }
    }

    public void onPause(Context context) {
        if (context != null) {
            long currentTimeMills = System.currentTimeMillis();
            String className = getClassName(context);
            LogUtil.i("NearMeStatistics", "onPause: " + className);
            this.mExecutorService.execute(new HandlePageVisitRunnable(context, className, currentTimeMills, 1));
            return;
        }
        LogUtil.e("NearMeStatistics", "onPause() called without context.");
    }

    public void onResume(Context context) {
        if (context != null) {
            long currentTimeMills = System.currentTimeMillis();
            String className = getClassName(context);
            LogUtil.i("NearMeStatistics", "onResume: " + className);
            this.mExecutorService.execute(new HandlePageVisitRunnable(context, className, currentTimeMills, 0));
            return;
        }
        LogUtil.e("NearMeStatistics", "onPause() called without context.");
    }

    private static String getClassName(Context context) {
        String name = "";
        if (context != null) {
            return context.getClass().getSimpleName();
        }
        return name;
    }

    private static void recordPageVisit(Context context) {
        String routes = PreferenceHandler.getPageVisitRoutes(context);
        int duration = PreferenceHandler.getPageVisitDuration(context);
        if (!TextUtils.isEmpty(routes)) {
            PageVisitBean bean = new PageVisitBean();
            bean.setActivities(routes);
            bean.setDuration((long) duration);
            bean.setTime(TimeInfoUtil.getFormatTime());
            RecordHandler.addTask(context, bean);
        }
        PreferenceHandler.setPageVisitDuration(context, 0);
        PreferenceHandler.setPageVisitRoutes(context, "");
    }

    private static void recordPause(Context context, String className, long currentTime) {
        long startTime = PreferenceHandler.getActivityStartTime(context);
        int gap = (int) ((currentTime - startTime) / 1000);
        if (className.equals(PreferenceHandler.getCurrentActivity(context)) && gap >= 0 && -1 != startTime) {
            try {
                JSONArray totalRoutes;
                String routes = PreferenceHandler.getPageVisitRoutes(context);
                int duration = PreferenceHandler.getPageVisitDuration(context);
                if (TextUtils.isEmpty(routes)) {
                    totalRoutes = new JSONArray();
                } else {
                    totalRoutes = new JSONArray(routes);
                    if (totalRoutes.length() >= 10) {
                        recordPageVisit(context);
                        totalRoutes = new JSONArray();
                    }
                }
                JSONArray newRoutes = new JSONArray();
                newRoutes.put(className);
                newRoutes.put(gap);
                duration += gap;
                totalRoutes.put(newRoutes);
                PreferenceHandler.setPageVisitDuration(context, duration);
                PreferenceHandler.setPageVisitRoutes(context, totalRoutes.toString());
            } catch (Throwable e) {
                LogUtil.e("NearMeStatistics", e);
            } catch (Throwable e1) {
                LogUtil.e("NearMeStatistics", e1);
                PreferenceHandler.setPageVisitRoutes(context, "");
                PreferenceHandler.setPageVisitDuration(context, 0);
            }
        }
        PreferenceHandler.setActivityEndTime(context, currentTime);
    }

    private static void recordResume(Context context, String className, long currentTime) {
        long endTime = PreferenceHandler.getActivityEndTime(context);
        long startTime = PreferenceHandler.getActivityStartTime(context);
        long timeout = ((long) PreferenceHandler.getSessionTimeout(context)) * 1000;
        LogUtil.d(LogUtil.TAG, "recordResume ...");
        LogUtil.d(LogUtil.TAG, "currentTime:" + currentTime + ", startTime:" + startTime + ", timeout:" + timeout + ", endTime:" + endTime);
        if (currentTime - startTime >= timeout && (-1 == endTime || endTime >= currentTime || currentTime - endTime >= timeout)) {
            NearMeStatistics.onCommon(context, CLIENT_START, CLIENT_START, null, false);
            PreferenceHandler.setPageVisitStartTime(context, System.currentTimeMillis());
        }
        PreferenceHandler.setActivityStartTime(context, currentTime);
        PreferenceHandler.setCurrentActivity(context, className);
    }
}

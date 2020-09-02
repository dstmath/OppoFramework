package com.oppo.statistics.agent;

import android.content.Context;
import android.text.TextUtils;
import com.oppo.statistics.data.PageVisitBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.storage.PreferenceHandler;
import com.oppo.statistics.util.LogUtil;
import com.oppo.statistics.util.TimeInfoUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;

public class PageVisitAgent {
    private static final int PAGE_VISIT_MAX_COUNT = 10;
    private static final int PAUSE = 1;
    private static final int RESUME = 0;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

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
        if (context != null) {
            return context.getClass().getSimpleName();
        }
        return "";
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

    /* access modifiers changed from: private */
    public static void recordPause(Context context, String className, long currentTime) {
        JSONArray totalRoutes;
        long startTime = PreferenceHandler.getActivityStartTime(context);
        int gap = (int) ((currentTime - startTime) / 1000);
        if (className.equals(PreferenceHandler.getCurrentActivity(context)) && gap >= 0 && -1 != startTime) {
            try {
                String routes = PreferenceHandler.getPageVisitRoutes(context);
                int duration = PreferenceHandler.getPageVisitDuration(context);
                if (!TextUtils.isEmpty(routes)) {
                    totalRoutes = new JSONArray(routes);
                    if (totalRoutes.length() >= 10) {
                        recordPageVisit(context);
                        totalRoutes = new JSONArray();
                    }
                } else {
                    totalRoutes = new JSONArray();
                }
                JSONArray newRoutes = new JSONArray();
                newRoutes.put(className);
                newRoutes.put(gap);
                totalRoutes.put(newRoutes);
                PreferenceHandler.setPageVisitDuration(context, duration + gap);
                PreferenceHandler.setPageVisitRoutes(context, totalRoutes.toString());
            } catch (JSONException e) {
                LogUtil.e("NearMeStatistics", e);
            } catch (Exception e1) {
                LogUtil.e("NearMeStatistics", e1);
                PreferenceHandler.setPageVisitRoutes(context, "");
                PreferenceHandler.setPageVisitDuration(context, 0);
            }
        }
        PreferenceHandler.setActivityEndTime(context, currentTime);
    }

    /* access modifiers changed from: private */
    public static void recordResume(Context context, String className, long currentTime) {
        long endTime = PreferenceHandler.getActivityEndTime(context);
        long startTime = PreferenceHandler.getActivityStartTime(context);
        long timeout = ((long) PreferenceHandler.getSessionTimeout(context)) * 1000;
        if (currentTime - startTime >= timeout && (-1 == endTime || endTime >= currentTime || currentTime - endTime >= timeout)) {
            AppStartAgent.recordAppStart(context);
            PreferenceHandler.setPageVisitStartTime(context, System.currentTimeMillis());
            recordPageVisit(context);
        }
        PreferenceHandler.setActivityStartTime(context, currentTime);
        PreferenceHandler.setCurrentActivity(context, className);
    }

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
            int i = this.mType;
            if (i == 0) {
                PageVisitAgent.recordResume(this.mContext, this.mClassName, this.mCurrentTimeMills);
            } else if (i == 1) {
                PageVisitAgent.recordPause(this.mContext, this.mClassName, this.mCurrentTimeMills);
            }
        }
    }
}

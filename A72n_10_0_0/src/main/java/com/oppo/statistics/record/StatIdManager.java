package com.oppo.statistics.record;

import android.content.Context;
import android.util.Log;
import com.oppo.statistics.storage.PreferenceHandler;
import java.util.UUID;

public class StatIdManager {
    public static final long EXPIRE_TIME_MS = 30000;
    private static final String SP_KEY_APP_EXIT_TIME = "AppExitTime";
    private static final String SP_KEY_APP_SESSION_ID = "AppSessionId";
    private String mAppSessionId;
    private long mExitAppTime;

    private StatIdManager() {
        this.mAppSessionId = null;
        this.mExitAppTime = 0;
    }

    /* access modifiers changed from: private */
    public static class Holder {
        private static final StatIdManager INSTANCE = new StatIdManager();

        private Holder() {
        }
    }

    public static StatIdManager getInstance() {
        return Holder.INSTANCE;
    }

    public String getAppSessionId(Context context) {
        if (this.mAppSessionId == null) {
            refreshAppSessionIdIfNeed(context);
        }
        return this.mAppSessionId;
    }

    public void onAppExit(Context context) {
        this.mExitAppTime = System.currentTimeMillis();
        setAppExitTime2Sp(context, this.mExitAppTime);
    }

    public void refreshAppSessionIdIfNeed(Context context) {
        Log.d("DCS-", "test refreshAppSessionIdIfNeed");
        if (!isAppSessionIdFresh(context)) {
            refreshAppSessionId(context);
        } else {
            this.mAppSessionId = getAppSessionIdFromSp(context);
        }
    }

    public void refreshAppSessionId(Context context) {
        this.mAppSessionId = buildSessionId();
        setAppSessionId2Sp(context, this.mAppSessionId);
    }

    private String buildSessionId() {
        return UUID.randomUUID().toString();
    }

    private boolean isAppSessionIdFresh(Context context) {
        if (this.mExitAppTime == 0) {
            this.mExitAppTime = getAppLastExitTimeFromSp(context);
        }
        long timeGap = System.currentTimeMillis() - this.mExitAppTime;
        Log.d("DCS-", "test isAppSessionIdFresh timeGap=" + timeGap);
        return timeGap > 0 && timeGap < EXPIRE_TIME_MS;
    }

    private void setAppExitTime2Sp(Context context, long time) {
        PreferenceHandler.setLong(context, SP_KEY_APP_EXIT_TIME, time);
    }

    private long getAppLastExitTimeFromSp(Context context) {
        return PreferenceHandler.getLong(context, SP_KEY_APP_EXIT_TIME, 0);
    }

    private void setAppSessionId2Sp(Context context, String appSessionId) {
        PreferenceHandler.setString(context, SP_KEY_APP_SESSION_ID, appSessionId);
    }

    private String getAppSessionIdFromSp(Context context) {
        return PreferenceHandler.getString(context, SP_KEY_APP_SESSION_ID, "");
    }
}

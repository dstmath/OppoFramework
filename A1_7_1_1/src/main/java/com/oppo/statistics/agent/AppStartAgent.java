package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.AppStartBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.util.LogUtil;
import com.oppo.statistics.util.TimeInfoUtil;

public class AppStartAgent {
    public static void recordAppStart(Context context) {
        LogUtil.i("NearMeStatistics", "调用AppStart");
        RecordHandler.addTask(context, new AppStartBean(TimeInfoUtil.getFormatTime()));
    }
}

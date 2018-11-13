package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.SpecialAppStartBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.util.AccountUtil;
import com.oppo.statistics.util.TimeInfoUtil;

public class SpecialAppStartAgent {
    public static void onSpecialAppStart(Context context, int appId) {
        RecordHandler.addTask(context, new SpecialAppStartBean(AccountUtil.getSsoId(context), TimeInfoUtil.getFormatTime(), appId));
    }
}

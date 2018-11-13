package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.UserActionBean;
import com.oppo.statistics.record.RecordHandler;
import com.oppo.statistics.util.TimeInfoUtil;

public class UserActionAgent {
    public static void recordUserAction(Context context, int actCode, int actionMount) {
        RecordHandler.addTask(context, new UserActionBean(actCode, TimeInfoUtil.getFormatHour(), actionMount));
    }
}

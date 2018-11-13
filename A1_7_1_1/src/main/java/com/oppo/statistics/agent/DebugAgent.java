package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.DebugBean;
import com.oppo.statistics.record.RecordHandler;

public class DebugAgent {
    public static void setDebug(Context context, boolean flags) {
        RecordHandler.addTask(context, new DebugBean(flags));
    }
}

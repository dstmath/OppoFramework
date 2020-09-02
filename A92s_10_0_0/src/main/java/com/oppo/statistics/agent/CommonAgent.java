package com.oppo.statistics.agent;

import android.content.Context;
import com.oppo.statistics.data.CommonBean;
import com.oppo.statistics.record.RecordHandler;
import java.util.Map;

public class CommonAgent extends BaseAgent {
    public static void recordCommon(Context context, String appId, String logTag, String eventID, Map<String, String> logMap, boolean uploadNow) {
        CommonBean bean = new CommonBean(logTag, eventID, map2JsonObject(logMap).toString(), uploadNow);
        bean.setAppId(appId);
        RecordHandler.addTask(context, bean);
    }
}

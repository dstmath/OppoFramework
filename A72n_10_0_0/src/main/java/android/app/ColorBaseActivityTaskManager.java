package android.app;

import android.content.Context;
import oppo.app.OppoCommonManager;

public abstract class ColorBaseActivityTaskManager extends OppoCommonManager {
    public ColorBaseActivityTaskManager() {
        super(Context.ACTIVITY_TASK_SERVICE);
    }
}

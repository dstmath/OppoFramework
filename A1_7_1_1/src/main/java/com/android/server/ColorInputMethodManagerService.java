package com.android.server;

import android.content.Context;
import android.os.ResultReceiver;
import com.android.internal.view.IInputMethodClient;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorScreenshotManager;

public class ColorInputMethodManagerService extends InputMethodManagerService {
    public ColorInputMethodManagerService(Context context) {
        super(context);
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (isLongshotMode()) {
            return hideCurrentInputLocked(flags, resultReceiver);
        }
        return super.hideSoftInput(client, flags, resultReceiver);
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        if (isLongshotMode()) {
            return false;
        }
        return super.showCurrentInputLocked(flags, resultReceiver);
    }

    private boolean isLongshotMode() {
        ColorScreenshotManager sm = ColorLongshotUtils.getScreenshotManager(this.mContext);
        if (sm != null) {
            return sm.isLongshotMode();
        }
        return false;
    }
}

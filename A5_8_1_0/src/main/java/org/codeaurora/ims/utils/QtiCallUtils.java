package org.codeaurora.ims.utils;

import android.content.Context;
import android.provider.Settings.Global;
import org.codeaurora.ims.QtiCallConstants;

public class QtiCallUtils {
    private QtiCallUtils() {
    }

    public static boolean isCsRetryEnabledByUser(Context context) {
        if (Global.getInt(context.getContentResolver(), QtiCallConstants.IMS_TO_CS_RETRY_ENABLED, 1) == 1) {
            return true;
        }
        return false;
    }
}

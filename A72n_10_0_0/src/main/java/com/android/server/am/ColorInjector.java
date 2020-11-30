package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.view.IColorAccidentallyTouchHelper;
import com.android.server.wm.ColorInputMethodKeyboardPositionManager;
import com.color.darkmode.IColorDarkModeManager;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorReflectDataUtils;
import com.color.util.ColorSecureKeyboardUtils;
import com.color.widget.ColorResolveInfoHelper;

class ColorInjector {
    ColorInjector() {
    }

    static class ActivityManagerService {
        ActivityManagerService() {
        }

        static void init(ActivityManagerService service) {
            Context context = service.mContext;
            OppoFeatureCache.getOrCreate(IColorAccidentallyTouchHelper.DEFAULT, new Object[0]).initOnAmsReady();
            ColorDisplayCompatUtils.getInstance().init(context);
            ColorReflectDataUtils.getInstance().init();
            ColorSecureKeyboardUtils.getInstance().init(context);
            ColorResolveInfoHelper.getInstance(context).init();
            OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0]).init(context);
            ColorInputMethodKeyboardPositionManager.getInstance(context).init();
        }
    }
}

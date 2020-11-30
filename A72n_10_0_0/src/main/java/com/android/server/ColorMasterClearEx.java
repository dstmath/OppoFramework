package com.android.server;

import android.content.Context;
import com.android.server.pm.IColorFontManagerService;

public class ColorMasterClearEx extends ColorDummyMasterClearEx {
    public ColorMasterClearEx(Context context) {
        super(context);
    }

    public void run() {
        IColorFontManagerService fontManagerService = (IColorFontManagerService) ColorLocalServices.getService(IColorFontManagerService.class);
        if (fontManagerService != null) {
            fontManagerService.handleFactoryReset();
        }
    }
}

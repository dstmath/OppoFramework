package com.android.server.wm;

import android.content.Context;
import android.os.Message;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyWindowManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoWindowManagerServiceEx {
    protected final WindowManagerService mWms;

    public OppoDummyWindowManagerServiceEx(Context context, WindowManagerService wms) {
        super(context);
        this.mWms = wms;
    }

    @Override // com.android.server.wm.IOppoWindowManagerServiceEx
    public WindowManagerService getWindowManagerService() {
        return this.mWms;
    }

    @Override // com.android.server.wm.IOppoWindowManagerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }
}

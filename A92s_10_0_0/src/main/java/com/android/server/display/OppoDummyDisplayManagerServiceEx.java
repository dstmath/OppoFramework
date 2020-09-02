package com.android.server.display;

import android.content.Context;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyDisplayManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoDisplayManagerServiceEx {
    protected final DisplayManagerService mDms;

    public OppoDummyDisplayManagerServiceEx(Context context, DisplayManagerService displayManagerService) {
        super(context);
        this.mDms = displayManagerService;
    }

    @Override // com.android.server.display.IOppoDisplayManagerServiceEx
    public DisplayManagerService getDisplayManagerService() {
        return this.mDms;
    }
}

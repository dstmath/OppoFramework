package com.android.server.display;

import android.content.Context;
import android.os.Bundle;

public class ColorDummyDisplayManagerServiceEx extends OppoDummyDisplayManagerServiceEx implements IColorDisplayManagerServiceEx {
    private static final String TAG = "ColorDummyDisplayManagerServiceEx";

    public ColorDummyDisplayManagerServiceEx(Context context, DisplayManagerService displayManagerService) {
        super(context, displayManagerService);
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void onStart() {
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
    }

    @Override // com.android.server.display.IColorDisplayManagerServiceEx
    public boolean setStateChanged(int msgId, Bundle extraData) {
        return false;
    }
}

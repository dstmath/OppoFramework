package com.android.server;

import android.content.Context;

public class ColorDummyMasterClearEx implements IColorMasterClearEx {
    protected final Context mContext;

    public ColorDummyMasterClearEx(Context context) {
        this.mContext = context;
    }

    @Override // com.android.server.IOppoMasterClearEx
    public void run() {
    }
}

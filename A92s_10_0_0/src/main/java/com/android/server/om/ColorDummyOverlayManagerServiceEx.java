package com.android.server.om;

import android.content.Context;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class ColorDummyOverlayManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IColorOverlayManagerServiceEx {
    public ColorDummyOverlayManagerServiceEx(Context context) {
        super(context);
    }

    @Override // com.android.server.om.IColorOverlayManagerServiceEx
    public void init() {
    }
}

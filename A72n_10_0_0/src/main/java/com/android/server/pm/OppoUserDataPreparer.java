package com.android.server.pm;

import android.content.Context;

public class OppoUserDataPreparer extends UserDataPreparer {
    OppoUserDataPreparer(Installer installer, Object installLock, Context context, boolean onlyCore) {
        super(installer, installLock, context, onlyCore);
    }
}

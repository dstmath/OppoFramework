package com.android.server.am;

import android.content.Context;

public class PswDummyActivityManagerServiceEx extends OppoDummyActivityManagerServiceEx implements IPswActivityManagerServiceEx {
    public PswDummyActivityManagerServiceEx(Context context, ActivityManagerService ams) {
        super(context, ams);
    }
}

package com.android.server.am;

import android.os.Binder;
import com.android.internal.os.OppoBatteryStatsImpl;

class OppoBaseServiceRecord extends Binder {
    public OppoBatteryStatsImpl.Uid.Pkg.Serv oppostats;

    OppoBaseServiceRecord() {
    }
}

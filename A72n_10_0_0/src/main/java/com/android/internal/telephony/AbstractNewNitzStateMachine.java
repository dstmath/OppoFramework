package com.android.internal.telephony;

import android.util.TimestampedValue;

public abstract class AbstractNewNitzStateMachine {
    protected IOppoNewNitzStateMachine mReference;

    public void OppoisUseNtptime(long currenttimeinmillis, long millisSinceNitzReceived, long ReferenceTimeMillis, TimestampedValue<Long> Time) {
        this.mReference.OppoisUseNtptime(currenttimeinmillis, millisSinceNitzReceived, ReferenceTimeMillis, Time);
    }

    public void OppoSetUseNtpTime(String name, String value) {
        this.mReference.OppoSetUseNtpTime(name, value);
    }

    public String OppoGetTimeZonesWithCapitalCity(String iso) {
        return this.mReference.OppoGetTimeZonesWithCapitalCity(iso);
    }

    public void OppoRecordNitzTimeZone(int settingType, String zoneId) {
        this.mReference.OppoRecordNitzTimeZone(settingType, zoneId);
    }
}

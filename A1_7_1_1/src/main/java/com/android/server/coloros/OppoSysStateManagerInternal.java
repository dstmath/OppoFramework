package com.android.server.coloros;

public abstract class OppoSysStateManagerInternal {
    public abstract void noteStartSensor(int i, int i2);

    public abstract void noteStopSensor(int i, int i2);

    public abstract void onPlugChanged(int i);

    public abstract void onWakefulnessChanged(int i);
}

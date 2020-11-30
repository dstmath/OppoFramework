package com.android.server.wm;

import android.os.Bundle;
import android.os.IBinder;
import com.oppo.screenmode.IOppoScreenMode;
import com.oppo.screenmode.IOppoScreenModeCallback;

public class IOppoScreenModeHook extends IOppoScreenMode.Stub {
    public void addCallback(IOppoScreenModeCallback callback) {
    }

    public void remove(IOppoScreenModeCallback callback) {
    }

    public void setClientRefreshRate(IBinder token, int rate) {
    }

    public boolean requestRefreshRate(boolean open, int rate) {
        return false;
    }

    public boolean supportDisplayCompat(String pkg, int uid) {
        return false;
    }

    public boolean setHighTemperatureStatus(int status, int rate) {
        return false;
    }

    public void enterDCAndLowBrightnessMode(boolean enter) {
    }

    public boolean isDisplayCompat(String packageName, int uid) {
        return false;
    }

    public void enterPSMode(boolean enter) {
    }

    public void enterPSModeOnRate(boolean enter, int rate) {
    }

    public boolean requestGameRefreshRate(String packageName, int rate) {
        return false;
    }

    public boolean requestRefreshRateWithToken(boolean open, int rate, IBinder token) {
        return false;
    }

    public boolean getGameList(Bundle outBundle) {
        return false;
    }

    public String getDisableOverrideViewList(String key) {
        return null;
    }

    public void enterPSModeOnRateWithToken(boolean open, int rate, IBinder token) {
    }
}

package com.mediatek.boostframework;

import android.util.Log;
import com.mediatek.powerhalwrapper.PowerHalWrapper;

public class Performance {
    private static final String TAG = "Performance";
    private static PowerHalWrapper mPowerHalWrap = null;
    private int mhandle = 0;

    public Performance() {
        mPowerHalWrap = PowerHalWrapper.getInstance();
    }

    public int perfchecklist(int... list) {
        return list.length % 2 == 0 ? 1 : 0;
    }

    public int perfLockAcquire(int duration, int... list) {
        if (perfchecklist(list) == 0) {
            return -1;
        }
        this.mhandle = mPowerHalWrap.perfLockAcquire(this.mhandle, duration, list);
        return this.mhandle;
    }

    public int perfLockRelease() {
        mPowerHalWrap.perfLockRelease(this.mhandle);
        return 0;
    }

    public int perfLockRelease(int handle) {
        mPowerHalWrap.perfLockRelease(handle);
        return 0;
    }

    private static void log(String info) {
        Log.d("@M_Performance", "[Performance] " + info + " ");
    }

    private static void loge(String info) {
        Log.e("@M_Performance", "[Performance] ERR: " + info + " ");
    }
}

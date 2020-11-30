package com.mediatek.server;

import android.util.Slog;
import com.android.server.power.ShutdownThread;
import com.mediatek.server.am.AmsExt;
import com.mediatek.server.anr.AnrManager;
import com.mediatek.server.pm.PmsExt;
import com.mediatek.server.powerhal.PowerHalManager;
import com.mediatek.server.ppl.MtkPplManager;
import com.mediatek.server.wm.WindowManagerDebugger;
import com.mediatek.server.wm.WmsExt;

public class MtkSystemServiceFactory {
    private static Object lock = new Object();
    private static MtkSystemServiceFactory sInstance;
    private WmsExt mWmsExt = new WmsExt();

    public static MtkSystemServiceFactory getInstance() {
        if (sInstance == null) {
            try {
                sInstance = (MtkSystemServiceFactory) Class.forName("com.mediatek.server.MtkSystemServiceFactoryImpl", false, MtkSystemServer.sClassLoader).getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception e) {
                Slog.e("MtkSystemServiceFactory", "getInstance: " + e.toString());
                sInstance = new MtkSystemServiceFactory();
            }
        }
        return sInstance;
    }

    public AnrManager makeAnrManager() {
        return new AnrManager();
    }

    public ShutdownThread makeMtkShutdownThread() {
        return new ShutdownThread();
    }

    public PmsExt makePmsExt() {
        return new PmsExt();
    }

    public PowerHalManager makePowerHalManager() {
        return new PowerHalManager();
    }

    public MtkPplManager makeMtkPplManager() {
        return new MtkPplManager();
    }

    public AmsExt makeAmsExt() {
        return new AmsExt();
    }

    public WindowManagerDebugger makeWindowManagerDebugger() {
        return new WindowManagerDebugger();
    }

    public WmsExt makeWmsExt() {
        return this.mWmsExt;
    }
}

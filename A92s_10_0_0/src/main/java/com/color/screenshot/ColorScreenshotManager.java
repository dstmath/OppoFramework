package com.color.screenshot;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.color.content.ColorContext;
import com.color.screenshot.IColorScreenshotManager;
import com.color.util.ColorLog;

public final class ColorScreenshotManager {
    private static final boolean DBG = ColorLongshotDump.DBG;
    public static final String GLOBAL_ACTION_VISIBLE = "global_action_visible";
    public static final String NAVIGATIONBAR_VISIBLE = "navigationbar_visible";
    public static final String SCREENSHOT_DIRECTION = "screenshot_direction";
    public static final String SCREENSHOT_ORIENTATION = "screenshot_orientation";
    public static final String SCREENSHOT_SOURCE = "screenshot_source";
    public static final String STATUSBAR_VISIBLE = "statusbar_visible";
    private static final String TAG = "LongshotDump";
    private static volatile ColorScreenshotManager sInstance = null;
    private final IColorScreenshotManager mService = IColorScreenshotManager.Stub.asInterface(ServiceManager.getService(ColorContext.SCREENSHOT_SERVICE));

    private ColorScreenshotManager() {
    }

    public static ColorScreenshotManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorScreenshotManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorScreenshotManager();
                }
            }
        }
        return sInstance;
    }

    public static ColorScreenshotManager peekInstance() {
        return sInstance;
    }

    public void takeScreenshot(Bundle extras) {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.takeScreenshot(extras);
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "takeScreenshot : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isScreenshotMode() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return false;
        }
        try {
            return iColorScreenshotManager.isScreenshotMode();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isScreenshotMode : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isScreenshotEdit() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return false;
        }
        try {
            return iColorScreenshotManager.isScreenshotEdit();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isScreenshotEdit : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public void takeLongshot(boolean statusBarVisible, boolean navBarVisible) {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.takeLongshot(statusBarVisible, navBarVisible);
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "takeLongshot : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public void stopLongshot() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.stopLongshot();
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "stopLongshot : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isLongshotMode() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return false;
        }
        try {
            return iColorScreenshotManager.isLongshotMode();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isLongshotMode : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isLongshotDisabled() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return false;
        }
        try {
            return iColorScreenshotManager.isLongshotDisabled();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isLongshotDisabled : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public void reportLongshotDumpResult(ColorLongshotDump result) {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.reportLongshotDumpResult(result);
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "reportLongshotDumpResult : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isScreenshotSupported() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return true;
        }
        try {
            return iColorScreenshotManager.isScreenshotSupported();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isScreenshotSupported : " + e.toString());
            return true;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return true;
        }
    }

    public void setScreenshotEnabled(boolean enabled) {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.setScreenshotEnabled(enabled);
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "setScreenshotEnabled : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isScreenshotEnabled() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return true;
        }
        try {
            return iColorScreenshotManager.isScreenshotEnabled();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isScreenshotEnabled : " + e.toString());
            return true;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return true;
        }
    }

    public void setLongshotEnabled(boolean enabled) {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.setLongshotEnabled(enabled);
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "setLongshotEnabled : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isLongshotEnabled() {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager == null) {
            return true;
        }
        try {
            return iColorScreenshotManager.isLongshotEnabled();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isLongshotEnabled : " + e.toString());
            return true;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return true;
        }
    }

    @Deprecated
    public void notifyOverScroll(ColorLongshotEvent event) {
        IColorScreenshotManager iColorScreenshotManager = this.mService;
        if (iColorScreenshotManager != null) {
            try {
                iColorScreenshotManager.notifyOverScroll(event);
            } catch (RemoteException e) {
                boolean z = DBG;
                ColorLog.e(z, "LongshotDump", "notifyOverScroll : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }
}

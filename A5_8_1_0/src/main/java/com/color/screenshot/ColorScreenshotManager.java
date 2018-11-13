package com.color.screenshot;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.color.content.ColorContext;
import com.color.screenshot.IColorScreenshotManager.Stub;
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
    private final IColorScreenshotManager mService = Stub.asInterface(ServiceManager.getService(ColorContext.SCREENSHOT_SERVICE));

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
        if (this.mService != null) {
            try {
                this.mService.takeScreenshot(extras);
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "takeScreenshot : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isScreenshotMode() {
        boolean result = false;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.isScreenshotMode();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isScreenshotMode : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public boolean isScreenshotEdit() {
        boolean result = false;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.isScreenshotEdit();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isScreenshotEdit : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public void takeLongshot(boolean statusBarVisible, boolean navBarVisible) {
        if (this.mService != null) {
            try {
                this.mService.takeLongshot(statusBarVisible, navBarVisible);
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "takeLongshot : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public void stopLongshot() {
        if (this.mService != null) {
            try {
                this.mService.stopLongshot();
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "stopLongshot : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isLongshotMode() {
        boolean result = false;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.isLongshotMode();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isLongshotMode : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public boolean isLongshotDisabled() {
        boolean result = false;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.isLongshotDisabled();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isLongshotDisabled : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public void reportLongshotDumpResult(ColorLongshotDump result) {
        if (this.mService != null) {
            try {
                this.mService.reportLongshotDumpResult(result);
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "reportLongshotDumpResult : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public void setScreenshotEnabled(boolean enabled) {
        if (this.mService != null) {
            try {
                this.mService.setScreenshotEnabled(enabled);
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "setScreenshotEnabled : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isScreenshotEnabled() {
        boolean result = true;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.isScreenshotEnabled();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isScreenshotEnabled : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public void setLongshotEnabled(boolean enabled) {
        if (this.mService != null) {
            try {
                this.mService.setLongshotEnabled(enabled);
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "setLongshotEnabled : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }

    public boolean isLongshotEnabled() {
        boolean result = true;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.isLongshotEnabled();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isLongshotEnabled : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public void notifyOverScroll(ColorLongshotEvent event) {
        if (this.mService != null) {
            try {
                this.mService.notifyOverScroll(event);
            } catch (RemoteException e) {
                ColorLog.e(DBG, "LongshotDump", "notifyOverScroll : " + e.toString());
            } catch (Exception e2) {
                ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            }
        }
    }
}

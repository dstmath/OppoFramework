package com.android.server.am;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class ColorKeyLayoutManagerUtil {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final int DELAY = 90000;
    private static final String GIMBAL_LAUNCH_PKG_FILE_PATH = "/data/oppo/coloros/keylayout/gimbal_launch_pkg_file";
    private static final String TAG = "ColorKeyLayoutManagerUtil";
    private static volatile ColorKeyLayoutManagerUtil sInstance = null;
    private String mGimbalLaunchPkg = "";

    private ColorKeyLayoutManagerUtil() {
        init();
    }

    public static ColorKeyLayoutManagerUtil getInstance() {
        if (sInstance == null) {
            synchronized (ColorKeyLayoutManagerUtil.class) {
                if (sInstance == null) {
                    sInstance = new ColorKeyLayoutManagerUtil();
                }
            }
        }
        return sInstance;
    }

    private void init() {
        new Timer().schedule(new TimerTask() {
            /* class com.android.server.am.ColorKeyLayoutManagerUtil.AnonymousClass1 */

            public void run() {
                File gimbalLaunchPkgFile = new File(ColorKeyLayoutManagerUtil.GIMBAL_LAUNCH_PKG_FILE_PATH);
                if (!gimbalLaunchPkgFile.exists()) {
                    Slog.w(ColorKeyLayoutManagerUtil.TAG, "init failed, has no gimbal launch package file: /data/oppo/coloros/keylayout/gimbal_launch_pkg_file");
                    return;
                }
                BufferedReader reader = null;
                InputStreamReader isr = null;
                try {
                    StringBuilder fileData = new StringBuilder();
                    InputStreamReader isr2 = new InputStreamReader(new FileInputStream(gimbalLaunchPkgFile), StandardCharsets.UTF_8);
                    BufferedReader reader2 = new BufferedReader(isr2);
                    char[] buf = new char[4096];
                    while (true) {
                        int numRead = reader2.read(buf);
                        if (numRead == -1) {
                            break;
                        }
                        fileData.append(String.valueOf(buf, 0, numRead));
                    }
                    reader2.close();
                    ColorKeyLayoutManagerUtil.this.mGimbalLaunchPkg = fileData.toString().trim();
                    if (ColorKeyLayoutManagerUtil.DEBUG) {
                        Slog.d(ColorKeyLayoutManagerUtil.TAG, "mGimbalLaunchPkg: " + ColorKeyLayoutManagerUtil.this.mGimbalLaunchPkg);
                    }
                    try {
                        reader2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        isr2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                } catch (Exception e3) {
                    Slog.e(ColorKeyLayoutManagerUtil.TAG, "init failed to read data from /data/oppo/coloros/keylayout/gimbal_launch_pkg_file");
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (0 != 0) {
                        isr.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            reader.close();
                        } catch (Exception e5) {
                            e5.printStackTrace();
                        }
                    }
                    if (0 != 0) {
                        try {
                            isr.close();
                        } catch (Exception e6) {
                            e6.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }, 90000);
    }

    public String getGimbalLaunchPkg() {
        if (DEBUG) {
            Slog.d(TAG, "getGimbalLaunchPkg " + this.mGimbalLaunchPkg);
        }
        return this.mGimbalLaunchPkg;
    }

    public void setGimbalLaunchPkg(String pkgName) {
        if (DEBUG) {
            Slog.d(TAG, "setGimbalLaunchPkg " + pkgName);
        }
        if (pkgName != null) {
            this.mGimbalLaunchPkg = pkgName;
        }
    }
}

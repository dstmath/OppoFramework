package com.android.server.power;

import android.app.ActivityManager;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.ColorWindowManager;
import com.android.server.am.ColorHansRestriction;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.app.ColorAppInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
/* compiled from: ColorWakeLockCheck */
public class CommonUtil {
    private static final boolean ADBG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) ADBG);
    private static final String ATAG = "OppoWakeLockCheck";
    private ActivityManager mActivityManager = null;
    private AudioManager mAudioManager = null;
    private final ColorWindowManager mColorWms;
    private final Context mContext;
    private OppoActivityManager mOppoAm = new OppoActivityManager();
    private IPackageManager mPkm = null;

    public CommonUtil(Context context) {
        this.mContext = context;
        this.mColorWms = new ColorWindowManager();
    }

    public String[] getActiveAudioPids() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
            if (this.mAudioManager == null) {
                return null;
            }
        }
        return getActiveAudioPids(this.mAudioManager.getParameters("get_pid"));
    }

    private String[] getActiveAudioPids(String pids) {
        if (pids == null || pids.length() == 0 || !pids.contains(":")) {
            return null;
        }
        return pids.split(":");
    }

    public ActivityManager.RunningAppProcessInfo getProcessForPid(String pid) {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
            if (this.mActivityManager == null) {
                return null;
            }
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : this.mActivityManager.getRunningAppProcesses()) {
            if (processInfo.pid == Integer.valueOf(pid).intValue()) {
                return processInfo;
            }
        }
        return null;
    }

    public String getPkgNameForUid(int uid) {
        try {
            return getIPackageManager().getNameForUid(uid);
        } catch (Exception e) {
            Slog.w("OppoWakeLockCheck", "getNameForUid exception");
            return null;
        }
    }

    public String[] getPackagesForUid(int uid) {
        try {
            return getIPackageManager().getPackagesForUid(uid);
        } catch (Exception e) {
            Slog.w("OppoWakeLockCheck", "getPackagesForUid exception");
            return null;
        }
    }

    public String getAppLabel(String packageName, int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        ApplicationInfo app = null;
        if (uid < 10000 || pm == null) {
            return "";
        }
        try {
            app = pm.getApplicationInfo(packageName, ColorHansRestriction.HANS_RESTRICTION_BLOCK_BINDER);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (app != null) {
            String label = pm.getApplicationLabel(app).toString();
            return "" + label;
        } else if (!ADBG) {
            return "";
        } else {
            Slog.e("OppoWakeLockCheck", "error happened when  getApplicationInfo from " + packageName);
            return "";
        }
    }

    public String[] getPkgsForUid(int uid) {
        try {
            return getIPackageManager().getPackagesForUid(uid);
        } catch (Exception e) {
            Slog.w("OppoWakeLockCheck", "getPackagesForUid exception");
            return null;
        }
    }

    public String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Slog.w("OppoWakeLockCheck", "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    public List<String> getAllTopPkgName() {
        try {
            OppoActivityManager mOppoAms = new OppoActivityManager();
            List<String> listTopPkg = new ArrayList<>();
            List<ColorAppInfo> listTopPkgTmp = mOppoAms.getAllTopAppInfos();
            if (listTopPkgTmp == null) {
                return null;
            }
            for (int i = 0; i < listTopPkgTmp.size(); i++) {
                String pkg = listTopPkgTmp.get(i).appInfo.packageName;
                if (pkg != null && !"".equals(pkg)) {
                    listTopPkg.add(pkg);
                }
            }
            Slog.i("OppoWakeLockCheck", "getAllTopPkgName: listTopPkg=" + listTopPkg + ", listTopPkgTmp=" + listTopPkgTmp);
            return listTopPkg;
        } catch (Exception e) {
            Slog.w("OppoWakeLockCheck", "getAllTopPkgName exception");
            return null;
        }
    }

    public int getUidForPkgName(String pkgName) {
        try {
            return getIPackageManager().getPackageUid(pkgName, 8192, 0);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public String getTopAppName() {
        ComponentName cn = null;
        try {
            cn = this.mOppoAm.getTopActivityComponentName();
        } catch (RemoteException e) {
        }
        return cn != null ? cn.getPackageName() : "";
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008b, code lost:
        if (0 == 0) goto L_0x008e;
     */
    public String getSurfceLayers() {
        String layers = "";
        BufferedReader reader = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("dumpsys SurfaceFlinger --list");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String lineText = reader2.readLine();
                if (lineText == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        Slog.e("OppoWakeLockCheck", "failed closing reader");
                    }
                } else if (!lineText.equals("FocusedStackFrame") && !lineText.equals("DimLayer") && !lineText.equals("TickerPanel") && !lineText.equals("Magnification Overlay")) {
                    if (!lineText.equals("AssertTip")) {
                        layers = layers.concat("  " + lineText);
                    }
                }
            }
            reader2.close();
        } catch (IOException e2) {
            Slog.e("OppoWakeLockCheck", "dumpsys SurfaceFlinger --list IOException");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Slog.e("OppoWakeLockCheck", "failed closing reader");
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    Slog.e("OppoWakeLockCheck", "failed closing reader");
                }
            }
            if (0 != 0) {
                process.destroy();
            }
            throw th;
        }
        process.destroy();
        return layers;
    }

    private IPackageManager getIPackageManager() {
        IPackageManager iPackageManager = this.mPkm;
        if (iPackageManager != null) {
            return iPackageManager;
        }
        this.mPkm = IPackageManager.Stub.asInterface(ServiceManager.getService(BrightnessConstants.AppSplineXml.TAG_PACKAGE));
        return this.mPkm;
    }

    public boolean isWindowShownForUid(int uid) {
        try {
            return this.mColorWms.isWindowShownForUid(uid);
        } catch (RemoteException e) {
            return true;
        }
    }
}

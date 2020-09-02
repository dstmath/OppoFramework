package com.android.server.location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.OppoMirrorProcess;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswLbsCustomize;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OppoLbsCustomize implements IPswLbsCustomize {
    private static final String CTS_VERSION_PROPERTIES = "persist.sys.cta";
    public static final String GPS_OPCUSTOM_FEATURE = "persist.sys.gps_disable";
    private static final int MAX_PID = 32768;
    private static final String TAG = "OppoLbsCustomize";
    private static OppoLbsCustomize mInstall = null;
    private Context mContext = null;
    private boolean mIsCtaVersion = false;
    private boolean mIsDisableForSpec = false;
    private PackageManager mPackageManager;

    private OppoLbsCustomize(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mIsDisableForSpec = SystemProperties.getInt(GPS_OPCUSTOM_FEATURE, 0) != 1 ? false : true;
        if (this.mIsDisableForSpec) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "location_mode", 0);
        }
        this.mIsCtaVersion = SystemProperties.getBoolean(CTS_VERSION_PROPERTIES, false);
    }

    public static OppoLbsCustomize getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoLbsCustomize(context);
        }
        return mInstall;
    }

    public boolean isForceGnssDisabled() {
        return this.mIsDisableForSpec;
    }

    public void getAppInfoForTr(String methodName, String providerName, int pid, String packageName) {
        if (this.mIsCtaVersion) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currentTime = new Date(System.currentTimeMillis());
            CharSequence appName = packageName;
            try {
                appName = this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(packageName, 0));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (pid <= 0 || pid > MAX_PID) {
                Log.e(TAG, "getAppInfoForTr: pid out of range, pid:" + String.valueOf(pid));
                return;
            }
            String processName = StringUtils.EMPTY;
            if (OppoMirrorProcess.getProcessNameByPid != null) {
                processName = (String) OppoMirrorProcess.getProcessNameByPid.call(new Object[]{Integer.valueOf(pid)});
            }
            Log.d("ctaifs", simpleDateFormat.format(currentTime) + " <" + ((Object) appName) + ">[" + "location" + "][" + processName + "]:[" + methodName + "." + providerName + "]" + "location" + "[" + providerName + "." + providerName + "]");
        }
    }

    public void setDebug(boolean isDebug) {
        FastNetworkLocation.setDebug(isDebug);
        OppoCoarseToFine.setDebug(isDebug);
        OppoGnssDiagnosticTool.setDebug(isDebug);
        OppoGnssDuration.setDebug(isDebug);
        OppoGnssWhiteListProxy.setDebug(isDebug);
        OppoLbsRomUpdateUtil.setDebug(isDebug);
        OppoLocationBlacklistUtil.setDebug(isDebug);
        OppoLocationStatistics.setDebug(isDebug);
        OppoNetworkUtil.setDebug(isDebug);
        OppoNlpProxy.setDebug(isDebug);
        OppoSuplController.setDebug(isDebug);
    }
}

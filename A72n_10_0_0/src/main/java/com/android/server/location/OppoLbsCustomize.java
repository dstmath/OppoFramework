package com.android.server.location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.OppoMirrorProcess;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswLbsCustomize;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoLbsCustomize implements IPswLbsCustomize {
    private static final String CTS_VERSION_PROPERTIES = "persist.sys.cta";
    public static final String GPS_OPCUSTOM_FEATURE = "persist.sys.gps_disable";
    private static final int MAX_PID = 32768;
    private static final String TAG = "OppoLbsCustomize";
    private static OppoLbsCustomize mInstall = null;
    private final String CUSTOMIZE_LIST_PATH = SystemProperties.get("sys.custom.whitelist", "/system/etc/oppo_customize_whitelist.xml");
    private Context mContext = null;
    private ArrayList<String> mCustomizeList = new ArrayList<>();
    private boolean mGpsBackGroundBlockFeatureDisable = false;
    private boolean mIsCtaVersion = false;
    private boolean mIsDisableForSpec = false;
    private PackageManager mPackageManager;

    private OppoLbsCustomize(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mIsDisableForSpec = SystemProperties.getInt("persist.sys.gps_disable", 0) == 1;
        if (this.mIsDisableForSpec) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "location_mode", 0);
        }
        this.mIsCtaVersion = SystemProperties.getBoolean(CTS_VERSION_PROPERTIES, false);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom") && this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.allow_gps_background")) {
            this.mGpsBackGroundBlockFeatureDisable = true;
        }
        if (this.mGpsBackGroundBlockFeatureDisable) {
            this.mCustomizeList = loadCustomizeWhiteList(this.CUSTOMIZE_LIST_PATH);
        }
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
            Log.d("ctaifs", simpleDateFormat.format(currentTime) + " <" + ((Object) appName) + ">[location][" + processName + "]:[" + methodName + "." + providerName + "]location[" + providerName + "." + providerName + "]");
        }
    }

    private ArrayList<String> loadCustomizeWhiteList(String path) {
        int type;
        String value;
        ArrayList<String> emptyList = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            Log.w(TAG, path + " file don't exist!");
            return emptyList;
        }
        ArrayList<String> ret = new ArrayList<>();
        FileInputStream stream = null;
        boolean success = false;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && "p".equals(parser.getName()) && (value = parser.getAttributeValue(null, "att")) != null) {
                    ret.add(value);
                }
            } while (type != 1);
            success = true;
            try {
                stream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e2) {
            Log.w(TAG, "failed parsing ", e2);
            if (0 != 0) {
                stream.close();
            }
        } catch (NumberFormatException e3) {
            Log.w(TAG, "failed parsing ", e3);
            if (0 != 0) {
                stream.close();
            }
        } catch (XmlPullParserException e4) {
            Log.w(TAG, "failed parsing ", e4);
            if (0 != 0) {
                stream.close();
            }
        } catch (IOException e5) {
            Log.w(TAG, "failed parsing ", e5);
            if (0 != 0) {
                stream.close();
            }
        } catch (IndexOutOfBoundsException e6) {
            Log.w(TAG, "failed parsing ", e6);
            if (0 != 0) {
                stream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            throw th;
        }
        if (success) {
            return ret;
        }
        Log.w(TAG, path + " file failed parsing!");
        return emptyList;
    }

    public ArrayList<String> getCustomizeWhiteList() {
        return this.mCustomizeList;
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

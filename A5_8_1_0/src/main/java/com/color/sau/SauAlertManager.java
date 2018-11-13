package com.color.sau;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Process;
import android.util.Log;
import com.color.sau.SAUDb.UpdateInfoColumns;
import com.color.util.ColorUnitConversionUtils;
import com.color.widget.ColorSAUAlertDialog;
import java.io.File;
import java.util.List;

public class SauAlertManager {
    private static final int ACTION_START_DOWNLOAD = 0;
    private static final int ACTION_START_INSTALL = 1;
    private static final int CAN_NOT_USE_OLD = 0;
    private static final int CAN_USE_OLD = 1;
    private static final boolean DEBUG = false;
    private static final int DOWNLOAD_FINISH = 1;
    private static final int DOWNLOAD_UNFINISH = 0;
    private static final int INSTALL_FINISH = 1;
    private static final int INSTALL_UNFINISH = 0;
    private static final int MOBILE_CONNECTED = 1;
    private static final int NO_NETWORK = 0;
    private static final int PATCH_FINISH = 1;
    private static final int PATCH_UNFINISH = 0;
    private static final String TAG = "SauJar";
    private static final String UPGRADE_SERVICE_ACTION = "oppo.intent.action.SAU_APP_JAR_UPGRADE_SERVICE";
    private static final int WIFI_CONNECTED = 2;
    private int mAllSize = 0;
    private boolean mCanUseOld = true;
    private Context mContext = null;
    private String mDescription = null;
    private final ISauUpgradeDialog mDialogListener = new ISauUpgradeDialog() {
        public void exitUpgrade() {
            SauAlertManager.this.killMySelf();
        }

        public void installNow() {
            Intent installIntent = SauAlertManager.this.createExplicitFromImplicitIntent(SauAlertManager.this.mContext, new Intent(SauAlertManager.UPGRADE_SERVICE_ACTION));
            if (installIntent != null) {
                installIntent.putExtra(UpdateInfoColumns.TYPE, "appJar");
                installIntent.putExtra("action", 1);
                installIntent.putExtra("pkgName", SauAlertManager.this.mPkg);
                SauAlertManager.this.mContext.startService(installIntent);
            }
        }

        public void upgradeNow() {
            Intent downloadIntent = SauAlertManager.this.createExplicitFromImplicitIntent(SauAlertManager.this.mContext, new Intent(SauAlertManager.UPGRADE_SERVICE_ACTION));
            if (downloadIntent != null) {
                downloadIntent.putExtra(UpdateInfoColumns.TYPE, "appJar");
                downloadIntent.putExtra("action", 0);
                downloadIntent.putExtra("pkgName", SauAlertManager.this.mPkg);
                if (SauAlertManager.this.mFileDeleted) {
                    downloadIntent.putExtra("fileDeleted", true);
                }
                SauAlertManager.this.mContext.startService(downloadIntent);
            }
            if (!SauAlertManager.this.mCanUseOld) {
                SauAlertManager.this.mProgressDialog = new SauWaitProgressDialog(SauAlertManager.this.mContext);
                if ((SauAlertManager.this.mContext instanceof Activity) && (((Activity) SauAlertManager.this.mContext).isFinishing() ^ 1) != 0) {
                    SauAlertManager.this.mProgressDialog.show();
                }
            }
        }

        public void upgradeLater() {
        }
    };
    private boolean mDownloadFinished = DEBUG;
    private boolean mFileDeleted = DEBUG;
    private String mFileName = null;
    private boolean mInstallFinished = DEBUG;
    private String mMd5All = null;
    private String mMd5Patch = null;
    private String mNewVerName = null;
    private String mOldFileName = null;
    private boolean mPatchFinished = DEBUG;
    private int mPatchSize = 0;
    private String mPkg = null;
    private SauWaitProgressDialog mProgressDialog = null;
    private int mType = 0;
    private ColorSAUAlertDialog mUpgradeDialog;
    private String mUrl = null;
    private SauPkgUpdateInfo pkgInfo = null;

    public void init(Context context, SauPkgUpdateInfo info) {
        this.mContext = context;
        this.pkgInfo = info;
    }

    public int createAlertDialog() {
        this.mPkg = this.pkgInfo.mPkg;
        this.mNewVerName = this.pkgInfo.mNewVerName;
        this.mDescription = this.pkgInfo.mDescription;
        this.mPatchSize = this.pkgInfo.mPatchSize;
        this.mAllSize = this.pkgInfo.mAllSize;
        this.mCanUseOld = this.pkgInfo.mCanUseOld == 1 ? true : DEBUG;
        this.mPatchFinished = this.pkgInfo.mPatchFinished == 1 ? true : DEBUG;
        this.mDownloadFinished = this.pkgInfo.mDownloadFinished == 1 ? true : DEBUG;
        this.mInstallFinished = this.pkgInfo.mInstallFinished == 1 ? true : DEBUG;
        this.mUrl = this.pkgInfo.mUrl;
        this.mType = this.pkgInfo.mType;
        this.mMd5Patch = this.pkgInfo.mMd5Patch;
        this.mMd5All = this.pkgInfo.mMd5All;
        this.mOldFileName = this.pkgInfo.mOldFileName;
        this.mFileName = this.pkgInfo.mFileName;
        if (!(this.mFileName == null || !this.mPatchFinished || new File(this.mFileName).exists())) {
            this.mPatchFinished = DEBUG;
            this.mFileDeleted = true;
        }
        int networkState = getNetworkState(this.mContext);
        if (networkState == 0 && (this.mPatchFinished ^ 1) != 0) {
            return 0;
        }
        boolean wifiConnected = networkState == 2 ? true : DEBUG;
        this.mUpgradeDialog = new ColorSAUAlertDialog(this.mContext);
        SauUpgradeAlertDialogFunc.setDialogListener(this.mDialogListener);
        SauUpgradeAlertDialogFunc.processDialogFunc(this.mUpgradeDialog, this.mPkg, this.mCanUseOld ^ 1, this.mPatchFinished, wifiConnected, this.mNewVerName, getSizeString((long) this.mPatchSize), this.mDescription);
        if (!(this.mContext instanceof Activity) || (((Activity) this.mContext).isFinishing() ^ 1) == 0) {
            Log.d(TAG, "activity is finishing, do not show");
            return 0;
        }
        this.mUpgradeDialog.show();
        return 1;
    }

    public ColorSAUAlertDialog getUpgradeDialog() {
        this.mPkg = this.pkgInfo.mPkg;
        this.mNewVerName = this.pkgInfo.mNewVerName;
        this.mDescription = this.pkgInfo.mDescription;
        this.mPatchSize = this.pkgInfo.mPatchSize;
        this.mAllSize = this.pkgInfo.mAllSize;
        this.mCanUseOld = this.pkgInfo.mCanUseOld == 1 ? true : DEBUG;
        this.mPatchFinished = this.pkgInfo.mPatchFinished == 1 ? true : DEBUG;
        this.mDownloadFinished = this.pkgInfo.mDownloadFinished == 1 ? true : DEBUG;
        this.mInstallFinished = this.pkgInfo.mInstallFinished == 1 ? true : DEBUG;
        this.mUrl = this.pkgInfo.mUrl;
        this.mType = this.pkgInfo.mType;
        this.mMd5Patch = this.pkgInfo.mMd5Patch;
        this.mMd5All = this.pkgInfo.mMd5All;
        this.mOldFileName = this.pkgInfo.mOldFileName;
        this.mFileName = this.pkgInfo.mFileName;
        if (!(this.mFileName == null || !this.mPatchFinished || new File(this.mFileName).exists())) {
            this.mPatchFinished = DEBUG;
            this.mFileDeleted = true;
        }
        int networkState = getNetworkState(this.mContext);
        if (networkState == 0 && (this.mPatchFinished ^ 1) != 0) {
            return null;
        }
        boolean wifiConnected = networkState == 2 ? true : DEBUG;
        this.mUpgradeDialog = new ColorSAUAlertDialog(this.mContext);
        SauUpgradeAlertDialogFunc.setDialogListener(this.mDialogListener);
        SauUpgradeAlertDialogFunc.processDialogFunc(this.mUpgradeDialog, this.mPkg, this.mCanUseOld ^ 1, this.mPatchFinished, wifiConnected, this.mNewVerName, getSizeString((long) this.mPatchSize), this.mDescription);
        return this.mUpgradeDialog;
    }

    private void killMySelf() {
        Process.killProcess(Process.myPid());
    }

    private Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    private int getNetworkState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isAvailable()) {
            return 0;
        }
        State wifiState = cm.getNetworkInfo(1).getState();
        State mobileState = cm.getNetworkInfo(0).getState();
        if (wifiState == State.CONNECTED || wifiState == State.CONNECTING) {
            return 2;
        }
        return (mobileState == State.CONNECTED || mobileState == State.CONNECTING) ? 1 : 0;
    }

    private String getSizeString(long size) {
        return new ColorUnitConversionUtils(this.mContext).getUnitValue(size);
    }
}

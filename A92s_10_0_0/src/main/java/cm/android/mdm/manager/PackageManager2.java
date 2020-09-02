package cm.android.mdm.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import cm.android.mdm.interfaces.IPackageManager;
import cm.android.mdm.util.CustomizeServiceManager;
import cm.android.mdm.util.MethodSignature;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PackageManager2 implements IPackageManager {
    private static final String ACTION_INSTALL_COMMIT = "com.android.ct.tianjia.s.deviceowner.INTENT_PACKAGE_INSTALL_COMMIT";
    private static final int ADD_APP_LIST = 1;
    private static final int BLACK_LIST = 1;
    private static final int DELETE_APP_LIST = 2;
    private static final int NORMAL = 0;
    private static final int PACKAGE_INSTALLER_STATUS_UNDEFINED = -1000;
    private static final String TAG = "PackageManager2";
    private static final int WHITE_LIST = 2;
    /* access modifiers changed from: private */
    public Context mContext;
    private InstallBroadcastReceiver mInstallBroadcastReceiver;
    private PackageInstaller mPackageInstaller;
    private PackageManager mPackageManager = null;
    private PackageInstaller.Session mSession;

    public PackageManager2(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
    }

    private class PackageDeleteObserver2 extends IPackageDeleteObserver.Stub {
        private boolean mFinished;
        private String mPackageName;
        /* access modifiers changed from: private */
        public int mResult;

        private PackageDeleteObserver2() {
            this.mFinished = false;
            this.mPackageName = "";
        }

        public void packageDeleted(String name, int status) {
            this.mFinished = true;
            this.mResult = status;
            this.mPackageName = name;
        }
    }

    private class InstallBroadcastReceiver extends BroadcastReceiver {
        private boolean mFinished;
        private String mPackageName;
        /* access modifiers changed from: private */
        public int mResult;

        private InstallBroadcastReceiver() {
            this.mFinished = false;
            this.mPackageName = "";
        }

        public void onReceive(Context context, Intent intent) {
            this.mResult = intent.getIntExtra("android.content.pm.extra.STATUS", PackageManager2.PACKAGE_INSTALLER_STATUS_UNDEFINED);
            if (this.mResult == 0) {
                PackageManager2.this.mContext.unregisterReceiver(this);
                this.mPackageName = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
            }
            this.mFinished = true;
            Log.d(PackageManager2.TAG, "Package: " + this.mPackageName + ", Installed: " + this.mResult);
        }
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    @Deprecated
    public void installPackage(Uri packageURI, IPackageManager.PackageInstallObserver observer, int flags, String installerPackageName) {
        installPackage(packageURI, observer);
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void installPackage(Uri packageURI, IPackageManager.PackageInstallObserver observer) {
        if (this.mPackageManager != null) {
            this.mInstallBroadcastReceiver = new InstallBroadcastReceiver();
            this.mPackageInstaller = this.mPackageManager.getPackageInstaller();
            PackageInfo mPi = this.mPackageManager.getPackageArchiveInfo(packageURI.getPath(), 1);
            if (mPi == null) {
                Log.e(TAG, "The package could not be parsed for the given uri :" + packageURI);
                return;
            }
            try {
                installPackage(mPi.packageName, packageURI.getPath());
            } catch (Exception e) {
                Log.d(TAG, "Package: " + mPi.packageName + "installPackage" + e);
            }
            if (observer != null) {
                observer.packageInstalled(mPi.packageName, this.mInstallBroadcastReceiver.mResult);
            }
        }
    }

    private void installPackage(String packageName, String packageLocation) throws Exception {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(1);
        params.setAppPackageName(packageName);
        int sessionId = this.mPackageInstaller.createSession(params);
        this.mSession = this.mPackageInstaller.openSession(sessionId);
        File file = new File(packageLocation);
        InputStream in = new FileInputStream(file);
        OutputStream out = this.mSession.openWrite("SilentPackageInstaller", 0, file.length());
        byte[] buffer = new byte[65536];
        while (true) {
            int c = in.read(buffer);
            if (c != -1) {
                out.write(buffer, NORMAL, c);
            } else {
                this.mSession.fsync(out);
                out.close();
                this.mSession.commit(getCommitCallback(sessionId));
                this.mSession.close();
                return;
            }
        }
    }

    private IntentSender getCommitCallback(int sessionId) {
        String action = "com.android.ct.tianjia.s.deviceowner.INTENT_PACKAGE_INSTALL_COMMIT." + sessionId;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        if (this.mInstallBroadcastReceiver == null) {
            this.mInstallBroadcastReceiver = new InstallBroadcastReceiver();
        }
        this.mContext.registerReceiver(this.mInstallBroadcastReceiver, intentFilter);
        return PendingIntent.getBroadcast(this.mContext, sessionId, new Intent(action), 134217728).getIntentSender();
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void deletePackage(String packageName, IPackageManager.PackageDeleteObserver observer, int flags) {
        if (this.mPackageManager != null) {
            PackageDeleteObserver2 deleteObserver = new PackageDeleteObserver2();
            this.mPackageManager.deletePackage(packageName, deleteObserver, flags);
            if (observer != null) {
                observer.packageDeleted(packageName, deleteObserver.mResult);
            }
        }
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void clearApplicationUserData(String packageName) {
        CustomizeServiceManager.clearAppData(packageName);
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void addDisallowUninstallApps(List<String> packageNames) {
        CustomizeServiceManager.addDisallowUninstallApps(packageNames);
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void removeDisallowUninstallApps(List<String> packageNames) {
        CustomizeServiceManager.removeDisallowUninstallApps(packageNames);
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void removeDisallowUninstallApps() {
        new ArrayList();
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public List<String> getDisallowUninstallApps() {
        return CustomizeServiceManager.getDisallowUninstallApps();
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void setAppRestriction(int pattern) {
        if (pattern == 1) {
            CustomizeServiceManager.setProp("persist.sys.enable_black_list", "true");
            CustomizeServiceManager.setProp("persist.sys.enable_white_list", "false");
        } else if (pattern == 2) {
            CustomizeServiceManager.setProp("persist.sys.enable_black_list", "false");
            CustomizeServiceManager.setProp("persist.sys.enable_white_list", "true");
        } else if (pattern == 0) {
            CustomizeServiceManager.setProp("persist.sys.enable_black_list", "false");
            CustomizeServiceManager.setProp("persist.sys.enable_white_list", "false");
        }
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void addAppRestriction(int pattern, List<String> list) {
        if (this.mPackageManager != null && pattern != 1 && pattern != 2) {
        }
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void removeAppRestriction(int pattern, List<String> list) {
        if (this.mPackageManager != null && pattern != 1 && pattern != 2) {
        }
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public void removeAppRestriction(int pattern) {
        if (this.mPackageManager != null && pattern != 1 && pattern != 2) {
        }
    }

    @Override // cm.android.mdm.interfaces.IPackageManager
    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(PackageManager2.class);
    }
}

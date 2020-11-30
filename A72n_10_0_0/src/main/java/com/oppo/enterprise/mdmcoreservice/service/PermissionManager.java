package com.oppo.enterprise.mdmcoreservice.service;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.oppo.enterprise.mdmcoreservice.certificate.OppoCertificateVerifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class PermissionManager {
    private static long DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    private static long DEFAULT_READ_TIMEOUT_MS = 3000;
    private static OkHttpClient client = new OkHttpClient.Builder().connectTimeout(DEFAULT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS).readTimeout(DEFAULT_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS).followRedirects(true).build();
    private static volatile PermissionManager sInstance;
    private Context mContext;
    private OppoCertificateVerifier mOppoCertificateVerifier;

    private PermissionManager() {
    }

    public void setContext(Context context) {
        this.mContext = context;
        if (this.mOppoCertificateVerifier == null) {
            Log.d("PermissionManager", "OppoCertificateVerifier create");
            OppoCertificateVerifier oppoCertificateVerifier = this.mOppoCertificateVerifier;
            this.mOppoCertificateVerifier = OppoCertificateVerifier.getInstance(this.mContext);
        }
    }

    private int checkPackagePermission(String permission, String pkgName, int userId) {
        int ret = -1;
        Log.d("PermissionManager", "checkPackagePermission package:" + pkgName + ";permission:" + permission);
        if (this.mOppoCertificateVerifier != null) {
            ret = this.mOppoCertificateVerifier.oppoCheckPermission(permission, pkgName);
        }
        if (ret == 0) {
            Log.d("PermissionManager", "checkPackagePermission is PERMISSION_GRANTED");
        } else {
            Log.d("PermissionManager", "checkPackagePermission is PERMISSION_DENIED");
        }
        return ret;
    }

    private String getPackageNameByPid(int pid) {
        for (ActivityManager.RunningAppProcessInfo processInfo : ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.pkgList[0];
            }
        }
        return null;
    }

    private void checkPermission(String permission, String api) throws SecurityException {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (api != null && !"".equals(api)) {
            permission = permission + "," + api;
        }
        String mPackageName = getPackageNameByPid(pid);
        if (mPackageName == null) {
            throw new SecurityException();
        } else if (checkPackagePermission(permission, mPackageName, UserHandle.getUserId(uid)) != 0) {
            throw new SecurityException("Check the permission:" + permission + " for package " + mPackageName + "failed!Please check the certificate in package.");
        }
    }

    public void checkPermission() throws SecurityException {
        try {
            checkPermission("com.oppo.permission.sec.MDM_PHONE_MANAGER", null);
            MdmStatistics.getInstance(this.mContext).recordInferfacecalled();
        } catch (Exception e) {
            MdmStatistics.getInstance(this.mContext).recordPermissionException();
            throw e;
        }
    }

    public void checkCritialPermission(String api) {
        try {
            checkPermission("com.oppo.permission.sec.MDM_CRITICAL", api);
            MdmStatistics.getInstance(this.mContext).recordInferfacecalled();
        } catch (Exception e) {
            MdmStatistics.getInstance(this.mContext).recordPermissionException();
            throw e;
        }
    }

    public static final PermissionManager getInstance() {
        PermissionManager permissionManager;
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (PermissionManager.class) {
            if (sInstance == null) {
                sInstance = new PermissionManager();
            }
            permissionManager = sInstance;
        }
        return permissionManager;
    }

    public OppoCertificateVerifier getOppoCertificateVerifier() {
        return this.mOppoCertificateVerifier;
    }

    public static boolean writeStringToFile(String logTag, String path, String value) {
        boolean result = false;
        FileWriter writer = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer2 = new FileWriter(file);
            writer2.write(value);
            writer2.close();
            result = true;
            try {
                writer2.close();
            } catch (IOException e) {
                Slog.i(logTag, "Failed to close file.");
            }
        } catch (IOException e2) {
            Slog.i(logTag, "Failed to write file.");
            if (0 != 0) {
                writer.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e3) {
                    Slog.i(logTag, "Failed to close file.");
                }
            }
            throw th;
        }
        return result;
    }
}

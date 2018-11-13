package com.android.server.oppo;

import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;

public class ElsaManager {
    private static int ELSA_RESUME_MASK_UNINIT = -1;
    private static final int PROP_ENABLE_RESUME_MASK = 16;
    private static int mElsaResumeMask = ELSA_RESUME_MASK_UNINIT;

    private static boolean checkIfElsaEnableReusme() {
        int elsaCurResumeMask = SystemProperties.getInt("persist.sys.elsa.enable", 0) & 16;
        if (mElsaResumeMask == ELSA_RESUME_MASK_UNINIT) {
            mElsaResumeMask = SystemProperties.getInt("persist.sys.elsa.enable", 0) & 16;
            return mElsaResumeMask > 0;
        } else if (mElsaResumeMask != elsaCurResumeMask) {
            if (elsaCurResumeMask == 0) {
                mElsaResumeMask = 0;
            }
            return false;
        } else if (mElsaResumeMask == 16 && elsaCurResumeMask == 16) {
            return true;
        } else {
            if (mElsaResumeMask == 0 && elsaCurResumeMask == 0) {
                return false;
            }
            return false;
        }
    }

    public static void elsaResume(int id, int timeout, boolean isTargetFreeze, int flag, String reason) {
        RuntimeException here = new RuntimeException("here");
        here.fillInStackTrace();
        Slog.d("elsa", "Called: elsaResume", here);
        if (checkIfElsaEnableReusme()) {
            IBinder binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR);
            if (binder != null) {
                try {
                    new ElsaManagerProxy(binder).elsaResume(id, timeout, isTargetFreeze ? 1 : 0, flag, reason);
                } catch (Exception e) {
                }
            }
        }
    }

    public static boolean isFrozingByPid(int pid) {
        if (pid == 0) {
            return false;
        }
        boolean result = false;
        IBinder binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR);
        if (binder == null) {
            return false;
        }
        try {
            result = new ElsaManagerProxy(binder).elsaGetPackageFreezing(pid, 1) > 0;
        } catch (Exception e) {
        }
        return result;
    }

    public static void elsaResumePid(int id, String reason) {
        if (checkIfElsaEnableReusme()) {
            elsaResume(id, 0, true, 1, reason);
        }
    }

    public static void AddDependApp(int uid, int uidDepend) {
        IBinder binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR);
        if (binder != null) {
            try {
                new ElsaManagerProxy(binder).elsaAddDependApp(uid, uidDepend);
            } catch (Exception e) {
            }
        }
    }

    public static void RemoveDependApp(int uid, int uidDepend) {
        IBinder binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR);
        if (binder != null) {
            try {
                new ElsaManagerProxy(binder).elsaRemoveDependApp(uid, uidDepend);
            } catch (Exception e) {
            }
        }
    }
}

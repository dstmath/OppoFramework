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
        boolean enable = false;
        int elsaCurResumeMask = SystemProperties.getInt("persist.sys.elsa.enable", 0) & 16;
        int i = mElsaResumeMask;
        if (i == ELSA_RESUME_MASK_UNINIT) {
            mElsaResumeMask = SystemProperties.getInt("persist.sys.elsa.enable", 0) & 16;
            if (mElsaResumeMask > 0) {
                enable = true;
            }
            return enable;
        } else if (i != elsaCurResumeMask) {
            if (elsaCurResumeMask == 0) {
                mElsaResumeMask = 0;
            }
            return false;
        } else if (i == 16 && elsaCurResumeMask == 16) {
            return true;
        } else {
            if (mElsaResumeMask != 0 || elsaCurResumeMask == 0) {
                return false;
            }
            return false;
        }
    }

    public static void elsaResume(int id, int timeout, boolean isTargetFreeze, int flag, String reason) {
        IBinder binder;
        RuntimeException here = new RuntimeException("here");
        here.fillInStackTrace();
        Slog.d("elsa", "Called: elsaResume", here);
        if (checkIfElsaEnableReusme() && (binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR)) != null) {
            try {
                new ElsaManagerProxy(binder).elsaResume(id, timeout, isTargetFreeze ? 1 : 0, flag, reason);
            } catch (Exception e) {
            }
        }
    }

    public static boolean isFrozingByPid(int pid) {
        IBinder binder;
        boolean result = false;
        if (pid == 0 || (binder = ServiceManager.checkService(IElsaManager.DESCRIPTOR)) == null) {
            return false;
        }
        try {
            if (new ElsaManagerProxy(binder).elsaGetPackageFreezing(pid, 1) > 0) {
                result = true;
            }
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    public static void elsaResumePid(int id, String reason) {
        if (checkIfElsaEnableReusme()) {
            elsaResume(id, 0, true, 1, reason);
        }
    }
}

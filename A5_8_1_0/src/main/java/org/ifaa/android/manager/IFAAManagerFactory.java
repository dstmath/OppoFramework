package org.ifaa.android.manager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.IBinder.DeathRecipient;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

public class IFAAManagerFactory {
    public static final String TAG = "IFAAManagerFactory";
    private static IFAAManager sFAAManager;

    private static class IFAAManagerOppo extends IFAAManagerV2 implements DeathRecipient {
        private int mAlikeyStatus;
        private FingerprintManager mFingerprintManager;
        private boolean mHasAlikeyStatus;

        /* synthetic */ IFAAManagerOppo(IFAAManagerOppo -this0) {
            this();
        }

        private IFAAManagerOppo() {
            this.mHasAlikeyStatus = false;
            this.mFingerprintManager = null;
        }

        static {
            try {
                System.loadLibrary("teeclientjni");
            } catch (UnsatisfiedLinkError e) {
                Log.e(IFAAManagerFactory.TAG, e.toString());
            }
        }

        public int getSupportBIOTypes(Context context) {
            if (getDeviceModel().equalsIgnoreCase("OPPO-Default")) {
                return 0;
            }
            return 1;
        }

        public int startBIOManager(Context context, int authType) {
            if (authType != 1) {
                return -1;
            }
            try {
                context.startActivityAsUser(new Intent("oppo.intent.action.FINGERPRINT"), UserHandle.OWNER);
                return 0;
            } catch (ActivityNotFoundException e) {
                return -1;
            }
        }

        public String getDeviceModel() {
            if ("true".equals(SystemProperties.get("persist.version.confidential"))) {
                String build = SystemProperties.get("ro.build.version.ota");
                if (build.startsWith("17011")) {
                    return "OPPO-R7011";
                }
                if (build.startsWith("17031")) {
                    return "OPPO-R7031";
                }
                if (build.startsWith("17321")) {
                    return "OPPO-R7321";
                }
                if (build.startsWith("PAAM00")) {
                    return "OPPO-R7081";
                }
                if (build.startsWith("PACM00")) {
                    return "OPPO-R7197";
                }
                if (build.startsWith("CPH1819EX") || build.startsWith("CPH1823EX") || build.startsWith("CPH1825EX")) {
                    return "OPPO-R7331";
                }
                return "OPPO-Default";
            }
            String str = Build.MODEL;
            if (str.equals("OPPO R6017") || str.equals("OPPO R6018") || str.equals("OPPO R9s") || str.equals("OPPO R9st") || str.equals("OPPO R6027") || str.equals("OPPO R9sk")) {
                return "OPPO-R9s";
            }
            if (str.equals("OPPO R6061") || str.equals("OPPO R6062") || str.equals("OPPO A57") || str.equals("OPPO A57t")) {
                return "OPPO-R6061";
            }
            if (str.equals("OPPO R6037") || str.equals("OPPO R9sPlus") || str.equals("OPPO R9s Plus") || str.equals("OPPO R9s Plust") || str.equals("OPPO R9sPlust")) {
                return "OPPO-R6037";
            }
            if (str.equals("OPPO R6091")) {
                return "OPPO-R6091";
            }
            if (str.equals("OPPO R9 Plustm A") || str.equals("OPPO R9 Plusm A") || str.equals("OPPO R9 Plust A") || str.equals("OPPO R9 Plus A") || str.equals("OPPO R5103")) {
                return "OPPO-R5103";
            }
            if (str.equals("OPPO R6051") || str.equals("OPPO R6052") || str.equals("OPPO R6102") || str.equals("OPPO R6103") || str.equals("OPPO R6118") || str.equals("OPPO R11") || str.equals("OPPO R11t") || str.equals("OPPO R11 Plus") || str.equals("OPPO R11 Plust") || str.equals("OPPO R11 Plusk") || str.equals("OPPO R11 Pluskt")) {
                return "OPPO-R6051";
            }
            if (str.equals("OPPO A77") || str.equals("OPPO A77t")) {
                return "OPPO-R7001";
            }
            if (str.equals("OPPO R11s") || str.equals("OPPO R11st") || str.equals("OPPO R11s Plus") || str.equals("OPPO R11s Plust")) {
                return "OPPO-R7011";
            }
            if (str.equals("CPH1723") || str.equals("CPH1725") || str.equals("CPH1727")) {
                return "OPPO-R7321";
            }
            if (str.equals("PAAM00") || str.equals("PAAT00")) {
                return "OPPO-R7081";
            }
            if (str.equals("PACM00") || str.equals("PACT00") || str.equals("CPH1835")) {
                return "OPPO-R7197";
            }
            if (str.equals("CPH1819") || str.equals("CPH1821") || str.equals("CPH1823") || str.equals("CPH1825")) {
                return "OPPO-R7331";
            }
            return "OPPO-Default";
        }

        public int getVersion() {
            return 2;
        }

        public byte[] processCmdV2(Context context, byte[] param) {
            if (this.mFingerprintManager == null) {
                this.mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            }
            if (this.mFingerprintManager != null) {
                return this.mFingerprintManager.alipayInvokeCommand(param);
            }
            Log.w(IFAAManagerFactory.TAG, "processCmdV2: no FingerprintManager!");
            return null;
        }

        public String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0) {
                return null;
            }
            for (byte b : src) {
                String hv = Integer.toHexString(b & 255);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        }

        public void binderDied() {
        }
    }

    public static IFAAManager getIFAAManager(Context context, int authType) {
        if (authType != 1) {
            return null;
        }
        if (sFAAManager == null) {
            sFAAManager = new IFAAManagerOppo();
        }
        return sFAAManager;
    }
}

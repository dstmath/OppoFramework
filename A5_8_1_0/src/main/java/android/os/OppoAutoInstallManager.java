package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OppoAutoInstallManager {
    private static final boolean DEBUG = true;
    public static boolean DEBUG_GR = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String DO_GR_CHECK_INTERNET = "DO_GR_CHECK_INTERNET";
    public static final String DO_GR_DOWN_INSTALL = "DO_GR_DOWN_INSTALL";
    public static final String DO_GR_EXIT = "DO_GR_EXIT";
    public static final String DO_GR_INSTALL_TALKBACK = "DO_GR_INSTALL_TALKBACK";
    public static final String DO_GR_REINSTALL = "DO_GR_REINSTALL";
    public static final String DO_GR_SHOW_EXCEPTION = "DO_GR_SHOW_EXCEPTION";
    public static final String DO_GR_SUCC = "DO_GR_SUCC";
    public static final String DO_GR_TALKBACK_SUCC = "DO_GR_TALKBACK_SUCC";
    public static final String EXCEPTION_TYPE_NETWORK = "NetworkError";
    public static final String EXCEPTION_TYPE_TALKBACK = "TalkbackError";
    public static final String GMAP_PNAME = "com.google.android.apps.maps";
    public static Integer GR_APK_NUMBER = Integer.valueOf(6);
    private static final int GR_BLACK_LIST = 679;
    private static final int GR_WHITE_LIST = 680;
    public static final String OPPO_ROAM_SUPPORT_PARAM_NAME = "canSupportOppoRoam";
    public static final String PARAM_APP_NAME = "appName";
    public static final String PARAM_BASE_CODE_PATH = "baseCodePath";
    public static final String PARAM_EXCEPTION_TYPE = "exceptionType";
    public static final String PARAM_PKG_NAME = "pkgName";
    public static final String SERVICE_NAME = "oppoautoinstall";
    public static final String TAG = "OppoAutoInstallManager";
    public static final String USER_IN_CHINA = "isInChina";
    public static final String WHETHER_IN_CHINA_PARAM_NAME = "isInChina";
    public static Boolean canCreateDialog = Boolean.valueOf(true);
    public static Boolean canReinstall = Boolean.valueOf(true);
    private static List<String> cannotExit = Arrays.asList(new String[]{"android"});
    private static List<String> grBlackList = Arrays.asList(new String[]{"com.google.android.exoplayer.playbacktests", "com.google.android.packageinstaller", "com.google.android.apps.youtube.testsuite", "com.google.android.accounts.gts.unaffiliated", "android.largeapk.app", "com.android.compatibility.common.deviceinfo", "com.android.cts.priv.ctsshim", "com.android.gts.ssaidapp1", "com.android.gts.ssaidapp2", "com.android.preconditions.gts", "com.google.android.marvin.talkback", "com.google.android.ar.svc"});
    public static List<String> grList = Arrays.asList(new String[]{"com.google.android.gms", "com.google.android.partnersetup", "com.google.android.gsf", "com.google.android.syncadapters.calendar", "com.google.android.syncadapters.contacts"});
    public static final Signature[] grSig = new Signature[]{new Signature("308204433082032ba003020102020900c2e08746644a308d300d06092a864886f70d01010405003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964301e170d3038303832313233313333345a170d3336303130373233313333345a3074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f696430820120300d06092a864886f70d01010105000382010d00308201080282010100ab562e00d83ba208ae0a966f124e29da11f2ab56d08f58e2cca91303e9b754d372f640a71b1dcb130967624e4656a7776a92193db2e5bfb724a91e77188b0e6a47a43b33d9609b77183145ccdf7b2e586674c9e1565b1f4c6a5955bff251a63dabf9c55c27222252e875e4f8154a645f897168c0b1bfc612eabf785769bb34aa7984dc7e2ea2764cae8307d8c17154d7ee5f64a51a44a602c249054157dc02cd5f5c0e55fbef8519fbe327f0b1511692c5a06f19d18385f5c4dbc2d6b93f68cc2979c70e18ab93866b3bd5db8999552a0e3b4c99df58fb918bedc182ba35e003c1b4b10dd244a8ee24fffd333872ab5221985edab0fc0d0b145b6aa192858e79020103a381d93081d6301d0603551d0e04160414c77d8cc2211756259a7fd382df6be398e4d786a53081a60603551d2304819e30819b8014c77d8cc2211756259a7fd382df6be398e4d786a5a178a4763074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6964820900c2e08746644a308d300c0603551d13040530030101ff300d06092a864886f70d010104050003820101006dd252ceef85302c360aaace939bcff2cca904bb5d7a1661f8ae46b2994204d0ff4a68c7ed1a531ec4595a623ce60763b167297a7ae35712c407f208f0cb109429124d7b106219c084ca3eb3f9ad5fb871ef92269a8be28bf16d44c8d9a08e6cb2f005bb3fe2cb96447e868e731076ad45b33f6009ea19c161e62641aa99271dfd5228c5c587875ddb7f452758d661f6cc0cccb7352e424cc4365c523532f7325137593c4ae341f4db41edda0d0b1071a7c440f0fe9ea01cb627ca674369d084bd2fd911ff06cdbf2cfa10dc0f893ae35762919048c7efc64c7144178342f70581c9de573af55b390dd7fdb9418631895d5f759f30112687ff621410c069308a")};
    public static Boolean isInnerVersion = Boolean.valueOf(true);
    public static Boolean isNoDialogInstalling = Boolean.valueOf(false);
    public static List<String> mGrApkPathList = Arrays.asList(new String[]{"/data/gr/138e8af41c2a62b4c06adf65577772419.gr", "/data/gr/290aa18407779e8f44cb57733d3b5ea23.gr", "/data/gr/3b64e23f2e4cdf5b109c52f30b37cdcb5.gr", "/data/gr/4f20989b475c563b80c11b18a5c02b457.gr", "/data/gr/5010a28878517c105a60f155f0c6f5c56.gr", "/data/gr/6f8acd492101e6b11f5eadcc188566ae1.gr"});
    public static List<String> queue = new ArrayList();
    private static IOppoAutoInstallService sService;
    public static Boolean willUseGrLeader = Boolean.valueOf(true);

    private static class PackageDeleteObserver extends Stub {
        /* synthetic */ PackageDeleteObserver(PackageDeleteObserver -this0) {
            this();
        }

        private PackageDeleteObserver() {
        }

        public void packageDeleted(String packageName, int returnCode) {
            if (returnCode == 1) {
                Log.d(OppoAutoInstallManager.TAG, "Geloin: we uninstalled " + packageName);
            }
        }
    }

    static {
        initGr();
    }

    public static final boolean init() {
        initGr();
        if (sService != null) {
            return true;
        }
        sService = IOppoAutoInstallService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
        return true;
    }

    public static void stopLeader() {
        willUseGrLeader = Boolean.valueOf(false);
    }

    public static Boolean isNeedLeader(String pkgName) {
        if (!(pkgName == null || (pkgName.startsWith("com.google.android.xts") ^ 1) == 0)) {
            int endsWith = pkgName.startsWith("com.google.android") ? !pkgName.endsWith(".gts") ? pkgName.endsWith(".xts") : 1 : 0;
            if (!((endsWith ^ 1) == 0 || (grBlackList.contains(pkgName) ^ 1) == 0 || (pkgName.startsWith("com.google.android.gts") ^ 1) == 0 || ActivityThread.inCptWhiteList(GR_BLACK_LIST, pkgName) || (!pkgName.startsWith("com.google.android") && !pkgName.equals("com.android.chrome") && !ActivityThread.inCptWhiteList(GR_WHITE_LIST, pkgName)))) {
                return Boolean.valueOf(true);
            }
        }
        return Boolean.valueOf(false);
    }

    public static void uninstallGrs(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        List<String> grList = grList;
        for (int i = 0; i < grList.size(); i++) {
            String pkgName = (String) grList.get(i);
            if (!queue.contains(pkgName)) {
                queue.add(pkgName);
                pm.deletePackage(pkgName, new PackageDeleteObserver(), 2);
            }
        }
    }

    public static Boolean grExists() {
        if (!willUseGrLeader.booleanValue() || (isInnerVersion.booleanValue() ^ 1) != 0) {
            return Boolean.valueOf(true);
        }
        String dataPath = "/data/data/";
        for (String name : grList) {
            if (!new File(dataPath + name).exists()) {
                return Boolean.valueOf(false);
            }
        }
        return Boolean.valueOf(true);
    }

    public static Boolean canShowDialog(String pkgName) {
        if (canCreateDialog.booleanValue()) {
            return Boolean.valueOf(true);
        }
        if (DEBUG_GR) {
            Log.d(TAG, "Geloin: We are installing GR so not leader to install.");
        }
        return Boolean.valueOf(false);
    }

    public static void exit(String pkgName) {
        if (pkgName == null || !cannotExit.contains(pkgName)) {
            doGr(null, null, pkgName, DO_GR_EXIT);
        } else if (DEBUG_GR) {
            Log.d(TAG, "Geloin: Some application can't be killed.");
        }
    }

    public static void doGr(String baseCodePath, String appName, String pkgName, String action) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                sService.doGr(baseCodePath, appName, pkgName, action);
            } catch (RemoteException e) {
                if (DEBUG_GR) {
                    Log.e(TAG, "Geloin: doGr exception!");
                    e.printStackTrace();
                }
            }
            return;
        }
        if (DEBUG_GR) {
            Log.d(TAG, "Geloin: Didn't init Service for GR.");
        }
    }

    private static void initGr() {
        willUseGrLeader = Boolean.valueOf(SystemProperties.getBoolean("gr.use.leader", false));
        if (willUseGrLeader.booleanValue()) {
            GR_APK_NUMBER = Integer.valueOf(SystemProperties.getInt("gr.apk.number", 6));
        }
        if (SystemProperties.get("ro.oppo.version", "CN").equals("CN")) {
            isInnerVersion = Boolean.valueOf(true);
        } else {
            isInnerVersion = Boolean.valueOf(false);
        }
    }
}

package android.os;

public final class OppoGoogleResource {
    private static final boolean DEBUG = true;
    public static final String TAG = "OppoGoogleResource";

    public static void doInstallGoogleApp(String baseCodePath, String appName, String pkgName, String action) {
        OppoAutoInstallManager.doGr(baseCodePath, appName, pkgName, action);
    }
}

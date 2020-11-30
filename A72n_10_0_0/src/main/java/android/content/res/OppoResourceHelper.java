package android.content.res;

import android.util.Log;
import java.io.IOException;
import java.util.List;

public class OppoResourceHelper {
    public static final String OPPO_RES_PATH = "/system/framework/oppo-framework-res.apk";
    private static final String TAG = "OppoResourceHelper";

    public static void addExtraAssetPaths(List<ApkAssets> apkAssets) {
        try {
            apkAssets.add(ApkAssets.loadFromPath(OPPO_RES_PATH, true));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create ColorOS system AssetManager", e);
        }
    }
}

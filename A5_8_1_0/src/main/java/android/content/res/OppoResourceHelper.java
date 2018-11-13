package android.content.res;

public class OppoResourceHelper {
    public static final String OPPO_RES_PATH = "/system/framework/oppo-framework-res.apk";

    public static void addExtraAssetPaths(AssetManager assetManager) {
        assetManager.addAssetPath(OPPO_RES_PATH);
    }
}

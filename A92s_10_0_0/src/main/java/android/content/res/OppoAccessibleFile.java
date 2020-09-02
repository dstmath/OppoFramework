package android.content.res;

import android.content.res.OppoThemeZipFile;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipFile;
import oppo.util.OppoDisplayUtils;

public class OppoAccessibleFile extends OppoBaseFile {
    private static final String[] ASSET_FILES = {"accessible/res/values", "accessible/res/drawable"};
    private static final int INDEX_COLORS = 0;
    private static final int INDEX_XHDPI = 1;
    private static final int INDEX_XXHDPI = 2;

    public OppoAccessibleFile(String packageName, ColorBaseResourcesImpl baseResources) {
        super(packageName, baseResources, true, true, true);
    }

    public boolean initValue() {
        clearCache(null);
        loadAssetValues(0);
        return true;
    }

    public void clearCache(ZipFile zipFile) {
        clean(zipFile);
    }

    protected static synchronized OppoAccessibleFile getAssetFile(String packageName, ColorBaseResourcesImpl baseResources) {
        synchronized (OppoAccessibleFile.class) {
            if (!TextUtils.isEmpty(packageName)) {
                if (baseResources != null) {
                    String name = getPackageName(packageName);
                    String key = name + ":/" + "accessible";
                    WeakReference weakReference = (WeakReference) sCacheFiles.get(key);
                    OppoAccessibleFile assetsFile = null;
                    if (weakReference != null) {
                        assetsFile = (OppoAccessibleFile) weakReference.get();
                    }
                    if (assetsFile != null) {
                        return assetsFile;
                    }
                    OppoAccessibleFile assetsFile2 = new OppoAccessibleFile(name, baseResources);
                    sCacheFiles.put(key, new WeakReference(assetsFile2));
                    return assetsFile2;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public OppoThemeZipFile.ThemeFileInfo getAssetInputStream(int index, String path) {
        InputStream is;
        if (TextUtils.isEmpty(path) || path.endsWith(".xml")) {
            return null;
        }
        String path2 = "accessible" + "/" + OppoDisplayUtils.getDrawbleDensityFolder(sDensity) + path.substring(path.lastIndexOf("/"));
        InputStream is2 = getAssetPathStream(this.mResources.getAssets(), path2);
        if (is2 != null) {
            return new OppoThemeZipFile.ThemeFileInfo(is2, 0);
        }
        for (int i = 0; i < sDensities.length; i++) {
            String temp = "accessible" + "/" + OppoDisplayUtils.getDrawbleDensityFolder(sDensities[i]) + path2.substring(path2.lastIndexOf("/"));
            if (!path2.equalsIgnoreCase(temp) && (is = getAssetPathStream(this.mResources.getAssets(), temp)) != null) {
                OppoThemeZipFile.ThemeFileInfo themeFileInfo = new OppoThemeZipFile.ThemeFileInfo(is, 0);
                if (sDensities[i] <= 1) {
                    return themeFileInfo;
                }
                themeFileInfo.mDensity = sDensities[i];
                return themeFileInfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean hasDrawables() {
        AssetManager assets = this.mResources.getAssets();
        if (assets == null) {
            return false;
        }
        int i = 0;
        while (i < sDensities.length) {
            try {
                if (assets.list("accessible" + "/" + OppoDisplayUtils.getDrawbleDensityFolder(sDensities[i])).length > 0) {
                    return true;
                }
                i++;
            } catch (IOException e) {
                if (!DEBUG_THEME) {
                    return false;
                }
                Log.e("OppoBaseFile", "hasAssetDrawables: asset list exception " + e.toString());
                return false;
            }
        }
        return false;
    }

    private void loadAssetValues(int index) {
        if (hasAssetValues()) {
            parseXmlStream(index, new OppoThemeZipFile.ThemeFileInfo(getAssetPathStream(this.mResources.getAssets(), ASSET_FILES[index] + "/" + "colors.xml"), (long) index));
        }
    }

    private InputStream getAssetPathStream(AssetManager assets, String path) {
        if (assets == null) {
            return null;
        }
        try {
            return assets.open(path);
        } catch (IOException e) {
            if (!DEBUG_THEME) {
                return null;
            }
            Log.e("OppoBaseFile", "getAssetPathStream exception: " + e.toString());
            return null;
        }
    }

    private boolean hasAssetValues() {
        AssetManager assets = this.mResources.getAssets();
        if (assets == null) {
            return false;
        }
        try {
            String[] values = assets.list(ASSET_FILES[0]);
            for (String document : values) {
                if (!TextUtils.isEmpty(document) && document.endsWith("colors.xml")) {
                    return true;
                }
            }
        } catch (IOException e) {
            if (DEBUG_THEME) {
                Log.e("OppoBaseFile", "hasAssetValues: asset list exception " + e.toString());
            }
        }
        return false;
    }
}

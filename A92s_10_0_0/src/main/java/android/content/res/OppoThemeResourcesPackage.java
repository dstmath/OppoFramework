package android.content.res;

import android.content.res.OppoThemeResources;
import android.content.res.OppoThemeZipFile;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class OppoThemeResourcesPackage extends OppoThemeResources {
    private static final String TAG = "OppoThemeResourcesPackage";
    private static final Map<String, WeakReference<OppoThemeResourcesPackage>> sPackageResources = new HashMap();
    private final boolean DEBUG = true;

    public OppoThemeResourcesPackage(OppoThemeResourcesPackage themeResourcesPackage, ColorBaseResourcesImpl resources, String packageName, OppoThemeResources.MetaData metaData) {
        super(themeResourcesPackage, resources, packageName, metaData);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.content.res.OppoThemeResourcesPackage.getTopLevelThemeResources(android.content.res.ColorBaseResourcesImpl, java.lang.String):android.content.res.OppoThemeResourcesPackage
     arg types: [android.content.res.ColorBaseResourcesImpl, java.lang.String]
     candidates:
      android.content.res.OppoThemeResources.getTopLevelThemeResources(android.content.res.ColorBaseResourcesImpl, java.lang.String):android.content.res.OppoThemeResources
      android.content.res.OppoThemeResourcesPackage.getTopLevelThemeResources(android.content.res.ColorBaseResourcesImpl, java.lang.String):android.content.res.OppoThemeResourcesPackage */
    public static OppoThemeResourcesPackage getThemeResources(ColorBaseResourcesImpl resources, String packageName) {
        OppoThemeResourcesPackage themeResourcesPackage = null;
        synchronized (sPackageResources) {
            if (sPackageResources.containsKey(packageName)) {
                themeResourcesPackage = sPackageResources.get(packageName).get();
            }
            if (themeResourcesPackage == null) {
                themeResourcesPackage = getTopLevelThemeResources(resources, packageName);
                sPackageResources.put(packageName, new WeakReference<>(themeResourcesPackage));
            } else if (themeResourcesPackage.checkPackageNoExit()) {
                themeResourcesPackage.clearIfNeeded();
                sPackageResources.remove(packageName);
            }
        }
        return themeResourcesPackage;
    }

    @Override // android.content.res.OppoThemeResources
    public static OppoThemeResourcesPackage getTopLevelThemeResources(ColorBaseResourcesImpl resources, String pathName) {
        OppoThemeResourcesPackage themeResourcesPackage = null;
        for (int i = 0; i < THEME_PATHS.length; i++) {
            themeResourcesPackage = new OppoThemeResourcesPackage(themeResourcesPackage, resources, pathName, THEME_PATHS[i]);
        }
        return themeResourcesPackage;
    }

    @Override // android.content.res.OppoThemeResources
    public CharSequence getThemeCharSequence(int id) {
        CharSequence res = super.getThemeCharSequence(id);
        if (res != null || getSystem() == null) {
            return res;
        }
        return getSystem().getThemeCharSequence(id);
    }

    @Override // android.content.res.OppoThemeResources
    public OppoThemeZipFile.ThemeFileInfo getThemeFileStream(int index, String path) {
        return getPackageThemeFileStream(index, path);
    }

    /* access modifiers changed from: protected */
    @Override // android.content.res.OppoThemeResources
    public boolean isMutiPackage() {
        return true;
    }

    @Override // android.content.res.OppoThemeResources
    public void setResource(ColorBaseResourcesImpl res) {
        super.setResource(res);
    }
}

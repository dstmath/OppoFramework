package oppo.content.res;

import android.content.res.ResourcesImpl;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import oppo.content.res.OppoThemeZipFile.ThemeFileInfo;

public class OppoThemeResourcesPackage extends OppoThemeResources {
    private static final String TAG = "OppoThemeResourcesPackage";
    private static final Map<String, WeakReference<OppoThemeResourcesPackage>> sPackageResources = new HashMap();
    private final boolean DEBUG = true;

    public OppoThemeResourcesPackage(OppoThemeResourcesPackage themeResourcesPackage, ResourcesImpl resources, String packageName, MetaData metaData) {
        super(themeResourcesPackage, resources, packageName, metaData);
    }

    public static OppoThemeResourcesPackage getThemeResources(ResourcesImpl resources, String packageName) {
        OppoThemeResourcesPackage themeResourcesPackage = null;
        synchronized (sPackageResources) {
            if (sPackageResources.containsKey(packageName)) {
                themeResourcesPackage = (OppoThemeResourcesPackage) ((WeakReference) sPackageResources.get(packageName)).get();
            }
            if (themeResourcesPackage == null) {
                themeResourcesPackage = getTopLevelThemeResources(resources, packageName);
                sPackageResources.put(packageName, new WeakReference(themeResourcesPackage));
            }
        }
        return themeResourcesPackage;
    }

    public static OppoThemeResourcesPackage getTopLevelThemeResources(ResourcesImpl resources, String pathName) {
        OppoThemeResourcesPackage themeResourcesPackage = null;
        int i = 0;
        while (i < THEME_PATHS.length) {
            i++;
            themeResourcesPackage = new OppoThemeResourcesPackage(themeResourcesPackage, resources, pathName, THEME_PATHS[i]);
        }
        return themeResourcesPackage;
    }

    public CharSequence getThemeCharSequence(int id) {
        CharSequence res = super.getThemeCharSequence(id);
        if (res == null) {
            return OppoThemeResources.getSystem().getThemeCharSequence(id);
        }
        return res;
    }

    public ThemeFileInfo getThemeFileStream(int index, String path) {
        return getPackageThemeFileStream(index, path);
    }

    protected boolean isMutiPackage() {
        return true;
    }

    public void setResource(ResourcesImpl res) {
        super.setResource(res);
    }
}

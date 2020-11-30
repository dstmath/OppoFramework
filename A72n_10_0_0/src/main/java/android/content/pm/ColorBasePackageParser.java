package android.content.pm;

import android.app.IColorCommonInjector;
import android.common.OppoFeatureCache;
import android.content.pm.PackageParser;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

public abstract class ColorBasePackageParser {
    private static final String PACKAGE_OPPO = "oppo";

    /* access modifiers changed from: package-private */
    public void hookDispCompat(PackageParser.Package owner, float maxAspectRatio) {
        owner.applicationInfo.maxAspectRatio = maxAspectRatio;
    }

    /* access modifiers changed from: package-private */
    public void hookActivityAliasTheme(PackageParser.Activity a, Resources res, XmlResourceParser parser, PackageParser.Activity target) {
        ((IColorCommonInjector) OppoFeatureCache.getOrCreate(IColorCommonInjector.DEFAULT, new Object[0])).hookActivityAliasTheme(a, res, parser, target);
    }

    static String hookNameError(String pkgName, String nameError) {
        if ("oppo".equals(pkgName)) {
            return null;
        }
        return nameError;
    }
}

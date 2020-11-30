package oppo.android;

import android.app.Activity;
import android.app.Application;
import android.app.IColorCommonInjector;
import android.content.pm.PackageParser;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;

public class ColorDummyCommonInjector implements IColorCommonInjector {
    private static volatile ColorDummyCommonInjector sInstance = null;

    public static ColorDummyCommonInjector getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyCommonInjector.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyCommonInjector();
                }
            }
        }
        return sInstance;
    }

    ColorDummyCommonInjector() {
    }

    @Override // android.app.IColorCommonInjector
    public void onCreateForActivity(Activity activity, Bundle savedInstanceState) {
    }

    @Override // android.app.IColorCommonInjector
    public void onCreateForApplication(Application application) {
    }

    @Override // android.app.IColorCommonInjector
    public void onConfigurationChangedForApplication(Application application, Configuration newConfig) {
    }

    @Override // android.app.IColorCommonInjector
    public void applyConfigurationToResourcesForResourcesManager(Configuration config, int changes) {
    }

    @Override // android.app.IColorCommonInjector
    public void hookPreloadResources(Resources res, String tag) {
    }

    @Override // android.app.IColorCommonInjector
    public void hookActivityAliasTheme(PackageParser.Activity a, Resources res, XmlResourceParser parser, PackageParser.Activity target) {
    }
}

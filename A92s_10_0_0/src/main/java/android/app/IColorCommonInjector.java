package android.app;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.PackageParser;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;

public interface IColorCommonInjector extends IOppoCommonFeature {
    public static final IColorCommonInjector DEFAULT = new IColorCommonInjector() {
        /* class android.app.IColorCommonInjector.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorCommonInjector getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorCommonInjector;
    }

    default void onCreateForActivity(Activity activity, Bundle savedInstanceState) {
    }

    default void onCreateForApplication(Application application) {
    }

    default void applyConfigurationToResourcesForResourcesManager(Configuration config, int changes) {
    }

    default void onConfigurationChangedForApplication(Application application, Configuration newConfig) {
    }

    default void hookPreloadResources(Resources res, String tag) {
    }

    default void hookActivityAliasTheme(PackageParser.Activity a, Resources res, XmlResourceParser parser, PackageParser.Activity target) {
    }
}

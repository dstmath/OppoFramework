package oppo.android;

import android.app.Activity;
import android.app.Application;
import android.app.IColorCommonInjector;
import android.app.OppoThemeHelper;
import android.common.OppoFeatureCache;
import android.content.pm.PackageParser;
import android.content.res.Configuration;
import android.content.res.IColorThemeManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import com.color.font.IColorFontManager;
import com.oppo.internal.R;

public class ColorCommonInjector implements IColorCommonInjector {
    private static volatile ColorCommonInjector sInstance = null;

    public static ColorCommonInjector getInstance() {
        if (sInstance == null) {
            synchronized (ColorCommonInjector.class) {
                if (sInstance == null) {
                    sInstance = new ColorCommonInjector();
                }
            }
        }
        return sInstance;
    }

    ColorCommonInjector() {
    }

    public void onCreateForActivity(Activity activity, Bundle savedInstanceState) {
    }

    public void onConfigurationChangedForApplication(Application application, Configuration newConfig) {
    }

    public void onCreateForApplication(Application application) {
        if (application != null) {
            OppoFeatureCache.getOrCreate(IColorFontManager.DEFAULT, new Object[0]).setCurrentAppName(application.getPackageName());
        }
    }

    public void applyConfigurationToResourcesForResourcesManager(Configuration config, int changes) {
        OppoThemeHelper.handleExtraConfigurationChanges(changes);
        OppoFeatureCache.getOrCreate(IColorFontManager.DEFAULT, new Object[0]).updateTypefaceInCurrProcess(config);
        OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0]).updateExtraConfigForUxIcon(changes);
    }

    public void hookPreloadResources(Resources mResources, String tag) {
        long startTime = SystemClock.uptimeMillis();
        TypedArray ar = mResources.obtainTypedArray(201786385);
        int N = preloadDrawables(mResources, ar, tag);
        ar.recycle();
        Log.i(tag, "...preloaded " + N + " oppo drawable resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        long startTime2 = SystemClock.uptimeMillis();
        TypedArray ar2 = mResources.obtainTypedArray(201786386);
        int N2 = preloadColorStateLists(mResources, ar2, tag);
        ar2.recycle();
        Log.i(tag, "...preloaded " + N2 + " oppo color resources in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
    }

    public void hookActivityAliasTheme(PackageParser.Activity a, Resources res, XmlResourceParser parser, PackageParser.Activity target) {
        TypedArray sb = res.obtainAttributes(parser, R.styleable.AndroidManifestActivityAlias);
        a.info.theme = sb.getResourceId(0, target.info.theme);
        sb.recycle();
    }

    private int preloadDrawables(Resources mResources, TypedArray ar, String TAG) {
        int N = ar.length();
        for (int i = 0; i < N; i++) {
            int id = ar.getResourceId(i, 0);
            if (id != 0 && mResources.getDrawable(id, null) == null) {
                throw new IllegalArgumentException("Unable to find preloaded drawable resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    private int preloadColorStateLists(Resources mResources, TypedArray ar, String TAG) {
        int N = ar.length();
        for (int i = 0; i < N; i++) {
            int id = ar.getResourceId(i, 0);
            if (id != 0 && mResources.getColorStateList(id, null) == null) {
                throw new IllegalArgumentException("Unable to find preloaded color resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }
}

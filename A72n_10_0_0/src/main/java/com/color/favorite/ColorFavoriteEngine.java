package com.color.favorite;

import android.app.Activity;
import android.app.ColorUxIconConstants;
import android.content.Context;
import android.os.SystemProperties;
import android.view.View;
import com.color.util.ColorLog;

public abstract class ColorFavoriteEngine implements IColorFavoriteEngine {
    protected static final boolean DBG = IColorFavoriteManager.DBG;
    private static final boolean ENGINE_DEBUG = SystemProperties.getBoolean("feature.favorite.debug", false);
    private static final boolean ENGINE_LOG = SystemProperties.getBoolean("feature.favorite.log", false);
    private static final boolean ENGINE_TEST = SystemProperties.getBoolean("feature.favorite.test", false);
    protected final String TAG = getClass().getSimpleName();

    /* access modifiers changed from: protected */
    public abstract void onInit();

    /* access modifiers changed from: protected */
    public abstract void onLoadRule(Context context, String str, ColorFavoriteCallback colorFavoriteCallback);

    /* access modifiers changed from: protected */
    public abstract void onLogActivityInfo(Activity activity);

    /* access modifiers changed from: protected */
    public abstract void onLogViewInfo(View view);

    /* access modifiers changed from: protected */
    public abstract void onProcessClick(View view, ColorFavoriteCallback colorFavoriteCallback);

    /* access modifiers changed from: protected */
    public abstract void onProcessCrawl(View view, ColorFavoriteCallback colorFavoriteCallback);

    /* access modifiers changed from: protected */
    public abstract void onProcessSave(View view, ColorFavoriteCallback colorFavoriteCallback);

    /* access modifiers changed from: protected */
    public abstract void onRelease();

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void init() {
        boolean z = DBG;
        ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] init()");
        onInit();
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void release() {
        boolean z = DBG;
        ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] release()");
        onRelease();
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void loadRule(Context context, String data, ColorFavoriteCallback callback) {
        boolean z = DBG;
        ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] loadRule() : " + context.getPackageName() + ColorUxIconConstants.IconLoader.FILE_SEPARATOR + context);
        onLoadRule(context, data, callback);
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void processClick(View clickView, ColorFavoriteCallback callback) {
        boolean z = DBG;
        ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] processClick() : " + clickView);
        onProcessClick(clickView, callback);
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void processCrawl(View rootView, ColorFavoriteCallback callback) {
        boolean z = DBG;
        ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] processCrawl() : " + rootView);
        onProcessCrawl(rootView, callback);
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void processSave(View rootView, ColorFavoriteCallback callback) {
        boolean z = DBG;
        ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] processSave() : " + rootView);
        onProcessSave(rootView, callback);
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void logActivityInfo(Activity activity) {
        try {
            boolean z = DBG;
            ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] logActivityInfo() : " + activity);
        } catch (Exception e) {
            boolean z2 = DBG;
            ColorLog.w(z2, "AnteaterFavorite", "[" + this.TAG + "] logActivityInfo() exception e : " + e);
        }
        onLogActivityInfo(activity);
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final void logViewInfo(View view) {
        try {
            boolean z = DBG;
            ColorLog.i(z, "AnteaterFavorite", "[" + this.TAG + "] logViewInfo() : " + view);
        } catch (Exception e) {
            boolean z2 = DBG;
            ColorLog.w(z2, "AnteaterFavorite", "[" + this.TAG + "] logViewInfo() exception e : " + e);
        }
        onLogViewInfo(view);
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final boolean isDebugOn() {
        return ENGINE_DEBUG;
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final boolean isTestOn() {
        return ENGINE_TEST;
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public final boolean isLogOn() {
        return ENGINE_LOG;
    }
}

package com.color.darkmode;

import android.app.Application;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.NinePatch;
import android.graphics.OppoBaseBaseCanvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.InputStream;

public interface IColorDarkModeManager extends IOppoCommonFeature {
    public static final IColorDarkModeManager DEFAULT = new IColorDarkModeManager() {
        /* class com.color.darkmode.IColorDarkModeManager.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDarkModeManager;
    }

    @Override // android.common.IOppoCommonFeature
    default IColorDarkModeManager getDefault() {
        return DEFAULT;
    }

    default void init(Context context) {
    }

    default void clearCache() {
    }

    default boolean forceDarkAllowedDefault(Context context, boolean forceDarkAllowedDefault) {
        return forceDarkAllowedDefault;
    }

    default void handleStartingWindow(Context context, String appWindowToken, Window window, View decorView) {
    }

    default boolean isInUnOpenAppList(String packageName) {
        return true;
    }

    default void changeUsageForceDarkAlgorithmType(View view, int type) {
    }

    default int hideAutoChangeUiMode(int curMode) {
        return curMode;
    }

    default boolean useForcePowerSave() {
        return true;
    }

    default void logForceDarkAllowedStatus(Context context, boolean forceDarkAllowedDefault) {
    }

    default void logConfigurationNightError(Context context, boolean isNightConfiguration) {
    }

    default boolean forceDarkWithoutTheme(Context context, boolean useAutoDark) {
        return useAutoDark;
    }

    default boolean ensureHardWareWhenDarkMode(Context context, boolean defaultValue) {
        return defaultValue;
    }

    default int ensureLayerTypeWhenDarkMode(Context context, int defaultValue) {
        return defaultValue;
    }

    default void startDelayInjectJS(WebView webView) {
    }

    default WebViewClient createWebViewClientWrapper(WebView webView, WebViewClient client) {
        return client;
    }

    default void refreshForceDark(View decor) {
    }

    default void checkViewOnAttachWindow(View view) {
    }

    default void checkViewOnDraw(View view) {
    }

    default int changeSystemUiVisibility(int oldSystemUiVisibility) {
        return oldSystemUiVisibility;
    }

    default void refreshForceDark(View decor, boolean isUseColorForceDark) {
    }

    default void refreshForceDark(View decor, int result) {
    }

    default int getDarkModeData(String packageName) {
        return 0;
    }

    default boolean isDarkModePage(String packageName, boolean systemDarkMode) {
        return systemDarkMode;
    }

    default void handleDisplayMCAFeature(int mode) {
    }

    default void initDarkModeStatus(Application application) {
    }

    default int handleEraseColor(int color) {
        return color;
    }

    default boolean shouldIntercept() {
        return false;
    }

    default Bitmap handleDecodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
        return null;
    }

    default Shader getDarkModeLinearGradient(float mX0, float mY0, float mX1, float mY1, int[] mColors, float[] mPositions, int mColor0, int mColor1, Shader.TileMode tileMode) {
        return null;
    }

    default Shader getDarkModeRadialGradient(float mX, float mY, float mRadius, int[] mColors, int mCenterColor, float[] mPositions, int mEdgeColor, Shader.TileMode tileMode) {
        return null;
    }

    default Shader getDarkModeSweepGradient(float mCx, float mCy, int[] mColors, float[] mPositions, int mColor0, int mColor1) {
        return null;
    }

    default int getVectorColor(int color) {
        return color;
    }

    default void changeColorFilterInDarkMode(ColorFilter colorFilter) {
    }

    default boolean isInDarkMode(boolean isHardware) {
        return false;
    }

    default OppoBaseBaseCanvas.RealPaintState getRealPaintState(Paint paint) {
        return null;
    }

    default void changePaintWhenDrawText(Paint paint) {
    }

    default void resetRealPaintIfNeed(Paint paint, OppoBaseBaseCanvas.RealPaintState realPaintState) {
    }

    default void changePaintWhenDrawArea(Paint paint, RectF rectF) {
    }

    default void changePaintWhenDrawArea(Paint paint, RectF rectF, Path path) {
    }

    default void changePaintWhenDrawPatch(NinePatch patch, Paint paint, RectF rectF) {
    }

    default int changeWhenDrawColor(int color, boolean isDarkMode) {
        return color;
    }

    default void changePaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
    }

    default int[] getDarkModeColors(int[] colors) {
        return colors;
    }

    default Paint getPaintWhenDrawPatch(NinePatch patch, Paint paint, RectF rectF) {
        return null;
    }

    default Paint getPaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
        return null;
    }
}

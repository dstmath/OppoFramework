package com.color.darkmode;

import android.app.Application;
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

public class ColorDummyDarkModeManager implements IColorDarkModeManager {

    private static class Holder {
        /* access modifiers changed from: private */
        public static final ColorDummyDarkModeManager INSTANCE = new ColorDummyDarkModeManager();

        private Holder() {
        }
    }

    public static ColorDummyDarkModeManager getInstance() {
        return Holder.INSTANCE;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void init(Context context) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void clearCache() {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean forceDarkAllowedDefault(Context context, boolean forceDarkAllowedDefault) {
        return forceDarkAllowedDefault;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void handleStartingWindow(Context context, String appWindowToken, Window window, View decorView) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean isInUnOpenAppList(String packageName) {
        return true;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changeUsageForceDarkAlgorithmType(View view, int type) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int hideAutoChangeUiMode(int curMode) {
        return curMode;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean useForcePowerSave() {
        return true;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void logForceDarkAllowedStatus(Context context, boolean forceDarkAllowedDefault) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void logConfigurationNightError(Context context, boolean isNightConfiguration) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean forceDarkWithoutTheme(Context context, boolean useAutoDark) {
        return useAutoDark;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean ensureHardWareWhenDarkMode(Context context, boolean defaultValue) {
        return defaultValue;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int ensureLayerTypeWhenDarkMode(Context context, int defaultValue) {
        return defaultValue;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void startDelayInjectJS(WebView webView) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public WebViewClient createWebViewClientWrapper(WebView webView, WebViewClient client) {
        return client;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void checkViewOnAttachWindow(View view) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void checkViewOnDraw(View view) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void refreshForceDark(View decor) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int changeSystemUiVisibility(int oldSystemUiVisibility) {
        return oldSystemUiVisibility;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void refreshForceDark(View decor, boolean isUseColorForceDark) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int getDarkModeData(String packageName) {
        return 0;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void refreshForceDark(View decor, int result) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean isDarkModePage(String packageName, boolean systemDarkMode) {
        return systemDarkMode;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void initDarkModeStatus(Application application) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int handleEraseColor(int color) {
        return color;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean shouldIntercept() {
        return false;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public Bitmap handleDecodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
        return null;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public Shader getDarkModeLinearGradient(float mX0, float mY0, float mX1, float mY1, int[] mColors, float[] mPositions, int mColor0, int mColor1, Shader.TileMode tileMode) {
        return null;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public Shader getDarkModeRadialGradient(float mX, float mY, float mRadius, int[] mColors, int mCenterColor, float[] mPositions, int mEdgeColor, Shader.TileMode tileMode) {
        return null;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public Shader getDarkModeSweepGradient(float mCx, float mCy, int[] mColors, float[] mPositions, int mColor0, int mColor1) {
        return null;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int getVectorColor(int color) {
        return color;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changeColorFilterInDarkMode(ColorFilter colorFilter) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public boolean isInDarkMode(boolean isHardware) {
        return false;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public OppoBaseBaseCanvas.RealPaintState getRealPaintState(Paint paint) {
        return null;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changePaintWhenDrawText(Paint paint) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void resetRealPaintIfNeed(Paint paint, OppoBaseBaseCanvas.RealPaintState realPaintState) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changePaintWhenDrawArea(Paint paint, RectF rectF) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changePaintWhenDrawArea(Paint paint, RectF rectF, Path path) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changePaintWhenDrawPatch(NinePatch patch, Paint paint, RectF rectF) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int changeWhenDrawColor(int color, boolean isDarkMode) {
        return color;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public void changePaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public int[] getDarkModeColors(int[] colors) {
        return colors;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public Paint getPaintWhenDrawPatch(NinePatch patch, Paint paint, RectF rectF) {
        return null;
    }

    @Override // com.color.darkmode.IColorDarkModeManager
    public Paint getPaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
        return null;
    }
}

package android.common;

import android.app.IColorCommonInjector;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.res.ColorDummyThemeManager;
import android.content.res.IColorThemeManager;
import android.content.res.Resources;
import android.inputmethodservice.ColorDummyInputMethodServiceUtils;
import android.inputmethodservice.IColorInputMethodServiceUtils;
import android.text.ITextJustificationHooks;
import android.text.TextDummyJustificationHooks;
import android.util.Slog;
import android.view.ColorDummyAccidentallyTouchHelper;
import android.view.ColorDummyBurmeseZgHooks;
import android.view.ColorDummyDirectViewHelper;
import android.view.ColorDummyViewHooks;
import android.view.ColorDummyViewRootUtil;
import android.view.IColorAccidentallyTouchHelper;
import android.view.IColorBurmeseZgHooks;
import android.view.IColorDirectViewHelper;
import android.view.IColorViewHooks;
import android.view.IColorViewRootUtil;
import android.view.View;
import android.view.ViewRootImpl;
import android.widget.ColorDummyListHooks;
import android.widget.ColorDummyOverScrollerHelper;
import android.widget.ColorDummyTextViewRTLUtilForUG;
import android.widget.IColorFtHooks;
import android.widget.IColorListHooks;
import android.widget.IColorMagnifierHooks;
import android.widget.IColorOverScrollerHelper;
import android.widget.IColorTextViewRTLUtilForUG;
import android.widget.OverScroller;
import com.android.internal.app.ColorDummyAlertControllerEuclidManager;
import com.android.internal.app.ColorDummyResolverManager;
import com.android.internal.app.IColorAlertControllerEuclidManager;
import com.android.internal.app.IColorResolverManager;
import com.android.internal.widget.ColorDummyFtHooks;
import com.color.darkmode.ColorDummyDarkModeManager;
import com.color.darkmode.IColorDarkModeManager;
import com.color.favorite.ColorDummyFavoriteManager;
import com.color.favorite.IColorFavoriteManager;
import com.color.font.ColorDummyFontManager;
import com.color.font.IColorFontManager;
import com.color.screenshot.ColorDummyScreenShotEuclidManager;
import com.color.screenshot.IColorScreenShotEuclidManager;
import com.coloros.deepthinker.ColorDummyDeepThinkerManager;
import com.coloros.deepthinker.IColorDeepThinkerManager;
import java.lang.ref.WeakReference;
import oppo.android.ColorDummyCommonInjector;

public class ColorFrameworkFactory implements IOppoCommonFactory {
    public static final String COLOR_FRAMEWORK_FACTORY_IMPL_NAME = "oppo.android.ColorFrameworkFactoryImpl";
    private static final boolean DEBUG = true;
    private static final String TAG = "ColorFrameworkFactory";
    private static volatile ColorFrameworkFactory sInstance = null;

    public static ColorFrameworkFactory getInstance() {
        if (sInstance == null) {
            synchronized (ColorFrameworkFactory.class) {
                if (sInstance == null) {
                    try {
                        sInstance = (ColorFrameworkFactory) newInstance(COLOR_FRAMEWORK_FACTORY_IMPL_NAME);
                    } catch (Exception e) {
                        Slog.e(TAG, " Reflect exception getInstance: " + e.toString());
                        sInstance = new ColorFrameworkFactory();
                    }
                }
            }
        }
        return sInstance;
    }

    @Override // android.common.IOppoCommonFactory
    public boolean isValid(int index) {
        return index < OppoFeatureList.OppoIndex.EndColorFrameworkFactory.ordinal() && index > OppoFeatureList.OppoIndex.StartColorFrameworkFactory.ordinal();
    }

    static Object newInstance(String className) throws Exception {
        return Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
    }

    public IColorViewRootUtil getColorViewRootUtil() {
        warn("getColorViewRootUtil dummy");
        return new ColorDummyViewRootUtil();
    }

    public IColorFontManager getColorFontManager() {
        warn("getColorFontManager dummy");
        return ColorDummyFontManager.getInstance();
    }

    public IColorFavoriteManager getColorFavoriteManager() {
        warn("getColorFavoriteManager dummy");
        return ColorDummyFavoriteManager.getInstance();
    }

    public IColorDarkModeManager getColorDarkModeManager() {
        warn("getColorDarkModeManager dummy");
        return ColorDummyDarkModeManager.getInstance();
    }

    public IColorDarkModeManager newColorDarkModeManager() {
        warn("newColorDarkModeManager dummy");
        return new ColorDummyDarkModeManager();
    }

    public IColorDirectViewHelper getColorDirectViewHelper(WeakReference<ViewRootImpl> viewAncestor) {
        warn("getColorDirectViewHelper dummy");
        return new ColorDummyDirectViewHelper(viewAncestor);
    }

    public IColorCommonInjector getColorCommonInjector() {
        return ColorDummyCommonInjector.getInstance();
    }

    public IColorTextViewRTLUtilForUG getColorTextViewRTLUtilForUG() {
        return ColorDummyTextViewRTLUtilForUG.getInstance();
    }

    public IColorViewHooks getColorViewHooks(View view, Resources res) {
        return new ColorDummyViewHooks();
    }

    public IColorScreenShotEuclidManager getColorScreenShotEuclidManager() {
        return ColorDummyScreenShotEuclidManager.getInstance();
    }

    public IColorInputMethodServiceUtils getColorInputMethodServiceUtils() {
        warn("getColorViewRootUtil dummy");
        return new ColorDummyInputMethodServiceUtils();
    }

    public IColorResolverManager getColorResolverManager() {
        warn("getColorResolverManager dummy");
        return new ColorDummyResolverManager();
    }

    public IColorThemeManager getColorThemeManager() {
        warn("getColorThemeManager dummy");
        return ColorDummyThemeManager.getInstance();
    }

    public IColorAccidentallyTouchHelper getColorAccidentallyTouchHelper() {
        return ColorDummyAccidentallyTouchHelper.getInstance();
    }

    public IColorOverScrollerHelper getColorOverScrollerHelper(OverScroller overScroller) {
        warn("getColorOverScrollerHelper dummy");
        return new ColorDummyOverScrollerHelper(overScroller);
    }

    public IColorListHooks getColorListHooks() {
        return new ColorDummyListHooks();
    }

    public ITextJustificationHooks getTextJustificationHooks() {
        return new TextDummyJustificationHooks();
    }

    public IColorFtHooks getColorFtHooks() {
        return new ColorDummyFtHooks();
    }

    public IColorMagnifierHooks getColorMagnifierHooks() {
        return IColorMagnifierHooks.DEFAULT;
    }

    public IColorAlertControllerEuclidManager getColorAlertControllerEuclidManger() {
        return ColorDummyAlertControllerEuclidManager.getInstance();
    }

    public IColorDeepThinkerManager getColorDeepThinkerManager(Context context) {
        return ColorDummyDeepThinkerManager.getInstance(context);
    }

    public IColorBurmeseZgHooks getColorBurmeseZgFlagHooks() {
        return new ColorDummyBurmeseZgHooks();
    }

    /* access modifiers changed from: protected */
    public void warn(String methodName) {
        Slog.w(TAG, methodName);
    }
}

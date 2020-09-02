package oppo.android;

import android.app.ColorUxIconConstants;
import android.app.IColorCommonInjector;
import android.common.ColorFrameworkFactory;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.common.OppoFeatureManager;
import android.content.Context;
import android.content.res.ColorThemeManager;
import android.content.res.IColorThemeManager;
import android.content.res.Resources;
import android.inputmethodservice.ColorInputMethodServiceUtils;
import android.inputmethodservice.IColorInputMethodServiceUtils;
import android.text.ITextJustificationHooks;
import android.text.TextJustificationHooksImpl;
import android.util.Log;
import android.view.ColorAccidentallyTouchHelper;
import android.view.ColorBurmeseZgFlagHooksImpl;
import android.view.ColorDirectViewHelper;
import android.view.ColorViewHooksImp;
import android.view.ColorViewRootUtil;
import android.view.IColorAccidentallyTouchHelper;
import android.view.IColorBurmeseZgHooks;
import android.view.IColorDirectViewHelper;
import android.view.IColorViewHooks;
import android.view.IColorViewRootUtil;
import android.view.View;
import android.view.ViewRootImpl;
import android.widget.ColorListHooksImp;
import android.widget.ColorOverScrollerHelper;
import android.widget.ColorTextViewRTLUtilForUG;
import android.widget.IColorFtHooks;
import android.widget.IColorListHooks;
import android.widget.IColorOverScrollerHelper;
import android.widget.IColorTextViewRTLUtilForUG;
import android.widget.OverScroller;
import com.android.internal.app.ColorAlertControllerEuclidManger;
import com.android.internal.app.ColorResolverManager;
import com.android.internal.app.IColorAlertControllerEuclidManager;
import com.android.internal.app.IColorResolverManager;
import com.android.internal.widget.ColorFtHooksImpl;
import com.color.antivirus.ColorAntiVirusBehaviorManager;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.darkmode.ColorDarkModeManager;
import com.color.darkmode.IColorDarkModeManager;
import com.color.favorite.ColorFavoriteManager;
import com.color.favorite.IColorFavoriteManager;
import com.color.font.ColorFontManager;
import com.color.font.IColorFontManager;
import com.color.screenshot.ColorScreenShotEuclidManager;
import com.color.screenshot.IColorScreenShotEuclidManager;
import com.color.theme.ColorThemeStyle;
import com.coloros.deepthinker.AlgorithmBinderCode;
import com.coloros.deepthinker.ColorDeepThinkerManager;
import com.coloros.deepthinker.IColorDeepThinkerManager;
import com.oppo.theme.IColorThemeStyle;
import java.lang.ref.WeakReference;

public class ColorFrameworkFactoryImpl extends ColorFrameworkFactory {
    private static final boolean DEBUG = false;
    private static final String TAG = "ColorFrameworkFactoryImpl";

    public <T extends IOppoCommonFeature> T getFeature(T def, Object... vars) {
        verityParams(def);
        if (!OppoFeatureManager.isSupport(def)) {
            return def;
        }
        switch (AnonymousClass1.$SwitchMap$android$common$OppoFeatureList$OppoIndex[def.index().ordinal()]) {
            case 1:
                return OppoFeatureManager.getTraceMonitor(getColorThemeManager());
            case 2:
                return OppoFeatureManager.getTraceMonitor(getColorAccidentallyTouchHelper());
            case 3:
                return OppoFeatureManager.getTraceMonitor(getColorDirectViewHelper(vars));
            case 4:
                return OppoFeatureManager.getTraceMonitor(getColorViewHooks(vars));
            case AlgorithmBinderCode.BIND_EVENT_HANDLE:
                return OppoFeatureManager.getTraceMonitor(getColorCommonInjector());
            case 6:
                return OppoFeatureManager.getTraceMonitor(getColorViewRootUtil(vars));
            case 7:
                return OppoFeatureManager.getTraceMonitor(getColorFontManager(vars));
            case ColorUxIconConstants.IconTheme.THEME_MATERIAL_RADIUS_PX:
                return OppoFeatureManager.getTraceMonitor(getColorListHooks());
            case 9:
                return OppoFeatureManager.getTraceMonitor(getColorTextViewRTLUtilForUG());
            case 10:
                return OppoFeatureManager.getTraceMonitor(getColorOverScrollerHelper(vars));
            case 11:
                return OppoFeatureManager.getTraceMonitor(getColorFavoriteManager());
            case ColorUxIconConstants.IconTheme.ICON_RADIUS_BIT_LENGTH:
                return OppoFeatureManager.getTraceMonitor(getColorScreenShotEuclidManager());
            case 13:
                return OppoFeatureManager.getTraceMonitor(getColorDarkModeManager());
            case 14:
                return OppoFeatureManager.getTraceMonitor(getColorInputMethodServiceUtils());
            case 15:
                return OppoFeatureManager.getTraceMonitor(getColorResolverManager());
            case 16:
                return OppoFeatureManager.getTraceMonitor(getTextJustificationHooks());
            case 17:
                return OppoFeatureManager.getTraceMonitor(getColorFtHooks());
            case 18:
                return OppoFeatureManager.getTraceMonitor(getColorMagnifierHooks());
            case 19:
                return OppoFeatureManager.getTraceMonitor(getColorAlertControllerEuclidManger());
            case 20:
                return OppoFeatureManager.getTraceMonitor(getColorDeepThinkerManager(vars));
            case 21:
                return OppoFeatureManager.getTraceMonitor(getColorBurmeseZgFlagHooks());
            case 22:
                return OppoFeatureManager.getTraceMonitor(getColorThemeStyle(vars));
            case 23:
                return OppoFeatureManager.getTraceMonitor(getColorAntiVirusBehaviorManager(vars));
            default:
                Log.i(TAG, "Unknow feature:" + def.index().name());
                return def;
        }
    }

    /* renamed from: oppo.android.ColorFrameworkFactoryImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$common$OppoFeatureList$OppoIndex = new int[OppoFeatureList.OppoIndex.values().length];

        static {
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorThemeManager.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAccidentallyTouchHelper.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDirectViewHelper.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorViewHooks.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorCommonInjector.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorViewRootUtil.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorFontManager.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorListHooks.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorTextViewRTLUtilForUG.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorOverScrollerHelper.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorFavoriteManager.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorScreenShotEuclidManager.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDarkModeManager.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorInputMethodServiceUtils.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorResolverManager.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.ITextJustificationHooks.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorFtHooks.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorMagnifierHooks.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAlertControllerEuclidManager.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorDeepThinkerManager.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorBurmeseZgHooks.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorThemeStyle.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$android$common$OppoFeatureList$OppoIndex[OppoFeatureList.OppoIndex.IColorAntiVirusBehaviorManager.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
        }
    }

    private IColorViewRootUtil getColorViewRootUtil(Object... vars) {
        return ColorViewRootUtil.getInstance();
    }

    private IColorFontManager getColorFontManager(Object... vars) {
        return ColorFontManager.getInstance();
    }

    private IColorDirectViewHelper getColorDirectViewHelper(Object... vars) {
        return new ColorDirectViewHelper((WeakReference) vars[0]);
    }

    private IColorViewHooks getColorViewHooks(Object... vars) {
        verityParamsType("getColorViewHooks", vars, 2, new Class[]{View.class, Resources.class});
        return new ColorViewHooksImp((View) vars[0], (Resources) vars[1]);
    }

    private IColorOverScrollerHelper getColorOverScrollerHelper(Object... vars) {
        verityParamsType("getColorOverScrollerHelper", vars, 1, new Class[]{OverScroller.class});
        return new ColorOverScrollerHelper((OverScroller) vars[0]);
    }

    private IColorDeepThinkerManager getColorDeepThinkerManager(Object... vars) {
        return ColorDeepThinkerManager.getInstance((Context) vars[0]);
    }

    private IColorAntiVirusBehaviorManager getColorAntiVirusBehaviorManager(Object... vars) {
        return ColorAntiVirusBehaviorManager.getInstance();
    }

    public IColorViewRootUtil getColorViewRootUtil() {
        return ColorViewRootUtil.getInstance();
    }

    public IColorFontManager getColorFontManager() {
        return ColorFontManager.getInstance();
    }

    public IColorFavoriteManager getColorFavoriteManager() {
        return ColorFavoriteManager.getInstance();
    }

    public IColorDarkModeManager getColorDarkModeManager() {
        return ColorDarkModeManager.getInstance();
    }

    public IColorDarkModeManager newColorDarkModeManager() {
        return new ColorDarkModeManager();
    }

    public IColorDirectViewHelper getColorDirectViewHelper(WeakReference<ViewRootImpl> viewAncestor) {
        return new ColorDirectViewHelper(viewAncestor);
    }

    public IColorCommonInjector getColorCommonInjector() {
        return ColorCommonInjector.getInstance();
    }

    public IColorTextViewRTLUtilForUG getColorTextViewRTLUtilForUG() {
        return ColorTextViewRTLUtilForUG.getInstance();
    }

    public IColorViewHooks getColorViewHooks(View view, Resources res) {
        return new ColorViewHooksImp(view, res);
    }

    public IColorScreenShotEuclidManager getColorScreenShotEuclidManager() {
        return ColorScreenShotEuclidManager.getInstance();
    }

    public IColorInputMethodServiceUtils getColorInputMethodServiceUtils() {
        return new ColorInputMethodServiceUtils();
    }

    public IColorResolverManager getColorResolverManager() {
        return new ColorResolverManager();
    }

    public IColorThemeManager getColorThemeManager() {
        return ColorThemeManager.getInstance();
    }

    public IColorAccidentallyTouchHelper getColorAccidentallyTouchHelper() {
        return ColorAccidentallyTouchHelper.getInstance();
    }

    public IColorOverScrollerHelper getColorOverScrollerHelper(OverScroller overScroller) {
        return new ColorOverScrollerHelper(overScroller);
    }

    public IColorListHooks getColorListHooks() {
        return new ColorListHooksImp();
    }

    public ITextJustificationHooks getTextJustificationHooks() {
        return new TextJustificationHooksImpl();
    }

    public IColorFtHooks getColorFtHooks() {
        return new ColorFtHooksImpl();
    }

    public IColorAlertControllerEuclidManager getColorAlertControllerEuclidManger() {
        return ColorAlertControllerEuclidManger.getInstance();
    }

    public IColorDeepThinkerManager getColorDeepThinkerManager(Context context) {
        return ColorDeepThinkerManager.getInstance(context);
    }

    public IColorBurmeseZgHooks getColorBurmeseZgFlagHooks() {
        return new ColorBurmeseZgFlagHooksImpl();
    }

    public IColorThemeStyle getColorThemeStyle(Object... vars) {
        return new ColorThemeStyle();
    }
}

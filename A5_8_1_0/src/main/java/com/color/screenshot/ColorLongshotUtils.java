package com.color.screenshot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import com.color.content.ColorContext;
import com.color.feature.ColorDisableFeatures.SystemCenter;

public final class ColorLongshotUtils {
    public static final Intent INTENT_HOME = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME");
    public static final String PACKAGE_EXSERVICEUI = "com.coloros.exserviceui";
    public static final String PACKAGE_FLOATASSISTANT = "com.coloros.floatassistant";
    public static final String PACKAGE_GALLERY = "com.coloros.gallery3d";
    public static final String PACKAGE_SCREENSHOT = "com.coloros.screenshot";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";
    private static final String[] SYSTEMUI_BARS = new String[]{"TickerPanel"};
    private static final String TAG = "LongshotDump";
    public static final String TAG_LONGSHOT = "Screenshot";
    public static final int VALUE_FIVE = 5;
    public static final int VALUE_FOUR = 4;
    public static final int VALUE_THREE = 3;
    public static final int VALUE_TWO = 2;
    public static final String VIEW_DECOR = "com.android.internal.policy.impl.PhoneWindow$DecorView";
    public static final String VIEW_NAVIGATIONBAR = "com.android.systemui.statusbar.phone.NavigationBarView";
    public static final String VIEW_STATUSBAR = "com.android.systemui.statusbar.phone.StatusBarWindowView";

    public static ColorScreenshotManager getScreenshotManager(Context context) {
        return (ColorScreenshotManager) context.getSystemService(ColorContext.SCREENSHOT_SERVICE);
    }

    public static boolean isDisabled(Context context) {
        return context.getPackageManager().hasSystemFeature(SystemCenter.LONGSHOT);
    }

    public static boolean isHomeApp(Context context) {
        String packageName = context.getPackageName();
        for (ResolveInfo ri : context.getPackageManager().queryIntentActivities(INTENT_HOME, 65536)) {
            if (packageName.equals(ri.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExServiceUiApp(String owningPackage) {
        return isPackage(owningPackage, PACKAGE_EXSERVICEUI);
    }

    public static boolean isExServiceUiApp(Context context) {
        return isExServiceUiApp(context.getPackageName());
    }

    public static boolean isScreenshotApp(String owningPackage) {
        return isPackage(owningPackage, PACKAGE_SCREENSHOT);
    }

    public static boolean isScreenshotApp(Context context) {
        return isScreenshotApp(context.getPackageName());
    }

    public static boolean isSystemUiApp(String owningPackage) {
        return isPackage(owningPackage, PACKAGE_SYSTEMUI);
    }

    public static boolean isSystemUiApp(Context context) {
        return isSystemUiApp(context.getPackageName());
    }

    public static boolean isDecorView(Object view) {
        return view.getClass().getName().equals(VIEW_DECOR);
    }

    public static boolean isStatusBarView(Object view) {
        return view.getClass().getName().equals(VIEW_STATUSBAR);
    }

    public static boolean isNavigationBarView(Object view) {
        return view.getClass().getName().equals(VIEW_NAVIGATIONBAR);
    }

    public static boolean isStatusBar(int type) {
        return 2000 == type;
    }

    public static boolean isNavigationBar(int type) {
        return LayoutParams.TYPE_NAVIGATION_BAR == type;
    }

    public static boolean isSystemUiBar(int type, CharSequence title) {
        if (isNavigationBar(type) || isStatusBar(type)) {
            return true;
        }
        if (!TextUtils.isEmpty(title)) {
            for (String name : SYSTEMUI_BARS) {
                if (name.equals(title)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTickerPanel(String owningPackage, CharSequence title) {
        if (isSystemUiApp(owningPackage)) {
            return "TickerPanel".equals(title);
        }
        return false;
    }

    public static boolean isSystemWindow(String owningPackage, CharSequence title, int type) {
        if (isExServiceUiApp(owningPackage)) {
            return true;
        }
        if (isSystemUiApp(owningPackage)) {
            return isSystemUiBar(type, title);
        }
        return false;
    }

    public static boolean isAllSystemWindow(String owningPackage, CharSequence title, int type) {
        if (isScreenshotApp(owningPackage)) {
            return true;
        }
        return isSystemWindow(owningPackage, title, type);
    }

    public static boolean isInputMethodWindow(int windowType, CharSequence title) {
        switch (windowType) {
            case LayoutParams.TYPE_INPUT_METHOD /*2011*/:
                return "InputMethod".equals(title);
            default:
                return false;
        }
    }

    public static String getBaseClassNameOf(View view) {
        AccessibilityNodeInfo node = null;
        try {
            node = view.createAccessibilityNodeInfo();
        } catch (Exception e) {
        }
        if (node == null) {
            return null;
        }
        CharSequence className = node.getClassName();
        try {
            node.recycle();
        } catch (Exception e2) {
        }
        if (className == null) {
            return null;
        }
        return className.toString();
    }

    public static boolean isWebFromBaseName(String baseClassName) {
        if (baseClassName == null) {
            return false;
        }
        return baseClassName.contains("WebView") || baseClassName.contains("webkit");
    }

    public static boolean isWebContent(String className) {
        if (className != null && className.startsWith("org.chromium.content.browser.") && className.endsWith("ContentView")) {
            return true;
        }
        return false;
    }

    public static boolean canScrollVertically(View view) {
        try {
            return testScrollVertically(view);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean canScrollVerticallyForward(ColorLongshotViewBase view) {
        return view.canLongScroll();
    }

    public static boolean canScrollVerticallyWithPadding(ColorLongshotViewBase view, int padding) {
        boolean z = false;
        int offset = view.computeLongScrollOffset();
        int range = view.computeLongScrollRange() - view.computeLongScrollExtent();
        if (range == 0) {
            return false;
        }
        if (offset < (range - 1) + padding) {
            z = true;
        }
        return z;
    }

    public static boolean isFloatAssistant(String owningPackage) {
        return isPackage(owningPackage, PACKAGE_FLOATASSISTANT);
    }

    public static boolean isGallery(String owningPackage) {
        return isPackage(owningPackage, PACKAGE_GALLERY);
    }

    private static boolean isPackage(String owningPackage, String packageName) {
        return packageName.equals(owningPackage);
    }

    private static boolean testScrollVertically(View view) {
        if (view.canScrollVertically(1) || view.canScrollVertically(-1)) {
            return true;
        }
        return false;
    }
}

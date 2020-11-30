package com.color.screenshot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import com.color.content.ColorContext;
import com.color.feature.ColorDisableFeatures;
import com.color.util.ColorLog;
import com.color.view.ColorWindowUtils;

public final class ColorLongshotUtils {
    public static final Intent INTENT_HOME = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
    public static final String PACKAGE_EXSERVICEUI = "com.coloros.exserviceui";
    public static final String PACKAGE_FLOATASSISTANT = "com.coloros.floatassistant";
    public static final String PACKAGE_GALLERY = "com.coloros.gallery3d";
    public static final String PACKAGE_SCREENSHOT = "com.coloros.screenshot";
    public static final String PACKAGE_SYSTEMUI = "com.android.systemui";
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
        return context.getPackageManager().hasSystemFeature(ColorDisableFeatures.SystemCenter.LONGSHOT);
    }

    public static boolean isInstalled(Context context) {
        StringBuilder sb;
        boolean result = false;
        try {
            context.getPackageManager().getPackageInfo("com.coloros.screenshot", 4);
            result = true;
            sb = new StringBuilder();
        } catch (PackageManager.NameNotFoundException e) {
            sb = new StringBuilder();
        } catch (Throwable th) {
            ColorLog.w("LongshotDump", "isInstalled : false");
            throw th;
        }
        sb.append("isInstalled : ");
        sb.append(result);
        ColorLog.w("LongshotDump", sb.toString());
        return result;
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
        return ColorWindowUtils.isExServiceUiApp(owningPackage);
    }

    public static boolean isExServiceUiApp(Context context) {
        return isExServiceUiApp(context.getPackageName());
    }

    public static boolean isScreenshotApp(String owningPackage) {
        return ColorWindowUtils.isScreenshotApp(owningPackage);
    }

    public static boolean isScreenshotApp(Context context) {
        return isScreenshotApp(context.getPackageName());
    }

    public static boolean isSystemUiApp(String owningPackage) {
        return ColorWindowUtils.isSystemUiApp(owningPackage);
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
        return ColorWindowUtils.isStatusBar(type);
    }

    public static boolean isNavigationBar(int type) {
        return ColorWindowUtils.isNavigationBar(type);
    }

    public static boolean isSystemUiBar(int type, CharSequence title) {
        return ColorWindowUtils.isSystemUiBar(type, title);
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
        return ColorWindowUtils.isInputMethodWindow(windowType, title);
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
        if (!baseClassName.contains("WebView") && !baseClassName.contains("webkit")) {
            return false;
        }
        return true;
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
        int offset = view.computeLongScrollOffset();
        int range = view.computeLongScrollRange() - view.computeLongScrollExtent();
        if (range != 0 && offset < (range - 1) + padding) {
            return true;
        }
        return false;
    }

    public static boolean isFloatAssistant(String owningPackage) {
        return ColorWindowUtils.isFloatAssistant(owningPackage);
    }

    public static boolean isGallery(String owningPackage) {
        return ColorWindowUtils.isGallery(owningPackage);
    }

    private static boolean testScrollVertically(View view) {
        if (!view.canScrollVertically(1) && !view.canScrollVertically(-1)) {
            return false;
        }
        return true;
    }
}

package com.color.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import com.oppo.internal.R;

public class ColorContextUtil {
    private static final String METADATA_STYLE_TITLE = "color.support.options";
    private static final String METADATA_STYLE_VALUE = "true";
    private static final String TAG = "ColorContextUtil";
    private static boolean sIsColorStyleActivity;
    private static boolean sIsColorStyleApplication;
    private static boolean sIsColorStyleApplicationAssigned;
    private static int sLastActivityHash;
    private final boolean mIsColorStyle;
    private final boolean mIsOppoStyle;

    public ColorContextUtil(Context context) {
        this.mIsOppoStyle = isOppoStyle(context);
        this.mIsColorStyle = isColorStyle(context, this.mIsOppoStyle);
    }

    public boolean isOppoStyle() {
        return this.mIsOppoStyle;
    }

    public boolean isColorStyle() {
        return this.mIsColorStyle;
    }

    public static boolean isOppoStyle(Context context) {
        if (context == null) {
            return false;
        }
        TypedArray a = context.getTheme().obtainStyledAttributes(R.styleable.OppoTheme);
        boolean isOppoTheme = a.getBoolean(4, false);
        a.recycle();
        return isOppoTheme;
    }

    public static Context getOppoThemeContext(Context context) {
        return isOppoStyle(context) ? context : new ContextThemeWrapper(context, 201523202);
    }

    public static int getResId(Context context, int id) {
        TypedValue value = new TypedValue();
        context.getResources().getValue(id, value, true);
        return value.resourceId;
    }

    public static boolean isColorStyle(Context context, boolean isOppoStyle) {
        if (isOppoStyle) {
            return true;
        }
        if (context == null) {
            return false;
        }
        boolean isColorStyle = isColorStyleInApplication(context);
        if (isColorStyle) {
            return true;
        }
        while (true) {
            try {
                if (!(context instanceof ContextWrapper)) {
                    break;
                } else if (context instanceof Activity) {
                    break;
                } else {
                    Context ctx = ((ContextWrapper) context).getBaseContext();
                    if (context == ctx) {
                        break;
                    }
                    context = ctx;
                }
            } catch (Exception e) {
                ColorLog.e(TAG, e.toString());
            }
        }
        if (context instanceof Activity) {
            return isColorStyleInActivity(context);
        }
        return isColorStyle;
    }

    private static boolean isColorStyleInActivity(Context context) {
        try {
            if (sLastActivityHash == context.hashCode()) {
                return sIsColorStyleActivity;
            }
            ActivityInfo info = context.getPackageManager().getActivityInfo(((Activity) context).getComponentName(), 128);
            if (info.metaData != null && METADATA_STYLE_VALUE.equals(info.metaData.getString(METADATA_STYLE_TITLE))) {
                sIsColorStyleActivity = true;
                sLastActivityHash = context.hashCode();
                return true;
            }
            sIsColorStyleActivity = false;
            sLastActivityHash = context.hashCode();
            return false;
        } catch (Exception e) {
            ColorLog.e(TAG, e.toString());
        }
    }

    private static boolean isColorStyleInApplication(Context context) {
        try {
            if (sIsColorStyleApplicationAssigned) {
                return sIsColorStyleApplication;
            }
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfoAsUser(context.getPackageName(), 128, UserHandle.getCallingUserId());
            if (!(appInfo == null || appInfo.metaData == null || !METADATA_STYLE_VALUE.equals(appInfo.metaData.getString(METADATA_STYLE_TITLE)))) {
                sIsColorStyleApplication = true;
                sIsColorStyleApplicationAssigned = true;
                return true;
            }
            sIsColorStyleApplication = false;
            sIsColorStyleApplicationAssigned = true;
            return false;
        } catch (Exception e) {
            ColorLog.e(TAG, e.toString());
        } catch (AbstractMethodError e2) {
            ColorLog.e(TAG, "AbstraceMethod not implemented by App : " + e2);
        }
    }

    public static boolean isColorStyle(Context context) {
        return isColorStyle(context, isOppoStyle(context));
    }

    public static TypedArray getWindowStyle(Context context) {
        return context.obtainStyledAttributes(android.R.styleable.Window);
    }

    public static Activity getActivityContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            Context base = ((ContextWrapper) context).getBaseContext();
            if (base == context) {
                return null;
            }
            context = base;
        }
        return null;
    }

    public static String getActivityContextName(Context context) {
        Activity activity = getActivityContext(context);
        if (activity == null) {
            return null;
        }
        return activity.getClass().getName();
    }

    public static int getAttrColor(Context context, int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }
}

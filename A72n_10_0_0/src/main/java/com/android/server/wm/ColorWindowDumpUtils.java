package com.android.server.wm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.color.direct.ColorDirectUtils;
import com.color.util.ColorLog;

public class ColorWindowDumpUtils {
    private static final float SMALL_WINDOW_RATE = 4.7f;
    private final DisplayMetrics mDispMetrics = new DisplayMetrics();
    private final DisplayMetrics mRealMetrics = new DisplayMetrics();
    private final Resources mResources;

    public ColorWindowDumpUtils(Context context) {
        this.mResources = context.getResources();
    }

    public DisplayMetrics getDispMetrics(DisplayContent displayContent) {
        displayContent.getDisplay().getMetrics(this.mDispMetrics);
        return this.mDispMetrics;
    }

    public DisplayMetrics getRealMetrics(DisplayContent displayContent) {
        displayContent.getDisplay().getRealMetrics(this.mRealMetrics);
        return this.mRealMetrics;
    }

    public int getStatusBarHeight() {
        return this.mResources.getDimensionPixelSize(201654274);
    }

    public String getOwningPackage(WindowState w) {
        WindowState parentWindow;
        String owningPackage = w.getOwningPackage();
        if (owningPackage != null || (parentWindow = w.getParentWindow()) == null) {
            return owningPackage;
        }
        return parentWindow.getOwningPackage();
    }

    public CharSequence getWindowTitle(WindowState w) {
        return w.mAttrs.getTitle();
    }

    public CharSequence getWindowName(WindowState w) {
        CharSequence title = getWindowTitle(w);
        if (TextUtils.isEmpty(title)) {
            return getOwningPackage(w);
        }
        return title;
    }

    public boolean isSmallFloatWindow(WindowState w, DisplayMetrics dipMetrics) {
        Rect contentFrame = w.getContentFrameLw();
        contentFrame.width();
        if (!isSmallSize(contentFrame.width(), dipMetrics.widthPixels) && !isSmallSize(contentFrame.height(), dipMetrics.heightPixels)) {
            return false;
        }
        return true;
    }

    public boolean isTouchableWindow(WindowState w) {
        return !hasFlags(w, 16);
    }

    public boolean isSecureWindow(WindowState w) {
        return hasFlags(w, 8192);
    }

    public boolean isFullScreenWindow(WindowState w, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusBarHeight) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        boolean z = ColorDirectUtils.DBG;
        ColorLog.d(z, "DirectService", "width:" + width + "-->dispMetrics.widthPixels:" + dispMetrics.widthPixels + "-->realMetrics.widthPixels:" + realMetrics.widthPixels + "-->contentFrame.bottom:" + contentFrame.bottom + "-->dispMetrics.heightPixels:" + dispMetrics.heightPixels + "-->realMetrics.heightPixels:" + realMetrics.heightPixels + "-->contentFrame.top:" + contentFrame.top + "-->statusBarHeight:" + statusBarHeight);
        if (width != dispMetrics.widthPixels && width != realMetrics.widthPixels) {
            return false;
        }
        if (dispMetrics.heightPixels == realMetrics.heightPixels) {
            if ((contentFrame.top == 0 || contentFrame.top == statusBarHeight) && contentFrame.bottom == dispMetrics.heightPixels) {
                return true;
            }
        } else if (contentFrame.height() == dispMetrics.heightPixels) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x010d A[RETURN] */
    public boolean isFullScreenWithColorFullDisplay(WindowState w, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusBarHeight, int rotation, int colorFullDisplayWidth, int colorFullDisplayHeight) {
        Rect contentFrame = w.getContentFrameLw();
        int width = contentFrame.width();
        boolean z = ColorDirectUtils.DBG;
        ColorLog.d(z, "DirectService", "width:" + width + "-->dispMetrics.widthPixels:" + dispMetrics.widthPixels + "-->realMetrics.widthPixels:" + realMetrics.widthPixels + "-->contentFrame.bottom:" + contentFrame.bottom + "-->dispMetrics.heightPixels:" + dispMetrics.heightPixels + "-->realMetrics.heightPixels:" + realMetrics.heightPixels + "-->contentFrame.top:" + contentFrame.top + "-->statusBarHeight:" + statusBarHeight + "-->rotation:" + rotation + "-->colorFullDisplayWidth:" + colorFullDisplayWidth + "-->colorFullDisplayHeight:" + colorFullDisplayHeight);
        if (rotation != 0) {
            if (rotation != 1) {
                if (rotation != 2) {
                    if (rotation != 3) {
                        return false;
                    }
                    if (contentFrame.right + statusBarHeight != dispMetrics.widthPixels && contentFrame.right + colorFullDisplayWidth + statusBarHeight != realMetrics.widthPixels) {
                        return false;
                    }
                    if (contentFrame.left != colorFullDisplayWidth && contentFrame.left != (realMetrics.widthPixels - dispMetrics.widthPixels) + colorFullDisplayWidth) {
                        return false;
                    }
                    if (contentFrame.bottom != dispMetrics.heightPixels && contentFrame.bottom != realMetrics.heightPixels) {
                        return false;
                    }
                    if (contentFrame.top == 0 || contentFrame.top == statusBarHeight) {
                        return true;
                    }
                    return false;
                }
            } else if (contentFrame.right + colorFullDisplayWidth != dispMetrics.widthPixels && contentFrame.right + colorFullDisplayWidth != realMetrics.widthPixels) {
                return false;
            } else {
                if (contentFrame.bottom != dispMetrics.heightPixels && contentFrame.bottom != realMetrics.heightPixels) {
                    return false;
                }
                if (contentFrame.top != 0 && contentFrame.top != statusBarHeight) {
                    return false;
                }
                if (contentFrame.left != 0 && contentFrame.left != statusBarHeight) {
                    return false;
                }
            }
            return true;
        }
        if (width != dispMetrics.widthPixels && width != realMetrics.widthPixels) {
            return false;
        }
        if (contentFrame.bottom + colorFullDisplayHeight != dispMetrics.heightPixels && contentFrame.bottom + colorFullDisplayHeight != realMetrics.heightPixels) {
            return false;
        }
        if (contentFrame.top != 0 && contentFrame.top != statusBarHeight) {
            return false;
        }
    }

    public int getWindowHeight(WindowState w) {
        return w.getContentFrameLw().height();
    }

    public int getWindowWidth(WindowState w) {
        return w.getContentFrameLw().width();
    }

    private boolean isSmallSize(int value, int total) {
        return ((float) value) < ((float) total) / SMALL_WINDOW_RATE;
    }

    private boolean hasFlags(WindowState w, int flags) {
        return (w.mAttrs.flags & flags) == flags;
    }
}

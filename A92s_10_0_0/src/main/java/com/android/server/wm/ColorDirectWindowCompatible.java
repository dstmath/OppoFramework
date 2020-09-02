package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.color.view.ColorWindowUtils;
import java.util.function.Consumer;

public class ColorDirectWindowCompatible {
    private static final String TAG = "DirectService";
    private final float HALFSIZE = 0.5f;
    private int mColorFullScreenDisplayHeight = 0;
    private boolean mColorFullScreenDisplayShow = false;
    private int mColorFullScreenDisplayWidth = 0;
    private CharSequence mCurrentPkg = "";
    private ColorDirectMainWindow mMainWindow = null;
    private final ColorWindowDumpUtils mUtils;

    public ColorDirectWindowCompatible(Context context) {
        this.mUtils = new ColorWindowDumpUtils(context);
    }

    public DisplayMetrics getDispMetrics(DisplayContent displayContent) {
        return this.mUtils.getDispMetrics(displayContent);
    }

    public DisplayMetrics getRealMetrics(DisplayContent displayContent) {
        return this.mUtils.getRealMetrics(displayContent);
    }

    public int getStatusBarHeight() {
        return this.mUtils.getStatusBarHeight();
    }

    public ColorDirectMainWindow traversalWindows(DisplayContent displayContent, DisplayMetrics dispMetrics, DisplayMetrics realMetrics, int statusbarHeight, ColorWindowTraversalListener listener) {
        this.mMainWindow = null;
        this.mCurrentPkg = "";
        displayContent.forAllWindows(new Consumer(dispMetrics, listener, displayContent.getRotation(), realMetrics, statusbarHeight) {
            /* class com.android.server.wm.$$Lambda$ColorDirectWindowCompatible$JEcpJ2ceKoHkJb7aQAHKUy0VrNo */
            private final /* synthetic */ DisplayMetrics f$1;
            private final /* synthetic */ ColorWindowTraversalListener f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ DisplayMetrics f$4;
            private final /* synthetic */ int f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ColorDirectWindowCompatible.this.lambda$traversalWindows$0$ColorDirectWindowCompatible(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, (WindowState) obj);
            }
        }, true);
        this.mColorFullScreenDisplayShow = false;
        return this.mMainWindow;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x007d, code lost:
        if (r12 != 3) goto L_0x0092;
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0096  */
    public /* synthetic */ void lambda$traversalWindows$0$ColorDirectWindowCompatible(DisplayMetrics dispMetrics, ColorWindowTraversalListener listener, int rotation, DisplayMetrics realMetrics, int statusbarHeight, WindowState w) {
        CharSequence winName = this.mUtils.getWindowName(w);
        if (this.mMainWindow != null) {
            if (!TextUtils.equals(this.mCurrentPkg, winName) || isSmallHalfWindow(w, dispMetrics, listener) || !isSmallHalfWindow(this.mMainWindow.getMainWindow(), dispMetrics, listener)) {
                listener.printWindow("not MainDisplayWindow  ", winName);
                return;
            }
            listener.printWindow("previous is small window,find next MainDisplayWindow ", winName);
        }
        if (w.mClient == null) {
            listener.printWindow("null WindowClient      ", winName);
        } else if (!w.isVisible()) {
            listener.printWindow("not Visible    ", winName);
        } else if (!w.isOnScreen()) {
            listener.printWindow("not onScreen    ", winName);
        } else {
            WindowManager.LayoutParams attrs = w.getAttrs();
            if (attrs != null && 2100 == attrs.type) {
                String windowTitle = attrs.getTitle().toString();
                if ("SagAreaWindow".equals(windowTitle)) {
                    listener.printWindow("is SagAreaWindow ", winName);
                    return;
                }
                if (rotation != 0) {
                    if (rotation != 1) {
                        if (rotation != 2) {
                        }
                    }
                    this.mColorFullScreenDisplayShow = "HColorFullScreenDisplay".equals(windowTitle);
                    if (this.mColorFullScreenDisplayShow) {
                        this.mColorFullScreenDisplayHeight = this.mUtils.getWindowHeight(w);
                        this.mColorFullScreenDisplayWidth = this.mUtils.getWindowWidth(w);
                    }
                    listener.printWindow("is ColorFullScreenDisplay ", winName);
                }
                this.mColorFullScreenDisplayShow = "VColorFullScreenDisplay".equals(windowTitle);
                if (this.mColorFullScreenDisplayShow) {
                }
                listener.printWindow("is ColorFullScreenDisplay ", winName);
            } else if (!w.isReadyForDisplay()) {
                listener.printWindow("not ReadyForDisplay    ", winName);
            } else if (!this.mUtils.isTouchableWindow(w)) {
                listener.printWindow("not TouchableWindow    ", winName);
            } else {
                CharSequence winTitle = this.mUtils.getWindowTitle(w);
                if (ColorWindowUtils.isInputMethodWindow(w.mAttrs.type, winTitle)) {
                    listener.printWindow("is  InputMethodWindow  ", winName);
                    return;
                }
                String owningPackage = this.mUtils.getOwningPackage(w);
                if (ColorWindowUtils.isScreenshotApp(owningPackage)) {
                    listener.printWindow("is  ScreenshotApp      ", winName);
                } else if (ColorWindowUtils.isExServiceUiApp(owningPackage)) {
                    listener.printWindow("is  ExServiceUiApp     ", winName);
                } else {
                    if (ColorWindowUtils.isSystemUiApp(owningPackage)) {
                        if (ColorWindowUtils.isStatusBar(w.mAttrs.type)) {
                            if (w.mAttrs.height != -1) {
                                listener.printWindow("is  SystemUi StatusBar ", winName);
                                return;
                            } else if (ColorWindowUtils.isSystemHeadsUp(w.mAttrs)) {
                                listener.printWindow("is  SystemUi StatusBar ", "HeadsUp");
                                return;
                            } else {
                                listener.printWindow("is  SystemUi StatusBar ", winName);
                            }
                        } else if (ColorWindowUtils.isSystemUiBar(w.mAttrs.type, winTitle)) {
                            listener.printWindow("is  SystemUi SystemWindow", winName);
                            return;
                        } else if (!this.mUtils.isFullScreenWindow(w, dispMetrics, realMetrics, statusbarHeight)) {
                            listener.printWindow("is  SystemUi NoFullWindow", winName);
                            return;
                        }
                    }
                    if (ColorWindowUtils.isFloatAssistant(owningPackage)) {
                        if (this.mUtils.isSmallFloatWindow(w, dispMetrics)) {
                            listener.printWindow("is  SmallFloatAssistant", winName);
                            return;
                        } else if (w.mAttrs.type >= 2000) {
                            listener.printWindow("is ExpandFloatAssistant", winName);
                            return;
                        } else {
                            listener.printWindow("is  FloatAssistWindow  ", winName);
                        }
                    }
                    if (ColorWindowUtils.isDirectApp(owningPackage)) {
                        listener.printWindow("is  isDirectApp        ", winName);
                    } else if (w.mAppToken == null) {
                        listener.printWindow("not App window  ", winName);
                    } else {
                        WindowManager.LayoutParams attrs2 = w.getAttrs();
                        if (attrs2 == null || 1 == attrs2.type || 2 == attrs2.type) {
                            listener.printWindow("is  MainDisplayWindow  ", winName);
                            this.mMainWindow = new ColorDirectMainWindow(w);
                            this.mCurrentPkg = winName;
                            return;
                        }
                        listener.printWindow("not Activity window  ", winName);
                    }
                }
            }
        }
    }

    private boolean isSmallHalfWindow(WindowState w, DisplayMetrics dipMetrics, ColorWindowTraversalListener listener) {
        Rect contentFrame = w.getContentFrameLw();
        float result = (((float) (contentFrame.width() * contentFrame.height())) * 1.0f) / ((float) (dipMetrics.widthPixels * dipMetrics.heightPixels));
        listener.printWindow("isSmallHalfWindow result:", "" + result);
        return result < 0.5f;
    }
}

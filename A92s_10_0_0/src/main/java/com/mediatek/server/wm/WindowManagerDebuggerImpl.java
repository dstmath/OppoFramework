package com.mediatek.server.wm;

import android.os.Build;
import android.util.Slog;
import android.view.WindowManager;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerDebugConfig;
import com.android.server.wm.WindowManagerService;
import java.io.PrintWriter;
import java.lang.reflect.Field;

public class WindowManagerDebuggerImpl extends WindowManagerDebugger {
    private static final String TAG = "WindowManagerDebuggerImpl";

    public WindowManagerDebuggerImpl() {
        WMS_DEBUG_ENG = "eng".equals(Build.TYPE);
        WMS_DEBUG_USER = true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00e5, code lost:
        r7[r13].setAccessible(r10);
        r15 = r7[r13];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00ec, code lost:
        if (r3 != r10) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00ef, code lost:
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        r15.setBoolean(null, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0116, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0117, code lost:
        r14 = true;
     */
    public void runDebug(PrintWriter pw, String[] args, int opti) {
        int mode;
        int opti2;
        boolean z;
        int opti3 = opti;
        String cmd = "help";
        if (opti3 < args.length) {
            cmd = args[opti3];
            opti3++;
        }
        if ("help".equals(cmd)) {
            mode = 0;
            pw.println("Window manager debug options:");
            pw.println("  -d enable <zone zone ...> : enable the debug zone");
            pw.println("  -d disable <zone zone ...> : disable the debug zone");
            pw.println("zone may be some of:");
            pw.println("  a[all]");
        } else if ("enable".equals(cmd)) {
            mode = 1;
        } else if ("disable".equals(cmd)) {
            mode = 2;
        } else {
            pw.println("Unknown debug argument: " + cmd + "; use \"-d help\" for help");
            return;
        }
        boolean setAll = false;
        Field[] fields = WindowManagerDebugConfig.class.getDeclaredFields();
        Field[] fieldsPolicy = PhoneWindowManager.class.getDeclaredFields();
        while (!setAll) {
            if (mode == 0 || opti3 < args.length) {
                if (opti3 < args.length) {
                    cmd = args[opti3];
                    opti2 = opti3 + 1;
                } else {
                    opti2 = opti3;
                }
                boolean z2 = true;
                setAll = mode == 0 || "all".equals(cmd) || "a".equals(cmd);
                int i = 0;
                while (i < fields.length) {
                    String name = fields[i].getName();
                    if (name == null) {
                        z = z2;
                    } else if (name.contains("DEBUG") || name.contains("SHOW") || name.equals("localLOGV")) {
                        if (!setAll) {
                            try {
                                if (!name.equals(cmd)) {
                                    z = z2;
                                }
                            } catch (IllegalAccessException e) {
                                e = e;
                                z = z2;
                                Slog.e(TAG, name + " setBoolean failed", e);
                                i++;
                                z2 = z;
                            }
                        }
                        if (mode != 0) {
                            fields[i].setAccessible(z2);
                            fields[i].setBoolean(null, mode == z2 ? z2 : false);
                            if (name.equals("localLOGV")) {
                                WindowManagerService.localLOGV = mode == z2 ? z2 : false;
                            }
                            int j = 0;
                            while (true) {
                                if (j >= fieldsPolicy.length) {
                                    break;
                                } else if (fieldsPolicy[j].getName().equals(name)) {
                                    break;
                                } else {
                                    j++;
                                    z2 = true;
                                }
                            }
                        }
                        Object[] objArr = new Object[2];
                        objArr[0] = name;
                        z = true;
                        try {
                            objArr[1] = Boolean.valueOf(fields[i].getBoolean(null));
                            pw.println(String.format("  %s = %b", objArr));
                        } catch (IllegalAccessException e2) {
                            e = e2;
                        }
                    } else {
                        z = z2;
                    }
                    i++;
                    z2 = z;
                }
                opti3 = opti2;
            } else {
                return;
            }
        }
    }

    public void debugInterceptKeyBeforeQueueing(String tag, int keycode, boolean interactive, boolean keyguardActive, int policyFlags, boolean down, boolean canceled, boolean isWakeKey, boolean screenshotChordVolumeDownKeyTriggered, int result, boolean useHapticFeedback, boolean isInjected) {
        Slog.d(tag, "interceptKeyTq keycode=" + keycode + " interactive=" + interactive + " keyguardActive=" + keyguardActive + " policyFlags=" + Integer.toHexString(policyFlags) + " down =" + down + " canceled = " + canceled + " isWakeKey=" + isWakeKey + " mVolumeDownKeyTriggered =" + screenshotChordVolumeDownKeyTriggered + " result = " + result + " useHapticFeedback = " + useHapticFeedback + " isInjected = " + isInjected);
    }

    public void debugApplyPostLayoutPolicyLw(String tag, WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs, WindowManagerPolicy.WindowState mTopFullscreenOpaqueWindowState, WindowManagerPolicy.WindowState attached, WindowManagerPolicy.WindowState imeTarget, boolean dreamingLockscreen, boolean showingDream) {
        Slog.i(tag, "applyPostLayoutPolicyLw Win " + win + ": win.isVisibleLw()=" + win.isVisibleLw() + ", win.hasDrawnLw()=" + win.hasDrawnLw() + ", win.isDrawnLw()=" + win.isDrawnLw() + ", attrs.type=" + attrs.type + ", attrs.privateFlags=#" + Integer.toHexString(attrs.privateFlags) + ", mTopFullscreenOpaqueWindowState=" + mTopFullscreenOpaqueWindowState + ", win.isGoneForLayoutLw()=" + win.isGoneForLayoutLw() + ", attached=" + attached + ", imeTarget=" + imeTarget + ", isFullscreen=" + attrs.isFullscreen() + ", normallyFullscreenWindows=, mDreamingLockscreen=" + dreamingLockscreen + ", mShowingDream=" + showingDream);
    }

    public void debugLayoutWindowLw(String tag, int adjust, int type, int fl, boolean canHideNavigationBar, int sysUiFl) {
        Slog.v(tag, "layoutWindowLw : sim=#" + Integer.toHexString(adjust) + ", type=" + type + ", flag=" + fl + ", canHideNavigationBar=" + canHideNavigationBar + ", sysUiFl=" + sysUiFl);
    }

    public void debugGetOrientation(String tag, boolean displayFrozen, int lastWindowForcedOrientation, int lastKeyguardForcedOrientation) {
        Slog.v(tag, "Checking window orientation: mDisplayFrozen=" + displayFrozen + ", mLastWindowForcedOrientation=" + lastWindowForcedOrientation + ", mLastKeyguardForcedOrientation=" + lastKeyguardForcedOrientation);
    }

    public void debugGetOrientingWindow(String tag, WindowManagerPolicy.WindowState w, WindowManager.LayoutParams attrs, boolean isVisible, boolean policyVisibilityAfterAnim, int policyVisibility, boolean destroying) {
        Slog.v(tag, w + " screenOrientation=" + attrs.screenOrientation + ", visibility=" + isVisible + ", mPolicyVisibilityAfterAnim=" + policyVisibilityAfterAnim + ", mPolicyVisibility=" + policyVisibility + ", destroying=" + destroying);
    }

    public void debugPrepareSurfaceLocked(String tag, boolean isWallpaper, WindowManagerPolicy.WindowState win, boolean wallpaperVisible, boolean isOnScreen, int policyVisibility, boolean hasSurface, boolean destroying, boolean lastHidden) {
        Slog.v(tag, win + " prepareSurfaceLocked , mIsWallpaper=" + isWallpaper + ", mWin.mWallpaperVisible=" + wallpaperVisible + ", w.isOnScreen=" + isOnScreen + ", w.mPolicyVisibility=" + policyVisibility + ", w.mHasSurface=" + hasSurface + ", w.mDestroying=" + destroying + ", mLastHidden=" + lastHidden);
    }

    public void debugRelayoutWindow(String tag, WindowManagerPolicy.WindowState win, int originType, int changeType) {
        Slog.e(tag, "Window : " + win + "changes the window type!!\nOriginal type : " + originType + "\nChanged type : " + changeType);
    }

    public void debugInputAttr(String tag, WindowManager.LayoutParams attrs) {
        Slog.v(tag, "Input attr :" + attrs);
    }

    public void debugViewVisibility(String tag, WindowManagerPolicy.WindowState win, int viewVisibility, int oldVisibility, boolean focusMayChange) {
        if (viewVisibility == 0 && oldVisibility != 0) {
            Slog.i(tag, "Relayout " + win + ": oldVis=" + oldVisibility + " newVis=" + viewVisibility + " focusMayChange = " + focusMayChange);
        }
    }
}

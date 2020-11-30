package com.android.server.wm;

import android.app.ActivityOptions;
import android.common.OppoFeatureCache;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.util.Slog;
import com.color.zoomwindow.ColorZoomWindowManager;

public class ColorZoomStarter {
    private static final String TAG = "ColorZoomStarter";
    private final ActivityTaskManagerService mAtms;

    public ColorZoomStarter(ActivityTaskManagerService atms) {
        this.mAtms = atms;
    }

    public int startZoomWindow(Intent intent, Bundle bOptions, int userId, String callPkg) {
        Throwable th;
        int userId2;
        Throwable th2;
        int i;
        ActivityOptions options;
        ActivityStack zoomStack;
        ActivityRecord r;
        int callPid = Binder.getCallingPid();
        int callUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mAtms.mGlobalLock) {
                try {
                    i = userId;
                    try {
                        userId2 = this.mAtms.handleIncomingUser(callPid, callUid, i, "startZoomWindow");
                        try {
                            options = ActivityOptions.makeBasic();
                        } catch (Throwable th3) {
                            th2 = th3;
                            try {
                                throw th2;
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                    } catch (Throwable th5) {
                        th2 = th5;
                        userId2 = i;
                        throw th2;
                    }
                    try {
                        int flag = bOptions.getInt("extra_window_mode");
                        int windowMode = ActivityOptions.fromBundle(bOptions).getLaunchWindowingMode();
                        if (!(flag == ColorZoomWindowManager.WINDOWING_MODE_ZOOM_LEGACY || windowMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM)) {
                            if (flag != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                                if (flag == ColorZoomWindowManager.WINDOWING_MODE_ZOOM_TO_FULLSCREEN) {
                                    windowMode = 1;
                                    options.setLaunchWindowingMode(1);
                                }
                                zoomStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1);
                                if (windowMode != ColorZoomWindowManager.WINDOWING_MODE_ZOOM && zoomStack != null) {
                                    zoomStack.startPausingLocked(false, true, (ActivityRecord) null, false);
                                    ActivityStack nextStack = this.mAtms.mRootActivityContainer.getNextFocusableStack(zoomStack, true);
                                    if (!(nextStack == null || (r = nextStack.topRunningActivityLocked()) == null || !r.moveFocusableActivityToTop("startZoomWindow"))) {
                                        zoomStack.setWindowingMode(1);
                                    }
                                } else if (flag == ColorZoomWindowManager.WINDOWING_MODE_ZOOM_TO_FULLSCREEN && zoomStack != null) {
                                    OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).updateZoomStack(zoomStack, options, (ActivityRecord) null, (ActivityRecord) null);
                                    Slog.w(TAG, "change zoomStack to full screen");
                                    Binder.restoreCallingIdentity(origId);
                                    return 0;
                                }
                                try {
                                } catch (Throwable th6) {
                                    th2 = th6;
                                    throw th2;
                                }
                                try {
                                } catch (Throwable th7) {
                                    th2 = th7;
                                    throw th2;
                                }
                                try {
                                    int result = this.mAtms.getActivityStartController().obtainStarter(intent, "startZoomWindow").setCallingUid(callUid).setCallingPid(callPid).setCallingPackage(callPkg).setResolvedType((String) null).setStartFlags(0).setActivityOptions(options.toBundle()).setMayWait(userId2).execute();
                                    Binder.restoreCallingIdentity(origId);
                                    return result;
                                } catch (Throwable th8) {
                                    th2 = th8;
                                    throw th2;
                                }
                            }
                        }
                        windowMode = ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
                        options.setLaunchWindowingMode(ColorZoomWindowManager.WINDOWING_MODE_ZOOM);
                        zoomStack = this.mAtms.mRootActivityContainer.getDefaultDisplay().getStack(ColorZoomWindowManager.WINDOWING_MODE_ZOOM, 1);
                        if (windowMode != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
                        }
                        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).updateZoomStack(zoomStack, options, (ActivityRecord) null, (ActivityRecord) null);
                        Slog.w(TAG, "change zoomStack to full screen");
                        Binder.restoreCallingIdentity(origId);
                        return 0;
                    } catch (Throwable th9) {
                        th2 = th9;
                        throw th2;
                    }
                } catch (Throwable th10) {
                    th2 = th10;
                    i = userId;
                    userId2 = i;
                    throw th2;
                }
            }
        } catch (Throwable th11) {
            th = th11;
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }
}

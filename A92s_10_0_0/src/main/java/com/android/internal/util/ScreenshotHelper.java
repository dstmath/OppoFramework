package com.android.internal.util;

import android.app.job.JobInfo;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.InputDevice;
import com.color.screenshot.IColorScreenShotEuclidManager;

public class ScreenshotHelper {
    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER = "com.android.systemui.screenshot.ScreenshotServiceErrorReceiver";
    private static final String SYSUI_SCREENSHOT_SERVICE = "com.android.systemui.screenshot.TakeScreenshotService";
    private static final String TAG = "ScreenshotHelper";
    private final int SCREENSHOT_TIMEOUT_MS = 10000;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public ServiceConnection mScreenshotConnection = null;
    /* access modifiers changed from: private */
    public final Object mScreenshotLock = new Object();

    public ScreenshotHelper(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0067, code lost:
        return;
     */
    public void takeScreenshot(final int screenshotType, final boolean hasStatus, final boolean hasNav, final Handler handler) {
        synchronized (this.mScreenshotLock) {
            try {
                if (!((IColorScreenShotEuclidManager) OppoFeatureCache.getOrCreate(IColorScreenShotEuclidManager.DEFAULT, new Object[0])).takeScreenshot(this.mContext, screenshotType, hasStatus, hasNav, handler)) {
                    if (this.mScreenshotConnection == null) {
                        ComponentName serviceComponent = new ComponentName("com.android.systemui", SYSUI_SCREENSHOT_SERVICE);
                        Intent serviceIntent = new Intent();
                        final Runnable mScreenshotTimeout = new Runnable() {
                            /* class com.android.internal.util.ScreenshotHelper.AnonymousClass1 */

                            public void run() {
                                synchronized (ScreenshotHelper.this.mScreenshotLock) {
                                    if (ScreenshotHelper.this.mScreenshotConnection != null) {
                                        ScreenshotHelper.this.mContext.unbindService(ScreenshotHelper.this.mScreenshotConnection);
                                        ServiceConnection unused = ScreenshotHelper.this.mScreenshotConnection = null;
                                        ScreenshotHelper.this.notifyScreenshotError();
                                    }
                                }
                            }
                        };
                        serviceIntent.setComponent(serviceComponent);
                        ServiceConnection conn = new ServiceConnection() {
                            /* class com.android.internal.util.ScreenshotHelper.AnonymousClass2 */

                            @Override // android.content.ServiceConnection
                            public void onServiceConnected(ComponentName name, IBinder service) {
                                synchronized (ScreenshotHelper.this.mScreenshotLock) {
                                    if (ScreenshotHelper.this.mScreenshotConnection == this) {
                                        Messenger messenger = new Messenger(service);
                                        Message msg = Message.obtain((Handler) null, screenshotType);
                                        msg.replyTo = new Messenger(new Handler(handler.getLooper()) {
                                            /* class com.android.internal.util.ScreenshotHelper.AnonymousClass2.AnonymousClass1 */

                                            @Override // android.os.Handler
                                            public void handleMessage(Message msg) {
                                                synchronized (ScreenshotHelper.this.mScreenshotLock) {
                                                    if (ScreenshotHelper.this.mScreenshotConnection == this) {
                                                        ScreenshotHelper.this.mContext.unbindService(ScreenshotHelper.this.mScreenshotConnection);
                                                        ServiceConnection unused = ScreenshotHelper.this.mScreenshotConnection = null;
                                                        handler.removeCallbacks(mScreenshotTimeout);
                                                    }
                                                }
                                            }
                                        });
                                        int i = 1;
                                        msg.arg1 = hasStatus ? 1 : 0;
                                        if (!hasNav) {
                                            i = 0;
                                        }
                                        msg.arg2 = i;
                                        try {
                                            messenger.send(msg);
                                        } catch (RemoteException e) {
                                            Log.e(ScreenshotHelper.TAG, "Couldn't take screenshot: " + e);
                                        }
                                    }
                                }
                            }

                            @Override // android.content.ServiceConnection
                            public void onServiceDisconnected(ComponentName name) {
                                synchronized (ScreenshotHelper.this.mScreenshotLock) {
                                    if (ScreenshotHelper.this.mScreenshotConnection != null) {
                                        ScreenshotHelper.this.mContext.unbindService(ScreenshotHelper.this.mScreenshotConnection);
                                        ServiceConnection unused = ScreenshotHelper.this.mScreenshotConnection = null;
                                        handler.removeCallbacks(mScreenshotTimeout);
                                        ScreenshotHelper.this.notifyScreenshotError();
                                    }
                                }
                            }
                        };
                        if (this.mContext.bindServiceAsUser(serviceIntent, conn, InputDevice.SOURCE_HDMI, UserHandle.CURRENT)) {
                            this.mScreenshotConnection = conn;
                            handler.postDelayed(mScreenshotTimeout, JobInfo.MIN_BACKOFF_MILLIS);
                        }
                    }
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyScreenshotError() {
        ComponentName errorComponent = new ComponentName("com.android.systemui", SYSUI_SCREENSHOT_ERROR_RECEIVER);
        Intent errorIntent = new Intent(Intent.ACTION_USER_PRESENT);
        errorIntent.setComponent(errorComponent);
        errorIntent.addFlags(335544320);
        this.mContext.sendBroadcastAsUser(errorIntent, UserHandle.CURRENT);
    }
}

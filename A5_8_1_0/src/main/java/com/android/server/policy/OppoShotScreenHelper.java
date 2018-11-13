package com.android.server.policy;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.view.OppoScreenShotUtil;
import android.view.WindowManagerPolicy.WindowState;
import com.android.server.am.OppoFreeFormManagerService;

public class OppoShotScreenHelper {
    protected Context mContext;
    private Handler mHandler;
    private WindowState mNavigationBar = null;
    ServiceConnection mScreenshotConnection = null;
    final Object mScreenshotLock = new Object();
    final Runnable mScreenshotTimeout = new Runnable() {
        public void run() {
            synchronized (OppoShotScreenHelper.this.mScreenshotLock) {
                if (OppoShotScreenHelper.this.mScreenshotConnection != null) {
                    OppoShotScreenHelper.this.mContext.unbindService(OppoShotScreenHelper.this.mScreenshotConnection);
                    OppoShotScreenHelper.this.mScreenshotConnection = null;
                }
            }
        }
    };
    private int mShotDirection = 0;
    private WindowState mStatusBar = null;

    public class ShotScreenObserver extends ContentObserver {
        private static final String OPPO_SMART_APPERCEIVE_DOUBLE_VOLUME = "oppo_double_finger_control_volume_enabled";
        private static final String OPPO_SMART_SCREEN_CAPTURE = "oppo_smart_apperceive_screen_capture";
        private int mSmartDoubleVolume = -1;
        private int mSmartScreenCapture = -1;

        public ShotScreenObserver(Context context, Handler handler) {
            super(handler);
            OppoShotScreenHelper.this.mContext = context;
        }

        void observe() {
            ContentResolver resolver = OppoShotScreenHelper.this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor(OPPO_SMART_SCREEN_CAPTURE), false, this);
            resolver.registerContentObserver(System.getUriFor(OPPO_SMART_APPERCEIVE_DOUBLE_VOLUME), false, this);
            updateService();
        }

        public void onChange(boolean selfChange) {
            updateService();
        }

        private void updateService() {
            ContentResolver resolver = OppoShotScreenHelper.this.mContext.getContentResolver();
            int smartScreenCapture = System.getInt(resolver, OPPO_SMART_SCREEN_CAPTURE, 0);
            if (this.mSmartScreenCapture == 1 && smartScreenCapture == 0) {
                OppoScreenShotUtil.resumeDeliverPointerEvent();
            }
            this.mSmartScreenCapture = smartScreenCapture;
            int smartDoubleVolume = System.getInt(resolver, OPPO_SMART_APPERCEIVE_DOUBLE_VOLUME, 0);
            if (this.mSmartDoubleVolume == 1 && smartScreenCapture == 0) {
                OppoScreenShotUtil.resumeDeliverPointerEvent();
            }
        }
    }

    public void init(Handler handler, Context context) {
        this.mHandler = handler;
        this.mContext = context;
        new ShotScreenObserver(context, handler).observe();
    }

    /* JADX WARNING: Missing block: B:12:0x0047, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void takeScreenshot() {
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName(OppoFreeFormManagerService.FREEFORM_CALLER_PKG, "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            intent.putExtra("direction", this.mShotDirection);
            intent.putExtra("entry", "ThreeFingers");
            ServiceConnection conn = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (OppoShotScreenHelper.this.mScreenshotLock) {
                        if (OppoShotScreenHelper.this.mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 1);
                        msg.replyTo = new Messenger(new Handler(OppoShotScreenHelper.this.mHandler.getLooper()) {
                            public void handleMessage(Message msg) {
                                synchronized (OppoShotScreenHelper.this.mScreenshotLock) {
                                    if (OppoShotScreenHelper.this.mScreenshotConnection == this) {
                                        OppoShotScreenHelper.this.mContext.unbindService(OppoShotScreenHelper.this.mScreenshotConnection);
                                        OppoShotScreenHelper.this.mScreenshotConnection = null;
                                        OppoShotScreenHelper.this.mHandler.removeCallbacks(OppoShotScreenHelper.this.mScreenshotTimeout);
                                    }
                                }
                            }
                        });
                        msg.arg2 = 0;
                        msg.arg1 = 0;
                        if (OppoShotScreenHelper.this.mStatusBar != null && OppoShotScreenHelper.this.mStatusBar.isVisibleLw()) {
                            msg.arg1 = 1;
                        }
                        if (OppoShotScreenHelper.this.mNavigationBar != null && OppoShotScreenHelper.this.mNavigationBar.isVisibleLw()) {
                            msg.arg2 = 1;
                        }
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }

                public void onServiceDisconnected(ComponentName name) {
                }
            };
            if (this.mContext.bindService(intent, conn, 1)) {
                this.mScreenshotConnection = conn;
                this.mHandler.postDelayed(this.mScreenshotTimeout, 10000);
            }
        }
    }

    public void shotScreen(WindowState statusBar, WindowState navigationBar, int direction, boolean isGlobalActionVisible, boolean isLandscape) {
        this.mStatusBar = statusBar;
        this.mNavigationBar = navigationBar;
        this.mShotDirection = direction;
        takeScreenshot();
    }
}

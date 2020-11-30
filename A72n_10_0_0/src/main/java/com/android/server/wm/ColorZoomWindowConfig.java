package com.android.server.wm;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.FgThread;
import com.color.app.IColorZoomWindowConfigChangedListener;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorZoomWindowConfig {
    public static final ArrayList<String> IGNORE_INPUTSHOWN_FORRESULT_LIST = new ArrayList<>(Arrays.asList("com.whatsapp/.TextStatusComposerActivity"));
    private static final String TAG = "ColorZoomWindowConfig";
    private static volatile ColorZoomWindowConfig sConfig = null;
    private static boolean sDebugSwitch = IColorZoomWindowManager.sDebugfDetail;
    private ColorZoomWindowRUSConfig mColorZoomWindowRUSConfig = new ColorZoomWindowRUSConfig();
    private final Object mConfigLock = new Object();
    private final OnConfigChangeListeners mOnConfigChangeListeners = new OnConfigChangeListeners(FgThread.get().getLooper());

    private ColorZoomWindowConfig() {
    }

    public static ColorZoomWindowConfig getInstance() {
        if (sConfig == null) {
            synchronized (ColorZoomWindowConfig.class) {
                if (sConfig == null) {
                    sConfig = new ColorZoomWindowConfig();
                }
            }
        }
        return sConfig;
    }

    public boolean addConfigChangedListener(IColorZoomWindowConfigChangedListener listener) {
        if (sDebugSwitch) {
            Slog.i(TAG, "addConfigChangedListener listener = " + listener);
        }
        synchronized (this.mConfigLock) {
            if (this.mOnConfigChangeListeners == null) {
                return false;
            }
            this.mOnConfigChangeListeners.addListenerLocked(listener);
            return true;
        }
    }

    public boolean removeConfigChangedListener(IColorZoomWindowConfigChangedListener listener) {
        if (sDebugSwitch) {
            Slog.i(TAG, "removeConfigChangedListener listener = " + listener);
        }
        synchronized (this.mConfigLock) {
            if (this.mOnConfigChangeListeners == null) {
                return false;
            }
            this.mOnConfigChangeListeners.removeListenerLocked(listener);
            return true;
        }
    }

    public ColorZoomWindowRUSConfig getZoomWindowConfig() {
        ColorZoomWindowRUSConfig colorZoomWindowRUSConfig;
        if (sDebugSwitch) {
            Slog.i(TAG, "getZoomWindowConfig start");
        }
        synchronized (this.mConfigLock) {
            colorZoomWindowRUSConfig = this.mColorZoomWindowRUSConfig;
        }
        return colorZoomWindowRUSConfig;
    }

    public void setZoomWindowConfig(ColorZoomWindowRUSConfig config) {
        if (sDebugSwitch) {
            Slog.i(TAG, "setZoomWindowConfig start");
        }
        synchronized (this.mConfigLock) {
            this.mColorZoomWindowRUSConfig = config;
            if (sDebugSwitch) {
                Slog.i(TAG, "mColorZoomWindowRUSConfig: " + this.mColorZoomWindowRUSConfig.toString());
            }
            if (this.mOnConfigChangeListeners != null) {
                this.mOnConfigChangeListeners.onConfigSwitchChanged(this.mColorZoomWindowRUSConfig.getZoomWindowSwitch());
                this.mOnConfigChangeListeners.onConfigTypeChanged(3);
            }
            ColorZoomWindowRUSConfigManager.getInstance().scheduleWriteConfig(config);
        }
        if (sDebugSwitch) {
            Slog.i(TAG, "setZoomWindowConfig end");
        }
    }

    public List<String> getUnSupportCpnList() {
        synchronized (this.mConfigLock) {
            List<String> unSupportCpnList = this.mColorZoomWindowRUSConfig.getUnSupportCpnList();
            if (unSupportCpnList != null) {
                return unSupportCpnList;
            }
            return new ArrayList();
        }
    }

    public List<String> getUnReusedActivityInZoomModeList() {
        List<String> unReusedCpnList;
        synchronized (this.mConfigLock) {
            unReusedCpnList = new ArrayList<>();
            unReusedCpnList.add("com.tencent.mobileqq/.activity.SplashActivity");
            unReusedCpnList.add("com.tencent.mobileqq/.activity.ChatActivity");
        }
        return unReusedCpnList;
    }

    public List<String> getIgnoreInputShownForResultList() {
        List<String> list;
        synchronized (this.mConfigLock) {
            list = new ArrayList<>();
            list.addAll(IGNORE_INPUTSHOWN_FORRESULT_LIST);
        }
        return list;
    }

    public List<String> getConfigList(int type) {
        List<String> replyPkgList;
        if (sDebugSwitch) {
            Slog.i(TAG, "getConfigList type = " + type);
        }
        synchronized (this.mConfigLock) {
            if (type == 1) {
                try {
                    List<String> pkgList = this.mColorZoomWindowRUSConfig.getPkgList();
                    if (pkgList != null) {
                        return pkgList;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (type == 3 && (replyPkgList = this.mColorZoomWindowRUSConfig.getReplyPkgList()) != null) {
                return replyPkgList;
            }
            return new ArrayList();
        }
    }

    public boolean isZoomWindowEnabled() {
        boolean zoomWindowSwitch;
        if (sDebugSwitch) {
            Slog.i(TAG, "isEnabled mZoomSwitch = " + this.mColorZoomWindowRUSConfig.getZoomWindowSwitch());
        }
        synchronized (this.mConfigLock) {
            zoomWindowSwitch = this.mColorZoomWindowRUSConfig.getZoomWindowSwitch();
        }
        return zoomWindowSwitch;
    }

    public void init() {
        if (sDebugSwitch) {
            Slog.d(TAG, "init");
        }
        synchronized (this.mConfigLock) {
            this.mColorZoomWindowRUSConfig = ColorZoomWindowRUSConfigManager.getInstance().readZoomWindowConfig();
        }
        if (sDebugSwitch) {
            Slog.i(TAG, "mColorZoomWindowRUSConfig: " + this.mColorZoomWindowRUSConfig.toString());
        }
    }

    /* access modifiers changed from: private */
    public static final class OnConfigChangeListeners extends Handler {
        private static final int MSG_CONFIG_SWITCH_CHANGED = 2;
        private static final int MSG_CONFIG_TYPE_CHANGED = 1;
        private final RemoteCallbackList<IColorZoomWindowConfigChangedListener> mConfigListeners = new RemoteCallbackList<>();

        public OnConfigChangeListeners(Looper looper) {
            super(looper);
        }

        /* JADX INFO: Multiple debug info for r0v1 int: [D('type' int), D('enabled' boolean)] */
        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean enabled = true;
            if (i == 1) {
                handleOnConfigTypeChanged(msg.arg1);
            } else if (i == 2) {
                if (msg.arg1 == 0) {
                    enabled = false;
                }
                handleOnConfigSwitchChanged(enabled);
            }
        }

        public void addListenerLocked(IColorZoomWindowConfigChangedListener listener) {
            this.mConfigListeners.register(listener);
        }

        public void removeListenerLocked(IColorZoomWindowConfigChangedListener listener) {
            this.mConfigListeners.unregister(listener);
        }

        public void onConfigTypeChanged(int type) {
            if (ColorZoomWindowConfig.sDebugSwitch) {
                Slog.d(ColorZoomWindowConfig.TAG, "onConfigTypeChanged type = " + type + " listenerCount = " + this.mConfigListeners.getRegisteredCallbackCount());
            }
            if (this.mConfigListeners.getRegisteredCallbackCount() > 0) {
                obtainMessage(1, type, 0).sendToTarget();
            }
        }

        public void onConfigSwitchChanged(boolean enable) {
            if (ColorZoomWindowConfig.sDebugSwitch) {
                Slog.d(ColorZoomWindowConfig.TAG, "onConfigTypeChanged enable = " + enable + " listenerCount = " + this.mConfigListeners.getRegisteredCallbackCount());
            }
            if (this.mConfigListeners.getRegisteredCallbackCount() > 0) {
                obtainMessage(2, enable ? 1 : 0, 0).sendToTarget();
            }
        }

        private void handleOnConfigTypeChanged(int type) {
            int count = this.mConfigListeners.beginBroadcast();
            if (ColorZoomWindowConfig.sDebugSwitch) {
                Slog.d(ColorZoomWindowConfig.TAG, "handleOnConfigTypeChanged:");
                Slog.d(ColorZoomWindowConfig.TAG, "mConfigListeners.beginBroadcast() count = " + count);
            }
            for (int i = 0; i < count; i++) {
                try {
                    try {
                        this.mConfigListeners.getBroadcastItem(i).onConfigTypeChanged(type);
                    } catch (RemoteException e) {
                        Slog.e(ColorZoomWindowConfig.TAG, "Permission listener is dead", e);
                    }
                } catch (Throwable th) {
                    this.mConfigListeners.finishBroadcast();
                    throw th;
                }
            }
            this.mConfigListeners.finishBroadcast();
        }

        private void handleOnConfigSwitchChanged(boolean enable) {
            int count = this.mConfigListeners.beginBroadcast();
            if (ColorZoomWindowConfig.sDebugSwitch) {
                Slog.d(ColorZoomWindowConfig.TAG, "handleOnConfigSwitchChanged:");
                Slog.d(ColorZoomWindowConfig.TAG, "mConfigListeners.beginBroadcast() count = " + count);
            }
            for (int i = 0; i < count; i++) {
                try {
                    try {
                        this.mConfigListeners.getBroadcastItem(i).onConfigSwitchChanged(enable);
                    } catch (RemoteException e) {
                        Slog.e(ColorZoomWindowConfig.TAG, "Zoom Window config listener is dead", e);
                    }
                } catch (Throwable th) {
                    this.mConfigListeners.finishBroadcast();
                    throw th;
                }
            }
            this.mConfigListeners.finishBroadcast();
        }
    }
}

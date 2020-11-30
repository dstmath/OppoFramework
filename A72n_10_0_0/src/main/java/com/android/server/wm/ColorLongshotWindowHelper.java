package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.view.IColorLongshotWindowManager;
import android.view.InputEvent;
import com.android.server.LocalServices;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ColorLongshotWindowHelper implements IColorLongshotWindowManager {
    private static final long LONGSHOT_DUMP_TIMEOUT = 1000;
    private static final String TAG = "LongshotDump";
    private final ColorLongshotWindowDump mDump;
    private final H mHandler = new H();
    private final WindowManagerService mService;

    public ColorLongshotWindowHelper(Context context, WindowManagerService service) {
        this.mService = service;
        this.mDump = new ColorLongshotWindowDump(context, service);
    }

    public void getFocusedWindowFrame(Rect frame) {
        WindowManagerInternal windowManagerInternal;
        IBinder focusedWindow;
        if (frame != null && (focusedWindow = (windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).getFocusedWindowToken()) != null) {
            windowManagerInternal.getWindowFrame(focusedWindow, frame);
        }
    }

    public int getLongshotSurfaceLayerByType(int type) {
        int surfaceLayer;
        synchronized (this.mService.mWindowMap) {
            surfaceLayer = this.mDump.getSurfaceLayerLocked(this.mService.getDefaultDisplayContentLocked(), type);
        }
        return surfaceLayer;
    }

    public int getLongshotSurfaceLayer() {
        return getLongshotSurfaceLayerByType(2303);
    }

    public void longshotNotifyConnected(boolean isConnected) {
        Message msg = this.mHandler.obtainMessage(1);
        msg.arg1 = isConnected ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    public boolean isNavigationBarVisible() {
        DisplayPolicy policy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (policy == null || !(policy instanceof OppoDisplayPolicy)) {
            return false;
        }
        return ((OppoDisplayPolicy) policy).isNavigationBarVisible();
    }

    public boolean isShortcutsPanelShow() {
        DisplayPolicy policy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (policy == null || !(policy instanceof OppoDisplayPolicy)) {
            return false;
        }
        return ((OppoDisplayPolicy) policy).isShortcutsPanelShow();
    }

    public boolean isVolumeShow() {
        DisplayPolicy policy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (policy == null || !(policy instanceof OppoDisplayPolicy)) {
            return false;
        }
        return ((OppoDisplayPolicy) policy).isVolumeShow();
    }

    public boolean isFloatAssistExpand() {
        DisplayPolicy policy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (policy == null || !(policy instanceof OppoDisplayPolicy)) {
            return false;
        }
        return ((OppoDisplayPolicy) policy).isFloatAssistExpand();
    }

    public boolean isEdgePanelExpand() {
        DisplayPolicy policy = this.mService.getDefaultDisplayContentLocked().getDisplayPolicy();
        if (policy == null || !(policy instanceof OppoDisplayPolicy)) {
            return false;
        }
        return ((OppoDisplayPolicy) policy).isEdgePanelExpand();
    }

    public void longshotInjectInput(InputEvent event, int mode) {
        synchronized (this.mService.mWindowMap) {
            this.mDump.injectInputLocked(this.mService.getDefaultDisplayContentLocked(), event, mode);
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        return this.mService.mPolicy.isKeyguardShowingAndNotOccluded();
    }

    public void longshotInjectInputBegin() {
        synchronized (this.mService.mWindowMap) {
            this.mDump.injectInputBeginLocked(this.mService.getDefaultDisplayContentLocked());
        }
    }

    public void longshotInjectInputEnd() {
        synchronized (this.mService.mWindowMap) {
            this.mDump.injectInputEndLocked(this.mService.getDefaultDisplayContentLocked());
        }
    }

    public IBinder getLongshotWindowByType(int type) {
        IBinder longshotWindowByTypeLocked;
        synchronized (this.mService.mWindowMap) {
            longshotWindowByTypeLocked = this.mDump.getLongshotWindowByTypeLocked(this.mService.getDefaultDisplayContentLocked(), type);
        }
        return longshotWindowByTypeLocked;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        Rect frame;
        InputEvent event;
        boolean isConnected = false;
        switch (code) {
            case 10202:
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    frame = (Rect) Rect.CREATOR.createFromParcel(data);
                } else {
                    frame = null;
                }
                getFocusedWindowFrame(frame);
                reply.writeNoException();
                if (frame != null) {
                    reply.writeInt(1);
                    frame.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 10203:
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    event = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                } else {
                    event = null;
                }
                longshotInjectInput(event, data.readInt());
                return true;
            case 10204:
                data.enforceInterface("android.view.IWindowManager");
                int layer = getLongshotSurfaceLayer();
                reply.writeNoException();
                reply.writeInt(layer);
                return true;
            case 10205:
                data.enforceInterface("android.view.IWindowManager");
                int layer2 = getLongshotSurfaceLayerByType(data.readInt());
                reply.writeNoException();
                reply.writeInt(layer2);
                return true;
            case 10206:
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() == 1) {
                    isConnected = true;
                }
                longshotNotifyConnected(isConnected);
                return true;
            case 10207:
                data.enforceInterface("android.view.IWindowManager");
                boolean isNavigationBarVisible = isNavigationBarVisible();
                reply.writeNoException();
                reply.writeInt(isNavigationBarVisible ? 1 : 0);
                return true;
            case 10208:
                data.enforceInterface("android.view.IWindowManager");
                boolean isKeyguardShowingAndNotOccluded = isKeyguardShowingAndNotOccluded();
                reply.writeNoException();
                reply.writeInt(isKeyguardShowingAndNotOccluded ? 1 : 0);
                return true;
            case 10209:
                data.enforceInterface("android.view.IWindowManager");
                boolean isShortcutsPanelShow = isShortcutsPanelShow();
                reply.writeNoException();
                reply.writeInt(isShortcutsPanelShow ? 1 : 0);
                return true;
            case 10210:
                data.enforceInterface("android.view.IWindowManager");
                longshotInjectInputBegin();
                return true;
            case 10211:
                data.enforceInterface("android.view.IWindowManager");
                longshotInjectInputEnd();
                return true;
            case 10212:
                data.enforceInterface("android.view.IWindowManager");
                IBinder token = getLongshotWindowByType(data.readInt());
                reply.writeNoException();
                reply.writeStrongBinder(token);
                return true;
            case 10213:
                data.enforceInterface("android.view.IWindowManager");
                boolean isVolumeShow = isVolumeShow();
                reply.writeNoException();
                reply.writeInt(isVolumeShow ? 1 : 0);
                return true;
            case 10214:
                data.enforceInterface("android.view.IWindowManager");
                boolean isFloatAssistExpand = isFloatAssistExpand();
                reply.writeNoException();
                reply.writeInt(isFloatAssistExpand ? 1 : 0);
                return true;
            case 10215:
                data.enforceInterface("android.view.IWindowManager");
                boolean isEdgePanelExpand = isEdgePanelExpand();
                reply.writeNoException();
                reply.writeInt(isEdgePanelExpand ? 1 : 0);
                return true;
            default:
                return false;
        }
    }

    public void setFileDescriptor(FileDescriptor fd) {
        this.mDump.setFileDescriptor(fd);
    }

    public boolean dumpWindows(PrintWriter pw, String name, String[] args) {
        return this.mDump.dumpWindows(pw, name, args);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = this.mHandler.obtainMessage(what);
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        public static final int MSG_LONGSHOT_NOTIFY_CONNECTED = 1;

        private H() {
        }

        public void handleMessage(Message msg) {
            boolean isConnected = true;
            if (msg.what == 1) {
                synchronized (ColorLongshotWindowHelper.this.mService.mWindowMap) {
                    if (1 != msg.arg1) {
                        isConnected = false;
                    }
                    ColorLongshotWindowHelper.this.mDump.notifyConnectedLocked(ColorLongshotWindowHelper.this.mService.getDefaultDisplayContentLocked(), isConnected);
                }
            }
        }
    }
}

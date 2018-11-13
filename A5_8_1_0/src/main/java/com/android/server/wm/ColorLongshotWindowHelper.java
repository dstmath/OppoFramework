package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.view.IColorLongshotWindowManager;
import android.view.InputEvent;
import android.view.WindowManagerInternal;
import com.android.server.LocalServices;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ColorLongshotWindowHelper implements IColorLongshotWindowManager {
    private static final long LONGSHOT_DUMP_TIMEOUT = 1000;
    private static final String TAG = "LongshotDump";
    private final ColorLongshotWindowDump mDump;
    private final H mHandler = new H(this, null);
    private final WindowManagerService mService;

    private class H extends Handler {
        public static final int MSG_LONGSHOT_NOTIFY_CONNECTED = 1;

        /* synthetic */ H(ColorLongshotWindowHelper this$0, H -this1) {
            this();
        }

        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (ColorLongshotWindowHelper.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ColorLongshotWindowHelper.this.mDump.notifyConnectedLocked(ColorLongshotWindowHelper.this.mService.getDefaultDisplayContentLocked(), 1 == msg.arg1);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public ColorLongshotWindowHelper(Context context, WindowManagerService service) {
        this.mService = service;
        this.mDump = new ColorLongshotWindowDump(context, service);
    }

    public void getFocusedWindowFrame(Rect frame) {
        if (frame != null) {
            WindowManagerInternal windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            IBinder focusedWindow = windowManagerInternal.getFocusedWindowToken();
            if (focusedWindow != null) {
                windowManagerInternal.getWindowFrame(focusedWindow, frame);
            }
        }
    }

    public int getLongshotSurfaceLayerByType(int type) {
        int surfaceLayer = 0;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                surfaceLayer = this.mDump.getSurfaceLayerLocked(this.mService.getDefaultDisplayContentLocked(), type);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return surfaceLayer;
    }

    public int getLongshotSurfaceLayer() {
        return getLongshotSurfaceLayerByType(2303);
    }

    public void longshotNotifyConnected(boolean isConnected) {
        int i = 1;
        Message msg = this.mHandler.obtainMessage(1);
        if (!isConnected) {
            i = 0;
        }
        msg.arg1 = i;
        this.mHandler.sendMessage(msg);
    }

    public boolean isNavigationBarVisible() {
        return this.mService.mPolicy.isNavigationBarVisible();
    }

    public boolean isShortcutsPanelShow() {
        return this.mService.mPolicy.isShortcutsPanelShow();
    }

    public void longshotInjectInput(InputEvent event, int mode) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mDump.injectInputLocked(this.mService.getDefaultDisplayContentLocked(), event, mode);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        return this.mService.mPolicy.isKeyguardShowingAndNotOccludedComp();
    }

    public void longshotInjectInputBegin() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mDump.injectInputBeginLocked(this.mService.getDefaultDisplayContentLocked());
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void longshotInjectInputEnd() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mDump.injectInputEndLocked(this.mService.getDefaultDisplayContentLocked());
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        int i = 0;
        int layer;
        boolean result;
        switch (code) {
            case 10016:
                Rect frame;
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
            case 10021:
                InputEvent event;
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    event = (InputEvent) InputEvent.CREATOR.createFromParcel(data);
                } else {
                    event = null;
                }
                longshotInjectInput(event, data.readInt());
                return true;
            case 10023:
                data.enforceInterface("android.view.IWindowManager");
                layer = getLongshotSurfaceLayer();
                reply.writeNoException();
                reply.writeInt(layer);
                return true;
            case 10024:
                data.enforceInterface("android.view.IWindowManager");
                layer = getLongshotSurfaceLayerByType(data.readInt());
                reply.writeNoException();
                reply.writeInt(layer);
                return true;
            case 10027:
                data.enforceInterface("android.view.IWindowManager");
                longshotNotifyConnected(data.readInt() == 1);
                return true;
            case 10028:
                data.enforceInterface("android.view.IWindowManager");
                result = isNavigationBarVisible();
                reply.writeNoException();
                if (result) {
                    i = 1;
                }
                reply.writeInt(i);
                return true;
            case 10029:
                data.enforceInterface("android.view.IWindowManager");
                result = isKeyguardShowingAndNotOccluded();
                reply.writeNoException();
                if (result) {
                    i = 1;
                }
                reply.writeInt(i);
                return true;
            case 10031:
                data.enforceInterface("android.view.IWindowManager");
                result = isShortcutsPanelShow();
                reply.writeNoException();
                if (result) {
                    i = 1;
                }
                reply.writeInt(i);
                return true;
            case 10032:
                data.enforceInterface("android.view.IWindowManager");
                longshotInjectInputBegin();
                return true;
            case 10033:
                data.enforceInterface("android.view.IWindowManager");
                longshotInjectInputEnd();
                return true;
            default:
                return false;
        }
    }

    public void setFileDescriptor(FileDescriptor fd) {
        this.mDump.setFileDescriptor(fd);
    }

    public boolean dumpWindows(PrintWriter pw, String name) {
        return this.mDump.dumpWindows(pw, name);
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
}

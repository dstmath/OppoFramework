package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStatusBar extends IInterface {

    public static abstract class Stub extends Binder implements IStatusBar {
        private static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBar";
        static final int TRANSACTION_addQsTile = 31;
        static final int TRANSACTION_animateCollapsePanels = 6;
        static final int TRANSACTION_animateExpandNotificationsPanel = 4;
        static final int TRANSACTION_animateExpandSettingsPanel = 5;
        static final int TRANSACTION_appTransitionCancelled = 22;
        static final int TRANSACTION_appTransitionFinished = 24;
        static final int TRANSACTION_appTransitionPending = 21;
        static final int TRANSACTION_appTransitionStarting = 23;
        static final int TRANSACTION_cancelPreloadRecentApps = 17;
        static final int TRANSACTION_clickQsTile = 33;
        static final int TRANSACTION_disable = 3;
        static final int TRANSACTION_dismissKeyboardShortcutsMenu = 19;
        static final int TRANSACTION_exitSplitScreen = 40;
        static final int TRANSACTION_handleOpeningSpecialApp = 38;
        static final int TRANSACTION_handleSystemKey = 34;
        static final int TRANSACTION_hideRecentApps = 13;
        static final int TRANSACTION_notifyMultiWindowFocusChanged = 41;
        static final int TRANSACTION_onCameraLaunchGestureDetected = 27;
        static final int TRANSACTION_preloadRecentApps = 16;
        static final int TRANSACTION_remQsTile = 32;
        static final int TRANSACTION_removeIcon = 2;
        static final int TRANSACTION_setIcon = 1;
        static final int TRANSACTION_setImeWindowStatus = 10;
        static final int TRANSACTION_setNavigationBarColor = 42;
        static final int TRANSACTION_setShortcutsPanelState = 37;
        static final int TRANSACTION_setStatusBarFunction = 43;
        static final int TRANSACTION_setSystemUiVisibility = 8;
        static final int TRANSACTION_setTopAppHidesStatusBar = 30;
        static final int TRANSACTION_setWindowState = 11;
        static final int TRANSACTION_showAssistDisclosure = 25;
        static final int TRANSACTION_showGlobalActionsMenu = 29;
        static final int TRANSACTION_showPictureInPictureMenu = 28;
        static final int TRANSACTION_showRecentApps = 12;
        static final int TRANSACTION_showScreenPinningRequest = 18;
        static final int TRANSACTION_showShutdownUi = 35;
        static final int TRANSACTION_startAssist = 26;
        static final int TRANSACTION_toggleKeyboardShortcutsMenu = 20;
        static final int TRANSACTION_togglePanel = 7;
        static final int TRANSACTION_toggleRecentApps = 14;
        static final int TRANSACTION_toggleSplitScreen = 15;
        static final int TRANSACTION_toggleSplitScreenMode = 39;
        static final int TRANSACTION_topAppWindowChanged = 9;
        static final int TRANSACTION_topIsFullscreen = 36;

        private static class Proxy implements IStatusBar {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setIcon(String slot, StatusBarIcon icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    if (icon != null) {
                        _data.writeInt(1);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void removeIcon(String slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void disable(int state1, int state2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state1);
                    _data.writeInt(state2);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void animateExpandNotificationsPanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void animateExpandSettingsPanel(String subPanel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(subPanel);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void animateCollapsePanels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void togglePanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vis);
                    _data.writeInt(fullscreenStackVis);
                    _data.writeInt(dockedStackVis);
                    _data.writeInt(mask);
                    if (fullscreenBounds != null) {
                        _data.writeInt(1);
                        fullscreenBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (dockedBounds != null) {
                        _data.writeInt(1);
                        dockedBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void topAppWindowChanged(boolean menuVisible) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!menuVisible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(vis);
                    _data.writeInt(backDisposition);
                    if (!showImeSwitcher) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setWindowState(int window, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(window);
                    _data.writeInt(state);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(triggeredFromAltTab ? 1 : 0);
                    if (!fromHome) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(triggeredFromAltTab ? 1 : 0);
                    if (!triggeredFromHomeKey) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void toggleRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void toggleSplitScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void preloadRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void cancelPreloadRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void showScreenPinningRequest(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void dismissKeyboardShortcutsMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void toggleKeyboardShortcutsMenu(int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void appTransitionPending() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void appTransitionCancelled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void appTransitionStarting(long statusBarAnimationsStartTime, long statusBarAnimationsDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(statusBarAnimationsStartTime);
                    _data.writeLong(statusBarAnimationsDuration);
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void appTransitionFinished() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void showAssistDisclosure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startAssist(Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCameraLaunchGestureDetected(int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(source);
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void showPictureInPictureMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void showGlobalActionsMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setTopAppHidesStatusBar(boolean hidesStatusBar) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!hidesStatusBar) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void addQsTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void remQsTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void clickQsTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handleSystemKey(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void showShutdownUi(boolean isReboot, String reason) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isReboot) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(reason);
                    this.mRemote.transact(35, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void topIsFullscreen(boolean fullscreen) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!fullscreen) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(36, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setShortcutsPanelState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(37, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handleOpeningSpecialApp(int state, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeString(pkg);
                    this.mRemote.transact(38, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void toggleSplitScreenMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(39, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void exitSplitScreen(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(40, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyMultiWindowFocusChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(41, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setNavigationBarColor(int vis, int color, boolean expandNavBar) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vis);
                    _data.writeInt(color);
                    if (!expandNavBar) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(42, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setStatusBarFunction(int functionCode, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(functionCode);
                    _data.writeString(pkgName);
                    this.mRemote.transact(43, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStatusBar asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStatusBar)) {
                return new Proxy(obj);
            }
            return (IStatusBar) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            switch (code) {
                case 1:
                    StatusBarIcon _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (StatusBarIcon) StatusBarIcon.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setIcon(_arg02, _arg1);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    removeIcon(data.readString());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    disable(data.readInt(), data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    animateExpandNotificationsPanel();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    animateExpandSettingsPanel(data.readString());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    animateCollapsePanels();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    togglePanel();
                    return true;
                case 8:
                    Rect _arg4;
                    Rect _arg5;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg03 = data.readInt();
                    int _arg12 = data.readInt();
                    int _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg4 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg4 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg5 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg5 = null;
                    }
                    setSystemUiVisibility(_arg03, _arg12, _arg2, _arg3, _arg4, _arg5);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    topAppWindowChanged(data.readInt() != 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    setImeWindowStatus(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt() != 0);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    setWindowState(data.readInt(), data.readInt());
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    showRecentApps(data.readInt() != 0, data.readInt() != 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    hideRecentApps(data.readInt() != 0, data.readInt() != 0);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    toggleRecentApps();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    toggleSplitScreen();
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    preloadRecentApps();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    cancelPreloadRecentApps();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    showScreenPinningRequest(data.readInt());
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    dismissKeyboardShortcutsMenu();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    toggleKeyboardShortcutsMenu(data.readInt());
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionPending();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionCancelled();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionStarting(data.readLong(), data.readLong());
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionFinished();
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    showAssistDisclosure();
                    return true;
                case 26:
                    Bundle _arg04;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    startAssist(_arg04);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    onCameraLaunchGestureDetected(data.readInt());
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    showPictureInPictureMenu();
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    showGlobalActionsMenu();
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    setTopAppHidesStatusBar(data.readInt() != 0);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    addQsTile(_arg0);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    remQsTile(_arg0);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    clickQsTile(_arg0);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    handleSystemKey(data.readInt());
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    showShutdownUi(data.readInt() != 0, data.readString());
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    topIsFullscreen(data.readInt() != 0);
                    return true;
                case 37:
                    data.enforceInterface(DESCRIPTOR);
                    setShortcutsPanelState(data.readInt());
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    handleOpeningSpecialApp(data.readInt(), data.readString());
                    return true;
                case 39:
                    data.enforceInterface(DESCRIPTOR);
                    toggleSplitScreenMode(data.readInt());
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    exitSplitScreen(data.readInt());
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    notifyMultiWindowFocusChanged(data.readInt());
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    setNavigationBarColor(data.readInt(), data.readInt(), data.readInt() != 0);
                    return true;
                case 43:
                    data.enforceInterface(DESCRIPTOR);
                    setStatusBarFunction(data.readInt(), data.readString());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addQsTile(ComponentName componentName) throws RemoteException;

    void animateCollapsePanels() throws RemoteException;

    void animateExpandNotificationsPanel() throws RemoteException;

    void animateExpandSettingsPanel(String str) throws RemoteException;

    void appTransitionCancelled() throws RemoteException;

    void appTransitionFinished() throws RemoteException;

    void appTransitionPending() throws RemoteException;

    void appTransitionStarting(long j, long j2) throws RemoteException;

    void cancelPreloadRecentApps() throws RemoteException;

    void clickQsTile(ComponentName componentName) throws RemoteException;

    void disable(int i, int i2) throws RemoteException;

    void dismissKeyboardShortcutsMenu() throws RemoteException;

    void exitSplitScreen(int i) throws RemoteException;

    void handleOpeningSpecialApp(int i, String str) throws RemoteException;

    void handleSystemKey(int i) throws RemoteException;

    void hideRecentApps(boolean z, boolean z2) throws RemoteException;

    void notifyMultiWindowFocusChanged(int i) throws RemoteException;

    void onCameraLaunchGestureDetected(int i) throws RemoteException;

    void preloadRecentApps() throws RemoteException;

    void remQsTile(ComponentName componentName) throws RemoteException;

    void removeIcon(String str) throws RemoteException;

    void setIcon(String str, StatusBarIcon statusBarIcon) throws RemoteException;

    void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) throws RemoteException;

    void setNavigationBarColor(int i, int i2, boolean z) throws RemoteException;

    void setShortcutsPanelState(int i) throws RemoteException;

    void setStatusBarFunction(int i, String str) throws RemoteException;

    void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) throws RemoteException;

    void setTopAppHidesStatusBar(boolean z) throws RemoteException;

    void setWindowState(int i, int i2) throws RemoteException;

    void showAssistDisclosure() throws RemoteException;

    void showGlobalActionsMenu() throws RemoteException;

    void showPictureInPictureMenu() throws RemoteException;

    void showRecentApps(boolean z, boolean z2) throws RemoteException;

    void showScreenPinningRequest(int i) throws RemoteException;

    void showShutdownUi(boolean z, String str) throws RemoteException;

    void startAssist(Bundle bundle) throws RemoteException;

    void toggleKeyboardShortcutsMenu(int i) throws RemoteException;

    void togglePanel() throws RemoteException;

    void toggleRecentApps() throws RemoteException;

    void toggleSplitScreen() throws RemoteException;

    void toggleSplitScreenMode(int i) throws RemoteException;

    void topAppWindowChanged(boolean z) throws RemoteException;

    void topIsFullscreen(boolean z) throws RemoteException;
}

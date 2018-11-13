package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IStatusBarService extends IInterface {

    public static abstract class Stub extends Binder implements IStatusBarService {
        private static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBarService";
        static final int TRANSACTION_addTile = 24;
        static final int TRANSACTION_clearNotificationEffects = 15;
        static final int TRANSACTION_clickTile = 26;
        static final int TRANSACTION_collapsePanels = 2;
        static final int TRANSACTION_disable = 3;
        static final int TRANSACTION_disable2 = 5;
        static final int TRANSACTION_disable2ForUser = 6;
        static final int TRANSACTION_disableForUser = 4;
        static final int TRANSACTION_exitSplitScreen = 31;
        static final int TRANSACTION_expandNotificationsPanel = 1;
        static final int TRANSACTION_expandSettingsPanel = 11;
        static final int TRANSACTION_getTopIsFullscreen = 30;
        static final int TRANSACTION_handleSystemNavigationKey = 27;
        static final int TRANSACTION_notifyMultiWindowFocusChanged = 32;
        static final int TRANSACTION_notifyMultiWindowModeChanged = 33;
        static final int TRANSACTION_onClearAllNotifications = 19;
        static final int TRANSACTION_onNotificationActionClick = 17;
        static final int TRANSACTION_onNotificationClear = 20;
        static final int TRANSACTION_onNotificationClick = 16;
        static final int TRANSACTION_onNotificationError = 18;
        static final int TRANSACTION_onNotificationExpansionChanged = 22;
        static final int TRANSACTION_onNotificationVisibilityChanged = 21;
        static final int TRANSACTION_onPanelHidden = 14;
        static final int TRANSACTION_onPanelRevealed = 13;
        static final int TRANSACTION_registerStatusBar = 12;
        static final int TRANSACTION_remTile = 25;
        static final int TRANSACTION_removeIcon = 9;
        static final int TRANSACTION_setIcon = 7;
        static final int TRANSACTION_setIconVisibility = 8;
        static final int TRANSACTION_setImeWindowStatus = 10;
        static final int TRANSACTION_setNavigationBarColor = 34;
        static final int TRANSACTION_setShortcutsPanelState = 29;
        static final int TRANSACTION_setSystemUiVisibility = 23;
        static final int TRANSACTION_toggleSplitScreen = 35;
        static final int TRANSACTION_topIsFullscreen = 28;

        private static class Proxy implements IStatusBarService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "com.android.internal.statusbar.IStatusBarService";
            }

            public void expandNotificationsPanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void collapsePanels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable(int what, IBinder token, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableForUser(int what, IBinder token, String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable2(int what, IBinder token, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable2ForUser(int what, IBinder token, String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(slot);
                    _data.writeString(iconPackage);
                    _data.writeInt(iconId);
                    _data.writeInt(iconLevel);
                    _data.writeString(contentDescription);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIconVisibility(String slot, boolean visible) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(slot);
                    if (visible) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeIcon(String slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(slot);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeStrongBinder(token);
                    _data.writeInt(vis);
                    _data.writeInt(backDisposition);
                    if (showImeSwitcher) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void expandSettingsPanel(String subPanel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(subPanel);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerStatusBar(IStatusBar callbacks, List<String> iconSlots, List<StatusBarIcon> iconList, int[] switches, List<IBinder> binders, Rect fullscreenStackBounds, Rect dockedStackBounds) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (switches == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(switches.length);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringList(iconSlots);
                    _reply.readTypedList(iconList, StatusBarIcon.CREATOR);
                    _reply.readIntArray(switches);
                    _reply.readBinderList(binders);
                    if (_reply.readInt() != 0) {
                        fullscreenStackBounds.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        dockedStackBounds.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPanelRevealed(boolean clearNotificationEffects, int numItems) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (clearNotificationEffects) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(numItems);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPanelHidden() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearNotificationEffects() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationClick(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(key);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationActionClick(String key, int actionIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(key);
                    _data.writeInt(actionIndex);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationError(String pkg, String tag, int id, int uid, int initialPid, String message, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    _data.writeInt(uid);
                    _data.writeInt(initialPid);
                    _data.writeString(message);
                    _data.writeInt(userId);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onClearAllNotifications(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(userId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationClear(String pkg, String tag, int id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    _data.writeInt(userId);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeTypedArray(newlyVisibleKeys, 0);
                    _data.writeTypedArray(noLongerVisibleKeys, 0);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeString(key);
                    if (userAction) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!expanded) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemUiVisibility(int vis, int mask, String cause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(vis);
                    _data.writeInt(mask);
                    _data.writeString(cause);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clickTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void handleSystemNavigationKey(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(key);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void topIsFullscreen(boolean fullscreen) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (fullscreen) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setShortcutsPanelState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(state);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getTopIsFullscreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void exitSplitScreen(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(state);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyMultiWindowFocusChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(state);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyMultiWindowModeChanged(boolean isInMultiWindow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    if (isInMultiWindow) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNavigationBarColor(int rotation, int color, boolean expandNavBar) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(rotation);
                    _data.writeInt(color);
                    if (expandNavBar) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleSplitScreen(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    _data.writeInt(mode);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.android.internal.statusbar.IStatusBarService");
        }

        public static IStatusBarService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.android.internal.statusbar.IStatusBarService");
            if (iin == null || !(iin instanceof IStatusBarService)) {
                return new Proxy(obj);
            }
            return (IStatusBarService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    expandNotificationsPanel();
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    collapsePanels();
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    disable(data.readInt(), data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    disableForUser(data.readInt(), data.readStrongBinder(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    disable2(data.readInt(), data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    disable2ForUser(data.readInt(), data.readStrongBinder(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    setIcon(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    setIconVisibility(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    removeIcon(data.readString());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    setImeWindowStatus(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    expandSettingsPanel(data.readString());
                    reply.writeNoException();
                    return true;
                case 12:
                    int[] _arg3;
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    IStatusBar _arg02 = com.android.internal.statusbar.IStatusBar.Stub.asInterface(data.readStrongBinder());
                    List<String> _arg1 = new ArrayList();
                    List<StatusBarIcon> _arg2 = new ArrayList();
                    int _arg3_length = data.readInt();
                    if (_arg3_length < 0) {
                        _arg3 = null;
                    } else {
                        _arg3 = new int[_arg3_length];
                    }
                    List<IBinder> _arg4 = new ArrayList();
                    Rect _arg5 = new Rect();
                    Rect _arg6 = new Rect();
                    registerStatusBar(_arg02, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
                    reply.writeNoException();
                    reply.writeStringList(_arg1);
                    reply.writeTypedList(_arg2);
                    reply.writeIntArray(_arg3);
                    reply.writeBinderList(_arg4);
                    if (_arg5 != null) {
                        reply.writeInt(1);
                        _arg5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg6 != null) {
                        reply.writeInt(1);
                        _arg6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 13:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onPanelRevealed(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onPanelHidden();
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    clearNotificationEffects();
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onNotificationClick(data.readString());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onNotificationActionClick(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onNotificationError(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onClearAllNotifications(data.readInt());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onNotificationClear(data.readString(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onNotificationVisibilityChanged((NotificationVisibility[]) data.createTypedArray(NotificationVisibility.CREATOR), (NotificationVisibility[]) data.createTypedArray(NotificationVisibility.CREATOR));
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    onNotificationExpansionChanged(data.readString(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    setSystemUiVisibility(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    addTile(_arg0);
                    reply.writeNoException();
                    return true;
                case 25:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    remTile(_arg0);
                    reply.writeNoException();
                    return true;
                case 26:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    clickTile(_arg0);
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    handleSystemNavigationKey(data.readInt());
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    topIsFullscreen(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 29:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    setShortcutsPanelState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 30:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    boolean _result = getTopIsFullscreen();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 31:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    exitSplitScreen(data.readInt());
                    reply.writeNoException();
                    return true;
                case 32:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    notifyMultiWindowFocusChanged(data.readInt());
                    reply.writeNoException();
                    return true;
                case 33:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    notifyMultiWindowModeChanged(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 34:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    setNavigationBarColor(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 35:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    toggleSplitScreen(data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.android.internal.statusbar.IStatusBarService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addTile(ComponentName componentName) throws RemoteException;

    void clearNotificationEffects() throws RemoteException;

    void clickTile(ComponentName componentName) throws RemoteException;

    void collapsePanels() throws RemoteException;

    void disable(int i, IBinder iBinder, String str) throws RemoteException;

    void disable2(int i, IBinder iBinder, String str) throws RemoteException;

    void disable2ForUser(int i, IBinder iBinder, String str, int i2) throws RemoteException;

    void disableForUser(int i, IBinder iBinder, String str, int i2) throws RemoteException;

    void exitSplitScreen(int i) throws RemoteException;

    void expandNotificationsPanel() throws RemoteException;

    void expandSettingsPanel(String str) throws RemoteException;

    boolean getTopIsFullscreen() throws RemoteException;

    void handleSystemNavigationKey(int i) throws RemoteException;

    void notifyMultiWindowFocusChanged(int i) throws RemoteException;

    void notifyMultiWindowModeChanged(boolean z) throws RemoteException;

    void onClearAllNotifications(int i) throws RemoteException;

    void onNotificationActionClick(String str, int i) throws RemoteException;

    void onNotificationClear(String str, String str2, int i, int i2) throws RemoteException;

    void onNotificationClick(String str) throws RemoteException;

    void onNotificationError(String str, String str2, int i, int i2, int i3, String str3, int i4) throws RemoteException;

    void onNotificationExpansionChanged(String str, boolean z, boolean z2) throws RemoteException;

    void onNotificationVisibilityChanged(NotificationVisibility[] notificationVisibilityArr, NotificationVisibility[] notificationVisibilityArr2) throws RemoteException;

    void onPanelHidden() throws RemoteException;

    void onPanelRevealed(boolean z, int i) throws RemoteException;

    void registerStatusBar(IStatusBar iStatusBar, List<String> list, List<StatusBarIcon> list2, int[] iArr, List<IBinder> list3, Rect rect, Rect rect2) throws RemoteException;

    void remTile(ComponentName componentName) throws RemoteException;

    void removeIcon(String str) throws RemoteException;

    void setIcon(String str, String str2, int i, int i2, String str3) throws RemoteException;

    void setIconVisibility(String str, boolean z) throws RemoteException;

    void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) throws RemoteException;

    void setNavigationBarColor(int i, int i2, boolean z) throws RemoteException;

    void setShortcutsPanelState(int i) throws RemoteException;

    void setSystemUiVisibility(int i, int i2, String str) throws RemoteException;

    void toggleSplitScreen(int i) throws RemoteException;

    void topIsFullscreen(boolean z) throws RemoteException;
}

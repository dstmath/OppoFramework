package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStatusBar extends IInterface {
    void addQsTile(ComponentName componentName) throws RemoteException;

    void animateCollapsePanels() throws RemoteException;

    void animateExpandNotificationsPanel() throws RemoteException;

    void animateExpandSettingsPanel(String str) throws RemoteException;

    void appTransitionCancelled(int i) throws RemoteException;

    void appTransitionFinished(int i) throws RemoteException;

    void appTransitionPending(int i) throws RemoteException;

    void appTransitionStarting(int i, long j, long j2) throws RemoteException;

    void cancelPreloadRecentApps() throws RemoteException;

    void clickQsTile(ComponentName componentName) throws RemoteException;

    void disable(int i, int i2, int i3) throws RemoteException;

    void dismissKeyboardShortcutsMenu() throws RemoteException;

    void handleSystemKey(int i) throws RemoteException;

    void hideBiometricDialog() throws RemoteException;

    void hideRecentApps(boolean z, boolean z2) throws RemoteException;

    void onBiometricAuthenticated(boolean z, String str) throws RemoteException;

    void onBiometricError(String str) throws RemoteException;

    void onBiometricHelp(String str) throws RemoteException;

    void onCameraLaunchGestureDetected(int i) throws RemoteException;

    void onDisplayReady(int i) throws RemoteException;

    void onProposedRotationChanged(int i, boolean z) throws RemoteException;

    void onRecentsAnimationStateChanged(boolean z) throws RemoteException;

    void preloadRecentApps() throws RemoteException;

    void remQsTile(ComponentName componentName) throws RemoteException;

    void removeIcon(String str) throws RemoteException;

    void setIcon(String str, StatusBarIcon statusBarIcon) throws RemoteException;

    void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) throws RemoteException;

    void setSystemUiVisibility(int i, int i2, int i3, int i4, int i5, Rect rect, Rect rect2, boolean z) throws RemoteException;

    void setTopAppHidesStatusBar(boolean z) throws RemoteException;

    void setWindowState(int i, int i2, int i3) throws RemoteException;

    void showAssistDisclosure() throws RemoteException;

    void showBiometricDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2) throws RemoteException;

    void showGlobalActionsMenu() throws RemoteException;

    void showPictureInPictureMenu() throws RemoteException;

    void showPinningEnterExitToast(boolean z) throws RemoteException;

    void showPinningEscapeToast() throws RemoteException;

    void showRecentApps(boolean z) throws RemoteException;

    void showScreenPinningRequest(int i) throws RemoteException;

    void showShutdownUi(boolean z, String str) throws RemoteException;

    void showWirelessChargingAnimation(int i) throws RemoteException;

    void startAssist(Bundle bundle) throws RemoteException;

    void toggleKeyboardShortcutsMenu(int i) throws RemoteException;

    void togglePanel() throws RemoteException;

    void toggleRecentApps() throws RemoteException;

    void toggleSplitScreen() throws RemoteException;

    void topAppWindowChanged(int i, boolean z) throws RemoteException;

    public static class Default implements IStatusBar {
        @Override // com.android.internal.statusbar.IStatusBar
        public void setIcon(String slot, StatusBarIcon icon) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void removeIcon(String slot) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void disable(int displayId, int state1, int state2) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void animateExpandNotificationsPanel() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void animateExpandSettingsPanel(String subPanel) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void animateCollapsePanels() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void togglePanel() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showWirelessChargingAnimation(int batteryLevel) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void setSystemUiVisibility(int displayId, int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, boolean navbarColorManagedByIme) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void topAppWindowChanged(int displayId, boolean menuVisible) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void setImeWindowStatus(int displayId, IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void setWindowState(int display, int window, int state) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showRecentApps(boolean triggeredFromAltTab) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void toggleRecentApps() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void toggleSplitScreen() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void preloadRecentApps() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void cancelPreloadRecentApps() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showScreenPinningRequest(int taskId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void dismissKeyboardShortcutsMenu() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void toggleKeyboardShortcutsMenu(int deviceId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void appTransitionPending(int displayId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void appTransitionCancelled(int displayId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void appTransitionStarting(int displayId, long statusBarAnimationsStartTime, long statusBarAnimationsDuration) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void appTransitionFinished(int displayId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showAssistDisclosure() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void startAssist(Bundle args) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onCameraLaunchGestureDetected(int source) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showPictureInPictureMenu() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showGlobalActionsMenu() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onProposedRotationChanged(int rotation, boolean isValid) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void setTopAppHidesStatusBar(boolean hidesStatusBar) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void addQsTile(ComponentName tile) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void remQsTile(ComponentName tile) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void clickQsTile(ComponentName tile) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void handleSystemKey(int key) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showPinningEnterExitToast(boolean entering) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showPinningEscapeToast() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showShutdownUi(boolean isReboot, String reason) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void showBiometricDialog(Bundle bundle, IBiometricServiceReceiverInternal receiver, int type, boolean requireConfirmation, int userId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onBiometricAuthenticated(boolean authenticated, String failureReason) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onBiometricHelp(String message) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onBiometricError(String error) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void hideBiometricDialog() throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onDisplayReady(int displayId) throws RemoteException {
        }

        @Override // com.android.internal.statusbar.IStatusBar
        public void onRecentsAnimationStateChanged(boolean running) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IStatusBar {
        private static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBar";
        static final int TRANSACTION_addQsTile = 33;
        static final int TRANSACTION_animateCollapsePanels = 6;
        static final int TRANSACTION_animateExpandNotificationsPanel = 4;
        static final int TRANSACTION_animateExpandSettingsPanel = 5;
        static final int TRANSACTION_appTransitionCancelled = 23;
        static final int TRANSACTION_appTransitionFinished = 25;
        static final int TRANSACTION_appTransitionPending = 22;
        static final int TRANSACTION_appTransitionStarting = 24;
        static final int TRANSACTION_cancelPreloadRecentApps = 18;
        static final int TRANSACTION_clickQsTile = 35;
        static final int TRANSACTION_disable = 3;
        static final int TRANSACTION_dismissKeyboardShortcutsMenu = 20;
        static final int TRANSACTION_handleSystemKey = 36;
        static final int TRANSACTION_hideBiometricDialog = 44;
        static final int TRANSACTION_hideRecentApps = 14;
        static final int TRANSACTION_onBiometricAuthenticated = 41;
        static final int TRANSACTION_onBiometricError = 43;
        static final int TRANSACTION_onBiometricHelp = 42;
        static final int TRANSACTION_onCameraLaunchGestureDetected = 28;
        static final int TRANSACTION_onDisplayReady = 45;
        static final int TRANSACTION_onProposedRotationChanged = 31;
        static final int TRANSACTION_onRecentsAnimationStateChanged = 46;
        static final int TRANSACTION_preloadRecentApps = 17;
        static final int TRANSACTION_remQsTile = 34;
        static final int TRANSACTION_removeIcon = 2;
        static final int TRANSACTION_setIcon = 1;
        static final int TRANSACTION_setImeWindowStatus = 11;
        static final int TRANSACTION_setSystemUiVisibility = 9;
        static final int TRANSACTION_setTopAppHidesStatusBar = 32;
        static final int TRANSACTION_setWindowState = 12;
        static final int TRANSACTION_showAssistDisclosure = 26;
        static final int TRANSACTION_showBiometricDialog = 40;
        static final int TRANSACTION_showGlobalActionsMenu = 30;
        static final int TRANSACTION_showPictureInPictureMenu = 29;
        static final int TRANSACTION_showPinningEnterExitToast = 37;
        static final int TRANSACTION_showPinningEscapeToast = 38;
        static final int TRANSACTION_showRecentApps = 13;
        static final int TRANSACTION_showScreenPinningRequest = 19;
        static final int TRANSACTION_showShutdownUi = 39;
        static final int TRANSACTION_showWirelessChargingAnimation = 8;
        static final int TRANSACTION_startAssist = 27;
        static final int TRANSACTION_toggleKeyboardShortcutsMenu = 21;
        static final int TRANSACTION_togglePanel = 7;
        static final int TRANSACTION_toggleRecentApps = 15;
        static final int TRANSACTION_toggleSplitScreen = 16;
        static final int TRANSACTION_topAppWindowChanged = 10;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setIcon";
                case 2:
                    return "removeIcon";
                case 3:
                    return "disable";
                case 4:
                    return "animateExpandNotificationsPanel";
                case 5:
                    return "animateExpandSettingsPanel";
                case 6:
                    return "animateCollapsePanels";
                case 7:
                    return "togglePanel";
                case 8:
                    return "showWirelessChargingAnimation";
                case 9:
                    return "setSystemUiVisibility";
                case 10:
                    return "topAppWindowChanged";
                case 11:
                    return "setImeWindowStatus";
                case 12:
                    return "setWindowState";
                case 13:
                    return "showRecentApps";
                case 14:
                    return "hideRecentApps";
                case 15:
                    return "toggleRecentApps";
                case 16:
                    return "toggleSplitScreen";
                case 17:
                    return "preloadRecentApps";
                case 18:
                    return "cancelPreloadRecentApps";
                case 19:
                    return "showScreenPinningRequest";
                case 20:
                    return "dismissKeyboardShortcutsMenu";
                case 21:
                    return "toggleKeyboardShortcutsMenu";
                case 22:
                    return "appTransitionPending";
                case 23:
                    return "appTransitionCancelled";
                case 24:
                    return "appTransitionStarting";
                case 25:
                    return "appTransitionFinished";
                case 26:
                    return "showAssistDisclosure";
                case 27:
                    return "startAssist";
                case 28:
                    return "onCameraLaunchGestureDetected";
                case 29:
                    return "showPictureInPictureMenu";
                case 30:
                    return "showGlobalActionsMenu";
                case 31:
                    return "onProposedRotationChanged";
                case 32:
                    return "setTopAppHidesStatusBar";
                case 33:
                    return "addQsTile";
                case 34:
                    return "remQsTile";
                case 35:
                    return "clickQsTile";
                case 36:
                    return "handleSystemKey";
                case 37:
                    return "showPinningEnterExitToast";
                case 38:
                    return "showPinningEscapeToast";
                case 39:
                    return "showShutdownUi";
                case 40:
                    return "showBiometricDialog";
                case 41:
                    return "onBiometricAuthenticated";
                case 42:
                    return "onBiometricHelp";
                case 43:
                    return "onBiometricError";
                case 44:
                    return "hideBiometricDialog";
                case 45:
                    return "onDisplayReady";
                case 46:
                    return "onRecentsAnimationStateChanged";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            StatusBarIcon _arg1;
            Rect _arg5;
            Rect _arg6;
            Bundle _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            ComponentName _arg04;
            Bundle _arg05;
            if (code != 1598968902) {
                boolean _arg06 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = StatusBarIcon.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setIcon(_arg07, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removeIcon(data.readString());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        disable(data.readInt(), data.readInt(), data.readInt());
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
                        data.enforceInterface(DESCRIPTOR);
                        showWirelessChargingAnimation(data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        int _arg12 = data.readInt();
                        int _arg2 = data.readInt();
                        int _arg3 = data.readInt();
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg6 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        setSystemUiVisibility(_arg08, _arg12, _arg2, _arg3, _arg4, _arg5, _arg6, data.readInt() != 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        topAppWindowChanged(_arg09, _arg06);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        setImeWindowStatus(data.readInt(), data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt() != 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        setWindowState(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        showRecentApps(_arg06);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg010 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        hideRecentApps(_arg010, _arg06);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        toggleRecentApps();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        toggleSplitScreen();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        preloadRecentApps();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        cancelPreloadRecentApps();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        showScreenPinningRequest(data.readInt());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        dismissKeyboardShortcutsMenu();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        toggleKeyboardShortcutsMenu(data.readInt());
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        appTransitionPending(data.readInt());
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        appTransitionCancelled(data.readInt());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        appTransitionStarting(data.readInt(), data.readLong(), data.readLong());
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        appTransitionFinished(data.readInt());
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        showAssistDisclosure();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        startAssist(_arg0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        onCameraLaunchGestureDetected(data.readInt());
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        showPictureInPictureMenu();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        showGlobalActionsMenu();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        onProposedRotationChanged(_arg011, _arg06);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        setTopAppHidesStatusBar(_arg06);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        addQsTile(_arg02);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        remQsTile(_arg03);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        clickQsTile(_arg04);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        handleSystemKey(data.readInt());
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        showPinningEnterExitToast(_arg06);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        showPinningEscapeToast();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        showShutdownUi(_arg06, data.readString());
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        showBiometricDialog(_arg05, IBiometricServiceReceiverInternal.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt() != 0, data.readInt());
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        onBiometricAuthenticated(_arg06, data.readString());
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        onBiometricHelp(data.readString());
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        onBiometricError(data.readString());
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        hideBiometricDialog();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        onDisplayReady(data.readInt());
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        onRecentsAnimationStateChanged(_arg06);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IStatusBar {
            public static IStatusBar sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.android.internal.statusbar.IStatusBar
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
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setIcon(slot, icon);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void removeIcon(String slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeIcon(slot);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void disable(int displayId, int state1, int state2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(state1);
                    _data.writeInt(state2);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().disable(displayId, state1, state2);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void animateExpandNotificationsPanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().animateExpandNotificationsPanel();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void animateExpandSettingsPanel(String subPanel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(subPanel);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().animateExpandSettingsPanel(subPanel);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void animateCollapsePanels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().animateCollapsePanels();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void togglePanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().togglePanel();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showWirelessChargingAnimation(int batteryLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(batteryLevel);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showWirelessChargingAnimation(batteryLevel);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void setSystemUiVisibility(int displayId, int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, boolean navbarColorManagedByIme) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(displayId);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(vis);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(fullscreenStackVis);
                        _data.writeInt(dockedStackVis);
                        _data.writeInt(mask);
                        int i = 0;
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
                        if (navbarColorManagedByIme) {
                            i = 1;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().setSystemUiVisibility(displayId, vis, fullscreenStackVis, dockedStackVis, mask, fullscreenBounds, dockedBounds, navbarColorManagedByIme);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void topAppWindowChanged(int displayId, boolean menuVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(menuVisible ? 1 : 0);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().topAppWindowChanged(displayId, menuVisible);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void setImeWindowStatus(int displayId, IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeStrongBinder(token);
                    _data.writeInt(vis);
                    _data.writeInt(backDisposition);
                    _data.writeInt(showImeSwitcher ? 1 : 0);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setImeWindowStatus(displayId, token, vis, backDisposition, showImeSwitcher);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void setWindowState(int display, int window, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(display);
                    _data.writeInt(window);
                    _data.writeInt(state);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setWindowState(display, window, state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showRecentApps(boolean triggeredFromAltTab) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(triggeredFromAltTab ? 1 : 0);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showRecentApps(triggeredFromAltTab);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    _data.writeInt(triggeredFromAltTab ? 1 : 0);
                    if (triggeredFromHomeKey) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hideRecentApps(triggeredFromAltTab, triggeredFromHomeKey);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void toggleRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().toggleRecentApps();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void toggleSplitScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().toggleSplitScreen();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void preloadRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().preloadRecentApps();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void cancelPreloadRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelPreloadRecentApps();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showScreenPinningRequest(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showScreenPinningRequest(taskId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void dismissKeyboardShortcutsMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dismissKeyboardShortcutsMenu();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void toggleKeyboardShortcutsMenu(int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().toggleKeyboardShortcutsMenu(deviceId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void appTransitionPending(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().appTransitionPending(displayId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void appTransitionCancelled(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().appTransitionCancelled(displayId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void appTransitionStarting(int displayId, long statusBarAnimationsStartTime, long statusBarAnimationsDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeLong(statusBarAnimationsStartTime);
                    _data.writeLong(statusBarAnimationsDuration);
                    if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().appTransitionStarting(displayId, statusBarAnimationsStartTime, statusBarAnimationsDuration);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void appTransitionFinished(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(25, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().appTransitionFinished(displayId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showAssistDisclosure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(26, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showAssistDisclosure();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
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
                    if (this.mRemote.transact(27, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startAssist(args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onCameraLaunchGestureDetected(int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(source);
                    if (this.mRemote.transact(28, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCameraLaunchGestureDetected(source);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showPictureInPictureMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(29, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showPictureInPictureMenu();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showGlobalActionsMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(30, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showGlobalActionsMenu();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onProposedRotationChanged(int rotation, boolean isValid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    _data.writeInt(isValid ? 1 : 0);
                    if (this.mRemote.transact(31, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProposedRotationChanged(rotation, isValid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void setTopAppHidesStatusBar(boolean hidesStatusBar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hidesStatusBar ? 1 : 0);
                    if (this.mRemote.transact(32, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setTopAppHidesStatusBar(hidesStatusBar);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
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
                    if (this.mRemote.transact(33, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addQsTile(tile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
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
                    if (this.mRemote.transact(34, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().remQsTile(tile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
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
                    if (this.mRemote.transact(35, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clickQsTile(tile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void handleSystemKey(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    if (this.mRemote.transact(36, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handleSystemKey(key);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showPinningEnterExitToast(boolean entering) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(entering ? 1 : 0);
                    if (this.mRemote.transact(37, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showPinningEnterExitToast(entering);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showPinningEscapeToast() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(38, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showPinningEscapeToast();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showShutdownUi(boolean isReboot, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isReboot ? 1 : 0);
                    _data.writeString(reason);
                    if (this.mRemote.transact(39, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showShutdownUi(isReboot, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void showBiometricDialog(Bundle bundle, IBiometricServiceReceiverInternal receiver, int type, boolean requireConfirmation, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(type);
                    if (requireConfirmation) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(40, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showBiometricDialog(bundle, receiver, type, requireConfirmation, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onBiometricAuthenticated(boolean authenticated, String failureReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(authenticated ? 1 : 0);
                    _data.writeString(failureReason);
                    if (this.mRemote.transact(41, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBiometricAuthenticated(authenticated, failureReason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onBiometricHelp(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    if (this.mRemote.transact(42, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBiometricHelp(message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onBiometricError(String error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(error);
                    if (this.mRemote.transact(43, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBiometricError(error);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void hideBiometricDialog() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(44, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hideBiometricDialog();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onDisplayReady(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (this.mRemote.transact(45, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDisplayReady(displayId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.statusbar.IStatusBar
            public void onRecentsAnimationStateChanged(boolean running) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(running ? 1 : 0);
                    if (this.mRemote.transact(46, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecentsAnimationStateChanged(running);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IStatusBar impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IStatusBar getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}

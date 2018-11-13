package android.view;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IShortcutService;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.mediatek.multiwindow.IFreeformStackListener;

public interface IWindowManager extends IInterface {

    public static abstract class Stub extends Binder implements IWindowManager {
        private static final String DESCRIPTOR = "android.view.IWindowManager";
        static final int TRANSACTION_addAppToken = 21;
        static final int TRANSACTION_addWindowToken = 19;
        static final int TRANSACTION_cancelTaskThumbnailTransition = 69;
        static final int TRANSACTION_cancelTaskWindowTransition = 68;
        static final int TRANSACTION_clearForcedDisplayDensityForUser = 13;
        static final int TRANSACTION_clearForcedDisplaySize = 9;
        static final int TRANSACTION_clearWindowContentFrameStats = 91;
        static final int TRANSACTION_closeSystemDialogs = 58;
        static final int TRANSACTION_createWallpaperInputConsumer = 101;
        static final int TRANSACTION_disableKeyguard = 50;
        static final int TRANSACTION_dismissKeyguard = 56;
        static final int TRANSACTION_enableScreenIfNeeded = 90;
        static final int TRANSACTION_endProlongedAnimations = 44;
        static final int TRANSACTION_executeAppTransition = 36;
        static final int TRANSACTION_exitKeyguardSecurely = 52;
        static final int TRANSACTION_freezeRotation = 78;
        static final int TRANSACTION_getAnimationScale = 59;
        static final int TRANSACTION_getAnimationScales = 60;
        static final int TRANSACTION_getAppOrientation = 24;
        static final int TRANSACTION_getBaseDisplayDensity = 11;
        static final int TRANSACTION_getBaseDisplaySize = 7;
        static final int TRANSACTION_getBoundsForNewConfiguration = 47;
        static final int TRANSACTION_getCurrentAnimatorScale = 63;
        static final int TRANSACTION_getDockedStackSide = 93;
        static final int TRANSACTION_getInitialDisplayDensity = 10;
        static final int TRANSACTION_getInitialDisplaySize = 6;
        static final int TRANSACTION_getNavBarColorFromAdaptation = 72;
        static final int TRANSACTION_getPendingAppTransition = 27;
        static final int TRANSACTION_getPreferredOptionsPanelGravity = 77;
        static final int TRANSACTION_getRotation = 74;
        static final int TRANSACTION_getSplitFromBack = 106;
        static final int TRANSACTION_getStableInsets = 99;
        static final int TRANSACTION_getStatusBarColorFromAdaptation = 73;
        static final int TRANSACTION_getWindowContentFrameStats = 92;
        static final int TRANSACTION_getWindowGlobalScale = 104;
        static final int TRANSACTION_hasNavigationBar = 87;
        static final int TRANSACTION_inKeyguardRestrictedInputMode = 55;
        static final int TRANSACTION_inputMethodClientHasFocus = 5;
        static final int TRANSACTION_isActivityNeedPalette = 71;
        static final int TRANSACTION_isKeyguardLocked = 53;
        static final int TRANSACTION_isKeyguardSecure = 54;
        static final int TRANSACTION_isKeyguardShown = 108;
        static final int TRANSACTION_isRotationFrozen = 80;
        static final int TRANSACTION_isSafeModeEnabled = 89;
        static final int TRANSACTION_isViewServerRunning = 3;
        static final int TRANSACTION_keyguardGoingAway = 57;
        static final int TRANSACTION_lockNow = 88;
        static final int TRANSACTION_notifyAppResumed = 39;
        static final int TRANSACTION_notifyAppStopped = 40;
        static final int TRANSACTION_openSession = 4;
        static final int TRANSACTION_overridePendingAppTransition = 28;
        static final int TRANSACTION_overridePendingAppTransitionAspectScaledThumb = 32;
        static final int TRANSACTION_overridePendingAppTransitionClipReveal = 30;
        static final int TRANSACTION_overridePendingAppTransitionInPlace = 34;
        static final int TRANSACTION_overridePendingAppTransitionMultiThumb = 33;
        static final int TRANSACTION_overridePendingAppTransitionMultiThumbFuture = 35;
        static final int TRANSACTION_overridePendingAppTransitionScaleUp = 29;
        static final int TRANSACTION_overridePendingAppTransitionThumb = 31;
        static final int TRANSACTION_pauseKeyDispatching = 16;
        static final int TRANSACTION_prepareAppTransition = 26;
        static final int TRANSACTION_reenableKeyguard = 51;
        static final int TRANSACTION_registerDockedStackListener = 96;
        static final int TRANSACTION_registerFreeformStackListener = 103;
        static final int TRANSACTION_registerShortcutKey = 100;
        static final int TRANSACTION_removeAppToken = 43;
        static final int TRANSACTION_removeRotationWatcher = 76;
        static final int TRANSACTION_removeWallpaperInputConsumer = 102;
        static final int TRANSACTION_removeWindowToken = 20;
        static final int TRANSACTION_requestAppKeyboardShortcuts = 98;
        static final int TRANSACTION_requestAssistScreenshot = 82;
        static final int TRANSACTION_resumeKeyDispatching = 17;
        static final int TRANSACTION_screenshotApplications = 83;
        static final int TRANSACTION_screenshotWallpaper = 81;
        static final int TRANSACTION_setAnimationScale = 61;
        static final int TRANSACTION_setAnimationScales = 62;
        static final int TRANSACTION_setAppOrientation = 23;
        static final int TRANSACTION_setAppStartingWindow = 37;
        static final int TRANSACTION_setAppTask = 22;
        static final int TRANSACTION_setAppVisibility = 38;
        static final int TRANSACTION_setDockedStackDividerTouchRegion = 95;
        static final int TRANSACTION_setDockedStackResizing = 94;
        static final int TRANSACTION_setEventDispatching = 18;
        static final int TRANSACTION_setFocusedApp = 25;
        static final int TRANSACTION_setForcedDisplayDensityForUser = 12;
        static final int TRANSACTION_setForcedDisplayScalingMode = 14;
        static final int TRANSACTION_setForcedDisplaySize = 8;
        static final int TRANSACTION_setFreeingChange = 107;
        static final int TRANSACTION_setInTouchMode = 64;
        static final int TRANSACTION_setNavigationBarState = 109;
        static final int TRANSACTION_setNewConfiguration = 46;
        static final int TRANSACTION_setOverscan = 15;
        static final int TRANSACTION_setRecentsVisibility = 85;
        static final int TRANSACTION_setResizeDimLayer = 97;
        static final int TRANSACTION_setScreenCaptureDisabled = 67;
        static final int TRANSACTION_setSplitFromBack = 105;
        static final int TRANSACTION_setStrictModeVisualIndicatorPreference = 66;
        static final int TRANSACTION_setTvPipVisibility = 86;
        static final int TRANSACTION_showStrictModeViolation = 65;
        static final int TRANSACTION_startAppFreezingScreen = 41;
        static final int TRANSACTION_startFreezingScreen = 48;
        static final int TRANSACTION_startViewServer = 1;
        static final int TRANSACTION_statusBarVisibilityChanged = 84;
        static final int TRANSACTION_stopAppFreezingScreen = 42;
        static final int TRANSACTION_stopFreezingScreen = 49;
        static final int TRANSACTION_stopViewServer = 2;
        static final int TRANSACTION_thawRotation = 79;
        static final int TRANSACTION_updateOrientationFromAppTokens = 45;
        static final int TRANSACTION_updateRotation = 70;
        static final int TRANSACTION_watchRotation = 75;

        private static class Proxy implements IWindowManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "android.view.IWindowManager";
            }

            public boolean startViewServer(int port) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(port);
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public boolean stopViewServer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public boolean isViewServerRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (callback != null) {
                        asBinder = callback.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (client != null) {
                        asBinder = client.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (inputContext != null) {
                        iBinder = inputContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    IWindowSession _result = android.view.IWindowSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean inputMethodClientHasFocus(IInputMethodClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
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

            public void getInitialDisplaySize(int displayId, Point size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        size.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getBaseDisplaySize(int displayId, Point size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        size.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplaySize(int displayId, int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearForcedDisplaySize(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getInitialDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBaseDisplayDensity(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplayDensityForUser(int displayId, int density, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(density);
                    _data.writeInt(userId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearForcedDisplayDensityForUser(int displayId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setForcedDisplayScalingMode(int displayId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(mode);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOverscan(int displayId, int left, int top, int right, int bottom) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(displayId);
                    _data.writeInt(left);
                    _data.writeInt(top);
                    _data.writeInt(right);
                    _data.writeInt(bottom);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pauseKeyDispatching(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeKeyDispatching(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setEventDispatching(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addWindowToken(IBinder token, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeInt(type);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeWindowToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showWhenLocked, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration configuration, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion, int rotationAnimationHint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(addPos);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    _data.writeInt(requestedOrientation);
                    _data.writeInt(fullscreen ? 1 : 0);
                    _data.writeInt(showWhenLocked ? 1 : 0);
                    _data.writeInt(userId);
                    _data.writeInt(configChanges);
                    _data.writeInt(voiceInteraction ? 1 : 0);
                    _data.writeInt(launchTaskBehind ? 1 : 0);
                    if (taskBounds != null) {
                        _data.writeInt(1);
                        taskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (configuration != null) {
                        _data.writeInt(1);
                        configuration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(taskResizeMode);
                    _data.writeInt(alwaysFocusable ? 1 : 0);
                    _data.writeInt(homeTask ? 1 : 0);
                    _data.writeInt(targetSdkVersion);
                    _data.writeInt(rotationAnimationHint);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppTask(IBinder token, int taskId, int stackId, Rect taskBounds, Configuration config, int taskResizeMode, boolean homeTask) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeInt(taskId);
                    _data.writeInt(stackId);
                    if (taskBounds != null) {
                        _data.writeInt(1);
                        taskBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(taskResizeMode);
                    if (!homeTask) {
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

            public void setAppOrientation(IApplicationToken token, int requestedOrientation) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(requestedOrientation);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppOrientation(IApplicationToken token) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFocusedApp(IBinder token, boolean moveFocusNow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    if (moveFocusNow) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(transit);
                    if (alwaysKeepCurrent) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPendingAppTransition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(packageName);
                    _data.writeInt(enterAnim);
                    _data.writeInt(exitAnim);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(startWidth);
                    _data.writeInt(startHeight);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(startWidth);
                    _data.writeInt(startHeight);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) throws RemoteException {
                int i = 1;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (srcThumb != null) {
                        _data.writeInt(1);
                        srcThumb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!scaleUp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) throws RemoteException {
                int i = 1;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (srcThumb != null) {
                        _data.writeInt(1);
                        srcThumb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(startX);
                    _data.writeInt(startY);
                    _data.writeInt(targetWidth);
                    _data.writeInt(targetHeight);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!scaleUp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback startedCallback, IRemoteCallback finishedCallback, boolean scaleUp) throws RemoteException {
                int i = 0;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeTypedArray(specs, 0);
                    if (startedCallback != null) {
                        asBinder = startedCallback.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (finishedCallback != null) {
                        iBinder = finishedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (scaleUp) {
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

            public void overridePendingAppTransitionInPlace(String packageName, int anim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(packageName);
                    _data.writeInt(anim);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback startedCallback, boolean scaleUp) throws RemoteException {
                int i = 0;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (specsFuture != null) {
                        asBinder = specsFuture.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (startedCallback != null) {
                        iBinder = startedCallback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (scaleUp) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void executeAppTransition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAppStartingWindow(IBinder token, String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean createIfNeeded) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    _data.writeInt(theme);
                    if (compatInfo != null) {
                        _data.writeInt(1);
                        compatInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (nonLocalizedLabel != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(nonLocalizedLabel, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(labelRes);
                    _data.writeInt(icon);
                    _data.writeInt(logo);
                    _data.writeInt(windowFlags);
                    _data.writeStrongBinder(transferFrom);
                    _data.writeInt(createIfNeeded ? 1 : 0);
                    this.mRemote.transact(37, _data, _reply, 0);
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

            public void setAppVisibility(IBinder token, boolean visible) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    if (visible) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppResumed(IBinder token, boolean wasStopped, boolean allowSavedSurface) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    if (wasStopped) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!allowSavedSurface) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyAppStopped(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAppFreezingScreen(IBinder token, int configChanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeInt(configChanges);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAppFreezingScreen(IBinder token, boolean force) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    if (force) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAppToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endProlongedAnimations() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Configuration _result;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (currentConfig != null) {
                        _data.writeInt(1);
                        currentConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(freezeThisOneIfNeeded);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Configuration) Configuration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] setNewConfiguration(Configuration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getBoundsForNewConfiguration(int stackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Rect _result;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(stackId);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startFreezingScreen(int exitAnim, int enterAnim) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(exitAnim);
                    _data.writeInt(enterAnim);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopFreezingScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableKeyguard(IBinder token, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    _data.writeString(tag);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reenableKeyguard(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void exitKeyguardSecurely(IOnKeyguardExitResult callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isKeyguardLocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(53, _data, _reply, 0);
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

            public boolean isKeyguardSecure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(54, _data, _reply, 0);
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

            public boolean inKeyguardRestrictedInputMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(55, _data, _reply, 0);
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

            public void dismissKeyguard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void keyguardGoingAway(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(flags);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeSystemDialogs(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(reason);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getAnimationScale(int which) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(which);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float[] getAnimationScales() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                    float[] _result = _reply.createFloatArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAnimationScale(int which, float scale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(which);
                    _data.writeFloat(scale);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAnimationScales(float[] scales) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeFloatArray(scales);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getCurrentAnimatorScale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInTouchMode(boolean showFocus) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (showFocus) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showStrictModeViolation(boolean on) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (on) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStrictModeVisualIndicatorPreference(String enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(enabled);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setScreenCaptureDisabled(int userId, boolean disabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(userId);
                    if (disabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelTaskWindowTransition(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(taskId);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelTaskThumbnailTransition(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(taskId);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (alwaysSendConfiguration) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!forceRelayout) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActivityNeedPalette(String pkg, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    _data.writeString(activityName);
                    this.mRemote.transact(71, _data, _reply, 0);
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

            public int getNavBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    _data.writeString(activityName);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStatusBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeString(pkg);
                    _data.writeString(activityName);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int watchRotation(IRotationWatcher watcher) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (watcher != null) {
                        iBinder = watcher.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (watcher != null) {
                        iBinder = watcher.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPreferredOptionsPanelGravity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void freezeRotation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(rotation);
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void thawRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRotationFrozen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(80, _data, _reply, 0);
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

            public Bitmap screenshotWallpaper() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bitmap _result;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bitmap) Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestAssistScreenshot(IAssistScreenshotReceiver receiver) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(82, _data, _reply, 0);
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

            public Bitmap screenshotApplications(IBinder appToken, int displayId, int maxWidth, int maxHeight, float frameScale) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bitmap _result;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(appToken);
                    _data.writeInt(displayId);
                    _data.writeInt(maxWidth);
                    _data.writeInt(maxHeight);
                    _data.writeFloat(frameScale);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bitmap) Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void statusBarVisibilityChanged(int visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(visibility);
                    this.mRemote.transact(84, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setRecentsVisibility(boolean visible) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(85, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setTvPipVisibility(boolean visible) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(86, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public boolean hasNavigationBar() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(87, _data, _reply, 0);
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

            public void lockNow(Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSafeModeEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(89, _data, _reply, 0);
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

            public void enableScreenIfNeeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearWindowContentFrameStats(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(91, _data, _reply, 0);
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

            public WindowContentFrameStats getWindowContentFrameStats(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WindowContentFrameStats _result;
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (WindowContentFrameStats) WindowContentFrameStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDockedStackSide() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDockedStackResizing(boolean resizing) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (resizing) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDockedStackDividerTouchRegion(Rect touchableRegion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (touchableRegion != null) {
                        _data.writeInt(1);
                        touchableRegion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerDockedStackListener(IDockedStackListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (visible) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(targetStackId);
                    _data.writeFloat(alpha);
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getStableInsets(Rect outInsets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        outInsets.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerShortcutKey(long shortcutCode, IShortcutService keySubscriber) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeLong(shortcutCode);
                    if (keySubscriber != null) {
                        iBinder = keySubscriber.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createWallpaperInputConsumer(InputChannel inputChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        inputChannel.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeWallpaperInputConsumer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerFreeformStackListener(IFreeformStackListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(103, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getWindowGlobalScale(IWindow window) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (window != null) {
                        iBinder = window.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(104, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSplitFromBack(boolean fromBack) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (fromBack) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(105, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getSplitFromBack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(106, _data, _reply, 0);
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

            public void setFreeingChange(boolean change) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    if (change) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isKeyguardShown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    this.mRemote.transact(108, _data, _reply, 0);
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

            public void setNavigationBarState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.view.IWindowManager");
                    _data.writeInt(state);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "android.view.IWindowManager");
        }

        public static IWindowManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.view.IWindowManager");
            if (iin == null || !(iin instanceof IWindowManager)) {
                return new Proxy(obj);
            }
            return (IWindowManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _arg0;
            Point _arg1;
            int _result2;
            int _arg2;
            IBinder _arg02;
            Bitmap _arg03;
            Configuration _arg04;
            float _result3;
            Bitmap _result4;
            Rect _arg05;
            switch (code) {
                case 1:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = startViewServer(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = stopViewServer();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isViewServerRunning();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface("android.view.IWindowManager");
                    IWindowSession _result5 = openSession(android.view.IWindowSessionCallback.Stub.asInterface(data.readStrongBinder()), com.android.internal.view.IInputMethodClient.Stub.asInterface(data.readStrongBinder()), com.android.internal.view.IInputContext.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeStrongBinder(_result5 != null ? _result5.asBinder() : null);
                    return true;
                case 5:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = inputMethodClientHasFocus(com.android.internal.view.IInputMethodClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface("android.view.IWindowManager");
                    _arg0 = data.readInt();
                    _arg1 = new Point();
                    getInitialDisplaySize(_arg0, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface("android.view.IWindowManager");
                    _arg0 = data.readInt();
                    _arg1 = new Point();
                    getBaseDisplaySize(_arg0, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 8:
                    data.enforceInterface("android.view.IWindowManager");
                    setForcedDisplaySize(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface("android.view.IWindowManager");
                    clearForcedDisplaySize(data.readInt());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getInitialDisplayDensity(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 11:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getBaseDisplayDensity(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 12:
                    data.enforceInterface("android.view.IWindowManager");
                    setForcedDisplayDensityForUser(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface("android.view.IWindowManager");
                    clearForcedDisplayDensityForUser(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface("android.view.IWindowManager");
                    setForcedDisplayScalingMode(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface("android.view.IWindowManager");
                    setOverscan(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface("android.view.IWindowManager");
                    pauseKeyDispatching(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface("android.view.IWindowManager");
                    resumeKeyDispatching(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface("android.view.IWindowManager");
                    setEventDispatching(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface("android.view.IWindowManager");
                    addWindowToken(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface("android.view.IWindowManager");
                    removeWindowToken(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 21:
                    Rect _arg11;
                    Configuration _arg12;
                    data.enforceInterface("android.view.IWindowManager");
                    _arg0 = data.readInt();
                    IApplicationToken _arg13 = android.view.IApplicationToken.Stub.asInterface(data.readStrongBinder());
                    _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    int _arg4 = data.readInt();
                    boolean _arg5 = data.readInt() != 0;
                    boolean _arg6 = data.readInt() != 0;
                    int _arg7 = data.readInt();
                    int _arg8 = data.readInt();
                    boolean _arg9 = data.readInt() != 0;
                    boolean _arg10 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg11 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg11 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg12 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    addAppToken(_arg0, _arg13, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10, _arg11, _arg12, data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 22:
                    Rect _arg32;
                    Configuration _arg42;
                    data.enforceInterface("android.view.IWindowManager");
                    _arg02 = data.readStrongBinder();
                    int _arg14 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg32 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg42 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        _arg42 = null;
                    }
                    setAppTask(_arg02, _arg14, _arg2, _arg32, _arg42, data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface("android.view.IWindowManager");
                    setAppOrientation(android.view.IApplicationToken.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 24:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getAppOrientation(android.view.IApplicationToken.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 25:
                    data.enforceInterface("android.view.IWindowManager");
                    setFocusedApp(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 26:
                    data.enforceInterface("android.view.IWindowManager");
                    prepareAppTransition(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getPendingAppTransition();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 28:
                    data.enforceInterface("android.view.IWindowManager");
                    overridePendingAppTransition(data.readString(), data.readInt(), data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 29:
                    data.enforceInterface("android.view.IWindowManager");
                    overridePendingAppTransitionScaleUp(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 30:
                    data.enforceInterface("android.view.IWindowManager");
                    overridePendingAppTransitionClipReveal(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 31:
                    data.enforceInterface("android.view.IWindowManager");
                    if (data.readInt() != 0) {
                        _arg03 = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    overridePendingAppTransitionThumb(_arg03, data.readInt(), data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 32:
                    data.enforceInterface("android.view.IWindowManager");
                    if (data.readInt() != 0) {
                        _arg03 = (Bitmap) Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    overridePendingAppTransitionAspectScaledThumb(_arg03, data.readInt(), data.readInt(), data.readInt(), data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 33:
                    data.enforceInterface("android.view.IWindowManager");
                    overridePendingAppTransitionMultiThumb((AppTransitionAnimationSpec[]) data.createTypedArray(AppTransitionAnimationSpec.CREATOR), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 34:
                    data.enforceInterface("android.view.IWindowManager");
                    overridePendingAppTransitionInPlace(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 35:
                    data.enforceInterface("android.view.IWindowManager");
                    overridePendingAppTransitionMultiThumbFuture(android.view.IAppTransitionAnimationSpecsFuture.Stub.asInterface(data.readStrongBinder()), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 36:
                    data.enforceInterface("android.view.IWindowManager");
                    executeAppTransition();
                    reply.writeNoException();
                    return true;
                case 37:
                    CompatibilityInfo _arg33;
                    CharSequence _arg43;
                    data.enforceInterface("android.view.IWindowManager");
                    _arg02 = data.readStrongBinder();
                    String _arg15 = data.readString();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg33 = (CompatibilityInfo) CompatibilityInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg33 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg43 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        _arg43 = null;
                    }
                    _result = setAppStartingWindow(_arg02, _arg15, _arg2, _arg33, _arg43, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 38:
                    data.enforceInterface("android.view.IWindowManager");
                    setAppVisibility(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 39:
                    data.enforceInterface("android.view.IWindowManager");
                    notifyAppResumed(data.readStrongBinder(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 40:
                    data.enforceInterface("android.view.IWindowManager");
                    notifyAppStopped(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 41:
                    data.enforceInterface("android.view.IWindowManager");
                    startAppFreezingScreen(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 42:
                    data.enforceInterface("android.view.IWindowManager");
                    stopAppFreezingScreen(data.readStrongBinder(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 43:
                    data.enforceInterface("android.view.IWindowManager");
                    removeAppToken(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 44:
                    data.enforceInterface("android.view.IWindowManager");
                    endProlongedAnimations();
                    reply.writeNoException();
                    return true;
                case 45:
                    data.enforceInterface("android.view.IWindowManager");
                    if (data.readInt() != 0) {
                        _arg04 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    Configuration _result6 = updateOrientationFromAppTokens(_arg04, data.readStrongBinder());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 46:
                    data.enforceInterface("android.view.IWindowManager");
                    if (data.readInt() != 0) {
                        _arg04 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    int[] _result7 = setNewConfiguration(_arg04);
                    reply.writeNoException();
                    reply.writeIntArray(_result7);
                    return true;
                case 47:
                    data.enforceInterface("android.view.IWindowManager");
                    Rect _result8 = getBoundsForNewConfiguration(data.readInt());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 48:
                    data.enforceInterface("android.view.IWindowManager");
                    startFreezingScreen(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 49:
                    data.enforceInterface("android.view.IWindowManager");
                    stopFreezingScreen();
                    reply.writeNoException();
                    return true;
                case 50:
                    data.enforceInterface("android.view.IWindowManager");
                    disableKeyguard(data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case 51:
                    data.enforceInterface("android.view.IWindowManager");
                    reenableKeyguard(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 52:
                    data.enforceInterface("android.view.IWindowManager");
                    exitKeyguardSecurely(android.view.IOnKeyguardExitResult.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 53:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isKeyguardLocked();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 54:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isKeyguardSecure();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 55:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = inKeyguardRestrictedInputMode();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 56:
                    data.enforceInterface("android.view.IWindowManager");
                    dismissKeyguard();
                    reply.writeNoException();
                    return true;
                case 57:
                    data.enforceInterface("android.view.IWindowManager");
                    keyguardGoingAway(data.readInt());
                    reply.writeNoException();
                    return true;
                case 58:
                    data.enforceInterface("android.view.IWindowManager");
                    closeSystemDialogs(data.readString());
                    reply.writeNoException();
                    return true;
                case 59:
                    data.enforceInterface("android.view.IWindowManager");
                    _result3 = getAnimationScale(data.readInt());
                    reply.writeNoException();
                    reply.writeFloat(_result3);
                    return true;
                case 60:
                    data.enforceInterface("android.view.IWindowManager");
                    float[] _result9 = getAnimationScales();
                    reply.writeNoException();
                    reply.writeFloatArray(_result9);
                    return true;
                case 61:
                    data.enforceInterface("android.view.IWindowManager");
                    setAnimationScale(data.readInt(), data.readFloat());
                    reply.writeNoException();
                    return true;
                case 62:
                    data.enforceInterface("android.view.IWindowManager");
                    setAnimationScales(data.createFloatArray());
                    reply.writeNoException();
                    return true;
                case 63:
                    data.enforceInterface("android.view.IWindowManager");
                    _result3 = getCurrentAnimatorScale();
                    reply.writeNoException();
                    reply.writeFloat(_result3);
                    return true;
                case 64:
                    data.enforceInterface("android.view.IWindowManager");
                    setInTouchMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 65:
                    data.enforceInterface("android.view.IWindowManager");
                    showStrictModeViolation(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 66:
                    data.enforceInterface("android.view.IWindowManager");
                    setStrictModeVisualIndicatorPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 67:
                    data.enforceInterface("android.view.IWindowManager");
                    setScreenCaptureDisabled(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 68:
                    data.enforceInterface("android.view.IWindowManager");
                    cancelTaskWindowTransition(data.readInt());
                    reply.writeNoException();
                    return true;
                case 69:
                    data.enforceInterface("android.view.IWindowManager");
                    cancelTaskThumbnailTransition(data.readInt());
                    reply.writeNoException();
                    return true;
                case 70:
                    data.enforceInterface("android.view.IWindowManager");
                    updateRotation(data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 71:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isActivityNeedPalette(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 72:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getNavBarColorFromAdaptation(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 73:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getStatusBarColorFromAdaptation(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 74:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getRotation();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 75:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = watchRotation(android.view.IRotationWatcher.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 76:
                    data.enforceInterface("android.view.IWindowManager");
                    removeRotationWatcher(android.view.IRotationWatcher.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 77:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getPreferredOptionsPanelGravity();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 78:
                    data.enforceInterface("android.view.IWindowManager");
                    freezeRotation(data.readInt());
                    reply.writeNoException();
                    return true;
                case 79:
                    data.enforceInterface("android.view.IWindowManager");
                    thawRotation();
                    reply.writeNoException();
                    return true;
                case 80:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isRotationFrozen();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 81:
                    data.enforceInterface("android.view.IWindowManager");
                    _result4 = screenshotWallpaper();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 82:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = requestAssistScreenshot(com.android.internal.app.IAssistScreenshotReceiver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 83:
                    data.enforceInterface("android.view.IWindowManager");
                    _result4 = screenshotApplications(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), data.readFloat());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 84:
                    data.enforceInterface("android.view.IWindowManager");
                    statusBarVisibilityChanged(data.readInt());
                    return true;
                case 85:
                    data.enforceInterface("android.view.IWindowManager");
                    setRecentsVisibility(data.readInt() != 0);
                    return true;
                case 86:
                    data.enforceInterface("android.view.IWindowManager");
                    setTvPipVisibility(data.readInt() != 0);
                    return true;
                case 87:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = hasNavigationBar();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 88:
                    Bundle _arg06;
                    data.enforceInterface("android.view.IWindowManager");
                    if (data.readInt() != 0) {
                        _arg06 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    lockNow(_arg06);
                    reply.writeNoException();
                    return true;
                case 89:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isSafeModeEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 90:
                    data.enforceInterface("android.view.IWindowManager");
                    enableScreenIfNeeded();
                    reply.writeNoException();
                    return true;
                case 91:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = clearWindowContentFrameStats(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 92:
                    data.enforceInterface("android.view.IWindowManager");
                    WindowContentFrameStats _result10 = getWindowContentFrameStats(data.readStrongBinder());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(1);
                        _result10.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 93:
                    data.enforceInterface("android.view.IWindowManager");
                    _result2 = getDockedStackSide();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 94:
                    data.enforceInterface("android.view.IWindowManager");
                    setDockedStackResizing(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 95:
                    data.enforceInterface("android.view.IWindowManager");
                    if (data.readInt() != 0) {
                        _arg05 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    setDockedStackDividerTouchRegion(_arg05);
                    reply.writeNoException();
                    return true;
                case 96:
                    data.enforceInterface("android.view.IWindowManager");
                    registerDockedStackListener(android.view.IDockedStackListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 97:
                    data.enforceInterface("android.view.IWindowManager");
                    setResizeDimLayer(data.readInt() != 0, data.readInt(), data.readFloat());
                    reply.writeNoException();
                    return true;
                case 98:
                    data.enforceInterface("android.view.IWindowManager");
                    requestAppKeyboardShortcuts(com.android.internal.os.IResultReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 99:
                    data.enforceInterface("android.view.IWindowManager");
                    _arg05 = new Rect();
                    getStableInsets(_arg05);
                    reply.writeNoException();
                    if (_arg05 != null) {
                        reply.writeInt(1);
                        _arg05.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 100:
                    data.enforceInterface("android.view.IWindowManager");
                    registerShortcutKey(data.readLong(), com.android.internal.policy.IShortcutService.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 101:
                    data.enforceInterface("android.view.IWindowManager");
                    InputChannel _arg07 = new InputChannel();
                    createWallpaperInputConsumer(_arg07);
                    reply.writeNoException();
                    if (_arg07 != null) {
                        reply.writeInt(1);
                        _arg07.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 102:
                    data.enforceInterface("android.view.IWindowManager");
                    removeWallpaperInputConsumer();
                    reply.writeNoException();
                    return true;
                case 103:
                    data.enforceInterface("android.view.IWindowManager");
                    registerFreeformStackListener(com.mediatek.multiwindow.IFreeformStackListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 104:
                    data.enforceInterface("android.view.IWindowManager");
                    _result3 = getWindowGlobalScale(android.view.IWindow.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeFloat(_result3);
                    return true;
                case 105:
                    data.enforceInterface("android.view.IWindowManager");
                    setSplitFromBack(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 106:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = getSplitFromBack();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 107:
                    data.enforceInterface("android.view.IWindowManager");
                    setFreeingChange(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 108:
                    data.enforceInterface("android.view.IWindowManager");
                    _result = isKeyguardShown();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 109:
                    data.enforceInterface("android.view.IWindowManager");
                    setNavigationBarState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("android.view.IWindowManager");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addAppToken(int i, IApplicationToken iApplicationToken, int i2, int i3, int i4, boolean z, boolean z2, int i5, int i6, boolean z3, boolean z4, Rect rect, Configuration configuration, int i7, boolean z5, boolean z6, int i8, int i9) throws RemoteException;

    void addWindowToken(IBinder iBinder, int i) throws RemoteException;

    void cancelTaskThumbnailTransition(int i) throws RemoteException;

    void cancelTaskWindowTransition(int i) throws RemoteException;

    void clearForcedDisplayDensityForUser(int i, int i2) throws RemoteException;

    void clearForcedDisplaySize(int i) throws RemoteException;

    boolean clearWindowContentFrameStats(IBinder iBinder) throws RemoteException;

    void closeSystemDialogs(String str) throws RemoteException;

    void createWallpaperInputConsumer(InputChannel inputChannel) throws RemoteException;

    void disableKeyguard(IBinder iBinder, String str) throws RemoteException;

    void dismissKeyguard() throws RemoteException;

    void enableScreenIfNeeded() throws RemoteException;

    void endProlongedAnimations() throws RemoteException;

    void executeAppTransition() throws RemoteException;

    void exitKeyguardSecurely(IOnKeyguardExitResult iOnKeyguardExitResult) throws RemoteException;

    void freezeRotation(int i) throws RemoteException;

    float getAnimationScale(int i) throws RemoteException;

    float[] getAnimationScales() throws RemoteException;

    int getAppOrientation(IApplicationToken iApplicationToken) throws RemoteException;

    int getBaseDisplayDensity(int i) throws RemoteException;

    void getBaseDisplaySize(int i, Point point) throws RemoteException;

    Rect getBoundsForNewConfiguration(int i) throws RemoteException;

    float getCurrentAnimatorScale() throws RemoteException;

    int getDockedStackSide() throws RemoteException;

    int getInitialDisplayDensity(int i) throws RemoteException;

    void getInitialDisplaySize(int i, Point point) throws RemoteException;

    int getNavBarColorFromAdaptation(String str, String str2) throws RemoteException;

    int getPendingAppTransition() throws RemoteException;

    int getPreferredOptionsPanelGravity() throws RemoteException;

    int getRotation() throws RemoteException;

    boolean getSplitFromBack() throws RemoteException;

    void getStableInsets(Rect rect) throws RemoteException;

    int getStatusBarColorFromAdaptation(String str, String str2) throws RemoteException;

    WindowContentFrameStats getWindowContentFrameStats(IBinder iBinder) throws RemoteException;

    float getWindowGlobalScale(IWindow iWindow) throws RemoteException;

    boolean hasNavigationBar() throws RemoteException;

    boolean inKeyguardRestrictedInputMode() throws RemoteException;

    boolean inputMethodClientHasFocus(IInputMethodClient iInputMethodClient) throws RemoteException;

    boolean isActivityNeedPalette(String str, String str2) throws RemoteException;

    boolean isKeyguardLocked() throws RemoteException;

    boolean isKeyguardSecure() throws RemoteException;

    boolean isKeyguardShown() throws RemoteException;

    boolean isRotationFrozen() throws RemoteException;

    boolean isSafeModeEnabled() throws RemoteException;

    boolean isViewServerRunning() throws RemoteException;

    void keyguardGoingAway(int i) throws RemoteException;

    void lockNow(Bundle bundle) throws RemoteException;

    void notifyAppResumed(IBinder iBinder, boolean z, boolean z2) throws RemoteException;

    void notifyAppStopped(IBinder iBinder) throws RemoteException;

    IWindowSession openSession(IWindowSessionCallback iWindowSessionCallback, IInputMethodClient iInputMethodClient, IInputContext iInputContext) throws RemoteException;

    void overridePendingAppTransition(String str, int i, int i2, IRemoteCallback iRemoteCallback) throws RemoteException;

    void overridePendingAppTransitionAspectScaledThumb(Bitmap bitmap, int i, int i2, int i3, int i4, IRemoteCallback iRemoteCallback, boolean z) throws RemoteException;

    void overridePendingAppTransitionClipReveal(int i, int i2, int i3, int i4) throws RemoteException;

    void overridePendingAppTransitionInPlace(String str, int i) throws RemoteException;

    void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] appTransitionAnimationSpecArr, IRemoteCallback iRemoteCallback, IRemoteCallback iRemoteCallback2, boolean z) throws RemoteException;

    void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, IRemoteCallback iRemoteCallback, boolean z) throws RemoteException;

    void overridePendingAppTransitionScaleUp(int i, int i2, int i3, int i4) throws RemoteException;

    void overridePendingAppTransitionThumb(Bitmap bitmap, int i, int i2, IRemoteCallback iRemoteCallback, boolean z) throws RemoteException;

    void pauseKeyDispatching(IBinder iBinder) throws RemoteException;

    void prepareAppTransition(int i, boolean z) throws RemoteException;

    void reenableKeyguard(IBinder iBinder) throws RemoteException;

    void registerDockedStackListener(IDockedStackListener iDockedStackListener) throws RemoteException;

    void registerFreeformStackListener(IFreeformStackListener iFreeformStackListener) throws RemoteException;

    void registerShortcutKey(long j, IShortcutService iShortcutService) throws RemoteException;

    void removeAppToken(IBinder iBinder) throws RemoteException;

    void removeRotationWatcher(IRotationWatcher iRotationWatcher) throws RemoteException;

    void removeWallpaperInputConsumer() throws RemoteException;

    void removeWindowToken(IBinder iBinder) throws RemoteException;

    void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) throws RemoteException;

    boolean requestAssistScreenshot(IAssistScreenshotReceiver iAssistScreenshotReceiver) throws RemoteException;

    void resumeKeyDispatching(IBinder iBinder) throws RemoteException;

    Bitmap screenshotApplications(IBinder iBinder, int i, int i2, int i3, float f) throws RemoteException;

    Bitmap screenshotWallpaper() throws RemoteException;

    void setAnimationScale(int i, float f) throws RemoteException;

    void setAnimationScales(float[] fArr) throws RemoteException;

    void setAppOrientation(IApplicationToken iApplicationToken, int i) throws RemoteException;

    boolean setAppStartingWindow(IBinder iBinder, String str, int i, CompatibilityInfo compatibilityInfo, CharSequence charSequence, int i2, int i3, int i4, int i5, IBinder iBinder2, boolean z) throws RemoteException;

    void setAppTask(IBinder iBinder, int i, int i2, Rect rect, Configuration configuration, int i3, boolean z) throws RemoteException;

    void setAppVisibility(IBinder iBinder, boolean z) throws RemoteException;

    void setDockedStackDividerTouchRegion(Rect rect) throws RemoteException;

    void setDockedStackResizing(boolean z) throws RemoteException;

    void setEventDispatching(boolean z) throws RemoteException;

    void setFocusedApp(IBinder iBinder, boolean z) throws RemoteException;

    void setForcedDisplayDensityForUser(int i, int i2, int i3) throws RemoteException;

    void setForcedDisplayScalingMode(int i, int i2) throws RemoteException;

    void setForcedDisplaySize(int i, int i2, int i3) throws RemoteException;

    void setFreeingChange(boolean z) throws RemoteException;

    void setInTouchMode(boolean z) throws RemoteException;

    void setNavigationBarState(int i) throws RemoteException;

    int[] setNewConfiguration(Configuration configuration) throws RemoteException;

    void setOverscan(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    void setRecentsVisibility(boolean z) throws RemoteException;

    void setResizeDimLayer(boolean z, int i, float f) throws RemoteException;

    void setScreenCaptureDisabled(int i, boolean z) throws RemoteException;

    void setSplitFromBack(boolean z) throws RemoteException;

    void setStrictModeVisualIndicatorPreference(String str) throws RemoteException;

    void setTvPipVisibility(boolean z) throws RemoteException;

    void showStrictModeViolation(boolean z) throws RemoteException;

    void startAppFreezingScreen(IBinder iBinder, int i) throws RemoteException;

    void startFreezingScreen(int i, int i2) throws RemoteException;

    boolean startViewServer(int i) throws RemoteException;

    void statusBarVisibilityChanged(int i) throws RemoteException;

    void stopAppFreezingScreen(IBinder iBinder, boolean z) throws RemoteException;

    void stopFreezingScreen() throws RemoteException;

    boolean stopViewServer() throws RemoteException;

    void thawRotation() throws RemoteException;

    Configuration updateOrientationFromAppTokens(Configuration configuration, IBinder iBinder) throws RemoteException;

    void updateRotation(boolean z, boolean z2) throws RemoteException;

    int watchRotation(IRotationWatcher iRotationWatcher) throws RemoteException;
}

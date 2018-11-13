package android.view;

import android.app.IColorKeyguardSessionCallback;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class IOppoWindowManagerImpl implements IOppoWindowManager {
    private static final String TAG = "IOppoWindowManagerImpl";
    private IBinder mRemote;

    public IOppoWindowManagerImpl() {
        this.mRemote = null;
        this.mRemote = ServiceManager.getService("window");
    }

    public boolean isSIMUnlockRunning() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10007, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isLockWndShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void keyguardSetApkLockScreenShowing(boolean showing) throws RemoteException {
        int i = 0;
        Log.d(TAG, "keyguardSetApkLockScreenShowing showing = " + showing);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (showing) {
                i = 1;
            }
            data.writeInt(i);
            this.mRemote.transact(10003, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public IBinder getApkUnlockWindow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder result = null;
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
            result = reply.readStrongBinder();
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void keyguardShowSecureApkLock(boolean show) throws RemoteException {
        int i = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (show) {
                i = 1;
            }
            data.writeInt(i);
            this.mRemote.transact(10005, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isLockOnShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isStatusBarVisible() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10012, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isInputShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10010, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isFullScreen() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10011, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isRotatingLw() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10013, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean checkIsFloatWindowForbidden(String packageName, int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeInt(type);
            this.mRemote.transact(10014, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void setMagnificationSpecEx(MagnificationSpec spec) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (spec != null) {
                data.writeInt(1);
                spec.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10015, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void getFocusedWindowFrame(Rect frame) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (frame != null) {
                data.writeInt(1);
                frame.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10016, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                frame.readFromParcel(reply);
            }
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void requestDismissKeyguard() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10017, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void requestKeyguard(String command) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            data.writeString(command);
            this.mRemote.transact(10019, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isWindowShownForUid(int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            data.writeInt(uid);
            this.mRemote.transact(10018, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean openKeyguardSession(IColorKeyguardSessionCallback callback, IBinder token, String module) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (callback != null) {
                iBinder = callback.asBinder();
            }
            data.writeStrongBinder(iBinder);
            data.writeStrongBinder(token);
            data.writeString(module);
            this.mRemote.transact(10020, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void removeWindowShownOnKeyguard() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10022, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int getLongshotSurfaceLayer() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = 0;
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10023, data, reply, 0);
            reply.readException();
            result = reply.readInt();
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public int getLongshotSurfaceLayerByType(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = 0;
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10024, data, reply, 0);
            reply.readException();
            result = reply.readInt();
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public String getCurrentFocus() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = null;
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10025, data, reply, 0);
            reply.readException();
            result = reply.readString();
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public void longshotNotifyConnected(boolean isConnected) throws RemoteException {
        int i = 1;
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (!isConnected) {
                i = 0;
            }
            data.writeInt(i);
            this.mRemote.transact(10027, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public boolean isNavigationBarVisible() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10028, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public Rect getFloatWindowRect(int displayId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            Rect result;
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            data.writeInt(displayId);
            this.mRemote.transact(10030, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = (Rect) Rect.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public boolean isShortcutsPanelShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10031, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void longshotInjectInput(InputEvent event, int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            if (event != null) {
                data.writeInt(1);
                event.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            data.writeInt(mode);
            this.mRemote.transact(10021, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(IOppoWindowManager.IS_KEYGUARD_SHOWING_AND_NOT_OCCLUDED, data, reply, 0);
            reply.readException();
            boolean result = reply.readInt() != 0;
            reply.recycle();
            data.recycle();
            return result;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void longshotInjectInputBegin() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10032, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public void longshotInjectInputEnd() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoWindowManager.DESCRIPTOR);
            this.mRemote.transact(10033, data, null, 1);
        } finally {
            data.recycle();
        }
    }
}

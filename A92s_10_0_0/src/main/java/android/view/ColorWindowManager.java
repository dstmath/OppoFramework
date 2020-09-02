package android.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.color.darkmode.IColorDarkModeListener;

public class ColorWindowManager extends ColorBaseWindowManager implements IColorWindowManager {
    private static final String OFFERTX = "offertX";
    private static final String OFFERTY = "offertY";
    private static final String SCALE = "scale";
    private static final String TAG = "ColorWindowManager";

    @Override // android.view.IColorWindowManager
    public boolean isLockWndShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void keyguardSetApkLockScreenShowing(boolean showing) throws RemoteException {
        Log.d("ColorWindowManager", "keyguardSetApkLockScreenShowing showing = " + showing);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(showing ? 1 : 0);
            this.mRemote.transact(10003, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public IBinder getApkUnlockWindow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
            return reply.readStrongBinder();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void keyguardShowSecureApkLock(boolean show) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(show ? 1 : 0);
            this.mRemote.transact(10005, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isLockOnShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isSIMUnlockRunning() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10007, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isInputShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10010, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isFullScreen() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10011, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isStatusBarVisible() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10012, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isRotatingLw() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10013, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean checkIsFloatWindowForbidden(String packageName, int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeInt(type);
            boolean result = false;
            this.mRemote.transact(10014, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void setMagnification(Bundle bundle) throws RemoteException {
        MagnificationSpec spec = MagnificationSpec.obtain();
        spec.scale = bundle.getFloat("scale", 1.0f);
        spec.offsetX = bundle.getFloat(OFFERTX, 0.0f);
        spec.offsetY = bundle.getFloat(OFFERTY, 0.0f);
        try {
            setMagnificationSpecEx(spec);
        } catch (RemoteException e) {
        }
    }

    @Override // android.view.IColorWindowManager
    public void setMagnificationSpecEx(MagnificationSpec spec) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
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

    @Override // android.view.IColorWindowManager
    public void requestDismissKeyguard() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(10017, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void requestKeyguard(String command) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(command);
            this.mRemote.transact(10019, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isWindowShownForUid(int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(uid);
            boolean result = false;
            this.mRemote.transact(10018, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void removeWindowShownOnKeyguard() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(10022, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public String getCurrentFocus() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(10025, data, reply, 0);
            reply.readException();
            return reply.readString();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public Rect getFloatWindowRect(int displayId) throws RemoteException {
        Rect result;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(displayId);
            this.mRemote.transact(10030, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = Rect.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void setSplitTimeout(int timeout) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(timeout);
            this.mRemote.transact(10036, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void createMonitorInputConsumer(IBinder token, String name, InputChannel inputChannel) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            _data.writeStrongBinder(token);
            _data.writeString(name);
            this.mRemote.transact(10040, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                inputChannel.readFromParcel(_reply);
            }
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean destroyMonitorInputConsumer(String name) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            _data.writeString(name);
            boolean _result = false;
            this.mRemote.transact(10041, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
            return _result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void setGestureFollowAnimation(boolean animation) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(animation ? 1 : 0);
            this.mRemote.transact(10042, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void startColorDragWindow(String packageName, int resId, int mode, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeInt(resId);
            data.writeInt(mode);
            if (options != null) {
                data.writeInt(1);
                options.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10046, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            this.mRemote.transact(10047, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            this.mRemote.transact(10048, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isInFreeformMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10049, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void getFreeformStackBounds(Rect outBounds) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(10050, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                outBounds.readFromParcel(reply);
            }
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public boolean isActivityNeedPalette(String pkg, String activityName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeString(activityName);
            boolean result = false;
            this.mRemote.transact(10051, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public int getNavBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeString(activityName);
            this.mRemote.transact(10052, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public int getStatusBarColorFromAdaptation(String pkg, String activityName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeString(activityName);
            this.mRemote.transact(10053, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public int getImeBgColorFromAdaptation(String pkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeString(pkg);
            this.mRemote.transact(10054, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public int getTypedWindowLayer(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10055, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public int getFocusedWindowIgnoreHomeMenuKey() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(10056, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10057, data, reply, 1);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10058, data, reply, 1);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorWindowManager
    public void setBootAnimationRotationLock(boolean lockRotation) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(lockRotation ? 1 : 0);
            this.mRemote.transact(10059, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}

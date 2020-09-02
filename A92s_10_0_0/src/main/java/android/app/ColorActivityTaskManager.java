package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.app.ColorAppInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.IColorAppSwitchObserver;
import com.color.app.IColorFreeformConfigChangedListener;
import com.color.app.IColorZoomWindowConfigChangedListener;
import com.color.zoomwindow.ColorZoomWindowInfo;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.IColorZoomWindowObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColorActivityTaskManager extends ColorBaseActivityTaskManager implements IColorActivityTaskManager {
    @Override // android.app.IColorActivityTaskManager
    public void setSecureController(IActivityController watcher) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public ComponentName getTopActivityComponentName() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10007, data, reply, 0);
            reply.readException();
            return ComponentName.readFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public ApplicationInfo getTopApplicationInfo() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10011, data, reply, 0);
            reply.readException();
            return ApplicationInfo.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isAppCallRefuseMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10035, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setAppCallRefuseMode(boolean enable) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(String.valueOf(enable));
            this.mRemote.transact(10036, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void swapDockedFullscreenStack() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10052, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public int getSplitScreenState(Intent intent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            if (intent != null) {
                data.writeInt(1);
                intent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10074, data, reply, 0);
            reply.readException();
            return Integer.valueOf(reply.readString()).intValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public List<ColorAppInfo> getAllTopAppInfos() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10053, data, reply, 0);
            reply.readException();
            return reply.createTypedArrayList(ColorAppInfo.CREATOR);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public List<String> getFreeformConfigList(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10054, data, reply, 0);
            reply.readException();
            List<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isFreeformEnabled() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10055, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean addFreeformConfigChangedListener(IColorFreeformConfigChangedListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10056, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean removeFreeformConfigChangedListener(IColorFreeformConfigChangedListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10057, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean registerAppSwitchObserver(String pkgName, IColorAppSwitchObserver observer, ColorAppSwitchConfig config) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(pkgName);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            config.writeToParcel(data, 0);
            this.mRemote.transact(10064, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean unregisterAppSwitchObserver(String pkgName, ColorAppSwitchConfig config) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(pkgName);
            config.writeToParcel(data, 0);
            this.mRemote.transact(10065, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setChildSpaceMode(boolean mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(String.valueOf(mode));
            this.mRemote.transact(10066, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setAllowLaunchApps(List<String> allowLaunchApps) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStringList(allowLaunchApps);
            this.mRemote.transact(10067, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public int startZoomWindow(Intent intent, Bundle options, int userId, String callPkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            if (intent != null) {
                data.writeInt(1);
                intent.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            if (options != null) {
                data.writeInt(1);
                data.writeBundle(options);
            } else {
                data.writeInt(0);
            }
            data.writeInt(userId);
            data.writeString(callPkg);
            this.mRemote.transact(10068, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean registerZoomWindowObserver(IColorZoomWindowObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            this.mRemote.transact(10069, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean unregisterZoomWindowObserver(IColorZoomWindowObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            this.mRemote.transact(10070, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public ColorZoomWindowInfo getCurrentZoomWindowState() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10071, data, reply, 0);
            reply.readException();
            return ColorZoomWindowInfo.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setBubbleMode(boolean inBubbleMode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(String.valueOf(inBubbleMode));
            this.mRemote.transact(10072, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void hideZoomWindow(int flag) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeInt(flag);
            this.mRemote.transact(10073, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public List<String> getZoomAppConfigList(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10075, data, reply, 0);
            reply.readException();
            List<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isSupportZoomWindowMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10078, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public ColorZoomWindowRUSConfig getZoomWindowConfig() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ColorZoomWindowRUSConfig();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10076, data, reply, 0);
            reply.readException();
            return (ColorZoomWindowRUSConfig) reply.readParcelable(ColorZoomWindowRUSConfig.class.getClassLoader());
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setZoomWindowConfig(ColorZoomWindowRUSConfig config) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeParcelable(config, 0);
            this.mRemote.transact(10077, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean addZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10079, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean removeZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10080, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void startLockDeviceMode(String rootPkg, String[] packages) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(rootPkg);
            data.writeStringArray(packages);
            this.mRemote.transact(10081, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void stopLockDeviceMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(10082, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean writeEdgeTouchPreventParam(String callPkg, String scenePkg, List<String> paramCmdList) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(callPkg);
            data.writeString(scenePkg);
            data.writeStringList(paramCmdList);
            this.mRemote.transact(IColorActivityTaskManager.SET_EDGE_TOUCH_PREVENT_PARAM, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setDefaultEdgeTouchPreventParam(String callPkg, List<String> paramCmdList) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(callPkg);
            data.writeStringList(paramCmdList);
            this.mRemote.transact(IColorActivityTaskManager.SET_DEFAULT_EDGE_TOUCH_PREVENT_PARAM, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean resetDefaultEdgeTouchPreventParam(String callPkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(callPkg);
            this.mRemote.transact(IColorActivityTaskManager.RESET_DEFAULT_EDGE_TOUCH_PREVENT_PARAM, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public boolean isSupportEdgeTouchPrevent() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            this.mRemote.transact(IColorActivityTaskManager.IS_SUPPORT_EDGE_TOUCH_PREVENT, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public void setEdgeTouchCallRules(String callPkg, Map<String, List<String>> rulesMap) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            data.writeString(callPkg);
            data.writeMap(rulesMap);
            this.mRemote.transact(IColorActivityTaskManager.SET_EDGE_TOUCH_CALL_RULES, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityTaskManager
    public int splitScreenForEdgePanel(Intent intent, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityTaskManager.DESCRIPTOR);
            intent.writeToParcel(data, 0);
            data.writeInt(userId);
            this.mRemote.transact(IColorActivityTaskManager.SPLIT_SCREEN_FOR_FLOAT_ASSISTENT, data, reply, 0);
            reply.readException();
            return Integer.valueOf(reply.readString()).intValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}

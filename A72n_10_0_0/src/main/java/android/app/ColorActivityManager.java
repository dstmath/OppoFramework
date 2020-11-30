package android.app;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.app.IColorHansListener;
import com.color.favorite.IColorFavoriteQueryCallback;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorProcDependData;
import com.color.util.ColorReflectData;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import com.oppo.app.IOppoPermissionRecordController;
import java.util.ArrayList;
import java.util.List;

public class ColorActivityManager extends ColorBaseActivityManager implements IColorActivityManager {
    @Override // android.app.IColorActivityManager
    public void updatePermissionChoice(String packageName, String permission, int choice) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeString(permission);
            data.writeInt(choice);
            this.mRemote.transact(10003, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setPermissionInterceptEnable(boolean enabled) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(String.valueOf(enabled));
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public boolean isPermissionInterceptEnabled() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10005, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setSystemProperties(String properties, String value) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(properties);
            data.writeString(value);
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void killPidForce(int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(pid);
            this.mRemote.transact(10008, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void increaseRutilsUsedCount() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10009, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void decreaseRutilsUsedCount() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10010, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void grantOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeString(permission);
            this.mRemote.transact(10012, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void revokeOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeString(permission);
            this.mRemote.transact(10013, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void handleAppForNotification(String pkgName, int uid, int otherInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            data.writeInt(uid);
            data.writeInt(otherInfo);
            this.mRemote.transact(10014, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ColorAccidentallyTouchData getAccidentallyTouchData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10015, data, reply, 0);
            reply.readException();
            return ColorAccidentallyTouchData.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setGameSpaceController(IOppoGameSpaceController watcher) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
            this.mRemote.transact(10016, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAppAssociatedProcess(int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(pid);
            this.mRemote.transact(10017, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            for (int i = reply.readInt(); i > 0; i--) {
                list.add(reply.readString());
            }
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAppAssociatedProcess(String pkgName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            this.mRemote.transact(10018, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            for (int i = reply.readInt(); i > 0; i--) {
                list.add(reply.readString());
            }
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ArrayList<String> getGlobalPkgWhiteList(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10019, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ArrayList<String> getGlobalProcessWhiteList() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10020, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeString(fromPkg);
            data.writeLong(timeout);
            this.mRemote.transact(10021, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void removeStageProtectInfo(String pkg, String fromPkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeString(fromPkg);
            this.mRemote.transact(10022, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ColorDisplayOptimizationData getDisplayOptimizationData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10023, data, reply, 0);
            reply.readException();
            return ColorDisplayOptimizationData.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ColorSecureKeyboardData getSecureKeyboardData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10024, data, reply, 0);
            reply.readException();
            return ColorSecureKeyboardData.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(type);
            this.mRemote.transact(10025, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void handleAppFromControlCenter(String pkgName, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            data.writeInt(uid);
            this.mRemote.transact(10026, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ColorDisplayCompatData getDisplayCompatData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10027, data, reply, 0);
            reply.readException();
            return ColorDisplayCompatData.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAllowedMultiApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList<>();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10028, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getCreatedMultiApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList<>();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10029, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public String getAliasMultiApp(String pkgName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            this.mRemote.transact(10030, data, reply, 0);
            reply.readException();
            return reply.readString();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void addMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(shareAppPkgName);
            data.writeString(miniProgramPkgName);
            data.writeString(miniProgramSignature);
            this.mRemote.transact(10031, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void removeMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(shareAppPkgName);
            data.writeString(miniProgramPkgName);
            data.writeString(miniProgramSignature);
            this.mRemote.transact(10032, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void launchRutils() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10033, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ColorResolveData getResolveData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10034, data, reply, 0);
            reply.readException();
            return ColorResolveData.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public ColorReflectData getReflectData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10037, data, reply, 0);
            reply.readException();
            return ColorReflectData.CREATOR.createFromParcel(reply);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void addFastAppWechatPay(String originAppCpn, String fastAppCpn) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(originAppCpn);
            data.writeString(fastAppCpn);
            this.mRemote.transact(10038, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void removeFastAppWechatPay(String originAppCpn, String fastAppCpn) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(originAppCpn);
            data.writeString(fastAppCpn);
            this.mRemote.transact(10039, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<ColorPackageFreezeData> getRunningProcesses() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10041, data, reply, 0);
            reply.readException();
            return reply.createTypedArrayList(ColorPackageFreezeData.CREATOR);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setPreventStartController(IOppoAppStartController watcher) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
            this.mRemote.transact(10042, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setAppStartMonitorController(IOppoAppStartController watcher) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
            this.mRemote.transact(10043, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getAppAssociatedActivity(String pkgName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            this.mRemote.transact(10045, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            for (int i = reply.readInt(); i > 0; i--) {
                list.add(reply.readString());
            }
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void addFastAppThirdLogin(String callerPkg, String replacePkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(callerPkg);
            data.writeString(replacePkg);
            this.mRemote.transact(10046, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void removeFastAppThirdLogin(String callerPkg, String replacePkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(callerPkg);
            data.writeString(replacePkg);
            this.mRemote.transact(10047, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void favoriteQueryRule(String packageName, IColorFavoriteQueryCallback callback) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeStrongBinder(callback != null ? callback.asBinder() : null);
            this.mRemote.transact(10048, data, reply, 1);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void addBackgroundRestrictedInfo(String callerPkg, List<String> targetPkgList) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(callerPkg);
            data.writeStringList(targetPkgList);
            this.mRemote.transact(10049, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setPreventIndulgeController(IOppoAppStartController controller) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStrongBinder(controller != null ? controller.asBinder() : null);
            this.mRemote.transact(10050, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void addPreventIndulgeList(List<String> pkgNames) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStringList(pkgNames);
            this.mRemote.transact(10051, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean getIsSupportMultiApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10059, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public boolean putConfigInfo(String configName, Bundle bundle, int flag, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(configName);
            data.writeBundle(bundle);
            data.writeInt(flag);
            data.writeInt(userId);
            this.mRemote.transact(10062, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public Bundle getConfigInfo(String configName, int flag, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new Bundle();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(configName);
            data.writeInt(flag);
            data.writeInt(userId);
            this.mRemote.transact(10063, data, reply, 0);
            reply.readException();
            return reply.readBundle();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public float updateCpuTracker(long lastUpdateTime) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeLong(lastUpdateTime);
            this.mRemote.transact(10060, data, reply, 0);
            reply.readException();
            return Float.valueOf(reply.readFloat()).floatValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<String> getCpuWorkingStats() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10061, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void forceTrimAppMemory(int level) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(level);
            this.mRemote.transact(10067, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void setPermissionRecordController(IOppoPermissionRecordController watcher) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
            this.mRemote.transact(10066, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean isInDarkModeUnOpenAppList(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            this.mRemote.transact(10068, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public int getDarkModeData(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            this.mRemote.transact(10077, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public boolean dumpProcPerfData(Bundle bundle) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeBundle(bundle);
            this.mRemote.transact(10069, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getProcCommonInfoList(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> list = new ArrayList<>();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10070, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<ColorProcDependData> getProcDependency(int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(pid);
            this.mRemote.transact(10071, data, reply, 0);
            reply.readException();
            return reply.createTypedArrayList(ColorProcDependData.CREATOR);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<ColorProcDependData> getProcDependency(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeInt(userId);
            this.mRemote.transact(10072, data, reply, 0);
            reply.readException();
            return reply.createTypedArrayList(ColorProcDependData.CREATOR);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getTaskPkgList(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> list = new ArrayList<>();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(taskId);
            this.mRemote.transact(10073, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void syncPermissionRecord() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10074, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void updateUidCpuTracker() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10075, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<String> getUidCpuWorkingStats() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        new ArrayList();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            this.mRemote.transact(10076, data, reply, 0);
            reply.readException();
            ArrayList<String> list = new ArrayList<>();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public List<String> getProcCmdline(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> list = new ArrayList<>();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeInt(pids.length);
            data.writeIntArray(pids);
            this.mRemote.transact(10079, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void activeGc(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            if (pids != null) {
                data.writeInt(pids.length);
                data.writeIntArray(pids);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10080, data, reply, 1);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void finishNotOrderReceiver(IBinder who, int hasCode, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeStrongBinder(who);
            data.writeInt(hasCode);
            data.writeInt(resultCode);
            data.writeString(resultData);
            data.writeBundle(resultExtras);
            data.writeInt(resultAbort ? 1 : 0);
            this.mRemote.transact(10081, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void reportSkippedFrames(long currentTime, long skippedFrames) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeLong(currentTime);
            data.writeLong(skippedFrames);
            this.mRemote.transact(10082, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public void reportSkippedFrames(long currentTime, boolean isAnimation, boolean isForeground, long skippedFrames) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeLong(currentTime);
            int i = 1;
            data.writeInt(isAnimation ? 1 : 0);
            if (!isForeground) {
                i = 0;
            }
            data.writeInt(i);
            data.writeLong(skippedFrames);
            this.mRemote.transact(10085, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public boolean registerHansListener(String callerPkg, IColorHansListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(callerPkg);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10083, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public boolean unregisterHansListener(String callerPkg, IColorHansListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(callerPkg);
            data.writeStrongBinder(listener != null ? listener.asBinder() : null);
            this.mRemote.transact(10084, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorActivityManager
    public boolean setAppFreeze(String callerPkg, Bundle bundle) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseActivityManager.DESCRIPTOR);
            data.writeString(callerPkg);
            data.writeBundle(bundle);
            this.mRemote.transact(10078, data, reply, 0);
            reply.readException();
            return reply.readBoolean();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}

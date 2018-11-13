package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorFormatterCompatibilityData;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorReflectData;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.oppo.app.IOppoAppFreezeController;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import java.util.ArrayList;
import java.util.List;

public class OppoActivityManager implements IOppoActivityManager {
    private static final String TAG = "OppoActivityManager";
    private IBinder mRemote;

    public OppoActivityManager() {
        this.mRemote = null;
        this.mRemote = ServiceManager.getService(Context.ACTIVITY_SERVICE);
    }

    public void setSecureController(IActivityController watcher) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            if (watcher != null) {
                iBinder = watcher.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
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

    public void grantOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeString(permission);
            this.mRemote.transact(10012, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void revokeOppoPermissionByGroup(String packageName, String permission) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(packageName);
            data.writeString(permission);
            this.mRemote.transact(10013, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setPermissionInterceptEnable(boolean enabled) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(String.valueOf(enabled));
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean isPermissionInterceptEnabled() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean enabled = true;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10005, data, reply, 0);
            reply.readException();
            enabled = Boolean.valueOf(reply.readString()).booleanValue();
            return enabled;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setSystemProperties(String properties, String value) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(properties);
            data.writeString(value);
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ComponentName getTopActivityComponentName() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ComponentName name = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10007, data, reply, 0);
            reply.readException();
            name = ComponentName.readFromParcel(reply);
            return name;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ApplicationInfo getTopApplicationInfo() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ApplicationInfo info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10011, data, reply, 0);
            reply.readException();
            info = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void killPidForce(int pid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeInt(pid);
            this.mRemote.transact(10008, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void increaseRutilsUsedCount() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10009, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void decreaseRutilsUsedCount() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10010, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void handleAppForNotification(String pkgName, int uid, int otherInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
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

    public ColorAccidentallyTouchData getAccidentallyTouchData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorAccidentallyTouchData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10015, data, reply, 0);
            reply.readException();
            info = (ColorAccidentallyTouchData) ColorAccidentallyTouchData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ColorSecureKeyboardData getSecureKeyboardData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorSecureKeyboardData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10024, data, reply, 0);
            reply.readException();
            info = (ColorSecureKeyboardData) ColorSecureKeyboardData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ColorResolveData getResolveData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorResolveData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10034, data, reply, 0);
            reply.readException();
            info = (ColorResolveData) ColorResolveData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ColorDisplayOptimizationData getDisplayOptimizationData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorDisplayOptimizationData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10023, data, reply, 0);
            reply.readException();
            info = (ColorDisplayOptimizationData) ColorDisplayOptimizationData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ColorDisplayCompatData getDisplayCompatData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorDisplayCompatData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10027, data, reply, 0);
            reply.readException();
            info = (ColorDisplayCompatData) ColorDisplayCompatData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setGameSpaceController(IOppoGameSpaceController watcher) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            if (watcher != null) {
                iBinder = watcher.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(10016, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<String> getAppAssociatedProcess(int pid) throws RemoteException {
        Throwable th;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeInt(pid);
            this.mRemote.transact(10017, data, reply, 0);
            reply.readException();
            ArrayList<String> list2 = new ArrayList();
            try {
                for (int i = reply.readInt(); i > 0; i--) {
                    list2.add(reply.readString());
                }
                data.recycle();
                reply.recycle();
                return list2;
            } catch (Throwable th2) {
                th = th2;
                list = list2;
                data.recycle();
                reply.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            data.recycle();
            reply.recycle();
            throw th;
        }
    }

    public List<String> getAppAssociatedProcess(String pkgName) throws RemoteException {
        Throwable th;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            this.mRemote.transact(10018, data, reply, 0);
            reply.readException();
            ArrayList<String> list2 = new ArrayList();
            try {
                for (int i = reply.readInt(); i > 0; i--) {
                    list2.add(reply.readString());
                }
                data.recycle();
                reply.recycle();
                return list2;
            } catch (Throwable th2) {
                th = th2;
                list = list2;
                data.recycle();
                reply.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            data.recycle();
            reply.recycle();
            throw th;
        }
    }

    public ArrayList<String> getGlobalPkgWhiteList(int type) throws RemoteException {
        Throwable th;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(10019, data, reply, 0);
            reply.readException();
            ArrayList<String> list2 = new ArrayList();
            try {
                reply.readStringList(list2);
                data.recycle();
                reply.recycle();
                return list2;
            } catch (Throwable th2) {
                th = th2;
                list = list2;
                data.recycle();
                reply.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            data.recycle();
            reply.recycle();
            throw th;
        }
    }

    public ArrayList<String> getGlobalProcessWhiteList() throws RemoteException {
        Throwable th;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10020, data, reply, 0);
            reply.readException();
            ArrayList<String> list2 = new ArrayList();
            try {
                reply.readStringList(list2);
                data.recycle();
                reply.recycle();
                return list2;
            } catch (Throwable th2) {
                th = th2;
                list = list2;
                data.recycle();
                reply.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            data.recycle();
            reply.recycle();
            throw th;
        }
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
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

    public void removeStageProtectInfo(String pkg, String fromPkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeString(fromPkg);
            this.mRemote.transact(10022, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) throws RemoteException {
        Throwable th;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(type);
            this.mRemote.transact(10025, data, reply, 0);
            reply.readException();
            ArrayList<String> list2 = new ArrayList();
            try {
                reply.readStringList(list2);
                data.recycle();
                reply.recycle();
                return list2;
            } catch (Throwable th2) {
                th = th2;
                list = list2;
                data.recycle();
                reply.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            data.recycle();
            reply.recycle();
            throw th;
        }
    }

    public void handleAppFromControlCenter(String pkgName, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            data.writeInt(uid);
            this.mRemote.transact(10026, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<String> getAllowedMultiApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10028, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<String> getCreatedMultiApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10029, data, reply, 0);
            reply.readException();
            reply.readStringList(list);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public String getAliasByPackage(String pkgName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String alias = "";
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            this.mRemote.transact(10030, data, reply, 0);
            reply.readException();
            alias = reply.readString();
            return alias;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void addMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
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

    public void removeMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
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

    public void launchRutils() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10033, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean isAppCallRefuseMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean enable = true;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10035, data, reply, 0);
            reply.readException();
            enable = Boolean.valueOf(reply.readString()).booleanValue();
            return enable;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setAppCallRefuseMode(boolean enable) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(String.valueOf(enable));
            this.mRemote.transact(10036, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ColorReflectData getReflectData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorReflectData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10037, data, reply, 0);
            reply.readException();
            info = (ColorReflectData) ColorReflectData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public ColorFormatterCompatibilityData getFormatterCompatData() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorFormatterCompatibilityData info = null;
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10040, data, reply, 0);
            reply.readException();
            info = (ColorFormatterCompatibilityData) ColorFormatterCompatibilityData.CREATOR.createFromParcel(reply);
            return info;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void addFastAppWechatPay(String originAppCpn, String fastAppCpn) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(originAppCpn);
            data.writeString(fastAppCpn);
            this.mRemote.transact(10038, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void removeFastAppWechatPay(String originAppCpn, String fastAppCpn) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(originAppCpn);
            data.writeString(fastAppCpn);
            this.mRemote.transact(10039, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<ColorPackageFreezeData> getRunningProcesses() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<ColorPackageFreezeData> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            this.mRemote.transact(10041, data, reply, 0);
            reply.readException();
            list = reply.createTypedArrayList(ColorPackageFreezeData.CREATOR);
            return list;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setPreventStartController(IOppoAppStartController watcher) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            if (watcher != null) {
                iBinder = watcher.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(10042, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setAppStartMonitorController(IOppoAppStartController watcher) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            if (watcher != null) {
                iBinder = watcher.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(10043, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public void setAppFreezeController(IOppoAppFreezeController watcher) throws RemoteException {
        IBinder iBinder = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            if (watcher != null) {
                iBinder = watcher.asBinder();
            }
            data.writeStrongBinder(iBinder);
            this.mRemote.transact(10044, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public List<String> getAppAssociatedActivity(String pkgName) throws RemoteException {
        Throwable th;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ArrayList<String> list = new ArrayList();
        try {
            data.writeInterfaceToken(IOppoActivityManager.DESCRIPTOR);
            data.writeString(pkgName);
            this.mRemote.transact(10045, data, reply, 0);
            reply.readException();
            ArrayList<String> list2 = new ArrayList();
            try {
                for (int i = reply.readInt(); i > 0; i--) {
                    list2.add(reply.readString());
                }
                data.recycle();
                reply.recycle();
                return list2;
            } catch (Throwable th2) {
                th = th2;
                list = list2;
                data.recycle();
                reply.recycle();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            data.recycle();
            reply.recycle();
            throw th;
        }
    }
}

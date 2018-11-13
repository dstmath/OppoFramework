package com.android.server.coloros;

import android.app.IAthenaLMKCallback;
import android.app.IAthenaLMKCallback.Stub;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoAthenaLowMemoryKiller;
import java.util.ArrayList;
import java.util.List;

public class OppoKillerManagerService extends Binder implements IOppoKillerManager {
    private static final String TAG = "OppoKillerManagerService";
    private static OppoKillerManagerService sInstance;
    private final ActivityManagerService mActivityManager;

    public abstract class OKillerContext {
        public static final String OKILLER_SERVICE = "athenaservice";
    }

    public static class SystemServer {
        public static void addService(ActivityManagerService ams) {
            ServiceManager.addService(OKillerContext.OKILLER_SERVICE, OppoKillerManagerService.getInstance(ams));
        }
    }

    private OppoKillerManagerService(ActivityManagerService service) {
        this.mActivityManager = service;
    }

    public static OppoKillerManagerService getInstance(ActivityManagerService service) {
        if (sInstance == null) {
            sInstance = new OppoKillerManagerService(service);
        }
        return sInstance;
    }

    public IBinder asBinder() {
        return this;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int result;
        switch (code) {
            case 101:
                data.enforceInterface(IOppoKillerManager.DESCRIPTOR);
                result = oppoKill(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case 102:
                data.enforceInterface(IOppoKillerManager.DESCRIPTOR);
                result = oppoFreeze(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case 103:
                data.enforceInterface(IOppoKillerManager.DESCRIPTOR);
                int thresholdMemSize = data.readInt();
                int thresholdOomAdj = data.readInt();
                List<String> whiteList = new ArrayList();
                List<String> killList = new ArrayList();
                data.readStringList(whiteList);
                data.readStringList(killList);
                result = athenaLowmemoryScan(thresholdMemSize, thresholdOomAdj, whiteList, killList);
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case 104:
                data.enforceInterface(IOppoKillerManager.DESCRIPTOR);
                result = registerAthenaLMKCallback(Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public int oppoKill(int uid, int pid, String packageName, int level, int flag) {
        if (level == 2) {
            forceStopPackage(packageName, UserHandle.getUserId(uid));
        } else if (level == 1) {
            killPidForce(pid, packageName);
        }
        return 0;
    }

    public int oppoFreeze(int uid, int pid, String packageName, int level, int flag) {
        return 0;
    }

    public int athenaLowmemoryScan(int thresholdMemSize, int thresholdOomAdj, List<String> whiteList, List<String> killList) {
        OppoAthenaLowMemoryKiller.getInstance(this.mActivityManager).doLowMemoryScan(thresholdMemSize, thresholdOomAdj, whiteList, killList);
        return 0;
    }

    public int registerAthenaLMKCallback(IAthenaLMKCallback callback, IBinder token) {
        OppoAthenaLowMemoryKiller.getInstance(this.mActivityManager).registerCallback(callback, token);
        return 0;
    }

    private void forceStopPackage(String packagename, int userId) {
        this.mActivityManager.forceStopPackage(packagename, userId);
    }

    private void killPidForce(int pid, String packageNameOrProcessName) {
        Process.killProcess(pid);
    }
}

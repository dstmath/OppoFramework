package com.android.server.am;

import android.app.IActivityController;
import android.app.IActivityController.Stub;
import android.app.IOppoActivityManager;
import android.app.IOppoActivityManager.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.coloros.OppoBrowserInterceptManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.oppo.IElsaManager;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorAccidentallyTouchUtils;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayCompatUtils;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorDisplayOptimizationUtils;
import com.color.util.ColorSecureKeyboardData;
import com.color.util.ColorSecureKeyboardUtils;
import com.oppo.app.IOppoGameSpaceController;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoActivityManagerService extends ActivityManagerService implements IOppoActivityManager, Service {
    private static String RUTILS_PROPERTIES = null;
    private static String RUTILS_USED_COUNT = null;
    private static final String TAG_OPPO = "OppoActivityManagerService";
    private final Object mLock;
    OppoPermissionInterceptPolicy mPermissionInterceptPolicy;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoActivityManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoActivityManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoActivityManagerService.<clinit>():void");
    }

    public OppoActivityManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mPermissionInterceptPolicy = null;
    }

    public void permissionInterceptPolicyReady() {
        this.mPermissionInterceptPolicy = OppoPermissionInterceptPolicy.getInstance(this);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean enabled;
        List<String> list;
        int N;
        int i;
        ArrayList<String> list2;
        switch (code) {
            case 10002:
                data.enforceInterface("android.app.IActivityManager");
                setSecureController(Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10003:
                data.enforceInterface("android.app.IActivityManager");
                updatePermissionChoice(data.readString(), data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            case 10004:
                data.enforceInterface("android.app.IActivityManager");
                setPermissionInterceptEnable(Boolean.valueOf(data.readString()).booleanValue());
                reply.writeNoException();
                return true;
            case 10005:
                data.enforceInterface("android.app.IActivityManager");
                enabled = isPermissionInterceptEnabled();
                reply.writeNoException();
                reply.writeString(String.valueOf(enabled));
                return true;
            case 10006:
                data.enforceInterface("android.app.IActivityManager");
                setSystemProperties(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10007:
                data.enforceInterface("android.app.IActivityManager");
                ComponentName name = getTopActivityComponentName();
                reply.writeNoException();
                ComponentName.writeToParcel(name, reply);
                return true;
            case 10008:
                data.enforceInterface("android.app.IActivityManager");
                killPidForce(data.readInt());
                reply.writeNoException();
                return true;
            case 10009:
                data.enforceInterface("android.app.IActivityManager");
                increaseRutilsUsedCount();
                reply.writeNoException();
                return true;
            case 10010:
                data.enforceInterface("android.app.IActivityManager");
                decreaseRutilsUsedCount();
                reply.writeNoException();
                return true;
            case 10011:
                data.enforceInterface("android.app.IActivityManager");
                enabled = isInBrowserInterceptWhiteList(data.readString());
                reply.writeNoException();
                reply.writeString(String.valueOf(enabled));
                return true;
            case 10012:
                data.enforceInterface("android.app.IActivityManager");
                ApplicationInfo info = getTopApplicationInfo();
                reply.writeNoException();
                info.writeToParcel(reply, 0);
                return true;
            case 10013:
                data.enforceInterface("android.app.IActivityManager");
                grantOppoPermissionByGroup(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10014:
                data.enforceInterface("android.app.IActivityManager");
                revokeOppoPermissionByGroup(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10015:
                data.enforceInterface("android.app.IActivityManager");
                handleAppForNotification(data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 10016:
                data.enforceInterface("android.app.IActivityManager");
                ColorAccidentallyTouchData info2 = getAccidentallyTouchData();
                reply.writeNoException();
                info2.writeToParcel(reply, 0);
                return true;
            case 10017:
                data.enforceInterface("android.app.IActivityManager");
                setGameSpaceController(IOppoGameSpaceController.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 10018:
                data.enforceInterface("android.app.IActivityManager");
                list = getAppAssociatedProcess(data.readInt());
                reply.writeNoException();
                N = list != null ? list.size() : -1;
                reply.writeInt(N);
                for (i = 0; i < N; i++) {
                    reply.writeString((String) list.get(i));
                }
                return true;
            case 10019:
                data.enforceInterface("android.app.IActivityManager");
                list = getAppAssociatedProcess(data.readString());
                reply.writeNoException();
                N = list != null ? list.size() : -1;
                reply.writeInt(N);
                for (i = 0; i < N; i++) {
                    reply.writeString((String) list.get(i));
                }
                return true;
            case 10020:
                data.enforceInterface("android.app.IActivityManager");
                list2 = getGlobalPkgWhiteList(data.readInt());
                reply.writeNoException();
                reply.writeStringList(list2);
                return true;
            case 10021:
                data.enforceInterface("android.app.IActivityManager");
                list2 = getGlobalProcessWhiteList();
                reply.writeNoException();
                reply.writeStringList(list2);
                return true;
            case 10022:
                data.enforceInterface("android.app.IActivityManager");
                addStageProtectInfo(data.readString(), data.readString(), data.readLong());
                reply.writeNoException();
                return true;
            case 10023:
                data.enforceInterface("android.app.IActivityManager");
                removeStageProtectInfo(data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10024:
                data.enforceInterface("android.app.IActivityManager");
                ColorDisplayOptimizationData info3 = getDisplayOptimizationData();
                reply.writeNoException();
                info3.writeToParcel(reply, 0);
                return true;
            case 10025:
                data.enforceInterface("android.app.IActivityManager");
                ColorSecureKeyboardData info4 = getSecureKeyboardData();
                reply.writeNoException();
                info4.writeToParcel(reply, 0);
                return true;
            case 10026:
                data.enforceInterface("android.app.IActivityManager");
                list2 = getStageProtectListFromPkg(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeStringList(list2);
                return true;
            case 10027:
                data.enforceInterface("android.app.IActivityManager");
                handleAppFromControlCenter(data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            case 10028:
                data.enforceInterface("android.app.IActivityManager");
                ColorDisplayCompatData info5 = getDisplayCompatData();
                reply.writeNoException();
                info5.writeToParcel(reply, 0);
                return true;
            case 10029:
                data.enforceInterface("android.app.IActivityManager");
                addMiniProgramShare(data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10030:
                data.enforceInterface("android.app.IActivityManager");
                removeMiniProgramShare(data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            case 10031:
                data.enforceInterface("android.app.IActivityManager");
                launchRutils();
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public void setSecureController(IActivityController controller) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        synchronized (this) {
            OppoSecureProtectUtils.setSecureControllerLocked(this, controller);
        }
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) {
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "updatePermissionChoice");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "updatePermissionChoice");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.updatePermissionChoice(packageName, permission, choice);
        }
    }

    public void grantOppoPermissionByGroup(String packageName, String permission) {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantOppoPermissionByGroup");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "grantOppoPermissionByGroup");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.grantOppoPermissionsFromRuntime(packageName, permission);
        }
    }

    public void revokeOppoPermissionByGroup(String packageName, String permission) {
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeOppoPermissionByGroup");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "revokeOppoPermissionByGroup");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.revokeOppoPermissionsFromRuntime(packageName, permission);
        }
    }

    public void setPermissionInterceptEnable(boolean enabled) {
        try {
            this.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setPermissionInterceptEnable");
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "setPermissionInterceptEnable");
        }
        if (this.mPermissionInterceptPolicy != null) {
            this.mPermissionInterceptPolicy.setPermissionInterceptEnable(enabled);
        }
    }

    public boolean isPermissionInterceptEnabled() {
        if (this.mPermissionInterceptPolicy != null) {
            return this.mPermissionInterceptPolicy.isPermissionInterceptEnabled();
        }
        return false;
    }

    public void setSystemProperties(String properties, String value) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setSystemProperties");
        SystemProperties.set(properties, value);
    }

    public ComponentName getTopActivityComponentName() {
        try {
            ComponentName name;
            ActivityRecord top = this.mStackSupervisor.topRunningActivityLocked();
            if (top != null) {
                name = new ComponentName(top.info.packageName, top.info.name);
            } else {
                name = new ComponentName(IElsaManager.EMPTY_PACKAGE, IElsaManager.EMPTY_PACKAGE);
            }
            return name;
        } catch (Exception e) {
            return new ComponentName(IElsaManager.EMPTY_PACKAGE, IElsaManager.EMPTY_PACKAGE);
        }
    }

    public ApplicationInfo getTopApplicationInfo() {
        try {
            ApplicationInfo info;
            ActivityRecord top = this.mStackSupervisor.topRunningActivityLocked();
            if (top == null || top.appInfo == null) {
                info = new ApplicationInfo();
            } else {
                info = top.appInfo;
            }
            return info;
        } catch (Exception e) {
            return new ApplicationInfo();
        }
    }

    public void killPidForce(int pid) {
        enforceCallingPermission("android.permission.FORCE_STOP_PACKAGES", "killPidForce");
        Process.sendSignal(pid, 9);
    }

    public void increaseRutilsUsedCount() {
        synchronized (this.mLock) {
            SystemProperties.set(RUTILS_USED_COUNT, Integer.toString(SystemProperties.getInt(RUTILS_USED_COUNT, 0) + 1));
        }
    }

    public void decreaseRutilsUsedCount() {
        synchronized (this.mLock) {
            int count = SystemProperties.getInt(RUTILS_USED_COUNT, 0);
            if (count > 0) {
                count--;
            }
            SystemProperties.set(RUTILS_USED_COUNT, Integer.toString(count));
        }
    }

    public int checkPermissionForProc(String permission, int pid, int uid, int token, Object callback) {
        return this.mPermissionInterceptPolicy.checkPermissionForProc(permission, pid, uid, token, (OppoPermissionCallback) callback);
    }

    public boolean isInBrowserInterceptWhiteList(String pkgName) {
        return OppoBrowserInterceptManager.getInstance().isInWhiteList(pkgName);
    }

    public void handleAppForNotification(String pkgName, int uid, int otherInfo) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "handleAppForNotification");
        OppoListManager.getInstance().handleAppForNotification(pkgName, uid, otherInfo);
        OppoProcessManagerHelper.resumeProcessByUID(uid, OppoProcessManager.RESUME_REASON_NOTIFY_STR);
    }

    public ColorAccidentallyTouchData getAccidentallyTouchData() {
        return ColorAccidentallyTouchUtils.getInstance().getTouchData();
    }

    public ColorSecureKeyboardData getSecureKeyboardData() {
        return ColorSecureKeyboardUtils.getInstance().getData();
    }

    public ColorDisplayOptimizationData getDisplayOptimizationData() {
        return ColorDisplayOptimizationUtils.getInstance().getOptimizationData();
    }

    public ColorDisplayCompatData getDisplayCompatData() {
        return ColorDisplayCompatUtils.getInstance().getDisplayCompatData();
    }

    public void setGameSpaceController(IOppoGameSpaceController controller) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "setGameSpaceController");
        OppoGameSpaceManager.getInstance().setGameSpaceController(controller);
    }

    public List<String> getAppAssociatedProcess(String pkgName) {
        if (pkgName == null) {
            return null;
        }
        ProcessRecord proc = null;
        boolean found = false;
        synchronized (this) {
            for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                proc = (ProcessRecord) this.mLruProcesses.get(i);
                if (pkgName.equals(proc.processName)) {
                    found = true;
                    break;
                }
            }
        }
        List<String> list = null;
        if (found && proc != null) {
            list = getAppAssociatedProcess(proc);
        }
        return list;
    }

    public List<String> getAppAssociatedProcess(int pid) {
        ProcessRecord proc = null;
        boolean found = false;
        synchronized (this) {
            for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                proc = (ProcessRecord) this.mLruProcesses.get(i);
                if (proc.pid == pid) {
                    found = true;
                    break;
                }
            }
        }
        if (!found || proc == null) {
            return null;
        }
        return getAppAssociatedProcess(proc);
    }

    private List<String> getAppAssociatedProcess(ProcessRecord app) {
        List<String> associatedAppList = new ArrayList();
        if (app == null) {
            return associatedAppList;
        }
        try {
            ProcessRecord proc;
            if (app.connections.size() > 0) {
                for (ConnectionRecord cr : app.connections) {
                    proc = cr.binding.service.app;
                    if (!(proc == null || proc.processName == null)) {
                        associatedAppList.add(proc.processName);
                    }
                }
            }
            if (app.conProviders.size() > 0) {
                for (ContentProviderConnection cc : app.conProviders) {
                    proc = cc.provider.proc;
                    if (!(proc == null || proc.processName == null)) {
                        associatedAppList.add(proc.processName);
                    }
                }
            }
            if (app.adjSource instanceof ProcessRecord) {
                proc = (ProcessRecord) app.adjSource;
                if (!(proc == null || proc.processName == null)) {
                    associatedAppList.add(proc.processName);
                }
            }
            return associatedAppList;
        } catch (Exception e) {
            while (true) {
                Slog.w(TAG_OPPO, "getAppAssociatedProcess    " + e);
                return associatedAppList;
            }
        } catch (Throwable th) {
            return associatedAppList;
        }
    }

    public ArrayList<String> getGlobalPkgWhiteList(int type) {
        return OppoListManager.getInstance().getGlobalWhiteList(this.mContext, type);
    }

    public ArrayList<String> getGlobalProcessWhiteList() {
        return OppoListManager.getInstance().getGlobalProcessWhiteList(this.mContext);
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "addStageProtectInfo");
        OppoListManager.getInstance().addStageProtectInfo(pkg, fromPkg, timeout);
    }

    public void removeStageProtectInfo(String pkg, String fromPkg) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "removeStageProtectInfo");
        OppoListManager.getInstance().removeStageProtectInfo(pkg, fromPkg);
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "getStageProtectListFromPkg");
        return OppoListManager.getInstance().getStageProtectListFromPkg(pkg, type);
    }

    public void handleAppFromControlCenter(String pkgName, int uid) {
        enforceCallingPermission("oppo.permission.OPPO_COMPONENT_SAFE", "handleAppFromControlCenter");
        OppoListManager.getInstance().handleAppFromControlCenter(pkgName, uid);
    }

    public void addMiniProgramShare(String srcPkgName, String dstPkgName, String signature) {
        OppoListManager.getInstance().addMiniProgramShare(srcPkgName, dstPkgName, signature);
    }

    public void removeMiniProgramShare(String srcPkgName, String dstPkgName, String signature) {
        OppoListManager.getInstance().removeMiniProgramShare(srcPkgName, dstPkgName, signature);
    }

    public void launchRutils() {
        SystemProperties.set(RUTILS_PROPERTIES, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }
}

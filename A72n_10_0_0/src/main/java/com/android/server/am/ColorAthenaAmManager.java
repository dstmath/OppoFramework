package com.android.server.am;

import android.common.OppoFeatureCache;
import android.os.Binder;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.IColorAthenaManager;
import com.color.util.ColorProcDependData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorAthenaAmManager implements IColorAthenaAmManager {
    public static final String TAG = "ColorAthenaAm";
    private static boolean sDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebug | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    boolean mDynamicDebug = false;

    private static class ColorAthenaAmManagerInstance {
        private static final ColorAthenaAmManager sInstance = new ColorAthenaAmManager();

        private ColorAthenaAmManagerInstance() {
        }
    }

    public static ColorAthenaAmManager getInstance() {
        return ColorAthenaAmManagerInstance.sInstance;
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mAms = amsEx.getActivityManagerService();
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebug | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "DEBUG_SWITCH " + this.DEBUG_SWITCH);
    }

    public List<ColorProcDependData> getProcDependency(int pid) {
        List<ColorProcDependData> dependOnThisInnerLocked;
        try {
            synchronized (this.mAms.mPidsSelfLocked) {
                dependOnThisInnerLocked = getDependOnThisInnerLocked(getProcessRecordLocked(pid));
            }
            return dependOnThisInnerLocked;
        } catch (Exception e) {
            Slog.d(TAG, "get process dependency error= " + e);
            return null;
        }
    }

    public List<ColorProcDependData> getProcDependency(String pkgName, int userId) {
        List<ColorProcDependData> dependOnThisInnerLocked;
        try {
            synchronized (this.mAms) {
                dependOnThisInnerLocked = getDependOnThisInnerLocked(getProcessRecordLocked(pkgName, userId));
            }
            return dependOnThisInnerLocked;
        } catch (Exception e) {
            Slog.d(TAG, "get process dependency error= " + e);
            return null;
        }
    }

    private List<ProcessRecord> getProcessRecordLocked(int pid) {
        List<ProcessRecord> resultList = new ArrayList<>();
        ProcessRecord proc = this.mAms.mPidsSelfLocked.get(pid);
        if (proc != null) {
            resultList.add(proc);
        }
        return resultList;
    }

    private List<ProcessRecord> getProcessRecordLocked(String pkgName, int userId) {
        List<ProcessRecord> resultList = new ArrayList<>();
        for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord proc = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
            if (userId == proc.userId && pkgName.equals(proc.info.packageName)) {
                resultList.add(proc);
            }
        }
        return resultList;
    }

    private List<ColorProcDependData> getDependOnThisInnerLocked(List<ProcessRecord> procList) {
        if (procList == null) {
            return null;
        }
        List<ColorProcDependData> resultList = new ArrayList<>();
        for (ProcessRecord app : procList) {
            String pkgName = app.info.packageName;
            ColorProcDependData data = new ColorProcDependData();
            data.mUid = app.uid;
            data.mPid = app.pid;
            data.mProcessName = app.processName;
            data.mPackageName = pkgName;
            getBindServiceDependency(app, data);
            getContentProviderDependency(app, data);
            if (!data.mServices.isEmpty() || !data.mClients.isEmpty()) {
                resultList.add(data);
            }
        }
        return resultList;
    }

    private void getBindServiceDependency(ProcessRecord app, ColorProcDependData data) {
        ServiceRecord sr;
        String pkgName;
        ColorProcDependData colorProcDependData = data;
        if (app != null && colorProcDependData != null) {
            String pkgName2 = app.info.packageName;
            if (app.connections.size() > 0) {
                Iterator it = app.connections.iterator();
                while (it.hasNext()) {
                    ConnectionRecord cr = (ConnectionRecord) it.next();
                    if (!(cr.binding.service.app == null || cr.binding.service.appInfo == null)) {
                        String sPkg = cr.binding.service.packageName;
                        String sProc = cr.binding.service.processName;
                        int sUid = cr.binding.service.appInfo.uid;
                        int sPid = cr.binding.service.app.pid;
                        if (sPkg != null && !sPkg.equals(pkgName2)) {
                            ColorProcDependData.ProcItem item = new ColorProcDependData.ProcItem(sUid, sPid, sPkg, sProc);
                            if (!colorProcDependData.mServices.contains(item)) {
                                colorProcDependData.mServices.add(item);
                                if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                    Slog.d(TAG, app + " [BSS] uid=" + sUid + ", pid=" + sPid + ", pkg=" + sPkg + ", proc=" + sProc);
                                }
                            } else if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                Slog.d(TAG, app + " [BSS] SKIP uid=" + sUid + ", pid=" + sPid + ", pkg=" + sPkg + ", proc=" + sProc);
                            }
                        }
                    }
                }
            }
            if (app.services.size() > 0) {
                Iterator it2 = app.services.iterator();
                while (it2.hasNext()) {
                    ServiceRecord sr2 = (ServiceRecord) it2.next();
                    if (sr2.getConnections() != null) {
                        int conni = sr2.getConnections().size() - 1;
                        while (conni >= 0) {
                            ArrayList<ConnectionRecord> list = (ArrayList) sr2.getConnections().valueAt(conni);
                            int i = 0;
                            while (i < list.size()) {
                                ConnectionRecord cr2 = list.get(i);
                                String cPkg = cr2.binding.client.info.packageName;
                                String cProc = cr2.binding.client.processName;
                                int cUid = cr2.clientUid;
                                int cPid = cr2.binding.client.pid;
                                if (cPkg == null) {
                                    pkgName = pkgName2;
                                    sr = sr2;
                                } else if (cPkg.equals(pkgName2)) {
                                    pkgName = pkgName2;
                                    sr = sr2;
                                } else {
                                    pkgName = pkgName2;
                                    ColorProcDependData.ProcItem item2 = new ColorProcDependData.ProcItem(cUid, cPid, cPkg, cProc);
                                    sr = sr2;
                                    if (!colorProcDependData.mClients.contains(item2)) {
                                        colorProcDependData.mClients.add(item2);
                                        if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                            Slog.d(TAG, app + " [BSC] uid=" + cUid + ", pid=" + cPid + ", pkg=" + cPkg + ", proc=" + cProc);
                                        }
                                    } else if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                        Slog.d(TAG, app + " [BSC] SKIP uid=" + cUid + ", pid=" + cPid + ", pkg=" + cPkg + ", proc=" + cProc);
                                    }
                                }
                                i++;
                                colorProcDependData = data;
                                it2 = it2;
                                pkgName2 = pkgName;
                                sr2 = sr;
                            }
                            conni--;
                            colorProcDependData = data;
                        }
                    }
                    colorProcDependData = data;
                    it2 = it2;
                    pkgName2 = pkgName2;
                }
            }
        }
    }

    private void getContentProviderDependency(ProcessRecord app, ColorProcDependData data) {
        String pkgName;
        Iterator it;
        ColorProcDependData colorProcDependData = data;
        if (app != null && colorProcDependData != null) {
            String pkgName2 = app.info.packageName;
            if (app.conProviders.size() > 0) {
                Iterator it2 = app.conProviders.iterator();
                while (it2.hasNext()) {
                    ContentProviderRecord cpr = ((ContentProviderConnection) it2.next()).provider;
                    if (cpr.proc != null) {
                        String sPkg = cpr.proc.info.packageName;
                        String sProc = cpr.proc.processName;
                        int sUid = cpr.proc.uid;
                        int sPid = cpr.proc.pid;
                        if (sPkg != null) {
                            if (!sPkg.equals(pkgName2)) {
                                ColorProcDependData.ProcItem item = new ColorProcDependData.ProcItem(sUid, sPid, sPkg, sProc);
                                if (!colorProcDependData.mServices.contains(item)) {
                                    colorProcDependData.mServices.add(item);
                                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(app);
                                        it = it2;
                                        sb.append(" [CPS] uid=");
                                        sb.append(sUid);
                                        sb.append(", pid=");
                                        sb.append(sPid);
                                        sb.append(", pkg=");
                                        sb.append(sPkg);
                                        sb.append(", proc=");
                                        sb.append(sProc);
                                        Slog.d(TAG, sb.toString());
                                    } else {
                                        it = it2;
                                    }
                                } else {
                                    it = it2;
                                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                        Slog.d(TAG, app + " [CPS] SKIP uid=" + sUid + ", pid=" + sPid + ", pkg=" + sPkg + ", proc=" + sProc);
                                    }
                                }
                                it2 = it;
                            }
                        }
                    }
                }
            }
            if (app.pubProviders.size() > 0) {
                int conni = app.pubProviders.size() - 1;
                while (conni >= 0) {
                    ContentProviderRecord cr = (ContentProviderRecord) app.pubProviders.valueAt(conni);
                    int i = 0;
                    while (i < cr.connections.size()) {
                        ContentProviderConnection cpr2 = (ContentProviderConnection) cr.connections.get(i);
                        if (cpr2.client == null) {
                            pkgName = pkgName2;
                        } else {
                            String cPkg = cpr2.client.info.packageName;
                            String cProc = cpr2.client.processName;
                            int cUid = cpr2.client.uid;
                            int cPid = cpr2.client.pid;
                            if (cPkg == null) {
                                pkgName = pkgName2;
                            } else if (cPkg.equals(pkgName2)) {
                                pkgName = pkgName2;
                            } else {
                                ColorProcDependData.ProcItem item2 = new ColorProcDependData.ProcItem(cUid, cPid, cPkg, cProc);
                                pkgName = pkgName2;
                                if (!colorProcDependData.mClients.contains(item2)) {
                                    colorProcDependData.mClients.add(item2);
                                    if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                        Slog.d(TAG, app + " [CPC] uid=" + cUid + ", pid=" + cPid + ", pkg=" + cPkg + ", proc=" + cProc);
                                    }
                                } else if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                                    Slog.d(TAG, app + " [CPC] SKIP uid=" + cUid + ", pid=" + cPid + ", pkg=" + cPkg + ", proc=" + cProc);
                                }
                            }
                        }
                        i++;
                        colorProcDependData = data;
                        pkgName2 = pkgName;
                    }
                    conni--;
                    colorProcDependData = data;
                }
            }
        }
    }

    public void forceTrimAppMemory(int level) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "forceTrimAppMemory uid : " + Binder.getCallingUid() + " pid : " + Binder.getCallingPid() + ", level: " + level);
        }
        List<String> protectList = OppoFeatureCache.get(IColorAthenaManager.DEFAULT).getProtectList();
        if (protectList != null) {
            synchronized (this.mAms) {
                for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord app = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                    if (app != null) {
                        String pkgName = app.info.packageName;
                        if (protectList.contains(pkgName)) {
                            if (this.DEBUG_SWITCH) {
                                Slog.d(TAG, "force trim memory skip protect name=" + pkgName);
                            }
                        } else if (app.thread != null) {
                            try {
                                app.thread.scheduleTrimMemory(level);
                            } catch (RemoteException e) {
                                Slog.d(TAG, "force trim memory get error: " + e);
                            }
                        }
                    }
                }
            }
            Slog.d(TAG, "force trim memory done, level: " + level);
        }
    }

    private void callProcGc(int pid) {
        if (pid > 0) {
            try {
                Process.sendSignalQuiet(pid, 10);
            } catch (Exception e) {
                Slog.d(TAG, "active gc get error: " + e);
            }
        }
    }

    public void activeGc(int[] pids) {
        if (pids != null) {
            for (int pid : pids) {
                callProcGc(pid);
            }
            return;
        }
        synchronized (this.mAms) {
            for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord app = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                if (app != null) {
                    callProcGc(app.pid);
                }
            }
        }
    }
}

package com.android.server.am;

import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.os.ProcStatsUtil;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.ServiceThread;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ColorPerfManager implements IColorPerfManager {
    private static final int DUMP_HPROF = 2;
    private static final int DUMP_SMAPS = 1;
    public static final String TAG = "ColorPerf";
    private static final int TYPE_GET_ALL_PROC = 1;
    private static boolean sDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH;
    private ActivityManagerService mAms;
    boolean mDynamicDebug;
    private final ProcessCpuTracker mProcessCpuTracker;

    private ColorPerfManager() {
        this.mDynamicDebug = false;
        this.DEBUG_SWITCH = sDebug | this.mDynamicDebug;
        this.mAms = null;
        this.mProcessCpuTracker = new ProcessCpuTracker(false);
    }

    /* access modifiers changed from: private */
    public static class ColorPerfManagerInstance {
        private static final ColorPerfManager sInstance = new ColorPerfManager();

        private ColorPerfManagerInstance() {
        }
    }

    public static ColorPerfManager getInstance() {
        return ColorPerfManagerInstance.sInstance;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpPerfDataLocked(int checkedDumpType, Bundle bundle, ProcessRecord proc) {
        File heapdumpDir = new File(new File(Environment.getDataDirectory(), "oppo"), "heapdump");
        if (!heapdumpDir.exists()) {
            Slog.e(TAG, "dump proc perf data dir not exists");
            return;
        }
        String fileNamePrefix = bundle.getString("file_name_prefix", "");
        Slog.i(TAG, "dump proc perf data type: " + checkedDumpType);
        if (TextUtils.isEmpty(fileNamePrefix)) {
            Slog.e(TAG, "dump proc perf data empty file name");
            return;
        }
        if ((checkedDumpType & 2) > 0) {
            dumpProcHeap(proc, heapdumpDir + "/" + fileNamePrefix + ".bin", bundle);
        }
        if ((checkedDumpType & 1) > 0) {
            dumpSmaps(bundle.getInt("pid", 0), heapdumpDir + "/" + fileNamePrefix + ".smaps");
        }
    }

    private int checkDumpType(int dumpType) {
        int result = 0;
        if ((dumpType & 2) > 0) {
            if (!OppoListManager.getInstance().isSystemDumpHeapEnable()) {
                Slog.e(TAG, "dump proc perf data feature not support");
            } else {
                result = 0 | 2;
            }
        }
        if ((dumpType & 1) > 0) {
            return result | 1;
        }
        return result;
    }

    private void dumpSmaps(int pid, String targetPath) {
        if (pid > 0) {
            saveFile("/proc/" + pid + "/smaps", targetPath);
        }
    }

    private boolean saveFile(String sourcePath, String targetPath) {
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(targetPath)) {
            return false;
        }
        File sFile = new File(sourcePath);
        File tFile = new File(targetPath);
        char[] buffer = new char[4096];
        if (!sFile.exists()) {
            return false;
        }
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            if (tFile.exists()) {
                tFile.delete();
            }
            tFile.createNewFile();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(sFile)));
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tFile, false)));
            while (true) {
                int bufferLength = reader2.read(buffer);
                if (bufferLength <= 0) {
                    break;
                }
                writer2.write(buffer, 0, bufferLength);
            }
            writer2.flush();
            try {
                reader2.close();
            } catch (IOException e) {
            }
            try {
                writer2.close();
                return true;
            } catch (IOException e2) {
                return true;
            }
        } catch (IOException e3) {
            Slog.e(TAG, "save smaps get error: " + e3);
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e5) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e6) {
                }
            }
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e7) {
                }
            }
            throw th;
        }
    }

    private void dumpProcHeap(ProcessRecord proc, String path, Bundle bundle) {
        Throwable th;
        RemoteException e;
        Exception e2;
        File file;
        ParcelFileDescriptor fd;
        boolean result;
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (bundle != null) {
            if (proc != null) {
                if (proc.thread != null) {
                    boolean managed = bundle.getBoolean("managed", false);
                    boolean mallocInfo = bundle.getBoolean("malloc_info", false);
                    boolean runGc = bundle.getBoolean("run_gc", false);
                    File file2 = null;
                    boolean result2 = false;
                    try {
                        try {
                            file = new File(path);
                            try {
                                if (file.exists()) {
                                    try {
                                        file.delete();
                                    } catch (RemoteException e3) {
                                        e = e3;
                                        file2 = file;
                                    } catch (Exception e4) {
                                        e2 = e4;
                                        file2 = file;
                                        try {
                                            Slog.e(TAG, "dump proc heap get Exception: " + e2);
                                            return;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        file2 = file;
                                        file2.delete();
                                        throw th;
                                    }
                                }
                                fd = ParcelFileDescriptor.open(file, 771751936);
                                if (fd == null) {
                                    Slog.e(TAG, "dump proc perf data get null fd");
                                    result = false;
                                } else {
                                    result = false;
                                }
                            } catch (RemoteException e5) {
                                e = e5;
                                file2 = file;
                                Slog.e(TAG, "dump proc heap get RemoteException: " + e);
                                return;
                            } catch (Exception e6) {
                                e2 = e6;
                                file2 = file;
                                Slog.e(TAG, "dump proc heap get Exception: " + e2);
                                return;
                            } catch (Throwable th4) {
                                th = th4;
                                file2 = file;
                                file2.delete();
                                throw th;
                            }
                        } catch (RemoteException e7) {
                            e = e7;
                            Slog.e(TAG, "dump proc heap get RemoteException: " + e);
                            return;
                        } catch (Exception e8) {
                            e2 = e8;
                            Slog.e(TAG, "dump proc heap get Exception: " + e2);
                            return;
                        }
                        try {
                            try {
                                proc.thread.dumpHeap(managed, mallocInfo, runGc, file.getAbsoluteFile().toString(), fd, (RemoteCallback) null);
                                if (1 == 0) {
                                    file.delete();
                                }
                                return;
                            } catch (RemoteException e9) {
                                e = e9;
                                result2 = result;
                                file2 = file;
                                Slog.e(TAG, "dump proc heap get RemoteException: " + e);
                                return;
                            } catch (Exception e10) {
                                e2 = e10;
                                result2 = result;
                                file2 = file;
                                Slog.e(TAG, "dump proc heap get Exception: " + e2);
                                return;
                            } catch (Throwable th5) {
                                th = th5;
                                result2 = result;
                                file2 = file;
                                file2.delete();
                                throw th;
                            }
                        } catch (RemoteException e11) {
                            e = e11;
                            result2 = result;
                            file2 = file;
                            Slog.e(TAG, "dump proc heap get RemoteException: " + e);
                            return;
                        } catch (Exception e12) {
                            e2 = e12;
                            result2 = result;
                            file2 = file;
                            Slog.e(TAG, "dump proc heap get Exception: " + e2);
                            return;
                        } catch (Throwable th6) {
                            th = th6;
                            result2 = result;
                            file2 = file;
                            file2.delete();
                            throw th;
                        }
                    } catch (RemoteException e13) {
                        e = e13;
                        Slog.e(TAG, "dump proc heap get RemoteException: " + e);
                        if (result2 || file2 == null) {
                            return;
                        }
                        file2.delete();
                        return;
                    } catch (Exception e14) {
                        e2 = e14;
                        Slog.e(TAG, "dump proc heap get Exception: " + e2);
                        if (result2 || file2 == null) {
                            return;
                        }
                        file2.delete();
                        return;
                    } catch (Throwable th7) {
                        th = th7;
                        if (!result2 && file2 != null) {
                            file2.delete();
                        }
                        throw th;
                    }
                }
            }
            Slog.e(TAG, "dump proc heap get unknown proc");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ProcessRecord findProcessLocked(int pid, int userId) {
        synchronized (this.mAms.mPidsSelfLocked) {
            ProcessRecord proc = this.mAms.mPidsSelfLocked.get(pid);
            if (proc == null) {
                return null;
            }
            if (userId == -1 || proc.userId == userId) {
                return proc;
            }
            return null;
        }
    }

    public boolean dumpProcPerfData(final Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        final int dumpType = bundle.getInt("dump_type", 0);
        final int checkedDumpType = checkDumpType(dumpType);
        if (checkedDumpType != 0) {
            final int pid = bundle.getInt("pid", 0);
            final int userId = bundle.getInt("user_id", UserHandle.myUserId());
            DumpThread.getHandler().post(new Runnable() {
                /* class com.android.server.am.ColorPerfManager.AnonymousClass1 */

                public void run() {
                    Trace.traceBegin(64, "dumpPerfDataLocked_" + dumpType + "_" + pid + "_" + Binder.getCallingPid());
                    try {
                        synchronized (this) {
                            ProcessRecord proc = ColorPerfManager.this.findProcessLocked(pid, userId);
                            if (proc != null) {
                                ColorPerfManager.this.dumpPerfDataLocked(checkedDumpType, bundle, proc);
                            } else {
                                Slog.d(ColorPerfManager.TAG, "proc info not found, pid=" + pid + ", userId=" + userId);
                            }
                        }
                    } catch (Exception e) {
                        Slog.i(ColorPerfManager.TAG, "dump proc perf data get Exception: " + e);
                    } catch (Throwable th) {
                        Trace.traceEnd(64);
                        throw th;
                    }
                    Trace.traceEnd(64);
                }
            });
            return true;
        }
        throw new IllegalArgumentException("dump proc perf data not support: type=" + dumpType);
    }

    private String getProcString(int uid, int pid, String name) {
        return uid + "#" + pid + "#" + name;
    }

    private List<String> getAllProcInfoList() {
        List<String> resultList = new ArrayList<>();
        synchronized (this.mProcessCpuTracker) {
            this.mProcessCpuTracker.update();
            int N = this.mProcessCpuTracker.countStats();
            for (int i = 0; i < N; i++) {
                ProcessCpuTracker.Stats st = this.mProcessCpuTracker.getStats(i);
                if (st.vsize > 0) {
                    resultList.add(getProcString(st.uid, st.pid, st.name));
                }
            }
        }
        return resultList;
    }

    public List<String> getProcCommonInfoList(int type) {
        if ((type & 1) > 0) {
            return getAllProcInfoList();
        }
        return new ArrayList();
    }

    public String getCmdlineName(int pid) {
        String cmdName = ProcStatsUtil.readTerminatedProcFile(new File(new File("/proc", Integer.toString(pid)), "cmdline").toString(), (byte) 0);
        if (cmdName == null) {
            return "<Unknown>";
        }
        int i = cmdName.lastIndexOf("/");
        if (i <= 0 || i >= cmdName.length() - 1) {
            return cmdName;
        }
        return cmdName.substring(i + 1);
    }

    private static final class DumpThread extends ServiceThread {
        private static Handler sHandler;
        private static HandlerExecutor sHandlerExecutor;
        private static DumpThread sInstance;

        private DumpThread() {
            super("color_perf.io", 0, true);
        }

        private static void ensureThreadLocked() {
            if (sInstance == null) {
                sInstance = new DumpThread();
                sInstance.start();
                sInstance.getLooper().setTraceTag(524288);
                sHandler = new Handler(sInstance.getLooper());
                sHandlerExecutor = new HandlerExecutor(sHandler);
            }
        }

        public static DumpThread get() {
            DumpThread dumpThread;
            synchronized (DumpThread.class) {
                ensureThreadLocked();
                dumpThread = sInstance;
            }
            return dumpThread;
        }

        public static Handler getHandler() {
            Handler handler;
            synchronized (DumpThread.class) {
                ensureThreadLocked();
                handler = sHandler;
            }
            return handler;
        }

        public static Executor getExecutor() {
            HandlerExecutor handlerExecutor;
            synchronized (DumpThread.class) {
                ensureThreadLocked();
                handlerExecutor = sHandlerExecutor;
            }
            return handlerExecutor;
        }
    }
}

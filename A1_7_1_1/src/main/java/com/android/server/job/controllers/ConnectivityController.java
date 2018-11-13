package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.NetworkInfo;
import android.net.NetworkPolicyManager;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;

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
public class ConnectivityController extends StateController implements OnNetworkActiveListener {
    private static final String TAG = "JobScheduler.Conn";
    private static ConnectivityController mSingleton;
    private static Object sCreationLock;
    private final ConnectivityManager mConnManager;
    private BroadcastReceiver mConnectivityReceiver;
    private INetworkPolicyListener mNetPolicyListener;
    private final NetworkPolicyManager mNetPolicyManager;
    @GuardedBy("mLock")
    private final ArrayList<JobStatus> mTrackedJobs;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.ConnectivityController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.ConnectivityController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.ConnectivityController.<clinit>():void");
    }

    public static ConnectivityController get(JobSchedulerService jms) {
        ConnectivityController connectivityController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new ConnectivityController(jms, jms.getContext(), jms.getLock());
            }
            connectivityController = mSingleton;
        }
        return connectivityController;
    }

    private ConnectivityController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mTrackedJobs = new ArrayList();
        this.mConnectivityReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ConnectivityController.this.updateTrackedJobs(-1);
            }
        };
        this.mNetPolicyListener = new Stub() {
            public void onUidRulesChanged(int uid, int uidRules) {
                ConnectivityController.this.updateTrackedJobs(uid);
            }

            public void onMeteredIfacesChanged(String[] meteredIfaces) {
                ConnectivityController.this.updateTrackedJobs(-1);
            }

            public void onRestrictBackgroundChanged(boolean restrictBackground) {
                ConnectivityController.this.updateTrackedJobs(-1);
            }

            public void onRestrictBackgroundWhitelistChanged(int uid, boolean whitelisted) {
                ConnectivityController.this.updateTrackedJobs(uid);
            }

            public void onRestrictBackgroundBlacklistChanged(int uid, boolean blacklisted) {
                ConnectivityController.this.updateTrackedJobs(uid);
            }
        };
        this.mConnManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        this.mNetPolicyManager = (NetworkPolicyManager) this.mContext.getSystemService(NetworkPolicyManager.class);
        this.mContext.registerReceiverAsUser(this.mConnectivityReceiver, UserHandle.SYSTEM, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"), null, null);
        this.mNetPolicyManager.registerListener(this.mNetPolicyListener);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (jobStatus.hasConnectivityConstraint() || jobStatus.hasUnmeteredConstraint() || jobStatus.hasNotRoamingConstraint()) {
            updateConstraintsSatisfied(jobStatus);
            this.mTrackedJobs.add(jobStatus);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if (jobStatus.hasConnectivityConstraint() || jobStatus.hasUnmeteredConstraint() || jobStatus.hasNotRoamingConstraint()) {
            this.mTrackedJobs.remove(jobStatus);
        }
    }

    private boolean updateConstraintsSatisfied(JobStatus jobStatus) {
        boolean connected = false;
        NetworkInfo info = this.mConnManager.getActiveNetworkInfoForUid(jobStatus.getSourceUid(), (jobStatus.getFlags() & 1) != 0);
        if (info != null) {
            connected = info.isConnected();
        }
        boolean unmetered = connected && !info.isMetered();
        boolean notRoaming = connected && !info.isRoaming();
        return (jobStatus.setConnectivityConstraintSatisfied(connected) | jobStatus.setUnmeteredConstraintSatisfied(unmetered)) | jobStatus.setNotRoamingConstraintSatisfied(notRoaming);
    }

    private void updateTrackedJobs(int uid) {
        synchronized (this.mLock) {
            int changed = 0;
            for (int i = 0; i < this.mTrackedJobs.size(); i++) {
                JobStatus js = (JobStatus) this.mTrackedJobs.get(i);
                if (uid == -1 || uid == js.getSourceUid()) {
                    changed |= updateConstraintsSatisfied(js);
                }
            }
            if (changed != 0) {
                this.mStateChangedListener.onControllerStateChanged();
            }
        }
    }

    public synchronized void onNetworkActive() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mTrackedJobs.size(); i++) {
                JobStatus js = (JobStatus) this.mTrackedJobs.get(i);
                if (js.isReady()) {
                    if (DEBUG) {
                        Slog.d(TAG, "Running " + js + " due to network activity.");
                    }
                    this.mStateChangedListener.onRunJobNow(js);
                }
            }
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.println("Connectivity.");
        pw.print("Tracking ");
        pw.print(this.mTrackedJobs.size());
        pw.println(":");
        for (int i = 0; i < this.mTrackedJobs.size(); i++) {
            JobStatus js = (JobStatus) this.mTrackedJobs.get(i);
            if (js.shouldDump(filterUid)) {
                pw.print("  #");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.print(": C=");
                pw.print(js.hasConnectivityConstraint());
                pw.print(": UM=");
                pw.print(js.hasUnmeteredConstraint());
                pw.print(": NR=");
                pw.println(js.hasNotRoamingConstraint());
            }
        }
    }
}

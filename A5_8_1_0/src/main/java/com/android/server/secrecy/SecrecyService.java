package com.android.server.secrecy;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.secrecy.ISecrecyService.Stub;
import android.secrecy.ISecrecyServiceReceiver;
import android.secrecy.SecrecyManagerInternal;
import android.util.Log;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.secrecy.policy.PolicyManager;
import com.android.server.secrecy.policy.util.LogUtil;
import com.android.server.secrecy.work.ActivityEncryptWork;
import com.android.server.secrecy.work.LogEncryptWork;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class SecrecyService extends SystemService {
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String SECRECY_SUPPORT_FEATURE = "oppo.secrecy.support";
    public static final String TAG = "SecrecyService";
    private ActivityEncryptWork mActivityEncryptWork;
    private final ConcurrentHashMap<IBinder, ClientRecord> mClientRecordMap = new ConcurrentHashMap();
    private final Context mContext;
    private LogEncryptWork mLogEncryptWork;
    private PolicyManager mPolicyManager;
    private boolean mSecrecySupport;
    private final ServiceThread mServiceThread;
    private boolean mSystemReady;

    final class ClientRecord implements DeathRecipient {
        IBinder mBinder;
        final ISecrecyServiceReceiver mReceiver;

        ClientRecord(IBinder binder, ISecrecyServiceReceiver receiver) {
            this.mReceiver = receiver;
            this.mBinder = binder;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.w(SecrecyService.TAG, "caught remote exception in linkToDeath: ", e);
            }
        }

        public void binderDied() {
            try {
                this.mBinder.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
            }
            SecrecyService.this.mClientRecordMap.remove(this.mBinder);
        }
    }

    private final class LocalService extends SecrecyManagerInternal {
        /* synthetic */ LocalService(SecrecyService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public boolean isInEncryptedAppList(ActivityInfo info, String callingPackage, int callingUid, int callingPid) {
            return SecrecyService.this.isInActivityConfig(info);
        }

        public boolean getSecrecyState(int type) {
            boolean result = SecrecyService.this.mPolicyManager.getPolicyState(type);
            Log.w(SecrecyService.TAG, "getSecrecyState type=" + type + ", result=" + result);
            return result;
        }
    }

    private final class SecrecyServiceWrapper extends Stub {
        /* synthetic */ SecrecyServiceWrapper(SecrecyService this$0, SecrecyServiceWrapper -this1) {
            this();
        }

        private SecrecyServiceWrapper() {
        }

        public boolean getSecrecyState(int type) {
            return SecrecyService.this.mPolicyManager.getPolicyState(type);
        }

        public boolean getSecrecyKey(byte[] key) {
            return SecrecyService.this.mPolicyManager.getSecrecyKey(key);
        }

        public byte[] generateCipherFromKey(int cipherLength) {
            return SecrecyConfig.getInstance().generateCipherFromKey(cipherLength);
        }

        public String generateTokenFromKey() {
            return SecrecyConfig.getInstance().generateTokenFromKey();
        }

        public boolean registerSecrecyServiceReceiver(ISecrecyServiceReceiver receiver) {
            if (!SecrecyService.this.isSecrecySupportLocal()) {
                return false;
            }
            IBinder binder = receiver.asBinder();
            SecrecyService.this.mClientRecordMap.putIfAbsent(binder, new ClientRecord(binder, receiver));
            return true;
        }

        public boolean isSecrecySupport() {
            return SecrecyService.this.isSecrecySupportLocal();
        }

        public boolean isInEncryptedAppList(ActivityInfo info, String callingPackage, int callingUid, int callingPid) {
            return SecrecyService.this.isInActivityConfig(info);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (Binder.getCallingUid() == 1000 || SecrecyService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
                SecrecyService.this.dumpInternal(fd, pw, args);
            } else {
                pw.println("Permission Denial: can't dump secrecy from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }
    }

    public SecrecyService(Context context) {
        super(context);
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        setSecrecySupport(context.getPackageManager().hasSystemFeature(SECRECY_SUPPORT_FEATURE));
        initPolicy();
        initWorks();
    }

    private void initWorks() {
        this.mActivityEncryptWork = new ActivityEncryptWork(this.mContext);
        this.mLogEncryptWork = new LogEncryptWork(this.mContext);
    }

    private void initPolicy() {
        this.mPolicyManager = PolicyManager.getInstance();
        this.mPolicyManager.setSecrecyService(this.mContext, this, this.mServiceThread.getLooper());
    }

    public void systemReady() {
        LogUtil.d(TAG, "systemReady");
        this.mSystemReady = true;
        this.mPolicyManager.systemReady(this.mSecrecySupport);
    }

    public boolean isSecrecySupportLocal() {
        return this.mSecrecySupport;
    }

    public void setSecrecySupport(boolean support) {
        this.mSecrecySupport = support;
        LogUtil.d(TAG, "setSecrecySupport mSecrecySupport = " + this.mSecrecySupport);
    }

    private boolean isInActivityConfig(ActivityInfo info) {
        if (isSecrecySupportLocal() && this.mPolicyManager.getPolicyState(2) && this.mActivityEncryptWork.preWork(info)) {
            return this.mActivityEncryptWork.doWork(info);
        }
        return false;
    }

    public void notifySecrecyState(Map map) {
        for (ClientRecord client : this.mClientRecordMap.values()) {
            try {
                client.mReceiver.onSecrecyStateChanged(map);
            } catch (RemoteException e) {
                client.binderDied();
            }
        }
    }

    public void onStart() {
        LogUtil.d(TAG, "onStart");
        publishBinderService("secrecy", new SecrecyServiceWrapper(this, null));
        publishLocalService(SecrecyManagerInternal.class, new LocalService(this, null));
    }

    private void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length > 0) {
            String opt = args[0];
            if (opt != null && opt.length() > 0 && opt.charAt(0) == '-') {
                int opti;
                if ("-h".equals(opt)) {
                    pw.println("secrecy service dump options:");
                    pw.println("  [-h] [cmd] ...");
                    pw.println("  cmd may be one of:");
                    pw.println("    l[log]: dynamically adjust secrecy log ");
                    return;
                } else if ("-import_key".equals(opt)) {
                    if (!isSecrecySupportLocal()) {
                        pw.println("This device do not support secrecy dump");
                        return;
                    } else if (1 < args.length) {
                        String keyArg = args[1];
                        opti = 1 + 1;
                        if (this.mPolicyManager.getDecryptTool().verifyKey(pw, keyArg)) {
                            try {
                                this.mPolicyManager.importRC4Key(keyArg);
                            } catch (IllegalArgumentException e) {
                                pw.println("ERROR: The key contains NOT hex.");
                            }
                        }
                        return;
                    } else {
                        pw.println("ERROR: Key argument is missing.");
                        return;
                    }
                } else if ("-config".equals(opt)) {
                    if (isSecrecySupportLocal()) {
                        if (2 < args.length) {
                            String configArg = args[1];
                            opti = 1 + 1;
                            String signatureArg = args[opti];
                            opti++;
                            this.mPolicyManager.getDecryptTool().config(pw, configArg, signatureArg);
                        } else if (1 < args.length) {
                            this.mPolicyManager.getDecryptTool().config(pw, args[1]);
                        } else {
                            pw.println("ERROR: Config or Signature argument is missing.");
                        }
                        return;
                    }
                    pw.println("This device do not support secrecy dump");
                    return;
                } else if ("-imei".equals(opt)) {
                    if (isSecrecySupportLocal()) {
                        pw.println("IMEI: " + this.mPolicyManager.getImei());
                        return;
                    }
                    pw.println("This device do not support secrecy dump");
                    return;
                } else if ("-status".equals(opt)) {
                    this.mPolicyManager.status(fd, pw);
                    return;
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                    return;
                }
            }
        }
        if (args.length > 0) {
            String cmd = args[0];
            if ("log".equals(cmd) || "l".equals(cmd)) {
                dynamicallyConfigLogTag(pw, args);
                return;
            } else if ("debug_switch".equals(cmd)) {
                pw.println("  all=" + DEBUG);
                return;
            }
        }
        if (isSecrecySupportLocal()) {
            pw.println("mSystemReady       = " + this.mSystemReady);
            pw.println("mSecrecySupport       = " + this.mSecrecySupport);
            pw.println("DEBUG    = " + DEBUG);
            pw.println("LogLevel = " + LogUtil.getLevelString());
            this.mPolicyManager.dump(fd, pw, "");
            if (!this.mPolicyManager.getPolicyState(2)) {
                this.mActivityEncryptWork.dump(fd, pw, "");
            }
            this.mLogEncryptWork.dump(fd, pw, "");
            return;
        }
        pw.println("This device do not support secrecy dump");
    }

    protected void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
        if ("all".equals(tag)) {
            DEBUG = on;
            LogUtil.dynamicallyConfigLog(on);
        }
    }

    protected void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in SecrecyService");
        pw.println("cmd: dumpsys secrecy log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }
}

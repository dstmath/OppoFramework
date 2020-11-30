package com.android.server.adb;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.debug.AdbManagerInternal;
import android.debug.IAdbManager;
import android.debug.IAdbTransport;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.sysprop.AdbProperties;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.theia.NoFocusWindow;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;

public class AdbService extends IAdbManager.Stub {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int MSG_BOOT_COMPLETED = 2;
    private static final int MSG_ENABLE_ADB = 1;
    private static final String TAG = "AdbService";
    private static final String USB_PERSISTENT_CONFIG_PROPERTY = "persist.sys.usb.config";
    private boolean mAdbEnabled;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private AdbDebuggingManager mDebuggingManager;
    private final AdbHandler mHandler;
    private final ArrayMap<IBinder, IAdbTransport> mTransports;

    public static class Lifecycle extends SystemService {
        private AdbService mAdbService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.adb.AdbService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.adb.AdbService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mAdbService = new AdbService(getContext());
            publishBinderService("adb", this.mAdbService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mAdbService.systemReady();
            } else if (phase == 1000) {
                this.mAdbService.bootCompleted();
            }
        }
    }

    private class AdbManagerInternalImpl extends AdbManagerInternal {
        private AdbManagerInternalImpl() {
        }

        public void registerTransport(IAdbTransport transport) {
            synchronized (AdbService.this.mTransports) {
                AdbService.this.mTransports.put(transport.asBinder(), transport);
            }
        }

        public void unregisterTransport(IAdbTransport transport) {
            synchronized (AdbService.this.mTransports) {
                AdbService.this.mTransports.remove(transport.asBinder());
            }
        }

        public boolean isAdbEnabled() {
            return AdbService.this.mAdbEnabled;
        }

        public File getAdbKeysFile() {
            return AdbService.this.mDebuggingManager.getUserKeyFile();
        }

        public File getAdbTempKeysFile() {
            return AdbService.this.mDebuggingManager.getAdbTempKeysFile();
        }
    }

    /* access modifiers changed from: private */
    public final class AdbHandler extends Handler {
        AdbHandler(Looper looper) {
            super(looper);
            try {
                AdbService.this.mAdbEnabled = SystemProperties.getBoolean("ro.oppo.adb.secure", false) || containsFunction(SystemProperties.get(AdbService.USB_PERSISTENT_CONFIG_PROPERTY, ""), "adb") || Settings.Global.getInt(AdbService.this.mContentResolver, "adb_enabled", 0) > 0 || "enable".equals(SystemProperties.get("ro.boot.atm", ""));
                if (!NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("SPECIAL_OPPO_CONFIG"))) {
                    AdbService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("adb_enabled"), false, new AdbSettingsObserver());
                }
            } catch (Exception e) {
                Slog.e(AdbService.TAG, "Error initializing AdbHandler", e);
            }
        }

        private boolean containsFunction(String functions, String function) {
            int index = functions.indexOf(function);
            if (index < 0) {
                return false;
            }
            if (index > 0 && functions.charAt(index - 1) != ',') {
                return false;
            }
            int charAfter = function.length() + index;
            if (charAfter >= functions.length() || functions.charAt(charAfter) == ',') {
                return true;
            }
            return false;
        }

        public void sendMessage(int what, boolean arg) {
            removeMessages(what);
            Message m = Message.obtain(this, what);
            m.arg1 = arg ? 1 : 0;
            sendMessage(m);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = true;
            if (i == 1) {
                AdbService adbService = AdbService.this;
                if (msg.arg1 != 1) {
                    z = false;
                }
                adbService.setAdbEnabled(z);
            } else if (i == 2 && AdbService.this.mDebuggingManager != null) {
                AdbService.this.mDebuggingManager.setAdbEnabled(AdbService.this.mAdbEnabled);
            }
        }
    }

    private class AdbSettingsObserver extends ContentObserver {
        AdbSettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            boolean enable = false;
            if (Settings.Global.getInt(AdbService.this.mContentResolver, "adb_enabled", 0) > 0) {
                enable = true;
            }
            AdbService.this.mHandler.sendMessage(1, enable);
        }
    }

    private AdbService(Context context) {
        this.mTransports = new ArrayMap<>();
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        boolean secureAdbEnabled = ((Boolean) AdbProperties.secure().orElse(false)).booleanValue();
        boolean dataEncrypted = NoFocusWindow.HUNG_CONFIG_ENABLE.equals(SystemProperties.get("vold.decrypt"));
        if (secureAdbEnabled && !dataEncrypted) {
            this.mDebuggingManager = new AdbDebuggingManager(context);
        }
        this.mHandler = new AdbHandler(FgThread.get().getLooper());
        LocalServices.addService(AdbManagerInternal.class, new AdbManagerInternalImpl());
    }

    public void systemReady() {
        if (DEBUG) {
            Slog.d(TAG, "systemReady");
        }
        try {
            Settings.Global.putInt(this.mContentResolver, "adb_enabled", this.mAdbEnabled ? 1 : 0);
        } catch (SecurityException e) {
            Slog.d(TAG, "ADB_ENABLED is restricted.");
        }
    }

    public void bootCompleted() {
        if (DEBUG) {
            Slog.d(TAG, "boot completed");
        }
        this.mHandler.sendEmptyMessage(2);
    }

    public void allowDebugging(boolean alwaysAllow, String publicKey) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEBUGGING", null);
        AdbDebuggingManager adbDebuggingManager = this.mDebuggingManager;
        if (adbDebuggingManager != null) {
            adbDebuggingManager.allowDebugging(alwaysAllow, publicKey);
        }
    }

    public void denyDebugging() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEBUGGING", null);
        AdbDebuggingManager adbDebuggingManager = this.mDebuggingManager;
        if (adbDebuggingManager != null) {
            adbDebuggingManager.denyDebugging();
        }
    }

    public void clearDebuggingKeys() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_DEBUGGING", null);
        AdbDebuggingManager adbDebuggingManager = this.mDebuggingManager;
        if (adbDebuggingManager != null) {
            adbDebuggingManager.clearDebuggingKeys();
            return;
        }
        throw new RuntimeException("Cannot clear ADB debugging keys, AdbDebuggingManager not enabled");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAdbEnabled(boolean enable) {
        if (DEBUG) {
            Slog.d(TAG, "setAdbEnabled(" + enable + "), mAdbEnabled=" + this.mAdbEnabled);
        }
        if (enable != this.mAdbEnabled) {
            this.mAdbEnabled = enable;
            for (IAdbTransport transport : this.mTransports.values()) {
                try {
                    transport.onAdbEnabled(enable);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Unable to send onAdbEnabled to transport " + transport.toString());
                }
            }
            AdbDebuggingManager adbDebuggingManager = this.mDebuggingManager;
            if (adbDebuggingManager != null) {
                adbDebuggingManager.setAdbEnabled(enable);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        DualDumpOutputStream dump;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
            long ident = Binder.clearCallingIdentity();
            try {
                ArraySet<String> argsSet = new ArraySet<>();
                Collections.addAll(argsSet, args);
                boolean dumpAsProto = false;
                if (argsSet.contains(PriorityDump.PROTO_ARG)) {
                    dumpAsProto = true;
                }
                if (argsSet.size() != 0 && !argsSet.contains("-a")) {
                    if (!dumpAsProto) {
                        pw.println("Dump current ADB state");
                        pw.println("  No commands available");
                    }
                }
                if (dumpAsProto) {
                    dump = new DualDumpOutputStream(new ProtoOutputStream(fd));
                } else {
                    pw.println("ADB MANAGER STATE (dumpsys adb):");
                    dump = new DualDumpOutputStream(new IndentingPrintWriter(pw, "  "));
                }
                if (this.mDebuggingManager != null) {
                    this.mDebuggingManager.dump(dump, "debugging_manager", 1146756268033L);
                }
                dump.flush();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
}

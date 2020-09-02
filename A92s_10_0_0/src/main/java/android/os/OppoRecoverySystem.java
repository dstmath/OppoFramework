package android.os;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IRecoverySystem;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class OppoRecoverySystem {
    private static final File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static final String PACKAGE_NAME_WIPING_EUICC_DATA_CALLBACK = "android";
    private static final File RECOVERY_DIR = new File("/cache/recovery");
    private static final String TAG = "OppoRecoverySystem";

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.String.replace(char, char):java.lang.String}
     arg types: [int, int]
     candidates:
      ClspMth{java.lang.String.replace(java.lang.CharSequence, java.lang.CharSequence):java.lang.String}
      ClspMth{java.lang.String.replace(char, char):java.lang.String} */
    private static String sanitizeArg(String arg) {
        return arg.replace(0, '?').replace(10, '?');
    }

    private static void bootCommand(Context context, String... args) throws IOException {
        LOG_FILE.delete();
        StringBuilder command = new StringBuilder();
        for (String arg : args) {
            if (!TextUtils.isEmpty(arg)) {
                command.append(arg);
                command.append("\n");
            }
        }
        try {
            IRecoverySystem.Stub.asInterface(ServiceManager.getService("recovery")).rebootRecoveryWithCommand(command.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        throw new IOException("Reboot failed (no permissions?)");
    }

    public static void clearBackupProperty() {
        Log.d(TAG, "clearBackupProperty!");
        try {
            IStorageManager mountService = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            mountService.setField(StorageManager.SYSTEM_LOCALE_KEY, "");
            mountService.setField(StorageManager.PATTERN_VISIBLE_KEY, "");
            mountService.setField(StorageManager.PASSWORD_VISIBLE_KEY, "");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void rebootFormatUserData(Context context, boolean shutdown, String reason, boolean force, boolean wipeEuicc) throws IOException {
        Log.d(TAG, "rebootFormatUserData!");
        UserManager um = (UserManager) context.getSystemService("user");
        if (force || !um.hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET)) {
            final ConditionVariable condition = new ConditionVariable();
            Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR_NOTIFICATION);
            intent.addFlags(285212672);
            context.sendOrderedBroadcastAsUser(intent, UserHandle.SYSTEM, Manifest.permission.MASTER_CLEAR, new BroadcastReceiver() {
                /* class android.os.OppoRecoverySystem.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    ConditionVariable.this.open();
                }
            }, null, 0, null, null);
            condition.block();
            if (wipeEuicc) {
                RecoverySystem.wipeEuiccData(context, "android");
            }
            String shutdownArg = null;
            if (shutdown) {
                shutdownArg = "--shutdown_after";
            }
            String reasonArg = null;
            if (!TextUtils.isEmpty(reason)) {
                reasonArg = "--reason=" + sanitizeArg(reason);
            }
            bootCommand(context, shutdownArg, "--format_data", reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
            return;
        }
        throw new SecurityException("Wiping data is not allowed for this user.");
    }

    public static void rebootFormatUserDataBackup(Context context, boolean shutdown, String reason, boolean force, boolean wipeEuicc) throws IOException {
        Log.d(TAG, "rebootFormatUserDataBackup!");
        UserManager um = (UserManager) context.getSystemService("user");
        if (force || !um.hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET)) {
            final ConditionVariable condition = new ConditionVariable();
            Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR_NOTIFICATION);
            intent.addFlags(285212672);
            context.sendOrderedBroadcastAsUser(intent, UserHandle.SYSTEM, Manifest.permission.MASTER_CLEAR, new BroadcastReceiver() {
                /* class android.os.OppoRecoverySystem.AnonymousClass2 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    ConditionVariable.this.open();
                }
            }, null, 0, null, null);
            condition.block();
            if (wipeEuicc) {
                RecoverySystem.wipeEuiccData(context, "android");
            }
            String shutdownArg = null;
            if (shutdown) {
                shutdownArg = "--shutdown_after";
            }
            String reasonArg = null;
            if (!TextUtils.isEmpty(reason)) {
                reasonArg = "--reason=" + sanitizeArg(reason);
            }
            bootCommand(context, shutdownArg, "--format_data_backup", reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
            return;
        }
        throw new SecurityException("Wiping data is not allowed for this user.");
    }
}

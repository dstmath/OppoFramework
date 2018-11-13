package com.android.server;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.Slog;
import java.io.IOException;

public class MasterClearReceiver extends BroadcastReceiver {
    private static final String TAG = "MasterClear";

    private class WipeAdoptableDisksTask extends AsyncTask<Void, Void, Void> {
        private final Thread mChainedTask;
        private final Context mContext;
        private final ProgressDialog mProgressDialog;

        public WipeAdoptableDisksTask(Context context, Thread chainedTask) {
            this.mContext = context;
            this.mChainedTask = chainedTask;
            this.mProgressDialog = new ProgressDialog(context);
        }

        protected void onPreExecute() {
            this.mProgressDialog.setIndeterminate(true);
            this.mProgressDialog.getWindow().setType(2003);
            this.mProgressDialog.setMessage(this.mContext.getText(17040539));
            this.mProgressDialog.show();
        }

        protected Void doInBackground(Void... params) {
            Slog.w(MasterClearReceiver.TAG, "Wiping adoptable disks");
            ((StorageManager) this.mContext.getSystemService("storage")).wipeAdoptableDisks();
            return null;
        }

        protected void onPostExecute(Void result) {
            this.mProgressDialog.dismiss();
            this.mChainedTask.start();
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (!"true".equals(SystemProperties.get("persist.sys.lock.phone"))) {
            if (!intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE") || "google.com".equals(intent.getStringExtra("from"))) {
                final boolean shutdown = intent.getBooleanExtra("shutdown", false);
                final String reason = intent.getStringExtra("android.intent.extra.REASON");
                boolean wipeExternalStorage = intent.getBooleanExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", false);
                final boolean forceWipe = intent.getBooleanExtra("android.intent.extra.FORCE_MASTER_CLEAR", false);
                final boolean bFormat = intent.getBooleanExtra("formatdata", false);
                Slog.w(TAG, "Get format command from extra, data will be formated!!!");
                if (context.getPackageManager().hasSystemFeature("oppo.business.custom") && context.getPackageManager().hasSystemFeature("oppo.customize.function.reset")) {
                    SystemProperties.set("persist.sys.wipemedia", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                }
                Slog.w(TAG, "!!! FACTORY RESET !!!");
                final Context context2 = context;
                Thread thr = new Thread("Reboot") {
                    public void run() {
                        try {
                            if (bFormat) {
                                Slog.d(MasterClearReceiver.TAG, "Call mtehod: rebootFormatUserData");
                                RecoverySystem.rebootFormatUserData(context2, shutdown, reason, forceWipe);
                            } else {
                                Slog.d(MasterClearReceiver.TAG, "Call mtehod: rebootWipeUserData");
                                RecoverySystem.rebootWipeUserData(context2, shutdown, reason, forceWipe);
                            }
                            Log.wtf(MasterClearReceiver.TAG, "Still running after master clear?!");
                        } catch (IOException e) {
                            Slog.e(MasterClearReceiver.TAG, "Can't perform master clear/factory reset", e);
                        } catch (SecurityException e2) {
                            Slog.e(MasterClearReceiver.TAG, "Can't perform master clear/factory reset", e2);
                        }
                    }
                };
                if (wipeExternalStorage) {
                    new WipeAdoptableDisksTask(context, thr).execute(new Void[0]);
                } else {
                    thr.start();
                }
                return;
            }
            Slog.w(TAG, "Ignoring master clear request -- not from trusted server.");
        }
    }
}

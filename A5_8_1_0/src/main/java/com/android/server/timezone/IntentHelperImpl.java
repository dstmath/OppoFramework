package com.android.server.timezone;

import android.app.timezone.RulesUpdaterContract;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Slog;
import com.android.server.EventLogTags;

final class IntentHelperImpl implements IntentHelper {
    private static final String TAG = "timezone.IntentHelperImpl";
    private final Context mContext;
    private String mUpdaterAppPackageName;

    private static class Receiver extends BroadcastReceiver {
        private final PackageTracker mPackageTracker;

        /* synthetic */ Receiver(PackageTracker packageTracker, Receiver -this1) {
            this(packageTracker);
        }

        private Receiver(PackageTracker packageTracker) {
            this.mPackageTracker = packageTracker;
        }

        public void onReceive(Context context, Intent intent) {
            Slog.d(IntentHelperImpl.TAG, "Received intent: " + intent.toString());
            this.mPackageTracker.triggerUpdateIfNeeded(true);
        }
    }

    IntentHelperImpl(Context context) {
        this.mContext = context;
    }

    public void initialize(String updaterAppPackageName, String dataAppPackageName, PackageTracker packageTracker) {
        this.mUpdaterAppPackageName = updaterAppPackageName;
        IntentFilter packageIntentFilter = new IntentFilter();
        packageIntentFilter.addDataScheme("package");
        packageIntentFilter.addDataSchemeSpecificPart(updaterAppPackageName, 0);
        packageIntentFilter.addDataSchemeSpecificPart(dataAppPackageName, 0);
        packageIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageIntentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        this.mContext.registerReceiver(new Receiver(packageTracker, null), packageIntentFilter);
    }

    public void sendTriggerUpdateCheck(CheckToken checkToken) {
        RulesUpdaterContract.sendBroadcast(this.mContext, this.mUpdaterAppPackageName, checkToken.toByteArray());
        EventLogTags.writeTimezoneTriggerCheck(checkToken.toString());
    }

    public synchronized void scheduleReliabilityTrigger(long minimumDelayMillis) {
        TimeZoneUpdateIdler.schedule(this.mContext, minimumDelayMillis);
    }

    public synchronized void unscheduleReliabilityTrigger() {
        TimeZoneUpdateIdler.unschedule(this.mContext);
    }
}

package com.android.server.location;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.Context;
import android.content.Intent;
import android.location.Geofence;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoPermissionConstants;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GeofenceManager implements LocationListener, OnFinished {
    private static final boolean D = false;
    private static final long MAX_AGE_NANOS = 300000000000L;
    private static final long MAX_INTERVAL_MS = 7200000;
    private static final int MAX_SPEED_M_S = 100;
    private static final long MIN_INTERVAL_MS = 60000;
    private static final int MSG_UPDATE_FENCES = 1;
    private static final String TAG = "GeofenceManager";
    private final AppOpsManager mAppOps;
    private final LocationBlacklist mBlacklist;
    private final Context mContext;
    private List<GeofenceState> mFences;
    private final GeofenceHandler mHandler;
    private Location mLastLocationUpdate;
    private final LocationManager mLocationManager;
    private long mLocationUpdateInterval;
    private Object mLock;
    private boolean mPendingUpdate;
    private boolean mReceivingLocationUpdates;
    private final WakeLock mWakeLock;

    private final class GeofenceHandler extends Handler {
        public GeofenceHandler() {
            super(true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    GeofenceManager.this.updateFences();
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.location.GeofenceManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.location.GeofenceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GeofenceManager.<clinit>():void");
    }

    public GeofenceManager(Context context, LocationBlacklist blacklist) {
        this.mLock = new Object();
        this.mFences = new LinkedList();
        this.mContext = context;
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, TAG);
        this.mHandler = new GeofenceHandler();
        this.mBlacklist = blacklist;
    }

    public void addFence(LocationRequest request, Geofence geofence, PendingIntent intent, int allowedResolutionLevel, int uid, String packageName) {
        if (D) {
            Slog.d(TAG, "addFence: request=" + request + ", geofence=" + geofence + ", intent=" + intent + ", uid=" + uid + ", packageName=" + packageName);
        }
        GeofenceState state = new GeofenceState(geofence, request.getExpireAt(), allowedResolutionLevel, uid, packageName, intent);
        synchronized (this.mLock) {
            for (int i = this.mFences.size() - 1; i >= 0; i--) {
                GeofenceState w = (GeofenceState) this.mFences.get(i);
                if (geofence.equals(w.mFence) && intent.equals(w.mIntent)) {
                    this.mFences.remove(i);
                    break;
                }
            }
            this.mFences.add(state);
            scheduleUpdateFencesLocked();
        }
    }

    public void removeFence(Geofence fence, PendingIntent intent) {
        if (D) {
            Slog.d(TAG, "removeFence: fence=" + fence + ", intent=" + intent);
        }
        synchronized (this.mLock) {
            Iterator<GeofenceState> iter = this.mFences.iterator();
            while (iter.hasNext()) {
                GeofenceState state = (GeofenceState) iter.next();
                if (state.mIntent.equals(intent)) {
                    if (fence == null) {
                        iter.remove();
                    } else if (fence.equals(state.mFence)) {
                        iter.remove();
                    }
                }
            }
            scheduleUpdateFencesLocked();
        }
    }

    public void removeFence(String packageName) {
        if (D) {
            Slog.d(TAG, "removeFence: packageName=" + packageName);
        }
        synchronized (this.mLock) {
            Iterator<GeofenceState> iter = this.mFences.iterator();
            while (iter.hasNext()) {
                if (((GeofenceState) iter.next()).mPackageName.equals(packageName)) {
                    iter.remove();
                }
            }
            scheduleUpdateFencesLocked();
        }
    }

    private void removeExpiredFencesLocked() {
        long time = SystemClock.elapsedRealtime();
        Iterator<GeofenceState> iter = this.mFences.iterator();
        while (iter.hasNext()) {
            if (((GeofenceState) iter.next()).mExpireAt < time) {
                iter.remove();
            }
        }
    }

    private void scheduleUpdateFencesLocked() {
        if (!this.mPendingUpdate) {
            this.mPendingUpdate = true;
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private Location getFreshLocationLocked() {
        Location location;
        if (this.mReceivingLocationUpdates) {
            location = this.mLastLocationUpdate;
        } else {
            location = null;
        }
        if (location == null && !this.mFences.isEmpty()) {
            location = this.mLocationManager.getLastLocation();
        }
        if (location != null && SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos() <= MAX_AGE_NANOS) {
            return location;
        }
        return null;
    }

    private void updateFences() {
        List<PendingIntent> enterIntents = new LinkedList();
        List<PendingIntent> exitIntents = new LinkedList();
        synchronized (this.mLock) {
            this.mPendingUpdate = false;
            removeExpiredFencesLocked();
            Location location = getFreshLocationLocked();
            double minFenceDistance = Double.MAX_VALUE;
            boolean needUpdates = false;
            for (GeofenceState state : this.mFences) {
                if (!this.mBlacklist.isBlacklisted(state.mPackageName)) {
                    if (LocationManagerService.resolutionLevelToOp(state.mAllowedResolutionLevel) >= 0) {
                        if (this.mAppOps.noteOpNoThrow(1, state.mUid, state.mPackageName) != 0) {
                            if (D) {
                                Slog.d(TAG, "skipping geofence processing for no op app: " + state.mPackageName);
                            }
                        }
                    }
                    needUpdates = true;
                    if (location != null) {
                        int event = state.processLocation(location);
                        if ((event & 1) != 0) {
                            enterIntents.add(state.mIntent);
                        }
                        if ((event & 2) != 0) {
                            exitIntents.add(state.mIntent);
                        }
                        double fenceDistance = state.getDistanceToBoundary();
                        if (fenceDistance < minFenceDistance) {
                            minFenceDistance = fenceDistance;
                        }
                    }
                } else if (D) {
                    Slog.d(TAG, "skipping geofence processing for blacklisted app: " + state.mPackageName);
                }
            }
            if (needUpdates) {
                long intervalMs;
                if (location == null || Double.compare(minFenceDistance, Double.MAX_VALUE) == 0) {
                    intervalMs = 60000;
                } else {
                    intervalMs = (long) Math.min(7200000.0d, Math.max(60000.0d, (1000.0d * minFenceDistance) / 100.0d));
                }
                if (!(this.mReceivingLocationUpdates && this.mLocationUpdateInterval == intervalMs)) {
                    this.mReceivingLocationUpdates = true;
                    this.mLocationUpdateInterval = intervalMs;
                    this.mLastLocationUpdate = location;
                    LocationRequest request = new LocationRequest();
                    request.setInterval(intervalMs).setFastestInterval(0);
                    this.mLocationManager.requestLocationUpdates(request, this, this.mHandler.getLooper());
                }
            } else if (this.mReceivingLocationUpdates) {
                this.mReceivingLocationUpdates = false;
                this.mLocationUpdateInterval = 0;
                this.mLastLocationUpdate = null;
                this.mLocationManager.removeUpdates(this);
            }
            if (D) {
                Slog.d(TAG, "updateFences: location=" + location + ", mFences.size()=" + this.mFences.size() + ", mReceivingLocationUpdates=" + this.mReceivingLocationUpdates + ", mLocationUpdateInterval=" + this.mLocationUpdateInterval + ", mLastLocationUpdate=" + this.mLastLocationUpdate);
            }
        }
        for (PendingIntent intent : exitIntents) {
            sendIntentExit(intent);
        }
        for (PendingIntent intent2 : enterIntents) {
            sendIntentEnter(intent2);
        }
    }

    private void sendIntentEnter(PendingIntent pendingIntent) {
        if (D) {
            Slog.d(TAG, "sendIntentEnter: pendingIntent=" + pendingIntent);
        }
        Intent intent = new Intent();
        intent.putExtra("entering", true);
        sendIntent(pendingIntent, intent);
    }

    private void sendIntentExit(PendingIntent pendingIntent) {
        if (D) {
            Slog.d(TAG, "sendIntentExit: pendingIntent=" + pendingIntent);
        }
        Intent intent = new Intent();
        intent.putExtra("entering", false);
        sendIntent(pendingIntent, intent);
    }

    private void sendIntent(PendingIntent pendingIntent, Intent intent) {
        this.mWakeLock.acquire();
        try {
            pendingIntent.send(this.mContext, 0, intent, this, null, OppoPermissionConstants.PERMISSION_ACCESS);
        } catch (CanceledException e) {
            removeFence(null, pendingIntent);
            this.mWakeLock.release();
        }
    }

    public void onLocationChanged(Location location) {
        synchronized (this.mLock) {
            if (this.mReceivingLocationUpdates) {
                this.mLastLocationUpdate = location;
            }
            if (this.mPendingUpdate) {
                this.mHandler.removeMessages(1);
            } else {
                this.mPendingUpdate = true;
            }
        }
        updateFences();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
        this.mWakeLock.release();
    }

    public void dump(PrintWriter pw) {
        pw.println("  Geofences:");
        for (GeofenceState state : this.mFences) {
            pw.append("    ");
            pw.append(state.mPackageName);
            pw.append(" ");
            pw.append(state.mFence.toString());
            pw.append("\n");
        }
    }
}

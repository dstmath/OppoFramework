package com.android.server.location;

import android.content.Context;
import android.hardware.location.GeofenceHardwareImpl;
import android.hardware.location.GeofenceHardwareRequestParcelable;
import android.hardware.location.IFusedLocationHardware;
import android.hardware.location.IFusedLocationHardware.Stub;
import android.hardware.location.IFusedLocationHardwareSink;
import android.location.FusedBatchOptions;
import android.location.IFusedGeofenceHardware;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.display.OppoBrightUtils;

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
public class FlpHardwareProvider {
    private static final int FIRST_VERSION_WITH_FLUSH_LOCATIONS = 2;
    private static final int FLP_GEOFENCE_MONITOR_STATUS_AVAILABLE = 2;
    private static final int FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE = 1;
    private static final int FLP_RESULT_ERROR = -1;
    private static final int FLP_RESULT_ID_EXISTS = -4;
    private static final int FLP_RESULT_ID_UNKNOWN = -5;
    private static final int FLP_RESULT_INSUFFICIENT_MEMORY = -2;
    private static final int FLP_RESULT_INVALID_GEOFENCE_TRANSITION = -6;
    private static final int FLP_RESULT_SUCCESS = 0;
    private static final int FLP_RESULT_TOO_MANY_GEOFENCES = -3;
    public static final String GEOFENCING = "Geofencing";
    public static final String LOCATION = "Location";
    private static final String TAG = "FlpHardwareProvider";
    private static FlpHardwareProvider sSingletonInstance;
    private int mBatchingCapabilities;
    private final Context mContext;
    private final IFusedGeofenceHardware mGeofenceHardwareService;
    private GeofenceHardwareImpl mGeofenceHardwareSink;
    private boolean mHaveBatchingCapabilities;
    private final IFusedLocationHardware mLocationHardware;
    private IFusedLocationHardwareSink mLocationSink;
    private final Object mLocationSinkLock;
    private int mVersion;

    private final class NetworkLocationListener implements LocationListener {
        /* synthetic */ NetworkLocationListener(FlpHardwareProvider this$0, NetworkLocationListener networkLocationListener) {
            this();
        }

        private NetworkLocationListener() {
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider()) && location.hasAccuracy()) {
                FlpHardwareProvider.this.nativeInjectLocation(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.location.FlpHardwareProvider.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.location.FlpHardwareProvider.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.FlpHardwareProvider.<clinit>():void");
    }

    private native void nativeAddGeofences(GeofenceHardwareRequestParcelable[] geofenceHardwareRequestParcelableArr);

    private static native void nativeClassInit();

    private native void nativeCleanup();

    private native void nativeFlushBatchedLocations();

    private native int nativeGetBatchSize();

    private native void nativeInit();

    private native void nativeInjectDeviceContext(int i);

    private native void nativeInjectDiagnosticData(String str);

    private native void nativeInjectLocation(Location location);

    private native boolean nativeIsDeviceContextSupported();

    private native boolean nativeIsDiagnosticSupported();

    private native boolean nativeIsGeofencingSupported();

    private static native boolean nativeIsSupported();

    private native void nativeModifyGeofenceOption(int i, int i2, int i3, int i4, int i5, int i6);

    private native void nativePauseGeofence(int i);

    private native void nativeRemoveGeofences(int[] iArr);

    private native void nativeRequestBatchedLocation(int i);

    private native void nativeResumeGeofence(int i, int i2);

    private native void nativeStartBatching(int i, FusedBatchOptions fusedBatchOptions);

    private native void nativeStopBatching(int i);

    private native void nativeUpdateBatchingOptions(int i, FusedBatchOptions fusedBatchOptions);

    public static FlpHardwareProvider getInstance(Context context) {
        if (sSingletonInstance == null) {
            sSingletonInstance = new FlpHardwareProvider(context);
            sSingletonInstance.nativeInit();
        }
        return sSingletonInstance;
    }

    private FlpHardwareProvider(Context context) {
        this.mGeofenceHardwareSink = null;
        this.mLocationSink = null;
        this.mVersion = 1;
        this.mLocationSinkLock = new Object();
        this.mLocationHardware = new Stub() {
            public void registerSink(IFusedLocationHardwareSink eventSink) {
                synchronized (FlpHardwareProvider.this.mLocationSinkLock) {
                    if (FlpHardwareProvider.this.mLocationSink != null) {
                        Log.e(FlpHardwareProvider.TAG, "Replacing an existing IFusedLocationHardware sink");
                    }
                    FlpHardwareProvider.this.mLocationSink = eventSink;
                }
                FlpHardwareProvider.this.maybeSendCapabilities();
            }

            public void unregisterSink(IFusedLocationHardwareSink eventSink) {
                synchronized (FlpHardwareProvider.this.mLocationSinkLock) {
                    if (FlpHardwareProvider.this.mLocationSink == eventSink) {
                        FlpHardwareProvider.this.mLocationSink = null;
                    }
                }
            }

            public int getSupportedBatchSize() {
                return FlpHardwareProvider.this.nativeGetBatchSize();
            }

            public void startBatching(int requestId, FusedBatchOptions options) {
                Log.d(FlpHardwareProvider.TAG, "startBatching requestId=" + requestId);
                FlpHardwareProvider.this.nativeStartBatching(requestId, options);
            }

            public void stopBatching(int requestId) {
                Log.d(FlpHardwareProvider.TAG, "stopBatching requestId=" + requestId);
                FlpHardwareProvider.this.nativeStopBatching(requestId);
            }

            public void updateBatchingOptions(int requestId, FusedBatchOptions options) {
                FlpHardwareProvider.this.nativeUpdateBatchingOptions(requestId, options);
            }

            public void requestBatchOfLocations(int batchSizeRequested) {
                FlpHardwareProvider.this.nativeRequestBatchedLocation(batchSizeRequested);
            }

            public void flushBatchedLocations() {
                if (getVersion() >= 2) {
                    FlpHardwareProvider.this.nativeFlushBatchedLocations();
                } else {
                    Log.wtf(FlpHardwareProvider.TAG, "Tried to call flushBatchedLocations on an unsupported implementation");
                }
            }

            public boolean supportsDiagnosticDataInjection() {
                return FlpHardwareProvider.this.nativeIsDiagnosticSupported();
            }

            public void injectDiagnosticData(String data) {
                FlpHardwareProvider.this.nativeInjectDiagnosticData(data);
            }

            public boolean supportsDeviceContextInjection() {
                return FlpHardwareProvider.this.nativeIsDeviceContextSupported();
            }

            public void injectDeviceContext(int deviceEnabledContext) {
                FlpHardwareProvider.this.nativeInjectDeviceContext(deviceEnabledContext);
            }

            public int getVersion() {
                return FlpHardwareProvider.this.getVersion();
            }
        };
        this.mGeofenceHardwareService = new IFusedGeofenceHardware.Stub() {
            public boolean isSupported() {
                return FlpHardwareProvider.this.nativeIsGeofencingSupported();
            }

            public void addGeofences(GeofenceHardwareRequestParcelable[] geofenceRequestsArray) {
                Log.d(FlpHardwareProvider.TAG, "addGeofences geofenceRequestsArray=" + geofenceRequestsArray);
                FlpHardwareProvider.this.nativeAddGeofences(geofenceRequestsArray);
            }

            public void removeGeofences(int[] geofenceIds) {
                Log.d(FlpHardwareProvider.TAG, "removeGeofences geofenceIds=" + geofenceIds);
                FlpHardwareProvider.this.nativeRemoveGeofences(geofenceIds);
            }

            public void pauseMonitoringGeofence(int geofenceId) {
                FlpHardwareProvider.this.nativePauseGeofence(geofenceId);
            }

            public void resumeMonitoringGeofence(int geofenceId, int monitorTransitions) {
                FlpHardwareProvider.this.nativeResumeGeofence(geofenceId, monitorTransitions);
            }

            public void modifyGeofenceOptions(int geofenceId, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer, int sourcesToUse) {
                FlpHardwareProvider.this.nativeModifyGeofenceOption(geofenceId, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer, sourcesToUse);
            }
        };
        this.mContext = context;
        LocationManager manager = (LocationManager) this.mContext.getSystemService("location");
        LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, OppoBrightUtils.MIN_LUX_LIMITI, false);
        request.setHideFromAppOps(true);
        manager.requestLocationUpdates(request, new NetworkLocationListener(this, null), Looper.myLooper());
    }

    public static boolean isSupported() {
        return nativeIsSupported();
    }

    private void onLocationReport(Location[] locations) {
        IFusedLocationHardwareSink sink;
        for (Location location : locations) {
            location.setProvider("fused");
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        synchronized (this.mLocationSinkLock) {
            sink = this.mLocationSink;
        }
        if (sink != null) {
            try {
                sink.onLocationAvailable(locations);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onLocationAvailable");
            }
        }
    }

    private void onBatchingCapabilities(int capabilities) {
        synchronized (this.mLocationSinkLock) {
            Log.d(TAG, "onBatchingCapabilities capabilities= " + capabilities);
            this.mHaveBatchingCapabilities = true;
            this.mBatchingCapabilities = capabilities;
        }
        maybeSendCapabilities();
        if (this.mGeofenceHardwareSink != null) {
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
    }

    private void onBatchingStatus(int status) {
        IFusedLocationHardwareSink sink;
        synchronized (this.mLocationSinkLock) {
            sink = this.mLocationSink;
        }
        if (sink != null) {
            try {
                sink.onStatusChanged(status);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onBatchingStatus");
            }
        }
    }

    private int getVersion() {
        synchronized (this.mLocationSinkLock) {
            Log.d(TAG, "getVersion mHaveBatchingCapabilities = " + this.mHaveBatchingCapabilities);
            Log.d(TAG, "getVersion mVersion = " + this.mVersion);
            if (this.mHaveBatchingCapabilities) {
                int i = this.mVersion;
                return i;
            }
            return 1;
        }
    }

    private void setVersion(int version) {
        synchronized (this.mLocationSinkLock) {
            this.mVersion = version;
        }
        if (this.mGeofenceHardwareSink != null) {
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
    }

    private void maybeSendCapabilities() {
        IFusedLocationHardwareSink sink;
        int batchingCapabilities;
        synchronized (this.mLocationSinkLock) {
            sink = this.mLocationSink;
            boolean haveBatchingCapabilities = this.mHaveBatchingCapabilities;
            batchingCapabilities = this.mBatchingCapabilities;
        }
        if (sink != null && haveBatchingCapabilities) {
            try {
                sink.onCapabilities(batchingCapabilities);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onLocationAvailable");
            }
        }
    }

    private void onDataReport(String data) {
        IFusedLocationHardwareSink sink;
        synchronized (this.mLocationSinkLock) {
            sink = this.mLocationSink;
        }
        try {
            if (this.mLocationSink != null) {
                sink.onDiagnosticDataAvailable(data);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling onDiagnosticDataAvailable");
        }
    }

    private void onGeofenceTransition(int geofenceId, Location location, int transition, long timestamp, int sourcesUsed) {
        getGeofenceHardwareSink().reportGeofenceTransition(geofenceId, updateLocationInformation(location), transition, timestamp, 1, sourcesUsed);
    }

    private void onGeofenceMonitorStatus(int status, int source, Location location) {
        int monitorStatus;
        Location updatedLocation = null;
        if (location != null) {
            updatedLocation = updateLocationInformation(location);
        }
        switch (status) {
            case 1:
                monitorStatus = 1;
                break;
            case 2:
                monitorStatus = 0;
                break;
            default:
                Log.e(TAG, "Invalid FlpHal Geofence monitor status: " + status);
                monitorStatus = 1;
                break;
        }
        getGeofenceHardwareSink().reportGeofenceMonitorStatus(1, monitorStatus, updatedLocation, source);
    }

    private void onGeofenceAdd(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceAddStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofenceRemove(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceRemoveStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofencePause(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofencePauseStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofenceResume(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceResumeStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofencingCapabilities(int capabilities) {
        getGeofenceHardwareSink().onCapabilities(capabilities);
    }

    public IFusedLocationHardware getLocationHardware() {
        return this.mLocationHardware;
    }

    public IFusedGeofenceHardware getGeofenceHardware() {
        return this.mGeofenceHardwareService;
    }

    public void cleanup() {
        Log.i(TAG, "Calling nativeCleanup()");
        nativeCleanup();
    }

    private GeofenceHardwareImpl getGeofenceHardwareSink() {
        if (this.mGeofenceHardwareSink == null) {
            this.mGeofenceHardwareSink = GeofenceHardwareImpl.getInstance(this.mContext);
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
        return this.mGeofenceHardwareSink;
    }

    private static int translateToGeofenceHardwareStatus(int flpHalResult) {
        switch (flpHalResult) {
            case -6:
                return 4;
            case -5:
                return 3;
            case -4:
                return 2;
            case -3:
                return 1;
            case -2:
                return 6;
            case -1:
                return 5;
            case 0:
                return 0;
            default:
                String str = TAG;
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(flpHalResult);
                Log.e(str, String.format("Invalid FlpHal result code: %d", objArr));
                return 5;
        }
    }

    private Location updateLocationInformation(Location location) {
        location.setProvider("fused");
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return location;
    }
}

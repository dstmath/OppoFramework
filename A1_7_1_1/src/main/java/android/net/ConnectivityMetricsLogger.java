package android.net;

import android.app.PendingIntent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.net.IConnectivityMetricsLogger.Stub;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

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
public class ConnectivityMetricsLogger {
    public static final int COMPONENT_TAG_BLUETOOTH = 1;
    public static final int COMPONENT_TAG_CONNECTIVITY = 0;
    public static final int COMPONENT_TAG_TELECOM = 3;
    public static final int COMPONENT_TAG_TELEPHONY = 4;
    public static final int COMPONENT_TAG_WIFI = 2;
    public static final String CONNECTIVITY_METRICS_LOGGER_SERVICE = "connectivity_metrics_logger";
    public static final String DATA_KEY_EVENTS_COUNT = "count";
    private static final boolean DBG = true;
    public static final int NUMBER_OF_COMPONENTS = 5;
    private static String TAG = null;
    public static final int TAG_SKIPPED_EVENTS = -1;
    private int mNumSkippedEvents;
    protected IConnectivityMetricsLogger mService;
    protected volatile long mServiceUnblockedTimestampMillis;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.ConnectivityMetricsLogger.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.ConnectivityMetricsLogger.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ConnectivityMetricsLogger.<clinit>():void");
    }

    public ConnectivityMetricsLogger() {
        this(Stub.asInterface(ServiceManager.getService(CONNECTIVITY_METRICS_LOGGER_SERVICE)));
    }

    public ConnectivityMetricsLogger(IConnectivityMetricsLogger service) {
        this.mService = service;
    }

    protected boolean checkLoggerService() {
        boolean z = true;
        if (this.mService != null) {
            return true;
        }
        this.mService = Stub.asInterface(ServiceManager.getService(CONNECTIVITY_METRICS_LOGGER_SERVICE));
        if (this.mService == null) {
            z = false;
        }
        return z;
    }

    public void logEvent(long timestamp, int componentTag, int eventTag, Parcelable data) {
        if (this.mService == null) {
            Log.d(TAG, "logEvent(" + componentTag + "," + eventTag + ") Service not ready");
        } else if (this.mServiceUnblockedTimestampMillis <= 0 || System.currentTimeMillis() >= this.mServiceUnblockedTimestampMillis) {
            long result;
            ConnectivityMetricsEvent skippedEventsEvent = null;
            if (this.mNumSkippedEvents > 0) {
                Bundle b = new Bundle();
                b.putInt(DATA_KEY_EVENTS_COUNT, this.mNumSkippedEvents);
                skippedEventsEvent = new ConnectivityMetricsEvent(this.mServiceUnblockedTimestampMillis, componentTag, -1, b);
                this.mServiceUnblockedTimestampMillis = 0;
            }
            ConnectivityMetricsEvent event = new ConnectivityMetricsEvent(timestamp, componentTag, eventTag, data);
            if (skippedEventsEvent == null) {
                try {
                    result = this.mService.logEvent(event);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error logging event", e);
                }
            } else {
                IConnectivityMetricsLogger iConnectivityMetricsLogger = this.mService;
                ConnectivityMetricsEvent[] connectivityMetricsEventArr = new ConnectivityMetricsEvent[2];
                connectivityMetricsEventArr[0] = skippedEventsEvent;
                connectivityMetricsEventArr[1] = event;
                result = iConnectivityMetricsLogger.logEvents(connectivityMetricsEventArr);
            }
            if (result == 0) {
                this.mNumSkippedEvents = 0;
            } else {
                this.mNumSkippedEvents++;
                if (result > 0) {
                    this.mServiceUnblockedTimestampMillis = result;
                }
            }
        } else {
            this.mNumSkippedEvents++;
        }
    }

    public ConnectivityMetricsEvent[] getEvents(Reference reference) {
        try {
            return this.mService.getEvents(reference);
        } catch (RemoteException e) {
            Log.e(TAG, "IConnectivityMetricsLogger.getEvents", e);
            return null;
        }
    }

    public boolean register(PendingIntent newEventsIntent) {
        try {
            return this.mService.register(newEventsIntent);
        } catch (RemoteException e) {
            Log.e(TAG, "IConnectivityMetricsLogger.register", e);
            return false;
        }
    }

    public boolean unregister(PendingIntent newEventsIntent) {
        try {
            this.mService.unregister(newEventsIntent);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "IConnectivityMetricsLogger.unregister", e);
            return false;
        }
    }
}

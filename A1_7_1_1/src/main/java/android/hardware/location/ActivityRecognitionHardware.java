package android.hardware.location;

import android.content.Context;
import android.hardware.location.IActivityRecognitionHardware.Stub;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Array;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ActivityRecognitionHardware extends Stub {
    private static final boolean DEBUG = false;
    private static final String ENFORCE_HW_PERMISSION_MESSAGE = "Permission 'android.permission.LOCATION_HARDWARE' not granted to access ActivityRecognitionHardware";
    private static final int EVENT_TYPE_COUNT = 3;
    private static final int EVENT_TYPE_DISABLED = 0;
    private static final int EVENT_TYPE_ENABLED = 1;
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int INVALID_ACTIVITY_TYPE = -1;
    private static final int NATIVE_SUCCESS_RESULT = 0;
    private static final String TAG = "ActivityRecognitionHW";
    private static ActivityRecognitionHardware sSingletonInstance;
    private static final Object sSingletonInstanceLock = null;
    private final Context mContext;
    private final SinkList mSinks;
    private final String[] mSupportedActivities;
    private final int mSupportedActivitiesCount;
    private final int[][] mSupportedActivitiesEnabledEvents;

    private static class Event {
        public int activity;
        public long timestamp;
        public int type;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ActivityRecognitionHardware.Event.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Event() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ActivityRecognitionHardware.Event.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ActivityRecognitionHardware.Event.<init>():void");
        }
    }

    private class SinkList extends RemoteCallbackList<IActivityRecognitionHardwareSink> {
        /* synthetic */ SinkList(ActivityRecognitionHardware this$0, SinkList sinkList) {
            this();
        }

        private SinkList() {
        }

        public void onCallbackDied(IActivityRecognitionHardwareSink callback) {
            int callbackCount = ActivityRecognitionHardware.this.mSinks.getRegisteredCallbackCount();
            if (ActivityRecognitionHardware.DEBUG) {
                Log.d(ActivityRecognitionHardware.TAG, "RegisteredCallbackCount: " + callbackCount);
            }
            if (callbackCount == 0) {
                for (int activity = 0; activity < ActivityRecognitionHardware.this.mSupportedActivitiesCount; activity++) {
                    for (int event = 0; event < 3; event++) {
                        disableActivityEventIfEnabled(activity, event);
                    }
                }
            }
        }

        private void disableActivityEventIfEnabled(int activityType, int eventType) {
            if (ActivityRecognitionHardware.this.mSupportedActivitiesEnabledEvents[activityType][eventType] == 1) {
                int result = ActivityRecognitionHardware.this.nativeDisableActivityEvent(activityType, eventType);
                ActivityRecognitionHardware.this.mSupportedActivitiesEnabledEvents[activityType][eventType] = 0;
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(activityType);
                objArr[1] = Integer.valueOf(eventType);
                objArr[2] = Integer.valueOf(result);
                Log.e(ActivityRecognitionHardware.TAG, String.format("DisableActivityEvent: activityType=%d, eventType=%d, result=%d", objArr));
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ActivityRecognitionHardware.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ActivityRecognitionHardware.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ActivityRecognitionHardware.<clinit>():void");
    }

    private static native void nativeClassInit();

    private native int nativeDisableActivityEvent(int i, int i2);

    private native int nativeEnableActivityEvent(int i, int i2, long j);

    private native int nativeFlush();

    private native String[] nativeGetSupportedActivities();

    private native void nativeInitialize();

    private static native boolean nativeIsSupported();

    private native void nativeRelease();

    private ActivityRecognitionHardware(Context context) {
        this.mSinks = new SinkList(this, null);
        nativeInitialize();
        this.mContext = context;
        this.mSupportedActivities = fetchSupportedActivities();
        this.mSupportedActivitiesCount = this.mSupportedActivities.length;
        Class cls = Integer.TYPE;
        int[] iArr = new int[2];
        iArr[0] = this.mSupportedActivitiesCount;
        iArr[1] = 3;
        this.mSupportedActivitiesEnabledEvents = (int[][]) Array.newInstance(cls, iArr);
    }

    public static ActivityRecognitionHardware getInstance(Context context) {
        ActivityRecognitionHardware activityRecognitionHardware;
        synchronized (sSingletonInstanceLock) {
            if (sSingletonInstance == null) {
                sSingletonInstance = new ActivityRecognitionHardware(context);
            }
            activityRecognitionHardware = sSingletonInstance;
        }
        return activityRecognitionHardware;
    }

    public static boolean isSupported() {
        return nativeIsSupported();
    }

    public String[] getSupportedActivities() {
        checkPermissions();
        return this.mSupportedActivities;
    }

    public boolean isActivitySupported(String activity) {
        checkPermissions();
        return getActivityType(activity) != -1;
    }

    public boolean registerSink(IActivityRecognitionHardwareSink sink) {
        checkPermissions();
        return this.mSinks.register(sink);
    }

    public boolean unregisterSink(IActivityRecognitionHardwareSink sink) {
        checkPermissions();
        return this.mSinks.unregister(sink);
    }

    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType == -1 || nativeEnableActivityEvent(activityType, eventType, reportLatencyNs) != 0) {
            return false;
        }
        this.mSupportedActivitiesEnabledEvents[activityType][eventType] = 1;
        return true;
    }

    public boolean disableActivityEvent(String activity, int eventType) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType == -1 || nativeDisableActivityEvent(activityType, eventType) != 0) {
            return false;
        }
        this.mSupportedActivitiesEnabledEvents[activityType][eventType] = 0;
        return true;
    }

    public boolean flush() {
        checkPermissions();
        if (nativeFlush() == 0) {
            return true;
        }
        return false;
    }

    private void onActivityChanged(Event[] events) {
        if (events == null || events.length == 0) {
            if (DEBUG) {
                Log.d(TAG, "No events to broadcast for onActivityChanged.");
            }
            return;
        }
        int i;
        int eventsLength = events.length;
        ActivityRecognitionEvent[] activityRecognitionEventArray = new ActivityRecognitionEvent[eventsLength];
        for (i = 0; i < eventsLength; i++) {
            Event event = events[i];
            activityRecognitionEventArray[i] = new ActivityRecognitionEvent(getActivityName(event.activity), event.type, event.timestamp);
        }
        ActivityChangedEvent activityChangedEvent = new ActivityChangedEvent(activityRecognitionEventArray);
        int size = this.mSinks.beginBroadcast();
        for (i = 0; i < size; i++) {
            try {
                ((IActivityRecognitionHardwareSink) this.mSinks.getBroadcastItem(i)).onActivityChanged(activityChangedEvent);
            } catch (RemoteException e) {
                Log.e(TAG, "Error delivering activity changed event.", e);
            }
        }
        this.mSinks.finishBroadcast();
    }

    private String getActivityName(int activityType) {
        if (activityType >= 0 && activityType < this.mSupportedActivities.length) {
            return this.mSupportedActivities[activityType];
        }
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(activityType);
        objArr[1] = Integer.valueOf(this.mSupportedActivities.length);
        Log.e(TAG, String.format("Invalid ActivityType: %d, SupportedActivities: %d", objArr));
        return null;
    }

    private int getActivityType(String activity) {
        if (TextUtils.isEmpty(activity)) {
            return -1;
        }
        int supportedActivitiesLength = this.mSupportedActivities.length;
        for (int i = 0; i < supportedActivitiesLength; i++) {
            if (activity.equals(this.mSupportedActivities[i])) {
                return i;
            }
        }
        return -1;
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", ENFORCE_HW_PERMISSION_MESSAGE);
    }

    private String[] fetchSupportedActivities() {
        String[] supportedActivities = nativeGetSupportedActivities();
        if (supportedActivities != null) {
            return supportedActivities;
        }
        return new String[0];
    }
}

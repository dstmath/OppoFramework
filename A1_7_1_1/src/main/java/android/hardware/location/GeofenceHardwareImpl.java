package android.hardware.location;

import android.Manifest.permission;
import android.content.Context;
import android.location.IFusedGeofenceHardware;
import android.location.IGpsGeofenceHardware;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IInterface;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class GeofenceHardwareImpl {
    private static final int ADD_GEOFENCE_CALLBACK = 2;
    private static final int CALLBACK_ADD = 2;
    private static final int CALLBACK_REMOVE = 3;
    private static final int CAPABILITY_GNSS = 1;
    private static final boolean DEBUG = true;
    private static final int FIRST_VERSION_WITH_CAPABILITIES = 2;
    private static final int GEOFENCE_CALLBACK_BINDER_DIED = 6;
    private static final int GEOFENCE_STATUS = 1;
    private static final int GEOFENCE_TRANSITION_CALLBACK = 1;
    private static final int LOCATION_HAS_ACCURACY = 16;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_INVALID = 0;
    private static final int MONITOR_CALLBACK_BINDER_DIED = 4;
    private static final int PAUSE_GEOFENCE_CALLBACK = 4;
    private static final int REAPER_GEOFENCE_ADDED = 1;
    private static final int REAPER_MONITOR_CALLBACK_ADDED = 2;
    private static final int REAPER_REMOVED = 3;
    private static final int REMOVE_GEOFENCE_CALLBACK = 3;
    private static final int RESOLUTION_LEVEL_COARSE = 2;
    private static final int RESOLUTION_LEVEL_FINE = 3;
    private static final int RESOLUTION_LEVEL_NONE = 1;
    private static final int RESUME_GEOFENCE_CALLBACK = 5;
    private static final String TAG = "GeofenceHardwareImpl";
    private static GeofenceHardwareImpl sInstance;
    private final ArrayList<IGeofenceHardwareMonitorCallback>[] mCallbacks;
    private Handler mCallbacksHandler;
    private int mCapabilities;
    private final Context mContext;
    private IFusedGeofenceHardware mFusedService;
    private Handler mGeofenceHandler;
    private final SparseArray<IGeofenceHardwareCallback> mGeofences;
    private IGpsGeofenceHardware mGpsService;
    private Handler mReaperHandler;
    private final ArrayList<Reaper> mReapers;
    private int[] mSupportedMonitorTypes;
    private int mVersion;
    private WakeLock mWakeLock;

    private class GeofenceTransition {
        private int mGeofenceId;
        private Location mLocation;
        private int mMonitoringType;
        private int mSourcesUsed;
        private long mTimestamp;
        private int mTransition;
        final /* synthetic */ GeofenceHardwareImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get0(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex:  in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get0(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get0(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ int m273-get0(android.hardware.location.GeofenceHardwareImpl.GeofenceTransition r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get0(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex:  in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get0(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get0(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get1(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):android.location.Location, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get1 */
        static /* synthetic */ android.location.Location m274-get1(android.hardware.location.GeofenceHardwareImpl.GeofenceTransition r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get1(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):android.location.Location, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get1(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):android.location.Location");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get2(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get2 */
        static /* synthetic */ int m275-get2(android.hardware.location.GeofenceHardwareImpl.GeofenceTransition r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get2(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get2(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get3(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get3 */
        static /* synthetic */ long m276-get3(android.hardware.location.GeofenceHardwareImpl.GeofenceTransition r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get3(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get3(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get4(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex:  in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get4(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get4(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get4 */
        static /* synthetic */ int m277-get4(android.hardware.location.GeofenceHardwareImpl.GeofenceTransition r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get4(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex:  in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get4(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.-get4(android.hardware.location.GeofenceHardwareImpl$GeofenceTransition):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.<init>(android.hardware.location.GeofenceHardwareImpl, int, int, long, android.location.Location, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        GeofenceTransition(android.hardware.location.GeofenceHardwareImpl r1, int r2, int r3, long r4, android.location.Location r6, int r7, int r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.<init>(android.hardware.location.GeofenceHardwareImpl, int, int, long, android.location.Location, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardwareImpl.GeofenceTransition.<init>(android.hardware.location.GeofenceHardwareImpl, int, int, long, android.location.Location, int, int):void");
        }
    }

    class Reaper implements DeathRecipient {
        private IGeofenceHardwareCallback mCallback;
        private IGeofenceHardwareMonitorCallback mMonitorCallback;
        private int mMonitoringType;
        final /* synthetic */ GeofenceHardwareImpl this$0;

        Reaper(GeofenceHardwareImpl this$0, IGeofenceHardwareCallback c, int monitoringType) {
            this.this$0 = this$0;
            this.mCallback = c;
            this.mMonitoringType = monitoringType;
        }

        Reaper(GeofenceHardwareImpl this$0, IGeofenceHardwareMonitorCallback c, int monitoringType) {
            this.this$0 = this$0;
            this.mMonitorCallback = c;
            this.mMonitoringType = monitoringType;
        }

        public void binderDied() {
            Message m;
            if (this.mCallback != null) {
                m = this.this$0.mGeofenceHandler.obtainMessage(6, this.mCallback);
                m.arg1 = this.mMonitoringType;
                this.this$0.mGeofenceHandler.sendMessage(m);
            } else if (this.mMonitorCallback != null) {
                m = this.this$0.mCallbacksHandler.obtainMessage(4, this.mMonitorCallback);
                m.arg1 = this.mMonitoringType;
                this.this$0.mCallbacksHandler.sendMessage(m);
            }
            this.this$0.mReaperHandler.sendMessage(this.this$0.mReaperHandler.obtainMessage(3, this));
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            if (this.mCallback != null) {
                hashCode = this.mCallback.asBinder().hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (hashCode + 527) * 31;
            if (this.mMonitorCallback != null) {
                i = this.mMonitorCallback.asBinder().hashCode();
            }
            return ((hashCode + i) * 31) + this.mMonitoringType;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            Reaper rhs = (Reaper) obj;
            if (!binderEquals(rhs.mCallback, this.mCallback) || !binderEquals(rhs.mMonitorCallback, this.mMonitorCallback)) {
                z = false;
            } else if (rhs.mMonitoringType != this.mMonitoringType) {
                z = false;
            }
            return z;
        }

        private boolean binderEquals(IInterface left, IInterface right) {
            boolean z = true;
            boolean z2 = false;
            if (left == null) {
                if (right != null) {
                    z = false;
                }
                return z;
            }
            if (right != null && left.asBinder() == right.asBinder()) {
                z2 = true;
            }
            return z2;
        }

        private boolean unlinkToDeath() {
            if (this.mMonitorCallback != null) {
                return this.mMonitorCallback.asBinder().unlinkToDeath(this, 0);
            }
            if (this.mCallback != null) {
                return this.mCallback.asBinder().unlinkToDeath(this, 0);
            }
            return true;
        }

        private boolean callbackEquals(IGeofenceHardwareCallback cb) {
            return this.mCallback != null && this.mCallback.asBinder() == cb.asBinder();
        }
    }

    public static synchronized GeofenceHardwareImpl getInstance(Context context) {
        GeofenceHardwareImpl geofenceHardwareImpl;
        synchronized (GeofenceHardwareImpl.class) {
            if (sInstance == null) {
                sInstance = new GeofenceHardwareImpl(context);
            }
            geofenceHardwareImpl = sInstance;
        }
        return geofenceHardwareImpl;
    }

    private GeofenceHardwareImpl(Context context) {
        this.mGeofences = new SparseArray();
        this.mCallbacks = new ArrayList[2];
        this.mReapers = new ArrayList();
        this.mVersion = 1;
        this.mSupportedMonitorTypes = new int[2];
        this.mGeofenceHandler = new Handler() {
            public void handleMessage(Message msg) {
                IGeofenceHardwareCallback callback;
                int geofenceId;
                int i;
                switch (msg.what) {
                    case 1:
                        GeofenceTransition geofenceTransition = msg.obj;
                        synchronized (GeofenceHardwareImpl.this.mGeofences) {
                            callback = (IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.get(GeofenceTransition.m273-get0(geofenceTransition));
                            Log.d(GeofenceHardwareImpl.TAG, "GeofenceTransistionCallback: GPS : GeofenceId: " + GeofenceTransition.m273-get0(geofenceTransition) + " Transition: " + GeofenceTransition.m277-get4(geofenceTransition) + " Location: " + GeofenceTransition.m274-get1(geofenceTransition) + ":" + GeofenceHardwareImpl.this.mGeofences);
                        }
                        if (callback != null) {
                            try {
                                callback.onGeofenceTransition(GeofenceTransition.m273-get0(geofenceTransition), GeofenceTransition.m277-get4(geofenceTransition), GeofenceTransition.m274-get1(geofenceTransition), GeofenceTransition.m276-get3(geofenceTransition), GeofenceTransition.m275-get2(geofenceTransition));
                            } catch (RemoteException e) {
                            }
                        }
                        GeofenceHardwareImpl.this.releaseWakeLock();
                        return;
                    case 2:
                        geofenceId = msg.arg1;
                        Log.d(GeofenceHardwareImpl.TAG, "handle add geofence callback id:" + geofenceId);
                        synchronized (GeofenceHardwareImpl.this.mGeofences) {
                            callback = (IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.get(geofenceId);
                        }
                        if (callback != null) {
                            try {
                                callback.onGeofenceAdd(geofenceId, msg.arg2);
                            } catch (RemoteException e2) {
                                Log.i(GeofenceHardwareImpl.TAG, "Remote Exception:" + e2);
                            }
                        }
                        GeofenceHardwareImpl.this.releaseWakeLock();
                        Log.d(GeofenceHardwareImpl.TAG, "handle add fence done");
                        return;
                    case 3:
                        geofenceId = msg.arg1;
                        Log.d(GeofenceHardwareImpl.TAG, "handle remove geofence callback id:" + geofenceId);
                        synchronized (GeofenceHardwareImpl.this.mGeofences) {
                            callback = (IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.get(geofenceId);
                        }
                        if (callback != null) {
                            IBinder callbackBinder = callback.asBinder();
                            boolean callbackInUse = false;
                            synchronized (GeofenceHardwareImpl.this.mGeofences) {
                                GeofenceHardwareImpl.this.mGeofences.remove(geofenceId);
                                i = 0;
                                while (i < GeofenceHardwareImpl.this.mGeofences.size()) {
                                    if (((IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.valueAt(i)).asBinder() == callbackBinder) {
                                        callbackInUse = true;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            if (!callbackInUse) {
                                Iterator<Reaper> iterator = GeofenceHardwareImpl.this.mReapers.iterator();
                                while (iterator.hasNext()) {
                                    Reaper reaper = (Reaper) iterator.next();
                                    if (reaper.mCallback != null && reaper.mCallback.asBinder() == callbackBinder) {
                                        iterator.remove();
                                        reaper.unlinkToDeath();
                                        Log.d(GeofenceHardwareImpl.TAG, String.format("Removed reaper %s because binder %s is no longer needed.", new Object[]{reaper, callbackBinder}));
                                    }
                                }
                            }
                            try {
                                callback.onGeofenceRemove(geofenceId, msg.arg2);
                            } catch (RemoteException e3) {
                            }
                        }
                        GeofenceHardwareImpl.this.releaseWakeLock();
                        Log.d(GeofenceHardwareImpl.TAG, "handle remove geofence done");
                        return;
                    case 4:
                        geofenceId = msg.arg1;
                        synchronized (GeofenceHardwareImpl.this.mGeofences) {
                            callback = (IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.get(geofenceId);
                        }
                        if (callback != null) {
                            try {
                                callback.onGeofencePause(geofenceId, msg.arg2);
                            } catch (RemoteException e4) {
                            }
                        }
                        GeofenceHardwareImpl.this.releaseWakeLock();
                        return;
                    case 5:
                        geofenceId = msg.arg1;
                        synchronized (GeofenceHardwareImpl.this.mGeofences) {
                            callback = (IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.get(geofenceId);
                        }
                        if (callback != null) {
                            try {
                                callback.onGeofenceResume(geofenceId, msg.arg2);
                            } catch (RemoteException e5) {
                            }
                        }
                        GeofenceHardwareImpl.this.releaseWakeLock();
                        return;
                    case 6:
                        callback = (IGeofenceHardwareCallback) msg.obj;
                        Log.d(GeofenceHardwareImpl.TAG, "Geofence callback reaped:" + callback);
                        int monitoringType = msg.arg1;
                        synchronized (GeofenceHardwareImpl.this.mGeofences) {
                            for (i = 0; i < GeofenceHardwareImpl.this.mGeofences.size(); i++) {
                                if (((IGeofenceHardwareCallback) GeofenceHardwareImpl.this.mGeofences.valueAt(i)).equals(callback)) {
                                    geofenceId = GeofenceHardwareImpl.this.mGeofences.keyAt(i);
                                    GeofenceHardwareImpl.this.removeGeofence(GeofenceHardwareImpl.this.mGeofences.keyAt(i), monitoringType);
                                    GeofenceHardwareImpl.this.mGeofences.remove(geofenceId);
                                }
                            }
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mCallbacksHandler = new Handler() {
            public void handleMessage(Message msg) {
                ArrayList<IGeofenceHardwareMonitorCallback> callbackList;
                IGeofenceHardwareMonitorCallback callback;
                switch (msg.what) {
                    case 1:
                        GeofenceHardwareMonitorEvent event = msg.obj;
                        callbackList = GeofenceHardwareImpl.this.mCallbacks[event.getMonitoringType()];
                        if (callbackList != null) {
                            Log.d(GeofenceHardwareImpl.TAG, "MonitoringSystemChangeCallback: " + event);
                            for (IGeofenceHardwareMonitorCallback c : callbackList) {
                                try {
                                    c.onMonitoringSystemChange(event);
                                } catch (RemoteException e) {
                                    Log.d(GeofenceHardwareImpl.TAG, "Error reporting onMonitoringSystemChange.", e);
                                }
                            }
                        }
                        GeofenceHardwareImpl.this.releaseWakeLock();
                        return;
                    case 2:
                        int monitoringType = msg.arg1;
                        callback = msg.obj;
                        callbackList = GeofenceHardwareImpl.this.mCallbacks[monitoringType];
                        if (callbackList == null) {
                            callbackList = new ArrayList();
                            GeofenceHardwareImpl.this.mCallbacks[monitoringType] = callbackList;
                        }
                        if (!callbackList.contains(callback)) {
                            callbackList.add(callback);
                            return;
                        }
                        return;
                    case 3:
                        callback = (IGeofenceHardwareMonitorCallback) msg.obj;
                        callbackList = GeofenceHardwareImpl.this.mCallbacks[msg.arg1];
                        if (callbackList != null) {
                            callbackList.remove(callback);
                            return;
                        }
                        return;
                    case 4:
                        callback = (IGeofenceHardwareMonitorCallback) msg.obj;
                        Log.d(GeofenceHardwareImpl.TAG, "Monitor callback reaped:" + callback);
                        callbackList = GeofenceHardwareImpl.this.mCallbacks[msg.arg1];
                        if (callbackList != null && callbackList.contains(callback)) {
                            callbackList.remove(callback);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mReaperHandler = new Handler() {
            public void handleMessage(Message msg) {
                Reaper r;
                switch (msg.what) {
                    case 1:
                        IGeofenceHardwareCallback callback = msg.obj;
                        r = new Reaper(GeofenceHardwareImpl.this, callback, msg.arg1);
                        if (!GeofenceHardwareImpl.this.mReapers.contains(r)) {
                            GeofenceHardwareImpl.this.mReapers.add(r);
                            try {
                                callback.asBinder().linkToDeath(r, 0);
                                return;
                            } catch (RemoteException e) {
                                return;
                            }
                        }
                        return;
                    case 2:
                        IGeofenceHardwareMonitorCallback monitorCallback = msg.obj;
                        r = new Reaper(GeofenceHardwareImpl.this, monitorCallback, msg.arg1);
                        if (!GeofenceHardwareImpl.this.mReapers.contains(r)) {
                            GeofenceHardwareImpl.this.mReapers.add(r);
                            try {
                                monitorCallback.asBinder().linkToDeath(r, 0);
                                return;
                            } catch (RemoteException e2) {
                                return;
                            }
                        }
                        return;
                    case 3:
                        GeofenceHardwareImpl.this.mReapers.remove((Reaper) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mContext = context;
        setMonitorAvailability(0, 2);
        setMonitorAvailability(1, 2);
    }

    private void acquireWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(1, TAG);
        }
        this.mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    private void updateGpsHardwareAvailability() {
        boolean gpsSupported;
        try {
            gpsSupported = this.mGpsService.isHardwareGeofenceSupported();
        } catch (RemoteException e) {
            Log.e(TAG, "Remote Exception calling LocationManagerService");
            gpsSupported = false;
        }
        if (gpsSupported) {
            setMonitorAvailability(0, 0);
        }
    }

    private void updateFusedHardwareAvailability() {
        boolean fusedSupported;
        try {
            boolean hasGnnsCapabilities = this.mVersion >= 2 ? (this.mCapabilities & 1) != 0 : true;
            fusedSupported = this.mFusedService != null ? this.mFusedService.isSupported() ? hasGnnsCapabilities : false : false;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling LocationManagerService");
            fusedSupported = false;
        }
        if (fusedSupported) {
            setMonitorAvailability(1, 0);
        }
    }

    public void setGpsHardwareGeofence(IGpsGeofenceHardware service) {
        if (this.mGpsService == null) {
            this.mGpsService = service;
            updateGpsHardwareAvailability();
        } else if (service == null) {
            this.mGpsService = null;
            Log.w(TAG, "GPS Geofence Hardware service seems to have crashed");
        } else {
            Log.e(TAG, "Error: GpsService being set again.");
        }
    }

    public void onCapabilities(int capabilities) {
        this.mCapabilities = capabilities;
        updateFusedHardwareAvailability();
    }

    public void setVersion(int version) {
        this.mVersion = version;
        updateFusedHardwareAvailability();
    }

    public void setFusedGeofenceHardware(IFusedGeofenceHardware service) {
        if (this.mFusedService == null) {
            this.mFusedService = service;
            updateFusedHardwareAvailability();
        } else if (service == null) {
            this.mFusedService = null;
            Log.w(TAG, "Fused Geofence Hardware service seems to have crashed");
        } else {
            Log.e(TAG, "Error: FusedService being set again");
        }
    }

    public int[] getMonitoringTypes() {
        boolean gpsSupported;
        boolean fusedSupported;
        synchronized (this.mSupportedMonitorTypes) {
            gpsSupported = this.mSupportedMonitorTypes[0] != 2;
            fusedSupported = this.mSupportedMonitorTypes[1] != 2;
        }
        if (gpsSupported) {
            if (fusedSupported) {
                return new int[]{0, 1};
            }
            return new int[]{0};
        } else if (!fusedSupported) {
            return new int[0];
        } else {
            return new int[]{1};
        }
    }

    public int getStatusOfMonitoringType(int monitoringType) {
        int i;
        synchronized (this.mSupportedMonitorTypes) {
            if (monitoringType >= this.mSupportedMonitorTypes.length || monitoringType < 0) {
                throw new IllegalArgumentException("Unknown monitoring type");
            }
            i = this.mSupportedMonitorTypes[monitoringType];
        }
        return i;
    }

    public int getCapabilitiesForMonitoringType(int monitoringType) {
        switch (this.mSupportedMonitorTypes[monitoringType]) {
            case 0:
                switch (monitoringType) {
                    case 0:
                        return 1;
                    case 1:
                        if (this.mVersion >= 2) {
                            return this.mCapabilities;
                        }
                        return 1;
                }
                break;
        }
        return 0;
    }

    public boolean addCircularFence(int monitoringType, GeofenceHardwareRequestParcelable request, IGeofenceHardwareCallback callback) {
        boolean result;
        int geofenceId = request.getId();
        Log.d(TAG, String.format("addCircularFence: monitoringType=%d, %s", new Object[]{Integer.valueOf(monitoringType), request}));
        synchronized (this.mGeofences) {
            this.mGeofences.put(geofenceId, callback);
        }
        switch (monitoringType) {
            case 0:
                if (this.mGpsService != null) {
                    try {
                        result = this.mGpsService.addCircularHardwareGeofence(request.getId(), request.getLatitude(), request.getLongitude(), request.getRadius(), request.getLastTransition(), request.getMonitorTransitions(), request.getNotificationResponsiveness(), request.getUnknownTimer());
                        break;
                    } catch (RemoteException e) {
                        Log.e(TAG, "AddGeofence: Remote Exception calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            case 1:
                if (this.mFusedService != null) {
                    try {
                        this.mFusedService.addGeofences(new GeofenceHardwareRequestParcelable[]{request});
                        result = true;
                        break;
                    } catch (RemoteException e2) {
                        Log.e(TAG, "AddGeofence: RemoteException calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            default:
                result = false;
                break;
        }
        if (result) {
            Message m = this.mReaperHandler.obtainMessage(1, callback);
            m.arg1 = monitoringType;
            this.mReaperHandler.sendMessage(m);
        } else {
            synchronized (this.mGeofences) {
                this.mGeofences.remove(geofenceId);
            }
        }
        Log.d(TAG, "addCircularFence: Result is: " + result);
        return result;
    }

    public boolean removeGeofence(int geofenceId, int monitoringType) {
        boolean result;
        Log.d(TAG, "Remove Geofence: GeofenceId: " + geofenceId);
        synchronized (this.mGeofences) {
            if (this.mGeofences.get(geofenceId) == null) {
                throw new IllegalArgumentException("Geofence " + geofenceId + " not registered.");
            }
        }
        switch (monitoringType) {
            case 0:
                if (this.mGpsService != null) {
                    try {
                        result = this.mGpsService.removeHardwareGeofence(geofenceId);
                        break;
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoveGeofence: Remote Exception calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            case 1:
                if (this.mFusedService != null) {
                    try {
                        this.mFusedService.removeGeofences(new int[]{geofenceId});
                        result = true;
                        break;
                    } catch (RemoteException e2) {
                        Log.e(TAG, "RemoveGeofence: RemoteException calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            default:
                result = false;
                break;
        }
        Log.d(TAG, "removeGeofence: Result is: " + result);
        return result;
    }

    public boolean pauseGeofence(int geofenceId, int monitoringType) {
        boolean result;
        Log.d(TAG, "Pause Geofence: GeofenceId: " + geofenceId);
        synchronized (this.mGeofences) {
            if (this.mGeofences.get(geofenceId) == null) {
                throw new IllegalArgumentException("Geofence " + geofenceId + " not registered.");
            }
        }
        switch (monitoringType) {
            case 0:
                if (this.mGpsService != null) {
                    try {
                        result = this.mGpsService.pauseHardwareGeofence(geofenceId);
                        break;
                    } catch (RemoteException e) {
                        Log.e(TAG, "PauseGeofence: Remote Exception calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            case 1:
                if (this.mFusedService != null) {
                    try {
                        this.mFusedService.pauseMonitoringGeofence(geofenceId);
                        result = true;
                        break;
                    } catch (RemoteException e2) {
                        Log.e(TAG, "PauseGeofence: RemoteException calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            default:
                result = false;
                break;
        }
        Log.d(TAG, "pauseGeofence: Result is: " + result);
        return result;
    }

    public boolean resumeGeofence(int geofenceId, int monitoringType, int monitorTransition) {
        boolean result;
        Log.d(TAG, "Resume Geofence: GeofenceId: " + geofenceId);
        synchronized (this.mGeofences) {
            if (this.mGeofences.get(geofenceId) == null) {
                throw new IllegalArgumentException("Geofence " + geofenceId + " not registered.");
            }
        }
        switch (monitoringType) {
            case 0:
                if (this.mGpsService != null) {
                    try {
                        result = this.mGpsService.resumeHardwareGeofence(geofenceId, monitorTransition);
                        break;
                    } catch (RemoteException e) {
                        Log.e(TAG, "ResumeGeofence: Remote Exception calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            case 1:
                if (this.mFusedService != null) {
                    try {
                        this.mFusedService.resumeMonitoringGeofence(geofenceId, monitorTransition);
                        result = true;
                        break;
                    } catch (RemoteException e2) {
                        Log.e(TAG, "ResumeGeofence: RemoteException calling LocationManagerService");
                        result = false;
                        break;
                    }
                }
                return false;
            default:
                result = false;
                break;
        }
        Log.d(TAG, "resumeGeofence: Result is: " + result);
        return result;
    }

    public boolean registerForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) {
        Message reaperMessage = this.mReaperHandler.obtainMessage(2, callback);
        reaperMessage.arg1 = monitoringType;
        this.mReaperHandler.sendMessage(reaperMessage);
        Message m = this.mCallbacksHandler.obtainMessage(2, callback);
        m.arg1 = monitoringType;
        this.mCallbacksHandler.sendMessage(m);
        return true;
    }

    public boolean unregisterForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) {
        Message m = this.mCallbacksHandler.obtainMessage(3, callback);
        m.arg1 = monitoringType;
        this.mCallbacksHandler.sendMessage(m);
        return true;
    }

    public void reportGeofenceTransition(int geofenceId, Location location, int transition, long transitionTimestamp, int monitoringType, int sourcesUsed) {
        if (location == null) {
            Log.e(TAG, String.format("Invalid Geofence Transition: location=null", new Object[0]));
            return;
        }
        Log.d(TAG, "GeofenceTransition| " + location + ", transition:" + transition + ", transitionTimestamp:" + transitionTimestamp + ", monitoringType:" + monitoringType + ", sourcesUsed:" + sourcesUsed);
        GeofenceTransition geofenceTransition = new GeofenceTransition(this, geofenceId, transition, transitionTimestamp, location, monitoringType, sourcesUsed);
        acquireWakeLock();
        this.mGeofenceHandler.obtainMessage(1, geofenceTransition).sendToTarget();
    }

    public void reportGeofenceMonitorStatus(int monitoringType, int monitoringStatus, Location location, int source) {
        setMonitorAvailability(monitoringType, monitoringStatus);
        acquireWakeLock();
        this.mCallbacksHandler.obtainMessage(1, new GeofenceHardwareMonitorEvent(monitoringType, monitoringStatus, source, location)).sendToTarget();
    }

    private void reportGeofenceOperationStatus(int operation, int geofenceId, int operationStatus) {
        acquireWakeLock();
        Message message = this.mGeofenceHandler.obtainMessage(operation);
        message.arg1 = geofenceId;
        message.arg2 = operationStatus;
        message.sendToTarget();
    }

    public void reportGeofenceAddStatus(int geofenceId, int status) {
        Log.d(TAG, "AddCallback| id:" + geofenceId + ", status:" + status);
        reportGeofenceOperationStatus(2, geofenceId, status);
    }

    public void reportGeofenceRemoveStatus(int geofenceId, int status) {
        Log.d(TAG, "RemoveCallback| id:" + geofenceId + ", status:" + status);
        reportGeofenceOperationStatus(3, geofenceId, status);
    }

    public void reportGeofencePauseStatus(int geofenceId, int status) {
        Log.d(TAG, "PauseCallbac| id:" + geofenceId + ", status" + status);
        reportGeofenceOperationStatus(4, geofenceId, status);
    }

    public void reportGeofenceResumeStatus(int geofenceId, int status) {
        Log.d(TAG, "ResumeCallback| id:" + geofenceId + ", status:" + status);
        reportGeofenceOperationStatus(5, geofenceId, status);
    }

    private void setMonitorAvailability(int monitor, int val) {
        synchronized (this.mSupportedMonitorTypes) {
            this.mSupportedMonitorTypes[monitor] = val;
        }
    }

    int getMonitoringResolutionLevel(int monitoringType) {
        switch (monitoringType) {
            case 0:
                return 3;
            case 1:
                return 3;
            default:
                return 1;
        }
    }

    int getAllowedResolutionLevel(int pid, int uid) {
        if (this.mContext.checkPermission(permission.ACCESS_FINE_LOCATION, pid, uid) == 0) {
            return 3;
        }
        if (this.mContext.checkPermission(permission.ACCESS_COARSE_LOCATION, pid, uid) == 0) {
            return 2;
        }
        return 1;
    }
}

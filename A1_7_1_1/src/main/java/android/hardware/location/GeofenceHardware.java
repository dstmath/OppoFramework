package android.hardware.location;

import android.hardware.location.IGeofenceHardwareCallback.Stub;
import android.os.Build.VERSION;
import android.os.RemoteException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

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
public final class GeofenceHardware {
    public static final int GEOFENCE_ENTERED = 1;
    public static final int GEOFENCE_ERROR_ID_EXISTS = 2;
    public static final int GEOFENCE_ERROR_ID_UNKNOWN = 3;
    public static final int GEOFENCE_ERROR_INSUFFICIENT_MEMORY = 6;
    public static final int GEOFENCE_ERROR_INVALID_TRANSITION = 4;
    public static final int GEOFENCE_ERROR_TOO_MANY_GEOFENCES = 1;
    public static final int GEOFENCE_EXITED = 2;
    public static final int GEOFENCE_FAILURE = 5;
    public static final int GEOFENCE_SUCCESS = 0;
    public static final int GEOFENCE_UNCERTAIN = 4;
    public static final int MONITORING_TYPE_FUSED_HARDWARE = 1;
    public static final int MONITORING_TYPE_GPS_HARDWARE = 0;
    public static final int MONITOR_CURRENTLY_AVAILABLE = 0;
    public static final int MONITOR_CURRENTLY_UNAVAILABLE = 1;
    public static final int MONITOR_UNSUPPORTED = 2;
    static final int NUM_MONITORS = 2;
    public static final int SOURCE_TECHNOLOGY_BLUETOOTH = 16;
    public static final int SOURCE_TECHNOLOGY_CELL = 8;
    public static final int SOURCE_TECHNOLOGY_GNSS = 1;
    public static final int SOURCE_TECHNOLOGY_SENSORS = 4;
    public static final int SOURCE_TECHNOLOGY_WIFI = 2;
    private HashMap<GeofenceHardwareCallback, GeofenceHardwareCallbackWrapper> mCallbacks;
    private HashMap<GeofenceHardwareMonitorCallback, GeofenceHardwareMonitorCallbackWrapper> mMonitorCallbacks;
    private IGeofenceHardware mService;

    class GeofenceHardwareCallbackWrapper extends Stub {
        private WeakReference<GeofenceHardwareCallback> mCallback;
        final /* synthetic */ GeofenceHardware this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.<init>(android.hardware.location.GeofenceHardware, android.hardware.location.GeofenceHardwareCallback):void, dex:  in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.<init>(android.hardware.location.GeofenceHardware, android.hardware.location.GeofenceHardwareCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.<init>(android.hardware.location.GeofenceHardware, android.hardware.location.GeofenceHardwareCallback):void, dex: 
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
        GeofenceHardwareCallbackWrapper(android.hardware.location.GeofenceHardware r1, android.hardware.location.GeofenceHardwareCallback r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.<init>(android.hardware.location.GeofenceHardware, android.hardware.location.GeofenceHardwareCallback):void, dex:  in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.<init>(android.hardware.location.GeofenceHardware, android.hardware.location.GeofenceHardwareCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.<init>(android.hardware.location.GeofenceHardware, android.hardware.location.GeofenceHardwareCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceAdd(int, int):void, dex: 
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
        public void onGeofenceAdd(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceAdd(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceAdd(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofencePause(int, int):void, dex: 
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
        public void onGeofencePause(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofencePause(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofencePause(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceRemove(int, int):void, dex: 
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
        public void onGeofenceRemove(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceRemove(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceRemove(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceResume(int, int):void, dex: 
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
        public void onGeofenceResume(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceResume(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceResume(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceTransition(int, int, android.location.Location, long, int):void, dex: 
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
        public void onGeofenceTransition(int r1, int r2, android.location.Location r3, long r4, int r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceTransition(int, int, android.location.Location, long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.GeofenceHardware.GeofenceHardwareCallbackWrapper.onGeofenceTransition(int, int, android.location.Location, long, int):void");
        }
    }

    class GeofenceHardwareMonitorCallbackWrapper extends IGeofenceHardwareMonitorCallback.Stub {
        private WeakReference<GeofenceHardwareMonitorCallback> mCallback;
        final /* synthetic */ GeofenceHardware this$0;

        GeofenceHardwareMonitorCallbackWrapper(GeofenceHardware this$0, GeofenceHardwareMonitorCallback c) {
            this.this$0 = this$0;
            this.mCallback = new WeakReference(c);
        }

        public void onMonitoringSystemChange(GeofenceHardwareMonitorEvent event) {
            boolean z = false;
            GeofenceHardwareMonitorCallback c = (GeofenceHardwareMonitorCallback) this.mCallback.get();
            if (c != null) {
                int monitoringType = event.getMonitoringType();
                if (event.getMonitoringStatus() == 0) {
                    z = true;
                }
                c.onMonitoringSystemChange(monitoringType, z, event.getLocation());
                if (VERSION.SDK_INT >= 21) {
                    c.onMonitoringSystemChange(event);
                }
            }
        }
    }

    public GeofenceHardware(IGeofenceHardware service) {
        this.mCallbacks = new HashMap();
        this.mMonitorCallbacks = new HashMap();
        this.mService = service;
    }

    public int[] getMonitoringTypes() {
        try {
            return this.mService.getMonitoringTypes();
        } catch (RemoteException e) {
            return new int[0];
        }
    }

    public int getStatusOfMonitoringType(int monitoringType) {
        try {
            return this.mService.getStatusOfMonitoringType(monitoringType);
        } catch (RemoteException e) {
            return 2;
        }
    }

    public boolean addGeofence(int geofenceId, int monitoringType, GeofenceHardwareRequest geofenceRequest, GeofenceHardwareCallback callback) {
        try {
            if (geofenceRequest.getType() == 0) {
                return this.mService.addCircularFence(monitoringType, new GeofenceHardwareRequestParcelable(geofenceId, geofenceRequest), getCallbackWrapper(callback));
            }
            throw new IllegalArgumentException("Geofence Request type not supported");
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean removeGeofence(int geofenceId, int monitoringType) {
        try {
            return this.mService.removeGeofence(geofenceId, monitoringType);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean pauseGeofence(int geofenceId, int monitoringType) {
        try {
            return this.mService.pauseGeofence(geofenceId, monitoringType);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean resumeGeofence(int geofenceId, int monitoringType, int monitorTransition) {
        try {
            return this.mService.resumeGeofence(geofenceId, monitoringType, monitorTransition);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean registerForMonitorStateChangeCallback(int monitoringType, GeofenceHardwareMonitorCallback callback) {
        try {
            return this.mService.registerForMonitorStateChangeCallback(monitoringType, getMonitorCallbackWrapper(callback));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean unregisterForMonitorStateChangeCallback(int monitoringType, GeofenceHardwareMonitorCallback callback) {
        boolean result = false;
        try {
            result = this.mService.unregisterForMonitorStateChangeCallback(monitoringType, getMonitorCallbackWrapper(callback));
            if (result) {
                removeMonitorCallback(callback);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    private void removeCallback(GeofenceHardwareCallback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    private GeofenceHardwareCallbackWrapper getCallbackWrapper(GeofenceHardwareCallback callback) {
        GeofenceHardwareCallbackWrapper wrapper;
        synchronized (this.mCallbacks) {
            wrapper = (GeofenceHardwareCallbackWrapper) this.mCallbacks.get(callback);
            if (wrapper == null) {
                wrapper = new GeofenceHardwareCallbackWrapper(this, callback);
                this.mCallbacks.put(callback, wrapper);
            }
        }
        return wrapper;
    }

    private void removeMonitorCallback(GeofenceHardwareMonitorCallback callback) {
        synchronized (this.mMonitorCallbacks) {
            this.mMonitorCallbacks.remove(callback);
        }
    }

    private GeofenceHardwareMonitorCallbackWrapper getMonitorCallbackWrapper(GeofenceHardwareMonitorCallback callback) {
        GeofenceHardwareMonitorCallbackWrapper wrapper;
        synchronized (this.mMonitorCallbacks) {
            wrapper = (GeofenceHardwareMonitorCallbackWrapper) this.mMonitorCallbacks.get(callback);
            if (wrapper == null) {
                wrapper = new GeofenceHardwareMonitorCallbackWrapper(this, callback);
                this.mMonitorCallbacks.put(callback, wrapper);
            }
        }
        return wrapper;
    }
}

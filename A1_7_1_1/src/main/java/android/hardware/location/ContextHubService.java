package android.hardware.location;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.location.IContextHubService.Stub;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
public class ContextHubService extends Stub {
    public static final int ANY_HUB = -1;
    private static final long APP_ID_ACTIVITY_RECOGNITION = 5147455389092024320L;
    public static final String CONTEXTHUB_SERVICE = "contexthub_service";
    private static final String ENFORCE_HW_PERMISSION_MESSAGE = "Permission 'android.permission.LOCATION_HARDWARE' not granted to access ContextHub Hardware";
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int HEADER_FIELD_APP_INSTANCE = 3;
    private static final int HEADER_FIELD_HUB_HANDLE = 2;
    private static final int HEADER_FIELD_LOAD_APP_ID_HI = 5;
    private static final int HEADER_FIELD_LOAD_APP_ID_LO = 4;
    private static final int HEADER_FIELD_MSG_TYPE = 0;
    private static final int HEADER_FIELD_MSG_VERSION = 1;
    private static final int MSG_HEADER_SIZE = 4;
    private static final int MSG_LOAD_APP_HEADER_SIZE = 6;
    public static final int MSG_LOAD_NANO_APP = 3;
    public static final int MSG_UNLOAD_NANO_APP = 4;
    private static final int OS_APP_INSTANCE = -1;
    private static final int PRE_LOADED_APP_MEM_REQ = 0;
    private static final String PRE_LOADED_APP_NAME = "Preloaded app, unknown";
    private static final String PRE_LOADED_APP_PUBLISHER = "Preloaded app, unknown";
    private static final String PRE_LOADED_GENERIC_UNKNOWN = "Preloaded app, unknown";
    private static final String TAG = "ContextHubService";
    private final RemoteCallbackList<IContextHubCallback> mCallbacksList;
    private final Context mContext;
    private final ContextHubInfo[] mContextHubInfo;
    private final ConcurrentHashMap<Integer, NanoAppInstanceInfo> mNanoAppHash;
    private final IVrStateCallbacks mVrStateCallbacks;

    /* renamed from: android.hardware.location.ContextHubService$1 */
    class AnonymousClass1 extends IVrStateCallbacks.Stub {
        final /* synthetic */ ContextHubService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.ContextHubService.1.<init>(android.hardware.location.ContextHubService):void, dex: 
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
        AnonymousClass1(android.hardware.location.ContextHubService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.ContextHubService.1.<init>(android.hardware.location.ContextHubService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubService.1.<init>(android.hardware.location.ContextHubService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.ContextHubService.1.onVrStateChanged(boolean):void, dex: 
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
        public void onVrStateChanged(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.ContextHubService.1.onVrStateChanged(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubService.1.onVrStateChanged(boolean):void");
        }
    }

    private native ContextHubInfo[] nativeInitialize();

    private native int nativeSendMessage(int[] iArr, byte[] bArr);

    public ContextHubService(Context context) {
        this.mNanoAppHash = new ConcurrentHashMap();
        this.mCallbacksList = new RemoteCallbackList();
        this.mVrStateCallbacks = new AnonymousClass1(this);
        this.mContext = context;
        this.mContextHubInfo = nativeInitialize();
        for (int i = 0; i < this.mContextHubInfo.length; i++) {
            Log.d(TAG, "ContextHub[" + i + "] id: " + this.mContextHubInfo[i].getId() + ", name:  " + this.mContextHubInfo[i].getName());
        }
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_VR_MODE)) {
            IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
            if (vrManager != null) {
                try {
                    vrManager.registerListener(this.mVrStateCallbacks);
                } catch (RemoteException e) {
                    Log.e(TAG, "VR state listener registration failed", e);
                }
            }
        }
    }

    public int registerCallback(IContextHubCallback callback) throws RemoteException {
        checkPermissions();
        this.mCallbacksList.register(callback);
        return 0;
    }

    public int[] getContextHubHandles() throws RemoteException {
        checkPermissions();
        int[] returnArray = new int[this.mContextHubInfo.length];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = i;
            Log.d(TAG, String.format("Hub %s is mapped to %d", new Object[]{this.mContextHubInfo[i].getName(), Integer.valueOf(returnArray[i])}));
        }
        return returnArray;
    }

    public ContextHubInfo getContextHubInfo(int contextHubHandle) throws RemoteException {
        checkPermissions();
        if (contextHubHandle < 0 || contextHubHandle >= this.mContextHubInfo.length) {
            return null;
        }
        return this.mContextHubInfo[contextHubHandle];
    }

    private static long parseAppId(NanoApp app) {
        ByteBuffer header = ByteBuffer.wrap(app.getAppBinary()).order(ByteOrder.LITTLE_ENDIAN);
        try {
            if (header.getInt(4) == 1330528590) {
                return header.getLong(8);
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return (long) app.getAppId();
    }

    public int loadNanoApp(int contextHubHandle, NanoApp app) throws RemoteException {
        checkPermissions();
        if (contextHubHandle < 0 || contextHubHandle >= this.mContextHubInfo.length) {
            Log.e(TAG, "Invalid contextHubhandle " + contextHubHandle);
            return -1;
        }
        int[] msgHeader = new int[6];
        msgHeader[2] = contextHubHandle;
        msgHeader[3] = -1;
        msgHeader[1] = 0;
        msgHeader[0] = 3;
        long appId = (long) app.getAppId();
        if ((appId >> 32) != 0) {
            Log.w(TAG, "Code has not been updated since API fix.");
        } else {
            appId = parseAppId(app);
        }
        msgHeader[4] = (int) (-1 & appId);
        msgHeader[5] = (int) ((appId >> 32) & -1);
        if (nativeSendMessage(msgHeader, app.getAppBinary()) == 0) {
            return 0;
        }
        Log.e(TAG, "Send Message returns error" + contextHubHandle);
        return -1;
    }

    public int unloadNanoApp(int nanoAppInstanceHandle) throws RemoteException {
        checkPermissions();
        if (((NanoAppInstanceInfo) this.mNanoAppHash.get(Integer.valueOf(nanoAppInstanceHandle))) == null) {
            return -1;
        }
        return nativeSendMessage(new int[]{-1, nanoAppInstanceHandle, 0, 4}, new byte[0]) != 0 ? -1 : 0;
    }

    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppInstanceHandle) throws RemoteException {
        checkPermissions();
        if (this.mNanoAppHash.containsKey(Integer.valueOf(nanoAppInstanceHandle))) {
            return (NanoAppInstanceInfo) this.mNanoAppHash.get(Integer.valueOf(nanoAppInstanceHandle));
        }
        return null;
    }

    public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) throws RemoteException {
        checkPermissions();
        ArrayList<Integer> foundInstances = new ArrayList();
        for (Integer nanoAppInstance : this.mNanoAppHash.keySet()) {
            if (filter.testMatch((NanoAppInstanceInfo) this.mNanoAppHash.get(nanoAppInstance))) {
                foundInstances.add(nanoAppInstance);
            }
        }
        int[] retArray = new int[foundInstances.size()];
        for (int i = 0; i < foundInstances.size(); i++) {
            retArray[i] = ((Integer) foundInstances.get(i)).intValue();
        }
        return retArray;
    }

    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage msg) throws RemoteException {
        checkPermissions();
        return nativeSendMessage(new int[]{hubHandle, nanoAppHandle, msg.getVersion(), msg.getMsgType()}, msg.getData());
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission(permission.DUMP) != 0) {
            pw.println("Permission Denial: can't dump contexthub_service");
            return;
        }
        pw.println("Dumping ContextHub Service");
        pw.println("");
        pw.println("=================== CONTEXT HUBS ====================");
        for (int i = 0; i < this.mContextHubInfo.length; i++) {
            pw.println("Handle " + i + " : " + this.mContextHubInfo[i].toString());
        }
        pw.println("");
        pw.println("=================== NANOAPPS ====================");
        for (Integer nanoAppInstance : this.mNanoAppHash.keySet()) {
            pw.println(nanoAppInstance + " : " + ((NanoAppInstanceInfo) this.mNanoAppHash.get(nanoAppInstance)).toString());
        }
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", ENFORCE_HW_PERMISSION_MESSAGE);
    }

    private int onMessageReceipt(int[] header, byte[] data) {
        if (header == null || data == null || header.length < 4) {
            return -1;
        }
        int callbacksCount = this.mCallbacksList.beginBroadcast();
        if (callbacksCount < 1) {
            Log.v(TAG, "No message callbacks registered.");
            return 0;
        }
        ContextHubMessage msg = new ContextHubMessage(header[0], header[1], data);
        for (int i = 0; i < callbacksCount; i++) {
            IContextHubCallback callback = (IContextHubCallback) this.mCallbacksList.getBroadcastItem(i);
            try {
                callback.onMessageReceipt(header[2], header[3], msg);
            } catch (RemoteException e) {
                Log.i(TAG, "Exception (" + e + ") calling remote callback (" + callback + ").");
            }
        }
        this.mCallbacksList.finishBroadcast();
        return 0;
    }

    private int addAppInstance(int hubHandle, int appInstanceHandle, long appId, int appVersion) {
        String action;
        NanoAppInstanceInfo appInfo = new NanoAppInstanceInfo();
        appInfo.setAppId(appId);
        appInfo.setAppVersion(appVersion);
        appInfo.setName("Preloaded app, unknown");
        appInfo.setContexthubId(hubHandle);
        appInfo.setHandle(appInstanceHandle);
        appInfo.setPublisher("Preloaded app, unknown");
        appInfo.setNeededExecMemBytes(0);
        appInfo.setNeededReadMemBytes(0);
        appInfo.setNeededWriteMemBytes(0);
        if (this.mNanoAppHash.containsKey(Integer.valueOf(appInstanceHandle))) {
            action = "Updated";
        } else {
            action = "Added";
        }
        this.mNanoAppHash.put(Integer.valueOf(appInstanceHandle), appInfo);
        Log.d(TAG, action + " app instance " + appInstanceHandle + " with id " + appId + " version " + appVersion);
        return 0;
    }

    private int deleteAppInstance(int appInstanceHandle) {
        if (this.mNanoAppHash.remove(Integer.valueOf(appInstanceHandle)) == null) {
            return -1;
        }
        return 0;
    }

    private void sendVrStateChangeMessageToApp(NanoAppInstanceInfo app, boolean vrModeEnabled) {
        int i = 1;
        int[] msgHeader = new int[]{0, 0, -1, app.getHandle()};
        byte[] data = new byte[1];
        if (!vrModeEnabled) {
            i = 0;
        }
        data[0] = (byte) i;
        int ret = nativeSendMessage(msgHeader, data);
        if (ret != 0) {
            Log.e(TAG, "Couldn't send VR state change notification (" + ret + ")!");
        }
    }
}

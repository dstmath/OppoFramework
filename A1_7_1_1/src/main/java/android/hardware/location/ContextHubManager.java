package android.hardware.location;

import android.content.Context;
import android.hardware.location.IContextHubCallback.Stub;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class ContextHubManager {
    private static final String TAG = "ContextHubManager";
    private Callback mCallback;
    private Handler mCallbackHandler;
    private Stub mClientCallback;
    private IContextHubService mContextHubService;
    @Deprecated
    private ICallback mLocalCallback;
    private final Looper mMainLooper;

    /* renamed from: android.hardware.location.ContextHubManager$1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ ContextHubManager this$0;

        /* renamed from: android.hardware.location.ContextHubManager$1$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ AnonymousClass1 this$1;
            final /* synthetic */ Callback val$callback;
            final /* synthetic */ int val$hubId;
            final /* synthetic */ ContextHubMessage val$message;
            final /* synthetic */ int val$nanoAppId;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.ContextHubManager.1.1.<init>(android.hardware.location.ContextHubManager$1, android.hardware.location.ContextHubManager$Callback, int, int, android.hardware.location.ContextHubMessage):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            AnonymousClass1(android.hardware.location.ContextHubManager.AnonymousClass1 r1, android.hardware.location.ContextHubManager.Callback r2, int r3, int r4, android.hardware.location.ContextHubMessage r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.ContextHubManager.1.1.<init>(android.hardware.location.ContextHubManager$1, android.hardware.location.ContextHubManager$Callback, int, int, android.hardware.location.ContextHubMessage):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubManager.1.1.<init>(android.hardware.location.ContextHubManager$1, android.hardware.location.ContextHubManager$Callback, int, int, android.hardware.location.ContextHubMessage):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.ContextHubManager.1.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.ContextHubManager.1.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubManager.1.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.ContextHubManager.1.<init>(android.hardware.location.ContextHubManager):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(android.hardware.location.ContextHubManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.location.ContextHubManager.1.<init>(android.hardware.location.ContextHubManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubManager.1.<init>(android.hardware.location.ContextHubManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.ContextHubManager.1.onMessageReceipt(int, int, android.hardware.location.ContextHubMessage):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onMessageReceipt(int r1, int r2, android.hardware.location.ContextHubMessage r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.location.ContextHubManager.1.onMessageReceipt(int, int, android.hardware.location.ContextHubMessage):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubManager.1.onMessageReceipt(int, int, android.hardware.location.ContextHubMessage):void");
        }
    }

    public static abstract class Callback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ContextHubManager.Callback.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        protected Callback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ContextHubManager.Callback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubManager.Callback.<init>():void");
        }

        public abstract void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);
    }

    @Deprecated
    public interface ICallback {
        void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);
    }

    public int[] getContextHubHandles() {
        int[] retVal = null;
        try {
            return getBinder().getContextHubHandles();
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch context hub handles : " + e);
            return retVal;
        }
    }

    public ContextHubInfo getContextHubInfo(int hubHandle) {
        ContextHubInfo retVal = null;
        try {
            return getBinder().getContextHubInfo(hubHandle);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch context hub info :" + e);
            return retVal;
        }
    }

    public int loadNanoApp(int hubHandle, NanoApp app) {
        int retVal = -1;
        if (app == null) {
            return retVal;
        }
        try {
            retVal = getBinder().loadNanoApp(hubHandle, app);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not load nanoApp :" + e);
        }
        return retVal;
    }

    public int unloadNanoApp(int nanoAppHandle) {
        int retVal = -1;
        try {
            return getBinder().unloadNanoApp(nanoAppHandle);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch unload nanoApp :" + e);
            return retVal;
        }
    }

    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppHandle) {
        NanoAppInstanceInfo retVal = null;
        try {
            return getBinder().getNanoAppInstanceInfo(nanoAppHandle);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch nanoApp info :" + e);
            return retVal;
        }
    }

    public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) {
        int[] retVal = null;
        try {
            return getBinder().findNanoAppOnHub(hubHandle, filter);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not query nanoApp instance :" + e);
            return retVal;
        }
    }

    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage message) {
        int retVal = -1;
        if (message == null || message.getData() == null) {
            Log.w(TAG, "null ptr");
            return retVal;
        }
        try {
            retVal = getBinder().sendMessage(hubHandle, nanoAppHandle, message);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not send message :" + e.toString());
        }
        return retVal;
    }

    public int registerCallback(Callback callback) {
        return registerCallback(callback, null);
    }

    @Deprecated
    public int registerCallback(ICallback callback) {
        if (this.mLocalCallback != null) {
            Log.w(TAG, "Max number of local callbacks reached!");
            return -1;
        }
        this.mLocalCallback = callback;
        return 0;
    }

    public int registerCallback(Callback callback, Handler handler) {
        synchronized (this) {
            if (this.mCallback != null) {
                Log.w(TAG, "Max number of callbacks reached!");
                return -1;
            }
            this.mCallback = callback;
            this.mCallbackHandler = handler;
            return 0;
        }
    }

    public int unregisterCallback(Callback callback) {
        synchronized (this) {
            if (callback != this.mCallback) {
                Log.w(TAG, "Cannot recognize callback!");
                return -1;
            }
            this.mCallback = null;
            this.mCallbackHandler = null;
            return 0;
        }
    }

    public synchronized int unregisterCallback(ICallback callback) {
        if (callback != this.mLocalCallback) {
            Log.w(TAG, "Cannot recognize local callback!");
            return -1;
        }
        this.mLocalCallback = null;
        return 0;
    }

    public ContextHubManager(Context context, Looper mainLooper) {
        this.mClientCallback = new AnonymousClass1(this);
        this.mMainLooper = mainLooper;
        IBinder b = ServiceManager.getService(ContextHubService.CONTEXTHUB_SERVICE);
        if (b != null) {
            this.mContextHubService = IContextHubService.Stub.asInterface(b);
            try {
                getBinder().registerCallback(this.mClientCallback);
                return;
            } catch (RemoteException e) {
                Log.w(TAG, "Could not register callback:" + e);
                return;
            }
        }
        Log.w(TAG, "failed to getService");
    }

    private IContextHubService getBinder() throws RemoteException {
        if (this.mContextHubService != null) {
            return this.mContextHubService;
        }
        throw new RemoteException("Service not connected.");
    }
}

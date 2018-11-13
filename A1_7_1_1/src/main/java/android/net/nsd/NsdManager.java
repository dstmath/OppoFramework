package android.net.nsd;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import java.util.concurrent.CountDownLatch;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public final class NsdManager {
    public static final String ACTION_NSD_STATE_CHANGED = "android.net.nsd.STATE_CHANGED";
    private static final int BASE = 393216;
    private static final int BUSY_LISTENER_KEY = -1;
    public static final int DISABLE = 393241;
    public static final int DISCOVER_SERVICES = 393217;
    public static final int DISCOVER_SERVICES_FAILED = 393219;
    public static final int DISCOVER_SERVICES_STARTED = 393218;
    public static final int ENABLE = 393240;
    public static final String EXTRA_NSD_STATE = "nsd_state";
    public static final int FAILURE_ALREADY_ACTIVE = 3;
    public static final int FAILURE_INTERNAL_ERROR = 0;
    public static final int FAILURE_MAX_LIMIT = 4;
    private static final int INVALID_LISTENER_KEY = 0;
    public static final int NATIVE_DAEMON_EVENT = 393242;
    public static final int NSD_STATE_DISABLED = 1;
    public static final int NSD_STATE_ENABLED = 2;
    public static final int PROTOCOL_DNS_SD = 1;
    public static final int REGISTER_SERVICE = 393225;
    public static final int REGISTER_SERVICE_FAILED = 393226;
    public static final int REGISTER_SERVICE_SUCCEEDED = 393227;
    public static final int RESOLVE_SERVICE = 393234;
    public static final int RESOLVE_SERVICE_FAILED = 393235;
    public static final int RESOLVE_SERVICE_SUCCEEDED = 393236;
    public static final int SERVICE_FOUND = 393220;
    public static final int SERVICE_LOST = 393221;
    public static final int STOP_DISCOVERY = 393222;
    public static final int STOP_DISCOVERY_FAILED = 393223;
    public static final int STOP_DISCOVERY_SUCCEEDED = 393224;
    private static final String TAG = "NsdManager";
    public static final int UNREGISTER_SERVICE = 393228;
    public static final int UNREGISTER_SERVICE_FAILED = 393229;
    public static final int UNREGISTER_SERVICE_SUCCEEDED = 393230;
    private final AsyncChannel mAsyncChannel;
    private final CountDownLatch mConnected;
    private Context mContext;
    private ServiceHandler mHandler;
    private int mListenerKey;
    private final SparseArray mListenerMap;
    private final Object mMapLock;
    INsdManager mService;
    private final SparseArray<NsdServiceInfo> mServiceMap;

    public interface DiscoveryListener {
        void onDiscoveryStarted(String str);

        void onDiscoveryStopped(String str);

        void onServiceFound(NsdServiceInfo nsdServiceInfo);

        void onServiceLost(NsdServiceInfo nsdServiceInfo);

        void onStartDiscoveryFailed(String str, int i);

        void onStopDiscoveryFailed(String str, int i);
    }

    public interface RegistrationListener {
        void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i);

        void onServiceRegistered(NsdServiceInfo nsdServiceInfo);

        void onServiceUnregistered(NsdServiceInfo nsdServiceInfo);

        void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i);
    }

    public interface ResolveListener {
        void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i);

        void onServiceResolved(NsdServiceInfo nsdServiceInfo);
    }

    private class ServiceHandler extends Handler {
        final /* synthetic */ NsdManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.nsd.NsdManager.ServiceHandler.<init>(android.net.nsd.NsdManager, android.os.Looper):void, dex: 
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
        ServiceHandler(android.net.nsd.NsdManager r1, android.os.Looper r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.nsd.NsdManager.ServiceHandler.<init>(android.net.nsd.NsdManager, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.nsd.NsdManager.ServiceHandler.<init>(android.net.nsd.NsdManager, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.nsd.NsdManager.ServiceHandler.handleMessage(android.os.Message):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.nsd.NsdManager.ServiceHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.nsd.NsdManager.ServiceHandler.handleMessage(android.os.Message):void");
        }
    }

    public NsdManager(Context context, INsdManager service) {
        this.mListenerKey = 1;
        this.mListenerMap = new SparseArray();
        this.mServiceMap = new SparseArray();
        this.mMapLock = new Object();
        this.mAsyncChannel = new AsyncChannel();
        this.mConnected = new CountDownLatch(1);
        this.mService = service;
        this.mContext = context;
        init();
    }

    private int putListener(Object listener, NsdServiceInfo s) {
        if (listener == null) {
            return 0;
        }
        synchronized (this.mMapLock) {
            if (this.mListenerMap.indexOfValue(listener) != -1) {
                return -1;
            }
            int key;
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + 1;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
            this.mServiceMap.put(key, s);
            return key;
        }
    }

    private Object getListener(int key) {
        if (key == 0) {
            return null;
        }
        Object obj;
        synchronized (this.mMapLock) {
            obj = this.mListenerMap.get(key);
        }
        return obj;
    }

    private NsdServiceInfo getNsdService(int key) {
        NsdServiceInfo nsdServiceInfo;
        synchronized (this.mMapLock) {
            nsdServiceInfo = (NsdServiceInfo) this.mServiceMap.get(key);
        }
        return nsdServiceInfo;
    }

    private void removeListener(int key) {
        if (key != 0) {
            synchronized (this.mMapLock) {
                this.mListenerMap.remove(key);
                this.mServiceMap.remove(key);
            }
        }
    }

    private int getListenerKey(Object listener) {
        synchronized (this.mMapLock) {
            int valueIndex = this.mListenerMap.indexOfValue(listener);
            if (valueIndex != -1) {
                int keyAt = this.mListenerMap.keyAt(valueIndex);
                return keyAt;
            }
            return 0;
        }
    }

    private String getNsdServiceInfoType(NsdServiceInfo s) {
        if (s == null) {
            return "?";
        }
        return s.getServiceType();
    }

    private void init() {
        Messenger messenger = getMessenger();
        if (messenger == null) {
            throw new RuntimeException("Failed to initialize");
        }
        HandlerThread t = new HandlerThread(TAG);
        t.start();
        this.mHandler = new ServiceHandler(this, t.getLooper());
        this.mAsyncChannel.connect(this.mContext, this.mHandler, messenger);
        try {
            this.mConnected.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "interrupted wait at init");
        }
    }

    public void registerService(NsdServiceInfo serviceInfo, int protocolType, RegistrationListener listener) {
        if (TextUtils.isEmpty(serviceInfo.getServiceName()) || TextUtils.isEmpty(serviceInfo.getServiceType())) {
            throw new IllegalArgumentException("Service name or type cannot be empty");
        } else if (serviceInfo.getPort() <= 0) {
            throw new IllegalArgumentException("Invalid port number");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (protocolType != 1) {
            throw new IllegalArgumentException("Unsupported protocol");
        } else {
            int key = putListener(listener, serviceInfo);
            if (key == -1) {
                throw new IllegalArgumentException("listener already in use");
            }
            this.mAsyncChannel.sendMessage(REGISTER_SERVICE, 0, key, serviceInfo);
        }
    }

    public void unregisterService(RegistrationListener listener) {
        int id = getListenerKey(listener);
        if (id == 0) {
            throw new IllegalArgumentException("listener not registered");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            this.mAsyncChannel.sendMessage(UNREGISTER_SERVICE, 0, id);
        }
    }

    public void discoverServices(String serviceType, int protocolType, DiscoveryListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (TextUtils.isEmpty(serviceType)) {
            throw new IllegalArgumentException("Service type cannot be empty");
        } else if (protocolType != 1) {
            throw new IllegalArgumentException("Unsupported protocol");
        } else {
            NsdServiceInfo s = new NsdServiceInfo();
            s.setServiceType(serviceType);
            int key = putListener(listener, s);
            if (key == -1) {
                throw new IllegalArgumentException("listener already in use");
            }
            this.mAsyncChannel.sendMessage(DISCOVER_SERVICES, 0, key, s);
        }
    }

    public void stopServiceDiscovery(DiscoveryListener listener) {
        int id = getListenerKey(listener);
        if (id == 0) {
            throw new IllegalArgumentException("service discovery not active on listener");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            this.mAsyncChannel.sendMessage(STOP_DISCOVERY, 0, id);
        }
    }

    public void resolveService(NsdServiceInfo serviceInfo, ResolveListener listener) {
        Log.d(TAG, "resolveService");
        if (TextUtils.isEmpty(serviceInfo.getServiceName()) || TextUtils.isEmpty(serviceInfo.getServiceType())) {
            throw new IllegalArgumentException("Service name or type cannot be empty");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else {
            int key = putListener(listener, serviceInfo);
            if (key == -1) {
                throw new IllegalArgumentException("listener already in use");
            }
            this.mAsyncChannel.sendMessage(RESOLVE_SERVICE, 0, key, serviceInfo);
        }
    }

    public void setEnabled(boolean enabled) {
        try {
            this.mService.setEnabled(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private Messenger getMessenger() {
        try {
            return this.mService.getMessenger();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}

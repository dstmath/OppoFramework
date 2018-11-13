package android.app.usage;

import android.app.usage.NetworkStats.Bucket;
import android.content.Context;
import android.net.DataUsageRequest;
import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.net.NetworkIdentity;
import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.util.Preconditions;

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
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class NetworkStatsManager {
    public static final int CALLBACK_LIMIT_REACHED = 0;
    public static final int CALLBACK_RELEASED = 1;
    private static final boolean DBG = true;
    private static final String TAG = "NetworkStatsManager";
    private final Context mContext;
    private final INetworkStatsService mService;

    private static class CallbackHandler extends Handler {
        private UsageCallback mCallback;
        private final int mNetworkType;
        private final String mSubscriberId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.app.usage.NetworkStatsManager.CallbackHandler.<init>(android.os.Looper, int, java.lang.String, android.app.usage.NetworkStatsManager$UsageCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        CallbackHandler(android.os.Looper r1, int r2, java.lang.String r3, android.app.usage.NetworkStatsManager.UsageCallback r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.app.usage.NetworkStatsManager.CallbackHandler.<init>(android.os.Looper, int, java.lang.String, android.app.usage.NetworkStatsManager$UsageCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.usage.NetworkStatsManager.CallbackHandler.<init>(android.os.Looper, int, java.lang.String, android.app.usage.NetworkStatsManager$UsageCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.app.usage.NetworkStatsManager.CallbackHandler.getObject(android.os.Message, java.lang.String):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private static java.lang.Object getObject(android.os.Message r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.app.usage.NetworkStatsManager.CallbackHandler.getObject(android.os.Message, java.lang.String):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.usage.NetworkStatsManager.CallbackHandler.getObject(android.os.Message, java.lang.String):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.app.usage.NetworkStatsManager.CallbackHandler.handleMessage(android.os.Message):void, dex: 
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
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.app.usage.NetworkStatsManager.CallbackHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.usage.NetworkStatsManager.CallbackHandler.handleMessage(android.os.Message):void");
        }
    }

    public static abstract class UsageCallback {
        private DataUsageRequest request;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.usage.NetworkStatsManager.UsageCallback.-get0(android.app.usage.NetworkStatsManager$UsageCallback):android.net.DataUsageRequest, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ android.net.DataUsageRequest m60-get0(android.app.usage.NetworkStatsManager.UsageCallback r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.usage.NetworkStatsManager.UsageCallback.-get0(android.app.usage.NetworkStatsManager$UsageCallback):android.net.DataUsageRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.usage.NetworkStatsManager.UsageCallback.-get0(android.app.usage.NetworkStatsManager$UsageCallback):android.net.DataUsageRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.usage.NetworkStatsManager.UsageCallback.-set0(android.app.usage.NetworkStatsManager$UsageCallback, android.net.DataUsageRequest):android.net.DataUsageRequest, dex: 
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
        /* renamed from: -set0 */
        static /* synthetic */ android.net.DataUsageRequest m61-set0(android.app.usage.NetworkStatsManager.UsageCallback r1, android.net.DataUsageRequest r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.usage.NetworkStatsManager.UsageCallback.-set0(android.app.usage.NetworkStatsManager$UsageCallback, android.net.DataUsageRequest):android.net.DataUsageRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.usage.NetworkStatsManager.UsageCallback.-set0(android.app.usage.NetworkStatsManager$UsageCallback, android.net.DataUsageRequest):android.net.DataUsageRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.usage.NetworkStatsManager.UsageCallback.<init>():void, dex: 
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
        public UsageCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.usage.NetworkStatsManager.UsageCallback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.usage.NetworkStatsManager.UsageCallback.<init>():void");
        }

        public abstract void onThresholdReached(int i, String str);
    }

    public NetworkStatsManager(Context context) {
        this.mContext = context;
        this.mService = Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
    }

    public Bucket querySummaryForDevice(int networkType, String subscriberId, long startTime, long endTime) throws SecurityException, RemoteException {
        try {
            NetworkStats stats = new NetworkStats(this.mContext, createTemplate(networkType, subscriberId), startTime, endTime);
            Bucket bucket = stats.getDeviceSummaryForNetwork();
            stats.close();
            return bucket;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot create template", e);
            return null;
        }
    }

    public Bucket querySummaryForUser(int networkType, String subscriberId, long startTime, long endTime) throws SecurityException, RemoteException {
        try {
            NetworkStats stats = new NetworkStats(this.mContext, createTemplate(networkType, subscriberId), startTime, endTime);
            stats.startSummaryEnumeration();
            stats.close();
            return stats.getSummaryAggregate();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot create template", e);
            return null;
        }
    }

    public NetworkStats querySummary(int networkType, String subscriberId, long startTime, long endTime) throws SecurityException, RemoteException {
        try {
            NetworkStats result = new NetworkStats(this.mContext, createTemplate(networkType, subscriberId), startTime, endTime);
            result.startSummaryEnumeration();
            return result;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot create template", e);
            return null;
        }
    }

    public NetworkStats queryDetailsForUid(int networkType, String subscriberId, long startTime, long endTime, int uid) throws SecurityException, RemoteException {
        return queryDetailsForUidTag(networkType, subscriberId, startTime, endTime, uid, 0);
    }

    public NetworkStats queryDetailsForUidTag(int networkType, String subscriberId, long startTime, long endTime, int uid, int tag) throws SecurityException {
        try {
            NetworkStats result = new NetworkStats(this.mContext, createTemplate(networkType, subscriberId), startTime, endTime);
            result.startHistoryEnumeration(uid, tag);
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "Error while querying stats for uid=" + uid + " tag=" + tag, e);
            return null;
        }
    }

    public NetworkStats queryDetails(int networkType, String subscriberId, long startTime, long endTime) throws SecurityException, RemoteException {
        try {
            NetworkStats result = new NetworkStats(this.mContext, createTemplate(networkType, subscriberId), startTime, endTime);
            result.startUserUidEnumeration();
            return result;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Cannot create template", e);
            return null;
        }
    }

    public void registerUsageCallback(int networkType, String subscriberId, long thresholdBytes, UsageCallback callback) {
        registerUsageCallback(networkType, subscriberId, thresholdBytes, callback, null);
    }

    public void registerUsageCallback(int networkType, String subscriberId, long thresholdBytes, UsageCallback callback, Handler handler) {
        Looper looper;
        Preconditions.checkNotNull(callback, "UsageCallback cannot be null");
        if (handler == null) {
            looper = Looper.myLooper();
        } else {
            looper = handler.getLooper();
        }
        Log.d(TAG, "registerUsageCallback called with: { networkType=" + networkType + " subscriberId=" + subscriberId + " thresholdBytes=" + thresholdBytes + " }");
        DataUsageRequest request = new DataUsageRequest(0, createTemplate(networkType, subscriberId), thresholdBytes);
        try {
            UsageCallback.m61-set0(callback, this.mService.registerUsageCallback(this.mContext.getOpPackageName(), request, new Messenger(new CallbackHandler(looper, networkType, subscriberId, callback)), new Binder()));
            Log.d(TAG, "registerUsageCallback returned " + UsageCallback.m60-get0(callback));
            if (UsageCallback.m60-get0(callback) == null) {
                Log.e(TAG, "Request from callback is null; should not happen");
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Remote exception when registering callback");
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterUsageCallback(UsageCallback callback) {
        if (callback == null || UsageCallback.m60-get0(callback) == null || UsageCallback.m60-get0(callback).requestId == 0) {
            throw new IllegalArgumentException("Invalid UsageCallback");
        }
        try {
            this.mService.unregisterUsageRequest(UsageCallback.m60-get0(callback));
        } catch (RemoteException e) {
            Log.d(TAG, "Remote exception when unregistering callback");
            throw e.rethrowFromSystemServer();
        }
    }

    private static NetworkTemplate createTemplate(int networkType, String subscriberId) {
        switch (networkType) {
            case 0:
                return NetworkTemplate.buildTemplateMobileAll(subscriberId);
            case 1:
                return NetworkTemplate.buildTemplateWifiWildcard();
            default:
                throw new IllegalArgumentException("Cannot create template for network type " + networkType + ", subscriberId '" + NetworkIdentity.scrubSubscriberId(subscriberId) + "'.");
        }
    }
}

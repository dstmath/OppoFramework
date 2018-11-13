package com.mediatek.gba;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.ims.internal.IImsService;
import com.mediatek.gba.IGbaService.Stub;

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
public final class GbaManager {
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID0 = null;
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID1 = null;
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID2 = null;
    public static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID3 = null;
    private static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID_HTTP = null;
    private static final byte[] DEFAULT_UA_SECURITY_PROTOCOL_ID_TLS = null;
    public static final String IMS_GBA_KS_EXT_NAF = "Ks_ext_NAF";
    public static final String IMS_GBA_KS_NAF = "Ks_NAF";
    public static final int IMS_GBA_ME = 1;
    public static final int IMS_GBA_NONE = 0;
    public static final int IMS_GBA_U = 2;
    public static final String IMS_SERVICE = "ims";
    private static final String TAG = "GbaManager";
    private static GbaManager mGbaManager;
    private static int mNetId;
    private static IGbaService mService;
    private final Context mContext;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.gba.GbaManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.gba.GbaManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaManager.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.gba.GbaManager.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    GbaManager(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.gba.GbaManager.<init>(android.content.Context):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaManager.<init>(android.content.Context):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaManager.is93MDSupport():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private boolean is93MDSupport() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaManager.is93MDSupport():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaManager.is93MDSupport():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaManager.getNafSecureProtocolId(boolean):byte[], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public byte[] getNafSecureProtocolId(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.gba.GbaManager.getNafSecureProtocolId(boolean):byte[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaManager.getNafSecureProtocolId(boolean):byte[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.mediatek.gba.GbaManager.setNetwork(android.net.Network):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setNetwork(android.net.Network r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.mediatek.gba.GbaManager.setNetwork(android.net.Network):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaManager.setNetwork(android.net.Network):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.gba.GbaManager.updateCachedKey(java.lang.String, byte[], int, com.mediatek.gba.NafSessionKey):void, dex: 
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
    public void updateCachedKey(java.lang.String r1, byte[] r2, int r3, com.mediatek.gba.NafSessionKey r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.gba.GbaManager.updateCachedKey(java.lang.String, byte[], int, com.mediatek.gba.NafSessionKey):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.gba.GbaManager.updateCachedKey(java.lang.String, byte[], int, com.mediatek.gba.NafSessionKey):void");
    }

    public static GbaManager getDefaultGbaManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        synchronized (GbaManager.class) {
            if (mGbaManager == null) {
                IBinder b = ServiceManager.getService("GbaService");
                if (b == null) {
                    Log.i("debug", "The binder is null");
                    return null;
                }
                mService = Stub.asInterface(b);
                mGbaManager = new GbaManager(context);
            }
            GbaManager gbaManager = mGbaManager;
            return gbaManager;
        }
    }

    public int getGbaSupported() {
        try {
            return mService.getGbaSupported();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getGbaSupported(int subId) {
        try {
            return mService.getGbaSupported();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isGbaKeyExpired(String nafFqdn, byte[] nafSecurProtocolId) {
        try {
            return mService.isGbaKeyExpired(nafFqdn, nafSecurProtocolId);
        } catch (RemoteException e) {
            return true;
        }
    }

    public boolean isGbaKeyExpired(String nafFqdn, byte[] nafSecurProtocolId, int subId) {
        try {
            return mService.isGbaKeyExpiredForSubscriber(nafFqdn, nafSecurProtocolId, subId);
        } catch (RemoteException e) {
            return true;
        }
    }

    public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun) {
        try {
            if (!is93MDSupport()) {
                return mService.runGbaAuthentication(nafFqdn, nafSecureProtocolId, forceRun);
            }
            return runNativeGba(nafFqdn, nafSecureProtocolId, forceRun, mNetId, SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
        } catch (RemoteException e) {
            return null;
        }
    }

    public NafSessionKey runGbaAuthentication(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int subId) {
        try {
            if (!is93MDSupport()) {
                return mService.runGbaAuthenticationForSubscriber(nafFqdn, nafSecureProtocolId, forceRun, subId);
            }
            return runNativeGba(nafFqdn, nafSecureProtocolId, forceRun, mNetId, SubscriptionManager.getPhoneId(subId));
        } catch (RemoteException e) {
            return null;
        }
    }

    private NafSessionKey runNativeGba(String nafFqdn, byte[] nafSecureProtocolId, boolean forceRun, int netId, int phoneId) {
        IBinder b = ServiceManager.getService("ims");
        if (b == null) {
            Log.e(TAG, "Service is unavailable binder is null");
            return null;
        }
        IImsService mImsService = IImsService.Stub.asInterface(b);
        if (mImsService == null) {
            Log.e(TAG, "Service is unavailable mImsService is null");
            return null;
        }
        try {
            return mImsService.runGbaAuthentication(nafFqdn, nafSecureProtocolId, forceRun, netId, phoneId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemotaException mImsService.runGbaAuthentication()");
            return null;
        }
    }

    public NafSessionKey getCachedKey(String nafFqdn, byte[] nafSecureProtocolId, int subId) {
        try {
            return mService.getCachedKey(nafFqdn, nafSecureProtocolId, subId);
        } catch (RemoteException e) {
            Log.e(TAG, "remote expcetion for getCachedKey");
            return null;
        }
    }
}

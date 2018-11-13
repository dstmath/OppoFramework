package com.android.server.fingerprint.keyguard;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.WindowManagerPolicy;
import com.android.server.LocalServices;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.policy.OppoScreenOffGestureManager;
import com.mediatek.anrmanager.ANRManager;

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
public class KeyguardPolicy {
    public static final String TAG = "FingerprintService.KeyguardPolicy";
    private static Object mMutex;
    private static KeyguardPolicy mSingleInstance;
    private Context mContext;
    private WindowManagerPolicy mPolicy;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.keyguard.KeyguardPolicy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.keyguard.KeyguardPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.keyguard.KeyguardPolicy.<clinit>():void");
    }

    public KeyguardPolicy(Context context) {
        this.mContext = context;
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
    }

    public static KeyguardPolicy getKeyguardPolicy(Context c) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new KeyguardPolicy(c);
            }
        }
        return mSingleInstance;
    }

    public static KeyguardPolicy getKeyguardPolicy() {
        return mSingleInstance;
    }

    public int hasKeyguard() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(10000, data, reply, 0);
                int result = reply.readInt();
                data.recycle();
                reply.recycle();
                return result;
            }
        } catch (RemoteException e) {
        }
        return -1;
    }

    public int setKeyguardVisibility(boolean isVisible) {
        int ret;
        int visible = isVisible ? 1 : 0;
        int retry = 0;
        do {
            ret = setKeyguardVisibility(visible);
            LogUtil.d(TAG, "setKeyguardVisibility: " + visible + " ret:" + ret);
            retry++;
            if (retry >= 3) {
                break;
            }
        } while (ret != 1);
        return ret;
    }

    private int setKeyguardVisibility(int visible) {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                data.writeInt(visible);
                flinger.transact(OppoScreenOffGestureManager.MSG_SCREEN_TURNED_OFF, data, reply, 0);
                int result = reply.readInt();
                data.recycle();
                reply.recycle();
                return result;
            }
        } catch (RemoteException e) {
        }
        return -1;
    }

    public void dispatchWakeUp(boolean isWakeUpByFingerprint) {
        this.mPolicy.dispatchWakeUp(isWakeUpByFingerprint);
    }

    public void hideKeyguard() {
        LogUtil.d(TAG, "hideKeyguard");
        this.mPolicy.dispatchWakeUp(true);
    }

    public void showKeyguard() {
        LogUtil.d(TAG, "showKeyguard");
        this.mPolicy.dispatchWakeUp(false);
    }

    private void calculateTime(String mode, long interval) {
        LogUtil.d(TAG, "TimeConsuming, " + mode + " :" + interval);
    }

    public void forceRefresh() {
        try {
            IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                flinger.transact(ANRManager.RENAME_TRACE_FILES_MSG, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            LogUtil.e(TAG, "Failed to refresh surface", ex);
        }
    }
}

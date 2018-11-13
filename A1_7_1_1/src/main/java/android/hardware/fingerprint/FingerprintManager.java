package android.hardware.fingerprint;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback.Stub;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.AndroidKeyStoreProvider;
import android.util.Log;
import android.util.Slog;
import java.security.Signature;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;

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
public class FingerprintManager {
    private static final boolean DEBUG = false;
    public static final int FINGERPRINT_ACQUIRED_ALREADY_ENROLLED = 1002;
    public static final int FINGERPRINT_ACQUIRED_GOOD = 0;
    public static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;
    public static final int FINGERPRINT_ACQUIRED_INSUFFICIENT = 2;
    public static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;
    public static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;
    public static final int FINGERPRINT_ACQUIRED_TOO_SIMILAR = 1001;
    public static final int FINGERPRINT_ACQUIRED_TOO_SLOW = 4;
    public static final int FINGERPRINT_ACQUIRED_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_ERROR_CANCELED = 5;
    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int FINGERPRINT_ERROR_UNABLE_TO_REMOVE = 6;
    public static final int FINGERPRINT_ERROR_VENDOR_BASE = 1000;
    public static final int FINGERPRINT_MONITOR_TYPE_ERROR = 1;
    public static final int FINGERPRINT_MONITOR_TYPE_POWER = 0;
    public static final int FINGERPRINT_MONITOR_TYPE_TP_PROTECT = 2;
    public static final int FINGERPRINT_SCREENOFF_CANCELED = 8;
    public static final String KEYGUARD_PACKAGENAME = "com.android.keyguard";
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_ENGINEERING_INFO = 1005;
    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ERROR = 104;
    private static final int MSG_IMAGE_INFO_ACQUIRED = 1004;
    private static final int MSG_MONITOR_EVENT_TRIGGERED = 1003;
    private static final int MSG_REMOVED = 105;
    private static final int MSG_TOUCHDOWN_EVNET = 1001;
    private static final int MSG_TOUCHUP_EVNET = 1002;
    private static final String TAG = "FingerprintManager";
    private AuthenticationCallback mAuthenticationCallback;
    private Context mContext;
    private CryptoObject mCryptoObject;
    private EngineeringInfoCallback mEngineeringInfoCallback;
    private EnrollmentCallback mEnrollmentCallback;
    private FingerprintInputCallback mFingerprintInputCallback;
    private Handler mHandler;
    private MonitorEventCallback mMonitorEventCallback;
    private RemovalCallback mRemovalCallback;
    private Fingerprint mRemovalFingerprint;
    private IFingerprintService mService;
    private IFingerprintServiceReceiver mServiceReceiver;
    private IBinder mToken;

    public static abstract class AuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        public void onAuthenticationFailed() {
        }

        public void onAuthenticationAcquired(int acquireInfo) {
        }

        public void onImageInfoAcquired(FingerprintImageInfo info) {
        }
    }

    public static class AuthenticationResult {
        private CryptoObject mCryptoObject;
        private Fingerprint mFingerprint;
        private int mUserId;

        public AuthenticationResult(CryptoObject crypto, Fingerprint fingerprint, int userId) {
            this.mCryptoObject = crypto;
            this.mFingerprint = fingerprint;
            this.mUserId = userId;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public Fingerprint getFingerprint() {
            return this.mFingerprint;
        }

        public int getUserId() {
            return this.mUserId;
        }
    }

    public static final class CryptoObject {
        private final Object mCrypto;

        public CryptoObject(Signature signature) {
            this.mCrypto = signature;
        }

        public CryptoObject(Cipher cipher) {
            this.mCrypto = cipher;
        }

        public CryptoObject(Mac mac) {
            this.mCrypto = mac;
        }

        public Signature getSignature() {
            return this.mCrypto instanceof Signature ? (Signature) this.mCrypto : null;
        }

        public Cipher getCipher() {
            return this.mCrypto instanceof Cipher ? (Cipher) this.mCrypto : null;
        }

        public Mac getMac() {
            return this.mCrypto instanceof Mac ? (Mac) this.mCrypto : null;
        }

        public long getOpId() {
            return this.mCrypto != null ? AndroidKeyStoreProvider.getKeyStoreOperationHandle(this.mCrypto) : 0;
        }
    }

    public interface EngineeringInfoCallback {
        void onEngineeringInfoUpdated(EngineeringInfo engineeringInfo);

        void onError(int i, CharSequence charSequence);
    }

    public static abstract class EnrollmentCallback {
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        }

        public void onEnrollmentProgress(int remaining) {
        }

        public void onTouchUp() {
        }
    }

    public class FingerprintImageInfo {
        public int mQuality;
        public int mScore;
        public int mType;
        final /* synthetic */ FingerprintManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.fingerprint.FingerprintManager.FingerprintImageInfo.<init>(android.hardware.fingerprint.FingerprintManager, int, int, int):void, dex: 
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
        public FingerprintImageInfo(android.hardware.fingerprint.FingerprintManager r1, int r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.fingerprint.FingerprintManager.FingerprintImageInfo.<init>(android.hardware.fingerprint.FingerprintManager, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.FingerprintImageInfo.<init>(android.hardware.fingerprint.FingerprintManager, int, int, int):void");
        }
    }

    public static abstract class FingerprintInputCallback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.FingerprintInputCallback.<init>():void, dex: 
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
        public FingerprintInputCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.FingerprintInputCallback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.FingerprintInputCallback.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.FingerprintInputCallback.onTouchDown():void, dex: 
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
        public void onTouchDown() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.FingerprintInputCallback.onTouchDown():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.FingerprintInputCallback.onTouchDown():void");
        }
    }

    public static abstract class LockoutResetCallback {
        public LockoutResetCallback() {
        }

        public void onLockoutReset() {
        }
    }

    public static abstract class MonitorEventCallback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.MonitorEventCallback.<init>():void, dex: 
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
        public MonitorEventCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.MonitorEventCallback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.MonitorEventCallback.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.MonitorEventCallback.onMonitorEventTriggered(int, java.lang.String):void, dex: 
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
        public void onMonitorEventTriggered(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.MonitorEventCallback.onMonitorEventTriggered(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.MonitorEventCallback.onMonitorEventTriggered(int, java.lang.String):void");
        }
    }

    private class MyHandler extends Handler {
        final /* synthetic */ FingerprintManager this$0;

        /* synthetic */ MyHandler(FingerprintManager this$0, Context context, MyHandler myHandler) {
            this(this$0, context);
        }

        /* synthetic */ MyHandler(FingerprintManager this$0, Looper looper, MyHandler myHandler) {
            this(this$0, looper);
        }

        /* synthetic */ MyHandler(FingerprintManager this$0, Looper looper, Callback callback, boolean async, MyHandler myHandler) {
            this(this$0, looper, callback, async);
        }

        private MyHandler(FingerprintManager this$0, Context context) {
            this.this$0 = this$0;
            super(context.getMainLooper());
        }

        private MyHandler(FingerprintManager this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
        }

        private MyHandler(FingerprintManager this$0, Looper looper, Callback callback, boolean async) {
            this.this$0 = this$0;
            super(looper, callback, async);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    sendEnrollResult((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 101:
                    sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
                case 102:
                    sendAuthenticatedSucceeded((Fingerprint) msg.obj, msg.arg1);
                    return;
                case 103:
                    sendAuthenticatedFailed();
                    return;
                case 104:
                    sendErrorResult(((Long) msg.obj).longValue(), msg.arg1);
                    return;
                case 105:
                    sendRemovedResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 1001:
                    sendTouchDownEvent(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 1002:
                    sendTouchUpEvent(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                    return;
                case 1003:
                    sendMonitorEventTriggered(msg.arg2, (String) msg.obj);
                    return;
                case 1004:
                    sendImageInfo((FingerprintImageInfo) msg.obj);
                    return;
                case 1005:
                    sendEngineeringInfo((EngineeringInfo) msg.obj);
                    return;
                default:
                    return;
            }
        }

        private void sendEngineeringInfo(EngineeringInfo info) {
            if (this.this$0.mEngineeringInfoCallback != null) {
                this.this$0.mEngineeringInfoCallback.onEngineeringInfoUpdated(info);
            }
        }

        private void sendTouchDownEvent(long deviceId, int fingerId, int groupId) {
            if (this.this$0.mFingerprintInputCallback != null) {
                this.this$0.mFingerprintInputCallback.onTouchDown();
            }
        }

        private void sendTouchUpEvent(long deviceId, int fingerId, int groupId) {
            if (this.this$0.mEnrollmentCallback != null) {
                this.this$0.mEnrollmentCallback.onTouchUp();
            }
        }

        private void sendMonitorEventTriggered(int type, String data) {
            if (this.this$0.mMonitorEventCallback != null) {
                this.this$0.mMonitorEventCallback.onMonitorEventTriggered(type, data);
            }
        }

        private void sendImageInfo(FingerprintImageInfo info) {
            if (this.this$0.mAuthenticationCallback != null) {
                this.this$0.mAuthenticationCallback.onImageInfoAcquired(info);
            }
        }

        private void sendRemovedResult(long deviceId, int fingerId, int groupId) {
            if (this.this$0.mRemovalCallback != null) {
                int reqFingerId = this.this$0.mRemovalFingerprint.getFingerId();
                int reqGroupId = this.this$0.mRemovalFingerprint.getGroupId();
                if (reqFingerId != 0 && fingerId != 0 && fingerId != reqFingerId) {
                    Log.w(FingerprintManager.TAG, "Finger id didn't match: " + fingerId + " != " + reqFingerId);
                } else if (groupId != reqGroupId) {
                    Log.w(FingerprintManager.TAG, "Group id didn't match: " + groupId + " != " + reqGroupId);
                } else {
                    if (FingerprintManager.DEBUG) {
                        Log.d(FingerprintManager.TAG, "onRemovalSucceeded");
                    }
                    this.this$0.mRemovalCallback.onRemovalSucceeded(new Fingerprint(null, groupId, fingerId, deviceId));
                }
            }
        }

        private void sendErrorResult(long deviceId, int errMsgId) {
            if (this.this$0.mEnrollmentCallback != null) {
                this.this$0.mEnrollmentCallback.onEnrollmentError(errMsgId, this.this$0.getErrorString(errMsgId));
            } else if (this.this$0.mAuthenticationCallback != null) {
                this.this$0.mAuthenticationCallback.onAuthenticationError(errMsgId, this.this$0.getErrorString(errMsgId));
            } else if (this.this$0.mRemovalCallback != null) {
                this.this$0.mRemovalCallback.onRemovalError(this.this$0.mRemovalFingerprint, errMsgId, this.this$0.getErrorString(errMsgId));
            }
        }

        private void sendEnrollResult(Fingerprint fp, int remaining) {
            if (this.this$0.mEnrollmentCallback != null) {
                this.this$0.mEnrollmentCallback.onEnrollmentProgress(remaining);
            }
        }

        private void sendAuthenticatedSucceeded(Fingerprint fp, int userId) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "sendAuthenticatedSucceeded");
            }
            if (this.this$0.mAuthenticationCallback != null) {
                this.this$0.mAuthenticationCallback.onAuthenticationSucceeded(new AuthenticationResult(this.this$0.mCryptoObject, fp, userId));
            }
        }

        private void sendAuthenticatedFailed() {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "sendAuthenticatedFailed");
            }
            if (this.this$0.mAuthenticationCallback != null) {
                this.this$0.mAuthenticationCallback.onAuthenticationFailed();
            }
        }

        private void sendAcquiredResult(long deviceId, int acquireInfo) {
            if (FingerprintManager.DEBUG) {
                Log.d(FingerprintManager.TAG, "sendAcquiredResult");
            }
            if (this.this$0.mAuthenticationCallback != null) {
                this.this$0.mAuthenticationCallback.onAuthenticationAcquired(acquireInfo);
            }
            String msg = this.this$0.getAcquiredString(acquireInfo);
            if (msg != null) {
                if (this.this$0.mEnrollmentCallback != null) {
                    this.this$0.mEnrollmentCallback.onEnrollmentHelp(acquireInfo, msg);
                } else if (this.this$0.mAuthenticationCallback != null) {
                    this.this$0.mAuthenticationCallback.onAuthenticationHelp(acquireInfo, msg);
                }
            }
        }
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private CryptoObject mCrypto;
        final /* synthetic */ FingerprintManager this$0;

        public OnAuthenticationCancelListener(FingerprintManager this$0, CryptoObject crypto) {
            this.this$0 = this$0;
            this.mCrypto = crypto;
        }

        public void onCancel() {
            this.this$0.cancelAuthentication(this.mCrypto);
        }
    }

    private class OnEnrollCancelListener implements OnCancelListener {
        final /* synthetic */ FingerprintManager this$0;

        /* synthetic */ OnEnrollCancelListener(FingerprintManager this$0, OnEnrollCancelListener onEnrollCancelListener) {
            this(this$0);
        }

        private OnEnrollCancelListener(FingerprintManager this$0) {
            this.this$0 = this$0;
        }

        public void onCancel() {
            this.this$0.cancelEnrollment();
        }
    }

    private class OnTouchEventMonitorCancelListener implements OnCancelListener {
        final /* synthetic */ FingerprintManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.hardware.fingerprint.FingerprintManager.OnTouchEventMonitorCancelListener.<init>(android.hardware.fingerprint.FingerprintManager):void, dex: 
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
        private OnTouchEventMonitorCancelListener(android.hardware.fingerprint.FingerprintManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.hardware.fingerprint.FingerprintManager.OnTouchEventMonitorCancelListener.<init>(android.hardware.fingerprint.FingerprintManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.OnTouchEventMonitorCancelListener.<init>(android.hardware.fingerprint.FingerprintManager):void");
        }

        /* synthetic */ OnTouchEventMonitorCancelListener(FingerprintManager this$0, OnTouchEventMonitorCancelListener onTouchEventMonitorCancelListener) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.hardware.fingerprint.FingerprintManager.OnTouchEventMonitorCancelListener.onCancel():void, dex: 
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
        public void onCancel() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.hardware.fingerprint.FingerprintManager.OnTouchEventMonitorCancelListener.onCancel():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.OnTouchEventMonitorCancelListener.onCancel():void");
        }
    }

    public static abstract class RemovalCallback {
        public RemovalCallback() {
        }

        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
        }

        public void onRemovalSucceeded(Fingerprint fingerprint) {
        }
    }

    public interface ScreenOnCallback {
        public static final int VERIFY_ABORT = 0;
        public static final int VERIFY_FAILED = -1;
        public static final int VERIFY_SUCCESS = 1;

        void onVerifyDone(int i);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.fingerprint.FingerprintManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.fingerprint.FingerprintManager.<clinit>():void");
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler) {
        authenticate(crypto, cancel, flags, callback, handler, UserHandle.myUserId());
    }

    private void useHandler(Handler handler) {
        if (KEYGUARD_PACKAGENAME.equals(this.mContext.getOpPackageName())) {
            if (handler != null) {
                if (DEBUG) {
                    Log.d(TAG, "keyguard Handler");
                }
                this.mHandler = new MyHandler(this, handler.getLooper(), null, true, null);
            } else {
                if (DEBUG) {
                    Log.d(TAG, "new Handler for keyguard");
                }
                this.mHandler = new MyHandler(this, this.mContext.getMainLooper(), null, true, null);
            }
            return;
        }
        if (handler != null) {
            this.mHandler = new MyHandler(this, handler.getLooper(), null);
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new MyHandler(this, this.mContext.getMainLooper(), null);
        }
    }

    public void authenticate(CryptoObject crypto, CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int userId) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an authentication callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "authentication already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(this, crypto));
        }
        if (this.mService != null) {
            try {
                useHandler(handler);
                this.mAuthenticationCallback = callback;
                this.mCryptoObject = crypto;
                this.mService.authenticate(this.mToken, crypto != null ? crypto.getOpId() : 0, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception while authenticating: ", e);
                if (callback != null) {
                    callback.onAuthenticationError(1, getErrorString(1));
                }
            }
        }
    }

    public void enroll(byte[] token, CancellationSignal cancel, int flags, int userId, EnrollmentCallback callback) {
        if (userId == -2) {
            userId = getCurrentUserId();
        }
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an enrollment callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "enrollment already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnEnrollCancelListener(this, null));
        }
        if (this.mService != null) {
            try {
                this.mEnrollmentCallback = callback;
                this.mService.enroll(this.mToken, token, userId, this.mServiceReceiver, flags, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in enroll: ", e);
                if (callback != null) {
                    callback.onEnrollmentError(1, getErrorString(1));
                }
            }
        }
    }

    public long preEnroll() {
        long result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.preEnroll(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in enroll: ", e);
            return result;
        }
    }

    public boolean pauseEnroll() {
        int result = 0;
        if (this.mService != null) {
            try {
                result = this.mService.pauseEnroll();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in pauseEnroll: ", e);
            }
        }
        if (result < 0) {
            return false;
        }
        return true;
    }

    public boolean continueEnroll() {
        int result = 0;
        if (this.mService != null) {
            try {
                result = this.mService.continueEnroll();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in continueEnroll: ", e);
            }
        }
        if (result < 0) {
            return false;
        }
        return true;
    }

    public int postEnroll() {
        int result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.postEnroll(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in post enroll: ", e);
            return result;
        }
    }

    public void setActiveUser(int userId) {
        if (this.mService != null) {
            try {
                this.mService.setActiveUser(userId);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in setActiveUser: ", e);
            }
        }
    }

    public void remove(Fingerprint fp, int userId, RemovalCallback callback) {
        if (this.mService != null) {
            try {
                this.mRemovalCallback = callback;
                this.mRemovalFingerprint = fp;
                this.mService.remove(this.mToken, fp.getFingerId(), fp.getGroupId(), userId, this.mServiceReceiver, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in remove: ", e);
                if (callback != null) {
                    callback.onRemovalError(fp, 1, getErrorString(1));
                }
            }
        }
    }

    public void rename(int fpId, int userId, String newName) {
        if (this.mService != null) {
            try {
                this.mService.rename(fpId, userId, newName);
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in rename(): ", e);
                return;
            }
        }
        Log.w(TAG, "rename(): Service not connected!");
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        if (this.mService != null) {
            try {
                return this.mService.getEnrolledFingerprints(userId, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getEnrolledFingerprints: ", e);
            }
        }
        return null;
    }

    public List<Fingerprint> getEnrolledFingerprints() {
        return getEnrolledFingerprints(UserHandle.myUserId());
    }

    public boolean hasEnrolledFingerprints() {
        if (this.mService != null) {
            try {
                return this.mService.hasEnrolledFingerprints(UserHandle.myUserId(), this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in hasEnrolledFingerprints: ", e);
            }
        }
        return false;
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (this.mService != null) {
            try {
                return this.mService.hasEnrolledFingerprints(userId, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in hasEnrolledFingerprints: ", e);
            }
        }
        return false;
    }

    public boolean isHardwareDetected() {
        if (this.mService != null) {
            try {
                return this.mService.isHardwareDetected(0, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in isFingerprintHardwareDetected(): ", e);
            }
        } else {
            Log.w(TAG, "isFingerprintHardwareDetected(): Service not connected!");
            return false;
        }
    }

    public long getAuthenticatorId() {
        if (this.mService != null) {
            try {
                return this.mService.getAuthenticatorId(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getAuthenticatorId(): ", e);
            }
        } else {
            Log.w(TAG, "getAuthenticatorId(): Service not connected!");
            return 0;
        }
    }

    public void resetTimeout(byte[] token) {
        if (this.mService != null) {
            try {
                Log.w(TAG, "resetTimeout, packageName = " + this.mContext.getOpPackageName());
                this.mService.resetTimeout(token);
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in resetTimeout(): ", e);
                return;
            }
        }
        Log.w(TAG, "resetTimeout(): Service not connected!");
    }

    public void addLockoutResetCallback(final LockoutResetCallback callback) {
        if (this.mService != null) {
            try {
                final PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
                this.mService.addLockoutResetCallback(new Stub() {
                    public void onLockoutReset(long deviceId) throws RemoteException {
                        final WakeLock wakeLock = powerManager.newWakeLock(1, "lockoutResetCallback");
                        wakeLock.acquire();
                        Handler -get6 = FingerprintManager.this.mHandler;
                        final LockoutResetCallback lockoutResetCallback = callback;
                        -get6.post(new Runnable() {
                            public void run() {
                                try {
                                    lockoutResetCallback.onLockoutReset();
                                } finally {
                                    wakeLock.release();
                                }
                            }
                        });
                    }
                });
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in addLockoutResetCallback(): ", e);
                return;
            }
        }
        Log.w(TAG, "addLockoutResetCallback(): Service not connected!");
    }

    public int getEnrollmentTotalTimes() {
        int result = 0;
        if (this.mService == null) {
            return result;
        }
        try {
            return this.mService.getEnrollmentTotalTimes(this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in enroll: ", e);
            return result;
        }
    }

    public void setTouchEventListener(FingerprintInputCallback callback, CancellationSignal cancel) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an setTouchEventListener callback");
        }
        if (cancel != null) {
            if (cancel.isCanceled()) {
                Log.w(TAG, "setTouchEventListener already canceled");
                return;
            }
            cancel.setOnCancelListener(new OnTouchEventMonitorCancelListener(this, null));
        }
        if (this.mService != null) {
            this.mFingerprintInputCallback = callback;
            try {
                this.mService.setTouchEventListener(this.mToken, this.mServiceReceiver, UserHandle.myUserId(), this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.e(TAG, "Remote exception in setTouchEventListener(): ", e);
            }
        }
    }

    private void cancelTouchEventListener() {
        if (this.mService != null) {
            try {
                this.mService.cancelTouchEventListener(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.w(TAG, "Remote exception while canceling touchevent");
                }
            }
        }
    }

    public void setMonitorEventListener(MonitorEventCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an setMonitorEventListener callback");
        } else if (this.mService != null) {
            this.mMonitorEventCallback = callback;
        }
    }

    public void pauseIdentify() {
        if (this.mService != null) {
            try {
                int result = this.mService.pauseIdentify(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in pauseIdentify: ", e);
            }
        }
    }

    public void continueIdentify() {
        if (this.mService != null) {
            try {
                int result = this.mService.continueIdentify(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in continueIdentify: ", e);
            }
        }
    }

    public void finishUnLockedScreen(boolean authenticated) {
        if (this.mService != null) {
            try {
                this.mService.finishUnLockedScreen(authenticated, this.mContext.getOpPackageName());
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "Remote exception in finishUnLockedScreen(): ", e);
                return;
            }
        }
        Log.w(TAG, "finishUnLockedScreen(): Service not connected!");
    }

    public int getAlikeyStatus() {
        if (this.mService != null) {
            try {
                return this.mService.getAlikeyStatus();
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getAlikeyStatus(): ", e);
            }
        } else {
            Log.w(TAG, "getAlikeyStatus(): Service not connected!");
            return -1;
        }
    }

    public int getEngineeringInfo(EngineeringInfoCallback callback, int type) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an getEngineeringInfo callback");
        } else if (this.mService != null) {
            try {
                this.mEngineeringInfoCallback = callback;
                return this.mService.getEngineeringInfo(this.mToken, this.mContext.getOpPackageName(), UserHandle.myUserId(), this.mServiceReceiver, type);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getEngineeringInfo(): ", e);
            }
        } else {
            Log.w(TAG, "getEngineeringInfo(): Service not connected!");
            return -1;
        }
    }

    public void cancelGetEngineeringInfo(int type) {
        if (this.mService != null) {
            try {
                this.mService.cancelGetEngineeringInfo(this.mToken, this.mContext.getOpPackageName(), type);
                this.mEngineeringInfoCallback = null;
                return;
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in cancelgetEngineeringInfo(): ", e);
                return;
            }
        }
        Log.w(TAG, "cancelgetEngineeringInfo(): Service not connected!");
    }

    public FingerprintManager(Context context, IFingerprintService service) {
        this.mToken = new Binder();
        this.mServiceReceiver = new IFingerprintServiceReceiver.Stub() {
            public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
                FingerprintManager.this.mHandler.obtainMessage(100, remaining, 0, new Fingerprint(null, groupId, fingerId, deviceId)).sendToTarget();
            }

            public void onAcquired(long deviceId, int acquireInfo) {
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onAcquired");
                }
                FingerprintManager.this.mHandler.obtainMessage(101, acquireInfo, 0, Long.valueOf(deviceId)).sendToTarget();
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onAcquired finished");
                }
            }

            public void onAuthenticationSucceeded(long deviceId, Fingerprint fp, int userId) {
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onAuthenticationSucceeded");
                }
                FingerprintManager.this.mHandler.obtainMessage(102, userId, 0, fp).sendToTarget();
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onAuthenticationSucceeded finished");
                }
            }

            public void onAuthenticationFailed(long deviceId) {
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onAuthenticationFailed");
                }
                FingerprintManager.this.mHandler.obtainMessage(103).sendToTarget();
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onAuthenticationFailed finished");
                }
            }

            public void onError(long deviceId, int error) {
                FingerprintManager.this.mHandler.obtainMessage(104, error, 0, Long.valueOf(deviceId)).sendToTarget();
            }

            public void onRemoved(long deviceId, int fingerId, int groupId) {
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onRemoved");
                }
                FingerprintManager.this.mHandler.obtainMessage(105, fingerId, groupId, Long.valueOf(deviceId)).sendToTarget();
                if (FingerprintManager.DEBUG) {
                    Log.d(FingerprintManager.TAG, "onRemoved finished");
                }
            }

            public void onEngineeringInfoUpdated(EngineeringInfo info) {
                FingerprintManager.this.mHandler.obtainMessage(1005, 0, 0, info).sendToTarget();
            }

            public void onTouchDown(long deviceId) {
                FingerprintManager.this.mHandler.obtainMessage(1001, 0, 0, Long.valueOf(deviceId)).sendToTarget();
            }

            public void onTouchUp(long deviceId) {
                FingerprintManager.this.mHandler.obtainMessage(1002, 0, 0, Long.valueOf(deviceId)).sendToTarget();
            }

            public void onMonitorEventTriggered(int type, String data) {
                FingerprintManager.this.mHandler.obtainMessage(1003, 0, type, data).sendToTarget();
            }

            public void onImageInfoAcquired(int type, int quality, int match_score) {
                FingerprintManager.this.mHandler.obtainMessage(1004, 0, 0, new FingerprintImageInfo(FingerprintManager.this, type, quality, match_score)).sendToTarget();
            }
        };
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "FingerprintManagerService was null");
        }
        this.mHandler = new MyHandler(this, context, null);
    }

    private int getCurrentUserId() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get current user id\n");
            return -10000;
        }
    }

    private void cancelEnrollment() {
        if (this.mService != null) {
            try {
                this.mService.cancelEnrollment(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.w(TAG, "Remote exception while canceling enrollment");
                }
            }
        }
    }

    private void cancelAuthentication(CryptoObject cryptoObject) {
        if (this.mService != null) {
            try {
                this.mService.cancelAuthentication(this.mToken, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                if (DEBUG) {
                    Log.w(TAG, "Remote exception while canceling enrollment");
                }
            }
        }
    }

    private String getErrorString(int errMsg) {
        switch (errMsg) {
            case 1:
                return this.mContext.getString(17039852);
            case 2:
                return this.mContext.getString(17039857);
            case 3:
                return this.mContext.getString(17039854);
            case 4:
                return this.mContext.getString(17039853);
            case 5:
                return this.mContext.getString(17039855);
            case 7:
                return this.mContext.getString(17039856);
            case 8:
                return "restart authenticate";
            default:
                if (errMsg >= 1000) {
                    int msgNumber = errMsg - 1000;
                    String[] msgArray = this.mContext.getResources().getStringArray(17236071);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    private String getAcquiredString(int acquireInfo) {
        switch (acquireInfo) {
            case 0:
                return null;
            case 1:
                return this.mContext.getString(17039847);
            case 2:
                return this.mContext.getString(17039848);
            case 3:
                return this.mContext.getString(17039849);
            case 4:
                return this.mContext.getString(17039851);
            case 5:
                return this.mContext.getString(17039850);
            case 1002:
                return "already enrolled finger";
            default:
                if (acquireInfo >= 1000) {
                    int msgNumber = acquireInfo - 1000;
                    String[] msgArray = this.mContext.getResources().getStringArray(17236070);
                    if (msgNumber < msgArray.length) {
                        return msgArray[msgNumber];
                    }
                }
                return null;
        }
    }

    public long getLockoutAttemptDeadline() {
        if (this.mService != null) {
            try {
                return this.mService.getLockoutAttemptDeadline(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getLockoutAttemptDeadline(): ", e);
            }
        } else {
            Log.w(TAG, "getLockoutAttemptDeadline(): Service not connected!");
            return -1;
        }
    }

    public int getFailedAttempts() {
        if (this.mService != null) {
            try {
                return this.mService.getFailedAttempts(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getFailedAttempts(): ", e);
            }
        } else {
            Log.w(TAG, "getFailedAttempts(): Service not connected!");
            return -1;
        }
    }
}

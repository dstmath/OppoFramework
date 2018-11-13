package com.android.internal.widget;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.IStrongAuthTracker;
import android.app.trust.TrustManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Button;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.Protocol;
import com.android.internal.widget.ICheckCredentialProgressCallback.Stub;
import com.android.internal.widget.LockPatternView.Cell;
import com.google.android.collect.Lists;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import libcore.util.HexEncoding;

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
    */
public class LockPatternUtils {
    @Deprecated
    public static final String BIOMETRIC_WEAK_EVER_CHOSEN_KEY = "lockscreen.biometricweakeverchosen";
    private static final boolean DEBUG = false;
    public static final String DISABLE_LOCKSCREEN_KEY = "lockscreen.disabled";
    private static final String ENABLED_TRUST_AGENTS = "lockscreen.enabledtrustagents";
    public static final int FAILED_ATTEMPTS_BEFORE_RESET = 20;
    public static final int FAILED_ATTEMPTS_BEFORE_WIPE_GRACE = 5;
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000;
    private static final String IS_TRUST_USUALLY_MANAGED = "lockscreen.istrustusuallymanaged";
    public static final String LEGACY_LOCK_PATTERN_ENABLED = "legacy_lock_pattern_enabled";
    public static final String LOCKOUT_ATTEMPT_DEADLINE = "lockscreen.lockoutattemptdeadline";
    public static final String LOCKOUT_ATTEMPT_TIMEOUT_MS = "lockscreen.lockoutattempttimeoutmss";
    @Deprecated
    public static final String LOCKOUT_PERMANENT_KEY = "lockscreen.lockedoutpermanently";
    @Deprecated
    public static final String LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK = "lockscreen.biometric_weak_fallback";
    public static final String LOCKSCREEN_OPTIONS = "lockscreen.options";
    public static final String LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS = "lockscreen.power_button_instantly_locks";
    public static final String LOCKSCREEN_WEAK_FALLBACK = "lockscreen.weak_fallback";
    public static final String LOCKSCREEN_WEAK_FALLBACK_FOR = "lockscreen.weak_fallback_for";
    @Deprecated
    public static final String LOCKSCREEN_WIDGETS_ENABLED = "lockscreen.widgets_enabled";
    public static final String LOCK_PASSWORD_SALT_KEY = "lockscreen.password_salt";
    private static final String LOCK_SCREEN_DEVICE_OWNER_INFO = "lockscreen.device_owner_info";
    private static final String LOCK_SCREEN_OWNER_INFO = "lock_screen_owner_info";
    private static final String LOCK_SCREEN_OWNER_INFO_ENABLED = "lock_screen_owner_info_enabled";
    public static final int MAX_ALLOWED_SEQUENCE = 3;
    public static final int MIN_LOCK_PASSWORD_SIZE = 4;
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    public static final int MIN_PATTERN_REGISTER_FAIL = 4;
    public static final String PASSWORD_HISTORY_KEY = "lockscreen.passwordhistory";
    @Deprecated
    public static final String PASSWORD_TYPE_ALTERNATE_KEY = "lockscreen.password_type_alternate";
    public static final String PASSWORD_TYPE_KEY = "lockscreen.password_type";
    public static final String PATTERN_EVER_CHOSEN_KEY = "lockscreen.patterneverchosen";
    public static final String PROFILE_KEY_NAME_DECRYPT = "profile_key_name_decrypt_";
    public static final String PROFILE_KEY_NAME_ENCRYPT = "profile_key_name_encrypt_";
    public static final String QUALCOMM_TIMEOUT_FLAG = "lockscreen.qualcomm_timeout_flag";
    public static final String SETTINGS_COMMAND_KEY = "settings_command_key";
    public static final String SETTINGS_COMMAND_VALUE = "settings_command_value";
    private static final String TAG = "LockPatternUtils";
    public static final String TYPE_VOICE_UNLOCK = "voice_unlock";
    public static final String VERIFY_PWD_ATTEMPT_DEADLINE = "lockscreen.verifypwdattemptdeadline";
    public static final int VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS = 172800000;
    public static final String VOICE_WEAK_FALLBACK_SET_KEY = "lockscreen.voice_weak_fallback_set";
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private DevicePolicyManager mDevicePolicyManager;
    private final Handler mHandler;
    private ILockSettings mLockSettingsService;
    private UserManager mUserManager;

    public interface CheckCredentialProgressCallback {
        void onEarlyMatched();
    }

    /* renamed from: com.android.internal.widget.LockPatternUtils$2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ LockPatternUtils this$0;
        final /* synthetic */ CheckCredentialProgressCallback val$callback;

        final /* synthetic */ class -void_onCredentialVerified__LambdaImpl0 implements Runnable {
            /* renamed from: val$-lambdaCtx */
            private /* synthetic */ CheckCredentialProgressCallback f20val$-lambdaCtx;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternUtils.2.-void_onCredentialVerified__LambdaImpl0.<init>(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void, dex: 
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
            public /* synthetic */ -void_onCredentialVerified__LambdaImpl0(com.android.internal.widget.LockPatternUtils.CheckCredentialProgressCallback r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternUtils.2.-void_onCredentialVerified__LambdaImpl0.<init>(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternUtils.2.-void_onCredentialVerified__LambdaImpl0.<init>(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternUtils.2.-void_onCredentialVerified__LambdaImpl0.run():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternUtils.2.-void_onCredentialVerified__LambdaImpl0.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternUtils.2.-void_onCredentialVerified__LambdaImpl0.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.LockPatternUtils.2.-com_android_internal_widget_LockPatternUtils$2-mthref-0(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void, dex: 
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
        /* renamed from: -com_android_internal_widget_LockPatternUtils$2-mthref-0 */
        static /* synthetic */ void m662-com_android_internal_widget_LockPatternUtils$2-mthref-0(com.android.internal.widget.LockPatternUtils.CheckCredentialProgressCallback r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.LockPatternUtils.2.-com_android_internal_widget_LockPatternUtils$2-mthref-0(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternUtils.2.-com_android_internal_widget_LockPatternUtils$2-mthref-0(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternUtils.2.<init>(com.android.internal.widget.LockPatternUtils, com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void, dex: 
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
        AnonymousClass2(com.android.internal.widget.LockPatternUtils r1, com.android.internal.widget.LockPatternUtils.CheckCredentialProgressCallback r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.LockPatternUtils.2.<init>(com.android.internal.widget.LockPatternUtils, com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternUtils.2.<init>(com.android.internal.widget.LockPatternUtils, com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternUtils.2.onCredentialVerified():void, dex: 
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
        public void onCredentialVerified() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.LockPatternUtils.2.onCredentialVerified():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternUtils.2.onCredentialVerified():void");
        }
    }

    public static final class RequestThrottledException extends Exception {
        private int mTimeoutMs;

        public RequestThrottledException(int timeoutMs) {
            this.mTimeoutMs = timeoutMs;
        }

        public int getTimeoutMs() {
            return this.mTimeoutMs;
        }
    }

    public static class StrongAuthTracker {
        private static final int ALLOWING_FINGERPRINT = 4;
        public static final int SOME_AUTH_REQUIRED_AFTER_USER_REQUEST = 4;
        public static final int STRONG_AUTH_NOT_REQUIRED = 0;
        public static final int STRONG_AUTH_REQUIRED_AFTER_BOOT = 1;
        public static final int STRONG_AUTH_REQUIRED_AFTER_DPM_LOCK_NOW = 2;
        public static final int STRONG_AUTH_REQUIRED_AFTER_LOCKOUT = 8;
        private final int mDefaultStrongAuthFlags;
        private final H mHandler;
        private final SparseIntArray mStrongAuthRequiredForUser;
        protected final IStrongAuthTracker.Stub mStub;

        /* renamed from: com.android.internal.widget.LockPatternUtils$StrongAuthTracker$1 */
        class AnonymousClass1 extends IStrongAuthTracker.Stub {
            final /* synthetic */ StrongAuthTracker this$1;

            AnonymousClass1(StrongAuthTracker this$1) {
                this.this$1 = this$1;
            }

            public void onStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
                this.this$1.mHandler.obtainMessage(1, strongAuthFlags, userId).sendToTarget();
            }
        }

        private class H extends Handler {
            static final int MSG_ON_STRONG_AUTH_REQUIRED_CHANGED = 1;
            final /* synthetic */ StrongAuthTracker this$1;

            public H(StrongAuthTracker this$1, Looper looper) {
                this.this$1 = this$1;
                super(looper);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        this.this$1.handleStrongAuthRequiredChanged(msg.arg1, msg.arg2);
                        return;
                    default:
                        return;
                }
            }
        }

        public StrongAuthTracker(Context context) {
            this(context, Looper.myLooper());
        }

        public StrongAuthTracker(Context context, Looper looper) {
            this.mStrongAuthRequiredForUser = new SparseIntArray();
            this.mStub = new AnonymousClass1(this);
            this.mHandler = new H(this, looper);
            this.mDefaultStrongAuthFlags = getDefaultFlags(context);
        }

        public static int getDefaultFlags(Context context) {
            return context.getResources().getBoolean(R.bool.config_strongAuthRequiredOnBoot) ? 1 : 0;
        }

        public int getStrongAuthForUser(int userId) {
            return this.mStrongAuthRequiredForUser.get(userId, this.mDefaultStrongAuthFlags);
        }

        public boolean isTrustAllowedForUser(int userId) {
            return getStrongAuthForUser(userId) == 0;
        }

        public boolean isFingerprintAllowedForUser(int userId) {
            return (getStrongAuthForUser(userId) & -5) == 0;
        }

        public void onStrongAuthRequiredChanged(int userId) {
        }

        protected void handleStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
            if (strongAuthFlags != getStrongAuthForUser(userId)) {
                if (strongAuthFlags == this.mDefaultStrongAuthFlags) {
                    this.mStrongAuthRequiredForUser.delete(userId);
                } else {
                    this.mStrongAuthRequiredForUser.put(userId, strongAuthFlags);
                }
                onStrongAuthRequiredChanged(userId);
            }
        }
    }

    public boolean isTrustUsuallyManaged(int userId) {
        if (this.mLockSettingsService instanceof ILockSettings.Stub) {
            try {
                return getLockSettings().getBoolean(IS_TRUST_USUALLY_MANAGED, false, userId);
            } catch (RemoteException e) {
                return false;
            }
        }
        throw new IllegalStateException("May only be called by TrustManagerService. Use TrustManager.isTrustUsuallyManaged()");
    }

    public void setTrustUsuallyManaged(boolean managed, int userId) {
        try {
            getLockSettings().setBoolean(IS_TRUST_USUALLY_MANAGED, managed, userId);
        } catch (RemoteException e) {
        }
    }

    public void userPresent(int userId) {
        try {
            getLockSettings().userPresent(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DevicePolicyManager getDevicePolicyManager() {
        if (this.mDevicePolicyManager == null) {
            this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
            if (this.mDevicePolicyManager == null) {
                Log.e(TAG, "Can't get DevicePolicyManagerService: is it running?", new IllegalStateException("Stack trace:"));
            }
        }
        return this.mDevicePolicyManager;
    }

    private UserManager getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(this.mContext);
        }
        return this.mUserManager;
    }

    private TrustManager getTrustManager() {
        TrustManager trust = (TrustManager) this.mContext.getSystemService("trust");
        if (trust == null) {
            Log.e(TAG, "Can't get TrustManagerService: is it running?", new IllegalStateException("Stack trace:"));
        }
        return trust;
    }

    public LockPatternUtils(Context context) {
        Handler handler = null;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        Looper looper = Looper.myLooper();
        if (looper != null) {
            handler = new Handler(looper);
        }
        this.mHandler = handler;
    }

    private ILockSettings getLockSettings() {
        if (this.mLockSettingsService == null) {
            this.mLockSettingsService = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
        }
        return this.mLockSettingsService;
    }

    public int getRequestedMinimumPasswordLength(int userId) {
        return getDevicePolicyManager().getPasswordMinimumLength(null, userId);
    }

    public int getRequestedPasswordQuality(int userId) {
        return getDevicePolicyManager().getPasswordQuality(null, userId);
    }

    private int getRequestedPasswordHistoryLength(int userId) {
        return getDevicePolicyManager().getPasswordHistoryLength(null, userId);
    }

    public int getRequestedPasswordMinimumLetters(int userId) {
        return getDevicePolicyManager().getPasswordMinimumLetters(null, userId);
    }

    public int getRequestedPasswordMinimumUpperCase(int userId) {
        return getDevicePolicyManager().getPasswordMinimumUpperCase(null, userId);
    }

    public int getRequestedPasswordMinimumLowerCase(int userId) {
        return getDevicePolicyManager().getPasswordMinimumLowerCase(null, userId);
    }

    public int getRequestedPasswordMinimumNumeric(int userId) {
        return getDevicePolicyManager().getPasswordMinimumNumeric(null, userId);
    }

    public int getRequestedPasswordMinimumSymbols(int userId) {
        return getDevicePolicyManager().getPasswordMinimumSymbols(null, userId);
    }

    public int getRequestedPasswordMinimumNonLetter(int userId) {
        return getDevicePolicyManager().getPasswordMinimumNonLetter(null, userId);
    }

    public void reportFailedPasswordAttempt(int userId) {
        getDevicePolicyManager().reportFailedPasswordAttempt(userId);
        getTrustManager().reportUnlockAttempt(false, userId);
    }

    public void reportSuccessfulPasswordAttempt(int userId) {
        getDevicePolicyManager().reportSuccessfulPasswordAttempt(userId);
        getTrustManager().reportUnlockAttempt(true, userId);
    }

    public int getCurrentFailedPasswordAttempts(int userId) {
        return getDevicePolicyManager().getCurrentFailedPasswordAttempts(userId);
    }

    public int getMaximumFailedPasswordsForWipe(int userId) {
        return getDevicePolicyManager().getMaximumFailedPasswordsForWipe(null, userId);
    }

    public byte[] verifyPattern(List<Cell> pattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyPattern(patternToString(pattern), challenge, userId);
            if (response == null) {
                return null;
            }
            if (response.getResponseCode() == 0) {
                setVerifyPwdAttemptDeadline(userId, 0, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
                return response.getPayload();
            } else if (response.getResponseCode() != 1) {
                return null;
            } else {
                throw new RequestThrottledException(response.getTimeout());
            }
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean checkPattern(List<Cell> pattern, int userId) throws RequestThrottledException {
        return checkPattern(pattern, userId, null);
    }

    public boolean checkPattern(List<Cell> pattern, int userId, CheckCredentialProgressCallback progressCallback) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().checkPattern(patternToString(pattern), userId, wrapCallback(progressCallback));
            if (response.getResponseCode() == 0) {
                setVerifyPwdAttemptDeadline(userId, 0, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
                return true;
            } else if (response.getResponseCode() != 1) {
                return false;
            } else {
                throw new RequestThrottledException(response.getTimeout());
            }
        } catch (RemoteException e) {
            return false;
        }
    }

    public byte[] verifyPassword(String password, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyPassword(password, challenge, userId);
            if (response.getResponseCode() == 0) {
                setVerifyPwdAttemptDeadline(userId, 0, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
                return response.getPayload();
            } else if (response.getResponseCode() != 1) {
                return null;
            } else {
                throw new RequestThrottledException(response.getTimeout());
            }
        } catch (RemoteException e) {
            return null;
        }
    }

    public byte[] verifyTiedProfileChallenge(String password, boolean isPattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyTiedProfileChallenge(password, isPattern, challenge, userId);
            if (response.getResponseCode() == 0) {
                return response.getPayload();
            }
            if (response.getResponseCode() != 1) {
                return null;
            }
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean checkPassword(String password, int userId) throws RequestThrottledException {
        return checkPassword(password, userId, null);
    }

    public boolean checkPassword(String password, int userId, CheckCredentialProgressCallback progressCallback) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().checkPassword(password, userId, wrapCallback(progressCallback));
            if (response.getResponseCode() == 0) {
                setVerifyPwdAttemptDeadline(userId, 0, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
                return true;
            } else if (response.getResponseCode() != 1) {
                return false;
            } else {
                throw new RequestThrottledException(response.getTimeout());
            }
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean checkVoldPassword(int userId) {
        try {
            return getLockSettings().checkVoldPassword(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean checkPasswordHistory(String password, int userId) {
        String passwordHashString = new String(passwordToHash(password, userId), StandardCharsets.UTF_8);
        String passwordHistory = getString(PASSWORD_HISTORY_KEY, userId);
        if (passwordHistory == null) {
            return false;
        }
        int passwordHashLength = passwordHashString.length();
        int passwordHistoryLength = getRequestedPasswordHistoryLength(userId);
        if (passwordHistoryLength == 0) {
            return false;
        }
        int neededPasswordHistoryLength = ((passwordHashLength * passwordHistoryLength) + passwordHistoryLength) - 1;
        if (passwordHistory.length() > neededPasswordHistoryLength) {
            passwordHistory = passwordHistory.substring(0, neededPasswordHistoryLength);
        }
        return passwordHistory.contains(passwordHashString);
    }

    private boolean savedPatternExists(int userId) {
        try {
            return getLockSettings().havePattern(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean savedPasswordExists(int userId) {
        try {
            return getLockSettings().havePassword(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isPatternEverChosen(int userId) {
        return getBoolean(PATTERN_EVER_CHOSEN_KEY, false, userId);
    }

    public int getActivePasswordQuality(int userId) {
        int quality = getKeyguardStoredPasswordQuality(userId);
        if (isLockPasswordEnabled(quality, userId) || isLockPatternEnabled(quality, userId)) {
            return quality;
        }
        return 0;
    }

    public void resetKeyStore(int userId) {
        try {
            getLockSettings().resetKeyStore(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't reset keystore " + e);
        }
    }

    public void clearLock(int userHandle) {
        clearLock(false, userHandle);
    }

    public void clearLock(boolean isFallback, int userHandle) {
        if (!isFallback) {
            deleteGallery(userHandle);
        }
        setLong(PASSWORD_TYPE_KEY, 0, userHandle);
        setVerifyPwdAttemptDeadline(userHandle, -1, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
        try {
            getLockSettings().setLockPassword(null, null, userHandle);
            getLockSettings().setLockPattern(null, null, userHandle);
        } catch (RemoteException e) {
        }
        if (userHandle == 0) {
            updateEncryptionPassword(1, null);
            setCredentialRequiredToDecrypt(false);
        }
        onAfterChangingPassword(userHandle);
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        setBoolean(DISABLE_LOCKSCREEN_KEY, disable, userId);
    }

    public boolean isLockScreenDisabled(int userId) {
        if (isSecure(userId)) {
            return false;
        }
        return getBoolean(DISABLE_LOCKSCREEN_KEY, false, userId);
    }

    public void saveLockPattern(List<Cell> pattern, int userId) {
        saveLockPattern(pattern, null, userId);
    }

    public void saveLockPattern(List<Cell> pattern, String savedPattern, int userId) {
        saveLockPattern(pattern, savedPattern, false, null, userId);
    }

    public void saveLockPattern(List<Cell> pattern, String savedPattern, boolean isFallback, String fallbackFor, int userId) {
        if (pattern != null) {
            try {
                if (pattern.size() >= 4) {
                    setLong(PASSWORD_TYPE_KEY, 65536, userId);
                    getLockSettings().setLockPattern(patternToString(pattern), savedPattern, userId);
                    setVerifyPwdAttemptDeadline(userId, 0, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
                    DevicePolicyManager dpm = getDevicePolicyManager();
                    if (userId == 0 && isDeviceEncryptionEnabled()) {
                        if (shouldEncryptWithCredentials(true)) {
                            updateEncryptionPassword(2, patternToString(pattern));
                        } else {
                            clearEncryptionPassword();
                        }
                    }
                    setBoolean(PATTERN_EVER_CHOSEN_KEY, true, userId);
                    if (isFallback) {
                        if (fallbackFor.equals(TYPE_VOICE_UNLOCK)) {
                            setLong(PASSWORD_TYPE_KEY, 16384, userId);
                            setLong(PASSWORD_TYPE_ALTERNATE_KEY, 65536, userId);
                            finishVoiceWeak(userId);
                            dpm.setActivePasswordState(16384, 0, 0, 0, 0, 0, 0, 0, userId);
                        }
                    } else {
                        deleteGallery(userId);
                    }
                    onAfterChangingPassword(userId);
                    return;
                }
            } catch (RemoteException re) {
                Log.e(TAG, "Couldn't save lock pattern " + re);
                return;
            }
        }
        throw new IllegalArgumentException("pattern must not be null and at least 4 dots long.");
    }

    private void updateCryptoUserInfo(int userId) {
        if (userId == 0) {
            String ownerInfo = isOwnerInfoEnabled(userId) ? getOwnerInfo(userId) : PhoneConstants.MVNO_TYPE_NONE;
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IMountService mountService = IMountService.Stub.asInterface(service);
            try {
                Log.d(TAG, "Setting owner info");
                mountService.setField("OwnerInfo", ownerInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing user info", e);
            }
        }
    }

    public void setOwnerInfo(String info, int userId) {
        setString(LOCK_SCREEN_OWNER_INFO, info, userId);
        updateCryptoUserInfo(userId);
    }

    public void setOwnerInfoEnabled(boolean enabled, int userId) {
        setBoolean(LOCK_SCREEN_OWNER_INFO_ENABLED, enabled, userId);
        updateCryptoUserInfo(userId);
    }

    public String getOwnerInfo(int userId) {
        return getString(LOCK_SCREEN_OWNER_INFO, userId);
    }

    public boolean isOwnerInfoEnabled(int userId) {
        return getBoolean(LOCK_SCREEN_OWNER_INFO_ENABLED, false, userId);
    }

    public void setDeviceOwnerInfo(String info) {
        if (info != null && info.isEmpty()) {
            info = null;
        }
        setString(LOCK_SCREEN_DEVICE_OWNER_INFO, info, 0);
    }

    public String getDeviceOwnerInfo() {
        return getString(LOCK_SCREEN_DEVICE_OWNER_INFO, 0);
    }

    public boolean isDeviceOwnerInfoEnabled() {
        return getDeviceOwnerInfo() != null;
    }

    public static int computePasswordQuality(String password) {
        boolean hasDigit = false;
        int len = password.length();
        for (int i = 0; i < len; i++) {
            if (Character.isDigit(password.charAt(i))) {
            }
            hasDigit = true;
        }
        if (null != null && hasDigit) {
            return Protocol.BASE_TETHERING;
        }
        if (null != null) {
            return 262144;
        }
        if (!hasDigit) {
            return 0;
        }
        int i2;
        if (maxLengthSequence(password) > 3) {
            i2 = 131072;
        } else {
            i2 = 196608;
        }
        return i2;
    }

    private static int categoryChar(char c) {
        if (DateFormat.AM_PM <= c && c <= DateFormat.TIME_ZONE) {
            return 0;
        }
        if (DateFormat.CAPITAL_AM_PM <= c && c <= 'Z') {
            return 1;
        }
        if ('0' > c || c > '9') {
            return 3;
        }
        return 2;
    }

    private static int maxDiffCategory(int category) {
        if (category == 0 || category == 1) {
            return 1;
        }
        if (category == 2) {
            return 10;
        }
        return 0;
    }

    public static int maxLengthSequence(String string) {
        if (string.length() == 0) {
            return 0;
        }
        char previousChar = string.charAt(0);
        int category = categoryChar(previousChar);
        int diff = 0;
        boolean hasDiff = false;
        int maxLength = 0;
        int startSequence = 0;
        for (int current = 1; current < string.length(); current++) {
            char currentChar = string.charAt(current);
            int categoryCurrent = categoryChar(currentChar);
            int currentDiff = currentChar - previousChar;
            if (categoryCurrent != category || Math.abs(currentDiff) > maxDiffCategory(category)) {
                maxLength = Math.max(maxLength, current - startSequence);
                startSequence = current;
                hasDiff = false;
                category = categoryCurrent;
            } else {
                if (hasDiff && currentDiff != diff) {
                    maxLength = Math.max(maxLength, current - startSequence);
                    startSequence = current - 1;
                }
                diff = currentDiff;
                hasDiff = true;
            }
            previousChar = currentChar;
        }
        return Math.max(maxLength, string.length() - startSequence);
    }

    private void updateEncryptionPassword(final int type, final String password) {
        if (isDeviceEncryptionEnabled()) {
            final IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the encryption password");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... dummy) {
                        try {
                            IMountService.Stub.asInterface(service).changeEncryptionPassword(type, password);
                        } catch (RemoteException e) {
                            Log.e(LockPatternUtils.TAG, "Error changing encryption password", e);
                        }
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_BASE_CLASS, note = "YaoJun.Luo@Plf.SDK, 2015-08-0 : Modify for the length of password is saved to settingsprovider", property = OppoRomType.ROM)
    public void saveLockPassword(String password, String savedPassword, int quality, int userHandle) {
        saveLockPassword(password, savedPassword, quality, false, null, userHandle);
    }

    public void saveLockPassword(String password, String savedPassword, int quality, boolean isFallback, String fallbackFor, int userHandle) {
        try {
            DevicePolicyManager dpm = getDevicePolicyManager();
            if (password == null || password.length() < 4) {
                throw new IllegalArgumentException("password must not be null and at least of length 4");
            }
            int computedQuality = computePasswordQuality(password);
            setLong(PASSWORD_TYPE_KEY, (long) Math.max(quality, computedQuality), userHandle);
            getLockSettings().setLockPassword(password, savedPassword, userHandle);
            setVerifyPwdAttemptDeadline(userHandle, 0, VERIFY_PWD_ATTEMPT_DEFAULT_TIMEOUT_MS);
            if (password != null) {
                System.putInt(this.mContext.getContentResolver(), "PASSWORD_LENGTH", password.length());
            }
            if (userHandle == 0 && isDeviceEncryptionEnabled()) {
                if (shouldEncryptWithCredentials(true)) {
                    int type;
                    boolean numeric = computedQuality == 131072;
                    boolean numericComplex = computedQuality == 196608;
                    if (numeric || numericComplex) {
                        type = 3;
                    } else {
                        type = 0;
                    }
                    updateEncryptionPassword(type, password);
                } else {
                    clearEncryptionPassword();
                }
            }
            if (isFallback) {
                if (fallbackFor.equals(TYPE_VOICE_UNLOCK)) {
                    setLong(PASSWORD_TYPE_KEY, 16384, userHandle);
                    setLong(PASSWORD_TYPE_ALTERNATE_KEY, (long) Math.max(quality, computedQuality), userHandle);
                    finishVoiceWeak(userHandle);
                    dpm.setActivePasswordState(16384, 0, 0, 0, 0, 0, 0, 0, userHandle);
                }
            } else {
                deleteGallery(userHandle);
            }
            String passwordHistory = getString(PASSWORD_HISTORY_KEY, userHandle);
            if (passwordHistory == null) {
                passwordHistory = PhoneConstants.MVNO_TYPE_NONE;
            }
            int passwordHistoryLength = getRequestedPasswordHistoryLength(userHandle);
            if (passwordHistoryLength == 0) {
                passwordHistory = PhoneConstants.MVNO_TYPE_NONE;
            } else {
                byte[] hash = passwordToHash(password, userHandle);
                passwordHistory = new String(hash, StandardCharsets.UTF_8) + "," + passwordHistory;
                passwordHistory = passwordHistory.substring(0, Math.min(((hash.length * passwordHistoryLength) + passwordHistoryLength) - 1, passwordHistory.length()));
            }
            setString(PASSWORD_HISTORY_KEY, passwordHistory, userHandle);
            onAfterChangingPassword(userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Unable to save lock password " + re);
        }
    }

    public static boolean isDeviceEncryptionEnabled() {
        return StorageManager.isEncrypted();
    }

    public static boolean isFileEncryptionEnabled() {
        return StorageManager.isFileEncryptedNativeOrEmulated();
    }

    public void clearEncryptionPassword() {
        updateEncryptionPassword(1, null);
    }

    public int getKeyguardStoredPasswordQuality(int userHandle) {
        int quality = (int) getLong(PASSWORD_TYPE_KEY, 0, userHandle);
        if (quality == 32768 || quality == 16384) {
            return (int) getLong(PASSWORD_TYPE_ALTERNATE_KEY, 0, userHandle);
        }
        return quality;
    }

    public void setSeparateProfileChallengeEnabled(int userHandle, boolean enabled, String managedUserPassword) {
        if (getUserManager().getUserInfo(userHandle).isManagedProfile()) {
            try {
                getLockSettings().setSeparateProfileChallengeEnabled(userHandle, enabled, managedUserPassword);
                onAfterChangingPassword(userHandle);
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't update work profile challenge enabled");
            }
        }
    }

    public boolean isSeparateProfileChallengeEnabled(int userHandle) {
        UserInfo info = getUserManager().getUserInfo(userHandle);
        if (info == null || !info.isManagedProfile()) {
            return false;
        }
        try {
            return getLockSettings().getSeparateProfileChallengeEnabled(userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't get separate profile challenge enabled");
            return false;
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        UserInfo info = getUserManager().getUserInfo(userHandle);
        if (info == null || !info.isManagedProfile()) {
            return false;
        }
        return getDevicePolicyManager().isSeparateProfileChallengeAllowed(userHandle);
    }

    public boolean isSeparateProfileChallengeAllowedToUnify(int userHandle) {
        return getDevicePolicyManager().isProfileActivePasswordSufficientForParent(userHandle);
    }

    public static List<Cell> stringToPattern(String string) {
        if (string == null) {
            return null;
        }
        List<Cell> result = Lists.newArrayList();
        byte[] bytes = string.getBytes();
        for (byte b : bytes) {
            byte b2 = (byte) (b - 49);
            result.add(Cell.of(b2 / 3, b2 % 3));
        }
        return result;
    }

    public static String patternToString(List<Cell> pattern) {
        if (pattern == null) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = (Cell) pattern.get(i);
            res[i] = (byte) (((cell.getRow() * 3) + cell.getColumn()) + 49);
        }
        return new String(res);
    }

    public static String patternStringToBaseZero(String pattern) {
        if (pattern == null) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        int patternSize = pattern.length();
        byte[] res = new byte[patternSize];
        byte[] bytes = pattern.getBytes();
        for (int i = 0; i < patternSize; i++) {
            res[i] = (byte) (bytes[i] - 49);
        }
        return new String(res);
    }

    public static byte[] patternToHash(List<Cell> pattern) {
        if (pattern == null) {
            return null;
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = (Cell) pattern.get(i);
            res[i] = (byte) ((cell.getRow() * 3) + cell.getColumn());
        }
        try {
            return MessageDigest.getInstance("SHA-1").digest(res);
        } catch (NoSuchAlgorithmException e) {
            return res;
        }
    }

    private String getSalt(int userId) {
        long salt = getLong(LOCK_PASSWORD_SALT_KEY, 0, userId);
        if (salt == 0) {
            try {
                salt = SecureRandom.getInstance("SHA1PRNG").nextLong();
                setLong(LOCK_PASSWORD_SALT_KEY, salt, userId);
                Log.v(TAG, "Initialized lock password salt for user: " + userId);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Couldn't get SecureRandom number", e);
            }
        }
        return Long.toHexString(salt);
    }

    public byte[] passwordToHash(String password, int userId) {
        if (password == null) {
            return null;
        }
        try {
            byte[] saltedPassword = (password + getSalt(userId)).getBytes();
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(saltedPassword);
            byte[] md5 = MessageDigest.getInstance("MD5").digest(saltedPassword);
            byte[] combined = new byte[(sha1.length + md5.length)];
            System.arraycopy(sha1, 0, combined, 0, sha1.length);
            System.arraycopy(md5, 0, combined, sha1.length, md5.length);
            return new String(HexEncoding.encode(combined)).getBytes(StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Missing digest algorithm: ", e);
        }
    }

    public boolean isSecure(int userId) {
        int mode = getKeyguardStoredPasswordQuality(userId);
        return !isLockPatternEnabled(mode, userId) ? isLockPasswordEnabled(mode, userId) : true;
    }

    public boolean isLockPasswordEnabled(int userId) {
        return isLockPasswordEnabled(getKeyguardStoredPasswordQuality(userId), userId);
    }

    private boolean isLockPasswordEnabled(int mode, int userId) {
        boolean passwordEnabled = true;
        if (!(mode == 262144 || mode == 131072 || mode == 196608 || mode == Protocol.BASE_TETHERING || mode == 393216 || mode == 524288)) {
            passwordEnabled = false;
        }
        if (passwordEnabled) {
            return savedPasswordExists(userId);
        }
        return false;
    }

    public boolean isLockPatternEnabled(int userId) {
        return isLockPatternEnabled(getKeyguardStoredPasswordQuality(userId), userId);
    }

    @Deprecated
    public boolean isLegacyLockPatternEnabled(int userId) {
        return getBoolean(LEGACY_LOCK_PATTERN_ENABLED, true, userId);
    }

    @Deprecated
    public void setLegacyLockPatternEnabled(int userId) {
        setBoolean("lock_pattern_autolock", true, userId);
    }

    private boolean isLockPatternEnabled(int mode, int userId) {
        if (mode == 65536) {
            return savedPatternExists(userId);
        }
        return false;
    }

    public boolean isVisiblePatternEnabled(int userId) {
        return getBoolean("lock_pattern_visible_pattern", false, userId);
    }

    public void setVisiblePatternEnabled(boolean enabled, int userId) {
        setBoolean("lock_pattern_visible_pattern", enabled, userId);
        if (userId == 0) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IMountService mountService = IMountService.Stub.asInterface(service);
            try {
                String str;
                String str2 = "PatternVisible";
                if (enabled) {
                    str = "1";
                } else {
                    str = "0";
                }
                mountService.setField(str2, str);
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing pattern visible state", e);
            }
        }
    }

    public void setVisiblePasswordEnabled(boolean enabled, int userId) {
        if (userId == 0) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IMountService mountService = IMountService.Stub.asInterface(service);
            try {
                String str;
                String str2 = "PasswordVisible";
                if (enabled) {
                    str = "1";
                } else {
                    str = "0";
                }
                mountService.setField(str2, str);
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing password visible state", e);
            }
        }
    }

    public boolean isTactileFeedbackEnabled() {
        return System.getIntForUser(this.mContentResolver, "haptic_feedback_enabled", 1, -2) != 0;
    }

    public long setLockoutAttemptDeadline(int userId, int timeoutMs) {
        long deadline = SystemClock.elapsedRealtime() + ((long) timeoutMs);
        setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline, userId);
        setLong(LOCKOUT_ATTEMPT_TIMEOUT_MS, (long) timeoutMs, userId);
        return deadline;
    }

    public long getLockoutAttemptDeadline(int userId) {
        long deadline = getLong(LOCKOUT_ATTEMPT_DEADLINE, 0, userId);
        long timeoutMs = getLong(LOCKOUT_ATTEMPT_TIMEOUT_MS, 0, userId);
        long now = SystemClock.elapsedRealtime();
        if (deadline >= now || deadline == 0) {
            if (deadline > now + timeoutMs) {
                deadline = now + timeoutMs;
                setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline, userId);
            }
            return deadline;
        }
        setLong(LOCKOUT_ATTEMPT_DEADLINE, 0, userId);
        setLong(LOCKOUT_ATTEMPT_TIMEOUT_MS, 0, userId);
        return 0;
    }

    public long getVerifyPwdAttemptDeadline(int userId) {
        long deadline = getLong(VERIFY_PWD_ATTEMPT_DEADLINE, 0, userId);
        if (deadline != 0) {
            return deadline;
        }
        deadline = SystemClock.elapsedRealtime() + 172800000;
        setLong(VERIFY_PWD_ATTEMPT_DEADLINE, deadline, userId);
        return deadline;
    }

    public long setVerifyPwdAttemptDeadline(int userId, long deadline, int timeoutMs) {
        if (deadline == 0) {
            deadline = SystemClock.elapsedRealtime() + ((long) timeoutMs);
        }
        setLong(VERIFY_PWD_ATTEMPT_DEADLINE, deadline, userId);
        return deadline;
    }

    private boolean getBoolean(String secureSettingKey, boolean defaultValue, int userId) {
        try {
            return getLockSettings().getBoolean(secureSettingKey, defaultValue, userId);
        } catch (RemoteException e) {
            return defaultValue;
        }
    }

    private void setBoolean(String secureSettingKey, boolean enabled, int userId) {
        try {
            getLockSettings().setBoolean(secureSettingKey, enabled, userId);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write boolean " + secureSettingKey + re);
        }
    }

    private long getLong(String secureSettingKey, long defaultValue, int userHandle) {
        try {
            return getLockSettings().getLong(secureSettingKey, defaultValue, userHandle);
        } catch (RemoteException e) {
            return defaultValue;
        }
    }

    private void setLong(String secureSettingKey, long value, int userHandle) {
        try {
            getLockSettings().setLong(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write long " + secureSettingKey + re);
        }
    }

    private String getString(String secureSettingKey, int userHandle) {
        try {
            return getLockSettings().getString(secureSettingKey, null, userHandle);
        } catch (RemoteException e) {
            return null;
        }
    }

    private void setString(String secureSettingKey, String value, int userHandle) {
        try {
            getLockSettings().setString(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write string " + secureSettingKey + re);
        }
    }

    public void setPowerButtonInstantlyLocks(boolean enabled, int userId) {
        setBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, enabled, userId);
    }

    public boolean getPowerButtonInstantlyLocks(int userId) {
        return getBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, true, userId);
    }

    public void setEnabledTrustAgents(Collection<ComponentName> activeTrustAgents, int userId) {
        StringBuilder sb = new StringBuilder();
        for (ComponentName cn : activeTrustAgents) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(cn.flattenToShortString());
        }
        setString(ENABLED_TRUST_AGENTS, sb.toString(), userId);
        getTrustManager().reportEnabledTrustAgentsChanged(userId);
    }

    public List<ComponentName> getEnabledTrustAgents(int userId) {
        String serialized = getString(ENABLED_TRUST_AGENTS, userId);
        if (TextUtils.isEmpty(serialized)) {
            return null;
        }
        String[] split = serialized.split(",");
        ArrayList<ComponentName> activeTrustAgents = new ArrayList(split.length);
        for (String s : split) {
            if (!TextUtils.isEmpty(s)) {
                activeTrustAgents.add(ComponentName.unflattenFromString(s));
            }
        }
        return activeTrustAgents;
    }

    public void requireCredentialEntry(int userId) {
        requireStrongAuth(4, userId);
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        try {
            getLockSettings().requireStrongAuth(strongAuthReason, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Error while requesting strong auth: " + e);
        }
    }

    private void onAfterChangingPassword(int userHandle) {
        getTrustManager().reportEnabledTrustAgentsChanged(userHandle);
    }

    public boolean isCredentialRequiredToDecrypt(boolean defaultValue) {
        int value = Global.getInt(this.mContentResolver, "require_password_to_decrypt", -1);
        if (value == -1) {
            return defaultValue;
        }
        return value != 0;
    }

    public void setCredentialRequiredToDecrypt(boolean required) {
        boolean z;
        int i = 1;
        if (getUserManager().isSystemUser()) {
            z = true;
        } else {
            z = getUserManager().isPrimaryUser();
        }
        if (!z) {
            throw new IllegalStateException("Only the system or primary user may call setCredentialRequiredForDecrypt()");
        } else if (isDeviceEncryptionEnabled()) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "require_password_to_decrypt";
            if (!required) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
    }

    private boolean isDoNotAskCredentialsOnBootSet() {
        return this.mDevicePolicyManager.getDoNotAskCredentialsOnBoot();
    }

    private boolean shouldEncryptWithCredentials(boolean defaultValue) {
        return isCredentialRequiredToDecrypt(defaultValue) && !isDoNotAskCredentialsOnBootSet();
    }

    private void throwIfCalledOnMainThread() {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalStateException("should not be called from the main thread.");
        }
    }

    public void registerStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        try {
            getLockSettings().registerStrongAuthTracker(strongAuthTracker.mStub);
        } catch (RemoteException e) {
            throw new RuntimeException("Could not register StrongAuthTracker");
        }
    }

    public void unregisterStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        try {
            getLockSettings().unregisterStrongAuthTracker(strongAuthTracker.mStub);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not unregister StrongAuthTracker", e);
        }
    }

    public int getStrongAuthForUser(int userId) {
        try {
            return getLockSettings().getStrongAuthForUser(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not get StrongAuth", e);
            return StrongAuthTracker.getDefaultFlags(this.mContext);
        }
    }

    public boolean isTrustAllowedForUser(int userId) {
        return getStrongAuthForUser(userId) == 0;
    }

    public boolean isFingerprintAllowedForUser(int userId) {
        return (getStrongAuthForUser(userId) & -5) == 0;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private com.android.internal.widget.ICheckCredentialProgressCallback wrapCallback(com.android.internal.widget.LockPatternUtils.CheckCredentialProgressCallback r3) {
        /*
        r2 = this;
        r0 = 0;
        if (r3 != 0) goto L_0x0004;
    L_0x0003:
        return r0;
    L_0x0004:
        r0 = r2.mHandler;
        if (r0 != 0) goto L_0x0011;
    L_0x0008:
        r0 = new java.lang.IllegalStateException;
        r1 = "Must construct LockPatternUtils on a looper thread to use progress callbacks.";
        r0.<init>(r1);
        throw r0;
    L_0x0011:
        r0 = new com.android.internal.widget.LockPatternUtils$2;
        r0.<init>(r2, r3);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternUtils.wrapCallback(com.android.internal.widget.LockPatternUtils$CheckCredentialProgressCallback):com.android.internal.widget.ICheckCredentialProgressCallback");
    }

    public boolean usingVoiceWeak() {
        return usingVoiceWeak(ActivityManager.getCurrentUser());
    }

    public boolean usingVoiceWeak(int userId) {
        boolean z = false;
        if (!SystemProperties.get("ro.mtk_voice_unlock_support").equals("1")) {
            return false;
        }
        if (((int) getLong(PASSWORD_TYPE_KEY, 65536, userId)) == 16384) {
            z = true;
        }
        return z;
    }

    private void finishVoiceWeak(int userId) {
        setVoiceUnlockFallbackSet(true);
        Intent intent = new Intent();
        intent.setClassName("com.mediatek.voiceunlock", "com.mediatek.voiceunlock.VoiceUnlockSetupEnd");
        this.mContext.startActivityAsUser(intent, new UserHandle(userId));
    }

    public void setVoiceUnlockFallbackSet(boolean set) {
        setVoiceUnlockFallbackSet(set, ActivityManager.getCurrentUser());
    }

    public void setVoiceUnlockFallbackSet(boolean set, int userId) {
        setBoolean(VOICE_WEAK_FALLBACK_SET_KEY, set, userId);
    }

    public boolean getVoiceUnlockFallbackSet() {
        return getVoiceUnlockFallbackSet(ActivityManager.getCurrentUser());
    }

    public boolean getVoiceUnlockFallbackSet(int userId) {
        return getBoolean(VOICE_WEAK_FALLBACK_SET_KEY, false, userId);
    }

    public void resetLockoutAttemptDeadline() {
        resetLockoutAttemptDeadline(ActivityManager.getCurrentUser());
    }

    public void resetLockoutAttemptDeadline(int userId) {
        setLong(LOCKOUT_ATTEMPT_DEADLINE, 0, userId);
    }

    public boolean usingBiometricWeak() {
        return usingBiometricWeak(ActivityManager.getCurrentUser());
    }

    public boolean usingBiometricWeak(int userId) {
        return ((int) getLong(PASSWORD_TYPE_KEY, 0, userId)) == 32768;
    }

    void deleteGallery(int userId) {
        if (usingBiometricWeak(userId)) {
            Intent intent = new Intent().setAction("com.android.facelock.DELETE_GALLERY");
            intent.putExtra("deleteGallery", true);
            this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId));
        }
    }

    public boolean isLockPasswordEnabledWithBackUp(int userId) {
        long mode = getLong(PASSWORD_TYPE_KEY, 0, userId);
        long backupMode = getLong(PASSWORD_TYPE_ALTERNATE_KEY, 0, userId);
        boolean passwordEnabled = (mode == 262144 || mode == 131072 || mode == 196608 || mode == 327680) ? true : mode == 393216;
        boolean backupEnabled = (backupMode == 262144 || backupMode == 131072 || backupMode == 196608 || backupMode == 327680) ? true : backupMode == 393216;
        boolean isPWEnabled = savedPasswordExists(userId) ? !passwordEnabled ? (usingBiometricWeak() || usingVoiceWeak()) ? backupEnabled : false : true : false;
        Log.d(TAG, "isLockPasswordEnabled = " + isPWEnabled);
        return isPWEnabled;
    }

    public boolean isLockPatternEnabledWithBackup(int userId) {
        boolean backupEnabled = getLong(PASSWORD_TYPE_ALTERNATE_KEY, 0, userId) == 65536;
        boolean save = savedPatternExists(userId);
        Log.d(TAG, "s=" + save + " ,L=" + getBoolean("lock_pattern_autolock", false, userId) + " ,q=" + (getLong(PASSWORD_TYPE_KEY, 65536, userId) == 65536) + " ,v=" + usingVoiceWeak());
        if (!savedPatternExists(userId) || !getBoolean("lock_pattern_autolock", false, userId)) {
            return false;
        }
        if (getLong(PASSWORD_TYPE_KEY, 65536, userId) == 65536) {
            return true;
        }
        if (usingBiometricWeak() || usingVoiceWeak()) {
            return backupEnabled;
        }
        return false;
    }

    public boolean isPermanentlyLocked() {
        return getBoolean(LOCKOUT_PERMANENT_KEY, false, ActivityManager.getCurrentUser());
    }

    public boolean isEmergencyCallCapable() {
        return this.mContext.getResources().getBoolean(R.bool.config_voice_capable);
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    public boolean isInCall() {
        return getTelecommManager().isInCall();
    }

    public void updateEmergencyCallButtonState(Button button, boolean shown, boolean showIcon) {
        if (isEmergencyCallCapable() && shown) {
            int textId;
            button.setVisibility(0);
            if (isInCall()) {
                int phoneCallIcon;
                textId = R.string.lockscreen_return_to_call;
                if (showIcon) {
                    phoneCallIcon = R.drawable.stat_sys_phone_call;
                } else {
                    phoneCallIcon = 0;
                }
                button.setCompoundDrawablesWithIntrinsicBounds(phoneCallIcon, 0, 0, 0);
            } else {
                int emergencyIcon;
                textId = R.string.lockscreen_emergency_call;
                if (showIcon) {
                    emergencyIcon = R.drawable.ic_emergency;
                } else {
                    emergencyIcon = 0;
                }
                button.setCompoundDrawablesWithIntrinsicBounds(emergencyIcon, 0, 0, 0);
            }
            button.setText(textId);
            return;
        }
        button.setVisibility(4);
    }

    public boolean isQualcommTimeoutFlagOn(int userId) {
        return getBoolean(QUALCOMM_TIMEOUT_FLAG, false, userId);
    }

    public void setQualcommTimeoutFlagOn(boolean flag, int userId) {
        setBoolean(QUALCOMM_TIMEOUT_FLAG, flag, userId);
    }
}

package android.accounts;

import android.accounts.IAccountManagerResponse.Stub;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
public class AccountManager {
    public static final String ACCOUNT_ACCESS_TOKEN_TYPE = "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE";
    public static final String ACTION_AUTHENTICATOR_INTENT = "android.accounts.AccountAuthenticator";
    public static final String AUTHENTICATOR_ATTRIBUTES_NAME = "account-authenticator";
    public static final String AUTHENTICATOR_META_DATA_NAME = "android.accounts.AccountAuthenticator";
    public static final int ERROR_CODE_BAD_ARGUMENTS = 7;
    public static final int ERROR_CODE_BAD_AUTHENTICATION = 9;
    public static final int ERROR_CODE_BAD_REQUEST = 8;
    public static final int ERROR_CODE_CANCELED = 4;
    public static final int ERROR_CODE_INVALID_RESPONSE = 5;
    public static final int ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE = 101;
    public static final int ERROR_CODE_NETWORK_ERROR = 3;
    public static final int ERROR_CODE_REMOTE_EXCEPTION = 1;
    public static final int ERROR_CODE_UNSUPPORTED_OPERATION = 6;
    public static final int ERROR_CODE_USER_RESTRICTED = 100;
    public static final String KEY_ACCOUNTS = "accounts";
    public static final String KEY_ACCOUNT_ACCESS_ID = "accountAccessId";
    public static final String KEY_ACCOUNT_AUTHENTICATOR_RESPONSE = "accountAuthenticatorResponse";
    public static final String KEY_ACCOUNT_MANAGER_RESPONSE = "accountManagerResponse";
    public static final String KEY_ACCOUNT_NAME = "authAccount";
    public static final String KEY_ACCOUNT_SESSION_BUNDLE = "accountSessionBundle";
    public static final String KEY_ACCOUNT_STATUS_TOKEN = "accountStatusToken";
    public static final String KEY_ACCOUNT_TYPE = "accountType";
    public static final String KEY_ANDROID_PACKAGE_NAME = "androidPackageName";
    public static final String KEY_AUTHENTICATOR_TYPES = "authenticator_types";
    public static final String KEY_AUTHTOKEN = "authtoken";
    public static final String KEY_AUTH_FAILED_MESSAGE = "authFailedMessage";
    public static final String KEY_AUTH_TOKEN_LABEL = "authTokenLabelKey";
    public static final String KEY_BOOLEAN_RESULT = "booleanResult";
    public static final String KEY_CALLER_PID = "callerPid";
    public static final String KEY_CALLER_UID = "callerUid";
    public static final String KEY_ERROR_CODE = "errorCode";
    public static final String KEY_ERROR_MESSAGE = "errorMessage";
    public static final String KEY_INTENT = "intent";
    public static final String KEY_LAST_AUTHENTICATED_TIME = "lastAuthenticatedTime";
    public static final String KEY_NOTIFY_ON_FAILURE = "notifyOnAuthFailure";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USERDATA = "userdata";
    public static final String LOGIN_ACCOUNTS_CHANGED_ACTION = "android.accounts.LOGIN_ACCOUNTS_CHANGED";
    private static final String TAG = "AccountManager";
    private final BroadcastReceiver mAccountsChangedBroadcastReceiver;
    private final HashMap<OnAccountsUpdateListener, Handler> mAccountsUpdatedListeners;
    private final Context mContext;
    private final Handler mMainHandler;
    private final IAccountManager mService;

    private abstract class AmsTask extends FutureTask<Bundle> implements AccountManagerFuture<Bundle> {
        final Activity mActivity;
        final AccountManagerCallback<Bundle> mCallback;
        final Handler mHandler;
        final IAccountManagerResponse mResponse = new Response(this, null);

        private class Response extends Stub {
            /* synthetic */ Response(AmsTask this$1, Response response) {
                this();
            }

            private Response() {
            }

            public void onResult(Bundle bundle) {
                Intent intent = (Intent) bundle.getParcelable("intent");
                if (intent != null && AmsTask.this.mActivity != null) {
                    AmsTask.this.mActivity.startActivity(intent);
                } else if (bundle.getBoolean("retry")) {
                    try {
                        AmsTask.this.doWork();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                } else {
                    AmsTask.this.set(bundle);
                }
            }

            public void onError(int code, String message) {
                if (code == 4 || code == 100 || code == 101) {
                    AmsTask.this.cancel(true);
                } else {
                    AmsTask.this.setException(AccountManager.this.convertErrorToException(code, message));
                }
            }
        }

        public abstract void doWork() throws RemoteException;

        public AmsTask(Activity activity, Handler handler, AccountManagerCallback<Bundle> callback) {
            super(new Callable<Bundle>(AccountManager.this) {
                public Bundle call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
            this.mHandler = handler;
            this.mCallback = callback;
            this.mActivity = activity;
        }

        public final AccountManagerFuture<Bundle> start() {
            try {
                doWork();
            } catch (RemoteException e) {
                setException(e);
            }
            return this;
        }

        protected void set(Bundle bundle) {
            if (bundle == null) {
                Log.e(AccountManager.TAG, "the bundle must not be null", new Exception());
            }
            super.set(bundle);
        }

        private Bundle internalGetResult(Long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            if (!isDone()) {
                AccountManager.this.ensureNotOnMainThread();
            }
            Bundle bundle;
            if (timeout == null) {
                try {
                    bundle = (Bundle) get();
                    cancel(true);
                    return bundle;
                } catch (CancellationException e) {
                    throw new OperationCanceledException();
                } catch (TimeoutException e2) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (InterruptedException e3) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (ExecutionException e4) {
                    Throwable cause = e4.getCause();
                    if (cause instanceof IOException) {
                        throw ((IOException) cause);
                    } else if (cause instanceof UnsupportedOperationException) {
                        throw new AuthenticatorException(cause);
                    } else if (cause instanceof AuthenticatorException) {
                        throw ((AuthenticatorException) cause);
                    } else if (cause instanceof RuntimeException) {
                        throw ((RuntimeException) cause);
                    } else if (cause instanceof Error) {
                        throw ((Error) cause);
                    } else {
                        throw new IllegalStateException(cause);
                    }
                } catch (Throwable th) {
                    cancel(true);
                }
            } else {
                bundle = (Bundle) get(timeout.longValue(), unit);
                cancel(true);
                return bundle;
            }
        }

        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }

        protected void done() {
            if (this.mCallback != null) {
                AccountManager.this.postToHandler(this.mHandler, this.mCallback, (AccountManagerFuture) this);
            }
        }
    }

    /* renamed from: android.accounts.AccountManager$10 */
    class AnonymousClass10 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.accounts.AccountManager.10.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.os.Bundle):void, dex:  in method: android.accounts.AccountManager.10.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.os.Bundle):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.accounts.AccountManager.10.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.os.Bundle):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$33.decode(InstructionCodec.java:728)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        AnonymousClass10(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.accounts.Account r6, java.lang.String r7, android.os.Bundle r8) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.accounts.AccountManager.10.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.os.Bundle):void, dex:  in method: android.accounts.AccountManager.10.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.10.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.10.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.10.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.10.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$12 */
    class AnonymousClass12 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;
        final /* synthetic */ String[] val$requiredFeatures;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.12.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle):void, dex: 
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
        AnonymousClass12(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, java.lang.String r6, java.lang.String r7, java.lang.String[] r8, android.app.Activity r9, android.os.Bundle r10) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.12.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.12.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.12.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.12.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.12.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$13 */
    class AnonymousClass13 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;
        final /* synthetic */ String[] val$requiredFeatures;
        final /* synthetic */ UserHandle val$userHandle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.accounts.AccountManager.13.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex:  in method: android.accounts.AccountManager.13.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.accounts.AccountManager.13.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$33.decode(InstructionCodec.java:728)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        AnonymousClass13(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, java.lang.String r6, java.lang.String r7, java.lang.String[] r8, android.app.Activity r9, android.os.Bundle r10, android.os.UserHandle r11) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.accounts.AccountManager.13.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex:  in method: android.accounts.AccountManager.13.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.13.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.UserHandle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.13.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.13.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.13.doWork():void");
        }
    }

    private abstract class BaseFutureTask<T> extends FutureTask<T> {
        final Handler mHandler;
        public final IAccountManagerResponse mResponse;
        final /* synthetic */ AccountManager this$0;

        protected class Response extends Stub {
            final /* synthetic */ BaseFutureTask this$1;

            protected Response(BaseFutureTask this$1) {
                this.this$1 = this$1;
            }

            public void onResult(Bundle bundle) {
                try {
                    T result = this.this$1.bundleToResult(bundle);
                    if (result != null) {
                        this.this$1.set(result);
                    }
                } catch (ClassCastException e) {
                    onError(5, "no result in response");
                } catch (AuthenticatorException e2) {
                    onError(5, "no result in response");
                }
            }

            public void onError(int code, String message) {
                if (code == 4 || code == 100 || code == 101) {
                    this.this$1.cancel(true);
                } else {
                    this.this$1.setException(this.this$1.this$0.convertErrorToException(code, message));
                }
            }
        }

        public abstract T bundleToResult(Bundle bundle) throws AuthenticatorException;

        public abstract void doWork() throws RemoteException;

        public BaseFutureTask(final AccountManager this$0, Handler handler) {
            this.this$0 = this$0;
            super(new Callable<T>() {
                public T call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
            this.mHandler = handler;
            this.mResponse = new Response(this);
        }

        protected void postRunnableToHandler(Runnable runnable) {
            (this.mHandler == null ? this.this$0.mMainHandler : this.mHandler).post(runnable);
        }

        protected void startTask() {
            try {
                doWork();
            } catch (RemoteException e) {
                setException(e);
            }
        }
    }

    private abstract class Future2Task<T> extends BaseFutureTask<T> implements AccountManagerFuture<T> {
        final AccountManagerCallback<T> mCallback;
        final /* synthetic */ AccountManager this$0;

        /* renamed from: android.accounts.AccountManager$Future2Task$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Future2Task this$1;

            AnonymousClass1(Future2Task this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.mCallback.run(this.this$1);
            }
        }

        public Future2Task(AccountManager this$0, Handler handler, AccountManagerCallback<T> callback) {
            this.this$0 = this$0;
            super(this$0, handler);
            this.mCallback = callback;
        }

        protected void done() {
            if (this.mCallback != null) {
                postRunnableToHandler(new AnonymousClass1(this));
            }
        }

        public Future2Task<T> start() {
            startTask();
            return this;
        }

        private T internalGetResult(Long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            if (!isDone()) {
                this.this$0.ensureNotOnMainThread();
            }
            T t;
            if (timeout == null) {
                try {
                    t = get();
                    cancel(true);
                    return t;
                } catch (InterruptedException e) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (TimeoutException e2) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (CancellationException e3) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (ExecutionException e4) {
                    Throwable cause = e4.getCause();
                    if (cause instanceof IOException) {
                        throw ((IOException) cause);
                    } else if (cause instanceof UnsupportedOperationException) {
                        throw new AuthenticatorException(cause);
                    } else if (cause instanceof AuthenticatorException) {
                        throw ((AuthenticatorException) cause);
                    } else if (cause instanceof RuntimeException) {
                        throw ((RuntimeException) cause);
                    } else if (cause instanceof Error) {
                        throw ((Error) cause);
                    } else {
                        throw new IllegalStateException(cause);
                    }
                } catch (Throwable th) {
                    cancel(true);
                }
            } else {
                t = get(timeout.longValue(), unit);
                cancel(true);
                return t;
            }
        }

        public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }
    }

    /* renamed from: android.accounts.AccountManager$14 */
    class AnonymousClass14 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ UserHandle val$fromUser;
        final /* synthetic */ UserHandle val$toUser;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.14.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.UserHandle, android.os.UserHandle):void, dex: 
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
        AnonymousClass14(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.os.Handler r3, android.accounts.AccountManagerCallback r4, android.accounts.Account r5, android.os.UserHandle r6, android.os.UserHandle r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.14.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.UserHandle, android.os.UserHandle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.14.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.UserHandle, android.os.UserHandle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public java.lang.Boolean bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.14.bundleToResult(android.os.Bundle):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.14.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.14.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.14.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$15 */
    class AnonymousClass15 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ int val$userId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.15.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.Bundle, android.app.Activity, int):void, dex: 
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
        AnonymousClass15(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.accounts.Account r6, android.os.Bundle r7, android.app.Activity r8, int r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.15.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.Bundle, android.app.Activity, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.15.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.Bundle, android.app.Activity, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.15.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.15.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.15.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$16 */
    class AnonymousClass16 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.16.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.app.Activity, android.os.Bundle):void, dex: 
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
        AnonymousClass16(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.accounts.Account r6, java.lang.String r7, android.app.Activity r8, android.os.Bundle r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.16.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.app.Activity, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.16.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.app.Activity, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.16.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.16.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.16.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$17 */
    class AnonymousClass17 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.accounts.AccountManager.17.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, android.app.Activity):void, dex:  in method: android.accounts.AccountManager.17.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, android.app.Activity):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.accounts.AccountManager.17.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, android.app.Activity):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$33.decode(InstructionCodec.java:728)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        AnonymousClass17(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, java.lang.String r6, android.app.Activity r7) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.accounts.AccountManager.17.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, android.app.Activity):void, dex:  in method: android.accounts.AccountManager.17.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, android.app.Activity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.17.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, android.app.Activity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.17.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.17.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.17.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$18 */
    class AnonymousClass18 implements Runnable {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ AccountManagerCallback val$callback;
        final /* synthetic */ AccountManagerFuture val$future;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.18.<init>(android.accounts.AccountManager, android.accounts.AccountManagerCallback, android.accounts.AccountManagerFuture):void, dex: 
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
        AnonymousClass18(android.accounts.AccountManager r1, android.accounts.AccountManagerCallback r2, android.accounts.AccountManagerFuture r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.18.<init>(android.accounts.AccountManager, android.accounts.AccountManagerCallback, android.accounts.AccountManagerFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.18.<init>(android.accounts.AccountManager, android.accounts.AccountManagerCallback, android.accounts.AccountManagerFuture):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.18.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.18.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.18.run():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ AccountManager this$0;

        AnonymousClass1(AccountManager this$0) {
            this.this$0 = this$0;
        }

        public void onReceive(Context context, Intent intent) {
            Account[] accounts = this.this$0.getAccounts();
            synchronized (this.this$0.mAccountsUpdatedListeners) {
                for (Entry<OnAccountsUpdateListener, Handler> entry : this.this$0.mAccountsUpdatedListeners.entrySet()) {
                    this.this$0.postToHandler((Handler) entry.getValue(), (OnAccountsUpdateListener) entry.getKey(), accounts);
                }
            }
        }
    }

    /* renamed from: android.accounts.AccountManager$20 */
    class AnonymousClass20 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;
        final /* synthetic */ String[] val$requiredFeatures;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.20.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle):void, dex: 
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
        AnonymousClass20(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, java.lang.String r6, java.lang.String r7, java.lang.String[] r8, android.app.Activity r9, android.os.Bundle r10) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.20.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.20.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.20.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.20.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.20.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$21 */
    class AnonymousClass21 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.21.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.app.Activity, android.os.Bundle):void, dex: 
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
        AnonymousClass21(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.accounts.Account r6, java.lang.String r7, android.app.Activity r8, android.os.Bundle r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.21.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.app.Activity, android.os.Bundle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.21.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String, android.app.Activity, android.os.Bundle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.21.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.21.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.21.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$22 */
    class AnonymousClass22 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ Bundle val$appInfo;
        final /* synthetic */ Bundle val$sessionBundle;
        final /* synthetic */ UserHandle val$userHandle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.22.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.os.Bundle, android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex: 
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
        AnonymousClass22(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.os.Bundle r6, android.app.Activity r7, android.os.Bundle r8, android.os.UserHandle r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.22.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.os.Bundle, android.app.Activity, android.os.Bundle, android.os.UserHandle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.22.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.os.Bundle, android.app.Activity, android.os.Bundle, android.os.UserHandle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.22.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.22.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.22.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$23 */
    class AnonymousClass23 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$statusToken;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.23.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String):void, dex: 
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
        AnonymousClass23(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.os.Handler r3, android.accounts.AccountManagerCallback r4, android.accounts.Account r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.23.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.23.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public java.lang.Boolean bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.23.bundleToResult(android.os.Bundle):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.23.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.23.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.23.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$2 */
    class AnonymousClass2 extends Future2Task<String> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ String val$authTokenType;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.2.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String):void, dex: 
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
        AnonymousClass2(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.os.Handler r3, android.accounts.AccountManagerCallback r4, java.lang.String r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.2.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.2.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.2.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.2.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.2.bundleToResult(android.os.Bundle):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.2.bundleToResult(android.os.Bundle):java.lang.String, dex: 
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
        public java.lang.String bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.2.bundleToResult(android.os.Bundle):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.2.bundleToResult(android.os.Bundle):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.2.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.2.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.2.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$5 */
    class AnonymousClass5 extends Future2Task<Account> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$newName;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.5.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String):void, dex: 
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
        AnonymousClass5(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.os.Handler r3, android.accounts.AccountManagerCallback r4, android.accounts.Account r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.5.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.5.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.5.bundleToResult(android.os.Bundle):android.accounts.Account, dex: 
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
        public android.accounts.Account bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.5.bundleToResult(android.os.Bundle):android.accounts.Account, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.5.bundleToResult(android.os.Bundle):android.accounts.Account");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.5.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.5.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.5.bundleToResult(android.os.Bundle):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.5.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.5.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.5.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$6 */
    class AnonymousClass6 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.6.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account):void, dex: 
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
        AnonymousClass6(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.os.Handler r3, android.accounts.AccountManagerCallback r4, android.accounts.Account r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.6.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.6.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public java.lang.Boolean bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.6.bundleToResult(android.os.Bundle):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.6.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.6.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.6.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$7 */
    class AnonymousClass7 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.accounts.AccountManager.7.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity):void, dex:  in method: android.accounts.AccountManager.7.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.accounts.AccountManager.7.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$33.decode(InstructionCodec.java:728)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        AnonymousClass7(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.accounts.Account r6, android.app.Activity r7) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.accounts.AccountManager.7.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity):void, dex:  in method: android.accounts.AccountManager.7.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.7.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.7.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.7.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.7.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$8 */
    class AnonymousClass8 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ UserHandle val$userHandle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.8.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.UserHandle):void, dex: 
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
        AnonymousClass8(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.os.Handler r3, android.accounts.AccountManagerCallback r4, android.accounts.Account r5, android.os.UserHandle r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.8.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.UserHandle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.8.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.os.UserHandle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public java.lang.Boolean bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Boolean, dex:  in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object bundleToResult(android.os.Bundle r1) throws android.accounts.AuthenticatorException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.8.bundleToResult(android.os.Bundle):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.8.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.8.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.8.doWork():void");
        }
    }

    /* renamed from: android.accounts.AccountManager$9 */
    class AnonymousClass9 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ UserHandle val$userHandle;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.accounts.AccountManager.9.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity, android.os.UserHandle):void, dex:  in method: android.accounts.AccountManager.9.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity, android.os.UserHandle):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.accounts.AccountManager.9.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity, android.os.UserHandle):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$33.decode(InstructionCodec.java:728)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        AnonymousClass9(android.accounts.AccountManager r1, android.accounts.AccountManager r2, android.app.Activity r3, android.os.Handler r4, android.accounts.AccountManagerCallback r5, android.accounts.Account r6, android.app.Activity r7, android.os.UserHandle r8) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.accounts.AccountManager.9.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity, android.os.UserHandle):void, dex:  in method: android.accounts.AccountManager.9.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity, android.os.UserHandle):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.9.<init>(android.accounts.AccountManager, android.accounts.AccountManager, android.app.Activity, android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.Account, android.app.Activity, android.os.UserHandle):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.9.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.9.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.9.doWork():void");
        }
    }

    private class GetAuthTokenByTypeAndFeaturesTask extends AmsTask implements AccountManagerCallback<Bundle> {
        final String mAccountType;
        final Bundle mAddAccountOptions;
        final String mAuthTokenType;
        final String[] mFeatures;
        volatile AccountManagerFuture<Bundle> mFuture;
        final Bundle mLoginOptions;
        final AccountManagerCallback<Bundle> mMyCallback;
        private volatile int mNumAccounts;
        final /* synthetic */ AccountManager this$0;

        /* renamed from: android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask$1 */
        class AnonymousClass1 implements AccountManagerCallback<Account[]> {
            final /* synthetic */ GetAuthTokenByTypeAndFeaturesTask this$1;

            /* renamed from: android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask$1$1 */
            class AnonymousClass1 extends Stub {
                final /* synthetic */ AnonymousClass1 this$2;

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.<init>(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask$1):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 12 more
                    */
                AnonymousClass1(android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.AnonymousClass1 r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.<init>(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask$1):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.<init>(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask$1):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.onError(int, java.lang.String):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 12 more
                    */
                public void onError(int r1, java.lang.String r2) throws android.os.RemoteException {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.onError(int, java.lang.String):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.onError(int, java.lang.String):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.onResult(android.os.Bundle):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                    	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                    	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                    	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                    	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                    	... 12 more
                    */
                public void onResult(android.os.Bundle r1) throws android.os.RemoteException {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.onResult(android.os.Bundle):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.1.onResult(android.os.Bundle):void");
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.<init>(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask):void, dex: 
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
            AnonymousClass1(android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.<init>(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.<init>(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.run(android.accounts.AccountManagerFuture):void, dex: 
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
            public void run(android.accounts.AccountManagerFuture<android.accounts.Account[]> r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.run(android.accounts.AccountManagerFuture):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.1.run(android.accounts.AccountManagerFuture):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.-wrap0(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask, java.lang.Throwable):void, dex: 
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
        /* renamed from: -wrap0 */
        static /* synthetic */ void m6-wrap0(android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask r1, java.lang.Throwable r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.-wrap0(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask, java.lang.Throwable):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.-wrap0(android.accounts.AccountManager$GetAuthTokenByTypeAndFeaturesTask, java.lang.Throwable):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.<init>(android.accounts.AccountManager, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.Bundle, android.accounts.AccountManagerCallback, android.os.Handler):void, dex: 
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
        GetAuthTokenByTypeAndFeaturesTask(android.accounts.AccountManager r1, java.lang.String r2, java.lang.String r3, java.lang.String[] r4, android.app.Activity r5, android.os.Bundle r6, android.os.Bundle r7, android.accounts.AccountManagerCallback<android.os.Bundle> r8, android.os.Handler r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.<init>(android.accounts.AccountManager, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.Bundle, android.accounts.AccountManagerCallback, android.os.Handler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.<init>(android.accounts.AccountManager, java.lang.String, java.lang.String, java.lang.String[], android.app.Activity, android.os.Bundle, android.os.Bundle, android.accounts.AccountManagerCallback, android.os.Handler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.doWork():void, dex: 
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
        public void doWork() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.doWork():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.doWork():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.run(android.accounts.AccountManagerFuture):void, dex: 
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
        public void run(android.accounts.AccountManagerFuture<android.os.Bundle> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.run(android.accounts.AccountManagerFuture):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.run(android.accounts.AccountManagerFuture):void");
        }
    }

    public AccountManager(Context context, IAccountManager service) {
        this.mAccountsUpdatedListeners = Maps.newHashMap();
        this.mAccountsChangedBroadcastReceiver = new AnonymousClass1(this);
        this.mContext = context;
        this.mService = service;
        this.mMainHandler = new Handler(this.mContext.getMainLooper());
    }

    public AccountManager(Context context, IAccountManager service, Handler handler) {
        this.mAccountsUpdatedListeners = Maps.newHashMap();
        this.mAccountsChangedBroadcastReceiver = new AnonymousClass1(this);
        this.mContext = context;
        this.mService = service;
        this.mMainHandler = handler;
    }

    public static Bundle sanitizeResult(Bundle result) {
        if (result == null || !result.containsKey(KEY_AUTHTOKEN) || TextUtils.isEmpty(result.getString(KEY_AUTHTOKEN))) {
            return result;
        }
        Bundle newResult = new Bundle(result);
        newResult.putString(KEY_AUTHTOKEN, "<omitted for logging purposes>");
        return newResult;
    }

    public static AccountManager get(Context context) {
        if (context != null) {
            return (AccountManager) context.getSystemService("account");
        }
        throw new IllegalArgumentException("context is null");
    }

    public String getPassword(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.getPassword(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getUserData(Account account, String key) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else {
            try {
                return this.mService.getUserData(account, key);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public AuthenticatorDescription[] getAuthenticatorTypes() {
        try {
            return this.mService.getAuthenticatorTypes(UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AuthenticatorDescription[] getAuthenticatorTypesAsUser(int userId) {
        try {
            return this.mService.getAuthenticatorTypes(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Account[] getAccounts() {
        try {
            return this.mService.getAccounts(null, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsAsUser(int userId) {
        try {
            return this.mService.getAccountsAsUser(null, userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsForPackage(String packageName, int uid) {
        try {
            return this.mService.getAccountsForPackage(packageName, uid, this.mContext.getOpPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName) {
        try {
            return this.mService.getAccountsByTypeForPackage(type, packageName, this.mContext.getOpPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsByType(String type) {
        return getAccountsByTypeAsUser(type, Process.myUserHandle());
    }

    public Account[] getAccountsByTypeAsUser(String type, UserHandle userHandle) {
        try {
            return this.mService.getAccountsAsUser(type, userHandle.getIdentifier(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) {
        try {
            this.mService.updateAppPermission(account, authTokenType, uid, value);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
    public android.accounts.AccountManagerFuture<java.lang.String> getAuthTokenLabel(java.lang.String r8, java.lang.String r9, android.accounts.AccountManagerCallback<java.lang.String> r10, android.os.Handler r11) {
        /*
        r7 = this;
        if (r8 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "accountType is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        if (r9 != 0) goto L_0x0016;
    L_0x000d:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "authTokenType is null";
        r0.<init>(r1);
        throw r0;
    L_0x0016:
        r0 = new android.accounts.AccountManager$2;
        r1 = r7;
        r2 = r7;
        r3 = r11;
        r4 = r10;
        r5 = r8;
        r6 = r9;
        r0.<init>(r1, r2, r3, r4, r5, r6);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.getAuthTokenLabel(java.lang.String, java.lang.String, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<java.lang.String>");
    }

    public AccountManagerFuture<Boolean> hasFeatures(Account account, String[] features, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features == null) {
            throw new IllegalArgumentException("features is null");
        } else {
            final Account account2 = account;
            final String[] strArr = features;
            return new Future2Task<Boolean>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.hasFeatures(this.mResponse, account2, strArr, this.mContext.getOpPackageName());
                }

                public /* bridge */ /* synthetic */ Object bundleToResult(Bundle bundle) throws AuthenticatorException {
                    return bundleToResult(bundle);
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        }
    }

    public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(String type, String[] features, AccountManagerCallback<Account[]> callback, Handler handler) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        final String str = type;
        final String[] strArr = features;
        return new Future2Task<Account[]>(this, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.getAccountsByFeatures(this.mResponse, str, strArr, this.mContext.getOpPackageName());
            }

            public /* bridge */ /* synthetic */ Object bundleToResult(Bundle bundle) throws AuthenticatorException {
                return bundleToResult(bundle);
            }

            public Account[] bundleToResult(Bundle bundle) throws AuthenticatorException {
                if (bundle.containsKey(AccountManager.KEY_ACCOUNTS)) {
                    Parcelable[] parcelables = bundle.getParcelableArray(AccountManager.KEY_ACCOUNTS);
                    Account[] descs = new Account[parcelables.length];
                    for (int i = 0; i < parcelables.length; i++) {
                        descs[i] = (Account) parcelables[i];
                    }
                    return descs;
                }
                throw new AuthenticatorException("no result in response");
            }
        }.start();
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.addAccountExplicitly(account, password, userdata);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean notifyAccountAuthenticated(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.accountAuthenticated(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
    public android.accounts.AccountManagerFuture<android.accounts.Account> renameAccount(android.accounts.Account r8, java.lang.String r9, android.accounts.AccountManagerCallback<android.accounts.Account> r10, android.os.Handler r11) {
        /*
        r7 = this;
        if (r8 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null.";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = android.text.TextUtils.isEmpty(r9);
        if (r0 == 0) goto L_0x001a;
    L_0x0011:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "newName is empty or null.";
        r0.<init>(r1);
        throw r0;
    L_0x001a:
        r0 = new android.accounts.AccountManager$5;
        r1 = r7;
        r2 = r7;
        r3 = r11;
        r4 = r10;
        r5 = r8;
        r6 = r9;
        r0.<init>(r1, r2, r3, r4, r5, r6);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.renameAccount(android.accounts.Account, java.lang.String, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.accounts.Account>");
    }

    public String getPreviousName(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.getPreviousName(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
    @java.lang.Deprecated
    public android.accounts.AccountManagerFuture<java.lang.Boolean> removeAccount(android.accounts.Account r7, android.accounts.AccountManagerCallback<java.lang.Boolean> r8, android.os.Handler r9) {
        /*
        r6 = this;
        if (r7 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = new android.accounts.AccountManager$6;
        r1 = r6;
        r2 = r6;
        r3 = r9;
        r4 = r8;
        r5 = r7;
        r0.<init>(r1, r2, r3, r4, r5);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.removeAccount(android.accounts.Account, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<java.lang.Boolean>");
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> removeAccount(android.accounts.Account r9, android.app.Activity r10, android.accounts.AccountManagerCallback<android.os.Bundle> r11, android.os.Handler r12) {
        /*
        r8 = this;
        if (r9 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = new android.accounts.AccountManager$7;
        r1 = r8;
        r2 = r8;
        r3 = r10;
        r4 = r12;
        r5 = r11;
        r6 = r9;
        r7 = r10;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.removeAccount(android.accounts.Account, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
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
    @java.lang.Deprecated
    public android.accounts.AccountManagerFuture<java.lang.Boolean> removeAccountAsUser(android.accounts.Account r8, android.accounts.AccountManagerCallback<java.lang.Boolean> r9, android.os.Handler r10, android.os.UserHandle r11) {
        /*
        r7 = this;
        if (r8 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        if (r11 != 0) goto L_0x0016;
    L_0x000d:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "userHandle is null";
        r0.<init>(r1);
        throw r0;
    L_0x0016:
        r0 = new android.accounts.AccountManager$8;
        r1 = r7;
        r2 = r7;
        r3 = r10;
        r4 = r9;
        r5 = r8;
        r6 = r11;
        r0.<init>(r1, r2, r3, r4, r5, r6);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.removeAccountAsUser(android.accounts.Account, android.accounts.AccountManagerCallback, android.os.Handler, android.os.UserHandle):android.accounts.AccountManagerFuture<java.lang.Boolean>");
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> removeAccountAsUser(android.accounts.Account r10, android.app.Activity r11, android.accounts.AccountManagerCallback<android.os.Bundle> r12, android.os.Handler r13, android.os.UserHandle r14) {
        /*
        r9 = this;
        if (r10 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        if (r14 != 0) goto L_0x0016;
    L_0x000d:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "userHandle is null";
        r0.<init>(r1);
        throw r0;
    L_0x0016:
        r0 = new android.accounts.AccountManager$9;
        r1 = r9;
        r2 = r9;
        r3 = r11;
        r4 = r13;
        r5 = r12;
        r6 = r10;
        r7 = r11;
        r8 = r14;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.removeAccountAsUser(android.accounts.Account, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler, android.os.UserHandle):android.accounts.AccountManagerFuture<android.os.Bundle>");
    }

    public boolean removeAccountExplicitly(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.removeAccountExplicitly(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void invalidateAuthToken(String accountType, String authToken) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authToken != null) {
            try {
                this.mService.invalidateAuthToken(accountType, authToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String peekAuthToken(Account account, String authTokenType) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            try {
                return this.mService.peekAuthToken(account, authTokenType);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setPassword(Account account, String password) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            this.mService.setPassword(account, password);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearPassword(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            this.mService.clearPassword(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUserData(Account account, String key, String value) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else {
            try {
                this.mService.setUserData(account, key, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            try {
                this.mService.setAuthToken(account, authTokenType, authToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String blockingGetAuthToken(Account account, String authTokenType, boolean notifyAuthFailure) throws OperationCanceledException, IOException, AuthenticatorException {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            Bundle bundle = (Bundle) getAuthToken(account, authTokenType, notifyAuthFailure, null, null).getResult();
            if (bundle != null) {
                return bundle.getString(KEY_AUTHTOKEN);
            }
            Log.e(TAG, "blockingGetAuthToken: null was returned from getResult() for " + account + ", authTokenType " + authTokenType);
            return null;
        }
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> getAuthToken(android.accounts.Account r10, java.lang.String r11, android.os.Bundle r12, android.app.Activity r13, android.accounts.AccountManagerCallback<android.os.Bundle> r14, android.os.Handler r15) {
        /*
        r9 = this;
        if (r10 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        if (r11 != 0) goto L_0x0016;
    L_0x000d:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "authTokenType is null";
        r0.<init>(r1);
        throw r0;
    L_0x0016:
        r8 = new android.os.Bundle;
        r8.<init>();
        if (r12 == 0) goto L_0x0020;
    L_0x001d:
        r8.putAll(r12);
    L_0x0020:
        r0 = "androidPackageName";
        r1 = r9.mContext;
        r1 = r1.getPackageName();
        r8.putString(r0, r1);
        r0 = new android.accounts.AccountManager$10;
        r1 = r9;
        r2 = r9;
        r3 = r13;
        r4 = r15;
        r5 = r14;
        r6 = r10;
        r7 = r11;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.getAuthToken(android.accounts.Account, java.lang.String, android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
    }

    @Deprecated
    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        return getAuthToken(account, authTokenType, null, notifyAuthFailure, (AccountManagerCallback) callback, handler);
    }

    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, Bundle options, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Account account2 = account;
            final String str = authTokenType;
            final boolean z = notifyAuthFailure;
            return new AmsTask(this, null, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.getAuthToken(this.mResponse, account2, str, z, false, optionsIn);
                }
            }.start();
        }
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> addAccount(java.lang.String r12, java.lang.String r13, java.lang.String[] r14, android.os.Bundle r15, android.app.Activity r16, android.accounts.AccountManagerCallback<android.os.Bundle> r17, android.os.Handler r18) {
        /*
        r11 = this;
        if (r12 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "accountType is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r10 = new android.os.Bundle;
        r10.<init>();
        if (r15 == 0) goto L_0x0015;
    L_0x0012:
        r10.putAll(r15);
    L_0x0015:
        r0 = "androidPackageName";
        r1 = r11.mContext;
        r1 = r1.getPackageName();
        r10.putString(r0, r1);
        r0 = new android.accounts.AccountManager$12;
        r1 = r11;
        r2 = r11;
        r3 = r16;
        r4 = r18;
        r5 = r17;
        r6 = r12;
        r7 = r13;
        r8 = r14;
        r9 = r16;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.addAccount(java.lang.String, java.lang.String, java.lang.String[], android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> addAccountAsUser(java.lang.String r14, java.lang.String r15, java.lang.String[] r16, android.os.Bundle r17, android.app.Activity r18, android.accounts.AccountManagerCallback<android.os.Bundle> r19, android.os.Handler r20, android.os.UserHandle r21) {
        /*
        r13 = this;
        if (r14 != 0) goto L_0x000b;
    L_0x0002:
        r1 = new java.lang.IllegalArgumentException;
        r2 = "accountType is null";
        r1.<init>(r2);
        throw r1;
    L_0x000b:
        if (r21 != 0) goto L_0x0016;
    L_0x000d:
        r1 = new java.lang.IllegalArgumentException;
        r2 = "userHandle is null";
        r1.<init>(r2);
        throw r1;
    L_0x0016:
        r11 = new android.os.Bundle;
        r11.<init>();
        if (r17 == 0) goto L_0x0022;
    L_0x001d:
        r0 = r17;
        r11.putAll(r0);
    L_0x0022:
        r1 = "androidPackageName";
        r2 = r13.mContext;
        r2 = r2.getPackageName();
        r11.putString(r1, r2);
        r1 = new android.accounts.AccountManager$13;
        r2 = r13;
        r3 = r13;
        r4 = r18;
        r5 = r20;
        r6 = r19;
        r7 = r14;
        r8 = r15;
        r9 = r16;
        r10 = r18;
        r12 = r21;
        r1.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12);
        r1 = r1.start();
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.addAccountAsUser(java.lang.String, java.lang.String, java.lang.String[], android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler, android.os.UserHandle):android.accounts.AccountManagerFuture<android.os.Bundle>");
    }

    public void addSharedAccountsFromParentUser(UserHandle parentUser, UserHandle user) {
        try {
            this.mService.addSharedAccountsFromParentUser(parentUser.getIdentifier(), user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
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
    public android.accounts.AccountManagerFuture<java.lang.Boolean> copyAccountToUser(android.accounts.Account r9, android.os.UserHandle r10, android.os.UserHandle r11, android.accounts.AccountManagerCallback<java.lang.Boolean> r12, android.os.Handler r13) {
        /*
        r8 = this;
        if (r9 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        if (r11 == 0) goto L_0x000f;
    L_0x000d:
        if (r10 != 0) goto L_0x0018;
    L_0x000f:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "fromUser and toUser cannot be null";
        r0.<init>(r1);
        throw r0;
    L_0x0018:
        r0 = new android.accounts.AccountManager$14;
        r1 = r8;
        r2 = r8;
        r3 = r13;
        r4 = r12;
        r5 = r9;
        r6 = r10;
        r7 = r11;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.copyAccountToUser(android.accounts.Account, android.os.UserHandle, android.os.UserHandle, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<java.lang.Boolean>");
    }

    public boolean removeSharedAccount(Account account, UserHandle user) {
        try {
            return this.mService.removeSharedAccountAsUser(account, user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Account[] getSharedAccounts(UserHandle user) {
        try {
            return this.mService.getSharedAccountsAsUser(user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public AccountManagerFuture<Bundle> confirmCredentials(Account account, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return confirmCredentialsAsUser(account, options, activity, callback, handler, Process.myUserHandle());
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> confirmCredentialsAsUser(android.accounts.Account r11, android.os.Bundle r12, android.app.Activity r13, android.accounts.AccountManagerCallback<android.os.Bundle> r14, android.os.Handler r15, android.os.UserHandle r16) {
        /*
        r10 = this;
        if (r11 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r9 = r16.getIdentifier();
        r0 = new android.accounts.AccountManager$15;
        r1 = r10;
        r2 = r10;
        r3 = r13;
        r4 = r15;
        r5 = r14;
        r6 = r11;
        r7 = r12;
        r8 = r13;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.confirmCredentialsAsUser(android.accounts.Account, android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler, android.os.UserHandle):android.accounts.AccountManagerFuture<android.os.Bundle>");
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> updateCredentials(android.accounts.Account r11, java.lang.String r12, android.os.Bundle r13, android.app.Activity r14, android.accounts.AccountManagerCallback<android.os.Bundle> r15, android.os.Handler r16) {
        /*
        r10 = this;
        if (r11 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = new android.accounts.AccountManager$16;
        r1 = r10;
        r2 = r10;
        r3 = r14;
        r4 = r16;
        r5 = r15;
        r6 = r11;
        r7 = r12;
        r8 = r14;
        r9 = r13;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.updateCredentials(android.accounts.Account, java.lang.String, android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> editProperties(java.lang.String r9, android.app.Activity r10, android.accounts.AccountManagerCallback<android.os.Bundle> r11, android.os.Handler r12) {
        /*
        r8 = this;
        if (r9 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "accountType is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = new android.accounts.AccountManager$17;
        r1 = r8;
        r2 = r8;
        r3 = r10;
        r4 = r12;
        r5 = r11;
        r6 = r9;
        r7 = r10;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.editProperties(java.lang.String, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
    }

    public boolean someUserHasAccount(Account account) {
        try {
            return this.mService.someUserHasAccount(account);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    private void ensureNotOnMainThread() {
        Looper looper = Looper.myLooper();
        if (looper != null && looper == this.mContext.getMainLooper()) {
            IllegalStateException exception = new IllegalStateException("calling this from your main thread can lead to deadlock");
            Log.e(TAG, "calling this from your main thread can lead to deadlock and/or ANRs", exception);
            if (this.mContext.getApplicationInfo().targetSdkVersion >= 8) {
                throw exception;
            }
        }
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
    private void postToHandler(android.os.Handler r2, android.accounts.AccountManagerCallback<android.os.Bundle> r3, android.accounts.AccountManagerFuture<android.os.Bundle> r4) {
        /*
        r1 = this;
        if (r2 != 0) goto L_0x0004;
    L_0x0002:
        r2 = r1.mMainHandler;
    L_0x0004:
        r0 = new android.accounts.AccountManager$18;
        r0.<init>(r1, r3, r4);
        r2.post(r0);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.postToHandler(android.os.Handler, android.accounts.AccountManagerCallback, android.accounts.AccountManagerFuture):void");
    }

    private void postToHandler(Handler handler, final OnAccountsUpdateListener listener, Account[] accounts) {
        final Account[] accountsCopy = new Account[accounts.length];
        System.arraycopy(accounts, 0, accountsCopy, 0, accountsCopy.length);
        if (handler == null) {
            handler = this.mMainHandler;
        }
        handler.post(new Runnable(this) {
            final /* synthetic */ AccountManager this$0;

            public void run() {
                try {
                    listener.onAccountsUpdated(accountsCopy);
                } catch (SQLException e) {
                    Log.e(AccountManager.TAG, "Can't update accounts", e);
                }
            }
        });
    }

    private Exception convertErrorToException(int code, String message) {
        if (code == 3) {
            return new IOException(message);
        }
        if (code == 6) {
            return new UnsupportedOperationException(message);
        }
        if (code == 5) {
            return new AuthenticatorException(message);
        }
        if (code == 7) {
            return new IllegalArgumentException(message);
        }
        return new AuthenticatorException(message);
    }

    public AccountManagerFuture<Bundle> getAuthTokenByFeatures(String accountType, String authTokenType, String[] features, Activity activity, Bundle addAccountOptions, Bundle getAuthTokenOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("account type is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            GetAuthTokenByTypeAndFeaturesTask task = new GetAuthTokenByTypeAndFeaturesTask(this, accountType, authTokenType, features, activity, addAccountOptions, getAuthTokenOptions, callback, handler);
            task.start();
            return task;
        }
    }

    @Deprecated
    public static Intent newChooseAccountIntent(Account selectedAccount, ArrayList<Account> allowableAccounts, String[] allowableAccountTypes, boolean alwaysPromptForAccount, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
        return newChooseAccountIntent(selectedAccount, allowableAccounts, allowableAccountTypes, descriptionOverrideText, addAccountAuthTokenType, addAccountRequiredFeatures, addAccountOptions);
    }

    public static Intent newChooseAccountIntent(Account selectedAccount, List<Account> allowableAccounts, String[] allowableAccountTypes, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
        Serializable serializable = null;
        Intent intent = new Intent();
        ComponentName componentName = ComponentName.unflattenFromString(Resources.getSystem().getString(17039456));
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        String str = ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST;
        if (allowableAccounts != null) {
            serializable = new ArrayList(allowableAccounts);
        }
        intent.putExtra(str, serializable);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY, allowableAccountTypes);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE, addAccountOptions);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_SELECTED_ACCOUNT, (Parcelable) selectedAccount);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_DESCRIPTION_TEXT_OVERRIDE, descriptionOverrideText);
        intent.putExtra("authTokenType", addAccountAuthTokenType);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY, addAccountRequiredFeatures);
        return intent;
    }

    public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately) {
        if (listener == null) {
            throw new IllegalArgumentException("the listener is null");
        }
        synchronized (this.mAccountsUpdatedListeners) {
            if (this.mAccountsUpdatedListeners.containsKey(listener)) {
                throw new IllegalStateException("this listener is already added");
            }
            boolean wasEmpty = this.mAccountsUpdatedListeners.isEmpty();
            this.mAccountsUpdatedListeners.put(listener, handler);
            if (wasEmpty) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(LOGIN_ACCOUNTS_CHANGED_ACTION);
                intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
                this.mContext.registerReceiver(this.mAccountsChangedBroadcastReceiver, intentFilter);
            }
        }
        if (updateImmediately) {
            postToHandler(handler, listener, getAccounts());
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0036, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeOnAccountsUpdatedListener(OnAccountsUpdateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        synchronized (this.mAccountsUpdatedListeners) {
            if (this.mAccountsUpdatedListeners.containsKey(listener)) {
                this.mAccountsUpdatedListeners.remove(listener);
                if (this.mAccountsUpdatedListeners.isEmpty()) {
                    this.mContext.unregisterReceiver(this.mAccountsChangedBroadcastReceiver);
                }
            } else {
                Log.e(TAG, "Listener was not previously added");
            }
        }
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> startAddAccountSession(java.lang.String r12, java.lang.String r13, java.lang.String[] r14, android.os.Bundle r15, android.app.Activity r16, android.accounts.AccountManagerCallback<android.os.Bundle> r17, android.os.Handler r18) {
        /*
        r11 = this;
        if (r12 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "accountType is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r10 = new android.os.Bundle;
        r10.<init>();
        if (r15 == 0) goto L_0x0015;
    L_0x0012:
        r10.putAll(r15);
    L_0x0015:
        r0 = "androidPackageName";
        r1 = r11.mContext;
        r1 = r1.getPackageName();
        r10.putString(r0, r1);
        r0 = new android.accounts.AccountManager$20;
        r1 = r11;
        r2 = r11;
        r3 = r16;
        r4 = r18;
        r5 = r17;
        r6 = r12;
        r7 = r13;
        r8 = r14;
        r9 = r16;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.startAddAccountSession(java.lang.String, java.lang.String, java.lang.String[], android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> startUpdateCredentialsSession(android.accounts.Account r11, java.lang.String r12, android.os.Bundle r13, android.app.Activity r14, android.accounts.AccountManagerCallback<android.os.Bundle> r15, android.os.Handler r16) {
        /*
        r10 = this;
        if (r11 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r9 = new android.os.Bundle;
        r9.<init>();
        if (r13 == 0) goto L_0x0015;
    L_0x0012:
        r9.putAll(r13);
    L_0x0015:
        r0 = "androidPackageName";
        r1 = r10.mContext;
        r1 = r1.getPackageName();
        r9.putString(r0, r1);
        r0 = new android.accounts.AccountManager$21;
        r1 = r10;
        r2 = r10;
        r3 = r14;
        r4 = r16;
        r5 = r15;
        r6 = r11;
        r7 = r12;
        r8 = r14;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.startUpdateCredentialsSession(android.accounts.Account, java.lang.String, android.os.Bundle, android.app.Activity, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
    }

    public AccountManagerFuture<Bundle> finishSession(Bundle sessionBundle, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return finishSessionAsUser(sessionBundle, activity, Process.myUserHandle(), callback, handler);
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
    public android.accounts.AccountManagerFuture<android.os.Bundle> finishSessionAsUser(android.os.Bundle r11, android.app.Activity r12, android.os.UserHandle r13, android.accounts.AccountManagerCallback<android.os.Bundle> r14, android.os.Handler r15) {
        /*
        r10 = this;
        if (r11 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "sessionBundle is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r8 = new android.os.Bundle;
        r8.<init>();
        r0 = "androidPackageName";
        r1 = r10.mContext;
        r1 = r1.getPackageName();
        r8.putString(r0, r1);
        r0 = new android.accounts.AccountManager$22;
        r1 = r10;
        r2 = r10;
        r3 = r12;
        r4 = r15;
        r5 = r14;
        r6 = r11;
        r7 = r12;
        r9 = r13;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.finishSessionAsUser(android.os.Bundle, android.app.Activity, android.os.UserHandle, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<android.os.Bundle>");
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
    public android.accounts.AccountManagerFuture<java.lang.Boolean> isCredentialsUpdateSuggested(android.accounts.Account r8, java.lang.String r9, android.accounts.AccountManagerCallback<java.lang.Boolean> r10, android.os.Handler r11) {
        /*
        r7 = this;
        if (r8 != 0) goto L_0x000b;
    L_0x0002:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "account is null";
        r0.<init>(r1);
        throw r0;
    L_0x000b:
        r0 = android.text.TextUtils.isEmpty(r9);
        if (r0 == 0) goto L_0x001a;
    L_0x0011:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "status token is empty";
        r0.<init>(r1);
        throw r0;
    L_0x001a:
        r0 = new android.accounts.AccountManager$23;
        r1 = r7;
        r2 = r7;
        r3 = r11;
        r4 = r10;
        r5 = r8;
        r6 = r9;
        r0.<init>(r1, r2, r3, r4, r5, r6);
        r0 = r0.start();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AccountManager.isCredentialsUpdateSuggested(android.accounts.Account, java.lang.String, android.accounts.AccountManagerCallback, android.os.Handler):android.accounts.AccountManagerFuture<java.lang.Boolean>");
    }

    public boolean hasAccountAccess(Account account, String packageName, UserHandle userHandle) {
        try {
            return this.mService.hasAccountAccess(account, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String packageName, UserHandle userHandle) {
        try {
            return this.mService.createRequestAccountAccessIntentSenderAsUser(account, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}

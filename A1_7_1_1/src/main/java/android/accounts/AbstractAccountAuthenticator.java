package android.accounts;

import android.Manifest.permission;
import android.accounts.IAccountAuthenticator.Stub;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;

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
public abstract class AbstractAccountAuthenticator {
    private static final String KEY_ACCOUNT = "android.accounts.AbstractAccountAuthenticator.KEY_ACCOUNT";
    private static final String KEY_AUTH_TOKEN_TYPE = "android.accounts.AbstractAccountAuthenticato.KEY_AUTH_TOKEN_TYPE";
    public static final String KEY_CUSTOM_TOKEN_EXPIRY = "android.accounts.expiry";
    private static final String KEY_OPTIONS = "android.accounts.AbstractAccountAuthenticator.KEY_OPTIONS";
    private static final String KEY_REQUIRED_FEATURES = "android.accounts.AbstractAccountAuthenticator.KEY_REQUIRED_FEATURES";
    private static final String TAG = "AccountAuthenticator";
    private final Context mContext;
    private Transport mTransport;

    /* renamed from: android.accounts.AbstractAccountAuthenticator$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ AbstractAccountAuthenticator this$0;
        final /* synthetic */ AccountAuthenticatorResponse val$response;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.1.<init>(android.accounts.AbstractAccountAuthenticator, android.accounts.AccountAuthenticatorResponse):void, dex: 
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
        AnonymousClass1(android.accounts.AbstractAccountAuthenticator r1, android.accounts.AccountAuthenticatorResponse r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.1.<init>(android.accounts.AbstractAccountAuthenticator, android.accounts.AccountAuthenticatorResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.1.<init>(android.accounts.AbstractAccountAuthenticator, android.accounts.AccountAuthenticatorResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AbstractAccountAuthenticator.1.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AbstractAccountAuthenticator.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.1.run():void");
        }
    }

    /* renamed from: android.accounts.AbstractAccountAuthenticator$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ AbstractAccountAuthenticator this$0;
        final /* synthetic */ AccountAuthenticatorResponse val$response;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.2.<init>(android.accounts.AbstractAccountAuthenticator, android.accounts.AccountAuthenticatorResponse):void, dex: 
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
        AnonymousClass2(android.accounts.AbstractAccountAuthenticator r1, android.accounts.AccountAuthenticatorResponse r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.2.<init>(android.accounts.AbstractAccountAuthenticator, android.accounts.AccountAuthenticatorResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.2.<init>(android.accounts.AbstractAccountAuthenticator, android.accounts.AccountAuthenticatorResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.accounts.AbstractAccountAuthenticator.2.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.accounts.AbstractAccountAuthenticator.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.2.run():void");
        }
    }

    /* renamed from: android.accounts.AbstractAccountAuthenticator$3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ AbstractAccountAuthenticator this$0;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String[] val$requiredFeatures;
        final /* synthetic */ AccountAuthenticatorResponse val$response;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.3.<init>(android.accounts.AbstractAccountAuthenticator, java.lang.String, java.lang.String[], android.os.Bundle, android.accounts.AccountAuthenticatorResponse):void, dex: 
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
        AnonymousClass3(android.accounts.AbstractAccountAuthenticator r1, java.lang.String r2, java.lang.String[] r3, android.os.Bundle r4, android.accounts.AccountAuthenticatorResponse r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.3.<init>(android.accounts.AbstractAccountAuthenticator, java.lang.String, java.lang.String[], android.os.Bundle, android.accounts.AccountAuthenticatorResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.3.<init>(android.accounts.AbstractAccountAuthenticator, java.lang.String, java.lang.String[], android.os.Bundle, android.accounts.AccountAuthenticatorResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AbstractAccountAuthenticator.3.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AbstractAccountAuthenticator.3.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.3.run():void");
        }
    }

    /* renamed from: android.accounts.AbstractAccountAuthenticator$4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ AbstractAccountAuthenticator this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ AccountAuthenticatorResponse val$response;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.4.<init>(android.accounts.AbstractAccountAuthenticator, java.lang.String, android.accounts.Account, android.os.Bundle, android.accounts.AccountAuthenticatorResponse):void, dex: 
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
        AnonymousClass4(android.accounts.AbstractAccountAuthenticator r1, java.lang.String r2, android.accounts.Account r3, android.os.Bundle r4, android.accounts.AccountAuthenticatorResponse r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.accounts.AbstractAccountAuthenticator.4.<init>(android.accounts.AbstractAccountAuthenticator, java.lang.String, android.accounts.Account, android.os.Bundle, android.accounts.AccountAuthenticatorResponse):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.4.<init>(android.accounts.AbstractAccountAuthenticator, java.lang.String, android.accounts.Account, android.os.Bundle, android.accounts.AccountAuthenticatorResponse):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.accounts.AbstractAccountAuthenticator.4.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.accounts.AbstractAccountAuthenticator.4.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.4.run():void");
        }
    }

    private class Transport extends Stub {
        final /* synthetic */ AbstractAccountAuthenticator this$0;

        /* synthetic */ Transport(AbstractAccountAuthenticator this$0, Transport transport) {
            this(this$0);
        }

        private Transport(AbstractAccountAuthenticator this$0) {
            this.this$0 = this$0;
        }

        public void addAccount(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] features, Bundle options) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "addAccount: accountType " + accountType + ", authTokenType " + authTokenType + ", features " + (features == null ? "[]" : Arrays.toString(features)));
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.addAccount(new AccountAuthenticatorResponse(response), accountType, authTokenType, features, options);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "addAccount: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "addAccount", accountType, e);
            }
        }

        public void confirmCredentials(IAccountAuthenticatorResponse response, Account account, Bundle options) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "confirmCredentials: " + account);
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.confirmCredentials(new AccountAuthenticatorResponse(response), account, options);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "confirmCredentials: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "confirmCredentials", account.toString(), e);
            }
        }

        public void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "getAuthTokenLabel: authTokenType " + authTokenType);
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTH_TOKEN_LABEL, this.this$0.getAuthTokenLabel(authTokenType));
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "getAuthTokenLabel: result " + AccountManager.sanitizeResult(result));
                }
                response.onResult(result);
            } catch (Exception e) {
                this.this$0.handleException(response, "getAuthTokenLabel", authTokenType, e);
            }
        }

        public void getAuthToken(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "getAuthToken: " + account + ", authTokenType " + authTokenType);
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.getAuthToken(new AccountAuthenticatorResponse(response), account, authTokenType, loginOptions);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "getAuthToken: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "getAuthToken", account.toString() + "," + authTokenType, e);
            }
        }

        public void updateCredentials(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "updateCredentials: " + account + ", authTokenType " + authTokenType);
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.updateCredentials(new AccountAuthenticatorResponse(response), account, authTokenType, loginOptions);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "updateCredentials: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "updateCredentials", account.toString() + "," + authTokenType, e);
            }
        }

        public void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException {
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.editProperties(new AccountAuthenticatorResponse(response), accountType);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "editProperties", accountType, e);
            }
        }

        public void hasFeatures(IAccountAuthenticatorResponse response, Account account, String[] features) throws RemoteException {
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.hasFeatures(new AccountAuthenticatorResponse(response), account, features);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "hasFeatures", account.toString(), e);
            }
        }

        public void getAccountRemovalAllowed(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.getAccountRemovalAllowed(new AccountAuthenticatorResponse(response), account);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "getAccountRemovalAllowed", account.toString(), e);
            }
        }

        public void getAccountCredentialsForCloning(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.getAccountCredentialsForCloning(new AccountAuthenticatorResponse(response), account);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "getAccountCredentialsForCloning", account.toString(), e);
            }
        }

        public void addAccountFromCredentials(IAccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws RemoteException {
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.addAccountFromCredentials(new AccountAuthenticatorResponse(response), account, accountCredentials);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "addAccountFromCredentials", account.toString(), e);
            }
        }

        public void startAddAccountSession(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] features, Bundle options) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "startAddAccountSession: accountType " + accountType + ", authTokenType " + authTokenType + ", features " + (features == null ? "[]" : Arrays.toString(features)));
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.startAddAccountSession(new AccountAuthenticatorResponse(response), accountType, authTokenType, features, options);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "startAddAccountSession: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "startAddAccountSession", accountType, e);
            }
        }

        public void startUpdateCredentialsSession(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "startUpdateCredentialsSession: " + account + ", authTokenType " + authTokenType);
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.startUpdateCredentialsSession(new AccountAuthenticatorResponse(response), account, authTokenType, loginOptions);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "startUpdateCredentialsSession: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "startUpdateCredentialsSession", account.toString() + "," + authTokenType, e);
            }
        }

        public void finishSession(IAccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "finishSession: accountType " + accountType);
            }
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.finishSession(new AccountAuthenticatorResponse(response), accountType, sessionBundle);
                if (result != null) {
                    result.keySet();
                }
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    Log.v(AbstractAccountAuthenticator.TAG, "finishSession: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "finishSession", accountType, e);
            }
        }

        public void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse response, Account account, String statusToken) throws RemoteException {
            this.this$0.checkBinderPermission();
            try {
                Bundle result = this.this$0.isCredentialsUpdateSuggested(new AccountAuthenticatorResponse(response), account, statusToken);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                this.this$0.handleException(response, "isCredentialsUpdateSuggested", account.toString(), e);
            }
        }
    }

    public abstract Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String str, String str2, String[] strArr, Bundle bundle) throws NetworkErrorException;

    public abstract Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException;

    public abstract Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String str);

    public abstract Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String str, Bundle bundle) throws NetworkErrorException;

    public abstract String getAuthTokenLabel(String str);

    public abstract Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strArr) throws NetworkErrorException;

    public abstract Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String str, Bundle bundle) throws NetworkErrorException;

    public AbstractAccountAuthenticator(Context context) {
        this.mTransport = new Transport(this, null);
        this.mContext = context;
    }

    private void handleException(IAccountAuthenticatorResponse response, String method, String data, Exception e) throws RemoteException {
        if (e instanceof NetworkErrorException) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, method + "(" + data + ")", e);
            }
            response.onError(3, e.getMessage());
        } else if (e instanceof UnsupportedOperationException) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, method + "(" + data + ")", e);
            }
            response.onError(6, method + " not supported");
        } else if (e instanceof IllegalArgumentException) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, method + "(" + data + ")", e);
            }
            response.onError(7, method + " not supported");
        } else {
            Log.w(TAG, method + "(" + data + ")", e);
            response.onError(1, method + " failed");
        }
    }

    private void checkBinderPermission() {
        int uid = Binder.getCallingUid();
        String perm = permission.ACCOUNT_MANAGER;
        if (this.mContext.checkCallingOrSelfPermission(permission.ACCOUNT_MANAGER) != 0) {
            throw new SecurityException("caller uid " + uid + " lacks " + permission.ACCOUNT_MANAGER);
        }
    }

    public final IBinder getIBinder() {
        return this.mTransport.asBinder();
    }

    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public android.os.Bundle getAccountCredentialsForCloning(android.accounts.AccountAuthenticatorResponse r3, android.accounts.Account r4) throws android.accounts.NetworkErrorException {
        /*
        r2 = this;
        r0 = new java.lang.Thread;
        r1 = new android.accounts.AbstractAccountAuthenticator$1;
        r1.<init>(r2, r3);
        r0.<init>(r1);
        r0.start();
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.getAccountCredentialsForCloning(android.accounts.AccountAuthenticatorResponse, android.accounts.Account):android.os.Bundle");
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public android.os.Bundle addAccountFromCredentials(android.accounts.AccountAuthenticatorResponse r3, android.accounts.Account r4, android.os.Bundle r5) throws android.accounts.NetworkErrorException {
        /*
        r2 = this;
        r0 = new java.lang.Thread;
        r1 = new android.accounts.AbstractAccountAuthenticator$2;
        r1.<init>(r2, r3);
        r0.<init>(r1);
        r0.start();
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.addAccountFromCredentials(android.accounts.AccountAuthenticatorResponse, android.accounts.Account, android.os.Bundle):android.os.Bundle");
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public android.os.Bundle startAddAccountSession(android.accounts.AccountAuthenticatorResponse r8, java.lang.String r9, java.lang.String r10, java.lang.String[] r11, android.os.Bundle r12) throws android.accounts.NetworkErrorException {
        /*
        r7 = this;
        r6 = new java.lang.Thread;
        r0 = new android.accounts.AbstractAccountAuthenticator$3;
        r1 = r7;
        r2 = r10;
        r3 = r11;
        r4 = r12;
        r5 = r8;
        r0.<init>(r1, r2, r3, r4, r5);
        r6.<init>(r0);
        r6.start();
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.startAddAccountSession(android.accounts.AccountAuthenticatorResponse, java.lang.String, java.lang.String, java.lang.String[], android.os.Bundle):android.os.Bundle");
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public android.os.Bundle startUpdateCredentialsSession(android.accounts.AccountAuthenticatorResponse r8, android.accounts.Account r9, java.lang.String r10, android.os.Bundle r11) throws android.accounts.NetworkErrorException {
        /*
        r7 = this;
        r6 = new java.lang.Thread;
        r0 = new android.accounts.AbstractAccountAuthenticator$4;
        r1 = r7;
        r2 = r10;
        r3 = r9;
        r4 = r11;
        r5 = r8;
        r0.<init>(r1, r2, r3, r4, r5);
        r6.<init>(r0);
        r6.start();
        r0 = 0;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.accounts.AbstractAccountAuthenticator.startUpdateCredentialsSession(android.accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String, android.os.Bundle):android.os.Bundle");
    }

    public Bundle finishSession(AccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws NetworkErrorException {
        Bundle result;
        if (TextUtils.isEmpty(accountType)) {
            Log.e(TAG, "Account type cannot be empty.");
            result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, 7);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "accountType cannot be empty.");
            return result;
        } else if (sessionBundle == null) {
            Log.e(TAG, "Session bundle cannot be null.");
            result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, 7);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "sessionBundle cannot be null.");
            return result;
        } else if (sessionBundle.containsKey(KEY_AUTH_TOKEN_TYPE)) {
            String authTokenType = sessionBundle.getString(KEY_AUTH_TOKEN_TYPE);
            Bundle options = sessionBundle.getBundle(KEY_OPTIONS);
            String[] requiredFeatures = sessionBundle.getStringArray(KEY_REQUIRED_FEATURES);
            Account account = (Account) sessionBundle.getParcelable(KEY_ACCOUNT);
            boolean containsKeyAccount = sessionBundle.containsKey(KEY_ACCOUNT);
            Bundle sessionOptions = new Bundle(sessionBundle);
            sessionOptions.remove(KEY_AUTH_TOKEN_TYPE);
            sessionOptions.remove(KEY_REQUIRED_FEATURES);
            sessionOptions.remove(KEY_OPTIONS);
            sessionOptions.remove(KEY_ACCOUNT);
            if (options != null) {
                options.putAll(sessionOptions);
                sessionOptions = options;
            }
            if (containsKeyAccount) {
                return updateCredentials(response, account, authTokenType, options);
            }
            return addAccount(response, accountType, authTokenType, requiredFeatures, sessionOptions);
        } else {
            result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, 6);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "Authenticator must override finishSession if startAddAccountSession or startUpdateCredentialsSession is overridden.");
            response.onResult(result);
            return result;
        }
    }

    public Bundle isCredentialsUpdateSuggested(AccountAuthenticatorResponse response, Account account, String statusToken) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }
}

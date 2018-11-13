package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import android.view.View;
import android.widget.Toast;
import com.android.ims.ImsConferenceState;
import com.android.internal.R;
import oppo.content.res.OppoThemeResources;

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
public class IntentForwarderActivity extends Activity {
    public static String FORWARD_INTENT_TO_MANAGED_PROFILE;
    public static String FORWARD_INTENT_TO_PARENT;
    public static String TAG;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.IntentForwarderActivity.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.app.IntentForwarderActivity.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.IntentForwarderActivity.<clinit>():void");
    }

    protected void onCreate(Bundle savedInstanceState) {
        int userMessageId;
        int targetUserId;
        super.onCreate(savedInstanceState);
        Intent intentReceived = getIntent();
        String className = intentReceived.getComponent().getClassName();
        if (className.equals(FORWARD_INTENT_TO_PARENT)) {
            userMessageId = R.string.forward_intent_to_owner;
            targetUserId = getProfileParent();
        } else if (className.equals(FORWARD_INTENT_TO_MANAGED_PROFILE)) {
            userMessageId = R.string.forward_intent_to_work;
            targetUserId = getManagedProfile();
        } else {
            Slog.wtf(TAG, IntentForwarderActivity.class.getName() + " cannot be called directly");
            userMessageId = -1;
            targetUserId = -10000;
        }
        if (targetUserId == -10000) {
            finish();
            return;
        }
        Intent newIntent = new Intent(intentReceived);
        newIntent.setComponent(null);
        newIntent.setPackage(null);
        newIntent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
        int callingUserId = getUserId();
        if (canForward(newIntent, targetUserId)) {
            if ("android.intent.action.CHOOSER".equals(newIntent.getAction())) {
                ((Intent) newIntent.getParcelableExtra("android.intent.extra.INTENT")).prepareToLeaveUser(callingUserId);
            } else {
                newIntent.prepareToLeaveUser(callingUserId);
            }
            ResolveInfo ri = getPackageManager().resolveActivityAsUser(newIntent, 65536, targetUserId);
            boolean shouldShowDisclosure = (ri == null || ri.activityInfo == null || !OppoThemeResources.FRAMEWORK_PACKAGE.equals(ri.activityInfo.packageName)) ? true : (ResolverActivity.class.getName().equals(ri.activityInfo.name) || ChooserActivity.class.getName().equals(ri.activityInfo.name)) ? false : true;
            try {
                startActivityAsCaller(newIntent, null, false, targetUserId);
            } catch (RuntimeException e) {
                int launchedFromUid = -1;
                String launchedFromPackage = "?";
                try {
                    launchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken());
                    launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(getActivityToken());
                } catch (RemoteException e2) {
                }
                Slog.wtf(TAG, "Unable to launch as UID " + launchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
            }
            if (shouldShowDisclosure) {
                Toast.makeText((Context) this, getString(userMessageId), 1).show();
            }
        } else {
            Slog.wtf(TAG, "the intent: " + newIntent + " cannot be forwarded from user " + callingUserId + " to user " + targetUserId);
        }
        finish();
    }

    boolean canForward(Intent intent, int targetUserId) {
        IPackageManager ipm = AppGlobals.getPackageManager();
        if ("android.intent.action.CHOOSER".equals(intent.getAction())) {
            if (intent.hasExtra("android.intent.extra.INITIAL_INTENTS")) {
                Slog.wtf(TAG, "An chooser intent with extra initial intents cannot be forwarded to a different user");
                return false;
            } else if (intent.hasExtra("android.intent.extra.REPLACEMENT_EXTRAS")) {
                Slog.wtf(TAG, "A chooser intent with replacement extras cannot be forwarded to a different user");
                return false;
            } else {
                intent = (Intent) intent.getParcelableExtra("android.intent.extra.INTENT");
                if (intent == null) {
                    Slog.wtf(TAG, "Cannot forward a chooser intent with no extra android.intent.extra.INTENT");
                    return false;
                }
            }
        }
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        if (intent.getSelector() != null) {
            intent = intent.getSelector();
        }
        try {
            return ipm.canForwardTo(intent, resolvedType, getUserId(), targetUserId);
        } catch (RemoteException e) {
            Slog.e(TAG, "PackageManagerService is dead?");
            return false;
        }
    }

    private int getManagedProfile() {
        for (UserInfo userInfo : ((UserManager) getSystemService(ImsConferenceState.USER)).getProfiles(UserHandle.myUserId())) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        Slog.wtf(TAG, FORWARD_INTENT_TO_MANAGED_PROFILE + " has been called, but there is no managed profile");
        return -10000;
    }

    private int getProfileParent() {
        UserInfo parent = ((UserManager) getSystemService(ImsConferenceState.USER)).getProfileParent(UserHandle.myUserId());
        if (parent != null) {
            return parent.id;
        }
        Slog.wtf(TAG, FORWARD_INTENT_TO_PARENT + " has been called, but there is no parent");
        return -10000;
    }
}

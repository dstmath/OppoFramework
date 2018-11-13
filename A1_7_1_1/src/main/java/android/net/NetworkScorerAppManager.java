package android.net;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
public final class NetworkScorerAppManager {
    private static final Intent SCORE_INTENT = null;
    private static final String TAG = "NetworkScorerAppManager";

    public static class NetworkScorerAppData {
        public final String mConfigurationActivityClassName;
        public final String mPackageName;
        public final int mPackageUid;
        public final CharSequence mScorerName;
        public final String mScoringServiceClassName;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.NetworkScorerAppManager.NetworkScorerAppData.<init>(java.lang.String, int, java.lang.CharSequence, java.lang.String, java.lang.String):void, dex: 
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
        public NetworkScorerAppData(java.lang.String r1, int r2, java.lang.CharSequence r3, java.lang.String r4, java.lang.String r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.NetworkScorerAppManager.NetworkScorerAppData.<init>(java.lang.String, int, java.lang.CharSequence, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkScorerAppManager.NetworkScorerAppData.<init>(java.lang.String, int, java.lang.CharSequence, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.NetworkScorerAppManager.NetworkScorerAppData.toString():java.lang.String, dex: 
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
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.NetworkScorerAppManager.NetworkScorerAppData.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkScorerAppManager.NetworkScorerAppData.toString():java.lang.String");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.NetworkScorerAppManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.NetworkScorerAppManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkScorerAppManager.<clinit>():void");
    }

    private NetworkScorerAppManager() {
    }

    public static Collection<NetworkScorerAppData> getAllValidScorers(Context context) {
        if (UserHandle.getCallingUserId() != 0) {
            return Collections.emptyList();
        }
        List<NetworkScorerAppData> scorers = new ArrayList();
        PackageManager pm = context.getPackageManager();
        for (ResolveInfo receiver : pm.queryBroadcastReceiversAsUser(SCORE_INTENT, 0, 0)) {
            ActivityInfo receiverInfo = receiver.activityInfo;
            if (receiverInfo != null && permission.BROADCAST_NETWORK_PRIVILEGED.equals(receiverInfo.permission) && pm.checkPermission(permission.SCORE_NETWORKS, receiverInfo.packageName) == 0) {
                String configurationActivityClassName = null;
                Intent intent = new Intent(NetworkScoreManager.ACTION_CUSTOM_ENABLE);
                intent.setPackage(receiverInfo.packageName);
                List<ResolveInfo> configActivities = pm.queryIntentActivities(intent, 0);
                if (!(configActivities == null || configActivities.isEmpty())) {
                    ActivityInfo activityInfo = ((ResolveInfo) configActivities.get(0)).activityInfo;
                    if (activityInfo != null) {
                        configurationActivityClassName = activityInfo.name;
                    }
                }
                String scoringServiceClassName = null;
                Intent intent2 = new Intent(NetworkScoreManager.ACTION_SCORE_NETWORKS);
                intent2.setPackage(receiverInfo.packageName);
                ResolveInfo resolveServiceInfo = pm.resolveService(intent2, 0);
                if (!(resolveServiceInfo == null || resolveServiceInfo.serviceInfo == null)) {
                    scoringServiceClassName = resolveServiceInfo.serviceInfo.name;
                }
                scorers.add(new NetworkScorerAppData(receiverInfo.packageName, receiverInfo.applicationInfo.uid, receiverInfo.loadLabel(pm), configurationActivityClassName, scoringServiceClassName));
            }
        }
        return scorers;
    }

    public static NetworkScorerAppData getActiveScorer(Context context) {
        return getScorer(context, Global.getString(context.getContentResolver(), Global.NETWORK_SCORER_APP));
    }

    public static boolean setActiveScorer(Context context, String packageName) {
        String oldPackageName = Global.getString(context.getContentResolver(), Global.NETWORK_SCORER_APP);
        if (TextUtils.equals(oldPackageName, packageName)) {
            return true;
        }
        Log.i(TAG, "Changing network scorer from " + oldPackageName + " to " + packageName);
        if (packageName == null) {
            Global.putString(context.getContentResolver(), Global.NETWORK_SCORER_APP, null);
            return true;
        } else if (getScorer(context, packageName) != null) {
            Global.putString(context.getContentResolver(), Global.NETWORK_SCORER_APP, packageName);
            return true;
        } else {
            Log.w(TAG, "Requested network scorer is not valid: " + packageName);
            return false;
        }
    }

    public static boolean isCallerActiveScorer(Context context, int callingUid) {
        boolean z = false;
        NetworkScorerAppData defaultApp = getActiveScorer(context);
        if (defaultApp == null || callingUid != defaultApp.mPackageUid) {
            return false;
        }
        if (context.checkCallingPermission(permission.SCORE_NETWORKS) == 0) {
            z = true;
        }
        return z;
    }

    public static NetworkScorerAppData getScorer(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        for (NetworkScorerAppData app : getAllValidScorers(context)) {
            if (packageName.equals(app.mPackageName)) {
                return app;
            }
        }
        return null;
    }
}

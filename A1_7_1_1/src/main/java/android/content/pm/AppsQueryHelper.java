package android.content.pm;

import android.app.AppGlobals;
import java.util.List;

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
public class AppsQueryHelper {
    public static int GET_APPS_WITH_INTERACT_ACROSS_USERS_PERM;
    public static int GET_IMES;
    public static int GET_NON_LAUNCHABLE_APPS;
    public static int GET_REQUIRED_FOR_SYSTEM_USER;
    private List<ApplicationInfo> mAllApps;
    private final IPackageManager mPackageManager;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.pm.AppsQueryHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.pm.AppsQueryHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.pm.AppsQueryHelper.<init>(android.content.pm.IPackageManager):void, dex: 
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
    public AppsQueryHelper(android.content.pm.IPackageManager r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.pm.AppsQueryHelper.<init>(android.content.pm.IPackageManager):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.<init>(android.content.pm.IPackageManager):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.AppsQueryHelper.getAllApps(int):java.util.List<android.content.pm.ApplicationInfo>, dex:  in method: android.content.pm.AppsQueryHelper.getAllApps(int):java.util.List<android.content.pm.ApplicationInfo>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.AppsQueryHelper.getAllApps(int):java.util.List<android.content.pm.ApplicationInfo>, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected java.util.List<android.content.pm.ApplicationInfo> getAllApps(int r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.content.pm.AppsQueryHelper.getAllApps(int):java.util.List<android.content.pm.ApplicationInfo>, dex:  in method: android.content.pm.AppsQueryHelper.getAllApps(int):java.util.List<android.content.pm.ApplicationInfo>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.getAllApps(int):java.util.List<android.content.pm.ApplicationInfo>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.AppsQueryHelper.getPackagesHoldingPermission(java.lang.String, int):java.util.List<android.content.pm.PackageInfo>, dex:  in method: android.content.pm.AppsQueryHelper.getPackagesHoldingPermission(java.lang.String, int):java.util.List<android.content.pm.PackageInfo>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.AppsQueryHelper.getPackagesHoldingPermission(java.lang.String, int):java.util.List<android.content.pm.PackageInfo>, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected java.util.List<android.content.pm.PackageInfo> getPackagesHoldingPermission(java.lang.String r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.content.pm.AppsQueryHelper.getPackagesHoldingPermission(java.lang.String, int):java.util.List<android.content.pm.PackageInfo>, dex:  in method: android.content.pm.AppsQueryHelper.getPackagesHoldingPermission(java.lang.String, int):java.util.List<android.content.pm.PackageInfo>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.getPackagesHoldingPermission(java.lang.String, int):java.util.List<android.content.pm.PackageInfo>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.content.pm.AppsQueryHelper.queryApps(int, boolean, android.os.UserHandle):java.util.List<java.lang.String>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public java.util.List<java.lang.String> queryApps(int r1, boolean r2, android.os.UserHandle r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.content.pm.AppsQueryHelper.queryApps(int, boolean, android.os.UserHandle):java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.queryApps(int, boolean, android.os.UserHandle):java.util.List<java.lang.String>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.AppsQueryHelper.queryIntentActivitiesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex:  in method: android.content.pm.AppsQueryHelper.queryIntentActivitiesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.AppsQueryHelper.queryIntentActivitiesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected java.util.List<android.content.pm.ResolveInfo> queryIntentActivitiesAsUser(android.content.Intent r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.content.pm.AppsQueryHelper.queryIntentActivitiesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex:  in method: android.content.pm.AppsQueryHelper.queryIntentActivitiesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.queryIntentActivitiesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.AppsQueryHelper.queryIntentServicesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex:  in method: android.content.pm.AppsQueryHelper.queryIntentServicesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.AppsQueryHelper.queryIntentServicesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected java.util.List<android.content.pm.ResolveInfo> queryIntentServicesAsUser(android.content.Intent r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.content.pm.AppsQueryHelper.queryIntentServicesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex:  in method: android.content.pm.AppsQueryHelper.queryIntentServicesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.AppsQueryHelper.queryIntentServicesAsUser(android.content.Intent, int):java.util.List<android.content.pm.ResolveInfo>");
    }

    public AppsQueryHelper() {
        this(AppGlobals.getPackageManager());
    }
}

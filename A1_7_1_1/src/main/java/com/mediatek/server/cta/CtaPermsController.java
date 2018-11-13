package com.mediatek.server.cta;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.content.pm.PermissionRecords;
import com.mediatek.server.cta.impl.CtaPermLinker;
import com.mediatek.server.cta.impl.PermErrorHelper;
import com.mediatek.server.cta.impl.PermRecordsController;
import com.mediatek.server.cta.impl.PermReviewFlagHelper;
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
public class CtaPermsController {
    public static boolean DEBUG;
    private Context mContext;
    private PermRecordsController mPermRecordsController;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.server.cta.CtaPermsController.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.server.cta.CtaPermsController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.server.cta.CtaPermsController.<clinit>():void");
    }

    public CtaPermsController(Context context) {
        this.mContext = context;
        this.mPermRecordsController = new PermRecordsController(context);
    }

    public void configDebugFlag(boolean z) {
        DEBUG = z;
    }

    public void systemReady() {
        this.mPermRecordsController.systemReady();
    }

    public boolean isPermissionReviewRequired(Package packageR, int i, boolean z) {
        return PermReviewFlagHelper.getInstance(this.mContext).isPermissionReviewRequired(packageR, i, z);
    }

    public List<String> getPermRecordPkgs() {
        return this.mPermRecordsController.getPermRecordPkgs();
    }

    public List<String> getPermRecordPerms(String str) {
        return this.mPermRecordsController.getPermRecordPerms(str);
    }

    public PermissionRecords getPermRecords(String str, String str2) {
        return this.mPermRecordsController.getPermRecords(str, str2);
    }

    public void reportPermRequestUsage(String str, int i) {
        this.mPermRecordsController.reportPermRequestUsage(str, i);
    }

    public void shutdown() {
        this.mPermRecordsController.shutdown();
    }

    public String parsePermName(int i, String str, String str2) {
        return PermErrorHelper.getInstance(this.mContext).parsePermName(i, str, str2);
    }

    public void linkCtaPermissions(Package packageR) {
        CtaPermLinker.getInstance(this.mContext).link(packageR);
    }
}

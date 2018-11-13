package android.service.notification;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

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
public class NotificationRankingUpdate implements Parcelable {
    public static final Creator<NotificationRankingUpdate> CREATOR = null;
    private final int[] mImportance;
    private final Bundle mImportanceExplanation;
    private final String[] mInterceptedKeys;
    private final String[] mKeys;
    private final Bundle mOverrideGroupKeys;
    private final Bundle mSuppressedVisualEffects;
    private final Bundle mVisibilityOverrides;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.service.notification.NotificationRankingUpdate.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.service.notification.NotificationRankingUpdate.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.notification.NotificationRankingUpdate.<clinit>():void");
    }

    public NotificationRankingUpdate(String[] keys, String[] interceptedKeys, Bundle visibilityOverrides, Bundle suppressedVisualEffects, int[] importance, Bundle explanation, Bundle overrideGroupKeys) {
        this.mKeys = keys;
        this.mInterceptedKeys = interceptedKeys;
        this.mVisibilityOverrides = visibilityOverrides;
        this.mSuppressedVisualEffects = suppressedVisualEffects;
        this.mImportance = importance;
        this.mImportanceExplanation = explanation;
        this.mOverrideGroupKeys = overrideGroupKeys;
    }

    public NotificationRankingUpdate(Parcel in) {
        this.mKeys = in.readStringArray();
        this.mInterceptedKeys = in.readStringArray();
        this.mVisibilityOverrides = in.readBundle();
        this.mSuppressedVisualEffects = in.readBundle();
        this.mImportance = new int[this.mKeys.length];
        in.readIntArray(this.mImportance);
        this.mImportanceExplanation = in.readBundle();
        this.mOverrideGroupKeys = in.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(this.mKeys);
        out.writeStringArray(this.mInterceptedKeys);
        out.writeBundle(this.mVisibilityOverrides);
        out.writeBundle(this.mSuppressedVisualEffects);
        out.writeIntArray(this.mImportance);
        out.writeBundle(this.mImportanceExplanation);
        out.writeBundle(this.mOverrideGroupKeys);
    }

    public String[] getOrderedKeys() {
        return this.mKeys;
    }

    public String[] getInterceptedKeys() {
        return this.mInterceptedKeys;
    }

    public Bundle getVisibilityOverrides() {
        return this.mVisibilityOverrides;
    }

    public Bundle getSuppressedVisualEffects() {
        return this.mSuppressedVisualEffects;
    }

    public int[] getImportance() {
        return this.mImportance;
    }

    public Bundle getImportanceExplanation() {
        return this.mImportanceExplanation;
    }

    public Bundle getOverrideGroupKeys() {
        return this.mOverrideGroupKeys;
    }
}

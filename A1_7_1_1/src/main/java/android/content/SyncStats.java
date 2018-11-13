package android.content;

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
public class SyncStats implements Parcelable {
    public static final Creator<SyncStats> CREATOR = null;
    public long numAuthExceptions;
    public long numConflictDetectedExceptions;
    public long numDeletes;
    public long numEntries;
    public long numInserts;
    public long numIoExceptions;
    public long numParseExceptions;
    public long numSkippedEntries;
    public long numUpdates;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.SyncStats.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.SyncStats.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.SyncStats.<clinit>():void");
    }

    public SyncStats() {
        this.numAuthExceptions = 0;
        this.numIoExceptions = 0;
        this.numParseExceptions = 0;
        this.numConflictDetectedExceptions = 0;
        this.numInserts = 0;
        this.numUpdates = 0;
        this.numDeletes = 0;
        this.numEntries = 0;
        this.numSkippedEntries = 0;
    }

    public SyncStats(Parcel in) {
        this.numAuthExceptions = in.readLong();
        this.numIoExceptions = in.readLong();
        this.numParseExceptions = in.readLong();
        this.numConflictDetectedExceptions = in.readLong();
        this.numInserts = in.readLong();
        this.numUpdates = in.readLong();
        this.numDeletes = in.readLong();
        this.numEntries = in.readLong();
        this.numSkippedEntries = in.readLong();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" stats [");
        if (this.numAuthExceptions > 0) {
            sb.append(" numAuthExceptions: ").append(this.numAuthExceptions);
        }
        if (this.numIoExceptions > 0) {
            sb.append(" numIoExceptions: ").append(this.numIoExceptions);
        }
        if (this.numParseExceptions > 0) {
            sb.append(" numParseExceptions: ").append(this.numParseExceptions);
        }
        if (this.numConflictDetectedExceptions > 0) {
            sb.append(" numConflictDetectedExceptions: ").append(this.numConflictDetectedExceptions);
        }
        if (this.numInserts > 0) {
            sb.append(" numInserts: ").append(this.numInserts);
        }
        if (this.numUpdates > 0) {
            sb.append(" numUpdates: ").append(this.numUpdates);
        }
        if (this.numDeletes > 0) {
            sb.append(" numDeletes: ").append(this.numDeletes);
        }
        if (this.numEntries > 0) {
            sb.append(" numEntries: ").append(this.numEntries);
        }
        if (this.numSkippedEntries > 0) {
            sb.append(" numSkippedEntries: ").append(this.numSkippedEntries);
        }
        sb.append("]");
        return sb.toString();
    }

    public void clear() {
        this.numAuthExceptions = 0;
        this.numIoExceptions = 0;
        this.numParseExceptions = 0;
        this.numConflictDetectedExceptions = 0;
        this.numInserts = 0;
        this.numUpdates = 0;
        this.numDeletes = 0;
        this.numEntries = 0;
        this.numSkippedEntries = 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.numAuthExceptions);
        dest.writeLong(this.numIoExceptions);
        dest.writeLong(this.numParseExceptions);
        dest.writeLong(this.numConflictDetectedExceptions);
        dest.writeLong(this.numInserts);
        dest.writeLong(this.numUpdates);
        dest.writeLong(this.numDeletes);
        dest.writeLong(this.numEntries);
        dest.writeLong(this.numSkippedEntries);
    }
}

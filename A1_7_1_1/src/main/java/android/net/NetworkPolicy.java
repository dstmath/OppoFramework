package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.BackupUtils;
import android.util.BackupUtils.BadVersionException;
import com.android.internal.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

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
public class NetworkPolicy implements Parcelable, Comparable<NetworkPolicy> {
    private static final int BACKUP_VERSION = 1;
    public static final Creator<NetworkPolicy> CREATOR = null;
    public static final int CYCLE_NONE = -1;
    private static final long DEFAULT_MTU = 1500;
    public static final long LIMIT_DISABLED = -1;
    public static final long SNOOZE_NEVER = -1;
    public static final long WARNING_DISABLED = -1;
    public int cycleDay;
    public String cycleTimezone;
    public boolean inferred;
    public long lastLimitSnooze;
    public long lastWarningSnooze;
    public long limitBytes;
    public boolean metered;
    public NetworkTemplate template;
    public long warningBytes;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.NetworkPolicy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.NetworkPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkPolicy.<clinit>():void");
    }

    @Deprecated
    public NetworkPolicy(NetworkTemplate template, int cycleDay, String cycleTimezone, long warningBytes, long limitBytes, boolean metered) {
        this(template, cycleDay, cycleTimezone, warningBytes, limitBytes, -1, -1, metered, false);
    }

    public NetworkPolicy(NetworkTemplate template, int cycleDay, String cycleTimezone, long warningBytes, long limitBytes, long lastWarningSnooze, long lastLimitSnooze, boolean metered, boolean inferred) {
        this.template = (NetworkTemplate) Preconditions.checkNotNull(template, "missing NetworkTemplate");
        this.cycleDay = cycleDay;
        this.cycleTimezone = (String) Preconditions.checkNotNull(cycleTimezone, "missing cycleTimezone");
        this.warningBytes = warningBytes;
        this.limitBytes = limitBytes;
        this.lastWarningSnooze = lastWarningSnooze;
        this.lastLimitSnooze = lastLimitSnooze;
        this.metered = metered;
        this.inferred = inferred;
    }

    public NetworkPolicy(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.template = (NetworkTemplate) in.readParcelable(null);
        this.cycleDay = in.readInt();
        this.cycleTimezone = in.readString();
        this.warningBytes = in.readLong();
        this.limitBytes = in.readLong();
        this.lastWarningSnooze = in.readLong();
        this.lastLimitSnooze = in.readLong();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.metered = z;
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.inferred = z2;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeParcelable(this.template, flags);
        dest.writeInt(this.cycleDay);
        dest.writeString(this.cycleTimezone);
        dest.writeLong(this.warningBytes);
        dest.writeLong(this.limitBytes);
        dest.writeLong(this.lastWarningSnooze);
        dest.writeLong(this.lastLimitSnooze);
        if (this.metered) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.inferred) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public int describeContents() {
        return 0;
    }

    public boolean isOverWarning(long totalBytes) {
        return this.warningBytes != -1 && totalBytes >= this.warningBytes;
    }

    public boolean isOverLimit(long totalBytes) {
        totalBytes += 3000;
        if (this.limitBytes == -1 || totalBytes < this.limitBytes) {
            return false;
        }
        return true;
    }

    public void clearSnooze() {
        this.lastWarningSnooze = -1;
        this.lastLimitSnooze = -1;
    }

    public boolean hasCycle() {
        return this.cycleDay != -1;
    }

    public int compareTo(NetworkPolicy another) {
        if (another == null || another.limitBytes == -1) {
            return -1;
        }
        if (this.limitBytes == -1 || another.limitBytes < this.limitBytes) {
            return 1;
        }
        return 0;
    }

    public int hashCode() {
        Object[] objArr = new Object[9];
        objArr[0] = this.template;
        objArr[1] = Integer.valueOf(this.cycleDay);
        objArr[2] = this.cycleTimezone;
        objArr[3] = Long.valueOf(this.warningBytes);
        objArr[4] = Long.valueOf(this.limitBytes);
        objArr[5] = Long.valueOf(this.lastWarningSnooze);
        objArr[6] = Long.valueOf(this.lastLimitSnooze);
        objArr[7] = Boolean.valueOf(this.metered);
        objArr[8] = Boolean.valueOf(this.inferred);
        return Objects.hash(objArr);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkPolicy)) {
            return false;
        }
        NetworkPolicy other = (NetworkPolicy) obj;
        if (this.cycleDay == other.cycleDay && this.warningBytes == other.warningBytes && this.limitBytes == other.limitBytes && this.lastWarningSnooze == other.lastWarningSnooze && this.lastLimitSnooze == other.lastLimitSnooze && this.metered == other.metered && this.inferred == other.inferred && Objects.equals(this.cycleTimezone, other.cycleTimezone)) {
            z = Objects.equals(this.template, other.template);
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("Np");
        builder.append("[").append(this.template).append("]");
        builder.append(":").append(this.cycleDay);
        builder.append(",").append(this.cycleTimezone);
        builder.append(", ").append(this.warningBytes);
        builder.append(",").append(this.limitBytes);
        builder.append(",").append(this.lastWarningSnooze);
        builder.append(",").append(this.lastLimitSnooze);
        builder.append(",").append(this.metered);
        builder.append(",").append(this.inferred);
        return builder.toString();
    }

    public byte[] getBytesForBackup() throws IOException {
        int i;
        int i2 = 1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(1);
        out.write(this.template.getBytesForBackup());
        out.writeInt(this.cycleDay);
        BackupUtils.writeString(out, this.cycleTimezone);
        out.writeLong(this.warningBytes);
        out.writeLong(this.limitBytes);
        out.writeLong(this.lastWarningSnooze);
        out.writeLong(this.lastLimitSnooze);
        if (this.metered) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.inferred) {
            i2 = 0;
        }
        out.writeInt(i2);
        return baos.toByteArray();
    }

    public static NetworkPolicy getNetworkPolicyFromBackup(DataInputStream in) throws IOException, BadVersionException {
        int version = in.readInt();
        if (version < 1 || version > 1) {
            throw new BadVersionException("Unknown Backup Serialization Version");
        }
        return new NetworkPolicy(NetworkTemplate.getNetworkTemplateFromBackup(in), in.readInt(), BackupUtils.readString(in), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readInt() == 1, in.readInt() == 1);
    }
}

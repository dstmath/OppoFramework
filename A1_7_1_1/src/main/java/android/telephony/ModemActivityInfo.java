package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

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
public class ModemActivityInfo implements Parcelable {
    public static final Creator<ModemActivityInfo> CREATOR = null;
    public static final int TX_POWER_LEVELS = 5;
    private final int mEnergyUsed;
    private final int mIdleTimeMs;
    private final int mRxTimeMs;
    private final int mSleepTimeMs;
    private final long mTimestamp;
    private final int[] mTxTimeMs;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.ModemActivityInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.ModemActivityInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.ModemActivityInfo.<clinit>():void");
    }

    public ModemActivityInfo(long timestamp, int sleepTimeMs, int idleTimeMs, int[] txTimeMs, int rxTimeMs, int energyUsed) {
        this.mTxTimeMs = new int[5];
        this.mTimestamp = timestamp;
        this.mSleepTimeMs = sleepTimeMs;
        this.mIdleTimeMs = idleTimeMs;
        if (txTimeMs != null) {
            System.arraycopy(txTimeMs, 0, this.mTxTimeMs, 0, Math.min(txTimeMs.length, 5));
        }
        this.mRxTimeMs = rxTimeMs;
        this.mEnergyUsed = energyUsed;
    }

    public String toString() {
        return "ModemActivityInfo{ mTimestamp=" + this.mTimestamp + " mSleepTimeMs=" + this.mSleepTimeMs + " mIdleTimeMs=" + this.mIdleTimeMs + " mTxTimeMs[]=" + Arrays.toString(this.mTxTimeMs) + " mRxTimeMs=" + this.mRxTimeMs + " mEnergyUsed=" + this.mEnergyUsed + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mTimestamp);
        dest.writeInt(this.mSleepTimeMs);
        dest.writeInt(this.mIdleTimeMs);
        for (int i = 0; i < 5; i++) {
            dest.writeInt(this.mTxTimeMs[i]);
        }
        dest.writeInt(this.mRxTimeMs);
        dest.writeInt(this.mEnergyUsed);
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public int[] getTxTimeMillis() {
        return this.mTxTimeMs;
    }

    public int getSleepTimeMillis() {
        return this.mSleepTimeMs;
    }

    public int getIdleTimeMillis() {
        return this.mIdleTimeMs;
    }

    public int getRxTimeMillis() {
        return this.mRxTimeMs;
    }

    public int getEnergyUsed() {
        return this.mEnergyUsed;
    }

    public boolean isValid() {
        boolean z = false;
        for (int txVal : getTxTimeMillis()) {
            if (txVal < 0) {
                return false;
            }
        }
        if (getIdleTimeMillis() >= 0 && getSleepTimeMillis() >= 0 && getRxTimeMillis() >= 0 && getEnergyUsed() >= 0 && !isEmpty()) {
            z = true;
        }
        return z;
    }

    private boolean isEmpty() {
        boolean z = false;
        for (int txVal : getTxTimeMillis()) {
            if (txVal != 0) {
                return false;
            }
        }
        if (getIdleTimeMillis() == 0 && getSleepTimeMillis() == 0 && getRxTimeMillis() == 0 && getEnergyUsed() == 0) {
            z = true;
        }
        return z;
    }
}

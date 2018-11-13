package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
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
public class KeymasterArguments implements Parcelable {
    public static final Creator<KeymasterArguments> CREATOR = null;
    public static final long UINT32_MAX_VALUE = 4294967295L;
    private static final long UINT32_RANGE = 4294967296L;
    public static final BigInteger UINT64_MAX_VALUE = null;
    private static final BigInteger UINT64_RANGE = null;
    private List<KeymasterArgument> mArguments;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.security.keymaster.KeymasterArguments.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.security.keymaster.KeymasterArguments.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.keymaster.KeymasterArguments.<clinit>():void");
    }

    /* synthetic */ KeymasterArguments(Parcel in, KeymasterArguments keymasterArguments) {
        this(in);
    }

    public KeymasterArguments() {
        this.mArguments = new ArrayList();
    }

    private KeymasterArguments(Parcel in) {
        this.mArguments = in.createTypedArrayList(KeymasterArgument.CREATOR);
    }

    public void addEnum(int tag, int value) {
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == 268435456 || tagType == 536870912) {
            addEnumTag(tag, value);
            return;
        }
        throw new IllegalArgumentException("Not an enum or repeating enum tag: " + tag);
    }

    public void addEnums(int tag, int... values) {
        if (KeymasterDefs.getTagType(tag) != 536870912) {
            throw new IllegalArgumentException("Not a repeating enum tag: " + tag);
        }
        for (int value : values) {
            addEnumTag(tag, value);
        }
    }

    public int getEnum(int tag, int defaultValue) {
        if (KeymasterDefs.getTagType(tag) != 268435456) {
            throw new IllegalArgumentException("Not an enum tag: " + tag);
        }
        KeymasterArgument arg = getArgumentByTag(tag);
        if (arg == null) {
            return defaultValue;
        }
        return getEnumTagValue(arg);
    }

    public List<Integer> getEnums(int tag) {
        if (KeymasterDefs.getTagType(tag) != 536870912) {
            throw new IllegalArgumentException("Not a repeating enum tag: " + tag);
        }
        List<Integer> values = new ArrayList();
        for (KeymasterArgument arg : this.mArguments) {
            if (arg.tag == tag) {
                values.add(Integer.valueOf(getEnumTagValue(arg)));
            }
        }
        return values;
    }

    private void addEnumTag(int tag, int value) {
        this.mArguments.add(new KeymasterIntArgument(tag, value));
    }

    private int getEnumTagValue(KeymasterArgument arg) {
        return ((KeymasterIntArgument) arg).value;
    }

    public void addUnsignedInt(int tag, long value) {
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType != 805306368 && tagType != 1073741824) {
            throw new IllegalArgumentException("Not an int or repeating int tag: " + tag);
        } else if (value < 0 || value > UINT32_MAX_VALUE) {
            throw new IllegalArgumentException("Int tag value out of range: " + value);
        } else {
            this.mArguments.add(new KeymasterIntArgument(tag, (int) value));
        }
    }

    public long getUnsignedInt(int tag, long defaultValue) {
        if (KeymasterDefs.getTagType(tag) != 805306368) {
            throw new IllegalArgumentException("Not an int tag: " + tag);
        }
        KeymasterArgument arg = getArgumentByTag(tag);
        if (arg == null) {
            return defaultValue;
        }
        return ((long) ((KeymasterIntArgument) arg).value) & UINT32_MAX_VALUE;
    }

    public void addUnsignedLong(int tag, BigInteger value) {
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == KeymasterDefs.KM_ULONG || tagType == KeymasterDefs.KM_ULONG_REP) {
            addLongTag(tag, value);
            return;
        }
        throw new IllegalArgumentException("Not a long or repeating long tag: " + tag);
    }

    public List<BigInteger> getUnsignedLongs(int tag) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_ULONG_REP) {
            throw new IllegalArgumentException("Tag is not a repeating long: " + tag);
        }
        List<BigInteger> values = new ArrayList();
        for (KeymasterArgument arg : this.mArguments) {
            if (arg.tag == tag) {
                values.add(getLongTagValue(arg));
            }
        }
        return values;
    }

    private void addLongTag(int tag, BigInteger value) {
        if (value.signum() == -1 || value.compareTo(UINT64_MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Long tag value out of range: " + value);
        }
        this.mArguments.add(new KeymasterLongArgument(tag, value.longValue()));
    }

    private BigInteger getLongTagValue(KeymasterArgument arg) {
        return toUint64(((KeymasterLongArgument) arg).value);
    }

    public void addBoolean(int tag) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_BOOL) {
            throw new IllegalArgumentException("Not a boolean tag: " + tag);
        }
        this.mArguments.add(new KeymasterBooleanArgument(tag));
    }

    public boolean getBoolean(int tag) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_BOOL) {
            throw new IllegalArgumentException("Not a boolean tag: " + tag);
        } else if (getArgumentByTag(tag) == null) {
            return false;
        } else {
            return true;
        }
    }

    public void addBytes(int tag, byte[] value) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_BYTES) {
            throw new IllegalArgumentException("Not a bytes tag: " + tag);
        } else if (value == null) {
            throw new NullPointerException("value == nulll");
        } else {
            this.mArguments.add(new KeymasterBlobArgument(tag, value));
        }
    }

    public byte[] getBytes(int tag, byte[] defaultValue) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_BYTES) {
            throw new IllegalArgumentException("Not a bytes tag: " + tag);
        }
        KeymasterArgument arg = getArgumentByTag(tag);
        if (arg == null) {
            return defaultValue;
        }
        return ((KeymasterBlobArgument) arg).blob;
    }

    public void addDate(int tag, Date value) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_DATE) {
            throw new IllegalArgumentException("Not a date tag: " + tag);
        } else if (value == null) {
            throw new NullPointerException("value == nulll");
        } else if (value.getTime() < 0) {
            throw new IllegalArgumentException("Date tag value out of range: " + value);
        } else {
            this.mArguments.add(new KeymasterDateArgument(tag, value));
        }
    }

    public void addDateIfNotNull(int tag, Date value) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_DATE) {
            throw new IllegalArgumentException("Not a date tag: " + tag);
        } else if (value != null) {
            addDate(tag, value);
        }
    }

    public Date getDate(int tag, Date defaultValue) {
        if (KeymasterDefs.getTagType(tag) != KeymasterDefs.KM_DATE) {
            throw new IllegalArgumentException("Tag is not a date type: " + tag);
        }
        KeymasterArgument arg = getArgumentByTag(tag);
        if (arg == null) {
            return defaultValue;
        }
        Date result = ((KeymasterDateArgument) arg).date;
        if (result.getTime() >= 0) {
            return result;
        }
        throw new IllegalArgumentException("Tag value too large. Tag: " + tag);
    }

    private KeymasterArgument getArgumentByTag(int tag) {
        for (KeymasterArgument arg : this.mArguments) {
            if (arg.tag == tag) {
                return arg;
            }
        }
        return null;
    }

    public boolean containsTag(int tag) {
        return getArgumentByTag(tag) != null;
    }

    public int size() {
        return this.mArguments.size();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(this.mArguments);
    }

    public void readFromParcel(Parcel in) {
        in.readTypedList(this.mArguments, KeymasterArgument.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public static BigInteger toUint64(long value) {
        if (value >= 0) {
            return BigInteger.valueOf(value);
        }
        return BigInteger.valueOf(value).add(UINT64_RANGE);
    }
}

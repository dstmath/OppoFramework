package com.google.protobuf.nano;

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
public final class FieldArray implements Cloneable {
    private static final FieldData DELETED = null;
    private FieldData[] mData;
    private int[] mFieldNumbers;
    private boolean mGarbage;
    private int mSize;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.google.protobuf.nano.FieldArray.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.google.protobuf.nano.FieldArray.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.nano.FieldArray.<clinit>():void");
    }

    FieldArray() {
        this(10);
    }

    FieldArray(int initialCapacity) {
        this.mGarbage = false;
        initialCapacity = idealIntArraySize(initialCapacity);
        this.mFieldNumbers = new int[initialCapacity];
        this.mData = new FieldData[initialCapacity];
        this.mSize = 0;
    }

    FieldData get(int fieldNumber) {
        int i = binarySearch(fieldNumber);
        if (i < 0 || this.mData[i] == DELETED) {
            return null;
        }
        return this.mData[i];
    }

    void remove(int fieldNumber) {
        int i = binarySearch(fieldNumber);
        if (i >= 0 && this.mData[i] != DELETED) {
            this.mData[i] = DELETED;
            this.mGarbage = true;
        }
    }

    private void gc() {
        int n = this.mSize;
        int o = 0;
        int[] keys = this.mFieldNumbers;
        FieldData[] values = this.mData;
        for (int i = 0; i < n; i++) {
            FieldData val = values[i];
            if (val != DELETED) {
                if (i != o) {
                    keys[o] = keys[i];
                    values[o] = val;
                    values[i] = null;
                }
                o++;
            }
        }
        this.mGarbage = false;
        this.mSize = o;
    }

    void put(int fieldNumber, FieldData data) {
        int i = binarySearch(fieldNumber);
        if (i >= 0) {
            this.mData[i] = data;
        } else {
            i = ~i;
            if (i >= this.mSize || this.mData[i] != DELETED) {
                if (this.mGarbage && this.mSize >= this.mFieldNumbers.length) {
                    gc();
                    i = ~binarySearch(fieldNumber);
                }
                if (this.mSize >= this.mFieldNumbers.length) {
                    int n = idealIntArraySize(this.mSize + 1);
                    int[] nkeys = new int[n];
                    FieldData[] nvalues = new FieldData[n];
                    System.arraycopy(this.mFieldNumbers, 0, nkeys, 0, this.mFieldNumbers.length);
                    System.arraycopy(this.mData, 0, nvalues, 0, this.mData.length);
                    this.mFieldNumbers = nkeys;
                    this.mData = nvalues;
                }
                if (this.mSize - i != 0) {
                    System.arraycopy(this.mFieldNumbers, i, this.mFieldNumbers, i + 1, this.mSize - i);
                    System.arraycopy(this.mData, i, this.mData, i + 1, this.mSize - i);
                }
                this.mFieldNumbers[i] = fieldNumber;
                this.mData[i] = data;
                this.mSize++;
            } else {
                this.mFieldNumbers[i] = fieldNumber;
                this.mData[i] = data;
            }
        }
    }

    int size() {
        if (this.mGarbage) {
            gc();
        }
        return this.mSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    FieldData dataAt(int index) {
        if (this.mGarbage) {
            gc();
        }
        return this.mData[index];
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof FieldArray)) {
            return false;
        }
        FieldArray other = (FieldArray) o;
        if (size() != other.size()) {
            return false;
        }
        if (arrayEquals(this.mFieldNumbers, other.mFieldNumbers, this.mSize)) {
            z = arrayEquals(this.mData, other.mData, this.mSize);
        }
        return z;
    }

    public int hashCode() {
        if (this.mGarbage) {
            gc();
        }
        int result = 17;
        for (int i = 0; i < this.mSize; i++) {
            result = (((result * 31) + this.mFieldNumbers[i]) * 31) + this.mData[i].hashCode();
        }
        return result;
    }

    private int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    private int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    private int binarySearch(int value) {
        int lo = 0;
        int hi = this.mSize - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = this.mFieldNumbers[mid];
            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal <= value) {
                return mid;
            } else {
                hi = mid - 1;
            }
        }
        return ~lo;
    }

    private boolean arrayEquals(int[] a, int[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean arrayEquals(FieldData[] a, FieldData[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    public final FieldArray clone() {
        int size = size();
        FieldArray clone = new FieldArray(size);
        System.arraycopy(this.mFieldNumbers, 0, clone.mFieldNumbers, 0, size);
        for (int i = 0; i < size; i++) {
            if (this.mData[i] != null) {
                clone.mData[i] = this.mData[i].clone();
            }
        }
        clone.mSize = size;
        return clone;
    }
}

package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

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
public class BitArray {
    private static final int BITS_PER_UNIT = 8;
    private static final int BYTES_PER_LINE = 8;
    private static final byte[][] NYBBLE = null;
    private int length;
    private byte[] repn;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.util.BitArray.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.util.BitArray.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.BitArray.<clinit>():void");
    }

    private static int subscript(int idx) {
        return idx / 8;
    }

    private static int position(int idx) {
        return 1 << (7 - (idx % 8));
    }

    public BitArray(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        this.length = length;
        this.repn = new byte[(((length + 8) - 1) / 8)];
    }

    public BitArray(int length, byte[] a) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        } else if (a.length * 8 < length) {
            throw new IllegalArgumentException("Byte array too short to represent bit array of given length");
        } else {
            this.length = length;
            int repLength = ((length + 8) - 1) / 8;
            byte bitMask = (byte) (255 << ((repLength * 8) - length));
            this.repn = new byte[repLength];
            System.arraycopy(a, 0, this.repn, 0, repLength);
            if (repLength > 0) {
                byte[] bArr = this.repn;
                int i = repLength - 1;
                bArr[i] = (byte) (bArr[i] & bitMask);
            }
        }
    }

    public BitArray(boolean[] bits) {
        this.length = bits.length;
        this.repn = new byte[((this.length + 7) / 8)];
        for (int i = 0; i < this.length; i++) {
            set(i, bits[i]);
        }
    }

    private BitArray(BitArray ba) {
        this.length = ba.length;
        this.repn = (byte[]) ba.repn.clone();
    }

    public boolean get(int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= this.length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        } else if ((this.repn[subscript(index)] & position(index)) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void set(int index, boolean value) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= this.length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        }
        int idx = subscript(index);
        int bit = position(index);
        byte[] bArr;
        if (value) {
            bArr = this.repn;
            bArr[idx] = (byte) (bArr[idx] | bit);
            return;
        }
        bArr = this.repn;
        bArr[idx] = (byte) (bArr[idx] & (~bit));
    }

    public int length() {
        return this.length;
    }

    public byte[] toByteArray() {
        return (byte[]) this.repn.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof BitArray)) {
            return false;
        }
        BitArray ba = (BitArray) obj;
        if (ba.length != this.length) {
            return false;
        }
        for (int i = 0; i < this.repn.length; i++) {
            if (this.repn[i] != ba.repn[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean[] toBooleanArray() {
        boolean[] bits = new boolean[this.length];
        for (int i = 0; i < this.length; i++) {
            bits[i] = get(i);
        }
        return bits;
    }

    public int hashCode() {
        int hashCode = 0;
        for (byte b : this.repn) {
            hashCode = (hashCode * 31) + b;
        }
        return this.length ^ hashCode;
    }

    public Object clone() {
        return new BitArray(this);
    }

    public String toString() {
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (i = 0; i < this.repn.length - 1; i++) {
            out.write(NYBBLE[(this.repn[i] >> 4) & 15], 0, 4);
            out.write(NYBBLE[this.repn[i] & 15], 0, 4);
            if (i % 8 == 7) {
                out.write(10);
            } else {
                out.write(32);
            }
        }
        for (i = (this.repn.length - 1) * 8; i < this.length; i++) {
            out.write(get(i) ? 49 : 48);
        }
        return new String(out.toByteArray());
    }

    public BitArray truncate() {
        for (int i = this.length - 1; i >= 0; i--) {
            if (get(i)) {
                return new BitArray(i + 1, Arrays.copyOf(this.repn, (i + 8) / 8));
            }
        }
        return new BitArray(1);
    }
}

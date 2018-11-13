package com.android.org.bouncycastle.util;

import java.math.BigInteger;

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
public final class Arrays {

    public static class Iterator<T> implements java.util.Iterator<T> {
        private final T[] dataArray;
        private int position;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.android.org.bouncycastle.util.Arrays.Iterator.<init>(java.lang.Object[]):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public Iterator(T[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.android.org.bouncycastle.util.Arrays.Iterator.<init>(java.lang.Object[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.util.Arrays.Iterator.<init>(java.lang.Object[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.org.bouncycastle.util.Arrays.Iterator.hasNext():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean hasNext() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.org.bouncycastle.util.Arrays.Iterator.hasNext():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.util.Arrays.Iterator.hasNext():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.org.bouncycastle.util.Arrays.Iterator.next():T, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public T next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.org.bouncycastle.util.Arrays.Iterator.next():T, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.util.Arrays.Iterator.next():T");
        }

        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from an Array.");
        }
    }

    private Arrays() {
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean areEqual(boolean[] a, boolean[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean areEqual(char[] a, char[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean areEqual(byte[] a, byte[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean constantTimeAreEqual(byte[] a, byte[] b) {
        boolean z = true;
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        int nonEqual = 0;
        for (int i = 0; i != a.length; i++) {
            nonEqual |= a[i] ^ b[i];
        }
        if (nonEqual != 0) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean areEqual(int[] a, int[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean areEqual(long[] a, long[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean areEqual(Object[] a, Object[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i != a.length; i++) {
            Object objA = a[i];
            Object objB = b[i];
            if (objA == null) {
                if (objB != null) {
                    return false;
                }
            } else if (!objA.equals(objB)) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains(short[] a, short n) {
        for (short s : a) {
            if (s == n) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(int[] a, int n) {
        for (int i : a) {
            if (i == n) {
                return true;
            }
        }
        return false;
    }

    public static void fill(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public static void fill(char[] array, char value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public static void fill(long[] array, long value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public static void fill(short[] array, short value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public static void fill(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public static int hashCode(byte[] data) {
        if (data == null) {
            return 0;
        }
        int i = data.length;
        int hc = i + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ data[i];
        }
    }

    public static int hashCode(byte[] data, int off, int len) {
        if (data == null) {
            return 0;
        }
        int i = len;
        int hc = len + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ data[off + i];
        }
    }

    public static int hashCode(char[] data) {
        if (data == null) {
            return 0;
        }
        int i = data.length;
        int hc = i + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ data[i];
        }
    }

    public static int hashCode(int[][] ints) {
        int hc = 0;
        for (int i = 0; i != ints.length; i++) {
            hc = (hc * 257) + hashCode(ints[i]);
        }
        return hc;
    }

    public static int hashCode(int[] data) {
        if (data == null) {
            return 0;
        }
        int i = data.length;
        int hc = i + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ data[i];
        }
    }

    public static int hashCode(int[] data, int off, int len) {
        if (data == null) {
            return 0;
        }
        int i = len;
        int hc = len + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ data[off + i];
        }
    }

    public static int hashCode(long[] data) {
        if (data == null) {
            return 0;
        }
        int i = data.length;
        int hc = i + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            long di = data[i];
            hc = (((hc * 257) ^ ((int) di)) * 257) ^ ((int) (di >>> 32));
        }
    }

    public static int hashCode(long[] data, int off, int len) {
        if (data == null) {
            return 0;
        }
        int i = len;
        int hc = len + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            long di = data[off + i];
            hc = (((hc * 257) ^ ((int) di)) * 257) ^ ((int) (di >>> 32));
        }
    }

    public static int hashCode(short[][][] shorts) {
        int hc = 0;
        for (int i = 0; i != shorts.length; i++) {
            hc = (hc * 257) + hashCode(shorts[i]);
        }
        return hc;
    }

    public static int hashCode(short[][] shorts) {
        int hc = 0;
        for (int i = 0; i != shorts.length; i++) {
            hc = (hc * 257) + hashCode(shorts[i]);
        }
        return hc;
    }

    public static int hashCode(short[] data) {
        if (data == null) {
            return 0;
        }
        int i = data.length;
        int hc = i + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ (data[i] & 255);
        }
    }

    public static int hashCode(Object[] data) {
        if (data == null) {
            return 0;
        }
        int i = data.length;
        int hc = i + 1;
        while (true) {
            i--;
            if (i < 0) {
                return hc;
            }
            hc = (hc * 257) ^ data[i].hashCode();
        }
    }

    public static byte[] clone(byte[] data) {
        if (data == null) {
            return null;
        }
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public static char[] clone(char[] data) {
        if (data == null) {
            return null;
        }
        char[] copy = new char[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public static byte[] clone(byte[] data, byte[] existing) {
        if (data == null) {
            return null;
        }
        if (existing == null || existing.length != data.length) {
            return clone(data);
        }
        System.arraycopy(data, 0, existing, 0, existing.length);
        return existing;
    }

    public static byte[][] clone(byte[][] data) {
        if (data == null) {
            return null;
        }
        byte[][] copy = new byte[data.length][];
        for (int i = 0; i != copy.length; i++) {
            copy[i] = clone(data[i]);
        }
        return copy;
    }

    public static byte[][][] clone(byte[][][] data) {
        if (data == null) {
            return null;
        }
        byte[][][] copy = new byte[data.length][][];
        for (int i = 0; i != copy.length; i++) {
            copy[i] = clone(data[i]);
        }
        return copy;
    }

    public static int[] clone(int[] data) {
        if (data == null) {
            return null;
        }
        int[] copy = new int[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public static long[] clone(long[] data) {
        if (data == null) {
            return null;
        }
        long[] copy = new long[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public static long[] clone(long[] data, long[] existing) {
        if (data == null) {
            return null;
        }
        if (existing == null || existing.length != data.length) {
            return clone(data);
        }
        System.arraycopy(data, 0, existing, 0, existing.length);
        return existing;
    }

    public static short[] clone(short[] data) {
        if (data == null) {
            return null;
        }
        short[] copy = new short[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public static BigInteger[] clone(BigInteger[] data) {
        if (data == null) {
            return null;
        }
        BigInteger[] copy = new BigInteger[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    public static byte[] copyOf(byte[] data, int newLength) {
        byte[] tmp = new byte[newLength];
        if (newLength < data.length) {
            System.arraycopy(data, 0, tmp, 0, newLength);
        } else {
            System.arraycopy(data, 0, tmp, 0, data.length);
        }
        return tmp;
    }

    public static char[] copyOf(char[] data, int newLength) {
        char[] tmp = new char[newLength];
        if (newLength < data.length) {
            System.arraycopy(data, 0, tmp, 0, newLength);
        } else {
            System.arraycopy(data, 0, tmp, 0, data.length);
        }
        return tmp;
    }

    public static int[] copyOf(int[] data, int newLength) {
        int[] tmp = new int[newLength];
        if (newLength < data.length) {
            System.arraycopy(data, 0, tmp, 0, newLength);
        } else {
            System.arraycopy(data, 0, tmp, 0, data.length);
        }
        return tmp;
    }

    public static long[] copyOf(long[] data, int newLength) {
        long[] tmp = new long[newLength];
        if (newLength < data.length) {
            System.arraycopy(data, 0, tmp, 0, newLength);
        } else {
            System.arraycopy(data, 0, tmp, 0, data.length);
        }
        return tmp;
    }

    public static BigInteger[] copyOf(BigInteger[] data, int newLength) {
        BigInteger[] tmp = new BigInteger[newLength];
        if (newLength < data.length) {
            System.arraycopy(data, 0, tmp, 0, newLength);
        } else {
            System.arraycopy(data, 0, tmp, 0, data.length);
        }
        return tmp;
    }

    public static byte[] copyOfRange(byte[] data, int from, int to) {
        int newLength = getLength(from, to);
        byte[] tmp = new byte[newLength];
        if (data.length - from < newLength) {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        } else {
            System.arraycopy(data, from, tmp, 0, newLength);
        }
        return tmp;
    }

    public static int[] copyOfRange(int[] data, int from, int to) {
        int newLength = getLength(from, to);
        int[] tmp = new int[newLength];
        if (data.length - from < newLength) {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        } else {
            System.arraycopy(data, from, tmp, 0, newLength);
        }
        return tmp;
    }

    public static long[] copyOfRange(long[] data, int from, int to) {
        int newLength = getLength(from, to);
        long[] tmp = new long[newLength];
        if (data.length - from < newLength) {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        } else {
            System.arraycopy(data, from, tmp, 0, newLength);
        }
        return tmp;
    }

    public static BigInteger[] copyOfRange(BigInteger[] data, int from, int to) {
        int newLength = getLength(from, to);
        BigInteger[] tmp = new BigInteger[newLength];
        if (data.length - from < newLength) {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        } else {
            System.arraycopy(data, from, tmp, 0, newLength);
        }
        return tmp;
    }

    private static int getLength(int from, int to) {
        int newLength = to - from;
        if (newLength >= 0) {
            return newLength;
        }
        StringBuffer sb = new StringBuffer(from);
        sb.append(" > ").append(to);
        throw new IllegalArgumentException(sb.toString());
    }

    public static byte[] append(byte[] a, byte b) {
        if (a == null) {
            return new byte[]{b};
        }
        int length = a.length;
        byte[] result = new byte[(length + 1)];
        System.arraycopy(a, 0, result, 0, length);
        result[length] = b;
        return result;
    }

    public static short[] append(short[] a, short b) {
        if (a == null) {
            return new short[]{b};
        }
        int length = a.length;
        short[] result = new short[(length + 1)];
        System.arraycopy(a, 0, result, 0, length);
        result[length] = b;
        return result;
    }

    public static int[] append(int[] a, int b) {
        if (a == null) {
            return new int[]{b};
        }
        int length = a.length;
        int[] result = new int[(length + 1)];
        System.arraycopy(a, 0, result, 0, length);
        result[length] = b;
        return result;
    }

    public static byte[] concatenate(byte[] a, byte[] b) {
        if (a != null && b != null) {
            byte[] rv = new byte[(a.length + b.length)];
            System.arraycopy(a, 0, rv, 0, a.length);
            System.arraycopy(b, 0, rv, a.length, b.length);
            return rv;
        } else if (b != null) {
            return clone(b);
        } else {
            return clone(a);
        }
    }

    public static byte[] concatenate(byte[] a, byte[] b, byte[] c) {
        if (a != null && b != null && c != null) {
            byte[] rv = new byte[((a.length + b.length) + c.length)];
            System.arraycopy(a, 0, rv, 0, a.length);
            System.arraycopy(b, 0, rv, a.length, b.length);
            System.arraycopy(c, 0, rv, a.length + b.length, c.length);
            return rv;
        } else if (a == null) {
            return concatenate(b, c);
        } else {
            if (b == null) {
                return concatenate(a, c);
            }
            return concatenate(a, b);
        }
    }

    public static byte[] concatenate(byte[] a, byte[] b, byte[] c, byte[] d) {
        if (a != null && b != null && c != null && d != null) {
            byte[] rv = new byte[(((a.length + b.length) + c.length) + d.length)];
            System.arraycopy(a, 0, rv, 0, a.length);
            System.arraycopy(b, 0, rv, a.length, b.length);
            System.arraycopy(c, 0, rv, a.length + b.length, c.length);
            System.arraycopy(d, 0, rv, (a.length + b.length) + c.length, d.length);
            return rv;
        } else if (d == null) {
            return concatenate(a, b, c);
        } else {
            if (c == null) {
                return concatenate(a, b, d);
            }
            if (b == null) {
                return concatenate(a, c, d);
            }
            return concatenate(b, c, d);
        }
    }

    public static int[] concatenate(int[] a, int[] b) {
        if (a == null) {
            return clone(b);
        }
        if (b == null) {
            return clone(a);
        }
        int[] c = new int[(a.length + b.length)];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] prepend(byte[] a, byte b) {
        if (a == null) {
            return new byte[]{b};
        }
        int length = a.length;
        byte[] result = new byte[(length + 1)];
        System.arraycopy(a, 0, result, 1, length);
        result[0] = b;
        return result;
    }

    public static short[] prepend(short[] a, short b) {
        if (a == null) {
            return new short[]{b};
        }
        int length = a.length;
        short[] result = new short[(length + 1)];
        System.arraycopy(a, 0, result, 1, length);
        result[0] = b;
        return result;
    }

    public static int[] prepend(int[] a, int b) {
        if (a == null) {
            return new int[]{b};
        }
        int length = a.length;
        int[] result = new int[(length + 1)];
        System.arraycopy(a, 0, result, 1, length);
        result[0] = b;
        return result;
    }

    public static byte[] reverse(byte[] a) {
        if (a == null) {
            return null;
        }
        int p1 = 0;
        int p2 = a.length;
        byte[] result = new byte[p2];
        while (true) {
            int p12 = p1;
            p2--;
            if (p2 < 0) {
                return result;
            }
            p1 = p12 + 1;
            result[p2] = a[p12];
        }
    }

    public static int[] reverse(int[] a) {
        if (a == null) {
            return null;
        }
        int p1 = 0;
        int p2 = a.length;
        int[] result = new int[p2];
        while (true) {
            int p12 = p1;
            p2--;
            if (p2 < 0) {
                return result;
            }
            p1 = p12 + 1;
            result[p2] = a[p12];
        }
    }
}

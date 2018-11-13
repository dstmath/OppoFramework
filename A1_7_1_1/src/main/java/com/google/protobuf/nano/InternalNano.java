package com.google.protobuf.nano;

import java.io.UnsupportedEncodingException;
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
public final class InternalNano {
    public static final Object LAZY_INIT_LOCK = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.google.protobuf.nano.InternalNano.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.google.protobuf.nano.InternalNano.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.nano.InternalNano.<clinit>():void");
    }

    private InternalNano() {
    }

    public static String stringDefaultValue(String bytes) {
        try {
            return new String(bytes.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Java VM does not support a standard character set.", e);
        }
    }

    public static byte[] bytesDefaultValue(String bytes) {
        try {
            return bytes.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Java VM does not support a standard character set.", e);
        }
    }

    public static byte[] copyFromUtf8(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported?");
        }
    }

    public static boolean equals(int[] field1, int[] field2) {
        boolean z = true;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (!(field2 == null || field2.length == 0)) {
            z = false;
        }
        return z;
    }

    public static boolean equals(long[] field1, long[] field2) {
        boolean z = true;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (!(field2 == null || field2.length == 0)) {
            z = false;
        }
        return z;
    }

    public static boolean equals(float[] field1, float[] field2) {
        boolean z = true;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (!(field2 == null || field2.length == 0)) {
            z = false;
        }
        return z;
    }

    public static boolean equals(double[] field1, double[] field2) {
        boolean z = true;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (!(field2 == null || field2.length == 0)) {
            z = false;
        }
        return z;
    }

    public static boolean equals(boolean[] field1, boolean[] field2) {
        boolean z = true;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (!(field2 == null || field2.length == 0)) {
            z = false;
        }
        return z;
    }

    public static boolean equals(byte[][] field1, byte[][] field2) {
        int index1 = 0;
        int length1 = field1 == null ? 0 : field1.length;
        int index2 = 0;
        int length2 = field2 == null ? 0 : field2.length;
        while (true) {
            if (index1 >= length1 || field1[index1] != null) {
                while (index2 < length2 && field2[index2] == null) {
                    index2++;
                }
                boolean atEndOf1 = index1 >= length1;
                boolean atEndOf2 = index2 >= length2;
                if (atEndOf1 && atEndOf2) {
                    return true;
                }
                if (atEndOf1 != atEndOf2 || !Arrays.equals(field1[index1], field2[index2])) {
                    return false;
                }
                index1++;
                index2++;
            } else {
                index1++;
            }
        }
    }

    public static boolean equals(Object[] field1, Object[] field2) {
        int index1 = 0;
        int length1 = field1 == null ? 0 : field1.length;
        int index2 = 0;
        int length2 = field2 == null ? 0 : field2.length;
        while (true) {
            if (index1 >= length1 || field1[index1] != null) {
                while (index2 < length2 && field2[index2] == null) {
                    index2++;
                }
                boolean atEndOf1 = index1 >= length1;
                boolean atEndOf2 = index2 >= length2;
                if (atEndOf1 && atEndOf2) {
                    return true;
                }
                if (atEndOf1 != atEndOf2 || !field1[index1].equals(field2[index2])) {
                    return false;
                }
                index1++;
                index2++;
            } else {
                index1++;
            }
        }
    }

    public static int hashCode(int[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(long[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(float[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(double[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(boolean[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(byte[][] field) {
        int result = 0;
        int i = 0;
        int size = field == null ? 0 : field.length;
        while (i < size) {
            byte[] element = field[i];
            if (element != null) {
                result = (result * 31) + Arrays.hashCode(element);
            }
            i++;
        }
        return result;
    }

    public static int hashCode(Object[] field) {
        int result = 0;
        int i = 0;
        int size = field == null ? 0 : field.length;
        while (i < size) {
            Object element = field[i];
            if (element != null) {
                result = (result * 31) + element.hashCode();
            }
            i++;
        }
        return result;
    }

    public static void cloneUnknownFieldData(ExtendableMessageNano original, ExtendableMessageNano cloned) {
        if (original.unknownFieldData != null) {
            cloned.unknownFieldData = original.unknownFieldData.clone();
        }
    }
}

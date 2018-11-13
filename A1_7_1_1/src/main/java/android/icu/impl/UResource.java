package android.icu.impl;

import java.nio.ByteBuffer;

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
public final class UResource {

    public static abstract class Value {
        public abstract String getAliasString();

        public abstract ByteBuffer getBinary();

        public abstract int getInt();

        public abstract int[] getIntVector();

        public abstract String getString();

        public abstract int getType();

        public abstract int getUInt();

        protected Value() {
        }

        public String toString() {
            switch (getType()) {
                case 0:
                    return getString();
                case 1:
                    return "(binary blob)";
                case 2:
                    return "(table)";
                case 7:
                    return Integer.toString(getInt());
                case 8:
                    return "(array)";
                case 14:
                    int[] iv = getIntVector();
                    StringBuilder sb = new StringBuilder("[");
                    sb.append(iv.length).append("]{");
                    if (iv.length != 0) {
                        sb.append(iv[0]);
                        for (int i = 1; i < iv.length; i++) {
                            sb.append(", ").append(iv[i]);
                        }
                    }
                    return sb.append('}').toString();
                default:
                    return "???";
            }
        }
    }

    public static class TableSink {
        public void put(Key key, Value value) {
        }

        public void putNoFallback(Key key) {
        }

        public ArraySink getOrCreateArraySink(Key key, int size) {
            return null;
        }

        public TableSink getOrCreateTableSink(Key key, int initialSize) {
            return null;
        }

        public void leave() {
        }
    }

    public static class ArraySink {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.ArraySink.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public ArraySink() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.ArraySink.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UResource.ArraySink.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.ArraySink.leave():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void leave() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.ArraySink.leave():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UResource.ArraySink.leave():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.ArraySink.put(int, android.icu.impl.UResource$Value):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void put(int r1, android.icu.impl.UResource.Value r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.ArraySink.put(int, android.icu.impl.UResource$Value):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UResource.ArraySink.put(int, android.icu.impl.UResource$Value):void");
        }

        public ArraySink getOrCreateArraySink(int index, int size) {
            return null;
        }

        public TableSink getOrCreateTableSink(int index, int initialSize) {
            return null;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static final class Key implements CharSequence, Cloneable, Comparable<Key> {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f22-assertionsDisabled = false;
        private byte[] bytes;
        private int length;
        private int offset;
        private String s;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UResource.Key.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.UResource.Key.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UResource.Key.<clinit>():void");
        }

        private Key(byte[] keyBytes, int keyOffset, int keyLength) {
            this.bytes = keyBytes;
            this.offset = keyOffset;
            this.length = keyLength;
        }

        public void setBytes(byte[] keyBytes, int keyOffset) {
            this.bytes = keyBytes;
            this.offset = keyOffset;
            this.length = 0;
            while (keyBytes[this.length + keyOffset] != (byte) 0) {
                this.length++;
            }
            this.s = null;
        }

        public void setToEmpty() {
            this.bytes = null;
            this.length = 0;
            this.offset = 0;
            this.s = null;
        }

        public Key clone() {
            try {
                return (Key) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        public char charAt(int i) {
            Object obj = null;
            if (!f22-assertionsDisabled) {
                if (i >= 0 && i < this.length) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return (char) this.bytes[this.offset + i];
        }

        public int length() {
            return this.length;
        }

        public Key subSequence(int start, int end) {
            Object obj = 1;
            if (!f22-assertionsDisabled) {
                Object obj2 = (start < 0 || start >= this.length) ? null : 1;
                if (obj2 == null) {
                    throw new AssertionError();
                }
            }
            if (!f22-assertionsDisabled) {
                if (start > end || end > this.length) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return new Key(this.bytes, this.offset + start, end - start);
        }

        public String toString() {
            if (this.s == null) {
                this.s = internalSubString(0, this.length);
            }
            return this.s;
        }

        private String internalSubString(int start, int end) {
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                sb.append((char) this.bytes[this.offset + i]);
            }
            return sb.toString();
        }

        public String substring(int start) {
            Object obj = null;
            if (!f22-assertionsDisabled) {
                if (start >= 0 && start < this.length) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return internalSubString(start, this.length);
        }

        public String substring(int start, int end) {
            Object obj = 1;
            if (!f22-assertionsDisabled) {
                Object obj2 = (start < 0 || start >= this.length) ? null : 1;
                if (obj2 == null) {
                    throw new AssertionError();
                }
            }
            if (!f22-assertionsDisabled) {
                if (start > end || end > this.length) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return internalSubString(start, end);
        }

        private boolean regionMatches(byte[] otherBytes, int otherOffset, int n) {
            for (int i = 0; i < n; i++) {
                if (this.bytes[this.offset + i] != otherBytes[otherOffset + i]) {
                    return false;
                }
            }
            return true;
        }

        private boolean regionMatches(int start, CharSequence cs, int n) {
            for (int i = 0; i < n; i++) {
                if (this.bytes[(this.offset + start) + i] != cs.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (other == null) {
                return false;
            }
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key)) {
                return false;
            }
            Key otherKey = (Key) other;
            if (this.length == otherKey.length) {
                z = regionMatches(otherKey.bytes, otherKey.offset, this.length);
            }
            return z;
        }

        public boolean contentEquals(CharSequence cs) {
            boolean z = false;
            if (cs == null) {
                return false;
            }
            if (this == cs) {
                z = true;
            } else if (cs.length() == this.length) {
                z = regionMatches(0, cs, this.length);
            }
            return z;
        }

        public boolean startsWith(CharSequence cs) {
            int csLength = cs.length();
            if (csLength <= this.length) {
                return regionMatches(0, cs, csLength);
            }
            return false;
        }

        public boolean endsWith(CharSequence cs) {
            int csLength = cs.length();
            return csLength <= this.length ? regionMatches(this.length - csLength, cs, csLength) : false;
        }

        public boolean regionMatches(int start, CharSequence cs) {
            int csLength = cs.length();
            return csLength == this.length - start ? regionMatches(start, cs, csLength) : false;
        }

        public int hashCode() {
            if (this.length == 0) {
                return 0;
            }
            int h = this.bytes[this.offset];
            for (int i = 1; i < this.length; i++) {
                h = (h * 37) + this.bytes[this.offset];
            }
            return h;
        }

        public int compareTo(Key other) {
            return compareTo((CharSequence) other);
        }

        public int compareTo(CharSequence cs) {
            int csLength = cs.length();
            int minLength = this.length <= csLength ? this.length : csLength;
            for (int i = 0; i < minLength; i++) {
                int diff = charAt(i) - cs.charAt(i);
                if (diff != 0) {
                    return diff;
                }
            }
            return this.length - csLength;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.<init>():void, dex: 
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
    public UResource() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.UResource.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UResource.<init>():void");
    }
}

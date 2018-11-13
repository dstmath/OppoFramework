package android.icu.impl.coll;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class CollationKeys {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f76-assertionsDisabled = false;
    private static final int CASE_LOWER_FIRST_COMMON_HIGH = 13;
    private static final int CASE_LOWER_FIRST_COMMON_LOW = 1;
    private static final int CASE_LOWER_FIRST_COMMON_MAX_COUNT = 7;
    private static final int CASE_LOWER_FIRST_COMMON_MIDDLE = 7;
    private static final int CASE_UPPER_FIRST_COMMON_HIGH = 15;
    private static final int CASE_UPPER_FIRST_COMMON_LOW = 3;
    private static final int CASE_UPPER_FIRST_COMMON_MAX_COUNT = 13;
    private static final int QUAT_COMMON_HIGH = 252;
    private static final int QUAT_COMMON_LOW = 28;
    private static final int QUAT_COMMON_MAX_COUNT = 113;
    private static final int QUAT_COMMON_MIDDLE = 140;
    private static final int QUAT_SHIFTED_LIMIT_BYTE = 27;
    static final int SEC_COMMON_HIGH = 69;
    private static final int SEC_COMMON_LOW = 5;
    private static final int SEC_COMMON_MAX_COUNT = 33;
    private static final int SEC_COMMON_MIDDLE = 37;
    public static final LevelCallback SIMPLE_LEVEL_FALLBACK = null;
    private static final int TER_LOWER_FIRST_COMMON_HIGH = 69;
    private static final int TER_LOWER_FIRST_COMMON_LOW = 5;
    private static final int TER_LOWER_FIRST_COMMON_MAX_COUNT = 33;
    private static final int TER_LOWER_FIRST_COMMON_MIDDLE = 37;
    private static final int TER_ONLY_COMMON_HIGH = 197;
    private static final int TER_ONLY_COMMON_LOW = 5;
    private static final int TER_ONLY_COMMON_MAX_COUNT = 97;
    private static final int TER_ONLY_COMMON_MIDDLE = 101;
    private static final int TER_UPPER_FIRST_COMMON_HIGH = 197;
    private static final int TER_UPPER_FIRST_COMMON_LOW = 133;
    private static final int TER_UPPER_FIRST_COMMON_MAX_COUNT = 33;
    private static final int TER_UPPER_FIRST_COMMON_MIDDLE = 165;
    private static final int[] levelMasks = null;

    public static class LevelCallback {
        boolean needToWrite(int level) {
            return true;
        }
    }

    public static abstract class SortKeyByteSink {
        private int appended_ = 0;
        protected byte[] buffer_;

        protected abstract void AppendBeyondCapacity(byte[] bArr, int i, int i2, int i3);

        protected abstract boolean Resize(int i, int i2);

        public SortKeyByteSink(byte[] dest) {
            this.buffer_ = dest;
        }

        public void setBufferAndAppended(byte[] dest, int app) {
            this.buffer_ = dest;
            this.appended_ = app;
        }

        public void Append(byte[] bytes, int n) {
            if (n > 0 && bytes != null) {
                int length = this.appended_;
                this.appended_ += n;
                if (n <= this.buffer_.length - length) {
                    System.arraycopy(bytes, 0, this.buffer_, length, n);
                } else {
                    AppendBeyondCapacity(bytes, 0, n, length);
                }
            }
        }

        public void Append(int b) {
            if (this.appended_ < this.buffer_.length || Resize(1, this.appended_)) {
                this.buffer_[this.appended_] = (byte) b;
            }
            this.appended_++;
        }

        public int NumberOfBytesAppended() {
            return this.appended_;
        }

        public int GetRemainingCapacity() {
            return this.buffer_.length - this.appended_;
        }

        public boolean Overflowed() {
            return this.appended_ > this.buffer_.length;
        }
    }

    private static final class SortKeyLevel {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f77-assertionsDisabled = false;
        private static final int INITIAL_CAPACITY = 40;
        byte[] buffer;
        int len;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        SortKeyLevel() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.ensureCapacity(int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private boolean ensureCapacity(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.ensureCapacity(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.ensureCapacity(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendByte(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void appendByte(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendByte(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendByte(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendReverseWeight16(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void appendReverseWeight16(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendReverseWeight16(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendReverseWeight16(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendTo(android.icu.impl.coll.CollationKeys$SortKeyByteSink):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void appendTo(android.icu.impl.coll.CollationKeys.SortKeyByteSink r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendTo(android.icu.impl.coll.CollationKeys$SortKeyByteSink):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendTo(android.icu.impl.coll.CollationKeys$SortKeyByteSink):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendWeight16(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void appendWeight16(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendWeight16(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendWeight16(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendWeight32(long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        void appendWeight32(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendWeight32(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.appendWeight32(long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.data():byte[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        byte[] data() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.data():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.data():byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.getAt(int):byte, dex:  in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.getAt(int):byte, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.getAt(int):byte, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
            	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        byte getAt(int r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.getAt(int):byte, dex:  in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.getAt(int):byte, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.getAt(int):byte");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.isEmpty():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        boolean isEmpty() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.isEmpty():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.isEmpty():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.length():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        int length() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.coll.CollationKeys.SortKeyLevel.length():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.SortKeyLevel.length():int");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationKeys.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationKeys.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationKeys.<clinit>():void");
    }

    private static SortKeyLevel getSortKeyLevel(int levels, int level) {
        return (levels & level) != 0 ? new SortKeyLevel() : null;
    }

    private CollationKeys() {
    }

    public static void writeSortKeyUpToQuaternary(CollationIterator iter, boolean[] compressibleBytes, CollationSettings settings, SortKeyByteSink sink, int minLevel, LevelCallback callback, boolean preflight) {
        int options = settings.options;
        int levels = levelMasks[CollationSettings.getStrength(options)];
        if ((options & 1024) != 0) {
            levels |= 8;
        }
        levels &= ~((1 << minLevel) - 1);
        if (levels != 0) {
            long variableTop;
            if ((options & 12) == 0) {
                variableTop = 0;
            } else {
                variableTop = settings.variableTop + 1;
            }
            int tertiaryMask = CollationSettings.getTertiaryMask(options);
            byte[] p234 = new byte[3];
            SortKeyLevel cases = getSortKeyLevel(levels, 8);
            SortKeyLevel secondaries = getSortKeyLevel(levels, 4);
            SortKeyLevel tertiaries = getSortKeyLevel(levels, 16);
            SortKeyLevel quaternaries = getSortKeyLevel(levels, 32);
            long prevReorderedPrimary = 0;
            int commonCases = 0;
            int commonSecondaries = 0;
            int commonTertiaries = 0;
            int commonQuaternaries = 0;
            int prevSecondary = 0;
            int secSegmentStart = 0;
            while (true) {
                iter.clearCEsIfNoneRemaining();
                long ce = iter.nextCE();
                long p = ce >>> 32;
                if (p < variableTop && p > Collation.MERGE_SEPARATOR_PRIMARY) {
                    if (commonQuaternaries != 0) {
                        commonQuaternaries--;
                        while (commonQuaternaries >= 113) {
                            quaternaries.appendByte(140);
                            commonQuaternaries -= 113;
                        }
                        quaternaries.appendByte(commonQuaternaries + 28);
                        commonQuaternaries = 0;
                    }
                    do {
                        if ((levels & 32) != 0) {
                            if (settings.hasReordering()) {
                                p = settings.reorder(p);
                            }
                            if ((((int) p) >>> 24) >= 27) {
                                quaternaries.appendByte(27);
                            }
                            quaternaries.appendWeight32(p);
                        }
                        do {
                            ce = iter.nextCE();
                            p = ce >>> 32;
                        } while (p == 0);
                        if (p >= variableTop) {
                            break;
                        }
                    } while (p > Collation.MERGE_SEPARATOR_PRIMARY);
                }
                if (p > 1 && (levels & 2) != 0) {
                    boolean isCompressible = compressibleBytes[((int) p) >>> 24];
                    if (settings.hasReordering()) {
                        p = settings.reorder(p);
                    }
                    int p1 = ((int) p) >>> 24;
                    if (!(isCompressible && p1 == (((int) prevReorderedPrimary) >>> 24))) {
                        if (prevReorderedPrimary != 0) {
                            if (p >= prevReorderedPrimary) {
                                sink.Append(255);
                            } else if (p1 > 2) {
                                sink.Append(3);
                            }
                        }
                        sink.Append(p1);
                        if (isCompressible) {
                            prevReorderedPrimary = p;
                        } else {
                            prevReorderedPrimary = 0;
                        }
                    }
                    byte p2 = (byte) ((int) (p >>> 16));
                    if (p2 != (byte) 0) {
                        p234[0] = p2;
                        p234[1] = (byte) ((int) (p >>> 8));
                        p234[2] = (byte) ((int) p);
                        int i = p234[1] == (byte) 0 ? 1 : p234[2] == (byte) 0 ? 2 : 3;
                        sink.Append(p234, i);
                    }
                    if (!preflight && sink.Overflowed()) {
                        return;
                    }
                }
                int lower32 = (int) ce;
                if (lower32 != 0) {
                    int b;
                    Object obj;
                    if ((levels & 4) != 0) {
                        int s = lower32 >>> 16;
                        if (s != 0) {
                            if (s == 1280 && ((options & 2048) == 0 || p != Collation.MERGE_SEPARATOR_PRIMARY)) {
                                commonSecondaries++;
                            } else if ((options & 2048) == 0) {
                                if (commonSecondaries != 0) {
                                    commonSecondaries--;
                                    while (commonSecondaries >= 33) {
                                        secondaries.appendByte(37);
                                        commonSecondaries -= 33;
                                    }
                                    if (s < 1280) {
                                        b = commonSecondaries + 5;
                                    } else {
                                        b = 69 - commonSecondaries;
                                    }
                                    secondaries.appendByte(b);
                                    commonSecondaries = 0;
                                }
                                secondaries.appendWeight16(s);
                            } else {
                                if (commonSecondaries != 0) {
                                    commonSecondaries--;
                                    int remainder = commonSecondaries % 33;
                                    if (prevSecondary < 1280) {
                                        b = remainder + 5;
                                    } else {
                                        b = 69 - remainder;
                                    }
                                    secondaries.appendByte(b);
                                    commonSecondaries -= remainder;
                                    while (commonSecondaries > 0) {
                                        secondaries.appendByte(37);
                                        commonSecondaries -= 33;
                                    }
                                }
                                if (0 >= p || p > Collation.MERGE_SEPARATOR_PRIMARY) {
                                    secondaries.appendReverseWeight16(s);
                                    prevSecondary = s;
                                } else {
                                    byte[] secs = secondaries.data();
                                    int length = secondaries.length() - 1;
                                    while (true) {
                                        int last = length;
                                        int i2 = secSegmentStart;
                                        if (i2 >= last) {
                                            break;
                                        }
                                        byte b2 = secs[i2];
                                        secSegmentStart = i2 + 1;
                                        secs[i2] = secs[last];
                                        length = last - 1;
                                        secs[last] = b2;
                                    }
                                    secondaries.appendByte(p == 1 ? 1 : 2);
                                    prevSecondary = 0;
                                    secSegmentStart = secondaries.length();
                                }
                            }
                        }
                    }
                    if ((levels & 8) != 0 && (CollationSettings.getStrength(options) != 0 ? (lower32 >>> 16) == 0 : p == 0)) {
                        int c = (lower32 >>> 8) & 255;
                        if (!f76-assertionsDisabled) {
                            if (((c & 192) != 192 ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        if ((c & 192) != 0 || c <= 1) {
                            if ((options & 256) == 0) {
                                if (commonCases != 0 && (c > 1 || !cases.isEmpty())) {
                                    commonCases--;
                                    while (commonCases >= 7) {
                                        cases.appendByte(112);
                                        commonCases -= 7;
                                    }
                                    if (c <= 1) {
                                        b = commonCases + 1;
                                    } else {
                                        b = 13 - commonCases;
                                    }
                                    cases.appendByte(b << 4);
                                    commonCases = 0;
                                }
                                if (c > 1) {
                                    c = ((c >>> 6) + 13) << 4;
                                }
                            } else {
                                if (commonCases != 0) {
                                    commonCases--;
                                    while (commonCases >= 13) {
                                        cases.appendByte(48);
                                        commonCases -= 13;
                                    }
                                    cases.appendByte((commonCases + 3) << 4);
                                    commonCases = 0;
                                }
                                if (c > 1) {
                                    c = (3 - (c >>> 6)) << 4;
                                }
                            }
                            cases.appendByte(c);
                        } else {
                            commonCases++;
                        }
                    }
                    if ((levels & 16) != 0) {
                        int t = lower32 & tertiaryMask;
                        if (!f76-assertionsDisabled) {
                            if ((Collation.CASE_MASK & lower32) != 49152) {
                                obj = 1;
                            } else {
                                obj = null;
                            }
                            if (obj == null) {
                                throw new AssertionError();
                            }
                        }
                        if (t == 1280) {
                            commonTertiaries++;
                        } else if ((32768 & tertiaryMask) == 0) {
                            if (commonTertiaries != 0) {
                                commonTertiaries--;
                                while (commonTertiaries >= 97) {
                                    tertiaries.appendByte(101);
                                    commonTertiaries -= 97;
                                }
                                if (t < 1280) {
                                    b = commonTertiaries + 5;
                                } else {
                                    b = 197 - commonTertiaries;
                                }
                                tertiaries.appendByte(b);
                                commonTertiaries = 0;
                            }
                            if (t > 1280) {
                                t += Collation.CASE_MASK;
                            }
                            tertiaries.appendWeight16(t);
                        } else if ((options & 256) == 0) {
                            if (commonTertiaries != 0) {
                                commonTertiaries--;
                                while (commonTertiaries >= 33) {
                                    tertiaries.appendByte(37);
                                    commonTertiaries -= 33;
                                }
                                if (t < 1280) {
                                    b = commonTertiaries + 5;
                                } else {
                                    b = 69 - commonTertiaries;
                                }
                                tertiaries.appendByte(b);
                                commonTertiaries = 0;
                            }
                            if (t > 1280) {
                                t += 16384;
                            }
                            tertiaries.appendWeight16(t);
                        } else {
                            if (t > 256) {
                                if ((lower32 >>> 16) != 0) {
                                    t ^= Collation.CASE_MASK;
                                    if (t < 50432) {
                                        t -= 16384;
                                    }
                                } else {
                                    if (!f76-assertionsDisabled) {
                                        obj = (34304 > t || t > 49151) ? null : 1;
                                        if (obj == null) {
                                            throw new AssertionError();
                                        }
                                    }
                                    t += 16384;
                                }
                            }
                            if (commonTertiaries != 0) {
                                commonTertiaries--;
                                while (commonTertiaries >= 33) {
                                    tertiaries.appendByte(165);
                                    commonTertiaries -= 33;
                                }
                                if (t < 34048) {
                                    b = commonTertiaries + 133;
                                } else {
                                    b = 197 - commonTertiaries;
                                }
                                tertiaries.appendByte(b);
                                commonTertiaries = 0;
                            }
                            tertiaries.appendWeight16(t);
                        }
                    }
                    if ((levels & 32) != 0) {
                        int q = lower32 & 65535;
                        if ((q & 192) == 0 && q > 256) {
                            commonQuaternaries++;
                        } else if (q == 256 && (options & 12) == 0 && quaternaries.isEmpty()) {
                            quaternaries.appendByte(1);
                        } else {
                            if (q == 256) {
                                q = 1;
                            } else {
                                q = ((q >>> 6) & 3) + 252;
                            }
                            if (commonQuaternaries != 0) {
                                commonQuaternaries--;
                                while (commonQuaternaries >= 113) {
                                    quaternaries.appendByte(140);
                                    commonQuaternaries -= 113;
                                }
                                if (q < 28) {
                                    b = commonQuaternaries + 28;
                                } else {
                                    b = 252 - commonQuaternaries;
                                }
                                quaternaries.appendByte(b);
                                commonQuaternaries = 0;
                            }
                            quaternaries.appendByte(q);
                        }
                    }
                    if ((lower32 >>> 24) == 1) {
                        if ((levels & 4) != 0) {
                            if (callback.needToWrite(2)) {
                                sink.Append(1);
                                secondaries.appendTo(sink);
                            } else {
                                return;
                            }
                        }
                        if ((levels & 8) != 0) {
                            if (callback.needToWrite(3)) {
                                sink.Append(1);
                                int length2 = cases.length() - 1;
                                int b3 = 0;
                                for (int i3 = 0; i3 < length2; i3++) {
                                    byte c2 = cases.getAt(i3);
                                    if (!f76-assertionsDisabled) {
                                        obj = ((c2 & 15) != 0 || c2 == (byte) 0) ? null : 1;
                                        if (obj == null) {
                                            throw new AssertionError();
                                        }
                                    }
                                    if (b3 == 0) {
                                        b3 = c2;
                                    } else {
                                        sink.Append(((c2 >> 4) & 15) | b3);
                                        b3 = 0;
                                    }
                                }
                                if (b3 != 0) {
                                    sink.Append(b3);
                                }
                            } else {
                                return;
                            }
                        }
                        if ((levels & 16) != 0) {
                            if (callback.needToWrite(4)) {
                                sink.Append(1);
                                tertiaries.appendTo(sink);
                            } else {
                                return;
                            }
                        }
                        if ((levels & 32) != 0 && callback.needToWrite(5)) {
                            sink.Append(1);
                            quaternaries.appendTo(sink);
                            return;
                        }
                        return;
                    }
                }
            }
        }
    }
}

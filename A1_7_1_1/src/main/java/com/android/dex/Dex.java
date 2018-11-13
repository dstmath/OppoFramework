package com.android.dex;

import com.android.dex.ClassData.Field;
import com.android.dex.ClassData.Method;
import com.android.dex.Code.CatchHandler;
import com.android.dex.Code.Try;
import com.android.dex.util.ByteInput;
import com.android.dex.util.ByteOutput;
import com.android.dex.util.FileUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.zip.Adler32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class Dex {
    private static final int CHECKSUM_OFFSET = 8;
    private static final int CHECKSUM_SIZE = 4;
    static final short[] EMPTY_SHORT_ARRAY = null;
    private static final int SIGNATURE_OFFSET = 12;
    private static final int SIGNATURE_SIZE = 20;
    private ByteBuffer data;
    private final FieldIdTable fieldIds;
    private final MethodIdTable methodIds;
    private int nextSectionStart;
    private final ProtoIdTable protoIds;
    private final StringTable strings;
    private final TableOfContents tableOfContents;
    private final TypeIndexToDescriptorIndexTable typeIds;
    private final TypeIndexToDescriptorTable typeNames;

    private final class ClassDefIterable implements Iterable<ClassDef> {
        /* synthetic */ ClassDefIterable(Dex this$0, ClassDefIterable classDefIterable) {
            this();
        }

        private ClassDefIterable() {
        }

        public Iterator<ClassDef> iterator() {
            if (Dex.this.tableOfContents.classDefs.exists()) {
                return new ClassDefIterator(Dex.this, null);
            }
            return Collections.emptySet().iterator();
        }
    }

    private final class ClassDefIterator implements Iterator<ClassDef> {
        private int count;
        private final Section in;
        final /* synthetic */ Dex this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.dex.Dex.ClassDefIterator.<init>(com.android.dex.Dex):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private ClassDefIterator(com.android.dex.Dex r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.dex.Dex.ClassDefIterator.<init>(com.android.dex.Dex):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.Dex.ClassDefIterator.<init>(com.android.dex.Dex):void");
        }

        /* synthetic */ ClassDefIterator(Dex this$0, ClassDefIterator classDefIterator) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.dex.Dex.ClassDefIterator.hasNext():boolean, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.dex.Dex.ClassDefIterator.hasNext():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.Dex.ClassDefIterator.hasNext():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.dex.Dex.ClassDefIterator.next():com.android.dex.ClassDef, dex: 
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
        public com.android.dex.ClassDef next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.dex.Dex.ClassDefIterator.next():com.android.dex.ClassDef, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.Dex.ClassDefIterator.next():com.android.dex.ClassDef");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.dex.Dex.ClassDefIterator.next():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.dex.Dex.ClassDefIterator.next():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.dex.Dex.ClassDefIterator.next():java.lang.Object");
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class FieldIdTable extends AbstractList<FieldId> implements RandomAccess {
        final /* synthetic */ Dex this$0;

        /* synthetic */ FieldIdTable(Dex this$0, FieldIdTable fieldIdTable) {
            this(this$0);
        }

        private FieldIdTable(Dex this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object get(int index) {
            return get(index);
        }

        public FieldId get(int index) {
            Dex.checkBounds(index, this.this$0.tableOfContents.fieldIds.size);
            return this.this$0.open(this.this$0.tableOfContents.fieldIds.off + (index * 8)).readFieldId();
        }

        public int size() {
            return this.this$0.tableOfContents.fieldIds.size;
        }
    }

    private final class MethodIdTable extends AbstractList<MethodId> implements RandomAccess {
        final /* synthetic */ Dex this$0;

        /* synthetic */ MethodIdTable(Dex this$0, MethodIdTable methodIdTable) {
            this(this$0);
        }

        private MethodIdTable(Dex this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object get(int index) {
            return get(index);
        }

        public MethodId get(int index) {
            Dex.checkBounds(index, this.this$0.tableOfContents.methodIds.size);
            return this.this$0.open(this.this$0.tableOfContents.methodIds.off + (index * 8)).readMethodId();
        }

        public int size() {
            return this.this$0.tableOfContents.methodIds.size;
        }
    }

    private final class ProtoIdTable extends AbstractList<ProtoId> implements RandomAccess {
        final /* synthetic */ Dex this$0;

        /* synthetic */ ProtoIdTable(Dex this$0, ProtoIdTable protoIdTable) {
            this(this$0);
        }

        private ProtoIdTable(Dex this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object get(int index) {
            return get(index);
        }

        public ProtoId get(int index) {
            Dex.checkBounds(index, this.this$0.tableOfContents.protoIds.size);
            return this.this$0.open(this.this$0.tableOfContents.protoIds.off + (index * 12)).readProtoId();
        }

        public int size() {
            return this.this$0.tableOfContents.protoIds.size;
        }
    }

    public final class Section implements ByteInput, ByteOutput {
        private final ByteBuffer data;
        private final int initialPosition;
        private final String name;
        final /* synthetic */ Dex this$0;

        /* synthetic */ Section(Dex this$0, String name, ByteBuffer data, Section section) {
            this(this$0, name, data);
        }

        private Section(Dex this$0, String name, ByteBuffer data) {
            this.this$0 = this$0;
            this.name = name;
            this.data = data;
            this.initialPosition = data.position();
        }

        public int getPosition() {
            return this.data.position();
        }

        public int readInt() {
            return this.data.getInt();
        }

        public short readShort() {
            return this.data.getShort();
        }

        public int readUnsignedShort() {
            return readShort() & 65535;
        }

        public byte readByte() {
            return this.data.get();
        }

        public byte[] readByteArray(int length) {
            byte[] result = new byte[length];
            this.data.get(result);
            return result;
        }

        public short[] readShortArray(int length) {
            if (length == 0) {
                return Dex.EMPTY_SHORT_ARRAY;
            }
            short[] result = new short[length];
            for (int i = 0; i < length; i++) {
                result[i] = readShort();
            }
            return result;
        }

        public int readUleb128() {
            return Leb128.readUnsignedLeb128(this);
        }

        public int readUleb128p1() {
            return Leb128.readUnsignedLeb128(this) - 1;
        }

        public int readSleb128() {
            return Leb128.readSignedLeb128(this);
        }

        public void writeUleb128p1(int i) {
            writeUleb128(i + 1);
        }

        public TypeList readTypeList() {
            short[] types = readShortArray(readInt());
            alignToFourBytes();
            return new TypeList(this.this$0, types);
        }

        public String readString() {
            int offset = readInt();
            int savedPosition = this.data.position();
            int savedLimit = this.data.limit();
            this.data.position(offset);
            this.data.limit(this.data.capacity());
            try {
                int expectedLength = readUleb128();
                String result = Mutf8.decode(this, new char[expectedLength]);
                if (result.length() != expectedLength) {
                    throw new DexException("Declared length " + expectedLength + " doesn't match decoded length of " + result.length());
                }
                this.data.position(savedPosition);
                this.data.limit(savedLimit);
                return result;
            } catch (Throwable e) {
                throw new DexException(e);
            } catch (Throwable th) {
                this.data.position(savedPosition);
                this.data.limit(savedLimit);
            }
        }

        public FieldId readFieldId() {
            return new FieldId(this.this$0, readUnsignedShort(), readUnsignedShort(), readInt());
        }

        public MethodId readMethodId() {
            return new MethodId(this.this$0, readUnsignedShort(), readUnsignedShort(), readInt());
        }

        public ProtoId readProtoId() {
            return new ProtoId(this.this$0, readInt(), readInt(), readInt());
        }

        public ClassDef readClassDef() {
            return new ClassDef(this.this$0, getPosition(), readInt(), readInt(), readInt(), readInt(), readInt(), readInt(), readInt(), readInt());
        }

        private Code readCode() {
            CatchHandler[] catchHandlers;
            Try[] tries;
            int registersSize = readUnsignedShort();
            int insSize = readUnsignedShort();
            int outsSize = readUnsignedShort();
            int triesSize = readUnsignedShort();
            int debugInfoOffset = readInt();
            short[] instructions = readShortArray(readInt());
            if (triesSize > 0) {
                if (instructions.length % 2 == 1) {
                    readShort();
                }
                Section triesSection = this.this$0.open(this.data.position());
                skip(triesSize * 8);
                catchHandlers = readCatchHandlers();
                tries = triesSection.readTries(triesSize, catchHandlers);
            } else {
                tries = new Try[0];
                catchHandlers = new CatchHandler[0];
            }
            return new Code(registersSize, insSize, outsSize, debugInfoOffset, instructions, tries, catchHandlers);
        }

        private CatchHandler[] readCatchHandlers() {
            int baseOffset = this.data.position();
            int catchHandlersSize = readUleb128();
            CatchHandler[] result = new CatchHandler[catchHandlersSize];
            for (int i = 0; i < catchHandlersSize; i++) {
                result[i] = readCatchHandler(this.data.position() - baseOffset);
            }
            return result;
        }

        private Try[] readTries(int triesSize, CatchHandler[] catchHandlers) {
            Try[] result = new Try[triesSize];
            for (int i = 0; i < triesSize; i++) {
                result[i] = new Try(readInt(), readUnsignedShort(), findCatchHandlerIndex(catchHandlers, readUnsignedShort()));
            }
            return result;
        }

        private int findCatchHandlerIndex(CatchHandler[] catchHandlers, int offset) {
            for (int i = 0; i < catchHandlers.length; i++) {
                if (catchHandlers[i].getOffset() == offset) {
                    return i;
                }
            }
            throw new IllegalArgumentException();
        }

        private CatchHandler readCatchHandler(int offset) {
            int size = readSleb128();
            int handlersCount = Math.abs(size);
            int[] typeIndexes = new int[handlersCount];
            int[] addresses = new int[handlersCount];
            for (int i = 0; i < handlersCount; i++) {
                typeIndexes[i] = readUleb128();
                addresses[i] = readUleb128();
            }
            return new CatchHandler(typeIndexes, addresses, size <= 0 ? readUleb128() : -1, offset);
        }

        private ClassData readClassData() {
            return new ClassData(readFields(readUleb128()), readFields(readUleb128()), readMethods(readUleb128()), readMethods(readUleb128()));
        }

        private Field[] readFields(int count) {
            Field[] result = new Field[count];
            int fieldIndex = 0;
            for (int i = 0; i < count; i++) {
                fieldIndex += readUleb128();
                result[i] = new Field(fieldIndex, readUleb128());
            }
            return result;
        }

        private Method[] readMethods(int count) {
            Method[] result = new Method[count];
            int methodIndex = 0;
            for (int i = 0; i < count; i++) {
                methodIndex += readUleb128();
                result[i] = new Method(methodIndex, readUleb128(), readUleb128());
            }
            return result;
        }

        private byte[] getBytesFrom(int start) {
            byte[] result = new byte[(this.data.position() - start)];
            this.data.position(start);
            this.data.get(result);
            return result;
        }

        public Annotation readAnnotation() {
            byte visibility = readByte();
            int start = this.data.position();
            new EncodedValueReader((ByteInput) this, 29).skipValue();
            return new Annotation(this.this$0, visibility, new EncodedValue(getBytesFrom(start)));
        }

        public EncodedValue readEncodedArray() {
            int start = this.data.position();
            new EncodedValueReader((ByteInput) this, 28).skipValue();
            return new EncodedValue(getBytesFrom(start));
        }

        public void skip(int count) {
            if (count < 0) {
                throw new IllegalArgumentException();
            }
            this.data.position(this.data.position() + count);
        }

        public void alignToFourBytes() {
            this.data.position((this.data.position() + 3) & -4);
        }

        public void alignToFourBytesWithZeroFill() {
            while ((this.data.position() & 3) != 0) {
                this.data.put((byte) 0);
            }
        }

        public void assertFourByteAligned() {
            if ((this.data.position() & 3) != 0) {
                throw new IllegalStateException("Not four byte aligned!");
            }
        }

        public void write(byte[] bytes) {
            this.data.put(bytes);
        }

        public void writeByte(int b) {
            this.data.put((byte) b);
        }

        public void writeShort(short i) {
            this.data.putShort(i);
        }

        public void writeUnsignedShort(int i) {
            short s = (short) i;
            if (i != (65535 & s)) {
                throw new IllegalArgumentException("Expected an unsigned short: " + i);
            }
            writeShort(s);
        }

        public void write(short[] shorts) {
            for (short s : shorts) {
                writeShort(s);
            }
        }

        public void writeInt(int i) {
            this.data.putInt(i);
        }

        public void writeUleb128(int i) {
            try {
                Leb128.writeUnsignedLeb128(this, i);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new DexException("Section limit " + this.data.limit() + " exceeded by " + this.name);
            }
        }

        public void writeSleb128(int i) {
            try {
                Leb128.writeSignedLeb128(this, i);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new DexException("Section limit " + this.data.limit() + " exceeded by " + this.name);
            }
        }

        public void writeStringData(String value) {
            try {
                writeUleb128(value.length());
                write(Mutf8.encode(value));
                writeByte(0);
            } catch (UTFDataFormatException e) {
                throw new AssertionError();
            }
        }

        public void writeTypeList(TypeList typeList) {
            short[] types = typeList.getTypes();
            writeInt(types.length);
            for (short type : types) {
                writeShort(type);
            }
            alignToFourBytesWithZeroFill();
        }

        public int remaining() {
            return this.data.remaining();
        }

        public int used() {
            return this.data.position() - this.initialPosition;
        }
    }

    private final class StringTable extends AbstractList<String> implements RandomAccess {
        final /* synthetic */ Dex this$0;

        /* synthetic */ StringTable(Dex this$0, StringTable stringTable) {
            this(this$0);
        }

        private StringTable(Dex this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object get(int index) {
            return get(index);
        }

        public String get(int index) {
            Dex.checkBounds(index, this.this$0.tableOfContents.stringIds.size);
            return this.this$0.open(this.this$0.tableOfContents.stringIds.off + (index * 4)).readString();
        }

        public int size() {
            return this.this$0.tableOfContents.stringIds.size;
        }
    }

    private final class TypeIndexToDescriptorIndexTable extends AbstractList<Integer> implements RandomAccess {
        final /* synthetic */ Dex this$0;

        /* synthetic */ TypeIndexToDescriptorIndexTable(Dex this$0, TypeIndexToDescriptorIndexTable typeIndexToDescriptorIndexTable) {
            this(this$0);
        }

        private TypeIndexToDescriptorIndexTable(Dex this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object get(int index) {
            return get(index);
        }

        public Integer get(int index) {
            return Integer.valueOf(this.this$0.descriptorIndexFromTypeIndex(index));
        }

        public int size() {
            return this.this$0.tableOfContents.typeIds.size;
        }
    }

    private final class TypeIndexToDescriptorTable extends AbstractList<String> implements RandomAccess {
        final /* synthetic */ Dex this$0;

        /* synthetic */ TypeIndexToDescriptorTable(Dex this$0, TypeIndexToDescriptorTable typeIndexToDescriptorTable) {
            this(this$0);
        }

        private TypeIndexToDescriptorTable(Dex this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object get(int index) {
            return get(index);
        }

        public String get(int index) {
            return this.this$0.strings.get(this.this$0.descriptorIndexFromTypeIndex(index));
        }

        public int size() {
            return this.this$0.tableOfContents.typeIds.size;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.dex.Dex.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.dex.Dex.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.dex.Dex.<clinit>():void");
    }

    public Dex(byte[] data) throws IOException {
        this(ByteBuffer.wrap(data));
    }

    private Dex(ByteBuffer data) throws IOException {
        this.tableOfContents = new TableOfContents();
        this.nextSectionStart = 0;
        this.strings = new StringTable(this, null);
        this.typeIds = new TypeIndexToDescriptorIndexTable(this, null);
        this.typeNames = new TypeIndexToDescriptorTable(this, null);
        this.protoIds = new ProtoIdTable(this, null);
        this.fieldIds = new FieldIdTable(this, null);
        this.methodIds = new MethodIdTable(this, null);
        this.data = data;
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        this.tableOfContents.readFrom(this);
    }

    public Dex(int byteCount) throws IOException {
        this.tableOfContents = new TableOfContents();
        this.nextSectionStart = 0;
        this.strings = new StringTable(this, null);
        this.typeIds = new TypeIndexToDescriptorIndexTable(this, null);
        this.typeNames = new TypeIndexToDescriptorTable(this, null);
        this.protoIds = new ProtoIdTable(this, null);
        this.fieldIds = new FieldIdTable(this, null);
        this.methodIds = new MethodIdTable(this, null);
        this.data = ByteBuffer.wrap(new byte[byteCount]);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Dex(InputStream in) throws IOException {
        this.tableOfContents = new TableOfContents();
        this.nextSectionStart = 0;
        this.strings = new StringTable(this, null);
        this.typeIds = new TypeIndexToDescriptorIndexTable(this, null);
        this.typeNames = new TypeIndexToDescriptorTable(this, null);
        this.protoIds = new ProtoIdTable(this, null);
        this.fieldIds = new FieldIdTable(this, null);
        this.methodIds = new MethodIdTable(this, null);
        loadFrom(in);
    }

    public Dex(File file) throws IOException {
        this.tableOfContents = new TableOfContents();
        this.nextSectionStart = 0;
        this.strings = new StringTable(this, null);
        this.typeIds = new TypeIndexToDescriptorIndexTable(this, null);
        this.typeNames = new TypeIndexToDescriptorTable(this, null);
        this.protoIds = new ProtoIdTable(this, null);
        this.fieldIds = new FieldIdTable(this, null);
        this.methodIds = new MethodIdTable(this, null);
        if (FileUtils.hasArchiveSuffix(file.getName())) {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry(DexFormat.DEX_IN_JAR_NAME);
            if (entry != null) {
                loadFrom(zipFile.getInputStream(entry));
                zipFile.close();
                return;
            }
            throw new DexException("Expected classes.dex in " + file);
        } else if (file.getName().endsWith(".dex")) {
            loadFrom(new FileInputStream(file));
        } else {
            throw new DexException("unknown output extension: " + file);
        }
    }

    public static Dex create(ByteBuffer data) throws IOException {
        data.order(ByteOrder.LITTLE_ENDIAN);
        if (data.get(0) == (byte) 100 && data.get(1) == (byte) 101 && data.get(2) == (byte) 121 && data.get(3) == (byte) 10) {
            data.position(8);
            int offset = data.getInt();
            int length = data.getInt();
            data.position(offset);
            data.limit(offset + length);
            data = data.slice();
        }
        return new Dex(data);
    }

    private void loadFrom(InputStream in) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        while (true) {
            int count = in.read(buffer);
            if (count != -1) {
                bytesOut.write(buffer, 0, count);
            } else {
                in.close();
                this.data = ByteBuffer.wrap(bytesOut.toByteArray());
                this.data.order(ByteOrder.LITTLE_ENDIAN);
                this.tableOfContents.readFrom(this);
                return;
            }
        }
    }

    private static void checkBounds(int index, int length) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("index:" + index + ", length=" + length);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        ByteBuffer data = this.data.duplicate();
        data.clear();
        while (data.hasRemaining()) {
            int count = Math.min(buffer.length, data.remaining());
            data.get(buffer, 0, count);
            out.write(buffer, 0, count);
        }
    }

    public void writeTo(File dexOut) throws IOException {
        OutputStream out = new FileOutputStream(dexOut);
        writeTo(out);
        out.close();
    }

    public TableOfContents getTableOfContents() {
        return this.tableOfContents;
    }

    public Section open(int position) {
        if (position < 0 || position >= this.data.capacity()) {
            throw new IllegalArgumentException("position=" + position + " length=" + this.data.capacity());
        }
        ByteBuffer sectionData = this.data.duplicate();
        sectionData.order(ByteOrder.LITTLE_ENDIAN);
        sectionData.position(position);
        sectionData.limit(this.data.capacity());
        return new Section(this, "section", sectionData, null);
    }

    public Section appendSection(int maxByteCount, String name) {
        if ((maxByteCount & 3) != 0) {
            throw new IllegalStateException("Not four byte aligned!");
        }
        int limit = this.nextSectionStart + maxByteCount;
        ByteBuffer sectionData = this.data.duplicate();
        sectionData.order(ByteOrder.LITTLE_ENDIAN);
        sectionData.position(this.nextSectionStart);
        sectionData.limit(limit);
        Section result = new Section(this, name, sectionData, null);
        this.nextSectionStart = limit;
        return result;
    }

    public int getLength() {
        return this.data.capacity();
    }

    public int getNextSectionStart() {
        return this.nextSectionStart;
    }

    public byte[] getBytes() {
        ByteBuffer data = this.data.duplicate();
        byte[] result = new byte[data.capacity()];
        data.position(0);
        data.get(result);
        return result;
    }

    public List<String> strings() {
        return this.strings;
    }

    public List<Integer> typeIds() {
        return this.typeIds;
    }

    public List<String> typeNames() {
        return this.typeNames;
    }

    public List<ProtoId> protoIds() {
        return this.protoIds;
    }

    public List<FieldId> fieldIds() {
        return this.fieldIds;
    }

    public List<MethodId> methodIds() {
        return this.methodIds;
    }

    public Iterable<ClassDef> classDefs() {
        return new ClassDefIterable(this, null);
    }

    public TypeList readTypeList(int offset) {
        if (offset == 0) {
            return TypeList.EMPTY;
        }
        return open(offset).readTypeList();
    }

    public ClassData readClassData(ClassDef classDef) {
        int offset = classDef.getClassDataOffset();
        if (offset != 0) {
            return open(offset).readClassData();
        }
        throw new IllegalArgumentException("offset == 0");
    }

    public Code readCode(Method method) {
        int offset = method.getCodeOffset();
        if (offset != 0) {
            return open(offset).readCode();
        }
        throw new IllegalArgumentException("offset == 0");
    }

    public byte[] computeSignature() throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            ByteBuffer data = this.data.duplicate();
            data.limit(data.capacity());
            data.position(32);
            while (data.hasRemaining()) {
                int count = Math.min(buffer.length, data.remaining());
                data.get(buffer, 0, count);
                digest.update(buffer, 0, count);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    public int computeChecksum() throws IOException {
        Adler32 adler32 = new Adler32();
        byte[] buffer = new byte[8192];
        ByteBuffer data = this.data.duplicate();
        data.limit(data.capacity());
        data.position(12);
        while (data.hasRemaining()) {
            int count = Math.min(buffer.length, data.remaining());
            data.get(buffer, 0, count);
            adler32.update(buffer, 0, count);
        }
        return (int) adler32.getValue();
    }

    public void writeHashes() throws IOException {
        open(12).write(computeSignature());
        open(8).writeInt(computeChecksum());
    }

    public int nameIndexFromFieldIndex(int fieldIndex) {
        checkBounds(fieldIndex, this.tableOfContents.fieldIds.size);
        return this.data.getInt(((this.tableOfContents.fieldIds.off + (fieldIndex * 8)) + 2) + 2);
    }

    public int findStringIndex(String s) {
        return Collections.binarySearch(this.strings, s);
    }

    public int findTypeIndex(String descriptor) {
        return Collections.binarySearch(this.typeNames, descriptor);
    }

    public int findFieldIndex(FieldId fieldId) {
        return Collections.binarySearch(this.fieldIds, fieldId);
    }

    public int findMethodIndex(MethodId methodId) {
        return Collections.binarySearch(this.methodIds, methodId);
    }

    public int findClassDefIndexFromTypeIndex(int typeIndex) {
        checkBounds(typeIndex, this.tableOfContents.typeIds.size);
        if (!this.tableOfContents.classDefs.exists()) {
            return -1;
        }
        for (int i = 0; i < this.tableOfContents.classDefs.size; i++) {
            if (typeIndexFromClassDefIndex(i) == typeIndex) {
                return i;
            }
        }
        return -1;
    }

    public int typeIndexFromFieldIndex(int fieldIndex) {
        checkBounds(fieldIndex, this.tableOfContents.fieldIds.size);
        return this.data.getShort((this.tableOfContents.fieldIds.off + (fieldIndex * 8)) + 2) & 65535;
    }

    public int declaringClassIndexFromMethodIndex(int methodIndex) {
        checkBounds(methodIndex, this.tableOfContents.methodIds.size);
        return this.data.getShort(this.tableOfContents.methodIds.off + (methodIndex * 8)) & 65535;
    }

    public int nameIndexFromMethodIndex(int methodIndex) {
        checkBounds(methodIndex, this.tableOfContents.methodIds.size);
        return this.data.getInt(((this.tableOfContents.methodIds.off + (methodIndex * 8)) + 2) + 2);
    }

    public short[] parameterTypeIndicesFromMethodIndex(int methodIndex) {
        checkBounds(methodIndex, this.tableOfContents.methodIds.size);
        int protoIndex = this.data.getShort((this.tableOfContents.methodIds.off + (methodIndex * 8)) + 2) & 65535;
        checkBounds(protoIndex, this.tableOfContents.protoIds.size);
        int parametersOffset = this.data.getInt(((this.tableOfContents.protoIds.off + (protoIndex * 12)) + 4) + 4);
        if (parametersOffset == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        int position = parametersOffset;
        int size = this.data.getInt(parametersOffset);
        if (size <= 0) {
            throw new AssertionError("Unexpected parameter type list size: " + size);
        }
        position = parametersOffset + 4;
        short[] types = new short[size];
        for (int i = 0; i < size; i++) {
            types[i] = this.data.getShort(position);
            position += 2;
        }
        return types;
    }

    public int returnTypeIndexFromMethodIndex(int methodIndex) {
        checkBounds(methodIndex, this.tableOfContents.methodIds.size);
        int protoIndex = this.data.getShort((this.tableOfContents.methodIds.off + (methodIndex * 8)) + 2) & 65535;
        checkBounds(protoIndex, this.tableOfContents.protoIds.size);
        return this.data.getInt((this.tableOfContents.protoIds.off + (protoIndex * 12)) + 4);
    }

    public int descriptorIndexFromTypeIndex(int typeIndex) {
        checkBounds(typeIndex, this.tableOfContents.typeIds.size);
        return this.data.getInt(this.tableOfContents.typeIds.off + (typeIndex * 4));
    }

    public int typeIndexFromClassDefIndex(int classDefIndex) {
        checkBounds(classDefIndex, this.tableOfContents.classDefs.size);
        return this.data.getInt(this.tableOfContents.classDefs.off + (classDefIndex * 32));
    }

    public int annotationDirectoryOffsetFromClassDefIndex(int classDefIndex) {
        checkBounds(classDefIndex, this.tableOfContents.classDefs.size);
        return this.data.getInt((((((this.tableOfContents.classDefs.off + (classDefIndex * 32)) + 4) + 4) + 4) + 4) + 4);
    }

    public short[] interfaceTypeIndicesFromClassDefIndex(int classDefIndex) {
        checkBounds(classDefIndex, this.tableOfContents.classDefs.size);
        int interfacesOffset = this.data.getInt((((this.tableOfContents.classDefs.off + (classDefIndex * 32)) + 4) + 4) + 4);
        if (interfacesOffset == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        int position = interfacesOffset;
        int size = this.data.getInt(interfacesOffset);
        if (size <= 0) {
            throw new AssertionError("Unexpected interfaces list size: " + size);
        }
        position = interfacesOffset + 4;
        short[] types = new short[size];
        for (int i = 0; i < size; i++) {
            types[i] = this.data.getShort(position);
            position += 2;
        }
        return types;
    }
}

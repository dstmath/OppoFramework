package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
public abstract class Trie2 implements Iterable<Range> {
    /* renamed from: -android-icu-impl-Trie2$ValueWidthSwitchesValues */
    private static final /* synthetic */ int[] f36-android-icu-impl-Trie2$ValueWidthSwitchesValues = null;
    static final int UNEWTRIE2_INDEX_1_LENGTH = 544;
    static final int UNEWTRIE2_INDEX_GAP_LENGTH = 576;
    static final int UNEWTRIE2_INDEX_GAP_OFFSET = 2080;
    static final int UNEWTRIE2_MAX_DATA_LENGTH = 1115264;
    static final int UNEWTRIE2_MAX_INDEX_2_LENGTH = 35488;
    static final int UTRIE2_BAD_UTF8_DATA_OFFSET = 128;
    static final int UTRIE2_CP_PER_INDEX_1_ENTRY = 2048;
    static final int UTRIE2_DATA_BLOCK_LENGTH = 32;
    static final int UTRIE2_DATA_GRANULARITY = 4;
    static final int UTRIE2_DATA_MASK = 31;
    static final int UTRIE2_DATA_START_OFFSET = 192;
    static final int UTRIE2_INDEX_1_OFFSET = 2112;
    static final int UTRIE2_INDEX_2_BLOCK_LENGTH = 64;
    static final int UTRIE2_INDEX_2_BMP_LENGTH = 2080;
    static final int UTRIE2_INDEX_2_MASK = 63;
    static final int UTRIE2_INDEX_2_OFFSET = 0;
    static final int UTRIE2_INDEX_SHIFT = 2;
    static final int UTRIE2_LSCP_INDEX_2_LENGTH = 32;
    static final int UTRIE2_LSCP_INDEX_2_OFFSET = 2048;
    static final int UTRIE2_MAX_INDEX_1_LENGTH = 512;
    static final int UTRIE2_OMITTED_BMP_INDEX_1_LENGTH = 32;
    static final int UTRIE2_OPTIONS_VALUE_BITS_MASK = 15;
    static final int UTRIE2_SHIFT_1 = 11;
    static final int UTRIE2_SHIFT_1_2 = 6;
    static final int UTRIE2_SHIFT_2 = 5;
    static final int UTRIE2_UTF8_2B_INDEX_2_LENGTH = 32;
    static final int UTRIE2_UTF8_2B_INDEX_2_OFFSET = 2080;
    private static ValueMapper defaultValueMapper;
    int data16;
    int[] data32;
    int dataLength;
    int dataNullOffset;
    int errorValue;
    int fHash;
    UTrie2Header header;
    int highStart;
    int highValueIndex;
    char[] index;
    int index2NullOffset;
    int indexLength;
    int initialValue;

    public interface ValueMapper {
        int map(int i);
    }

    public class CharSequenceIterator implements Iterator<CharSequenceValues> {
        private CharSequenceValues fResults;
        private int index;
        private CharSequence text;
        private int textLength;
        final /* synthetic */ Trie2 this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.Trie2.CharSequenceIterator.<init>(android.icu.impl.Trie2, java.lang.CharSequence, int):void, dex: 
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
        CharSequenceIterator(android.icu.impl.Trie2 r1, java.lang.CharSequence r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.Trie2.CharSequenceIterator.<init>(android.icu.impl.Trie2, java.lang.CharSequence, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.<init>(android.icu.impl.Trie2, java.lang.CharSequence, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.Trie2.CharSequenceIterator.hasNext():boolean, dex: 
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
        public final boolean hasNext() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.Trie2.CharSequenceIterator.hasNext():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.hasNext():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.Trie2.CharSequenceIterator.hasPrevious():boolean, dex: 
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
        public final boolean hasPrevious() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.Trie2.CharSequenceIterator.hasPrevious():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.hasPrevious():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.Trie2.CharSequenceIterator.next():android.icu.impl.Trie2$CharSequenceValues, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.impl.Trie2.CharSequenceValues next() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.Trie2.CharSequenceIterator.next():android.icu.impl.Trie2$CharSequenceValues, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.next():android.icu.impl.Trie2$CharSequenceValues");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.Trie2.CharSequenceIterator.next():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.Trie2.CharSequenceIterator.next():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.next():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.Trie2.CharSequenceIterator.previous():android.icu.impl.Trie2$CharSequenceValues, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public android.icu.impl.Trie2.CharSequenceValues previous() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.Trie2.CharSequenceIterator.previous():android.icu.impl.Trie2$CharSequenceValues, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.previous():android.icu.impl.Trie2$CharSequenceValues");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.icu.impl.Trie2.CharSequenceIterator.set(int):void, dex:  in method: android.icu.impl.Trie2.CharSequenceIterator.set(int):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.icu.impl.Trie2.CharSequenceIterator.set(int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void set(int r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.icu.impl.Trie2.CharSequenceIterator.set(int):void, dex:  in method: android.icu.impl.Trie2.CharSequenceIterator.set(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceIterator.set(int):void");
        }

        public void remove() {
            throw new UnsupportedOperationException("Trie2.CharSequenceIterator does not support remove().");
        }
    }

    public static class CharSequenceValues {
        public int codePoint;
        public int index;
        public int value;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.Trie2.CharSequenceValues.<init>():void, dex: 
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
        public CharSequenceValues() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.Trie2.CharSequenceValues.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.CharSequenceValues.<init>():void");
        }
    }

    public static class Range {
        public int endCodePoint;
        public boolean leadSurrogate;
        public int startCodePoint;
        public int value;

        public Range() {
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (other == null || !other.getClass().equals(getClass())) {
                return false;
            }
            Range tother = (Range) other;
            if (this.startCodePoint == tother.startCodePoint && this.endCodePoint == tother.endCodePoint && this.value == tother.value && this.leadSurrogate == tother.leadSurrogate) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return Trie2.hashByte(Trie2.hashInt(Trie2.hashUChar32(Trie2.hashUChar32(Trie2.initHash(), this.startCodePoint), this.endCodePoint), this.value), this.leadSurrogate ? 1 : 0);
        }
    }

    class Trie2Iterator implements Iterator<Range> {
        private boolean doLeadSurrogates;
        private boolean doingCodePoints;
        private int limitCP;
        private ValueMapper mapper;
        private int nextStart;
        private Range returnValue;
        final /* synthetic */ Trie2 this$0;

        Trie2Iterator(Trie2 this$0, ValueMapper vm) {
            this.this$0 = this$0;
            this.returnValue = new Range();
            this.doingCodePoints = true;
            this.doLeadSurrogates = true;
            this.mapper = vm;
            this.nextStart = 0;
            this.limitCP = 1114112;
            this.doLeadSurrogates = true;
        }

        Trie2Iterator(Trie2 this$0, char leadSurrogate, ValueMapper vm) {
            this.this$0 = this$0;
            this.returnValue = new Range();
            this.doingCodePoints = true;
            this.doLeadSurrogates = true;
            if (leadSurrogate < 55296 || leadSurrogate > UCharacter.MAX_HIGH_SURROGATE) {
                throw new IllegalArgumentException("Bad lead surrogate value.");
            }
            this.mapper = vm;
            this.nextStart = (leadSurrogate - 55232) << 10;
            this.limitCP = this.nextStart + 1024;
            this.doLeadSurrogates = false;
        }

        public /* bridge */ /* synthetic */ Object next() {
            return next();
        }

        public Range next() {
            boolean z = false;
            if (hasNext()) {
                int mappedVal;
                int endOfRange;
                if (this.nextStart >= this.limitCP) {
                    this.doingCodePoints = false;
                    this.nextStart = 55296;
                }
                if (!this.doingCodePoints) {
                    mappedVal = this.mapper.map(this.this$0.getFromU16SingleLead((char) this.nextStart));
                    endOfRange = rangeEndLS((char) this.nextStart);
                    while (endOfRange < UTF16.LEAD_SURROGATE_MAX_VALUE) {
                        if (this.mapper.map(this.this$0.getFromU16SingleLead((char) (endOfRange + 1))) != mappedVal) {
                            break;
                        }
                        endOfRange = rangeEndLS((char) (endOfRange + 1));
                    }
                } else {
                    int val = this.this$0.get(this.nextStart);
                    mappedVal = this.mapper.map(val);
                    endOfRange = this.this$0.rangeEnd(this.nextStart, this.limitCP, val);
                    while (endOfRange < this.limitCP - 1) {
                        val = this.this$0.get(endOfRange + 1);
                        if (this.mapper.map(val) != mappedVal) {
                            break;
                        }
                        endOfRange = this.this$0.rangeEnd(endOfRange + 1, this.limitCP, val);
                    }
                }
                this.returnValue.startCodePoint = this.nextStart;
                this.returnValue.endCodePoint = endOfRange;
                this.returnValue.value = mappedVal;
                Range range = this.returnValue;
                if (!this.doingCodePoints) {
                    z = true;
                }
                range.leadSurrogate = z;
                this.nextStart = endOfRange + 1;
                return this.returnValue;
            }
            throw new NoSuchElementException();
        }

        public boolean hasNext() {
            return (this.doingCodePoints && (this.doLeadSurrogates || this.nextStart < this.limitCP)) || this.nextStart < UTF16.TRAIL_SURROGATE_MIN_VALUE;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int rangeEndLS(char startingLS) {
            if (startingLS >= UCharacter.MAX_HIGH_SURROGATE) {
                return UTF16.LEAD_SURROGATE_MAX_VALUE;
            }
            int val = this.this$0.getFromU16SingleLead(startingLS);
            int c = startingLS + 1;
            while (c <= UTF16.LEAD_SURROGATE_MAX_VALUE && this.this$0.getFromU16SingleLead((char) c) == val) {
                c++;
            }
            return c - 1;
        }
    }

    static class UTrie2Header {
        int dataNullOffset;
        int index2NullOffset;
        int indexLength;
        int options;
        int shiftedDataLength;
        int shiftedHighStart;
        int signature;

        UTrie2Header() {
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
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
    enum ValueWidth {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.Trie2.ValueWidth.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.Trie2.ValueWidth.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.ValueWidth.<clinit>():void");
        }
    }

    /* renamed from: -getandroid-icu-impl-Trie2$ValueWidthSwitchesValues */
    private static /* synthetic */ int[] m12-getandroid-icu-impl-Trie2$ValueWidthSwitchesValues() {
        if (f36-android-icu-impl-Trie2$ValueWidthSwitchesValues != null) {
            return f36-android-icu-impl-Trie2$ValueWidthSwitchesValues;
        }
        int[] iArr = new int[ValueWidth.values().length];
        try {
            iArr[ValueWidth.BITS_16.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ValueWidth.BITS_32.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        f36-android-icu-impl-Trie2$ValueWidthSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.Trie2.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.Trie2.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.Trie2.<clinit>():void");
    }

    public abstract int get(int i);

    public abstract int getFromU16SingleLead(char c);

    public Trie2() {
    }

    public static Trie2 createFromSerialized(ByteBuffer bytes) throws IOException {
        boolean isBigEndian = false;
        ByteOrder outerByteOrder = bytes.order();
        try {
            UTrie2Header header = new UTrie2Header();
            header.signature = bytes.getInt();
            switch (header.signature) {
                case 845771348:
                    if (outerByteOrder == ByteOrder.BIG_ENDIAN) {
                        isBigEndian = true;
                    }
                    bytes.order(isBigEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                    header.signature = 1416784178;
                case 1416784178:
                    header.options = bytes.getChar();
                    header.indexLength = bytes.getChar();
                    header.shiftedDataLength = bytes.getChar();
                    header.index2NullOffset = bytes.getChar();
                    header.dataNullOffset = bytes.getChar();
                    header.shiftedHighStart = bytes.getChar();
                    if ((header.options & 15) > 1) {
                        throw new IllegalArgumentException("UTrie2 serialized format error.");
                    }
                    ValueWidth width;
                    Trie2 This;
                    if ((header.options & 15) == 0) {
                        width = ValueWidth.BITS_16;
                        This = new Trie2_16();
                    } else {
                        width = ValueWidth.BITS_32;
                        This = new Trie2_32();
                    }
                    This.header = header;
                    This.indexLength = header.indexLength;
                    This.dataLength = header.shiftedDataLength << 2;
                    This.index2NullOffset = header.index2NullOffset;
                    This.dataNullOffset = header.dataNullOffset;
                    This.highStart = header.shiftedHighStart << 11;
                    This.highValueIndex = This.dataLength - 4;
                    if (width == ValueWidth.BITS_16) {
                        This.highValueIndex += This.indexLength;
                    }
                    int indexArraySize = This.indexLength;
                    if (width == ValueWidth.BITS_16) {
                        indexArraySize += This.dataLength;
                    }
                    This.index = ICUBinary.getChars(bytes, indexArraySize, 0);
                    if (width == ValueWidth.BITS_16) {
                        This.data16 = This.indexLength;
                    } else {
                        This.data32 = ICUBinary.getInts(bytes, This.dataLength, 0);
                    }
                    switch (m12-getandroid-icu-impl-Trie2$ValueWidthSwitchesValues()[width.ordinal()]) {
                        case 1:
                            This.data32 = null;
                            This.initialValue = This.index[This.dataNullOffset];
                            This.errorValue = This.index[This.data16 + 128];
                            break;
                        case 2:
                            This.data16 = 0;
                            This.initialValue = This.data32[This.dataNullOffset];
                            This.errorValue = This.data32[128];
                            break;
                        default:
                            throw new IllegalArgumentException("UTrie2 serialized format error.");
                    }
                    bytes.order(outerByteOrder);
                    return This;
                default:
                    throw new IllegalArgumentException("Buffer does not contain a serialized UTrie2");
            }
        } catch (Throwable th) {
            bytes.order(outerByteOrder);
        }
        bytes.order(outerByteOrder);
    }

    public static int getVersion(InputStream is, boolean littleEndianOk) throws IOException {
        if (is.markSupported()) {
            is.mark(4);
            byte[] sig = new byte[4];
            int read = is.read(sig);
            is.reset();
            if (read != sig.length) {
                return 0;
            }
            if (sig[0] == (byte) 84 && sig[1] == (byte) 114 && sig[2] == (byte) 105 && sig[3] == (byte) 101) {
                return 1;
            }
            if (sig[0] == (byte) 84 && sig[1] == (byte) 114 && sig[2] == (byte) 105 && sig[3] == (byte) 50) {
                return 2;
            }
            if (littleEndianOk) {
                if (sig[0] == (byte) 101 && sig[1] == (byte) 105 && sig[2] == (byte) 114 && sig[3] == (byte) 84) {
                    return 1;
                }
                return (sig[0] == (byte) 50 && sig[1] == (byte) 105 && sig[2] == (byte) 114 && sig[3] == (byte) 84) ? 2 : 0;
            }
        }
        throw new IllegalArgumentException("Input stream must support mark().");
    }

    public final boolean equals(Object other) {
        if (!(other instanceof Trie2)) {
            return false;
        }
        Trie2 OtherTrie = (Trie2) other;
        Iterator<Range> otherIter = OtherTrie.iterator();
        for (Range rangeFromThis : this) {
            if (!otherIter.hasNext()) {
                return false;
            }
            if (!rangeFromThis.equals((Range) otherIter.next())) {
                return false;
            }
        }
        if (!otherIter.hasNext() && this.errorValue == OtherTrie.errorValue && this.initialValue == OtherTrie.initialValue) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (this.fHash == 0) {
            int hash = initHash();
            for (Range r : this) {
                hash = hashInt(hash, r.hashCode());
            }
            if (hash == 0) {
                hash = 1;
            }
            this.fHash = hash;
        }
        return this.fHash;
    }

    public Iterator<Range> iterator() {
        return iterator(defaultValueMapper);
    }

    public Iterator<Range> iterator(ValueMapper mapper) {
        return new Trie2Iterator(this, mapper);
    }

    public Iterator<Range> iteratorForLeadSurrogate(char lead, ValueMapper mapper) {
        return new Trie2Iterator(this, lead, mapper);
    }

    public Iterator<Range> iteratorForLeadSurrogate(char lead) {
        return new Trie2Iterator(this, lead, defaultValueMapper);
    }

    protected int serializeHeader(DataOutputStream dos) throws IOException {
        dos.writeInt(this.header.signature);
        dos.writeShort(this.header.options);
        dos.writeShort(this.header.indexLength);
        dos.writeShort(this.header.shiftedDataLength);
        dos.writeShort(this.header.index2NullOffset);
        dos.writeShort(this.header.dataNullOffset);
        dos.writeShort(this.header.shiftedHighStart);
        for (int i = 0; i < this.header.indexLength; i++) {
            dos.writeChar(this.index[i]);
        }
        return this.header.indexLength + 16;
    }

    public CharSequenceIterator charSequenceIterator(CharSequence text, int index) {
        return new CharSequenceIterator(this, text, index);
    }

    int rangeEnd(int start, int limitp, int val) {
        int limit = Math.min(this.highStart, limitp);
        int c = start + 1;
        while (c < limit && get(c) == val) {
            c++;
        }
        if (c >= this.highStart) {
            c = limitp;
        }
        return c - 1;
    }

    private static int initHash() {
        return -2128831035;
    }

    private static int hashByte(int h, int b) {
        return (h * 16777619) ^ b;
    }

    private static int hashUChar32(int h, int c) {
        return hashByte(hashByte(hashByte(h, c & 255), (c >> 8) & 255), c >> 16);
    }

    private static int hashInt(int h, int i) {
        return hashByte(hashByte(hashByte(hashByte(h, i & 255), (i >> 8) & 255), (i >> 16) & 255), (i >> 24) & 255);
    }
}

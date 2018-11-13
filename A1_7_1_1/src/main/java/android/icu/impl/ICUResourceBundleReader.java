package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.UResource.ArraySink;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
import android.icu.impl.UResource.Value;
import android.icu.impl.locale.BaseLocale;
import android.icu.text.UTF16;
import android.icu.util.ICUException;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.ULocale;
import android.icu.util.UResourceTypeMismatchException;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

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
public final class ICUResourceBundleReader {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f17-assertionsDisabled = false;
    private static ReaderCache CACHE = null;
    private static final int DATA_FORMAT = 1382380354;
    private static final boolean DEBUG = false;
    private static final CharBuffer EMPTY_16_BIT_UNITS = null;
    private static final Array EMPTY_ARRAY = null;
    private static final Table EMPTY_TABLE = null;
    private static final String ICU_RESOURCE_SUFFIX = ".res";
    private static final IsAcceptable IS_ACCEPTABLE = null;
    static final int LARGE_SIZE = 24;
    private static final ICUResourceBundleReader NULL_READER = null;
    private static int[] PUBLIC_TYPES = null;
    private static final int URES_ATT_IS_POOL_BUNDLE = 2;
    private static final int URES_ATT_NO_FALLBACK = 1;
    private static final int URES_ATT_USES_POOL_BUNDLE = 4;
    private static final int URES_INDEX_16BIT_TOP = 6;
    private static final int URES_INDEX_ATTRIBUTES = 5;
    private static final int URES_INDEX_BUNDLE_TOP = 3;
    private static final int URES_INDEX_KEYS_TOP = 1;
    private static final int URES_INDEX_LENGTH = 0;
    private static final int URES_INDEX_MAX_TABLE_LENGTH = 4;
    private static final int URES_INDEX_POOL_CHECKSUM = 7;
    private static final ByteBuffer emptyByteBuffer = null;
    private static final byte[] emptyBytes = null;
    private static final char[] emptyChars = null;
    private static final int[] emptyInts = null;
    private static final String emptyString = "";
    private CharBuffer b16BitUnits;
    private ByteBuffer bytes;
    private int dataVersion;
    private boolean isPoolBundle;
    private byte[] keyBytes;
    private int localKeyLimit;
    private boolean noFallback;
    private ICUResourceBundleReader poolBundleReader;
    private int poolCheckSum;
    private int poolStringIndex16Limit;
    private int poolStringIndexLimit;
    private ResourceCache resourceCache;
    private int rootRes;
    private boolean usesPoolBundle;

    static class Container {
        protected int itemsOffset;
        protected int size;

        final int getSize() {
            return this.size;
        }

        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return -1;
        }

        protected int getContainer16Resource(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            int res16 = reader.b16BitUnits.charAt(this.itemsOffset + index);
            if (res16 >= reader.poolStringIndex16Limit) {
                res16 = (res16 - reader.poolStringIndex16Limit) + reader.poolStringIndexLimit;
            }
            return 1610612736 | res16;
        }

        protected int getContainer32Resource(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            return reader.getInt(this.itemsOffset + (index * 4));
        }

        int getResource(ICUResourceBundleReader reader, String resKey) {
            return getContainerResource(reader, Integer.parseInt(resKey));
        }

        Container() {
        }
    }

    static class Array extends Container {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f18-assertionsDisabled = false;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.Array.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.Array.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.Array.<clinit>():void");
        }

        Array() {
        }

        void getAllItems(ICUResourceBundleReader reader, Key key, ReaderValue value, ArraySink sink) {
            for (int i = 0; i < this.size; i++) {
                int res = getContainerResource(reader, i);
                int type = ICUResourceBundleReader.RES_GET_TYPE(res);
                int numItems;
                if (ICUResourceBundleReader.URES_IS_ARRAY(type)) {
                    numItems = reader.getArrayLength(res);
                    ArraySink subSink = sink.getOrCreateArraySink(i, numItems);
                    if (subSink != null) {
                        Array array = reader.getArray(res);
                        if (!f18-assertionsDisabled) {
                            if ((array.size == numItems ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        array.getAllItems(reader, key, value, subSink);
                    } else {
                        continue;
                    }
                } else if (ICUResourceBundleReader.URES_IS_TABLE(type)) {
                    numItems = reader.getTableLength(res);
                    TableSink subSink2 = sink.getOrCreateTableSink(i, numItems);
                    if (subSink2 != null) {
                        Table table = reader.getTable(res);
                        if (!f18-assertionsDisabled) {
                            if ((table.size == numItems ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        table.getAllItems(reader, key, value, subSink2);
                    } else {
                        continue;
                    }
                } else {
                    value.res = res;
                    sink.put(i, value);
                }
            }
            sink.leave();
        }
    }

    private static final class Array16 extends Array {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer16Resource(reader, index);
        }

        Array16(ICUResourceBundleReader reader, int offset) {
            this.size = reader.b16BitUnits.charAt(offset);
            this.itemsOffset = offset + 1;
        }
    }

    private static final class Array32 extends Array {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Array32(ICUResourceBundleReader reader, int offset) {
            offset = reader.getResourceByteOffset(offset);
            this.size = reader.getInt(offset);
            this.itemsOffset = offset + 4;
        }
    }

    private static final class IsAcceptable implements Authenticate {
        /* synthetic */ IsAcceptable(IsAcceptable isAcceptable) {
            this();
        }

        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] formatVersion) {
            if (formatVersion[0] == (byte) 1 && (formatVersion[1] & 255) >= 1) {
                return true;
            }
            if ((byte) 2 > formatVersion[0] || formatVersion[0] > (byte) 3) {
                return false;
            }
            return true;
        }
    }

    private static class ReaderCache extends SoftCache<ReaderCacheKey, ICUResourceBundleReader, ClassLoader> {
        /* synthetic */ ReaderCache(ReaderCache readerCache) {
            this();
        }

        private ReaderCache() {
        }

        protected ICUResourceBundleReader createInstance(ReaderCacheKey key, ClassLoader loader) {
            String fullName = ICUResourceBundleReader.getFullName(key.baseName, key.localeID);
            try {
                ByteBuffer inBytes;
                if (key.baseName == null || !key.baseName.startsWith("android/icu/impl/data/icudt56b")) {
                    InputStream stream = ICUData.getStream(loader, fullName);
                    if (stream == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                    inBytes = ICUBinary.getByteBufferFromInputStreamAndCloseStream(stream);
                } else {
                    inBytes = ICUBinary.getData(loader, fullName, fullName.substring("android/icu/impl/data/icudt56b".length() + 1));
                    if (inBytes == null) {
                        return ICUResourceBundleReader.NULL_READER;
                    }
                }
                return new ICUResourceBundleReader(inBytes, key.baseName, key.localeID, loader, null);
            } catch (IOException ex) {
                throw new ICUUncheckedIOException("Data file " + fullName + " is corrupt - " + ex.getMessage(), ex);
            }
        }
    }

    private static class ReaderCacheKey {
        final String baseName;
        final String localeID;

        ReaderCacheKey(String baseName, String localeID) {
            if (baseName == null) {
                baseName = "";
            }
            this.baseName = baseName;
            if (localeID == null) {
                localeID = "";
            }
            this.localeID = localeID;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ReaderCacheKey)) {
                return false;
            }
            ReaderCacheKey info = (ReaderCacheKey) obj;
            if (this.baseName.equals(info.baseName)) {
                z = this.localeID.equals(info.localeID);
            }
            return z;
        }

        public int hashCode() {
            return this.baseName.hashCode() ^ this.localeID.hashCode();
        }
    }

    static class ReaderValue extends Value {
        ICUResourceBundleReader reader;
        private int res;

        ReaderValue() {
        }

        public int getType() {
            return ICUResourceBundleReader.PUBLIC_TYPES[ICUResourceBundleReader.RES_GET_TYPE(this.res)];
        }

        public String getString() {
            String s = this.reader.getString(this.res);
            if (s != null) {
                return s;
            }
            throw new UResourceTypeMismatchException("");
        }

        public String getAliasString() {
            String s = this.reader.getAlias(this.res);
            if (s != null) {
                return s;
            }
            throw new UResourceTypeMismatchException("");
        }

        public int getInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == 7) {
                return ICUResourceBundleReader.RES_GET_INT(this.res);
            }
            throw new UResourceTypeMismatchException("");
        }

        public int getUInt() {
            if (ICUResourceBundleReader.RES_GET_TYPE(this.res) == 7) {
                return ICUResourceBundleReader.RES_GET_UINT(this.res);
            }
            throw new UResourceTypeMismatchException("");
        }

        public int[] getIntVector() {
            int[] iv = this.reader.getIntVector(this.res);
            if (iv != null) {
                return iv;
            }
            throw new UResourceTypeMismatchException("");
        }

        public ByteBuffer getBinary() {
            ByteBuffer bb = this.reader.getBinary(this.res);
            if (bb != null) {
                return bb;
            }
            throw new UResourceTypeMismatchException("");
        }
    }

    private static final class ResourceCache {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f19-assertionsDisabled = false;
        private static final int NEXT_BITS = 6;
        private static final int ROOT_BITS = 7;
        private static final int SIMPLE_LENGTH = 32;
        private int[] keys;
        private int length;
        private int levelBitsList;
        private int maxOffsetBits;
        private Level rootLevel;
        private Object[] values;

        private static final class Level {
            /* renamed from: -assertionsDisabled */
            static final /* synthetic */ boolean f20-assertionsDisabled = false;
            int[] keys;
            int levelBitsList;
            int mask;
            int shift;
            Object[] values;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.Level.<clinit>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            static {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.Level.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.ResourceCache.Level.<clinit>():void");
            }

            Level(int levelBitsList, int shift) {
                int i = 0;
                this.levelBitsList = levelBitsList;
                this.shift = shift;
                int bits = levelBitsList & 15;
                if (!f20-assertionsDisabled) {
                    if (bits != 0) {
                        i = 1;
                    }
                    if (i == 0) {
                        throw new AssertionError();
                    }
                }
                int length = 1 << bits;
                this.mask = length - 1;
                this.keys = new int[length];
                this.values = new Object[length];
            }

            Object get(int key) {
                int index = (key >> this.shift) & this.mask;
                int k = this.keys[index];
                if (k == key) {
                    return this.values[index];
                }
                if (k == 0) {
                    Level level = this.values[index];
                    if (level != null) {
                        return level.get(key);
                    }
                }
                return null;
            }

            Object putIfAbsent(int key, Object item, int size) {
                int index = (key >> this.shift) & this.mask;
                int k = this.keys[index];
                if (k == key) {
                    return ResourceCache.putIfCleared(this.values, index, item, size);
                }
                Level level;
                if (k == 0) {
                    level = this.values[index];
                    if (level != null) {
                        return level.putIfAbsent(key, item, size);
                    }
                    this.keys[index] = key;
                    this.values[index] = ResourceCache.storeDirectly(size) ? item : new SoftReference(item);
                    return item;
                }
                level = new Level(this.levelBitsList >> 4, this.shift + (this.levelBitsList & 15));
                int i = (k >> level.shift) & level.mask;
                level.keys[i] = k;
                level.values[i] = this.values[index];
                this.keys[index] = 0;
                this.values[index] = level;
                return level.putIfAbsent(key, item, size);
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.ResourceCache.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.ResourceCache.<clinit>():void");
        }

        private static boolean storeDirectly(int size) {
            return size >= 24 ? CacheValue.futureInstancesWillBeStrong() : true;
        }

        private static final Object putIfCleared(Object[] values, int index, Object item, int size) {
            Object value = values[index];
            if (!(value instanceof SoftReference)) {
                return value;
            }
            if (!f19-assertionsDisabled) {
                if ((size >= 24 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            value = ((SoftReference) value).get();
            if (value != null) {
                return value;
            }
            SoftReference softReference;
            if (CacheValue.futureInstancesWillBeStrong()) {
                softReference = item;
            } else {
                softReference = new SoftReference(item);
            }
            values[index] = softReference;
            return item;
        }

        ResourceCache(int maxOffset) {
            Object obj = null;
            this.keys = new int[32];
            this.values = new Object[32];
            if (!f19-assertionsDisabled) {
                if (maxOffset != 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            this.maxOffsetBits = 28;
            while (maxOffset <= 134217727) {
                maxOffset <<= 1;
                this.maxOffsetBits--;
            }
            int keyBits = this.maxOffsetBits + 2;
            if (keyBits <= 7) {
                this.levelBitsList = keyBits;
            } else if (keyBits < 10) {
                this.levelBitsList = (keyBits - 3) | 48;
            } else {
                this.levelBitsList = 7;
                keyBits -= 7;
                int shift = 4;
                while (keyBits > 6) {
                    if (keyBits < 9) {
                        this.levelBitsList |= ((keyBits - 3) | 48) << shift;
                        return;
                    }
                    this.levelBitsList |= 6 << shift;
                    keyBits -= 6;
                    shift += 4;
                }
                this.levelBitsList |= keyBits << shift;
            }
        }

        private int makeKey(int res) {
            int type = ICUResourceBundleReader.RES_GET_TYPE(res);
            int miniType = type == 6 ? 1 : type == 5 ? 3 : type == 9 ? 2 : 0;
            return ICUResourceBundleReader.RES_GET_OFFSET(res) | (miniType << this.maxOffsetBits);
        }

        private int findSimple(int key) {
            int start = 0;
            int limit = this.length;
            while (limit - start > 8) {
                int mid = (start + limit) / 2;
                if (key < this.keys[mid]) {
                    limit = mid;
                } else {
                    start = mid;
                }
            }
            while (start < limit) {
                int k = this.keys[start];
                if (key < k) {
                    return ~start;
                }
                if (key == k) {
                    return start;
                }
                start++;
            }
            return ~start;
        }

        /* JADX WARNING: Missing block: B:24:0x0032, code:
            return r1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized Object get(int res) {
            Object obj = null;
            synchronized (this) {
                Object value;
                if (!f19-assertionsDisabled) {
                    if (ICUResourceBundleReader.RES_GET_OFFSET(res) != 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if (this.length >= 0) {
                    int index = findSimple(res);
                    if (index >= 0) {
                        value = this.values[index];
                    } else {
                        return null;
                    }
                }
                value = this.rootLevel.get(makeKey(res));
                if (value == null) {
                    return null;
                }
                if (value instanceof SoftReference) {
                    value = ((SoftReference) value).get();
                }
            }
        }

        synchronized Object putIfAbsent(int res, Object item, int size) {
            if (this.length >= 0) {
                int index = findSimple(res);
                if (index >= 0) {
                    return putIfCleared(this.values, index, item, size);
                } else if (this.length < 32) {
                    index = ~index;
                    if (index < this.length) {
                        System.arraycopy(this.keys, index, this.keys, index + 1, this.length - index);
                        System.arraycopy(this.values, index, this.values, index + 1, this.length - index);
                    }
                    this.length++;
                    this.keys[index] = res;
                    this.values[index] = storeDirectly(size) ? item : new SoftReference(item);
                    return item;
                } else {
                    this.rootLevel = new Level(this.levelBitsList, 0);
                    for (int i = 0; i < 32; i++) {
                        this.rootLevel.putIfAbsent(makeKey(this.keys[i]), this.values[i], 0);
                    }
                    this.keys = null;
                    this.values = null;
                    this.length = -1;
                }
            }
            return this.rootLevel.putIfAbsent(makeKey(res), item, size);
        }
    }

    static class Table extends Container {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f21-assertionsDisabled = false;
        private static final int URESDATA_ITEM_NOT_FOUND = -1;
        protected int[] key32Offsets;
        protected char[] keyOffsets;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.Table.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.Table.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.Table.<clinit>():void");
        }

        String getKey(ICUResourceBundleReader reader, int index) {
            if (index < 0 || this.size <= index) {
                return null;
            }
            String -wrap11;
            if (this.keyOffsets != null) {
                -wrap11 = reader.getKey16String(this.keyOffsets[index]);
            } else {
                -wrap11 = reader.getKey32String(this.key32Offsets[index]);
            }
            return -wrap11;
        }

        int findTableItem(ICUResourceBundleReader reader, CharSequence key) {
            int start = 0;
            int limit = this.size;
            while (start < limit) {
                int result;
                int mid = (start + limit) >>> 1;
                if (this.keyOffsets != null) {
                    result = reader.compareKeys(key, this.keyOffsets[mid]);
                } else {
                    result = reader.compareKeys32(key, this.key32Offsets[mid]);
                }
                if (result < 0) {
                    limit = mid;
                } else if (result <= 0) {
                    return mid;
                } else {
                    start = mid + 1;
                }
            }
            return -1;
        }

        int getResource(ICUResourceBundleReader reader, String resKey) {
            return getContainerResource(reader, findTableItem(reader, resKey));
        }

        void getAllItems(ICUResourceBundleReader reader, Key key, ReaderValue value, TableSink sink) {
            for (int i = 0; i < this.size; i++) {
                if (this.keyOffsets != null) {
                    reader.setKeyFromKey16(this.keyOffsets[i], key);
                } else {
                    reader.setKeyFromKey32(this.key32Offsets[i], key);
                }
                int res = getContainerResource(reader, i);
                int type = ICUResourceBundleReader.RES_GET_TYPE(res);
                int numItems;
                if (ICUResourceBundleReader.URES_IS_ARRAY(type)) {
                    numItems = reader.getArrayLength(res);
                    ArraySink subSink = sink.getOrCreateArraySink(key, numItems);
                    if (subSink != null) {
                        Array array = reader.getArray(res);
                        if (!f21-assertionsDisabled) {
                            if ((array.size == numItems ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        array.getAllItems(reader, key, value, subSink);
                    } else {
                        continue;
                    }
                } else if (ICUResourceBundleReader.URES_IS_TABLE(type)) {
                    numItems = reader.getTableLength(res);
                    TableSink subSink2 = sink.getOrCreateTableSink(key, numItems);
                    if (subSink2 != null) {
                        Table table = reader.getTable(res);
                        if (!f21-assertionsDisabled) {
                            if ((table.size == numItems ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        table.getAllItems(reader, key, value, subSink2);
                    } else {
                        continue;
                    }
                } else if (reader.isNoInheritanceMarker(res)) {
                    sink.putNoFallback(key);
                } else {
                    value.res = res;
                    sink.put(key, value);
                }
            }
            sink.leave();
        }

        Table() {
        }
    }

    private static final class Table1632 extends Table {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer32Resource(reader, index);
        }

        Table1632(ICUResourceBundleReader reader, int offset) {
            offset = reader.getResourceByteOffset(offset);
            this.keyOffsets = reader.getTableKeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = (((this.size + 2) & -2) * 2) + offset;
        }
    }

    private static final class Table16 extends Table {
        int getContainerResource(ICUResourceBundleReader reader, int index) {
            return getContainer16Resource(reader, index);
        }

        Table16(ICUResourceBundleReader reader, int offset) {
            this.keyOffsets = reader.getTable16KeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = (offset + 1) + this.size;
        }
    }

    private static final class Table32 extends Table {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUResourceBundleReader.Table32.<init>(android.icu.impl.ICUResourceBundleReader, int):void, dex: 
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
        Table32(android.icu.impl.ICUResourceBundleReader r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUResourceBundleReader.Table32.<init>(android.icu.impl.ICUResourceBundleReader, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.Table32.<init>(android.icu.impl.ICUResourceBundleReader, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.Table32.getContainerResource(android.icu.impl.ICUResourceBundleReader, int):int, dex: 
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
        int getContainerResource(android.icu.impl.ICUResourceBundleReader r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.Table32.getContainerResource(android.icu.impl.ICUResourceBundleReader, int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.Table32.getContainerResource(android.icu.impl.ICUResourceBundleReader, int):int");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUResourceBundleReader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUResourceBundleReader.<clinit>():void");
    }

    /* synthetic */ ICUResourceBundleReader(ByteBuffer inBytes, String baseName, String localeID, ClassLoader loader, ICUResourceBundleReader iCUResourceBundleReader) {
        this(inBytes, baseName, localeID, loader);
    }

    private ICUResourceBundleReader() {
    }

    private ICUResourceBundleReader(ByteBuffer inBytes, String baseName, String localeID, ClassLoader loader) throws IOException {
        init(inBytes);
        if (this.usesPoolBundle) {
            this.poolBundleReader = getReader(baseName, "pool", loader);
            if (!this.poolBundleReader.isPoolBundle) {
                throw new IllegalStateException("pool.res is not a pool bundle");
            } else if (this.poolBundleReader.poolCheckSum != this.poolCheckSum) {
                throw new IllegalStateException("pool.res has a different checksum than this bundle");
            }
        }
    }

    static ICUResourceBundleReader getReader(String baseName, String localeID, ClassLoader root) {
        ICUResourceBundleReader reader = (ICUResourceBundleReader) CACHE.getInstance(new ReaderCacheKey(baseName, localeID), root);
        if (reader == NULL_READER) {
            return null;
        }
        return reader;
    }

    private void init(ByteBuffer inBytes) throws IOException {
        this.dataVersion = ICUBinary.readHeader(inBytes, DATA_FORMAT, IS_ACCEPTABLE);
        int majorFormatVersion = inBytes.get(16);
        this.bytes = ICUBinary.sliceWithOrder(inBytes);
        int dataLength = this.bytes.remaining();
        this.rootRes = this.bytes.getInt(0);
        int indexes0 = getIndexesInt(0);
        int indexLength = indexes0 & 255;
        if (indexLength <= 4) {
            throw new ICUException("not enough indexes");
        }
        if (dataLength >= ((indexLength + 1) << 2)) {
            int bundleTop = getIndexesInt(3);
            if (dataLength >= (bundleTop << 2)) {
                int maxOffset = bundleTop - 1;
                if (majorFormatVersion >= 3) {
                    this.poolStringIndexLimit = indexes0 >>> 8;
                }
                if (indexLength > 5) {
                    int att = getIndexesInt(5);
                    this.noFallback = (att & 1) != 0;
                    this.isPoolBundle = (att & 2) != 0;
                    this.usesPoolBundle = (att & 4) != 0;
                    this.poolStringIndexLimit |= (61440 & att) << 12;
                    this.poolStringIndex16Limit = att >>> 16;
                }
                int keysBottom = indexLength + 1;
                int keysTop = getIndexesInt(1);
                if (keysTop > keysBottom) {
                    if (this.isPoolBundle) {
                        this.keyBytes = new byte[((keysTop - keysBottom) << 2)];
                        this.bytes.position(keysBottom << 2);
                    } else {
                        this.localKeyLimit = keysTop << 2;
                        this.keyBytes = new byte[this.localKeyLimit];
                    }
                    this.bytes.get(this.keyBytes);
                }
                if (indexLength > 6) {
                    int _16BitTop = getIndexesInt(6);
                    if (_16BitTop > keysTop) {
                        int num16BitUnits = (_16BitTop - keysTop) * 2;
                        this.bytes.position(keysTop << 2);
                        this.b16BitUnits = this.bytes.asCharBuffer();
                        this.b16BitUnits.limit(num16BitUnits);
                        maxOffset |= num16BitUnits - 1;
                    } else {
                        this.b16BitUnits = EMPTY_16_BIT_UNITS;
                    }
                } else {
                    this.b16BitUnits = EMPTY_16_BIT_UNITS;
                }
                if (indexLength > 7) {
                    this.poolCheckSum = getIndexesInt(7);
                }
                if (!this.isPoolBundle || this.b16BitUnits.length() > 1) {
                    this.resourceCache = new ResourceCache(maxOffset);
                }
                this.bytes.position(0);
                return;
            }
        }
        throw new ICUException("not enough bytes");
    }

    private int getIndexesInt(int i) {
        return this.bytes.getInt((i + 1) << 2);
    }

    VersionInfo getVersion() {
        return ICUBinary.getVersionInfoFromCompactInt(this.dataVersion);
    }

    int getRootResource() {
        return this.rootRes;
    }

    boolean getNoFallback() {
        return this.noFallback;
    }

    boolean getUsesPoolBundle() {
        return this.usesPoolBundle;
    }

    static int RES_GET_TYPE(int res) {
        return res >>> 28;
    }

    private static int RES_GET_OFFSET(int res) {
        return 268435455 & res;
    }

    private int getResourceByteOffset(int offset) {
        return offset << 2;
    }

    static int RES_GET_INT(int res) {
        return (res << 4) >> 4;
    }

    static int RES_GET_UINT(int res) {
        return 268435455 & res;
    }

    static boolean URES_IS_ARRAY(int type) {
        return type == 8 || type == 9;
    }

    static boolean URES_IS_TABLE(int type) {
        return type == 2 || type == 5 || type == 4;
    }

    private char[] getChars(int offset, int count) {
        char[] chars = new char[count];
        if (count <= 16) {
            for (int i = 0; i < count; i++) {
                chars[i] = this.bytes.getChar(offset);
                offset += 2;
            }
        } else {
            CharBuffer temp = this.bytes.asCharBuffer();
            temp.position(offset / 2);
            temp.get(chars);
        }
        return chars;
    }

    private int getInt(int offset) {
        return this.bytes.getInt(offset);
    }

    private int[] getInts(int offset, int count) {
        int[] ints = new int[count];
        if (count <= 16) {
            for (int i = 0; i < count; i++) {
                ints[i] = this.bytes.getInt(offset);
                offset += 4;
            }
        } else {
            IntBuffer temp = this.bytes.asIntBuffer();
            temp.position(offset / 4);
            temp.get(ints);
        }
        return ints;
    }

    private char[] getTable16KeyOffsets(int offset) {
        int offset2 = offset + 1;
        int length = this.b16BitUnits.charAt(offset);
        if (length <= 0) {
            return emptyChars;
        }
        char[] result = new char[length];
        if (length <= 16) {
            int i = 0;
            while (i < length) {
                offset = offset2 + 1;
                result[i] = this.b16BitUnits.charAt(offset2);
                i++;
                offset2 = offset;
            }
            offset = offset2;
        } else {
            CharBuffer temp = this.b16BitUnits.duplicate();
            temp.position(offset2);
            temp.get(result);
            offset = offset2;
        }
        return result;
    }

    private char[] getTableKeyOffsets(int offset) {
        int length = this.bytes.getChar(offset);
        if (length > 0) {
            return getChars(offset + 2, length);
        }
        return emptyChars;
    }

    private int[] getTable32KeyOffsets(int offset) {
        int length = getInt(offset);
        if (length > 0) {
            return getInts(offset + 4, length);
        }
        return emptyInts;
    }

    private static String makeKeyStringFromBytes(byte[] keyBytes, int keyOffset) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            byte b = keyBytes[keyOffset];
            if (b == (byte) 0) {
                return sb.toString();
            }
            keyOffset++;
            sb.append((char) b);
        }
    }

    private String getKey16String(int keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return makeKeyStringFromBytes(this.keyBytes, keyOffset);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
    }

    private String getKey32String(int keyOffset) {
        if (keyOffset >= 0) {
            return makeKeyStringFromBytes(this.keyBytes, keyOffset);
        }
        return makeKeyStringFromBytes(this.poolBundleReader.keyBytes, Integer.MAX_VALUE & keyOffset);
    }

    private void setKeyFromKey16(int keyOffset, Key key) {
        if (keyOffset < this.localKeyLimit) {
            key.setBytes(this.keyBytes, keyOffset);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
        }
    }

    private void setKeyFromKey32(int keyOffset, Key key) {
        if (keyOffset >= 0) {
            key.setBytes(this.keyBytes, keyOffset);
        } else {
            key.setBytes(this.poolBundleReader.keyBytes, Integer.MAX_VALUE & keyOffset);
        }
    }

    private int compareKeys(CharSequence key, char keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return ICUBinary.compareKeys(key, this.keyBytes, (int) keyOffset);
        }
        return ICUBinary.compareKeys(key, this.poolBundleReader.keyBytes, keyOffset - this.localKeyLimit);
    }

    private int compareKeys32(CharSequence key, int keyOffset) {
        if (keyOffset >= 0) {
            return ICUBinary.compareKeys(key, this.keyBytes, keyOffset);
        }
        return ICUBinary.compareKeys(key, this.poolBundleReader.keyBytes, Integer.MAX_VALUE & keyOffset);
    }

    String getStringV2(int res) {
        Object obj = 1;
        if (!f17-assertionsDisabled) {
            if ((RES_GET_TYPE(res) == 6 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int offset = RES_GET_OFFSET(res);
        if (!f17-assertionsDisabled) {
            if (offset == 0) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (String) value;
        }
        String s;
        int first = this.b16BitUnits.charAt(offset);
        if ((first & -1024) == UTF16.TRAIL_SURROGATE_MIN_VALUE) {
            int length;
            if (first < 57327) {
                length = first & Opcodes.OP_NEW_INSTANCE_JUMBO;
                offset++;
            } else if (first < 57343) {
                length = ((first - 57327) << 16) | this.b16BitUnits.charAt(offset + 1);
                offset += 2;
            } else {
                length = (this.b16BitUnits.charAt(offset + 1) << 16) | this.b16BitUnits.charAt(offset + 2);
                offset += 3;
            }
            s = this.b16BitUnits.subSequence(offset, offset + length).toString();
        } else if (first == 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) first);
            while (true) {
                offset++;
                char c = this.b16BitUnits.charAt(offset);
                if (c == 0) {
                    break;
                }
                sb.append(c);
            }
            s = sb.toString();
        }
        return (String) this.resourceCache.putIfAbsent(res, s, s.length() * 2);
    }

    private String makeStringFromBytes(int offset, int length) {
        if (length <= 16) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(this.bytes.getChar(offset));
                offset += 2;
            }
            return sb.toString();
        }
        offset /= 2;
        return this.bytes.asCharBuffer().subSequence(offset, offset + length).toString();
    }

    String getString(int res) {
        int offset = RES_GET_OFFSET(res);
        if (res != offset && RES_GET_TYPE(res) != 6) {
            return null;
        }
        if (offset == 0) {
            return "";
        }
        if (res == offset) {
            Object value = this.resourceCache.get(res);
            if (value != null) {
                return (String) value;
            }
            offset = getResourceByteOffset(offset);
            String s = makeStringFromBytes(offset + 4, getInt(offset));
            return (String) this.resourceCache.putIfAbsent(res, s, s.length() * 2);
        } else if (offset < this.poolStringIndexLimit) {
            return this.poolBundleReader.getStringV2(res);
        } else {
            return getStringV2(res - this.poolStringIndexLimit);
        }
    }

    private boolean isNoInheritanceMarker(int res) {
        boolean z = false;
        int offset = RES_GET_OFFSET(res);
        if (offset != 0) {
            if (res == offset) {
                offset = getResourceByteOffset(offset);
                if (getInt(offset) == 3 && this.bytes.getChar(offset + 4) == 8709 && this.bytes.getChar(offset + 6) == 8709 && this.bytes.getChar(offset + 8) == 8709) {
                    z = true;
                }
                return z;
            } else if (RES_GET_TYPE(res) == 6) {
                if (offset < this.poolStringIndexLimit) {
                    return this.poolBundleReader.isStringV2NoInheritanceMarker(offset);
                }
                return isStringV2NoInheritanceMarker(offset - this.poolStringIndexLimit);
            }
        }
        return false;
    }

    private boolean isStringV2NoInheritanceMarker(int offset) {
        boolean z = true;
        int first = this.b16BitUnits.charAt(offset);
        if (first == 8709) {
            if (this.b16BitUnits.charAt(offset + 1) != 8709 || this.b16BitUnits.charAt(offset + 2) != 8709) {
                z = false;
            } else if (this.b16BitUnits.charAt(offset + 3) != 0) {
                z = false;
            }
            return z;
        } else if (first != 56323) {
            return false;
        } else {
            if (this.b16BitUnits.charAt(offset + 1) != 8709 || this.b16BitUnits.charAt(offset + 2) != 8709) {
                z = false;
            } else if (this.b16BitUnits.charAt(offset + 3) != 8709) {
                z = false;
            }
            return z;
        }
    }

    String getAlias(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 3) {
            return null;
        }
        if (offset == 0) {
            return "";
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (String) value;
        }
        offset = getResourceByteOffset(offset);
        int length = getInt(offset);
        return (String) this.resourceCache.putIfAbsent(res, makeStringFromBytes(offset + 4, length), length * 2);
    }

    byte[] getBinary(int res, byte[] ba) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 1) {
            return null;
        }
        if (offset == 0) {
            return emptyBytes;
        }
        offset = getResourceByteOffset(offset);
        int length = getInt(offset);
        if (length == 0) {
            return emptyBytes;
        }
        if (ba == null || ba.length != length) {
            ba = new byte[length];
        }
        offset += 4;
        if (length <= 16) {
            int i = 0;
            int offset2 = offset;
            while (i < length) {
                offset = offset2 + 1;
                ba[i] = this.bytes.get(offset2);
                i++;
                offset2 = offset;
            }
        } else {
            ByteBuffer temp = this.bytes.duplicate();
            temp.position(offset);
            temp.get(ba);
        }
        return ba;
    }

    ByteBuffer getBinary(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 1) {
            return null;
        }
        if (offset == 0) {
            return emptyByteBuffer.duplicate();
        }
        offset = getResourceByteOffset(offset);
        int length = getInt(offset);
        if (length == 0) {
            return emptyByteBuffer.duplicate();
        }
        offset += 4;
        ByteBuffer result = this.bytes.duplicate();
        result.position(offset).limit(offset + length);
        result = ICUBinary.sliceWithOrder(result);
        if (!result.isReadOnly()) {
            result = result.asReadOnlyBuffer();
        }
        return result;
    }

    int[] getIntVector(int res) {
        int offset = RES_GET_OFFSET(res);
        if (RES_GET_TYPE(res) != 14) {
            return null;
        }
        if (offset == 0) {
            return emptyInts;
        }
        offset = getResourceByteOffset(offset);
        return getInts(offset + 4, getInt(offset));
    }

    private int getArrayLength(int res) {
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return 0;
        }
        int type = RES_GET_TYPE(res);
        if (type == 8) {
            return getInt(getResourceByteOffset(offset));
        }
        if (type == 9) {
            return this.b16BitUnits.charAt(offset);
        }
        return 0;
    }

    Array getArray(int res) {
        int type = RES_GET_TYPE(res);
        if (!URES_IS_ARRAY(type)) {
            return null;
        }
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return EMPTY_ARRAY;
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (Array) value;
        }
        return (Array) this.resourceCache.putIfAbsent(res, type == 8 ? new Array32(this, offset) : new Array16(this, offset), 0);
    }

    private int getTableLength(int res) {
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return 0;
        }
        int type = RES_GET_TYPE(res);
        if (type == 2) {
            return this.bytes.getChar(getResourceByteOffset(offset));
        } else if (type == 5) {
            return this.b16BitUnits.charAt(offset);
        } else {
            if (type == 4) {
                return getInt(getResourceByteOffset(offset));
            }
            return 0;
        }
    }

    Table getTable(int res) {
        int type = RES_GET_TYPE(res);
        if (!URES_IS_TABLE(type)) {
            return null;
        }
        int offset = RES_GET_OFFSET(res);
        if (offset == 0) {
            return EMPTY_TABLE;
        }
        Object value = this.resourceCache.get(res);
        if (value != null) {
            return (Table) value;
        }
        Table table;
        int size;
        if (type == 2) {
            table = new Table1632(this, offset);
            size = table.getSize() * 2;
        } else if (type == 5) {
            table = new Table16(this, offset);
            size = table.getSize() * 2;
        } else {
            table = new Table32(this, offset);
            size = table.getSize() * 4;
        }
        return (Table) this.resourceCache.putIfAbsent(res, table, size);
    }

    public static String getFullName(String baseName, String localeName) {
        if (baseName == null || baseName.length() == 0) {
            if (localeName.length() == 0) {
                return ULocale.getDefault().toString();
            }
            return localeName + ICU_RESOURCE_SUFFIX;
        } else if (baseName.indexOf(46) != -1) {
            baseName = baseName.replace('.', '/');
            if (localeName.length() == 0) {
                return baseName + ICU_RESOURCE_SUFFIX;
            }
            return baseName + BaseLocale.SEP + localeName + ICU_RESOURCE_SUFFIX;
        } else if (baseName.charAt(baseName.length() - 1) != '/') {
            return baseName + "/" + localeName + ICU_RESOURCE_SUFFIX;
        } else {
            return baseName + localeName + ICU_RESOURCE_SUFFIX;
        }
    }
}

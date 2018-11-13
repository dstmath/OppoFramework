package android.icu.text;

import android.icu.impl.CharTrie;
import android.icu.impl.ICUBinary;
import android.icu.impl.StringPrepDataReader;
import android.icu.impl.UBiDiProps;
import android.icu.lang.UCharacter;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

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
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
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
public final class StringPrep {
    public static final int ALLOW_UNASSIGNED = 1;
    private static final WeakReference<StringPrep>[] CACHE = null;
    private static final int CHECK_BIDI_ON = 2;
    public static final int DEFAULT = 0;
    private static final int DELETE = 3;
    private static final int FOUR_UCHARS_MAPPING_INDEX_START = 6;
    private static final int INDEX_MAPPING_DATA_SIZE = 1;
    private static final int INDEX_TOP = 16;
    private static final int MAP = 1;
    private static final int MAX_INDEX_VALUE = 16319;
    private static final int MAX_PROFILE = 13;
    private static final int NORMALIZATION_ON = 1;
    private static final int NORM_CORRECTNS_LAST_UNI_VERSION = 2;
    private static final int ONE_UCHAR_MAPPING_INDEX_START = 3;
    private static final int OPTIONS = 7;
    private static final String[] PROFILE_NAMES = null;
    private static final int PROHIBITED = 2;
    public static final int RFC3491_NAMEPREP = 0;
    public static final int RFC3530_NFS4_CIS_PREP = 3;
    public static final int RFC3530_NFS4_CS_PREP = 1;
    public static final int RFC3530_NFS4_CS_PREP_CI = 2;
    public static final int RFC3530_NFS4_MIXED_PREP_PREFIX = 4;
    public static final int RFC3530_NFS4_MIXED_PREP_SUFFIX = 5;
    public static final int RFC3722_ISCSI = 6;
    public static final int RFC3920_NODEPREP = 7;
    public static final int RFC3920_RESOURCEPREP = 8;
    public static final int RFC4011_MIB = 9;
    public static final int RFC4013_SASLPREP = 10;
    public static final int RFC4505_TRACE = 11;
    public static final int RFC4518_LDAP = 12;
    public static final int RFC4518_LDAP_CI = 13;
    private static final int THREE_UCHARS_MAPPING_INDEX_START = 5;
    private static final int TWO_UCHARS_MAPPING_INDEX_START = 4;
    private static final int TYPE_LIMIT = 4;
    private static final int TYPE_THRESHOLD = 65520;
    private static final int UNASSIGNED = 0;
    private UBiDiProps bdp;
    private boolean checkBiDi;
    private boolean doNFKC;
    private int[] indexes;
    private char[] mappingData;
    private VersionInfo normCorrVer;
    private CharTrie sprepTrie;
    private VersionInfo sprepUniVer;

    private static final class Values {
        boolean isIndex;
        int type;
        int value;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.StringPrep.Values.<init>():void, dex: 
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
        private Values() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.StringPrep.Values.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrep.Values.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.StringPrep.Values.<init>(android.icu.text.StringPrep$Values):void, dex: 
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
        /* synthetic */ Values(android.icu.text.StringPrep.Values r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.StringPrep.Values.<init>(android.icu.text.StringPrep$Values):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrep.Values.<init>(android.icu.text.StringPrep$Values):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.icu.text.StringPrep.Values.reset():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void reset() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.icu.text.StringPrep.Values.reset():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrep.Values.reset():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.StringPrep.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.StringPrep.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrep.<clinit>():void");
    }

    private char getCodePointValue(int ch) {
        return this.sprepTrie.getCodePointValue(ch);
    }

    private static VersionInfo getVersionInfo(int comp) {
        return VersionInfo.getInstance((comp >> 24) & 255, (comp >> 16) & 255, (comp >> 8) & 255, comp & 255);
    }

    private static VersionInfo getVersionInfo(byte[] version) {
        if (version.length != 4) {
            return null;
        }
        return VersionInfo.getInstance(version[0], version[1], version[2], version[3]);
    }

    public StringPrep(InputStream inputStream) throws IOException {
        this(ICUBinary.getByteBufferFromInputStreamAndCloseStream(inputStream));
    }

    private StringPrep(ByteBuffer bytes) throws IOException {
        boolean z;
        boolean z2 = true;
        StringPrepDataReader reader = new StringPrepDataReader(bytes);
        this.indexes = reader.readIndexes(16);
        this.sprepTrie = new CharTrie(bytes, null);
        this.mappingData = reader.read(this.indexes[1] / 2);
        reader.getDataFormatVersion();
        if ((this.indexes[7] & 1) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.doNFKC = z;
        if ((this.indexes[7] & 2) <= 0) {
            z2 = false;
        }
        this.checkBiDi = z2;
        this.sprepUniVer = getVersionInfo(reader.getUnicodeVersion());
        this.normCorrVer = getVersionInfo(this.indexes[2]);
        VersionInfo normUniVer = UCharacter.getUnicodeVersion();
        if (normUniVer.compareTo(this.sprepUniVer) < 0 && normUniVer.compareTo(this.normCorrVer) < 0 && (this.indexes[7] & 1) > 0) {
            throw new IOException("Normalization Correction version not supported");
        } else if (this.checkBiDi) {
            this.bdp = UBiDiProps.INSTANCE;
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0053, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static StringPrep getInstance(int profile) {
        Throwable th;
        if (profile < 0 || profile > 13) {
            throw new IllegalArgumentException("Bad profile type");
        }
        synchronized (CACHE) {
            try {
                StringPrep instance;
                WeakReference<StringPrep> ref = CACHE[profile];
                if (ref != null) {
                    instance = (StringPrep) ref.get();
                } else {
                    instance = null;
                }
                StringPrep instance2;
                if (instance == null) {
                    try {
                        ByteBuffer bytes = ICUBinary.getRequiredData(PROFILE_NAMES[profile] + ".spp");
                        if (bytes != null) {
                            instance2 = new StringPrep(bytes);
                        } else {
                            instance2 = instance;
                        }
                        if (instance2 != null) {
                            CACHE[profile] = new WeakReference(instance2);
                        }
                    } catch (Throwable e) {
                        throw new ICUUncheckedIOException(e);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } else {
                    instance2 = instance;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private static final void getValues(char trieWord, Values values) {
        values.reset();
        if (trieWord == 0) {
            values.type = 4;
        } else if (trieWord >= 65520) {
            values.type = trieWord - TYPE_THRESHOLD;
        } else {
            values.type = 1;
            if ((trieWord & 2) > 0) {
                values.isIndex = true;
                values.value = trieWord >> 2;
            } else {
                values.isIndex = false;
                values.value = (trieWord << 16) >> 16;
                values.value >>= 2;
            }
            if ((trieWord >> 2) == MAX_INDEX_VALUE) {
                values.type = 3;
                values.isIndex = false;
                values.value = 0;
            }
        }
    }

    private StringBuffer map(UCharacterIterator iter, int options) throws StringPrepParseException {
        Values val = new Values();
        StringBuffer dest = new StringBuffer();
        boolean allowUnassigned = (options & 1) > 0;
        while (true) {
            int ch = iter.nextCodePoint();
            if (ch == -1) {
                return dest;
            }
            getValues(getCodePointValue(ch), val);
            if (val.type != 0 || allowUnassigned) {
                if (val.type == 1) {
                    if (val.isIndex) {
                        int length;
                        int index = val.value;
                        if (index >= this.indexes[3] && index < this.indexes[4]) {
                            length = 1;
                        } else if (index >= this.indexes[4] && index < this.indexes[5]) {
                            length = 2;
                        } else if (index < this.indexes[5] || index >= this.indexes[6]) {
                            int index2 = index + 1;
                            length = this.mappingData[index];
                            index = index2;
                        } else {
                            length = 3;
                        }
                        dest.append(this.mappingData, index, length);
                    } else {
                        ch -= val.value;
                    }
                } else if (val.type == 3) {
                }
                UTF16.append(dest, ch);
            } else {
                throw new StringPrepParseException("An unassigned code point was found in the input", 3, iter.getText(), iter.getIndex());
            }
        }
    }

    private StringBuffer normalize(StringBuffer src) {
        return new StringBuffer(Normalizer.normalize(src.toString(), Normalizer.NFKC, 32));
    }

    public StringBuffer prepare(UCharacterIterator src, int options) throws StringPrepParseException {
        StringBuffer mapOut = map(src, options);
        StringBuffer normOut = mapOut;
        if (this.doNFKC) {
            normOut = normalize(mapOut);
        }
        UCharacterIterator iter = UCharacterIterator.getInstance(normOut);
        Values val = new Values();
        int direction = 23;
        int firstCharDir = 23;
        int rtlPos = -1;
        int ltrPos = -1;
        boolean rightToLeft = false;
        boolean leftToRight = false;
        while (true) {
            int ch = iter.nextCodePoint();
            if (ch != -1) {
                getValues(getCodePointValue(ch), val);
                if (val.type == 2) {
                    throw new StringPrepParseException("A prohibited code point was found in the input", 2, iter.getText(), val.value);
                } else if (this.checkBiDi) {
                    direction = this.bdp.getClass(ch);
                    if (firstCharDir == 23) {
                        firstCharDir = direction;
                    }
                    if (direction == 0) {
                        leftToRight = true;
                        ltrPos = iter.getIndex() - 1;
                    }
                    if (direction == 1 || direction == 13) {
                        rightToLeft = true;
                        rtlPos = iter.getIndex() - 1;
                    }
                }
            } else {
                if (this.checkBiDi) {
                    String str;
                    String text;
                    if (leftToRight && rightToLeft) {
                        str = "The input does not conform to the rules for BiDi code points.";
                        text = iter.getText();
                        if (rtlPos <= ltrPos) {
                            rtlPos = ltrPos;
                        }
                        throw new StringPrepParseException(str, 4, text, rtlPos);
                    } else if (rightToLeft && !((firstCharDir == 1 || firstCharDir == 13) && (direction == 1 || direction == 13))) {
                        str = "The input does not conform to the rules for BiDi code points.";
                        text = iter.getText();
                        if (rtlPos <= ltrPos) {
                            rtlPos = ltrPos;
                        }
                        throw new StringPrepParseException(str, 4, text, rtlPos);
                    }
                }
                return normOut;
            }
        }
    }

    public String prepare(String src, int options) throws StringPrepParseException {
        return prepare(UCharacterIterator.getInstance(src), options).toString();
    }
}

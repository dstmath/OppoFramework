package android.icu.impl.coll;

import dalvik.bytecode.Opcodes;

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
public final class CollationFastLatin {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f42-assertionsDisabled = false;
    static final int BAIL_OUT = 1;
    public static final int BAIL_OUT_RESULT = -2;
    static final int CASE_AND_TERTIARY_MASK = 31;
    static final int CASE_MASK = 24;
    static final int COMMON_SEC = 160;
    static final int COMMON_SEC_PLUS_OFFSET = 192;
    static final int COMMON_TER = 0;
    static final int COMMON_TER_PLUS_OFFSET = 32;
    static final int CONTRACTION = 1024;
    static final int CONTR_CHAR_MASK = 511;
    static final int CONTR_LENGTH_SHIFT = 9;
    static final int EOS = 2;
    static final int EXPANSION = 2048;
    static final int INDEX_MASK = 1023;
    public static final int LATIN_LIMIT = 384;
    public static final int LATIN_MAX = 383;
    static final int LATIN_MAX_UTF8_LEAD = 197;
    static final int LONG_INC = 8;
    static final int LONG_PRIMARY_MASK = 65528;
    static final int LOWER_CASE = 8;
    static final int MAX_LONG = 4088;
    static final int MAX_SEC_AFTER = 352;
    static final int MAX_SEC_BEFORE = 128;
    static final int MAX_SEC_HIGH = 992;
    static final int MAX_SHORT = 64512;
    static final int MAX_TER_AFTER = 7;
    static final int MERGE_WEIGHT = 3;
    static final int MIN_LONG = 3072;
    static final int MIN_SEC_AFTER = 192;
    static final int MIN_SEC_BEFORE = 0;
    static final int MIN_SEC_HIGH = 384;
    static final int MIN_SHORT = 4096;
    static final int NUM_FAST_CHARS = 448;
    static final int PUNCT_LIMIT = 8256;
    static final int PUNCT_START = 8192;
    static final int SECONDARY_MASK = 992;
    static final int SEC_INC = 32;
    static final int SEC_OFFSET = 32;
    static final int SHORT_INC = 1024;
    static final int SHORT_PRIMARY_MASK = 64512;
    static final int TERTIARY_MASK = 7;
    static final int TER_OFFSET = 32;
    static final int TWO_CASES_MASK = 1572888;
    static final int TWO_COMMON_SEC_PLUS_OFFSET = 12583104;
    static final int TWO_COMMON_TER_PLUS_OFFSET = 2097184;
    static final int TWO_LONG_PRIMARIES_MASK = -458760;
    static final int TWO_LOWER_CASES = 524296;
    static final int TWO_SECONDARIES_MASK = 65012704;
    static final int TWO_SEC_OFFSETS = 2097184;
    static final int TWO_SHORT_PRIMARIES_MASK = -67044352;
    static final int TWO_TERTIARIES_MASK = 458759;
    static final int TWO_TER_OFFSETS = 2097184;
    public static final int VERSION = 2;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationFastLatin.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationFastLatin.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationFastLatin.<clinit>():void");
    }

    static int getCharIndex(char c) {
        if (c <= 383) {
            return c;
        }
        if (8192 > c || c >= 8256) {
            return -1;
        }
        return c - 7808;
    }

    public static int getOptions(CollationData data, CollationSettings settings, char[] primaries) {
        char[] header = data.fastLatinTableHeader;
        if (header == null) {
            return -1;
        }
        if (!f42-assertionsDisabled) {
            if (((header[0] >> 8) == 2 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!f42-assertionsDisabled) {
            if ((primaries.length == 384 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (primaries.length != 384) {
            return -1;
        }
        int miniVarTop;
        int c;
        if ((settings.options & 12) == 0) {
            miniVarTop = Opcodes.OP_IGET_CHAR_JUMBO;
        } else {
            int i = settings.getMaxVariable() + 1;
            if (i >= (header[0] & 255)) {
                return -1;
            }
            miniVarTop = header[i];
        }
        boolean digitsAreReordered = false;
        if (settings.hasReordering()) {
            long prevStart = 0;
            long beforeDigitStart = 0;
            long digitStart = 0;
            long afterDigitStart = 0;
            for (int group = 4096; group < 4104; group++) {
                long start = settings.reorder(data.getFirstPrimaryForGroup(group));
                if (group == 4100) {
                    beforeDigitStart = prevStart;
                    digitStart = start;
                } else if (start == 0) {
                    continue;
                } else if (start < prevStart) {
                    return -1;
                } else {
                    if (digitStart != 0 && afterDigitStart == 0 && prevStart == beforeDigitStart) {
                        afterDigitStart = start;
                    }
                    prevStart = start;
                }
            }
            long latinStart = settings.reorder(data.getFirstPrimaryForGroup(25));
            if (latinStart < prevStart) {
                return -1;
            }
            if (afterDigitStart == 0) {
                afterDigitStart = latinStart;
            }
            if (beforeDigitStart >= digitStart || digitStart >= afterDigitStart) {
                digitsAreReordered = true;
            }
        }
        char[] table = data.fastLatinTable;
        for (c = 0; c < 384; c++) {
            int p = table[c];
            if (p >= 4096) {
                p &= 64512;
            } else if (p > miniVarTop) {
                p &= LONG_PRIMARY_MASK;
            } else {
                p = 0;
            }
            primaries[c] = (char) p;
        }
        if (digitsAreReordered || (settings.options & 2) != 0) {
            for (c = 48; c <= 57; c++) {
                primaries[c] = 0;
            }
        }
        return (miniVarTop << 16) | settings.options;
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:210:0x039e  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x03ed  */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x0499  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x03fd  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x04eb  */
    /* JADX WARNING: Removed duplicated region for block: B:326:0x0587  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x04fb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int compareUTF16(char[] table, char[] primaries, int options, CharSequence left, CharSequence right, int startIndex) {
        int rightIndex;
        int leftIndex;
        int c;
        long pairAndInc;
        int variableTop = options >> 16;
        options &= 65535;
        int rightIndex2 = startIndex;
        int leftPair = 0;
        int rightPair = 0;
        int leftIndex2 = startIndex;
        while (true) {
            if (leftPair != 0) {
                rightIndex = rightIndex2;
                leftIndex = leftIndex2;
            } else if (leftIndex2 == left.length()) {
                leftPair = 2;
                rightIndex = rightIndex2;
                leftIndex = leftIndex2;
            } else {
                leftIndex = leftIndex2 + 1;
                c = left.charAt(leftIndex2);
                if (c <= 383) {
                    leftPair = primaries[c];
                    if (leftPair != 0) {
                        rightIndex = rightIndex2;
                    } else if (c <= 57 && c >= 48 && (options & 2) != 0) {
                        return -2;
                    } else {
                        leftPair = table[c];
                    }
                } else if (8192 > c || c >= PUNCT_LIMIT) {
                    leftPair = lookup(table, c);
                } else {
                    leftPair = table[(c - 8192) + 384];
                }
                if (leftPair >= 4096) {
                    leftPair &= 64512;
                    rightIndex = rightIndex2;
                } else if (leftPair > variableTop) {
                    leftPair &= LONG_PRIMARY_MASK;
                    rightIndex = rightIndex2;
                } else {
                    pairAndInc = nextPair(table, c, leftPair, left, leftIndex);
                    if (pairAndInc < 0) {
                        leftIndex++;
                        pairAndInc = ~pairAndInc;
                    }
                    leftPair = (int) pairAndInc;
                    if (leftPair == 1) {
                        return -2;
                    }
                    leftPair = getPrimaries(variableTop, leftPair);
                    leftIndex2 = leftIndex;
                }
            }
            while (rightPair == 0) {
                if (rightIndex == right.length()) {
                    rightPair = 2;
                    rightIndex2 = rightIndex;
                    break;
                }
                rightIndex2 = rightIndex + 1;
                c = right.charAt(rightIndex);
                if (c <= 383) {
                    rightPair = primaries[c];
                    if (rightPair != 0) {
                        break;
                    } else if (c <= 57 && c >= 48 && (options & 2) != 0) {
                        return -2;
                    } else {
                        rightPair = table[c];
                    }
                } else if (8192 > c || c >= PUNCT_LIMIT) {
                    rightPair = lookup(table, c);
                } else {
                    rightPair = table[(c - 8192) + 384];
                }
                if (rightPair >= 4096) {
                    rightPair &= 64512;
                    break;
                } else if (rightPair > variableTop) {
                    rightPair &= LONG_PRIMARY_MASK;
                    break;
                } else {
                    pairAndInc = nextPair(table, c, rightPair, right, rightIndex2);
                    if (pairAndInc < 0) {
                        rightIndex2++;
                        pairAndInc = ~pairAndInc;
                    }
                    rightPair = (int) pairAndInc;
                    if (rightPair == 1) {
                        return -2;
                    }
                    rightPair = getPrimaries(variableTop, rightPair);
                    rightIndex = rightIndex2;
                }
            }
            rightIndex2 = rightIndex;
            if (leftPair != rightPair) {
                int leftPrimary = leftPair & 65535;
                int rightPrimary = rightPair & 65535;
                if (leftPrimary == rightPrimary) {
                    if (leftPair == 2) {
                        break;
                    }
                    leftPair >>>= 16;
                    rightPair >>>= 16;
                    leftIndex2 = leftIndex;
                } else {
                    return leftPrimary < rightPrimary ? -1 : 1;
                }
            } else if (leftPair == 2) {
                break;
            } else {
                rightPair = 0;
                leftPair = 0;
                leftIndex2 = leftIndex;
            }
        }
        if (CollationSettings.getStrength(options) >= 1) {
            rightIndex2 = startIndex;
            rightPair = 0;
            leftPair = 0;
            leftIndex2 = startIndex;
            while (true) {
                if (leftPair != 0) {
                    rightIndex = rightIndex2;
                    leftIndex = leftIndex2;
                } else if (leftIndex2 == left.length()) {
                    leftPair = 2;
                    rightIndex = rightIndex2;
                    leftIndex = leftIndex2;
                } else {
                    leftIndex = leftIndex2 + 1;
                    c = left.charAt(leftIndex2);
                    if (c <= 383) {
                        leftPair = table[c];
                    } else if (8192 > c || c >= PUNCT_LIMIT) {
                        leftPair = lookup(table, c);
                    } else {
                        leftPair = table[(c - 8192) + 384];
                    }
                    if (leftPair >= 4096) {
                        leftPair = getSecondariesFromOneShortCE(leftPair);
                        rightIndex = rightIndex2;
                    } else if (leftPair > variableTop) {
                        leftPair = 192;
                        rightIndex = rightIndex2;
                    } else {
                        pairAndInc = nextPair(table, c, leftPair, left, leftIndex);
                        if (pairAndInc < 0) {
                            leftIndex++;
                            pairAndInc = ~pairAndInc;
                        }
                        leftPair = getSecondaries(variableTop, (int) pairAndInc);
                        leftIndex2 = leftIndex;
                    }
                }
                while (rightPair == 0) {
                    if (rightIndex == right.length()) {
                        rightPair = 2;
                        rightIndex2 = rightIndex;
                        break;
                    }
                    rightIndex2 = rightIndex + 1;
                    c = right.charAt(rightIndex);
                    if (c <= 383) {
                        rightPair = table[c];
                    } else if (8192 > c || c >= PUNCT_LIMIT) {
                        rightPair = lookup(table, c);
                    } else {
                        rightPair = table[(c - 8192) + 384];
                    }
                    if (rightPair >= 4096) {
                        rightPair = getSecondariesFromOneShortCE(rightPair);
                        break;
                    } else if (rightPair > variableTop) {
                        rightPair = 192;
                        break;
                    } else {
                        pairAndInc = nextPair(table, c, rightPair, right, rightIndex2);
                        if (pairAndInc < 0) {
                            rightIndex2++;
                            pairAndInc = ~pairAndInc;
                        }
                        rightPair = getSecondaries(variableTop, (int) pairAndInc);
                        rightIndex = rightIndex2;
                    }
                }
                rightIndex2 = rightIndex;
                if (leftPair != rightPair) {
                    int leftSecondary = leftPair & 65535;
                    int rightSecondary = rightPair & 65535;
                    if (leftSecondary == rightSecondary) {
                        if (leftPair == 2) {
                            break;
                        }
                        leftPair >>>= 16;
                        rightPair >>>= 16;
                        leftIndex2 = leftIndex;
                    } else if ((options & 2048) != 0) {
                        return -2;
                    } else {
                        return leftSecondary < rightSecondary ? -1 : 1;
                    }
                } else if (leftPair == 2) {
                    break;
                } else {
                    rightPair = 0;
                    leftPair = 0;
                    leftIndex2 = leftIndex;
                }
            }
        }
        if ((options & 1024) != 0) {
            boolean strengthIsPrimary = CollationSettings.getStrength(options) == 0;
            rightIndex2 = startIndex;
            rightPair = 0;
            leftPair = 0;
            leftIndex2 = startIndex;
            while (true) {
                if (leftPair == 0) {
                    if (leftIndex2 == left.length()) {
                        leftPair = 2;
                        rightIndex = rightIndex2;
                        if (rightPair == 0) {
                            if (rightIndex == right.length()) {
                                rightPair = 2;
                            } else {
                                rightIndex2 = rightIndex + 1;
                                c = right.charAt(rightIndex);
                                if (c <= 383) {
                                    rightPair = table[c];
                                } else {
                                    rightPair = lookup(table, c);
                                }
                                if (rightPair < MIN_LONG) {
                                    pairAndInc = nextPair(table, c, rightPair, right, rightIndex2);
                                    if (pairAndInc < 0) {
                                        rightIndex2++;
                                        pairAndInc = ~pairAndInc;
                                    }
                                    rightPair = (int) pairAndInc;
                                }
                                rightPair = getCases(variableTop, strengthIsPrimary, rightPair);
                            }
                            rightPair = 2;
                        }
                        if (leftPair == rightPair) {
                            int leftCase = leftPair & 65535;
                            int rightCase = rightPair & 65535;
                            if (leftCase != rightCase) {
                                if ((options & 256) == 0) {
                                    return leftCase < rightCase ? -1 : 1;
                                }
                                return leftCase < rightCase ? 1 : -1;
                            } else if (leftPair == 2) {
                                rightIndex2 = rightIndex;
                                leftIndex = leftIndex2;
                                break;
                            } else {
                                leftPair >>>= 16;
                                rightPair >>>= 16;
                            }
                        } else if (leftPair == 2) {
                            rightIndex2 = rightIndex;
                            leftIndex = leftIndex2;
                            break;
                        } else {
                            rightPair = 0;
                            leftPair = 0;
                        }
                        rightIndex2 = rightIndex;
                    } else {
                        leftIndex = leftIndex2 + 1;
                        c = left.charAt(leftIndex2);
                        if (c <= 383) {
                            leftPair = table[c];
                        } else {
                            leftPair = lookup(table, c);
                        }
                        if (leftPair < MIN_LONG) {
                            pairAndInc = nextPair(table, c, leftPair, left, leftIndex);
                            if (pairAndInc < 0) {
                                leftIndex++;
                                pairAndInc = ~pairAndInc;
                            }
                            leftPair = (int) pairAndInc;
                        }
                        leftPair = getCases(variableTop, strengthIsPrimary, leftPair);
                        leftIndex2 = leftIndex;
                    }
                }
                rightIndex = rightIndex2;
                if (rightPair == 0) {
                }
                if (leftPair == rightPair) {
                }
                rightIndex2 = rightIndex;
            }
        }
        if (CollationSettings.getStrength(options) <= 1) {
            return 0;
        }
        boolean withCaseBits = CollationSettings.isTertiaryWithCaseBits(options);
        rightIndex2 = startIndex;
        rightPair = 0;
        leftPair = 0;
        leftIndex2 = startIndex;
        while (true) {
            if (leftPair == 0) {
                if (leftIndex2 == left.length()) {
                    leftPair = 2;
                    rightIndex = rightIndex2;
                    if (rightPair == 0) {
                        if (rightIndex == right.length()) {
                            rightPair = 2;
                        } else {
                            rightIndex2 = rightIndex + 1;
                            c = right.charAt(rightIndex);
                            if (c <= 383) {
                                rightPair = table[c];
                            } else {
                                rightPair = lookup(table, c);
                            }
                            if (rightPair < MIN_LONG) {
                                pairAndInc = nextPair(table, c, rightPair, right, rightIndex2);
                                if (pairAndInc < 0) {
                                    rightIndex2++;
                                    pairAndInc = ~pairAndInc;
                                }
                                rightPair = (int) pairAndInc;
                            }
                            rightPair = getTertiaries(variableTop, withCaseBits, rightPair);
                        }
                        rightPair = 2;
                    }
                    if (leftPair == rightPair) {
                        int leftTertiary = leftPair & 65535;
                        int rightTertiary = rightPair & 65535;
                        if (leftTertiary == rightTertiary) {
                            if (leftPair == 2) {
                                break;
                            }
                            leftPair >>>= 16;
                            rightPair >>>= 16;
                            rightIndex2 = rightIndex;
                        } else {
                            if (CollationSettings.sortsTertiaryUpperCaseFirst(options)) {
                                if (leftTertiary > 3) {
                                    leftTertiary ^= 24;
                                }
                                if (rightTertiary > 3) {
                                    rightTertiary ^= 24;
                                }
                            }
                            return leftTertiary < rightTertiary ? -1 : 1;
                        }
                    } else if (leftPair == 2) {
                        break;
                    } else {
                        rightPair = 0;
                        leftPair = 0;
                        rightIndex2 = rightIndex;
                    }
                } else {
                    leftIndex = leftIndex2 + 1;
                    c = left.charAt(leftIndex2);
                    if (c <= 383) {
                        leftPair = table[c];
                    } else {
                        leftPair = lookup(table, c);
                    }
                    if (leftPair < MIN_LONG) {
                        pairAndInc = nextPair(table, c, leftPair, left, leftIndex);
                        if (pairAndInc < 0) {
                            leftIndex++;
                            pairAndInc = ~pairAndInc;
                        }
                        leftPair = (int) pairAndInc;
                    }
                    leftPair = getTertiaries(variableTop, withCaseBits, leftPair);
                    leftIndex2 = leftIndex;
                }
            }
            rightIndex = rightIndex2;
            if (rightPair == 0) {
            }
            if (leftPair == rightPair) {
            }
        }
        if (CollationSettings.getStrength(options) <= 2) {
            return 0;
        }
        rightIndex2 = startIndex;
        rightPair = 0;
        leftPair = 0;
        leftIndex2 = startIndex;
        while (true) {
            if (leftPair == 0) {
                if (leftIndex2 == left.length()) {
                    leftPair = 2;
                    rightIndex = rightIndex2;
                    if (rightPair == 0) {
                        if (rightIndex == right.length()) {
                            rightPair = 2;
                        } else {
                            rightIndex2 = rightIndex + 1;
                            c = right.charAt(rightIndex);
                            if (c <= 383) {
                                rightPair = table[c];
                            } else {
                                rightPair = lookup(table, c);
                            }
                            if (rightPair < MIN_LONG) {
                                pairAndInc = nextPair(table, c, rightPair, right, rightIndex2);
                                if (pairAndInc < 0) {
                                    rightIndex2++;
                                    pairAndInc = ~pairAndInc;
                                }
                                rightPair = (int) pairAndInc;
                            }
                            rightPair = getQuaternaries(variableTop, rightPair);
                        }
                        rightPair = 2;
                    }
                    if (leftPair == rightPair) {
                        int leftQuaternary = leftPair & 65535;
                        int rightQuaternary = rightPair & 65535;
                        if (leftQuaternary == rightQuaternary) {
                            if (leftPair == 2) {
                                break;
                            }
                            leftPair >>>= 16;
                            rightPair >>>= 16;
                            rightIndex2 = rightIndex;
                        } else {
                            return leftQuaternary < rightQuaternary ? -1 : 1;
                        }
                    } else if (leftPair == 2) {
                        break;
                    } else {
                        rightPair = 0;
                        leftPair = 0;
                        rightIndex2 = rightIndex;
                    }
                } else {
                    leftIndex = leftIndex2 + 1;
                    c = left.charAt(leftIndex2);
                    if (c <= 383) {
                        leftPair = table[c];
                    } else {
                        leftPair = lookup(table, c);
                    }
                    if (leftPair < MIN_LONG) {
                        pairAndInc = nextPair(table, c, leftPair, left, leftIndex);
                        if (pairAndInc < 0) {
                            leftIndex++;
                            pairAndInc = ~pairAndInc;
                        }
                        leftPair = (int) pairAndInc;
                    }
                    leftPair = getQuaternaries(variableTop, leftPair);
                    leftIndex2 = leftIndex;
                }
            }
            rightIndex = rightIndex2;
            if (rightPair == 0) {
            }
            if (leftPair == rightPair) {
            }
        }
        return 0;
    }

    private static int lookup(char[] table, int c) {
        if (!f42-assertionsDisabled) {
            if ((c > LATIN_MAX ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (8192 <= c && c < PUNCT_LIMIT) {
            return table[(c - 8192) + 384];
        }
        if (c == 65534) {
            return 3;
        }
        if (c == 65535) {
            return 64680;
        }
        return 1;
    }

    private static long nextPair(char[] table, int c, int ce, CharSequence s16, int sIndex) {
        if (ce >= MIN_LONG || ce < 1024) {
            return (long) ce;
        }
        int index;
        if (ce >= 2048) {
            index = (ce & 1023) + NUM_FAST_CHARS;
            return (((long) table[index + 1]) << 16) | ((long) table[index]);
        }
        index = (ce & 1023) + NUM_FAST_CHARS;
        boolean inc = false;
        if (sIndex != s16.length()) {
            int x;
            int i = sIndex;
            i = sIndex + 1;
            int c2 = s16.charAt(sIndex);
            if (c2 > LATIN_MAX) {
                if (8192 <= c2 && c2 < PUNCT_LIMIT) {
                    c2 = (c2 - 8192) + 384;
                } else if (c2 != 65534 && c2 != 65535) {
                    return 1;
                } else {
                    c2 = -1;
                }
            }
            int i2 = index;
            int head = table[index];
            do {
                i2 += head >> 9;
                head = table[i2];
                x = head & 511;
            } while (x < c2);
            if (x == c2) {
                index = i2;
                inc = true;
            }
        }
        int length = table[index] >> 9;
        if (length == 1) {
            return 1;
        }
        long result;
        ce = table[index + 1];
        if (length == 2) {
            result = (long) ce;
        } else {
            result = (((long) table[index + 2]) << 16) | ((long) ce);
        }
        if (inc) {
            result = ~result;
        }
        return result;
    }

    private static int getPrimaries(int variableTop, int pair) {
        int ce = pair & 65535;
        if (ce >= 4096) {
            return TWO_SHORT_PRIMARIES_MASK & pair;
        }
        if (ce > variableTop) {
            return TWO_LONG_PRIMARIES_MASK & pair;
        }
        if (ce >= MIN_LONG) {
            return 0;
        }
        return pair;
    }

    private static int getSecondariesFromOneShortCE(int ce) {
        ce &= 992;
        if (ce < 384) {
            return ce + 32;
        }
        return ((ce + 32) << 16) | 192;
    }

    private static int getSecondaries(int variableTop, int pair) {
        if (pair > 65535) {
            int ce = pair & 65535;
            if (ce >= 4096) {
                return (TWO_SECONDARIES_MASK & pair) + 2097184;
            }
            if (ce > variableTop) {
                return TWO_COMMON_SEC_PLUS_OFFSET;
            }
            if (!f42-assertionsDisabled) {
                if ((ce >= MIN_LONG ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            return 0;
        } else if (pair >= 4096) {
            return getSecondariesFromOneShortCE(pair);
        } else {
            if (pair > variableTop) {
                return 192;
            }
            if (pair >= MIN_LONG) {
                return 0;
            }
            return pair;
        }
    }

    private static int getCases(int variableTop, boolean strengthIsPrimary, int pair) {
        Object obj = null;
        int ce;
        if (pair > 65535) {
            ce = pair & 65535;
            if (ce >= 4096) {
                if (strengthIsPrimary && (-67108864 & pair) == 0) {
                    return pair & 24;
                }
                return pair & TWO_CASES_MASK;
            } else if (ce > variableTop) {
                return TWO_LOWER_CASES;
            } else {
                if (!f42-assertionsDisabled) {
                    if (ce >= MIN_LONG) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                return 0;
            }
        } else if (pair >= 4096) {
            ce = pair;
            pair &= 24;
            return (strengthIsPrimary || (ce & 992) < 384) ? pair : pair | 524288;
        } else if (pair > variableTop) {
            return 8;
        } else {
            if (pair >= MIN_LONG) {
                return 0;
            }
            return pair;
        }
    }

    private static int getTertiaries(int variableTop, boolean withCaseBits, int pair) {
        int ce;
        if (pair > 65535) {
            ce = pair & 65535;
            if (ce >= 4096) {
                if (withCaseBits) {
                    pair &= 2031647;
                } else {
                    pair &= TWO_TERTIARIES_MASK;
                }
                return pair + 2097184;
            } else if (ce > variableTop) {
                pair = (pair & TWO_TERTIARIES_MASK) + 2097184;
                if (withCaseBits) {
                    return pair | TWO_LOWER_CASES;
                }
                return pair;
            } else {
                if (!f42-assertionsDisabled) {
                    if ((ce >= MIN_LONG ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                return 0;
            }
        } else if (pair >= 4096) {
            ce = pair;
            if (withCaseBits) {
                pair = (pair & 31) + 32;
                return (ce & 992) >= 384 ? pair | 2621440 : pair;
            } else {
                pair = (pair & 7) + 32;
                if ((ce & 992) >= 384) {
                    return pair | 2097152;
                }
                return pair;
            }
        } else if (pair > variableTop) {
            pair = (pair & 7) + 32;
            if (withCaseBits) {
                return pair | 8;
            }
            return pair;
        } else if (pair >= MIN_LONG) {
            return 0;
        } else {
            return pair;
        }
    }

    private static int getQuaternaries(int variableTop, int pair) {
        if (pair > 65535) {
            int ce = pair & 65535;
            if (ce > variableTop) {
                return TWO_SHORT_PRIMARIES_MASK;
            }
            if (!f42-assertionsDisabled) {
                if ((ce >= MIN_LONG ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            return pair & TWO_LONG_PRIMARIES_MASK;
        } else if (pair >= 4096) {
            if ((pair & 992) >= 384) {
                return TWO_SHORT_PRIMARIES_MASK;
            }
            return 64512;
        } else if (pair > variableTop) {
            return 64512;
        } else {
            if (pair >= MIN_LONG) {
                return pair & LONG_PRIMARY_MASK;
            }
            return pair;
        }
    }

    private CollationFastLatin() {
    }
}

package java.util;

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
class ComparableTimSort {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f68-assertionsDisabled = false;
    private static final int INITIAL_TMP_STORAGE_LENGTH = 256;
    private static final int MIN_GALLOP = 7;
    private static final int MIN_MERGE = 32;
    private final Object[] a;
    private int minGallop;
    private final int[] runBase;
    private final int[] runLen;
    private int stackSize;
    private Object[] tmp;
    private int tmpBase;
    private int tmpLen;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.ComparableTimSort.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.ComparableTimSort.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.ComparableTimSort.<clinit>():void");
    }

    private ComparableTimSort(Object[] a, Object[] work, int workBase, int workLen) {
        this.minGallop = 7;
        this.stackSize = 0;
        this.a = a;
        int len = a.length;
        int tlen = len < 512 ? len >>> 1 : 256;
        if (work == null || workLen < tlen || workBase + tlen > work.length) {
            this.tmp = new Object[tlen];
            this.tmpBase = 0;
            this.tmpLen = tlen;
        } else {
            this.tmp = work;
            this.tmpBase = workBase;
            this.tmpLen = workLen;
        }
        int stackLen = len < 120 ? 5 : len < 1542 ? 10 : len < 119151 ? 24 : 40;
        this.runBase = new int[stackLen];
        this.runLen = new int[stackLen];
    }

    static void sort(Object[] a, int lo, int hi, Object[] work, int workBase, int workLen) {
        Object obj;
        Object obj2 = 1;
        if (!f68-assertionsDisabled) {
            obj = (a == null || lo < 0 || lo > hi || hi > a.length) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int nRemaining = hi - lo;
        if (nRemaining >= 2) {
            if (nRemaining < 32) {
                binarySort(a, lo, hi, lo + countRunAndMakeAscending(a, lo, hi));
                return;
            }
            ComparableTimSort ts = new ComparableTimSort(a, work, workBase, workLen);
            int minRun = minRunLength(nRemaining);
            do {
                int runLen = countRunAndMakeAscending(a, lo, hi);
                if (runLen < minRun) {
                    int force = nRemaining <= minRun ? nRemaining : minRun;
                    binarySort(a, lo, lo + force, lo + runLen);
                    runLen = force;
                }
                ts.pushRun(lo, runLen);
                ts.mergeCollapse();
                lo += runLen;
                nRemaining -= runLen;
            } while (nRemaining != 0);
            if (!f68-assertionsDisabled) {
                if (lo == hi) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            ts.mergeForceCollapse();
            if (!f68-assertionsDisabled) {
                if (ts.stackSize != 1) {
                    obj2 = null;
                }
                if (obj2 == null) {
                    throw new AssertionError();
                }
            }
        }
    }

    private static void binarySort(Object[] a, int lo, int hi, int start) {
        if (!f68-assertionsDisabled) {
            Object obj = (lo > start || start > hi) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (start == lo) {
            start++;
        }
        while (start < hi) {
            Comparable pivot = a[start];
            int left = lo;
            int right = start;
            if (!f68-assertionsDisabled) {
                if ((lo <= right ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            while (left < right) {
                int mid = (left + right) >>> 1;
                if (pivot.compareTo(a[mid]) < 0) {
                    right = mid;
                } else {
                    left = mid + 1;
                }
            }
            if (!f68-assertionsDisabled) {
                if ((left == right ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            int n = start - left;
            switch (n) {
                case 1:
                    break;
                case 2:
                    a[left + 2] = a[left + 1];
                    break;
                default:
                    System.arraycopy((Object) a, left, (Object) a, left + 1, n);
                    continue;
            }
            a[left + 1] = a[left];
            a[left] = pivot;
            start++;
        }
    }

    private static int countRunAndMakeAscending(Object[] a, int lo, int hi) {
        int i = 0;
        if (!f68-assertionsDisabled) {
            if (lo < hi) {
                i = 1;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        int runHi2 = runHi + 1;
        if (((Comparable) a[runHi]).compareTo(a[lo]) < 0) {
            runHi = runHi2;
            while (runHi < hi && ((Comparable) a[runHi]).compareTo(a[runHi - 1]) < 0) {
                runHi++;
            }
            reverseRange(a, lo, runHi);
        } else {
            runHi = runHi2;
            while (runHi < hi && ((Comparable) a[runHi]).compareTo(a[runHi - 1]) >= 0) {
                runHi++;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(Object[] a, int lo, int hi) {
        int hi2 = hi - 1;
        int i = lo;
        while (i < hi2) {
            Object t = a[i];
            lo = i + 1;
            a[i] = a[hi2];
            hi = hi2 - 1;
            a[hi2] = t;
            hi2 = hi;
            i = lo;
        }
    }

    private static int minRunLength(int n) {
        Object obj = null;
        if (!f68-assertionsDisabled) {
            if (n >= 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int r = 0;
        while (n >= 32) {
            r |= n & 1;
            n >>= 1;
        }
        return n + r;
    }

    private void pushRun(int runBase, int runLen) {
        this.runBase[this.stackSize] = runBase;
        this.runLen[this.stackSize] = runLen;
        this.stackSize++;
    }

    private void mergeCollapse() {
        while (this.stackSize > 1) {
            int n = this.stackSize - 2;
            if (n > 0 && this.runLen[n - 1] <= this.runLen[n] + this.runLen[n + 1]) {
                if (this.runLen[n - 1] < this.runLen[n + 1]) {
                    n--;
                }
                mergeAt(n);
            } else if (this.runLen[n] <= this.runLen[n + 1]) {
                mergeAt(n);
            } else {
                return;
            }
        }
    }

    private void mergeForceCollapse() {
        while (this.stackSize > 1) {
            int n = this.stackSize - 2;
            if (n > 0 && this.runLen[n - 1] < this.runLen[n + 1]) {
                n--;
            }
            mergeAt(n);
        }
    }

    private void mergeAt(int i) {
        int i2;
        if (!f68-assertionsDisabled) {
            if ((this.stackSize >= 2 ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (!f68-assertionsDisabled) {
            if ((i >= 0 ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (!f68-assertionsDisabled) {
            i2 = (i == this.stackSize + -2 || i == this.stackSize - 3) ? 1 : 0;
            if (i2 == 0) {
                throw new AssertionError();
            }
        }
        int base1 = this.runBase[i];
        int len1 = this.runLen[i];
        int base2 = this.runBase[i + 1];
        int len2 = this.runLen[i + 1];
        if (!f68-assertionsDisabled) {
            i2 = (len1 <= 0 || len2 <= 0) ? 0 : 1;
            if (i2 == 0) {
                throw new AssertionError();
            }
        }
        if (!f68-assertionsDisabled) {
            if ((base1 + len1 == base2 ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        this.runLen[i] = len1 + len2;
        if (i == this.stackSize - 3) {
            this.runBase[i + 1] = this.runBase[i + 2];
            this.runLen[i + 1] = this.runLen[i + 2];
        }
        this.stackSize--;
        int k = gallopRight((Comparable) this.a[base2], this.a, base1, len1, 0);
        if (!f68-assertionsDisabled) {
            if ((k >= 0 ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        base1 += k;
        len1 -= k;
        if (len1 != 0) {
            len2 = gallopLeft((Comparable) this.a[(base1 + len1) - 1], this.a, base2, len2, len2 - 1);
            if (!f68-assertionsDisabled) {
                if ((len2 >= 0 ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (len2 != 0) {
                if (len1 <= len2) {
                    mergeLo(base1, len1, base2, len2);
                } else {
                    mergeHi(base1, len1, base2, len2);
                }
            }
        }
    }

    private static int gallopLeft(Comparable<Object> key, Object[] a, int base, int len, int hint) {
        Object obj;
        Object obj2 = 1;
        if (!f68-assertionsDisabled) {
            obj = (len <= 0 || hint < 0 || hint >= len) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int lastOfs = 0;
        int ofs = 1;
        int maxOfs;
        if (key.compareTo(a[base + hint]) > 0) {
            maxOfs = len - hint;
            while (ofs < maxOfs && key.compareTo(a[(base + hint) + ofs]) > 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            maxOfs = hint + 1;
            while (ofs < maxOfs && key.compareTo(a[(base + hint) - ofs]) <= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        }
        if (!f68-assertionsDisabled) {
            if (-1 > lastOfs || lastOfs >= ofs || ofs > len) {
                obj = null;
            } else {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (key.compareTo(a[base + m]) > 0) {
                lastOfs = m + 1;
            } else {
                ofs = m;
            }
        }
        if (!f68-assertionsDisabled) {
            if (lastOfs != ofs) {
                obj2 = null;
            }
            if (obj2 == null) {
                throw new AssertionError();
            }
        }
        return ofs;
    }

    private static int gallopRight(Comparable<Object> key, Object[] a, int base, int len, int hint) {
        Object obj;
        Object obj2 = 1;
        if (!f68-assertionsDisabled) {
            obj = (len <= 0 || hint < 0 || hint >= len) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int ofs = 1;
        int lastOfs = 0;
        int maxOfs;
        if (key.compareTo(a[base + hint]) < 0) {
            maxOfs = hint + 1;
            while (ofs < maxOfs && key.compareTo(a[(base + hint) - ofs]) < 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        } else {
            maxOfs = len - hint;
            while (ofs < maxOfs && key.compareTo(a[(base + hint) + ofs]) >= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0) {
                    ofs = maxOfs;
                }
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        }
        if (!f68-assertionsDisabled) {
            if (-1 > lastOfs || lastOfs >= ofs || ofs > len) {
                obj = null;
            } else {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((ofs - lastOfs) >>> 1);
            if (key.compareTo(a[base + m]) < 0) {
                ofs = m;
            } else {
                lastOfs = m + 1;
            }
        }
        if (!f68-assertionsDisabled) {
            if (lastOfs != ofs) {
                obj2 = null;
            }
            if (obj2 == null) {
                throw new AssertionError();
            }
        }
        return ofs;
    }

    /* JADX WARNING: Missing block: B:89:0x0139, code:
            if (r10 >= 0) goto L_0x013c;
     */
    /* JADX WARNING: Missing block: B:90:0x013b, code:
            r10 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void mergeLo(int base1, int len1, int base2, int len2) {
        Object obj;
        if (!f68-assertionsDisabled) {
            obj = (len1 <= 0 || len2 <= 0 || base1 + len1 != base2) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        Object a = this.a;
        Object tmp = ensureCapacity(len1);
        int cursor1 = this.tmpBase;
        int cursor2 = base2;
        int dest = base1;
        System.arraycopy(a, base1, tmp, cursor1, len1);
        dest = base1 + 1;
        cursor2 = base2 + 1;
        a[base1] = a[base2];
        len2--;
        if (len2 == 0) {
            System.arraycopy(tmp, cursor1, a, dest, len1);
        } else if (len1 == 1) {
            System.arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1];
        } else {
            int minGallop = this.minGallop;
            loop0:
            while (true) {
                int dest2;
                int cursor22;
                int cursor12;
                int count1 = 0;
                int count2 = 0;
                do {
                    if (!f68-assertionsDisabled) {
                        obj = (len1 <= 1 || len2 <= 0) ? null : 1;
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    if (((Comparable) a[cursor2]).compareTo(tmp[cursor1]) < 0) {
                        dest2 = dest + 1;
                        cursor22 = cursor2 + 1;
                        a[dest] = a[cursor2];
                        count2++;
                        count1 = 0;
                        len2--;
                        if (len2 == 0) {
                            dest = dest2;
                            cursor2 = cursor22;
                            break loop0;
                        }
                        dest = dest2;
                        cursor2 = cursor22;
                    } else {
                        dest2 = dest + 1;
                        cursor12 = cursor1 + 1;
                        a[dest] = tmp[cursor1];
                        count1++;
                        count2 = 0;
                        len1--;
                        if (len1 == 1) {
                            dest = dest2;
                            cursor1 = cursor12;
                            break loop0;
                        }
                        dest = dest2;
                        cursor1 = cursor12;
                    }
                } while ((count1 | count2) < minGallop);
                while (true) {
                    if (!f68-assertionsDisabled) {
                        obj = (len1 <= 1 || len2 <= 0) ? null : 1;
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    count1 = gallopRight((Comparable) a[cursor2], tmp, cursor1, len1, 0);
                    if (count1 != 0) {
                        System.arraycopy(tmp, cursor1, a, dest, count1);
                        dest += count1;
                        cursor1 += count1;
                        len1 -= count1;
                        if (len1 <= 1) {
                            break loop0;
                        }
                    }
                    dest2 = dest + 1;
                    cursor22 = cursor2 + 1;
                    a[dest] = a[cursor2];
                    len2--;
                    if (len2 != 0) {
                        count2 = gallopLeft((Comparable) tmp[cursor1], a, cursor22, len2, 0);
                        if (count2 != 0) {
                            System.arraycopy(a, cursor22, a, dest2, count2);
                            dest = dest2 + count2;
                            cursor2 = cursor22 + count2;
                            len2 -= count2;
                            if (len2 == 0) {
                                break loop0;
                            }
                        }
                        dest = dest2;
                        cursor2 = cursor22;
                        dest2 = dest + 1;
                        cursor12 = cursor1 + 1;
                        a[dest] = tmp[cursor1];
                        len1--;
                        if (len1 != 1) {
                            minGallop--;
                            if (((count2 >= 7 ? 1 : 0) | (count1 >= 7 ? 1 : 0)) == 0) {
                                break;
                            }
                            dest = dest2;
                            cursor1 = cursor12;
                        } else {
                            dest = dest2;
                            cursor1 = cursor12;
                            break loop0;
                        }
                    }
                    dest = dest2;
                    cursor2 = cursor22;
                    break loop0;
                }
                minGallop += 2;
                dest = dest2;
                cursor1 = cursor12;
            }
            if (minGallop < 1) {
                minGallop = 1;
            }
            this.minGallop = minGallop;
            if (len1 == 1) {
                if (!f68-assertionsDisabled) {
                    if (len2 > 0) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                System.arraycopy(a, cursor2, a, dest, len2);
                a[dest + len2] = tmp[cursor1];
            } else if (len1 == 0) {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            } else {
                if (!f68-assertionsDisabled) {
                    if ((len2 == 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                if (!f68-assertionsDisabled) {
                    if ((len1 > 1 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                System.arraycopy(tmp, cursor1, a, dest, len1);
            }
        }
    }

    /* JADX WARNING: Missing block: B:89:0x016b, code:
            if (r11 >= 0) goto L_0x016e;
     */
    /* JADX WARNING: Missing block: B:90:0x016d, code:
            r11 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void mergeHi(int base1, int len1, int base2, int len2) {
        Object obj;
        if (!f68-assertionsDisabled) {
            obj = (len1 <= 0 || len2 <= 0 || base1 + len1 != base2) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        Object a = this.a;
        Object tmp = ensureCapacity(len2);
        int tmpBase = this.tmpBase;
        System.arraycopy(a, base2, tmp, tmpBase, len2);
        int cursor1 = (base1 + len1) - 1;
        int cursor2 = (tmpBase + len2) - 1;
        int i = (base2 + len2) - 1;
        int dest = i - 1;
        int cursor12 = cursor1 - 1;
        a[i] = a[cursor1];
        len1--;
        if (len1 == 0) {
            System.arraycopy(tmp, tmpBase, a, dest - (len2 - 1), len2);
        } else if (len2 == 1) {
            i = dest - len1;
            System.arraycopy(a, (cursor12 - len1) + 1, a, i + 1, len1);
            a[i] = tmp[cursor2];
        } else {
            int minGallop = this.minGallop;
            loop0:
            while (true) {
                int cursor22;
                i = dest;
                cursor1 = cursor12;
                int count1 = 0;
                int count2 = 0;
                do {
                    if (!f68-assertionsDisabled) {
                        obj = (len1 <= 0 || len2 <= 1) ? null : 1;
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    if (((Comparable) tmp[cursor2]).compareTo(a[cursor1]) < 0) {
                        dest = i - 1;
                        cursor12 = cursor1 - 1;
                        a[i] = a[cursor1];
                        count1++;
                        count2 = 0;
                        len1--;
                        if (len1 == 0) {
                            i = dest;
                            cursor1 = cursor12;
                            break loop0;
                        }
                        i = dest;
                        cursor1 = cursor12;
                    } else {
                        dest = i - 1;
                        cursor22 = cursor2 - 1;
                        a[i] = tmp[cursor2];
                        count2++;
                        count1 = 0;
                        len2--;
                        if (len2 == 1) {
                            i = dest;
                            cursor2 = cursor22;
                            break loop0;
                        }
                        i = dest;
                        cursor2 = cursor22;
                    }
                } while ((count1 | count2) < minGallop);
                while (true) {
                    if (!f68-assertionsDisabled) {
                        obj = (len1 <= 0 || len2 <= 1) ? null : 1;
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    count1 = len1 - gallopRight((Comparable) tmp[cursor2], a, base1, len1, len1 - 1);
                    if (count1 != 0) {
                        i -= count1;
                        cursor1 -= count1;
                        len1 -= count1;
                        System.arraycopy(a, cursor1 + 1, a, i + 1, count1);
                        if (len1 == 0) {
                            break loop0;
                        }
                    }
                    dest = i - 1;
                    cursor22 = cursor2 - 1;
                    a[i] = tmp[cursor2];
                    len2--;
                    if (len2 != 1) {
                        count2 = len2 - gallopLeft((Comparable) a[cursor1], tmp, tmpBase, len2, len2 - 1);
                        if (count2 != 0) {
                            i = dest - count2;
                            cursor2 = cursor22 - count2;
                            len2 -= count2;
                            System.arraycopy(tmp, cursor2 + 1, a, i + 1, count2);
                            if (len2 <= 1) {
                                break loop0;
                            }
                        }
                        i = dest;
                        cursor2 = cursor22;
                        dest = i - 1;
                        cursor12 = cursor1 - 1;
                        a[i] = a[cursor1];
                        len1--;
                        if (len1 != 0) {
                            minGallop--;
                            if (((count2 >= 7 ? 1 : 0) | (count1 >= 7 ? 1 : 0)) == 0) {
                                break;
                            }
                            i = dest;
                            cursor1 = cursor12;
                        } else {
                            i = dest;
                            cursor1 = cursor12;
                            break loop0;
                        }
                    }
                    i = dest;
                    cursor2 = cursor22;
                    break loop0;
                }
                minGallop += 2;
            }
            if (minGallop < 1) {
                minGallop = 1;
            }
            this.minGallop = minGallop;
            if (len2 == 1) {
                if (!f68-assertionsDisabled) {
                    if (len1 > 0) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                i -= len1;
                System.arraycopy(a, (cursor1 - len1) + 1, a, i + 1, len1);
                a[i] = tmp[cursor2];
            } else if (len2 == 0) {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            } else {
                if (!f68-assertionsDisabled) {
                    if ((len1 == 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                if (!f68-assertionsDisabled) {
                    if ((len2 > 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                System.arraycopy(tmp, tmpBase, a, i - (len2 - 1), len2);
            }
        }
    }

    private Object[] ensureCapacity(int minCapacity) {
        if (this.tmpLen < minCapacity) {
            int newSize = minCapacity;
            newSize = minCapacity | (minCapacity >> 1);
            newSize |= newSize >> 2;
            newSize |= newSize >> 4;
            newSize |= newSize >> 8;
            newSize = (newSize | (newSize >> 16)) + 1;
            if (newSize < 0) {
                newSize = minCapacity;
            } else {
                newSize = Math.min(newSize, this.a.length >>> 1);
            }
            this.tmp = new Object[newSize];
            this.tmpLen = newSize;
            this.tmpBase = 0;
        }
        return this.tmp;
    }
}

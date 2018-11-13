package android.text;

import android.graphics.Paint.FontMetricsInt;
import android.text.Layout.Alignment;
import android.text.Layout.Directions;
import android.text.StaticLayout.Builder;
import android.text.TextUtils.TruncateAt;
import android.text.style.UpdateLayout;
import android.text.style.WrapTogetherSpan;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.ref.WeakReference;

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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DynamicLayout extends Layout {
    private static final int BLOCK_MINIMUM_CHARACTER_LENGTH = 400;
    private static final int COLUMNS_ELLIPSIZE = 6;
    private static final int COLUMNS_NORMAL = 4;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 5;
    private static final int ELLIPSIS_START = 4;
    private static final int ELLIPSIS_UNDEFINED = Integer.MIN_VALUE;
    private static final int HYPHEN = 3;
    public static final int INVALID_BLOCK_INDEX = -1;
    private static final int PRIORITY = 128;
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_MASK = 536870912;
    private static final int TOP = 1;
    private static Builder sBuilder;
    private static final Object[] sLock = null;
    private static StaticLayout sStaticLayout;
    private CharSequence mBase;
    private int[] mBlockEndLines;
    private int[] mBlockIndices;
    private int mBottomPadding;
    private int mBreakStrategy;
    private CharSequence mDisplay;
    private boolean mEllipsize;
    private TruncateAt mEllipsizeAt;
    private int mEllipsizedWidth;
    private int mHyphenationFrequency;
    private boolean mIncludePad;
    private int mIndexFirstChangedBlock;
    private PackedIntVector mInts;
    private int mNumberOfBlocks;
    private PackedObjectVector<Directions> mObjects;
    private int mTopPadding;
    private ChangeWatcher mWatcher;

    private static class ChangeWatcher implements TextWatcher, SpanWatcher {
        private WeakReference<DynamicLayout> mLayout;

        public ChangeWatcher(DynamicLayout layout) {
            this.mLayout = new WeakReference(layout);
        }

        private void reflow(CharSequence s, int where, int before, int after) {
            DynamicLayout ml = (DynamicLayout) this.mLayout.get();
            if (ml != null) {
                ml.reflow(s, where, before, after);
            } else if (s instanceof Spannable) {
                ((Spannable) s).removeSpan(this);
            }
        }

        public void beforeTextChanged(CharSequence s, int where, int before, int after) {
        }

        public void onTextChanged(CharSequence s, int where, int before, int after) {
            reflow(s, where, before, after);
        }

        public void afterTextChanged(Editable s) {
        }

        public void onSpanAdded(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
            }
        }

        public void onSpanRemoved(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
            }
        }

        public void onSpanChanged(Spannable s, Object o, int start, int end, int nstart, int nend) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
                reflow(s, nstart, nend - nstart, nend - nstart);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.text.DynamicLayout.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.text.DynamicLayout.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.DynamicLayout.<clinit>():void");
    }

    public DynamicLayout(CharSequence base, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, base, paint, width, align, spacingmult, spacingadd, includepad);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, display, paint, width, align, spacingmult, spacingadd, includepad, null, 0);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth) {
        this(base, display, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, 0, 0, ellipsize, ellipsizedWidth);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, int breakStrategy, int hyphenationFrequency, TruncateAt ellipsize, int ellipsizedWidth) {
        CharSequence charSequence;
        int[] start;
        if (ellipsize == null) {
            charSequence = display;
        } else if (display instanceof Spanned) {
            charSequence = new SpannedEllipsizer(display);
        } else {
            charSequence = new Ellipsizer(display);
        }
        super(charSequence, paint, width, align, textDir, spacingmult, spacingadd);
        this.mBase = base;
        this.mDisplay = display;
        if (ellipsize != null) {
            this.mInts = new PackedIntVector(6);
            this.mEllipsizedWidth = ellipsizedWidth;
            this.mEllipsizeAt = ellipsize;
        } else {
            this.mInts = new PackedIntVector(4);
            this.mEllipsizedWidth = width;
            this.mEllipsizeAt = null;
        }
        this.mObjects = new PackedObjectVector(1);
        this.mIncludePad = includepad;
        this.mBreakStrategy = breakStrategy;
        this.mHyphenationFrequency = hyphenationFrequency;
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            this.mEllipsize = true;
        }
        if (ellipsize != null) {
            start = new int[6];
            start[4] = Integer.MIN_VALUE;
        } else {
            start = new int[4];
        }
        Directions[] dirs = new Directions[1];
        dirs[0] = DIRS_ALL_LEFT_TO_RIGHT;
        FontMetricsInt fm = new FontMetricsInt();
        paint.getFontMetricsInt(base, fm);
        int asc = fm.ascent;
        int desc = fm.descent;
        start[0] = 1073741824;
        start[1] = 0;
        start[2] = desc;
        this.mInts.insertAt(0, start);
        start[1] = desc - asc;
        this.mInts.insertAt(1, start);
        this.mObjects.insertAt(0, dirs);
        reflow(base, 0, 0, base.length());
        if (base instanceof Spannable) {
            if (this.mWatcher == null) {
                this.mWatcher = new ChangeWatcher(this);
            }
            Spannable sp = (Spannable) base;
            ChangeWatcher[] spans = (ChangeWatcher[]) sp.getSpans(0, sp.length(), ChangeWatcher.class);
            for (Object removeSpan : spans) {
                sp.removeSpan(removeSpan);
            }
            sp.setSpan(this.mWatcher, 0, base.length(), 8388626);
        }
    }

    private void reflow(CharSequence s, int where, int before, int after) {
        if (s == this.mBase) {
            int i;
            StaticLayout reflowed;
            Builder b;
            int[] ints;
            CharSequence text = this.mDisplay;
            int len = text.length();
            int find = TextUtils.lastIndexOf(text, 10, where - 1);
            if (find < 0) {
                find = 0;
            } else {
                find++;
            }
            int diff = where - find;
            before += diff;
            after += diff;
            where -= diff;
            int look = TextUtils.indexOf(text, 10, where + after);
            if (look < 0) {
                look = len;
            } else {
                look++;
            }
            int change = look - (where + after);
            before += change;
            after += change;
            if (text instanceof Spanned) {
                Spanned sp = (Spanned) text;
                boolean again;
                do {
                    again = false;
                    Object[] force = sp.getSpans(where, where + after, WrapTogetherSpan.class);
                    for (i = 0; i < force.length; i++) {
                        int st = sp.getSpanStart(force[i]);
                        int en = sp.getSpanEnd(force[i]);
                        if (st < where) {
                            again = true;
                            diff = where - st;
                            before += diff;
                            after += diff;
                            where -= diff;
                        }
                        if (en > where + after) {
                            again = true;
                            diff = en - (where + after);
                            before += diff;
                            after += diff;
                        }
                    }
                } while (again);
            }
            int startline = getLineForOffset(where);
            int startv = getLineTop(startline);
            int endline = getLineForOffset(where + before);
            if (where + after == len) {
                endline = getLineCount();
            }
            int endv = getLineTop(endline);
            boolean islast = endline == getLineCount();
            synchronized (sLock) {
                reflowed = sStaticLayout;
                b = sBuilder;
                sStaticLayout = null;
                sBuilder = null;
            }
            if (reflowed == null) {
                StaticLayout staticLayout = new StaticLayout(null);
                b = Builder.obtain(text, where, where + after, getPaint(), getWidth());
            }
            b.setText(text, where, where + after).setPaint(getPaint()).setWidth(getWidth()).setTextDirection(getTextDirectionHeuristic()).setLineSpacing(getSpacingAdd(), getSpacingMultiplier()).setEllipsizedWidth(this.mEllipsizedWidth).setEllipsize(this.mEllipsizeAt).setBreakStrategy(this.mBreakStrategy).setHyphenationFrequency(this.mHyphenationFrequency);
            reflowed.generate(b, false, true);
            int n = reflowed.getLineCount();
            if (where + after != len && reflowed.getLineStart(n - 1) == where + after) {
                n--;
            }
            this.mInts.deleteAt(startline, endline - startline);
            this.mObjects.deleteAt(startline, endline - startline);
            int ht = reflowed.getLineTop(n);
            int toppad = 0;
            int botpad = 0;
            if (this.mIncludePad && startline == 0) {
                toppad = reflowed.getTopPadding();
                this.mTopPadding = toppad;
                ht -= toppad;
            }
            if (this.mIncludePad && islast) {
                botpad = reflowed.getBottomPadding();
                this.mBottomPadding = botpad;
                ht += botpad;
            }
            this.mInts.adjustValuesBelow(startline, 0, after - before);
            this.mInts.adjustValuesBelow(startline, 1, (startv - endv) + ht);
            if (this.mEllipsize) {
                ints = new int[6];
                ints[4] = Integer.MIN_VALUE;
            } else {
                ints = new int[4];
            }
            Directions[] objects = new Directions[1];
            for (i = 0; i < n; i++) {
                ints[0] = (reflowed.getLineContainsTab(i) ? 536870912 : 0) | ((reflowed.getParagraphDirection(i) << 30) | reflowed.getLineStart(i));
                int top = reflowed.getLineTop(i) + startv;
                if (i > 0) {
                    top -= toppad;
                }
                ints[1] = top;
                int desc = reflowed.getLineDescent(i);
                if (i == n - 1) {
                    desc += botpad;
                }
                ints[2] = desc;
                objects[0] = reflowed.getLineDirections(i);
                ints[3] = reflowed.getHyphen(i);
                if (this.mEllipsize) {
                    ints[4] = reflowed.getEllipsisStart(i);
                    ints[5] = reflowed.getEllipsisCount(i);
                }
                this.mInts.insertAt(startline + i, ints);
                this.mObjects.insertAt(startline + i, objects);
            }
            updateBlocks(startline, endline - 1, n);
            b.finish();
            synchronized (sLock) {
                sStaticLayout = reflowed;
                sBuilder = b;
            }
        }
    }

    private void createBlocks() {
        int offset = 400;
        this.mNumberOfBlocks = 0;
        CharSequence text = this.mDisplay;
        while (true) {
            offset = TextUtils.indexOf(text, 10, offset);
            if (offset < 0) {
                break;
            }
            addBlockAtOffset(offset);
            offset += 400;
        }
        addBlockAtOffset(text.length());
        this.mBlockIndices = new int[this.mBlockEndLines.length];
        for (int i = 0; i < this.mBlockEndLines.length; i++) {
            this.mBlockIndices[i] = -1;
        }
    }

    private void addBlockAtOffset(int offset) {
        int line = getLineForOffset(offset);
        if (this.mBlockEndLines == null) {
            this.mBlockEndLines = ArrayUtils.newUnpaddedIntArray(1);
            this.mBlockEndLines[this.mNumberOfBlocks] = line;
            this.mNumberOfBlocks++;
            return;
        }
        if (line > this.mBlockEndLines[this.mNumberOfBlocks - 1]) {
            this.mBlockEndLines = GrowingArrayUtils.append(this.mBlockEndLines, this.mNumberOfBlocks, line);
            this.mNumberOfBlocks++;
        }
    }

    void updateBlocks(int startLine, int endLine, int newLineCount) {
        if (this.mBlockEndLines == null) {
            createBlocks();
            return;
        }
        int i;
        int i2;
        int firstBlock = -1;
        int lastBlock = -1;
        for (i = 0; i < this.mNumberOfBlocks; i++) {
            if (this.mBlockEndLines[i] >= startLine) {
                firstBlock = i;
                break;
            }
        }
        for (i = firstBlock; i < this.mNumberOfBlocks; i++) {
            if (this.mBlockEndLines[i] >= endLine) {
                lastBlock = i;
                break;
            }
        }
        int lastBlockEndLine = this.mBlockEndLines[lastBlock];
        if (firstBlock == 0) {
            i2 = 0;
        } else {
            i2 = this.mBlockEndLines[firstBlock - 1] + 1;
        }
        boolean createBlockBefore = startLine > i2;
        boolean createBlock = newLineCount > 0;
        boolean createBlockAfter = endLine < this.mBlockEndLines[lastBlock];
        int numAddedBlocks = 0;
        if (createBlockBefore) {
            numAddedBlocks = 1;
        }
        if (createBlock) {
            numAddedBlocks++;
        }
        if (createBlockAfter) {
            numAddedBlocks++;
        }
        int newNumberOfBlocks = (this.mNumberOfBlocks + numAddedBlocks) - ((lastBlock - firstBlock) + 1);
        if (newNumberOfBlocks == 0) {
            this.mBlockEndLines[0] = 0;
            this.mBlockIndices[0] = -1;
            this.mNumberOfBlocks = 1;
            return;
        }
        int newFirstChangedBlock;
        if (newNumberOfBlocks > this.mBlockEndLines.length) {
            int[] blockEndLines = ArrayUtils.newUnpaddedIntArray(Math.max(this.mBlockEndLines.length * 2, newNumberOfBlocks));
            int[] blockIndices = new int[blockEndLines.length];
            System.arraycopy(this.mBlockEndLines, 0, blockEndLines, 0, firstBlock);
            System.arraycopy(this.mBlockIndices, 0, blockIndices, 0, firstBlock);
            System.arraycopy(this.mBlockEndLines, lastBlock + 1, blockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            System.arraycopy(this.mBlockIndices, lastBlock + 1, blockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            this.mBlockEndLines = blockEndLines;
            this.mBlockIndices = blockIndices;
        } else {
            System.arraycopy(this.mBlockEndLines, lastBlock + 1, this.mBlockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            System.arraycopy(this.mBlockIndices, lastBlock + 1, this.mBlockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
        }
        this.mNumberOfBlocks = newNumberOfBlocks;
        int deltaLines = newLineCount - ((endLine - startLine) + 1);
        if (deltaLines != 0) {
            newFirstChangedBlock = firstBlock + numAddedBlocks;
            for (i = newFirstChangedBlock; i < this.mNumberOfBlocks; i++) {
                int[] iArr = this.mBlockEndLines;
                iArr[i] = iArr[i] + deltaLines;
                this.mBlockIndices[i] = -1;
            }
        } else {
            newFirstChangedBlock = this.mNumberOfBlocks;
        }
        this.mIndexFirstChangedBlock = Math.min(this.mIndexFirstChangedBlock, newFirstChangedBlock);
        int blockIndex = firstBlock;
        if (createBlockBefore) {
            this.mBlockEndLines[blockIndex] = startLine - 1;
            this.mBlockIndices[blockIndex] = -1;
            blockIndex++;
        }
        if (createBlock) {
            this.mBlockEndLines[blockIndex] = (startLine + newLineCount) - 1;
            this.mBlockIndices[blockIndex] = -1;
            blockIndex++;
        }
        if (createBlockAfter) {
            this.mBlockEndLines[blockIndex] = lastBlockEndLine + deltaLines;
            this.mBlockIndices[blockIndex] = -1;
        }
    }

    void setBlocksDataForTest(int[] blockEndLines, int[] blockIndices, int numberOfBlocks) {
        this.mBlockEndLines = new int[blockEndLines.length];
        this.mBlockIndices = new int[blockIndices.length];
        System.arraycopy(blockEndLines, 0, this.mBlockEndLines, 0, blockEndLines.length);
        System.arraycopy(blockIndices, 0, this.mBlockIndices, 0, blockIndices.length);
        this.mNumberOfBlocks = numberOfBlocks;
    }

    public int[] getBlockEndLines() {
        return this.mBlockEndLines;
    }

    public int[] getBlockIndices() {
        return this.mBlockIndices;
    }

    public int getNumberOfBlocks() {
        return this.mNumberOfBlocks;
    }

    public int getIndexFirstChangedBlock() {
        return this.mIndexFirstChangedBlock;
    }

    public void setIndexFirstChangedBlock(int i) {
        this.mIndexFirstChangedBlock = i;
    }

    public int getLineCount() {
        return this.mInts.size() - 1;
    }

    public int getLineTop(int line) {
        return this.mInts.getValue(line, 1);
    }

    public int getLineDescent(int line) {
        return this.mInts.getValue(line, 2);
    }

    public int getLineStart(int line) {
        return this.mInts.getValue(line, 0) & START_MASK;
    }

    public boolean getLineContainsTab(int line) {
        return (this.mInts.getValue(line, 0) & 536870912) != 0;
    }

    public int getParagraphDirection(int line) {
        return this.mInts.getValue(line, 0) >> 30;
    }

    public final Directions getLineDirections(int line) {
        return (Directions) this.mObjects.getValue(line, 0);
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    public int getHyphen(int line) {
        return this.mInts.getValue(line, 3);
    }

    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    public int getEllipsisStart(int line) {
        if (this.mEllipsizeAt == null) {
            return 0;
        }
        return this.mInts.getValue(line, 4);
    }

    public int getEllipsisCount(int line) {
        if (this.mEllipsizeAt == null) {
            return 0;
        }
        return this.mInts.getValue(line, 5);
    }

    public boolean isSingleLineRtoL() {
        return (getLineDirections(0).mDirections[1] & 67108864) != 0;
    }
}

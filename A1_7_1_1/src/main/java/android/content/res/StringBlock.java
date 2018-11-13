package android.content.res;

import android.graphics.Color;
import android.hardware.Camera.Parameters;
import android.service.notification.ZenModeConfig;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan.WithDensity;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseArray;

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
final class StringBlock {
    private static final String TAG = "AssetManager";
    private static final boolean localLOGV = false;
    private final long mNative;
    private final boolean mOwnsNative;
    private SparseArray<CharSequence> mSparseStrings;
    private CharSequence[] mStrings;
    StyleIDs mStyleIDs;
    private final boolean mUseSparse;

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
    private static class Height implements WithDensity {
        private static float sProportion;
        private int mSize;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.res.StringBlock.Height.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.res.StringBlock.Height.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.res.StringBlock.Height.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.content.res.StringBlock.Height.<init>(int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public Height(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.content.res.StringBlock.Height.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.res.StringBlock.Height.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.content.res.StringBlock.Height.chooseHeight(java.lang.CharSequence, int, int, int, int, android.graphics.Paint$FontMetricsInt):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void chooseHeight(java.lang.CharSequence r1, int r2, int r3, int r4, int r5, android.graphics.Paint.FontMetricsInt r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.content.res.StringBlock.Height.chooseHeight(java.lang.CharSequence, int, int, int, int, android.graphics.Paint$FontMetricsInt):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.res.StringBlock.Height.chooseHeight(java.lang.CharSequence, int, int, int, int, android.graphics.Paint$FontMetricsInt):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.content.res.StringBlock.Height.chooseHeight(java.lang.CharSequence, int, int, int, int, android.graphics.Paint$FontMetricsInt, android.text.TextPaint):void, dex: 
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
        public void chooseHeight(java.lang.CharSequence r1, int r2, int r3, int r4, int r5, android.graphics.Paint.FontMetricsInt r6, android.text.TextPaint r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.content.res.StringBlock.Height.chooseHeight(java.lang.CharSequence, int, int, int, int, android.graphics.Paint$FontMetricsInt, android.text.TextPaint):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.res.StringBlock.Height.chooseHeight(java.lang.CharSequence, int, int, int, int, android.graphics.Paint$FontMetricsInt, android.text.TextPaint):void");
        }
    }

    static final class StyleIDs {
        private int bigId;
        private int boldId;
        private int italicId;
        private int listItemId;
        private int marqueeId;
        private int smallId;
        private int strikeId;
        private int subId;
        private int supId;
        private int ttId;
        private int underlineId;

        StyleIDs() {
            this.boldId = -1;
            this.italicId = -1;
            this.underlineId = -1;
            this.ttId = -1;
            this.bigId = -1;
            this.smallId = -1;
            this.subId = -1;
            this.supId = -1;
            this.strikeId = -1;
            this.listItemId = -1;
            this.marqueeId = -1;
        }
    }

    private static native long nativeCreate(byte[] bArr, int i, int i2);

    private static native void nativeDestroy(long j);

    private static native int nativeGetSize(long j);

    private static native String nativeGetString(long j, int i);

    private static native int[] nativeGetStyle(long j, int i);

    public StringBlock(byte[] data, boolean useSparse) {
        this.mStyleIDs = null;
        this.mNative = nativeCreate(data, 0, data.length);
        this.mUseSparse = useSparse;
        this.mOwnsNative = true;
    }

    public StringBlock(byte[] data, int offset, int size, boolean useSparse) {
        this.mStyleIDs = null;
        this.mNative = nativeCreate(data, offset, size);
        this.mUseSparse = useSparse;
        this.mOwnsNative = true;
    }

    /* JADX WARNING: Missing block: B:95:0x0178, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CharSequence get(int idx) {
        synchronized (this) {
            CharSequence res;
            if (this.mStrings != null) {
                res = this.mStrings[idx];
                if (res != null) {
                    return res;
                }
            } else if (this.mSparseStrings != null) {
                res = (CharSequence) this.mSparseStrings.get(idx);
                if (res != null) {
                    return res;
                }
            } else {
                int num = nativeGetSize(this.mNative);
                if (!this.mUseSparse || num <= 250) {
                    this.mStrings = new CharSequence[num];
                } else {
                    this.mSparseStrings = new SparseArray();
                }
            }
            String str = nativeGetString(this.mNative, idx);
            res = str;
            int[] style = nativeGetStyle(this.mNative, idx);
            if (style != null) {
                if (this.mStyleIDs == null) {
                    this.mStyleIDs = new StyleIDs();
                }
                for (int styleIndex = 0; styleIndex < style.length; styleIndex += 3) {
                    int styleId = style[styleIndex];
                    if (!(styleId == this.mStyleIDs.boldId || styleId == this.mStyleIDs.italicId)) {
                        if (!(styleId == this.mStyleIDs.underlineId || styleId == this.mStyleIDs.ttId || styleId == this.mStyleIDs.bigId || styleId == this.mStyleIDs.smallId || styleId == this.mStyleIDs.subId || styleId == this.mStyleIDs.supId || styleId == this.mStyleIDs.strikeId || styleId == this.mStyleIDs.listItemId || styleId == this.mStyleIDs.marqueeId)) {
                            String styleTag = nativeGetString(this.mNative, styleId);
                            if (styleTag.equals("b")) {
                                this.mStyleIDs.boldId = styleId;
                            } else if (styleTag.equals("i")) {
                                this.mStyleIDs.italicId = styleId;
                            } else if (styleTag.equals("u")) {
                                this.mStyleIDs.underlineId = styleId;
                            } else if (styleTag.equals(TtmlUtils.TAG_TT)) {
                                this.mStyleIDs.ttId = styleId;
                            } else if (styleTag.equals("big")) {
                                this.mStyleIDs.bigId = styleId;
                            } else if (styleTag.equals("small")) {
                                this.mStyleIDs.smallId = styleId;
                            } else if (styleTag.equals("sup")) {
                                this.mStyleIDs.supId = styleId;
                            } else if (styleTag.equals(Parameters.SENSOR_DEV_SUB)) {
                                this.mStyleIDs.subId = styleId;
                            } else if (styleTag.equals("strike")) {
                                this.mStyleIDs.strikeId = styleId;
                            } else if (styleTag.equals("li")) {
                                this.mStyleIDs.listItemId = styleId;
                            } else if (styleTag.equals("marquee")) {
                                this.mStyleIDs.marqueeId = styleId;
                            }
                        }
                    }
                }
                res = applyStyles(str, style, this.mStyleIDs);
            }
            if (this.mStrings != null) {
                this.mStrings[idx] = res;
            } else {
                this.mSparseStrings.put(idx, res);
            }
        }
    }

    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            if (this.mOwnsNative) {
                nativeDestroy(this.mNative);
            }
        }
    }

    private CharSequence applyStyles(String str, int[] style, StyleIDs ids) {
        if (style.length == 0) {
            return str;
        }
        SpannableString buffer = new SpannableString(str);
        for (int i = 0; i < style.length; i += 3) {
            int type = style[i];
            if (type == ids.boldId) {
                buffer.setSpan(new StyleSpan(1), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.italicId) {
                buffer.setSpan(new StyleSpan(2), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.underlineId) {
                buffer.setSpan(new UnderlineSpan(), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.ttId) {
                buffer.setSpan(new TypefaceSpan("monospace"), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.bigId) {
                buffer.setSpan(new RelativeSizeSpan(1.25f), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.smallId) {
                buffer.setSpan(new RelativeSizeSpan(0.8f), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.subId) {
                buffer.setSpan(new SubscriptSpan(), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.supId) {
                buffer.setSpan(new SuperscriptSpan(), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.strikeId) {
                buffer.setSpan(new StrikethroughSpan(), style[i + 1], style[i + 2] + 1, 33);
            } else if (type == ids.listItemId) {
                addParagraphSpan(buffer, new BulletSpan(10), style[i + 1], style[i + 2] + 1);
            } else if (type == ids.marqueeId) {
                buffer.setSpan(TruncateAt.MARQUEE, style[i + 1], style[i + 2] + 1, 18);
            } else {
                String tag = nativeGetString(this.mNative, type);
                String sub;
                if (tag.startsWith("font;")) {
                    sub = subtag(tag, ";height=");
                    if (sub != null) {
                        addParagraphSpan(buffer, new Height(Integer.parseInt(sub)), style[i + 1], style[i + 2] + 1);
                    }
                    sub = subtag(tag, ";size=");
                    if (sub != null) {
                        buffer.setSpan(new AbsoluteSizeSpan(Integer.parseInt(sub), true), style[i + 1], style[i + 2] + 1, 33);
                    }
                    sub = subtag(tag, ";fgcolor=");
                    if (sub != null) {
                        buffer.setSpan(getColor(sub, true), style[i + 1], style[i + 2] + 1, 33);
                    }
                    sub = subtag(tag, ";color=");
                    if (sub != null) {
                        buffer.setSpan(getColor(sub, true), style[i + 1], style[i + 2] + 1, 33);
                    }
                    sub = subtag(tag, ";bgcolor=");
                    if (sub != null) {
                        buffer.setSpan(getColor(sub, false), style[i + 1], style[i + 2] + 1, 33);
                    }
                    sub = subtag(tag, ";face=");
                    if (sub != null) {
                        buffer.setSpan(new TypefaceSpan(sub), style[i + 1], style[i + 2] + 1, 33);
                    }
                } else if (tag.startsWith("a;")) {
                    sub = subtag(tag, ";href=");
                    if (sub != null) {
                        buffer.setSpan(new URLSpan(sub), style[i + 1], style[i + 2] + 1, 33);
                    }
                } else if (tag.startsWith("annotation;")) {
                    int len = tag.length();
                    int t = tag.indexOf(59);
                    while (t < len) {
                        int eq = tag.indexOf(61, t);
                        if (eq < 0) {
                            break;
                        }
                        int next = tag.indexOf(59, eq);
                        if (next < 0) {
                            next = len;
                        }
                        buffer.setSpan(new Annotation(tag.substring(t + 1, eq), tag.substring(eq + 1, next)), style[i + 1], style[i + 2] + 1, 33);
                        t = next;
                    }
                }
            }
        }
        return new SpannedString(buffer);
    }

    private static CharacterStyle getColor(String color, boolean foreground) {
        int c = -16777216;
        if (!TextUtils.isEmpty(color)) {
            if (color.startsWith("@")) {
                Resources res = Resources.getSystem();
                int colorRes = res.getIdentifier(color.substring(1), ColorsColumns.COLOR, ZenModeConfig.SYSTEM_AUTHORITY);
                if (colorRes != 0) {
                    ColorStateList colors = res.getColorStateList(colorRes, null);
                    if (foreground) {
                        return new TextAppearanceSpan(null, 0, 0, colors, null);
                    }
                    c = colors.getDefaultColor();
                }
            } else {
                try {
                    c = Color.parseColor(color);
                } catch (IllegalArgumentException e) {
                    c = -16777216;
                }
            }
        }
        if (foreground) {
            return new ForegroundColorSpan(c);
        }
        return new BackgroundColorSpan(c);
    }

    private static void addParagraphSpan(Spannable buffer, Object what, int start, int end) {
        int len = buffer.length();
        if (!(start == 0 || start == len || buffer.charAt(start - 1) == 10)) {
            start--;
            while (start > 0 && buffer.charAt(start - 1) != 10) {
                start--;
            }
        }
        if (!(end == 0 || end == len || buffer.charAt(end - 1) == 10)) {
            end++;
            while (end < len && buffer.charAt(end - 1) != 10) {
                end++;
            }
        }
        buffer.setSpan(what, start, end, 51);
    }

    private static String subtag(String full, String attribute) {
        int start = full.indexOf(attribute);
        if (start < 0) {
            return null;
        }
        start += attribute.length();
        int end = full.indexOf(59, start);
        if (end < 0) {
            return full.substring(start);
        }
        return full.substring(start, end);
    }

    StringBlock(long obj, boolean useSparse) {
        this.mStyleIDs = null;
        this.mNative = obj;
        this.mUseSparse = useSparse;
        this.mOwnsNative = false;
    }
}

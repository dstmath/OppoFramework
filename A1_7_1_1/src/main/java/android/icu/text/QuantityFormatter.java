package android.icu.text;

import android.icu.impl.SimplePatternFormatter;
import android.icu.impl.StandardPlural;
import android.icu.text.PluralRules.FixedDecimal;
import java.text.FieldPosition;

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
class QuantityFormatter {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f104-assertionsDisabled = false;
    private final SimplePatternFormatter[] templates;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.QuantityFormatter.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.QuantityFormatter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.QuantityFormatter.<clinit>():void");
    }

    public QuantityFormatter() {
        this.templates = new SimplePatternFormatter[StandardPlural.COUNT];
    }

    public void addIfAbsent(CharSequence variant, String template) {
        int idx = StandardPlural.indexFromString(variant);
        if (this.templates[idx] == null) {
            this.templates[idx] = SimplePatternFormatter.compileMinMaxPlaceholders(template, 0, 1);
        }
    }

    public boolean isValid() {
        return this.templates[StandardPlural.OTHER_INDEX] != null;
    }

    public String format(double number, NumberFormat numberFormat, PluralRules pluralRules) {
        String formatStr = numberFormat.format(number);
        SimplePatternFormatter formatter = this.templates[selectPlural(number, numberFormat, pluralRules).ordinal()];
        if (formatter == null) {
            formatter = this.templates[StandardPlural.OTHER_INDEX];
            if (!f104-assertionsDisabled) {
                if ((formatter != null ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
        }
        CharSequence[] charSequenceArr = new CharSequence[1];
        charSequenceArr[0] = formatStr;
        return formatter.format(charSequenceArr);
    }

    public SimplePatternFormatter getByVariant(CharSequence variant) {
        if (f104-assertionsDisabled || isValid()) {
            int idx = StandardPlural.indexOrOtherIndexFromString(variant);
            SimplePatternFormatter template = this.templates[idx];
            if (template != null || idx == StandardPlural.OTHER_INDEX) {
                return template;
            }
            return this.templates[StandardPlural.OTHER_INDEX];
        }
        throw new AssertionError();
    }

    public static StandardPlural selectPlural(double number, NumberFormat numberFormat, PluralRules rules) {
        String pluralKeyword;
        if (numberFormat instanceof DecimalFormat) {
            pluralKeyword = rules.select(((DecimalFormat) numberFormat).getFixedDecimal(number));
        } else {
            pluralKeyword = rules.select(number);
        }
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    public static StandardPlural selectPlural(Number number, NumberFormat fmt, PluralRules rules, StringBuffer formattedNumber, FieldPosition pos) {
        FieldPosition fpos = new UFieldPosition(pos.getFieldAttribute(), pos.getField());
        fmt.format((Object) number, formattedNumber, fpos);
        String pluralKeyword = rules.select(new FixedDecimal(number.doubleValue(), fpos.getCountVisibleFractionDigits(), fpos.getFractionDigits()));
        pos.setBeginIndex(fpos.getBeginIndex());
        pos.setEndIndex(fpos.getEndIndex());
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    public static StringBuilder format(String compiledPattern, CharSequence value, StringBuilder appendTo, FieldPosition pos) {
        int[] offsets = new int[1];
        CharSequence[] charSequenceArr = new CharSequence[1];
        charSequenceArr[0] = value;
        SimplePatternFormatter.formatAndAppend(compiledPattern, appendTo, offsets, charSequenceArr);
        if (!(pos.getBeginIndex() == 0 && pos.getEndIndex() == 0)) {
            if (offsets[0] >= 0) {
                pos.setBeginIndex(pos.getBeginIndex() + offsets[0]);
                pos.setEndIndex(pos.getEndIndex() + offsets[0]);
            } else {
                pos.setBeginIndex(0);
                pos.setEndIndex(0);
            }
        }
        return appendTo;
    }
}

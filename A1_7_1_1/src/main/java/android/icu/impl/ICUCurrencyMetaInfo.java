package android.icu.impl;

import android.icu.text.CurrencyMetaInfo;
import android.icu.text.CurrencyMetaInfo.CurrencyDigits;
import android.icu.text.CurrencyMetaInfo.CurrencyFilter;
import android.icu.text.CurrencyMetaInfo.CurrencyInfo;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ICUCurrencyMetaInfo extends CurrencyMetaInfo {
    private static final int Currency = 2;
    private static final int Date = 4;
    private static final int Everything = Integer.MAX_VALUE;
    private static final long MASK = 4294967295L;
    private static final int Region = 1;
    private static final int Tender = 8;
    private ICUResourceBundle digitInfo;
    private ICUResourceBundle regionInfo;

    private interface Collector<T> {
        void collect(String str, String str2, long j, long j2, int i, boolean z);

        int collects();

        List<T> getList();
    }

    private static class CurrencyCollector implements Collector<String> {
        private final UniqueList<String> result;

        /* synthetic */ CurrencyCollector(CurrencyCollector currencyCollector) {
            this();
        }

        private CurrencyCollector() {
            this.result = UniqueList.create();
        }

        public void collect(String region, String currency, long from, long to, int priority, boolean tender) {
            this.result.add(currency);
        }

        public int collects() {
            return 2;
        }

        public List<String> getList() {
            return this.result.list();
        }
    }

    private static class InfoCollector implements Collector<CurrencyInfo> {
        private List<CurrencyInfo> result;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.<init>():void, dex: 
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
        private InfoCollector() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.<init>(android.icu.impl.ICUCurrencyMetaInfo$InfoCollector):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        /* synthetic */ InfoCollector(android.icu.impl.ICUCurrencyMetaInfo.InfoCollector r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.<init>(android.icu.impl.ICUCurrencyMetaInfo$InfoCollector):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.<init>(android.icu.impl.ICUCurrencyMetaInfo$InfoCollector):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.collect(java.lang.String, java.lang.String, long, long, int, boolean):void, dex: 
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
        public void collect(java.lang.String r1, java.lang.String r2, long r3, long r5, int r7, boolean r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.collect(java.lang.String, java.lang.String, long, long, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.collect(java.lang.String, java.lang.String, long, long, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.getList():java.util.List<android.icu.text.CurrencyMetaInfo$CurrencyInfo>, dex: 
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
        public java.util.List<android.icu.text.CurrencyMetaInfo.CurrencyInfo> getList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.getList():java.util.List<android.icu.text.CurrencyMetaInfo$CurrencyInfo>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.InfoCollector.getList():java.util.List<android.icu.text.CurrencyMetaInfo$CurrencyInfo>");
        }

        public int collects() {
            return Integer.MAX_VALUE;
        }
    }

    private static class RegionCollector implements Collector<String> {
        private final UniqueList<String> result;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.<init>():void, dex: 
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
        private RegionCollector() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.<init>():void");
        }

        /* synthetic */ RegionCollector(RegionCollector regionCollector) {
            this();
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.collect(java.lang.String, java.lang.String, long, long, int, boolean):void, dex: 
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
        public void collect(java.lang.String r1, java.lang.String r2, long r3, long r5, int r7, boolean r8) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.collect(java.lang.String, java.lang.String, long, long, int, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.collect(java.lang.String, java.lang.String, long, long, int, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.getList():java.util.List<java.lang.String>, dex: 
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
        public java.util.List<java.lang.String> getList() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.getList():java.util.List<java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUCurrencyMetaInfo.RegionCollector.getList():java.util.List<java.lang.String>");
        }

        public int collects() {
            return 1;
        }
    }

    private static class UniqueList<T> {
        private List<T> list;
        private Set<T> seen;

        private UniqueList() {
            this.seen = new HashSet();
            this.list = new ArrayList();
        }

        private static <T> UniqueList<T> create() {
            return new UniqueList();
        }

        void add(T value) {
            if (!this.seen.contains(value)) {
                this.list.add(value);
                this.seen.add(value);
            }
        }

        List<T> list() {
            return Collections.unmodifiableList(this.list);
        }
    }

    public ICUCurrencyMetaInfo() {
        ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b/curr", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        this.regionInfo = bundle.findTopLevel("CurrencyMap");
        this.digitInfo = bundle.findTopLevel("CurrencyMeta");
    }

    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return collect(new InfoCollector(), filter);
    }

    public List<String> currencies(CurrencyFilter filter) {
        return collect(new CurrencyCollector(), filter);
    }

    public List<String> regions(CurrencyFilter filter) {
        return collect(new RegionCollector(), filter);
    }

    public CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, CurrencyUsage.STANDARD);
    }

    public CurrencyDigits currencyDigits(String isoCode, CurrencyUsage currencyPurpose) {
        ICUResourceBundle b = this.digitInfo.findWithFallback(isoCode);
        if (b == null) {
            b = this.digitInfo.findWithFallback("DEFAULT");
        }
        int[] data = b.getIntVector();
        if (currencyPurpose == CurrencyUsage.CASH) {
            return new CurrencyDigits(data[2], data[3]);
        }
        if (currencyPurpose == CurrencyUsage.STANDARD) {
            return new CurrencyDigits(data[0], data[1]);
        }
        return new CurrencyDigits(data[0], data[1]);
    }

    private <T> List<T> collect(Collector<T> collector, CurrencyFilter filter) {
        if (filter == null) {
            filter = CurrencyFilter.all();
        }
        int needed = collector.collects();
        if (filter.region != null) {
            needed |= 1;
        }
        if (filter.currency != null) {
            needed |= 2;
        }
        if (!(filter.from == Long.MIN_VALUE && filter.to == Long.MAX_VALUE)) {
            needed |= 4;
        }
        if (filter.tenderOnly) {
            needed |= 8;
        }
        if (needed != 0) {
            if (filter.region != null) {
                ICUResourceBundle b = this.regionInfo.findWithFallback(filter.region);
                if (b != null) {
                    collectRegion(collector, filter, needed, b);
                }
            } else {
                for (int i = 0; i < this.regionInfo.getSize(); i++) {
                    collectRegion(collector, filter, needed, this.regionInfo.at(i));
                }
            }
        }
        return collector.getList();
    }

    private <T> void collectRegion(Collector<T> collector, CurrencyFilter filter, int needed, ICUResourceBundle b) {
        String region = b.getKey();
        if (needed == 1) {
            collector.collect(b.getKey(), null, 0, 0, -1, false);
            return;
        }
        for (int i = 0; i < b.getSize(); i++) {
            ICUResourceBundle r = b.at(i);
            if (r.getSize() != 0) {
                String currency = null;
                long from = Long.MIN_VALUE;
                long to = Long.MAX_VALUE;
                boolean tender = true;
                if ((needed & 2) != 0) {
                    currency = r.at("id").getString();
                    if (!(filter.currency == null || filter.currency.equals(currency))) {
                    }
                }
                if ((needed & 4) != 0) {
                    from = getDate(r.at("from"), Long.MIN_VALUE, false);
                    to = getDate(r.at("to"), Long.MAX_VALUE, true);
                    if (filter.from <= to) {
                        if (filter.to < from) {
                        }
                    }
                }
                if ((needed & 8) != 0) {
                    ICUResourceBundle tenderBundle = r.at("tender");
                    tender = tenderBundle != null ? "true".equals(tenderBundle.getString()) : true;
                    if (filter.tenderOnly && !tender) {
                    }
                }
                collector.collect(region, currency, from, to, i, tender);
            }
        }
    }

    private long getDate(ICUResourceBundle b, long defaultValue, boolean endOfDay) {
        if (b == null) {
            return defaultValue;
        }
        int[] values = b.getIntVector();
        return (((long) values[0]) << 32) | (((long) values[1]) & MASK);
    }
}

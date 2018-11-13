package android.icu.impl;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
import android.icu.impl.UResource.Value;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import libcore.icu.RelativeDateTimeFormatter;

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
public class TimeZoneNamesImpl extends TimeZoneNames {
    private static final Pattern LOC_EXCLUSION_PATTERN = null;
    private static volatile Set<String> METAZONE_IDS = null;
    private static final String MZ_PREFIX = "meta:";
    private static final MZ2TZsCache MZ_TO_TZS_CACHE = null;
    private static final NameType[] NAME_TYPE_VALUES = null;
    private static final TZ2MZsCache TZ_TO_MZS_CACHE = null;
    private static final String ZONE_STRINGS_BUNDLE = "zoneStrings";
    private static final long serialVersionUID = -2179814848495897472L;
    private transient ConcurrentHashMap<String, ZNames> _mzNamesMap;
    private transient boolean _namesFullyLoaded;
    private transient TextTrieMap<NameInfo> _namesTrie;
    private transient boolean _namesTrieFullyLoaded;
    private transient ConcurrentHashMap<String, ZNames> _tzNamesMap;
    private transient ICUResourceBundle _zoneStrings;

    private static class MZ2TZsCache extends SoftCache<String, Map<String, String>, String> {
        /* synthetic */ MZ2TZsCache(MZ2TZsCache mZ2TZsCache) {
            this();
        }

        private MZ2TZsCache() {
        }

        protected Map<String, String> createInstance(String key, String data) {
            try {
                UResourceBundle regionMap = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "metaZones").get("mapTimezones").get(key);
                Set<String> regions = regionMap.keySet();
                Map<String, String> map = new HashMap(regions.size());
                try {
                    for (String region : regions) {
                        map.put(region.intern(), regionMap.getString(region).intern());
                    }
                    return map;
                } catch (MissingResourceException e) {
                    Map<String, String> map2 = map;
                    return Collections.emptyMap();
                }
            } catch (MissingResourceException e2) {
                return Collections.emptyMap();
            }
        }
    }

    private static class MZMapEntry {
        private long _from;
        private String _mzID;
        private long _to;

        MZMapEntry(String mzID, long from, long to) {
            this._mzID = mzID;
            this._from = from;
            this._to = to;
        }

        String mzID() {
            return this._mzID;
        }

        long from() {
            return this._from;
        }

        long to() {
            return this._to;
        }
    }

    private static class NameInfo {
        String mzID;
        NameType type;
        String tzID;

        /* synthetic */ NameInfo(NameInfo nameInfo) {
            this();
        }

        private NameInfo() {
        }
    }

    private static class NameSearchHandler implements ResultHandler<NameInfo> {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f47-assertionsDisabled = false;
        private Collection<MatchInfo> _matches;
        private int _maxMatchLen;
        private EnumSet<NameType> _nameTypes;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.<init>(java.util.EnumSet):void, dex: 
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
        NameSearchHandler(java.util.EnumSet<android.icu.text.TimeZoneNames.NameType> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.<init>(java.util.EnumSet):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.<init>(java.util.EnumSet):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.getMatches():java.util.Collection<android.icu.text.TimeZoneNames$MatchInfo>, dex: 
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
        public java.util.Collection<android.icu.text.TimeZoneNames.MatchInfo> getMatches() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.getMatches():java.util.Collection<android.icu.text.TimeZoneNames$MatchInfo>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.getMatches():java.util.Collection<android.icu.text.TimeZoneNames$MatchInfo>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.getMaxMatchLen():int, dex: 
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
        public int getMaxMatchLen() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.getMaxMatchLen():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.getMaxMatchLen():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.handlePrefixMatch(int, java.util.Iterator):boolean, dex: 
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
        public boolean handlePrefixMatch(int r1, java.util.Iterator<android.icu.impl.TimeZoneNamesImpl.NameInfo> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.handlePrefixMatch(int, java.util.Iterator):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.handlePrefixMatch(int, java.util.Iterator):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.resetResults():void, dex: 
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
        public void resetResults() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.resetResults():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.NameSearchHandler.resetResults():void");
        }
    }

    private static class TZ2MZsCache extends SoftCache<String, List<MZMapEntry>, String> {
        /* synthetic */ TZ2MZsCache(TZ2MZsCache tZ2MZsCache) {
            this();
        }

        private TZ2MZsCache() {
        }

        protected List<MZMapEntry> createInstance(String key, String data) {
            try {
                UResourceBundle zoneBundle = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "metaZones").get("metazoneInfo").get(data.replace('/', ':'));
                List<MZMapEntry> mzMaps = new ArrayList(zoneBundle.getSize());
                int idx = 0;
                while (idx < zoneBundle.getSize()) {
                    try {
                        UResourceBundle mz = zoneBundle.get(idx);
                        String mzid = mz.getString(0);
                        String fromStr = "1970-01-01 00:00";
                        String toStr = "9999-12-31 23:59";
                        if (mz.getSize() == 3) {
                            fromStr = mz.getString(1);
                            toStr = mz.getString(2);
                        }
                        mzMaps.add(new MZMapEntry(mzid, parseDate(fromStr), parseDate(toStr)));
                        idx++;
                    } catch (MissingResourceException e) {
                        List<MZMapEntry> list = mzMaps;
                        return Collections.emptyList();
                    }
                }
                return mzMaps;
            } catch (MissingResourceException e2) {
                return Collections.emptyList();
            }
        }

        private static long parseDate(String text) {
            int idx;
            int n;
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int min = 0;
            for (idx = 0; idx <= 3; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad year");
                }
                year = (year * 10) + n;
            }
            for (idx = 5; idx <= 6; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad month");
                }
                month = (month * 10) + n;
            }
            for (idx = 8; idx <= 9; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad day");
                }
                day = (day * 10) + n;
            }
            for (idx = 11; idx <= 12; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad hour");
                }
                hour = (hour * 10) + n;
            }
            for (idx = 14; idx <= 15; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad minute");
                }
                min = (min * 10) + n;
            }
            return ((Grego.fieldsToDay(year, month - 1, day) * 86400000) + (((long) hour) * RelativeDateTimeFormatter.HOUR_IN_MILLIS)) + (((long) min) * RelativeDateTimeFormatter.MINUTE_IN_MILLIS);
        }
    }

    private static class ZNames {
        private static final ZNames EMPTY_ZNAMES = null;
        private static final int EX_LOC_INDEX = 0;
        private String[] _names;
        private boolean didAddIntoTrie;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.TimeZoneNamesImpl.ZNames.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.TimeZoneNamesImpl.ZNames.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZNames.<clinit>():void");
        }

        protected ZNames(String[] names) {
            this._names = names;
            this.didAddIntoTrie = names == null;
        }

        public static ZNames getInstance(String[] names, String tzID) {
            if (tzID != null && (names == null || names[EX_LOC_INDEX] == null)) {
                String locationName = TimeZoneNamesImpl.getDefaultExemplarLocationName(tzID);
                if (locationName != null) {
                    if (names == null) {
                        names = new String[(EX_LOC_INDEX + 1)];
                    }
                    names[EX_LOC_INDEX] = locationName;
                }
            }
            if (names == null) {
                return EMPTY_ZNAMES;
            }
            return new ZNames(names);
        }

        public static ZNames getInstance(ZNamesLoader loader, ICUResourceBundle zoneStrings, String key, String tzID) {
            return getInstance(loader.load(zoneStrings, key), tzID);
        }

        public String getName(NameType type) {
            if (this._names == null || type.ordinal() >= this._names.length) {
                return null;
            }
            return this._names[type.ordinal()];
        }

        public void addNamesIntoTrie(String mzID, String tzID, TextTrieMap<NameInfo> trie) {
            if (this._names != null && !this.didAddIntoTrie) {
                for (int i = 0; i < this._names.length; i++) {
                    String name = this._names[i];
                    if (name != null) {
                        NameInfo info = new NameInfo();
                        info.mzID = mzID;
                        info.tzID = tzID;
                        info.type = TimeZoneNamesImpl.NAME_TYPE_VALUES[i];
                        trie.put(name, info);
                    }
                }
                this.didAddIntoTrie = true;
            }
        }
    }

    private static final class ZNamesLoader extends TableSink {
        private static ZNamesLoader DUMMY_LOADER;
        private static String NO_NAME;
        private static int NUM_META_ZONE_NAMES;
        private static int NUM_TIME_ZONE_NAMES;
        private String[] names;
        private int numNames;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.TimeZoneNamesImpl.ZNamesLoader.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.TimeZoneNamesImpl.ZNamesLoader.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZNamesLoader.<clinit>():void");
        }

        private ZNamesLoader(int numNames) {
            this.numNames = numNames;
        }

        static ZNamesLoader forMetaZoneNames() {
            return new ZNamesLoader(NUM_META_ZONE_NAMES);
        }

        static ZNamesLoader forTimeZoneNames() {
            return new ZNamesLoader(NUM_TIME_ZONE_NAMES);
        }

        String[] load(ICUResourceBundle zoneStrings, String key) {
            if (zoneStrings == null || key == null || key.length() == 0) {
                return null;
            }
            try {
                zoneStrings.getAllTableItemsWithFallback(key, this);
                return getNames();
            } catch (MissingResourceException e) {
                return null;
            }
        }

        private static NameType nameTypeFromKey(Key key) {
            NameType nameType = null;
            if (key.length() != 2) {
                return null;
            }
            char c0 = key.charAt(0);
            char c1 = key.charAt(1);
            if (c0 == 'l') {
                if (c1 == 'g') {
                    nameType = NameType.LONG_GENERIC;
                } else if (c1 == 's') {
                    nameType = NameType.LONG_STANDARD;
                } else if (c1 == 'd') {
                    nameType = NameType.LONG_DAYLIGHT;
                }
                return nameType;
            } else if (c0 == 's') {
                if (c1 == 'g') {
                    nameType = NameType.SHORT_GENERIC;
                } else if (c1 == 's') {
                    nameType = NameType.SHORT_STANDARD;
                } else if (c1 == 'd') {
                    nameType = NameType.SHORT_DAYLIGHT;
                }
                return nameType;
            } else if (c0 == 'e' && c1 == 'c') {
                return NameType.EXEMPLAR_LOCATION;
            } else {
                return null;
            }
        }

        public void put(Key key, Value value) {
            if (value.getType() == 0) {
                if (this.names == null) {
                    this.names = new String[this.numNames];
                }
                NameType type = nameTypeFromKey(key);
                if (type != null && type.ordinal() < this.numNames && this.names[type.ordinal()] == null) {
                    this.names[type.ordinal()] = value.getString();
                }
            }
        }

        public void putNoFallback(Key key) {
            if (this.names == null) {
                this.names = new String[this.numNames];
            }
            NameType type = nameTypeFromKey(key);
            if (type != null && type.ordinal() < this.numNames && this.names[type.ordinal()] == null) {
                this.names[type.ordinal()] = NO_NAME;
            }
        }

        private String[] getNames() {
            if (this.names == null) {
                return null;
            }
            int length = 0;
            for (int i = 0; i < this.numNames; i++) {
                String name = this.names[i];
                if (name != null) {
                    if (name == NO_NAME) {
                        this.names[i] = null;
                    } else {
                        length = i + 1;
                    }
                }
            }
            if (length == 0) {
                return null;
            }
            String[] result;
            if (length == this.numNames || this.numNames == NUM_TIME_ZONE_NAMES) {
                result = this.names;
                this.names = null;
                return result;
            }
            result = new String[length];
            do {
                length--;
                result[length] = this.names[length];
                this.names[length] = null;
            } while (length > 0);
            return result;
        }
    }

    private final class ZoneStringsLoader extends TableSink {
        private static final int INITIAL_NUM_ZONES = 300;
        private HashMap<Key, ZNamesLoader> keyToLoader;
        private StringBuilder sb;
        final /* synthetic */ TimeZoneNamesImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.<init>(android.icu.impl.TimeZoneNamesImpl):void, dex: 
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
        private ZoneStringsLoader(android.icu.impl.TimeZoneNamesImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.<init>(android.icu.impl.TimeZoneNamesImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.<init>(android.icu.impl.TimeZoneNamesImpl):void");
        }

        /* synthetic */ ZoneStringsLoader(TimeZoneNamesImpl this$0, ZoneStringsLoader zoneStringsLoader) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.mzIDFromKey(android.icu.impl.UResource$Key):java.lang.String, dex: 
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
        private java.lang.String mzIDFromKey(android.icu.impl.UResource.Key r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.mzIDFromKey(android.icu.impl.UResource$Key):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.mzIDFromKey(android.icu.impl.UResource$Key):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.tzIDFromKey(android.icu.impl.UResource$Key):java.lang.String, dex: 
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
        private java.lang.String tzIDFromKey(android.icu.impl.UResource.Key r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.tzIDFromKey(android.icu.impl.UResource$Key):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.tzIDFromKey(android.icu.impl.UResource$Key):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.getOrCreateTableSink(android.icu.impl.UResource$Key, int):android.icu.impl.UResource$TableSink, dex: 
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
        public android.icu.impl.UResource.TableSink getOrCreateTableSink(android.icu.impl.UResource.Key r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.getOrCreateTableSink(android.icu.impl.UResource$Key, int):android.icu.impl.UResource$TableSink, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.getOrCreateTableSink(android.icu.impl.UResource$Key, int):android.icu.impl.UResource$TableSink");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.load():void, dex: 
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
        void load() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.load():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.load():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.putNoFallback(android.icu.impl.UResource$Key):void, dex: 
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
        public void putNoFallback(android.icu.impl.UResource.Key r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.putNoFallback(android.icu.impl.UResource$Key):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.ZoneStringsLoader.putNoFallback(android.icu.impl.UResource$Key):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.TimeZoneNamesImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.TimeZoneNamesImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TimeZoneNamesImpl.<clinit>():void");
    }

    public TimeZoneNamesImpl(ULocale locale) {
        initialize(locale);
    }

    public Set<String> getAvailableMetaZoneIDs() {
        return _getAvailableMetaZoneIDs();
    }

    static Set<String> _getAvailableMetaZoneIDs() {
        if (METAZONE_IDS == null) {
            synchronized (TimeZoneNamesImpl.class) {
                if (METAZONE_IDS == null) {
                    METAZONE_IDS = Collections.unmodifiableSet(UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "metaZones").get("mapTimezones").keySet());
                }
            }
        }
        return METAZONE_IDS;
    }

    public Set<String> getAvailableMetaZoneIDs(String tzID) {
        return _getAvailableMetaZoneIDs(tzID);
    }

    static Set<String> _getAvailableMetaZoneIDs(String tzID) {
        if (tzID == null || tzID.length() == 0) {
            return Collections.emptySet();
        }
        List<MZMapEntry> maps = (List) TZ_TO_MZS_CACHE.getInstance(tzID, tzID);
        if (maps.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> mzIDs = new HashSet(maps.size());
        for (MZMapEntry map : maps) {
            mzIDs.add(map.mzID());
        }
        return Collections.unmodifiableSet(mzIDs);
    }

    public String getMetaZoneID(String tzID, long date) {
        return _getMetaZoneID(tzID, date);
    }

    static String _getMetaZoneID(String tzID, long date) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        String mzID = null;
        for (MZMapEntry map : (List) TZ_TO_MZS_CACHE.getInstance(tzID, tzID)) {
            if (date >= map.from() && date < map.to()) {
                mzID = map.mzID();
                break;
            }
        }
        return mzID;
    }

    public String getReferenceZoneID(String mzID, String region) {
        return _getReferenceZoneID(mzID, region);
    }

    static String _getReferenceZoneID(String mzID, String region) {
        if (mzID == null || mzID.length() == 0) {
            return null;
        }
        String str = null;
        Map<String, String> regionTzMap = (Map) MZ_TO_TZS_CACHE.getInstance(mzID, mzID);
        if (!regionTzMap.isEmpty()) {
            str = (String) regionTzMap.get(region);
            if (str == null) {
                str = (String) regionTzMap.get("001");
            }
        }
        return str;
    }

    public String getMetaZoneDisplayName(String mzID, NameType type) {
        if (mzID == null || mzID.length() == 0) {
            return null;
        }
        return loadMetaZoneNames(null, mzID).getName(type);
    }

    public String getTimeZoneDisplayName(String tzID, NameType type) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        return loadTimeZoneNames(null, tzID).getName(type);
    }

    public String getExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        return loadTimeZoneNames(null, tzID).getName(NameType.EXEMPLAR_LOCATION);
    }

    public synchronized Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> nameTypes) {
        if (text != null) {
            if (text.length() != 0 && start >= 0) {
                if (start < text.length()) {
                    ResultHandler handler = new NameSearchHandler(nameTypes);
                    this._namesTrie.find(text, start, handler);
                    if (handler.getMaxMatchLen() == text.length() - start || this._namesTrieFullyLoaded) {
                        return handler.getMatches();
                    }
                    addAllNamesIntoTrie();
                    handler.resetResults();
                    this._namesTrie.find(text, start, handler);
                    if (handler.getMaxMatchLen() == text.length() - start) {
                        return handler.getMatches();
                    }
                    internalLoadAllDisplayNames();
                    addAllNamesIntoTrie();
                    for (String tzID : TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null)) {
                        String tzID2;
                        if (!this._tzNamesMap.containsKey(tzID2)) {
                            tzID2 = tzID2.intern();
                            ZNames tznames = ZNames.getInstance(null, tzID2);
                            tznames.addNamesIntoTrie(null, tzID2, this._namesTrie);
                            this._tzNamesMap.put(tzID2, tznames);
                        }
                    }
                    this._namesTrieFullyLoaded = true;
                    handler.resetResults();
                    this._namesTrie.find(text, start, handler);
                    return handler.getMatches();
                }
            }
        }
        throw new IllegalArgumentException("bad input text or range");
    }

    public synchronized void loadAllDisplayNames() {
        internalLoadAllDisplayNames();
    }

    public void getDisplayNames(String tzID, NameType[] types, long date, String[] dest, int destOffset) {
        if (tzID != null && tzID.length() != 0) {
            ZNames tzNames = loadTimeZoneNames(null, tzID);
            ZNames mzNames = null;
            for (int i = 0; i < types.length; i++) {
                NameType type = types[i];
                String name = tzNames.getName(type);
                if (name == null) {
                    if (mzNames == null) {
                        String mzID = getMetaZoneID(tzID, date);
                        if (mzID == null || mzID.length() == 0) {
                            mzNames = ZNames.EMPTY_ZNAMES;
                        } else {
                            mzNames = loadMetaZoneNames(null, mzID);
                        }
                    }
                    name = mzNames.getName(type);
                }
                dest[destOffset + i] = name;
            }
        }
    }

    private void internalLoadAllDisplayNames() {
        if (!this._namesFullyLoaded) {
            new ZoneStringsLoader(this, null).load();
            this._namesFullyLoaded = true;
        }
    }

    private void addAllNamesIntoTrie() {
        for (Entry<String, ZNames> entry : this._tzNamesMap.entrySet()) {
            ((ZNames) entry.getValue()).addNamesIntoTrie(null, (String) entry.getKey(), this._namesTrie);
        }
        for (Entry<String, ZNames> entry2 : this._mzNamesMap.entrySet()) {
            ((ZNames) entry2.getValue()).addNamesIntoTrie((String) entry2.getKey(), null, this._namesTrie);
        }
    }

    private void initialize(ULocale locale) {
        this._zoneStrings = (ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b/zone", locale)).get(ZONE_STRINGS_BUNDLE);
        this._tzNamesMap = new ConcurrentHashMap();
        this._mzNamesMap = new ConcurrentHashMap();
        this._namesFullyLoaded = false;
        this._namesTrie = new TextTrieMap(true);
        this._namesTrieFullyLoaded = false;
        String tzCanonicalID = ZoneMeta.getCanonicalCLDRID(TimeZone.getDefault());
        if (tzCanonicalID != null) {
            loadStrings(tzCanonicalID);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x000a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadStrings(String tzCanonicalID) {
        if (tzCanonicalID != null) {
            if (tzCanonicalID.length() != 0) {
                loadTimeZoneNames(null, tzCanonicalID);
                ZNamesLoader loader = ZNamesLoader.forMetaZoneNames();
                for (String mzID : getAvailableMetaZoneIDs(tzCanonicalID)) {
                    loadMetaZoneNames(loader, mzID);
                }
                addAllNamesIntoTrie();
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this._zoneStrings.getULocale());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        initialize((ULocale) in.readObject());
    }

    private synchronized ZNames loadMetaZoneNames(ZNamesLoader loader, String mzID) {
        ZNames znames;
        znames = (ZNames) this._mzNamesMap.get(mzID);
        if (znames == null) {
            if (loader == null) {
                loader = ZNamesLoader.forMetaZoneNames();
            }
            znames = ZNames.getInstance(loader, this._zoneStrings, MZ_PREFIX + mzID, null);
            mzID = mzID.intern();
            if (this._namesTrieFullyLoaded) {
                znames.addNamesIntoTrie(mzID, null, this._namesTrie);
            }
            this._mzNamesMap.put(mzID, znames);
        }
        return znames;
    }

    private synchronized ZNames loadTimeZoneNames(ZNamesLoader loader, String tzID) {
        ZNames tznames;
        tznames = (ZNames) this._tzNamesMap.get(tzID);
        if (tznames == null) {
            if (loader == null) {
                loader = ZNamesLoader.forTimeZoneNames();
            }
            tznames = ZNames.getInstance(loader, this._zoneStrings, tzID.replace('/', ':'), tzID);
            tzID = tzID.intern();
            if (this._namesTrieFullyLoaded) {
                tznames.addNamesIntoTrie(null, tzID, this._namesTrie);
            }
            this._tzNamesMap.put(tzID, tznames);
        }
        return tznames;
    }

    public static String getDefaultExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0 || LOC_EXCLUSION_PATTERN.matcher(tzID).matches()) {
            return null;
        }
        String location = null;
        int sep = tzID.lastIndexOf(47);
        if (sep > 0 && sep + 1 < tzID.length()) {
            location = tzID.substring(sep + 1).replace('_', ' ');
        }
        return location;
    }
}

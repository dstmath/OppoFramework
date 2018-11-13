package android.icu.impl;

import android.icu.impl.ICURWLock.Stats;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
public class ICUService extends ICUNotifier {
    private static final boolean DEBUG = false;
    private SoftReference<Map<String, CacheEntry>> cacheref;
    private int defaultSize;
    private LocaleRef dnref;
    private final List<Factory> factories;
    private final ICURWLock factoryLock;
    private SoftReference<Map<String, Factory>> idref;
    protected final String name;

    public interface Factory {
        Object create(Key key, ICUService iCUService);

        String getDisplayName(String str, ULocale uLocale);

        void updateVisibleIDs(Map<String, Factory> map);
    }

    public static class Key {
        private final String id;

        public Key(String id) {
            this.id = id;
        }

        public final String id() {
            return this.id;
        }

        public String canonicalID() {
            return this.id;
        }

        public String currentID() {
            return canonicalID();
        }

        public String currentDescriptor() {
            return "/" + currentID();
        }

        public boolean fallback() {
            return false;
        }

        public boolean isFallbackOf(String idToCheck) {
            return canonicalID().equals(idToCheck);
        }
    }

    private static final class CacheEntry {
        final String actualDescriptor;
        final Object service;

        CacheEntry(String actualDescriptor, Object service) {
            this.actualDescriptor = actualDescriptor;
            this.service = service;
        }
    }

    private static class LocaleRef {
        private Comparator<Object> com;
        private final ULocale locale;
        private SoftReference<SortedMap<String, String>> ref;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUService.LocaleRef.<init>(java.util.SortedMap, android.icu.util.ULocale, java.util.Comparator):void, dex: 
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
        LocaleRef(java.util.SortedMap<java.lang.String, java.lang.String> r1, android.icu.util.ULocale r2, java.util.Comparator<java.lang.Object> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUService.LocaleRef.<init>(java.util.SortedMap, android.icu.util.ULocale, java.util.Comparator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.LocaleRef.<init>(java.util.SortedMap, android.icu.util.ULocale, java.util.Comparator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUService.LocaleRef.get(android.icu.util.ULocale, java.util.Comparator):java.util.SortedMap<java.lang.String, java.lang.String>, dex: 
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
        java.util.SortedMap<java.lang.String, java.lang.String> get(android.icu.util.ULocale r1, java.util.Comparator<java.lang.Object> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUService.LocaleRef.get(android.icu.util.ULocale, java.util.Comparator):java.util.SortedMap<java.lang.String, java.lang.String>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.LocaleRef.get(android.icu.util.ULocale, java.util.Comparator):java.util.SortedMap<java.lang.String, java.lang.String>");
        }
    }

    public interface ServiceListener extends EventListener {
        void serviceChanged(ICUService iCUService);
    }

    public static class SimpleFactory implements Factory {
        protected String id;
        protected Object instance;
        protected boolean visible;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUService.SimpleFactory.<init>(java.lang.Object, java.lang.String):void, dex: 
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
        public SimpleFactory(java.lang.Object r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUService.SimpleFactory.<init>(java.lang.Object, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.SimpleFactory.<init>(java.lang.Object, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUService.SimpleFactory.<init>(java.lang.Object, java.lang.String, boolean):void, dex: 
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
        public SimpleFactory(java.lang.Object r1, java.lang.String r2, boolean r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.ICUService.SimpleFactory.<init>(java.lang.Object, java.lang.String, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.SimpleFactory.<init>(java.lang.Object, java.lang.String, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUService.SimpleFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex: 
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
        public java.lang.Object create(android.icu.impl.ICUService.Key r1, android.icu.impl.ICUService r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.ICUService.SimpleFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.SimpleFactory.create(android.icu.impl.ICUService$Key, android.icu.impl.ICUService):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.ICUService.SimpleFactory.getDisplayName(java.lang.String, android.icu.util.ULocale):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String getDisplayName(java.lang.String r1, android.icu.util.ULocale r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.ICUService.SimpleFactory.getDisplayName(java.lang.String, android.icu.util.ULocale):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.SimpleFactory.getDisplayName(java.lang.String, android.icu.util.ULocale):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUService.SimpleFactory.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.ICUService.SimpleFactory.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.SimpleFactory.toString():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.impl.ICUService.SimpleFactory.updateVisibleIDs(java.util.Map):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void updateVisibleIDs(java.util.Map<java.lang.String, android.icu.impl.ICUService.Factory> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.impl.ICUService.SimpleFactory.updateVisibleIDs(java.util.Map):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.SimpleFactory.updateVisibleIDs(java.util.Map):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.ICUService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.<clinit>():void");
    }

    public ICUService() {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = "";
    }

    public ICUService(String name) {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = name;
    }

    public Object get(String descriptor) {
        return getKey(createKey(descriptor), null);
    }

    public Object get(String descriptor, String[] actualReturn) {
        if (descriptor != null) {
            return getKey(createKey(descriptor), actualReturn);
        }
        throw new NullPointerException("descriptor must not be null");
    }

    public Object getKey(Key key) {
        return getKey(key, null);
    }

    public Object getKey(Key key, String[] actualReturn) {
        return getKey(key, actualReturn, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x03b4  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0133 A:{Catch:{ all -> 0x011b }} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0230 A:{Catch:{ all -> 0x011b }} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0173 A:{Catch:{ all -> 0x011b }} */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x037e  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x019e A:{Catch:{ all -> 0x011b }} */
    /* JADX WARNING: Missing block: B:32:0x011c, code:
            r24.factoryLock.releaseRead();
     */
    /* JADX WARNING: Missing block: B:33:0x0125, code:
            throw r21;
     */
    /* JADX WARNING: Missing block: B:69:0x02a5, code:
            r0 = new android.icu.impl.ICUService.CacheEntry(r8, r19);
     */
    /* JADX WARNING: Missing block: B:72:0x02b0, code:
            if (DEBUG == false) goto L_0x02de;
     */
    /* JADX WARNING: Missing block: B:73:0x02b2, code:
            java.lang.System.out.println(r24.name + " factory supported: " + r8 + ", caching");
     */
    /* JADX WARNING: Missing block: B:74:0x02de, code:
            r17 = r0;
     */
    /* JADX WARNING: Missing block: B:83:0x0316, code:
            if (r25.fallback() != false) goto L_0x0318;
     */
    /* JADX WARNING: Missing block: B:104:0x03af, code:
            r21 = th;
     */
    /* JADX WARNING: Missing block: B:105:0x03b0, code:
            r17 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Object getKey(Key key, String[] actualReturn, Factory factory) {
        if (this.factories.size() == 0) {
            return handleDefault(key, actualReturn);
        }
        if (DEBUG) {
            System.out.println("Service: " + this.name + " key: " + key.canonicalID());
        }
        if (key != null) {
            try {
                int NDebug;
                String currentDescriptor;
                CacheEntry result;
                this.factoryLock.acquireRead();
                Map cache = null;
                SoftReference<Map<String, CacheEntry>> cref = this.cacheref;
                if (cref != null) {
                    if (DEBUG) {
                        System.out.println("Service " + this.name + " ref exists");
                    }
                    cache = (Map) cref.get();
                }
                if (cache == null) {
                    if (DEBUG) {
                        System.out.println("Service " + this.name + " cache was empty");
                    }
                    cache = Collections.synchronizedMap(new HashMap());
                    cref = new SoftReference(cache);
                }
                Iterable iterable = null;
                boolean putInCache = false;
                int NDebug2 = 0;
                int startIndex = 0;
                int limit = this.factories.size();
                boolean cacheResult = true;
                if (factory != null) {
                    for (int i = 0; i < limit; i++) {
                        if (factory == this.factories.get(i)) {
                            startIndex = i + 1;
                            break;
                        }
                    }
                    if (startIndex == 0) {
                        throw new IllegalStateException("Factory " + factory + "not registered with service: " + this);
                    }
                    cacheResult = false;
                    NDebug = 0;
                    currentDescriptor = key.currentDescriptor();
                    if (DEBUG) {
                        NDebug2 = NDebug;
                    } else {
                        NDebug2 = NDebug + 1;
                        System.out.println(this.name + "[" + NDebug + "] looking for: " + currentDescriptor);
                    }
                    result = (CacheEntry) cache.get(currentDescriptor);
                    if (result == null) {
                        if (DEBUG) {
                            System.out.println("did not find: " + currentDescriptor + " in cache");
                        }
                        putInCache = cacheResult;
                        int i2 = startIndex;
                        while (i2 < limit) {
                            int index = i2 + 1;
                            Factory f = (Factory) this.factories.get(i2);
                            if (DEBUG) {
                                System.out.println("trying factory[" + (index - 1) + "] " + f.toString());
                            }
                            Object service = f.create(key, this);
                            if (service != null) {
                                break;
                            }
                            if (DEBUG) {
                                System.out.println("factory did not support: " + currentDescriptor);
                            }
                            i2 = index;
                        }
                        if (iterable == null) {
                            iterable = new ArrayList(5);
                        }
                        iterable.add(currentDescriptor);
                    } else if (DEBUG) {
                        System.out.println(this.name + " found with descriptor: " + currentDescriptor);
                    }
                    if (result == null) {
                        if (putInCache) {
                            if (DEBUG) {
                                System.out.println("caching '" + result.actualDescriptor + "'");
                            }
                            cache.put(result.actualDescriptor, result);
                            if (r5 != null) {
                                for (String desc : r5) {
                                    if (DEBUG) {
                                        System.out.println(this.name + " adding descriptor: '" + desc + "' for actual: '" + result.actualDescriptor + "'");
                                    }
                                    cache.put(desc, result);
                                }
                            }
                            this.cacheref = cref;
                        }
                        if (actualReturn != null) {
                            if (result.actualDescriptor.indexOf("/") == 0) {
                                actualReturn[0] = result.actualDescriptor.substring(1);
                            } else {
                                actualReturn[0] = result.actualDescriptor;
                            }
                        }
                        if (DEBUG) {
                            System.out.println("found in service: " + this.name);
                        }
                        Object obj = result.service;
                        this.factoryLock.releaseRead();
                        return obj;
                    }
                    this.factoryLock.releaseRead();
                }
                NDebug = NDebug2;
                currentDescriptor = key.currentDescriptor();
                if (DEBUG) {
                }
                result = (CacheEntry) cache.get(currentDescriptor);
                if (result == null) {
                }
                if (result == null) {
                }
            } catch (Throwable th) {
                Throwable th2 = th;
            }
        }
        if (DEBUG) {
            System.out.println("not found in service: " + this.name);
        }
        return handleDefault(key, actualReturn);
    }

    protected Object handleDefault(Key key, String[] actualIDReturn) {
        return null;
    }

    public Set<String> getVisibleIDs() {
        return getVisibleIDs(null);
    }

    public Set<String> getVisibleIDs(String matchID) {
        Set<String> result = getVisibleIDMap().keySet();
        Key fallbackKey = createKey(matchID);
        if (fallbackKey == null) {
            return result;
        }
        Set<String> temp = new HashSet(result.size());
        for (String id : result) {
            if (fallbackKey.isFallbackOf(id)) {
                temp.add(id);
            }
        }
        return temp;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0033 A:{Catch:{ all -> 0x003d }} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x000e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Map<String, Factory> getVisibleIDMap() {
        Map<String, Factory> idcache;
        Throwable th;
        Map<String, Factory> idcache2 = null;
        SoftReference<Map<String, Factory>> ref = this.idref;
        if (ref != null) {
            idcache = (Map) ref.get();
            if (idcache != null) {
                synchronized (this) {
                    ListIterator<Factory> lIter;
                    try {
                        try {
                            try {
                                lIter = this.factories.listIterator(this.factories.size());
                                while (lIter.hasPrevious()) {
                                }
                                idcache2 = Collections.unmodifiableMap(idcache2);
                                this.idref = new SoftReference(idcache2);
                                this.factoryLock.releaseRead();
                            } catch (Throwable th2) {
                                th = th2;
                                idcache2 = idcache;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            idcache2 = idcache;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        try {
                            this.factoryLock.releaseRead();
                            throw th;
                        } catch (Throwable th5) {
                            th = th5;
                            throw th;
                        }
                    }
                    if (ref == this.idref || this.idref == null) {
                        this.factoryLock.acquireRead();
                        idcache2 = new HashMap();
                        lIter = this.factories.listIterator(this.factories.size());
                        while (lIter.hasPrevious()) {
                            ((Factory) lIter.previous()).updateVisibleIDs(idcache2);
                        }
                        idcache2 = Collections.unmodifiableMap(idcache2);
                        this.idref = new SoftReference(idcache2);
                        this.factoryLock.releaseRead();
                    } else {
                        ref = this.idref;
                        idcache2 = (Map) ref.get();
                    }
                    this.factoryLock.acquireRead();
                    idcache2 = new HashMap();
                }
            }
            return idcache;
        }
        idcache = idcache2;
        if (idcache != null) {
            return idcache;
        }
        return idcache;
    }

    public String getDisplayName(String id) {
        return getDisplayName(id, ULocale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(String id, ULocale locale) {
        Map<String, Factory> m = getVisibleIDMap();
        Factory f = (Factory) m.get(id);
        if (f != null) {
            return f.getDisplayName(id, locale);
        }
        Key key = createKey(id);
        while (key.fallback()) {
            f = (Factory) m.get(key.currentID());
            if (f != null) {
                return f.getDisplayName(id, locale);
            }
        }
        return null;
    }

    public SortedMap<String, String> getDisplayNames() {
        return getDisplayNames(ULocale.getDefault(Category.DISPLAY), null, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale locale) {
        return getDisplayNames(locale, null, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale locale, Comparator<Object> com) {
        return getDisplayNames(locale, com, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale locale, String matchID) {
        return getDisplayNames(locale, null, matchID);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030 A:{Catch:{ all -> 0x004a }} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x000e  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006e A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SortedMap<String, String> getDisplayNames(ULocale locale, Comparator<Object> com, String matchID) {
        SortedMap<String, String> dncache;
        Throwable th;
        Key matchKey;
        SortedMap<String, String> dncache2 = null;
        LocaleRef ref = this.dnref;
        if (ref != null) {
            dncache = ref.get(locale, com);
            if (dncache != null) {
                synchronized (this) {
                    try {
                        try {
                            for (Entry<String, Factory> e : getVisibleIDMap().entrySet()) {
                            }
                            dncache2 = Collections.unmodifiableSortedMap(dncache2);
                            this.dnref = new LocaleRef(dncache2, locale, com);
                        } catch (Throwable th2) {
                            th = th2;
                            dncache2 = dncache;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                    if (ref == this.dnref || this.dnref == null) {
                        dncache2 = new TreeMap(com);
                        for (Entry<String, Factory> e2 : getVisibleIDMap().entrySet()) {
                            String id = (String) e2.getKey();
                            dncache2.put(((Factory) e2.getValue()).getDisplayName(id, locale), id);
                        }
                        dncache2 = Collections.unmodifiableSortedMap(dncache2);
                        this.dnref = new LocaleRef(dncache2, locale, com);
                    } else {
                        ref = this.dnref;
                        dncache2 = ref.get(locale, com);
                    }
                    dncache2 = new TreeMap(com);
                }
            }
            matchKey = createKey(matchID);
            if (matchKey != null) {
                return dncache;
            }
            SortedMap<String, String> result = new TreeMap(dncache);
            Iterator<Entry<String, String>> iter = result.entrySet().iterator();
            while (iter.hasNext()) {
                if (!matchKey.isFallbackOf((String) ((Entry) iter.next()).getValue())) {
                    iter.remove();
                }
            }
            return result;
        }
        dncache = dncache2;
        if (dncache != null) {
            matchKey = createKey(matchID);
        }
        matchKey = createKey(matchID);
        if (matchKey != null) {
        }
    }

    public final List<Factory> factories() {
        try {
            this.factoryLock.acquireRead();
            List<Factory> arrayList = new ArrayList(this.factories);
            return arrayList;
        } finally {
            this.factoryLock.releaseRead();
        }
    }

    public Factory registerObject(Object obj, String id) {
        return registerObject(obj, id, true);
    }

    public Factory registerObject(Object obj, String id, boolean visible) {
        return registerFactory(new SimpleFactory(obj, createKey(id).canonicalID(), visible));
    }

    public final Factory registerFactory(Factory factory) {
        if (factory == null) {
            throw new NullPointerException();
        }
        try {
            this.factoryLock.acquireWrite();
            this.factories.add(0, factory);
            clearCaches();
            notifyChanged();
            return factory;
        } finally {
            this.factoryLock.releaseWrite();
        }
    }

    public final boolean unregisterFactory(Factory factory) {
        if (factory == null) {
            throw new NullPointerException();
        }
        boolean result = false;
        try {
            this.factoryLock.acquireWrite();
            if (this.factories.remove(factory)) {
                result = true;
                clearCaches();
            }
            this.factoryLock.releaseWrite();
            if (result) {
                notifyChanged();
            }
            return result;
        } catch (Throwable th) {
            this.factoryLock.releaseWrite();
        }
    }

    public final void reset() {
        try {
            this.factoryLock.acquireWrite();
            reInitializeFactories();
            clearCaches();
            notifyChanged();
        } finally {
            this.factoryLock.releaseWrite();
        }
    }

    protected void reInitializeFactories() {
        this.factories.clear();
    }

    public boolean isDefault() {
        return this.factories.size() == this.defaultSize;
    }

    protected void markDefault() {
        this.defaultSize = this.factories.size();
    }

    public Key createKey(String id) {
        return id == null ? null : new Key(id);
    }

    protected void clearCaches() {
        this.cacheref = null;
        this.idref = null;
        this.dnref = null;
    }

    protected void clearServiceCache() {
        this.cacheref = null;
    }

    protected boolean acceptsListener(EventListener l) {
        return l instanceof ServiceListener;
    }

    protected void notifyListener(EventListener l) {
        ((ServiceListener) l).serviceChanged(this);
    }

    public String stats() {
        Stats stats = this.factoryLock.resetStats();
        if (stats != null) {
            return stats.toString();
        }
        return "no stats";
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return super.toString() + "{" + this.name + "}";
    }
}

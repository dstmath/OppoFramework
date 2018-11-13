package sun.security.jca;

import java.security.Provider;

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
public class Providers {
    private static final String BACKUP_PROVIDER_CLASSNAME = "sun.security.provider.VerificationProvider";
    private static final String[] jarVerificationProviders = null;
    private static volatile ProviderList providerList;
    private static final ThreadLocal<ProviderList> threadLists = null;
    private static volatile int threadListsUsed;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: sun.security.jca.Providers.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: sun.security.jca.Providers.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.jca.Providers.<clinit>():void");
    }

    private Providers() {
    }

    public static Provider getSunProvider() {
        try {
            return (Provider) Class.forName(jarVerificationProviders[0]).newInstance();
        } catch (Exception e) {
            try {
                return (Provider) Class.forName(BACKUP_PROVIDER_CLASSNAME).newInstance();
            } catch (Exception e2) {
                throw new RuntimeException("Sun provider not found", e);
            }
        }
    }

    public static Object startJarVerification() {
        return beginThreadProviderList(getProviderList().getJarList(jarVerificationProviders));
    }

    public static void stopJarVerification(Object obj) {
        endThreadProviderList((ProviderList) obj);
    }

    public static ProviderList getProviderList() {
        ProviderList list = getThreadProviderList();
        if (list == null) {
            return getSystemProviderList();
        }
        return list;
    }

    public static void setProviderList(ProviderList newList) {
        if (getThreadProviderList() == null) {
            setSystemProviderList(newList);
        } else {
            changeThreadProviderList(newList);
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0014, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:12:0x0016, code:
            r0 = getSystemProviderList();
            r1 = r0.removeInvalid();
     */
    /* JADX WARNING: Missing block: B:13:0x001e, code:
            if (r1 == r0) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:14:0x0020, code:
            setSystemProviderList(r1);
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:15:0x0024, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ProviderList getFullProviderList() {
        synchronized (Providers.class) {
            ProviderList list = getThreadProviderList();
            if (list != null) {
                ProviderList newList = list.removeInvalid();
                if (newList != list) {
                    changeThreadProviderList(newList);
                    list = newList;
                }
            }
        }
    }

    private static ProviderList getSystemProviderList() {
        return providerList;
    }

    private static void setSystemProviderList(ProviderList list) {
        providerList = list;
    }

    public static ProviderList getThreadProviderList() {
        if (threadListsUsed == 0) {
            return null;
        }
        return (ProviderList) threadLists.get();
    }

    private static void changeThreadProviderList(ProviderList list) {
        threadLists.set(list);
    }

    public static synchronized ProviderList beginThreadProviderList(ProviderList list) {
        ProviderList oldList;
        synchronized (Providers.class) {
            if (ProviderList.debug != null) {
                ProviderList.debug.println("ThreadLocal providers: " + list);
            }
            oldList = (ProviderList) threadLists.get();
            threadListsUsed++;
            threadLists.set(list);
        }
        return oldList;
    }

    public static synchronized void endThreadProviderList(ProviderList list) {
        synchronized (Providers.class) {
            if (list == null) {
                if (ProviderList.debug != null) {
                    ProviderList.debug.println("Disabling ThreadLocal providers");
                }
                threadLists.remove();
            } else {
                if (ProviderList.debug != null) {
                    ProviderList.debug.println("Restoring previous ThreadLocal providers: " + list);
                }
                threadLists.set(list);
            }
            threadListsUsed--;
        }
    }
}

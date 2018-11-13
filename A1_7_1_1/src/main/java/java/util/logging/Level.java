package java.util.logging;

import dalvik.system.VMStack;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
public class Level implements Serializable {
    public static final Level ALL = null;
    public static final Level CONFIG = null;
    public static final Level FINE = null;
    public static final Level FINER = null;
    public static final Level FINEST = null;
    public static final Level INFO = null;
    public static final Level OFF = null;
    public static final Level SEVERE = null;
    public static final Level WARNING = null;
    private static String defaultBundle = null;
    private static final long serialVersionUID = -8176160795706313070L;
    private String localizedLevelName;
    private final String name;
    private transient ResourceBundle rb;
    private final String resourceBundleName;
    private final int value;

    static final class KnownLevel {
        private static Map<Integer, List<KnownLevel>> intToLevels;
        private static Map<String, List<KnownLevel>> nameToLevels;
        final Level levelObject;
        final Level mirroredLevel;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.logging.Level.KnownLevel.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.logging.Level.KnownLevel.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.Level.KnownLevel.<clinit>():void");
        }

        KnownLevel(Level l) {
            this.levelObject = l;
            if (l.getClass() == Level.class) {
                this.mirroredLevel = l;
            } else {
                this.mirroredLevel = new Level(l.name, l.value, l.resourceBundleName);
            }
        }

        static synchronized void add(Level l) {
            synchronized (KnownLevel.class) {
                KnownLevel o = new KnownLevel(l);
                List<KnownLevel> list = (List) nameToLevels.get(l.name);
                if (list == null) {
                    list = new ArrayList();
                    nameToLevels.put(l.name, list);
                }
                list.add(o);
                list = (List) intToLevels.get(Integer.valueOf(l.value));
                if (list == null) {
                    list = new ArrayList();
                    intToLevels.put(Integer.valueOf(l.value), list);
                }
                list.add(o);
            }
        }

        static synchronized KnownLevel findByName(String name) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) nameToLevels.get(name);
                if (list != null) {
                    KnownLevel knownLevel = (KnownLevel) list.get(0);
                    return knownLevel;
                }
                return null;
            }
        }

        static synchronized KnownLevel findByValue(int value) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) intToLevels.get(Integer.valueOf(value));
                if (list != null) {
                    KnownLevel knownLevel = (KnownLevel) list.get(0);
                    return knownLevel;
                }
                return null;
            }
        }

        static synchronized KnownLevel findByLocalizedLevelName(String name) {
            synchronized (KnownLevel.class) {
                for (List<KnownLevel> levels : nameToLevels.values()) {
                    for (KnownLevel l : levels) {
                        if (name.equals(l.levelObject.getLocalizedLevelName())) {
                            return l;
                        }
                    }
                }
                return null;
            }
        }

        static synchronized KnownLevel findByLocalizedName(String name) {
            synchronized (KnownLevel.class) {
                for (List<KnownLevel> levels : nameToLevels.values()) {
                    for (KnownLevel l : levels) {
                        if (name.equals(l.levelObject.getLocalizedName())) {
                            return l;
                        }
                    }
                }
                return null;
            }
        }

        /* JADX WARNING: Missing block: B:17:0x004d, code:
            return r0;
     */
        /* JADX WARNING: Missing block: B:19:0x004f, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static synchronized KnownLevel matches(Level l) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) nameToLevels.get(l.name);
                if (list != null) {
                    for (KnownLevel level : list) {
                        Level other = level.mirroredLevel;
                        if (l.value != other.value || (l.resourceBundleName != other.resourceBundleName && (l.resourceBundleName == null || !l.resourceBundleName.equals(other.resourceBundleName)))) {
                        }
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.logging.Level.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.logging.Level.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.Level.<clinit>():void");
    }

    protected Level(String name, int value) {
        this(name, value, null);
    }

    protected Level(String name, int value, String resourceBundleName) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
        this.resourceBundleName = resourceBundleName;
        if (resourceBundleName != null) {
            try {
                ClassLoader cl = VMStack.getCallingClassLoader();
                if (cl != null) {
                    this.rb = ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
                } else {
                    this.rb = ResourceBundle.getBundle(resourceBundleName);
                }
            } catch (MissingResourceException e) {
                this.rb = null;
            }
        }
        if (resourceBundleName != null) {
            name = null;
        }
        this.localizedLevelName = name;
        KnownLevel.add(this);
    }

    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    public String getName() {
        return this.name;
    }

    public String getLocalizedName() {
        return getLocalizedLevelName();
    }

    final String getLevelName() {
        return this.name;
    }

    final synchronized String getLocalizedLevelName() {
        if (this.localizedLevelName != null) {
            return this.localizedLevelName;
        }
        try {
            this.localizedLevelName = this.rb.getString(this.name);
        } catch (Exception e) {
            this.localizedLevelName = this.name;
        }
        return this.localizedLevelName;
    }

    static Level findLevel(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        KnownLevel level = KnownLevel.findByName(name);
        if (level != null) {
            return level.mirroredLevel;
        }
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x);
            if (level == null) {
                Level levelObject = new Level(name, x);
                level = KnownLevel.findByValue(x);
            }
            return level.mirroredLevel;
        } catch (NumberFormatException e) {
            level = KnownLevel.findByLocalizedLevelName(name);
            if (level != null) {
                return level.mirroredLevel;
            }
            return null;
        }
    }

    public final String toString() {
        return this.name;
    }

    public final int intValue() {
        return this.value;
    }

    private Object readResolve() {
        KnownLevel o = KnownLevel.matches(this);
        if (o != null) {
            return o.levelObject;
        }
        return new Level(this.name, this.value, this.resourceBundleName);
    }

    public static synchronized Level parse(String name) throws IllegalArgumentException {
        synchronized (Level.class) {
            name.length();
            KnownLevel level = KnownLevel.findByName(name);
            Level level2;
            if (level != null) {
                level2 = level.levelObject;
                return level2;
            }
            try {
                int x = Integer.parseInt(name);
                level = KnownLevel.findByValue(x);
                if (level == null) {
                    Level levelObject = new Level(name, x);
                    level = KnownLevel.findByValue(x);
                }
                level2 = level.levelObject;
                return level2;
            } catch (NumberFormatException e) {
                level = KnownLevel.findByLocalizedName(name);
                if (level != null) {
                    return level.levelObject;
                }
                throw new IllegalArgumentException("Bad level \"" + name + "\"");
            }
        }
    }

    public boolean equals(Object ox) {
        boolean z = false;
        try {
            if (((Level) ox).value == this.value) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        return this.value;
    }
}

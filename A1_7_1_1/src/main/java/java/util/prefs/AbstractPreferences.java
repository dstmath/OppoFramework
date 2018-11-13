package java.util.prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class AbstractPreferences extends Preferences {
    private static final AbstractPreferences[] EMPTY_ABSTRACT_PREFS_ARRAY = null;
    private static final String[] EMPTY_STRING_ARRAY = null;
    private static Thread eventDispatchThread;
    private static final List<EventObject> eventQueue = null;
    private final String absolutePath;
    private Map<String, AbstractPreferences> kidCache;
    protected final Object lock;
    private final String name;
    protected boolean newNode;
    private final ArrayList<NodeChangeListener> nodeListeners;
    final AbstractPreferences parent;
    private final ArrayList<PreferenceChangeListener> prefListeners;
    private boolean removed;
    private final AbstractPreferences root;

    /* renamed from: java.util.prefs.AbstractPreferences$1 */
    class AnonymousClass1 implements PrivilegedAction<Boolean> {
        final /* synthetic */ AbstractPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.AbstractPreferences.1.<init>(java.util.prefs.AbstractPreferences):void, dex: 
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
        AnonymousClass1(java.util.prefs.AbstractPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.AbstractPreferences.1.<init>(java.util.prefs.AbstractPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.1.<init>(java.util.prefs.AbstractPreferences):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.AbstractPreferences.1.run():java.lang.Boolean, dex: 
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
        public java.lang.Boolean run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.AbstractPreferences.1.run():java.lang.Boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.1.run():java.lang.Boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.AbstractPreferences.1.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.AbstractPreferences.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.1.run():java.lang.Object");
        }
    }

    private static class EventDispatchThread extends Thread {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.prefs.AbstractPreferences.EventDispatchThread.<init>():void, dex: 
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
        private EventDispatchThread() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.prefs.AbstractPreferences.EventDispatchThread.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.EventDispatchThread.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.prefs.AbstractPreferences.EventDispatchThread.<init>(java.util.prefs.AbstractPreferences$EventDispatchThread):void, dex: 
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
        /* synthetic */ EventDispatchThread(java.util.prefs.AbstractPreferences.EventDispatchThread r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.prefs.AbstractPreferences.EventDispatchThread.<init>(java.util.prefs.AbstractPreferences$EventDispatchThread):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.EventDispatchThread.<init>(java.util.prefs.AbstractPreferences$EventDispatchThread):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.AbstractPreferences.EventDispatchThread.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.AbstractPreferences.EventDispatchThread.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.EventDispatchThread.run():void");
        }
    }

    private class NodeAddedEvent extends NodeChangeEvent {
        private static final long serialVersionUID = -6743557530157328528L;
        final /* synthetic */ AbstractPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.AbstractPreferences.NodeAddedEvent.<init>(java.util.prefs.AbstractPreferences, java.util.prefs.Preferences, java.util.prefs.Preferences):void, dex: 
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
        NodeAddedEvent(java.util.prefs.AbstractPreferences r1, java.util.prefs.Preferences r2, java.util.prefs.Preferences r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.AbstractPreferences.NodeAddedEvent.<init>(java.util.prefs.AbstractPreferences, java.util.prefs.Preferences, java.util.prefs.Preferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.NodeAddedEvent.<init>(java.util.prefs.AbstractPreferences, java.util.prefs.Preferences, java.util.prefs.Preferences):void");
        }
    }

    private class NodeRemovedEvent extends NodeChangeEvent {
        private static final long serialVersionUID = 8735497392918824837L;
        final /* synthetic */ AbstractPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.AbstractPreferences.NodeRemovedEvent.<init>(java.util.prefs.AbstractPreferences, java.util.prefs.Preferences, java.util.prefs.Preferences):void, dex: 
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
        NodeRemovedEvent(java.util.prefs.AbstractPreferences r1, java.util.prefs.Preferences r2, java.util.prefs.Preferences r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.AbstractPreferences.NodeRemovedEvent.<init>(java.util.prefs.AbstractPreferences, java.util.prefs.Preferences, java.util.prefs.Preferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.NodeRemovedEvent.<init>(java.util.prefs.AbstractPreferences, java.util.prefs.Preferences, java.util.prefs.Preferences):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.prefs.AbstractPreferences.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.prefs.AbstractPreferences.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.AbstractPreferences.<clinit>():void");
    }

    protected abstract AbstractPreferences childSpi(String str);

    protected abstract String[] childrenNamesSpi() throws BackingStoreException;

    protected abstract void flushSpi() throws BackingStoreException;

    protected abstract String getSpi(String str);

    protected abstract String[] keysSpi() throws BackingStoreException;

    protected abstract void putSpi(String str, String str2);

    protected abstract void removeNodeSpi() throws BackingStoreException;

    protected abstract void removeSpi(String str);

    protected abstract void syncSpi() throws BackingStoreException;

    protected AbstractPreferences(AbstractPreferences parent, String name) {
        this.newNode = false;
        this.kidCache = new HashMap();
        this.removed = false;
        this.prefListeners = new ArrayList();
        this.nodeListeners = new ArrayList();
        this.lock = new Object();
        if (parent == null) {
            if (name.equals("")) {
                this.absolutePath = "/";
                this.root = this;
            } else {
                throw new IllegalArgumentException("Root name '" + name + "' must be \"\"");
            }
        } else if (name.indexOf(47) != -1) {
            throw new IllegalArgumentException("Name '" + name + "' contains '/'");
        } else if (name.equals("")) {
            throw new IllegalArgumentException("Illegal name: empty string");
        } else {
            String str;
            this.root = parent.root;
            if (parent == this.root) {
                str = "/" + name;
            } else {
                str = parent.absolutePath() + "/" + name;
            }
            this.absolutePath = str;
        }
        this.name = name;
        this.parent = parent;
    }

    public void put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        } else if (key.length() > 80) {
            throw new IllegalArgumentException("Key too long: " + key);
        } else if (value.length() > 8192) {
            throw new IllegalArgumentException("Value too long: " + value);
        } else {
            synchronized (this.lock) {
                if (this.removed) {
                    throw new IllegalStateException("Node has been removed.");
                }
                putSpi(key, value);
                enqueuePreferenceChangeEvent(key, value);
            }
        }
    }

    public String get(String key, String def) {
        if (key == null) {
            throw new NullPointerException("Null key");
        }
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            String result = null;
            try {
                result = getSpi(key);
            } catch (Exception e) {
            }
            if (result != null) {
                def = result;
            }
        }
        return def;
    }

    public void remove(String key) {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            removeSpi(key);
            enqueuePreferenceChangeEvent(key, null);
        }
    }

    public void clear() throws BackingStoreException {
        synchronized (this.lock) {
            String[] keys = keys();
            for (String remove : keys) {
                remove(remove);
            }
        }
    }

    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    public int getInt(String key, int def) {
        int result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Integer.parseInt(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    public long getLong(String key, long def) {
        long result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Long.parseLong(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putBoolean(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    public boolean getBoolean(String key, boolean def) {
        boolean result = def;
        String value = get(key, null);
        if (value == null) {
            return result;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return result;
    }

    public void putFloat(String key, float value) {
        put(key, Float.toString(value));
    }

    public float getFloat(String key, float def) {
        float result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Float.parseFloat(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    public double getDouble(String key, double def) {
        double result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Double.parseDouble(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putByteArray(String key, byte[] value) {
        put(key, Base64.byteArrayToBase64(value));
    }

    public byte[] getByteArray(String key, byte[] def) {
        byte[] result = def;
        String value = get(key, null);
        if (value == null) {
            return result;
        }
        try {
            return Base64.base64ToByteArray(value);
        } catch (RuntimeException e) {
            return result;
        }
    }

    public String[] keys() throws BackingStoreException {
        String[] keysSpi;
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            keysSpi = keysSpi();
        }
        return keysSpi;
    }

    public String[] childrenNames() throws BackingStoreException {
        Set<String> s;
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            s = new TreeSet(this.kidCache.keySet());
            for (String kid : childrenNamesSpi()) {
                s.add(kid);
            }
        }
        return (String[]) s.toArray(EMPTY_STRING_ARRAY);
    }

    protected final AbstractPreferences[] cachedChildren() {
        return (AbstractPreferences[]) this.kidCache.values().toArray(EMPTY_ABSTRACT_PREFS_ARRAY);
    }

    public Preferences parent() {
        Preferences preferences;
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            preferences = this.parent;
        }
        return preferences;
    }

    public Preferences node(String path) {
        synchronized (this.lock) {
            Preferences preferences;
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (path.equals("")) {
                return this;
            } else if (path.equals("/")) {
                preferences = this.root;
                return preferences;
            } else if (path.charAt(0) != '/') {
                preferences = node(new StringTokenizer(path, "/", true));
                return preferences;
            } else {
                return this.root.node(new StringTokenizer(path.substring(1), "/", true));
            }
        }
    }

    private Preferences node(StringTokenizer path) {
        String token = path.nextToken();
        if (token.equals("/")) {
            throw new IllegalArgumentException("Consecutive slashes in path");
        }
        synchronized (this.lock) {
            AbstractPreferences child = (AbstractPreferences) this.kidCache.get(token);
            if (child == null) {
                if (token.length() > 80) {
                    throw new IllegalArgumentException("Node name " + token + " too long");
                }
                child = childSpi(token);
                if (child.newNode) {
                    enqueueNodeAddedEvent(child);
                }
                this.kidCache.put(token, child);
            }
            if (path.hasMoreTokens()) {
                path.nextToken();
                if (path.hasMoreTokens()) {
                    Preferences node = child.node(path);
                    return node;
                }
                throw new IllegalArgumentException("Path ends with slash");
            }
            return child;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0013, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean nodeExists(String path) throws BackingStoreException {
        boolean z = false;
        synchronized (this.lock) {
            if (path.equals("")) {
                if (!this.removed) {
                    z = true;
                }
            } else if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (path.equals("/")) {
                return true;
            } else if (path.charAt(0) != '/') {
                z = nodeExists(new StringTokenizer(path, "/", true));
                return z;
            } else {
                return this.root.nodeExists(new StringTokenizer(path.substring(1), "/", true));
            }
        }
    }

    private boolean nodeExists(StringTokenizer path) throws BackingStoreException {
        String token = path.nextToken();
        if (token.equals("/")) {
            throw new IllegalArgumentException("Consecutive slashes in path");
        }
        synchronized (this.lock) {
            AbstractPreferences child = (AbstractPreferences) this.kidCache.get(token);
            if (child == null) {
                child = getChild(token);
            }
            if (child == null) {
                return false;
            } else if (path.hasMoreTokens()) {
                path.nextToken();
                if (path.hasMoreTokens()) {
                    boolean nodeExists = child.nodeExists(path);
                    return nodeExists;
                }
                throw new IllegalArgumentException("Path ends with slash");
            } else {
                return true;
            }
        }
    }

    public void removeNode() throws BackingStoreException {
        if (this == this.root) {
            throw new UnsupportedOperationException("Can't remove the root!");
        }
        synchronized (this.parent.lock) {
            removeNode2();
            this.parent.kidCache.remove(this.name);
        }
    }

    private void removeNode2() throws BackingStoreException {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node already removed.");
            }
            String[] kidNames = childrenNamesSpi();
            for (int i = 0; i < kidNames.length; i++) {
                if (!this.kidCache.containsKey(kidNames[i])) {
                    this.kidCache.put(kidNames[i], childSpi(kidNames[i]));
                }
            }
            Iterator<AbstractPreferences> i2 = this.kidCache.values().iterator();
            while (i2.hasNext()) {
                try {
                    ((AbstractPreferences) i2.next()).removeNode2();
                    i2.remove();
                } catch (BackingStoreException e) {
                }
            }
            removeNodeSpi();
            this.removed = true;
            this.parent.enqueueNodeRemovedEvent(this);
        }
    }

    public String name() {
        return this.name;
    }

    public String absolutePath() {
        return this.absolutePath;
    }

    public boolean isUserNode() {
        return ((Boolean) AccessController.doPrivileged(new AnonymousClass1(this))).booleanValue();
    }

    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        if (pcl == null) {
            throw new NullPointerException("Change listener is null.");
        }
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            this.prefListeners.add(pcl);
        }
        startEventDispatchThreadIfNecessary();
    }

    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (this.prefListeners.contains(pcl)) {
                this.prefListeners.remove((Object) pcl);
            } else {
                throw new IllegalArgumentException("Listener not registered.");
            }
        }
    }

    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (ncl == null) {
            throw new NullPointerException("Change listener is null.");
        }
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            }
            this.nodeListeners.add(ncl);
        }
        startEventDispatchThreadIfNecessary();
    }

    public void removeNodeChangeListener(NodeChangeListener ncl) {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (this.nodeListeners.contains(ncl)) {
                this.nodeListeners.remove((Object) ncl);
            } else {
                throw new IllegalArgumentException("Listener not registered.");
            }
        }
    }

    protected AbstractPreferences getChild(String nodeName) throws BackingStoreException {
        synchronized (this.lock) {
            String[] kidNames = childrenNames();
            for (int i = 0; i < kidNames.length; i++) {
                if (kidNames[i].equals(nodeName)) {
                    AbstractPreferences childSpi = childSpi(kidNames[i]);
                    return childSpi;
                }
            }
            return null;
        }
    }

    public String toString() {
        return (isUserNode() ? "User" : "System") + " Preference Node: " + absolutePath();
    }

    public void sync() throws BackingStoreException {
        sync2();
    }

    private void sync2() throws BackingStoreException {
        AbstractPreferences[] cachedKids;
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed");
            }
            syncSpi();
            cachedKids = cachedChildren();
        }
        for (AbstractPreferences sync2 : cachedKids) {
            sync2.sync2();
        }
    }

    public void flush() throws BackingStoreException {
        flush2();
    }

    /* JADX WARNING: Missing block: B:10:0x0011, code:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:12:0x0013, code:
            if (r1 >= r0.length) goto L_0x0020;
     */
    /* JADX WARNING: Missing block: B:13:0x0015, code:
            r0[r1].flush2();
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:17:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void flush2() throws BackingStoreException {
        synchronized (this.lock) {
            flushSpi();
            if (this.removed) {
                return;
            }
            AbstractPreferences[] cachedKids = cachedChildren();
        }
    }

    protected boolean isRemoved() {
        boolean z;
        synchronized (this.lock) {
            z = this.removed;
        }
        return z;
    }

    private static synchronized void startEventDispatchThreadIfNecessary() {
        synchronized (AbstractPreferences.class) {
            if (eventDispatchThread == null) {
                eventDispatchThread = new EventDispatchThread();
                eventDispatchThread.setDaemon(true);
                eventDispatchThread.start();
            }
        }
    }

    PreferenceChangeListener[] prefListeners() {
        PreferenceChangeListener[] preferenceChangeListenerArr;
        synchronized (this.lock) {
            preferenceChangeListenerArr = (PreferenceChangeListener[]) this.prefListeners.toArray(new PreferenceChangeListener[this.prefListeners.size()]);
        }
        return preferenceChangeListenerArr;
    }

    NodeChangeListener[] nodeListeners() {
        NodeChangeListener[] nodeChangeListenerArr;
        synchronized (this.lock) {
            nodeChangeListenerArr = (NodeChangeListener[]) this.nodeListeners.toArray(new NodeChangeListener[this.nodeListeners.size()]);
        }
        return nodeChangeListenerArr;
    }

    private void enqueuePreferenceChangeEvent(String key, String newValue) {
        if (!this.prefListeners.isEmpty()) {
            synchronized (eventQueue) {
                eventQueue.add(new PreferenceChangeEvent(this, key, newValue));
                eventQueue.notify();
            }
        }
    }

    private void enqueueNodeAddedEvent(Preferences child) {
        if (!this.nodeListeners.isEmpty()) {
            synchronized (eventQueue) {
                eventQueue.add(new NodeAddedEvent(this, this, child));
                eventQueue.notify();
            }
        }
    }

    private void enqueueNodeRemovedEvent(Preferences child) {
        if (!this.nodeListeners.isEmpty()) {
            synchronized (eventQueue) {
                eventQueue.add(new NodeRemovedEvent(this, this, child));
                eventQueue.notify();
            }
        }
    }

    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        XmlSupport.export(os, this, false);
    }

    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        XmlSupport.export(os, this, true);
    }
}

package java.util.logging;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import sun.util.logging.PlatformLogger;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class LogManager {
    public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";
    private static final int MAX_ITERATIONS = 400;
    private static final Level defaultLevel = null;
    private static LoggingMXBean loggingMXBean;
    private static LogManager manager;
    private PropertyChangeSupport changes;
    private final Permission controlPermission;
    private boolean deathImminent;
    private boolean initializedGlobalHandlers;
    private final ReferenceQueue<Logger> loggerRefQueue;
    private Properties props;
    private volatile boolean readPrimordialConfiguration;
    private Logger rootLogger;
    private final LoggerContext systemContext;
    private final LoggerContext userContext;

    /* renamed from: java.util.logging.LogManager$3 */
    class AnonymousClass3 implements PrivilegedAction<Void> {
        final /* synthetic */ LogManager this$0;
        final /* synthetic */ Logger val$l;
        final /* synthetic */ Logger val$sysLogger;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.logging.LogManager.3.<init>(java.util.logging.LogManager, java.util.logging.Logger, java.util.logging.Logger):void, dex: 
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
        AnonymousClass3(java.util.logging.LogManager r1, java.util.logging.Logger r2, java.util.logging.Logger r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.logging.LogManager.3.<init>(java.util.logging.LogManager, java.util.logging.Logger, java.util.logging.Logger):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.3.<init>(java.util.logging.LogManager, java.util.logging.Logger, java.util.logging.Logger):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.logging.LogManager.3.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.logging.LogManager.3.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.3.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.logging.LogManager.3.run():java.lang.Void, dex: 
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
        public java.lang.Void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.logging.LogManager.3.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.3.run():java.lang.Void");
        }
    }

    /* renamed from: java.util.logging.LogManager$5 */
    static class AnonymousClass5 implements PrivilegedAction<Object> {
        final /* synthetic */ Level val$level;
        final /* synthetic */ Logger val$logger;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.logging.LogManager.5.<init>(java.util.logging.Logger, java.util.logging.Level):void, dex: 
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
        AnonymousClass5(java.util.logging.Logger r1, java.util.logging.Level r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.logging.LogManager.5.<init>(java.util.logging.Logger, java.util.logging.Level):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.5.<init>(java.util.logging.Logger, java.util.logging.Level):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.logging.LogManager.5.run():java.lang.Object, dex: 
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
        public java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.logging.LogManager.5.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.5.run():java.lang.Object");
        }
    }

    /* renamed from: java.util.logging.LogManager$6 */
    static class AnonymousClass6 implements PrivilegedAction<Object> {
        final /* synthetic */ Logger val$logger;
        final /* synthetic */ Logger val$parent;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.logging.LogManager.6.<init>(java.util.logging.Logger, java.util.logging.Logger):void, dex: 
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
        AnonymousClass6(java.util.logging.Logger r1, java.util.logging.Logger r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.logging.LogManager.6.<init>(java.util.logging.Logger, java.util.logging.Logger):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.6.<init>(java.util.logging.Logger, java.util.logging.Logger):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.logging.LogManager.6.run():java.lang.Object, dex: 
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
        public java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.logging.LogManager.6.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.6.run():java.lang.Object");
        }
    }

    private class Cleaner extends Thread {
        final /* synthetic */ LogManager this$0;

        /* synthetic */ Cleaner(LogManager this$0, Cleaner cleaner) {
            this(this$0);
        }

        private Cleaner(LogManager this$0) {
            this.this$0 = this$0;
            setContextClassLoader(null);
        }

        public void run() {
            LogManager mgr = LogManager.manager;
            synchronized (this.this$0) {
                this.this$0.deathImminent = true;
                this.this$0.initializedGlobalHandlers = true;
            }
            this.this$0.reset();
        }
    }

    private static class LogNode {
        HashMap<String, LogNode> children;
        final LoggerContext context;
        LoggerWeakRef loggerRef;
        LogNode parent;

        LogNode(LogNode parent, LoggerContext context) {
            this.parent = parent;
            this.context = context;
        }

        void walkAndSetParent(Logger parent) {
            if (this.children != null) {
                for (LogNode node : this.children.values()) {
                    LoggerWeakRef ref = node.loggerRef;
                    Logger logger = ref == null ? null : (Logger) ref.get();
                    if (logger == null) {
                        node.walkAndSetParent(parent);
                    } else {
                        LogManager.doSetParent(logger, parent);
                    }
                }
            }
        }
    }

    static class LoggerContext {
        /* renamed from: -assertionsDisabled */
        static final /* synthetic */ boolean f83-assertionsDisabled = false;
        private final Hashtable<String, LoggerWeakRef> namedLoggers;
        private final boolean requiresDefaultLoggers;
        private final LogNode root;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.logging.LogManager.LoggerContext.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.logging.LogManager.LoggerContext.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.LoggerContext.<clinit>():void");
        }

        /* synthetic */ LoggerContext(LoggerContext loggerContext) {
            this();
        }

        private LoggerContext() {
            this(false);
        }

        private LoggerContext(boolean requiresDefaultLoggers) {
            this.namedLoggers = new Hashtable();
            this.root = new LogNode(null, this);
            this.requiresDefaultLoggers = requiresDefaultLoggers;
        }

        Logger demandLogger(String name, String resourceBundleName) {
            return LogManager.manager.demandLogger(name, resourceBundleName, null);
        }

        private void ensureInitialized() {
            if (this.requiresDefaultLoggers) {
                ensureDefaultLogger(LogManager.manager.rootLogger);
                ensureDefaultLogger(Logger.global);
            }
        }

        /* JADX WARNING: Missing block: B:12:0x001d, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized Logger findLogger(String name) {
            ensureInitialized();
            LoggerWeakRef ref = (LoggerWeakRef) this.namedLoggers.get(name);
            if (ref == null) {
                return null;
            }
            Logger logger = (Logger) ref.get();
            if (logger == null) {
                removeLogger(name);
            }
        }

        private void ensureAllDefaultLoggers(Logger logger) {
            if (this.requiresDefaultLoggers) {
                String name = logger.getName();
                if (!name.isEmpty()) {
                    ensureDefaultLogger(LogManager.manager.rootLogger);
                }
                if (!Logger.GLOBAL_LOGGER_NAME.equals(name)) {
                    ensureDefaultLogger(Logger.global);
                }
            }
        }

        private void ensureDefaultLogger(Logger logger) {
            boolean z = false;
            if (this.requiresDefaultLoggers && logger != null && (logger == Logger.global || logger == LogManager.manager.rootLogger)) {
                if (!this.namedLoggers.containsKey(logger.getName())) {
                    addLocalLogger(logger, false);
                }
                return;
            }
            if (!f83-assertionsDisabled) {
                if (logger == null) {
                    z = true;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
        }

        boolean addLocalLogger(Logger logger) {
            return addLocalLogger(logger, this.requiresDefaultLoggers);
        }

        boolean addLocalLogger(Logger logger, LogManager manager) {
            return addLocalLogger(logger, this.requiresDefaultLoggers, manager);
        }

        boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            return addLocalLogger(logger, addDefaultLoggersIfNeeded, LogManager.manager);
        }

        synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded, LogManager manager) {
            if (addDefaultLoggersIfNeeded) {
                ensureAllDefaultLoggers(logger);
            }
            String name = logger.getName();
            if (name == null) {
                throw new NullPointerException();
            }
            LoggerWeakRef ref = (LoggerWeakRef) this.namedLoggers.get(name);
            if (ref != null) {
                if (ref.get() != null) {
                    return false;
                }
                removeLogger(name);
            }
            manager.getClass();
            ref = new LoggerWeakRef(manager, logger);
            this.namedLoggers.put(name, ref);
            Level level = manager.getLevelProperty(name + ".level", null);
            if (level != null) {
                LogManager.doSetLevel(logger, level);
            }
            processParentHandlers(logger, name);
            LogNode node = getNode(name);
            node.loggerRef = ref;
            Logger parent = null;
            for (LogNode nodep = node.parent; nodep != null; nodep = nodep.parent) {
                LoggerWeakRef nodeRef = nodep.loggerRef;
                if (nodeRef != null) {
                    parent = (Logger) nodeRef.get();
                    if (parent != null) {
                        break;
                    }
                }
            }
            if (parent != null) {
                LogManager.doSetParent(logger, parent);
            }
            node.walkAndSetParent(logger);
            ref.setNode(node);
            return true;
        }

        void removeLogger(String name) {
            this.namedLoggers.remove(name);
        }

        synchronized Enumeration<String> getLoggerNames() {
            ensureInitialized();
            return this.namedLoggers.keys();
        }

        private void processParentHandlers(final Logger logger, final String name) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(this) {
                final /* synthetic */ LoggerContext this$1;

                public /* bridge */ /* synthetic */ Object run() {
                    return run();
                }

                public Void run() {
                    if (!(logger == LogManager.manager.rootLogger || LogManager.manager.getBooleanProperty(name + ".useParentHandlers", true))) {
                        logger.setUseParentHandlers(false);
                    }
                    return null;
                }
            });
            int ix = 1;
            while (true) {
                int ix2 = name.indexOf(".", ix);
                if (ix2 >= 0) {
                    String pname = name.substring(0, ix2);
                    if (LogManager.manager.getProperty(pname + ".level") != null || LogManager.manager.getProperty(pname + ".handlers") != null) {
                        demandLogger(pname, null);
                    }
                    ix = ix2 + 1;
                } else {
                    return;
                }
            }
        }

        LogNode getNode(String name) {
            if (name == null || name.equals("")) {
                return this.root;
            }
            LogNode node = this.root;
            while (name.length() > 0) {
                String head;
                int ix = name.indexOf(".");
                if (ix > 0) {
                    head = name.substring(0, ix);
                    name = name.substring(ix + 1);
                } else {
                    head = name;
                    name = "";
                }
                if (node.children == null) {
                    node.children = new HashMap();
                }
                LogNode child = (LogNode) node.children.get(head);
                if (child == null) {
                    child = new LogNode(node, this);
                    node.children.put(head, child);
                }
                node = child;
            }
            return node;
        }
    }

    final class LoggerWeakRef extends WeakReference<Logger> {
        private String name;
        private LogNode node;
        private WeakReference<Logger> parentRef;
        final /* synthetic */ LogManager this$0;

        LoggerWeakRef(LogManager this$0, Logger logger) {
            this.this$0 = this$0;
            super(logger, this$0.loggerRefQueue);
            this.name = logger.getName();
        }

        void dispose() {
            if (this.node != null) {
                this.node.context.removeLogger(this.name);
                this.name = null;
                this.node.loggerRef = null;
                this.node = null;
            }
            if (this.parentRef != null) {
                Logger parent = (Logger) this.parentRef.get();
                if (parent != null) {
                    parent.removeChildLogger(this);
                }
                this.parentRef = null;
            }
        }

        void setNode(LogNode node) {
            this.node = node;
        }

        void setParentRef(WeakReference<Logger> parentRef) {
            this.parentRef = parentRef;
        }
    }

    private class RootLogger extends Logger {
        final /* synthetic */ LogManager this$0;

        /* synthetic */ RootLogger(LogManager this$0, RootLogger rootLogger) {
            this(this$0);
        }

        private RootLogger(LogManager this$0) {
            this.this$0 = this$0;
            super("", null);
            setLevel(LogManager.defaultLevel);
        }

        public void log(LogRecord record) {
            this.this$0.initializeGlobalHandlers();
            super.log(record);
        }

        public void addHandler(Handler h) {
            this.this$0.initializeGlobalHandlers();
            super.addHandler(h);
        }

        public void removeHandler(Handler h) {
            this.this$0.initializeGlobalHandlers();
            super.removeHandler(h);
        }

        public Handler[] getHandlers() {
            this.this$0.initializeGlobalHandlers();
            return super.getHandlers();
        }
    }

    static class SystemLoggerContext extends LoggerContext {
        SystemLoggerContext() {
            super();
        }

        Logger demandLogger(String name, String resourceBundleName) {
            Logger result = findLogger(name);
            if (result == null) {
                Logger newLogger = new Logger(name, resourceBundleName);
                do {
                    if (addLocalLogger(newLogger)) {
                        result = newLogger;
                        continue;
                    } else {
                        result = findLogger(name);
                        continue;
                    }
                } while (result == null);
            }
            return result;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.logging.LogManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.logging.LogManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.<clinit>():void");
    }

    protected LogManager() {
        this.props = new Properties();
        this.changes = new PropertyChangeSupport(LogManager.class);
        this.systemContext = new SystemLoggerContext();
        this.userContext = new LoggerContext();
        this.initializedGlobalHandlers = true;
        this.loggerRefQueue = new ReferenceQueue();
        this.controlPermission = new LoggingPermission("control", null);
        try {
            Runtime.getRuntime().addShutdownHook(new Cleaner(this, null));
        } catch (IllegalStateException e) {
        }
    }

    public static LogManager getLogManager() {
        if (manager != null) {
            manager.readPrimordialConfiguration();
        }
        return manager;
    }

    private void readPrimordialConfiguration() {
        if (!this.readPrimordialConfiguration) {
            synchronized (this) {
                if (!this.readPrimordialConfiguration) {
                    if (System.out == null) {
                        return;
                    } else {
                        this.readPrimordialConfiguration = true;
                        try {
                            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                                public Void run() throws Exception {
                                    LogManager.this.readConfiguration();
                                    PlatformLogger.redirectPlatformLoggers();
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        if (l == null) {
            throw new NullPointerException();
        }
        checkPermission();
        this.changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        checkPermission();
        this.changes.removePropertyChangeListener(l);
    }

    private LoggerContext getUserContext() {
        return this.userContext;
    }

    private List<LoggerContext> contexts() {
        List<LoggerContext> cxs = new ArrayList();
        cxs.add(this.systemContext);
        cxs.add(getUserContext());
        return cxs;
    }

    Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        Logger result = getLogger(name);
        if (result == null) {
            Logger newLogger = new Logger(name, resourceBundleName, caller);
            while (!addLogger(newLogger)) {
                result = getLogger(name);
                if (result != null) {
                }
            }
            return newLogger;
        }
        return result;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    java.util.logging.Logger demandSystemLogger(java.lang.String r5, java.lang.String r6) {
        /*
        r4 = this;
        r3 = r4.systemContext;
        r2 = r3.demandLogger(r5, r6);
    L_0x0006:
        r3 = r4.addLogger(r2);
        if (r3 == 0) goto L_0x0022;
    L_0x000c:
        r1 = r2;
    L_0x000d:
        if (r1 == 0) goto L_0x0006;
    L_0x000f:
        if (r1 == r2) goto L_0x0021;
    L_0x0011:
        r3 = r2.getHandlers();
        r3 = r3.length;
        if (r3 != 0) goto L_0x0021;
    L_0x0018:
        r0 = r1;
        r3 = new java.util.logging.LogManager$3;
        r3.<init>(r4, r0, r2);
        java.security.AccessController.doPrivileged(r3);
    L_0x0021:
        return r2;
    L_0x0022:
        r1 = r4.getLogger(r5);
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.demandSystemLogger(java.lang.String, java.lang.String):java.util.logging.Logger");
    }

    private static Class getClassInstance(String cname) {
        if (cname == null) {
            return null;
        }
        try {
            return ClassLoader.getSystemClassLoader().loadClass(cname);
        } catch (ClassNotFoundException e) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(cname);
            } catch (ClassNotFoundException e2) {
                return null;
            }
        }
    }

    private void loadLoggerHandlers(final Logger logger, String name, final String handlersPropertyName) {
        AccessController.doPrivileged(new PrivilegedAction<Object>(this) {
            final /* synthetic */ LogManager this$0;

            public Object run() {
                String[] names = this.this$0.parseClassNames(handlersPropertyName);
                for (String word : names) {
                    try {
                        Handler hdl = (Handler) LogManager.getClassInstance(word).newInstance();
                        String levs = this.this$0.getProperty(word + ".level");
                        if (levs != null) {
                            Level l = Level.findLevel(levs);
                            if (l != null) {
                                hdl.setLevel(l);
                            } else {
                                System.err.println("Can't set level for " + word);
                            }
                        }
                        logger.addHandler(hdl);
                    } catch (Object ex) {
                        System.err.println("Can't load log handler \"" + word + "\"");
                        System.err.println("" + ex);
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    final synchronized void drainLoggerRefQueueBounded() {
        int i = 0;
        while (i < 400) {
            if (this.loggerRefQueue != null) {
                LoggerWeakRef ref = (LoggerWeakRef) this.loggerRefQueue.poll();
                if (ref == null) {
                    break;
                }
                ref.dispose();
                i++;
            } else {
                break;
            }
        }
    }

    public boolean addLogger(Logger logger) {
        String name = logger.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        drainLoggerRefQueueBounded();
        if (!getUserContext().addLocalLogger(logger, this)) {
            return false;
        }
        loadLoggerHandlers(logger, name, name + ".handlers");
        return true;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static void doSetLevel(java.util.logging.Logger r2, java.util.logging.Level r3) {
        /*
        r0 = java.lang.System.getSecurityManager();
        if (r0 != 0) goto L_0x000a;
    L_0x0006:
        r2.setLevel(r3);
        return;
    L_0x000a:
        r1 = new java.util.logging.LogManager$5;
        r1.<init>(r2, r3);
        java.security.AccessController.doPrivileged(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.doSetLevel(java.util.logging.Logger, java.util.logging.Level):void");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static void doSetParent(java.util.logging.Logger r2, java.util.logging.Logger r3) {
        /*
        r0 = java.lang.System.getSecurityManager();
        if (r0 != 0) goto L_0x000a;
    L_0x0006:
        r2.setParent(r3);
        return;
    L_0x000a:
        r1 = new java.util.logging.LogManager$6;
        r1.<init>(r2, r3);
        java.security.AccessController.doPrivileged(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogManager.doSetParent(java.util.logging.Logger, java.util.logging.Logger):void");
    }

    public Logger getLogger(String name) {
        return getUserContext().findLogger(name);
    }

    public Enumeration<String> getLoggerNames() {
        return getUserContext().getLoggerNames();
    }

    public void readConfiguration() throws IOException, SecurityException {
        InputStream in;
        checkPermission();
        String cname = System.getProperty("java.util.logging.config.class");
        if (cname != null) {
            try {
                getClassInstance(cname).newInstance();
                return;
            } catch (Object ex) {
                System.err.println("Logging configuration class \"" + cname + "\" failed");
                System.err.println("" + ex);
            }
        }
        String fname = System.getProperty("java.util.logging.config.file");
        if (fname == null) {
            fname = System.getProperty("java.home");
            if (fname == null) {
                throw new Error("Can't find java.home ??");
            }
            fname = new File(new File(fname, "lib"), "logging.properties").getCanonicalPath();
        }
        try {
            in = new FileInputStream(fname);
        } catch (Exception e) {
            in = LogManager.class.getResourceAsStream("logging.properties");
            if (in == null) {
                throw e;
            }
        }
        try {
            readConfiguration(new BufferedInputStream(in));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void reset() throws SecurityException {
        checkPermission();
        synchronized (this) {
            this.props = new Properties();
            this.initializedGlobalHandlers = true;
        }
        for (LoggerContext cx : contexts()) {
            Enumeration<String> enum_ = cx.getLoggerNames();
            while (enum_.hasMoreElements()) {
                Logger logger = cx.findLogger((String) enum_.nextElement());
                if (logger != null) {
                    resetLogger(logger);
                }
            }
        }
    }

    private void resetLogger(Logger logger) {
        Handler[] targets = logger.getHandlers();
        for (Handler h : targets) {
            logger.removeHandler(h);
            try {
                h.close();
            } catch (Exception e) {
            }
        }
        String name = logger.getName();
        if (name == null || !name.equals("")) {
            logger.setLevel(null);
        } else {
            logger.setLevel(defaultLevel);
        }
    }

    private String[] parseClassNames(String propertyName) {
        String hands = getProperty(propertyName);
        if (hands == null) {
            return new String[0];
        }
        hands = hands.trim();
        int ix = 0;
        Vector<String> result = new Vector();
        while (ix < hands.length()) {
            int end = ix;
            while (end < hands.length() && !Character.isWhitespace(hands.charAt(end)) && hands.charAt(end) != ',') {
                end++;
            }
            String word = hands.substring(ix, end);
            ix = end + 1;
            word = word.trim();
            if (word.length() != 0) {
                result.add(word);
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        checkPermission();
        reset();
        this.props.load(ins);
        String[] names = parseClassNames("config");
        for (String word : names) {
            try {
                getClassInstance(word).newInstance();
            } catch (Object ex) {
                System.err.println("Can't load config class \"" + word + "\"");
                System.err.println("" + ex);
            }
        }
        setLevelsOnExistingLoggers();
        this.changes.firePropertyChange(null, null, null);
        synchronized (this) {
            this.initializedGlobalHandlers = false;
        }
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    int getIntProperty(String name, int defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        val = val.toLowerCase();
        if (val.equals("true") || val.equals("1")) {
            return true;
        }
        if (val.equals("false") || val.equals("0")) {
            return false;
        }
        return defaultValue;
    }

    Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.findLevel(val.trim());
        if (l == null) {
            l = defaultValue;
        }
        return l;
    }

    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Filter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Formatter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    private synchronized void initializeGlobalHandlers() {
        if (!this.initializedGlobalHandlers) {
            this.initializedGlobalHandlers = true;
            if (!this.deathImminent) {
                loadLoggerHandlers(this.rootLogger, null, "handlers");
            }
        }
    }

    void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(this.controlPermission);
        }
    }

    public void checkAccess() throws SecurityException {
        checkPermission();
    }

    private synchronized void setLevelsOnExistingLoggers() {
        Enumeration<?> enum_ = this.props.propertyNames();
        while (enum_.hasMoreElements()) {
            String key = (String) enum_.nextElement();
            if (key.endsWith(".level")) {
                String name = key.substring(0, key.length() - 6);
                Level level = getLevelProperty(key, null);
                if (level == null) {
                    System.err.println("Bad level value for property: " + key);
                } else {
                    for (LoggerContext cx : contexts()) {
                        Logger l = cx.findLogger(name);
                        if (l != null) {
                            l.setLevel(level);
                        }
                    }
                }
            }
        }
    }

    public static synchronized LoggingMXBean getLoggingMXBean() {
        LoggingMXBean loggingMXBean;
        synchronized (LogManager.class) {
            if (loggingMXBean == null) {
                loggingMXBean = new Logging();
            }
            loggingMXBean = loggingMXBean;
        }
        return loggingMXBean;
    }
}

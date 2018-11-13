package java.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import sun.util.locale.BaseLocale;
import sun.util.logging.PlatformLogger;

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
public class FileSystemPreferences extends AbstractPreferences {
    private static final int EACCES = 13;
    private static final int EAGAIN = 11;
    private static final String[] EMPTY_STRING_ARRAY = null;
    private static final int ERROR_CODE = 1;
    private static int INIT_SLEEP_TIME = 0;
    private static final int LOCK_HANDLE = 0;
    private static int MAX_ATTEMPTS = 0;
    private static final int USER_READ_WRITE = 384;
    private static final int USER_RWX = 448;
    private static final int USER_RWX_ALL_RX = 493;
    private static final int USER_RW_ALL_READ = 420;
    private static boolean isSystemRootModified;
    private static boolean isSystemRootWritable;
    private static boolean isUserRootModified;
    private static boolean isUserRootWritable;
    static File systemLockFile;
    static Preferences systemRoot;
    private static File systemRootDir;
    private static int systemRootLockHandle;
    private static File systemRootModFile;
    private static long systemRootModTime;
    static File userLockFile;
    static Preferences userRoot;
    private static File userRootDir;
    private static int userRootLockHandle;
    private static File userRootModFile;
    private static long userRootModTime;
    final List<Change> changeLog;
    private final File dir;
    private final boolean isUserNode;
    private long lastSyncTime;
    NodeCreate nodeCreate;
    private Map<String, String> prefsCache;
    private final File prefsFile;
    private final File tmpFile;

    /* renamed from: java.util.prefs.FileSystemPreferences$2 */
    static class AnonymousClass2 implements PrivilegedAction<Void> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.prefs.FileSystemPreferences.2.<init>():void, dex: 
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
        AnonymousClass2() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.prefs.FileSystemPreferences.2.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.2.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.2.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.2.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.2.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.2.run():java.lang.Void, dex: 
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
        public java.lang.Void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.2.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.2.run():java.lang.Void");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$3 */
    static class AnonymousClass3 implements PrivilegedAction<Void> {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.prefs.FileSystemPreferences.3.<init>():void, dex: 
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
        AnonymousClass3() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.prefs.FileSystemPreferences.3.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.3.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.3.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.3.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.3.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.3.run():java.lang.Void, dex: 
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
        public java.lang.Void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.3.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.3.run():java.lang.Void");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$4 */
    class AnonymousClass4 implements PrivilegedAction<Void> {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.4.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        AnonymousClass4(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.4.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.4.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.4.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.4.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.4.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.4.run():java.lang.Void, dex: 
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
        public java.lang.Void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.4.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.4.run():java.lang.Void");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$5 */
    class AnonymousClass5 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.5.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        AnonymousClass5(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.5.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.5.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.5.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.5.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.5.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.5.run():java.lang.Void, dex: 
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
        public java.lang.Void run() throws java.util.prefs.BackingStoreException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.5.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.5.run():java.lang.Void");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$6 */
    class AnonymousClass6 implements PrivilegedAction<String[]> {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.6.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        AnonymousClass6(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.6.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.6.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.6.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.6.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.6.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.6.run():java.lang.String[], dex: 
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
        public java.lang.String[] run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.6.run():java.lang.String[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.6.run():java.lang.String[]");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$7 */
    class AnonymousClass7 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.7.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        AnonymousClass7(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.7.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.7.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.7.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Void, dex:  in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public java.lang.Void run() throws java.util.prefs.BackingStoreException {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Void, dex:  in method: java.util.prefs.FileSystemPreferences.7.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.7.run():java.lang.Void");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$8 */
    class AnonymousClass8 implements PrivilegedAction<Long> {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.8.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        AnonymousClass8(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.8.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.8.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.8.run():java.lang.Long, dex: 
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
        public java.lang.Long run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.8.run():java.lang.Long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.8.run():java.lang.Long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.8.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.8.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.8.run():java.lang.Object");
        }
    }

    /* renamed from: java.util.prefs.FileSystemPreferences$9 */
    class AnonymousClass9 implements PrivilegedAction<Void> {
        final /* synthetic */ FileSystemPreferences this$0;
        final /* synthetic */ Long val$newModTime;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.9.<init>(java.util.prefs.FileSystemPreferences, java.lang.Long):void, dex: 
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
        AnonymousClass9(java.util.prefs.FileSystemPreferences r1, java.lang.Long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.9.<init>(java.util.prefs.FileSystemPreferences, java.lang.Long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.9.<init>(java.util.prefs.FileSystemPreferences, java.lang.Long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.9.run():java.lang.Object, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.9.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.9.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.9.run():java.lang.Void, dex: 
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
        public java.lang.Void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.9.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.9.run():java.lang.Void");
        }
    }

    private abstract class Change {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.Change.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        private Change(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.Change.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.Change.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /* synthetic */ Change(FileSystemPreferences this$0, Change change) {
            this(this$0);
        }

        abstract void replay();
    }

    private class NodeCreate extends Change {
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.NodeCreate.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
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
        private NodeCreate(java.util.prefs.FileSystemPreferences r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.NodeCreate.<init>(java.util.prefs.FileSystemPreferences):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.NodeCreate.<init>(java.util.prefs.FileSystemPreferences):void");
        }

        /* synthetic */ NodeCreate(FileSystemPreferences this$0, NodeCreate nodeCreate) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.prefs.FileSystemPreferences.NodeCreate.replay():void, dex: 
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
        void replay() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.prefs.FileSystemPreferences.NodeCreate.replay():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.NodeCreate.replay():void");
        }
    }

    private class Put extends Change {
        String key;
        final /* synthetic */ FileSystemPreferences this$0;
        String value;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.prefs.FileSystemPreferences.Put.<init>(java.util.prefs.FileSystemPreferences, java.lang.String, java.lang.String):void, dex:  in method: java.util.prefs.FileSystemPreferences.Put.<init>(java.util.prefs.FileSystemPreferences, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.prefs.FileSystemPreferences.Put.<init>(java.util.prefs.FileSystemPreferences, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        Put(java.util.prefs.FileSystemPreferences r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.prefs.FileSystemPreferences.Put.<init>(java.util.prefs.FileSystemPreferences, java.lang.String, java.lang.String):void, dex:  in method: java.util.prefs.FileSystemPreferences.Put.<init>(java.util.prefs.FileSystemPreferences, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.Put.<init>(java.util.prefs.FileSystemPreferences, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.Put.replay():void, dex: 
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
        void replay() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.Put.replay():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.Put.replay():void");
        }
    }

    private class Remove extends Change {
        String key;
        final /* synthetic */ FileSystemPreferences this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.Remove.<init>(java.util.prefs.FileSystemPreferences, java.lang.String):void, dex: 
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
        Remove(java.util.prefs.FileSystemPreferences r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.prefs.FileSystemPreferences.Remove.<init>(java.util.prefs.FileSystemPreferences, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.Remove.<init>(java.util.prefs.FileSystemPreferences, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.Remove.replay():void, dex: 
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
        void replay() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.prefs.FileSystemPreferences.Remove.replay():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.Remove.replay():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.prefs.FileSystemPreferences.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.<clinit>():void");
    }

    private static native int chmod(String str, int i);

    private static native int[] lockFile0(String str, int i, boolean z);

    private static native int unlockFile0(int i);

    private static PlatformLogger getLogger() {
        return PlatformLogger.getLogger("java.util.prefs");
    }

    static synchronized Preferences getUserRoot() {
        Preferences preferences;
        synchronized (FileSystemPreferences.class) {
            if (userRoot == null) {
                setupUserRoot();
                userRoot = new FileSystemPreferences(true);
            }
            preferences = userRoot;
        }
        return preferences;
    }

    private static void setupUserRoot() {
        AccessController.doPrivileged(new AnonymousClass2());
    }

    static synchronized Preferences getSystemRoot() {
        Preferences preferences;
        synchronized (FileSystemPreferences.class) {
            if (systemRoot == null) {
                setupSystemRoot();
                systemRoot = new FileSystemPreferences(false);
            }
            preferences = systemRoot;
        }
        return preferences;
    }

    private static void setupSystemRoot() {
        AccessController.doPrivileged(new AnonymousClass3());
    }

    private void replayChanges() {
        int n = this.changeLog.size();
        for (int i = 0; i < n; i++) {
            ((Change) this.changeLog.get(i)).replay();
        }
    }

    private static void syncWorld() {
        Preferences userRt;
        Preferences systemRt;
        synchronized (FileSystemPreferences.class) {
            userRt = userRoot;
            systemRt = systemRoot;
        }
        if (userRt != null) {
            try {
                userRt.flush();
            } catch (Object e) {
                getLogger().warning("Couldn't flush user prefs: " + e);
            }
        }
        if (systemRt != null) {
            try {
                systemRt.flush();
            } catch (Object e2) {
                getLogger().warning("Couldn't flush system prefs: " + e2);
            }
        }
    }

    private FileSystemPreferences(boolean user) {
        super(null, "");
        this.prefsCache = null;
        this.lastSyncTime = 0;
        this.changeLog = new ArrayList();
        this.nodeCreate = null;
        this.isUserNode = user;
        this.dir = user ? userRootDir : systemRootDir;
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
    }

    public FileSystemPreferences(String path, File lockFile, boolean isUserNode) {
        super(null, "");
        this.prefsCache = null;
        this.lastSyncTime = 0;
        this.changeLog = new ArrayList();
        this.nodeCreate = null;
        this.isUserNode = isUserNode;
        this.dir = new File(path);
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
        this.newNode = !this.dir.exists();
        if (this.newNode) {
            this.prefsCache = new TreeMap();
            this.nodeCreate = new NodeCreate(this, null);
            this.changeLog.add(this.nodeCreate);
        }
        if (isUserNode) {
            userLockFile = lockFile;
            userRootModFile = new File(lockFile.getParentFile(), lockFile.getName() + ".rootmod");
            return;
        }
        systemLockFile = lockFile;
        systemRootModFile = new File(lockFile.getParentFile(), lockFile.getName() + ".rootmod");
    }

    private FileSystemPreferences(FileSystemPreferences parent, String name) {
        super(parent, name);
        this.prefsCache = null;
        this.lastSyncTime = 0;
        this.changeLog = new ArrayList();
        this.nodeCreate = null;
        this.isUserNode = parent.isUserNode;
        this.dir = new File(parent.dir, dirName(name));
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
        AccessController.doPrivileged(new AnonymousClass4(this));
        if (this.newNode) {
            this.prefsCache = new TreeMap();
            this.nodeCreate = new NodeCreate(this, null);
            this.changeLog.add(this.nodeCreate);
        }
    }

    public boolean isUserNode() {
        return this.isUserNode;
    }

    protected void putSpi(String key, String value) {
        initCacheIfNecessary();
        this.changeLog.add(new Put(this, key, value));
        this.prefsCache.put(key, value);
    }

    protected String getSpi(String key) {
        initCacheIfNecessary();
        return (String) this.prefsCache.get(key);
    }

    protected void removeSpi(String key) {
        initCacheIfNecessary();
        this.changeLog.add(new Remove(this, key));
        this.prefsCache.remove(key);
    }

    private void initCacheIfNecessary() {
        if (this.prefsCache == null) {
            try {
                loadCache();
            } catch (Exception e) {
                this.prefsCache = new TreeMap();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x006e A:{SYNTHETIC, Splitter: B:27:0x006e} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007f A:{Catch:{ Exception -> 0x0021 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0073 A:{SYNTHETIC, Splitter: B:30:0x0073} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadCache() throws BackingStoreException {
        Throwable th;
        Throwable th2 = null;
        Map<String, String> m = new TreeMap();
        long newLastSyncTime = 0;
        try {
            newLastSyncTime = this.prefsFile.lastModified();
            FileInputStream fis = null;
            try {
                FileInputStream fis2 = new FileInputStream(this.prefsFile);
                try {
                    XmlSupport.importMap(fis2, m);
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        throw th2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fis = fis2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        throw th2;
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fis != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable e) {
            if (e instanceof InvalidPreferencesFormatException) {
                getLogger().warning("Invalid preferences format in " + this.prefsFile.getPath());
                this.prefsFile.renameTo(new File(this.prefsFile.getParentFile(), "IncorrectFormatPrefs.xml"));
                m = new TreeMap();
            } else if (e instanceof FileNotFoundException) {
                getLogger().warning("Prefs file removed in background " + this.prefsFile.getPath());
            } else {
                getLogger().warning("Exception while reading cache: " + e.getMessage());
                throw new BackingStoreException(e);
            }
        }
        this.prefsCache = m;
        this.lastSyncTime = newLastSyncTime;
    }

    private void writeBackCache() throws BackingStoreException {
        try {
            AccessController.doPrivileged(new AnonymousClass5(this));
        } catch (PrivilegedActionException e) {
            throw ((BackingStoreException) e.getException());
        }
    }

    protected String[] keysSpi() {
        initCacheIfNecessary();
        return (String[]) this.prefsCache.keySet().toArray(new String[this.prefsCache.size()]);
    }

    protected String[] childrenNamesSpi() {
        return (String[]) AccessController.doPrivileged(new AnonymousClass6(this));
    }

    protected AbstractPreferences childSpi(String name) {
        return new FileSystemPreferences(this, name);
    }

    public void removeNode() throws BackingStoreException {
        synchronized ((isUserNode() ? userLockFile : systemLockFile)) {
            if (lockFile(false)) {
                try {
                    super.removeNode();
                } finally {
                    unlockFile();
                }
            } else {
                throw new BackingStoreException("Couldn't get file lock.");
            }
        }
    }

    protected void removeNodeSpi() throws BackingStoreException {
        try {
            AccessController.doPrivileged(new AnonymousClass7(this));
        } catch (PrivilegedActionException e) {
            throw ((BackingStoreException) e.getException());
        }
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public synchronized void sync() throws java.util.prefs.BackingStoreException {
        /*
        r6 = this;
        monitor-enter(r6);
        r2 = r6.isUserNode();	 Catch:{ all -> 0x0024 }
        if (r2 == 0) goto L_0x0027;	 Catch:{ all -> 0x0024 }
    L_0x0007:
        r1 = 0;	 Catch:{ all -> 0x0024 }
    L_0x0008:
        r3 = r6.isUserNode();	 Catch:{ all -> 0x0024 }
        if (r3 == 0) goto L_0x002f;	 Catch:{ all -> 0x0024 }
    L_0x000e:
        r3 = userLockFile;	 Catch:{ all -> 0x0024 }
        r4 = r3;	 Catch:{ all -> 0x0024 }
    L_0x0011:
        monitor-enter(r4);	 Catch:{ all -> 0x0024 }
        r3 = r6.lockFile(r1);	 Catch:{ all -> 0x0021 }
        if (r3 != 0) goto L_0x0033;	 Catch:{ all -> 0x0021 }
    L_0x0018:
        r3 = new java.util.prefs.BackingStoreException;	 Catch:{ all -> 0x0021 }
        r5 = "Couldn't get file lock.";	 Catch:{ all -> 0x0021 }
        r3.<init>(r5);	 Catch:{ all -> 0x0021 }
        throw r3;	 Catch:{ all -> 0x0021 }
    L_0x0021:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0024 }
        throw r3;	 Catch:{ all -> 0x0024 }
    L_0x0024:
        r3 = move-exception;
        monitor-exit(r6);
        throw r3;
    L_0x0027:
        r3 = isSystemRootWritable;	 Catch:{ all -> 0x0024 }
        if (r3 == 0) goto L_0x002d;	 Catch:{ all -> 0x0024 }
    L_0x002b:
        r1 = 0;	 Catch:{ all -> 0x0024 }
        goto L_0x0008;	 Catch:{ all -> 0x0024 }
    L_0x002d:
        r1 = 1;	 Catch:{ all -> 0x0024 }
        goto L_0x0008;	 Catch:{ all -> 0x0024 }
    L_0x002f:
        r3 = systemLockFile;	 Catch:{ all -> 0x0024 }
        r4 = r3;
        goto L_0x0011;
    L_0x0033:
        r3 = new java.util.prefs.FileSystemPreferences$8;	 Catch:{ all -> 0x0021 }
        r3.<init>(r6);	 Catch:{ all -> 0x0021 }
        r0 = java.security.AccessController.doPrivileged(r3);	 Catch:{ all -> 0x0021 }
        r0 = (java.lang.Long) r0;	 Catch:{ all -> 0x0021 }
        super.sync();	 Catch:{ all -> 0x004f }
        r3 = new java.util.prefs.FileSystemPreferences$9;	 Catch:{ all -> 0x004f }
        r3.<init>(r6, r0);	 Catch:{ all -> 0x004f }
        java.security.AccessController.doPrivileged(r3);	 Catch:{ all -> 0x004f }
        r6.unlockFile();	 Catch:{ all -> 0x0021 }
        monitor-exit(r4);	 Catch:{ all -> 0x0024 }
        monitor-exit(r6);
        return;
    L_0x004f:
        r3 = move-exception;
        r6.unlockFile();	 Catch:{ all -> 0x0021 }
        throw r3;	 Catch:{ all -> 0x0021 }
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.FileSystemPreferences.sync():void");
    }

    protected void syncSpi() throws BackingStoreException {
        syncSpiPrivileged();
    }

    private void syncSpiPrivileged() throws BackingStoreException {
        if (isRemoved()) {
            throw new IllegalStateException("Node has been removed");
        } else if (this.prefsCache != null) {
            long lastModifiedTime;
            if (isUserNode() ? isUserRootModified : isSystemRootModified) {
                lastModifiedTime = this.prefsFile.lastModified();
                if (lastModifiedTime != this.lastSyncTime) {
                    loadCache();
                    replayChanges();
                    this.lastSyncTime = lastModifiedTime;
                }
            } else if (!(this.lastSyncTime == 0 || this.dir.exists())) {
                this.prefsCache = new TreeMap();
                replayChanges();
            }
            if (!this.changeLog.isEmpty()) {
                writeBackCache();
                lastModifiedTime = this.prefsFile.lastModified();
                if (this.lastSyncTime <= lastModifiedTime) {
                    this.lastSyncTime = 1000 + lastModifiedTime;
                    this.prefsFile.setLastModified(this.lastSyncTime);
                }
                this.changeLog.clear();
            }
        }
    }

    public void flush() throws BackingStoreException {
        if (!isRemoved()) {
            sync();
        }
    }

    protected void flushSpi() throws BackingStoreException {
    }

    private static boolean isDirChar(char ch) {
        return (ch <= 31 || ch >= 127 || ch == '/' || ch == '.' || ch == '_') ? false : true;
    }

    private static String dirName(String nodeName) {
        int n = nodeName.length();
        for (int i = 0; i < n; i++) {
            if (!isDirChar(nodeName.charAt(i))) {
                return BaseLocale.SEP + Base64.byteArrayToAltBase64(byteArray(nodeName));
            }
        }
        return nodeName;
    }

    private static byte[] byteArray(String s) {
        int len = s.length();
        byte[] result = new byte[(len * 2)];
        int j = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            int i2 = j + 1;
            result[j] = (byte) (c >> 8);
            j = i2 + 1;
            result[i2] = (byte) c;
        }
        return result;
    }

    private static String nodeName(String dirName) {
        if (dirName.charAt(0) != '_') {
            return dirName;
        }
        byte[] a = Base64.altBase64ToByteArray(dirName.substring(1));
        StringBuffer result = new StringBuffer(a.length / 2);
        int i = 0;
        while (i < a.length) {
            int i2 = i + 1;
            int highByte = a[i] & 255;
            i = i2 + 1;
            result.append((char) ((highByte << 8) | (a[i2] & 255)));
        }
        return result.toString();
    }

    private boolean lockFile(boolean shared) throws SecurityException {
        boolean usernode = isUserNode();
        int errorCode = 0;
        File lockFile = usernode ? userLockFile : systemLockFile;
        long sleepTime = (long) INIT_SLEEP_TIME;
        int i = 0;
        while (i < MAX_ATTEMPTS) {
            try {
                int[] result = lockFile0(lockFile.getCanonicalPath(), usernode ? USER_READ_WRITE : USER_RW_ALL_READ, shared);
                errorCode = result[1];
                if (result[0] != 0) {
                    if (usernode) {
                        userRootLockHandle = result[0];
                    } else {
                        systemRootLockHandle = result[0];
                    }
                    return true;
                }
            } catch (IOException e) {
            }
            try {
                Thread.sleep(sleepTime);
                sleepTime *= 2;
                i++;
            } catch (InterruptedException e2) {
                checkLockFile0ErrorCode(errorCode);
                return false;
            }
        }
        checkLockFile0ErrorCode(errorCode);
        return false;
    }

    private void checkLockFile0ErrorCode(int errorCode) throws SecurityException {
        if (errorCode == 13) {
            throw new SecurityException("Could not lock " + (isUserNode() ? "User prefs." : "System prefs.") + " Lock file access denied.");
        } else if (errorCode != 11) {
            getLogger().warning("Could not lock " + (isUserNode() ? "User prefs. " : "System prefs.") + " Unix error code " + errorCode + ".");
        }
    }

    private void unlockFile() {
        boolean usernode = isUserNode();
        File lockFile;
        if (usernode) {
            lockFile = userLockFile;
        } else {
            lockFile = systemLockFile;
        }
        int lockHandle = usernode ? userRootLockHandle : systemRootLockHandle;
        if (lockHandle == 0) {
            getLogger().warning("Unlock: zero lockHandle for " + (usernode ? "user" : "system") + " preferences.)");
            return;
        }
        int result = unlockFile0(lockHandle);
        if (result != 0) {
            getLogger().warning("Could not drop file-lock on " + (isUserNode() ? "user" : "system") + " preferences." + " Unix error code " + result + ".");
            if (result == 13) {
                throw new SecurityException("Could not unlock" + (isUserNode() ? "User prefs." : "System prefs.") + " Lock file access denied.");
            }
        }
        if (isUserNode()) {
            userRootLockHandle = 0;
        } else {
            systemRootLockHandle = 0;
        }
    }
}

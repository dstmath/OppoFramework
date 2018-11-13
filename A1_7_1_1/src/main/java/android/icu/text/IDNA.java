package android.icu.text;

import android.icu.impl.IDNA2003;
import android.icu.impl.UTS46;
import java.util.Collections;
import java.util.EnumSet;

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
public abstract class IDNA {
    @Deprecated
    public static final int ALLOW_UNASSIGNED = 1;
    public static final int CHECK_BIDI = 4;
    public static final int CHECK_CONTEXTJ = 8;
    public static final int CHECK_CONTEXTO = 64;
    public static final int DEFAULT = 0;
    public static final int NONTRANSITIONAL_TO_ASCII = 16;
    public static final int NONTRANSITIONAL_TO_UNICODE = 32;
    public static final int USE_STD3_RULES = 2;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Error {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.IDNA.Error.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.IDNA.Error.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Error.<clinit>():void");
        }
    }

    public static final class Info {
        private EnumSet<Error> errors;
        private boolean isBiDi;
        private boolean isOkBiDi;
        private boolean isTransDiff;
        private EnumSet<Error> labelErrors;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.-get0(android.icu.text.IDNA$Info):java.util.EnumSet, dex: 
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
        /* renamed from: -get0 */
        static /* synthetic */ java.util.EnumSet m46-get0(android.icu.text.IDNA.Info r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.-get0(android.icu.text.IDNA$Info):java.util.EnumSet, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-get0(android.icu.text.IDNA$Info):java.util.EnumSet");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.text.IDNA.Info.-get1(android.icu.text.IDNA$Info):boolean, dex: 
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
        /* renamed from: -get1 */
        static /* synthetic */ boolean m47-get1(android.icu.text.IDNA.Info r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.text.IDNA.Info.-get1(android.icu.text.IDNA$Info):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-get1(android.icu.text.IDNA$Info):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.text.IDNA.Info.-get2(android.icu.text.IDNA$Info):boolean, dex: 
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
        /* renamed from: -get2 */
        static /* synthetic */ boolean m48-get2(android.icu.text.IDNA.Info r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.text.IDNA.Info.-get2(android.icu.text.IDNA$Info):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-get2(android.icu.text.IDNA$Info):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.-get3(android.icu.text.IDNA$Info):java.util.EnumSet, dex: 
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
        /* renamed from: -get3 */
        static /* synthetic */ java.util.EnumSet m49-get3(android.icu.text.IDNA.Info r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.-get3(android.icu.text.IDNA$Info):java.util.EnumSet, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-get3(android.icu.text.IDNA$Info):java.util.EnumSet");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.icu.text.IDNA.Info.-set0(android.icu.text.IDNA$Info, boolean):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -set0 */
        static /* synthetic */ boolean m50-set0(android.icu.text.IDNA.Info r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.icu.text.IDNA.Info.-set0(android.icu.text.IDNA$Info, boolean):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-set0(android.icu.text.IDNA$Info, boolean):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.icu.text.IDNA.Info.-set1(android.icu.text.IDNA$Info, boolean):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -set1 */
        static /* synthetic */ boolean m51-set1(android.icu.text.IDNA.Info r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.icu.text.IDNA.Info.-set1(android.icu.text.IDNA$Info, boolean):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-set1(android.icu.text.IDNA$Info, boolean):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: android.icu.text.IDNA.Info.-set2(android.icu.text.IDNA$Info, boolean):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -set2 */
        static /* synthetic */ boolean m52-set2(android.icu.text.IDNA.Info r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: android.icu.text.IDNA.Info.-set2(android.icu.text.IDNA$Info, boolean):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-set2(android.icu.text.IDNA$Info, boolean):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.IDNA.Info.-wrap0(android.icu.text.IDNA$Info):void, dex: 
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
        /* renamed from: -wrap0 */
        static /* synthetic */ void m53-wrap0(android.icu.text.IDNA.Info r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.IDNA.Info.-wrap0(android.icu.text.IDNA$Info):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.-wrap0(android.icu.text.IDNA$Info):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.text.IDNA.Info.<init>():void, dex: 
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
        public Info() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.text.IDNA.Info.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.reset():void, dex: 
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
        private void reset() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.reset():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.reset():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.getErrors():java.util.Set<android.icu.text.IDNA$Error>, dex: 
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
        public java.util.Set<android.icu.text.IDNA.Error> getErrors() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.getErrors():java.util.Set<android.icu.text.IDNA$Error>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.getErrors():java.util.Set<android.icu.text.IDNA$Error>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.hasErrors():boolean, dex: 
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
        public boolean hasErrors() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.text.IDNA.Info.hasErrors():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.hasErrors():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.icu.text.IDNA.Info.isTransitionalDifferent():boolean, dex: 
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
        public boolean isTransitionalDifferent() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.icu.text.IDNA.Info.isTransitionalDifferent():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IDNA.Info.isTransitionalDifferent():boolean");
        }
    }

    public abstract StringBuilder labelToASCII(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public abstract StringBuilder labelToUnicode(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public abstract StringBuilder nameToASCII(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public abstract StringBuilder nameToUnicode(CharSequence charSequence, StringBuilder stringBuilder, Info info);

    public static IDNA getUTS46Instance(int options) {
        return new UTS46(options);
    }

    @Deprecated
    protected static void resetInfo(Info info) {
        Info.m53-wrap0(info);
    }

    @Deprecated
    protected static boolean hasCertainErrors(Info info, EnumSet<Error> errors) {
        return (Info.m46-get0(info).isEmpty() || Collections.disjoint(Info.m46-get0(info), errors)) ? false : true;
    }

    @Deprecated
    protected static boolean hasCertainLabelErrors(Info info, EnumSet<Error> errors) {
        return (Info.m49-get3(info).isEmpty() || Collections.disjoint(Info.m49-get3(info), errors)) ? false : true;
    }

    @Deprecated
    protected static void addLabelError(Info info, Error error) {
        Info.m49-get3(info).add(error);
    }

    @Deprecated
    protected static void promoteAndResetLabelErrors(Info info) {
        if (!Info.m49-get3(info).isEmpty()) {
            Info.m46-get0(info).addAll(Info.m49-get3(info));
            Info.m49-get3(info).clear();
        }
    }

    @Deprecated
    protected static void addError(Info info, Error error) {
        Info.m46-get0(info).add(error);
    }

    @Deprecated
    protected static void setTransitionalDifferent(Info info) {
        Info.m52-set2(info, true);
    }

    @Deprecated
    protected static void setBiDi(Info info) {
        Info.m50-set0(info, true);
    }

    @Deprecated
    protected static boolean isBiDi(Info info) {
        return Info.m47-get1(info);
    }

    @Deprecated
    protected static void setNotOkBiDi(Info info) {
        Info.m51-set1(info, false);
    }

    @Deprecated
    protected static boolean isOkBiDi(Info info) {
        return Info.m48-get2(info);
    }

    @Deprecated
    protected IDNA() {
    }

    @Deprecated
    public static StringBuffer convertToASCII(String src, int options) throws StringPrepParseException {
        return convertToASCII(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToASCII(StringBuffer src, int options) throws StringPrepParseException {
        return convertToASCII(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToASCII(UCharacterIterator src, int options) throws StringPrepParseException {
        return IDNA2003.convertToASCII(src, options);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(UCharacterIterator src, int options) throws StringPrepParseException {
        return convertIDNToASCII(src.getText(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(StringBuffer src, int options) throws StringPrepParseException {
        return convertIDNToASCII(src.toString(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToASCII(String src, int options) throws StringPrepParseException {
        return IDNA2003.convertIDNToASCII(src, options);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(String src, int options) throws StringPrepParseException {
        return convertToUnicode(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(StringBuffer src, int options) throws StringPrepParseException {
        return convertToUnicode(UCharacterIterator.getInstance(src), options);
    }

    @Deprecated
    public static StringBuffer convertToUnicode(UCharacterIterator src, int options) throws StringPrepParseException {
        return IDNA2003.convertToUnicode(src, options);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(UCharacterIterator src, int options) throws StringPrepParseException {
        return convertIDNToUnicode(src.getText(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(StringBuffer src, int options) throws StringPrepParseException {
        return convertIDNToUnicode(src.toString(), options);
    }

    @Deprecated
    public static StringBuffer convertIDNToUnicode(String src, int options) throws StringPrepParseException {
        return IDNA2003.convertIDNToUnicode(src, options);
    }

    @Deprecated
    public static int compare(StringBuffer s1, StringBuffer s2, int options) throws StringPrepParseException {
        if (s1 != null && s2 != null) {
            return IDNA2003.compare(s1.toString(), s2.toString(), options);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }

    @Deprecated
    public static int compare(String s1, String s2, int options) throws StringPrepParseException {
        if (s1 != null && s2 != null) {
            return IDNA2003.compare(s1, s2, options);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }

    @Deprecated
    public static int compare(UCharacterIterator s1, UCharacterIterator s2, int options) throws StringPrepParseException {
        if (s1 != null && s2 != null) {
            return IDNA2003.compare(s1.getText(), s2.getText(), options);
        }
        throw new IllegalArgumentException("One of the source buffers is null");
    }
}

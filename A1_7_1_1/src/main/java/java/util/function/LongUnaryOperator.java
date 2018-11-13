package java.util.function;

import java.util.Objects;

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
@FunctionalInterface
public interface LongUnaryOperator {

    final /* synthetic */ class -java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0 implements LongUnaryOperator {
        private /* synthetic */ LongUnaryOperator val$after;
        private /* synthetic */ LongUnaryOperator val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0.<init>(java.util.function.LongUnaryOperator, java.util.function.LongUnaryOperator):void, dex: 
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
        public /* synthetic */ -java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0(java.util.function.LongUnaryOperator r1, java.util.function.LongUnaryOperator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0.<init>(java.util.function.LongUnaryOperator, java.util.function.LongUnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0.<init>(java.util.function.LongUnaryOperator, java.util.function.LongUnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0.applyAsLong(long):long, dex: 
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
        public long applyAsLong(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0.applyAsLong(long):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0.applyAsLong(long):long");
        }
    }

    final /* synthetic */ class -java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0 implements LongUnaryOperator {
        private /* synthetic */ LongUnaryOperator val$before;
        private /* synthetic */ LongUnaryOperator val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0.<init>(java.util.function.LongUnaryOperator, java.util.function.LongUnaryOperator):void, dex: 
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
        public /* synthetic */ -java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0(java.util.function.LongUnaryOperator r1, java.util.function.LongUnaryOperator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0.<init>(java.util.function.LongUnaryOperator, java.util.function.LongUnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0.<init>(java.util.function.LongUnaryOperator, java.util.function.LongUnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0.applyAsLong(long):long, dex: 
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
        public long applyAsLong(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0.applyAsLong(long):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0.applyAsLong(long):long");
        }
    }

    final /* synthetic */ class -java_util_function_LongUnaryOperator_identity__LambdaImpl0 implements LongUnaryOperator {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_identity__LambdaImpl0.<init>():void, dex: 
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
        public /* synthetic */ -java_util_function_LongUnaryOperator_identity__LambdaImpl0() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_identity__LambdaImpl0.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.LongUnaryOperator.-java_util_function_LongUnaryOperator_identity__LambdaImpl0.<init>():void");
        }

        public long applyAsLong(long arg0) {
            return LongUnaryOperator.m76-java_util_function_LongUnaryOperator_lambda$3(arg0);
        }
    }

    long applyAsLong(long j);

    LongUnaryOperator compose(LongUnaryOperator before) {
        Objects.requireNonNull(before);
        return new -java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0(this, before);
    }

    /* renamed from: -java_util_function_LongUnaryOperator_lambda$1 */
    /* synthetic */ long m77-java_util_function_LongUnaryOperator_lambda$1(LongUnaryOperator before, long v) {
        return applyAsLong(before.applyAsLong(v));
    }

    LongUnaryOperator andThen(LongUnaryOperator after) {
        Objects.requireNonNull(after);
        return new -java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0(this, after);
    }

    /* renamed from: -java_util_function_LongUnaryOperator_lambda$2 */
    /* synthetic */ long m78-java_util_function_LongUnaryOperator_lambda$2(LongUnaryOperator after, long t) {
        return after.applyAsLong(applyAsLong(t));
    }

    /* renamed from: -java_util_function_LongUnaryOperator_lambda$3 */
    static /* synthetic */ long m76-java_util_function_LongUnaryOperator_lambda$3(long t) {
        return t;
    }

    static LongUnaryOperator identity() {
        return new -java_util_function_LongUnaryOperator_identity__LambdaImpl0();
    }
}

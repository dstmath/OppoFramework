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
public interface DoubleUnaryOperator {

    final /* synthetic */ class -java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0 implements DoubleUnaryOperator {
        private /* synthetic */ DoubleUnaryOperator val$after;
        private /* synthetic */ DoubleUnaryOperator val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0.<init>(java.util.function.DoubleUnaryOperator, java.util.function.DoubleUnaryOperator):void, dex: 
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
        public /* synthetic */ -java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0(java.util.function.DoubleUnaryOperator r1, java.util.function.DoubleUnaryOperator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0.<init>(java.util.function.DoubleUnaryOperator, java.util.function.DoubleUnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0.<init>(java.util.function.DoubleUnaryOperator, java.util.function.DoubleUnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0.applyAsDouble(double):double, dex: 
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
        public double applyAsDouble(double r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0.applyAsDouble(double):double, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0.applyAsDouble(double):double");
        }
    }

    final /* synthetic */ class -java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0 implements DoubleUnaryOperator {
        private /* synthetic */ DoubleUnaryOperator val$before;
        private /* synthetic */ DoubleUnaryOperator val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0.<init>(java.util.function.DoubleUnaryOperator, java.util.function.DoubleUnaryOperator):void, dex: 
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
        public /* synthetic */ -java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0(java.util.function.DoubleUnaryOperator r1, java.util.function.DoubleUnaryOperator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0.<init>(java.util.function.DoubleUnaryOperator, java.util.function.DoubleUnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0.<init>(java.util.function.DoubleUnaryOperator, java.util.function.DoubleUnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0.applyAsDouble(double):double, dex: 
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
        public double applyAsDouble(double r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0.applyAsDouble(double):double, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0.applyAsDouble(double):double");
        }
    }

    final /* synthetic */ class -java_util_function_DoubleUnaryOperator_identity__LambdaImpl0 implements DoubleUnaryOperator {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_identity__LambdaImpl0.<init>():void, dex: 
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
        public /* synthetic */ -java_util_function_DoubleUnaryOperator_identity__LambdaImpl0() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_identity__LambdaImpl0.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_identity__LambdaImpl0.<init>():void");
        }

        public double applyAsDouble(double arg0) {
            return DoubleUnaryOperator.m67-java_util_function_DoubleUnaryOperator_lambda$3(arg0);
        }
    }

    double applyAsDouble(double d);

    DoubleUnaryOperator compose(DoubleUnaryOperator before) {
        Objects.requireNonNull(before);
        return new -java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0(this, before);
    }

    /* renamed from: -java_util_function_DoubleUnaryOperator_lambda$1 */
    /* synthetic */ double m68-java_util_function_DoubleUnaryOperator_lambda$1(DoubleUnaryOperator before, double v) {
        return applyAsDouble(before.applyAsDouble(v));
    }

    DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
        Objects.requireNonNull(after);
        return new -java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0(this, after);
    }

    /* renamed from: -java_util_function_DoubleUnaryOperator_lambda$2 */
    /* synthetic */ double m69-java_util_function_DoubleUnaryOperator_lambda$2(DoubleUnaryOperator after, double t) {
        return after.applyAsDouble(applyAsDouble(t));
    }

    /* renamed from: -java_util_function_DoubleUnaryOperator_lambda$3 */
    static /* synthetic */ double m67-java_util_function_DoubleUnaryOperator_lambda$3(double t) {
        return t;
    }

    static DoubleUnaryOperator identity() {
        return new -java_util_function_DoubleUnaryOperator_identity__LambdaImpl0();
    }
}

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
@FunctionalInterface
public interface IntUnaryOperator {

    final /* synthetic */ class -java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0 implements IntUnaryOperator {
        private /* synthetic */ IntUnaryOperator val$after;
        private /* synthetic */ IntUnaryOperator val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0.<init>(java.util.function.IntUnaryOperator, java.util.function.IntUnaryOperator):void, dex: 
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
        public /* synthetic */ -java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0(java.util.function.IntUnaryOperator r1, java.util.function.IntUnaryOperator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0.<init>(java.util.function.IntUnaryOperator, java.util.function.IntUnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0.<init>(java.util.function.IntUnaryOperator, java.util.function.IntUnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0.applyAsInt(int):int, dex: 
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
        public int applyAsInt(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0.applyAsInt(int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0.applyAsInt(int):int");
        }
    }

    final /* synthetic */ class -java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0 implements IntUnaryOperator {
        private /* synthetic */ IntUnaryOperator val$before;
        private /* synthetic */ IntUnaryOperator val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0.<init>(java.util.function.IntUnaryOperator, java.util.function.IntUnaryOperator):void, dex: 
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
        public /* synthetic */ -java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0(java.util.function.IntUnaryOperator r1, java.util.function.IntUnaryOperator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0.<init>(java.util.function.IntUnaryOperator, java.util.function.IntUnaryOperator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0.<init>(java.util.function.IntUnaryOperator, java.util.function.IntUnaryOperator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0.applyAsInt(int):int, dex: 
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
        public int applyAsInt(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0.applyAsInt(int):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0.applyAsInt(int):int");
        }
    }

    final /* synthetic */ class -java_util_function_IntUnaryOperator_identity__LambdaImpl0 implements IntUnaryOperator {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_identity__LambdaImpl0.<init>():void, dex: 
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
        public /* synthetic */ -java_util_function_IntUnaryOperator_identity__LambdaImpl0() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_identity__LambdaImpl0.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.function.IntUnaryOperator.-java_util_function_IntUnaryOperator_identity__LambdaImpl0.<init>():void");
        }

        public int applyAsInt(int arg0) {
            return IntUnaryOperator.m57-java_util_function_IntUnaryOperator_lambda$3(arg0);
        }
    }

    int applyAsInt(int i);

    IntUnaryOperator compose(IntUnaryOperator before) {
        Objects.requireNonNull(before);
        return new -java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0(this, before);
    }

    /* renamed from: -java_util_function_IntUnaryOperator_lambda$1 */
    /* synthetic */ int m58-java_util_function_IntUnaryOperator_lambda$1(IntUnaryOperator before, int v) {
        return applyAsInt(before.applyAsInt(v));
    }

    IntUnaryOperator andThen(IntUnaryOperator after) {
        Objects.requireNonNull(after);
        return new -java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0(this, after);
    }

    /* renamed from: -java_util_function_IntUnaryOperator_lambda$2 */
    /* synthetic */ int m59-java_util_function_IntUnaryOperator_lambda$2(IntUnaryOperator after, int t) {
        return after.applyAsInt(applyAsInt(t));
    }

    /* renamed from: -java_util_function_IntUnaryOperator_lambda$3 */
    static /* synthetic */ int m57-java_util_function_IntUnaryOperator_lambda$3(int t) {
        return t;
    }

    static IntUnaryOperator identity() {
        return new -java_util_function_IntUnaryOperator_identity__LambdaImpl0();
    }
}

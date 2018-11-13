package java.util.stream;

import java.util.function.Function;
import java.util.function.Supplier;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
final /* synthetic */ class Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0 implements Function {
    private /* synthetic */ Supplier val$downstreamSupplier;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.stream.Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0.<init>(java.util.function.Supplier):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public /* synthetic */ Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0(java.util.function.Supplier r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.stream.Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0.<init>(java.util.function.Supplier):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0.<init>(java.util.function.Supplier):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.stream.Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0.apply(java.lang.Object):java.lang.Object, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public java.lang.Object apply(java.lang.Object r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.stream.Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0.apply(java.lang.Object):java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0.apply(java.lang.Object):java.lang.Object");
    }
}

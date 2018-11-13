package java.util.stream;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.ReferencePipeline.StatefulOp;
import java.util.stream.Sink.ChainedReference;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
final class DistinctOps {

    /* renamed from: java.util.stream.DistinctOps$1 */
    static class AnonymousClass1 extends StatefulOp<T, T> {

        final /* synthetic */ class -java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0 implements Consumer {
            private /* synthetic */ ConcurrentHashMap val$map;
            private /* synthetic */ AtomicBoolean val$seenNull;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0.<init>(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.ConcurrentHashMap):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public /* synthetic */ -java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0(java.util.concurrent.atomic.AtomicBoolean r1, java.util.concurrent.ConcurrentHashMap r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0.<init>(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.ConcurrentHashMap):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0.<init>(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.ConcurrentHashMap):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0.accept(java.lang.Object):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void accept(java.lang.Object r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0.accept(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0.accept(java.lang.Object):void");
            }
        }

        final /* synthetic */ class -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0 implements Supplier {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public /* synthetic */ -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0.<init>():void");
            }

            public Object get() {
                return new LinkedHashSet();
            }
        }

        final /* synthetic */ class -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1 implements BiConsumer {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public /* synthetic */ -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1.accept(java.lang.Object, java.lang.Object):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void accept(java.lang.Object r1, java.lang.Object r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1.accept(java.lang.Object, java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1.accept(java.lang.Object, java.lang.Object):void");
            }
        }

        final /* synthetic */ class -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2 implements BiConsumer {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public /* synthetic */ -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2.accept(java.lang.Object, java.lang.Object):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void accept(java.lang.Object r1, java.lang.Object r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2.accept(java.lang.Object, java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2.accept(java.lang.Object, java.lang.Object):void");
            }
        }

        /* renamed from: java.util.stream.DistinctOps$1$1 */
        class AnonymousClass1 extends ChainedReference<T, T> {
            T lastSeen;
            boolean seenNull;
            final /* synthetic */ AnonymousClass1 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.1.<init>(java.util.stream.DistinctOps$1, java.util.stream.Sink):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(java.util.stream.DistinctOps.AnonymousClass1 r1, java.util.stream.Sink r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.1.<init>(java.util.stream.DistinctOps$1, java.util.stream.Sink):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.1.<init>(java.util.stream.DistinctOps$1, java.util.stream.Sink):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: java.util.stream.DistinctOps.1.1.accept(java.lang.Object):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void accept(T r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: java.util.stream.DistinctOps.1.1.accept(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.1.accept(java.lang.Object):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: java.util.stream.DistinctOps.1.1.begin(long):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void begin(long r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: java.util.stream.DistinctOps.1.1.begin(long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.1.begin(long):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: java.util.stream.DistinctOps.1.1.end():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void end() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: java.util.stream.DistinctOps.1.1.end():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.1.end():void");
            }
        }

        /* renamed from: java.util.stream.DistinctOps$1$2 */
        class AnonymousClass2 extends ChainedReference<T, T> {
            Set<T> seen;
            final /* synthetic */ AnonymousClass1 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.2.<init>(java.util.stream.DistinctOps$1, java.util.stream.Sink):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass2(java.util.stream.DistinctOps.AnonymousClass1 r1, java.util.stream.Sink r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.2.<init>(java.util.stream.DistinctOps$1, java.util.stream.Sink):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.2.<init>(java.util.stream.DistinctOps$1, java.util.stream.Sink):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.stream.DistinctOps.1.2.accept(java.lang.Object):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void accept(T r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.stream.DistinctOps.1.2.accept(java.lang.Object):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.2.accept(java.lang.Object):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.2.begin(long):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void begin(long r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.2.begin(long):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.2.begin(long):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.2.end():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void end() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.stream.DistinctOps.1.2.end():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.2.end():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1-mthref-1(java.util.LinkedHashSet, java.lang.Object):void, dex: 
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
        /* renamed from: -java_util_stream_DistinctOps$1-mthref-1 */
        static /* synthetic */ void m135-java_util_stream_DistinctOps$1-mthref-1(java.util.LinkedHashSet r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1-mthref-1(java.util.LinkedHashSet, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1-mthref-1(java.util.LinkedHashSet, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1-mthref-2(java.util.LinkedHashSet, java.util.LinkedHashSet):void, dex: 
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
        /* renamed from: -java_util_stream_DistinctOps$1-mthref-2 */
        static /* synthetic */ void m136-java_util_stream_DistinctOps$1-mthref-2(java.util.LinkedHashSet r1, java.util.LinkedHashSet r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1-mthref-2(java.util.LinkedHashSet, java.util.LinkedHashSet):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1-mthref-2(java.util.LinkedHashSet, java.util.LinkedHashSet):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1_lambda$4(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.ConcurrentHashMap, java.lang.Object):void, dex: 
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
        /* renamed from: -java_util_stream_DistinctOps$1_lambda$4 */
        static /* synthetic */ void m137-java_util_stream_DistinctOps$1_lambda$4(java.util.concurrent.atomic.AtomicBoolean r1, java.util.concurrent.ConcurrentHashMap r2, java.lang.Object r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1_lambda$4(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.ConcurrentHashMap, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.-java_util_stream_DistinctOps$1_lambda$4(java.util.concurrent.atomic.AtomicBoolean, java.util.concurrent.ConcurrentHashMap, java.lang.Object):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.<init>(java.util.stream.AbstractPipeline, java.util.stream.StreamShape, int):void, dex: 
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
        AnonymousClass1(java.util.stream.AbstractPipeline r1, java.util.stream.StreamShape r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.1.<init>(java.util.stream.AbstractPipeline, java.util.stream.StreamShape, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.<init>(java.util.stream.AbstractPipeline, java.util.stream.StreamShape, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.opEvaluateParallel(java.util.stream.PipelineHelper, java.util.Spliterator, java.util.function.IntFunction):java.util.stream.Node<T>, dex: 
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
        public <P_IN> java.util.stream.Node<T> opEvaluateParallel(java.util.stream.PipelineHelper<T> r1, java.util.Spliterator<P_IN> r2, java.util.function.IntFunction<T[]> r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.opEvaluateParallel(java.util.stream.PipelineHelper, java.util.Spliterator, java.util.function.IntFunction):java.util.stream.Node<T>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.opEvaluateParallel(java.util.stream.PipelineHelper, java.util.Spliterator, java.util.function.IntFunction):java.util.stream.Node<T>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.opEvaluateParallelLazy(java.util.stream.PipelineHelper, java.util.Spliterator):java.util.Spliterator<T>, dex: 
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
        public <P_IN> java.util.Spliterator<T> opEvaluateParallelLazy(java.util.stream.PipelineHelper<T> r1, java.util.Spliterator<P_IN> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.opEvaluateParallelLazy(java.util.stream.PipelineHelper, java.util.Spliterator):java.util.Spliterator<T>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.opEvaluateParallelLazy(java.util.stream.PipelineHelper, java.util.Spliterator):java.util.Spliterator<T>");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.opWrapSink(int, java.util.stream.Sink):java.util.stream.Sink<T>, dex: 
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
        public java.util.stream.Sink<T> opWrapSink(int r1, java.util.stream.Sink<T> r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.stream.DistinctOps.1.opWrapSink(int, java.util.stream.Sink):java.util.stream.Sink<T>, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.1.opWrapSink(int, java.util.stream.Sink):java.util.stream.Sink<T>");
        }

        <P_IN> Node<T> reduce(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return Nodes.node((Collection) ReduceOps.makeRef(new -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0(), new -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1(), new -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2()).evaluateParallel(helper, spliterator));
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.<init>():void, dex: 
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
    private DistinctOps() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.stream.DistinctOps.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.<init>():void");
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
    static <T> java.util.stream.ReferencePipeline<T, T> makeRef(java.util.stream.AbstractPipeline<?, T, ?> r4) {
        /*
        r0 = new java.util.stream.DistinctOps$1;
        r1 = java.util.stream.StreamShape.REFERENCE;
        r2 = java.util.stream.StreamOpFlag.IS_DISTINCT;
        r3 = java.util.stream.StreamOpFlag.NOT_SIZED;
        r2 = r2 | r3;
        r0.<init>(r4, r1, r2);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.DistinctOps.makeRef(java.util.stream.AbstractPipeline):java.util.stream.ReferencePipeline<T, T>");
    }
}

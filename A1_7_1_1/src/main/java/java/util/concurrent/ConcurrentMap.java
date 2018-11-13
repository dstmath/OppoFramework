package java.util.concurrent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

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
public interface ConcurrentMap<K, V> extends Map<K, V> {

    final /* synthetic */ class -void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ BiFunction val$function;
        private /* synthetic */ ConcurrentMap val$this;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ConcurrentMap.-void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0.<init>(java.util.concurrent.ConcurrentMap, java.util.function.BiFunction):void, dex: 
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
        public /* synthetic */ -void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0(java.util.concurrent.ConcurrentMap r1, java.util.function.BiFunction r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.concurrent.ConcurrentMap.-void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0.<init>(java.util.concurrent.ConcurrentMap, java.util.function.BiFunction):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ConcurrentMap.-void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0.<init>(java.util.concurrent.ConcurrentMap, java.util.function.BiFunction):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ConcurrentMap.-void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0.accept(java.lang.Object, java.lang.Object):void, dex: 
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
        public void accept(java.lang.Object r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.concurrent.ConcurrentMap.-void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0.accept(java.lang.Object, java.lang.Object):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ConcurrentMap.-void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0.accept(java.lang.Object, java.lang.Object):void");
        }
    }

    V putIfAbsent(K k, V v);

    boolean remove(Object obj, Object obj2);

    V replace(K k, V v);

    boolean replace(K k, V v, V v2);

    V getOrDefault(Object key, V defaultValue) {
        V v = get(key);
        return v != null ? v : defaultValue;
    }

    void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Entry<K, V> entry : entrySet()) {
            try {
                action.accept(entry.getKey(), entry.getValue());
            } catch (IllegalStateException e) {
            }
        }
    }

    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        forEach(new -void_replaceAll_java_util_function_BiFunction_function_LambdaImpl0(this, function));
    }

    /* renamed from: -java_util_concurrent_ConcurrentMap_lambda$1 */
    /* synthetic */ void m128-java_util_concurrent_ConcurrentMap_lambda$1(BiFunction function, Object k, Object v) {
        while (!replace(k, v, function.apply(k, v))) {
            v = get(k);
            if (v == null) {
                return;
            }
        }
    }

    V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V oldValue = get(key);
        if (oldValue == null) {
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                oldValue = putIfAbsent(key, newValue);
                if (oldValue == null) {
                    return newValue;
                }
            }
        }
        return oldValue;
    }

    V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V newValue;
        Objects.requireNonNull(remappingFunction);
        boolean remove;
        do {
            V oldValue = get(key);
            if (oldValue == null) {
                return null;
            }
            newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                remove = remove(key, oldValue);
                continue;
            } else {
                remove = replace(key, oldValue, newValue);
                continue;
            }
        } while (!remove);
        return newValue;
    }

    V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        while (true) {
            V oldValue = get(key);
            while (true) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    if (oldValue == null) {
                        oldValue = putIfAbsent(key, newValue);
                        if (oldValue == null) {
                            return newValue;
                        }
                    } else if (replace(key, oldValue, newValue)) {
                        return newValue;
                    }
                } else if (oldValue == null || remove(key, oldValue)) {
                    return null;
                }
            }
        }
        return null;
    }

    V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        while (true) {
            V oldValue = get(key);
            while (oldValue == null) {
                oldValue = putIfAbsent(key, value);
                if (oldValue == null) {
                    return value;
                }
            }
            V newValue = remappingFunction.apply(oldValue, value);
            if (newValue != null) {
                if (replace(key, oldValue, newValue)) {
                    return newValue;
                }
            } else if (remove(key, oldValue)) {
                return null;
            }
        }
    }
}

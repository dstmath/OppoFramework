package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4.AnonymousClass1;

@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);

    <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return new AnonymousClass1((byte) 1, this, before);
    }

    /* renamed from: lambda$-java_util_function_Function_2660 */
    /* synthetic */ Object m2lambda$-java_util_function_Function_2660(Function before, Object v) {
        return apply(before.apply(v));
    }

    <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return new AnonymousClass1((byte) 0, this, after);
    }

    /* renamed from: lambda$-java_util_function_Function_3525 */
    /* synthetic */ Object m3lambda$-java_util_function_Function_3525(Function after, Object t) {
        return after.apply(apply(t));
    }

    static <T> Function<T, T> identity() {
        return -$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4.$INST$0;
    }

    /* renamed from: lambda$-java_util_function_Function_3851 */
    static /* synthetic */ Object m1lambda$-java_util_function_Function_3851(Object t) {
        return t;
    }
}

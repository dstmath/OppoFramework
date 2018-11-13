package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<T, U, R> {
    R apply(T t, U u);

    <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return new -$Lambda$1MZdIZ-DL_fjy9l0o8IMJk57T2g(this, after);
    }

    /* renamed from: lambda$-java_util_function_BiFunction_2840 */
    /* synthetic */ Object m14lambda$-java_util_function_BiFunction_2840(Function after, Object t, Object u) {
        return after.apply(apply(t, u));
    }
}

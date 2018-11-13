package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U.AnonymousClass1;

@FunctionalInterface
public interface BiPredicate<T, U> {
    boolean test(T t, U u);

    BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1((byte) 0, this, other);
    }

    /* renamed from: lambda$-java_util_function_BiPredicate_2994 */
    /* synthetic */ boolean m25lambda$-java_util_function_BiPredicate_2994(BiPredicate other, Object t, Object u) {
        return test(t, u) ? other.test(t, u) : false;
    }

    /* renamed from: lambda$-java_util_function_BiPredicate_3305 */
    /* synthetic */ boolean m26lambda$-java_util_function_BiPredicate_3305(Object t, Object u) {
        return test(t, u) ^ 1;
    }

    BiPredicate<T, U> negate() {
        return new -$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U(this);
    }

    BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1((byte) 1, this, other);
    }

    /* renamed from: lambda$-java_util_function_BiPredicate_4269 */
    /* synthetic */ boolean m27lambda$-java_util_function_BiPredicate_4269(BiPredicate other, Object t, Object u) {
        return !test(t, u) ? other.test(t, u) : true;
    }
}

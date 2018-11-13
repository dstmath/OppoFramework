package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$7wUuYdVTmZGtkCqNZ8xzJcRU4aE.AnonymousClass1;

@FunctionalInterface
public interface IntPredicate {
    boolean test(int i);

    IntPredicate and(IntPredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1((byte) 0, this, other);
    }

    /* renamed from: lambda$-java_util_function_IntPredicate_2831 */
    /* synthetic */ boolean m88lambda$-java_util_function_IntPredicate_2831(IntPredicate other, int value) {
        return test(value) ? other.test(value) : false;
    }

    /* renamed from: lambda$-java_util_function_IntPredicate_3136 */
    /* synthetic */ boolean m89lambda$-java_util_function_IntPredicate_3136(int value) {
        return test(value) ^ 1;
    }

    IntPredicate negate() {
        return new -$Lambda$7wUuYdVTmZGtkCqNZ8xzJcRU4aE(this);
    }

    IntPredicate or(IntPredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1((byte) 1, this, other);
    }

    /* renamed from: lambda$-java_util_function_IntPredicate_4072 */
    /* synthetic */ boolean m90lambda$-java_util_function_IntPredicate_4072(IntPredicate other, int value) {
        return !test(value) ? other.test(value) : true;
    }
}

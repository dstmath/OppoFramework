package java.util.stream;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.MatchOps.AnonymousClass1MatchSink;
import java.util.stream.MatchOps.AnonymousClass2MatchSink;
import java.util.stream.MatchOps.AnonymousClass3MatchSink;
import java.util.stream.MatchOps.AnonymousClass4MatchSink;

final /* synthetic */ class -$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM implements Supplier {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f197-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f198-$f1;

    private final /* synthetic */ Object $m$0() {
        return new AnonymousClass4MatchSink((MatchKind) this.f197-$f0, (DoublePredicate) this.f198-$f1);
    }

    private final /* synthetic */ Object $m$1() {
        return new AnonymousClass2MatchSink((MatchKind) this.f197-$f0, (IntPredicate) this.f198-$f1);
    }

    private final /* synthetic */ Object $m$2() {
        return new AnonymousClass3MatchSink((MatchKind) this.f197-$f0, (LongPredicate) this.f198-$f1);
    }

    private final /* synthetic */ Object $m$3() {
        return new AnonymousClass1MatchSink((MatchKind) this.f197-$f0, (Predicate) this.f198-$f1);
    }

    public /* synthetic */ -$Lambda$6Ha_z5NODYn_Qs3Vg00tRqQ1oBM(byte b, Object obj, Object obj2) {
        this.$id = b;
        this.f197-$f0 = obj;
        this.f198-$f1 = obj2;
    }

    public final Object get() {
        switch (this.$id) {
            case (byte) 0:
                return $m$0();
            case (byte) 1:
                return $m$1();
            case (byte) 2:
                return $m$2();
            case (byte) 3:
                return $m$3();
            default:
                throw new AssertionError();
        }
    }
}

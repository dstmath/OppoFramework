package java.util.stream;

import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$F2FxWS-_sj37qqP78wv60F_s_Zw implements Supplier {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f199-$f0;

    private final /* synthetic */ Object $m$0() {
        return ((AbstractPipeline) this.f199-$f0).m101lambda$-java_util_stream_AbstractPipeline_14339();
    }

    private final /* synthetic */ Object $m$1() {
        return AbstractPipeline.m100lambda$-java_util_stream_AbstractPipeline_20439((Spliterator) this.f199-$f0);
    }

    private final /* synthetic */ Object $m$2() {
        return new Object[]{this.f199-$f0};
    }

    private final /* synthetic */ Object $m$3() {
        return new Partition(((Collector) this.f199-$f0).supplier().lambda$-java_util_stream_Collectors_49198(), ((Collector) this.f199-$f0).supplier().lambda$-java_util_stream_Collectors_49198());
    }

    private final /* synthetic */ Object $m$4() {
        return new Consumer<T>((BinaryOperator) this.f199-$f0) {
            boolean present = false;
            T value = null;

            /* renamed from: accept */
            public void -java_util_stream_Collectors-mthref-13(T t) {
                if (this.present) {
                    this.value = r1.apply(this.value, t);
                    return;
                }
                this.value = t;
                this.present = true;
            }
        };
    }

    public /* synthetic */ -$Lambda$F2FxWS-_sj37qqP78wv60F_s_Zw(byte b, Object obj) {
        this.$id = b;
        this.f199-$f0 = obj;
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
            case (byte) 4:
                return $m$4();
            default:
                throw new AssertionError();
        }
    }
}

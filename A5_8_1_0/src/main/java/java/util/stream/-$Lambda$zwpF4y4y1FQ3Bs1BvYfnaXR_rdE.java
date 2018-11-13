package java.util.stream;

import java.util.function.DoubleConsumer;
import java.util.stream.DoublePipeline.5.AnonymousClass1;

final /* synthetic */ class -$Lambda$zwpF4y4y1FQ3Bs1BvYfnaXR_rdE implements DoubleConsumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f234-$f0;

    private final /* synthetic */ void $m$0(double arg0) {
        ((AnonymousClass1) this.f234-$f0).m106lambda$-java_util_stream_DoublePipeline$5$1_10563(arg0);
    }

    private final /* synthetic */ void $m$1(double arg0) {
        ((Sink) this.f234-$f0).-java_util_stream_ReferencePipeline$9$1-mthref-0(arg0);
    }

    private final /* synthetic */ void $m$2(double arg0) {
        ((Sink) this.f234-$f0).-java_util_stream_ReferencePipeline$9$1-mthref-0(arg0);
    }

    public /* synthetic */ -$Lambda$zwpF4y4y1FQ3Bs1BvYfnaXR_rdE(byte b, Object obj) {
        this.$id = b;
        this.f234-$f0 = obj;
    }

    public final void accept(double d) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(d);
                return;
            case (byte) 1:
                $m$1(d);
                return;
            case (byte) 2:
                $m$2(d);
                return;
            default:
                throw new AssertionError();
        }
    }
}

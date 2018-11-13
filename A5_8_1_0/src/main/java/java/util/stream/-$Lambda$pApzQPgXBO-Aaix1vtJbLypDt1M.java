package java.util.stream;

import java.util.List;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$pApzQPgXBO-Aaix1vtJbLypDt1M implements Consumer {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f223-$f0;

    private final /* synthetic */ void $m$0(Object arg0) {
        ((Sink) this.f223-$f0).-java_util_stream_SortedOps$RefSortingSink-mthref-0(arg0);
    }

    private final /* synthetic */ void $m$1(Object arg0) {
        ((List) this.f223-$f0).-java_util_stream_SpinedBuffer-mthref-0(arg0);
    }

    public /* synthetic */ -$Lambda$pApzQPgXBO-Aaix1vtJbLypDt1M(byte b, Object obj) {
        this.$id = b;
        this.f223-$f0 = obj;
    }

    public final void accept(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(obj);
                return;
            case (byte) 1:
                $m$1(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}

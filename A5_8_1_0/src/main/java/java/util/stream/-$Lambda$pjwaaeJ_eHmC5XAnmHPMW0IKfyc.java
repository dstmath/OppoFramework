package java.util.stream;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Sink.OfDouble;
import java.util.stream.Sink.OfInt;
import java.util.stream.Sink.OfLong;

final /* synthetic */ class -$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc implements BooleanSupplier {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f226-$f0;

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$1 */
    final /* synthetic */ class AnonymousClass1 implements OfDouble {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f227-$f0;

        private final /* synthetic */ void $m$0(double arg0) {
            ((DoubleConsumer) this.f227-$f0).-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(arg0);
        }

        private final /* synthetic */ void $m$1(double arg0) {
            ((SpinedBuffer.OfDouble) this.f227-$f0).-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj) {
            this.$id = b;
            this.f227-$f0 = obj;
        }

        public final void accept(double d) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(d);
                    return;
                case (byte) 1:
                    $m$1(d);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$2 */
    final /* synthetic */ class AnonymousClass2 implements OfInt {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f228-$f0;

        private final /* synthetic */ void $m$0(int arg0) {
            ((IntConsumer) this.f228-$f0).-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(arg0);
        }

        private final /* synthetic */ void $m$1(int arg0) {
            ((SpinedBuffer.OfInt) this.f228-$f0).-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass2(byte b, Object obj) {
            this.$id = b;
            this.f228-$f0 = obj;
        }

        public final void accept(int i) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(i);
                    return;
                case (byte) 1:
                    $m$1(i);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$3 */
    final /* synthetic */ class AnonymousClass3 implements OfLong {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f229-$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((LongConsumer) this.f229-$f0).-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(arg0);
        }

        private final /* synthetic */ void $m$1(long arg0) {
            ((SpinedBuffer.OfLong) this.f229-$f0).-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass3(byte b, Object obj) {
            this.$id = b;
            this.f229-$f0 = obj;
        }

        public final void accept(long j) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(j);
                    return;
                case (byte) 1:
                    $m$1(j);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: java.util.stream.-$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc$4 */
    final /* synthetic */ class AnonymousClass4 implements Sink {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f230-$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((Consumer) this.f230-$f0).-java_util_stream_SortedOps$RefSortingSink-mthref-0(arg0);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            ((SpinedBuffer) this.f230-$f0).-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-0(arg0);
        }

        public /* synthetic */ AnonymousClass4(byte b, Object obj) {
            this.$id = b;
            this.f230-$f0 = obj;
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

    private final /* synthetic */ boolean $m$0() {
        return ((DoubleWrappingSpliterator) this.f226-$f0).m139xdd52a83c();
    }

    private final /* synthetic */ boolean $m$1() {
        return ((IntWrappingSpliterator) this.f226-$f0).m140x5e3b06a7();
    }

    private final /* synthetic */ boolean $m$2() {
        return ((LongWrappingSpliterator) this.f226-$f0).m141xd775d779();
    }

    private final /* synthetic */ boolean $m$3() {
        return ((WrappingSpliterator) this.f226-$f0).m147x5c6ed919();
    }

    public /* synthetic */ -$Lambda$pjwaaeJ_eHmC5XAnmHPMW0IKfyc(byte b, Object obj) {
        this.$id = b;
        this.f226-$f0 = obj;
    }

    public final boolean getAsBoolean() {
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

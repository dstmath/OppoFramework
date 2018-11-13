package java.util.stream;

import java.util.function.LongBinaryOperator;
import java.util.function.ObjLongConsumer;

final /* synthetic */ class -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA implements LongBinaryOperator {
    public static final /* synthetic */ -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA $INST$0 = new -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA((byte) 0);
    public static final /* synthetic */ -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA $INST$1 = new -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA((byte) 1);
    public static final /* synthetic */ -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA $INST$2 = new -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA((byte) 2);
    private final /* synthetic */ byte $id;

    /* renamed from: java.util.stream.-$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA$2 */
    final /* synthetic */ class AnonymousClass2 implements ObjLongConsumer {
        public static final /* synthetic */ AnonymousClass2 $INST$0 = new AnonymousClass2((byte) 0);
        public static final /* synthetic */ AnonymousClass2 $INST$1 = new AnonymousClass2((byte) 1);
        private final /* synthetic */ byte $id;

        private /* synthetic */ AnonymousClass2(byte b) {
            this.$id = b;
        }

        public final void accept(Object obj, long j) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(obj, j);
                    return;
                case (byte) 1:
                    $m$1(obj, j);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private /* synthetic */ -$Lambda$RYrQKhHyGc-mMxiERR98xxRAWkA(byte b) {
        this.$id = b;
    }

    public final long applyAsLong(long j, long j2) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(j, j2);
            case (byte) 1:
                return $m$1(j, j2);
            case (byte) 2:
                return $m$2(j, j2);
            default:
                throw new AssertionError();
        }
    }
}

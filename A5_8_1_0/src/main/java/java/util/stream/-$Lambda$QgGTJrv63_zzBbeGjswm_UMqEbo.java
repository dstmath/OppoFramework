package java.util.stream;

import java.util.function.IntBinaryOperator;
import java.util.function.ObjIntConsumer;

final /* synthetic */ class -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo implements IntBinaryOperator {
    public static final /* synthetic */ -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo $INST$0 = new -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo((byte) 0);
    public static final /* synthetic */ -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo $INST$1 = new -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo((byte) 1);
    public static final /* synthetic */ -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo $INST$2 = new -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo((byte) 2);
    private final /* synthetic */ byte $id;

    /* renamed from: java.util.stream.-$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo$2 */
    final /* synthetic */ class AnonymousClass2 implements ObjIntConsumer {
        public static final /* synthetic */ AnonymousClass2 $INST$0 = new AnonymousClass2((byte) 0);
        public static final /* synthetic */ AnonymousClass2 $INST$1 = new AnonymousClass2((byte) 1);
        private final /* synthetic */ byte $id;

        private /* synthetic */ AnonymousClass2(byte b) {
            this.$id = b;
        }

        public final void accept(Object obj, int i) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(obj, i);
                    return;
                case (byte) 1:
                    $m$1(obj, i);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private /* synthetic */ -$Lambda$QgGTJrv63_zzBbeGjswm_UMqEbo(byte b) {
        this.$id = b;
    }

    public final int applyAsInt(int i, int i2) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(i, i2);
            case (byte) 1:
                return $m$1(i, i2);
            case (byte) 2:
                return $m$2(i, i2);
            default:
                throw new AssertionError();
        }
    }
}

package android.os;

import java.util.Iterator;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

final /* synthetic */ class -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA implements ToIntFunction {
    public static final /* synthetic */ -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA $INST$0 = new -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA((byte) 0);
    public static final /* synthetic */ -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA $INST$1 = new -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA((byte) 1);
    private final /* synthetic */ byte $id;

    /* renamed from: android.os.-$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f103-$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return HidlSupport.deepEquals(((Iterator) this.f103-$f0).next(), arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f103-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: android.os.-$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA$2 */
    final /* synthetic */ class AnonymousClass2 implements IntPredicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f104-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f105-$f1;

        private final /* synthetic */ boolean $m$0(int arg0) {
            return HidlSupport.deepEquals(((Object[]) this.f104-$f0)[arg0], ((Object[]) this.f105-$f1)[arg0]);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.f104-$f0 = obj;
            this.f105-$f1 = obj2;
        }

        public final boolean test(int i) {
            return $m$0(i);
        }
    }

    private /* synthetic */ -$Lambda$G_Gcg0ia_B_NRvJUIh_Nis__dWA(byte b) {
        this.$id = b;
    }

    public final int applyAsInt(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj);
            case (byte) 1:
                return $m$1(obj);
            default:
                throw new AssertionError();
        }
    }
}

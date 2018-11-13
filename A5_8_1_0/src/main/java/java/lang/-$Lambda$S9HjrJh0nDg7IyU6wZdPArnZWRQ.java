package java.lang;

import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$S9HjrJh0nDg7IyU6wZdPArnZWRQ implements Supplier {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f22-$f0;

    private final /* synthetic */ Object $m$0() {
        return ((CharSequence) this.f22-$f0).m11lambda$-java_lang_CharSequence_6032();
    }

    private final /* synthetic */ Object $m$1() {
        return ((CharSequence) this.f22-$f0).m12lambda$-java_lang_CharSequence_8746();
    }

    public /* synthetic */ -$Lambda$S9HjrJh0nDg7IyU6wZdPArnZWRQ(byte b, Object obj) {
        this.$id = b;
        this.f22-$f0 = obj;
    }

    public final Object get() {
        switch (this.$id) {
            case (byte) 0:
                return $m$0();
            case (byte) 1:
                return $m$1();
            default:
                throw new AssertionError();
        }
    }
}

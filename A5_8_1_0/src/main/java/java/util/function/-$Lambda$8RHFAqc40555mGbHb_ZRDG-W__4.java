package java.util.function;

final /* synthetic */ class -$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4 implements Function {
    public static final /* synthetic */ -$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4 $INST$0 = new -$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4();

    /* renamed from: java.util.function.-$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4$1 */
    final /* synthetic */ class AnonymousClass1 implements Function {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f64-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f65-$f1;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((Function) this.f64-$f0).m3lambda$-java_util_function_Function_3525((Function) this.f65-$f1, arg0);
        }

        private final /* synthetic */ Object $m$1(Object arg0) {
            return ((Function) this.f64-$f0).m2lambda$-java_util_function_Function_2660((Function) this.f65-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f64-$f0 = obj;
            this.f65-$f1 = obj2;
        }

        public final Object apply(Object obj) {
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

    private /* synthetic */ -$Lambda$8RHFAqc40555mGbHb_ZRDG-W__4() {
    }

    public final Object apply(Object obj) {
        return $m$0(obj);
    }
}

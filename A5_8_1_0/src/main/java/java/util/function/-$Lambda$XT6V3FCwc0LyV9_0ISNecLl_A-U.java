package java.util.function;

final /* synthetic */ class -$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U implements BiPredicate {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f177-$f0;

    /* renamed from: java.util.function.-$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U$1 */
    final /* synthetic */ class AnonymousClass1 implements BiPredicate {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f178-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f179-$f1;

        private final /* synthetic */ boolean $m$0(Object arg0, Object arg1) {
            return ((BiPredicate) this.f178-$f0).m25lambda$-java_util_function_BiPredicate_2994((BiPredicate) this.f179-$f1, arg0, arg1);
        }

        private final /* synthetic */ boolean $m$1(Object arg0, Object arg1) {
            return ((BiPredicate) this.f178-$f0).m27lambda$-java_util_function_BiPredicate_4269((BiPredicate) this.f179-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f178-$f0 = obj;
            this.f179-$f1 = obj2;
        }

        public final boolean test(Object obj, Object obj2) {
            switch (this.$id) {
                case (byte) 0:
                    return $m$0(obj, obj2);
                case (byte) 1:
                    return $m$1(obj, obj2);
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ boolean $m$0(Object arg0, Object arg1) {
        return ((BiPredicate) this.f177-$f0).m26lambda$-java_util_function_BiPredicate_3305(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U(Object obj) {
        this.f177-$f0 = obj;
    }

    public final boolean test(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}

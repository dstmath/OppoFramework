package java.util.function;

final /* synthetic */ class -$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE implements Predicate {
    public static final /* synthetic */ -$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE $INST$0 = new -$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE();

    /* renamed from: java.util.function.-$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f61-$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return this.f61-$f0.lambda$-java_util_function_Predicate_4628(arg0);
        }

        private final /* synthetic */ boolean $m$1(Object arg0) {
            return ((Predicate) this.f61-$f0).m21lambda$-java_util_function_Predicate_3052(arg0);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj) {
            this.$id = b;
            this.f61-$f0 = obj;
        }

        public final boolean test(Object obj) {
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

    /* renamed from: java.util.function.-$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE$2 */
    final /* synthetic */ class AnonymousClass2 implements Predicate {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f62-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f63-$f1;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((Predicate) this.f62-$f0).m20lambda$-java_util_function_Predicate_2759((Predicate) this.f63-$f1, arg0);
        }

        private final /* synthetic */ boolean $m$1(Object arg0) {
            return ((Predicate) this.f62-$f0).m22lambda$-java_util_function_Predicate_3988((Predicate) this.f63-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass2(byte b, Object obj, Object obj2) {
            this.$id = b;
            this.f62-$f0 = obj;
            this.f63-$f1 = obj2;
        }

        public final boolean test(Object obj) {
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

    private /* synthetic */ -$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE() {
    }

    public final boolean test(Object obj) {
        return $m$0(obj);
    }
}

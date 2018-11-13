package android.hardware.radio;

import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$YT5WdsCCCONt9rJHRq-uXhDUWbM implements Predicate {
    public static final /* synthetic */ -$Lambda$YT5WdsCCCONt9rJHRq-uXhDUWbM $INST$0 = new -$Lambda$YT5WdsCCCONt9rJHRq-uXhDUWbM((byte) 0);
    public static final /* synthetic */ -$Lambda$YT5WdsCCCONt9rJHRq-uXhDUWbM $INST$1 = new -$Lambda$YT5WdsCCCONt9rJHRq-uXhDUWbM((byte) 1);
    private final /* synthetic */ byte $id;

    private /* synthetic */ -$Lambda$YT5WdsCCCONt9rJHRq-uXhDUWbM(byte b) {
        this.$id = b;
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

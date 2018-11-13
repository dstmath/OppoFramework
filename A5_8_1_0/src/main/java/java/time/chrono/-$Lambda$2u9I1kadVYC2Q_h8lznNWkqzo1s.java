package java.time.chrono;

import java.io.Serializable;
import java.util.Comparator;

final /* synthetic */ class -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s implements Comparator, Serializable {
    public static final /* synthetic */ -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s $INST$0 = new -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s((byte) 0);
    public static final /* synthetic */ -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s $INST$1 = new -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s((byte) 1);
    public static final /* synthetic */ -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s $INST$2 = new -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s((byte) 2);
    private final /* synthetic */ byte $id;

    private /* synthetic */ -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s(byte b) {
        this.$id = b;
    }

    public final int compare(Object obj, Object obj2) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj, obj2);
            case (byte) 1:
                return $m$1(obj, obj2);
            case (byte) 2:
                return $m$2(obj, obj2);
            default:
                throw new AssertionError();
        }
    }
}

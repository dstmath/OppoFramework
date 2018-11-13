package android.os;

final /* synthetic */ class -$Lambda$BcGBlsGjMZMF6Ej78rWJ608MYSM implements Runnable {
    public static final /* synthetic */ -$Lambda$BcGBlsGjMZMF6Ej78rWJ608MYSM $INST$0 = new -$Lambda$BcGBlsGjMZMF6Ej78rWJ608MYSM((byte) 0);
    public static final /* synthetic */ -$Lambda$BcGBlsGjMZMF6Ej78rWJ608MYSM $INST$1 = new -$Lambda$BcGBlsGjMZMF6Ej78rWJ608MYSM((byte) 1);
    private final /* synthetic */ byte $id;

    private /* synthetic */ -$Lambda$BcGBlsGjMZMF6Ej78rWJ608MYSM(byte b) {
        this.$id = b;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            default:
                throw new AssertionError();
        }
    }
}

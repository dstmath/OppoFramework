package com.android.server.wm;

import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0 implements Predicate {
    public static final /* synthetic */ -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0 $INST$0 = new -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0((byte) 0);
    public static final /* synthetic */ -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0 $INST$1 = new -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0((byte) 1);
    public static final /* synthetic */ -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0 $INST$2 = new -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0((byte) 2);
    public static final /* synthetic */ -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0 $INST$3 = new -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0((byte) 3);
    private final /* synthetic */ byte $id;

    private /* synthetic */ -$Lambda$6vWlz1P88lMkMG4g2BvX9ZYCNN0(byte b) {
        this.$id = b;
    }

    public final boolean test(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj);
            case (byte) 1:
                return $m$1(obj);
            case (byte) 2:
                return $m$2(obj);
            case (byte) 3:
                return $m$3(obj);
            default:
                throw new AssertionError();
        }
    }
}

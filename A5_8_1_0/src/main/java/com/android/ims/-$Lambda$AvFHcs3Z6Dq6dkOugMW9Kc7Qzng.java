package com.android.ims;

import android.net.Uri;
import android.telephony.ims.ImsServiceProxy.INotifyStatusChanged;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng implements Consumer {
    public static final /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng $INST$0 = new -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng((byte) 0);
    public static final /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng $INST$1 = new -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng((byte) 1);
    public static final /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng $INST$2 = new -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng((byte) 2);
    public static final /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng $INST$3 = new -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng((byte) 3);
    public static final /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng $INST$4 = new -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng((byte) 4);
    public static final /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng $INST$5 = new -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng((byte) 5);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$1 */
    final /* synthetic */ class AnonymousClass1 implements INotifyStatusChanged {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f0-$f0;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.f0-$f0).m3lambda$-com_android_ims_ImsManager_89494();
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f0-$f0 = obj;
        }

        public final void notifyStatusChanged() {
            $m$0();
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f1-$f0;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.f1-$f0).-com_android_ims_ImsManager-mthref-0();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f1-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$3 */
    final /* synthetic */ class AnonymousClass3 implements Consumer {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f2-$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_101553((Uri[]) this.f2-$f0);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_99343((ImsReasonInfo) this.f2-$f0);
        }

        public /* synthetic */ AnonymousClass3(byte b, Object obj) {
            this.$id = b;
            this.f2-$f0 = obj;
        }

        public final void accept(Object obj) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(obj);
                    return;
                case (byte) 1:
                    $m$1(obj);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$4 */
    final /* synthetic */ class AnonymousClass4 implements Consumer {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f3-$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_98401(this.f3-$f0);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_98908(this.f3-$f0);
        }

        private final /* synthetic */ void $m$2(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_101235(this.f3-$f0);
        }

        public /* synthetic */ AnonymousClass4(byte b, int i) {
            this.$id = b;
            this.f3-$f0 = i;
        }

        public final void accept(Object obj) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(obj);
                    return;
                case (byte) 1:
                    $m$1(obj);
                    return;
                case (byte) 2:
                    $m$2(obj);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$5 */
    final /* synthetic */ class AnonymousClass5 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f4-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f5-$f1;

        private final /* synthetic */ void $m$0() {
            ((ImsManager) this.f5-$f1).m1lambda$-com_android_ims_ImsManager_41891(this.f4-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((ImsManager) this.f5-$f1).m2lambda$-com_android_ims_ImsManager_45227(this.f4-$f0);
        }

        public /* synthetic */ AnonymousClass5(byte b, int i, Object obj) {
            this.$id = b;
            this.f4-$f0 = i;
            this.f5-$f1 = obj;
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

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$6 */
    final /* synthetic */ class AnonymousClass6 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f6-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f7-$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_101993(this.f6-$f0, (ImsReasonInfo) this.f7-$f1);
        }

        public /* synthetic */ AnonymousClass6(int i, Object obj) {
            this.f6-$f0 = i;
            this.f7-$f1 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.ims.-$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng$7 */
    final /* synthetic */ class AnonymousClass7 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f8-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f9-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f10-$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ImsConnectionStateListener) arg0).lambda$-com_android_ims_ImsManager$ImsRegistrationListenerProxy_100864(this.f8-$f0, (int[]) this.f9-$f1, (int[]) this.f10-$f2);
        }

        public /* synthetic */ AnonymousClass7(int i, Object obj, Object obj2) {
            this.f8-$f0 = i;
            this.f9-$f1 = obj;
            this.f10-$f2 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private /* synthetic */ -$Lambda$AvFHcs3Z6Dq6dkOugMW9Kc7Qzng(byte b) {
        this.$id = b;
    }

    public final void accept(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                $m$0(obj);
                return;
            case (byte) 1:
                $m$1(obj);
                return;
            case (byte) 2:
                $m$2(obj);
                return;
            case (byte) 3:
                $m$3(obj);
                return;
            case (byte) 4:
                $m$4(obj);
                return;
            case (byte) 5:
                $m$5(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}

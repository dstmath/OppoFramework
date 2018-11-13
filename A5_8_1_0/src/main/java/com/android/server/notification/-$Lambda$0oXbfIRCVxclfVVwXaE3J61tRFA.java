package com.android.server.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.os.UserHandle;
import android.os.VibrationEffect;
import com.android.server.notification.ManagedServices.ManagedServiceInfo;
import com.android.server.notification.NotificationManagerService.AnonymousClass15;
import com.android.server.notification.NotificationManagerService.NotificationListeners;

final /* synthetic */ class -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA implements FlagChecker {
    public static final /* synthetic */ -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA $INST$0 = new -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA();

    /* renamed from: com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f266-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f267-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f268-$f2;

        private final /* synthetic */ void $m$0() {
            ((NotificationManagerService) this.f266-$f0).m181xd527677((NotificationRecord) this.f267-$f1, (VibrationEffect) this.f268-$f2);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3) {
            this.f266-$f0 = obj;
            this.f267-$f1 = obj2;
            this.f268-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f269-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f270-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f271-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f272-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f273-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f274-$f5;

        private final /* synthetic */ void $m$0() {
            ((NotificationListeners) this.f270-$f1).m184xbe46aa06((ManagedServiceInfo) this.f271-$f2, (String) this.f272-$f3, (UserHandle) this.f273-$f4, (NotificationChannel) this.f274-$f5, this.f269-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((NotificationListeners) this.f270-$f1).m185xbe471704((ManagedServiceInfo) this.f271-$f2, (String) this.f272-$f3, (UserHandle) this.f273-$f4, (NotificationChannelGroup) this.f274-$f5, this.f269-$f0);
        }

        public /* synthetic */ AnonymousClass2(byte b, int i, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.$id = b;
            this.f269-$f0 = i;
            this.f270-$f1 = obj;
            this.f271-$f2 = obj2;
            this.f272-$f3 = obj3;
            this.f273-$f4 = obj4;
            this.f274-$f5 = obj5;
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

    /* renamed from: com.android.server.notification.-$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA$3 */
    final /* synthetic */ class AnonymousClass3 implements FlagChecker {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f275-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f276-$f1;

        private final /* synthetic */ boolean $m$0(int arg0) {
            return AnonymousClass15.m182x1ae61dbb(this.f275-$f0, this.f276-$f1, arg0);
        }

        public /* synthetic */ AnonymousClass3(int i, int i2) {
            this.f275-$f0 = i;
            this.f276-$f1 = i2;
        }

        public final boolean apply(int i) {
            return $m$0(i);
        }
    }

    private /* synthetic */ -$Lambda$0oXbfIRCVxclfVVwXaE3J61tRFA() {
    }

    public final boolean apply(int i) {
        return $m$0(i);
    }
}

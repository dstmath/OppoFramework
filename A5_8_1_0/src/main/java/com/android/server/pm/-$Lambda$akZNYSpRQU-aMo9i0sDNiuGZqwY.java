package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY implements Consumer {
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$0 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 0);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$1 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 1);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$2 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 2);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$3 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 3);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$4 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 4);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$5 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 5);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$6 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 6);
    public static final /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY $INST$7 = new -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY((byte) 7);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.server.pm.-$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f297-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f298-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f299-$f2;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutService) this.f298-$f1).m76lambda$-com_android_server_pm_ShortcutService_104586((ShortcutUser) this.f299-$f2, this.f297-$f0, (ApplicationInfo) arg0);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            ShortcutUser.m197lambda$-com_android_server_pm_ShortcutUser_8319(this.f297-$f0, (String) this.f298-$f1, (Consumer) this.f299-$f2, (ShortcutPackageItem) arg0);
        }

        public /* synthetic */ AnonymousClass1(byte b, int i, Object obj, Object obj2) {
            this.$id = b;
            this.f297-$f0 = i;
            this.f298-$f1 = obj;
            this.f299-$f2 = obj2;
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

    private /* synthetic */ -$Lambda$akZNYSpRQU-aMo9i0sDNiuGZqwY(byte b) {
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
            case (byte) 6:
                $m$6(obj);
                return;
            case (byte) 7:
                $m$7(obj);
                return;
            default:
                throw new AssertionError();
        }
    }
}

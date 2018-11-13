package com.android.server.wm;

import android.graphics.Rect;
import android.os.IBinder;
import android.util.MutableBoolean;
import android.view.WindowManagerPolicy.WindowState;
import com.android.internal.util.ToBooleanFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI implements Screenshoter {
    public static final /* synthetic */ -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI $INST$0 = new -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI((byte) 0);
    public static final /* synthetic */ -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI $INST$1 = new -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI((byte) 1);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f393-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f394-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f395-$f2;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.f393-$f0).m228lambda$-com_android_server_wm_DisplayContent_129498((WindowState) this.f394-$f1, (WindowState) this.f395-$f2, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3) {
            this.f393-$f0 = obj;
            this.f394-$f1 = obj2;
            this.f395-$f2 = obj3;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$2 */
    final /* synthetic */ class AnonymousClass2 implements Consumer {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f396-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f397-$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((WindowState) arg0).mWinAnimator.seamlesslyRotateWindow(this.f396-$f0, this.f397-$f1);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            DisplayContent.m224lambda$-com_android_server_wm_DisplayContent_137054(this.f396-$f0, this.f397-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass2(byte b, int i, int i2) {
            this.$id = b;
            this.f396-$f0 = i;
            this.f397-$f1 = i2;
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

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$3 */
    final /* synthetic */ class AnonymousClass3 implements Predicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f398-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f399-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f400-$f2;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.f400-$f2).m226lambda$-com_android_server_wm_DisplayContent_116512(this.f398-$f0, this.f399-$f1, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass3(int i, int i2, Object obj) {
            this.f398-$f0 = i;
            this.f399-$f1 = i2;
            this.f400-$f2 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$4 */
    final /* synthetic */ class AnonymousClass4 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f401-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f402-$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((DisplayContent) this.f402-$f1).m243lambda$-com_android_server_wm_DisplayContent_64986(this.f401-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass4(boolean z, Object obj) {
            this.f401-$f0 = z;
            this.f402-$f1 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.wm.-$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI$5 */
    final /* synthetic */ class AnonymousClass5 implements ToBooleanFunction {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f403-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ boolean f404-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ int f405-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f406-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f407-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f408-$f5;
        /* renamed from: -$f6 */
        private final /* synthetic */ Object f409-$f6;
        /* renamed from: -$f7 */
        private final /* synthetic */ Object f410-$f7;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((DisplayContent) this.f406-$f3).m232lambda$-com_android_server_wm_DisplayContent_151510(this.f405-$f2, this.f403-$f0, (IBinder) this.f407-$f4, (MutableBoolean) this.f408-$f5, this.f404-$f1, (Rect) this.f409-$f6, (Rect) this.f410-$f7, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass5(boolean z, boolean z2, int i, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.f403-$f0 = z;
            this.f404-$f1 = z2;
            this.f405-$f2 = i;
            this.f406-$f3 = obj;
            this.f407-$f4 = obj2;
            this.f408-$f5 = obj3;
            this.f409-$f6 = obj4;
            this.f410-$f7 = obj5;
        }

        public final boolean apply(Object obj) {
            return $m$0(obj);
        }
    }

    private /* synthetic */ -$Lambda$OzPvdnGprtQoLZLCvw2GU8IaGyI(byte b) {
        this.$id = b;
    }

    public final Object screenshot(Rect rect, int i, int i2, int i3, int i4, boolean z, int i5) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(rect, i, i2, i3, i4, z, i5);
            case (byte) 1:
                return $m$1(rect, i, i2, i3, i4, z, i5);
            default:
                throw new AssertionError();
        }
    }
}

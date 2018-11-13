package com.android.server.wm;

import com.android.internal.util.ToBooleanFunction;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$lpBUCbECLvWBIi8CcvaEY5AB7jM implements ToBooleanFunction {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f436-$f0;

    /* renamed from: com.android.server.wm.-$Lambda$lpBUCbECLvWBIi8CcvaEY5AB7jM$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f437-$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((NonAppWindowContainers) this.f437-$f0).m245x641417d7((WindowState) arg0);
        }

        private final /* synthetic */ boolean $m$1(Object arg0) {
            return ((DisplayContent) this.f437-$f0).m240lambda$-com_android_server_wm_DisplayContent_42846((WindowState) arg0);
        }

        private final /* synthetic */ boolean $m$2(Object arg0) {
            return ((DisplayContent) this.f437-$f0).m229lambda$-com_android_server_wm_DisplayContent_132684((WindowState) arg0);
        }

        private final /* synthetic */ boolean $m$3(Object arg0) {
            return WallpaperController.m259lambda$-com_android_server_wm_WallpaperController_23032((WindowState) this.f437-$f0, (WindowState) arg0);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj) {
            this.$id = b;
            this.f437-$f0 = obj;
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

    private final /* synthetic */ boolean $m$0(Object arg0) {
        return ((DisplayContent) this.f436-$f0).m237lambda$-com_android_server_wm_DisplayContent_23059((WindowState) arg0);
    }

    private final /* synthetic */ boolean $m$1(Object arg0) {
        return ((WallpaperController) this.f436-$f0).m260lambda$-com_android_server_wm_WallpaperController_4687((WindowState) arg0);
    }

    public /* synthetic */ -$Lambda$lpBUCbECLvWBIi8CcvaEY5AB7jM(byte b, Object obj) {
        this.$id = b;
        this.f436-$f0 = obj;
    }

    public final boolean apply(Object obj) {
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

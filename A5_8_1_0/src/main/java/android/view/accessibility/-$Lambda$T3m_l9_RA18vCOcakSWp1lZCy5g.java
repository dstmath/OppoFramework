package android.view.accessibility;

import android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityManager.HighTextContrastChangeListener;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;

final /* synthetic */ class -$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f58-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f59-$f1;

    /* renamed from: android.view.accessibility.-$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f60-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f61-$f1;

        private final /* synthetic */ void $m$0() {
            ((AccessibilityStateChangeListener) this.f61-$f1).lambda$-android_view_accessibility_AccessibilityManager_40854(this.f60-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((HighTextContrastChangeListener) this.f61-$f1).lambda$-android_view_accessibility_AccessibilityManager_42776(this.f60-$f0);
        }

        private final /* synthetic */ void $m$2() {
            ((TouchExplorationStateChangeListener) this.f61-$f1).lambda$-android_view_accessibility_AccessibilityManager_41813(this.f60-$f0);
        }

        public /* synthetic */ AnonymousClass1(byte b, boolean z, Object obj) {
            this.$id = b;
            this.f60-$f0 = z;
            this.f61-$f1 = obj;
        }

        public final void run() {
            switch (this.$id) {
                case (byte) 0:
                    $m$0();
                    return;
                case (byte) 1:
                    $m$1();
                    return;
                case (byte) 2:
                    $m$2();
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ void $m$0() {
        ((android.view.accessibility.AccessibilityManager.AnonymousClass1) this.f58-$f0).m4lambda$-android_view_accessibility_AccessibilityManager$1_9166((AccessibilityServicesStateChangeListener) this.f59-$f1);
    }

    public /* synthetic */ -$Lambda$T3m_l9_RA18vCOcakSWp1lZCy5g(Object obj, Object obj2) {
        this.f58-$f0 = obj;
        this.f59-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}

package android.view.autofill;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import java.util.List;

final /* synthetic */ class -$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f62-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f63-$f1;

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f64-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f65-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f66-$f2;

        private final /* synthetic */ void $m$0() {
            AutofillManagerClient.m5x6d47ba26((AutofillManager) this.f64-$f0, (IntentSender) this.f65-$f1, (Intent) this.f66-$f2);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3) {
            this.f64-$f0 = obj;
            this.f65-$f1 = obj2;
            this.f66-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f67-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f68-$f1;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f68-$f1).setSessionFinished(this.f67-$f0);
        }

        public /* synthetic */ AnonymousClass2(int i, Object obj) {
            this.f67-$f0 = i;
            this.f68-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f69-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f70-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f71-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f72-$f3;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f70-$f1).autofill(this.f69-$f0, (List) this.f71-$f2, (List) this.f72-$f3);
        }

        public /* synthetic */ AnonymousClass3(int i, Object obj, Object obj2, Object obj3) {
            this.f69-$f0 = i;
            this.f70-$f1 = obj;
            this.f71-$f2 = obj2;
            this.f72-$f3 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$4 */
    final /* synthetic */ class AnonymousClass4 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f73-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f74-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f75-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f76-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f77-$f4;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f75-$f2).authenticate(this.f73-$f0, this.f74-$f1, (IntentSender) this.f76-$f3, (Intent) this.f77-$f4);
        }

        public /* synthetic */ AnonymousClass4(int i, int i2, Object obj, Object obj2, Object obj3) {
            this.f73-$f0 = i;
            this.f74-$f1 = i2;
            this.f75-$f2 = obj;
            this.f76-$f3 = obj2;
            this.f77-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$5 */
    final /* synthetic */ class AnonymousClass5 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f78-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f79-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ int f80-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f81-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f82-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f83-$f5;
        /* renamed from: -$f6 */
        private final /* synthetic */ Object f84-$f6;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f81-$f3).requestShowFillUi(this.f78-$f0, (AutofillId) this.f82-$f4, this.f79-$f1, this.f80-$f2, (Rect) this.f83-$f5, (IAutofillWindowPresenter) this.f84-$f6);
        }

        public /* synthetic */ AnonymousClass5(int i, int i2, int i3, Object obj, Object obj2, Object obj3, Object obj4) {
            this.f78-$f0 = i;
            this.f79-$f1 = i2;
            this.f80-$f2 = i3;
            this.f81-$f3 = obj;
            this.f82-$f4 = obj2;
            this.f83-$f5 = obj3;
            this.f84-$f6 = obj4;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$6 */
    final /* synthetic */ class AnonymousClass6 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f85-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f86-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f87-$f2;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f87-$f2).setSaveUiState(this.f86-$f1, this.f85-$f0);
        }

        public /* synthetic */ AnonymousClass6(boolean z, int i, Object obj) {
            this.f85-$f0 = z;
            this.f86-$f1 = i;
            this.f87-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$7 */
    final /* synthetic */ class AnonymousClass7 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f88-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f89-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f90-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f91-$f3;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f90-$f2).notifyNoFillUi(this.f89-$f1, (AutofillId) this.f91-$f3, this.f88-$f0);
        }

        public /* synthetic */ AnonymousClass7(boolean z, int i, Object obj, Object obj2) {
            this.f88-$f0 = z;
            this.f89-$f1 = i;
            this.f90-$f2 = obj;
            this.f91-$f3 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$8 */
    final /* synthetic */ class AnonymousClass8 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f92-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f93-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f94-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f95-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f96-$f4;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f94-$f2).setTrackedViews(this.f93-$f1, (AutofillId[]) this.f95-$f3, this.f92-$f0, (AutofillId[]) this.f96-$f4);
        }

        public /* synthetic */ AnonymousClass8(boolean z, int i, Object obj, Object obj2, Object obj3) {
            this.f92-$f0 = z;
            this.f93-$f1 = i;
            this.f94-$f2 = obj;
            this.f95-$f3 = obj2;
            this.f96-$f4 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: android.view.autofill.-$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I$9 */
    final /* synthetic */ class AnonymousClass9 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f97-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ boolean f98-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ boolean f99-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f100-$f3;

        private final /* synthetic */ void $m$0() {
            ((AutofillManager) this.f100-$f3).setState(this.f97-$f0, this.f98-$f1, this.f99-$f2);
        }

        public /* synthetic */ AnonymousClass9(boolean z, boolean z2, boolean z3, Object obj) {
            this.f97-$f0 = z;
            this.f98-$f1 = z2;
            this.f99-$f2 = z3;
            this.f100-$f3 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        ((AutofillManager) this.f62-$f0).requestHideFillUi((AutofillId) this.f63-$f1);
    }

    public /* synthetic */ -$Lambda$6ub2tg3C-4hyczXTkY_CEW2ET8I(Object obj, Object obj2) {
        this.f62-$f0 = obj;
        this.f63-$f1 = obj2;
    }

    public final void run() {
        $m$0();
    }
}

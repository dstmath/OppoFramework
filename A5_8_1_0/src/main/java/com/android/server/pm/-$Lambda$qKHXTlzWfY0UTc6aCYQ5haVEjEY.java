package com.android.server.pm;

import android.content.ComponentName;
import android.content.pm.ShortcutInfo;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY implements Consumer {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f313-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f314-$f1;

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f315-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f316-$f1;

        private final /* synthetic */ void $m$0() {
            ((com.android.server.pm.ShortcutService.AnonymousClass3) this.f316-$f1).m83lambda$-com_android_server_pm_ShortcutService$3_17937(this.f315-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i, Object obj) {
            this.f315-$f0 = i;
            this.f316-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$2 */
    final /* synthetic */ class AnonymousClass2 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f317-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f318-$f1;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutLauncher) arg0).lambda$-com_android_server_pm_ShortcutService_83644((String) this.f318-$f1, this.f317-$f0);
        }

        public /* synthetic */ AnonymousClass2(int i, Object obj) {
            this.f317-$f0 = i;
            this.f318-$f1 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f319-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ long f320-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f321-$f2;

        private final /* synthetic */ void $m$0() {
            ((ShortcutService) this.f321-$f2).m79lambda$-com_android_server_pm_ShortcutService_21923(this.f320-$f1, this.f319-$f0);
        }

        public /* synthetic */ AnonymousClass3(int i, long j, Object obj) {
            this.f319-$f0 = i;
            this.f320-$f1 = j;
            this.f321-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$4 */
    final /* synthetic */ class AnonymousClass4 implements Predicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f322-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ long f323-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f324-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f325-$f3;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return LocalService.m84lambda$-com_android_server_pm_ShortcutService$LocalService_87317(this.f323-$f1, (ArraySet) this.f324-$f2, (ComponentName) this.f325-$f3, this.f322-$f0, (ShortcutInfo) arg0);
        }

        public /* synthetic */ AnonymousClass4(int i, long j, Object obj, Object obj2) {
            this.f322-$f0 = i;
            this.f323-$f1 = j;
            this.f324-$f2 = obj;
            this.f325-$f3 = obj2;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$5 */
    final /* synthetic */ class AnonymousClass5 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f326-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f327-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f328-$f2;

        private final /* synthetic */ void $m$0() {
            ((com.android.server.pm.ShortcutService.AnonymousClass3) this.f328-$f2).m82lambda$-com_android_server_pm_ShortcutService$3_17769(this.f326-$f0, this.f327-$f1);
        }

        public /* synthetic */ AnonymousClass5(int i, int i2, Object obj) {
            this.f326-$f0 = i;
            this.f327-$f1 = i2;
            this.f328-$f2 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$6 */
    final /* synthetic */ class AnonymousClass6 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f329-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f330-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ int f331-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ int f332-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ long f333-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f334-$f5;
        /* renamed from: -$f6 */
        private final /* synthetic */ Object f335-$f6;
        /* renamed from: -$f7 */
        private final /* synthetic */ Object f336-$f7;
        /* renamed from: -$f8 */
        private final /* synthetic */ Object f337-$f8;
        /* renamed from: -$f9 */
        private final /* synthetic */ Object f338-$f9;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((LocalService) this.f334-$f5).m85lambda$-com_android_server_pm_ShortcutService$LocalService_86201(this.f329-$f0, (String) this.f335-$f6, (List) this.f336-$f7, this.f333-$f4, (ComponentName) this.f337-$f8, this.f330-$f1, this.f331-$f2, (ArrayList) this.f338-$f9, this.f332-$f3, (ShortcutPackage) arg0);
        }

        public /* synthetic */ AnonymousClass6(int i, int i2, int i3, int i4, long j, Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
            this.f329-$f0 = i;
            this.f330-$f1 = i2;
            this.f331-$f2 = i3;
            this.f332-$f3 = i4;
            this.f333-$f4 = j;
            this.f334-$f5 = obj;
            this.f335-$f6 = obj2;
            this.f336-$f7 = obj3;
            this.f337-$f8 = obj4;
            this.f338-$f9 = obj5;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY$7 */
    final /* synthetic */ class AnonymousClass7 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f339-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f340-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f341-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f342-$f3;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShortcutService) this.f341-$f2).m81lambda$-com_android_server_pm_ShortcutService_82367((String) this.f342-$f3, this.f340-$f1, this.f339-$f0, (ShortcutUser) arg0);
        }

        public /* synthetic */ AnonymousClass7(boolean z, int i, Object obj, Object obj2) {
            this.f339-$f0 = z;
            this.f340-$f1 = i;
            this.f341-$f2 = obj;
            this.f342-$f3 = obj2;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private final /* synthetic */ void $m$0(Object arg0) {
        ((ShortcutService) this.f313-$f0).m75lambda$-com_android_server_pm_ShortcutService_102815((ArrayList) this.f314-$f1, (ShortcutPackageItem) arg0);
    }

    public /* synthetic */ -$Lambda$qKHXTlzWfY0UTc6aCYQ5haVEjEY(Object obj, Object obj2) {
        this.f313-$f0 = obj;
        this.f314-$f1 = obj2;
    }

    public final void accept(Object obj) {
        $m$0(obj);
    }
}

package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountManagerInternal.OnAppPermissionChangeListener;
import android.os.Bundle;
import android.os.RemoteCallback.OnResultListener;
import com.android.server.content.SyncStorageEngine.EndPoint;
import java.util.Comparator;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA implements Comparator {
    public static final /* synthetic */ -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA $INST$0 = new -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA((byte) 0);
    public static final /* synthetic */ -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA $INST$1 = new -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA((byte) 1);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        public static final /* synthetic */ AnonymousClass1 $INST$0 = new AnonymousClass1((byte) 0);
        public static final /* synthetic */ AnonymousClass1 $INST$1 = new AnonymousClass1((byte) 1);
        private final /* synthetic */ byte $id;

        private /* synthetic */ AnonymousClass1(byte b) {
            this.$id = b;
        }

        public final boolean test(Object obj) {
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

    /* renamed from: com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA$2 */
    final /* synthetic */ class AnonymousClass2 implements OnAppPermissionChangeListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f217-$f0;

        private final /* synthetic */ void $m$0(Account arg0, int arg1) {
            ((SyncManager) this.f217-$f0).m164lambda$-com_android_server_content_SyncManager_28501(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f217-$f0 = obj;
        }

        public final void onAppPermissionChanged(Account account, int i) {
            $m$0(account, i);
        }
    }

    /* renamed from: com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA$3 */
    final /* synthetic */ class AnonymousClass3 implements OnResultListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ long f218-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ long f219-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f220-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f221-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f222-$f4;

        private final /* synthetic */ void $m$0(Bundle arg0) {
            ((SyncHandler) this.f220-$f2).m166x851e89a4((EndPoint) this.f221-$f3, this.f218-$f0, this.f219-$f1, (Bundle) this.f222-$f4, arg0);
        }

        public /* synthetic */ AnonymousClass3(long j, long j2, Object obj, Object obj2, Object obj3) {
            this.f218-$f0 = j;
            this.f219-$f1 = j2;
            this.f220-$f2 = obj;
            this.f221-$f3 = obj2;
            this.f222-$f4 = obj3;
        }

        public final void onResult(Bundle bundle) {
            $m$0(bundle);
        }
    }

    /* renamed from: com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA$4 */
    final /* synthetic */ class AnonymousClass4 implements OnResultListener {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f223-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ int f224-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ int f225-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ long f226-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f227-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f228-$f5;
        /* renamed from: -$f6 */
        private final /* synthetic */ Object f229-$f6;
        /* renamed from: -$f7 */
        private final /* synthetic */ Object f230-$f7;

        private final /* synthetic */ void $m$0(Bundle arg0) {
            ((SyncManager) this.f227-$f4).m165lambda$-com_android_server_content_SyncManager_45398((AccountAndUser) this.f228-$f5, this.f223-$f0, this.f224-$f1, (String) this.f229-$f6, (Bundle) this.f230-$f7, this.f225-$f2, this.f226-$f3, arg0);
        }

        public /* synthetic */ AnonymousClass4(int i, int i2, int i3, long j, Object obj, Object obj2, Object obj3, Object obj4) {
            this.f223-$f0 = i;
            this.f224-$f1 = i2;
            this.f225-$f2 = i3;
            this.f226-$f3 = j;
            this.f227-$f4 = obj;
            this.f228-$f5 = obj2;
            this.f229-$f6 = obj3;
            this.f230-$f7 = obj4;
        }

        public final void onResult(Bundle bundle) {
            $m$0(bundle);
        }
    }

    private /* synthetic */ -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA(byte b) {
        this.$id = b;
    }

    public final int compare(Object obj, Object obj2) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj, obj2);
            case (byte) 1:
                return $m$1(obj, obj2);
            default:
                throw new AssertionError();
        }
    }
}

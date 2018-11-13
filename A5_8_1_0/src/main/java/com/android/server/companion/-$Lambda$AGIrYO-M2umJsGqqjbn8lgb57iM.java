package com.android.server.companion;

import android.content.pm.PackageInfo;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.FunctionalUtils.ThrowingConsumer;
import com.android.internal.util.FunctionalUtils.ThrowingRunnable;
import com.android.internal.util.FunctionalUtils.ThrowingSupplier;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlSerializer;

final /* synthetic */ class -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM implements Function {
    public static final /* synthetic */ -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM $INST$0 = new -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM((byte) 0);
    public static final /* synthetic */ -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM $INST$1 = new -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM((byte) 1);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$1 */
    final /* synthetic */ class AnonymousClass1 implements ThrowingConsumer {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f186-$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((ShellCmd) this.f186-$f0).m157xb6b5bde2((Association) arg0);
        }

        private final /* synthetic */ void $m$1(Object arg0) {
            ((XmlSerializer) this.f186-$f0).startTag(null, CompanionDeviceManagerService.XML_TAG_ASSOCIATION).attribute(null, CompanionDeviceManagerService.XML_ATTR_PACKAGE, ((Association) arg0).companionAppPackage).attribute(null, CompanionDeviceManagerService.XML_ATTR_DEVICE, ((Association) arg0).deviceAddress).endTag(null, CompanionDeviceManagerService.XML_TAG_ASSOCIATION);
        }

        public /* synthetic */ AnonymousClass1(byte b, Object obj) {
            this.$id = b;
            this.f186-$f0 = obj;
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

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f187-$f0;

        private final /* synthetic */ void $m$0() {
            ((CompanionDeviceManagerService) this.f187-$f0).-com_android_server_companion_CompanionDeviceManagerService-mthref-0();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f187-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$3 */
    final /* synthetic */ class AnonymousClass3 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f188-$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            CompanionDeviceManagerService.m152x9eb5e932((Set) this.f188-$f0, (FileOutputStream) arg0);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.f188-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$4 */
    final /* synthetic */ class AnonymousClass4 implements Function {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f189-$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return CollectionUtils.filter((Set) arg0, new AnonymousClass5((String) this.f189-$f0));
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.f189-$f0 = obj;
        }

        public final Object apply(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$5 */
    final /* synthetic */ class AnonymousClass5 implements Predicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f190-$f0;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return (Objects.equals(((Association) arg0).companionAppPackage, (String) this.f190-$f0) ^ 1);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.f190-$f0 = obj;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$6 */
    final /* synthetic */ class AnonymousClass6 implements ThrowingRunnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f191-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f192-$f1;

        private final /* synthetic */ void $m$0() {
            ((CompanionDeviceManagerService) this.f191-$f0).m154x9eaaf569((PackageInfo) this.f192-$f1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.f191-$f0 = obj;
            this.f192-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$7 */
    final /* synthetic */ class AnonymousClass7 implements ThrowingSupplier {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f193-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f194-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f195-$f2;

        private final /* synthetic */ Object $m$0() {
            return ((CompanionDeviceManagerService) this.f194-$f1).m155x9eabcf1b((String) this.f195-$f2, this.f193-$f0);
        }

        public /* synthetic */ AnonymousClass7(int i, Object obj, Object obj2) {
            this.f193-$f0 = i;
            this.f194-$f1 = obj;
            this.f195-$f2 = obj2;
        }

        public final Object get() {
            return $m$0();
        }
    }

    /* renamed from: com.android.server.companion.-$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM$8 */
    final /* synthetic */ class AnonymousClass8 implements Function {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f196-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f197-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f198-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f199-$f3;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return ((CompanionDeviceManagerService) this.f197-$f1).m156x9eb5ca91(this.f196-$f0, (String) this.f198-$f2, (String) this.f199-$f3, (Set) arg0);
        }

        private final /* synthetic */ Object $m$1(Object arg0) {
            return ((CompanionDeviceManagerService) this.f197-$f1).m153x9eaae661(this.f196-$f0, (String) this.f198-$f2, (String) this.f199-$f3, (Set) arg0);
        }

        public /* synthetic */ AnonymousClass8(byte b, int i, Object obj, Object obj2, Object obj3) {
            this.$id = b;
            this.f196-$f0 = i;
            this.f197-$f1 = obj;
            this.f198-$f2 = obj2;
            this.f199-$f3 = obj3;
        }

        public final Object apply(Object obj) {
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

    private /* synthetic */ -$Lambda$AGIrYO-M2umJsGqqjbn8lgb57iM(byte b) {
        this.$id = b;
    }

    public final Object apply(Object obj) {
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

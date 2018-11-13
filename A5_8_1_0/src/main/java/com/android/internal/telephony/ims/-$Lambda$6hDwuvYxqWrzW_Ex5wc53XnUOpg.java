package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Pair;
import com.android.internal.telephony.ims.ImsResolver.ImsServiceControllerFactory;
import java.util.function.Function;
import java.util.function.Supplier;

final /* synthetic */ class -$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg implements Supplier {
    public static final /* synthetic */ -$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg $INST$0 = new -$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg();

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$1 */
    final /* synthetic */ class AnonymousClass1 implements Callback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f42-$f0;

        private final /* synthetic */ boolean $m$0(Message arg0) {
            return ((ImsResolver) this.f42-$f0).m25lambda$-com_android_internal_telephony_ims_ImsResolver_8323(arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f42-$f0 = obj;
        }

        public final boolean handleMessage(Message message) {
            return $m$0(message);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$2 */
    final /* synthetic */ class AnonymousClass2 implements ImsServiceControllerFactory {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f43-$f0;

        private final /* synthetic */ ImsServiceController $m$0(Context arg0, ComponentName arg1) {
            return ((ImsResolver) this.f43-$f0).m24lambda$-com_android_internal_telephony_ims_ImsResolver_7707(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f43-$f0 = obj;
        }

        public final ImsServiceController get(Context context, ComponentName componentName) {
            return $m$0(context, componentName);
        }
    }

    /* renamed from: com.android.internal.telephony.ims.-$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg$3 */
    final /* synthetic */ class AnonymousClass3 implements Function {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f44-$f0;

        private final /* synthetic */ Object $m$0(Object arg0) {
            return new Pair(Integer.valueOf(this.f44-$f0), (Integer) arg0);
        }

        private final /* synthetic */ Object $m$1(Object arg0) {
            return new Pair(Integer.valueOf(this.f44-$f0), (Integer) arg0);
        }

        private final /* synthetic */ Object $m$2(Object arg0) {
            return new Pair(Integer.valueOf(this.f44-$f0), (Integer) arg0);
        }

        public /* synthetic */ AnonymousClass3(byte b, int i) {
            this.$id = b;
            this.f44-$f0 = i;
        }

        public final Object apply(Object obj) {
            switch (this.$id) {
                case (byte) 0:
                    return $m$0(obj);
                case (byte) 1:
                    return $m$1(obj);
                case (byte) 2:
                    return $m$2(obj);
                default:
                    throw new AssertionError();
            }
        }
    }

    private /* synthetic */ -$Lambda$6hDwuvYxqWrzW_Ex5wc53XnUOpg() {
    }

    public final Object get() {
        return $m$0();
    }
}

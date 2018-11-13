package android.app;

import android.app.PendingIntent.OnMarshaledListener;
import android.os.Parcel;

final /* synthetic */ class -$Lambda$vZ1qb742P9hE4drBY-TrOZB_qKo implements OnMarshaledListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f44-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f45-$f1;

    private final /* synthetic */ void $m$0(PendingIntent arg0, Parcel arg1, int arg2) {
        ((Notification) this.f44-$f0).m6lambda$-android_app_Notification_87011((Parcel) this.f45-$f1, arg0, arg1, arg2);
    }

    public /* synthetic */ -$Lambda$vZ1qb742P9hE4drBY-TrOZB_qKo(Object obj, Object obj2) {
        this.f44-$f0 = obj;
        this.f45-$f1 = obj2;
    }

    public final void onMarshaled(PendingIntent pendingIntent, Parcel parcel, int i) {
        $m$0(pendingIntent, parcel, i);
    }
}

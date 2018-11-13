package com.android.internal.content;

import android.os.ParcelFileDescriptor.OnCloseListener;
import java.io.File;
import java.io.IOException;

final /* synthetic */ class -$Lambda$qCDQZ4U5of2rgsneNEo3bc5KTII implements OnCloseListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f130-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f131-$f1;

    private final /* synthetic */ void $m$0(IOException arg0) {
        ((FileSystemProvider) this.f130-$f0).lambda$-com_android_internal_content_FileSystemProvider_15674((File) this.f131-$f1, arg0);
    }

    public /* synthetic */ -$Lambda$qCDQZ4U5of2rgsneNEo3bc5KTII(Object obj, Object obj2) {
        this.f130-$f0 = obj;
        this.f131-$f1 = obj2;
    }

    public final void onClose(IOException iOException) {
        $m$0(iOException);
    }
}

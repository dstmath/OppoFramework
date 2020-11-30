package androidx.test.internal.util;

import androidx.test.internal.util.LogUtil;

/* access modifiers changed from: package-private */
public final /* synthetic */ class LogUtil$$Lambda$1 implements LogUtil.Supplier {
    private final String arg$1;

    LogUtil$$Lambda$1(String str) {
        this.arg$1 = str;
    }

    @Override // androidx.test.internal.util.LogUtil.Supplier
    public Object get() {
        return LogUtil.lambda$logDebugWithProcess$1$LogUtil(this.arg$1);
    }
}

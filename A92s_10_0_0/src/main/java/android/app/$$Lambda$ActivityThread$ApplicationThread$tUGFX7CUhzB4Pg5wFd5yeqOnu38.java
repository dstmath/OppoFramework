package android.app;

import java.util.function.BiConsumer;

/* renamed from: android.app.-$$Lambda$ActivityThread$ApplicationThread$tUGFX7CUhzB4Pg5wFd5yeqOnu38  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityThread$ApplicationThread$tUGFX7CUhzB4Pg5wFd5yeqOnu38 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ActivityThread$ApplicationThread$tUGFX7CUhzB4Pg5wFd5yeqOnu38 INSTANCE = new $$Lambda$ActivityThread$ApplicationThread$tUGFX7CUhzB4Pg5wFd5yeqOnu38();

    private /* synthetic */ $$Lambda$ActivityThread$ApplicationThread$tUGFX7CUhzB4Pg5wFd5yeqOnu38() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityThread) ((ActivityThread) obj)).handleTrimMemory(((Integer) obj2).intValue());
    }
}

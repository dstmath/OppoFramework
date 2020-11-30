package com.android.server.wm;

import com.color.app.ColorAppInfo;
import java.util.function.Predicate;

/* renamed from: com.android.server.wm.-$$Lambda$ColorSplitWindowManagerService$04NqSS2mUHNbEWem9Wf6OTiV2WY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ColorSplitWindowManagerService$04NqSS2mUHNbEWem9Wf6OTiV2WY implements Predicate {
    public static final /* synthetic */ $$Lambda$ColorSplitWindowManagerService$04NqSS2mUHNbEWem9Wf6OTiV2WY INSTANCE = new $$Lambda$ColorSplitWindowManagerService$04NqSS2mUHNbEWem9Wf6OTiV2WY();

    private /* synthetic */ $$Lambda$ColorSplitWindowManagerService$04NqSS2mUHNbEWem9Wf6OTiV2WY() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ColorSplitWindowManagerService.lambda$isHomeVisible$0((ColorAppInfo) obj);
    }
}

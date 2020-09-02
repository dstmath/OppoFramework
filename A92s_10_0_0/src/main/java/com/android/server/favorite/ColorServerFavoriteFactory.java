package com.android.server.favorite;

import android.content.Context;
import com.color.favorite.ColorFavoriteEngines;

public class ColorServerFavoriteFactory {
    private static final String TAG = "ColorServerFavoriteFactory";

    /* renamed from: com.android.server.favorite.ColorServerFavoriteFactory$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$color$favorite$ColorFavoriteEngines = new int[ColorFavoriteEngines.values().length];

        static {
            try {
                $SwitchMap$com$color$favorite$ColorFavoriteEngines[ColorFavoriteEngines.TEDDY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public ColorServerFavoriteEngine setEngine(ColorFavoriteEngines engine) {
        if (AnonymousClass1.$SwitchMap$com$color$favorite$ColorFavoriteEngines[engine.ordinal()] != 1) {
            return new ColorServerEngineNone(null);
        }
        return new ColorServerEngineTeddy();
    }

    private static class ColorServerEngineNone extends ColorServerFavoriteEngine {
        private ColorServerEngineNone() {
        }

        /* synthetic */ ColorServerEngineNone(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.favorite.ColorServerFavoriteEngine
        public void onStartQuery(Context context, String packageName, ColorServerFavoriteCallback callback) {
        }
    }
}

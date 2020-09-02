package com.color.favorite;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;

public class ColorFavoriteFactory {

    /* renamed from: com.color.favorite.ColorFavoriteFactory$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$color$favorite$ColorFavoriteEngines = new int[ColorFavoriteEngines.values().length];

        static {
            try {
                $SwitchMap$com$color$favorite$ColorFavoriteEngines[ColorFavoriteEngines.TEDDY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public ColorFavoriteEngine setEngine(ColorFavoriteEngines engine) {
        if (AnonymousClass1.$SwitchMap$com$color$favorite$ColorFavoriteEngines[engine.ordinal()] != 1) {
            return new ColorEngineNone(null);
        }
        return new ColorEngineTeddy();
    }

    private static class ColorEngineNone extends ColorFavoriteEngine {
        private ColorEngineNone() {
        }

        /* synthetic */ ColorEngineNone(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onInit() {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onRelease() {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onLoadRule(Context context, String data, ColorFavoriteCallback callback) {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onProcessClick(View clickView, ColorFavoriteCallback callback) {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onProcessCrawl(View rootView, ColorFavoriteCallback callback) {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onProcessSave(View rootView, ColorFavoriteCallback callback) {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onLogActivityInfo(Activity activity) {
        }

        /* access modifiers changed from: protected */
        @Override // com.color.favorite.ColorFavoriteEngine
        public void onLogViewInfo(View view) {
        }

        @Override // com.color.favorite.IColorFavoriteEngine
        public Handler getWorkHandler() {
            return null;
        }
    }
}

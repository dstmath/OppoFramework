package com.android.server.favorite;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import cn.teddymobile.free.anteater.AnteaterServer;
import cn.teddymobile.free.anteater.resources.RuleResourcesServer;
import java.lang.ref.WeakReference;

public class ColorServerEngineTeddy extends ColorServerFavoriteEngine {
    /* access modifiers changed from: protected */
    @Override // com.android.server.favorite.ColorServerFavoriteEngine
    public void onStartQuery(Context context, String packageName, ColorServerFavoriteCallback callback) {
        AnteaterServer.getInstance().startQuery(context, packageName, new AnteaterQueryCallback(callback));
    }

    private static class AnteaterQueryCallback implements RuleResourcesServer.QueryCallback {
        private final WeakReference<ColorServerFavoriteCallback> mCallback;

        public AnteaterQueryCallback(ColorServerFavoriteCallback callback) {
            this.mCallback = new WeakReference<>(callback);
        }

        public Handler createWorkHandler(Context context, String name, int priority) {
            ColorServerFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                return callback.createWorkHandler(context, name, priority);
            }
            return null;
        }

        public void unlinkBinderToDeath(IBinder.DeathRecipient deathRecipient) {
            ColorServerFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                callback.unlinkBinderToDeath(deathRecipient);
            }
        }

        public void linkBinderToDeath(IBinder.DeathRecipient deathRecipient) {
            ColorServerFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                callback.linkBinderToDeath(deathRecipient);
            }
        }

        public void onQueryResult(Context context, String data) {
            ColorServerFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                callback.onQueryResult(context, data);
            }
        }
    }
}

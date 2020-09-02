package com.android.server.favorite;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.color.favorite.ColorFavoriteEngines;
import com.color.favorite.ColorFavoriteQueryResult;
import com.color.favorite.IColorFavoriteConstans;
import com.color.favorite.IColorFavoriteQueryCallback;
import com.color.util.ColorLog;
import java.lang.ref.WeakReference;

public class ColorServerFavoriteManager implements IColorFavoriteConstans {
    public static final boolean DBG = true;
    public static final String TAG = "ColorServerFavoriteManager";
    private static volatile ColorServerFavoriteManager sInstance = null;
    private IColorServerFavoriteEngine mEngine = null;
    private final ColorServerFavoriteFactory mFactory = new ColorServerFavoriteFactory();

    private ColorServerFavoriteManager() {
        setEngine(ColorFavoriteEngines.TEDDY);
    }

    public static ColorServerFavoriteManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorServerFavoriteManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorServerFavoriteManager();
                }
            }
        }
        return sInstance;
    }

    public void setEngine(ColorFavoriteEngines engine) {
        ColorLog.d(true, "AnteaterFavorite", "[ColorServerFavoriteManager] setEngine : " + engine);
        this.mEngine = this.mFactory.setEngine(engine);
    }

    public void startQuery(Context context, String packageName, IColorFavoriteQueryCallback callback) {
        IColorServerFavoriteEngine iColorServerFavoriteEngine = this.mEngine;
        if (iColorServerFavoriteEngine != null) {
            iColorServerFavoriteEngine.startQuery(context, packageName, new FavoriteQueryCallback(callback));
        }
    }

    private static class FavoriteQueryCallback extends ColorServerFavoriteCallback {
        private final WeakReference<IColorFavoriteQueryCallback> mCallback;

        public FavoriteQueryCallback(IColorFavoriteQueryCallback callback) {
            this.mCallback = new WeakReference<>(callback);
        }

        @Override // com.android.server.favorite.IColorServerFavoriteCallback
        public void unlinkBinderToDeath(IBinder.DeathRecipient deathRecipient) {
            IBinder binder;
            IColorFavoriteQueryCallback callback = this.mCallback.get();
            if (callback != null && (binder = callback.asBinder()) != null) {
                binder.unlinkToDeath(deathRecipient, 0);
            }
        }

        @Override // com.android.server.favorite.IColorServerFavoriteCallback
        public void linkBinderToDeath(IBinder.DeathRecipient deathRecipient) {
            IBinder binder;
            IColorFavoriteQueryCallback callback = this.mCallback.get();
            if (callback != null && (binder = callback.asBinder()) != null) {
                try {
                    binder.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    ColorLog.w("AnteaterFavorite", e.getMessage(), e);
                } catch (Exception e2) {
                    ColorLog.e("AnteaterFavorite", e2.getMessage(), e2);
                }
            }
        }

        @Override // com.android.server.favorite.IColorServerFavoriteCallback
        public void onQueryResult(Context context, String data) {
            IColorFavoriteQueryCallback callback = this.mCallback.get();
            if (callback != null) {
                try {
                    ColorFavoriteQueryResult result = new ColorFavoriteQueryResult();
                    if (!TextUtils.isEmpty(data)) {
                        result.getBundle().putString("data", data);
                    }
                    callback.onQueryResult(result);
                } catch (RemoteException e) {
                    ColorLog.w(true, "AnteaterFavorite", e.getMessage(), e);
                } catch (Exception e2) {
                    ColorLog.e("AnteaterFavorite", e2.getMessage(), e2);
                }
            }
        }
    }
}

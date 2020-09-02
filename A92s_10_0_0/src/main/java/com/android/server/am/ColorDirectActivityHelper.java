package com.android.server.am;

import android.app.IColorDirectActivityManager;
import android.content.Context;
import android.os.Parcel;
import com.android.server.favorite.ColorServerFavoriteManager;
import com.color.favorite.IColorFavoriteQueryCallback;

public class ColorDirectActivityHelper implements IColorDirectActivityManager {
    private static final String TAG = "ColorDirectActivityHelper";
    private final Context mContext;
    private final ActivityManagerService mService;

    public ColorDirectActivityHelper(Context context, ActivityManagerService service) {
        this.mContext = context;
        this.mService = service;
    }

    public void favoriteQueryRule(String packageName, IColorFavoriteQueryCallback callback) {
        ColorServerFavoriteManager.getInstance().startQuery(this.mContext, packageName, callback);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        if (code != 10048) {
            return false;
        }
        data.enforceInterface("android.app.IActivityManager");
        favoriteQueryRule(data.readString(), IColorFavoriteQueryCallback.Stub.asInterface(data.readStrongBinder()));
        return true;
    }
}

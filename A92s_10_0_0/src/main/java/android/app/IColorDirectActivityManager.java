package android.app;

import android.os.RemoteException;
import com.color.favorite.IColorFavoriteQueryCallback;

public interface IColorDirectActivityManager {
    void favoriteQueryRule(String str, IColorFavoriteQueryCallback iColorFavoriteQueryCallback) throws RemoteException;
}

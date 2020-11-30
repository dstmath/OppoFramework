package com.color.favorite;

import com.color.favorite.IColorFavoriteQueryCallback;

public abstract class ColorFavoriteQueryCallback extends IColorFavoriteQueryCallback.Stub {
    @Override // com.color.favorite.IColorFavoriteQueryCallback
    public void onQueryResult(ColorFavoriteQueryResult result) {
    }
}

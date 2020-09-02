package cn.teddymobile.free.anteater.resources;

import android.os.IBinder;
import cn.teddymobile.free.anteater.resources.RuleResourcesServer;
import java.util.Map;

public class RuleServerCallback implements IBinder.DeathRecipient {
    private final Map<String, RuleServerCallback> mCache;
    private RuleResourcesServer.QueryCallback mCallback = null;

    public RuleServerCallback(Map<String, RuleServerCallback> cache) {
        this.mCache = cache;
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        setCallback(null);
    }

    public void setCallback(RuleResourcesServer.QueryCallback callback) {
        RuleResourcesServer.QueryCallback queryCallback = this.mCallback;
        if (queryCallback != null) {
            queryCallback.unlinkBinderToDeath(this);
        }
        this.mCallback = callback;
        RuleResourcesServer.QueryCallback queryCallback2 = this.mCallback;
        if (queryCallback2 != null) {
            queryCallback2.linkBinderToDeath(this);
            return;
        }
        synchronized (this.mCache) {
            this.mCache.remove(this);
        }
    }

    public RuleResourcesServer.QueryCallback getCallback() {
        return this.mCallback;
    }
}

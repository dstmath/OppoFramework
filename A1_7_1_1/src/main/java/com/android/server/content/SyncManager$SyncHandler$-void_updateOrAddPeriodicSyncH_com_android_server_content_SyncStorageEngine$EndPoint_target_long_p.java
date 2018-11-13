package com.android.server.content;

import android.os.Bundle;
import android.os.RemoteCallback.OnResultListener;
import com.android.server.content.SyncStorageEngine.EndPoint;

final /* synthetic */ class SyncManager$SyncHandler$-void_updateOrAddPeriodicSyncH_com_android_server_content_SyncStorageEngine$EndPoint_target_long_pollFrequency_long_flex_android_os_Bundle_extras_LambdaImpl0 implements OnResultListener {
    private /* synthetic */ Bundle val$extras;
    private /* synthetic */ long val$flex;
    private /* synthetic */ long val$pollFrequency;
    private /* synthetic */ EndPoint val$target;
    private /* synthetic */ SyncHandler val$this;

    public /* synthetic */ SyncManager$SyncHandler$-void_updateOrAddPeriodicSyncH_com_android_server_content_SyncStorageEngine$EndPoint_target_long_pollFrequency_long_flex_android_os_Bundle_extras_LambdaImpl0(SyncHandler syncHandler, EndPoint endPoint, long j, long j2, Bundle bundle) {
        this.val$this = syncHandler;
        this.val$target = endPoint;
        this.val$pollFrequency = j;
        this.val$flex = j2;
        this.val$extras = bundle;
    }

    public void onResult(Bundle arg0) {
        this.val$this.m24-com_android_server_content_SyncManager$SyncHandler_lambda$1(this.val$target, this.val$pollFrequency, this.val$flex, this.val$extras, arg0);
    }
}

package com.coloros.eventhub.sdk;

import com.coloros.eventhub.sdk.aidl.DeviceEventResult;
import com.coloros.eventhub.sdk.aidl.IEventCallback;

public abstract class EventCallback extends IEventCallback.Stub {
    @Override // com.coloros.eventhub.sdk.aidl.IEventCallback
    public abstract void onEventStateChanged(DeviceEventResult deviceEventResult);
}

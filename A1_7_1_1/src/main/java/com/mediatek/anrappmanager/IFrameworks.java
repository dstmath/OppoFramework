package com.mediatek.anrappmanager;

import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue;

public interface IFrameworks {
    String getActivityManagerDescriptor();

    MessageQueue looperGetQueue(Looper looper);

    String messageQueueDumpMessageQueue(MessageQueue messageQueue);

    IBinder serviceManagerGetService(String str);

    long systemClockCurrentTimeMicro();
}

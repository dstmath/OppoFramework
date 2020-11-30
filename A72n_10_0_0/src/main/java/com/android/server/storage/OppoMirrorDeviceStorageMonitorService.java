package com.android.server.storage;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefObject;
import java.util.concurrent.atomic.AtomicInteger;

public class OppoMirrorDeviceStorageMonitorService {
    public static Class<?> TYPE = RefClass.load(OppoMirrorDeviceStorageMonitorService.class, DeviceStorageMonitorService.class);
    public static RefObject<AtomicInteger> mSeq;
}

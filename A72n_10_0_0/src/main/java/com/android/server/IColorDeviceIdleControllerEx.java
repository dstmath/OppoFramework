package com.android.server;

public interface IColorDeviceIdleControllerEx extends IOppoDeviceIdleControllerEx {
    @Override // com.android.server.IOppoDeviceIdleControllerEx
    DeviceIdleController getDeviceIdleController();
}

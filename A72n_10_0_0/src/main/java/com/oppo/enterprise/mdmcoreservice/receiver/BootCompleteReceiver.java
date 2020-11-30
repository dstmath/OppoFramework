package com.oppo.enterprise.mdmcoreservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.help.DeviceAudioInitManagerHelper;
import com.oppo.enterprise.mdmcoreservice.help.DeviceInitManagerHelper;
import com.oppo.enterprise.mdmcoreservice.help.ThreadManagerHelper;
import com.oppo.enterprise.mdmcoreservice.service.InitService;

public class BootCompleteReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d("BootCompleteReceiver", "get BOOT_COMPLETED broadcast, start mdm  service");
            context.startService(new Intent(context, InitService.class));
            initDevice(context);
            initAudioSettingManager(context);
        }
    }

    private void initDevice(Context context) {
        final DeviceInitManagerHelper deviceInitManager = new DeviceInitManagerHelper(context);
        if (deviceInitManager.hasNoInit()) {
            ThreadManagerHelper.getInstance().postInTheadPool(new Runnable() {
                /* class com.oppo.enterprise.mdmcoreservice.receiver.BootCompleteReceiver.AnonymousClass1 */

                public void run() {
                    deviceInitManager.initProp();
                }
            });
        }
    }

    private void initAudioSettingManager(Context context) {
        final DeviceAudioInitManagerHelper deviceAudioInitManager = new DeviceAudioInitManagerHelper(context);
        ThreadManagerHelper.getInstance().postInTheadPool(new Runnable() {
            /* class com.oppo.enterprise.mdmcoreservice.receiver.BootCompleteReceiver.AnonymousClass2 */

            public void run() {
                deviceAudioInitManager.setSpkDefaultSetting();
                deviceAudioInitManager.setMicDefaultSetting();
            }
        });
    }
}

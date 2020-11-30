package com.oppo.enterprise.mdmcoreservice.help;

import android.content.Context;
import android.media.AudioSystem;
import android.os.SystemProperties;

public class DeviceAudioInitManagerHelper {
    private Context mContext;

    public DeviceAudioInitManagerHelper(Context context) {
        this.mContext = context;
    }

    public void setSpkDefaultSetting() {
        LogHelper.e("DeviceAudioInitManagerHelper", "set speaker mute");
        String spk = SystemProperties.get("persist.sys.spk.forbid", (String) null);
        if (spk.equals("1")) {
            LogHelper.e("DeviceAudioInitManagerHelper", "speaker mute after set property");
            AudioSystem.setParameters("speaker_mute=On");
        } else if (spk.equals("0")) {
            LogHelper.e("DeviceAudioInitManagerHelper", "speaker unmute after set property");
            AudioSystem.setParameters("speaker_mute=Off");
        } else {
            LogHelper.e("DeviceAudioInitManagerHelper", "No speaker mute property");
        }
    }

    public void setMicDefaultSetting() {
        LogHelper.e("DeviceAudioInitManagerHelper", "set microphone mute");
        AudioSystem.setParameters("custom_enable=enable");
        String mic = SystemProperties.get("persist.sys.mic.forbid", (String) null);
        if (mic.equals("1")) {
            LogHelper.e("DeviceAudioInitManagerHelper", "disable microphone after setting property");
            AudioSystem.setParameters("mute_mic_enable=on");
            AudioSystem.muteMicrophone(true);
        } else if (mic.equals("0")) {
            LogHelper.e("DeviceAudioInitManagerHelper", "enable microphone after setting property");
            AudioSystem.setParameters("mute_mic_enable=off");
            AudioSystem.muteMicrophone(false);
        } else {
            LogHelper.e("DeviceAudioInitManagerHelper", "No microphone mute property");
        }
    }
}

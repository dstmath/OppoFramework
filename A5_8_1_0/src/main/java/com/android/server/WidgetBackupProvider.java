package com.android.server;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import java.util.List;

public interface WidgetBackupProvider {
    List<String> getWidgetParticipants(int i);

    byte[] getWidgetState(String str, int i);

    void restoreFinished(int i);

    void restoreStarting(int i);

    void restoreWidgetState(String str, byte[] bArr, int i);

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
    void updateProvidersForPackage(String str, int i);
}

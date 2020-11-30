package com.android.server.am;

import android.content.Context;
import com.android.server.ColorLocalServices;
import com.android.server.wm.ColorDummyFreeformManager;
import com.android.server.wm.IColorFreeformManager;

public class ColorDummyActivityManagerServiceEx extends OppoDummyActivityManagerServiceEx implements IColorActivityManagerServiceEx {
    public ColorDummyActivityManagerServiceEx(Context context, ActivityManagerService ams) {
        super(context, ams);
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
        registerDummyColorCustomManager();
    }

    @Override // com.android.server.am.IColorActivityManagerServiceEx
    public IColorBroadcastQueueEx getColorBroadcastQueueEx(BroadcastQueue queue) {
        return new ColorDummyBroadcastQueueEx(queue);
    }

    @Override // com.android.server.am.IColorActivityManagerServiceEx
    public IColorActivityManagerServiceInner getColorActivityManagerServiceInner() {
        return null;
    }

    @Override // com.android.server.am.IColorActivityManagerServiceEx
    public int getProcPid(int pid) {
        return 0;
    }

    @Override // com.android.server.am.IColorActivityManagerServiceEx
    public void putProcInfoArray(int pid, int uid) {
    }

    @Override // com.android.server.am.IColorActivityManagerServiceEx
    public void deleteProcInfoArray(int pid) {
    }

    @Override // com.android.server.am.IColorActivityManagerServiceEx
    public ProcessRecord getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        return this.mAms.getProcessRecordLocked(processName, uid, keepIfLarge);
    }

    private void registerDummyColorCustomManager() {
        registerColorFreeformManager();
    }

    private void registerColorFreeformManager() {
        ColorLocalServices.addService(IColorFreeformManager.class, new ColorDummyFreeformManager());
    }
}

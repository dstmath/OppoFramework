package com.android.server.wm;

import android.content.Context;
import android.os.Parcel;
import android.view.IColorDirectWindowManager;
import com.color.direct.ColorDirectFindCmd;

public class ColorDirectWindowHelper implements IColorDirectWindowManager {
    private static final String TAG = "DirectService";
    private final ColorDirectWindowDump mDump;
    private final WindowManagerService mService;

    public ColorDirectWindowHelper(Context context, WindowManagerService service) {
        this.mService = service;
        this.mDump = new ColorDirectWindowDump(context, service);
    }

    public void directFindCmd(ColorDirectFindCmd findCmd) {
        synchronized (this.mService.mWindowMap) {
            this.mDump.findCmdLocked(this.mService.getDefaultDisplayContentLocked(), findCmd);
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        ColorDirectFindCmd findCmd;
        if (code != 10402) {
            return false;
        }
        data.enforceInterface("android.view.IWindowManager");
        if (data.readInt() != 0) {
            findCmd = (ColorDirectFindCmd) ColorDirectFindCmd.CREATOR.createFromParcel(data);
        } else {
            findCmd = null;
        }
        directFindCmd(findCmd);
        return true;
    }
}

package com.mediatek.mtklogger.c2klogger;

import android.util.Log;
import com.mediatek.mtklogger.c2klogger.EtsDnlder;

public class EtsDnlderThread extends Thread {
    private EtsDnlder mDnlder;

    public EtsDnlderThread(EtsDnlder dnlder) {
        this.mDnlder = dnlder;
    }

    private void close() {
        this.mDnlder.close();
    }

    public void run() {
        Log.i("via_ets", "Donwloader Thread started");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int retry = 10;
        while (retry > 0 && !this.mDnlder.loopback()) {
            retry--;
        }
        if (retry == 0) {
            this.mDnlder.getCallback().onProcess(EtsDnlder.DnldStatus.Error, 0, "Do loopback failed");
            close();
            return;
        }
        this.mDnlder.getCallback().onProcess(EtsDnlder.DnldStatus.Readying, 0, "Do loopback success");
        if (!this.mDnlder.jump2load(EtsDnlder.CBPMode.Boot, true)) {
            this.mDnlder.getCallback().onProcess(EtsDnlder.DnldStatus.Error, 0, "Reset device to boot failed");
            close();
            return;
        }
        this.mDnlder.getCallback().onProcess(EtsDnlder.DnldStatus.WaitingBoot, 0, "Reset device to boot succuss");
        for (String imgPath : this.mDnlder.getImgFiles()) {
            byte flashSection = this.mDnlder.getFlashSectionIndex(imgPath);
            if (flashSection >= 0 && flashSection < EtsDnlder.sSectionName.length && !this.mDnlder.downloadFlash(flashSection, imgPath)) {
                close();
                this.mDnlder.getCallback().onProcess(EtsDnlder.DnldStatus.Error, 0, "Download flash failed");
                return;
            }
        }
        this.mDnlder.jump2load(EtsDnlder.CBPMode.CP, false);
        this.mDnlder.getCallback().onProcess(EtsDnlder.DnldStatus.Finishied, 0, "Download flash finished");
    }
}

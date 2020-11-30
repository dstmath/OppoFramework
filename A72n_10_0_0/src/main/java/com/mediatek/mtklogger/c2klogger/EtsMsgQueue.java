package com.mediatek.mtklogger.c2klogger;

import android.util.Log;
import java.util.LinkedList;

public class EtsMsgQueue extends LinkedList<EtsMsg> {
    public boolean offer(EtsMsg o) {
        boolean ret = false;
        synchronized (this) {
            if (super.offer((EtsMsgQueue) o)) {
                notify();
                ret = true;
            }
        }
        return ret;
    }

    @Override // java.util.Queue, java.util.LinkedList, java.util.Deque
    public synchronized EtsMsg poll() {
        return (EtsMsg) super.poll();
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0007  */
    private EtsMsg findMsg(short id) {
        EtsMsg msg;
        while (!isEmpty()) {
            synchronized (this) {
                msg = poll();
            }
            if (id == -1 || id == msg.getId()) {
                return msg;
            }
            while (!isEmpty()) {
            }
        }
        return null;
    }

    public EtsMsg waitForMsg(short id, long timeout) {
        EtsMsg ret = null;
        long end = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < end) {
            try {
                if (isEmpty()) {
                    Log.v("via_ets", "cache is empty, wait for " + (timeout / 3) + " ms");
                    synchronized (this) {
                        wait(timeout / 3);
                    }
                }
                ret = findMsg(id);
                if (ret != null) {
                    break;
                }
                Log.v("via_ets", "time out, wait for 100 ms to continue");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (ret == null) {
            Log.w("via_ets", "can't get the special msg");
        }
        return ret;
    }
}

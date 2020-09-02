package com.color.antivirus.qihoo;

import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.color.antivirus.AntivirusLog;
import com.color.antivirus.qihoo.ITransmitPointService;
import com.color.antivirus.qihoo.IUidSetChangeCallbackInterface;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BehaviorBidSender {
    private static final boolean DEBUG = false;
    private static final int MAX_POINT_COUNT = 1000;
    private static final long POLL_TIMEOUT = 200;
    private static final String TAG = "BehaviorBidSender";
    private static final long TRY_GET_SERVICE_INTERNAL = 3000;
    private static volatile BehaviorBidSender sInstance = null;
    /* access modifiers changed from: private */
    public boolean bCurUidInMonitorSet;
    private long lastTimeTryInitService;
    /* access modifiers changed from: private */
    public Set<Integer> mMonitorUidSet;
    /* access modifiers changed from: private */
    public BlockingQueue<Integer> mPoints;
    /* access modifiers changed from: private */
    public ITransmitPointService mTransmitPointService;
    private IUidSetChangeCallbackInterface mUidChangeCallback;

    class SendId extends Thread {
        SendId() {
        }

        public void run() {
            Integer bid;
            int uid = Process.myUid();
            int pid = Process.myPid();
            while (BehaviorBidSender.this.bCurUidInMonitorSet) {
                try {
                    if (!(BehaviorBidSender.this.mPoints == null || (bid = (Integer) BehaviorBidSender.this.mPoints.poll(BehaviorBidSender.POLL_TIMEOUT, TimeUnit.MILLISECONDS)) == null || !BehaviorBidSender.this.checkAndInitService())) {
                        BehaviorBidSender.this.mTransmitPointService.pushId(uid, pid, bid.intValue());
                    }
                } catch (Throwable e) {
                    AntivirusLog.e(BehaviorBidSender.TAG, e.getMessage());
                }
            }
            if (BehaviorBidSender.this.mPoints != null) {
                BehaviorBidSender.this.mPoints.clear();
            }
        }
    }

    public static BehaviorBidSender getInstance() {
        if (sInstance == null) {
            synchronized (BehaviorBidSender.class) {
                if (sInstance == null) {
                    sInstance = new BehaviorBidSender();
                }
            }
        }
        return sInstance;
    }

    private BehaviorBidSender() {
        this.mTransmitPointService = null;
        this.mMonitorUidSet = null;
        this.lastTimeTryInitService = 0;
        this.bCurUidInMonitorSet = false;
        this.mUidChangeCallback = new IUidSetChangeCallbackInterface.Stub() {
            /* class com.color.antivirus.qihoo.BehaviorBidSender.AnonymousClass1 */

            public void onUidSetChanged(int[] uidArray) throws RemoteException {
                if (BehaviorBidSender.this.mMonitorUidSet == null) {
                    AntivirusLog.d(BehaviorBidSender.TAG, "in onUidSetChanged() MonitorUidSet null");
                    return;
                }
                BehaviorBidSender.this.mMonitorUidSet.clear();
                for (int itemUid : uidArray) {
                    BehaviorBidSender.this.mMonitorUidSet.add(Integer.valueOf(itemUid));
                }
                if (!BehaviorBidSender.this.mMonitorUidSet.contains(Integer.valueOf(Process.myUid()))) {
                    boolean unused = BehaviorBidSender.this.bCurUidInMonitorSet = false;
                } else if (!BehaviorBidSender.this.bCurUidInMonitorSet) {
                    boolean unused2 = BehaviorBidSender.this.bCurUidInMonitorSet = true;
                    new SendId().start();
                }
            }
        };
        this.mMonitorUidSet = new CopyOnWriteArraySet();
        this.mPoints = new LinkedBlockingQueue((int) MAX_POINT_COUNT);
    }

    public void init() {
        checkAndInitService();
    }

    /* access modifiers changed from: private */
    public boolean checkAndInitService() {
        if (this.mTransmitPointService == null) {
            try {
                long cur = System.currentTimeMillis();
                if (this.lastTimeTryInitService != 0 && Math.abs(cur - this.lastTimeTryInitService) < TRY_GET_SERVICE_INTERNAL) {
                    return false;
                }
                this.lastTimeTryInitService = cur;
                this.mTransmitPointService = ITransmitPointService.Stub.asInterface(ServiceManager.getService("transmit_point"));
                if (this.mTransmitPointService != null) {
                    this.mTransmitPointService.registerUidChangeCallback(this.mUidChangeCallback);
                } else {
                    AntivirusLog.d(TAG, "mTransmitPointService = null");
                }
            } catch (Exception e) {
                AntivirusLog.e(TAG, e.getMessage());
            }
        }
        if (this.mTransmitPointService != null) {
            return true;
        }
        return false;
    }

    public boolean checkUidInMonitorSet(int uid) {
        Set<Integer> set;
        if (!checkAndInitService() || (set = this.mMonitorUidSet) == null) {
            return false;
        }
        return set.contains(Integer.valueOf(uid));
    }

    public void pushId(int bid) {
        BlockingQueue<Integer> blockingQueue;
        if (bid != 0 && checkAndInitService() && this.bCurUidInMonitorSet && (blockingQueue = this.mPoints) != null) {
            try {
                if (!blockingQueue.offer(Integer.valueOf(bid))) {
                    this.mPoints.clear();
                    AntivirusLog.e(TAG, "Maybe something's wrong, fail to insert uid " + Process.myUid() + " pid " + Process.myPid() + " bid " + bid);
                }
            } catch (Exception e) {
                AntivirusLog.e(TAG, e.getMessage() + " uid " + Process.myUid() + " pid " + Process.myPid() + " bid " + bid);
            }
        }
    }

    public void notifyProcessDied(int uid, int pid) {
        if (checkAndInitService()) {
            try {
                this.mTransmitPointService.notifyProcessDied(uid, pid);
            } catch (Exception e) {
                AntivirusLog.e(TAG, e.getMessage() + " uid " + uid + " pid " + pid);
            }
        }
    }

    private long intToLong(int int1, int int2, int int3) {
        if (int1 >= 10000) {
            int1 -= 10000;
        }
        return ((((long) (((int1 & 16383) << 18) | (262143 & int2))) & 4294967295L) << 32) | (4294967295L & ((long) int3));
    }
}

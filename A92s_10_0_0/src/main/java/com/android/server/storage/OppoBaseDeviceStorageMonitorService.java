package com.android.server.storage;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageManager;
import com.android.server.SystemService;
import java.io.PrintWriter;

public abstract class OppoBaseDeviceStorageMonitorService extends SystemService {
    private static final long DEFAULT_CHECK_INTERVAL = 30000;
    private static final int MSG_CHECK = 1;
    protected static final int OPPO_LEVEL_FULL = 2;
    protected static final int OPPO_LEVEL_LOW = 1;
    protected static final int OPPO_LEVEL_NORMAL = 0;
    protected static final int OPPO_LEVEL_UNKNOWN = -1;
    protected Looper looperStorageMonitor;
    protected final Handler mHandler;
    protected volatile int mOppoForceLevle = -1;

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                OppoBaseDeviceStorageMonitorService.this.oppoCheckStorage();
                if (!OppoBaseDeviceStorageMonitorService.this.mHandler.hasMessages(1)) {
                    OppoBaseDeviceStorageMonitorService.this.mHandler.sendMessageDelayed(OppoBaseDeviceStorageMonitorService.this.mHandler.obtainMessage(1), 30000);
                }
            }
        }
    }

    public OppoBaseDeviceStorageMonitorService(Context context) {
        super(context);
        HandlerThread hd = new HandlerThread("OppoDeviceStorageMonitor");
        hd.start();
        this.looperStorageMonitor = hd.getLooper();
        this.mHandler = new WorkerHandler(this.looperStorageMonitor);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    /* access modifiers changed from: package-private */
    public void oppoCheckStorage() {
    }

    /* access modifiers changed from: package-private */
    public long getMemoryLowThresholdInternal() {
        return ((StorageManager) getContext().getSystemService(StorageManager.class)).getStorageLowBytes(Environment.getDataDirectory());
    }

    /* access modifiers changed from: package-private */
    public void oppoDumpImpl(PrintWriter pw) {
    }

    /* access modifiers changed from: protected */
    public void reScheduleCheck(long delay) {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, delay);
    }

    /* access modifiers changed from: package-private */
    public boolean oppoSimulationTest(String[] args, PrintWriter pw) {
        return false;
    }
}

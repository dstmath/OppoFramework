package com.mediatek.mtklogger.c2klogger;

import android.system.OsConstants;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import libcore.io.IoBridge;

public abstract class EtsDevice {
    public static final int TTY_DEV_CACHE_SIZE = 262144;
    private FileDescriptor mFileDescriptor;
    /* access modifiers changed from: private */
    public InputStream mInSteam;
    private EtsMsgQueue mMsgCache = new EtsMsgQueue();
    List<EtsMsg> mMsgTotalList = new ArrayList();
    private OutputStream mOutStream;
    protected String mPathDev = null;
    protected int mPerLogSize = 6291456;
    private ReadThread mReadThread = null;
    protected boolean mStopLogrecord = true;
    private File mTtyDev = null;
    protected boolean mWorking = false;
    private int mWriteTimeout = 0;

    public enum CBPStatus {
        Boot,
        CP,
        Unknown
    }

    public enum ErrorCode {
        DevUnvalid,
        Unknown
    }

    public EtsDevice() {
        Log.i("via_ets", "version: 1.0.0");
    }

    private class ReadThread extends Thread {
        private ReadThread() {
        }

        public void run() {
            super.run();
            Log.i("via_ets", "read thread start");
            byte[] bufRead = new byte[EtsDevice.this.mPerLogSize];
            byte[] buf = new byte[EtsDevice.TTY_DEV_CACHE_SIZE];
            int sizeTotal = 0;
            int logOutputCaculate = 0;
            while (true) {
                if (EtsDevice.this.mStopLogrecord) {
                    break;
                }
                try {
                    if (EtsDevice.this.mInSteam != null) {
                        int size = EtsDevice.this.mInSteam.read(buf);
                        logOutputCaculate++;
                        if (size <= 0) {
                            Log.w("via_ets", "read " + size + " byte");
                            if (EtsDevice.this.mWorking) {
                                if (size < 0 && !EtsDevice.this.onError(ErrorCode.DevUnvalid)) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            System.arraycopy(buf, 0, bufRead, sizeTotal, size);
                            sizeTotal += size;
                            if (size == 262143) {
                                Log.w("via_ets", "receive: " + size + " bytes");
                            } else if (logOutputCaculate > 100) {
                                Log.v("via_ets", "receive " + logOutputCaculate + " sizeTotal = " + sizeTotal + " bytes");
                                logOutputCaculate = 0;
                            } else if (logOutputCaculate == 1) {
                                Log.v("via_ets", "receive " + logOutputCaculate + " sizeTotal = " + sizeTotal + " bytes");
                            }
                            if (sizeTotal > EtsDevice.this.mPerLogSize - EtsDevice.TTY_DEV_CACHE_SIZE || EtsDevice.this.mStopLogrecord) {
                                byte[] bufWrite = new byte[EtsDevice.this.mPerLogSize];
                                System.arraycopy(bufRead, 0, bufWrite, 0, sizeTotal);
                                EtsDevice.this.writeToFile(bufWrite, sizeTotal);
                                sizeTotal = 0;
                                logOutputCaculate = 0;
                                try {
                                    bufRead = new byte[EtsDevice.this.mPerLogSize];
                                    if (EtsDevice.this.mStopLogrecord) {
                                        break;
                                    }
                                } catch (OutOfMemoryError e) {
                                    Log.e("via_ets", "Memory free bytes < " + EtsDevice.this.mPerLogSize);
                                    return;
                                }
                            }
                        }
                    } else {
                        Log.w("via_ets", "in_steam is null");
                        break;
                    }
                } catch (IOException e2) {
                    Log.e("via_ets", "Something error happend for IO read!");
                    e2.printStackTrace();
                    if (!EtsDevice.this.onError(ErrorCode.Unknown)) {
                        break;
                    }
                }
            }
            Log.i("via_ets", "read thread exit! _working ? " + EtsDevice.this.mStopLogrecord);
            EtsDevice.this.writeToFile(bufRead, sizeTotal);
        }
    }

    /* access modifiers changed from: private */
    public void writeToFile(byte[] buffer, int sizeTotal) {
        onDataReceived(buffer, sizeTotal);
    }

    /* access modifiers changed from: protected */
    public Boolean create(String pathDev) {
        Log.i("via_ets", "create device on " + pathDev);
        try {
            openDevice(pathDev);
            this.mWorking = true;
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            this.mTtyDev = null;
            return false;
        } catch (InvalidParameterException e2) {
            e2.printStackTrace();
            this.mTtyDev = null;
            return false;
        } catch (IOException e3) {
            e3.printStackTrace();
            this.mTtyDev = null;
            return false;
        }
    }

    public void startReadThread() {
        this.mReadThread = new ReadThread();
        this.mReadThread.start();
    }

    /* access modifiers changed from: protected */
    public void destroy() {
        Log.i("via_ets", "destroy device");
        this.mWorking = false;
        closeDevice();
    }

    /* access modifiers changed from: protected */
    public void openDevice(String pathDev) throws SecurityException, IOException, InvalidParameterException {
        if (this.mTtyDev != null) {
            Log.w("via_ets", "device already opened");
            return;
        }
        if (pathDev == null) {
            pathDev = this.mPathDev;
        }
        if (pathDev == null) {
            pathDev = C2KLogUtils.DEFAULT_CONIFG_DEVICEPATH;
            this.mPathDev = C2KLogUtils.DEFAULT_CONIFG_DEVICEPATH;
        }
        this.mTtyDev = new File(pathDev);
        this.mFileDescriptor = IoBridge.open(this.mTtyDev.getAbsolutePath(), OsConstants.O_RDWR | OsConstants.O_APPEND);
        this.mOutStream = new FileOutputStream(this.mFileDescriptor);
        this.mInSteam = new FileInputStream(this.mFileDescriptor);
        this.mPathDev = pathDev;
    }

    /* access modifiers changed from: protected */
    public void closeDevice() {
        Log.i("via_ets", "closeDevice()");
        if (isOpened().booleanValue()) {
            try {
                if (this.mOutStream != null) {
                    this.mOutStream.close();
                    this.mOutStream = null;
                }
                if (this.mInSteam != null) {
                    this.mInSteam.close();
                    this.mInSteam = null;
                }
                if (this.mFileDescriptor != null) {
                    IoBridge.closeAndSignalBlockedThreads(this.mFileDescriptor);
                    this.mFileDescriptor = null;
                }
                Log.i("via_ets", "in_steam & out_stream closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mTtyDev = null;
            Log.i("via_ets", "closeDevice() done");
        }
    }

    /* access modifiers changed from: protected */
    public Boolean isOpened() {
        return Boolean.valueOf(this.mTtyDev != null);
    }

    private void onDataReceived(byte[] buffer, int sizeTotal) {
        onEtsMsgReceived(buffer, EtsMsg.removeErrorBuffer(buffer, sizeTotal));
    }

    /* access modifiers changed from: protected */
    public void onEtsMsgReceived(EtsMsg msg) {
        this.mMsgCache.offer(msg);
    }

    /* access modifiers changed from: protected */
    public void onEtsMsgReceived(byte[] buffer, int size) {
    }

    /* access modifiers changed from: protected */
    public void onEtsMsgReceived(List<EtsMsg> list) {
    }

    /* access modifiers changed from: protected */
    public boolean onError(ErrorCode code) {
        Log.e("via_ets", "Error:" + code.toString());
        return false;
    }

    public void write(EtsMsg msg) throws NullPointerException, ConcurrentModificationException {
        try {
            byte[] buf = msg.getBuf();
            if (buf != null) {
                this.mOutStream.write(buf);
            }
            this.mWriteTimeout = 0;
        } catch (IOException e) {
            this.mWriteTimeout++;
            if (this.mWriteTimeout <= 5) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                write(msg);
                return;
            }
            Log.e("via_ets", "Send a msg, id = " + ((int) msg.getId()) + " is failed! Throw ConcurrentModificationException to stop write!");
            this.mWriteTimeout = 0;
            throw new ConcurrentModificationException();
        }
    }

    public boolean cmpBytes(byte[] data1, byte[] data2) {
        if (data1.length != data2.length) {
            return false;
        }
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    public EtsMsg waitForMsg(short id, long timeout) {
        return this.mMsgCache.waitForMsg(id, timeout);
    }

    /* access modifiers changed from: protected */
    public EtsMsg sendAndWait(EtsMsg msgReq, short id, long timeout) {
        write(msgReq);
        return waitForMsg(id, timeout);
    }

    public boolean loopback() {
        Log.v("via_ets", "do loopback");
        if (sendAndWait(new EtsMsg(0, new byte[]{(byte) ((int) (System.currentTimeMillis() & 255))}), 0, 2000) == null) {
            Log.e("via_ets", "failed, no response");
            return false;
        }
        Log.i("via_ets", "success");
        return true;
    }

    private CBPStatus getCbpStatus() {
        CBPStatus ret = CBPStatus.Unknown;
        EtsMsg msg = sendAndWait(new EtsMsg(288, null), 288, 1000);
        if (msg != null) {
            if (msg.getData()[0] == 0) {
                ret = CBPStatus.Boot;
            } else {
                ret = CBPStatus.CP;
            }
        }
        Log.v("via_ets", ret.name() + " mode");
        return ret;
    }

    public boolean waitForCP(long timeoutS) {
        for (int i = 0; ((long) i) < timeoutS; i++) {
            if (getCbpStatus() == CBPStatus.CP) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

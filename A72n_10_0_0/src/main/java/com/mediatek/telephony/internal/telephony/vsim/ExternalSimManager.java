package com.mediatek.telephony.internal.telephony.vsim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkProxyController;
import com.mediatek.internal.telephony.MtkSubscriptionController;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.uicc.MtkUiccController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mediatek.telephony.MtkServiceState;

public class ExternalSimManager {
    private static final int AUTO_RETRY_DURATION = 2000;
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final int EVENT_VSIM_INDICATION = 1;
    private static final int MAX_VSIM_UICC_CMD_LEN = 269;
    private static final byte NO_RESPONSE_STATUS_WORD_BYTE1 = 0;
    private static final byte NO_RESPONSE_STATUS_WORD_BYTE2 = 0;
    private static final int NO_RESPONSE_TIMEOUT_DURATION = 13000;
    private static final int OPPO_NO_RESPONSE_TIMEOUT_DURATION = 20000;
    private static final int PLATFORM_READY_CATEGORY_RADIO = 3;
    private static final int PLATFORM_READY_CATEGORY_SIM_SWITCH = 2;
    private static final int PLATFORM_READY_CATEGORY_SUB = 1;
    private static boolean PLUG_IN_AUTO_RETRY = true;
    private static final int PLUG_IN_AUTO_RETRY_TIMEOUT = 40000;
    private static final String PREFERED_AKA_SIM_SLOT = "vendor.gsm.prefered.aka.sim.slot";
    private static final String PREFERED_RSIM_SLOT = "vendor.gsm.prefered.rsim.slot";
    static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    private static final int RECOVERY_TO_REAL_SIM_TIMEOUT = 300000;
    private static final int SET_CAPABILITY_DONE = 2;
    private static final int SET_CAPABILITY_FAILED = 3;
    private static final int SET_CAPABILITY_NONE = 0;
    private static final int SET_CAPABILITY_ONGOING = 1;
    private static final int SIM_STATE_RETRY_DURATION = 20000;
    private static final int SOCKET_OPEN_RETRY_MILLIS = 4000;
    private static final String TAG = "ExternalSimMgr";
    private static final int TRY_RESET_MODEM_DURATION = 2000;
    private static ExternalSimManager sInstance = null;
    private static int sPreferedAkaSlot = -1;
    private static int sPreferedRsimSlot = -1;
    private CommandsInterface[] mCi = null;
    private Context mContext = null;
    private VsimEvenHandler mEventHandler = null;
    private VsimIndEventHandler mIndHandler = null;
    private final Object mLock = new Object();
    private final Object mLockForEventReq = new Object();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            Rlog.d(ExternalSimManager.TAG, "[Receiver]+");
            String action = intent.getAction();
            Rlog.d(ExternalSimManager.TAG, "Action: " + action);
            if (!action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE") || ExternalSimManager.this.mSetCapabilityDone != 1) {
                if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED") && ExternalSimManager.this.mSetCapabilityDone == 1) {
                    synchronized (ExternalSimManager.this.mLock) {
                        ExternalSimManager.this.mSetCapabilityDone = 3;
                        ExternalSimManager.this.sendCapabilityDoneEvent();
                    }
                    Rlog.d(ExternalSimManager.TAG, "SET_CAPABILITY_FAILED, notify all");
                }
            } else if (ExternalSimManager.this.mEventHandler.getVsimSlotId(2) == RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()) {
                synchronized (ExternalSimManager.this.mLock) {
                    ExternalSimManager.this.mSetCapabilityDone = 2;
                    ExternalSimManager.this.sendCapabilityDoneEvent();
                }
                Rlog.d(ExternalSimManager.TAG, "SET_CAPABILITY_DONE, notify all");
            }
            Rlog.d(ExternalSimManager.TAG, "[Receiver]-");
        }
    };
    private Timer mRecoveryTimer = null;
    private int mSetCapabilityDone = 0;
    private int mUserMainPhoneId = -1;
    private boolean mUserRadioOn = false;

    public ExternalSimManager() {
        Rlog.d(TAG, "construtor 0 parameter is called - done");
    }

    private ExternalSimManager(Context context, CommandsInterface[] ci) {
        Rlog.d(TAG, "construtor 1 parameter is called - start");
        initVsimConfiguration();
        startRecoveryTimer();
        this.mContext = context;
        this.mCi = ci;
        new Thread() {
            /* class com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager.AnonymousClass1 */

            public void run() {
                Looper.prepare();
                ExternalSimManager externalSimManager = ExternalSimManager.this;
                externalSimManager.mEventHandler = new VsimEvenHandler();
                ExternalSimManager externalSimManager2 = ExternalSimManager.this;
                externalSimManager2.mIndHandler = new VsimIndEventHandler();
                for (int i = 0; i < ExternalSimManager.this.mCi.length; i++) {
                    ExternalSimManager.this.mCi[i].registerForVsimIndication(ExternalSimManager.this.mIndHandler, 1, new Integer(i));
                }
                Looper.loop();
            }
        }.start();
        new Thread() {
            /* class com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager.AnonymousClass2 */

            public void run() {
                while (true) {
                    if (ExternalSimManager.this.mEventHandler == null || ExternalSimManager.this.mIndHandler == null) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ExternalSimManager.this.sendExternalSimConnectedEvent(0);
                        new ServerTask().listenConnection();
                        return;
                    }
                }
            }
        }.start();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        intentFilter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        context.registerReceiver(this.mReceiver, intentFilter);
        Rlog.d(TAG, "construtor is called - end");
    }

    public static ExternalSimManager make(Context context, CommandsInterface[] ci) {
        if (sInstance == null) {
            sInstance = new ExternalSimManager(context, ci);
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public static String truncateString(String original) {
        if (original == null || original.length() < 6) {
            return original;
        }
        return original.substring(0, 2) + "***" + original.substring(original.length() - 4);
    }

    private static IMtkTelephonyEx getITelephonyEx() {
        return IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCapabilityDoneEvent() {
        VsimEvent event = new VsimEvent(0, ExternalSimConstants.MSG_ID_CAPABILITY_SWITCH_DONE, -1);
        Message msg = new Message();
        msg.obj = event;
        this.mEventHandler.sendMessage(msg);
        Rlog.d(TAG, "sendCapabilityDoneEvent....");
    }

    public boolean initializeService(byte[] userData) {
        Rlog.d(TAG, "initializeService() - start");
        if (SystemProperties.getInt("ro.vendor.mtk_external_sim_support", 0) == 0) {
            Rlog.d(TAG, "initializeService() - mtk_external_sim_support didn't support");
            return false;
        }
        SystemProperties.set("ctl.start", "osi");
        Rlog.d(TAG, "initializeService() - end");
        return true;
    }

    public boolean finalizeService(byte[] userData) {
        Rlog.d(TAG, "finalizeService() - start");
        if (SystemProperties.getInt("ro.vendor.mtk_external_sim_support", 0) == 0) {
            Rlog.d(TAG, "initializeService() - mtk_external_sim_support didn't support");
            return false;
        }
        SystemProperties.set("ctl.stop", "osi");
        Rlog.d(TAG, "finalizeService() - end");
        return true;
    }

    public void setVsimProperty(int phoneId, String property, String value) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(phoneId, property, value);
        TelephonyManager.getDefault();
        TelephonyManager.getTelephonyProperty(phoneId, property, "");
        do {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TelephonyManager.getDefault();
        } while (!TelephonyManager.getTelephonyProperty(phoneId, property, "").equals(value));
    }

    public void initVsimConfiguration() {
        sPreferedRsimSlot = SystemProperties.getInt(PREFERED_RSIM_SLOT, -1);
        sPreferedAkaSlot = SystemProperties.getInt(PREFERED_AKA_SIM_SLOT, -1);
    }

    public static boolean isNonDsdaRemoteSimSupport() {
        return SystemProperties.getInt("ro.vendor.mtk_non_dsda_rsim_support", 0) == 1;
    }

    public static boolean isSupportVsimHotPlugOut() {
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            TelephonyManager.getDefault();
            String capability = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.modem.vsim.capability", "0");
            if (capability != null && capability.length() > 0 && !"0".equals(capability)) {
                try {
                    if ((Integer.parseInt(capability) & 2) > 0) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Rlog.d(TAG, e.toString());
                } catch (Exception e2) {
                    Rlog.d(TAG, e2.toString());
                }
            }
        }
        return false;
    }

    public static int getPreferedRsimSlot() {
        return sPreferedRsimSlot;
    }

    public static boolean isAnyVsimEnabled() {
        if (SystemProperties.getInt("ro.vendor.mtk_external_sim_only_slots", 0) != 0) {
            return true;
        }
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            TelephonyManager.getDefault();
            String enable = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.enabled", "0");
            TelephonyManager.getDefault();
            String inserted = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.inserted", "0");
            if (!(enable == null || enable.length() <= 0 || "0".equals(enable) || inserted == null || inserted.length() <= 0 || "0".equals(inserted))) {
                return true;
            }
        }
        return false;
    }

    public void startRecoveryTimer() {
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            TelephonyManager.getDefault();
            String persist = TelephonyManager.getTelephonyProperty(i, "persist.vendor.radio.external.sim", "0");
            if (persist != null && persist.length() > 0 && String.valueOf(2).equals(persist)) {
                if (this.mRecoveryTimer == null) {
                    this.mRecoveryTimer = new Timer();
                    int timout = RECOVERY_TO_REAL_SIM_TIMEOUT;
                    TelephonyManager.getDefault();
                    String userTimeout = TelephonyManager.getTelephonyProperty(i, "persist.vendor.radio.vsim.timeout", String.valueOf((int) RECOVERY_TO_REAL_SIM_TIMEOUT));
                    try {
                        if (Integer.parseInt(userTimeout) > 0) {
                            timout = Integer.parseInt(userTimeout) * 1000;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    this.mRecoveryTimer.schedule(new RecoveryRealSimTask(), (long) timout);
                    Rlog.i(TAG, "startRecoveryTimer: " + timout + " ms.");
                    return;
                } else {
                    return;
                }
            }
        }
        Rlog.i(TAG, "No need to startRecoveryTimer since didn't set persist VSIM.");
    }

    public void stopRecoveryTimer() {
        Timer timer = this.mRecoveryTimer;
        if (timer != null) {
            timer.cancel();
            this.mRecoveryTimer.purge();
            this.mRecoveryTimer = null;
            Rlog.i(TAG, "stopRecoveryTimer.");
        }
    }

    public class RecoveryRealSimTask extends TimerTask {
        public RecoveryRealSimTask() {
        }

        public void run() {
            for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
                TelephonyManager.getDefault();
                String enabled = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.enabled", "0");
                if (enabled != null && enabled.length() > 0 && !"0".equals(enabled)) {
                    Rlog.i(ExternalSimManager.TAG, "Auto recovery time out, disable VSIM...");
                    ExternalSimManager.this.sendDisableEvent(1 << i, 1);
                }
            }
            if (!ExternalSimManager.isSupportVsimHotPlugOut()) {
                int i2 = 0;
                while (i2 < TelephonyManager.getDefault().getSimCount()) {
                    TelephonyManager.getDefault();
                    String enabled2 = TelephonyManager.getTelephonyProperty(i2, "vendor.gsm.external.sim.enabled", "0");
                    if (enabled2 == null || enabled2.length() <= 0 || "0".equals(enabled2)) {
                        i2++;
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ExternalSimManager.this.disableAllVsimWithResetModem();
            }
        }
    }

    public void disableAllVsimWithResetModem() {
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            waitRildSetDisabledProperty(i);
        }
        VsimEvenHandler vsimEvenHandler = this.mEventHandler;
        if (vsimEvenHandler != null) {
            vsimEvenHandler.retryIfRadioUnavailable(null);
        }
        RadioManager.getInstance().setSilentRebootPropertyForAllModem("1");
        UiccController.getInstance().resetRadioForVsim();
        Rlog.i(TAG, "disableAllVsimWithResetModem...");
    }

    public void sendDisableEvent(int slotId, int simType) {
        VsimEvent disableEvent = new VsimEvent(0, 3, slotId);
        disableEvent.putInt(2);
        disableEvent.putInt(simType);
        Message msg = new Message();
        msg.obj = disableEvent;
        this.mEventHandler.sendMessage(msg);
        Rlog.i(TAG, "sendDisableEvent[" + slotId + "]....");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendExternalSimConnectedEvent(int connected) {
        VsimEvent connectedEvent = new VsimEvent(0, 3, 0);
        connectedEvent.putInt(ExternalSimConstants.EVENT_TYPE_EXTERNAL_SIM_CONNECTED);
        connectedEvent.putInt(connected);
        Message msg = new Message();
        msg.obj = connectedEvent;
        this.mEventHandler.sendMessage(msg);
        Rlog.i(TAG, "sendExternalSimConnectedEvent connected=" + connected);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void waitRildSetDisabledProperty(int slotId) {
        TelephonyManager.getDefault();
        String enabled = TelephonyManager.getTelephonyProperty(slotId, "vendor.gsm.external.sim.enabled", "0");
        while (enabled != null && enabled.length() > 0 && !"0".equals(enabled)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TelephonyManager.getDefault();
            enabled = TelephonyManager.getTelephonyProperty(slotId, "vendor.gsm.external.sim.enabled", "0");
        }
    }

    public class ServerTask {
        public static final String HOST_NAME = "vsim-adaptor";
        private VsimIoThread ioThread = null;

        public ServerTask() {
        }

        public void listenConnection() {
            Rlog.d(ExternalSimManager.TAG, "listenConnection() - start");
            LocalServerSocket serverSocket = null;
            ExecutorService threadExecutor = Executors.newCachedThreadPool();
            try {
                while (true) {
                    LocalSocket socket = new LocalServerSocket(HOST_NAME).accept();
                    Rlog.i(ExternalSimManager.TAG, "There is a client is accpted: " + socket.toString());
                    ExternalSimManager.this.stopRecoveryTimer();
                    ExternalSimManager.this.sendExternalSimConnectedEvent(1);
                    threadExecutor.execute(new ConnectionHandler(socket, ExternalSimManager.this.mEventHandler));
                }
            } catch (IOException e) {
                Rlog.w(ExternalSimManager.TAG, "listenConnection catch IOException");
                e.printStackTrace();
                Rlog.d(ExternalSimManager.TAG, "listenConnection finally!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (0 != 0) {
                    serverSocket.close();
                }
            } catch (Exception e2) {
                Rlog.w(ExternalSimManager.TAG, "listenConnection catch Exception");
                e2.printStackTrace();
                Rlog.d(ExternalSimManager.TAG, "listenConnection finally!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (0 != 0) {
                    try {
                        serverSocket.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                Rlog.d(ExternalSimManager.TAG, "listenConnection finally!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (0 != 0) {
                    try {
                        serverSocket.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
            Rlog.d(ExternalSimManager.TAG, "listenConnection() - end");
        }
    }

    public class ConnectionHandler implements Runnable {
        public static final String RILD_SERVER_NAME = "rild-vsim";
        private VsimEvenHandler mEventHandler;
        private LocalSocket mSocket;

        public ConnectionHandler(LocalSocket clientSocket, VsimEvenHandler eventHandler) {
            this.mSocket = clientSocket;
            this.mEventHandler = eventHandler;
        }

        public void run() {
            Rlog.i(ExternalSimManager.TAG, "New connection: " + this.mSocket.toString());
            try {
                VsimIoThread ioThread = new VsimIoThread(ServerTask.HOST_NAME, this.mSocket.getInputStream(), this.mSocket.getOutputStream(), this.mEventHandler);
                this.mEventHandler.setDataStream(ioThread, null);
                ioThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class VsimEvent {
        public static final int DEFAULT_MAX_DATA_LENGTH = 512;
        private byte[] mData;
        private int mDataLen;
        private int mEventMaxDataLen;
        private int mMessageId;
        private int mReadOffset;
        private int mSlotId;
        private int mTransactionId;

        public VsimEvent(int transactionId, int messageId) {
            this(transactionId, messageId, 0);
        }

        public VsimEvent(int transactionId, int messageId, int slotId) {
            this(transactionId, messageId, 512, slotId);
        }

        public VsimEvent(int transactionId, int messageId, int length, int slotId) {
            this.mEventMaxDataLen = 512;
            this.mTransactionId = transactionId;
            this.mMessageId = messageId;
            this.mSlotId = slotId;
            this.mEventMaxDataLen = length;
            this.mData = new byte[this.mEventMaxDataLen];
            this.mDataLen = 0;
            this.mReadOffset = 0;
        }

        public void resetOffset() {
            synchronized (this) {
                this.mReadOffset = 0;
            }
        }

        public int putInt(int value) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - 4) {
                    return -1;
                }
                for (int i = 0; i < 4; i++) {
                    this.mData[this.mDataLen] = (byte) ((value >> (i * 8)) & 255);
                    this.mDataLen++;
                }
                return 0;
            }
        }

        public int putShort(int value) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - 2) {
                    return -1;
                }
                for (int i = 0; i < 2; i++) {
                    this.mData[this.mDataLen] = (byte) ((value >> (i * 8)) & 255);
                    this.mDataLen++;
                }
                return 0;
            }
        }

        public int putByte(int value) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - 1) {
                    return -1;
                }
                this.mData[this.mDataLen] = (byte) (value & 255);
                this.mDataLen++;
                return 0;
            }
        }

        public int putString(String str, int len) {
            synchronized (this) {
                if (this.mDataLen > this.mEventMaxDataLen - len) {
                    return -1;
                }
                byte[] s = str.getBytes();
                if (len < str.length()) {
                    System.arraycopy(s, 0, this.mData, this.mDataLen, len);
                    this.mDataLen += len;
                } else {
                    int remain = len - str.length();
                    System.arraycopy(s, 0, this.mData, this.mDataLen, str.length());
                    this.mDataLen += str.length();
                    for (int i = 0; i < remain; i++) {
                        this.mData[this.mDataLen] = 0;
                        this.mDataLen++;
                    }
                }
                return 0;
            }
        }

        public int putBytes(byte[] value) {
            synchronized (this) {
                int len = value.length;
                if (len > this.mEventMaxDataLen) {
                    return -1;
                }
                System.arraycopy(value, 0, this.mData, this.mDataLen, len);
                this.mDataLen += len;
                return 0;
            }
        }

        public byte[] getData() {
            byte[] tempData;
            synchronized (this) {
                tempData = new byte[this.mDataLen];
                System.arraycopy(this.mData, 0, tempData, 0, this.mDataLen);
            }
            return tempData;
        }

        public int getDataLen() {
            int i;
            synchronized (this) {
                i = this.mDataLen;
            }
            return i;
        }

        public byte[] getDataByReadOffest() {
            byte[] tempData;
            synchronized (this) {
                tempData = new byte[(this.mDataLen - this.mReadOffset)];
                System.arraycopy(this.mData, this.mReadOffset, tempData, 0, this.mDataLen - this.mReadOffset);
            }
            return tempData;
        }

        public int getMessageId() {
            return this.mMessageId;
        }

        public int getSlotBitMask() {
            return this.mSlotId;
        }

        public int getFirstSlotId() {
            int simCount = TelephonyManager.getDefault().getSimCount();
            if (getSlotBitMask() > (1 << (simCount - 1))) {
                Rlog.w(ExternalSimManager.TAG, "getFirstSlotId, invalid slot id: " + getSlotBitMask());
                return 0;
            }
            for (int i = 0; i < simCount; i++) {
                if ((getSlotBitMask() & (1 << i)) != 0) {
                    return i;
                }
            }
            Rlog.w(ExternalSimManager.TAG, "getFirstSlotId, invalid slot id: " + getSlotBitMask());
            return 0;
        }

        public int getTransactionId() {
            return this.mTransactionId;
        }

        public int getInt() {
            int ret = 0;
            synchronized (this) {
                if (this.mData.length >= 4) {
                    ret = ((this.mData[this.mReadOffset + 3] & PplMessageManager.Type.INVALID) << 24) | ((this.mData[this.mReadOffset + 2] & PplMessageManager.Type.INVALID) << 16) | ((this.mData[this.mReadOffset + 1] & PplMessageManager.Type.INVALID) << 8) | (this.mData[this.mReadOffset] & PplMessageManager.Type.INVALID);
                    this.mReadOffset += 4;
                }
            }
            return ret;
        }

        public int getShort() {
            int ret;
            synchronized (this) {
                ret = ((this.mData[this.mReadOffset + 1] & PplMessageManager.Type.INVALID) << 8) | (this.mData[this.mReadOffset] & PplMessageManager.Type.INVALID);
                this.mReadOffset += 2;
            }
            return ret;
        }

        public int getByte() {
            int ret;
            synchronized (this) {
                ret = this.mData[this.mReadOffset] & PplMessageManager.Type.INVALID;
                this.mReadOffset++;
            }
            return ret;
        }

        public byte[] getBytes(int length) {
            synchronized (this) {
                if (length > this.mDataLen - this.mReadOffset) {
                    return null;
                }
                byte[] ret = new byte[length];
                for (int i = 0; i < length; i++) {
                    ret[i] = this.mData[this.mReadOffset];
                    this.mReadOffset++;
                }
                return ret;
            }
        }

        public String getString(int len) {
            byte[] buf = new byte[len];
            synchronized (this) {
                System.arraycopy(this.mData, this.mReadOffset, buf, 0, len);
                this.mReadOffset += len;
            }
            return new String(buf).trim();
        }

        public String toString() {
            return new String("dumpEvent: transaction_id: " + getTransactionId() + ", message_id:" + getMessageId() + ", slot_id:" + getSlotBitMask() + ", data_len:" + getDataLen() + ", event:" + ExternalSimManager.truncateString(IccUtils.bytesToHexString(getData())));
        }
    }

    /* access modifiers changed from: package-private */
    public class VsimIoThread extends Thread {
        private static final int MAX_DATA_LENGTH = 20480;
        private VsimEvenHandler mEventHandler = null;
        private DataInputStream mInput = null;
        private boolean mIsContinue = true;
        private String mName = "";
        private DataOutputStream mOutput = null;
        private String mServerName = "";
        private byte[] readBuffer = null;

        public VsimIoThread(String name, InputStream inputStream, OutputStream outputStream, VsimEvenHandler eventHandler) {
            this.mName = name;
            this.mInput = new DataInputStream(inputStream);
            this.mOutput = new DataOutputStream(outputStream);
            this.mEventHandler = eventHandler;
            logd("VsimIoThread constructor is called.");
        }

        public void terminate() {
            logd("VsimIoThread terminate.");
            this.mIsContinue = false;
        }

        public void run() {
            logd("VsimIoThread running.");
            while (this.mIsContinue) {
                try {
                    VsimEvent event = readEvent();
                    if (event != null) {
                        Message msg = new Message();
                        msg.obj = event;
                        this.mEventHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    logw("VsimIoThread IOException.");
                    e.printStackTrace();
                    if (this.mServerName.equals("")) {
                        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
                            TelephonyManager.getDefault();
                            String enabled = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.enabled", "0");
                            TelephonyManager.getDefault();
                            String insert = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.inserted", "0");
                            if (enabled != null && enabled.length() > 0 && !"0".equals(enabled)) {
                                if (insert == null || insert.length() <= 0) {
                                    insert = "0";
                                }
                                ExternalSimManager.this.sendDisableEvent(1 << i, Integer.valueOf(insert).intValue());
                                logi("Disable VSIM and reset modem since socket disconnected.");
                            }
                        }
                        ExternalSimManager.this.sendExternalSimConnectedEvent(0);
                        logw("Socket disconnected and vsim is disabled.");
                        terminate();
                    }
                } catch (Exception e2) {
                    logw("VsimIoThread Exception.");
                    e2.printStackTrace();
                }
            }
        }

        private void writeBytes(byte[] value, int len) throws IOException {
            this.mOutput.write(value, 0, len);
        }

        private void writeInt(int value) throws IOException {
            for (int i = 0; i < 4; i++) {
                this.mOutput.write((value >> (i * 8)) & 255);
            }
        }

        public int writeEvent(VsimEvent event) {
            return writeEvent(event, false);
        }

        public int writeEvent(VsimEvent event, boolean isBigEndian) {
            logd("writeEvent Enter, isBigEndian:" + isBigEndian);
            int ret = -1;
            try {
                synchronized (this) {
                    if (this.mOutput != null) {
                        dumpEvent(event);
                        writeInt(event.getTransactionId());
                        writeInt(event.getMessageId());
                        writeInt(event.getSlotBitMask());
                        writeInt(event.getDataLen());
                        writeBytes(event.getData(), event.getDataLen());
                        this.mOutput.flush();
                        ret = 0;
                    } else {
                        loge("mOut is null, socket is not setup");
                    }
                }
                return ret;
            } catch (Exception e) {
                loge("writeEvent Exception");
                e.printStackTrace();
                return -1;
            }
        }

        private int readInt() throws IOException {
            byte[] tempBuf = new byte[8];
            if (this.mInput.read(tempBuf, 0, 4) >= 0) {
                return ((tempBuf[1] & PplMessageManager.Type.INVALID) << 8) | (tempBuf[3] << 24) | ((tempBuf[2] & PplMessageManager.Type.INVALID) << 16) | (tempBuf[0] & PplMessageManager.Type.INVALID);
            }
            loge("readInt(), fail to read and throw exception");
            throw new IOException("fail to read");
        }

        private VsimEvent readEvent() throws IOException {
            logd("readEvent Enter");
            int transaction_id = readInt();
            int msg_id = readInt();
            int slot_id = readInt();
            int data_len = readInt();
            logd("readEvent transaction_id: " + transaction_id + ", msgId: " + msg_id + ", slot_id: " + slot_id + ", len: " + data_len);
            if (data_len <= ExternalSimManager.MAX_VSIM_UICC_CMD_LEN) {
                this.readBuffer = new byte[data_len];
                int offset = 0;
                int remaining = data_len;
                do {
                    int countRead = this.mInput.read(this.readBuffer, offset, remaining);
                    if (countRead >= 0) {
                        offset += countRead;
                        remaining -= countRead;
                    } else {
                        loge("readEvent(), fail to read and throw exception");
                        throw new IOException("fail to read");
                    }
                } while (remaining > 0);
                VsimEvent event = new VsimEvent(transaction_id, msg_id, data_len, slot_id);
                event.putBytes(this.readBuffer);
                dumpEvent(event);
                return event;
            }
            loge("readEvent(), data_len large than 269");
            throw new IOException("unreasonable data length");
        }

        private void dumpEvent(VsimEvent event) {
            if (ExternalSimManager.ENG) {
                logd("dumpEvent: transaction_id: " + event.getTransactionId() + ", message_id:" + event.getMessageId() + ", slot_id:" + event.getSlotBitMask() + ", data_len:" + event.getDataLen() + ", event:" + ExternalSimManager.truncateString(IccUtils.bytesToHexString(event.getData())));
                return;
            }
            logd("dumpEvent: transaction_id: " + event.getTransactionId() + ", message_id:" + event.getMessageId() + ", slot_id:" + event.getSlotBitMask() + ", data_len:" + event.getDataLen());
        }

        private void logd(String s) {
            Rlog.d(ExternalSimManager.TAG, "[" + this.mName + "] " + s);
        }

        private void logi(String s) {
            Rlog.i(ExternalSimManager.TAG, "[" + this.mName + "] " + s);
        }

        private void logw(String s) {
            Rlog.w(ExternalSimManager.TAG, "[" + this.mName + "] " + s);
        }

        private void loge(String s) {
            Rlog.e(ExternalSimManager.TAG, "[" + this.mName + "] " + s);
        }
    }

    public class VsimIndEventHandler extends Handler {
        public VsimIndEventHandler() {
        }

        /* access modifiers changed from: protected */
        public Integer getCiIndex(Message msg) {
            Integer index = new Integer(0);
            if (msg == null) {
                return index;
            }
            if (msg.obj != null && (msg.obj instanceof Integer)) {
                return (Integer) msg.obj;
            }
            if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
                return index;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
                return index;
            }
            return (Integer) ar.userObj;
        }

        public void handleMessage(Message msg) {
            Integer index = getCiIndex(msg);
            if (index.intValue() < 0 || index.intValue() >= ExternalSimManager.this.mCi.length) {
                Rlog.e(ExternalSimManager.TAG, "Invalid index : " + index + " received with event " + msg.what);
                return;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (msg.what != 1) {
                Rlog.e(ExternalSimManager.TAG, " Unknown Event " + msg.what);
                return;
            }
            if (ExternalSimManager.ENG) {
                Rlog.d(ExternalSimManager.TAG, "Received EVENT_VSIM_INDICATION...");
            }
            VsimEvent indicationEvent = (VsimEvent) ar.result;
            dumpEvent(indicationEvent);
            Message vsimMsg = new Message();
            vsimMsg.obj = indicationEvent;
            ExternalSimManager.this.mEventHandler.sendMessage(vsimMsg);
        }

        private void dumpEvent(VsimEvent event) {
            if (ExternalSimManager.ENG) {
                Rlog.d(ExternalSimManager.TAG, "dumpEvent: transaction_id: " + event.getTransactionId() + ", message_id:" + event.getMessageId() + ", slot_id:" + event.getSlotBitMask() + ", data_len:" + event.getDataLen() + ", event:" + ExternalSimManager.truncateString(IccUtils.bytesToHexString(event.getData())));
                return;
            }
            Rlog.d(ExternalSimManager.TAG, "dumpEvent: transaction_id: " + event.getTransactionId() + ", message_id:" + event.getMessageId() + ", slot_id:" + event.getSlotBitMask() + ", data_len:" + event.getDataLen());
        }
    }

    public class VsimEvenHandler extends Handler {
        private eventHandlerTread[] mEventHandlingThread = null;
        private boolean mHasNotifyEnableEvnetToModem = false;
        private boolean mIsAkaOccupyRf = false;
        private boolean[] mIsMdWaitingResponse = null;
        private boolean mIsSwitchRfSuccessful = false;
        private boolean[] mIsWaitingAuthRsp = null;
        private long mLastDisableEventTime = 0;
        private int[] mNoResponseTimeOut = null;
        private Timer[] mNoResponseTimer = null;
        private Runnable mTryResetModemRunnable = new Runnable() {
            /* class com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager.VsimEvenHandler.AnonymousClass1 */

            public void run() {
                MtkUiccController uiccCtrl = UiccController.getInstance();
                if (uiccCtrl.isAllRadioAvailable()) {
                    RadioManager.getInstance().setSilentRebootPropertyForAllModem("1");
                    uiccCtrl.resetRadioForVsim();
                    Rlog.i(ExternalSimManager.TAG, "mTryResetModemRunnable reset modem done.");
                    return;
                }
                VsimEvenHandler vsimEvenHandler = VsimEvenHandler.this;
                vsimEvenHandler.postDelayed(vsimEvenHandler.mTryResetModemRunnable, 2000);
            }
        };
        private VsimIoThread mVsimAdaptorIo = null;
        private VsimIoThread mVsimRilIo = null;
        private VsimEvent[] mWaitingEvent = null;

        public VsimEvenHandler() {
            int simCount = TelephonyManager.getDefault().getSimCount();
            this.mIsMdWaitingResponse = new boolean[simCount];
            this.mNoResponseTimer = new Timer[simCount];
            this.mWaitingEvent = new VsimEvent[simCount];
            this.mIsWaitingAuthRsp = new boolean[simCount];
            this.mNoResponseTimeOut = new int[simCount];
            this.mEventHandlingThread = new eventHandlerTread[simCount];
            for (int i = 0; i < simCount; i++) {
                this.mIsMdWaitingResponse[i] = false;
                this.mNoResponseTimer[i] = null;
                this.mWaitingEvent[i] = null;
                this.mIsWaitingAuthRsp[i] = false;
                this.mNoResponseTimeOut[i] = ExternalSimManager.NO_RESPONSE_TIMEOUT_DURATION;
                this.mEventHandlingThread[i] = null;
            }
        }

        public void handleMessage(Message msg) {
            VsimEvent event;
            if (msg.obj instanceof AsyncResult) {
                event = (VsimEvent) ((AsyncResult) msg.obj).userObj;
            } else {
                event = (VsimEvent) msg.obj;
            }
            int slotId = event.getFirstSlotId();
            if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
                new eventHandlerTread((VsimEvent) msg.obj).start();
                return;
            }
            while (true) {
                eventHandlerTread[] eventhandlertreadArr = this.mEventHandlingThread;
                if (eventhandlertreadArr[slotId] == null || !eventhandlertreadArr[slotId].isWaiting()) {
                    this.mEventHandlingThread[slotId] = new eventHandlerTread(event);
                    this.mEventHandlingThread[slotId].start();
                } else {
                    Rlog.d(ExternalSimManager.TAG, "handleMessage[" + slotId + "] thread running, delay 100 ms...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.mEventHandlingThread[slotId] = new eventHandlerTread(event);
            this.mEventHandlingThread[slotId].start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDataStream(VsimIoThread vsimAdpatorIo, VsimIoThread vsimRilIo) {
            this.mVsimAdaptorIo = vsimAdpatorIo;
            this.mVsimRilIo = vsimRilIo;
            Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler setDataStream done.");
        }

        private int getRspMessageId(int requestMsgId) {
            if (requestMsgId == 1) {
                return ExternalSimConstants.MSG_ID_INITIALIZATION_RESPONSE;
            }
            if (requestMsgId == 2) {
                return ExternalSimConstants.MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE;
            }
            if (requestMsgId == 3) {
                return ExternalSimConstants.MSG_ID_EVENT_RESPONSE;
            }
            if (requestMsgId == 7) {
                return ExternalSimConstants.MSG_ID_GET_SERVICE_STATE_RESPONSE;
            }
            if (requestMsgId == 8) {
                return ExternalSimConstants.MSG_ID_FINALIZATION_RESPONSE;
            }
            if (requestMsgId == 2001) {
                return 5;
            }
            switch (requestMsgId) {
                case ExternalSimConstants.MSG_ID_UICC_RESET_REQUEST /* 1004 */:
                    return 4;
                case ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST /* 1005 */:
                    return 5;
                case ExternalSimConstants.MSG_ID_UICC_POWER_DOWN_REQUEST /* 1006 */:
                    return 6;
                default:
                    Rlog.d(ExternalSimManager.TAG, "getRspMessageId: " + requestMsgId + "no support.");
                    return -1;
            }
        }

        public class eventHandlerTread extends Thread {
            boolean isWaiting = true;
            VsimEvent mEvent = null;

            public eventHandlerTread(VsimEvent event) {
                this.mEvent = event;
            }

            public boolean isWaiting() {
                return this.isWaiting;
            }

            public void setWaiting(boolean waiting) {
                this.isWaiting = waiting;
            }

            public void run() {
                Rlog.d(ExternalSimManager.TAG, "eventHandlerTread[ " + this.mEvent.getFirstSlotId() + "]: run...");
                VsimEvenHandler.this.dispatchCallback(this.mEvent);
                this.isWaiting = false;
            }
        }

        public class TimeOutTimerTask extends TimerTask {
            int mSlotId = 0;

            public TimeOutTimerTask(int slotId) {
                this.mSlotId = slotId;
            }

            public void run() {
                synchronized (ExternalSimManager.this.mLock) {
                    if (VsimEvenHandler.this.mWaitingEvent[this.mSlotId] != null) {
                        VsimEvenHandler.this.sendNoResponseError(VsimEvenHandler.this.mWaitingEvent[this.mSlotId]);
                    }
                    Rlog.i(ExternalSimManager.TAG, "TimeOutTimerTask[" + this.mSlotId + "] time out and send response to modem directly.");
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendNoResponseError(VsimEvent event) {
            if (this.mIsWaitingAuthRsp[event.getFirstSlotId()]) {
                this.mIsWaitingAuthRsp[event.getFirstSlotId()] = false;
                sendRsimAuthProgressEvent(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP);
            }
            if (getMdWaitingFlag(event.getFirstSlotId())) {
                setMdWaitingFlag(false, event.getFirstSlotId());
                VsimEvent response = new VsimEvent(event.getTransactionId(), getRspMessageId(event.getMessageId()), event.getSlotBitMask());
                response.putInt(-1);
                response.putInt(2);
                response.putByte(0);
                response.putByte(0);
                ExternalSimManager.this.mCi[event.getFirstSlotId()].sendVsimOperation(response.getTransactionId(), response.getMessageId(), response.getInt(), response.getInt(), response.getDataByReadOffest(), null);
            }
        }

        private void sendVsimNotification(int slotId, int transactionId, int eventId, int simType, Message message) {
            boolean result = ExternalSimManager.this.mCi[slotId].sendVsimNotification(transactionId, eventId, simType, message);
            Rlog.d(ExternalSimManager.TAG, "sendVsimNotification result = " + result);
            if (message == null) {
                int timeOut = 0;
                while (!result && timeOut < ExternalSimManager.PLUG_IN_AUTO_RETRY_TIMEOUT) {
                    try {
                        Thread.sleep(2000);
                        timeOut += MtkGsmCdmaPhone.EVENT_IMS_UT_DONE;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    result = ExternalSimManager.this.mCi[slotId].sendVsimNotification(transactionId, eventId, simType, message);
                }
            }
            if (!result) {
                Rlog.e(ExternalSimManager.TAG, "sendVsimNotification fail until 40000");
            }
        }

        private int sendSetRsimMappingInfoSync(int slotId, int transactionId) throws InterruptedException {
            VsimEvent event = new VsimEvent(0, ExternalSimConstants.MSG_ID_EVENT_RESPONSE, 1 << slotId);
            event.putInt(6);
            event.putInt(2);
            Message msg = new Message();
            msg.obj = event;
            msg.setTarget(ExternalSimManager.this.mEventHandler);
            if (ExternalSimManager.this.mCi[slotId].sendVsimNotification(transactionId, 6, 2, msg)) {
                Rlog.d(ExternalSimManager.TAG, "sendSetRsimMappingInfoSync before mLock.wait");
                ExternalSimManager.this.mLock.wait();
                return 0;
            }
            Rlog.e(ExternalSimManager.TAG, "sendSetRsimMappingInfoSync fail.");
            return -2;
        }

        private void sendPlugOutEvent(VsimEvent event) {
            TelephonyManager.getDefault();
            String isInserted = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
            if ("0".equals(isInserted)) {
                Rlog.d(ExternalSimManager.TAG, "sendPlugOutEvent: " + isInserted);
                return;
            }
            VsimEvent plugOutEvent = new VsimEvent(event.getTransactionId(), 3, event.getSlotBitMask());
            plugOutEvent.putInt(3);
            plugOutEvent.putInt(1);
            setMdWaitingFlag(false, event.getFirstSlotId());
            sendVsimNotification(event.getFirstSlotId(), plugOutEvent.mTransactionId, 3, 1, null);
        }

        private void sendHotPlugEvent(VsimEvent event, boolean plugIn) {
            int eventId = 4;
            if (!plugIn) {
                eventId = 3;
            }
            sendVsimNotification(event.getFirstSlotId(), event.getTransactionId(), eventId, 1, null);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getVsimSlotId(int simType) {
            if (simType == 2) {
                int rSim = SystemProperties.getInt(ExternalSimManager.PREFERED_RSIM_SLOT, -1);
                if (rSim == -1) {
                    return ExternalSimManager.sPreferedRsimSlot;
                }
                return rSim;
            } else if (simType != 3) {
                for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
                    TelephonyManager.getDefault();
                    String enable = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.enabled", "0");
                    if (enable != null && enable.length() > 0 && !"0".equals(enable)) {
                        TelephonyManager.getDefault();
                        String inserted = TelephonyManager.getTelephonyProperty(i, "vendor.gsm.external.sim.inserted", "0");
                        if (inserted != null && inserted.length() > 0 && String.valueOf(simType).equals(inserted)) {
                            return i;
                        }
                    }
                }
                return -1;
            } else {
                int akaSim = SystemProperties.getInt(ExternalSimManager.PREFERED_AKA_SIM_SLOT, -1);
                if (akaSim == -1) {
                    return ExternalSimManager.sPreferedAkaSlot;
                }
                return akaSim;
            }
        }

        private void sendRsimAuthProgressEvent(int eventId) {
            this.mIsSwitchRfSuccessful = false;
            int akaSim = getVsimSlotId(3);
            int rSim = getVsimSlotId(2);
            if (akaSim < 0 || akaSim > TelephonyManager.getDefault().getSimCount() || rSim < 0 || rSim > TelephonyManager.getDefault().getSimCount()) {
                Rlog.d(ExternalSimManager.TAG, "sendRsimAuthProgressEvent aka sim: " + akaSim + ", rsim: " + rSim);
                this.mIsSwitchRfSuccessful = true;
                return;
            }
            if (eventId == 201) {
                this.mIsAkaOccupyRf = true;
            } else if (eventId == 202) {
                if (!this.mIsAkaOccupyRf) {
                    Rlog.d(ExternalSimManager.TAG, "sendRsimAuthProgressEvent, aka didn't occupy rf");
                    return;
                }
                this.mIsAkaOccupyRf = false;
            }
            Rlog.d(ExternalSimManager.TAG, "sendRsimAuthProgressEvent mIsWaitingAuthRsp[" + rSim + "]: " + this.mIsWaitingAuthRsp[rSim]);
            VsimEvent event = new VsimEvent(0, ExternalSimConstants.MSG_ID_EVENT_RESPONSE, 1 << akaSim);
            event.putInt(eventId);
            event.putInt(1);
            Message msg = new Message();
            msg.obj = event;
            msg.setTarget(ExternalSimManager.this.mEventHandler);
            sendVsimNotification(event.getFirstSlotId(), event.mTransactionId, eventId, 1, msg);
            Rlog.d(ExternalSimManager.TAG, "sendRsimAuthProgressEvent eventId: " + eventId);
            try {
                ExternalSimManager.this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.w(ExternalSimManager.TAG, "sendRsimAuthProgressEvent InterruptedException.");
            }
        }

        private void sendActiveAkaSimEvent(int slotId, boolean turnOn) {
            int eventId;
            Rlog.d(ExternalSimManager.TAG, "sendActiveAkaSimEvent[" + slotId + "]: " + turnOn);
            int rsimSlot = getVsimSlotId(2);
            if (rsimSlot >= 0 && this.mIsWaitingAuthRsp[rsimSlot] && !turnOn) {
                sendRsimAuthProgressEvent(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP);
            }
            VsimEvent akaEvent = new VsimEvent(0, 3, 1 << slotId);
            if (turnOn) {
                int unused = ExternalSimManager.sPreferedAkaSlot = akaEvent.getFirstSlotId();
                eventId = 6;
                akaEvent.putInt(6);
            } else {
                int unused2 = ExternalSimManager.sPreferedAkaSlot = -1;
                eventId = ExternalSimConstants.EVENT_TYPE_RSIM_AUTH_DONE;
                akaEvent.putInt(ExternalSimConstants.EVENT_TYPE_RSIM_AUTH_DONE);
            }
            akaEvent.putInt(3);
            sendVsimNotification(akaEvent.getFirstSlotId(), akaEvent.mTransactionId, eventId, 3, null);
            if (rsimSlot >= 0 && this.mIsWaitingAuthRsp[rsimSlot] && turnOn) {
                sendRsimAuthProgressEvent(201);
            }
        }

        private void setMdWaitingFlag(boolean isWaiting, int slotId) {
            setMdWaitingFlag(isWaiting, null, slotId);
        }

        private void setMdWaitingFlag(boolean isWaiting, VsimEvent event, int slotId) {
            Rlog.d(ExternalSimManager.TAG, "setMdWaitingFlag[" + slotId + "]: " + isWaiting);
            this.mIsMdWaitingResponse[slotId] = isWaiting;
            if (isWaiting) {
                this.mWaitingEvent[slotId] = event;
                Timer[] timerArr = this.mNoResponseTimer;
                if (timerArr[slotId] == null) {
                    timerArr[slotId] = new Timer(true);
                }
                TelephonyManager.getDefault();
                String isVsimEnabled = TelephonyManager.getTelephonyProperty(event != null ? event.getFirstSlotId() : -1, "vendor.gsm.external.sim.enabled", "0");
                if ("".equals(isVsimEnabled) || "0".equals(isVsimEnabled)) {
                    this.mNoResponseTimer[slotId].schedule(new TimeOutTimerTask(slotId), 500);
                    if (System.currentTimeMillis() > this.mLastDisableEventTime + 5000) {
                        postDelayed(this.mTryResetModemRunnable, 2000);
                    }
                    Rlog.i(ExternalSimManager.TAG, "recevice modem event under vsim disabled state. lastDisableTime:" + this.mLastDisableEventTime);
                    return;
                }
                this.mNoResponseTimer[slotId].schedule(new TimeOutTimerTask(slotId), (long) this.mNoResponseTimeOut[slotId]);
                return;
            }
            Timer[] timerArr2 = this.mNoResponseTimer;
            if (timerArr2[slotId] != null) {
                timerArr2[slotId].cancel();
                this.mNoResponseTimer[slotId].purge();
                this.mNoResponseTimer[slotId] = null;
            }
            this.mWaitingEvent[slotId] = null;
        }

        private boolean getMdWaitingFlag(int slotId) {
            Rlog.d(ExternalSimManager.TAG, "getMdWaitingFlag[" + slotId + "]: " + this.mIsMdWaitingResponse[slotId]);
            return this.mIsMdWaitingResponse[slotId];
        }

        private boolean isRsimDataConnected() {
            if (ExternalSimManager.sPreferedRsimSlot < 0) {
                return false;
            }
            PhoneConstants.DataState dataState = PhoneFactory.getPhone(ExternalSimManager.sPreferedRsimSlot).getDataConnectionState();
            Rlog.d(ExternalSimManager.TAG, "rsim data state[" + ExternalSimManager.sPreferedRsimSlot + "]: " + dataState);
            if (dataState == PhoneConstants.DataState.CONNECTED) {
                return true;
            }
            return false;
        }

        private boolean isPlatformReady(int category) {
            if (category == 1) {
                return SubscriptionController.getInstance().isReady();
            }
            if (category == 2) {
                return true ^ ProxyController.getInstance().isCapabilitySwitching();
            }
            if (category == 3) {
                return UiccController.getInstance().isAllRadioAvailable();
            }
            Rlog.d(ExternalSimManager.TAG, "isPlatformReady invalid category: " + category);
            return true;
        }

        private int retryIfPlatformNotReady(VsimEvent event, int category) {
            boolean isReady = isPlatformReady(category);
            Rlog.d(ExternalSimManager.TAG, "retryIfPlatformNotReady category= " + category + ", isReady= " + isReady);
            if (!isReady) {
                int timeOut = 0;
                do {
                    try {
                        Thread.sleep(2000);
                        timeOut += MtkGsmCdmaPhone.EVENT_IMS_UT_DONE;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isReady = isPlatformReady(category);
                    if (isReady) {
                        break;
                    }
                } while (timeOut < ExternalSimManager.PLUG_IN_AUTO_RETRY_TIMEOUT);
            }
            if (isReady) {
                return 0;
            }
            Rlog.d(ExternalSimManager.TAG, "retryIfPlatformNotReady return not ready");
            return -2;
        }

        private boolean switchModemCapability(int rsimSlot) {
            int raf;
            MtkProxyController ctrl = ProxyController.getInstance();
            if (ctrl == null) {
                return false;
            }
            try {
                int len = TelephonyManager.getDefault().getPhoneCount();
                RadioAccessFamily[] rafs = new RadioAccessFamily[len];
                boolean atLeastOneMatch = false;
                for (int phoneId = 0; phoneId < len; phoneId++) {
                    if (phoneId == rsimSlot) {
                        raf = ctrl.getMaxRafSupported();
                        atLeastOneMatch = true;
                    } else {
                        raf = ctrl.getMinRafSupported();
                    }
                    Rlog.d(ExternalSimManager.TAG, "[switchModemCapability] raf[" + phoneId + "]=" + raf);
                    rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
                }
                if (atLeastOneMatch) {
                    ctrl.setRadioCapability(rafs);
                    return true;
                }
                Rlog.e(ExternalSimManager.TAG, "[switchModemCapability] rsim error:" + rsimSlot);
                return false;
            } catch (RuntimeException e) {
                Rlog.e(ExternalSimManager.TAG, "[switchModemCapability] setRadioCapability: Runtime Exception");
                e.printStackTrace();
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int retryIfRadioUnavailable(VsimEvent event) {
            return retryIfPlatformNotReady(event, 3);
        }

        private int retryIfSubNotReady(VsimEvent event) {
            return retryIfPlatformNotReady(event, 1);
        }

        private int retryIfCapabilitySwitching(VsimEvent event) {
            return retryIfPlatformNotReady(event, 2);
        }

        private void changeRadioSetting(boolean turnOn) {
            int simCount = TelephonyManager.getDefault().getSimCount();
            if (ExternalSimManager.isNonDsdaRemoteSimSupport() && simCount > 2) {
                int rsim = SystemProperties.getInt(ExternalSimManager.PREFERED_RSIM_SLOT, -1);
                int akaSim = SystemProperties.getInt(ExternalSimManager.PREFERED_AKA_SIM_SLOT, -1);
                for (int i = 0; i < simCount; i++) {
                    if (!(-1 == rsim || i == rsim || -1 == akaSim || i == akaSim)) {
                        int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(i);
                        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                        if (telephony != null) {
                            if (!turnOn) {
                                try {
                                    if (telephony.isRadioOnForSubscriber(subId, ExternalSimManager.this.mContext.getOpPackageName())) {
                                        ExternalSimManager.this.mUserRadioOn = true;
                                        telephony.setRadioForSubscriber(subId, false);
                                        Rlog.i(ExternalSimManager.TAG, "changeRadioSetting trun off radio subId:" + subId);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (true == turnOn && ExternalSimManager.this.mUserRadioOn) {
                                ExternalSimManager.this.mUserRadioOn = false;
                                telephony.setRadioForSubscriber(subId, true);
                                Rlog.i(ExternalSimManager.TAG, "changeRadioSetting trun on radio subId:" + subId);
                            }
                        } else {
                            Rlog.d(ExternalSimManager.TAG, "telephony is null");
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:211:0x04f2  */
        /* JADX WARNING: Removed duplicated region for block: B:212:0x04f7  */
        /* JADX WARNING: Removed duplicated region for block: B:215:0x0503  */
        private void handleEventRequest(int type, VsimEvent event) {
            VsimIoThread vsimIoThread;
            int newSlotId;
            int result;
            int result2;
            IccCardConstants.State state;
            int newSlotId2;
            int result3;
            int result4;
            int result5;
            int i;
            Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] start");
            int slotId = event.getFirstSlotId();
            int simType = event.getInt();
            int result6 = 0;
            int newSlotId3 = -1;
            Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler First slotId:" + slotId + ", simType:" + simType);
            if (0 != 0 || slotId < 0) {
                newSlotId = -1;
            } else if (slotId >= TelephonyManager.getDefault().getSimCount()) {
                newSlotId = -1;
            } else if (UiccController.getInstance().ignoreGetSimStatus()) {
                newSlotId = -1;
            } else if (type != 204) {
                switch (type) {
                    case 1:
                        newSlotId = -1;
                        result6 = retryIfRadioUnavailable(event);
                        if (result6 >= 0) {
                            result6 = retryIfSubNotReady(event);
                            if (result6 >= 0) {
                                result6 = retryIfCapabilitySwitching(event);
                                if (result6 >= 0) {
                                    MtkSubscriptionController ctrl = SubscriptionController.getInstance();
                                    int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(slotId);
                                    if (simType == 2) {
                                        ctrl.setDefaultDataSubIdWithoutCapabilitySwitch(subId);
                                        if (ExternalSimManager.isNonDsdaRemoteSimSupport()) {
                                            int unused = ExternalSimManager.sPreferedRsimSlot = slotId;
                                        }
                                        Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler set default data to subId: " + subId);
                                    }
                                    if (ExternalSimManager.isSupportVsimHotPlugOut() && !ExternalSimManager.isNonDsdaRemoteSimSupport()) {
                                        sendHotPlugEvent(event, false);
                                        waitSimPlugOut(slotId, MtkGsmCdmaPhone.EVENT_IMS_UT_DONE);
                                    }
                                    sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                                }
                                newSlotId3 = newSlotId;
                                break;
                            } else {
                                newSlotId3 = -1;
                                break;
                            }
                        } else {
                            newSlotId3 = -1;
                            break;
                        }
                    case 2:
                        newSlotId = -1;
                        TelephonyManager.getDefault();
                        String enabled = TelephonyManager.getTelephonyProperty(slotId, "vendor.gsm.external.sim.enabled", "0");
                        if (enabled != null && enabled.length() != 0) {
                            if (!"0".equals(enabled)) {
                                result = retryIfCapabilitySwitching(event);
                                if (result >= 0) {
                                    VsimEvent[] vsimEventArr = this.mWaitingEvent;
                                    if (vsimEventArr[slotId] != null) {
                                        sendNoResponseError(vsimEventArr[slotId]);
                                    }
                                    sendPlugOutEvent(event);
                                    if (ExternalSimManager.isNonDsdaRemoteSimSupport() || ExternalSimManager.isSupportVsimHotPlugOut()) {
                                        SubscriptionController ctrl2 = SubscriptionController.getInstance();
                                        IccCardConstants.State state2 = IccCardConstants.State.NOT_READY;
                                        int timeOut = 0;
                                        do {
                                            try {
                                                Thread.sleep(200);
                                                timeOut += 200;
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            IccCardConstants.State state3 = IccCardConstants.State.intToState(ctrl2.getSimStateForSlotIndex(slotId));
                                            List<SubscriptionInfo> subInfos = ctrl2.getSubInfoUsingSlotIndexPrivileged(slotId);
                                            if ((state3 == IccCardConstants.State.ABSENT || state3 == IccCardConstants.State.NOT_READY || state3 == IccCardConstants.State.UNKNOWN) && subInfos == null) {
                                            }
                                            Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler DISABLE_EXTERNAL_SIM state: " + state3);
                                        } while (timeOut < 20000);
                                        Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler DISABLE_EXTERNAL_SIM state: " + state3);
                                    }
                                    SystemProperties.set("gsm.vsim.slotid", "-1");
                                    if (getVsimSlotId(2) == slotId) {
                                        int unused2 = ExternalSimManager.sPreferedRsimSlot = -1;
                                        int unused3 = ExternalSimManager.sPreferedAkaSlot = -1;
                                        SystemProperties.set("vendor.gsm.disable.sim.dialog", "0");
                                    }
                                    sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                                    this.mLastDisableEventTime = System.currentTimeMillis();
                                    if (ExternalSimManager.isNonDsdaRemoteSimSupport() || ExternalSimManager.isSupportVsimHotPlugOut()) {
                                        Rlog.d(ExternalSimManager.TAG, "Disable VSIM without reset modem, sim switch:" + ((MtkSubscriptionController) SubscriptionController.getInstance()).setDefaultDataSubIdWithResult(SubscriptionManager.getDefaultDataSubscriptionId()));
                                    } else {
                                        ExternalSimManager.this.waitRildSetDisabledProperty(slotId);
                                        RadioManager.getInstance().setSilentRebootPropertyForAllModem("1");
                                        UiccController.getInstance().resetRadioForVsim();
                                    }
                                    if (ExternalSimManager.isSupportVsimHotPlugOut() && !ExternalSimManager.isNonDsdaRemoteSimSupport()) {
                                        sendHotPlugEvent(event, true);
                                    }
                                }
                                result6 = result;
                                newSlotId3 = newSlotId;
                                break;
                            }
                        }
                        Rlog.w(ExternalSimManager.TAG, "VsimEvenHandler didn't not enabled before.");
                        newSlotId3 = newSlotId;
                        break;
                    case 3:
                        newSlotId = -1;
                        VsimEvent[] vsimEventArr2 = this.mWaitingEvent;
                        if (vsimEventArr2[slotId] != null) {
                            sendNoResponseError(vsimEventArr2[slotId]);
                        }
                        sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                        newSlotId3 = newSlotId;
                        break;
                    case 4:
                        newSlotId = -1;
                        result = retryIfCapabilitySwitching(event);
                        if (result >= 0) {
                            if (ExternalSimManager.isNonDsdaRemoteSimSupport() || ExternalSimManager.isSupportVsimHotPlugOut()) {
                                SubscriptionController ctrl3 = SubscriptionController.getInstance();
                                IccCardConstants.State state4 = IccCardConstants.State.NOT_READY;
                                int timeOut2 = 0;
                                do {
                                    try {
                                        Thread.sleep(200);
                                        timeOut2 += 200;
                                    } catch (InterruptedException e2) {
                                        e2.printStackTrace();
                                    }
                                    state = IccCardConstants.State.intToState(ctrl3.getSimStateForSlotIndex(slotId));
                                    if (state == IccCardConstants.State.ABSENT || state == IccCardConstants.State.NOT_READY || state == IccCardConstants.State.UNKNOWN) {
                                        Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler REQUEST_TYPE_PLUG_IN state: " + state);
                                    }
                                } while (timeOut2 < 20000);
                                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler REQUEST_TYPE_PLUG_IN state: " + state);
                            }
                            MtkSubscriptionController ctrl4 = SubscriptionController.getInstance();
                            if (slotId != RadioCapabilitySwitchUtil.getMainCapabilityPhoneId() && simType != 1) {
                                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler need to do capablity switch");
                                if (!ctrl4.isReady()) {
                                    result6 = -2;
                                    newSlotId3 = -1;
                                    break;
                                } else {
                                    if (ctrl4.setDefaultDataSubIdWithResult(MtkSubscriptionManager.getSubIdUsingPhoneId(slotId))) {
                                        result2 = 0;
                                    } else {
                                        result2 = -1;
                                    }
                                    result6 = result2;
                                    newSlotId3 = -1;
                                    break;
                                }
                            } else {
                                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler no need to do capablity switch");
                                sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                                if (ExternalSimManager.isNonDsdaRemoteSimSupport() || ExternalSimManager.isSupportVsimHotPlugOut()) {
                                    Rlog.d(ExternalSimManager.TAG, "VSIM allow to enable without reset modem");
                                } else {
                                    RadioManager.getInstance().setSilentRebootPropertyForAllModem("1");
                                    UiccController.getInstance().resetRadioForVsim();
                                }
                            }
                        }
                        result6 = result;
                        newSlotId3 = newSlotId;
                        break;
                    case 5:
                        newSlotId = -1;
                        sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                        newSlotId3 = newSlotId;
                        break;
                    case 6:
                        newSlotId = -1;
                        if (simType == 2) {
                            int mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
                            Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler REQUEST_TYPE_SET_MAPPING_INFO:mainPhoneId= " + mainPhoneId);
                            int result7 = retryIfCapabilitySwitching(event);
                            if (result7 < 0) {
                                result6 = result7;
                                newSlotId3 = -1;
                            } else {
                                try {
                                    Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler isCapabilitySwitching: false.");
                                    int unused4 = ExternalSimManager.sPreferedRsimSlot = slotId;
                                    int result8 = sendSetRsimMappingInfoSync(slotId, event.mTransactionId);
                                    try {
                                        Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler sendSetRsimMappingInfoSync result:" + result8);
                                        if (result8 < 0) {
                                            result4 = result8;
                                        } else if (mainPhoneId == slotId) {
                                            result4 = result8;
                                        } else {
                                            try {
                                                ExternalSimManager.this.mSetCapabilityDone = 1;
                                                if (!switchModemCapability(slotId)) {
                                                    ExternalSimManager.this.mSetCapabilityDone = 0;
                                                    int unused5 = ExternalSimManager.sPreferedRsimSlot = -1;
                                                    result3 = result8;
                                                    try {
                                                        sendVsimNotification(slotId, event.mTransactionId, 7, simType, null);
                                                        result6 = -2;
                                                        newSlotId3 = -1;
                                                    } catch (InterruptedException e3) {
                                                        Rlog.w(ExternalSimManager.TAG, "VsimEvenHandler InterruptedException.");
                                                        result6 = result3;
                                                        newSlotId3 = -1;
                                                        VsimEvent eventResponse = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                                                        eventResponse.putInt(result6);
                                                        vsimIoThread = this.mVsimAdaptorIo;
                                                        if (vsimIoThread != null) {
                                                        }
                                                        Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
                                                    }
                                                } else {
                                                    if (ExternalSimManager.this.mSetCapabilityDone == 1) {
                                                        Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler before mLock.wait");
                                                        ExternalSimManager.this.mLock.wait();
                                                    }
                                                    Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler after mLock.wait");
                                                    if (ExternalSimManager.this.mSetCapabilityDone == 3) {
                                                        int unused6 = ExternalSimManager.sPreferedRsimSlot = -1;
                                                        i = 0;
                                                        sendVsimNotification(slotId, event.mTransactionId, 7, simType, null);
                                                        result5 = -2;
                                                    } else {
                                                        i = 0;
                                                        result5 = result8;
                                                    }
                                                    try {
                                                        ExternalSimManager.this.mSetCapabilityDone = i;
                                                        result6 = result5;
                                                    } catch (InterruptedException e4) {
                                                        result3 = result5;
                                                        Rlog.w(ExternalSimManager.TAG, "VsimEvenHandler InterruptedException.");
                                                        result6 = result3;
                                                        newSlotId3 = -1;
                                                        VsimEvent eventResponse2 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                                                        eventResponse2.putInt(result6);
                                                        vsimIoThread = this.mVsimAdaptorIo;
                                                        if (vsimIoThread != null) {
                                                        }
                                                        Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
                                                    }
                                                    newSlotId3 = -1;
                                                }
                                            } catch (InterruptedException e5) {
                                                result3 = result8;
                                                Rlog.w(ExternalSimManager.TAG, "VsimEvenHandler InterruptedException.");
                                                result6 = result3;
                                                newSlotId3 = -1;
                                                VsimEvent eventResponse22 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                                                eventResponse22.putInt(result6);
                                                vsimIoThread = this.mVsimAdaptorIo;
                                                if (vsimIoThread != null) {
                                                }
                                                Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
                                            }
                                        }
                                        result6 = result4;
                                        newSlotId3 = -1;
                                    } catch (InterruptedException e6) {
                                        result3 = result8;
                                        Rlog.w(ExternalSimManager.TAG, "VsimEvenHandler InterruptedException.");
                                        result6 = result3;
                                        newSlotId3 = -1;
                                        VsimEvent eventResponse222 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                                        eventResponse222.putInt(result6);
                                        vsimIoThread = this.mVsimAdaptorIo;
                                        if (vsimIoThread != null) {
                                        }
                                        Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
                                    }
                                } catch (InterruptedException e7) {
                                    result3 = result7;
                                    Rlog.w(ExternalSimManager.TAG, "VsimEvenHandler InterruptedException.");
                                    result6 = result3;
                                    newSlotId3 = -1;
                                    VsimEvent eventResponse2222 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                                    eventResponse2222.putInt(result6);
                                    vsimIoThread = this.mVsimAdaptorIo;
                                    if (vsimIoThread != null) {
                                    }
                                    Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
                                }
                            }
                        } else if (simType != 3) {
                            result6 = -1;
                            newSlotId3 = -1;
                        } else if (slotId < 0 || slotId >= TelephonyManager.getDefault().getSimCount()) {
                            int akaSim = SystemProperties.getInt(ExternalSimManager.PREFERED_AKA_SIM_SLOT, -1);
                            if (akaSim != -1) {
                                newSlotId2 = akaSim;
                                sendActiveAkaSimEvent(akaSim, false);
                            } else {
                                newSlotId2 = -1;
                            }
                            Rlog.d(ExternalSimManager.TAG, "Reset PREFERED_AKA_SIM_SLOT");
                            newSlotId3 = newSlotId2;
                        } else {
                            sendActiveAkaSimEvent(slotId, true);
                            newSlotId3 = newSlotId;
                        }
                        break;
                    case 7:
                        newSlotId = -1;
                        if (simType == 3) {
                            int akaSim2 = SystemProperties.getInt(ExternalSimManager.PREFERED_AKA_SIM_SLOT, -1);
                            if (akaSim2 != -1) {
                                sendActiveAkaSimEvent(akaSim2, false);
                            }
                            Rlog.d(ExternalSimManager.TAG, "Reset PREFERED_AKA_SIM_SLOT");
                        } else if (simType == 2) {
                            if (SystemProperties.getInt(ExternalSimManager.PREFERED_RSIM_SLOT, -1) != -1) {
                                sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                            }
                        }
                        newSlotId3 = newSlotId;
                        break;
                    case 8:
                        newSlotId = -1;
                        this.mNoResponseTimeOut[slotId] = simType * 1000;
                        sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                        newSlotId3 = newSlotId;
                        break;
                    case 9:
                        newSlotId = -1;
                        SystemProperties.set("vendor.gsm.disable.sim.dialog", "1");
                        newSlotId3 = newSlotId;
                        break;
                    case 10:
                        newSlotId = -1;
                        SystemProperties.set("vendor.gsm.disable.sim.dialog", "0");
                        newSlotId3 = newSlotId;
                        break;
                    case 11:
                        newSlotId = -1;
                        sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                        newSlotId3 = newSlotId;
                        break;
                    default:
                        result6 = -1;
                        Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler invalid event id.");
                        break;
                }
                VsimEvent eventResponse22222 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                eventResponse22222.putInt(result6);
                vsimIoThread = this.mVsimAdaptorIo;
                if (vsimIoThread != null) {
                    vsimIoThread.writeEvent(eventResponse22222);
                }
                Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
            } else {
                sendVsimNotification(slotId, event.mTransactionId, type, simType, null);
                Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
                return;
            }
            if (0 == 0) {
                result6 = -1;
                newSlotId3 = newSlotId;
                VsimEvent eventResponse222222 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
                eventResponse222222.putInt(result6);
                vsimIoThread = this.mVsimAdaptorIo;
                if (vsimIoThread != null) {
                }
                Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
            }
            newSlotId3 = newSlotId;
            VsimEvent eventResponse2222222 = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_EVENT_RESPONSE, newSlotId3 != -1 ? event.getSlotBitMask() : 1 << newSlotId3);
            eventResponse2222222.putInt(result6);
            vsimIoThread = this.mVsimAdaptorIo;
            if (vsimIoThread != null) {
            }
            Rlog.i(ExternalSimManager.TAG, "VsimEvenHandler eventHandlerByType: type[" + type + "] end");
        }

        private void waitSimPlugOut(int slotId, int duration) {
            SubscriptionController ctrl = SubscriptionController.getInstance();
            IccCardConstants.State state = IccCardConstants.State.NOT_READY;
            int timeOut = 0;
            do {
                try {
                    Thread.sleep(200);
                    timeOut += 200;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                IccCardConstants.State state2 = IccCardConstants.State.intToState(ctrl.getSimStateForSlotIndex(slotId));
                List<SubscriptionInfo> subInfos = ctrl.getSubInfoUsingSlotIndexPrivileged(slotId);
                if ((state2 == IccCardConstants.State.ABSENT || state2 == IccCardConstants.State.NOT_READY || state2 == IccCardConstants.State.UNKNOWN) && subInfos == null) {
                    return;
                }
            } while (timeOut < duration);
        }

        private void handleGetPlatformCapability(VsimEvent event) {
            event.getInt();
            int simType = event.getInt();
            VsimEvent response = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE, event.getSlotBitMask());
            if (SubscriptionController.getInstance().isReady()) {
                response.putInt(0);
            } else {
                response.putInt(-2);
            }
            TelephonyManager.MultiSimVariants config = TelephonyManager.getDefault().getMultiSimConfiguration();
            if (config == TelephonyManager.MultiSimVariants.DSDS) {
                response.putInt(1);
            } else if (config == TelephonyManager.MultiSimVariants.DSDA) {
                response.putInt(2);
            } else if (config == TelephonyManager.MultiSimVariants.TSTS) {
                response.putInt(3);
            } else {
                response.putInt(0);
            }
            if (SystemProperties.getInt("ro.vendor.mtk_external_sim_support", 0) > 0) {
                int mDefaultSupportVersion = 3;
                if (ExternalSimManager.isNonDsdaRemoteSimSupport()) {
                    mDefaultSupportVersion = 3 | 4;
                }
                response.putInt(mDefaultSupportVersion);
            } else {
                response.putInt(0);
            }
            int simCount = TelephonyManager.getDefault().getSimCount();
            Rlog.d(ExternalSimManager.TAG, "handleGetPlatformCapability simType: " + simType + ", simCount: " + simCount);
            if (simType == 1) {
                int rsimSlot = SystemProperties.getInt(ExternalSimManager.PREFERED_RSIM_SLOT, -1);
                if (rsimSlot == -1) {
                    response.putInt((1 << simCount) - 1);
                } else if (rsimSlot == 1 || rsimSlot == 4) {
                    response.putInt(2);
                } else if (rsimSlot == 2) {
                    response.putInt(1);
                }
            } else if (config == TelephonyManager.MultiSimVariants.DSDA) {
                int isCdmaCard = 0;
                int isHasCard = 0;
                for (int i = 0; i < simCount; i++) {
                    String cardType = SystemProperties.get(ExternalSimManager.PROPERTY_RIL_FULL_UICC_TYPE[i], "");
                    if (!cardType.equals("")) {
                        isHasCard |= 1 << i;
                    }
                    if (cardType.contains("CSIM") || cardType.contains("RUIM") || cardType.contains("UIM")) {
                        isCdmaCard |= 1 << i;
                    }
                }
                Rlog.d(ExternalSimManager.TAG, "handleGetPlatformCapability isCdmaCard: " + isCdmaCard + ", isHasCard: " + isHasCard);
                if (isHasCard == 0) {
                    response.putInt(0);
                } else if (isCdmaCard == 0) {
                    response.putInt(0);
                } else {
                    response.putInt(((1 << simCount) - 1) ^ isCdmaCard);
                }
            } else if (!ExternalSimManager.isNonDsdaRemoteSimSupport()) {
                response.putInt(0);
            } else if (config == TelephonyManager.MultiSimVariants.DSDS) {
                response.putInt((1 << simCount) - 1);
            } else if (config == TelephonyManager.MultiSimVariants.TSTS) {
                int vsimOnly = SystemProperties.getInt("ro.vendor.mtk_external_sim_only_slots", 0);
                if (vsimOnly != 0) {
                    response.putInt(vsimOnly);
                } else {
                    response.putInt((1 << simCount) - 1);
                }
            }
            this.mVsimAdaptorIo.writeEvent(response);
        }

        private void handleServiceStateRequest(VsimEvent event) {
            int result = 0;
            int voiceRejectCause = -1;
            int dataRejectCause = -1;
            VsimEvent response = new VsimEvent(event.getTransactionId(), ExternalSimConstants.MSG_ID_GET_SERVICE_STATE_RESPONSE, event.getSlotBitMask());
            if (SubscriptionController.getInstance().isReady()) {
                int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(event.getFirstSlotId());
                ServiceState ss = TelephonyManager.getDefault().getServiceStateForSubscriber(subId);
                if (ss != null) {
                    MtkServiceState mtkSs = (MtkServiceState) ss;
                    Rlog.d(ExternalSimManager.TAG, "handleServiceStateRequest subId: " + subId + ", ss = " + mtkSs.toString());
                    voiceRejectCause = mtkSs.getVoiceRejectCause();
                    dataRejectCause = mtkSs.getDataRejectCause();
                }
            } else {
                result = -2;
            }
            response.putInt(result);
            response.putInt(voiceRejectCause);
            response.putInt(dataRejectCause);
            this.mVsimAdaptorIo.writeEvent(response);
        }

        private Object getLock(int msgId) {
            if (msgId == 3 || msgId == 1003 || msgId == 1009 || msgId == 1010 || msgId == 2001 || msgId == 2002) {
                return ExternalSimManager.this.mLock;
            }
            return ExternalSimManager.this.mLock;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dispatchCallback(VsimEvent event) {
            synchronized (getLock(event.getMessageId())) {
                boolean z = false;
                if (this.mEventHandlingThread[event.getFirstSlotId()] != null) {
                    this.mEventHandlingThread[event.getFirstSlotId()].setWaiting(false);
                }
                int msgId = event.getMessageId();
                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler handleMessage[" + event.getFirstSlotId() + "]: msgId[" + msgId + "] start");
                if (msgId == 1009) {
                    TelephonyManager.getDefault();
                    String inserted = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
                    if (inserted != null && inserted.length() > 0 && !"0".equals(inserted)) {
                        this.mVsimAdaptorIo.writeEvent(event);
                    }
                } else if (msgId == 1010) {
                    sendNoResponseError(new VsimEvent(0, ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST, 1 << getVsimSlotId(2)));
                    TelephonyManager.getDefault();
                    String inserted2 = TelephonyManager.getTelephonyProperty(1 << getVsimSlotId(2), "vendor.gsm.external.sim.inserted", "0");
                    if (inserted2 != null && inserted2.length() > 0 && !"0".equals(inserted2)) {
                        this.mVsimAdaptorIo.writeEvent(event);
                    }
                } else if (msgId == 2001) {
                    setMdWaitingFlag(true, event, event.getFirstSlotId());
                    this.mIsWaitingAuthRsp[event.getFirstSlotId()] = true;
                    sendRsimAuthProgressEvent(201);
                    event.mMessageId = ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST;
                    TelephonyManager.getDefault();
                    String inserted3 = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
                    if (!this.mIsSwitchRfSuccessful) {
                        sendNoResponseError(event);
                    } else if (inserted3 != null && inserted3.length() > 0 && !"0".equals(inserted3)) {
                        this.mVsimAdaptorIo.writeEvent(event);
                    }
                } else if (msgId == 2002) {
                    ExternalSimManager.this.mLock.notifyAll();
                } else if (msgId != 5001) {
                    switch (msgId) {
                        case 1:
                            break;
                        case 2:
                            handleGetPlatformCapability(event);
                            break;
                        case 3:
                            handleEventRequest(event.getInt(), event);
                            break;
                        case 4:
                            if (getMdWaitingFlag(event.getFirstSlotId())) {
                                setMdWaitingFlag(false, event.getFirstSlotId());
                                ExternalSimManager.this.mCi[event.getFirstSlotId()].sendVsimOperation(event.getTransactionId(), event.getMessageId(), event.getInt(), event.getInt(), event.getDataByReadOffest(), null);
                                break;
                            }
                            break;
                        case 5:
                            if (this.mIsWaitingAuthRsp[event.getFirstSlotId()]) {
                                this.mIsWaitingAuthRsp[event.getFirstSlotId()] = false;
                                sendRsimAuthProgressEvent(ExternalSimConstants.EVENT_TYPE_RECEIVE_RSIM_AUTH_RSP);
                            }
                            if (getMdWaitingFlag(event.getFirstSlotId())) {
                                setMdWaitingFlag(false, event.getFirstSlotId());
                                TelephonyManager.getDefault();
                                String inserted4 = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
                                if (inserted4 == null || inserted4.length() <= 0 || "0".equals(inserted4)) {
                                    Rlog.d(ExternalSimManager.TAG, "ignore UICC_APDU_RESPONSE since vsim plug out.");
                                } else {
                                    ExternalSimManager.this.mCi[event.getFirstSlotId()].sendVsimOperation(event.getTransactionId(), event.getMessageId(), event.getInt(), event.getInt(), event.getDataByReadOffest(), null);
                                }
                                break;
                            }
                            break;
                        case 6:
                            break;
                        case 7:
                            handleServiceStateRequest(event);
                            break;
                        case 8:
                            break;
                        default:
                            switch (msgId) {
                                case ExternalSimConstants.MSG_ID_EVENT_RESPONSE /* 1003 */:
                                    int type = event.getInt();
                                    if (type != 201) {
                                        if (type != 202) {
                                            if (type == 6) {
                                                ExternalSimManager.this.mLock.notifyAll();
                                                break;
                                            }
                                        }
                                    }
                                    if (event.getInt() >= 0) {
                                        z = true;
                                    }
                                    this.mIsSwitchRfSuccessful = z;
                                    ExternalSimManager.this.mLock.notifyAll();
                                    break;
                                case ExternalSimConstants.MSG_ID_UICC_RESET_REQUEST /* 1004 */:
                                    setMdWaitingFlag(true, event, event.getFirstSlotId());
                                    TelephonyManager.getDefault();
                                    String inserted5 = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
                                    if (this.mVsimAdaptorIo != null && inserted5 != null && inserted5.length() > 0 && !"0".equals(inserted5)) {
                                        this.mVsimAdaptorIo.writeEvent(event);
                                        break;
                                    }
                                case ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST /* 1005 */:
                                    setMdWaitingFlag(true, event, event.getFirstSlotId());
                                    TelephonyManager.getDefault();
                                    String inserted6 = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
                                    if (this.mVsimAdaptorIo != null && inserted6 != null && inserted6.length() > 0 && !"0".equals(inserted6)) {
                                        this.mVsimAdaptorIo.writeEvent(event);
                                        break;
                                    } else {
                                        Rlog.d(ExternalSimManager.TAG, "ignore UICC_APDU_REQUEST since vsim plug out.");
                                        sendNoResponseError(event);
                                        break;
                                    }
                                case ExternalSimConstants.MSG_ID_UICC_POWER_DOWN_REQUEST /* 1006 */:
                                    TelephonyManager.getDefault();
                                    String inserted7 = TelephonyManager.getTelephonyProperty(event.getFirstSlotId(), "vendor.gsm.external.sim.inserted", "0");
                                    if (this.mVsimAdaptorIo != null && inserted7 != null && inserted7.length() > 0 && !"0".equals(inserted7)) {
                                        this.mVsimAdaptorIo.writeEvent(event);
                                        break;
                                    }
                                default:
                                    Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler handleMessage: default");
                                    break;
                            }
                            break;
                    }
                } else {
                    ExternalSimManager.this.mCi[event.getFirstSlotId()].sendVsimOperation(event.getTransactionId(), event.getMessageId(), event.getInt(), event.getInt(), event.getDataByReadOffest(), null);
                }
                Rlog.d(ExternalSimManager.TAG, "VsimEvenHandler handleMessage[" + event.getFirstSlotId() + "]: msgId[" + msgId + "] end");
            }
        }
    }
}

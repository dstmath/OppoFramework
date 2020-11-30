package com.mediatek.imsplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import com.mediatek.ims.ImsCallSessionProxy;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.plugin.impl.ImsSelfActivatorBase;
import com.mediatek.ims.ril.ImsCommandsInterface;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkSubscriptionManager;

public class ImsSelfActivatorImpl extends ImsSelfActivatorBase {
    protected static final String ACTION_AIRPLANE_CHANGE_DONE = "com.mediatek.intent.action.AIRPLANE_CHANGE_DONE";
    private static final int EVENT_IMS_REGISTRATION_INFO = 1;
    private static final int EVENT_IMS_REGISTRATION_TIMEOUT = 2;
    protected static final String EXTRA_AIRPLANE_MODE = "airplaneMode";
    private static final int IMS_REG_TIMEOUT = 10000;
    private static final boolean MTK_VZW_SUPPORT = "OP12".equals(SystemProperties.get("persist.vendor.operator.optr", "OM"));
    private static final String TAG = "ImsExtSelfActivatorImpl";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.imsplugin.ImsSelfActivatorImpl.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (ImsSelfActivatorImpl.ACTION_AIRPLANE_CHANGE_DONE.equals(intent.getAction())) {
                boolean isFlightmode = intent.getBooleanExtra(ImsSelfActivatorImpl.EXTRA_AIRPLANE_MODE, ImsSelfActivatorImpl.MTK_VZW_SUPPORT);
                Rlog.d(ImsSelfActivatorImpl.TAG, "ACTION_AIRPLANE_CHANGE_DONE: " + isFlightmode);
                if (isFlightmode == ImsSelfActivatorImpl.EVENT_IMS_REGISTRATION_INFO) {
                    ImsSelfActivatorImpl imsSelfActivatorImpl = ImsSelfActivatorImpl.this;
                    imsSelfActivatorImpl.doSelfActivationDial(imsSelfActivatorImpl.mDialString, ImsSelfActivatorImpl.this.mCallProfile, ImsSelfActivatorImpl.MTK_VZW_SUPPORT);
                    ImsSelfActivatorImpl.this.mContext.unregisterReceiver(ImsSelfActivatorImpl.this.mBroadcastReceiver);
                }
            }
        }
    };
    private ImsCallProfile mCallProfile;
    private Context mContext;
    private String mDialString;
    private Handler mHandler;
    private ImsCallSessionProxy mImsCallSessionProxy;
    private ImsCommandsInterface mImsRILAdapter;
    private ImsService mImsService;
    private boolean mIsDialed;
    private int mPhoneId;

    public ImsSelfActivatorImpl(Context context, Handler handler, ImsCallSessionProxy callSessionProxy, ImsCommandsInterface imsRILAdapter, ImsService imsService, int phoneId) {
        super(context, handler, callSessionProxy, imsRILAdapter, imsService, phoneId);
        Rlog.d(TAG, "Construct ImsExtSelfActivatorImpl()");
        this.mContext = context;
        this.mImsCallSessionProxy = callSessionProxy;
        this.mImsRILAdapter = imsRILAdapter;
        this.mHandler = new MyHandler(handler.getLooper());
        this.mPhoneId = phoneId;
        this.mImsService = imsService;
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == ImsSelfActivatorImpl.EVENT_IMS_REGISTRATION_INFO) {
                AsyncResult ar = (AsyncResult) msg.obj;
                int socketId = ((int[]) ar.result)[ImsSelfActivatorImpl.EVENT_IMS_REGISTRATION_TIMEOUT];
                Rlog.d(ImsSelfActivatorImpl.TAG, "handle EVENT_IMS_REGISTRATION_INFO");
                if (socketId != ImsSelfActivatorImpl.this.mPhoneId) {
                    Rlog.d(ImsSelfActivatorImpl.TAG, "Socket id and phone id not matched");
                } else if (((int[]) ar.result)[0] == ImsSelfActivatorImpl.EVENT_IMS_REGISTRATION_INFO) {
                    ImsSelfActivatorImpl.this.dialAnyway();
                    ImsSelfActivatorImpl.this.mHandler.removeMessages(ImsSelfActivatorImpl.EVENT_IMS_REGISTRATION_TIMEOUT);
                }
            } else if (i == ImsSelfActivatorImpl.EVENT_IMS_REGISTRATION_TIMEOUT) {
                ImsSelfActivatorImpl.this.dialAnyway();
            }
        }
    }

    public void doSelfActivationDial(String dialString, ImsCallProfile callProfile, boolean isEcc) {
        Rlog.d(TAG, "doSelfActivationDial()");
        this.mDialString = dialString;
        this.mCallProfile = callProfile;
        this.mIsDialed = MTK_VZW_SUPPORT;
        if (!tryLeaveFlightmodeBeforeDial()) {
            this.mImsRILAdapter.registerForImsRegistrationInfo(this.mHandler, (int) EVENT_IMS_REGISTRATION_INFO, (Object) null);
            this.mHandler.sendEmptyMessageDelayed(EVENT_IMS_REGISTRATION_TIMEOUT, 10000);
            notifySelfActivationSMBeforeDial(isEcc);
        }
    }

    private void notifySelfActivationSMBeforeDial(boolean isEcc) {
        IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (iTelEx == null) {
            Rlog.d(TAG, "Can not access ITelephonyEx");
            return;
        }
        Bundle extra = new Bundle();
        extra.putInt("key_mo_call_type", isEcc ? 1 : 0);
        Rlog.d(TAG, "notifySelfActivationSMBeforeDial");
        try {
            ImsService.getInstance(this.mContext);
            iTelEx.selfActivationAction((int) EVENT_IMS_REGISTRATION_INFO, extra, MtkSubscriptionManager.getSubIdUsingPhoneId(this.mPhoneId));
        } catch (RemoteException e) {
            Rlog.e(TAG, "RemoteException doSelfActivationDial()");
        }
    }

    public void close() {
        Rlog.d(TAG, "close()");
        this.mImsRILAdapter.unregisterForImsRegistrationInfo(this.mHandler);
        this.mHandler.removeMessages(EVENT_IMS_REGISTRATION_TIMEOUT);
        this.mImsCallSessionProxy = null;
        this.mCallProfile = null;
        this.mImsRILAdapter = null;
        this.mImsService = null;
    }

    public boolean shouldProcessSelfActivation(int phoneId) {
        if (this.mImsService.getImsServiceState(phoneId) == 0) {
            Rlog.d(TAG, "shouldProcessSelfActivation() IMS is IN_SERVICE, return false");
            return MTK_VZW_SUPPORT;
        }
        IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (iTelEx == null) {
            Rlog.d(TAG, "Can not access ITelephonyEx");
            return MTK_VZW_SUPPORT;
        }
        try {
            ImsService.getInstance(this.mContext);
            int selfActivateState = iTelEx.getSelfActivateState(MtkSubscriptionManager.getSubIdUsingPhoneId(phoneId));
            Rlog.d(TAG, "Self Activate State: " + selfActivateState);
            if (selfActivateState == EVENT_IMS_REGISTRATION_TIMEOUT) {
                return true;
            }
            return MTK_VZW_SUPPORT;
        } catch (RemoteException e) {
            Rlog.e(TAG, "RemoteException shouldProcessSelfActivation()");
            return MTK_VZW_SUPPORT;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dialAnyway() {
        if (!this.mIsDialed) {
            this.mImsCallSessionProxy.start(this.mDialString, this.mCallProfile);
            this.mIsDialed = true;
        }
    }

    private boolean tryLeaveFlightmodeBeforeDial() {
        boolean isInFlightmode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) > 0 ? EVENT_IMS_REGISTRATION_INFO : false;
        Rlog.d(TAG, "tryLeaveFlightmodeBeforeDial() isInFlightmode: " + isInFlightmode);
        if (!isInFlightmode) {
            return MTK_VZW_SUPPORT;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", MTK_VZW_SUPPORT);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_AIRPLANE_CHANGE_DONE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        return true;
    }
}

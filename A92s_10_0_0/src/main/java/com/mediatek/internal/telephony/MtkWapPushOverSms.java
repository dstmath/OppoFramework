package com.mediatek.internal.telephony;

import android.app.BroadcastOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.AbstractInboundSmsHandler;
import com.android.internal.telephony.IWapPushManager;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.WapPushOverSms;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import java.util.HashMap;

public class MtkWapPushOverSms extends WapPushOverSms {
    private static final boolean ENG = "eng".equals(Build.TYPE);
    private static final String TAG = "Mtk_WAP_PUSH";
    private Bundle bundle;

    public MtkWapPushOverSms(Context context) {
        super(context);
    }

    /* JADX INFO: Multiple debug info for r0v31 int: [D('dataIndex' int), D('intentData' byte[])] */
    private DecodedResult decodeWapPdu(byte[] pdu, InboundSmsHandler handler) {
        byte[] intentData;
        int subId;
        Exception e;
        DecodedResult result = new DecodedResult();
        if (ENG) {
            OppoRlog.Rlog.d(TAG, "Rx: " + IccUtils.bytesToHexString(pdu));
        }
        int index = 0 + 1;
        try {
            int transactionId = pdu[0] & 255;
            int index2 = index + 1;
            int pduType = pdu[index] & 255;
            int phoneId = handler.getPhone().getPhoneId();
            if (!(pduType == 6 || pduType == 7)) {
                int index3 = this.mContext.getResources().getInteger(17694907);
                if (index3 != -1) {
                    int index4 = index3 + 1;
                    transactionId = pdu[index3] & 255;
                    index2 = index4 + 1;
                    pduType = pdu[index4] & 255;
                    if (ENG) {
                        OppoRlog.Rlog.d(TAG, "index = " + index2 + " PDU Type = " + pduType + " transactionID = " + transactionId);
                    }
                    if (!(pduType == 6 || pduType == 7)) {
                        if (ENG) {
                            OppoRlog.Rlog.w(TAG, "Received non-PUSH WAP PDU. Type = " + pduType);
                        }
                        result.statusCode = 1;
                        return result;
                    }
                } else {
                    if (ENG) {
                        OppoRlog.Rlog.w(TAG, "Received non-PUSH WAP PDU. Type = " + pduType);
                    }
                    result.statusCode = 1;
                    return result;
                }
            }
            MtkWspTypeDecoder pduDecoder = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeWspTypeDecoder(pdu);
            if (!pduDecoder.decodeUintvarInteger(index2)) {
                if (ENG) {
                    OppoRlog.Rlog.w(TAG, "Received PDU. Header Length error.");
                }
                result.statusCode = 2;
                return result;
            }
            int headerLength = (int) pduDecoder.getValue32();
            int index5 = index2 + pduDecoder.getDecodedDataLength();
            if (!pduDecoder.decodeContentType(index5)) {
                if (ENG) {
                    OppoRlog.Rlog.w(TAG, "Received PDU. Header Content-Type error.");
                }
                result.statusCode = 2;
                return result;
            }
            String mimeType = pduDecoder.getValueString();
            long binaryContentType = pduDecoder.getValue32();
            int index6 = index5 + pduDecoder.getDecodedDataLength();
            byte[] header = new byte[headerLength];
            System.arraycopy(pdu, index5, header, 0, header.length);
            pduDecoder.decodeHeaders(index6, (headerLength - index6) + index5);
            if (mimeType == null || !mimeType.equals("application/vnd.wap.coc")) {
                int dataIndex = index5 + headerLength;
                intentData = new byte[(pdu.length - dataIndex)];
                System.arraycopy(pdu, dataIndex, intentData, 0, intentData.length);
            } else {
                intentData = pdu;
            }
            int[] subIds = SubscriptionManager.getSubId(phoneId);
            if (subIds == null || subIds.length <= 0) {
                subId = SmsManager.getDefaultSmsSubscriptionId();
            } else {
                subId = subIds[0];
            }
            try {
                e = new PduParser(intentData, shouldParseContentDisposition(subId)).parse();
            } catch (Exception e2) {
                OppoRlog.Rlog.e(TAG, "Unable to parse PDU: " + e2.toString());
                e = null;
            }
            if (e != null && e.getMessageType() == 130) {
                NotificationInd nInd = (NotificationInd) e;
                if (nInd.getFrom() != null && OppoRlog.BlockChecker.isBlocked(this.mContext, nInd.getFrom().getString())) {
                    result.statusCode = 1;
                    result.isBlock = true;
                }
            }
            if (pduDecoder.seekXWapApplicationId(index6, (index6 + headerLength) - 1)) {
                pduDecoder.decodeXWapApplicationId((int) pduDecoder.getValue32());
                String wapAppId = pduDecoder.getValueString();
                if (wapAppId == null) {
                    wapAppId = Integer.toString((int) pduDecoder.getValue32());
                }
                result.wapAppId = wapAppId;
                String contentType = mimeType == null ? Long.toString(binaryContentType) : mimeType;
                result.contentType = contentType;
                if (ENG) {
                    OppoRlog.Rlog.v(TAG, "appid found: " + wapAppId + ":" + contentType);
                }
            }
            result.subId = subId;
            result.phoneId = phoneId;
            result.parsedPdu = e;
            result.mimeType = mimeType;
            result.transactionId = transactionId;
            result.pduType = pduType;
            result.header = header;
            result.intentData = intentData;
            result.contentTypeParameters = pduDecoder.getContentParameters();
            result.statusCode = -1;
            result.headerList = pduDecoder.getHeaders();
            return result;
        } catch (ArrayIndexOutOfBoundsException aie) {
            OppoRlog.Rlog.e(TAG, "ignoring dispatchWapPdu() array index exception: " + aie);
            result.statusCode = 2;
        }
    }

    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler) {
        ComponentName componentName;
        Bundle options;
        try {
            DecodedResult result = decodeWapPdu(pdu, handler);
            if (result.statusCode != -1) {
                return result.statusCode;
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                writeInboxMessage(result.subId, result.parsedPdu);
            }
            handler.oemSetDefaultWappush(this.mContext);
            if (result.wapAppId != null) {
                boolean processFurther = true;
                try {
                    IWapPushManager wapPushMan = this.mWapPushManager;
                    if (wapPushMan != null) {
                        if (ENG) {
                            OppoRlog.Rlog.w(TAG, "addPowerSaveTempWhitelistAppForMms - start");
                        }
                        synchronized (this) {
                            this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(this.mWapPushManagerPackage, 0, "mms-mgr");
                        }
                        if (ENG) {
                            OppoRlog.Rlog.d(TAG, "addPowerSaveTempWhitelistAppForMms - end");
                        }
                        Intent intent = new Intent();
                        intent.putExtra("transactionId", result.transactionId);
                        intent.putExtra("pduType", result.pduType);
                        intent.putExtra("header", result.header);
                        intent.putExtra("data", result.intentData);
                        intent.putExtra("contentTypeParameters", result.contentTypeParameters);
                        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, result.phoneId);
                        intent.putExtra("wspHeaders", result.headerList);
                        if (this.bundle != null) {
                            OppoRlog.Rlog.d(TAG, "put addr info into intent 1");
                            intent.putExtra("address", this.bundle.getString("address"));
                            intent.putExtra("service_center", this.bundle.getString("service_center"));
                        }
                        int procRet = wapPushMan.processMessage(result.wapAppId, result.contentType, intent);
                        if (ENG) {
                            OppoRlog.Rlog.v(TAG, "procRet:" + procRet);
                        }
                        if ((procRet & 1) > 0 && (32768 & procRet) == 0) {
                            processFurther = false;
                        }
                    } else if (ENG) {
                        OppoRlog.Rlog.d(TAG, "wap push manager not found!");
                    }
                    if (!processFurther) {
                        return 1;
                    }
                } catch (RemoteException e) {
                    if (ENG) {
                        OppoRlog.Rlog.w(TAG, "remote func failed...");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (ENG) {
                OppoRlog.Rlog.v(TAG, "fall back to existing handler");
            }
            if (result.mimeType != null) {
                Intent intent2 = new Intent("android.provider.Telephony.WAP_PUSH_DELIVER");
                intent2.setType(result.mimeType);
                intent2.putExtra("transactionId", result.transactionId);
                intent2.putExtra("pduType", result.pduType);
                intent2.putExtra("header", result.header);
                intent2.putExtra("data", result.intentData);
                intent2.putExtra("contentTypeParameters", result.contentTypeParameters);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent2, result.phoneId);
                intent2.putExtra("wspHeaders", result.headerList);
                if (this.bundle != null) {
                    OppoRlog.Rlog.d(TAG, "put addr info into intent 2");
                    intent2.putExtra("address", this.bundle.getString("address"));
                    intent2.putExtra("service_center", this.bundle.getString("service_center"));
                }
                ComponentName componentName2 = SmsApplication.getDefaultMmsApplication(this.mContext, true);
                ComponentName romComponentName = AbstractInboundSmsHandler.romDealWithMtMms(result.isBlock, intent2, componentName2);
                if (romComponentName != null) {
                    componentName = romComponentName;
                } else {
                    componentName = componentName2;
                }
                if (componentName != null) {
                    intent2.setComponent(componentName);
                    if (ENG) {
                        OppoRlog.Rlog.v(TAG, "Delivering MMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
                    }
                    try {
                        long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(componentName.getPackageName(), 0, "mms-app");
                        BroadcastOptions bopts = BroadcastOptions.makeBasic();
                        bopts.setTemporaryAppWhitelistDuration(duration);
                        options = bopts.toBundle();
                    } catch (RemoteException e2) {
                        OppoRlog.Rlog.d(TAG, "dispatchWapPdu exception");
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                    handler.dispatchIntent(intent2, getPermissionForType(result.mimeType), getAppOpsPermissionForIntent(result.mimeType), options, receiver, UserHandle.SYSTEM);
                    return -1;
                }
                options = null;
                handler.dispatchIntent(intent2, getPermissionForType(result.mimeType), getAppOpsPermissionForIntent(result.mimeType), options, receiver, UserHandle.SYSTEM);
                return -1;
            } else if (!ENG) {
                return 2;
            } else {
                OppoRlog.Rlog.w(TAG, "Header Content-Type error.");
                return 2;
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    private final class DecodedResult {
        String contentType;
        HashMap<String, String> contentTypeParameters;
        byte[] header;
        HashMap<String, String> headerList;
        byte[] intentData;
        boolean isBlock;
        String mimeType;
        GenericPdu parsedPdu;
        int pduType;
        int phoneId;
        int statusCode;
        int subId;
        int transactionId;
        String wapAppId;

        private DecodedResult() {
            this.isBlock = false;
        }
    }

    public int dispatchWapPdu(byte[] pdu, BroadcastReceiver receiver, InboundSmsHandler handler, Bundle extra) {
        if (ENG) {
            OppoRlog.Rlog.i(TAG, "dispathchWapPdu!");
        }
        this.bundle = extra;
        return dispatchWapPdu(pdu, receiver, handler);
    }
}

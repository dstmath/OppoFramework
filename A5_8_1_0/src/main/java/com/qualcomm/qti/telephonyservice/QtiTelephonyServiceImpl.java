package com.qualcomm.qti.telephonyservice;

import android.content.Context;
import android.util.Log;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qti.telephonyservice.IQtiTelephonyService.Stub;

public class QtiTelephonyServiceImpl extends Stub {
    private static final String ACCESS_USER_AUTHENTICATION_APIS_ERROR = "Need com.qualcomm.qti.permission.ACCESS_USER_AUTHENTICATION_APIS to access api";
    private static final String ACCESS_USER_AUTHENTICATION_APIS_PERMISSION = "com.qualcomm.qti.permission.ACCESS_USER_AUTHENTICATION_APIS";
    private static final String TAG = "QtiTelephonyServiceImpl";
    private static final String VERSION = "1";
    private final Context mContext;
    private IQcRilHook mQcRilHook;
    private RilOemMessageBuilder messageBuilder = new RilOemMessageBuilder();

    public QtiTelephonyServiceImpl(Context context, IQcRilHook qcRilHook) {
        this.mContext = context;
        this.mQcRilHook = qcRilHook;
        Log.d(TAG, "Service created. Version=1");
    }

    protected void setQcRilHook(IQcRilHook qcRilHook) {
        this.mQcRilHook = qcRilHook;
    }

    public String getVersion() {
        return VERSION;
    }

    public KsNafResponse gbaInit(byte[] securityProtocol, String nafFullyQualifiedDomainName, int slotId, int applicationType, boolean forceBootStrapping) {
        enforceAccessUserAuthenticationApisPermission();
        KsNafResponse response = null;
        try {
            return RilOemProtoParser.parseKsNafResponse(this.mQcRilHook.qcRilSendProtocolBufferMessage(this.messageBuilder.buildGbaInitRequest(securityProtocol, nafFullyQualifiedDomainName, slotId, applicationType, forceBootStrapping), slotId), 1);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "IllegalArgumentException: " + ex);
            return response;
        } catch (InvalidProtocolBufferMicroException ex2) {
            Log.e(TAG, "InvalidProtocolBufferMicroException: " + ex2);
            return response;
        }
    }

    public byte[] getImpi(int slotId, int applicationType, boolean secure) {
        enforceAccessUserAuthenticationApisPermission();
        byte[] response = new byte[0];
        try {
            return RilOemProtoParser.parseImpi(this.mQcRilHook.qcRilSendProtocolBufferMessage(this.messageBuilder.buildImpiRequest(slotId, applicationType, secure), slotId), 2);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "IllegalArgumentException: " + ex);
            return response;
        } catch (InvalidProtocolBufferMicroException ex2) {
            Log.e(TAG, "InvalidProtocolBufferMicroException: " + ex2);
            return response;
        }
    }

    private void enforceAccessUserAuthenticationApisPermission() {
        this.mContext.enforceCallingOrSelfPermission(ACCESS_USER_AUTHENTICATION_APIS_PERMISSION, ACCESS_USER_AUTHENTICATION_APIS_ERROR);
    }
}

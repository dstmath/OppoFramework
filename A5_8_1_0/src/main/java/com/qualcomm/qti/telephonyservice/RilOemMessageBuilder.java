package com.qualcomm.qti.telephonyservice;

import com.google.protobuf.micro.ByteStringMicro;
import com.qualcomm.qti.telephonyservice.TelephonyServiceProtos.RILOEMMessage;
import com.qualcomm.qti.telephonyservice.TelephonyServiceProtos.RilOemGbaInitRequestPayload;
import com.qualcomm.qti.telephonyservice.TelephonyServiceProtos.RilOemImpiRequestPayload;

public class RilOemMessageBuilder {
    private static final String TAG = "RilOemMessageBuilder";
    private static final int TOKEN_BASE = 1000;
    private int mToken = TOKEN_BASE;

    public byte[] buildImpiRequest(int slotId, int applicationType, boolean secure) {
        validateSlotId(slotId);
        validateApplicationType(applicationType);
        RilOemImpiRequestPayload impiRequest = new RilOemImpiRequestPayload();
        impiRequest.setSlotId(slotId);
        impiRequest.setApplicationType(applicationType);
        impiRequest.setSecure(secure);
        return buildMessage(2, impiRequest.toByteArray());
    }

    public byte[] buildGbaInitRequest(byte[] securityProtocol, String nafFullyQualifiedDomainName, int slotId, int applicationType, boolean forceBootStrapping) {
        validateSlotId(slotId);
        validateApplicationType(applicationType);
        if (nafFullyQualifiedDomainName == null || nafFullyQualifiedDomainName.isEmpty()) {
            throw new IllegalArgumentException("nafFullyQualifiedDomainName cannot be null or empty");
        } else if (securityProtocol.length == 0) {
            throw new IllegalArgumentException("securityProtocol cannot be null or empty");
        } else {
            RilOemGbaInitRequestPayload gbaInitRequest = new RilOemGbaInitRequestPayload();
            gbaInitRequest.setSecurityProtocol(ByteStringMicro.copyFrom(securityProtocol));
            gbaInitRequest.setNafFullyQualifiedDomainName(nafFullyQualifiedDomainName);
            gbaInitRequest.setSlotId(slotId);
            gbaInitRequest.setApplicationType(applicationType);
            gbaInitRequest.setForceBootstrapping(forceBootStrapping);
            return buildMessage(1, gbaInitRequest.toByteArray());
        }
    }

    private byte[] buildMessage(int messageId, byte[] payload) {
        RILOEMMessage messageTag = new RILOEMMessage();
        messageTag.setToken(incrementToken());
        messageTag.setType(1);
        messageTag.setId(messageId);
        messageTag.setPayload(ByteStringMicro.copyFrom(payload));
        return messageTag.toByteArray();
    }

    private int incrementToken() {
        int i;
        synchronized (this) {
            i = this.mToken + 1;
            this.mToken = i;
        }
        return i;
    }

    private void validateSlotId(int slotId) {
        if (slotId != 0 && slotId != 1 && slotId != 2) {
            throw new IllegalArgumentException("Not a valid SlotId");
        }
    }

    private void validateApplicationType(int applicationType) {
        if (applicationType != 0 && applicationType != 1 && applicationType != 2 && applicationType != 3 && applicationType != 4 && applicationType != 5) {
            throw new IllegalArgumentException("Not a valid ApplicationType");
        }
    }
}

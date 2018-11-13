package com.qualcomm.qti.telephonyservice;

import android.util.Log;
import com.google.protobuf.micro.InvalidProtocolBufferMicroException;
import com.qualcomm.qti.telephonyservice.TelephonyServiceProtos.RILOEMMessage;
import com.qualcomm.qti.telephonyservice.TelephonyServiceProtos.RilOemGbaInitResponsePayload;
import com.qualcomm.qti.telephonyservice.TelephonyServiceProtos.RilOemImpiResponsePayload;

public class RilOemProtoParser {
    private static final String TAG = "RilOemProtoParser";

    private static void validateProtoBuf(byte[] protobuf) {
        if (protobuf == null) {
            throw new IllegalArgumentException("protoBuf cannot be null ");
        }
    }

    private static void validateResponse(RILOEMMessage response, int messageId) {
        if (2 != response.getType()) {
            throw new IllegalArgumentException("Expected RilOemMessageType 2");
        } else if (messageId != response.getId()) {
            throw new IllegalArgumentException("Expected RilOemMessageId " + messageId);
        } else if (response.hasError() && response.getError() != 0) {
            Log.e(TAG, "RilOemError: " + response.getError());
            throw new IllegalArgumentException("Expected RilOemError 0");
        }
    }

    public static byte[] parseImpi(byte[] protobuf, int messageId) throws InvalidProtocolBufferMicroException {
        validateProtoBuf(protobuf);
        RILOEMMessage responseMessage = RILOEMMessage.parseFrom(protobuf);
        validateResponse(responseMessage, messageId);
        return RilOemImpiResponsePayload.parseFrom(responseMessage.getPayload().toByteArray()).getImpi().toByteArray();
    }

    public static KsNafResponse parseKsNafResponse(byte[] protobuf, int messageId) throws InvalidProtocolBufferMicroException {
        validateProtoBuf(protobuf);
        RILOEMMessage responseMessage = RILOEMMessage.parseFrom(protobuf);
        validateResponse(responseMessage, messageId);
        RilOemGbaInitResponsePayload gbaInitResponse = RilOemGbaInitResponsePayload.parseFrom(responseMessage.getPayload().toByteArray());
        return new KsNafResponse(gbaInitResponse.getKsNAFType(), gbaInitResponse.getKsNAFResponse().toByteArray(), gbaInitResponse.getBootstrapTransactionId(), gbaInitResponse.getKsLifetime());
    }
}

package com.android.server.neuron.publish;

import android.os.Parcel;
import android.util.Slog;

public final class Response {
    static final String TAG = "NeuronSystem";
    private boolean mIsIndication;
    private NativeIndication mNativeIndicaion;
    private NativeResponse mRespData;

    private Response(NativeResponse data) {
        this.mIsIndication = false;
        this.mRespData = null;
        this.mNativeIndicaion = null;
        this.mIsIndication = false;
        this.mRespData = data;
    }

    private Response(NativeIndication indication) {
        this.mIsIndication = false;
        this.mRespData = null;
        this.mNativeIndicaion = null;
        this.mIsIndication = true;
        this.mNativeIndicaion = indication;
    }

    public boolean isIndication() {
        return this.mIsIndication;
    }

    public NativeResponse getResponseData() {
        return this.mRespData;
    }

    public NativeIndication getIndication() {
        return this.mNativeIndicaion;
    }

    public static Response makeReponse(Parcel parcel) {
        if (parcel.readInt() != 61695) {
            return null;
        }
        if (parcel.readInt() != 0) {
            NativeIndication ni = parseIndicationFromParcel(parcel);
            if (ni == null) {
                return null;
            }
            return new Response(ni);
        }
        NativeResponse nr = new NativeResponse();
        nr.serial = parcel.readInt();
        nr.error = parcel.readInt();
        return new Response(nr);
    }

    private static NativeIndication parseIndicationFromParcel(Parcel parcel) {
        NativeIndication ni = new NativeIndication();
        ni.command = parcel.readInt();
        int i = ni.command;
        switch (i) {
            case 101:
            case 102:
            case 103:
            case 104:
                break;
            default:
                switch (i) {
                    case ProtocolConstants.UNSOL_SET_RSSI_UPDATE_FREQ:
                    case ProtocolConstants.UNSOL_SET_GPS_UPDATE_FREQ:
                    case ProtocolConstants.UNSOL_SET_SENSOR_UPDATE_FREQ:
                        break;
                    default:
                        Slog.e("NeuronSystem", "Unknow indication command:" + ni.command);
                        return null;
                    case ProtocolConstants.UNSOL_SET_ELSA_MODE:
                        ni.arg1 = parcel.readInt();
                        break;
                }
        }
        return ni;
    }

    public static class NativeResponse {
        public int error;
        public int serial;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("serial:");
            sb.append(String.valueOf(this.serial));
            sb.append(" ok:");
            boolean z = true;
            if (1 != this.error) {
                z = false;
            }
            sb.append(z);
            return sb.toString();
        }
    }

    public static class NativeIndication {
        public int arg1;
        public int arg2;
        public int command;

        public String toString() {
            return "command:" + String.valueOf(this.command);
        }
    }
}

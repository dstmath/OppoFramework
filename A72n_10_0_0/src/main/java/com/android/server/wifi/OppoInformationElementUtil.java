package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.util.Log;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OppoInformationElementUtil {
    public static final int BYTE_MASK = 255;
    public static final int INT_LENGTH_IN_BYTE = 4;
    public static final long INT_MASK = 4294967295L;
    public static final int MICROSOFT_OUI_TYPE = 15880192;
    public static final int NIBBLE_MASK = 15;
    public static final int SHORT_HIGH_MASK = 65280;
    public static final int SHORT_LOW_MASK = 255;
    public static final int SHORT_MASK = 65535;
    private static final String TAG = "OppoInformationElementUtil";
    public static final int VENDOR_OUI_LENGTH = 3;
    private static final int VENDOR_VALUE_MAX_LENGTH = 10;
    public static final int WPS_VENDOR_OUI_TYPE = 82989056;

    public static class WpsAttr {
        public static final short APPLICATION_EXT = 4184;
        public static final short APPSESSIONKEY = 4195;
        public static final short AP_CHANNEL = 4097;
        public static final short AP_SETUP_LOCKED = 4183;
        public static final short ASSOC_STATE = 4098;
        public static final short AUTHENTICATOR = 4101;
        public static final short AUTH_TYPE = 4099;
        public static final short AUTH_TYPE_FLAGS = 4100;
        public static final short CONFIG_ERROR = 4105;
        public static final short CONFIG_METHODS = 4104;
        public static final short CONFIRM_URL4 = 4106;
        public static final short CONFIRM_URL6 = 4107;
        public static final short CONN_TYPE = 4108;
        public static final short CONN_TYPE_FLAGS = 4109;
        public static final short CRED = 4110;
        public static final short DEV_NAME = 4113;
        public static final short DEV_PASSWORD_ID = 4114;
        public static final short EAP_IDENTITY = 4173;
        public static final short EAP_TYPE = 4185;
        public static final short ENCR_SETTINGS = 4120;
        public static final short ENCR_TYPE = 4111;
        public static final short ENCR_TYPE_FLAGS = 4112;
        public static final short ENROLLEE_NONCE = 4122;
        public static final short EXTENSIBILITY_TEST = 4346;
        public static final short E_HASH1 = 4116;
        public static final short E_HASH2 = 4117;
        public static final short E_SNONCE1 = 4118;
        public static final short E_SNONCE2 = 4119;
        public static final short FEATURE_ID = 4123;
        public static final short IDENTITY = 4124;
        public static final short IDENTITY_PROOF = 4125;
        public static final short IEEE802_1X_ENABLED = 4194;
        public static final short IV = 4192;
        public static final short KEY_ID = 4127;
        public static final short KEY_LIFETIME = 4177;
        public static final short KEY_PROVIDED_AUTO = 4193;
        public static final short KEY_WRAP_AUTH = 4126;
        public static final short MAC_ADDR = 4128;
        public static final short MANUFACTURER = 4129;
        public static final short MODEL_NAME = 4131;
        public static final short MODEL_NUMBER = 4132;
        public static final short MSG_COUNTER = 4174;
        public static final short MSG_TYPE = 4130;
        public static final short NETWORK_INDEX = 4134;
        public static final short NETWORK_KEY = 4135;
        public static final short NETWORK_KEY_INDEX = 4136;
        public static final short NEW_DEVICE_NAME = 4137;
        public static final short NEW_PASSWORD = 4138;
        public static final short OOB_DEVICE_PASSWORD = 4140;
        public static final short OS_VERSION = 4141;
        public static final short PERMITTED_CFG_METHODS = 4178;
        public static final short PORTABLE_DEV = 4182;
        public static final short POWER_LEVEL = 4143;
        public static final short PRIMARY_DEV_TYPE = 4180;
        public static final short PSK_CURRENT = 4144;
        public static final short PSK_MAX = 4145;
        public static final short PUBKEY_HASH = 4175;
        public static final short PUBLIC_KEY = 4146;
        public static final short RADIO_ENABLE = 4147;
        public static final short REBOOT = 4148;
        public static final short REGISTRAR_CURRENT = 4149;
        public static final short REGISTRAR_ESTABLISHED = 4150;
        public static final short REGISTRAR_LIST = 4151;
        public static final short REGISTRAR_MAX = 4152;
        public static final short REGISTRAR_NONCE = 4153;
        public static final short REKEY_KEY = 4176;
        public static final short REQUESTED_DEV_TYPE = 4202;
        public static final short REQUEST_TYPE = 4154;
        public static final short RESPONSE_TYPE = 4155;
        public static final short RF_BANDS = 4156;
        public static final short R_HASH1 = 4157;
        public static final short R_HASH2 = 4158;
        public static final short R_SNONCE1 = 4159;
        public static final short R_SNONCE2 = 4160;
        public static final short SECONDARY_DEV_TYPE_LIST = 4181;
        public static final short SELECTED_REGISTRAR = 4161;
        public static final short SELECTED_REGISTRAR_CONFIG_METHODS = 4179;
        public static final short SERIAL_NUMBER = 4162;
        public static final short SSID = 4165;
        public static final short TOTAL_NETWORKS = 4166;
        public static final short UUID_E = 4167;
        public static final short UUID_R = 4168;
        public static final short VENDOR_EXT = 4169;
        public static final short VERSION = 4170;
        public static final short WEPTRANSMITKEY = 4196;
        public static final short WPS_STATE = 4164;
        public static final short X509_CERT = 4172;
        public static final short X509_CERT_REQ = 4171;
    }

    public static boolean isVendorSpecElement(ScanResult.InformationElement ie) {
        if (ie != null && ie.id == 221) {
            return true;
        }
        return false;
    }

    public static boolean isWpsElement(ScanResult.InformationElement ie) {
        if (ie == null || ie.id != 221) {
            return false;
        }
        try {
            if (ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).getInt() == 82989056) {
                return true;
            }
            return false;
        } catch (BufferUnderflowException e) {
            Log.e(TAG, "Couldn't parse VSA IE, buffer underflow");
            return false;
        }
    }

    public static String getVendorOUI(ScanResult sr) {
        if (sr == null || sr.informationElements == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        ScanResult.InformationElement[] informationElementArr = sr.informationElements;
        int length = informationElementArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            ScanResult.InformationElement ie = informationElementArr[i];
            if (isVendorSpecElement(ie) && !isMicrosoftVendor(ie)) {
                sb.append(parseOuiFromVendorIE(ie));
                break;
            }
            i++;
        }
        return sb.toString();
    }

    public static String getWifiStandard(ScanResult sr) {
        StringBuilder wifiStandard = new StringBuilder();
        if (sr != null) {
            ScanResult.InformationElement[] informationElementArr = sr.informationElements;
            for (ScanResult.InformationElement ie : informationElementArr) {
                if (ie != null) {
                    int i = ie.id;
                    if (i == 45) {
                        wifiStandard.append("[HT]");
                    } else if (i == 191) {
                        wifiStandard.append("[VHT]");
                    }
                }
            }
        }
        return wifiStandard.toString();
    }

    public static String getVendorSpec(ScanResult sr) {
        WpsInformationElement wpsIE = new WpsInformationElement();
        wpsIE.from(sr);
        return wpsIE.toString() + getVendorOUI(sr);
    }

    private static boolean isMicrosoftVendor(ScanResult.InformationElement ie) {
        if (ie != null && ie.id == 221) {
            try {
                byte[] oui = new byte[3];
                ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).get(oui);
                if (15880192 == byteArrayToInt(oui)) {
                    return true;
                }
                return false;
            } catch (BufferUnderflowException e) {
                Log.e(TAG, "Couldn't parse Microsoft IE, buffer underflow");
            }
        }
        return false;
    }

    private static String parseOuiFromVendorIE(ScanResult.InformationElement vendorIe) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer buf = ByteBuffer.wrap(vendorIe.bytes).order(ByteOrder.LITTLE_ENDIAN);
        int attrLen = buf.remaining();
        if (attrLen <= 3) {
            return "";
        }
        try {
            byte[] oui = new byte[3];
            buf.get(oui);
            sb.append("Chipset OUI:" + byteArrayToHexString(oui));
            int valueLen = 10;
            if (attrLen - 3 <= 10) {
                valueLen = attrLen - 3;
            }
            byte[] value = new byte[valueLen];
            buf.get(value);
            sb.append(", Value:");
            sb.append(byteArrayToHexString(value));
        } catch (BufferUnderflowException e) {
            Log.e(TAG, "Couldn't parse WPS IE, buffer underflow");
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static String byteArrayToString(byte[] data) {
        if (data == null) {
            return "";
        }
        return new String(data);
    }

    private static String byteArrayToHexString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (data == null || data.length <= 0) {
            return "";
        }
        for (byte b : data) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static int byteArrayToInt(byte[] data) {
        if (data == null || data.length <= 0) {
            return 0;
        }
        int ret = 0;
        int length = 4;
        if (data.length <= 4) {
            length = data.length;
        }
        for (int i = 0; i < length; i++) {
            ret |= (data[i] & 255) << (i * 8);
        }
        return ret;
    }

    public static class WpsInformationElement {
        private String mDeviceName = "";
        private String mManufacturer = "";
        private String mModeName = "";
        private String mModeNumber = "";
        private String mSerialNumber = "";

        public void from(ScanResult sr) {
            if (!(sr == null || sr.informationElements == null)) {
                ScanResult.InformationElement[] informationElementArr = sr.informationElements;
                for (ScanResult.InformationElement ie : informationElementArr) {
                    if (OppoInformationElementUtil.isWpsElement(ie)) {
                        fromWpsIe(ie);
                        return;
                    }
                }
            }
        }

        public void fromWpsIe(ScanResult.InformationElement wpsIe) {
            ByteBuffer buf = ByteBuffer.wrap(wpsIe.bytes).order(ByteOrder.LITTLE_ENDIAN);
            try {
                buf.getInt();
                while (buf.remaining() > 4) {
                    int attr = ((buf.get() & 255) << 8) | (buf.get() & 255);
                    byte[] attrBytes = new byte[(((buf.get() & 255) << 8) | (buf.get() & 255))];
                    buf.get(attrBytes);
                    if (attr == 4113) {
                        this.mDeviceName = OppoInformationElementUtil.byteArrayToString(attrBytes);
                    } else if (attr == 4129) {
                        this.mManufacturer = OppoInformationElementUtil.byteArrayToString(attrBytes);
                    } else if (attr == 4162) {
                        this.mSerialNumber = OppoInformationElementUtil.byteArrayToString(attrBytes);
                    } else if (attr == 4131) {
                        this.mModeName = OppoInformationElementUtil.byteArrayToString(attrBytes);
                    } else if (attr == 4132) {
                        this.mModeNumber = OppoInformationElementUtil.byteArrayToString(attrBytes);
                    }
                }
            } catch (BufferUnderflowException e) {
                Log.e(OppoInformationElementUtil.TAG, "Couldn't parse WPS IE 3, buffer underflow");
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(this.mManufacturer)) {
                sb.append("Manufacturer:" + this.mManufacturer);
            }
            if (!TextUtils.isEmpty(this.mModeName)) {
                sb.append(", Model:" + this.mModeName);
            }
            if (!TextUtils.isEmpty(this.mModeNumber)) {
                sb.append(", Model number:" + this.mModeNumber);
            }
            if (!TextUtils.isEmpty(this.mDeviceName)) {
                sb.append(", Device:" + this.mDeviceName);
            }
            sb.append(" ");
            return sb.toString();
        }
    }
}

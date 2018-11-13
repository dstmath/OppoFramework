package com.android.server.wifi.anqp.eap;

import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import com.android.server.wifi.anqp.eap.EAP.EAPMethodID;
import com.android.server.wifi.hotspot2.pps.Credential;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EAPMethod {
    /* renamed from: -com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues */
    private static final /* synthetic */ int[] f12-com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues = null;
    /* renamed from: -com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues */
    private static final /* synthetic */ int[] f13-com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues = null;
    private final Map<AuthInfoID, Set<AuthParam>> mAuthParams;
    private final EAPMethodID mEAPMethodID;

    /* renamed from: -getcom-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues */
    private static /* synthetic */ int[] m12xd686309e() {
        if (f12-com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues != null) {
            return f12-com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues;
        }
        int[] iArr = new int[AuthInfoID.values().length];
        try {
            iArr[AuthInfoID.CredentialType.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AuthInfoID.ExpandedEAPMethod.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AuthInfoID.ExpandedInnerEAPMethod.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AuthInfoID.InnerAuthEAPMethodType.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AuthInfoID.NonEAPInnerAuthType.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AuthInfoID.TunneledEAPMethodCredType.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AuthInfoID.Undefined.ordinal()] = 13;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[AuthInfoID.VendorSpecific.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        f12-com-android-server-wifi-anqp-eap-EAP$AuthInfoIDSwitchesValues = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues */
    private static /* synthetic */ int[] m13x55a4ef8b() {
        if (f13-com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues != null) {
            return f13-com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues;
        }
        int[] iArr = new int[EAPMethodID.values().length];
        try {
            iArr[EAPMethodID.EAP_3Com.ordinal()] = 13;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[EAPMethodID.EAP_AKA.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[EAPMethodID.EAP_AKAPrim.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[EAPMethodID.EAP_ActiontecWireless.ordinal()] = 14;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[EAPMethodID.EAP_EKE.ordinal()] = 15;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[EAPMethodID.EAP_FAST.ordinal()] = 16;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[EAPMethodID.EAP_GPSK.ordinal()] = 17;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[EAPMethodID.EAP_HTTPDigest.ordinal()] = 18;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[EAPMethodID.EAP_IKEv2.ordinal()] = 19;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[EAPMethodID.EAP_KEA.ordinal()] = 20;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[EAPMethodID.EAP_KEA_VALIDATE.ordinal()] = 21;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[EAPMethodID.EAP_LEAP.ordinal()] = 22;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[EAPMethodID.EAP_Link.ordinal()] = 23;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[EAPMethodID.EAP_MD5.ordinal()] = 24;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[EAPMethodID.EAP_MOBAC.ordinal()] = 25;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[EAPMethodID.EAP_MSCHAPv2.ordinal()] = 26;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[EAPMethodID.EAP_OTP.ordinal()] = 27;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[EAPMethodID.EAP_PAX.ordinal()] = 28;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[EAPMethodID.EAP_PEAP.ordinal()] = 29;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[EAPMethodID.EAP_POTP.ordinal()] = 30;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[EAPMethodID.EAP_PSK.ordinal()] = 31;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[EAPMethodID.EAP_PWD.ordinal()] = 32;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[EAPMethodID.EAP_RSA.ordinal()] = 33;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[EAPMethodID.EAP_SAKE.ordinal()] = 34;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[EAPMethodID.EAP_SIM.ordinal()] = 3;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[EAPMethodID.EAP_SPEKE.ordinal()] = 35;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[EAPMethodID.EAP_TEAP.ordinal()] = 36;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[EAPMethodID.EAP_TLS.ordinal()] = 4;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[EAPMethodID.EAP_TTLS.ordinal()] = 5;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[EAPMethodID.EAP_ZLXEAP.ordinal()] = 37;
        } catch (NoSuchFieldError e30) {
        }
        f13-com-android-server-wifi-anqp-eap-EAP$EAPMethodIDSwitchesValues = iArr;
        return iArr;
    }

    public EAPMethod(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() < 3) {
            throw new ProtocolException("Runt EAP Method: " + payload.remaining());
        }
        int length = payload.get() & 255;
        int count = payload.get() & 255;
        this.mEAPMethodID = EAP.mapEAPMethod(payload.get() & 255);
        this.mAuthParams = new EnumMap(AuthInfoID.class);
        int realCount = 0;
        ByteBuffer paramPayload = payload.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        paramPayload.limit((paramPayload.position() + length) - 2);
        payload.position((payload.position() + length) - 2);
        while (paramPayload.hasRemaining()) {
            int id = paramPayload.get() & 255;
            AuthInfoID authInfoID = EAP.mapAuthMethod(id);
            if (authInfoID == null) {
                throw new ProtocolException("Unknown auth parameter ID: " + id);
            }
            int len = paramPayload.get() & 255;
            if (len == 0 || len > paramPayload.remaining()) {
                throw new ProtocolException("Bad auth method length: " + len);
            }
            switch (m12xd686309e()[authInfoID.ordinal()]) {
                case 1:
                    addAuthParam(new Credential(authInfoID, len, paramPayload));
                    break;
                case 2:
                    addAuthParam(new ExpandedEAPMethod(authInfoID, len, paramPayload));
                    break;
                case 3:
                    addAuthParam(new ExpandedEAPMethod(authInfoID, len, paramPayload));
                    break;
                case 4:
                    addAuthParam(new InnerAuthEAP(len, paramPayload));
                    break;
                case 5:
                    addAuthParam(new NonEAPInnerAuth(len, paramPayload));
                    break;
                case 6:
                    addAuthParam(new Credential(authInfoID, len, paramPayload));
                    break;
                case 7:
                    addAuthParam(new VendorSpecificAuth(len, paramPayload));
                    break;
                default:
                    break;
            }
            realCount++;
        }
        if (realCount != count) {
            throw new ProtocolException("Invalid parameter count: " + realCount + ", expected " + count);
        }
    }

    public EAPMethod(EAPMethodID eapMethodID, AuthParam authParam) {
        this.mEAPMethodID = eapMethodID;
        this.mAuthParams = new HashMap(1);
        if (authParam != null) {
            Set<AuthParam> authParams = new HashSet();
            authParams.add(authParam);
            this.mAuthParams.put(authParam.getAuthInfoID(), authParams);
        }
    }

    private void addAuthParam(AuthParam param) {
        Set<AuthParam> authParams = (Set) this.mAuthParams.get(param.getAuthInfoID());
        if (authParams == null) {
            authParams = new HashSet();
            this.mAuthParams.put(param.getAuthInfoID(), authParams);
        }
        authParams.add(param);
    }

    public Map<AuthInfoID, Set<AuthParam>> getAuthParams() {
        return Collections.unmodifiableMap(this.mAuthParams);
    }

    public EAPMethodID getEAPMethodID() {
        return this.mEAPMethodID;
    }

    public int match(Credential credential) {
        EAPMethod credMethod = credential.getEAPMethod();
        if (this.mEAPMethodID != credMethod.getEAPMethodID()) {
            return -1;
        }
        switch (m13x55a4ef8b()[this.mEAPMethodID.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                if (this.mAuthParams.isEmpty()) {
                    return 2;
                }
                int paramCount = 0;
                for (Entry<AuthInfoID, Set<AuthParam>> entry : credMethod.getAuthParams().entrySet()) {
                    Set<AuthParam> params = (Set) this.mAuthParams.get(entry.getKey());
                    if (params != null) {
                        if (!Collections.disjoint(params, (Collection) entry.getValue())) {
                            return 3;
                        }
                        paramCount += params.size();
                    }
                }
                return paramCount > 0 ? -1 : 2;
            default:
                return 2;
        }
    }

    public AuthParam getAuthParam() {
        if (this.mAuthParams.isEmpty()) {
            return null;
        }
        Set<AuthParam> params = (Set) this.mAuthParams.values().iterator().next();
        if (params.isEmpty()) {
            return null;
        }
        return (AuthParam) params.iterator().next();
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        EAPMethod that = (EAPMethod) thatObject;
        if (this.mEAPMethodID == that.mEAPMethodID) {
            z = this.mAuthParams.equals(that.mAuthParams);
        }
        return z;
    }

    public int hashCode() {
        return (this.mEAPMethodID.hashCode() * 31) + this.mAuthParams.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EAP Method ").append(this.mEAPMethodID).append(10);
        for (Set<AuthParam> paramSet : this.mAuthParams.values()) {
            for (AuthParam param : paramSet) {
                sb.append("      ").append(param.toString());
            }
        }
        return sb.toString();
    }
}

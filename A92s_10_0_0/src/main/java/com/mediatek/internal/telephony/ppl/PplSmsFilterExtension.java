package com.mediatek.internal.telephony.ppl;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import java.util.ArrayList;
import java.util.List;
import mediatek.telephony.MtkSmsMessage;
import vendor.mediatek.hardware.pplagent.V1_0.IPplAgent;

public class PplSmsFilterExtension extends ContextWrapper implements IPplSmsFilter {
    public static final String INSTRUCTION_KEY_FROM = "From";
    public static final String INSTRUCTION_KEY_SIM_ID = "SimId";
    public static final String INSTRUCTION_KEY_TO = "To";
    public static final String INSTRUCTION_KEY_TYPE = "Type";
    public static final String INTENT_REMOTE_INSTRUCTION_RECEIVED = "com.mediatek.ppl.REMOTE_INSTRUCTION_RECEIVED";
    private static final String TAG = "PPL/PplSmsFilterExtension";
    public static final boolean USER_LOAD = TextUtils.equals(Build.TYPE, DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER);
    private IPplAgent mAgent;
    private final boolean mEnabled;
    private final PplMessageManager mMessageManager;

    public PplSmsFilterExtension(Context context) {
        super(context);
        Log.d(TAG, "PplSmsFilterExtension enter");
        if (!"1".equals(SystemProperties.get("ro.vendor.mtk_privacy_protection_lock"))) {
            this.mAgent = null;
            this.mMessageManager = null;
            this.mEnabled = false;
            return;
        }
        try {
            this.mAgent = IPplAgent.getService();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get PPLAgent", e);
        }
        if (this.mAgent == null) {
            Log.e(TAG, "mAgent is null!");
            this.mMessageManager = null;
            this.mEnabled = false;
            return;
        }
        this.mMessageManager = new PplMessageManager(context);
        this.mEnabled = true;
        Log.d(TAG, "PplSmsFilterExtension exit");
    }

    private void convertArrayListToByteArray(ArrayList<Byte> in, byte[] out) {
        int i = 0;
        while (i < in.size() && i < out.length) {
            out[i] = in.get(i).byteValue();
            i++;
        }
    }

    public byte[] readControlData() {
        IPplAgent iPplAgent = this.mAgent;
        if (iPplAgent == null) {
            Log.e(TAG, "[writeControlData] mAgent is null !!!");
            return null;
        }
        try {
            ArrayList<Byte> data = iPplAgent.readControlData();
            byte[] buff = new byte[data.size()];
            convertArrayListToByteArray(data, buff);
            return buff;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* JADX INFO: Multiple debug info for r14v11 int: [D('i' int), D('pduCount' int)] */
    @Override // com.mediatek.internal.telephony.ppl.IPplSmsFilter
    public boolean pplFilter(Bundle params) {
        String content;
        String src;
        String dst;
        boolean z;
        boolean z2 = false;
        if (!this.mEnabled) {
            Log.d(TAG, "pplFilter returns false: feature not enabled");
            return false;
        }
        String format = params.getString(IPplSmsFilter.KEY_FORMAT);
        boolean isMO = params.getInt(IPplSmsFilter.KEY_SMS_TYPE) == 1;
        int subId = params.getInt(IPplSmsFilter.KEY_SUB_ID);
        int simId = SubscriptionManager.getSlotIndex(subId);
        Log.d(TAG, "pplFilter: subId = " + subId + ". simId = " + simId);
        Object[] messages = (Object[]) params.getSerializable(IPplSmsFilter.KEY_PDUS);
        if (messages == null) {
            content = params.getString(IPplSmsFilter.KEY_MSG_CONTENT);
            src = params.getString(IPplSmsFilter.KEY_SRC_ADDR);
            dst = params.getString(IPplSmsFilter.KEY_DST_ADDR);
            Log.d(TAG, "pplFilter: Read msg directly:" + stringForSecureSms(content));
        } else {
            byte[][] pdus = new byte[messages.length][];
            for (int i = 0; i < messages.length; i++) {
                pdus[i] = (byte[]) messages[i];
            }
            int pduCount = pdus.length;
            if (pduCount > 1) {
                Log.d(TAG, "pplFilter return false: ppl sms is short msg, count should <= 1 ");
                return false;
            }
            MtkSmsMessage[] msgs = new MtkSmsMessage[pduCount];
            for (int i2 = 0; i2 < pduCount; i2++) {
                msgs[i2] = MtkSmsMessage.createFromPdu(pdus[i2], format);
            }
            Log.d(TAG, "pplFilter: pdus length " + pdus.length);
            if (msgs[0] == null) {
                Log.d(TAG, "pplFilter returns false: message is null");
                return false;
            }
            content = msgs[0].getMessageBody();
            Log.d(TAG, "pplFilter: message content is " + stringForSecureSms(content));
            z2 = false;
            src = msgs[0].getOriginatingAddress();
            dst = msgs[0].getDestinationAddress();
        }
        if (content == null) {
            Log.d(TAG, "pplFilter returns false: content is null");
            return z2;
        }
        PplControlData controlData = PplControlData.buildControlData(readControlData());
        if (controlData == null) {
            z = false;
        } else if (!controlData.isEnabled()) {
            z = false;
        } else {
            if (isMO) {
                Log.d(TAG, "pplFilter: dst is " + stringForSecureNumber(dst));
                if (!matchNumber(dst, controlData.TrustedNumberList)) {
                    Log.d(TAG, "pplFilter returns false: MO number does not match");
                    return false;
                }
            } else {
                Log.d(TAG, "pplFilter: src is " + stringForSecureNumber(src));
                if (!matchNumber(src, controlData.TrustedNumberList)) {
                    Log.d(TAG, "pplFilter returns false: MT number does not match");
                    return false;
                }
            }
            byte instruction = this.mMessageManager.getMessageType(content);
            if (instruction == -1) {
                Log.d(TAG, "pplFilter returns false: message is not matched");
                return false;
            }
            if (isMO) {
                if (instruction == 1 || instruction == 3 || instruction == 5 || instruction == 7) {
                    Log.d(TAG, "pplFilter returns false: ignore MO command: " + ((int) instruction));
                    return false;
                }
            } else if (instruction == 0 || instruction == 2 || instruction == 4 || instruction == 6 || instruction == 8 || instruction == 9 || instruction == 10 || instruction == 11) {
                Log.d(TAG, "pplFilter returns false: ignore MT command: " + ((int) instruction));
                return false;
            }
            Intent intent = new Intent(INTENT_REMOTE_INSTRUCTION_RECEIVED);
            intent.setClassName("com.mediatek.ppl", "com.mediatek.ppl.PplService");
            intent.putExtra(INSTRUCTION_KEY_TYPE, instruction);
            intent.putExtra(INSTRUCTION_KEY_SIM_ID, simId);
            if (isMO) {
                intent.putExtra(INSTRUCTION_KEY_TO, dst);
            } else {
                intent.putExtra(INSTRUCTION_KEY_FROM, src);
            }
            Log.d(TAG, "start PPL Service");
            startService(intent);
            return true;
        }
        Log.d(TAG, "pplFilter returns false: control data is null or ppl is not enabled");
        return z;
    }

    private boolean matchNumber(String number, List<String> numbers) {
        if (number == null || numbers == null) {
            return false;
        }
        for (String s : numbers) {
            if (PhoneNumberUtils.compare(s, number)) {
                return true;
            }
        }
        return false;
    }

    private String stringForSecureNumber(String data) {
        if (USER_LOAD || TextUtils.isEmpty(data)) {
            return "";
        }
        int length = data.length();
        if (data.length() >= 11) {
            return "*******" + data.substring(7, length);
        } else if (length <= 4) {
            return data.replaceAll("\\w", "*");
        } else {
            return "***" + data.substring(3, length);
        }
    }

    public static String stringForSecureSms(String data) {
        if (USER_LOAD || TextUtils.isEmpty(data)) {
            return "";
        }
        if (data.length() < 6) {
            return data.replaceAll("\\w", "*");
        }
        return data.substring(0, 5) + "......";
    }
}

package com.oppo.internal.telephony.rus;

import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.util.XmlUtils;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class RusUpdatePplmn extends RusBase {
    private static int PROJECT_SIM_NUM = TelephonyManager.getDefault().getSimCount();
    private static final String TAG = "RusUpdatePplmn";
    private String mPplmn_List = null;

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public boolean parseRusXML(XmlPullParser parser, HashMap<String, String> rusData) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return true;
        }
        try {
            if ("cmcc_plmn".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                if ("pplmn_list".equals(parser.getName())) {
                    rusData.put("cmcc_plmn", parser.getAttributeValue(null, "plmn"));
                }
            } else if ("cu_plmn".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                if ("pplmn_list".equals(parser.getName())) {
                    rusData.put("cu_plmn", parser.getAttributeValue(null, "plmn"));
                }
            } else if ("ct_plmn".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                if ("pplmn_list".equals(parser.getName())) {
                    rusData.put("ct_plmn", parser.getAttributeValue(null, "plmn"));
                }
            }
            return true;
        } catch (XmlPullParserException e) {
            printLog(TAG, e.toString());
            return true;
        } catch (Exception e2) {
            printLog(TAG, e2.toString());
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",cmcc_plmn:" + rusData.get("cmcc_plmn") + ",cu_plmn:" + rusData.get("cu_plmn") + ",ct_plmn:" + rusData.get("ct_plmn"));
        if (rusData.containsKey("cmcc_plmn")) {
            sendAtCommandForPplmn(rusData.get("cmcc_plmn"));
        }
        if (rusData.containsKey("cu_plmn")) {
            sendAtCommandForPplmn(rusData.get("cu_plmn"));
        }
        if (rusData.containsKey("ct_plmn")) {
            sendAtCommandForPplmn(rusData.get("ct_plmn"));
        }
    }

    private void sendAtCommandForPplmn(String pplmn_list) {
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            try {
                printLog(TAG, "phone id:=" + i);
                this.sProxyPhones[i].invokeOemRilRequestStrings(new String[]{"AT+EPOL=" + pplmn_list, ""}, (Message) null);
            } catch (Exception e) {
                printLog(TAG, "hanlder doNVwrite wrong");
                e.printStackTrace();
                return;
            }
        }
    }
}

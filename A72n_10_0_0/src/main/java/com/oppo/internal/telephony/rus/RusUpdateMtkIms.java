package com.oppo.internal.telephony.rus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.imsphone.AbstractImsPhone;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.imsphone.OppoImsDatabaseHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class RusUpdateMtkIms extends RusBase {
    private static final String ATTR_OP = "op";
    private static final String KEY_IS_CONFIG_READY = "rus_mtk_ims_is_config_ready";
    private static final String KEY_LAST_OP = "rus_mtk_ims_last_op";
    private static final String START_MD_CONFIG_TAG = "md_config";
    private static final String TAG = "RusUpdateMtkIms";
    private static Context mContext;

    public RusUpdateMtkIms() {
        if (this.mPhone != null) {
            mContext = this.mPhone.getContext();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public boolean parseRusXML(XmlPullParser parser, HashMap<String, String> rusData) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
            return true;
        }
        try {
            printLog(TAG, "parser.getName() : " + parser.getName());
            if (START_MD_CONFIG_TAG.equals(parser.getName())) {
                if (parser.getAttributeCount() <= 0 || !ATTR_OP.equals(parser.getAttributeName(0))) {
                    PersistableBundle config = PersistableBundle.restoreFromXml(parser);
                    if (config != null) {
                        printLog(TAG, "config for reset : " + config.toString());
                        rusData.put("reset", config.getBoolean("reset", true) ? "1" : "0");
                    }
                } else {
                    String op = parser.getAttributeValue(0);
                    String module = parser.getAttributeValue(1);
                    PersistableBundle config2 = PersistableBundle.restoreFromXml(parser);
                    printLog(TAG, "config : " + config2.toString());
                    for (String name : config2.keySet()) {
                        String value = config2.getString(name);
                        String key = "md_config|" + op + "|" + module + "|" + name;
                        rusData.put(key, value);
                        printLog(TAG, "updateConfig key : " + key + ", value : " + value);
                    }
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
        printLog(TAG, "executeRusCommand() isReboot:" + isReboot);
        boolean needReset = false;
        boolean isNeedUpdate = false;
        Iterator<Map.Entry<String, String>> iter = rusData.entrySet().iterator();
        List<String> configList = new ArrayList<>();
        while (true) {
            boolean z = true;
            if (!iter.hasNext()) {
                break;
            }
            Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            String[] item = key.split("\\|");
            printLog(TAG, "executeRusCommand key : " + key + ", value : " + value + ",item.length:" + item.length);
            if (item.length == 4) {
                String op = item[1];
                String module = item[2];
                String name = item[3];
                if (!configList.contains(op)) {
                    OppoImsDatabaseHelper.getDbHelper(mContext).deleteConfig(op, module);
                    configList.add(op);
                }
                printLog(TAG, "executeRusCommand op : " + op + ", name : " + name + ",module : " + module + ", value : " + value);
                OppoImsDatabaseHelper.getDbHelper(mContext).updateConfig(op, name, value, module);
                isNeedUpdate = true;
            } else if (item.length == 1) {
                if (rusData.get("reset") != "1") {
                    z = false;
                }
                needReset = z;
            }
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!isNeedUpdate || sharedPrefs == null) {
            printLog(TAG, "no need to update bcz empty paramter");
            return;
        }
        sharedPrefs.edit().putBoolean(KEY_IS_CONFIG_READY, true).apply();
        TelephonyManager tm = TelephonyManager.getDefault();
        for (int i = 0; i < tm.getPhoneCount(); i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null) {
                SharedPreferences.Editor edit = sharedPrefs.edit();
                edit.putString(KEY_LAST_OP + i, "").apply();
                ((AbstractImsPhone) OemTelephonyUtils.typeCasting(AbstractImsPhone.class, phone.getImsPhone())).setRusConfig(needReset);
            }
        }
    }
}

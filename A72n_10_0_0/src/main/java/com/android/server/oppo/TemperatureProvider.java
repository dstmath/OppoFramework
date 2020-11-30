package com.android.server.oppo;

import android.os.ConditionVariable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;

public class TemperatureProvider {
    private static final int BLOCK_TIME_OUT = 1000;
    private static final String DIVIDER = "_";
    public static final String HIGH_TEMPERATURE_DISABLE_FLASH_CHARGE_LIMIT = "HighTemperatureDisableFlashChargeLimit";
    public static final int HIGH_TEMPERATURE_DISABLE_FLASH_COUNTDOWN_TIME = 30;
    public static final String HIGH_TEMPERATURE_DISABLE_FLASH_COUNT_TIME = "HighTemperatureDisableFlashCountTime";
    public static final String HIGH_TEMPERATURE_DISABLE_FLASH_LIMIT = "HighTemperatureDisableFlashLimit";
    public static final String HIGH_TEMPERATURE_DISABLE_FLASH_SWITCH = "HighTemperatureDisableFlashSwitch";
    public static final int HIGH_TEMPERATURE_THRESHOLD = 420;
    public static final String SWITCH_OFF = "false";
    public static final String SWITCH_ON = "true";
    private static final String TAG = "TemperatureProvider";
    private static final String TEMPERATURE_ROOT_PATH = "oppo_version/etc/temperature_profile/sys_high_temp_protect_";
    private static final String XML_PARENT_TAG = "filter-conf";
    private static String sManufacturer = null;
    private final ConditionVariable mSig = new ConditionVariable();
    private HashMap<String, String> mTemperatureMap = new HashMap<>();

    public void initTemperatureParams() {
        this.mSig.close();
        new Thread() {
            /* class com.android.server.oppo.TemperatureProvider.AnonymousClass1 */

            public void run() {
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(TemperatureProvider.TEMPERATURE_ROOT_PATH + TemperatureProvider.this.getManufacturer() + TemperatureProvider.DIVIDER + TemperatureProvider.this.readPrjName() + ".xml");
                    TemperatureProvider.this.parseXml(inputStream);
                    try {
                        inputStream.close();
                    } catch (Exception t) {
                        Slog.e(TemperatureProvider.TAG, "initTemperatureParams, close inputStream error.", t);
                    }
                } catch (Exception e) {
                    Slog.e(TemperatureProvider.TAG, "initTemperatureParams, occur error.", e);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception t2) {
                            Slog.e(TemperatureProvider.TAG, "initTemperatureParams, close inputStream error.", t2);
                        }
                    }
                    throw th;
                }
                TemperatureProvider.this.mSig.open();
            }
        }.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parseXml(InputStream inputStream) {
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, "utf-8");
            for (int event = xmlParser.getEventType(); 1 != event; event = xmlParser.next()) {
                if (2 == event && !xmlParser.getName().equals(XML_PARENT_TAG)) {
                    String text = xmlParser.nextText();
                    if (xmlParser.getEventType() != 3) {
                        xmlParser.nextTag();
                    }
                    this.mTemperatureMap.put(xmlParser.getName(), text);
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "parseXml, occur error.", e);
        }
    }

    /* access modifiers changed from: protected */
    public int getTemperatureLimit() {
        String limit = this.mTemperatureMap.get(HIGH_TEMPERATURE_DISABLE_FLASH_LIMIT);
        return TextUtils.isEmpty(limit) ? HIGH_TEMPERATURE_THRESHOLD : Integer.parseInt(limit);
    }

    /* access modifiers changed from: protected */
    public int getTemperatureChargeLimit() {
        String limit = this.mTemperatureMap.get(HIGH_TEMPERATURE_DISABLE_FLASH_CHARGE_LIMIT);
        return TextUtils.isEmpty(limit) ? HIGH_TEMPERATURE_THRESHOLD : Integer.parseInt(limit);
    }

    /* access modifiers changed from: protected */
    public int getTemperatureCountDownTime() {
        String time = this.mTemperatureMap.get(HIGH_TEMPERATURE_DISABLE_FLASH_COUNT_TIME);
        if (TextUtils.isEmpty(time)) {
            return 30;
        }
        return Integer.parseInt(time);
    }

    /* access modifiers changed from: protected */
    public boolean isTemperatureSwitchOn() {
        this.mSig.block(1000);
        String temperatureSwitch = this.mTemperatureMap.get(HIGH_TEMPERATURE_DISABLE_FLASH_SWITCH);
        return temperatureSwitch == null || SWITCH_ON.equals(temperatureSwitch);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getManufacturer() {
        String str = sManufacturer;
        if (str != null) {
            return str;
        }
        sManufacturer = SystemProperties.get("ro.product.manufacturer", "");
        return sManufacturer;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String readPrjName() {
        String prjName = null;
        try {
            prjName = SystemProperties.get("ro.separate.soft", "");
        } catch (Exception e) {
            Slog.e(TAG, "readPrjName, occur error. " + ((String) null), e);
        }
        if (!TextUtils.isEmpty(prjName)) {
            return prjName.toLowerCase().replaceAll(StringUtils.SPACE, "");
        }
        return prjName;
    }
}

package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.os.Environment;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VoiceMailConstants {
    static final String LOG_TAG = "VoiceMailConstants";
    static final int NAME = 0;
    static final int NUMBER = 1;
    static final String PARTNER_VOICEMAIL_PATH = "etc/voicemail-conf.xml";
    static final int SIZE = 3;
    static final int TAG = 2;
    private HashMap<String, String[]> CarrierVmMap = new HashMap<>();

    @UnsupportedAppUsage
    VoiceMailConstants() {
        loadVoiceMail();
    }

    public boolean containsCarrier(String carrier) {
        return this.CarrierVmMap.containsKey(carrier);
    }

    /* access modifiers changed from: package-private */
    public String getCarrierName(String carrier) {
        return this.CarrierVmMap.get(carrier)[0];
    }

    /* access modifiers changed from: package-private */
    public String getVoiceMailNumber(String carrier) {
        return this.CarrierVmMap.get(carrier)[1];
    }

    /* access modifiers changed from: package-private */
    public String getVoiceMailTag(String carrier) {
        return this.CarrierVmMap.get(carrier)[2];
    }

    private void loadVoiceMail() {
        try {
            FileReader vmReader = new FileReader(new File(Environment.getRootDirectory(), PARTNER_VOICEMAIL_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(vmReader);
                XmlUtils.beginDocument(parser, "voicemail");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"voicemail".equals(parser.getName())) {
                        try {
                            vmReader.close();
                            return;
                        } catch (IOException e) {
                            return;
                        }
                    } else {
                        this.CarrierVmMap.put(parser.getAttributeValue(null, "numeric"), new String[]{parser.getAttributeValue(null, "carrier"), parser.getAttributeValue(null, "vmnumber"), parser.getAttributeValue(null, "vmtag")});
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e2);
                vmReader.close();
            } catch (IOException e3) {
                Rlog.w(LOG_TAG, "Exception in Voicemail parser " + e3);
                vmReader.close();
            } catch (Throwable th) {
                try {
                    vmReader.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARTNER_VOICEMAIL_PATH);
        }
    }
}

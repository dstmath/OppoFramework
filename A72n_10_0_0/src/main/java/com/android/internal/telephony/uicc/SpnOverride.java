package com.android.internal.telephony.uicc;

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

public class SpnOverride {
    static final String LOG_TAG = "SpnOverride";
    protected static final String OEM_SPN_OVERRIDE_PATH = "telephony/spn-conf.xml";
    protected static final String PARTNER_SPN_OVERRIDE_PATH = "etc/spn-conf.xml";
    protected HashMap<String, String> mCarrierSpnMap = new HashMap<>();

    public SpnOverride() {
        loadSpnOverrides();
    }

    public boolean containsCarrier(String carrier) {
        return this.mCarrierSpnMap.containsKey(carrier);
    }

    public String getSpn(String carrier) {
        return this.mCarrierSpnMap.get(carrier);
    }

    /* access modifiers changed from: protected */
    public void loadSpnOverrides() {
        File spnFile = new File(Environment.getRootDirectory(), PARTNER_SPN_OVERRIDE_PATH);
        File oemSpnFile = new File(Environment.getOemDirectory(), OEM_SPN_OVERRIDE_PATH);
        if (oemSpnFile.exists()) {
            long oemSpnTime = oemSpnFile.lastModified();
            long sysSpnTime = spnFile.lastModified();
            Rlog.d(LOG_TAG, "SPN Timestamp: oemTime = " + oemSpnTime + " sysTime = " + sysSpnTime);
            if (oemSpnTime > sysSpnTime) {
                Rlog.d(LOG_TAG, "SPN in OEM image is newer than System image");
                spnFile = oemSpnFile;
            }
        } else {
            Rlog.d(LOG_TAG, "No SPN in OEM image = " + oemSpnFile.getPath() + " Load SPN from system image");
        }
        try {
            FileReader spnReader = new FileReader(spnFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "spnOverrides");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"spnOverride".equals(parser.getName())) {
                        spnReader.close();
                        return;
                    }
                    this.mCarrierSpnMap.put(parser.getAttributeValue(null, "numeric"), parser.getAttributeValue(null, "spn"));
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in spn-conf parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in spn-conf parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can not open " + spnFile.getAbsolutePath());
        }
    }
}

package android.telephony;

import android.util.Log;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OppoSpnOverride {
    private static final String SPN_DATAPATH = "/data/data/com.android.phone/";
    private static final String TAG = "OppoSpnOverride";
    private static final String cityLan = "lan_";
    static final Object sInstSync = new Object();
    private static OppoSpnOverride sInstance = null;
    private static final String spnname_postfix = ".xml";
    private static final String spnname_substr = "spn_";
    private String citylan_name = null;
    private HashMap<String, String> mCarrierSpnMap = new HashMap();

    private OppoSpnOverride(String lan_name) {
        this.citylan_name = lan_name;
        loadSpnOverrides(lan_name);
    }

    public static OppoSpnOverride getInstance(String lan_name) {
        synchronized (sInstSync) {
            if (sInstance == null || (lan_name.equals(sInstance.getLanName()) ^ 1) != 0) {
                sInstance = new OppoSpnOverride(lan_name);
            }
        }
        return sInstance;
    }

    public boolean containsCarrier(String carrier) {
        return this.mCarrierSpnMap.containsKey(carrier);
    }

    public String getSpn(String carrier) {
        return (String) this.mCarrierSpnMap.get(carrier);
    }

    private String getLanName() {
        return this.citylan_name;
    }

    private void loadSpnOverrides(String lan_name) {
        FileNotFoundException e;
        Exception e2;
        Throwable th;
        Log.d(TAG, "getXmlFile");
        FileReader confreader = null;
        File file = new File("/data/data/com.android.phone/spn_" + lan_name + spnname_postfix);
        Log.d(TAG, "mSpnInfoDataPath=/data/data/com.android.phone/spn_" + lan_name + spnname_postfix);
        if (file.exists()) {
            try {
                FileReader confreader2 = new FileReader(file);
                try {
                    XmlPullParser confparser = XmlPullParserFactory.newInstance().newPullParser();
                    confparser.setInput(confreader2);
                    if (confparser != null) {
                        XmlUtils.beginDocument(confparser, cityLan + lan_name);
                        XmlUtils.nextElement(confparser);
                        while (confparser.getEventType() != 1) {
                            getRow(confparser);
                            if (this.mCarrierSpnMap == null) {
                                throw new XmlPullParserException("Expected 'spn' tag", confparser, null);
                            }
                            XmlUtils.nextElement(confparser);
                        }
                    } else {
                        Log.d(TAG, "confparser==null");
                    }
                    if (confreader2 != null) {
                        try {
                            confreader2.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    confreader = confreader2;
                } catch (Exception e5) {
                    e2 = e5;
                    confreader = confreader2;
                } catch (Throwable th2) {
                    th = th2;
                    confreader = confreader2;
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                try {
                    Log.e(TAG, "getXmlFile file not found", e);
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e7) {
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e9) {
                e2 = e9;
                Log.e(TAG, "getXmlFile Exception while parsing", e2);
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e10) {
                    }
                }
            }
        }
    }

    private void getRow(XmlPullParser parser) {
        if (!"spnOverride".equals(parser.getName())) {
            Log.d(TAG, "spnOverride is not matched");
        }
        String numeric = parser.getAttributeValue(null, "numeric");
        String data = parser.getAttributeValue(null, "spn");
        Log.d(TAG, "numeric=" + numeric + "spn=" + data);
        this.mCarrierSpnMap.put(numeric, data);
    }
}

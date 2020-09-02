package com.android.server.wifi;

import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class RandomizedMacStoreData implements WifiConfigStore.StoreData {
    private static final String XML_TAG_MAC_MAP = "MacMapEntry";
    private static final String XML_TAG_SECTION_HEADER_MAC_ADDRESS_MAP = "MacAddressMap";
    private Map<String, String> mMacMapping;

    RandomizedMacStoreData() {
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        Map<String, String> map = this.mMacMapping;
        if (map != null) {
            XmlUtil.writeNextValue(out, XML_TAG_MAC_MAP, map);
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            while (!XmlUtil.isNextSectionEnd(in, outerTagDepth)) {
                String[] valueName = new String[1];
                Object value = XmlUtil.readCurrentValue(in, valueName);
                if (valueName[0] != null) {
                    String str = valueName[0];
                    char c = 65535;
                    if (str.hashCode() == -1138073915 && str.equals(XML_TAG_MAC_MAP)) {
                        c = 0;
                    }
                    if (c == 0) {
                        this.mMacMapping = (Map) value;
                    } else {
                        throw new XmlPullParserException("Unknown tag under MacAddressMap: " + valueName[0]);
                    }
                } else {
                    throw new XmlPullParserException("Missing value name");
                }
            }
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mMacMapping = null;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return true;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return XML_TAG_SECTION_HEADER_MAC_ADDRESS_MAP;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public int getStoreFileId() {
        return 0;
    }

    public Map<String, String> getMacMapping() {
        Map<String, String> map = this.mMacMapping;
        if (map == null) {
            return new HashMap();
        }
        return map;
    }

    public void setMacMapping(Map<String, String> macMapping) {
        this.mMacMapping = macMapping;
    }
}

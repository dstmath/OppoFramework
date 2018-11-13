package com.android.internal.telephony.oem.rus;

import android.os.FileUtils;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OemRFSettings;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class RusUpdateRFSettings extends RusBase {
    private static final String CONFIG_VALUE = "configs";
    private static final String FILE_PATH = "/data/data/com.android.phone/RFSettings.xml";
    private static final String ITEM_DEFAULT = "com.tencent.tmgp.sgame";
    private static final String ITEM_VALUE = "item";
    private static final String TAG = "RusUpdateRFSettings";
    private String mContent = null;

    public void execute() {
        printLog(TAG, "ValiateAndLoad RFSettings");
        try {
            this.mContent = getContent();
            if (this.mContent != null) {
                this.mContent = this.mContent.substring(this.mContent.indexOf("<RFSettings>"), this.mContent.indexOf("</RFSettings>") + "</RFSettings>".length());
                if (this.mFileUtils.saveToFile(this.mContent, FILE_PATH).exists()) {
                    process(false);
                } else {
                    printLog(TAG, "RFSettings save error");
                    return;
                }
            }
            printLog(TAG, "the romupdate database data is null");
        } catch (Exception e) {
            printLog(TAG, "Exception while parsing " + e);
        }
        return;
    }

    /* JADX WARNING: Missing block: B:4:0x000e, code:
            if (r26.mContent == null) goto L_0x0010;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void process(boolean isBoot) {
        boolean mSwitch = true;
        int maxTimer = 0;
        int maxTimes = 0;
        ArrayList<String> mList = new ArrayList();
        if (!isBoot) {
            try {
            } catch (Exception e) {
                printLog(TAG, "Exception executeRFSettings " + e.getMessage());
                mSwitch = true;
                mList.clear();
                mList.add(ITEM_DEFAULT);
            }
        }
        File file = new File(FILE_PATH);
        if (file.exists()) {
            this.mContent = FileUtils.readTextFile(file, 0, (String) null);
        }
        if (this.mContent == null) {
            mSwitch = true;
            mList.add(ITEM_DEFAULT);
        } else {
            try {
                Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(this.mContent.getBytes())).getDocumentElement();
                NodeList switchItem = root.getElementsByTagName(CONFIG_VALUE);
                if (switchItem.getLength() > 0) {
                    Element element = (Element) switchItem.item(0);
                    String txt = element.getAttribute("switch");
                    if (txt.length() > 0) {
                        mSwitch = "0".equals(txt) ^ 1;
                    }
                    txt = element.getAttribute("maxtimer");
                    if (txt.length() > 0) {
                        maxTimer = Integer.valueOf(txt).intValue();
                    }
                    txt = element.getAttribute("maxtimes");
                    if (txt.length() > 0) {
                        maxTimes = Integer.valueOf(txt).intValue();
                    }
                }
                NodeList items = root.getElementsByTagName(ITEM_VALUE);
                int len = items.getLength();
                for (int i = 0; i < len; i++) {
                    mList.add(((Element) items.item(i)).getTextContent());
                }
            } catch (ParserConfigurationException e2) {
                printLog(TAG, "Exception executeRFSettings 1 " + e2.getMessage());
            } catch (SAXException e3) {
                printLog(TAG, "Exception executeRFSettings 2 " + e3.getMessage());
            } catch (IOException e4) {
                printLog(TAG, "Exception executeRFSettings 3 " + e4.getMessage());
            } catch (Exception e5) {
                printLog(TAG, "Exception executeRFSettings " + e5.getMessage());
            }
        }
        startSetting(isBoot, mSwitch, maxTimer, maxTimes, mList);
    }

    private void startSetting(boolean isBoot, boolean isSwitch, int maxTimer, int maxTimes, ArrayList<String> list) {
        if (OemConstant.SWITCH_LOG && list != null) {
            printLog(TAG, "isSwitch:" + isSwitch + ",maxTimer:" + maxTimer + ",maxTimes:" + maxTimes);
            for (int i = list.size() - 1; i >= 0; i--) {
                printLog(TAG, "item:" + ((String) list.get(i)));
            }
        }
        OemRFSettings.sIsSwitch = isSwitch;
        if (!isBoot || isSwitch) {
            OemRFSettings rfSettings = OemRFSettings.getDefault(this.mPhone.getContext());
            rfSettings.setInitValue(maxTimer, maxTimes);
            if (isBoot) {
                rfSettings.restore(this.mPhone.getContext());
            }
            if (isSwitch) {
                rfSettings.setGameList(list);
                rfSettings.register();
            } else {
                rfSettings.unregister();
            }
        }
    }
}

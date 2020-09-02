package com.oppo.internal.telephony.rus;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class RusBase extends Handler {
    private static final String TAG = "RusBase";
    protected static final int UPDATE_EXE_OTA_COMMAND = 22;
    protected static final int UPDATE_EXE_REBOOT_COMMAND = 21;
    protected static final int UPDATE_EXE_RUS_COMMAND = 20;
    private static final boolean VDBG = false;
    protected static final HandlerThread mHandlerThread = new HandlerThread("OemRUS", 10);
    protected FileUtils mFileUtils;
    protected boolean mOtaRemoveRus;
    protected Phone mPhone;
    protected boolean mRebootExecute;
    protected HashMap<String, String> mRusData;
    protected String mRusDataTag;
    protected RusServerHelper mRusServerHelp;
    protected Phone[] sProxyPhones;

    /* access modifiers changed from: protected */
    public abstract void executeRusCommand(HashMap<String, String> hashMap, boolean z);

    static {
        mHandlerThread.start();
    }

    public RusBase() {
        super(mHandlerThread.getLooper(), null, true);
        this.mRebootExecute = false;
        this.mOtaRemoveRus = false;
        this.mRusData = null;
        this.mRusDataTag = "";
        this.mFileUtils = null;
        this.mPhone = null;
        this.sProxyPhones = null;
        this.mRusServerHelp = null;
        this.mFileUtils = new FileUtils();
        this.mRusServerHelp = new RusServerHelper();
        this.mPhone = PhoneFactory.getDefaultPhone();
        this.sProxyPhones = PhoneFactory.getPhones();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 20:
                printLog(TAG, "UPDATE_EXE_RUS_COMMAND");
                executeRusCommand(this.mRusData, false);
                return;
            case 21:
                printLog(TAG, "UPDATE_EXE_REBOOT_COMMAND");
                executeRusCommand(this.mRusData, true);
                return;
            case 22:
                printLog(TAG, "UPDATE_EXE_OTA_COMMAND");
                executeOtaRemoveRusCommand(this.mRusData);
                return;
            default:
                printLog(TAG, "error ,hava some error network");
                return;
        }
    }

    public void executeBaseRusAction(String rusDataTag, String rusVer) {
        String rusdatafilename = rusDataTag + ".xml";
        this.mRusDataTag = rusDataTag;
        if (initRusData(rusdatafilename)) {
            Message msg = Message.obtain();
            msg.what = 20;
            sendMessage(msg);
        }
    }

    public void executeBaseRebootAction(String rusDataTag, String rusVer) {
        String rusdatafilename = rusDataTag + ".xml";
        this.mRusDataTag = rusDataTag;
        if (initRusData(rusdatafilename)) {
            Message msg = Message.obtain();
            String newOtaVersion = this.mRusServerHelp.getNewOtaVer();
            String oldOtaVersion = this.mRusServerHelp.getOldOtaVer();
            printLog(TAG, "newOtaVersion = " + newOtaVersion + ",oldOtaVersion = " + oldOtaVersion + ",mOtaRemoveRus = " + this.mOtaRemoveRus + ",mRebootExecute = " + this.mRebootExecute);
            if (oldOtaVersion != null && !oldOtaVersion.equals(newOtaVersion) && this.mOtaRemoveRus) {
                this.mRusServerHelp.deleteExistFile(rusdatafilename);
                msg.what = 22;
                sendMessage(msg);
            } else if (this.mRebootExecute) {
                msg.what = 21;
                sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void executeOtaRemoveRusCommand(HashMap<String, String> hashMap) {
    }

    /* access modifiers changed from: protected */
    public boolean parseRusXML(XmlPullParser parser, HashMap<String, String> rusData) {
        String name = parser.getName();
        try {
            if (parser.getAttributeCount() > 0) {
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    printDebugLog(TAG, "parseRusXML name = " + name + ",attrname = " + parser.getAttributeName(i) + ",attrvalue= " + parser.getAttributeValue(i));
                    rusData.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                }
            } else {
                int eventType = parser.next();
                printDebugLog(TAG, "parseRusXML newitem = " + parser.getName() + ",eventType= " + eventType);
                if (eventType != 2) {
                    if (eventType != 1) {
                        if (eventType == 4) {
                            printDebugLog(TAG, "parseRusXML newitem isWhitespace= " + parser.isWhitespace());
                            if (!parser.isWhitespace()) {
                                printDebugLog(TAG, "parseRusXML newitem value= " + parser.getText());
                                rusData.put(name, parser.getText());
                            }
                        }
                    }
                }
                return false;
            }
            return true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            printLog(TAG, "XmlPullParserException name = " + name);
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
            printLog(TAG, "IOException name = " + name);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean initRusData(String rusDataFileName) {
        String rusData = this.mRusServerHelp.readRusDataFromXml(rusDataFileName);
        boolean results = false;
        if (rusData == null) {
            printLog(TAG, "initRusData:rusData=null");
            return false;
        }
        this.mRusData = new HashMap<>();
        printLog(TAG, "initRusData rusData = " + rusData);
        try {
            XmlPullParser strparser = this.mRusServerHelp.getXmlParser(rusData, "");
            if (strparser != null) {
                int eventType = strparser.next();
                while (eventType != 1) {
                    printDebugLog(TAG, "initRusData eventType=" + eventType + ",getName=" + strparser.getName() + ",getText=" + strparser.getText());
                    if (eventType != 2) {
                        results = true;
                    } else {
                        results = parseRusXML(strparser, this.mRusData);
                    }
                    if (results) {
                        eventType = strparser.next();
                    }
                }
            }
            printRusData();
            return results;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCurrentXmlExist() {
        String rusData = this.mRusServerHelp.readRusDataFromXml(this.mRusDataTag + ".xml");
        if (rusData == null || rusData.isEmpty()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void printRusData() {
        HashMap<String, String> hashMap = this.mRusData;
        if (hashMap == null) {
            printLog(TAG, "defaultValue is null ");
            return;
        }
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            printDebugLog(TAG, "printRusData()mRusData key=" + entry.getKey() + ",value=" + entry.getValue());
        }
    }

    /* access modifiers changed from: protected */
    public void printLog(String tag, String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(tag + "-" + this.mRusDataTag, msg);
        }
    }

    /* access modifiers changed from: protected */
    public void printDebugLog(String tag, String msg) {
    }
}

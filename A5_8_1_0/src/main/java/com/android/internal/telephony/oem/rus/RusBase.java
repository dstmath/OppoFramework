package com.android.internal.telephony.oem.rus;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.util.XmlUtils;
import java.io.FileReader;
import org.xmlpull.v1.XmlPullParser;

public abstract class RusBase extends Handler {
    protected static final int ONSUCCEED_WRITE = 36;
    private static final String TAG = "RusBase";
    protected static final int UPDATE_CONTENT_SUCCESS = 21;
    protected static final int UPDATE_VER_READ_ERROR = 20;
    protected static final HandlerThread mHandlerThread = new HandlerThread("OemRUS", 10);
    protected FileUtils mFileUtils;
    protected String mPath;
    protected Phone mPhone;
    protected String mProviderContent;

    public abstract void execute();

    static {
        mHandlerThread.start();
    }

    public RusBase() {
        super(mHandlerThread.getLooper(), null, true);
        this.mPath = null;
        this.mProviderContent = null;
        this.mFileUtils = null;
        this.mPhone = null;
        this.mFileUtils = new FileUtils();
        this.mPhone = PhoneFactory.getDefaultPhone();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 20:
                printLog(TAG, "update_ver_read_error error");
                return;
            case 21:
                printLog(TAG, "UPDATE_SUCCESS SUCCESS");
                return;
            case 36:
                try {
                    onSucceed(msg);
                    return;
                } catch (Exception e) {
                    printLog(TAG, "hanlder doNVwrite wrong");
                    e.printStackTrace();
                    return;
                }
            default:
                printLog(TAG, "error ,hava some error network");
                return;
        }
    }

    public void onSucceed(Message msg) {
    }

    protected Message obtainSucceedMsg() {
        Message msg = Message.obtain();
        msg.what = 36;
        return msg;
    }

    protected XmlPullParser getXmlParser(FileReader confreader, String beginString) {
        XmlPullParser xmlPullParser = null;
        try {
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(confreader);
            XmlUtils.beginDocument(xmlPullParser, beginString);
            return xmlPullParser;
        } catch (Exception e) {
            printLog(TAG, "Exception while parsing network xml file" + e);
            return xmlPullParser;
        }
    }

    protected void setPath(String path) {
        this.mPath = path;
    }

    protected String getPath() {
        return this.mPath;
    }

    protected void setContent(String providerContent) {
        this.mProviderContent = providerContent;
    }

    protected String getContent() {
        return this.mProviderContent;
    }

    protected void printLog(String tag, String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(tag, msg);
        }
    }

    protected String createNetworkRomupdateXmlFile(String providerString, String networkInfoFileName, String networkIndexStart, String networkIndexEnd, int subStringCount) {
        printLog(TAG, "romupdate networkInfoFileName path=" + (this.mPath + networkInfoFileName));
        int networkStartIndex = providerString.indexOf(networkIndexStart);
        int networkEndIndex = providerString.indexOf(networkIndexEnd);
        printLog(TAG, "network_index start=" + networkStartIndex + " network_indext end" + networkEndIndex);
        String networkContent = providerString.substring(networkStartIndex, networkEndIndex + subStringCount);
        printLog(TAG, "Network xml string is:" + networkContent);
        return networkContent;
    }
}

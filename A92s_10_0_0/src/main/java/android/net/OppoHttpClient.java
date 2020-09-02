package android.net;

import android.content.Context;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.text.format.Time;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.TimeZone;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class OppoHttpClient {
    private static final long AVERAGE_RECEIVE_TIME = 832;
    private static final boolean DEBUG = true;
    private static final long GMT_BEIJING_OFFSET = 28800000;
    private static final String TAG = "OppoHttpClient";
    private static final long VALID_LAST_TIME_THRESHOLD = 1500;
    private static long mLastGotSuccessLocaltime = 0;
    private static final String oppoServerURL_RANDOM = "http://newds01.coloros.com/autotime/dateandtime.xml?number=";
    private static final String oppoServerURL_RANDOM2 = "http://newds02.coloros.com/autotime/dateandtime.xml?number=";
    private long mHttpTime;
    private long mHttpTimeReference;
    private long mRoundTripTime;

    public boolean requestTime(Context context, int selServerUrl, int timeout) {
        return forceRefreshTimeFromOppoServer(context, selServerUrl, timeout);
    }

    /* JADX INFO: Multiple debug info for r2v3 int: [D('returnFlag' boolean), D('responseCode' int)] */
    /* JADX INFO: Multiple debug info for r3v5 android.net.OppoHttpClient$DateTimeXmlParseHandler: [D('mDateTimeXmlParseHandler' android.net.OppoHttpClient$DateTimeXmlParseHandler), D('mSaxParser' javax.xml.parsers.SAXParser)] */
    /* JADX INFO: Multiple debug info for r2v8 int[]: [D('mIntDateData' int[]), D('mXmlReader' org.xml.sax.XMLReader)] */
    /* JADX INFO: Multiple debug info for r4v6 java.lang.String[]: [D('mDateString' java.lang.String), D('dateStrings' java.lang.String[])] */
    /* JADX INFO: Multiple debug info for r4v10 int: [D('daylightOffset' int), D('mOppoServerTime' android.text.format.Time)] */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x033c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x033d, code lost:
        r3 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        return r12;
     */
    private boolean forceRefreshTimeFromOppoServer(Context context, int selServerUrl, int timeout) {
        HttpURLConnection httpconn;
        int timeout2;
        String[] dateStrings;
        boolean z;
        Log.d(TAG, "Enter forceRefreshTimeFromOppoServer run");
        String oppoServerURL = oppoServerURL_RANDOM;
        if (selServerUrl > 0) {
            oppoServerURL = oppoServerURL_RANDOM2;
        }
        String oppoServerURL2 = oppoServerURL + System.currentTimeMillis();
        URL url = new URL(oppoServerURL2);
        try {
            Log.i(TAG, "Cur http request:" + oppoServerURL2);
            String proxyHost = Proxy.getDefaultHost();
            int proxyPort = Proxy.getDefaultPort();
            Log.d(TAG, "OppoServer proxyHost = " + proxyHost + " proxyPort = " + proxyPort);
            if (getNetType(context)) {
                Log.d(TAG, "Get network type success!");
                Log.d(TAG, "HttpURLConnection open openConnection success!");
                httpconn = (HttpURLConnection) url.openConnection();
            } else {
                Log.d(TAG, "Use http proxy!");
                httpconn = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            }
            httpconn.setDoInput(true);
            httpconn.setUseCaches(false);
            httpconn.setRequestProperty("Accept-Charset", "UTF-8");
            if (selServerUrl > 0) {
                timeout2 = timeout * 3;
            } else {
                timeout2 = timeout;
            }
            try {
                Log.d(TAG, "timeout:" + timeout2);
                httpconn.setConnectTimeout(timeout2);
                httpconn.setReadTimeout(timeout2);
                long requestTicks = SystemClock.elapsedRealtime();
                Log.d(TAG, "Strart to connect http server!");
                httpconn.connect();
                Log.d(TAG, "Connect http server success!");
                InputStreamReader mInputStreamReader = null;
                BufferedReader mBufferedReader = null;
                String mDateTimeXmlString = "";
                StringBuffer sb = new StringBuffer();
                long mBeginParseTime = 0;
                this.mHttpTimeReference = 0;
                int responseCode = httpconn.getResponseCode();
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Http responseCode:");
                int responseCode2 = responseCode;
                try {
                    sb2.append(responseCode2);
                    Log.d(TAG, sb2.toString());
                    if (responseCode2 == 200) {
                        try {
                            mBeginParseTime = System.currentTimeMillis();
                            mInputStreamReader = new InputStreamReader(httpconn.getInputStream(), "utf-8");
                            mBufferedReader = new BufferedReader(mInputStreamReader);
                            while (true) {
                                String lineString = mBufferedReader.readLine();
                                if (lineString == null) {
                                    break;
                                }
                                mDateTimeXmlString = lineString;
                                sb.append(lineString);
                                Log.d(TAG, "Read response, lineString=" + lineString + ",sb=" + sb.toString());
                                responseCode2 = responseCode2;
                            }
                            Log.d(TAG, "Read response data success! mDateTimeXmlString=" + mDateTimeXmlString);
                        } catch (Exception e) {
                            e = e;
                            Log.e(TAG, "oppoServer exception: " + e);
                            return false;
                        }
                    }
                    long responseTicks = SystemClock.elapsedRealtime();
                    try {
                        this.mHttpTimeReference = SystemClock.elapsedRealtime();
                        if (mBufferedReader != null) {
                            mBufferedReader.close();
                        }
                        if (mInputStreamReader != null) {
                            mInputStreamReader.close();
                        }
                        httpconn.disconnect();
                        Log.d(TAG, "Start to parser http response data!");
                        XMLReader mXmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                        DateTimeXmlParseHandler mDateTimeXmlParseHandler = new DateTimeXmlParseHandler();
                        mXmlReader.setContentHandler(mDateTimeXmlParseHandler);
                        mXmlReader.parse(new InputSource(new StringReader(mDateTimeXmlString)));
                        String mDateString = mDateTimeXmlParseHandler.getDate();
                        String[] dateStrings2 = mDateString.split(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        int[] mIntDateData = new int[3];
                        int i = 0;
                        while (true) {
                            dateStrings = dateStrings2;
                            if (i >= dateStrings.length) {
                                break;
                            }
                            mIntDateData[i] = Integer.parseInt(dateStrings[i]);
                            i++;
                            proxyHost = proxyHost;
                            dateStrings2 = dateStrings;
                            mDateString = mDateString;
                        }
                        String[] dateStrings3 = mDateTimeXmlParseHandler.getTime().split(SettingsStringUtil.DELIMITER);
                        int[] mIntTimeData = new int[3];
                        int i2 = 0;
                        while (i2 < dateStrings3.length) {
                            mIntTimeData[i2] = Integer.parseInt(dateStrings3[i2]);
                            i2++;
                            dateStrings = dateStrings;
                            dateStrings3 = dateStrings3;
                            mDateTimeXmlParseHandler = mDateTimeXmlParseHandler;
                        }
                        Time mOppoServerTime = new Time();
                        Log.d(TAG, "Parser time success, hour= " + mIntTimeData[0] + " minute = " + mIntTimeData[1] + "seconds =" + mIntTimeData[2]);
                        mOppoServerTime.set(mIntTimeData[2], mIntTimeData[1], mIntTimeData[0], mIntDateData[2], mIntDateData[1] + -1, mIntDateData[0]);
                        long mGMTTime = mOppoServerTime.toMillis(true) - GMT_BEIJING_OFFSET;
                        long mNow = ((long) TimeZone.getDefault().getRawOffset()) + mGMTTime + (System.currentTimeMillis() - mBeginParseTime) + AVERAGE_RECEIVE_TIME;
                        this.mHttpTime = ((long) (TimeZone.getDefault().getOffset(mNow) - TimeZone.getDefault().getRawOffset())) + mNow;
                        this.mRoundTripTime = responseTicks - requestTicks;
                        boolean returnFlag = true;
                        try {
                            synchronized (OppoHttpClient.class) {
                                try {
                                    if (SystemClock.elapsedRealtime() - mLastGotSuccessLocaltime > VALID_LAST_TIME_THRESHOLD) {
                                        try {
                                            if (SystemProperties.getLong("persist.sys.lasttime", 0) >= mGMTTime) {
                                                Log.d(TAG, "Cached by carrieroperator or others, Need Ntp algin time!");
                                                Log.d(TAG, "mGMTTime:" + mGMTTime);
                                                returnFlag = false;
                                            }
                                        } catch (Throwable th) {
                                            th = th;
                                            z = true;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                }
                                            }
                                            throw th;
                                        }
                                    }
                                    SystemProperties.set("persist.sys.lasttime", Long.toString(mGMTTime));
                                    mLastGotSuccessLocaltime = SystemClock.elapsedRealtime();
                                } catch (Throwable th3) {
                                    th = th3;
                                    z = true;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                        } catch (Exception e2) {
                            e = e2;
                            url = url;
                            Log.e(TAG, "oppoServer exception: " + e);
                            return false;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        url = url;
                        Log.e(TAG, "oppoServer exception: " + e);
                        return false;
                    }
                } catch (Exception e4) {
                    e = e4;
                    Log.e(TAG, "oppoServer exception: " + e);
                    return false;
                }
            } catch (Exception e5) {
                e = e5;
                Log.e(TAG, "oppoServer exception: " + e);
                return false;
            }
        } catch (Exception e6) {
            e = e6;
            Log.e(TAG, "oppoServer exception: " + e);
            return false;
        }
    }

    private boolean getNetType(Context context) {
        NetworkInfo info;
        String apn;
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService("connectivity");
        if (conn == null || (info = conn.getActiveNetworkInfo()) == null) {
            return false;
        }
        String type = info.getTypeName();
        if (type.equalsIgnoreCase("WIFI")) {
            return true;
        }
        if ((type.equalsIgnoreCase("MOBILE") || type.equalsIgnoreCase("GPRS")) && (apn = info.getExtraInfo()) != null && apn.equalsIgnoreCase("cmwap")) {
            return false;
        }
        return true;
    }

    public class DateTimeXmlParseHandler extends DefaultHandler {
        private String mDateString = "";
        private boolean mIsDateFlag = false;
        private boolean mIsTimeFlag = false;
        private boolean mIsTimeZoneFlag = false;
        private String mTimeString = "";
        private String mTimeZoneString = "";

        public DateTimeXmlParseHandler() {
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (this.mIsTimeZoneFlag) {
                this.mTimeZoneString = new String(ch, start, length);
            } else if (this.mIsDateFlag) {
                this.mDateString = new String(ch, start, length);
            } else if (this.mIsTimeFlag) {
                this.mTimeString = new String(ch, start, length);
            }
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals("TimeZone")) {
                this.mIsTimeZoneFlag = false;
            } else if (localName.equals("Date")) {
                this.mIsDateFlag = false;
            } else if (localName.equals("Time")) {
                this.mIsTimeFlag = false;
            }
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equals("TimeZone")) {
                this.mIsTimeZoneFlag = true;
            } else if (localName.equals("Date")) {
                this.mIsDateFlag = true;
            } else if (localName.equals("Time")) {
                this.mIsTimeFlag = true;
            }
        }

        public String getTimeZone() {
            return this.mTimeZoneString;
        }

        public String getDate() {
            return this.mDateString;
        }

        public String getTime() {
            return this.mTimeString;
        }
    }

    public long getHttpTime() {
        return this.mHttpTime;
    }

    public long getHttpTimeReference() {
        return this.mHttpTimeReference;
    }

    public long getRoundTripTime() {
        return this.mRoundTripTime;
    }
}

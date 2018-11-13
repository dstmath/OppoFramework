package android.net;

import android.content.Context;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.Time;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
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
    private static final String oppoServerURL_RANDOM = "http://newds01.myoppo.com/autotime/dateandtime.xml?number=";
    private static final String oppoServerURL_RANDOM2 = "http://newds02.myoppo.com/autotime/dateandtime.xml?number=";
    private long mHttpTime;
    private long mHttpTimeReference;
    private long mRoundTripTime;

    public class DateTimeXmlParseHandler extends DefaultHandler {
        private String mDateString = "";
        private boolean mIsDateFlag = false;
        private boolean mIsTimeFlag = false;
        private boolean mIsTimeZoneFlag = false;
        private String mTimeString = "";
        private String mTimeZoneString = "";

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

        public void endDocument() throws SAXException {
            super.endDocument();
        }

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

        public void startDocument() throws SAXException {
            super.startDocument();
        }

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

    public boolean requestTime(Context context, int selServerUrl, int timeout) {
        return forceRefreshTimeFromOppoServer(context, selServerUrl, timeout);
    }

    private boolean forceRefreshTimeFromOppoServer(Context context, int selServerUrl, int timeout) {
        Exception e;
        Log.d(TAG, "Enter forceRefreshTimeFromOppoServer run");
        try {
            String oppoServerURL = oppoServerURL_RANDOM;
            if (selServerUrl > 0) {
                oppoServerURL = oppoServerURL_RANDOM2;
            }
            oppoServerURL = oppoServerURL + System.currentTimeMillis();
            URL url = new URL(oppoServerURL);
            URL url2;
            try {
                HttpURLConnection httpconn;
                int i;
                Log.i(TAG, "Cur http request:" + oppoServerURL);
                String proxyHost = Proxy.getDefaultHost();
                int proxyPort = Proxy.getDefaultPort();
                Log.d(TAG, "OppoServer proxyHost = " + proxyHost + " proxyPort = " + proxyPort);
                if (getNetType(context)) {
                    Log.d(TAG, "Get network type success!");
                    httpconn = (HttpURLConnection) url.openConnection();
                    Log.d(TAG, "HttpURLConnection open openConnection success!");
                } else {
                    Log.d(TAG, "Use http proxy!");
                    httpconn = (HttpURLConnection) url.openConnection(new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                }
                httpconn.setDoInput(true);
                httpconn.setUseCaches(false);
                httpconn.setRequestProperty("Accept-Charset", "UTF-8");
                if (selServerUrl > 0) {
                    timeout *= 3;
                }
                Log.d(TAG, "timeout:" + timeout);
                httpconn.setConnectTimeout(timeout);
                httpconn.setReadTimeout(timeout);
                long requestTicks = SystemClock.elapsedRealtime();
                Log.d(TAG, "Strart to connect http server!");
                httpconn.connect();
                Log.d(TAG, "Connect http server success!");
                InputStreamReader mInputStreamReader = null;
                BufferedReader mBufferedReader = null;
                String mDateTimeXmlString = "";
                long mBeginParseTime = 0;
                this.mHttpTimeReference = 0;
                int responseCode = httpconn.getResponseCode();
                Log.d(TAG, "Http responseCode:" + responseCode);
                if (responseCode == 200) {
                    mBeginParseTime = System.currentTimeMillis();
                    InputStreamReader inputStreamReader = new InputStreamReader(httpconn.getInputStream(), "utf-8");
                    mBufferedReader = new BufferedReader(inputStreamReader);
                    String str = "";
                    while (true) {
                        str = mBufferedReader.readLine();
                        if (str == null) {
                            break;
                        }
                        mDateTimeXmlString = str;
                    }
                    Log.d(TAG, "Read response data success!");
                }
                long responseTicks = SystemClock.elapsedRealtime();
                this.mHttpTimeReference = SystemClock.elapsedRealtime();
                mBufferedReader.close();
                mInputStreamReader.close();
                httpconn.disconnect();
                Log.d(TAG, "Start to parser http response data!");
                XMLReader mXmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                DateTimeXmlParseHandler dateTimeXmlParseHandler = new DateTimeXmlParseHandler();
                mXmlReader.setContentHandler(dateTimeXmlParseHandler);
                mXmlReader.parse(new InputSource(new StringReader(mDateTimeXmlString)));
                String[] dateStrings = dateTimeXmlParseHandler.getDate().split("-");
                int[] mIntDateData = new int[3];
                for (i = 0; i < dateStrings.length; i++) {
                    mIntDateData[i] = Integer.parseInt(dateStrings[i]);
                }
                String[] timeStrings = dateTimeXmlParseHandler.getTime().split(":");
                int[] mIntTimeData = new int[3];
                for (i = 0; i < timeStrings.length; i++) {
                    mIntTimeData[i] = Integer.parseInt(timeStrings[i]);
                }
                Time mOppoServerTime = new Time();
                Log.d(TAG, "Parser time success, hour= " + mIntTimeData[0] + " minute = " + mIntTimeData[1] + "seconds =" + mIntTimeData[2]);
                mOppoServerTime.set(mIntTimeData[2], mIntTimeData[1], mIntTimeData[0], mIntDateData[2], mIntDateData[1] - 1, mIntDateData[0]);
                long mGMTTime = mOppoServerTime.toMillis(true) - GMT_BEIJING_OFFSET;
                long mNow = ((((long) TimeZone.getDefault().getRawOffset()) + mGMTTime) + (System.currentTimeMillis() - mBeginParseTime)) + AVERAGE_RECEIVE_TIME;
                this.mHttpTime = ((long) (TimeZone.getDefault().getOffset(mNow) - TimeZone.getDefault().getRawOffset())) + mNow;
                this.mRoundTripTime = responseTicks - requestTicks;
                boolean returnFlag = true;
                synchronized (OppoHttpClient.class) {
                    if (SystemClock.elapsedRealtime() - mLastGotSuccessLocaltime <= VALID_LAST_TIME_THRESHOLD || SystemProperties.getLong("persist.sys.lasttime", 0) < mGMTTime) {
                        SystemProperties.set("persist.sys.lasttime", Long.toString(mGMTTime));
                        mLastGotSuccessLocaltime = SystemClock.elapsedRealtime();
                    } else {
                        Log.d(TAG, "Cached by carrieroperator or others, Need Ntp algin time!");
                        Log.d(TAG, "mGMTTime:" + mGMTTime);
                        returnFlag = false;
                    }
                }
                url2 = url;
                return returnFlag;
            } catch (Exception e2) {
                e = e2;
                url2 = url;
                Log.e(TAG, "oppoServer exception: " + e);
                return false;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "oppoServer exception: " + e);
            return false;
        }
    }

    private boolean getNetType(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conn == null) {
            return false;
        }
        NetworkInfo info = conn.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        String type = info.getTypeName();
        if (type.equalsIgnoreCase("WIFI")) {
            return true;
        }
        if (!type.equalsIgnoreCase("MOBILE") && !type.equalsIgnoreCase("GPRS")) {
            return true;
        }
        String apn = info.getExtraInfo();
        return apn == null || !apn.equalsIgnoreCase("cmwap");
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

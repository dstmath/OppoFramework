package com.oppo.enterprise.mdmcoreservice.certificate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
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
import java.net.URL;
import java.util.TimeZone;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class OppoHttpClient {
    private static long mLastGotSuccessLocaltime = 0;
    private long mHttpTime;
    private long mHttpTimeReference;
    private long mRoundTripTime;

    public boolean requestTime(Context context, int selServerUrl, int timeout) {
        return forceRefreshTimeFromOppoServer(context, selServerUrl, timeout);
    }

    /* JADX INFO: Multiple debug info for r5v6 org.xml.sax.XMLReader: [D('mSaxParserFactory' javax.xml.parsers.SAXParserFactory), D('mXmlReader' org.xml.sax.XMLReader)] */
    /* JADX INFO: Multiple debug info for r6v5 com.oppo.enterprise.mdmcoreservice.certificate.OppoHttpClient$DateTimeXmlParseHandler: [D('mDateTimeXmlParseHandler' com.oppo.enterprise.mdmcoreservice.certificate.OppoHttpClient$DateTimeXmlParseHandler), D('mSaxParser' javax.xml.parsers.SAXParser)] */
    /* JADX INFO: Multiple debug info for r2v3 int[]: [D('mDateTimeXmlString' java.lang.String), D('mIntDateData' int[])] */
    /* JADX INFO: Multiple debug info for r5v8 java.lang.String[]: [D('dateStrings' java.lang.String[]), D('mXmlReader' org.xml.sax.XMLReader)] */
    private boolean forceRefreshTimeFromOppoServer(Context context, int selServerUrl, int timeout) {
        Exception e;
        HttpURLConnection httpconn;
        int timeout2;
        URL url;
        StringBuffer sb;
        String mDateTimeXmlString;
        String[] timeStrings;
        boolean returnFlag = false;
        Log.d("OppoHttpClient", "Enter forceRefreshTimeFromOppoServer run");
        String oppoServerURL = "http://newds01.coloros.com/autotime/dateandtime.xml?number=";
        if (selServerUrl > 0) {
            oppoServerURL = "http://newds02.coloros.com/autotime/dateandtime.xml?number=";
        }
        String oppoServerURL2 = oppoServerURL + System.currentTimeMillis();
        URL url2 = new URL(oppoServerURL2);
        try {
            Log.i("OppoHttpClient", "Cur http request:" + oppoServerURL2);
            String proxyHost = Proxy.getDefaultHost();
            int proxyPort = Proxy.getDefaultPort();
            Log.d("OppoHttpClient", "OppoServer proxyHost = " + proxyHost + " proxyPort = " + proxyPort);
            if (getNetType(context)) {
                Log.d("OppoHttpClient", "Get network type success!");
                httpconn = (HttpURLConnection) url2.openConnection();
                Log.d("OppoHttpClient", "HttpURLConnection open openConnection success!");
            } else {
                Log.d("OppoHttpClient", "Use http proxy!");
                httpconn = (HttpURLConnection) url2.openConnection(new java.net.Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            }
            httpconn.setDoInput(true);
            httpconn.setUseCaches(false);
            httpconn.setRequestProperty("Accept-Charset", "UTF-8");
            if (selServerUrl > 0) {
                timeout2 = 3 * timeout;
            } else {
                timeout2 = timeout;
            }
            try {
                Log.d("OppoHttpClient", "timeout:" + timeout2);
                httpconn.setConnectTimeout(timeout2);
                httpconn.setReadTimeout(timeout2);
                long requestTicks = SystemClock.elapsedRealtime();
                Log.d("OppoHttpClient", "Strart to connect http server!");
                httpconn.connect();
                Log.d("OppoHttpClient", "Connect http server success!");
                InputStreamReader mInputStreamReader = null;
                BufferedReader mBufferedReader = null;
                StringBuffer sb2 = new StringBuffer();
                long mBeginParseTime = 0;
                this.mHttpTimeReference = 0;
                int responseCode = httpconn.getResponseCode();
                Log.d("OppoHttpClient", "Http responseCode:" + responseCode);
                if (responseCode == 200) {
                    mBeginParseTime = System.currentTimeMillis();
                    mInputStreamReader = new InputStreamReader(httpconn.getInputStream(), "utf-8");
                    mBufferedReader = new BufferedReader(mInputStreamReader);
                    mDateTimeXmlString = "";
                    while (true) {
                        String lineString = mBufferedReader.readLine();
                        if (lineString == null) {
                            break;
                        }
                        sb2.append(lineString);
                        try {
                            StringBuilder sb3 = new StringBuilder();
                            url = url2;
                            try {
                                sb3.append("Read response, lineString=");
                                sb3.append(lineString);
                                sb3.append(",sb=");
                                sb3.append(sb2.toString());
                                Log.d("OppoHttpClient", sb3.toString());
                                sb2 = sb2;
                                mDateTimeXmlString = lineString;
                                returnFlag = returnFlag;
                                url2 = url;
                            } catch (Exception e2) {
                                e = e2;
                                url2 = url;
                                Log.e("OppoHttpClient", "oppoServer exception: " + e);
                                return false;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            Log.e("OppoHttpClient", "oppoServer exception: " + e);
                            return false;
                        }
                    }
                    url = url2;
                    sb = sb2;
                    Log.d("OppoHttpClient", "Read response data success! mDateTimeXmlString=" + mDateTimeXmlString);
                } else {
                    url = url2;
                    sb = sb2;
                    mDateTimeXmlString = "";
                }
                long responseTicks = SystemClock.elapsedRealtime();
                this.mHttpTimeReference = SystemClock.elapsedRealtime();
                if (mBufferedReader != null) {
                    mBufferedReader.close();
                }
                if (mInputStreamReader != null) {
                    mInputStreamReader.close();
                }
                httpconn.disconnect();
                Log.d("OppoHttpClient", "Start to parser http response data!");
                XMLReader mXmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                DateTimeXmlParseHandler mDateTimeXmlParseHandler = new DateTimeXmlParseHandler();
                mXmlReader.setContentHandler(mDateTimeXmlParseHandler);
                mXmlReader.parse(new InputSource(new StringReader(mDateTimeXmlString)));
                String mDateString = mDateTimeXmlParseHandler.getDate();
                String[] dateStrings = mDateString.split("-");
                int[] mIntDateData = new int[3];
                int i = 0;
                while (true) {
                    timeStrings = dateStrings;
                    if (i >= timeStrings.length) {
                        break;
                    }
                    mIntDateData[i] = Integer.parseInt(timeStrings[i]);
                    i++;
                    dateStrings = timeStrings;
                    mXmlReader = mXmlReader;
                    mDateString = mDateString;
                }
                String[] timeStrings2 = mDateTimeXmlParseHandler.getTime().split(":");
                int[] mIntTimeData = new int[3];
                int i2 = 0;
                while (i2 < timeStrings2.length) {
                    mIntTimeData[i2] = Integer.parseInt(timeStrings2[i2]);
                    i2++;
                    timeStrings2 = timeStrings2;
                    timeStrings = timeStrings;
                    mDateTimeXmlParseHandler = mDateTimeXmlParseHandler;
                }
                Time mOppoServerTime = new Time();
                Log.d("OppoHttpClient", "Parser time success, hour= " + mIntTimeData[0] + " minute = " + mIntTimeData[1] + "seconds =" + mIntTimeData[2]);
                mOppoServerTime.set(mIntTimeData[2], mIntTimeData[1], mIntTimeData[0], mIntDateData[2], mIntDateData[1] + -1, mIntDateData[0]);
                long mGMTTime = mOppoServerTime.toMillis(true) - 28800000;
                long mNow = ((long) TimeZone.getDefault().getRawOffset()) + mGMTTime + (System.currentTimeMillis() - mBeginParseTime) + 832;
                this.mHttpTime = ((long) (TimeZone.getDefault().getOffset(mNow) - TimeZone.getDefault().getRawOffset())) + mNow;
                this.mRoundTripTime = responseTicks - requestTicks;
                boolean returnFlag2 = true;
                try {
                    synchronized (OppoHttpClient.class) {
                        try {
                            if (SystemClock.elapsedRealtime() - mLastGotSuccessLocaltime > 1500) {
                                if (SystemProperties.getLong("persist.sys.lasttime", 0) >= mGMTTime) {
                                    Log.d("OppoHttpClient", "Cached by carrieroperator or others, Need Ntp algin time!");
                                    Log.d("OppoHttpClient", "mGMTTime:" + mGMTTime);
                                    returnFlag2 = false;
                                    return returnFlag2;
                                }
                            }
                            SystemProperties.set("persist.sys.lasttime", Long.toString(mGMTTime));
                            mLastGotSuccessLocaltime = SystemClock.elapsedRealtime();
                            return returnFlag2;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Exception e4) {
                    e = e4;
                    url2 = url;
                    Log.e("OppoHttpClient", "oppoServer exception: " + e);
                    return false;
                }
            } catch (Exception e5) {
                e = e5;
                Log.e("OppoHttpClient", "oppoServer exception: " + e);
                return false;
            }
        } catch (Exception e6) {
            e = e6;
            Log.e("OppoHttpClient", "oppoServer exception: " + e);
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
}

package com.android.server.wifi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class OppoConnectivityVodafoneParser {
    private static final boolean DBG = true;
    private static final String TAG = "OppoConnectivityVodafoneParser";
    private static final String VDF_COUNTRY_XML_FILE_PATH = "/system/oppo/connectivity_vdf.xml";
    private static final String VDF_COUNTRY_XML_FILE_ROOT_ITEM = "connectivity_vdf";
    private static final boolean XML_DBG = true;
    private static OppoConnectivityVodafoneParser sInstance;
    private final String mConfigFilePath;
    private final Context mContext;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread("OppoConnectivityTmobileParser");

    public OppoConnectivityVodafoneParser(Context context, String filePath) {
        Log.i(TAG, TAG);
        this.mContext = context;
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mConfigFilePath = (filePath == null || filePath.equals("")) ? VDF_COUNTRY_XML_FILE_PATH : filePath;
        registerForBroadcastReceiver();
    }

    public static OppoConnectivityVodafoneParser getInstance(Context context, String filePath) {
        if (sInstance == null) {
            synchronized (OppoConnectivityVodafoneParser.class) {
                if (sInstance == null) {
                    sInstance = new OppoConnectivityVodafoneParser(context, filePath);
                }
            }
        }
        return sInstance;
    }

    public String getConfigFilePath() {
        return this.mConfigFilePath;
    }

    private void registerForBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.OPPO_START_CUSTOMIZE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoConnectivityVodafoneParser.AnonymousClass1 */

            public void onReceive(final Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals("oppo.intent.action.OPPO_START_CUSTOMIZE")) {
                    String operator = intent.getStringExtra("operator");
                    final String region = intent.getStringExtra("region");
                    Log.i(OppoConnectivityVodafoneParser.TAG, action + " received, operator:" + operator + ", region:" + region);
                    if (operator == null || !operator.equals("VODAFONE")) {
                        Log.e(OppoConnectivityVodafoneParser.TAG, action + ", operator is not VODAFONE!");
                        return;
                    }
                    OppoConnectivityVodafoneParser.this.mHandler.post(new Runnable() {
                        /* class com.android.server.wifi.OppoConnectivityVodafoneParser.AnonymousClass1.AnonymousClass1 */

                        public void run() {
                            String newWifiState = OppoConnectivityVodafoneParser.this.getVdfCountryInfo(region, "wifi");
                            WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
                            Log.i(OppoConnectivityVodafoneParser.TAG, action + ", newWifiState is " + newWifiState);
                            if (newWifiState == null) {
                                Log.e(OppoConnectivityVodafoneParser.TAG, action + ", newWifiState is bad.");
                            } else if (newWifiState.equals("on")) {
                                wifiManager.setWifiEnabled(true);
                            } else if (newWifiState.equals("off")) {
                                wifiManager.setWifiEnabled(false);
                            } else {
                                Log.e(OppoConnectivityVodafoneParser.TAG, action + ", do nothing");
                            }
                            String newBluetoothState = OppoConnectivityVodafoneParser.this.getVdfCountryInfo(region, "bluetooth");
                            BluetoothAdapter bluetoothAdapter = ((BluetoothManager) context.getSystemService("bluetooth")).getAdapter();
                            Log.i(OppoConnectivityVodafoneParser.TAG, action + ", newBluetoothState is " + newBluetoothState);
                            if (newBluetoothState == null) {
                                Log.e(OppoConnectivityVodafoneParser.TAG, action + ", newBluetoothState is bad.");
                            } else if (newBluetoothState.equals("on")) {
                                bluetoothAdapter.enable();
                            } else if (newBluetoothState.equals("off")) {
                                bluetoothAdapter.disable();
                            } else {
                                Log.e(OppoConnectivityVodafoneParser.TAG, action + ", do nothing");
                            }
                        }
                    });
                }
            }
        }, new IntentFilter(filter));
    }

    /* JADX INFO: Multiple debug info for r0v15 org.w3c.dom.Document: [D('factory' javax.xml.parsers.DocumentBuilderFactory), D('doc' org.w3c.dom.Document)] */
    public String getVdfCountryInfo(String country, String module) {
        ParserConfigurationException e;
        SAXException e2;
        IOException e3;
        Log.i(TAG, "getVdfCountryInfo, country:" + country + ", module:" + module);
        if (country != null && module != null) {
            if (module.equals("wifi") || module.equals("bluetooth") || module.equals("nfc") || module.equals("gps")) {
                try {
                    try {
                        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(this.mConfigFilePath));
                        if (doc == null) {
                            Log.e(TAG, "getVdfCountryInfo, null doc!");
                            return null;
                        }
                        Element root = doc.getDocumentElement();
                        String rootText = root.getTagName().trim();
                        if (rootText != null) {
                            if (rootText.equals(VDF_COUNTRY_XML_FILE_ROOT_ITEM)) {
                                NodeList children = root.getChildNodes();
                                int childIndex = 0;
                                while (childIndex < children.getLength()) {
                                    Node child = children.item(childIndex);
                                    if (child instanceof Element) {
                                        Element childElement = (Element) child;
                                        String tagName = childElement.getTagName();
                                        String tagText = ((Text) childElement.getFirstChild()).getData().trim();
                                        if (tagName != null && tagName.equals("country_toggle_update") && tagText != null && tagText.equals(country)) {
                                            String ret = childElement.getAttribute(module);
                                            Log.i(TAG, "getVdfCountryInfo, ret:" + ret);
                                            return ret;
                                        }
                                    }
                                    childIndex++;
                                    doc = doc;
                                }
                                Log.i(TAG, "getVdfCountryInfo, not found, return null");
                                return null;
                            }
                        }
                        Log.e(TAG, "getVdfCountryInfo, xml root tag is WRONG!");
                        return null;
                    } catch (ParserConfigurationException e4) {
                        e = e4;
                        Log.e(TAG, "ParserConfigurationException:" + e);
                        return null;
                    } catch (SAXException e5) {
                        e2 = e5;
                        Log.e(TAG, "SAXException:" + e2);
                        return null;
                    } catch (IOException e6) {
                        e3 = e6;
                        Log.e(TAG, "IOException:" + e3);
                        return null;
                    }
                } catch (ParserConfigurationException e7) {
                    e = e7;
                    Log.e(TAG, "ParserConfigurationException:" + e);
                    return null;
                } catch (SAXException e8) {
                    e2 = e8;
                    Log.e(TAG, "SAXException:" + e2);
                    return null;
                } catch (IOException e9) {
                    e3 = e9;
                    Log.e(TAG, "IOException:" + e3);
                    return null;
                }
            }
        }
        Log.e(TAG, "getVdfCountryInfo, country or module is WRONG!");
        return null;
    }
}

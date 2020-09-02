package com.mediatek.appresolutiontuner;

import android.util.Slog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResolutionTunerAppList {
    private static final String APP_LIST_PATH = "system/etc/resolution_tuner_app_list.xml";
    private static final String NODE_FILTERED_WINDOW = "filteredwindow";
    private static final String NODE_PACKAGE_NAME = "packagename";
    private static final String NODE_SCALE = "scale";
    private static final String NODE_SCALING_FLOW = "flow";
    private static final String TAG = "AppResolutionTuner";
    private static final String TAG_APP = "app";
    private static final String VALUE_SCALING_FLOW_SURFACEVIEW = "surfaceview";
    private static final String VALUE_SCALING_FLOW_WMS = "wms";
    private static ResolutionTunerAppList sInstance;
    private ArrayList<Applic> mTunerAppCache;

    public static ResolutionTunerAppList getInstance() {
        if (sInstance == null) {
            sInstance = new ResolutionTunerAppList();
        }
        return sInstance;
    }

    public void loadTunerAppList() {
        Slog.d(TAG, "loadTunerAppList + ");
        InputStream inputStream = null;
        try {
            File target = new File(APP_LIST_PATH);
            if (!target.exists()) {
                Slog.e(TAG, "Target file doesn't exist: system/etc/resolution_tuner_app_list.xml");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "close failed..", e);
                    }
                }
            } else {
                InputStream inputStream2 = new FileInputStream(target);
                this.mTunerAppCache = parseAppListFile(inputStream2);
                try {
                    inputStream2.close();
                } catch (IOException e2) {
                    Slog.w(TAG, "close failed..", e2);
                }
                Slog.d(TAG, "loadTunerAppList - ");
            }
        } catch (IOException e3) {
            Slog.w(TAG, "IOException", e3);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    Slog.w(TAG, "close failed..", e4);
                }
            }
            throw th;
        }
    }

    public boolean isScaledByWMS(String packageName, String windowName) {
        ArrayList<Applic> arrayList = this.mTunerAppCache;
        if (arrayList == null) {
            return false;
        }
        Iterator<Applic> it = arrayList.iterator();
        while (it.hasNext()) {
            Applic app = it.next();
            if (app.getPackageName().equals(packageName) && app.getScalingFlow().equals(VALUE_SCALING_FLOW_WMS)) {
                return !app.isFiltered(windowName);
            }
        }
        return false;
    }

    public boolean isScaledBySurfaceView(String packageName) {
        ArrayList<Applic> arrayList = this.mTunerAppCache;
        if (arrayList == null) {
            return false;
        }
        Iterator<Applic> it = arrayList.iterator();
        while (it.hasNext()) {
            Applic app = it.next();
            if (app.getPackageName().equals(packageName) && !app.getScalingFlow().equals(VALUE_SCALING_FLOW_WMS)) {
                return true;
            }
        }
        return false;
    }

    public float getScaleValue(String packageName) {
        ArrayList<Applic> arrayList = this.mTunerAppCache;
        if (arrayList == null) {
            return 1.0f;
        }
        Iterator<Applic> it = arrayList.iterator();
        while (it.hasNext()) {
            Applic app = it.next();
            if (app.getPackageName().equals(packageName)) {
                return app.getScale();
            }
        }
        return 1.0f;
    }

    class Applic {
        private ArrayList<String> filteredWindows = new ArrayList<>();
        private String packageName;
        private float scale;
        private String scalingFlow = "";

        Applic() {
        }

        public String getPackageName() {
            return this.packageName;
        }

        public void setPackageName(String packageName2) {
            this.packageName = packageName2;
        }

        public float getScale() {
            return this.scale;
        }

        public void setScale(float scale2) {
            this.scale = scale2;
        }

        public void addFilteredWindow(String windowName) {
            this.filteredWindows.add(windowName);
        }

        public boolean isFiltered(String windowName) {
            return this.filteredWindows.contains(windowName);
        }

        public String getScalingFlow() {
            return this.scalingFlow;
        }

        public void setScalingFlow(String scalingFlow2) {
            this.scalingFlow = scalingFlow2;
        }

        public String toString() {
            return "App{packageName='" + this.packageName + '\'' + ", scale='" + this.scale + '\'' + ", filteredWindows= " + this.filteredWindows + '\'' + ", scalingFlow= " + this.scalingFlow + '\'' + '}';
        }
    }

    private ArrayList<Applic> parseAppListFile(InputStream is) {
        ArrayList<Applic> list = new ArrayList<>();
        try {
            NodeList appList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getElementsByTagName(TAG_APP);
            for (int i = 0; i < appList.getLength(); i++) {
                NodeList childNodes = appList.item(i).getChildNodes();
                Applic applic = new Applic();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node childNode = childNodes.item(j);
                    if (childNode.getNodeName().equals(NODE_PACKAGE_NAME)) {
                        applic.setPackageName(childNode.getTextContent());
                    } else if (childNode.getNodeName().equals(NODE_SCALE)) {
                        applic.setScale(Float.parseFloat(childNode.getTextContent()));
                    } else if (childNode.getNodeName().startsWith(NODE_FILTERED_WINDOW)) {
                        applic.addFilteredWindow(childNode.getTextContent());
                    } else if (childNode.getNodeName().startsWith(NODE_SCALING_FLOW)) {
                        applic.setScalingFlow(childNode.getTextContent());
                    }
                }
                list.add(applic);
                Slog.d(TAG, "dom2xml: " + applic);
            }
            return list;
        } catch (ParserConfigurationException e) {
            Slog.w(TAG, "dom2xml ParserConfigurationException", e);
            return list;
        } catch (SAXException e2) {
            Slog.w(TAG, "dom2xml SAXException", e2);
            return list;
        } catch (IOException e3) {
            Slog.w(TAG, "IOException", e3);
            return list;
        }
    }
}

package com.android.server.enterprise;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.server.oppo.OppoCustomizeNotificationHelper;
import com.android.server.pm.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoEnterpriseServiceHelper {
    public static final String ADB_INSTALL_UNINSTALL_POLICY_PROPERTY = "persist.sys.oppo.adbiusdisable";
    public static final String DISABLED_DEACTIVATE_CONTROL_FILENAME = "disabled_deactivate_mdmPackages_list.xml";
    public static final String INTERACTIVE_CONTROL_FILENAME = "interaction_ins_un_packages.xml";
    public static final String INTERACTIVE_CONTROL_LIST_PATH = "/data/system/";
    public static final String INTERACTIVE_INSTALL_UNINSTALL_POLICY_PROPERTY = "persist.sys.oppo.inunpolicy";
    private static final String TAG = "OppoEnterpriseServiceHelper";
    private final String CUSTOMIZED_FEATURE_NAME;
    private Context mContext;
    private boolean mIsCustomized;

    private OppoEnterpriseServiceHelper() {
        this.CUSTOMIZED_FEATURE_NAME = OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM;
        this.mIsCustomized = false;
    }

    /* access modifiers changed from: private */
    public static class OppoEnterpriseServiceHelperHolder {
        private static OppoEnterpriseServiceHelper sInstance = new OppoEnterpriseServiceHelper();

        private OppoEnterpriseServiceHelperHolder() {
        }
    }

    public static OppoEnterpriseServiceHelper getInstance() {
        return OppoEnterpriseServiceHelperHolder.sInstance;
    }

    public void init(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mIsCustomized = this.mContext.getPackageManager().hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM);
            Slog.d(TAG, "OppoEnterpriseServiceHelper init success. isCustomized=" + this.mIsCustomized);
        }
    }

    public boolean hasOppoEnterpriseFeature() {
        return this.mIsCustomized;
    }

    /* JADX INFO: Multiple debug info for r4v1 java.io.FileOutputStream: [D('fileos' java.io.FileOutputStream), D('e' java.io.IOException)] */
    public boolean saveListToFile(String path, String name, List<String> list) {
        File file = new File(path, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                Slog.i(TAG, "failed create file " + e);
            }
        }
        FileOutputStream fileos = null;
        try {
            FileOutputStream fileos2 = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fileos2, "UTF-8");
            serializer.startDocument(null, true);
            serializer.startTag(null, "packages");
            for (int i = 0; i < list.size(); i++) {
                String pkg = list.get(i);
                if (pkg != null) {
                    serializer.startTag(null, Settings.ATTR_PACKAGE);
                    serializer.text(pkg);
                    serializer.endTag(null, Settings.ATTR_PACKAGE);
                }
            }
            serializer.endTag(null, "packages");
            serializer.endDocument();
            serializer.flush();
            try {
                fileos2.close();
            } catch (IOException e2) {
                Slog.i(TAG, "failed close stream " + e2);
            }
            return true;
        } catch (Exception e3) {
            Slog.i(TAG, "failed write file " + e3);
            if (0 != 0) {
                try {
                    fileos.close();
                } catch (IOException e4) {
                    Slog.i(TAG, "failed close stream " + e4);
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileos.close();
                } catch (IOException e5) {
                    Slog.i(TAG, "failed close stream " + e5);
                }
            }
            throw th;
        }
    }

    public List<String> readXMLFile(String path, String name) {
        StringBuilder sb;
        int type;
        String pkg;
        List<String> list = new ArrayList<>();
        File file = new File(path, name);
        if (!file.exists()) {
            return list;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && Settings.ATTR_PACKAGE.equals(parser.getName()) && (pkg = parser.nextText()) != null) {
                    list.add(pkg);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (NullPointerException e2) {
            Slog.i(TAG, "failed parsing " + e2);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Slog.i(TAG, "failed parsing " + e4);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Slog.i(TAG, "failed parsing " + e6);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Slog.i(TAG, "failed IOException " + e8);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Slog.i(TAG, "failed parsing " + e10);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Slog.i(TAG, "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
        return list;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Slog.i(TAG, sb.toString());
        return list;
    }

    public boolean hasInteractionCallingPermission(Intent intent, String callingPackage) {
        String className = "";
        if (!(intent == null || intent.getComponent() == null)) {
            className = intent.getComponent().getClassName();
        }
        int currentPolicy = SystemProperties.getInt(INTERACTIVE_INSTALL_UNINSTALL_POLICY_PROPERTY, -1);
        if (("com.android.packageinstaller.PackageInstallerActivity".equals(className) || "com.android.packageinstaller.UninstallerActivity".equals(className)) && currentPolicy >= 0) {
            if (currentPolicy == 0) {
                return false;
            }
            if (currentPolicy != 1 || readXMLFile(INTERACTIVE_CONTROL_LIST_PATH, INTERACTIVE_CONTROL_FILENAME).contains(callingPackage)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean hasAdbInstallUninstallPermission() {
        if (SystemProperties.getBoolean(ADB_INSTALL_UNINSTALL_POLICY_PROPERTY, false)) {
            return false;
        }
        return true;
    }

    public boolean disabledDeactivateMdmPackages(String adminPackage) {
        try {
            List<String> packageNames = getInstance().readXMLFile(INTERACTIVE_CONTROL_LIST_PATH, DISABLED_DEACTIVATE_CONTROL_FILENAME);
            if (packageNames.isEmpty() || !packageNames.contains(adminPackage)) {
                return false;
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
}

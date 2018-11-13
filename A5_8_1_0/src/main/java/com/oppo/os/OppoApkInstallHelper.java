package com.oppo.os;

import android.content.Context;
import android.os.Environment;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoApkInstallHelper {
    private static final String TAG = "OppoApkInstallHelper";
    private static boolean external = false;
    private static boolean internal = false;

    static {
        parsexml();
    }

    public static boolean InstallUIDisplay(Context context) {
        if (internal) {
            return true;
        }
        if (!external || OppoUsbEnvironment.isExternalSDRemoved(context)) {
            return false;
        }
        return true;
    }

    public static boolean IsInstallSdMounted(Context context) {
        if (internal) {
            return "mounted".equals(OppoUsbEnvironment.getInternalSdState(context));
        }
        if (external) {
            return "mounted".equals(OppoUsbEnvironment.getExternalSdState(context));
        }
        return false;
    }

    private static void parsexml() {
        File permFile = new File(Environment.getRootDirectory(), "etc/apk_install.xml");
        try {
            FileReader permReader = new FileReader(permFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (parser.getName().equals("path")) {
                                boolean z;
                                eventType = parser.next();
                                if (parser.getText().equals("internal")) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                internal = z;
                                if (parser.getText().equals("external")) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                external = z;
                                break;
                            }
                            break;
                    }
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got execption parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got execption parsing permissions.", e2);
            }
            if (permReader != null) {
                try {
                    permReader.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't find or open apk_install file " + permFile);
        }
    }
}

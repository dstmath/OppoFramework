package com.oppo.os;

import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
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
            return Environment.MEDIA_MOUNTED.equals(OppoUsbEnvironment.getInternalSdState(context));
        }
        if (external) {
            return Environment.MEDIA_MOUNTED.equals(OppoUsbEnvironment.getExternalSdState(context));
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
                int eventType = parser.getEventType();
                while (true) {
                    boolean z = true;
                    if (eventType != 1) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                if (parser.getName().equals("path")) {
                                    parser.next();
                                    internal = parser.getText().equals(MediaStore.VOLUME_INTERNAL);
                                    if (!parser.getText().equals(MediaStore.VOLUME_EXTERNAL)) {
                                        z = false;
                                    }
                                    external = z;
                                }
                            }
                        }
                        eventType = parser.next();
                    }
                    try {
                        permReader.close();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (XmlPullParserException e2) {
                Slog.w(TAG, "Got execption parsing permissions.", e2);
            } catch (IOException e3) {
                Slog.w(TAG, "Got execption parsing permissions.", e3);
            }
        } catch (FileNotFoundException e4) {
            Slog.w(TAG, "Couldn't find or open apk_install file " + permFile);
        }
    }
}

package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;
import android.util.Xml;
import com.android.internal.net.VpnConfig;
import com.android.internal.notification.SystemNotificationChannels;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoVpnHelper {
    private static final String GAME_SPACE_PACKAGE_NAME = "com.coloros.gamespace";
    private static final boolean LOGD = true;
    private static final String TAG = "Vpn";
    private static final String TAG_PACKAGE = "package";
    private static final String mPath = "/data/oppo/coloros/permission/vpn_filter.xml";
    private Context mContext;
    private final int mVPNNotificationID = 43690;

    public OppoVpnHelper(Context ctx) {
        this.mContext = ctx;
    }

    public PendingIntent prepareStatusIntent(PendingIntent statusIntent) {
        long token = Binder.clearCallingIdentity();
        try {
            return VpnConfig.getIntentForStatusPanel(this.mContext);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void showNotification(String label, int icon_place_holder, int user_handler, String cur_package, PendingIntent statusIntent, VpnConfig config) {
        String text;
        Log.i(TAG, "agentConnect : mUserHandle = " + user_handler);
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        if (nm != null) {
            String title = this.mContext.getString(17041215);
            if (config.session == null) {
                text = this.mContext.getString(17041213);
            } else {
                text = this.mContext.getString(17041214, config.session);
            }
            Notification notification = new Notification.Builder(this.mContext, SystemNotificationChannels.VPN).setSmallIcon(17303773).setContentTitle(title).setContentText(text).setContentIntent(statusIntent).setDefaults(0).setOngoing(true).build();
            Log.i(TAG, "isUidSystem(Binder.getCallingUid()): " + isUidSystem(Binder.getCallingUid()));
            if (!isUidSystem(Binder.getCallingUid())) {
                long uid = Binder.clearCallingIdentity();
                nm.notifyAsUser(null, 43690, notification, new UserHandle(user_handler));
                Binder.restoreCallingIdentity(uid);
            } else if (!cur_package.equals(GAME_SPACE_PACKAGE_NAME)) {
                nm.notifyAsUser(null, 43690, notification, new UserHandle(user_handler));
            } else {
                Log.d(TAG, "The pkg is special, do not show notification! ");
            }
        }
    }

    public void hideNotification(int user_handler) {
        Log.i(TAG, "agentDisconnect : mUserHandle = " + user_handler);
        Log.i(TAG, "hideNotification: user_handler " + user_handler);
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        if (nm == null) {
            return;
        }
        if (isUidSystem(Binder.getCallingUid())) {
            nm.cancelAsUser(null, 43690, new UserHandle(user_handler));
            return;
        }
        long uid = Binder.clearCallingIdentity();
        nm.cancelAsUser(null, 43690, new UserHandle(user_handler));
        Binder.restoreCallingIdentity(uid);
    }

    public VpnConfig parseApplicationsFromXml(VpnConfig config) {
        String content = readFromFile(mPath);
        if (content == null) {
            return config;
        }
        if (config.disallowedApplications == null) {
            config.disallowedApplications = new ArrayList();
        }
        FileReader xmlReader = null;
        StringReader strReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            StringReader strReader2 = new StringReader(content);
            parser.setInput(strReader2);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String name = parser.getName();
                        parser.next();
                        String value = parser.getText();
                        if ("package".equals(name) && !config.disallowedApplications.contains(value)) {
                            config.disallowedApplications.add(value);
                        }
                    }
                }
            }
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            strReader2.close();
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
            if (xmlReader != null) {
                xmlReader.close();
            }
            if (strReader != null) {
                strReader.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (xmlReader != null) {
                xmlReader.close();
            }
            if (strReader != null) {
                strReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            if (strReader != null) {
                strReader.close();
            }
            throw th;
        }
        Log.i(TAG, "config.disallowedApplications = " + config.disallowedApplications);
        return config;
    }

    private String readFromFile(String pathStr) {
        File path = new File(pathStr);
        if (!path.exists()) {
            Log.i(TAG, pathStr + " not exist!");
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line);
            }
            String stringBuffer = buffer.toString();
            try {
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (is == null) {
                return null;
            }
            is.close();
            return null;
        } catch (IOException e3) {
            e3.printStackTrace();
            if (is == null) {
                return null;
            }
            try {
                is.close();
                return null;
            } catch (IOException e4) {
                e4.printStackTrace();
                return null;
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    private static boolean isUidSystem(int uid) {
        int appid = UserHandle.getAppId(uid);
        return appid == 1000 || appid == 1001 || uid == 0;
    }
}

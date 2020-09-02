package com.android.server.pm;

import android.content.Context;
import android.util.ArraySet;
import android.util.Slog;
import com.oppo.util.RomUpdateHelper;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class BlackAppInstallHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/black_app_install.xml";
    public static final String FILTER_NAME = "safe_packageinstall_list";
    private static final String SYS_FILE_DIR = "system/etc/black_app_install.xml";
    static final String TAG = "BlackAppInstallHelper";
    /* access modifiers changed from: private */
    public ArraySet<String> mBlackList = new ArraySet<>();

    private class BlackAppInstallUpdateInfo extends RomUpdateHelper.UpdateInfo {
        private static final String BLACK_INSTALL_APP_LIST = "black";

        public BlackAppInstallUpdateInfo() {
            super(BlackAppInstallHelper.this);
        }

        public void parseContentFromXML(String content) {
            int type;
            if (content != null && !content.isEmpty()) {
                ArrayList arrayList = null;
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setInput(new StringReader(content));
                    parser.nextTag();
                    do {
                        type = parser.next();
                        if (type == 2 && BLACK_INSTALL_APP_LIST.equals(parser.getName())) {
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                            }
                            String pkgTmp = parser.nextText().trim();
                            if (pkgTmp != null && pkgTmp.length() > 0) {
                                arrayList.add(pkgTmp);
                            }
                        }
                    } while (type != 1);
                    if (arrayList != null && !arrayList.isEmpty()) {
                        BlackAppInstallHelper.this.mBlackList.clear();
                        BlackAppInstallHelper.this.mBlackList.addAll(arrayList);
                        arrayList.clear();
                        Slog.e(BlackAppInstallHelper.TAG, "new mBlackList is " + BlackAppInstallHelper.this.mBlackList);
                    }
                } catch (Exception e) {
                    Slog.e(BlackAppInstallHelper.TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public BlackAppInstallHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new BlackAppInstallUpdateInfo(), new BlackAppInstallUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArraySet<String> getBlackAppList() {
        return this.mBlackList;
    }
}

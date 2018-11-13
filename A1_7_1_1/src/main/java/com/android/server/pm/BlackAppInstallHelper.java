package com.android.server.pm;

import android.content.Context;
import android.util.ArraySet;
import android.util.Slog;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class BlackAppInstallHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/black_app_install.xml";
    public static final String FILTER_NAME = "safe_packageinstall_list";
    private static final String SYS_FILE_DIR = "system/etc/black_app_install.xml";
    static final String TAG = "BlackAppInstallHelper";
    private ArraySet<String> mBlackList = new ArraySet();

    private class BlackAppInstallUpdateInfo extends UpdateInfo {
        private static final String BLACK_INSTALL_APP_LIST = "black";

        public BlackAppInstallUpdateInfo() {
            super(BlackAppInstallHelper.this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:34:0x0052 A:{SYNTHETIC, EDGE_INSN: B:34:0x0052->B:23:0x0052 ?: BREAK  } */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0050 A:{LOOP_END, LOOP:0: B:7:0x001f->B:22:0x0050} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            Exception e;
            if (content != null && !content.isEmpty()) {
                List<String> tmpList = null;
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setInput(new StringReader(content));
                    parser.nextTag();
                    while (true) {
                        List<String> tmpList2;
                        try {
                            tmpList2 = tmpList;
                            int type = parser.next();
                            if (type == 2) {
                                if (BLACK_INSTALL_APP_LIST.equals(parser.getName())) {
                                    if (tmpList2 == null) {
                                        tmpList = new ArrayList();
                                    } else {
                                        tmpList = tmpList2;
                                    }
                                    String pkgTmp = parser.nextText().trim();
                                    if (pkgTmp != null && pkgTmp.length() > 0) {
                                        tmpList.add(pkgTmp);
                                    }
                                    if (type != 1) {
                                        break;
                                    }
                                }
                            }
                            tmpList = tmpList2;
                            if (type != 1) {
                            }
                        } catch (Exception e2) {
                            e = e2;
                            tmpList = tmpList2;
                            Slog.e(BlackAppInstallHelper.TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if (!(tmpList == null || tmpList.isEmpty())) {
                        BlackAppInstallHelper.this.mBlackList.clear();
                        BlackAppInstallHelper.this.mBlackList.addAll(tmpList);
                        tmpList.clear();
                        Slog.e(BlackAppInstallHelper.TAG, "new mBlackList is " + BlackAppInstallHelper.this.mBlackList);
                    }
                } catch (Exception e3) {
                    e = e3;
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

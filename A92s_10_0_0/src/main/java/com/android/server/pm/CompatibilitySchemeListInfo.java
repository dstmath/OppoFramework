package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.wm.ActivityTaskManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CompatibilitySchemeListInfo {
    private static final int SCHEME_BEFORE_N = 0;
    private static final int SCHEME_OF_N = 1;
    private static final int SCHEME_OF_O = 2;
    public static final String TAG_CP = "CompatibilityHelper";
    private final ArrayList<CompatibilitySchemeInfo> mAllSchemeList = new ArrayList<CompatibilitySchemeInfo>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass1 */

        {
            add(new CompatibilitySchemeInfo("1160512", 0, null));
            add(new CompatibilitySchemeInfo("1160473", 0, null));
            add(new CompatibilitySchemeInfo("1160176", 0, null));
            add(new CompatibilitySchemeInfo("1160167", 0, null));
            add(new CompatibilitySchemeInfo("1160154", 0, null));
            add(new CompatibilitySchemeInfo("1160037", 0, null));
            add(new CompatibilitySchemeInfo("1160024", 0, null));
            add(new CompatibilitySchemeInfo("1160002", 0, null));
            add(new CompatibilitySchemeInfo("1159995", 0, null));
            add(new CompatibilitySchemeInfo("1159672", 0, null));
            add(new CompatibilitySchemeInfo("1159531", 0, null));
            add(new CompatibilitySchemeInfo("1144146", 0, null));
            add(new CompatibilitySchemeInfo("1137052", 0, null));
            add(new CompatibilitySchemeInfo("1137026", 0, null));
            add(new CompatibilitySchemeInfo("1137003", 0, null));
            add(new CompatibilitySchemeInfo("1136971", 0, null));
            add(new CompatibilitySchemeInfo("1136826", 0, null));
            add(new CompatibilitySchemeInfo("1162231", 0, null));
            add(new CompatibilitySchemeInfo("1287420", 2, null));
            add(new CompatibilitySchemeInfo("1289480", 2, null));
            add(new CompatibilitySchemeInfo("1268350", 1, null));
            add(new CompatibilitySchemeInfo("1255453", 1, null));
            add(new CompatibilitySchemeInfo("1136685", 1, null));
            add(new CompatibilitySchemeInfo("1137056", 1, null));
            add(new CompatibilitySchemeInfo("1137047", 1, null));
            add(new CompatibilitySchemeInfo("1137040", 1, null));
            add(new CompatibilitySchemeInfo("1136999", 1, null));
            add(new CompatibilitySchemeInfo("1136955", 1, null));
            add(new CompatibilitySchemeInfo("1136929", 1, null));
            add(new CompatibilitySchemeInfo("1136844", 1, null));
            add(new CompatibilitySchemeInfo("1136833", 1, null));
            add(new CompatibilitySchemeInfo("1136814", 1, null));
            add(new CompatibilitySchemeInfo("1136800", 1, null));
            add(new CompatibilitySchemeInfo("1136786", 1, null));
            add(new CompatibilitySchemeInfo("1136726", 1, null));
            add(new CompatibilitySchemeInfo("1136702", 1, null));
            add(new CompatibilitySchemeInfo("1136668", 1, null));
            add(new CompatibilitySchemeInfo("1202241", 2, null));
            add(new CompatibilitySchemeInfo("1202199", 2, null));
            add(new CompatibilitySchemeInfo("1246076", 2, null));
            add(new CompatibilitySchemeInfo("1289261", 2, null));
            add(new CompatibilitySchemeInfo("1289511", 2, null));
            add(new CompatibilitySchemeInfo("1305650", 2, null));
            add(new CompatibilitySchemeInfo("1323851", 2, null));
            add(new CompatibilitySchemeInfo("1327078", 2, null));
            add(new CompatibilitySchemeInfo("1347705", 2, null));
            add(new CompatibilitySchemeInfo("1390149", 2, null));
            add(new CompatibilitySchemeInfo("1466491", 2, null));
            add(new CompatibilitySchemeInfo("1526441", 2, null));
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<CompatibilitySchemeInfo> mMsm8953SchemeList = new ArrayList<CompatibilitySchemeInfo>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass6 */

        {
            add(new CompatibilitySchemeInfo("107753", 0, null));
            add(new CompatibilitySchemeInfo("108753", 1, null));
            add(new CompatibilitySchemeInfo("109753", 2, null));
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<CompatibilitySchemeInfo> mMt6763SchemeList = new ArrayList<CompatibilitySchemeInfo>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass5 */

        {
            add(new CompatibilitySchemeInfo("107753", 0, null));
            add(new CompatibilitySchemeInfo("108753", 1, null));
            add(new CompatibilitySchemeInfo("109753", 2, null));
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<CompatibilitySchemeInfo> mMt6771SchemeList = new ArrayList<CompatibilitySchemeInfo>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass3 */

        {
            add(new CompatibilitySchemeInfo("1159531", 0, null));
            add(new CompatibilitySchemeInfo("1160512", 0, null));
            add(new CompatibilitySchemeInfo("1144146", 0, null));
            add(new CompatibilitySchemeInfo("1159672", 0, null));
            add(new CompatibilitySchemeInfo("1160037", 0, null));
            add(new CompatibilitySchemeInfo("1136971", 0, null));
            add(new CompatibilitySchemeInfo("1160002", 0, null));
            add(new CompatibilitySchemeInfo("1159995", 0, null));
            add(new CompatibilitySchemeInfo("1160176", 0, null));
            add(new CompatibilitySchemeInfo("1136826", 0, null));
            add(new CompatibilitySchemeInfo("1160024", 0, null));
            add(new CompatibilitySchemeInfo("1160154", 0, null));
            add(new CompatibilitySchemeInfo("1160473", 0, null));
            add(new CompatibilitySchemeInfo("1137026", 0, null));
            add(new CompatibilitySchemeInfo("1137052", 0, null));
            add(new CompatibilitySchemeInfo("1268350", 1, null));
            add(new CompatibilitySchemeInfo("1287420", 2, null));
            add(new CompatibilitySchemeInfo("1255453", 1, null));
            add(new CompatibilitySchemeInfo("1137056", 1, null));
            add(new CompatibilitySchemeInfo("1136999", 1, null));
            add(new CompatibilitySchemeInfo("1136726", 1, null));
            add(new CompatibilitySchemeInfo("1136814", 1, null));
            add(new CompatibilitySchemeInfo("1137040", 1, null));
            add(new CompatibilitySchemeInfo("1136955", 1, null));
            add(new CompatibilitySchemeInfo("1136786", 1, null));
            add(new CompatibilitySchemeInfo("1136833", 1, null));
            add(new CompatibilitySchemeInfo("1137047", 1, null));
            add(new CompatibilitySchemeInfo("1136668", 1, null));
            add(new CompatibilitySchemeInfo("1136844", 1, null));
            add(new CompatibilitySchemeInfo("1136800", 1, null));
            add(new CompatibilitySchemeInfo("1202241", 2, null));
            add(new CompatibilitySchemeInfo("1202199", 2, null));
            add(new CompatibilitySchemeInfo("1246076", 2, null));
            add(new CompatibilitySchemeInfo("1289261", 2, null));
            add(new CompatibilitySchemeInfo("1289511", 2, null));
            add(new CompatibilitySchemeInfo("1289480", 2, null));
            add(new CompatibilitySchemeInfo("1305650", 2, null));
            add(new CompatibilitySchemeInfo("1323851", 2, null));
            add(new CompatibilitySchemeInfo("1327078", 2, null));
            add(new CompatibilitySchemeInfo("1347705", 2, null));
            add(new CompatibilitySchemeInfo("1390149", 2, null));
            add(new CompatibilitySchemeInfo("1466491", 2, null));
        }
    };
    private final Map<String, ArrayList<CompatibilitySchemeInfo>> mPlfListMap = new HashMap<String, ArrayList<CompatibilitySchemeInfo>>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass7 */

        {
            put("sdm660", CompatibilitySchemeListInfo.this.mSdm660SchemeList);
            put("mt6763", CompatibilitySchemeListInfo.this.mMt6763SchemeList);
            put("msm8953", CompatibilitySchemeListInfo.this.mMsm8953SchemeList);
            put("mt6771", CompatibilitySchemeListInfo.this.mMt6771SchemeList);
            put("sdm845", CompatibilitySchemeListInfo.this.mSdm845SchemeList);
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<CompatibilitySchemeInfo> mSdm660SchemeList = new ArrayList<CompatibilitySchemeInfo>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass2 */

        {
            add(new CompatibilitySchemeInfo("1160512", 0, null));
            add(new CompatibilitySchemeInfo("1160473", 0, null));
            add(new CompatibilitySchemeInfo("1160176", 0, null));
            add(new CompatibilitySchemeInfo("1160154", 0, null));
            add(new CompatibilitySchemeInfo("1160037", 0, null));
            add(new CompatibilitySchemeInfo("1160024", 0, null));
            add(new CompatibilitySchemeInfo("1160002", 0, null));
            add(new CompatibilitySchemeInfo("1159995", 0, null));
            add(new CompatibilitySchemeInfo("1159672", 0, null));
            add(new CompatibilitySchemeInfo("1159531", 0, null));
            add(new CompatibilitySchemeInfo("1144146", 0, null));
            add(new CompatibilitySchemeInfo("1137026", 0, null));
            add(new CompatibilitySchemeInfo("1136971", 0, null));
            add(new CompatibilitySchemeInfo("1136826", 0, null));
            add(new CompatibilitySchemeInfo("1137052", 0, null));
            add(new CompatibilitySchemeInfo("1162231", 0, null));
            add(new CompatibilitySchemeInfo("1255453", 1, null));
            add(new CompatibilitySchemeInfo("1268350", 1, null));
            add(new CompatibilitySchemeInfo("1136685", 1, null));
            add(new CompatibilitySchemeInfo("1137040", 1, null));
            add(new CompatibilitySchemeInfo("1136999", 1, null));
            add(new CompatibilitySchemeInfo("1136955", 1, null));
            add(new CompatibilitySchemeInfo("1136929", 1, null));
            add(new CompatibilitySchemeInfo("1136844", 1, null));
            add(new CompatibilitySchemeInfo("1136833", 1, null));
            add(new CompatibilitySchemeInfo("1136814", 1, null));
            add(new CompatibilitySchemeInfo("1136786", 1, null));
            add(new CompatibilitySchemeInfo("1136726", 1, null));
            add(new CompatibilitySchemeInfo("1136668", 1, null));
            add(new CompatibilitySchemeInfo("1137047", 1, null));
            add(new CompatibilitySchemeInfo("1136800", 1, null));
            add(new CompatibilitySchemeInfo("1137056", 1, null));
            add(new CompatibilitySchemeInfo("1287420", 2, null));
            add(new CompatibilitySchemeInfo("1202241", 2, null));
            add(new CompatibilitySchemeInfo("1202199", 2, null));
            add(new CompatibilitySchemeInfo("1246076", 2, null));
            add(new CompatibilitySchemeInfo("1289261", 2, null));
            add(new CompatibilitySchemeInfo("1289511", 2, null));
            add(new CompatibilitySchemeInfo("1305650", 2, null));
            add(new CompatibilitySchemeInfo("1323851", 2, null));
            add(new CompatibilitySchemeInfo("1327078", 2, null));
            add(new CompatibilitySchemeInfo("1347705", 2, null));
            add(new CompatibilitySchemeInfo("1390149", 2, null));
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<CompatibilitySchemeInfo> mSdm845SchemeList = new ArrayList<CompatibilitySchemeInfo>() {
        /* class com.android.server.pm.CompatibilitySchemeListInfo.AnonymousClass4 */

        {
            add(new CompatibilitySchemeInfo("1159531", 0, null));
            add(new CompatibilitySchemeInfo("1160512", 0, null));
            add(new CompatibilitySchemeInfo("1144146", 0, null));
            add(new CompatibilitySchemeInfo("1159672", 0, null));
            add(new CompatibilitySchemeInfo("1160037", 0, null));
            add(new CompatibilitySchemeInfo("1136971", 0, null));
            add(new CompatibilitySchemeInfo("1160002", 0, null));
            add(new CompatibilitySchemeInfo("1159995", 0, null));
            add(new CompatibilitySchemeInfo("1160176", 0, null));
            add(new CompatibilitySchemeInfo("1136826", 0, null));
            add(new CompatibilitySchemeInfo("1160024", 0, null));
            add(new CompatibilitySchemeInfo("1160154", 0, null));
            add(new CompatibilitySchemeInfo("1160473", 0, null));
            add(new CompatibilitySchemeInfo("1137026", 0, null));
            add(new CompatibilitySchemeInfo("1137052", 0, null));
            add(new CompatibilitySchemeInfo("1268350", 1, null));
            add(new CompatibilitySchemeInfo("1287420", 2, null));
            add(new CompatibilitySchemeInfo("1255453", 1, null));
            add(new CompatibilitySchemeInfo("1137056", 1, null));
            add(new CompatibilitySchemeInfo("1136999", 1, null));
            add(new CompatibilitySchemeInfo("1136726", 1, null));
            add(new CompatibilitySchemeInfo("1136814", 1, null));
            add(new CompatibilitySchemeInfo("1137040", 1, null));
            add(new CompatibilitySchemeInfo("1136955", 1, null));
            add(new CompatibilitySchemeInfo("1136786", 1, null));
            add(new CompatibilitySchemeInfo("1136833", 1, null));
            add(new CompatibilitySchemeInfo("1137047", 1, null));
            add(new CompatibilitySchemeInfo("1136668", 1, null));
            add(new CompatibilitySchemeInfo("1136844", 1, null));
            add(new CompatibilitySchemeInfo("1136800", 1, null));
            add(new CompatibilitySchemeInfo("1202241", 2, null));
            add(new CompatibilitySchemeInfo("1202199", 2, null));
            add(new CompatibilitySchemeInfo("1289261", 2, null));
            add(new CompatibilitySchemeInfo("1289480", 2, null));
            add(new CompatibilitySchemeInfo("1305650", 2, null));
            add(new CompatibilitySchemeInfo("1390149", 2, null));
            add(new CompatibilitySchemeInfo("1314009", 2, null));
            add(new CompatibilitySchemeInfo("1433671", 2, null));
            add(new CompatibilitySchemeInfo("1314010", 2, null));
            add(new CompatibilitySchemeInfo("1314011", 2, null));
            add(new CompatibilitySchemeInfo("1450009", 2, null));
            add(new CompatibilitySchemeInfo("1526441", 2, null));
        }
    };

    /* access modifiers changed from: private */
    public class CompatibilitySchemeInfo {
        public int mAndVer;
        public String mPlfInfo;
        public String mSID;

        public CompatibilitySchemeInfo(String sId, int andVer, String plfInfo) {
            this.mSID = sId;
            this.mAndVer = andVer;
            this.mPlfInfo = plfInfo;
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        String cmd = null;
        if (args.length > 1) {
            cmd = args[1];
        }
        if (cmd == null) {
            dumpAll(pw);
            dumpCurrent(pw);
        } else if (ActivityTaskManagerService.DUMP_ACTIVITIES_SHORT_CMD.equals(cmd) || "all".equals(cmd)) {
            dumpAll(pw);
        } else if ("c".equals(cmd) || "current".equals(cmd)) {
            dumpCurrent(pw);
        } else if ("H".equals(cmd) || "history".equals(cmd)) {
            dumpEachVersion(pw, 0);
        } else if ("N".equals(cmd) || "Nougat".equals(cmd)) {
            dumpEachVersion(pw, 1);
        } else if ("O".equals(cmd) || "Oreo".equals(cmd)) {
            dumpEachVersion(pw, 2);
        } else {
            pw.println("invalid parameters!!!");
        }
    }

    private void dumpEachVersion(PrintWriter pw, int version) {
        ArrayList<CompatibilitySchemeInfo> tmp = this.mAllSchemeList;
        StringBuilder dumpMsg = new StringBuilder();
        dumpMsg.append(CompatibilityHelper.convertAndroid2String(version));
        dumpMsg.append(" list:\n");
        Iterator<CompatibilitySchemeInfo> it = tmp.iterator();
        while (it.hasNext()) {
            CompatibilitySchemeInfo info = it.next();
            if (info.mAndVer == version) {
                dumpMsg.append("[ ");
                dumpMsg.append(info.mSID);
                dumpMsg.append(", ");
                dumpMsg.append(CompatibilityHelper.convertAndroid2String(info.mAndVer));
                dumpMsg.append(", ");
                dumpMsg.append(info.mPlfInfo);
                dumpMsg.append(" ]\n");
            }
        }
        pw.println(dumpMsg.toString());
    }

    private void dumpAll(PrintWriter pw) {
        ArrayList<CompatibilitySchemeInfo> tmp = this.mAllSchemeList;
        StringBuilder dumpMsg = new StringBuilder();
        dumpMsg.append("-------------All scheme list------------\n");
        Iterator<CompatibilitySchemeInfo> it = tmp.iterator();
        while (it.hasNext()) {
            CompatibilitySchemeInfo info = it.next();
            dumpMsg.append("[ ");
            dumpMsg.append(info.mSID);
            dumpMsg.append(", ");
            dumpMsg.append(CompatibilityHelper.convertAndroid2String(info.mAndVer));
            dumpMsg.append(", ");
            dumpMsg.append(info.mPlfInfo);
            dumpMsg.append(" ]\n");
        }
        pw.println(dumpMsg.toString());
    }

    private void dumpCurrent(PrintWriter pw) {
        ArrayList<CompatibilitySchemeInfo> tmp = this.mPlfListMap.get(SystemProperties.get("ro.board.platform", ""));
        if (tmp == null) {
            Slog.e("CompatibilityHelper", "can not get the platform information!!!");
            return;
        }
        StringBuilder dumpMsg = new StringBuilder();
        dumpMsg.append("-------------Current scheme list------------\n");
        Iterator<CompatibilitySchemeInfo> it = tmp.iterator();
        while (it.hasNext()) {
            CompatibilitySchemeInfo info = it.next();
            dumpMsg.append("[ ");
            dumpMsg.append(info.mSID);
            dumpMsg.append(", ");
            dumpMsg.append(CompatibilityHelper.convertAndroid2String(info.mAndVer));
            dumpMsg.append(", ");
            dumpMsg.append(info.mPlfInfo);
            dumpMsg.append(" ]\n");
        }
        pw.println(dumpMsg.toString());
    }
}

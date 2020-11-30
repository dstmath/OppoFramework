package com.color.content;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class ColorRuleInfo implements Parcelable {
    private static final String BLACK_STR = "black";
    private static final int CHOICE_BLACK = 1;
    private static final int CHOICE_INVALID = -1;
    private static final int CHOICE_WHITE = 0;
    public static final Parcelable.Creator<ColorRuleInfo> CREATOR = new Parcelable.Creator<ColorRuleInfo>() {
        /* class com.color.content.ColorRuleInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorRuleInfo createFromParcel(Parcel source) {
            return new ColorRuleInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorRuleInfo[] newArray(int size) {
            return new ColorRuleInfo[size];
        }
    };
    private static boolean DEBUG = DEBUG_PANIC;
    private static boolean DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String INVALID_STR = "invalid";
    private static final String NAME_STR = "name";
    private static final String TAG = "CII_ColorRuleInfo";
    private static final String TAG_INTENT_FILETER = "intent-filter";
    private static final String TAG_SOURCE_PKG_BLACK = "source-pkg-black";
    private static final String TAG_SOURCE_PKG_CHOICE = "source-pkg-choice";
    private static final String TAG_SOURCE_PKG_WHITE = "source-pkg-white";
    private static final String TAG_TARGET_CPN = "target-cpn";
    private static final String WHITE_STR = "white";
    public IntentFilter mIntentFilter;
    public List<String> mSourcePkgBlackList;
    public int mSourcePkgChoice;
    public List<String> mSourcePkgWhiteList;
    public List<String> mTargetComponentList;

    public static void setDebugEnable(boolean enable) {
        DEBUG = enable;
    }

    private static String getStringByChoice(int choice) {
        if (choice == -1) {
            return INVALID_STR;
        }
        if (choice == 0) {
            return WHITE_STR;
        }
        if (choice != 1) {
            return INVALID_STR;
        }
        return BLACK_STR;
    }

    private static int getIntByChoice(String choice) {
        if (WHITE_STR.equals(choice)) {
            return 0;
        }
        if (BLACK_STR.equals(choice)) {
            return 1;
        }
        return INVALID_STR.equals(choice) ? -1 : -1;
    }

    public ColorRuleInfo() {
        this.mSourcePkgChoice = -1;
        this.mTargetComponentList = new ArrayList();
        this.mSourcePkgWhiteList = new ArrayList();
        this.mSourcePkgBlackList = new ArrayList();
    }

    public ColorRuleInfo(ColorRuleInfo cri) {
        this.mSourcePkgChoice = -1;
        if (cri != null) {
            this.mTargetComponentList = new ArrayList(cri.mTargetComponentList);
            this.mSourcePkgChoice = cri.mSourcePkgChoice;
            this.mSourcePkgWhiteList = new ArrayList(cri.mSourcePkgWhiteList);
            this.mSourcePkgBlackList = new ArrayList(cri.mSourcePkgBlackList);
            this.mIntentFilter = new IntentFilter(cri.mIntentFilter);
        }
    }

    public ColorRuleInfo(List<String> targetCpnList, int choice, List<String> sourcePkgWhiteList, List<String> sourcePkgBlackList, IntentFilter intentfilter) {
        this.mSourcePkgChoice = -1;
        this.mTargetComponentList = targetCpnList;
        this.mSourcePkgChoice = choice;
        this.mSourcePkgWhiteList = sourcePkgWhiteList;
        this.mSourcePkgBlackList = sourcePkgBlackList;
        this.mIntentFilter = intentfilter;
    }

    public boolean needIntercept(String cpn, String callingPkg, Intent intent) {
        boolean intercept = matchTargetCpn(cpn) && matchSourcePkg(callingPkg) && matchIntent(intent);
        if (DEBUG) {
            Slog.i(TAG, "needIntercept intercept = " + intercept);
        }
        return intercept;
    }

    private boolean matchTargetCpn(String cpn) {
        boolean match = false;
        List<String> list = this.mTargetComponentList;
        if (list != null && list.contains(cpn)) {
            match = true;
        }
        if (DEBUG) {
            Slog.i(TAG, "matchTargetCpn cpn = " + cpn + "match = " + match);
        }
        return match;
    }

    private boolean matchSourcePkg(String callingPkg) {
        boolean match = false;
        int i = this.mSourcePkgChoice;
        if (i == 0) {
            List<String> list = this.mSourcePkgWhiteList;
            if (list != null && list.contains(callingPkg)) {
                match = true;
            }
        } else if (i == 1) {
            List<String> list2 = this.mSourcePkgBlackList;
            if (list2 != null && !list2.contains(callingPkg)) {
                match = true;
            }
        } else {
            match = false;
        }
        if (DEBUG) {
            Slog.i(TAG, "matchSourcePkg callingPkg = " + callingPkg + " choice = " + getStringByChoice(this.mSourcePkgChoice) + " match = " + match);
        }
        return match;
    }

    private boolean matchIntent(Intent intent) {
        boolean matchAction = true;
        boolean matchScheme = true;
        IntentFilter intentFilter = this.mIntentFilter;
        if (!(intentFilter == null || intent == null)) {
            if (intentFilter.countActions() > 0) {
                matchAction = this.mIntentFilter.matchAction(intent.getAction());
            }
            if (this.mIntentFilter.countDataSchemes() > 0) {
                matchScheme = intent.getData() != null ? this.mIntentFilter.hasDataScheme(intent.getData().getScheme()) : false;
            }
        }
        boolean match = matchAction && matchScheme;
        if (DEBUG) {
            Slog.i(TAG, "matchIntent matchAction = " + matchAction + " matchScheme = " + matchScheme);
        }
        return match;
    }

    public ColorRuleInfo(Parcel source) {
        this.mSourcePkgChoice = -1;
        int length = source.readInt();
        this.mTargetComponentList = new ArrayList();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                this.mTargetComponentList.add(source.readString());
            }
        }
        this.mSourcePkgChoice = source.readInt();
        int length2 = source.readInt();
        this.mSourcePkgWhiteList = new ArrayList();
        if (length2 > 0) {
            for (int i2 = 0; i2 < length2; i2++) {
                this.mSourcePkgWhiteList.add(source.readString());
            }
        }
        int length3 = source.readInt();
        this.mSourcePkgBlackList = new ArrayList();
        if (length3 > 0) {
            for (int i3 = 0; i3 < length3; i3++) {
                this.mSourcePkgBlackList.add(source.readString());
            }
        }
        if (source.readInt() != 0) {
            this.mIntentFilter = IntentFilter.CREATOR.createFromParcel(source);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        List<String> list = this.mTargetComponentList;
        if (list != null) {
            int N = list.size();
            dest.writeInt(N);
            for (int i = 0; i < N; i++) {
                dest.writeString(this.mTargetComponentList.get(i));
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mSourcePkgChoice);
        List<String> list2 = this.mSourcePkgWhiteList;
        if (list2 != null) {
            int N2 = list2.size();
            dest.writeInt(N2);
            for (int i2 = 0; i2 < N2; i2++) {
                dest.writeString(this.mSourcePkgWhiteList.get(i2));
            }
        } else {
            dest.writeInt(0);
        }
        List<String> list3 = this.mSourcePkgBlackList;
        if (list3 != null) {
            int N3 = list3.size();
            dest.writeInt(N3);
            for (int i3 = 0; i3 < N3; i3++) {
                dest.writeString(this.mSourcePkgBlackList.get(i3));
            }
        } else {
            dest.writeInt(0);
        }
        if (this.mIntentFilter != null) {
            dest.writeInt(1);
            this.mIntentFilter.writeToParcel(dest, flags);
            return;
        }
        dest.writeInt(0);
    }

    public void writeToXml(XmlSerializer serializer) throws IOException {
        List<String> list = this.mTargetComponentList;
        if (list != null && list.size() > 0) {
            for (String cpn : this.mTargetComponentList) {
                serializer.startTag(null, TAG_TARGET_CPN);
                serializer.attribute(null, "name", cpn);
                serializer.endTag(null, TAG_TARGET_CPN);
            }
        }
        serializer.startTag(null, TAG_SOURCE_PKG_CHOICE);
        serializer.attribute(null, "name", getStringByChoice(this.mSourcePkgChoice));
        serializer.endTag(null, TAG_SOURCE_PKG_CHOICE);
        List<String> list2 = this.mSourcePkgWhiteList;
        if (list2 != null && list2.size() > 0) {
            for (String pkgName : this.mSourcePkgWhiteList) {
                serializer.startTag(null, TAG_SOURCE_PKG_WHITE);
                serializer.attribute(null, "name", pkgName);
                serializer.endTag(null, TAG_SOURCE_PKG_WHITE);
            }
        }
        List<String> list3 = this.mSourcePkgBlackList;
        if (list3 != null && list3.size() > 0) {
            for (String pkgName2 : this.mSourcePkgBlackList) {
                serializer.startTag(null, TAG_SOURCE_PKG_BLACK);
                serializer.attribute(null, "name", pkgName2);
                serializer.endTag(null, TAG_SOURCE_PKG_BLACK);
            }
        }
        if (this.mIntentFilter != null) {
            serializer.startTag(null, TAG_INTENT_FILETER);
            this.mIntentFilter.writeToXml(serializer);
            serializer.endTag(null, TAG_INTENT_FILETER);
        }
    }

    public void readFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (DEBUG) {
                    Slog.i(TAG, "readFromXml tagName = " + tagName);
                }
                if (tagName.equals(TAG_TARGET_CPN)) {
                    addTargetCpn(parser.getAttributeValue(null, "name"));
                } else if (tagName.equals(TAG_SOURCE_PKG_CHOICE)) {
                    setSourcePkgChoice(parser.getAttributeValue(null, "name"));
                } else if (tagName.equals(TAG_SOURCE_PKG_WHITE)) {
                    addSourcePkgWhite(parser.getAttributeValue(null, "name"));
                } else if (tagName.equals(TAG_SOURCE_PKG_BLACK)) {
                    addSourcePkgBlack(parser.getAttributeValue(null, "name"));
                } else if (tagName.equals(TAG_INTENT_FILETER)) {
                    IntentFilter filter = new IntentFilter();
                    filter.readFromXml(parser);
                    setIntentFilter(filter);
                } else {
                    Slog.e(TAG, "Unknown tag parsing ColorRuleInfo : " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void addTargetCpn(String cpn) {
        if (DEBUG) {
            Slog.i(TAG, "addTargetCpn cpn = " + cpn);
        }
        if (this.mTargetComponentList == null) {
            this.mTargetComponentList = new ArrayList();
        }
        if (!this.mTargetComponentList.contains(cpn)) {
            this.mTargetComponentList.add(cpn);
        }
    }

    private void setSourcePkgChoice(String choice) {
        if (DEBUG) {
            Slog.i(TAG, "setSourcePkgChoice choice = " + choice);
        }
        this.mSourcePkgChoice = getIntByChoice(choice);
    }

    private void addSourcePkgWhite(String name) {
        if (DEBUG) {
            Slog.i(TAG, "addSourcePkgWhite name = " + name);
        }
        if (this.mSourcePkgWhiteList == null) {
            this.mSourcePkgWhiteList = new ArrayList();
        }
        if (!this.mSourcePkgWhiteList.contains(name)) {
            this.mSourcePkgWhiteList.add(name);
        }
    }

    private void addSourcePkgBlack(String name) {
        if (DEBUG) {
            Slog.i(TAG, "addSourcePkgBlack name = " + name);
        }
        if (this.mSourcePkgBlackList == null) {
            this.mSourcePkgBlackList = new ArrayList();
        }
        if (!this.mSourcePkgBlackList.contains(name)) {
            this.mSourcePkgBlackList.add(name);
        }
    }

    private void setIntentFilter(IntentFilter filter) {
        if (DEBUG) {
            Slog.i(TAG, "setIntentFilter filter = " + filter);
        }
        this.mIntentFilter = filter;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(128);
        toShortString(b);
        return b.toString();
    }

    private void toShortString(StringBuilder b) {
        if (this.mTargetComponentList != null) {
            b.append(" targetcpn = [");
            Iterator<String> it = this.mTargetComponentList.iterator();
            while (it.hasNext()) {
                b.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + it.next() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            b.append("] ");
        }
        b.append(" choice = " + getStringByChoice(this.mSourcePkgChoice));
        if (this.mSourcePkgWhiteList != null) {
            b.append(" sourcewhite = [");
            Iterator<String> it2 = this.mSourcePkgWhiteList.iterator();
            while (it2.hasNext()) {
                b.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + it2.next() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            b.append("] ");
        }
        if (this.mSourcePkgBlackList != null) {
            b.append(" sourceblack = [");
            Iterator<String> it3 = this.mSourcePkgBlackList.iterator();
            while (it3.hasNext()) {
                b.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + it3.next() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            b.append("] ");
        }
        IntentFilter intentFilter = this.mIntentFilter;
        if (intentFilter != null) {
            b.append(intentFilter.toString());
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("DEBUG = " + DEBUG);
        List<String> list = this.mTargetComponentList;
        if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                pw.print(prefix);
                pw.println("targetCpn = " + it.next());
            }
        }
        pw.print(prefix);
        pw.println("Choice = " + getStringByChoice(this.mSourcePkgChoice));
        List<String> list2 = this.mSourcePkgWhiteList;
        if (list2 != null) {
            Iterator<String> it2 = list2.iterator();
            while (it2.hasNext()) {
                pw.print(prefix);
                pw.println("sourcePkgWhite = " + it2.next());
            }
        }
        List<String> list3 = this.mSourcePkgBlackList;
        if (list3 != null) {
            Iterator<String> it3 = list3.iterator();
            while (it3.hasNext()) {
                pw.print(prefix);
                pw.println("sourcePkgBlack = " + it3.next());
            }
        }
        pw.print(prefix);
        pw.println("Intent-Filter:");
        IntentFilter intentFilter = this.mIntentFilter;
        if (intentFilter != null) {
            PrintWriterPrinter printWriterPrinter = new PrintWriterPrinter(pw);
            intentFilter.dump(printWriterPrinter, prefix + "  ");
        }
    }
}

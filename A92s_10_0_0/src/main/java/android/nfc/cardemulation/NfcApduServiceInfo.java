package android.nfc.cardemulation;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Xml;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NfcApduServiceInfo extends ApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<NfcApduServiceInfo> CREATOR = new Parcelable.Creator<NfcApduServiceInfo>() {
        /* class android.nfc.cardemulation.NfcApduServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NfcApduServiceInfo createFromParcel(Parcel source) {
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            boolean onHost = source.readInt() != 0;
            ArrayList<NfcAidGroup> staticNfcAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(staticNfcAidGroups, NfcAidGroup.CREATOR);
            }
            ArrayList<NfcAidGroup> dynamicNfcAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(dynamicNfcAidGroups, NfcAidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            ESeInfo seExtension = ESeInfo.CREATOR.createFromParcel(source);
            new byte[1][0] = 0;
            source.createByteArray();
            NfcApduServiceInfo service = new NfcApduServiceInfo(info, onHost, description, staticNfcAidGroups, dynamicNfcAidGroups, requiresUnlock, bannerResource, uid, settingsActivityName, seExtension, source.readInt() != 0);
            service.setServiceState("other", source.readInt());
            return service;
        }

        @Override // android.os.Parcelable.Creator
        public NfcApduServiceInfo[] newArray(int size) {
            return new NfcApduServiceInfo[size];
        }
    };
    static final String NXP_NFC_EXT_META_DATA = "com.nxp.nfc.extensions";
    static final int POWER_STATE_BATTERY_OFF = 4;
    static final int POWER_STATE_SWITCH_OFF = 2;
    static final int POWER_STATE_SWITCH_ON = 1;
    static final String SECURE_ELEMENT_ESE = "eSE";
    public static final int SECURE_ELEMENT_ROUTE_ESE = 1;
    public static final int SECURE_ELEMENT_ROUTE_UICC = 2;
    public static final int SECURE_ELEMENT_ROUTE_UICC2 = 4;
    static final String SECURE_ELEMENT_SIM = "SIM";
    static final String SECURE_ELEMENT_UICC = "UICC";
    static final String SECURE_ELEMENT_UICC2 = "UICC2";
    static final String TAG = "NfcApduServiceInfo";
    byte[] mByteArrayBanner;
    final HashMap<String, NfcAidGroup> mDynamicNfcAidGroups;
    final boolean mModifiable;
    final ESeInfo mSeExtension;
    int mServiceState;
    final HashMap<String, NfcAidGroup> mStaticNfcAidGroups;

    public NfcApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<NfcAidGroup> staticNfcAidGroups, ArrayList<NfcAidGroup> dynamicNfcAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName, ESeInfo seExtension, boolean modifiable) {
        super(info, description, nfcAidGroups2AidGroups(staticNfcAidGroups), nfcAidGroups2AidGroups(dynamicNfcAidGroups), requiresUnlock, bannerResource, uid, settingsActivityName, (String) null, (String) null);
        this.mByteArrayBanner = null;
        this.mModifiable = modifiable;
        this.mServiceState = 2;
        this.mStaticNfcAidGroups = new HashMap<>();
        this.mDynamicNfcAidGroups = new HashMap<>();
        if (staticNfcAidGroups != null) {
            Iterator<NfcAidGroup> it = staticNfcAidGroups.iterator();
            while (it.hasNext()) {
                NfcAidGroup nfcAidGroup = it.next();
                this.mStaticNfcAidGroups.put(nfcAidGroup.getCategory(), nfcAidGroup);
            }
        }
        if (dynamicNfcAidGroups != null) {
            Iterator<NfcAidGroup> it2 = dynamicNfcAidGroups.iterator();
            while (it2.hasNext()) {
                NfcAidGroup nfcAidGroup2 = it2.next();
                this.mDynamicNfcAidGroups.put(nfcAidGroup2.getCategory(), nfcAidGroup2);
            }
        }
        this.mSeExtension = seExtension;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0189, code lost:
        throw new org.xmlpull.v1.XmlPullParserException("Unsupported se name: " + r10);
     */
    public NfcApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        super(pm, info, onHost);
        XmlResourceParser parser;
        int i;
        boolean powerValue;
        this.mByteArrayBanner = null;
        this.mModifiable = false;
        this.mServiceState = 2;
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser2 = null;
        XmlResourceParser extParser = null;
        if (onHost) {
            try {
                parser = si.loadXmlMetaData(pm, "android.nfc.cardemulation.host_apdu_service");
                if (parser == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.host_apdu_service meta-data");
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
            } catch (Throwable th) {
                if (parser2 != null) {
                    parser2.close();
                }
                throw th;
            }
        } else {
            parser = si.loadXmlMetaData(pm, "android.nfc.cardemulation.off_host_apdu_service");
            if (parser != null) {
                extParser = si.loadXmlMetaData(pm, NXP_NFC_EXT_META_DATA);
                if (extParser == null) {
                    Log.d(TAG, "No com.nxp.nfc.extensions meta-data");
                }
            } else {
                throw new XmlPullParserException("No android.nfc.cardemulation.off_host_apdu_service meta-data");
            }
        }
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        String tagName = parser.getName();
        if (onHost) {
            if (!"host-apdu-service".equals(tagName)) {
                throw new XmlPullParserException("Meta-data does not start with <host-apdu-service> tag");
            }
        }
        if (!onHost) {
            if (!"offhost-apdu-service".equals(tagName)) {
                throw new XmlPullParserException("Meta-data does not start with <offhost-apdu-service> tag");
            }
        }
        pm.getResourcesForApplication(si.applicationInfo);
        Xml.asAttributeSet(parser);
        this.mStaticNfcAidGroups = new HashMap<>();
        this.mDynamicNfcAidGroups = new HashMap<>();
        for (Map.Entry<String, AidGroup> stringaidgroup : this.mStaticAidGroups.entrySet()) {
            this.mStaticNfcAidGroups.put(stringaidgroup.getKey(), new NfcAidGroup(stringaidgroup.getValue()));
        }
        for (Iterator it = this.mDynamicAidGroups.entrySet().iterator(); it.hasNext(); it = it) {
            Map.Entry<String, AidGroup> stringaidgroup2 = (Map.Entry) it.next();
            this.mDynamicNfcAidGroups.put(stringaidgroup2.getKey(), new NfcAidGroup(stringaidgroup2.getValue()));
        }
        parser.close();
        if (extParser != null) {
            try {
                int eventType2 = extParser.getEventType();
                int depth = extParser.getDepth();
                String seName = null;
                int powerState = 0;
                while (eventType2 != 2 && eventType2 != 1) {
                    eventType2 = extParser.next();
                }
                String tagName2 = extParser.getName();
                if ("extensions".equals(tagName2)) {
                    while (true) {
                        int eventType3 = extParser.next();
                        if ((eventType3 != 3 || extParser.getDepth() > depth) && eventType3 != 1) {
                            String tagName3 = extParser.getName();
                            if (eventType3 == 2 && "se-id".equals(tagName3)) {
                                seName = extParser.getAttributeValue(null, "name");
                                if (seName == null || (!seName.equalsIgnoreCase(SECURE_ELEMENT_ESE) && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC) && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC2) && !seName.equalsIgnoreCase(SECURE_ELEMENT_SIM))) {
                                }
                            } else if (eventType3 == 2) {
                                if ("se-power-state".equals(tagName3)) {
                                    String powerName = extParser.getAttributeValue(null, "name");
                                    if (extParser.getAttributeValue(null, "value").equals("true")) {
                                        powerValue = true;
                                    } else {
                                        powerValue = false;
                                    }
                                    if (powerName.equalsIgnoreCase("SwitchOn") && powerValue) {
                                        powerState |= 1;
                                    } else if (powerName.equalsIgnoreCase("SwitchOff") && powerValue) {
                                        powerState |= 2;
                                    } else if (powerName.equalsIgnoreCase("BatteryOff") && powerValue) {
                                        powerState |= 4;
                                    }
                                }
                            }
                        }
                    }
                    if (seName != null) {
                        if (seName.equals(SECURE_ELEMENT_ESE)) {
                            i = 1;
                        } else if (seName.equals(SECURE_ELEMENT_UICC) || seName.equals(SECURE_ELEMENT_SIM)) {
                            i = 2;
                        } else {
                            i = 4;
                        }
                        this.mSeExtension = new ESeInfo(i, powerState);
                        Log.d(TAG, this.mSeExtension.toString());
                    } else {
                        this.mSeExtension = new ESeInfo(-1, 0);
                        Log.d(TAG, this.mSeExtension.toString());
                    }
                    return;
                }
                throw new XmlPullParserException("Meta-data does not start with <extensions> tag " + tagName2);
            } finally {
                extParser.close();
            }
        } else if (!onHost) {
            Log.e(TAG, "SE extension not present, Setting default offhost seID");
            this.mSeExtension = new ESeInfo(2, 0);
        } else {
            this.mSeExtension = new ESeInfo(-1, 0);
        }
    }

    static ArrayList<AidGroup> nfcAidGroups2AidGroups(ArrayList<NfcAidGroup> nfcAidGroup) {
        ArrayList<AidGroup> aidGroups = new ArrayList<>();
        if (nfcAidGroup != null) {
            Iterator<NfcAidGroup> it = nfcAidGroup.iterator();
            while (it.hasNext()) {
                aidGroups.add(it.next().createAidGroup());
            }
        }
        return aidGroups;
    }

    public void writeToXml(XmlSerializer out) throws IOException {
        String modifiable;
        out.attribute(null, "description", this.mDescription);
        if (this.mModifiable) {
            modifiable = "true";
        } else {
            modifiable = "false";
        }
        out.attribute(null, "modifiable", modifiable);
        out.attribute(null, "uid", Integer.toString(this.mUid));
        out.attribute(null, "seId", Integer.toString(this.mSeExtension.seId));
        out.attribute(null, "bannerId", Integer.toString(this.mBannerResourceId));
        Iterator<NfcAidGroup> it = this.mDynamicNfcAidGroups.values().iterator();
        while (it.hasNext()) {
            it.next().writeAsXml(out);
        }
    }

    public ResolveInfo getResolveInfo() {
        return this.mService;
    }

    public ArrayList<String> getAids() {
        ArrayList<String> aids = new ArrayList<>();
        Iterator<NfcAidGroup> it = getNfcAidGroups().iterator();
        while (it.hasNext()) {
            aids.addAll(it.next().getAids());
        }
        return aids;
    }

    public ArrayList<NfcAidGroup> getNfcAidGroups() {
        ArrayList<NfcAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, NfcAidGroup> entry : this.mDynamicNfcAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, NfcAidGroup> entry2 : this.mStaticNfcAidGroups.entrySet()) {
            if (!this.mDynamicNfcAidGroups.containsKey(entry2.getKey())) {
                groups.add(entry2.getValue());
            }
        }
        return groups;
    }

    public ApduServiceInfo createApduServiceInfo() {
        return new ApduServiceInfo(getResolveInfo(), getDescription(), nfcAidGroups2AidGroups(getStaticNfcAidGroups()), nfcAidGroups2AidGroups(getDynamicNfcAidGroups()), requiresUnlock(), getBannerId(), getUid(), getSettingsActivityName(), (String) null, (String) null);
    }

    public ApduServiceInfo createApduServiceInfo(String category) {
        return new ApduServiceInfo(getResolveInfo(), getDescription(), nfcAidGroups2AidGroups(getStaticNfcAidGroups()), nfcAidGroups2AidGroups(getDynamicNfcAidGroups()), requiresUnlock(), getBannerId(), getUid(), getSettingsActivityName(), (String) null, (String) null, this.mServiceState, this.mByteArrayBanner);
    }

    public int getAidCacheSize(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getAidCacheSizeForCategory("other");
    }

    public int getAidCacheSizeForCategory(String category) {
        List<String> aids;
        ArrayList<NfcAidGroup> nfcAidGroups = new ArrayList<>();
        int aidCacheSize = 0;
        nfcAidGroups.addAll(getStaticNfcAidGroups());
        nfcAidGroups.addAll(getDynamicNfcAidGroups());
        if (nfcAidGroups.size() == 0) {
            return 0;
        }
        Iterator<NfcAidGroup> it = nfcAidGroups.iterator();
        while (it.hasNext()) {
            NfcAidGroup aidCache = it.next();
            if (!(!aidCache.getCategory().equals(category) || (aids = aidCache.getAids()) == null || aids.size() == 0)) {
                for (String aid : aids) {
                    int aidLen = aid.length();
                    if (aid.endsWith("*")) {
                        aidLen--;
                    }
                    aidCacheSize += aidLen >> 1;
                }
            }
        }
        return aidCacheSize;
    }

    public int geTotalAidNum(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getTotalAidNumCategory("other");
    }

    private int getTotalAidNumCategory(String category) {
        List<String> aids;
        ArrayList<NfcAidGroup> aidGroups = new ArrayList<>();
        int aidTotalNum = 0;
        aidGroups.addAll(getStaticNfcAidGroups());
        aidGroups.addAll(getDynamicNfcAidGroups());
        if (aidGroups.size() == 0) {
            return 0;
        }
        Iterator<NfcAidGroup> it = aidGroups.iterator();
        while (it.hasNext()) {
            NfcAidGroup aidCache = it.next();
            if (!(!aidCache.getCategory().equals(category) || (aids = aidCache.getAids()) == null || aids.size() == 0)) {
                for (String aid : aids) {
                    if (aid != null && aid.length() > 0) {
                        aidTotalNum++;
                    }
                }
            }
        }
        return aidTotalNum;
    }

    public ArrayList<NfcAidGroup> getStaticNfcAidGroups() {
        ArrayList<NfcAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, NfcAidGroup> entry : this.mStaticNfcAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ArrayList<NfcAidGroup> getDynamicNfcAidGroups() {
        ArrayList<NfcAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, NfcAidGroup> entry : this.mDynamicNfcAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public String getOtherAidGroupDescription() {
        String otherAidGroupDescription = null;
        if (this.mStaticNfcAidGroups.containsKey("other")) {
            otherAidGroupDescription = this.mStaticNfcAidGroups.get("other").description;
        } else if (this.mDynamicNfcAidGroups.containsKey("other")) {
            otherAidGroupDescription = this.mDynamicNfcAidGroups.get("other").description;
        } else {
            Log.e(TAG, "getOtherAidGroupDescription: Aid Group with OTHER category not available");
        }
        Log.e(TAG, "getOtherAidGroupDescription: " + otherAidGroupDescription);
        return otherAidGroupDescription;
    }

    public ESeInfo getSEInfo() {
        return this.mSeExtension;
    }

    public boolean getModifiable() {
        return this.mModifiable;
    }

    public Bitmap getBitmapBanner() {
        byte[] bArr = this.mByteArrayBanner;
        if (bArr == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
    }

    public void setOrReplaceDynamicNfcAidGroup(NfcAidGroup nfcAidGroup) {
        NfcApduServiceInfo.super.setOrReplaceDynamicAidGroup(nfcAidGroup);
        this.mDynamicNfcAidGroups.put(nfcAidGroup.getCategory(), nfcAidGroup);
    }

    public NfcAidGroup getDynamicNfcAidGroupForCategory(String category) {
        return this.mDynamicNfcAidGroups.get(category);
    }

    public boolean removeDynamicNfcAidGroupForCategory(String category) {
        NfcApduServiceInfo.super.removeDynamicAidGroupForCategory(category);
        return this.mDynamicNfcAidGroups.remove(category) != null;
    }

    public Drawable loadBanner(PackageManager pm) {
        try {
            Resources res = pm.getResourcesForApplication(this.mService.serviceInfo.packageName);
            if (this.mBannerResourceId == -1) {
                return new BitmapDrawable(getBitmapBanner());
            }
            return res.getDrawable(this.mBannerResourceId, null);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public int getBannerId() {
        return this.mBannerResourceId;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(getComponent());
        out.append(", description: " + this.mDescription);
        out.append(", Static AID Groups: ");
        for (NfcAidGroup nfcAidGroup : this.mStaticNfcAidGroups.values()) {
            out.append(nfcAidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (NfcAidGroup nfcAidGroup2 : this.mDynamicNfcAidGroups.values()) {
            out.append(nfcAidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NfcApduServiceInfo)) {
            return false;
        }
        return ((NfcApduServiceInfo) o).getComponent().equals(getComponent());
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeInt(this.mStaticNfcAidGroups.size());
        if (this.mStaticNfcAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticNfcAidGroups.values()));
        }
        dest.writeInt(this.mDynamicNfcAidGroups.size());
        if (this.mDynamicNfcAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicNfcAidGroups.values()));
        }
        dest.writeInt(this.mRequiresDeviceUnlock ? 1 : 0);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
        dest.writeString(this.mSettingsActivityName);
        this.mSeExtension.writeToParcel(dest, flags);
        dest.writeByteArray(this.mByteArrayBanner);
        dest.writeInt(this.mModifiable ? 1 : 0);
        dest.writeInt(this.mServiceState);
    }

    public boolean isServiceEnabled(String category) {
        int i;
        if (!category.equals("other") || (i = this.mServiceState) == 1 || i == 3) {
            return true;
        }
        return false;
    }

    public void enableService(String category, boolean flagEnable) {
        if (category == "other") {
            Log.d(TAG, "setServiceState:Description:" + this.mDescription + ":InternalState:" + this.mServiceState + ":flagEnable:" + flagEnable);
            if (this.mServiceState != 1 || !flagEnable) {
                if (this.mServiceState == 0 && !flagEnable) {
                    return;
                }
                if (this.mServiceState == 3 && !flagEnable) {
                    return;
                }
                if (this.mServiceState != 2 || !flagEnable) {
                    if (this.mServiceState == 1 && !flagEnable) {
                        this.mServiceState = 3;
                    } else if (this.mServiceState == 0 && flagEnable) {
                        this.mServiceState = 2;
                    } else if (this.mServiceState == 3 && flagEnable) {
                        this.mServiceState = 1;
                    } else if (this.mServiceState == 2 && !flagEnable) {
                        this.mServiceState = 0;
                    }
                }
            }
        }
    }

    public int getServiceState(String category) {
        if (!category.equals("other")) {
            return 1;
        }
        return this.mServiceState;
    }

    public int setServiceState(String category, int state) {
        if (!category.equals("other")) {
            return 1;
        }
        this.mServiceState = state;
        return this.mServiceState;
    }

    public void updateServiceCommitStatus(String category, boolean commitStatus) {
        if (category.equals("other")) {
            Log.d(TAG, "updateServiceCommitStatus:Description:" + this.mDescription + ":InternalState:" + this.mServiceState + ":commitStatus:" + commitStatus);
            if (commitStatus) {
                int i = this.mServiceState;
                if (i == 3) {
                    this.mServiceState = 0;
                } else if (i == 2) {
                    this.mServiceState = 1;
                }
            } else {
                int i2 = this.mServiceState;
                if (i2 == 3) {
                    this.mServiceState = 1;
                } else if (i2 == 2) {
                    this.mServiceState = 0;
                }
            }
        }
    }

    static String serviceStateToString(int state) {
        if (state == 0) {
            return "DISABLED";
        }
        if (state == 1) {
            return "ENABLED";
        }
        if (state == 2) {
            return "ENABLING";
        }
        if (state != 3) {
            return "UNKNOWN";
        }
        return "DISABLING";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        NfcApduServiceInfo.super.dump(fd, pw, args);
        StringBuilder sb = new StringBuilder();
        sb.append("    Routing Destination: ");
        sb.append(this.mOnHost ? "host" : "secure element");
        pw.println(sb.toString());
        if (hasCategory("other")) {
            pw.println("    Service State: " + serviceStateToString(this.mServiceState));
        }
    }

    public static class ESeInfo implements Parcelable {
        public static final Parcelable.Creator<ESeInfo> CREATOR = new Parcelable.Creator<ESeInfo>() {
            /* class android.nfc.cardemulation.NfcApduServiceInfo.ESeInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ESeInfo createFromParcel(Parcel source) {
                return new ESeInfo(source.readInt(), source.readInt());
            }

            @Override // android.os.Parcelable.Creator
            public ESeInfo[] newArray(int size) {
                return new ESeInfo[size];
            }
        };
        final int powerState;
        final int seId;

        public ESeInfo(int seId2, int powerState2) {
            this.seId = seId2;
            this.powerState = powerState2;
        }

        public int getSeId() {
            return this.seId;
        }

        public int getPowerState() {
            return this.powerState;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("seId: ");
            sb.append(this.seId);
            sb.append(",Power state: [switchOn: ");
            boolean z = true;
            sb.append((this.powerState & 1) != 0);
            sb.append(",switchOff: ");
            sb.append((this.powerState & 2) != 0);
            sb.append(",batteryOff: ");
            if ((this.powerState & 4) == 0) {
                z = false;
            }
            sb.append(z);
            sb.append("]");
            return new StringBuilder(sb.toString()).toString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.seId);
            dest.writeInt(this.powerState);
        }
    }
}

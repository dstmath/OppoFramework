package com.st.android.nfc_extensions;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public final class StApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<StApduServiceInfo> CREATOR = new Parcelable.Creator<StApduServiceInfo>() {
        /* class com.st.android.nfc_extensions.StApduServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StApduServiceInfo createFromParcel(Parcel source) {
            Drawable drawable;
            Bitmap bitmap;
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            boolean z = source.readInt() != 0;
            String offHostName = source.readString();
            String staticOffHostName = source.readString();
            ArrayList<StAidGroup> staticStAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(staticStAidGroups, StAidGroup.CREATOR);
            }
            ArrayList<StAidGroup> dynamicStAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(dynamicStAidGroups, StAidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            if (getClass().getClassLoader() == null || (bitmap = (Bitmap) source.readParcelable(getClass().getClassLoader())) == null) {
                drawable = null;
            } else {
                drawable = new BitmapDrawable(bitmap);
            }
            StApduServiceInfo service = new StApduServiceInfo(info, description, staticStAidGroups, dynamicStAidGroups, requiresUnlock, bannerResource, uid, settingsActivityName, offHostName, staticOffHostName, drawable);
            service.setServiceState("other", source.readInt());
            return service;
        }

        @Override // android.os.Parcelable.Creator
        public StApduServiceInfo[] newArray(int size) {
            return new StApduServiceInfo[size];
        }
    };
    static final boolean DBG = true;
    static final int POWER_STATE_BATTERY_OFF = 4;
    static final int POWER_STATE_SWITCH_OFF = 2;
    static final int POWER_STATE_SWITCH_ON = 1;
    static final String SECURE_ELEMENT_ALT_ESE = "eSE";
    static final String SECURE_ELEMENT_ALT_UICC = "UICC";
    static final String SECURE_ELEMENT_ALT_UICC2 = "UICC2";
    static final String SECURE_ELEMENT_ESE = "eSE1";
    public static final int SECURE_ELEMENT_ROUTE_ESE = 1;
    public static final int SECURE_ELEMENT_ROUTE_UICC = 2;
    public static final int SECURE_ELEMENT_ROUTE_UICC2 = 4;
    static final String SECURE_ELEMENT_UICC = "SIM";
    static final String SECURE_ELEMENT_UICC1 = "SIM1";
    static final String SECURE_ELEMENT_UICC2 = "SIM2";
    static final String TAG = "APINfc_StApduServiceInfo";
    public final Drawable mBanner;
    final int mBannerResourceId;
    final String mDescription;
    final HashMap<String, StAidGroup> mDynamicStAidGroups;
    String mOffHostName;
    final boolean mOnHost;
    final boolean mRequiresDeviceUnlock;
    final ResolveInfo mService;
    int mServiceState;
    final String mSettingsActivityName;
    final String mStaticOffHostName;
    final HashMap<String, StAidGroup> mStaticStAidGroups;
    final int mUid;
    boolean mWasAccounted;

    public StApduServiceInfo(ResolveInfo info, String description, ArrayList<StAidGroup> staticStAidGroups, ArrayList<StAidGroup> dynamicStAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName, String offHost, String staticOffHost, Drawable banner) {
        Log.d(TAG, "Constructor - offHost: " + offHost + ", description: " + description + ", nb of staticStAidGroups: " + staticStAidGroups.size() + ", nb of dynamicStAidGroups: " + dynamicStAidGroups.size());
        this.mService = info;
        this.mDescription = description;
        this.mStaticStAidGroups = new HashMap<>();
        this.mDynamicStAidGroups = new HashMap<>();
        this.mOffHostName = offHost;
        this.mStaticOffHostName = staticOffHost;
        this.mOnHost = offHost == null;
        this.mRequiresDeviceUnlock = requiresUnlock;
        this.mBanner = banner;
        this.mServiceState = 2;
        this.mWasAccounted = DBG;
        Iterator<StAidGroup> it = staticStAidGroups.iterator();
        while (it.hasNext()) {
            StAidGroup stAidGroup = it.next();
            this.mStaticStAidGroups.put(stAidGroup.getCategory(), stAidGroup);
        }
        Iterator<StAidGroup> it2 = dynamicStAidGroups.iterator();
        while (it2.hasNext()) {
            StAidGroup stAidGroup2 = it2.next();
            this.mDynamicStAidGroups.put(stAidGroup2.getCategory(), stAidGroup2);
        }
        this.mBannerResourceId = bannerResource;
        this.mUid = uid;
        this.mSettingsActivityName = settingsActivityName;
    }

    public StApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        XmlResourceParser parser;
        boolean z;
        int i;
        Log.d(TAG, "Constructor - onHost: " + onHost + ", reading from meta-data");
        this.mBanner = null;
        int i2 = 2;
        this.mServiceState = 2;
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser2 = null;
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
            if (parser == null) {
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
        Resources res = pm.getResourcesForApplication(si.applicationInfo);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        if (onHost) {
            TypedArray sa = res.obtainAttributes(attrs, R.styleable.HostApduService);
            this.mService = info;
            this.mDescription = sa.getString(0);
            this.mRequiresDeviceUnlock = sa.getBoolean(2, false);
            this.mBannerResourceId = sa.getResourceId(3, -1);
            this.mSettingsActivityName = sa.getString(1);
            this.mOffHostName = null;
            this.mStaticOffHostName = this.mOffHostName;
            sa.recycle();
        } else {
            TypedArray sa2 = res.obtainAttributes(attrs, R.styleable.OffHostApduService);
            this.mService = info;
            this.mDescription = sa2.getString(0);
            this.mRequiresDeviceUnlock = false;
            this.mBannerResourceId = sa2.getResourceId(2, -1);
            this.mSettingsActivityName = sa2.getString(1);
            this.mOffHostName = sa2.getString(3);
            if (this.mOffHostName != null) {
                if (this.mOffHostName.equals("eSE")) {
                    this.mOffHostName = SECURE_ELEMENT_ESE;
                } else if (this.mOffHostName.equals(SECURE_ELEMENT_UICC)) {
                    this.mOffHostName = "SIM1";
                }
            }
            this.mStaticOffHostName = this.mOffHostName;
            sa2.recycle();
        }
        this.mStaticStAidGroups = new HashMap<>();
        this.mDynamicStAidGroups = new HashMap<>();
        this.mOnHost = onHost;
        Log.d(TAG, "Constructor - mService: " + this.mService.resolvePackageName);
        Log.d(TAG, "Constructor - mDescription: " + this.mDescription);
        Log.d(TAG, "Constructor - mOffHostName: " + this.mOffHostName);
        int depth = parser.getDepth();
        StAidGroup currentGroup = null;
        while (true) {
            int eventType2 = parser.next();
            if ((eventType2 != 3 || parser.getDepth() > depth) && eventType2 != 1) {
                String tagName2 = parser.getName();
                if (eventType2 == i2 && "aid-group".equals(tagName2) && currentGroup == null) {
                    TypedArray groupAttrs = res.obtainAttributes(attrs, R.styleable.AidGroup);
                    String groupCategory = groupAttrs.getString(1);
                    String groupDescription = groupAttrs.getString(0);
                    String groupCategory2 = !"payment".equals(groupCategory) ? "other" : groupCategory;
                    Log.d(TAG, "Constructor - groupCategory: " + groupCategory2);
                    StAidGroup currentGroup2 = this.mStaticStAidGroups.get(groupCategory2);
                    if (currentGroup2 == null) {
                        currentGroup = new StAidGroup(groupCategory2, groupDescription);
                    } else if (!"other".equals(groupCategory2)) {
                        Log.e(TAG, "Not allowing multiple aid-groups in the " + groupCategory2 + " category");
                        currentGroup = null;
                    } else {
                        currentGroup = currentGroup2;
                    }
                    groupAttrs.recycle();
                    i2 = 2;
                } else if (eventType2 != 3 || !"aid-group".equals(tagName2) || currentGroup == null) {
                    if (eventType2 == 2 && "aid-filter".equals(tagName2) && currentGroup != null) {
                        TypedArray a = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        String aid = a.getString(0).toUpperCase();
                        if (!CardEmulation.isValidAid(aid) || currentGroup.aids.contains(aid)) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid);
                        } else {
                            currentGroup.aids.add(aid);
                        }
                        a.recycle();
                        i = 2;
                        z = false;
                    } else if (eventType2 != 2 || !"aid-prefix-filter".equals(tagName2) || currentGroup == null) {
                        i = 2;
                        if (eventType2 != 2) {
                            z = false;
                        } else if (!tagName2.equals("aid-suffix-filter") || currentGroup == null) {
                            z = false;
                        } else {
                            TypedArray a2 = res.obtainAttributes(attrs, R.styleable.AidFilter);
                            z = false;
                            String aid2 = a2.getString(0).toUpperCase().concat("#");
                            if (!CardEmulation.isValidAid(aid2) || currentGroup.aids.contains(aid2)) {
                                Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid2);
                            } else {
                                currentGroup.aids.add(aid2);
                            }
                            a2.recycle();
                        }
                    } else {
                        TypedArray a3 = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        String aid3 = a3.getString(0).toUpperCase().concat("*");
                        if (!CardEmulation.isValidAid(aid3) || currentGroup.aids.contains(aid3)) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid3);
                        } else {
                            currentGroup.aids.add(aid3);
                        }
                        a3.recycle();
                        i = 2;
                        z = false;
                    }
                    i2 = i;
                } else {
                    if (currentGroup.aids.size() <= 0) {
                        Log.w(TAG, "Not adding <aid-group> with empty or invalid AIDs");
                    } else if (!this.mStaticStAidGroups.containsKey(currentGroup.category)) {
                        this.mStaticStAidGroups.put(currentGroup.category, currentGroup);
                    }
                    currentGroup = null;
                    i2 = 2;
                }
            }
        }
        parser.close();
        this.mUid = si.applicationInfo.uid;
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public String getOffHostSecureElement() {
        return this.mOffHostName;
    }

    static ArrayList<AidGroup> stAidGroups2AidGroups(ArrayList<StAidGroup> stAidGroup) {
        ArrayList<AidGroup> aidGroups = new ArrayList<>();
        if (stAidGroup != null) {
            Iterator<StAidGroup> it = stAidGroup.iterator();
            while (it.hasNext()) {
                aidGroups.add(it.next().getAidGroup());
            }
        }
        return aidGroups;
    }

    public List<String> getAids() {
        ArrayList<String> aids = new ArrayList<>();
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            aids.addAll(it.next().aids);
        }
        return aids;
    }

    public List<String> getPrefixAids() {
        ArrayList<String> prefixAids = new ArrayList<>();
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            for (String aid : it.next().aids) {
                if (aid.endsWith("*")) {
                    prefixAids.add(aid);
                }
            }
        }
        return prefixAids;
    }

    public List<String> getSubsetAids() {
        ArrayList<String> subsetAids = new ArrayList<>();
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            for (String aid : it.next().aids) {
                if (aid.endsWith("#")) {
                    subsetAids.add(aid);
                }
            }
        }
        return subsetAids;
    }

    public StAidGroup getDynamicStAidGroupForCategory(String category) {
        return this.mDynamicStAidGroups.get(category);
    }

    public boolean removeDynamicStAidGroupForCategory(String category) {
        if (this.mDynamicStAidGroups.remove(category) != null) {
            return DBG;
        }
        return false;
    }

    public ArrayList<StAidGroup> getStAidGroups() {
        ArrayList<StAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StAidGroup> entry : this.mDynamicStAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, StAidGroup> entry2 : this.mStaticStAidGroups.entrySet()) {
            if (!this.mDynamicStAidGroups.containsKey(entry2.getKey())) {
                groups.add(entry2.getValue());
            }
        }
        return groups;
    }

    public ArrayList<AidGroup> getLegacyAidGroups() {
        return stAidGroups2AidGroups(getStAidGroups());
    }

    public String getCategoryForAid(String aid) {
        Iterator<StAidGroup> it = getStAidGroups().iterator();
        while (it.hasNext()) {
            StAidGroup group = it.next();
            if (group.aids.contains(aid.toUpperCase())) {
                return group.category;
            }
        }
        return null;
    }

    public boolean hasCategory(String category) {
        if (this.mStaticStAidGroups.containsKey(category) || this.mDynamicStAidGroups.containsKey(category)) {
            return DBG;
        }
        return false;
    }

    public boolean isOnHost() {
        return this.mOnHost;
    }

    public boolean requiresUnlock() {
        return this.mRequiresDeviceUnlock;
    }

    public String getDescription() {
        String str = this.mDescription;
        if (str != null) {
            return str;
        }
        return this.mService.serviceInfo.name;
    }

    public String getGsmaDescription() {
        String result = getDescription();
        String aidDescr = null;
        StAidGroup grp = this.mDynamicStAidGroups.get("other");
        if (grp == null) {
            StAidGroup grp2 = this.mStaticStAidGroups.get("other");
            if (!(grp2 == null || grp2.getDescription() == null)) {
                aidDescr = grp2.getDescription();
            }
        } else if (grp.getDescription() != null) {
            aidDescr = grp.getDescription();
        }
        if (aidDescr == null) {
            return result;
        }
        return result + " (" + aidDescr + ")";
    }

    public String getGsmaDescription(PackageManager pm) {
        CharSequence label = loadLabel(pm);
        if (label == null) {
            label = loadAppLabel(pm);
        }
        if (label == null) {
            return getGsmaDescription();
        }
        return label.toString() + " - " + getGsmaDescription();
    }

    public int getUid() {
        return this.mUid;
    }

    public void setOrReplaceDynamicStAidGroup(StAidGroup aidGroup) {
        this.mDynamicStAidGroups.put(aidGroup.getCategory(), aidGroup);
    }

    public void setOffHostSecureElement(String offHost) {
        this.mOffHostName = offHost;
    }

    public void unsetOffHostSecureElement() {
        this.mOffHostName = this.mStaticOffHostName;
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public CharSequence loadAppLabel(PackageManager pm) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(this.mService.resolvePackageName, 128));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public Drawable loadBanner(PackageManager pm) {
        if (this.mBannerResourceId == -1) {
            return this.mBanner;
        }
        try {
            Resources res = pm.getResourcesForApplication(this.mService.serviceInfo.packageName);
            if (res != null) {
                return res.getDrawable(this.mBannerResourceId);
            }
            return null;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner (name)");
            return null;
        }
    }

    public String getSettingsActivityName() {
        return this.mSettingsActivityName;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("StApduService: ");
        out.append(getComponent());
        out.append(", description: " + this.mDescription);
        out.append(", Static AID Groups: ");
        for (StAidGroup aidGroup : this.mStaticStAidGroups.values()) {
            out.append(aidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (StAidGroup aidGroup2 : this.mDynamicStAidGroups.values()) {
            out.append(aidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return DBG;
        }
        if (!(o instanceof StApduServiceInfo)) {
            return false;
        }
        return ((StApduServiceInfo) o).getComponent().equals(getComponent());
    }

    public int hashCode() {
        return getComponent().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public ResolveInfo getResolveInfo() {
        return this.mService;
    }

    public ApduServiceInfo createApduServiceInfo() {
        return new ApduServiceInfo(this.mService, getDescription(), stAidGroups2AidGroups(getStaticStAidGroups()), stAidGroups2AidGroups(getDynamicStAidGroups()), requiresUnlock(), getBannerId(), getUid(), getSettingsActivityName(), getOffHostSecureElement(), this.mStaticOffHostName);
    }

    public int getAidCacheSize(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getAidCacheSizeForCategory("other");
    }

    public int getAidCacheSizeForCategory(String category) {
        List<String> aids;
        ArrayList<StAidGroup> stAidGroups = new ArrayList<>();
        int aidCacheSize = 0;
        stAidGroups.add(this.mStaticStAidGroups.get(category));
        stAidGroups.add(this.mDynamicStAidGroups.get(category));
        Iterator<StAidGroup> it = stAidGroups.iterator();
        while (it.hasNext()) {
            StAidGroup aidCache = it.next();
            if (!(aidCache == null || (aids = aidCache.getAids()) == null)) {
                for (String aid : aids) {
                    if (aid != null) {
                        int aidLen = aid.length();
                        if (aid.endsWith("*") || aid.endsWith("#")) {
                            aidLen--;
                        }
                        aidCacheSize += aidLen >> 1;
                    }
                }
            }
        }
        return aidCacheSize;
    }

    public int getCatOthersAidSizeInLmrt() {
        return getAidCacheSizeForCategory("other") + (getTotalAidNum("other") * 4);
    }

    public int getTotalAidNum(String category) {
        if (!"other".equals(category) || !hasCategory("other")) {
            return 0;
        }
        return getTotalAidNumCategory("other");
    }

    private int getTotalAidNumCategory(String category) {
        List<String> aids;
        ArrayList<StAidGroup> aidGroups = new ArrayList<>();
        int aidTotalNum = 0;
        aidGroups.add(this.mStaticStAidGroups.get(category));
        aidGroups.add(this.mDynamicStAidGroups.get(category));
        Iterator<StAidGroup> it = aidGroups.iterator();
        while (it.hasNext()) {
            StAidGroup aidCache = it.next();
            if (!(aidCache == null || (aids = aidCache.getAids()) == null)) {
                for (String aid : aids) {
                    if (aid != null && aid.length() > 0) {
                        aidTotalNum++;
                    }
                }
            }
        }
        return aidTotalNum;
    }

    public ArrayList<StAidGroup> getStaticStAidGroups() {
        ArrayList<StAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StAidGroup> entry : this.mStaticStAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ArrayList<StAidGroup> getDynamicStAidGroups() {
        ArrayList<StAidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, StAidGroup> entry : this.mDynamicStAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public int getBannerId() {
        return this.mBannerResourceId;
    }

    public boolean isServiceEnabled(String category) {
        int i;
        if (category != "other" || (i = this.mServiceState) == 1 || i == 3) {
            return DBG;
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
        if (category != "other") {
            return 1;
        }
        return this.mServiceState;
    }

    public int setServiceState(String category, int state) {
        if (category != "other") {
            return 1;
        }
        this.mServiceState = state;
        return this.mServiceState;
    }

    public boolean getWasAccounted() {
        return this.mWasAccounted;
    }

    public void updateServiceCommitStatus(String category, boolean commitStatus) {
        if (category == "other") {
            Log.d(TAG, "updateServiceCommitStatus(enter) - Description: " + this.mDescription + ", InternalState: " + this.mServiceState + ", commitStatus: " + commitStatus);
            boolean z = false;
            if (commitStatus) {
                int i = this.mServiceState;
                if (i == 3) {
                    this.mServiceState = 0;
                } else if (i == 2) {
                    this.mServiceState = 1;
                }
                if (this.mServiceState == 1) {
                    z = true;
                }
                this.mWasAccounted = z;
            } else {
                int i2 = this.mServiceState;
                this.mWasAccounted = i2 == 1 || i2 == 2;
                int i3 = this.mServiceState;
                if (i3 == 3) {
                    this.mServiceState = 1;
                } else if (i3 == 2) {
                    this.mServiceState = 0;
                }
            }
            Log.d(TAG, "updateServiceCommitStatus(exit) - InternalState: " + this.mServiceState);
        }
    }

    public static String serviceStateToString(int state) {
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

    public void writeToParcel(Parcel dest, int flags) {
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeString(this.mOffHostName);
        dest.writeString(this.mStaticOffHostName);
        dest.writeInt(this.mStaticStAidGroups.size());
        if (this.mStaticStAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticStAidGroups.values()));
        }
        dest.writeInt(this.mDynamicStAidGroups.size());
        if (this.mDynamicStAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicStAidGroups.values()));
        }
        dest.writeInt(this.mRequiresDeviceUnlock ? 1 : 0);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
        dest.writeString(this.mSettingsActivityName);
        Drawable drawable = this.mBanner;
        if (drawable != null) {
            dest.writeParcelable(((BitmapDrawable) drawable).getBitmap(), flags);
        } else {
            dest.writeParcelable(null, flags);
        }
        dest.writeInt(this.mServiceState);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    " + getComponent() + " (Description: " + getDescription() + ")");
        if (this.mOnHost) {
            pw.println("    On Host Service");
        } else {
            pw.println("    Off-host Service");
            pw.println("        Current off-host SE" + this.mOffHostName + " static off-host: " + this.mOffHostName);
        }
        pw.println("    Static off-host Secure Element:");
        pw.println("    Static AID groups:");
        for (StAidGroup group : this.mStaticStAidGroups.values()) {
            pw.println("        Category: " + group.category);
            String desc = group.getDescription();
            if (desc != null) {
                pw.println("           Descr: " + desc);
            }
            Iterator<String> it = group.aids.iterator();
            while (it.hasNext()) {
                pw.println("            AID: " + it.next());
            }
        }
        pw.println("    Dynamic AID groups:");
        for (StAidGroup group2 : this.mDynamicStAidGroups.values()) {
            pw.println("        Category: " + group2.category);
            String desc2 = group2.getDescription();
            if (desc2 != null) {
                pw.println("           Descr: " + desc2);
            }
            Iterator<String> it2 = group2.aids.iterator();
            while (it2.hasNext()) {
                pw.println("            AID: " + it2.next());
            }
        }
        pw.println("    Settings Activity: " + this.mSettingsActivityName);
        if (hasCategory("other")) {
            pw.println("    Service State: " + serviceStateToString(this.mServiceState));
        }
    }
}

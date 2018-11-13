package android.nfc.cardemulation;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParserException;

public class ApduServiceInfo implements Parcelable {
    public static final Creator<ApduServiceInfo> CREATOR = new Creator<ApduServiceInfo>() {
        public ApduServiceInfo createFromParcel(Parcel source) {
            ResolveInfo info = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            boolean onHost = source.readInt() != 0;
            ArrayList<AidGroup> staticAidGroups = new ArrayList();
            if (source.readInt() > 0) {
                source.readTypedList(staticAidGroups, AidGroup.CREATOR);
            }
            ArrayList<AidGroup> dynamicAidGroups = new ArrayList();
            if (source.readInt() > 0) {
                source.readTypedList(dynamicAidGroups, AidGroup.CREATOR);
            }
            return new ApduServiceInfo(info, onHost, description, staticAidGroups, dynamicAidGroups, source.readInt() != 0, source.readInt(), source.readInt(), source.readString());
        }

        public ApduServiceInfo[] newArray(int size) {
            return new ApduServiceInfo[size];
        }
    };
    static final String TAG = "ApduServiceInfo";
    protected int mBannerResourceId;
    protected String mDescription;
    protected HashMap<String, AidGroup> mDynamicAidGroups;
    protected boolean mOnHost;
    protected boolean mRequiresDeviceUnlock;
    protected ResolveInfo mService;
    protected String mSettingsActivityName;
    protected HashMap<String, AidGroup> mStaticAidGroups;
    protected int mUid;

    public ApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<AidGroup> staticAidGroups, ArrayList<AidGroup> dynamicAidGroups, boolean requiresUnlock, int bannerResource, int uid, String settingsActivityName) {
        this.mService = info;
        this.mDescription = description;
        this.mStaticAidGroups = new HashMap();
        this.mDynamicAidGroups = new HashMap();
        this.mOnHost = onHost;
        this.mRequiresDeviceUnlock = requiresUnlock;
        for (AidGroup aidGroup : staticAidGroups) {
            this.mStaticAidGroups.put(aidGroup.category, aidGroup);
        }
        for (AidGroup aidGroup2 : dynamicAidGroups) {
            this.mDynamicAidGroups.put(aidGroup2.category, aidGroup2);
        }
        this.mBannerResourceId = bannerResource;
        this.mUid = uid;
        this.mSettingsActivityName = settingsActivityName;
    }

    public ApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser = null;
        if (onHost) {
            try {
                parser = si.loadXmlMetaData(pm, HostApduService.SERVICE_META_DATA);
                if (parser == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.host_apdu_service meta-data");
                }
            } catch (NameNotFoundException e) {
                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
            } catch (Throwable th) {
                if (parser != null) {
                    parser.close();
                }
            }
        } else {
            parser = si.loadXmlMetaData(pm, OffHostApduService.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No android.nfc.cardemulation.off_host_apdu_service meta-data");
            }
        }
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        String tagName = parser.getName();
        if (onHost && ("host-apdu-service".equals(tagName) ^ 1) != 0) {
            throw new XmlPullParserException("Meta-data does not start with <host-apdu-service> tag");
        } else if (onHost || ("offhost-apdu-service".equals(tagName) ^ 1) == 0) {
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            TypedArray sa;
            if (onHost) {
                sa = res.obtainAttributes(attrs, R.styleable.HostApduService);
                this.mService = info;
                this.mDescription = sa.getString(0);
                this.mRequiresDeviceUnlock = sa.getBoolean(2, false);
                this.mBannerResourceId = sa.getResourceId(3, -1);
                this.mSettingsActivityName = sa.getString(1);
                sa.recycle();
            } else {
                sa = res.obtainAttributes(attrs, R.styleable.OffHostApduService);
                this.mService = info;
                this.mDescription = sa.getString(0);
                this.mRequiresDeviceUnlock = false;
                this.mBannerResourceId = sa.getResourceId(2, -1);
                this.mSettingsActivityName = sa.getString(1);
                sa.recycle();
            }
            this.mStaticAidGroups = new HashMap();
            this.mDynamicAidGroups = new HashMap();
            this.mOnHost = onHost;
            int depth = parser.getDepth();
            AidGroup currentGroup = null;
            while (true) {
                eventType = parser.next();
                if ((eventType != 3 || parser.getDepth() > depth) && eventType != 1) {
                    tagName = parser.getName();
                    TypedArray a;
                    String aid;
                    if (eventType == 2 && "aid-group".equals(tagName) && currentGroup == null) {
                        TypedArray groupAttrs = res.obtainAttributes(attrs, R.styleable.AidGroup);
                        String groupCategory = groupAttrs.getString(1);
                        String groupDescription = groupAttrs.getString(0);
                        if (!CardEmulation.CATEGORY_PAYMENT.equals(groupCategory)) {
                            groupCategory = CardEmulation.CATEGORY_OTHER;
                        }
                        currentGroup = (AidGroup) this.mStaticAidGroups.get(groupCategory);
                        if (currentGroup == null) {
                            currentGroup = new AidGroup(groupCategory, groupDescription);
                        } else if (!CardEmulation.CATEGORY_OTHER.equals(groupCategory)) {
                            Log.e(TAG, "Not allowing multiple aid-groups in the " + groupCategory + " category");
                            currentGroup = null;
                        }
                        groupAttrs.recycle();
                    } else if (eventType == 3 && "aid-group".equals(tagName) && currentGroup != null) {
                        if (currentGroup.aids.size() <= 0) {
                            Log.e(TAG, "Not adding <aid-group> with empty or invalid AIDs");
                        } else if (!this.mStaticAidGroups.containsKey(currentGroup.category)) {
                            this.mStaticAidGroups.put(currentGroup.category, currentGroup);
                        }
                        currentGroup = null;
                    } else if (eventType == 2 && "aid-filter".equals(tagName) && currentGroup != null) {
                        a = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        aid = a.getString(0).toUpperCase();
                        if (!CardEmulation.isValidAid(aid) || (currentGroup.aids.contains(aid) ^ 1) == 0) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid);
                        } else {
                            currentGroup.aids.add(aid);
                        }
                        a.recycle();
                    } else if (eventType == 2 && "aid-prefix-filter".equals(tagName) && currentGroup != null) {
                        a = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        aid = a.getString(0).toUpperCase().concat("*");
                        if (!CardEmulation.isValidAid(aid) || (currentGroup.aids.contains(aid) ^ 1) == 0) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid);
                        } else {
                            currentGroup.aids.add(aid);
                        }
                        a.recycle();
                    } else if (eventType == 2 && tagName.equals("aid-suffix-filter") && currentGroup != null) {
                        a = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        aid = a.getString(0).toUpperCase().concat("#");
                        if (!CardEmulation.isValidAid(aid) || (currentGroup.aids.contains(aid) ^ 1) == 0) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid);
                        } else {
                            currentGroup.aids.add(aid);
                        }
                        a.recycle();
                    }
                }
            }
            if (parser != null) {
                parser.close();
            }
            this.mUid = si.applicationInfo.uid;
        } else {
            throw new XmlPullParserException("Meta-data does not start with <offhost-apdu-service> tag");
        }
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public List<String> getAids() {
        ArrayList<String> aids = new ArrayList();
        for (AidGroup group : getAidGroups()) {
            aids.addAll(group.aids);
        }
        return aids;
    }

    public List<String> getPrefixAids() {
        ArrayList<String> prefixAids = new ArrayList();
        for (AidGroup group : getAidGroups()) {
            for (String aid : group.aids) {
                if (aid.endsWith("*")) {
                    prefixAids.add(aid);
                }
            }
        }
        return prefixAids;
    }

    public List<String> getSubsetAids() {
        ArrayList<String> subsetAids = new ArrayList();
        for (AidGroup group : getAidGroups()) {
            for (String aid : group.aids) {
                if (aid.endsWith("#")) {
                    subsetAids.add(aid);
                }
            }
        }
        return subsetAids;
    }

    public AidGroup getDynamicAidGroupForCategory(String category) {
        return (AidGroup) this.mDynamicAidGroups.get(category);
    }

    public boolean removeDynamicAidGroupForCategory(String category) {
        return this.mDynamicAidGroups.remove(category) != null;
    }

    public ArrayList<AidGroup> getAidGroups() {
        ArrayList<AidGroup> groups = new ArrayList();
        for (Entry<String, AidGroup> entry : this.mDynamicAidGroups.entrySet()) {
            groups.add((AidGroup) entry.getValue());
        }
        for (Entry<String, AidGroup> entry2 : this.mStaticAidGroups.entrySet()) {
            if (!this.mDynamicAidGroups.containsKey(entry2.getKey())) {
                groups.add((AidGroup) entry2.getValue());
            }
        }
        return groups;
    }

    public String getCategoryForAid(String aid) {
        for (AidGroup group : getAidGroups()) {
            if (group.aids.contains(aid.toUpperCase())) {
                return group.category;
            }
        }
        return null;
    }

    public boolean hasCategory(String category) {
        return !this.mStaticAidGroups.containsKey(category) ? this.mDynamicAidGroups.containsKey(category) : true;
    }

    public boolean isOnHost() {
        return this.mOnHost;
    }

    public boolean requiresUnlock() {
        return this.mRequiresDeviceUnlock;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setOrReplaceDynamicAidGroup(AidGroup aidGroup) {
        this.mDynamicAidGroups.put(aidGroup.getCategory(), aidGroup);
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public CharSequence loadAppLabel(PackageManager pm) {
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(this.mService.resolvePackageName, 128));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public Drawable loadBanner(PackageManager pm) {
        try {
            return pm.getResourcesForApplication(this.mService.serviceInfo.packageName).getDrawable(this.mBannerResourceId);
        } catch (NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public String getSettingsActivityName() {
        return this.mSettingsActivityName;
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(getComponent());
        out.append(", description: ").append(this.mDescription);
        out.append(", Static AID Groups: ");
        for (AidGroup aidGroup : this.mStaticAidGroups.values()) {
            out.append(aidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (AidGroup aidGroup2 : this.mDynamicAidGroups.values()) {
            out.append(aidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ApduServiceInfo) {
            return ((ApduServiceInfo) o).getComponent().equals(getComponent());
        }
        return false;
    }

    public int hashCode() {
        return getComponent().hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeInt(this.mStaticAidGroups.size());
        if (this.mStaticAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticAidGroups.values()));
        }
        dest.writeInt(this.mDynamicAidGroups.size());
        if (this.mDynamicAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicAidGroups.values()));
        }
        if (!this.mRequiresDeviceUnlock) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
        dest.writeString(this.mSettingsActivityName);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    " + getComponent() + " (Description: " + getDescription() + ")");
        pw.println("    Static AID groups:");
        for (AidGroup group : this.mStaticAidGroups.values()) {
            pw.println("        Category: " + group.category);
            for (String aid : group.aids) {
                pw.println("            AID: " + aid);
            }
        }
        pw.println("    Dynamic AID groups:");
        for (AidGroup group2 : this.mDynamicAidGroups.values()) {
            pw.println("        Category: " + group2.category);
            for (String aid2 : group2.aids) {
                pw.println("            AID: " + aid2);
            }
        }
        pw.println("    Settings Activity: " + this.mSettingsActivityName);
    }
}

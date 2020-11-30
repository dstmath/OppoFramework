package android.nfc.cardemulation;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class NfcAidGroup extends AidGroup implements Parcelable {
    public static final Parcelable.Creator<NfcAidGroup> CREATOR = new Parcelable.Creator<NfcAidGroup>() {
        /* class android.nfc.cardemulation.NfcAidGroup.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NfcAidGroup createFromParcel(Parcel source) {
            String category = source.readString();
            int listSize = source.readInt();
            ArrayList<String> aidList = new ArrayList<>();
            if (listSize > 0) {
                source.readStringList(aidList);
            }
            String description = source.readString();
            if (aidList.size() == 0) {
                return new NfcAidGroup(category, description);
            }
            return new NfcAidGroup(aidList, category, description);
        }

        @Override // android.os.Parcelable.Creator
        public NfcAidGroup[] newArray(int size) {
            return new NfcAidGroup[size];
        }
    };
    static final String TAG = "NfcAidGroup";

    public NfcAidGroup(List<String> aids, String category, String description) {
        super(aids, category);
        this.description = description;
    }

    public NfcAidGroup(List<String> aids, String category) {
        super(aids, category);
    }

    public NfcAidGroup(String category, String description) {
        super(category, description);
    }

    public NfcAidGroup(AidGroup aid) {
        this(aid.getAids(), aid.getCategory(), getDescription(aid));
    }

    static String getDescription(AidGroup aid) {
        for (Field f : aid.getClass().getDeclaredFields()) {
            f.setAccessible(true);
        }
        return aid.description;
    }

    public AidGroup createAidGroup() {
        if (getAids() == null || getAids().isEmpty()) {
            Log.d(TAG, "Empty aid group creation");
            return new AidGroup(getCategory(), getDescription());
        }
        Log.d(TAG, "Non Empty aid group creation");
        return new AidGroup(getAids(), getCategory());
    }

    public String getDescription() {
        return this.description;
    }

    public void writeToParcel(Parcel dest, int flags) {
        NfcAidGroup.super.writeToParcel(dest, flags);
        if (this.description != null) {
            dest.writeString(this.description);
        } else {
            dest.writeString(null);
        }
    }

    public static NfcAidGroup createFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        String category = null;
        String description = null;
        ArrayList<String> aids = new ArrayList<>();
        boolean inGroup = false;
        int eventType = parser.getEventType();
        int minDepth = parser.getDepth();
        while (eventType != 1 && parser.getDepth() >= minDepth) {
            String tagName = parser.getName();
            if (eventType == 2) {
                if (tagName.equals("aid")) {
                    if (inGroup) {
                        String aid = parser.getAttributeValue(null, "value");
                        if (aid != null) {
                            aids.add(aid.toUpperCase());
                        }
                    } else {
                        Log.d(TAG, "Ignoring <aid> tag while not in group");
                    }
                } else if (tagName.equals("aid-group")) {
                    category = parser.getAttributeValue(null, "category");
                    description = parser.getAttributeValue(null, "description");
                    if (category == null) {
                        Log.e(TAG, "<aid-group> tag without valid category");
                        return null;
                    }
                    inGroup = true;
                } else {
                    Log.d(TAG, "Ignoring unexpected tag: " + tagName);
                }
            } else if (eventType == 3 && tagName.equals("aid-group") && inGroup) {
                if (aids.size() > 0) {
                    return new NfcAidGroup(aids, category, description);
                }
                return new NfcAidGroup(category, description);
            }
            eventType = parser.next();
        }
        return null;
    }

    public void writeAsXml(XmlSerializer out) throws IOException {
        out.startTag(null, "aid-group");
        out.attribute(null, "category", this.category);
        if (this.description != null) {
            out.attribute(null, "description", this.description);
        }
        for (String aid : this.aids) {
            out.startTag(null, "aid");
            out.attribute(null, "value", aid);
            out.endTag(null, "aid");
        }
        out.endTag(null, "aid-group");
    }
}

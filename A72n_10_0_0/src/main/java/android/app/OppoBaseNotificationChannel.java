package android.app;

import android.os.Parcel;
import android.text.TextUtils;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class OppoBaseNotificationChannel {
    private static final String ATT_BADGE_OPTION = "badge_option";
    private static final String ATT_CHANGEABLE_FOLD = "changeable_fold";
    private static final String ATT_CHANGEABLE_SHOW_ICON = "changeable_show_icon";
    private static final String ATT_FOLD = "fold";
    private static final String ATT_MAX_MESSAGES = "max_messages";
    private static final String ATT_OPUSH = "opush";
    private static final String ATT_SHOW_BANNER = "show_banner";
    private static final String ATT_SHOW_ICON = "show_icon";
    private static final String ATT_SUPPORT_NUM_BADGE = "support_num_badge";
    public static final int USER_BADGE_OPTION = 65568;
    public static final int USER_LOCKED_CHANGEABLE_FOLD = 65664;
    public static final int USER_LOCKED_CHANGEABLE_SHOW_ICON = 65792;
    public static final int USER_LOCKED_FOLD = 65538;
    public static final int USER_LOCKED_MAX_MESSAGES = 65544;
    public static final int USER_LOCKED_OPUSH = 65600;
    public static final int USER_LOCKED_SHOW_BANNER = 65537;
    public static final int USER_LOCKED_SHOW_ICON = 65540;
    public static final int USER_SUPPORT_NUM_BADGE = 65552;
    private int mBadgeOption = 0;
    private boolean mChangeableFold = true;
    private boolean mChangeableShowIcon = true;
    private boolean mFold = false;
    private int mMaxMessages = -1;
    private boolean mOpush = false;
    private boolean mShowBanner = false;
    private boolean mShowIcon = false;
    private boolean mSupportNumBadge = false;

    public OppoBaseNotificationChannel(String id, CharSequence name, int importance) {
        boolean z = false;
        this.mShowBanner = importance >= 4 ? true : z;
    }

    protected OppoBaseNotificationChannel(Parcel in) {
    }

    /* access modifiers changed from: protected */
    public void readFromParcel(Parcel in) {
        this.mShowBanner = in.readBoolean();
        this.mFold = in.readBoolean();
        this.mOpush = in.readBoolean();
        this.mShowIcon = in.readBoolean();
        this.mMaxMessages = in.readInt();
        this.mSupportNumBadge = in.readBoolean();
        this.mBadgeOption = in.readInt();
        this.mChangeableFold = in.readBoolean();
        this.mChangeableShowIcon = in.readBoolean();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mShowBanner);
        dest.writeBoolean(this.mFold);
        dest.writeBoolean(this.mOpush);
        dest.writeBoolean(this.mShowIcon);
        dest.writeInt(this.mMaxMessages);
        dest.writeBoolean(this.mSupportNumBadge);
        dest.writeInt(this.mBadgeOption);
        dest.writeBoolean(this.mChangeableFold);
        dest.writeBoolean(this.mChangeableShowIcon);
    }

    public boolean canShowBanner() {
        return this.mShowBanner;
    }

    public void setShowBanner(boolean show) {
        this.mShowBanner = show;
    }

    public boolean isFold() {
        return this.mFold;
    }

    public void setFold(boolean fold) {
        this.mFold = fold;
    }

    public void setOpush(boolean isOpush) {
        this.mOpush = isOpush;
    }

    public boolean isOpush() {
        return this.mOpush;
    }

    public void setMaxMessage(int max) {
        this.mMaxMessages = max;
    }

    public int getMaxMessages() {
        return this.mMaxMessages;
    }

    public void setShowIcon(boolean show) {
        this.mShowIcon = show;
    }

    public boolean canShowIcon() {
        return this.mShowIcon;
    }

    public void setSupportNumBadge(boolean supportNumBadge) {
        this.mSupportNumBadge = supportNumBadge;
    }

    public boolean isSupportNumBadge() {
        return this.mSupportNumBadge;
    }

    public void setBadgeOption(int badgeOption) {
        this.mBadgeOption = badgeOption;
    }

    public int getBadgeOption() {
        return this.mBadgeOption;
    }

    public boolean isChangeableFold() {
        return this.mChangeableFold;
    }

    public void setChangeableFold(boolean changeable) {
        this.mChangeableFold = changeable;
    }

    public boolean isChangeableShowIcon() {
        return this.mChangeableShowIcon;
    }

    public void setChangeableShowIcon(boolean changeable) {
        this.mChangeableShowIcon = changeable;
    }

    /* access modifiers changed from: protected */
    public void populateFromXml(XmlPullParser parser) {
        setShowBanner(safeBool(parser, ATT_SHOW_BANNER, false));
        setFold(safeBool(parser, ATT_FOLD, false));
        setOpush(safeBool(parser, ATT_OPUSH, false));
        setShowIcon(safeBool(parser, ATT_SHOW_ICON, false));
        setMaxMessage(safeInt(parser, ATT_MAX_MESSAGES, -1));
        setSupportNumBadge(safeBool(parser, ATT_SUPPORT_NUM_BADGE, false));
        setBadgeOption(safeInt(parser, ATT_BADGE_OPTION, 0));
        setChangeableFold(safeBool(parser, ATT_CHANGEABLE_FOLD, true));
        setChangeableShowIcon(safeBool(parser, ATT_CHANGEABLE_SHOW_ICON, true));
    }

    /* access modifiers changed from: protected */
    public void writeXml(XmlSerializer out) throws IOException {
        if (canShowBanner()) {
            out.attribute(null, ATT_SHOW_BANNER, Boolean.toString(canShowBanner()));
        }
        if (isFold()) {
            out.attribute(null, ATT_FOLD, Boolean.toString(isFold()));
        }
        if (isOpush()) {
            out.attribute(null, ATT_OPUSH, Boolean.toString(isOpush()));
        }
        if (canShowIcon()) {
            out.attribute(null, ATT_SHOW_ICON, Boolean.toString(canShowIcon()));
        }
        if (getMaxMessages() != -1) {
            out.attribute(null, ATT_MAX_MESSAGES, String.valueOf(getMaxMessages()));
        }
        if (isSupportNumBadge()) {
            out.attribute(null, ATT_SUPPORT_NUM_BADGE, Boolean.toString(isSupportNumBadge()));
        }
        if (getBadgeOption() != 0) {
            out.attribute(null, ATT_BADGE_OPTION, String.valueOf(getBadgeOption()));
        }
        if (isChangeableFold()) {
            out.attribute(null, ATT_CHANGEABLE_FOLD, String.valueOf(isChangeableFold()));
        }
        if (isChangeableShowIcon()) {
            out.attribute(null, ATT_CHANGEABLE_SHOW_ICON, String.valueOf(isChangeableShowIcon()));
        }
    }

    public void toJson(JSONObject record) throws JSONException {
        record.put(ATT_SHOW_BANNER, canShowBanner());
        record.put(ATT_FOLD, isFold());
        record.put(ATT_OPUSH, isOpush());
        record.put(ATT_SHOW_ICON, canShowIcon());
        record.put(ATT_MAX_MESSAGES, getMaxMessages());
        record.put(ATT_SUPPORT_NUM_BADGE, isSupportNumBadge());
        record.put(ATT_BADGE_OPTION, getBadgeOption());
        record.put(ATT_CHANGEABLE_FOLD, isChangeableFold());
        record.put(ATT_CHANGEABLE_SHOW_ICON, isChangeableShowIcon());
    }

    public boolean equals(OppoBaseNotificationChannel that) {
        if (this.mShowBanner == that.canShowBanner() && this.mFold == that.isFold() && this.mOpush == that.isOpush() && this.mShowIcon == that.canShowIcon() && this.mMaxMessages == that.getMaxMessages() && this.mSupportNumBadge == that.isSupportNumBadge() && this.mBadgeOption == that.getBadgeOption() && this.mChangeableFold == that.isChangeableFold() && this.mChangeableShowIcon == that.isChangeableShowIcon()) {
            return true;
        }
        return false;
    }

    public int hashCode(int result) {
        return (((((((((((((((((result * 31) + (canShowBanner() ? 1 : 0)) * 31) + (isFold() ? 1 : 0)) * 31) + (isOpush() ? 1 : 0)) * 31) + (canShowIcon() ? 1 : 0)) * 31) + getMaxMessages()) * 31) + (isSupportNumBadge() ? 1 : 0)) * 31) + getBadgeOption()) * 31) + (isChangeableFold() ? 1 : 0)) * 31) + (isChangeableShowIcon() ? 1 : 0);
    }

    public String toString() {
        return ", mShowBanner=" + this.mShowBanner + ", mFold=" + this.mFold + ", mOpush=" + this.mOpush + ", mShowIcon=" + this.mShowIcon + ", mMaxMessages=" + this.mMaxMessages + ", mSupportNumBadge=" + this.mSupportNumBadge + ", mBadgeOption=" + this.mBadgeOption + ", mChangeableFold=" + this.mChangeableFold + ", mChangeableShowIcon=" + this.mChangeableShowIcon;
    }

    private static boolean safeBool(XmlPullParser parser, String att, boolean defValue) {
        String value = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static int safeInt(XmlPullParser parser, String att, int defValue) {
        return tryParseInt(parser.getAttributeValue(null, att), defValue);
    }

    private static int tryParseInt(String value, int defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }
}

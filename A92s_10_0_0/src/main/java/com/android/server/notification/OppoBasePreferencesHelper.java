package com.android.server.notification;

import android.app.NotificationChannel;
import android.app.OppoBaseNotificationChannel;
import com.android.internal.util.XmlUtils;
import com.android.server.notification.NotificationManagerService;
import java.io.IOException;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public abstract class OppoBasePreferencesHelper {
    private static final String ATT_BADGE_OPTION = "badge_option";
    private static final String ATT_CHANGEABLE_FOLD = "changeable_fold";
    private static final String ATT_CHANGEABLE_SHOW_ICON = "changeable_show_icon";
    private static final String ATT_FOLD = "fold";
    private static final String ATT_MAX_MESSAGES = "max_messages";
    private static final String ATT_OPUSH = "opush";
    private static final String ATT_SHOW_BANNER = "show_banner";
    private static final String ATT_SHOW_ICON = "show_icon";
    private static final String ATT_STOW_NOTIFICATION = "att_stow_notification";
    private static final String ATT_SUPPORT_NUM_BADGE = "support_num_badge";
    private static final int DEFAULT_BADGE_OPTION = 2;
    private static final boolean DEFAULT_CHANGEABLE_FOLD = true;
    private static final boolean DEFAULT_CHANGEABLE_SHOW_ICON = true;
    private static final boolean DEFAULT_FOLD = false;
    private static final int DEFAULT_MAX_MESSAGES = 1000;
    private static final boolean DEFAULT_NUM_BADGE_SUPPORT = false;
    private static final boolean DEFAULT_OPUSH = false;
    private static final boolean DEFAULT_SHOW_BANNER = false;
    private static final boolean DEFAULT_SHOW_ICON = true;
    private static final int DEFAULT_STOW_OPTION = 0;
    private NotificationManagerService.NotificationListeners mListeners;

    public static class OppoBasePackagePreferences {
        int badgeOption = 2;
        boolean mChangeableFold = true;
        boolean mChangeableShowIcon = true;
        boolean mFold = false;
        int mMaxMessages = 1000;
        boolean mOpush = false;
        boolean mShowBanner = false;
        boolean mShowIcon = true;
        int mStowOption = 0;
        boolean supportNumBadge = false;
    }

    /* access modifiers changed from: package-private */
    public abstract OppoBasePackagePreferences getOrCreatePackagePreferences(String str, int i);

    /* access modifiers changed from: package-private */
    public abstract void setShowBadge(String str, int i, boolean z);

    /* access modifiers changed from: package-private */
    public abstract void updateConfig();

    public void setListeners(NotificationManagerService.NotificationListeners listeners) {
        this.mListeners = listeners;
    }

    /* access modifiers changed from: protected */
    public void clearData(String preferenceKey, Map<String, ? extends OppoBasePackagePreferences> packagePreferences, Map<String, ? extends OppoBasePackagePreferences> restoredWithoutUids) {
        if (packagePreferences != null) {
            packagePreferences.remove(preferenceKey);
        }
        if (restoredWithoutUids != null) {
            restoredWithoutUids.remove(preferenceKey);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyListenersChannelCreated(String pkg, int uid, NotificationChannel channel) {
    }

    public void readXml(OppoBasePackagePreferences r, XmlPullParser parser) {
        r.badgeOption = XmlUtils.readIntAttribute(parser, ATT_BADGE_OPTION, 2);
        r.supportNumBadge = XmlUtils.readBooleanAttribute(parser, ATT_SUPPORT_NUM_BADGE, false);
        r.mFold = XmlUtils.readBooleanAttribute(parser, ATT_FOLD, false);
        r.mOpush = XmlUtils.readBooleanAttribute(parser, ATT_OPUSH, false);
        r.mShowBanner = XmlUtils.readBooleanAttribute(parser, ATT_SHOW_BANNER, false);
        r.mShowIcon = XmlUtils.readBooleanAttribute(parser, ATT_SHOW_ICON, true);
        r.mMaxMessages = XmlUtils.readIntAttribute(parser, ATT_MAX_MESSAGES, 1000);
        r.mChangeableFold = XmlUtils.readBooleanAttribute(parser, ATT_CHANGEABLE_FOLD, true);
        r.mChangeableShowIcon = XmlUtils.readBooleanAttribute(parser, ATT_CHANGEABLE_SHOW_ICON, true);
        r.mStowOption = XmlUtils.readIntAttribute(parser, ATT_STOW_NOTIFICATION, 0);
    }

    public boolean isBasePreferencesDefault(OppoBasePackagePreferences r) {
        return (!r.supportNumBadge && r.badgeOption == 2 && r.mShowIcon && r.mChangeableShowIcon && !r.mFold && r.mChangeableFold && !r.mShowBanner && r.mMaxMessages == 1000 && !r.mOpush && r.mStowOption == 0) ? false : true;
    }

    public void writeAttrbute(XmlSerializer out, OppoBasePackagePreferences r) throws IOException {
        out.attribute(null, ATT_BADGE_OPTION, Integer.toString(r.badgeOption));
        out.attribute(null, ATT_SUPPORT_NUM_BADGE, Boolean.toString(r.supportNumBadge));
        out.attribute(null, ATT_SHOW_BANNER, Boolean.toString(r.mShowBanner));
        out.attribute(null, ATT_FOLD, Boolean.toString(r.mFold));
        out.attribute(null, ATT_OPUSH, Boolean.toString(r.mOpush));
        out.attribute(null, ATT_SHOW_ICON, Boolean.toString(r.mShowIcon));
        out.attribute(null, ATT_MAX_MESSAGES, Integer.toString(r.mMaxMessages));
        out.attribute(null, ATT_CHANGEABLE_FOLD, Boolean.toString(r.mChangeableFold));
        out.attribute(null, ATT_CHANGEABLE_SHOW_ICON, Boolean.toString(r.mChangeableShowIcon));
        out.attribute(null, ATT_STOW_NOTIFICATION, Integer.toString(r.mStowOption));
    }

    public void updateNotificationChannel(OppoBaseNotificationChannel updatedChannel, OppoBasePackagePreferences r) {
        r.mFold = updatedChannel.isFold();
        r.mOpush = updatedChannel.isOpush();
        r.mShowBanner = updatedChannel.canShowBanner();
        r.mShowIcon = updatedChannel.canShowIcon();
        r.mMaxMessages = updatedChannel.getMaxMessages();
        r.mChangeableFold = updatedChannel.isChangeableFold();
        r.mChangeableShowIcon = updatedChannel.isChangeableShowIcon();
    }

    public void setSupportNumBadge(String packageName, int uid, boolean support) {
        getOrCreatePackagePreferences(packageName, uid).supportNumBadge = support;
        updateConfig();
    }

    public boolean getSupportNumBadge(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).supportNumBadge;
    }

    public void setBadgeOption(String packageName, int uid, int option) {
        getOrCreatePackagePreferences(packageName, uid).badgeOption = option;
        boolean showBadge = true;
        if (!(option == 1 || option == 2)) {
            showBadge = false;
        }
        setShowBadge(packageName, uid, showBadge);
        updateConfig();
    }

    public int getBadgeOption(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).badgeOption;
    }

    public void setFold(String packageName, int uid, boolean fold) {
        getOrCreatePackagePreferences(packageName, uid).mFold = fold;
        updateConfig();
    }

    public boolean getFold(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mFold;
    }

    public void isOpush(String packageName, int uid, boolean opush) {
        getOrCreatePackagePreferences(packageName, uid).mOpush = opush;
        updateConfig();
    }

    public boolean isOpush(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mOpush;
    }

    public void setShowBanner(String packageName, int uid, boolean showBanner) {
        getOrCreatePackagePreferences(packageName, uid).mShowBanner = showBanner;
        updateConfig();
    }

    public boolean canShowBanner(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mShowBanner;
    }

    public void setShowIcon(String packageName, int uid, boolean showIcon) {
        getOrCreatePackagePreferences(packageName, uid).mShowIcon = showIcon;
        updateConfig();
    }

    public boolean canShowIcon(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mShowIcon;
    }

    public void setMaxMessages(String packageName, int uid, int maxMessages) {
        getOrCreatePackagePreferences(packageName, uid).mMaxMessages = maxMessages;
        updateConfig();
    }

    public int getMaxMessages(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mMaxMessages;
    }

    public boolean isChangeableFold(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mChangeableFold;
    }

    public void setChangeableFold(String packageName, int uid, boolean changeable) {
        getOrCreatePackagePreferences(packageName, uid).mChangeableFold = changeable;
        updateConfig();
    }

    public boolean isChangeAbleShowIcon(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mChangeableShowIcon;
    }

    public void setChangeableShowIcon(String packageName, int uid, boolean changeable) {
        getOrCreatePackagePreferences(packageName, uid).mChangeableShowIcon = changeable;
        updateConfig();
    }

    public int getStowOption(String packageName, int uid) {
        return getOrCreatePackagePreferences(packageName, uid).mStowOption;
    }

    public void setStowOption(String packageName, int uid, int option) {
        getOrCreatePackagePreferences(packageName, uid).mStowOption = option;
        updateConfig();
    }
}

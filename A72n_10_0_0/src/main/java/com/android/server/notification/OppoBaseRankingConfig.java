package com.android.server.notification;

public interface OppoBaseRankingConfig {
    boolean canShowBanner(String str, int i);

    boolean canShowIcon(String str, int i);

    int getBadgeOption(String str, int i);

    boolean getFold(String str, int i);

    int getMaxMessages(String str, int i);

    int getStowOption(String str, int i);

    boolean getSupportNumBadge(String str, int i);

    boolean isChangeAbleShowIcon(String str, int i);

    boolean isChangeableFold(String str, int i);

    void isOpush(String str, int i, boolean z);

    boolean isOpush(String str, int i);

    void setBadgeOption(String str, int i, int i2);

    void setChangeableFold(String str, int i, boolean z);

    void setChangeableShowIcon(String str, int i, boolean z);

    void setFold(String str, int i, boolean z);

    void setMaxMessages(String str, int i, int i2);

    void setShowBanner(String str, int i, boolean z);

    void setShowIcon(String str, int i, boolean z);

    void setStowOption(String str, int i, int i2);

    void setSupportNumBadge(String str, int i, boolean z);
}

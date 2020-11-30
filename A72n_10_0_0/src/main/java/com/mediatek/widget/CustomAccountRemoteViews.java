package com.mediatek.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;
import com.mediatek.internal.R;
import java.util.List;

public class CustomAccountRemoteViews {
    private static final int MOSTACCOUNTNUMBER = 8;
    private static final int ROWACCOUNTNUMBER = 4;
    private static final String TAG = "CustomAccountRemoteViews";
    private final int[][] RESOURCE_ID;
    private RemoteViews mBigRemoteViews;
    private Context mContext;
    private List<AccountInfo> mData;
    private RemoteViews mNormalRemoteViews;
    private int mRequestCode;

    private final class IdIndex {
        public static final int CONTAINER_ID = 0;
        public static final int HIGHTLIGHT_DIVIDER_ID = 5;
        public static final int IMG_ID = 1;
        public static final int NAME_ID = 2;
        public static final int NORMAL_DIVIDER_ID = 4;
        public static final int NUMBER_ID = 3;

        private IdIndex() {
        }
    }

    public CustomAccountRemoteViews(Context context, String packageName) {
        this(context, packageName, null);
    }

    public CustomAccountRemoteViews(Context context, String packageName, List<AccountInfo> data) {
        this.RESOURCE_ID = new int[][]{new int[]{R.id.account_zero_container, R.id.account_zero_img, R.id.account_zero_name, R.id.account_zero_number, R.id.account_zero_normal_divider, R.id.account_zero_highlight_divider}, new int[]{R.id.account_one_container, R.id.account_one_img, R.id.account_one_name, R.id.account_one_number, R.id.account_one_normal_divider, R.id.account_one_highlight_divider}, new int[]{R.id.account_two_container, R.id.account_two_img, R.id.account_two_name, R.id.account_two_number, R.id.account_two_normal_divider, R.id.account_two_highlight_divider}, new int[]{R.id.account_three_container, R.id.account_three_img, R.id.account_three_name, R.id.account_three_number, R.id.account_three_normal_divider, R.id.account_three_highlight_divider}, new int[]{R.id.account_four_container, R.id.account_four_img, R.id.account_four_name, R.id.account_four_number, R.id.account_four_normal_divider, R.id.account_four_highlight_divider}, new int[]{R.id.account_five_container, R.id.account_five_img, R.id.account_five_name, R.id.account_five_number, R.id.account_five_normal_divider, R.id.account_five_highlight_divider}, new int[]{R.id.account_six_container, R.id.account_six_img, R.id.account_six_name, R.id.account_six_number, R.id.account_six_normal_divider, R.id.account_six_highlight_divider}, new int[]{R.id.account_seven_container, R.id.account_seven_img, R.id.account_seven_name, R.id.account_seven_number, R.id.account_seven_normal_divider, R.id.account_seven_highlight_divider}};
        this.mNormalRemoteViews = new RemoteViews(packageName, (int) R.layout.normal_default_account_select_title);
        this.mBigRemoteViews = new RemoteViews(packageName, (int) R.layout.custom_select_default_account_notification);
        this.mData = data;
        this.mContext = context;
        this.mRequestCode = 0;
    }

    public RemoteViews getNormalRemoteViews() {
        return this.mNormalRemoteViews;
    }

    public RemoteViews getBigRemoteViews() {
        return this.mBigRemoteViews;
    }

    public void setData(List<AccountInfo> data) {
        this.mData = data;
    }

    public void configureView() {
        if (this.mData != null) {
            Log.d(TAG, "---configureView---view size = " + this.mData.size());
            if (this.mData.size() > 4) {
                this.mBigRemoteViews.setViewVisibility(R.id.select_account_row_two_container, 0);
            }
            int i = 0;
            while (i < this.mData.size() && i < 8) {
                Log.d(TAG, "--- configure account id: " + i);
                configureAccount(this.RESOURCE_ID[i], this.mData.get(i));
                i++;
            }
            int end = 8;
            if (this.mData.size() <= 4) {
                end = 4;
                this.mBigRemoteViews.setViewVisibility(R.id.select_account_row_two_container, 8);
            }
            for (int i2 = this.mData.size(); i2 < end; i2++) {
                this.mBigRemoteViews.setViewVisibility(this.RESOURCE_ID[i2][0], 8);
            }
            return;
        }
        Log.w(TAG, "Data can not be null");
    }

    private void configureAccount(int[] resourceId, AccountInfo accountInfo) {
        if (accountInfo.getIcon() != null) {
            this.mBigRemoteViews.setViewVisibility(resourceId[0], 0);
            this.mBigRemoteViews.setImageViewBitmap(resourceId[1], accountInfo.getIcon());
        } else if (accountInfo.getIconId() != 0) {
            this.mBigRemoteViews.setViewVisibility(resourceId[0], 0);
            this.mBigRemoteViews.setImageViewResource(resourceId[1], accountInfo.getIconId());
        } else {
            Log.w(TAG, "--- The icon of account is null ---");
        }
        if (accountInfo.getLabel() == null) {
            this.mBigRemoteViews.setViewVisibility(resourceId[2], 8);
        } else {
            this.mBigRemoteViews.setTextViewText(resourceId[2], accountInfo.getLabel());
        }
        if (accountInfo.getNumber() == null) {
            this.mBigRemoteViews.setViewVisibility(resourceId[3], 8);
        } else {
            this.mBigRemoteViews.setTextViewText(resourceId[3], accountInfo.getNumber());
        }
        Log.d(TAG, "active: " + accountInfo.isActive());
        if (accountInfo.isActive()) {
            this.mBigRemoteViews.setViewVisibility(resourceId[4], 8);
            this.mBigRemoteViews.setViewVisibility(resourceId[5], 0);
        } else {
            this.mBigRemoteViews.setViewVisibility(resourceId[4], 0);
            this.mBigRemoteViews.setViewVisibility(resourceId[5], 8);
        }
        if (accountInfo.getIntent() != null) {
            Context context = this.mContext;
            int i = this.mRequestCode;
            this.mRequestCode = i + 1;
            this.mBigRemoteViews.setOnClickPendingIntent(resourceId[0], PendingIntent.getBroadcast(context, i, accountInfo.getIntent(), 134217728));
        }
    }

    public static class AccountInfo {
        private Bitmap mIcon;
        private int mIconId;
        private Intent mIntent;
        private boolean mIsActive;
        private String mLabel;
        private String mNumber;

        public AccountInfo(Bitmap icon, String label, String number, Intent intent) {
            this(0, icon, label, number, intent, false);
        }

        public AccountInfo(int iconId, String label, String number, Intent intent) {
            this(iconId, null, label, number, intent, false);
        }

        public AccountInfo(int iconId, Bitmap icon, String label, String number, Intent intent, boolean isActive) {
            this.mIconId = iconId;
            this.mIcon = icon;
            this.mLabel = label;
            this.mNumber = number;
            this.mIntent = intent;
            this.mIsActive = isActive;
        }

        public int getIconId() {
            int i = this.mIconId;
            if (i != 0) {
                return i;
            }
            return 0;
        }

        public Bitmap getIcon() {
            Bitmap bitmap = this.mIcon;
            if (bitmap != null) {
                return bitmap;
            }
            return null;
        }

        public String getLabel() {
            return this.mLabel;
        }

        public String getNumber() {
            return this.mNumber;
        }

        public Intent getIntent() {
            return this.mIntent;
        }

        public boolean isActive() {
            return this.mIsActive;
        }

        public void setActiveStatus(boolean active) {
            this.mIsActive = active;
        }
    }
}

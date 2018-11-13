package com.suntek.rcs.ui.common;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmojiPackageBO;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmoticonBO;
import com.suntek.mway.rcs.client.api.emoticon.EmoticonApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.rcs.ui.common.utils.RcsUtils;
import java.util.ArrayList;
import java.util.List;

public class RcsEmojiInitialize {
    private ImageButton mAddBtn;
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v == RcsEmojiInitialize.this.mDeleteBtn) {
                RcsEmojiInitialize.this.mViewOnClickListener.onEmojiDeleteListener();
            } else if (v == RcsEmojiInitialize.this.mAddBtn) {
                RcsEmojiInitialize.this.mViewOnClickListener.addEmojiPackageListener();
            }
        }
    };
    private Context mContext;
    private String mDefaultPackageId = "-1";
    private ImageButton mDeleteBtn;
    private GridView mEmojiGridView;
    private ArrayList<EmojiPackageBO> mEmojiPackages = new ArrayList();
    private EmojiResources mEmojiResources;
    private View mEmojiView = null;
    private GirdViewAdapter mGirdViewAdapter;
    private OnClickListener mImageOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            String packageId = (String) view.getTag();
            if (!TextUtils.isEmpty(packageId) && !RcsEmojiInitialize.this.mSelectPackageId.equals(packageId)) {
                RcsEmojiInitialize.this.mSelectPackageId = packageId;
                RcsEmojiInitialize.this.setImageButtonCheck(RcsEmojiInitialize.this.mSelectPackageId);
                RcsEmojiInitialize.this.mGirdViewAdapter.setEmojiData(RcsEmojiInitialize.this.mSelectPackageId);
            }
        }
    };
    private LayoutParams mLayoutParams;
    private LinearLayout mLinearLayout;
    private String mSelectPackageId = "";
    private ViewOnClickListener mViewOnClickListener;
    private ViewStub mViewStub;
    private ArrayList<View> packageListButton = new ArrayList();

    public static class EmojiResources {
        private int mAddEmojiBtnResId;
        private int mContentLinearLayoutResId;
        private int mDeleteEmojiBtnResId;
        private int mEmojiButtonBgResId;
        private int mEmojiGridViewItemResId;
        private int mEmojiGridViewResId;
        private int mEmojiPopupBgResId;
        private int mIconResId;
        private int mItemResId;
        private int mTextFaceResId;
        private int mTitleResId;

        private EmojiResources(int titleResId, int iconResId, int textFaceResId, int itemResId, int emojiButtonBgResId, int emojiGridViewItemResId, int deleteEmojiBtnResId, int addEmojiBtnResId, int emojiGridViewResId, int contentLinearLayoutResId, int emojiPopupBgResId) {
            this.mTitleResId = titleResId;
            this.mIconResId = iconResId;
            this.mTextFaceResId = textFaceResId;
            this.mItemResId = itemResId;
            this.mEmojiButtonBgResId = emojiButtonBgResId;
            this.mEmojiGridViewItemResId = emojiGridViewItemResId;
            this.mDeleteEmojiBtnResId = deleteEmojiBtnResId;
            this.mAddEmojiBtnResId = addEmojiBtnResId;
            this.mEmojiGridViewResId = emojiGridViewResId;
            this.mContentLinearLayoutResId = contentLinearLayoutResId;
            this.mEmojiPopupBgResId = emojiPopupBgResId;
        }

        public static EmojiResources create(int titleResId, int iconResId, int textFaceResId, int itemResId, int emojiButtonBgResId, int emojiGridViewItemResId, int deleteEmojiBtnResId, int addEmojiBtnResId, int emojiGridViewResId, int contentLinearLayoutResId, int emojiPopupBgResId) {
            return new EmojiResources(titleResId, iconResId, textFaceResId, itemResId, emojiButtonBgResId, emojiGridViewItemResId, deleteEmojiBtnResId, addEmojiBtnResId, emojiGridViewResId, contentLinearLayoutResId, emojiPopupBgResId);
        }
    }

    public class GirdViewAdapter extends BaseAdapter {
        private OnClickListener mClickListener = new OnClickListener() {
            public void onClick(View view) {
                if (GirdViewAdapter.this.mPackageId.equals(RcsEmojiInitialize.this.mDefaultPackageId)) {
                    GirdViewAdapter.this.mViewOnClickListener.faceTextSelectListener((String) view.getTag());
                    return;
                }
                GirdViewAdapter.this.mViewOnClickListener.emojiSelectListener((EmoticonBO) view.getTag());
            }
        };
        private Context mContext;
        private ArrayList<EmoticonBO> mEmojiObjects = new ArrayList();
        private final int[] mFaceTexts = new int[]{128512, 128513, 128514, 128515, 128516, 128517, 128518, 128519, 128520, 128521, 128522, 128523, 128524, 128525, 128526, 128527, 128528, 128529, 128530, 128531, 128532, 128533, 128534, 128535, 128536, 128537, 128538, 128539, 128540, 128541, 128543, 128544, 128545, 128546, 128547, 128548, 128549, 128550, 128551, 128552, 128553, 128554, 128555, 128556, 128557, 128559, 128560, 128561, 128562, 128563, 128564, 128565, 128566, 128567, 128568, 128569, 128570, 128571, 128572, 128573, 128575, 128576, 128581, 128582, 128583, 128584, 128585, 128586, 128587, 128588, 128589, 128591};
        private int mItemHeight;
        private String mPackageId = "";
        private ViewOnClickListener mViewOnClickListener;
        private OnLongClickListener onLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                try {
                    RcsUtils.openPopupWindow(GirdViewAdapter.this.mContext, arg0, EmoticonApi.getInstance().decrypt2Bytes(((EmoticonBO) arg0.getTag()).getEmoticonId(), 2), RcsEmojiInitialize.this.mEmojiResources.mEmojiPopupBgResId);
                } catch (ServiceDisconnectedException e) {
                    e.printStackTrace();
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
                return false;
            }
        };

        private class ViewHolder {
            ImageView icon;
            RelativeLayout mItemView;
            TextView textFace;
            TextView title;

            public void setItemHeight(int height) {
                ((RelativeLayout.LayoutParams) this.mItemView.getLayoutParams()).height = height;
            }

            public ViewHolder(View convertView) {
                this.title = (TextView) convertView.findViewById(RcsEmojiInitialize.this.mEmojiResources.mTitleResId);
                this.icon = (ImageView) convertView.findViewById(RcsEmojiInitialize.this.mEmojiResources.mIconResId);
                this.textFace = (TextView) convertView.findViewById(RcsEmojiInitialize.this.mEmojiResources.mTextFaceResId);
                this.mItemView = (RelativeLayout) convertView.findViewById(RcsEmojiInitialize.this.mEmojiResources.mItemResId);
                this.mItemView.setBackgroundResource(RcsEmojiInitialize.this.mEmojiResources.mEmojiButtonBgResId);
            }
        }

        public GirdViewAdapter(Context context, ViewOnClickListener viewOnClickListener) {
            this.mContext = context;
            this.mViewOnClickListener = viewOnClickListener;
        }

        private void setItemHeight(int height) {
            this.mItemHeight = height;
        }

        public void setEmojiData(String packageId) {
            this.mPackageId = packageId;
            if (this.mPackageId.equals(RcsEmojiInitialize.this.mDefaultPackageId)) {
                this.mEmojiObjects.clear();
                notifyDataSetChanged();
                return;
            }
            try {
                List<EmoticonBO> list = EmoticonApi.getInstance().queryEmoticons(packageId);
                if (list != null && list.size() > 0) {
                    this.mEmojiObjects.clear();
                    this.mEmojiObjects.addAll(list);
                    notifyDataSetChanged();
                }
            } catch (ServiceDisconnectedException e) {
                e.printStackTrace();
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }

        public int getCount() {
            if (this.mPackageId.equals(RcsEmojiInitialize.this.mDefaultPackageId)) {
                return this.mFaceTexts.length;
            }
            return this.mEmojiObjects.size();
        }

        public Object getItem(int position) {
            if (this.mPackageId.equals(RcsEmojiInitialize.this.mDefaultPackageId)) {
                return Integer.valueOf(this.mFaceTexts[position]);
            }
            return this.mEmojiObjects.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(this.mContext).inflate(RcsEmojiInitialize.this.mEmojiResources.mEmojiGridViewItemResId, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.setItemHeight(this.mItemHeight);
            if (this.mPackageId.equals(RcsEmojiInitialize.this.mDefaultPackageId)) {
                int faceInt = ((Integer) getItem(position)).intValue();
                holder.title.setVisibility(8);
                holder.icon.setVisibility(8);
                holder.textFace.setVisibility(0);
                String faceText = new String(new int[]{faceInt}, 0, 1);
                holder.textFace.setText(faceText);
                holder.mItemView.setTag(faceText);
                holder.mItemView.setOnClickListener(this.mClickListener);
            } else {
                holder.textFace.setVisibility(8);
                holder.icon.setVisibility(0);
                holder.title.setVisibility(0);
                EmoticonBO bean = (EmoticonBO) getItem(position);
                holder.title.setText(bean.getEmoticonName());
                RcsEmojiStoreUtil.getInstance().loadImageAsynById(holder.icon, bean.getEmoticonId(), 1);
                holder.mItemView.setTag(bean);
                holder.mItemView.setOnClickListener(this.mClickListener);
                holder.mItemView.setOnLongClickListener(this.onLongClickListener);
            }
            return convertView;
        }
    }

    class LoadSessionTask extends AsyncTask<Void, Void, List<EmojiPackageBO>> {
        LoadSessionTask() {
        }

        protected List<EmojiPackageBO> doInBackground(Void... params) {
            List<EmojiPackageBO> packageList = new ArrayList();
            List<EmojiPackageBO> list = getStorePackageList();
            if (list != null) {
                packageList.addAll(list);
            }
            return packageList;
        }

        protected void onPostExecute(List<EmojiPackageBO> result) {
            super.onPostExecute(result);
            if (RcsEmojiInitialize.this.mEmojiPackages.size() <= 0 || RcsEmojiInitialize.this.mEmojiPackages.size() != result.size()) {
                RcsEmojiInitialize.this.mEmojiPackages.clear();
                RcsEmojiInitialize.this.mEmojiPackages.addAll(result);
                RcsEmojiInitialize.this.initPackageView(result);
                RcsEmojiInitialize.this.setImageButtonCheck(RcsEmojiInitialize.this.mSelectPackageId);
                RcsEmojiInitialize.this.mGirdViewAdapter.setEmojiData(RcsEmojiInitialize.this.mSelectPackageId);
            }
        }

        private ArrayList<EmojiPackageBO> getStorePackageList() {
            ArrayList<EmojiPackageBO> storelist = new ArrayList();
            try {
                List<EmojiPackageBO> list = EmoticonApi.getInstance().queryEmojiPackages();
                if (list != null && list.size() > 0) {
                    storelist.addAll(list);
                }
            } catch (ServiceDisconnectedException e) {
                e.printStackTrace();
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
            return storelist;
        }
    }

    public interface ViewOnClickListener {
        void addEmojiPackageListener();

        void emojiSelectListener(EmoticonBO emoticonBO);

        void faceTextSelectListener(String str);

        void onEmojiDeleteListener();

        void viewOpenOrCloseListener(boolean z);
    }

    public View getEmojiView() {
        return this.mEmojiView;
    }

    public RcsEmojiInitialize(Context context, ViewStub viewStub, ViewOnClickListener viewOnClickListener, EmojiResources emojiResources) {
        this.mContext = context;
        this.mViewStub = viewStub;
        this.mViewOnClickListener = viewOnClickListener;
        this.mLayoutParams = new LayoutParams(RcsUtils.dip2px(this.mContext, 45.0f), -1);
        this.mLayoutParams.leftMargin = RcsUtils.dip2px(this.mContext, 1.0f);
        this.mSelectPackageId = this.mDefaultPackageId;
        this.mEmojiResources = emojiResources;
    }

    public void closeOrOpenView() {
        if (this.mEmojiView == null) {
            RcsUtils.closeKB((Activity) this.mContext);
            initEmojiView();
            this.mViewOnClickListener.viewOpenOrCloseListener(true);
            return;
        }
        if (this.mEmojiView == null || this.mEmojiView.getVisibility() != 8) {
            this.mEmojiView.setVisibility(8);
            RcsUtils.openKB(this.mContext);
            this.mViewOnClickListener.viewOpenOrCloseListener(false);
        } else {
            RcsUtils.closeKB((Activity) this.mContext);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    RcsEmojiInitialize.this.mEmojiView.setVisibility(0);
                    RcsEmojiInitialize.this.mViewOnClickListener.viewOpenOrCloseListener(true);
                }
            }, 200);
        }
    }

    public void closeViewAndKB() {
        this.mEmojiView.setVisibility(8);
        this.mViewOnClickListener.viewOpenOrCloseListener(false);
    }

    public void refreshData() {
        new LoadSessionTask().execute(new Void[0]);
    }

    private void initEmojiView() {
        this.mEmojiView = this.mViewStub.inflate();
        this.mEmojiGridView = (GridView) this.mEmojiView.findViewById(this.mEmojiResources.mEmojiGridViewResId);
        this.mLinearLayout = (LinearLayout) this.mEmojiView.findViewById(this.mEmojiResources.mContentLinearLayoutResId);
        this.mDeleteBtn = (ImageButton) this.mEmojiView.findViewById(this.mEmojiResources.mDeleteEmojiBtnResId);
        this.mDeleteBtn.setVisibility(8);
        this.mDeleteBtn.setOnClickListener(this.mClickListener);
        this.mAddBtn = (ImageButton) this.mEmojiView.findViewById(this.mEmojiResources.mAddEmojiBtnResId);
        this.mAddBtn.setOnClickListener(this.mClickListener);
        this.mGirdViewAdapter = new GirdViewAdapter(this.mContext, this.mViewOnClickListener);
        this.mEmojiGridView.setAdapter(this.mGirdViewAdapter);
        new LoadSessionTask().execute(new Void[0]);
    }

    private void initPackageView(List<EmojiPackageBO> packageList) {
        this.mLinearLayout.removeAllViews();
        this.packageListButton.clear();
        TextView textView = createTextView();
        this.mLinearLayout.addView(textView);
        this.packageListButton.add(textView);
        for (int i = 0; i < packageList.size(); i++) {
            ImageButton imageButton = createImageView((EmojiPackageBO) packageList.get(i));
            this.mLinearLayout.addView(imageButton);
            this.packageListButton.add(imageButton);
        }
    }

    private TextView createTextView() {
        TextView textView = new TextView(this.mContext);
        textView.setLayoutParams(this.mLayoutParams);
        textView.setPadding(2, 2, 2, 2);
        textView.setTag(this.mDefaultPackageId);
        textView.setTextSize(25.0f);
        String text = new String(new int[]{128513}, 0, 1);
        textView.setGravity(17);
        textView.setText(text);
        textView.setOnClickListener(this.mImageOnClickListener);
        return textView;
    }

    private ImageButton createImageView(EmojiPackageBO emojiPackageBO) {
        ImageButton imageButton = new ImageButton(this.mContext);
        imageButton.setLayoutParams(this.mLayoutParams);
        imageButton.setScaleType(ScaleType.CENTER_INSIDE);
        imageButton.setPadding(2, 2, 2, 2);
        RcsEmojiStoreUtil.getInstance().loadImageAsynById(imageButton, emojiPackageBO.getPackageId(), 3);
        imageButton.setTag(emojiPackageBO.getPackageId());
        imageButton.setOnClickListener(this.mImageOnClickListener);
        return imageButton;
    }

    private void setImageButtonCheck(String checkId) {
        if (checkId.equals(this.mDefaultPackageId)) {
            this.mDeleteBtn.setVisibility(0);
            this.mEmojiGridView.setNumColumns(7);
            this.mGirdViewAdapter.setItemHeight(RcsUtils.dip2px(this.mContext, 45.0f));
        } else {
            this.mDeleteBtn.setVisibility(8);
            this.mEmojiGridView.setNumColumns(4);
            this.mGirdViewAdapter.setItemHeight(RcsUtils.dip2px(this.mContext, 80.0f));
        }
        for (View view : this.packageListButton) {
            if (((String) view.getTag()).equals(checkId)) {
                view.setBackgroundColor(-1);
            } else {
                view.setBackgroundColor(-7829368);
            }
        }
    }
}

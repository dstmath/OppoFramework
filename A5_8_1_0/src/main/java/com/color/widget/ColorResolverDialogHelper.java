package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.app.ResolverActivity.ResolveListAdapter;
import com.color.oshare.ColorOshareDevice;
import com.color.oshare.ColorOshareServiceUtil;
import com.color.oshare.ColorOshareState;
import com.color.oshare.IColorOshareCallback;
import com.color.oshare.IColorOshareInitListener;
import com.color.oshare.IColorOshareInitListener.Stub;
import com.color.util.ColorContextUtil;
import com.color.widget.ColorPagerAdapter.ColorResolverItemEventListener;
import com.color.widget.ColorRecyclerView.Adapter;
import com.color.widget.ColorRecyclerView.ViewHolder;
import com.color.widget.ColorViewPager.OnPageChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oppo.util.OppoStatistics;

public class ColorResolverDialogHelper {
    private static final String CODE = "20120";
    private static final String GALLERY_PIN_LIST = "gallery_pin_list";
    private static final String KEY = "49";
    private static final String KEY_TYPE = "type";
    private static final String RECOMMEND_EVENT_ID = "resolver_recommend";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String SECRET = "be7a52eaeb67a660ecfdcff7c742c8a2";
    private static final String TAG = "ColorResolverDialogHelper";
    private static final String TYPE_GALLERY = "gallery";
    private String TYPE_EMAIL;
    private String TYPE_EXCEL;
    private String TYPE_PDF;
    private String TYPE_PPT;
    private String TYPE_TEXT;
    private String TYPE_VIDEO;
    private String TYPE_WORD;
    private Activity mActivity;
    private AlertDialog mAlertDialog;
    private boolean mAlwaysUseOption;
    private int mAnimDistance;
    private boolean mBegineOshare;
    private IntentSender mChosenComponentSender;
    private List<ColorItem> mColorItemList;
    private ColorMenuView mColorMenuView;
    private Context mContext;
    private ArrayList<ColorOshareDevice> mDeviceList;
    private ColorDotView mDotView;
    private Intent mIntent;
    private boolean mIsChecked;
    private List<ResolveInfo> mList;
    private List<ColorGridView> mListColorGridView;
    private OnItemLongClickListener mLongclickListener;
    private View mNoticeHelpView;
    private View mNoticeOpenOshareView;
    private IColorOshareCallback mOShareCallback;
    private IColorOshareInitListener mOShareInitListener;
    private boolean mOShareServiceInited;
    private ColorOshareServiceUtil mOShareServiceUtil;
    private OnItemClickListener mOnItemClickListener;
    private View mOpenOsharePanel;
    private View mOpenWifiBlueToothView;
    private Intent mOriginIntent;
    private View mOshareIcon;
    private View mOshareingPanel;
    public int mPageCount;
    private ColorResolverPagerAdapter mPagerAdapter;
    private ColorRecyclerView mRecyclerView;
    private ColorResolveInfoHelper mResolveInfoHelper;
    private View mResolveView;
    private ResolverOshareingAdapter mResolverOshareingAdapter;
    private List<ResolveInfo> mRiList;
    private ColorResolverDialogViewPager mViewPager;

    public class MyViewHolder extends ViewHolder {
        public final TextView userName;
        public final View userPanel;
        public final ImageView userPic;
        public final ColorTransferProgress userPreogerss;
        public final TextView userStatus;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.userName = (TextView) itemView.findViewById(201458980);
            this.userStatus = (TextView) itemView.findViewById(201458981);
            this.userPic = (ImageView) itemView.findViewById(201458982);
            this.userPreogerss = (ColorTransferProgress) itemView.findViewById(201458971);
            this.userPanel = itemView.findViewById(201458972);
        }
    }

    private class ResolverOshareingAdapter extends Adapter<MyViewHolder> {
        /* renamed from: -com-color-oshare-ColorOshareStateSwitchesValues */
        private static final /* synthetic */ int[] f0-com-color-oshare-ColorOshareStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$color$oshare$ColorOshareState;
        String BUSUY_STR;
        String CANCEL_STR;
        String CANCEL_WAIT_STR;
        String READY_STR;
        String TRANSITING_STR;
        String TRANSIT_FAILED_STR;
        String TRANSIT_REJECT_STR;
        String TRANSIT_SUCCESS_STR;
        String TRANSIT_TIMEOUT_STR;
        String TRANSIT_WAIT_STR;
        private Context mContext = null;
        private ArrayList<ColorOshareDevice> mDeviceList;
        int mStateTextColorFail;
        int mStateTextColorNomarl;
        int mStateTextColorSucces;

        /* renamed from: -getcom-color-oshare-ColorOshareStateSwitchesValues */
        private static /* synthetic */ int[] m0-getcom-color-oshare-ColorOshareStateSwitchesValues() {
            if (f0-com-color-oshare-ColorOshareStateSwitchesValues != null) {
                return f0-com-color-oshare-ColorOshareStateSwitchesValues;
            }
            int[] iArr = new int[ColorOshareState.values().length];
            try {
                iArr[ColorOshareState.BUSUY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ColorOshareState.BUSY.ordinal()] = 11;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ColorOshareState.CANCEL.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ColorOshareState.CANCEL_WAIT.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ColorOshareState.IDLE.ordinal()] = 12;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ColorOshareState.READY.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[ColorOshareState.SPACE_NOT_ENOUGH.ordinal()] = 13;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[ColorOshareState.TRANSITING.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[ColorOshareState.TRANSIT_FAILED.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[ColorOshareState.TRANSIT_REJECT.ordinal()] = 7;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[ColorOshareState.TRANSIT_SUCCESS.ordinal()] = 8;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[ColorOshareState.TRANSIT_TIMEOUT.ordinal()] = 9;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[ColorOshareState.TRANSIT_WAIT.ordinal()] = 10;
            } catch (NoSuchFieldError e13) {
            }
            f0-com-color-oshare-ColorOshareStateSwitchesValues = iArr;
            return iArr;
        }

        public void setDeviceList(ArrayList<ColorOshareDevice> deviceList) {
            this.mDeviceList = deviceList;
        }

        public ResolverOshareingAdapter(Context context) {
            this.mContext = context;
            this.mStateTextColorNomarl = ColorContextUtil.getAttrColor(this.mContext, 201392714);
            this.mStateTextColorSucces = ColorContextUtil.getAttrColor(this.mContext, 201392701);
            this.mStateTextColorFail = ColorContextUtil.getAttrColor(this.mContext, 201392720);
            this.READY_STR = context.getString(201590141);
            this.TRANSIT_WAIT_STR = context.getString(201590134);
            this.TRANSITING_STR = context.getString(201590140);
            this.TRANSIT_FAILED_STR = context.getString(201590135);
            this.TRANSIT_REJECT_STR = context.getString(201590136);
            this.TRANSIT_SUCCESS_STR = context.getString(201590137);
            this.BUSUY_STR = context.getString(201590138);
            this.CANCEL_STR = context.getString(201590139);
            this.CANCEL_WAIT_STR = context.getString(201590147);
            this.TRANSIT_TIMEOUT_STR = context.getString(201590142);
        }

        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(201917586, parent, false));
        }

        public void onBindViewHolder(MyViewHolder holder, int position) {
            if (this.mDeviceList != null && this.mDeviceList.size() != 0) {
                final ColorOshareDevice receiver = (ColorOshareDevice) this.mDeviceList.get(position);
                if (receiver != null) {
                    holder.itemView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (ColorResolverDialogHelper.this.mOShareServiceUtil != null && ColorResolverDialogHelper.this.mOShareServiceInited) {
                                ColorResolverDialogHelper.this.mOShareServiceUtil.sendData(ColorResolverDialogHelper.this.mOriginIntent, receiver);
                            }
                        }
                    });
                    if (receiver.getState() == ColorOshareState.TRANSITING) {
                        holder.userPreogerss.setVisibility(0);
                        holder.userPreogerss.setProgress(receiver.getProgress());
                    } else {
                        holder.userPreogerss.setVisibility(4);
                    }
                    holder.userStatus.setText(getStateString(receiver.getState()));
                    holder.userStatus.setTextColor(getStateColor(receiver.getState()));
                    holder.userName.setText(receiver.getName());
                }
            }
        }

        public int getItemCount() {
            if (this.mDeviceList == null) {
                return 0;
            }
            return this.mDeviceList.size();
        }

        private String getStateString(ColorOshareState state) {
            String stringId = "";
            switch (m0-getcom-color-oshare-ColorOshareStateSwitchesValues()[state.ordinal()]) {
                case 1:
                    return this.BUSUY_STR;
                case 2:
                    return this.CANCEL_STR;
                case 3:
                    return this.CANCEL_WAIT_STR;
                case 4:
                    return this.READY_STR;
                case 5:
                    return this.TRANSITING_STR;
                case 6:
                    return this.TRANSIT_FAILED_STR;
                case 7:
                    return this.TRANSIT_REJECT_STR;
                case 8:
                    return this.TRANSIT_SUCCESS_STR;
                case 9:
                    return this.TRANSIT_TIMEOUT_STR;
                case 10:
                    return this.TRANSIT_WAIT_STR;
                default:
                    return stringId;
            }
        }

        private int getStateColor(ColorOshareState state) {
            int color = this.mStateTextColorNomarl;
            switch (m0-getcom-color-oshare-ColorOshareStateSwitchesValues()[state.ordinal()]) {
                case 1:
                case 6:
                case 7:
                case 9:
                    return this.mStateTextColorFail;
                case 8:
                    return this.mStateTextColorSucces;
                default:
                    return color;
            }
        }
    }

    private void updateOShareUI() {
        if (this.mOShareServiceUtil == null || !this.mOShareServiceUtil.isSendOn()) {
            if (this.mOpenOsharePanel != null) {
                this.mOpenOsharePanel.setVisibility(0);
            }
            if (this.mOpenWifiBlueToothView != null) {
                this.mOpenWifiBlueToothView.setVisibility(0);
            }
            if (this.mNoticeOpenOshareView != null) {
                this.mNoticeOpenOshareView.setVisibility(8);
            }
            if (this.mOshareIcon != null) {
                this.mOshareIcon.setBackgroundResource(201852187);
            }
            if (this.mOshareingPanel != null) {
                this.mOshareingPanel.setVisibility(8);
                return;
            }
            return;
        }
        this.mOpenWifiBlueToothView.setVisibility(8);
        if (this.mDeviceList == null || this.mDeviceList.size() < 1) {
            if (this.mOpenOsharePanel != null) {
                this.mOpenOsharePanel.setVisibility(0);
            }
            if (this.mOshareIcon != null) {
                this.mOshareIcon.setBackgroundResource(201852185);
            }
            if (this.mNoticeOpenOshareView != null) {
                this.mNoticeOpenOshareView.setVisibility(0);
            }
            if (this.mOshareingPanel != null) {
                this.mOshareingPanel.setVisibility(8);
                return;
            }
            return;
        }
        if (this.mOshareingPanel != null) {
            this.mOshareingPanel.setVisibility(0);
        }
        if (this.mOpenOsharePanel != null) {
            this.mOpenOsharePanel.setVisibility(8);
        }
    }

    public ColorResolverDialogHelper(Context context, Intent intent) {
        this.TYPE_EMAIL = "email";
        this.TYPE_VIDEO = "video";
        this.TYPE_TEXT = "text";
        this.TYPE_PDF = "pdf";
        this.TYPE_WORD = "word";
        this.TYPE_EXCEL = "excel";
        this.TYPE_PPT = "ppt";
        this.mRiList = new ArrayList();
        this.mList = new ArrayList();
        this.mColorItemList = new ArrayList();
        this.mIsChecked = false;
        this.mOShareServiceInited = false;
        this.mBegineOshare = false;
        this.mOShareInitListener = new Stub() {
            public void onShareUninit() throws RemoteException {
                ColorResolverDialogHelper.this.mOShareServiceInited = false;
                if (ColorResolverDialogHelper.this.mOShareServiceUtil != null) {
                    ColorResolverDialogHelper.this.mOShareServiceUtil.unregisterCallback(ColorResolverDialogHelper.this.mOShareCallback);
                }
            }

            public void onShareInit() throws RemoteException {
                ColorResolverDialogHelper.this.mOShareServiceInited = true;
                if (ColorResolverDialogHelper.this.mContext != null && (ColorResolverDialogHelper.this.mContext instanceof Activity)) {
                    ((Activity) ColorResolverDialogHelper.this.mContext).runOnUiThread(new Runnable() {
                        public void run() {
                            ColorResolverDialogHelper.this.updateOShareUI();
                        }
                    });
                }
                if (ColorResolverDialogHelper.this.mOShareServiceUtil != null) {
                    ColorResolverDialogHelper.this.mOShareServiceUtil.registerCallback(ColorResolverDialogHelper.this.mOShareCallback);
                }
            }
        };
        this.mOShareCallback = new IColorOshareCallback.Stub() {
            public void onDeviceChanged(List<ColorOshareDevice> deviceList) throws RemoteException {
                ColorResolverDialogHelper.this.mDeviceList = (ArrayList) deviceList;
                ColorResolverDialogHelper.this.mResolverOshareingAdapter.setDeviceList(ColorResolverDialogHelper.this.mDeviceList);
                if (ColorResolverDialogHelper.this.mContext != null && (ColorResolverDialogHelper.this.mContext instanceof Activity)) {
                    ((Activity) ColorResolverDialogHelper.this.mContext).runOnUiThread(new Runnable() {
                        public void run() {
                            ColorResolverDialogHelper.this.updateOShareUI();
                            ColorResolverDialogHelper.this.mResolverOshareingAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            public void onSendSwitchChanged(boolean isOn) {
                if (ColorResolverDialogHelper.this.mContext != null && (ColorResolverDialogHelper.this.mContext instanceof Activity)) {
                    ((Activity) ColorResolverDialogHelper.this.mContext).runOnUiThread(new Runnable() {
                        public void run() {
                            ColorResolverDialogHelper.this.updateOShareUI();
                        }
                    });
                }
            }
        };
        this.mAlwaysUseOption = false;
        this.mLongclickListener = null;
        this.mOnItemClickListener = null;
        this.mContext = context;
        this.mIntent = intent;
        this.mResolveView = LayoutInflater.from(this.mContext).inflate(201917536, null);
        this.mViewPager = (ColorResolverDialogViewPager) this.mResolveView.findViewById(201458891);
        this.mDotView = (ColorDotView) this.mResolveView.findViewById(201458892);
        this.mColorMenuView = (ColorMenuView) this.mResolveView.findViewById(201458893);
        this.mAlertDialog = new Builder(this.mContext, 201524238).setDeleteDialogOption(2).setNegativeButton(this.mContext.getResources().getText(17039360), null).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (ColorResolverDialogHelper.this.mOShareServiceUtil != null) {
                    ColorResolverDialogHelper.this.mOShareServiceUtil.stop();
                    ColorResolverDialogHelper.this.mOShareServiceUtil.unregisterCallback(ColorResolverDialogHelper.this.mOShareCallback);
                    ColorResolverDialogHelper.this.mOShareServiceUtil = null;
                }
            }
        }).create();
    }

    private void initOShareService() {
        this.mOShareServiceUtil = new ColorOshareServiceUtil(this.mContext, this.mOShareInitListener);
        this.mOShareServiceUtil.initShareEngine();
    }

    private void initOShareView() {
        this.mResolveView.findViewById(201458983).setVisibility(0);
        this.mResolveView.findViewById(201458974);
        this.mOshareingPanel = this.mResolveView.findViewById(201458974);
        this.mRecyclerView = (ColorRecyclerView) this.mResolveView.findViewById(201458975);
        this.mResolverOshareingAdapter = new ResolverOshareingAdapter(this.mContext);
        ColorLinearLayoutManager layoutManager = new ColorLinearLayoutManager(this.mContext, 0, false);
        this.mRecyclerView.setAdapter(this.mResolverOshareingAdapter);
        this.mRecyclerView.setLayoutManager(layoutManager);
        this.mOshareIcon = this.mResolveView.findViewById(201458976);
        this.mOpenWifiBlueToothView = this.mResolveView.findViewById(201458977);
        this.mNoticeOpenOshareView = this.mResolveView.findViewById(201458978);
        this.mNoticeOpenOshareView.setVisibility(8);
        this.mOpenOsharePanel = this.mResolveView.findViewById(201458973);
        this.mOpenWifiBlueToothView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ColorResolverDialogHelper.this.mOShareServiceUtil != null) {
                    ColorResolverDialogHelper.this.mOShareServiceUtil.switchSend(true);
                }
            }
        });
        this.mNoticeHelpView = this.mResolveView.findViewById(201458979);
        this.mNoticeHelpView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    ColorResolverDialogHelper.this.mContext.startActivity(new Intent("coloros.intent.action.help"));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void create() {
        Parcelable targetParcelable = this.mIntent.getParcelableExtra("android.intent.extra.INTENT");
        this.mChosenComponentSender = (IntentSender) this.mIntent.getParcelableExtra("android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER");
        if (targetParcelable == null || ((targetParcelable instanceof Intent) ^ 1) == 0) {
            if (targetParcelable == null) {
                targetParcelable = this.mIntent;
            }
            this.mOriginIntent = (Intent) targetParcelable;
            String mActionStr = this.mOriginIntent.getAction();
            if (mActionStr != null && (mActionStr.equalsIgnoreCase("android.intent.action.SEND") || mActionStr.equalsIgnoreCase("android.intent.action.SEND_MULTIPLE"))) {
                initOShareService();
                initOShareView();
            }
            this.mRiList = this.mContext.getPackageManager().queryIntentActivities(this.mOriginIntent, 0);
            Parcelable[] pa = this.mIntent.getParcelableArrayExtra("android.intent.extra.INITIAL_INTENTS");
            Intent[] initialIntents = null;
            if (pa != null) {
                initialIntents = new Intent[pa.length];
                int i = 0;
                while (i < pa.length) {
                    if (pa[i] instanceof Intent) {
                        Intent in = pa[i];
                        modifyTargetIntent(in);
                        initialIntents[i] = in;
                        i++;
                    } else {
                        Log.w(TAG, "Initial intent #" + i + " not an Intent: " + pa[i]);
                        return;
                    }
                }
            }
            addInitiaIntents(initialIntents);
            this.mList.addAll(this.mRiList);
            this.mPageCount = (int) Math.ceil(((double) this.mList.size()) / ((double) getPagerSize()));
            this.mAlertDialog.setView(getResolveView());
            return;
        }
        Log.w(TAG, "Target is not an intent: " + targetParcelable);
    }

    private void modifyTargetIntent(Intent in) {
        String action = in.getAction();
    }

    public ColorResolverDialogHelper(Activity context, Intent intent, boolean alwaysUseOption) {
        this(context, intent, null, alwaysUseOption);
    }

    public ColorResolverDialogHelper(Activity context, Intent intent, Intent[] initialIntents, boolean alwaysUseOption) {
        this(context, intent, null, alwaysUseOption, null);
    }

    public ColorResolverDialogHelper(Activity context, Intent intent, Intent[] initialIntents, boolean alwaysUseOption, List<ResolveInfo> displayResolverlist) {
        int i = 0;
        this.TYPE_EMAIL = "email";
        this.TYPE_VIDEO = "video";
        this.TYPE_TEXT = "text";
        this.TYPE_PDF = "pdf";
        this.TYPE_WORD = "word";
        this.TYPE_EXCEL = "excel";
        this.TYPE_PPT = "ppt";
        this.mRiList = new ArrayList();
        this.mList = new ArrayList();
        this.mColorItemList = new ArrayList();
        this.mIsChecked = false;
        this.mOShareServiceInited = false;
        this.mBegineOshare = false;
        this.mOShareInitListener = /* anonymous class already generated */;
        this.mOShareCallback = /* anonymous class already generated */;
        this.mAlwaysUseOption = false;
        this.mLongclickListener = null;
        this.mOnItemClickListener = null;
        this.mContext = context;
        this.mActivity = context;
        this.mOriginIntent = intent;
        if (displayResolverlist != null) {
            this.mRiList = displayResolverlist;
        } else if (intent != null || initialIntents != null) {
            this.mRiList = context.getPackageManager().queryIntentActivities(intent, 0);
            if (this.mRiList.size() == 0) {
                Intent in = new Intent();
                if (intent.getAction() != null) {
                    in.setAction(intent.getAction());
                }
                if (intent.getType() != null) {
                    in.setType(intent.getType());
                }
                if (intent.getExtras() != null) {
                    in.putExtras(intent.getExtras());
                }
                PackageManager packageManager = context.getPackageManager();
                if (alwaysUseOption) {
                    i = 64;
                }
                this.mRiList = packageManager.queryIntentActivities(in, i | 65536);
            }
            addInitiaIntents(initialIntents);
        } else {
            return;
        }
        Log.d(TAG, "init " + this.mRiList + ", " + this.mOriginIntent);
        this.mResolveInfoHelper = ColorResolveInfoHelper.getInstance(context);
        this.mResolveInfoHelper.resort(this.mRiList, intent);
        this.mList.addAll(this.mRiList);
        Log.d(TAG, "resort " + this.mRiList);
        this.mPageCount = (int) Math.ceil(((double) this.mList.size()) / ((double) getPagerSize()));
    }

    public void resortList(List<ResolveInfo> displayResolverlist) {
        if (displayResolverlist != null) {
            this.mRiList = displayResolverlist;
        }
        this.mResolveInfoHelper.resort(this.mRiList, this.mOriginIntent);
        this.mList.clear();
        this.mList.addAll(this.mRiList);
        this.mPageCount = (int) Math.ceil(((double) this.mList.size()) / ((double) getPagerSize()));
    }

    public List<ResolveInfo> getResolveInforList() {
        return this.mList;
    }

    private void addInitiaIntents(Intent[] initialIntents) {
        if (initialIntents != null) {
            for (Intent ii : initialIntents) {
                if (ii != null) {
                    ActivityInfo ai = ii.resolveActivityInfo(this.mContext.getPackageManager(), 0);
                    if (ai == null) {
                        Log.w(TAG, "No activity found for " + ii);
                    } else {
                        ResolveInfo ri = new ResolveInfo();
                        ri.activityInfo = ai;
                        if (ii instanceof LabeledIntent) {
                            LabeledIntent li = (LabeledIntent) ii;
                            ri.resolvePackageName = li.getSourcePackage();
                            ri.labelRes = li.getLabelResource();
                            ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                            ri.icon = li.getIconResource();
                        }
                        this.mList.add(ri);
                    }
                }
            }
        }
    }

    public void setResolveView(ColorResolverDialogViewPager mViewPager, ColorDotView mDotView, CheckBox mCheckbox) {
        setResolveView(mViewPager, mDotView, mCheckbox, false);
    }

    private int getPagerSize() {
        int pagerSize;
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            pagerSize = 4;
        } else {
            pagerSize = 8;
        }
        if ((this.mActivity instanceof Activity) && this.mActivity.isInMultiWindowMode()) {
            return 4;
        }
        return pagerSize;
    }

    public void setResolveView(final ColorResolverDialogViewPager viewPager, final ColorDotView mDotView, CheckBox mCheckbox, boolean safeForwardingMode) {
        mDotView.setDotSize(this.mList.size());
        if (((int) Math.ceil(((double) this.mList.size()) / ((double) getPagerSize()))) > 1) {
            mDotView.setVisibility(0);
        } else {
            mDotView.setVisibility(8);
        }
        this.mPagerAdapter = new ColorResolverPagerAdapter(this.mContext, this.mListColorGridView, this.mList, this.mPageCount, this.mOriginIntent, mCheckbox, this.mAlertDialog, safeForwardingMode);
        if (this.mPagerAdapter.needMoreIcon() && this.mPagerAdapter.getMoreIconPageCount() == 1) {
            mDotView.setVisibility(8);
        }
        this.mViewPager = viewPager;
        viewPager.setAdapter(this.mPagerAdapter);
        viewPager.setColorResolverItemEventListener(new ColorResolverItemEventListener() {
            public void OnItemLongClick(int position) {
                if (ColorResolverDialogHelper.this.mLongclickListener != null) {
                    ColorResolverDialogHelper.this.mLongclickListener.onItemLongClick(null, null, position, -1);
                    viewPager.performHapticFeedback(0);
                }
            }

            public void OnItemClick(int position) {
                if (ColorResolverDialogHelper.this.mOnItemClickListener != null) {
                    ColorResolverDialogHelper.this.mOnItemClickListener.onItemClick(null, null, position, -1);
                }
            }
        });
        viewPager.setColorGridViewList(this.mListColorGridView, this.mList, this.mOriginIntent, mCheckbox, this.mAlertDialog);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageSelected(int position) {
                mDotView.setHightlightDot(position);
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longclickListener) {
        this.mLongclickListener = longclickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public ColorResolverPagerAdapter getPagerAdapter() {
        return this.mPagerAdapter;
    }

    public View getResolveView() {
        this.mDotView.setDotSize(this.mList.size());
        int dotCount = (int) Math.ceil(((double) this.mList.size()) / ((double) getPagerSize()));
        this.mPagerAdapter = new ColorResolverPagerAdapter(this.mContext, this.mListColorGridView, this.mList, this.mPageCount, this.mOriginIntent, null, this.mAlertDialog, false);
        this.mPagerAdapter.setChosenComponentSender(this.mChosenComponentSender);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mViewPager.setColorGridViewList(this.mListColorGridView, this.mList, this.mOriginIntent, null, this.mAlertDialog);
        this.mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageSelected(int position) {
                ColorResolverDialogHelper.this.mDotView.setHightlightDot(position);
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        return this.mResolveView;
    }

    public boolean setColorMenuView(List<ColorItem> colorItems) {
        if (colorItems.size() > 0) {
            this.mColorItemList.addAll(colorItems);
            this.mColorMenuView.setColorItem(this.mColorItemList);
            this.mColorMenuView.setVisibility(0);
            return true;
        }
        this.mColorMenuView.setVisibility(8);
        return false;
    }

    public void dismiss() {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.dismiss();
        } else if (this.mActivity != null && (this.mActivity.isFinishing() ^ 1) != 0) {
            this.mActivity.finish();
        }
    }

    public void show() {
        create();
        if (this.mAlertDialog != null) {
            this.mAlertDialog.show();
        }
    }

    public void show(Intent intent) {
        this.mIntent = intent;
        create();
        if (this.mAlertDialog != null) {
            this.mAlertDialog.show();
        }
    }

    public void setTitle(int resId) {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.setTitle(resId);
        }
    }

    public void setTitle(CharSequence title) {
        if (this.mAlertDialog != null) {
            this.mAlertDialog.setTitle(title);
        }
    }

    public void unRegister() {
        if (this.mPagerAdapter != null) {
            this.mPagerAdapter.unRegister();
        }
    }

    public void adjustPosition(List<String> priorPackageName) {
        if (this.mRiList.size() != 0) {
            this.mResolveInfoHelper.adjustPosition(this.mRiList, priorPackageName);
        }
    }

    public boolean clickMoreIcon(Activity activity, int position) {
        Log.d(TAG, "clickMoreIcon : " + position);
        if (!this.mPagerAdapter.needMoreIcon() || this.mPagerAdapter.getMoreIconTotalPosition() != position) {
            return false;
        }
        View pager = activity.findViewById(201458889);
        View dotsView = activity.findViewById(201458890);
        if (this.mPageCount > 1) {
            dotsView.setVisibility(0);
        }
        this.mPagerAdapter.setNeedMoreIcon(false);
        this.mPagerAdapter.setNeedAnim(true);
        this.mPagerAdapter.notifyDataSetChanged();
        if (needExpand()) {
            startExpandAnimation(pager, dotsView);
        }
        return true;
    }

    private void startExpandAnimation(View pager, View dotView) {
        ValueAnimator heightAnimation = getHeightAnim(pager);
        ValueAnimator dotAnimation = getDotViewAnim(dotView);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{heightAnimation, dotAnimation});
        animatorSet.start();
    }

    private ValueAnimator getHeightAnim(final View view) {
        int itemHeight = (int) this.mContext.getResources().getDimension(201655410);
        int dotHeight = ((int) this.mContext.getResources().getDimension(201655401)) * 2;
        int dotMarginTop = (int) this.mContext.getResources().getDimension(201654457);
        int distance;
        if (this.mPageCount > 1) {
            distance = (itemHeight + dotHeight) + dotMarginTop;
        } else {
            distance = itemHeight;
        }
        ValueAnimator anim = ValueAnimator.ofInt(new int[]{this.mAnimDistance, 0});
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setY((float) ((Integer) animation.getAnimatedValue()).intValue());
            }
        });
        anim.setDuration(350);
        anim.setInterpolator(new PathInterpolator(0.42f, 0.42f, 0.0f, 1.0f));
        return anim;
    }

    private ValueAnimator getDotViewAnim(final View view) {
        view.setAlpha(0.0f);
        ValueAnimator anim = ValueAnimator.ofInt(new int[]{0, 255});
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animatior) {
                view.setAlpha((float) ((Integer) animatior.getAnimatedValue()).intValue());
            }
        });
        anim.setDuration(240);
        anim.setStartDelay(300);
        anim.setInterpolator(new PathInterpolator(0.42f, 0.42f, 0.0f, 1.0f));
        return anim;
    }

    public boolean needExpand() {
        if (this.mResolveInfoHelper.isChooserAction(this.mOriginIntent) || getPagerSize() == 4) {
            return false;
        }
        int itemHeight = (int) this.mContext.getResources().getDimension(201655410);
        int dotHeight = ((int) this.mContext.getResources().getDimension(201655401)) * 2;
        int dotMarginTop = (int) this.mContext.getResources().getDimension(201654457);
        int total = this.mList.size();
        int resolveTopSize = this.mResolveInfoHelper.getResolveTopSize();
        int moreIconPageCount = (int) Math.ceil(((double) (resolveTopSize + 1)) / ((double) getPagerSize()));
        Log.d(TAG, "need expand, total : " + total + ", resolveTopSize : " + resolveTopSize + ", " + moreIconPageCount);
        if (resolveTopSize <= 0 || resolveTopSize >= 4) {
            if (this.mPageCount > 1 && moreIconPageCount == 1) {
                this.mAnimDistance = dotHeight + dotMarginTop;
                Log.d(TAG, "need expand, mAnimDistance : " + this.mAnimDistance);
                return true;
            }
        } else if (total > 4) {
            if (this.mPageCount > 1) {
                this.mAnimDistance = (itemHeight + dotHeight) + dotMarginTop;
            } else {
                this.mAnimDistance = itemHeight;
            }
            Log.d(TAG, "need expand, mAnimDistance : " + this.mAnimDistance);
            return true;
        }
        return false;
    }

    public void adjustForExpand(final Activity activity) {
        FrameLayout customPanel = (FrameLayout) activity.findViewById(16908808);
        customPanel.setPadding(0, 0, 0, 0);
        showRecommend(activity);
        if (needExpand()) {
            Log.d(TAG, "start adjustForExpand ");
            activity.getWindow().getAttributes().height = (int) this.mContext.getResources().getDimension(201655563);
            customPanel.setBackground(null);
            activity.findViewById(201458889).setBackgroundResource(201852168);
            View bottom = activity.findViewById(201458987);
            bottom.setBackgroundResource(201852163);
            bottom.setPadding(0, 0, 0, 0);
            activity.findViewById(201458986).setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    activity.finish();
                    return false;
                }
            });
            if (((CheckBox) activity.findViewById(201458856)).getVisibility() == 8) {
                View dotsView = activity.findViewById(201458890);
                LayoutParams params = (LayoutParams) dotsView.getLayoutParams();
                params.bottomMargin = (int) this.mContext.getResources().getDimension(201654460);
                dotsView.setLayoutParams(params);
            }
        }
    }

    public void showTargetDetails(ResolveInfo ri, SharedPreferences prefs, String type, ResolveListAdapter adapter) {
        int i;
        final String componentName = ri.activityInfo.getComponentName().flattenToShortString();
        boolean pinned = false;
        Set pinPrefList = null;
        if (TYPE_GALLERY.equals(type)) {
            String galleryPinList = Secure.getString(this.mContext.getContentResolver(), GALLERY_PIN_LIST);
            if (!TextUtils.isEmpty(galleryPinList)) {
                pinPrefList = new HashSet(Arrays.asList(galleryPinList.split(";")));
            }
        } else {
            pinPrefList = prefs.getStringSet(type, null);
        }
        if (pinPrefList != null) {
            pinned = pinPrefList.contains(componentName);
        }
        Log.d(TAG, "showTargetDetails : " + pinPrefList + ", type : " + type + ", componentName : " + componentName + ", isPinned : " + pinned);
        Builder deleteDialogOption = new Builder(this.mContext).setDeleteDialogOption(3);
        if (pinned) {
            i = 201786395;
        } else {
            i = 201786394;
        }
        final SharedPreferences sharedPreferences = prefs;
        final String str = type;
        final ResolveListAdapter resolveListAdapter = adapter;
        final ResolveInfo resolveInfo = ri;
        deleteDialogOption.setItems(i, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        ColorResolverDialogHelper.this.updatePinnedData(sharedPreferences, componentName, str);
                        resolveListAdapter.handlePackagesChanged();
                        return;
                    case 1:
                        ColorResolverDialogHelper.this.mContext.startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", resolveInfo.activityInfo.packageName, null)).addFlags(524288));
                        ((Activity) ColorResolverDialogHelper.this.mContext).overridePendingTransition(201981964, 201981968);
                        return;
                    default:
                        return;
                }
            }
        }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).create().show();
    }

    private void updatePinnedData(SharedPreferences prefs, String componentName, String type) {
        boolean isPinned = false;
        if (TYPE_GALLERY.equals(type)) {
            String galleryPinList = Secure.getString(this.mContext.getContentResolver(), GALLERY_PIN_LIST);
            Log.d(TAG, "galleryPinList = " + galleryPinList);
            List<String> newList = new ArrayList();
            if (!TextUtils.isEmpty(galleryPinList)) {
                List<String> list = Arrays.asList(galleryPinList.split(";"));
                isPinned = list.contains(componentName);
                newList = new ArrayList(list);
            }
            Log.d(TAG, "newList = " + newList);
            if (isPinned) {
                newList.remove(componentName);
                Log.d(TAG, "remove : " + componentName);
            } else {
                newList.add(componentName);
                Log.d(TAG, "add : " + componentName);
            }
            String newString = listToString(newList, ';');
            Secure.putString(this.mContext.getContentResolver(), GALLERY_PIN_LIST, newString);
            Log.d(TAG, "putStringForUser : " + newString);
            return;
        }
        Set<String> pinPrefList = prefs.getStringSet(type, null);
        Set<String> newList2 = new HashSet();
        if (pinPrefList != null) {
            isPinned = pinPrefList.contains(componentName);
            newList2 = new HashSet(pinPrefList);
        }
        Log.d(TAG, "newList = " + newList2);
        if (isPinned) {
            prefs.edit().remove(type).apply();
            newList2.remove(componentName);
            prefs.edit().putStringSet(type, newList2).apply();
            Log.d(TAG, "remove : " + componentName);
            return;
        }
        prefs.edit().remove(type).apply();
        newList2.add(componentName);
        prefs.edit().putStringSet(type, newList2).apply();
        Log.d(TAG, "add : " + componentName);
    }

    private String listToString(List<String> list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                sb.append((String) list.get(i));
            } else {
                sb.append((String) list.get(i));
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    private void showRecommend(Activity activity) {
        View marketJump = activity.findViewById(201458997);
        String intentType = this.mResolveInfoHelper.getIntentType(this.mOriginIntent);
        if (!this.mResolveInfoHelper.isMarketRecommendType(intentType)) {
            marketJump.setVisibility(8);
        } else if (support(this.mContext)) {
            marketJump.setVisibility(0);
            if ("txt".equals(intentType)) {
                intentType = "text";
            }
            final String type = intentType;
            marketJump.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ColorResolverDialogHelper.this.startRecommend(ColorResolverDialogHelper.this.mContext, type);
                }
            });
        } else {
            marketJump.setVisibility(8);
        }
    }

    private boolean startRecommend(Context context, String type) {
        int code = 0;
        try {
            Uri uri = Uri.parse("content://oaps_mk");
            Bundle bundle = new Bundle();
            bundle.putString("rtp", type);
            bundle.putString("goback", "1");
            bundle.putString("secret", SECRET);
            bundle.putString("enterId", KEY);
            bundle.putString("sgtp", "1");
            Bundle responseBundle = call(context, uri, "/recapp", bundle);
            if (responseBundle != null && responseBundle.containsKey("code")) {
                code = responseBundle.getInt("code");
            }
            Log.d(TAG, "startRecommend:" + type + ",response:" + code);
            if (code == 1) {
                Map map = new HashMap();
                map.put(KEY_TYPE, type);
                OppoStatistics.onCommon(this.mContext, CODE, RECOMMEND_EVENT_ID, map, false);
                Log.d(TAG, "statistics data [resolver_recommend]: " + map);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (code == 1) {
            return true;
        }
        return false;
    }

    private boolean support(Context context) {
        Uri uri = Uri.parse("content://oaps_mk");
        String supportPath = "/support";
        Bundle bundle = new Bundle();
        bundle.putString("tp", "/recapp");
        bundle.putString("secret", SECRET);
        bundle.putString("enterId", KEY);
        bundle.putString("sgtp", "1");
        int code = 0;
        try {
            Bundle responseBundle = call(context, uri, supportPath, bundle);
            if (responseBundle != null && responseBundle.containsKey("code")) {
                code = responseBundle.getInt("code");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Log.d(TAG, "oaps support:" + code);
        if (code == 1) {
            return true;
        }
        return false;
    }

    private Bundle call(Context context, Uri uri, String path, Bundle bundle) {
        try {
            return context.getContentResolver().call(uri, path, "", bundle);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}

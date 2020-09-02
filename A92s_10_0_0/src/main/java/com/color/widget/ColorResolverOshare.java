package com.color.widget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LinearLayoutManager;
import com.android.internal.widget.RecyclerView;
import com.color.oshare.ColorOshareDevice;
import com.color.oshare.ColorOshareServiceUtil;
import com.color.oshare.ColorOshareState;
import com.color.oshare.IColorOshareCallback;
import com.color.oshare.IColorOshareInitListener;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import com.color.widget.ColorResolverOshare;
import java.util.List;

public class ColorResolverOshare {
    private static final String O_SHARE_CLASS = "com.coloros.oshare.ui.GrantUriActivity";
    private static final String O_SHARE_PACKAGE = "com.coloros.oshare";
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public IColorOshareCallback mOShareCallback = new IColorOshareCallback.Stub() {
        /* class com.color.widget.ColorResolverOshare.AnonymousClass2 */

        @Override // com.color.oshare.IColorOshareCallback
        public void onDeviceChanged(List<ColorOshareDevice> deviceList) throws RemoteException {
            if (ColorResolverOshare.this.mContext != null && (ColorResolverOshare.this.mContext instanceof Activity)) {
                ((Activity) ColorResolverOshare.this.mContext).runOnUiThread(new Runnable(deviceList) {
                    /* class com.color.widget.$$Lambda$ColorResolverOshare$2$L4e_WSF3NOpsBZNm5DqJQ_zLMDQ */
                    private final /* synthetic */ List f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ColorResolverOshare.AnonymousClass2.this.lambda$onDeviceChanged$0$ColorResolverOshare$2(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onDeviceChanged$0$ColorResolverOshare$2(List deviceList) {
            ColorResolverOshare.this.updateOShareUI(deviceList);
            if (ColorResolverOshare.this.mResolverOshareingAdapter != null) {
                ColorResolverOshare.this.mResolverOshareingAdapter.setDeviceList(deviceList);
                if (ColorResolverOshare.this.mRecyclerView != null && !ColorResolverOshare.this.mRecyclerView.isComputingLayout()) {
                    if (ColorResolverOshare.this.mResolverOshareingAdapter.isDefaultAction() || ColorResolverOshare.this.mResolverOshareingAdapter.isUpOrCancelAction()) {
                        ColorResolverOshare.this.mResolverOshareingAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override // com.color.oshare.IColorOshareCallback
        public void onSendSwitchChanged(boolean isOn) {
            if (ColorResolverOshare.this.mContext != null && (ColorResolverOshare.this.mContext instanceof Activity)) {
                ((Activity) ColorResolverOshare.this.mContext).runOnUiThread(new Runnable() {
                    /* class com.color.widget.$$Lambda$ColorResolverOshare$2$XMQ6J18DuhB_fbwWjLjd4oUK4uk */

                    public final void run() {
                        ColorResolverOshare.AnonymousClass2.this.lambda$onSendSwitchChanged$1$ColorResolverOshare$2();
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onSendSwitchChanged$1$ColorResolverOshare$2() {
            ColorResolverOshare.this.updateOShareUI(null);
        }
    };
    private IColorOshareInitListener mOShareInitListener = new IColorOshareInitListener.Stub() {
        /* class com.color.widget.ColorResolverOshare.AnonymousClass1 */

        @Override // com.color.oshare.IColorOshareInitListener
        public void onShareUninit() throws RemoteException {
            boolean unused = ColorResolverOshare.this.mOShareServiceInited = false;
            if (ColorResolverOshare.this.mOShareServiceUtil != null) {
                ColorResolverOshare.this.mOShareServiceUtil.unregisterCallback(ColorResolverOshare.this.mOShareCallback);
            }
        }

        @Override // com.color.oshare.IColorOshareInitListener
        public void onShareInit() throws RemoteException {
            boolean unused = ColorResolverOshare.this.mOShareServiceInited = true;
            if (ColorResolverOshare.this.mContext != null && (ColorResolverOshare.this.mContext instanceof Activity)) {
                ((Activity) ColorResolverOshare.this.mContext).runOnUiThread(new Runnable() {
                    /* class com.color.widget.$$Lambda$ColorResolverOshare$1$uOugzJvvA8lqkfC_7bzJqXd7jy0 */

                    public final void run() {
                        ColorResolverOshare.AnonymousClass1.this.lambda$onShareInit$0$ColorResolverOshare$1();
                    }
                });
            }
            if (ColorResolverOshare.this.mOShareServiceUtil != null) {
                ColorResolverOshare.this.mOShareServiceUtil.registerCallback(ColorResolverOshare.this.mOShareCallback);
            }
        }

        public /* synthetic */ void lambda$onShareInit$0$ColorResolverOshare$1() {
            ColorResolverOshare.this.updateOShareUI(null);
        }
    };
    /* access modifiers changed from: private */
    public boolean mOShareServiceInited = false;
    /* access modifiers changed from: private */
    public ColorOshareServiceUtil mOShareServiceUtil;
    /* access modifiers changed from: private */
    public Intent mOriginIntent;
    private View mOshareClosedView;
    private View mOshareOpenedView;
    private View mOshareSharingView;
    RecyclerView mRecyclerView;
    /* access modifiers changed from: private */
    public ResolverOshareingAdapter mResolverOshareingAdapter;

    public ColorResolverOshare(Context context, Intent intent) {
        this.mContext = context;
        this.mOriginIntent = intent;
    }

    /* access modifiers changed from: package-private */
    public void initOShareService() {
        this.mOShareServiceUtil = new ColorOshareServiceUtil(this.mContext, this.mOShareInitListener);
        this.mOShareServiceUtil.initShareEngine();
    }

    /* access modifiers changed from: package-private */
    public void initOShareView(View oShareView) {
        this.mOshareClosedView = oShareView.findViewById(201458973);
        View view = this.mOshareClosedView;
        if (view instanceof ViewStub) {
            this.mOshareClosedView = ((ViewStub) view).inflate();
        }
        this.mOshareOpenedView = oShareView.findViewById(201458978);
        this.mOshareSharingView = oShareView.findViewById(201458974);
        float fontScale = this.mContext.getResources().getConfiguration().fontScale;
        TextView blueToothTitle = (TextView) this.mOshareClosedView.findViewById(201458988);
        if (blueToothTitle != null) {
            blueToothTitle.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) this.mContext.getResources().getDimensionPixelSize(201654413), fontScale, 4)));
        }
        Button oshareOpen = (Button) this.mOshareClosedView.findViewById(201459054);
        if (oshareOpen != null) {
            oshareOpen.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) this.mContext.getResources().getDimensionPixelSize(201654413), fontScale, 4)));
            oshareOpen.setOnClickListener(new View.OnClickListener() {
                /* class com.color.widget.$$Lambda$ColorResolverOshare$XFpriM_f0q54is1AnsBsbIFKKe4 */

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ColorResolverOshare.this.lambda$initOShareView$0$ColorResolverOshare(view);
                }
            });
        }
    }

    public /* synthetic */ void lambda$initOShareView$0$ColorResolverOshare(View v) {
        ColorOshareServiceUtil colorOshareServiceUtil = this.mOShareServiceUtil;
        if (colorOshareServiceUtil != null && !colorOshareServiceUtil.isSendOn()) {
            this.mOShareServiceUtil.switchSend(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void onResume() {
        ColorOshareServiceUtil colorOshareServiceUtil = this.mOShareServiceUtil;
        if (colorOshareServiceUtil != null) {
            try {
                colorOshareServiceUtil.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPause() {
        ColorOshareServiceUtil colorOshareServiceUtil = this.mOShareServiceUtil;
        if (colorOshareServiceUtil != null) {
            try {
                colorOshareServiceUtil.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDestroy() {
        ColorOshareServiceUtil colorOshareServiceUtil = this.mOShareServiceUtil;
        if (colorOshareServiceUtil != null) {
            colorOshareServiceUtil.stop();
            this.mOShareServiceUtil.unregisterCallback(this.mOShareCallback);
            this.mOShareServiceUtil = null;
        }
    }

    /* access modifiers changed from: private */
    public void updateOShareUI(List<ColorOshareDevice> deviceList) {
        resetOshareView();
        ColorOshareServiceUtil colorOshareServiceUtil = this.mOShareServiceUtil;
        if (colorOshareServiceUtil == null || !colorOshareServiceUtil.isSendOn()) {
            View view = this.mOshareClosedView;
            if (view != null) {
                view.setVisibility(0);
            }
        } else if (deviceList == null || deviceList.size() < 1) {
            initOshareOpenedView();
            View view2 = this.mOshareOpenedView;
            if (view2 != null) {
                view2.setVisibility(0);
            }
        } else {
            initOsharingView();
            View view3 = this.mOshareSharingView;
            if (view3 != null) {
                view3.setVisibility(0);
            }
        }
    }

    private void initOshareOpenedView() {
        View view = this.mOshareOpenedView;
        if (view instanceof ViewStub) {
            this.mOshareOpenedView = ((ViewStub) view).inflate();
        }
        if (this.mOshareSharingView != null) {
            float fontScale = this.mContext.getResources().getConfiguration().fontScale;
            TextView shareTitle = (TextView) this.mOshareOpenedView.findViewById(201458989);
            if (shareTitle != null) {
                shareTitle.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) this.mContext.getResources().getDimensionPixelSize(201654413), fontScale, 4)));
            }
            View noticeHelpView = this.mOshareOpenedView.findViewById(201459048);
            if (noticeHelpView != null && !noticeHelpView.hasOnClickListeners()) {
                ((TextView) noticeHelpView.findViewById(201458979)).setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) this.mContext.getResources().getDimensionPixelSize(201654413), fontScale, 4)));
                noticeHelpView.setOnClickListener(new View.OnClickListener() {
                    /* class com.color.widget.$$Lambda$ColorResolverOshare$9CH_Ynnfh9pnapGtd1U7l1u9Ies */

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        ColorResolverOshare.this.lambda$initOshareOpenedView$1$ColorResolverOshare(view);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$initOshareOpenedView$1$ColorResolverOshare(View v) {
        try {
            this.mContext.startActivity(new Intent("coloros.intent.action.help"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initOsharingView() {
        View view = this.mOshareSharingView;
        if (view instanceof ViewStub) {
            this.mOshareSharingView = ((ViewStub) view).inflate();
        }
        if (this.mOshareSharingView != null && this.mResolverOshareingAdapter == null) {
            float fontScale = this.mContext.getResources().getConfiguration().fontScale;
            TextView oshareText = (TextView) this.mOshareSharingView.findViewById(201459055);
            if (oshareText != null) {
                oshareText.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) this.mContext.getResources().getDimensionPixelSize(201654409), fontScale, 4)));
            }
            this.mRecyclerView = (RecyclerView) this.mOshareSharingView.findViewById(201458975);
            this.mResolverOshareingAdapter = new ResolverOshareingAdapter(this.mContext);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.mContext, 0, false);
            this.mRecyclerView.setAdapter(this.mResolverOshareingAdapter);
            this.mRecyclerView.setLayoutManager(layoutManager);
        }
    }

    private void resetOshareView() {
        View view = this.mOshareClosedView;
        if (view != null) {
            view.setVisibility(8);
        }
        View view2 = this.mOshareOpenedView;
        if (view2 != null) {
            view2.setVisibility(8);
        }
        View view3 = this.mOshareSharingView;
        if (view3 != null) {
            view3.setVisibility(8);
        }
    }

    private static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public static final int EMPTY_TYPE = -1;

        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        /* access modifiers changed from: private */
        public final View processCover;
        public final TextView userName;
        public final View userPanel;
        public final ImageView userPic;
        public final ColorCircleProgressBar userProgress;
        public final TextView userStatus;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.userName = (TextView) itemView.findViewById(201458980);
            this.userStatus = (TextView) itemView.findViewById(201458981);
            this.userPic = (ImageView) itemView.findViewById(201458982);
            this.userProgress = (ColorCircleProgressBar) itemView.findViewById(201458971);
            this.processCover = itemView.findViewById(201459053);
            this.userPanel = itemView.findViewById(201458972);
        }
    }

    /* access modifiers changed from: private */
    public class ResolverOshareingAdapter extends RecyclerView.Adapter {
        private static final int MI = 30;
        private static final int OPPO = 10;
        private static final int REALME = 11;
        private static final int VIVO = 20;
        String BUSUY_STR;
        String CANCEL_STR;
        String CANCEL_WAIT_STR;
        private final int ICON_COVER_COLOR = Color.parseColor("#7F000000");
        String READY_STR;
        String TRANSITING_STR;
        String TRANSIT_FAILED_STR;
        String TRANSIT_REJECT_STR;
        String TRANSIT_SUCCESS_STR;
        String TRANSIT_TIMEOUT_STR;
        String TRANSIT_WAIT_STR;
        private int mAction = Integer.MIN_VALUE;
        private Context mContext;
        private List<ColorOshareDevice> mDeviceList;
        int mStateTextColorFail;
        int mStateTextColorNomarl;
        int mStateTextColorSucces;

        public void setDeviceList(List<ColorOshareDevice> deviceList) {
            this.mDeviceList = deviceList;
        }

        public ResolverOshareingAdapter(Context context) {
            this.mContext = context;
            this.mStateTextColorNomarl = context.getColor(201721002);
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

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View
         arg types: [int, android.view.ViewGroup, int]
         candidates:
          android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View
          android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View */
        @Override // com.android.internal.widget.RecyclerView.Adapter
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == -1) {
                View view = new View(parent.getContext());
                view.setLayoutParams(new LinearLayout.LayoutParams(parent.getResources().getDimensionPixelSize(201655791), 1));
                return new EmptyViewHolder(view);
            }
            MyViewHolder myViewHolder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(201917586, parent, false));
            myViewHolder.processCover.setVisibility(8);
            return myViewHolder;
        }

        @Override // com.android.internal.widget.RecyclerView.Adapter
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            List<ColorOshareDevice> list = this.mDeviceList;
            if (list != null && !list.isEmpty() && position <= this.mDeviceList.size() && (viewHolder instanceof MyViewHolder) && position != 0) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                ColorOshareDevice receiver = this.mDeviceList.get(position - 1);
                if (receiver != null) {
                    bindCompanyIcon(holder.userPic, receiver.getVender());
                    if (receiver.getState() == ColorOshareState.TRANSITING) {
                        holder.userPic.setColorFilter(this.ICON_COVER_COLOR);
                        holder.userProgress.setVisibility(0);
                        holder.userProgress.setProgress(receiver.getProgress());
                    } else {
                        holder.userPic.clearColorFilter();
                        holder.userProgress.setVisibility(8);
                    }
                    holder.userStatus.setText(getStateString(receiver.getState()));
                    holder.userStatus.setTextColor(getStateColor(receiver.getState()));
                    holder.userName.setText(receiver.getName());
                    holder.userPic.setOnTouchListener(new View.OnTouchListener(receiver) {
                        /* class com.color.widget.$$Lambda$ColorResolverOshare$ResolverOshareingAdapter$Wc1xqmhsQECsvh7SGea7ardXLxA */
                        private final /* synthetic */ ColorOshareDevice f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // android.view.View.OnTouchListener
                        public final boolean onTouch(View view, MotionEvent motionEvent) {
                            return ColorResolverOshare.ResolverOshareingAdapter.this.lambda$onBindViewHolder$0$ColorResolverOshare$ResolverOshareingAdapter(this.f$1, view, motionEvent);
                        }
                    });
                }
            }
        }

        public /* synthetic */ boolean lambda$onBindViewHolder$0$ColorResolverOshare$ResolverOshareingAdapter(ColorOshareDevice receiver, View v, MotionEvent event) {
            return fadeBackTouchEvent(event, receiver);
        }

        @Override // com.android.internal.widget.RecyclerView.Adapter
        public int getItemViewType(int position) {
            if (position == 0) {
                return -1;
            }
            return super.getItemViewType(position);
        }

        @Override // com.android.internal.widget.RecyclerView.Adapter
        public int getItemCount() {
            List<ColorOshareDevice> list = this.mDeviceList;
            if (list == null || list.isEmpty()) {
                return 0;
            }
            return this.mDeviceList.size() + 1;
        }

        private String getStateString(ColorOshareState state) {
            switch (state) {
                case READY:
                    return this.READY_STR;
                case TRANSIT_WAIT:
                    return this.TRANSIT_WAIT_STR;
                case TRANSITING:
                    return this.TRANSITING_STR;
                case TRANSIT_FAILED:
                    return this.TRANSIT_FAILED_STR;
                case TRANSIT_REJECT:
                    return this.TRANSIT_REJECT_STR;
                case TRANSIT_SUCCESS:
                    return this.TRANSIT_SUCCESS_STR;
                case BUSUY:
                    return this.BUSUY_STR;
                case CANCEL:
                    return this.CANCEL_STR;
                case CANCEL_WAIT:
                    return this.CANCEL_WAIT_STR;
                case TRANSIT_TIMEOUT:
                    return this.TRANSIT_TIMEOUT_STR;
                default:
                    return "";
            }
        }

        private void bindCompanyIcon(ImageView userPic, int vender) {
            int userPicResource;
            if (vender == 10) {
                userPicResource = 201852307;
            } else if (vender == 11) {
                userPicResource = 201852310;
            } else if (vender == 20) {
                userPicResource = 201852308;
            } else if (vender != 30) {
                userPicResource = 201852306;
            } else {
                userPicResource = 201852309;
            }
            userPic.setImageResource(userPicResource);
            userPic.getDrawable().mutate();
        }

        private int getStateColor(ColorOshareState state) {
            int color = this.mStateTextColorNomarl;
            switch (state) {
                case READY:
                case TRANSIT_WAIT:
                case TRANSITING:
                case TRANSIT_SUCCESS:
                case CANCEL:
                    return this.mStateTextColorSucces;
                case TRANSIT_FAILED:
                case TRANSIT_REJECT:
                case BUSUY:
                case TRANSIT_TIMEOUT:
                    return this.mStateTextColorFail;
                case CANCEL_WAIT:
                default:
                    return color;
            }
        }

        private void oShareClick(ColorOshareDevice receiver) {
            if (ColorResolverOshare.this.mOShareServiceUtil != null && ColorResolverOshare.this.mOShareServiceInited) {
                Intent grantIntent = new Intent(ColorResolverOshare.this.mOriginIntent);
                grantIntent.setComponent(new ComponentName(ColorResolverOshare.O_SHARE_PACKAGE, ColorResolverOshare.O_SHARE_CLASS));
                grantIntent.addFlags(65536);
                try {
                    ((Activity) this.mContext).startActivityAsCaller(grantIntent, null, null, false, -10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ColorResolverOshare.this.mOShareServiceUtil.sendData(ColorResolverOshare.this.mOriginIntent, receiver);
            }
        }

        private boolean fadeBackTouchEvent(MotionEvent event, ColorOshareDevice receiver) {
            this.mAction = event.getAction();
            if (isUpOrCancelAction()) {
                if (ColorResolverOshare.this.mRecyclerView != null && !ColorResolverOshare.this.mRecyclerView.isComputingLayout()) {
                    notifyDataSetChanged();
                } else if (ColorResolverOshare.this.mRecyclerView != null) {
                    ColorResolverOshare.this.mRecyclerView.post(new Runnable() {
                        /* class com.color.widget.$$Lambda$ColorResolverOshare$ResolverOshareingAdapter$9QYL3grTJtJeQDiwPOLR2_HFAY */

                        public final void run() {
                            ColorResolverOshare.ResolverOshareingAdapter.this.lambda$fadeBackTouchEvent$1$ColorResolverOshare$ResolverOshareingAdapter();
                        }
                    });
                }
                if (this.mAction == 1) {
                    oShareClick(receiver);
                }
            }
            return true;
        }

        public /* synthetic */ void lambda$fadeBackTouchEvent$1$ColorResolverOshare$ResolverOshareingAdapter() {
            notifyDataSetChanged();
        }

        /* access modifiers changed from: package-private */
        public boolean isUpOrCancelAction() {
            int i = this.mAction;
            return i == 3 || i == 1;
        }

        /* access modifiers changed from: package-private */
        public boolean isDefaultAction() {
            return this.mAction == Integer.MIN_VALUE;
        }
    }
}

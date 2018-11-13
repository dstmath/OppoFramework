package com.oppo.util;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.PopupWindow;

public class OppoListBackToTopUtils {
    private static final int DISSMISS_TIMEOUT = 1000;
    private int BTN_LOC_X;
    private int BTN_LOC_Y;
    Runnable dissmissAuto = new Runnable() {
        public void run() {
            if (OppoListBackToTopUtils.this.mPopupWindow != null && OppoListBackToTopUtils.this.mPopupWindow.isShowing()) {
                OppoListBackToTopUtils.this.mPopupWindow.dismiss();
            }
        }
    };
    Handler handler = new Handler();
    private Button mBackToTop;
    private Context mContext;
    private AbsListView mList;
    private PopupWindow mPopupWindow;

    public OppoListBackToTopUtils(AbsListView list) {
        this.mList = list;
        this.mContext = this.mList.getContext();
        DisplayMetrics dm = this.mList.getContext().getResources().getDisplayMetrics();
        this.BTN_LOC_X = (dm.widthPixels * 85) / 100;
        this.BTN_LOC_Y = (dm.heightPixels * 80) / 100;
    }

    private void initBackToTopWindow() {
        ViewGroup mPopupContent = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(201917517, null);
        this.mBackToTop = (Button) mPopupContent.findViewById(201458853);
        this.mBackToTop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OppoListBackToTopUtils.this.mList.setSelection(2);
                OppoListBackToTopUtils.this.mList.startFlingToTop();
            }
        });
        this.mPopupWindow = new PopupWindow(this.mContext.getApplicationContext());
        this.mPopupWindow.setContentView(mPopupContent);
        this.mPopupWindow.setAnimationStyle(201524228);
        this.mPopupWindow.setFocusable(false);
        this.mPopupWindow.setWindowLayoutMode(-2, -2);
        this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
    }

    public void showBackToTop(boolean show) {
        this.handler.removeCallbacks(this.dissmissAuto);
        if (show) {
            if (this.mPopupWindow == null) {
                initBackToTopWindow();
            }
            if (!this.mPopupWindow.isShowing()) {
                this.mBackToTop.setEnabled(true);
                this.mPopupWindow.showAtLocation(this.mList, 0, this.BTN_LOC_X, this.BTN_LOC_Y);
            }
            this.handler.postDelayed(this.dissmissAuto, 1000);
        } else if (this.mPopupWindow != null && this.mPopupWindow.isShowing()) {
            this.mPopupWindow.dismiss();
        }
    }

    public void dismiss() {
        this.mPopupWindow.dismiss();
    }

    public void setBtnHorizontalPos(int xPos) {
        this.BTN_LOC_X = xPos;
    }

    public void setBtnVerticalPos(int yPos) {
        this.BTN_LOC_Y = yPos;
    }

    public void updateBtnPosition() {
        DisplayMetrics dm = this.mList.getContext().getResources().getDisplayMetrics();
        this.BTN_LOC_X = (dm.widthPixels * 85) / 100;
        this.BTN_LOC_Y = (dm.heightPixels * 80) / 100;
        if (this.mPopupWindow != null && this.mPopupWindow.isShowing()) {
            this.handler.post(this.dissmissAuto);
        }
    }
}

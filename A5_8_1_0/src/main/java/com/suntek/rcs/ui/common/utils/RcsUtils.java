package com.suntek.rcs.ui.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import com.suntek.rcs.ui.common.RcsEmojiGifView;
import com.suntek.rcs.ui.common.RcsLog;
import java.io.Closeable;

public class RcsUtils {
    public static int dip2px(Context context, float dipValue) {
        return (int) ((dipValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static void closeKB(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            ((InputMethodManager) activity.getSystemService("input_method")).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 2);
        }
    }

    public static void openKB(Context context) {
        ((InputMethodManager) context.getSystemService("input_method")).toggleSoftInput(0, 2);
    }

    public static void openPopupWindow(Context context, View view, byte[] data, int emojiPopupBgResId) {
        LayoutParams mGifParam = new LayoutParams(-2, -2);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap != null) {
            int windowWidth = bitmap.getWidth() + dip2px(context, 40.0f);
            int windowHeight = bitmap.getHeight() + dip2px(context, 40.0f);
            ColorDrawable transparent = new ColorDrawable(0);
            RelativeLayout relativeLayout = new RelativeLayout(context);
            relativeLayout.setLayoutParams(new LayoutParams(windowWidth, windowHeight));
            relativeLayout.setBackgroundResource(emojiPopupBgResId);
            relativeLayout.setGravity(17);
            RcsEmojiGifView emojiGifView = new RcsEmojiGifView(context);
            emojiGifView.setLayoutParams(mGifParam);
            emojiGifView.setBackground(transparent);
            emojiGifView.setMonieByteData(data);
            relativeLayout.addView(emojiGifView);
            PopupWindow popupWindow = new PopupWindow(view, windowWidth, windowHeight);
            popupWindow.setBackgroundDrawable(transparent);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setContentView(relativeLayout);
            popupWindow.showAtLocation(view, 17, 0, 0);
            popupWindow.update();
        }
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable e) {
                RcsLog.e(e);
            }
        }
    }
}

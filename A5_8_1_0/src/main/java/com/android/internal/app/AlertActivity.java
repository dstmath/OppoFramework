package com.android.internal.app;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.app.AlertController.AlertParams;
import com.color.util.ColorContextUtil;

public abstract class AlertActivity extends Activity implements DialogInterface {
    protected AlertController mAlert;
    protected AlertParams mAlertParams;

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Yujun.Feng@Plf.SDK : Add for disable the statusBar", property = OppoRomType.ROM)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert = AlertController.create(this, this, getWindow());
        this.mAlertParams = new AlertParams(this);
        if (ColorContextUtil.isOppoStyle(this)) {
            setStatusBarDisable();
        }
    }

    public void cancel() {
        finish();
    }

    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return dispatchPopulateAccessibilityEvent(this, event);
    }

    public static boolean dispatchPopulateAccessibilityEvent(Activity act, AccessibilityEvent event) {
        event.setClassName(Dialog.class.getName());
        event.setPackageName(act.getPackageName());
        LayoutParams params = act.getWindow().getAttributes();
        boolean isFullScreen = params.width == -1 ? params.height == -1 : false;
        event.setFullScreen(isFullScreen);
        return false;
    }

    protected void setupAlert() {
        this.mAlert.installContent(this.mAlertParams);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Yujun.Feng@Plf.SDK : Add for when the dialog is the middle's dialog,the statusBar is not pull down", property = OppoRomType.ROM)
    private void setStatusBarDisable() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.isDisableStatusBar = 1;
        dialogWindow.setAttributes(p);
    }
}

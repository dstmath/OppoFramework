package com.android.server.am;

import android.app.AlertDialog;
import android.app.Dialog;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Message;
import android.util.Slog;
import com.android.server.wm.ColorAppStartupManagerHelper;
import com.color.util.ColorTypeCastingHelper;

public final class ColorActivityManagerMessageHelper extends ColorActivityManagerCommonHelper {
    static final int COLOR_AMS_MSG_INDEX = 1001;
    static final int NOT_ALLOWED_START_MSG = 1002;
    static final int RESET_SLEEP_CHECK_FLAG_MSG = 1005;
    static final int SHOW_ACTIVITY_BEHIND_KEYGUARD_MSG = 1006;
    static final int START_CHOOSER_ACTIVITY_MSG = 1001;
    static final int START_OPPO_SITE_MSG = 1003;
    private static final String TAG = "ColorActivityManagerMessageHelper";
    private IColorActivityManagerServiceInner mColorActivityManagerServiceInner = null;

    public ColorActivityManagerMessageHelper(Context context, IColorActivityManagerServiceEx amsEx) {
        super(context, amsEx);
        if (amsEx != null) {
            this.mColorActivityManagerServiceInner = amsEx.getColorActivityManagerServiceInner();
        }
    }

    public boolean handleMessage(Message msg, int whichHandler) {
        if (!checkCodeValid(msg.what, 2, 1)) {
            if (DEBUG) {
                Slog.i(TAG, "Invalid message = " + msg);
            }
            return false;
        } else if (whichHandler == 1) {
            return handleMainMessage(msg);
        } else {
            if (whichHandler == 2) {
                return handleUiMessage(msg);
            }
            if (whichHandler == 3) {
                return handleBgMessage(msg);
            }
            if (whichHandler == 4) {
                return handleKillMessage(msg);
            }
            Slog.i(TAG, "Unknow handle = " + whichHandler);
            return false;
        }
    }

    private boolean handleMainMessage(Message msg) {
        int i = msg.what;
        if (i == 1001) {
            OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).handleChooseActivityMsg(msg);
            return false;
        } else if (i != 1003) {
            return false;
        } else {
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).handOppoSiteMsg(msg);
            return false;
        }
    }

    private boolean handleUiMessage(Message msg) {
        if (msg.what == 1002 && ColorAppStartupManagerHelper.getInstance().canShowDialogs(this.mAms)) {
            String packageName = (String) msg.obj;
            if (packageName != null) {
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).handleAppStartForbidden(packageName);
            }
            if (OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getDialogTitleText() == null || OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getDialogContentText() == null || OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getDialogButtonText() == null) {
                Dialog d = new BaseErrorDialog(this.mAms.mUiContext);
                d.show();
                d.dismiss();
            } else {
                AlertDialog dialog = new AlertDialog.Builder(this.mContext, 201523207).create();
                dialog.getWindow().setType(2003);
                dialog.setCancelable(false);
                dialog.setTitle(OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getDialogTitleText());
                dialog.setMessage(OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getDialogContentText());
                dialog.setButton(-1, OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).getDialogButtonText(), msg.getTarget().obtainMessage(48, dialog));
                dialog.show();
            }
            OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).resetDialogShowText();
        }
        return false;
    }

    private boolean handleBgMessage(Message msg) {
        return false;
    }

    private boolean handleKillMessage(Message msg) {
        return false;
    }

    private static OppoBaseActivityManagerService typeCasting(ActivityManagerService ams) {
        if (ams != null) {
            return (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, ams);
        }
        return null;
    }
}

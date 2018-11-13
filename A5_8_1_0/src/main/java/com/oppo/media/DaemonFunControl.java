package com.oppo.media;

import android.content.Context;

public class DaemonFunControl {
    private static final String TAG = "OppoMultimediaService_FunControl";
    private final Context mContext;
    private String[] mFunControlName = new String[]{"MultimediaService", "setMode", "recordConflict", "streamAdjust", "setspeakerphoneOn", "killMediaserver", "streamMute", "killApp", "releaseAudiotrack"};
    private OppoDaemonListHelper mOppoDaemonListHelper;

    public DaemonFunControl(Context context, OppoDaemonListHelper listhelper) {
        this.mContext = context;
        this.mOppoDaemonListHelper = listhelper;
    }

    boolean getDaemonFunEnable(int index) {
        boolean ret = false;
        String mAttribute = "";
        if (index < 0 || index >= this.mFunControlName.length) {
            return false;
        }
        mAttribute = this.mOppoDaemonListHelper.getAttributeByAppName("control", this.mFunControlName[index]);
        DebugLog.d(TAG, "Attribute :" + mAttribute);
        if (mAttribute != null && mAttribute.equals("true")) {
            ret = true;
        }
        return ret;
    }
}

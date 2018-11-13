package com.suntek.rcs.ui.common;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import com.suntek.mway.rcs.client.aidl.constant.Parameter;
import com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn.ResultUtil;
import com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn.RichScrnShowing;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.richscreen.RichScreenApi;

public class RcsRichScreen {
    public static final int ACTIVE = 3;
    private static String ADDRESS_BOOK_HAVE_BEEN_UPDATED = "934 10 20000";
    private static String ADDRESS_BOOK_IS_BING_VIEWED_ENHANCED_OSD_SETTINGS = "933 10 12000";
    private static String ADDRESS_BOOK_IS_BING_VIEWED_VIEW_ENHANCED_SCREEN = "933 10 11000";
    public static final int CALL_WAITING = 5;
    private static String COMPLETED_RESTORE_FACTORY_SETTINGS = "934 90 30000";
    public static final int CONFERENCED = 11;
    public static final int CONNECTING = 13;
    private static final int DEFAULT_NUMBER_LENGTH = 11;
    public static final int DIALING = 6;
    public static final int DISCONNECTED = 10;
    public static final int DISCONNECTING = 9;
    private static String DISPLAY_RICH_SCREEN_EN = "Display rich screen ?";
    public static final int IDLE = 2;
    public static final int INCOMING = 4;
    private static String INCOMING_VOICE_CALL_THE_TERMINAL_STARTS_RINGING = "122 00 18000";
    private static String INITIATE_A_VOICE_CALL = "111 00 00000";
    public static final int INVALID = 0;
    public static final int NEW = 1;
    private static String NON_SPECIFIC_EVENTS = "000 00 00000";
    public static final int ONHOLD = 8;
    private static String OUTGOING_VIDEO_CALL = "211 00 00000";
    private static String RCS_GREETING_STRING_EN = "Gretting:";
    private static String RCS_MISSDNADDRESS_STRING_EN = "MissdnAddress:";
    private static final int RCS_STATIC_IMAGE = 0;
    private static final int RCS_VIDEO = 2;
    private static final int RCS_VIRTUAL_IMAGE = 1;
    public static final int REDIALING = 7;
    public static final int SELECT_PHONE_ACCOUNT = 12;
    private static String SIM_CARD_HAS_BEEN_REPLACED = "944 90 40000";
    private static String SWITCHED_VOICE_CALLS_CALLED_SIDE = "123 00 20000";
    private static String SWITCHED_VOICE_CALLS_CALLINGSIDE = "113 00 20000";
    private static String VIDEO_CALL_COMES_IN_THE_TERMINAL_STARTS_RINGING = "222 00 00000";
    private static String VIDEO_CALL_HANG_UP_CALLED_SIDE = "224 00 20000";
    private static String VIDEO_CALL_HANG_UP_CALLING_SIDE = "214 00 20000";
    private static String VIDEO_CALL_IS_CONNECTED_CALLED_SIDE_SELECT = "223 00 20000";
    private static String VIDEO_CALL_IS_CONNECTED_CALLING_SIDE = "213 00 20000";
    private static String VOICE_CALL_HANG_UP_CALLED_SIDE = "124 00 20000";
    private static String VOICE_CALL_HANG_UP_CALLING_SIDE = "114 00 20000";
    private boolean isGetRichScreenCompleted = false;
    private AudioManager mAudioManager;
    private Context mContext;
    private GifMovieView mGifMovieView;
    private TextView mGreeting;
    private String mNumber = null;
    private String mPhoneEevnt = null;
    private ImageView mRcsPhoto;
    MediaPlayer mediaPlayer;
    private TextView missdnAddress;
    private SurfaceView msurface = null;
    private SurfaceHolder surfaceholder;
    private String videoPath = null;

    public RcsRichScreen(Context context, ImageView rcsImageView, TextView Greeting, TextView rcsMissdnAddress, GifMovieView GifMovieView, SurfaceView surface) {
        this.mGifMovieView = GifMovieView;
        this.mGreeting = Greeting;
        this.missdnAddress = rcsMissdnAddress;
        this.mRcsPhoto = rcsImageView;
        this.msurface = surface;
        this.mContext = context;
        initSurfaceView();
    }

    private void initSurfaceView() {
        this.surfaceholder = this.msurface.getHolder();
        this.surfaceholder.setType(3);
        this.surfaceholder.addCallback(new Callback() {
            public void surfaceDestroyed(SurfaceHolder arg0) {
                if (RcsRichScreen.this.mediaPlayer != null) {
                    RcsRichScreen.this.mediaPlayer.release();
                }
            }

            public void surfaceCreated(SurfaceHolder arg0) {
                RcsRichScreen.this.play(RcsRichScreen.this.videoPath);
            }

            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            }
        });
    }

    private void play(String videoPath) {
        if (videoPath != null) {
            if (this.mediaPlayer != null) {
                this.mediaPlayer.release();
                this.mediaPlayer = null;
            }
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(3);
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
            this.mAudioManager.setStreamMute(3, true);
            this.mediaPlayer.setLooping(true);
            this.mediaPlayer.setDisplay(this.surfaceholder);
            try {
                this.mediaPlayer.setDataSource(videoPath);
                this.mediaPlayer.prepare();
                this.mediaPlayer.start();
            } catch (Exception e) {
                RcsLog.i("play video wrong");
            }
        }
    }

    private void reset() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.seekTo(0);
            this.mediaPlayer.start();
        }
    }

    private void stop() {
        if (this.mediaPlayer != null) {
            RcsLog.i("stop the video");
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    public void updateRichScreenInfo(String PhoneEevnt) {
        if (this.mNumber != null) {
            RichScrnShowing result = null;
            this.mPhoneEevnt = PhoneEevnt;
            if (this.mNumber != null) {
                try {
                    RcsLog.i("getRichScreenApi" + this.mNumber);
                    ResultUtil resultUtils = RichScreenApi.getInstance().getRichScrnObj(this.mNumber, PhoneEevnt);
                    if (resultUtils != null) {
                        result = (RichScrnShowing) resultUtils.getResultObj();
                    }
                    RcsLog.i(Parameter.EXTRA_RESULT + result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (ServiceDisconnectedException e2) {
                    RcsLog.w("ServiceDisconnectedException:" + e2);
                }
            }
            refreshUI(result);
            if (!(result == null || (TextUtils.isEmpty(result.getGreeting()) ^ 1) == 0)) {
                this.mGreeting.setVisibility(0);
                StringBuilder greetingString = new StringBuilder();
                greetingString.append(RCS_GREETING_STRING_EN);
                greetingString.append(result.getGreeting());
                this.mGreeting.setText(greetingString.toString());
            }
            if (result == null || (TextUtils.isEmpty(result.getMissdnAddress()) ^ 1) == 0) {
                this.missdnAddress.setVisibility(8);
                try {
                    RcsLog.i("getRichScreenApi.DownloadHomeLocRules" + RichScreenApi.getInstance());
                    RichScreenApi.getInstance().downloadHomeLocRules(this.mPhoneEevnt);
                } catch (Throwable e3) {
                    RcsLog.w(e3);
                }
            } else {
                this.missdnAddress.setVisibility(0);
                StringBuilder missdnAddressString = new StringBuilder();
                missdnAddressString.append(RCS_MISSDNADDRESS_STRING_EN);
                missdnAddressString.append(result.getMissdnAddress());
                this.missdnAddress.setText(missdnAddressString.toString());
            }
        }
    }

    private void refreshUI(RichScrnShowing result) {
        if (result == null) {
            RcsLog.i("refreshUI retult is null");
            setRcsFragmentVisibleDefault();
            return;
        }
        RcsLog.i("result.getGreeting()" + result.getGreeting());
        RcsLog.i("result.getSourceType()" + result.getSourceType());
        RcsLog.i("result.getSourceType()" + result.getLocalSourceUrl());
        this.mGreeting.setVisibility(8);
        this.mRcsPhoto.setVisibility(8);
        this.msurface.setVisibility(8);
        this.mGifMovieView.setVisibility(8);
        this.missdnAddress.setVisibility(8);
        switch (Integer.valueOf(result.getSourceType()).intValue()) {
            case 0:
                this.mRcsPhoto.setVisibility(0);
                this.mRcsPhoto.setImageBitmap(BitmapFactory.decodeFile(result.getLocalSourceUrl()));
                break;
            case 1:
                this.mGifMovieView.setVisibility(0);
                this.mGifMovieView.setMovieResource(result.getLocalSourceUrl());
                break;
            case 2:
                this.msurface.setVisibility(0);
                this.videoPath = result.getLocalSourceUrl();
                break;
            default:
                this.mRcsPhoto.setVisibility(0);
                break;
        }
    }

    public String getPhoneEventForRichScreen(int state, int videoState) {
        String phoneEevnt = INITIATE_A_VOICE_CALL;
        RcsLog.i("PhoneEevnt:" + phoneEevnt);
        switch (state) {
            case 3:
                if (!phoneEevnt.equals(INITIATE_A_VOICE_CALL)) {
                    if (phoneEevnt.equals(INCOMING_VOICE_CALL_THE_TERMINAL_STARTS_RINGING) || phoneEevnt.equals(VIDEO_CALL_COMES_IN_THE_TERMINAL_STARTS_RINGING)) {
                        if (!VideoProfile.isBidirectional(videoState)) {
                            phoneEevnt = SWITCHED_VOICE_CALLS_CALLED_SIDE;
                            break;
                        }
                        phoneEevnt = VIDEO_CALL_IS_CONNECTED_CALLED_SIDE_SELECT;
                        setRcsFragmentVisibleGone();
                        break;
                    }
                } else if (!VideoProfile.isBidirectional(videoState)) {
                    phoneEevnt = SWITCHED_VOICE_CALLS_CALLINGSIDE;
                    break;
                } else {
                    phoneEevnt = VIDEO_CALL_IS_CONNECTED_CALLING_SIDE;
                    break;
                }
            case 4:
            case 5:
                if (!VideoProfile.isBidirectional(videoState)) {
                    phoneEevnt = INCOMING_VOICE_CALL_THE_TERMINAL_STARTS_RINGING;
                    break;
                }
                phoneEevnt = VIDEO_CALL_COMES_IN_THE_TERMINAL_STARTS_RINGING;
                break;
            case 6:
            case 7:
            case 13:
                if (!VideoProfile.isBidirectional(videoState)) {
                    phoneEevnt = INITIATE_A_VOICE_CALL;
                    break;
                }
                phoneEevnt = OUTGOING_VIDEO_CALL;
                setRcsFragmentVisibleDefault();
                break;
            default:
                RcsLog.i("updateCallStateWidgets: unexpected call: " + state);
                break;
        }
        RcsLog.i("mPhoneEevnt:" + phoneEevnt);
        return phoneEevnt;
    }

    private void setRcsFragmentVisibleGone() {
        this.mGreeting.setVisibility(8);
        this.mRcsPhoto.setVisibility(8);
        this.msurface.setVisibility(8);
        this.mGifMovieView.setVisibility(8);
        this.missdnAddress.setVisibility(8);
    }

    private void setRcsFragmentVisibleDefault() {
        this.mGreeting.setVisibility(8);
        this.mRcsPhoto.setVisibility(0);
        this.msurface.setVisibility(8);
        this.mGifMovieView.setVisibility(8);
        this.missdnAddress.setVisibility(8);
    }

    private void createComfirmDialogInVideCall(final String phoneEvent) {
        new Builder(this.mContext).setMessage(DISPLAY_RICH_SCREEN_EN).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RcsRichScreen.this.updateRichScreenInfo(phoneEvent);
            }
        }).setNegativeButton(17039360, null).create().show();
    }

    public void updateRichScreenByCallState(int state, int videoState) {
        if (state == 10) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    try {
                        RcsLog.i("getRichScreenApi.downloadRichScreen:" + RcsRichScreen.this.mNumber);
                        RichScreenApi.getInstance().downloadRichScrnObj(RcsRichScreen.this.mNumber, RcsRichScreen.this.mPhoneEevnt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
            return;
        }
        String phoneEvent = getPhoneEventForRichScreen(state, videoState);
        if (phoneEvent.equals(VIDEO_CALL_COMES_IN_THE_TERMINAL_STARTS_RINGING)) {
            RcsLog.i("video call income do not set richscreen");
            this.isGetRichScreenCompleted = true;
            createComfirmDialogInVideCall(phoneEvent);
        }
        if (!this.isGetRichScreenCompleted) {
            updateRichScreenInfo(phoneEvent);
            this.isGetRichScreenCompleted = true;
        }
    }

    public void setNumber(String number) {
        this.mNumber = getFormatNumber(number);
    }

    public static String getFormatNumber(String number) {
        if (number == null) {
            return "";
        }
        number = number.replaceAll("-", "").replaceAll(" ", "").replaceAll(",", "");
        int numberLen = number.length();
        if (numberLen > 11) {
            number = number.substring(numberLen - 11, numberLen);
        }
        return number;
    }
}

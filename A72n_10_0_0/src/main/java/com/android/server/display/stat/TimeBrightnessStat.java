package com.android.server.display.stat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.EventLogTags;
import com.android.server.SystemService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.IColorEyeProtectManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.display.stat.BackLightStat;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.input.InputManagerService;
import java.util.ArrayList;

public class TimeBrightnessStat implements BackLightStat.Callback {
    private static final float ALL_AUTO_PERCENT = 0.95f;
    private static final float ALL_MANUAL_PERCENT = 0.05f;
    private static final float AUTO_PERCENT = 0.5f;
    private static final int BRIGHTNESS_BY_USER_TIMEOUT = 500;
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int DEFAULT_10_MINUTES = 600000;
    private static final int DEFAULT_15_MINUTES = 900000;
    private static final int DEFAULT_20_MINUTES = 1200000;
    private static final int DEFAULT_5_MINUTES = 300000;
    private static final int[] DEFAULT_DUR_LEV_MONITOR = {654, 852, 1063, 1285, 1480, OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS};
    private static final int[] DEFAULT_LEVELS_MONITOR = {6, 13, 18, 31, 59, 97, HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2, 182, InputManagerService.SW_JACK_BITS, 262, 282, IColorEyeProtectManager.LEVEL_COLOR_MATRIX_COLOR, 562, 758, 869, OppoBrightUtils.TEN_BITS_MAXBRIGHTNESS, 1100, 1225, 1354, 1427, 1536, OppoBrightUtils.ELEVEN_BITS_MAXBRIGHTNESS};
    private static final int[] DEFAULT_LUX_MONITOR = {0, 1, 2, 3, 8, 16, 36, 60, 100, 260, 540, 1000, 2250, 4600, 5900, 8600, 10000, 20000, 30000, EventLogTags.VOLUME_CHANGED, 60000, 80000};
    private static final int DEFAULT_MIN_BRIGHTNES = 10;
    private static final int DEFAUTL_MIN_MINUTES_1 = 60000;
    private static final int DEFAUTL_MIN_MINUTES_3 = 180000;
    private static final int EVENT_ID_HIGH_LEV_TIME_STAT = 781;
    private static final int EVENT_ID_LEVELS_TIME_STAT = 782;
    private static final int EVENT_ID_TIME_STAT = 780;
    private static final String KEY_AUTO_BY_USER_COUNT = "auto_byuser_count";
    private static final String KEY_AUTO_LOW_BTN_COUNT = "auto_low_btn_count";
    private static final String KEY_AUTO_MODE_TIME = "auto_mode_time";
    private static final String KEY_AUTO_TIME_PERCENT = "auto_time_percent";
    private static final String KEY_DUR_BAD_MAX_TIME = "dur_bad_max_time";
    private static final String KEY_DUR_REGION_300_400 = "dur_region_300_400_nit";
    private static final String KEY_DUR_REGION_300_LOW = "dur_region_300_nit_low";
    private static final String KEY_DUR_REGION_400_500 = "dur_region_400_500_nit";
    private static final String KEY_DUR_REGION_500_600 = "dur_region_500_600_nit";
    private static final String KEY_DUR_REGION_600_700 = "dur_region_600_700_nit";
    private static final String KEY_DUR_REGION_700_800 = "dur_region_700_800_nit";
    private static final String KEY_DUR_REGION_COUNT = "dur_region_count";
    private static final String KEY_HBM_TIME = "hbm_time";
    private static final String KEY_LEVELS_TIME = "levels_time";
    private static final String KEY_LUXS_TIME = "luxs_time";
    private static final String KEY_MANU_MODE_TIME = "manu_mode_time";
    private static final String KEY_MAX_BTN_TIME = "max_btn_time";
    private static final String KEY_SAVE_POWER_MODE_TIME = "save_power_mode_time";
    private static final String KEY_TOTAL_BY_USER_COUNT = "total_byuser_count";
    private static final String KEY_TOTAL_LOW_BTN_COUNT = "total_low_btn_count";
    private static final String KEY_USER_CLASS = "user_class";
    private static final int MSG_BRIGHTNESS_BY_USER = 3003;
    private static final int MSG_BRIGHTNESS_MAX = 3004;
    private static final int MSG_BRIGHTNESS_MODE_CHANGE = 3002;
    private static final int MSG_LEVEL_CHANGE = 3007;
    private static final int MSG_LUX_CHANGE = 3006;
    private static final int MSG_SAVE_POWER_MODE_CHANGE = 3005;
    private static final int MSG_SCREEN_OFF = 3000;
    private static final int MSG_SCREEN_ON = 3001;
    private static final String TAG = "TimeBrightnessStat";
    private static final String VALUE_USER_CLASS_ALL_AUTO = "all_auto";
    private static final String VALUE_USER_CLASS_ALL_MANUAL = "all_manual";
    private static final String VALUE_USER_CLASS_AUTO = "auto";
    private static final String VALUE_USER_CLASS_ERROR = "error";
    private static final String VALUE_USER_CLASS_MANUAL = "manual";
    private static volatile TimeBrightnessStat sTimeBrightnessStat;
    private int mAutoByUserCount = 0;
    private int mAutoLowBtnCount = 0;
    private long mAutoTotalTime = 0;
    private BackLightStat mBackLightStat;
    private long mBadDurMaxBtnTime = 0;
    private boolean mBootCompleted = false;
    private ArrayList<LevelStatInfo> mBtns = new ArrayList<>(20);
    private boolean mByUser = false;
    private Context mContext;
    private int mCurrLux = -1;
    private int mCurrMode = 1;
    private int mCurrTarget = -1;
    private ArrayList<Integer> mDurKeys = new ArrayList<>(20);
    private int mDurMaxLux = 8600;
    private long mDurTotalTime = 0;
    private ArrayList<DurStatInfo> mDurs = new ArrayList<>(20);
    private long mHBMtotalTime = 0;
    private TimeBackLightHandler mHandler;
    private long mLastModeChangeTime = 0;
    private int mLastTarget = -1;
    private ArrayList<Integer> mLevKeys = new ArrayList<>(20);
    private int mLowBtn = -1;
    private ArrayList<Integer> mLuxKeys = new ArrayList<>(20);
    private ArrayList<LevelStatInfo> mLuxs = new ArrayList<>(20);
    private long mManuTotalTime = 0;
    private int mMaxBtn = OppoBrightUtils.TEN_BITS_MAXBRIGHTNESS;
    private long mMaxBtnTime = 0;
    private int mPowerState = 2;
    private int mSavePowerMode = -1;
    private long mSavePowerTime = 0;
    private long mStartBadDurMaxTime = 0;
    private long mStartDurTime = 0;
    private long mStartHBMTime = 0;
    private long mStartMaxBtnTime = 0;
    private long mStartSavePowerTime = 0;
    private boolean mSupportLevStat = false;
    private int mTotalByUserCount = 0;
    private int mTotalLowBtnCount = 0;
    private int mUpdateTarget = 0;
    private String mVersion = null;

    private TimeBrightnessStat(Context context, BackLightStat stat) {
        this.mContext = context;
        this.mBackLightStat = stat;
    }

    public static TimeBrightnessStat getInstance(Context context, BackLightStat stat) {
        if (sTimeBrightnessStat == null) {
            synchronized (TimeBrightnessStat.class) {
                if (sTimeBrightnessStat == null) {
                    sTimeBrightnessStat = new TimeBrightnessStat(context, stat);
                }
            }
        }
        return sTimeBrightnessStat;
    }

    public void init(Handler handler) {
        this.mHandler = new TimeBackLightHandler(handler.getLooper());
        this.mCurrMode = Settings.System.getInt(this.mContext.getContentResolver(), "screen_brightness_mode", 1);
        this.mLastModeChangeTime = this.mBackLightStat.uptimeMillis();
        this.mLevKeys.clear();
        this.mLuxKeys.clear();
        this.mDurKeys.clear();
        this.mBtns.clear();
        this.mLuxs.clear();
        this.mDurs.clear();
        loadConfig();
        if (this.mLevKeys.size() == 0) {
            int i = 0;
            while (true) {
                int[] iArr = DEFAULT_LEVELS_MONITOR;
                if (i >= iArr.length) {
                    break;
                }
                this.mLevKeys.add(Integer.valueOf(iArr[i]));
                i++;
            }
        }
        if (this.mLuxKeys.size() == 0) {
            int i2 = 0;
            while (true) {
                int[] iArr2 = DEFAULT_LUX_MONITOR;
                if (i2 >= iArr2.length) {
                    break;
                }
                this.mLuxKeys.add(Integer.valueOf(iArr2[i2]));
                i2++;
            }
        }
        if (this.mDurKeys.size() == 0) {
            int i3 = 0;
            while (true) {
                int[] iArr3 = DEFAULT_DUR_LEV_MONITOR;
                if (i3 >= iArr3.length) {
                    break;
                }
                this.mDurKeys.add(Integer.valueOf(iArr3[i3]));
                i3++;
            }
        }
        for (int i4 = 0; i4 < this.mLevKeys.size(); i4++) {
            this.mBtns.add(new LevelStatInfo(this.mLevKeys.get(i4).intValue(), 0, 0, false));
        }
        for (int i5 = 0; i5 < this.mLuxKeys.size(); i5++) {
            this.mLuxs.add(new LevelStatInfo(this.mLuxKeys.get(i5).intValue(), 0, 0, false));
        }
        for (int i6 = 0; i6 < this.mDurKeys.size(); i6++) {
            String key = null;
            int key_int = this.mDurKeys.get(i6).intValue();
            if (i6 == 0) {
                key = KEY_DUR_REGION_300_LOW;
            } else if (i6 == 1) {
                key = KEY_DUR_REGION_300_400;
            } else if (i6 == 2) {
                key = KEY_DUR_REGION_400_500;
            } else if (i6 == 3) {
                key = KEY_DUR_REGION_500_600;
            } else if (i6 == 4) {
                key = KEY_DUR_REGION_600_700;
            } else if (i6 == 5) {
                key = KEY_DUR_REGION_700_800;
            }
            this.mDurs.add(new DurStatInfo(key, key_int, 0, 0, 0, 0));
        }
        this.mBootCompleted = true;
    }

    public void loadConfig() {
        this.mSupportLevStat = this.mBackLightStat.getBackLightStatSupport();
        ArrayList<Integer> levels = this.mBackLightStat.getBackLightStatAppLevels();
        if (levels != null) {
            for (int i = 0; i < levels.size(); i++) {
                this.mLevKeys.add(levels.get(i));
            }
        }
        ArrayList<Integer> luxs = this.mBackLightStat.getBackLightStatLuxLevels();
        if (luxs != null) {
            for (int i2 = 0; i2 < luxs.size(); i2++) {
                this.mLuxKeys.add(luxs.get(i2));
            }
        }
        ArrayList<Integer> durs = this.mBackLightStat.getBackLightStatDurLevels();
        if (durs != null) {
            for (int i3 = 0; i3 < durs.size(); i3++) {
                this.mDurKeys.add(durs.get(i3));
            }
        }
        this.mDurMaxLux = this.mBackLightStat.getBackLightStatMaxLux();
        this.mVersion = this.mBackLightStat.getVersion();
    }

    public void setMaxBtn(int maxBtn) {
        this.mMaxBtn = maxBtn;
    }

    public void setLowBtn(int lowBtn) {
        this.mLowBtn = lowBtn;
    }

    public void setCurrTarget(int state, int currTarget, boolean byUser) {
        int i;
        int i2;
        boolean changed = false;
        boolean lastByUser = this.mByUser;
        if (currTarget != this.mCurrTarget && !byUser) {
            this.mCurrTarget = currTarget;
        }
        this.mPowerState = state;
        this.mByUser = byUser;
        if ((lastByUser && !byUser) || !lastByUser) {
            changed = true;
        }
        if (lastByUser && !byUser && this.mSupportLevStat) {
            sendBrightnessByUser();
        }
        if (this.mSupportLevStat) {
            sendBrightnessMax();
        }
        boolean auto = true;
        if (this.mCurrMode != 1) {
            auto = false;
        }
        if (auto && this.mSupportLevStat && this.mBootCompleted && changed && (i = this.mUpdateTarget) != (i2 = this.mCurrTarget)) {
            this.mLastTarget = i;
            this.mUpdateTarget = i2;
            if (DEBUG) {
                Slog.d(TAG, "brightness changed " + this.mLastTarget + "->" + this.mUpdateTarget);
            }
            sendLevlChange();
        }
    }

    private boolean isValidBrightness(int value) {
        return value > 0;
    }

    private String getUserClass(float percent) {
        if (percent > ALL_AUTO_PERCENT) {
            return VALUE_USER_CLASS_ALL_AUTO;
        }
        if (percent >= 0.5f) {
            return "auto";
        }
        if (percent < 0.5f && percent >= ALL_MANUAL_PERCENT) {
            return VALUE_USER_CLASS_MANUAL;
        }
        if (percent >= OppoBrightUtils.MIN_LUX_LIMITI) {
            return VALUE_USER_CLASS_ALL_MANUAL;
        }
        return VALUE_USER_CLASS_ERROR;
    }

    private void uploadData(String reason) {
        long now = this.mBackLightStat.uptimeMillis();
        int i = this.mCurrMode;
        if (i == 1) {
            this.mAutoTotalTime += now - this.mLastModeChangeTime;
        } else if (i == 0) {
            this.mManuTotalTime += now - this.mLastModeChangeTime;
        }
        this.mLastModeChangeTime = now;
        long j = this.mStartMaxBtnTime;
        if (j != 0 && this.mCurrTarget == this.mMaxBtn) {
            this.mMaxBtnTime += now - j;
            this.mStartMaxBtnTime = now;
        }
        long j2 = this.mStartHBMTime;
        if (j2 != 0 && this.mCurrTarget > this.mMaxBtn) {
            this.mHBMtotalTime += now - j2;
            this.mStartHBMTime = now;
        }
        long j3 = this.mStartSavePowerTime;
        if (j3 != 0 && this.mSavePowerMode == 1) {
            this.mSavePowerTime += now - j3;
            this.mStartSavePowerTime = now;
        }
        String time = this.mBackLightStat.getCurrSimpleFormatTime();
        long j4 = this.mAutoTotalTime;
        long totalTime = this.mManuTotalTime + j4;
        float percent = totalTime > 0 ? ((float) j4) / ((float) totalTime) : OppoBrightUtils.MIN_LUX_LIMITI;
        StringBuilder sb = new StringBuilder((int) SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
        sb.append(KEY_AUTO_MODE_TIME);
        sb.append(",");
        sb.append(this.mAutoTotalTime);
        sb.append(",");
        sb.append(KEY_MANU_MODE_TIME);
        sb.append(",");
        sb.append(this.mManuTotalTime);
        sb.append(",");
        sb.append(KEY_AUTO_TIME_PERCENT);
        sb.append(",");
        sb.append(percent);
        sb.append(",");
        sb.append(KEY_USER_CLASS);
        sb.append(",");
        sb.append(getUserClass(percent));
        sb.append(",");
        sb.append(KEY_MAX_BTN_TIME);
        sb.append(",");
        sb.append(this.mMaxBtnTime);
        sb.append(",");
        sb.append(KEY_HBM_TIME);
        sb.append(",");
        sb.append(this.mHBMtotalTime);
        sb.append(",");
        sb.append(KEY_SAVE_POWER_MODE_TIME);
        sb.append(",");
        sb.append(this.mSavePowerTime);
        sb.append(",");
        sb.append(KEY_TOTAL_BY_USER_COUNT);
        sb.append(",");
        sb.append(this.mTotalByUserCount);
        sb.append(",");
        sb.append(KEY_AUTO_BY_USER_COUNT);
        sb.append(",");
        sb.append(this.mAutoByUserCount);
        sb.append(",");
        sb.append(KEY_TOTAL_LOW_BTN_COUNT);
        sb.append(",");
        sb.append(this.mTotalLowBtnCount);
        sb.append(",");
        sb.append(KEY_AUTO_LOW_BTN_COUNT);
        sb.append(",");
        sb.append(this.mAutoLowBtnCount);
        sb.append(",");
        String manu = this.mBackLightStat.getLcdManufacture();
        sb.append(BackLightStat.KEY_LCD_MANU);
        sb.append(",");
        sb.append(manu);
        sb.append(",");
        sb.append(BackLightStat.KEY_VERSION);
        sb.append(",");
        sb.append(this.mVersion);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_TIME);
        sb.append(",");
        sb.append(time);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_REASON);
        sb.append(",");
        sb.append(reason);
        String uploadData = sb.toString();
        if (totalTime >= 180000) {
            this.mBackLightStat.reportBackLightInfor(EVENT_ID_TIME_STAT, uploadData);
        }
        if (DEBUG) {
            Slog.d(TAG, "uploadData size=" + uploadData.length() + StringUtils.SPACE + uploadData);
        }
        this.mAutoTotalTime = 0;
        this.mManuTotalTime = 0;
        this.mMaxBtnTime = 0;
        this.mHBMtotalTime = 0;
        this.mSavePowerTime = 0;
        this.mTotalByUserCount = 0;
        this.mAutoByUserCount = 0;
        this.mTotalLowBtnCount = 0;
        this.mAutoLowBtnCount = 0;
    }

    private void uploadData2(String reason) {
        long now = this.mBackLightStat.uptimeMillis();
        StringBuilder sb = new StringBuilder((int) SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
        boolean auto = true;
        if (this.mCurrMode != 1) {
            auto = false;
        }
        if (auto) {
            long j = this.mStartBadDurMaxTime;
            if (j != 0 && this.mCurrTarget == this.mMaxBtn && this.mCurrLux < this.mDurMaxLux) {
                this.mBadDurMaxBtnTime += now - j;
                this.mStartBadDurMaxTime = now;
            }
        }
        sb.append(KEY_DUR_BAD_MAX_TIME);
        sb.append(",");
        sb.append(this.mBadDurMaxBtnTime);
        sb.append(",");
        updateDurBtnTime();
        int count = 0;
        for (int i = 0; i < this.mDurs.size(); i++) {
            DurStatInfo info = this.mDurs.get(i);
            sb.append(info.key);
            sb.append(",");
            sb.append(info.durCount_5);
            sb.append(StringUtils.SPACE);
            sb.append(info.durCount_10);
            sb.append(StringUtils.SPACE);
            sb.append(info.durCount_15);
            sb.append(StringUtils.SPACE);
            sb.append(info.durCount_20);
            sb.append(",");
            count += info.durCount_5 + info.durCount_10 + info.durCount_15 + info.durCount_20;
        }
        String time = this.mBackLightStat.getCurrSimpleFormatTime();
        String manu = this.mBackLightStat.getLcdManufacture();
        sb.append(BackLightStat.KEY_LCD_MANU);
        sb.append(",");
        sb.append(manu);
        sb.append(",");
        sb.append(BackLightStat.KEY_VERSION);
        sb.append(",");
        sb.append(this.mVersion);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_TIME);
        sb.append(",");
        sb.append(time);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_REASON);
        sb.append(",");
        sb.append(reason);
        String uploadData = sb.toString();
        if (this.mBadDurMaxBtnTime > 0 && count > 0) {
            this.mBackLightStat.reportBackLightInfor(EVENT_ID_HIGH_LEV_TIME_STAT, uploadData);
        }
        if (DEBUG) {
            Slog.d(TAG, "uploadData size=" + uploadData.length() + " count=" + count + StringUtils.SPACE + uploadData);
        }
        this.mBadDurMaxBtnTime = 0;
        for (int i2 = 0; i2 < this.mDurs.size(); i2++) {
            DurStatInfo info2 = this.mDurs.get(i2);
            info2.totalTime = 0;
            info2.durCount_5 = 0;
            info2.durCount_10 = 0;
            info2.durCount_15 = 0;
            info2.durCount_20 = 0;
        }
    }

    private void uploadDataAutoLev(String reason) {
        long now = this.mBackLightStat.uptimeMillis();
        StringBuilder sb = new StringBuilder(1100);
        boolean z = true;
        if (this.mCurrMode != 1) {
            z = false;
        }
        updateAutoBtnTime();
        sb.append(KEY_LEVELS_TIME);
        sb.append(",");
        sb.append("levels_time_start");
        sb.append(",");
        long btnTotalTime = 0;
        for (int i = 0; i < this.mBtns.size(); i++) {
            LevelStatInfo info = this.mBtns.get(i);
            sb.append("key_" + info.key);
            sb.append(",");
            sb.append(info.totalTime);
            sb.append(",");
            btnTotalTime += info.totalTime;
        }
        sb.append(KEY_LEVELS_TIME);
        sb.append(",");
        sb.append("levels_time_end");
        sb.append(",");
        handleLuxChange();
        sb.append(KEY_LUXS_TIME);
        sb.append(",");
        sb.append("luxs_time_start");
        sb.append(",");
        long luxTotalTime = 0;
        for (int i2 = 0; i2 < this.mLuxs.size(); i2++) {
            LevelStatInfo info2 = this.mLuxs.get(i2);
            sb.append("key_" + info2.key);
            sb.append(",");
            sb.append(info2.totalTime);
            sb.append(",");
            luxTotalTime += info2.totalTime;
        }
        sb.append(KEY_LUXS_TIME);
        sb.append(",");
        sb.append("luxs_time_end");
        sb.append(",");
        String time = this.mBackLightStat.getCurrSimpleFormatTime();
        String manu = this.mBackLightStat.getLcdManufacture();
        sb.append(BackLightStat.KEY_LCD_MANU);
        sb.append(",");
        sb.append(manu);
        sb.append(",");
        sb.append(BackLightStat.KEY_VERSION);
        sb.append(",");
        sb.append(this.mVersion);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_TIME);
        sb.append(",");
        sb.append(time);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_REASON);
        sb.append(",");
        sb.append(reason);
        String uploadData = sb.toString();
        if (Math.abs(luxTotalTime - btnTotalTime) <= 180000 && luxTotalTime >= 60000) {
            this.mBackLightStat.reportBackLightInfor(EVENT_ID_LEVELS_TIME_STAT, uploadData);
        }
        if (DEBUG) {
            Slog.d(TAG, "uploadData size=" + uploadData.length() + " deta=" + Math.abs(luxTotalTime - btnTotalTime) + " levTime=" + btnTotalTime + " luxTime=" + luxTotalTime + StringUtils.SPACE + uploadData);
        }
        int i3 = 0;
        while (i3 < this.mBtns.size()) {
            this.mBtns.get(i3).totalTime = 0;
            i3++;
            now = now;
        }
        for (int i4 = 0; i4 < this.mLuxs.size(); i4++) {
            this.mLuxs.get(i4).totalTime = 0;
        }
    }

    private void uploadDataAutoLux(String reason) {
        this.mBackLightStat.uptimeMillis();
        StringBuilder sb = new StringBuilder(300);
        boolean z = true;
        if (this.mCurrMode != 1) {
            z = false;
        }
        String time = this.mBackLightStat.getCurrSimpleFormatTime();
        String manu = this.mBackLightStat.getLcdManufacture();
        sb.append(BackLightStat.KEY_LCD_MANU);
        sb.append(",");
        sb.append(manu);
        sb.append(",");
        sb.append(BackLightStat.KEY_VERSION);
        sb.append(",");
        sb.append(this.mVersion);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_TIME);
        sb.append(",");
        sb.append(time);
        sb.append(",");
        sb.append(BackLightStat.KEY_UPLOAD_REASON);
        sb.append(",");
        sb.append(reason);
        String uploadData = sb.toString();
        if (DEBUG) {
            Slog.d(TAG, "uploadData size=" + uploadData.length() + StringUtils.SPACE + uploadData);
        }
    }

    @Override // com.android.server.display.stat.BackLightStat.Callback
    public void onReceive(String action, Object... values) {
        if (!TextUtils.isEmpty(action) && this.mSupportLevStat) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_SHUT_DOWN);
                uploadData2(BackLightStat.VALUE_UPLOAD_SHUT_DOWN);
                uploadDataAutoLev(BackLightStat.VALUE_UPLOAD_SHUT_DOWN);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                sendScreenON();
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                sendScreenOff();
            } else if (BackLightStat.ACTION_ON_ALARM.equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_ON_ALARM);
                uploadData2(BackLightStat.VALUE_UPLOAD_ON_ALARM);
                uploadDataAutoLev(BackLightStat.VALUE_UPLOAD_ON_ALARM);
            } else if ("android.intent.action.REBOOT".equals(action)) {
                uploadData(BackLightStat.VALUE_UPLOAD_REBOOT);
                uploadData2(BackLightStat.VALUE_UPLOAD_REBOOT);
                uploadDataAutoLev(BackLightStat.VALUE_UPLOAD_REBOOT);
            } else {
                boolean auto = false;
                if (BackLightStat.ACTION_BRIGHTNESS_MODE.equals(action)) {
                    if (values != null) {
                        try {
                            if (values[0] instanceof Integer) {
                                this.mCurrMode = ((Integer) values[0]).intValue();
                                sendBrightnessModeChange();
                                sendLevlChange();
                                sendLuxChange();
                            }
                        } catch (Exception e) {
                            Slog.e(TAG, "action:" + action + e.toString());
                        }
                    }
                } else if (BackLightStat.ACTION_SAVE_POWER_MODE.equals(action)) {
                    if (values != null) {
                        try {
                            if (values[0] instanceof Integer) {
                                this.mSavePowerMode = ((Integer) values[0]).intValue();
                                sendSavePowerModeChange();
                            }
                        } catch (Exception e2) {
                            Slog.e(TAG, "action:" + action + e2.toString());
                        }
                    }
                } else if (BackLightStat.ACTION_LUX_CHANGE.equals(action) && values != null) {
                    try {
                        if (values[0] instanceof Integer) {
                            this.mCurrLux = ((Integer) values[0]).intValue();
                            if (this.mCurrMode == 1) {
                                auto = true;
                            }
                            if (auto) {
                                sendLuxChange();
                            }
                        }
                    } catch (Exception e3) {
                        Slog.e(TAG, "action:" + action + e3.toString());
                    }
                }
            }
        }
    }

    private void sendBrightnessByUser() {
        TimeBackLightHandler timeBackLightHandler = this.mHandler;
        if (timeBackLightHandler != null) {
            timeBackLightHandler.removeMessages(MSG_BRIGHTNESS_BY_USER);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_BRIGHTNESS_BY_USER), 500);
        }
    }

    private void sendBrightnessMax() {
        TimeBackLightHandler timeBackLightHandler = this.mHandler;
        if (timeBackLightHandler != null) {
            timeBackLightHandler.removeMessages(MSG_BRIGHTNESS_MAX);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_BRIGHTNESS_MAX));
        }
    }

    private void sendScreenOff() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SCREEN_OFF));
    }

    private void sendScreenON() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SCREEN_ON));
    }

    private void sendBrightnessModeChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_BRIGHTNESS_MODE_CHANGE));
    }

    private void sendSavePowerModeChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_SAVE_POWER_MODE_CHANGE));
    }

    private void sendLuxChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_LUX_CHANGE));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLuxChange() {
        long now = this.mBackLightStat.uptimeMillis();
        int i = 0;
        boolean auto = this.mCurrMode == 1;
        if (this.mCurrTarget == this.mMaxBtn) {
            if (auto && this.mStartBadDurMaxTime == 0 && this.mCurrLux < this.mDurMaxLux) {
                this.mStartBadDurMaxTime = now;
            }
        } else if (auto) {
            long j = this.mStartBadDurMaxTime;
            if (j != 0) {
                this.mBadDurMaxBtnTime += now - j;
                this.mStartBadDurMaxTime = 0;
            }
        }
        LevelStatInfo currInfo = null;
        LevelStatInfo lastInfo = null;
        int size = this.mLuxs.size();
        int i2 = 0;
        while (i2 < size) {
            LevelStatInfo info = this.mLuxs.get(i2);
            LevelStatInfo fistInfo = this.mLuxs.get(i);
            LevelStatInfo endInfo = this.mLuxs.get(size - 1);
            int i3 = info.key;
            if (info.inRegion) {
                lastInfo = info;
            }
            if (fistInfo.key >= this.mCurrLux) {
                currInfo = fistInfo;
            } else {
                if (i2 < size - 1) {
                    int min = this.mLuxs.get(i2).key;
                    int max = this.mLuxs.get(i2 + 1).key;
                    int i4 = this.mCurrLux;
                    if (i4 > min && i4 <= max) {
                        currInfo = this.mLuxs.get(i2 + 1);
                    }
                }
                int max2 = this.mCurrLux;
                ArrayList<Integer> arrayList = this.mLuxKeys;
                if (max2 >= arrayList.get(arrayList.size() - 1).intValue()) {
                    currInfo = endInfo;
                }
            }
            i2++;
            i = 0;
        }
        if (this.mCurrMode == 1) {
            if (lastInfo != null && currInfo != null) {
                lastInfo.inRegion = false;
                if (lastInfo.startTime != 0) {
                    lastInfo.totalTime += now - lastInfo.startTime;
                }
                lastInfo.startTime = 0;
                currInfo.inRegion = true;
                currInfo.startTime = now;
            } else if (currInfo != null) {
                currInfo.inRegion = true;
                currInfo.startTime = now;
            }
        } else if (lastInfo != null && currInfo != null) {
            if (lastInfo.startTime != 0) {
                lastInfo.totalTime += now - lastInfo.startTime;
            }
            lastInfo.inRegion = false;
            lastInfo.startTime = 0;
            currInfo.inRegion = false;
            currInfo.startTime = 0;
        }
    }

    private void sendLevlChange() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_LEVEL_CHANGE));
    }

    private void updateDurBtnTime() {
        long now = this.mBackLightStat.uptimeMillis();
        DurStatInfo currInfo = null;
        DurStatInfo lastInfo = null;
        int size = this.mDurs.size();
        for (int i = 0; i < size; i++) {
            DurStatInfo info = this.mDurs.get(i);
            int i2 = info.key_int;
            int first = this.mDurs.get(0).key_int;
            DurStatInfo endInfo = this.mDurs.get(size - 1);
            if (info.inRegion) {
                lastInfo = info;
            }
            if (i < size - 1) {
                int min = this.mDurs.get(i).key_int;
                int max = this.mDurs.get(i + 1).key_int;
                int i3 = this.mUpdateTarget;
                if (i3 >= min && i3 < max) {
                    currInfo = this.mDurs.get(i + 1);
                }
            }
            int i4 = this.mUpdateTarget;
            ArrayList<Integer> arrayList = this.mDurKeys;
            if (i4 == arrayList.get(arrayList.size() - 1).intValue()) {
                currInfo = endInfo;
            }
            if (this.mUpdateTarget < first) {
                currInfo = this.mDurs.get(0);
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "mUpdateTarget=" + this.mUpdateTarget + " lastInfo:" + lastInfo + "  currInfo:" + currInfo);
        }
        if (this.mCurrMode == 1) {
            if (lastInfo != null && currInfo != null) {
                lastInfo.inRegion = false;
                if (lastInfo.startTime != 0) {
                    lastInfo.totalTime += now - lastInfo.startTime;
                }
                lastInfo.startTime = 0;
                long totalTime = lastInfo.totalTime;
                if (totalTime >= 1200000) {
                    lastInfo.durCount_20++;
                } else if (totalTime >= 900000) {
                    lastInfo.durCount_15++;
                } else if (totalTime >= 600000) {
                    lastInfo.durCount_10++;
                } else if (totalTime >= BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
                    lastInfo.durCount_5++;
                }
                currInfo.inRegion = true;
                currInfo.totalTime = 0;
                currInfo.startTime = now;
            } else if (currInfo != null) {
                currInfo.inRegion = true;
                currInfo.totalTime = 0;
                currInfo.startTime = now;
            }
        } else if (lastInfo != null && currInfo != null) {
            if (lastInfo.startTime != 0) {
                lastInfo.totalTime += now - lastInfo.startTime;
            }
            long totalTime2 = lastInfo.totalTime;
            if (totalTime2 >= 1200000) {
                lastInfo.durCount_20++;
            } else if (totalTime2 >= 900000) {
                lastInfo.durCount_15++;
            } else if (totalTime2 >= 600000) {
                lastInfo.durCount_10++;
            } else if (totalTime2 >= BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
                lastInfo.durCount_5++;
            }
            lastInfo.inRegion = false;
            lastInfo.startTime = 0;
            lastInfo.totalTime = 0;
            currInfo.inRegion = false;
            currInfo.startTime = 0;
            currInfo.totalTime = 0;
        }
    }

    private void updateAutoBtnTime() {
        long now = this.mBackLightStat.uptimeMillis();
        LevelStatInfo currInfo = null;
        LevelStatInfo lastInfo = null;
        int size = this.mBtns.size();
        for (int i = 0; i < size; i++) {
            LevelStatInfo info = this.mBtns.get(i);
            LevelStatInfo fistInfo = this.mBtns.get(0);
            LevelStatInfo endInfo = this.mBtns.get(size - 1);
            int i2 = info.key;
            if (info.inRegion) {
                lastInfo = info;
            }
            if (fistInfo.key >= this.mUpdateTarget) {
                currInfo = fistInfo;
            } else {
                if (i < size - 1) {
                    int min = this.mBtns.get(i).key;
                    int max = this.mBtns.get(i + 1).key;
                    int i3 = this.mUpdateTarget;
                    if (i3 > min && i3 <= max) {
                        currInfo = this.mBtns.get(i + 1);
                    }
                }
                int min2 = this.mUpdateTarget;
                ArrayList<Integer> arrayList = this.mLevKeys;
                if (min2 == arrayList.get(arrayList.size() - 1).intValue()) {
                    currInfo = endInfo;
                }
            }
        }
        if (this.mCurrMode == 1) {
            if (lastInfo != null && currInfo != null) {
                lastInfo.inRegion = false;
                if (lastInfo.startTime != 0) {
                    lastInfo.totalTime += now - lastInfo.startTime;
                }
                lastInfo.startTime = 0;
                currInfo.inRegion = true;
                currInfo.startTime = now;
            } else if (currInfo != null) {
                currInfo.inRegion = true;
                currInfo.startTime = now;
            }
        } else if (lastInfo != null && currInfo != null) {
            if (lastInfo.startTime != 0) {
                lastInfo.totalTime += now - lastInfo.startTime;
            }
            lastInfo.inRegion = false;
            lastInfo.startTime = 0;
            currInfo.inRegion = false;
            currInfo.startTime = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLevlChange() {
        updateDurBtnTime();
        updateAutoBtnTime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBtnByUser() {
        this.mTotalByUserCount++;
        if (this.mCurrMode == 1) {
            this.mAutoByUserCount++;
        }
        if (isValidBrightness(this.mLowBtn) && this.mCurrTarget < this.mLowBtn) {
            this.mTotalLowBtnCount++;
            if (this.mCurrMode == 1) {
                this.mAutoLowBtnCount++;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBtnMax() {
        long now = this.mBackLightStat.uptimeMillis();
        if (this.mCurrTarget == this.mMaxBtn) {
            if (this.mStartMaxBtnTime == 0) {
                this.mStartMaxBtnTime = now;
            }
            long j = this.mStartHBMTime;
            if (j != 0) {
                this.mHBMtotalTime += now - j;
                this.mStartHBMTime = 0;
            }
            if (this.mCurrMode == 1 && this.mStartBadDurMaxTime == 0 && this.mCurrLux < this.mDurMaxLux) {
                this.mStartBadDurMaxTime = now;
            }
        } else if (this.mCurrMode == 1) {
            long j2 = this.mStartBadDurMaxTime;
            if (j2 != 0) {
                this.mBadDurMaxBtnTime += now - j2;
                this.mStartBadDurMaxTime = 0;
            }
        }
        if (this.mCurrTarget > this.mMaxBtn) {
            if (this.mStartHBMTime == 0) {
                this.mStartHBMTime = now;
            }
            long j3 = this.mStartMaxBtnTime;
            if (j3 != 0) {
                this.mMaxBtnTime += now - j3;
                this.mStartMaxBtnTime = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSavePowerModeChange() {
        long now = this.mBackLightStat.uptimeMillis();
        if (this.mStartSavePowerTime == 0 && this.mSavePowerMode == 1) {
            this.mStartSavePowerTime = now;
            return;
        }
        long j = this.mStartSavePowerTime;
        if (j != 0 && this.mSavePowerMode == 0) {
            this.mSavePowerTime += now - j;
            this.mStartSavePowerTime = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenON() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBtnModeChange() {
        long now = this.mBackLightStat.uptimeMillis();
        int i = this.mCurrMode;
        if (i == 1) {
            this.mManuTotalTime += now - this.mLastModeChangeTime;
            if (this.mCurrTarget == this.mMaxBtn && this.mStartBadDurMaxTime == 0 && this.mCurrLux < this.mDurMaxLux) {
                this.mStartBadDurMaxTime = now;
            }
        } else if (i == 0) {
            this.mAutoTotalTime += now - this.mLastModeChangeTime;
            long j = this.mStartBadDurMaxTime;
            if (j != 0) {
                this.mBadDurMaxBtnTime += now - j;
                this.mStartBadDurMaxTime = 0;
            }
        }
        this.mLastModeChangeTime = now;
        if (DEBUG) {
            Slog.d(TAG, "currMode=" + this.mCurrMode);
        }
    }

    /* access modifiers changed from: private */
    public final class TimeBackLightHandler extends Handler {
        public TimeBackLightHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TimeBrightnessStat.MSG_SCREEN_OFF /* 3000 */:
                    TimeBrightnessStat.this.handleScreenOff();
                    return;
                case TimeBrightnessStat.MSG_SCREEN_ON /* 3001 */:
                    TimeBrightnessStat.this.handleScreenON();
                    return;
                case TimeBrightnessStat.MSG_BRIGHTNESS_MODE_CHANGE /* 3002 */:
                    TimeBrightnessStat.this.handleBtnModeChange();
                    return;
                case TimeBrightnessStat.MSG_BRIGHTNESS_BY_USER /* 3003 */:
                    TimeBrightnessStat.this.handleBtnByUser();
                    return;
                case TimeBrightnessStat.MSG_BRIGHTNESS_MAX /* 3004 */:
                    TimeBrightnessStat.this.handleBtnMax();
                    return;
                case TimeBrightnessStat.MSG_SAVE_POWER_MODE_CHANGE /* 3005 */:
                    TimeBrightnessStat.this.handleSavePowerModeChange();
                    return;
                case TimeBrightnessStat.MSG_LUX_CHANGE /* 3006 */:
                    TimeBrightnessStat.this.handleLuxChange();
                    return;
                case TimeBrightnessStat.MSG_LEVEL_CHANGE /* 3007 */:
                    TimeBrightnessStat.this.handleLevlChange();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class LevelStatInfo {
        boolean inRegion = false;
        int key = 0;
        long startTime = 0;
        long totalTime = 0;

        public LevelStatInfo(int key2, long totalTime2, long startTime2, boolean inRegion2) {
            this.key = key2;
            this.totalTime = totalTime2;
            this.startTime = startTime2;
            this.inRegion = inRegion2;
        }

        public String toString() {
            return "LevelStatInfo{key=" + this.key + ", inRegion=" + this.inRegion + ", totalTime=" + this.totalTime + ", startTime=" + this.startTime + "}";
        }
    }

    /* access modifiers changed from: private */
    public final class DurStatInfo {
        int currTarget = 0;
        int durCount_10 = 0;
        int durCount_15 = 0;
        int durCount_20 = 0;
        int durCount_5 = 0;
        boolean inRegion = false;
        String key = null;
        int key_int = 0;
        int lastTarget = 0;
        long startTime = 0;
        long totalTime = 0;

        public DurStatInfo(String key2, int key_int2, long totalTime2, long startTime2, int lastTarget2, int currTarget2) {
            this.key = key2;
            this.key_int = key_int2;
            this.totalTime = totalTime2;
            this.startTime = startTime2;
            this.lastTarget = lastTarget2;
            this.currTarget = currTarget2;
        }

        public String toString() {
            return "DurStatInfo{key=" + this.key + ", key_int=" + this.key_int + ", inRegion=" + this.inRegion + ", totalTime=" + this.totalTime + ", startTime=" + this.startTime + ", durCount_5=" + this.durCount_5 + ", durCount_10=" + this.durCount_10 + ", durCount_15=" + this.durCount_15 + ", durCount_20=" + this.durCount_20 + "}";
        }
    }
}

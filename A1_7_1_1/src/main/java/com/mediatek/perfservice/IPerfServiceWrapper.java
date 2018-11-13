package com.mediatek.perfservice;

public interface IPerfServiceWrapper {
    public static final int CMD_GET_CLUSTER_CPU_FREQ_MAX = 11;
    public static final int CMD_GET_CLUSTER_CPU_FREQ_MIN = 10;
    public static final int CMD_GET_CLUSTER_CPU_NUM = 9;
    public static final int CMD_GET_CLUSTER_NUM = 8;
    public static final int CMD_GET_CPU_FREQ_BIG_LEVEL_COUNT = 2;
    public static final int CMD_GET_CPU_FREQ_LEVEL_COUNT = 0;
    public static final int CMD_GET_CPU_FREQ_LITTLE_LEVEL_COUNT = 1;
    public static final int CMD_GET_FOREGROUND_TYPE = 12;
    public static final int CMD_GET_GPU_FREQ_LEVEL_COUNT = 3;
    public static final int CMD_GET_MEM_FREQ_LEVEL_COUNT = 4;
    public static final int CMD_GET_PACK_BOOST_MODE = 1;
    public static final int CMD_GET_PACK_BOOST_TIMEOUT = 2;
    public static final int CMD_GET_PACK_IN_WHITE_LIST = 0;
    public static final int CMD_GET_PERF_INDEX_MAX = 6;
    public static final int CMD_GET_PERF_INDEX_MIN = 5;
    public static final int CMD_GET_PERF_NORMALIZED_INDEX_MAX = 7;
    public static final int CMD_SET_BG_BOOST_VALUE = 63;
    public static final int CMD_SET_CLUSTER_CPU_CORE_MAX = 16;
    public static final int CMD_SET_CLUSTER_CPU_CORE_MIN = 15;
    public static final int CMD_SET_CLUSTER_CPU_FREQ_MAX = 18;
    public static final int CMD_SET_CLUSTER_CPU_FREQ_MIN = 17;
    public static final int CMD_SET_CPUFREQ_ABOVE_HISPEED_DELAY = 14;
    public static final int CMD_SET_CPUFREQ_HISPEED_FREQ = 12;
    public static final int CMD_SET_CPUFREQ_MIN_SAMPLE_TIME = 13;
    public static final int CMD_SET_CPU_CORE_BIG_LITTLE_MAX = 3;
    public static final int CMD_SET_CPU_CORE_BIG_LITTLE_MIN = 2;
    public static final int CMD_SET_CPU_CORE_MAX = 1;
    public static final int CMD_SET_CPU_CORE_MIN = 0;
    public static final int CMD_SET_CPU_DOWN_THRESHOLD = 21;
    public static final int CMD_SET_CPU_FREQ_BIG_LITTLE_MAX = 7;
    public static final int CMD_SET_CPU_FREQ_BIG_LITTLE_MIN = 6;
    public static final int CMD_SET_CPU_FREQ_MAX = 5;
    public static final int CMD_SET_CPU_FREQ_MIN = 4;
    public static final int CMD_SET_CPU_UP_THRESHOLD = 20;
    public static final int CMD_SET_DCS_MODE = 53;
    public static final int CMD_SET_FG_BOOST_VALUE = 50;
    public static final int CMD_SET_GLOBAL_CPUSET = 61;
    public static final int CMD_SET_GPU_FREQ_MAX = 9;
    public static final int CMD_SET_GPU_FREQ_MIN = 8;
    public static final int CMD_SET_HEAVY_TASK_ENABLED = 26;
    public static final int CMD_SET_IBOOST_DOWN_THRESHOLD = 29;
    public static final int CMD_SET_IBOOST_UP_THRESHOLD = 28;
    public static final int CMD_SET_NORMALIZED_PERF_INDEX = 23;
    public static final int CMD_SET_PERF_INDEX = 22;
    public static final int CMD_SET_PPM_MODE = 24;
    public static final int CMD_SET_ROOT_CLUSTER = 19;
    public static final int CMD_SET_RUSH_BOOST_ENABLED = 25;
    public static final int CMD_SET_SCN_VALID = 27;
    public static final int CMD_SET_SCREEN_OFF_STATE = 11;
    public static final int CMD_SET_TA_BOOST_VALUE = 62;
    public static final int CMD_SET_VCORE = 10;
    public static final int CMD_SET_WIPHY_CAM = 30;
    public static final int DISPLAY_TYPE_GAME = 0;
    public static final int DISPLAY_TYPE_NO_TOUCH_BOOST = 2;
    public static final int DISPLAY_TYPE_OTHERS = 1;
    public static final int NOTIFY_USER_TYPE_CORE_ONLINE = 5;
    public static final int NOTIFY_USER_TYPE_DETECT = 8;
    public static final int NOTIFY_USER_TYPE_DISPLAY_TYPE = 2;
    public static final int NOTIFY_USER_TYPE_FRAME_UPDATE = 1;
    public static final int NOTIFY_USER_TYPE_OTHERS = 7;
    public static final int NOTIFY_USER_TYPE_PERF_MODE = 6;
    public static final int NOTIFY_USER_TYPE_PID = 0;
    public static final int NOTIFY_USER_TYPE_SCENARIO_OFF = 4;
    public static final int NOTIFY_USER_TYPE_SCENARIO_ON = 3;
    public static final int PERF_MODE_NORMAL = 0;
    public static final int PERF_MODE_SPORTS = 1;
    public static final int SCN_APP_LAUNCH = 4;
    public static final int SCN_APP_ROTATE = 5;
    public static final int SCN_APP_SWITCH = 1;
    public static final int SCN_APP_TOUCH = 6;
    public static final int SCN_GALLERY_BOOST = 12;
    public static final int SCN_GAME_LAUNCH = 3;
    public static final int SCN_GAMING = 8;
    public static final int SCN_GLSURFACE = 19;
    public static final int SCN_MAX = 20;
    public static final int SCN_NONE = 0;
    public static final int SCN_NORMAL_MODE = 11;
    public static final int SCN_PACKAGE_SWITCH = 2;
    public static final int SCN_PROCESS_CREATE = 13;
    public static final int SCN_SPORTS_MODE = 10;
    public static final int SCN_SP_MODE = 17;
    public static final int SCN_SW_BOOST_1 = 14;
    public static final int SCN_SW_BOOST_2 = 15;
    public static final int SCN_SW_BOOST_3 = 16;
    public static final int SCN_SW_FRAME_UPDATE = 7;
    public static final int SCN_SW_LEVEL_BOOST = 9;
    public static final int SCN_VR_MODE = 18;
    public static final int SCREEN_OFF_DISABLE = 0;
    public static final int SCREEN_OFF_ENABLE = 1;
    public static final int SCREEN_OFF_WAIT_RESTORE = 2;
    public static final int STATE_DEAD = 3;
    public static final int STATE_DESTROYED = 2;
    public static final int STATE_PAUSED = 0;
    public static final int STATE_RESUMED = 1;
    public static final int STATE_STOPPED = 4;

    void appBoostEnable(String str);

    void boostDisable(int i);

    void boostEnable(int i);

    void boostEnableTimeout(int i, int i2);

    void boostEnableTimeoutMs(int i, int i2);

    void dumpAll();

    int getClusterInfo(int i, int i2);

    String getGiftAttr(String str, String str2);

    int getLastBoostPid();

    int getPackAttr(String str, int i);

    void levelBoost(int i);

    void notifyAppState(String str, String str2, int i, int i2);

    void notifyDisplayType(int i);

    void notifyFrameUpdate(int i);

    void notifyUserStatus(int i, int i2);

    int reloadWhiteList();

    void restorePolicy(int i);

    void setExclusiveCore(int i, int i2);

    void setFavorPid(int i);

    void setUidInfo(int i, int i2);

    void userDisable(int i);

    void userDisableAll();

    void userEnable(int i);

    void userEnableAsync(int i);

    void userEnableTimeout(int i, int i2);

    void userEnableTimeoutAsync(int i, int i2);

    void userEnableTimeoutMs(int i, int i2);

    void userEnableTimeoutMsAsync(int i, int i2);

    int userGetCapability(int i);

    int userReg(int i, int i2);

    int userRegBigLittle(int i, int i2, int i3, int i4);

    int userRegScn();

    void userRegScnConfig(int i, int i2, int i3, int i4, int i5, int i6);

    void userResetAll();

    void userRestoreAll();

    void userUnreg(int i);

    void userUnregScn(int i);
}

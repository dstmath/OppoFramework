package com.oppo.os;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import java.util.Objects;

public final class WaveformEffect implements Parcelable {
    public static final Parcelable.Creator<WaveformEffect> CREATOR = new Parcelable.Creator<WaveformEffect>() {
        /* class com.oppo.os.WaveformEffect.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WaveformEffect createFromParcel(Parcel p) {
            return new WaveformEffect(p);
        }

        @Override // android.os.Parcelable.Creator
        public WaveformEffect[] newArray(int size) {
            return new WaveformEffect[size];
        }
    };
    public static final int EFFECT_AFGAME_DOUBLE_KILL = 76;
    public static final int EFFECT_AFGAME_NORMAL_KILL = 75;
    public static final int EFFECT_AFGAME_PANTA_KILL = 79;
    public static final int EFFECT_AFGAME_TRIPLE_KILL = 77;
    public static final int EFFECT_AFGAME_ULTRA_KILL = 78;
    public static final int EFFECT_ALARM_WEATHER_CLOUDY = 145;
    public static final int EFFECT_ALARM_WEATHER_DEFAULT = 147;
    public static final int EFFECT_ALARM_WEATHER_RAIN = 151;
    public static final int EFFECT_ALARM_WEATHER_SMOG = 149;
    public static final int EFFECT_ALARM_WEATHER_SNOW = 150;
    public static final int EFFECT_ALARM_WEATHER_SUNNY = 148;
    public static final int EFFECT_ALARM_WEATHER_THUNDERSTORM = 146;
    public static final int EFFECT_ALARM_WEATHER_WIND = 144;
    public static final int EFFECT_CUSTOMIZED_ATTACH_TO_MIDDLE = 73;
    public static final int EFFECT_CUSTOMIZED_BREATHE_SPREAD_OUT = 74;
    public static final int EFFECT_CUSTOMIZED_CONFLICT = 52;
    public static final int EFFECT_CUSTOMIZED_CONVERGE = 51;
    public static final int EFFECT_CUSTOMIZED_LONG_VIBRATE = 70;
    public static final int EFFECT_CUSTOMIZED_RUSH_LEFT_TO_RIGHT = 53;
    public static final int EFFECT_CUSTOMIZED_SPREAD_OUT = 50;
    public static final int EFFECT_CUSTOMIZED_STRONG_GRANULAR = 69;
    public static final int EFFECT_CUSTOMIZED_STRONG_ONE_SENCOND = 72;
    public static final int EFFECT_CUSTOMIZED_STRONG_POINTFOUR_SENCOND = 71;
    public static final int EFFECT_CUSTOMIZED_THREE_DIMENSION_TOUCH = 49;
    public static final int EFFECT_CUSTOMIZED_WEAK_GRANULAR = 68;
    public static final int EFFECT_INVALID = -1;
    public static final int EFFECT_MODERATE_SHORT_VIBRATE_ONCE = 2;
    public static final int EFFECT_MODERATE_SHORT_VIBRATE_TRIPLE = 3;
    public static final int EFFECT_MODERATE_SHORT_VIBRATE_TWICE = 3;
    public static final int EFFECT_NOTIFICATION_CRYSTALCLEAR = 129;
    public static final int EFFECT_NOTIFICATION_EMERGE = 131;
    public static final int EFFECT_NOTIFICATION_FUN = 139;
    public static final int EFFECT_NOTIFICATION_GRANULES = 143;
    public static final int EFFECT_NOTIFICATION_HARP = 133;
    public static final int EFFECT_NOTIFICATION_HEARTBEAT = 6;
    public static final int EFFECT_NOTIFICATION_INGENIOUS = 142;
    public static final int EFFECT_NOTIFICATION_INSTANT = 138;
    public static final int EFFECT_NOTIFICATION_JOY = 136;
    public static final int EFFECT_NOTIFICATION_OVERTONE = 134;
    public static final int EFFECT_NOTIFICATION_PERCUSSION = 135;
    public static final int EFFECT_NOTIFICATION_RAPID = 8;
    public static final int EFFECT_NOTIFICATION_RECEIVE = 140;
    public static final int EFFECT_NOTIFICATION_REMIND = 7;
    public static final int EFFECT_NOTIFICATION_RIPPLES = 132;
    public static final int EFFECT_NOTIFICATION_SIMPLE = 128;
    public static final int EFFECT_NOTIFICATION_SPLASH = 141;
    public static final int EFFECT_NOTIFICATION_STREAK = 5;
    public static final int EFFECT_NOTIFICATION_SYMPHONIC = 4;
    public static final int EFFECT_NOTIFICATION_TUNES = 130;
    public static final int EFFECT_NOTIFICATION_TWINKLE = 137;
    public static final int EFFECT_RINGTONE_BLISS = 106;
    public static final int EFFECT_RINGTONE_CALM = 100;
    public static final int EFFECT_RINGTONE_CHILDHOOD = 121;
    public static final int EFFECT_RINGTONE_CLASSIC = 108;
    public static final int EFFECT_RINGTONE_COMMUTE = 123;
    public static final int EFFECT_RINGTONE_DANCE = 110;
    public static final int EFFECT_RINGTONE_DAZZLE = 119;
    public static final int EFFECT_RINGTONE_DELIGHT = 107;
    public static final int EFFECT_RINGTONE_DREAM = 103;
    public static final int EFFECT_RINGTONE_FAIRVIEWS = 125;
    public static final int EFFECT_RINGTONE_FIREFLY = 112;
    public static final int EFFECT_RINGTONE_GAZINGOUT = 118;
    public static final int EFFECT_RINGTONE_LAKESIDE = 117;
    public static final int EFFECT_RINGTONE_LONGING = 105;
    public static final int EFFECT_RINGTONE_MEMORIES = 115;
    public static final int EFFECT_RINGTONE_NIGHT = 116;
    public static final int EFFECT_RINGTONE_NOSTALGIC = 101;
    public static final int EFFECT_RINGTONE_NOVIBRATE = 67;
    public static final int EFFECT_RINGTONE_PLAYPARK = 109;
    public static final int EFFECT_RINGTONE_PURE = 127;
    public static final int EFFECT_RINGTONE_REALME_ITSREALME = 82;
    public static final int EFFECT_RINGTONE_REALME_JINGLE = 81;
    public static final int EFFECT_RINGTONE_REALME_TUNE = 80;
    public static final int EFFECT_RINGTONE_RELAX = 120;
    public static final int EFFECT_RINGTONE_ROMANCE = 102;
    public static final int EFFECT_RINGTONE_SCHOOL = 122;
    public static final int EFFECT_RINGTONE_SILENCE = 114;
    public static final int EFFECT_RINGTONE_SOLITUDE = 126;
    public static final int EFFECT_RINGTONE_STARS = 113;
    public static final int EFFECT_RINGTONE_SUMMER = 124;
    public static final int EFFECT_RINGTONE_TRINKETS = 111;
    public static final int EFFECT_RINGTONE_VISIONS = 104;
    public static final int EFFECT_SGAME_DOUBLE_KILL = 55;
    public static final int EFFECT_SGAME_FIRST_BLOOD = 54;
    public static final int EFFECT_SGAME_GODLIKE = 62;
    public static final int EFFECT_SGAME_KILLING_SPREE = 59;
    public static final int EFFECT_SGAME_LEGENDARY = 63;
    public static final int EFFECT_SGAME_PANTA_KILL = 58;
    public static final int EFFECT_SGAME_RAMPAGE = 60;
    public static final int EFFECT_SGAME_TRIBLE_KILL = 56;
    public static final int EFFECT_SGAME_ULTRA_KILL = 57;
    public static final int EFFECT_SGAME_UNSTOPPABLE = 61;
    public static final int EFFECT_VIBRATE_WITH_RINGTONE = 64;
    public static final int EFFECT_WEAKEST_SHORT_VIBRATE_ONCE = 0;
    public static final int EFFECT_WEAK_SHORT_VIBRATE_ONCE = 1;
    private static final ArrayMap RINGTONE_FILENAME_TITLE = new ArrayMap();
    private static final ArrayMap RINGTONE_TITLE_EFFECTS = new ArrayMap();
    private static final SparseIntArray RTPINDEX_EFFECTS = new SparseIntArray();
    private static final int RTP_INDEX_AFGAME_DOUBLE_KILL = 101;
    private static final int RTP_INDEX_AFGAME_NORMAL_KILL = 100;
    private static final int RTP_INDEX_AFGAME_PANTA_KILL = 104;
    private static final int RTP_INDEX_AFGAME_TRIBLE_KILL = 102;
    private static final int RTP_INDEX_AFGAME_ULTRA_KILL = 103;
    private static final int RTP_INDEX_ALARM_WEATHER_CLOUDY = 143;
    private static final int RTP_INDEX_ALARM_WEATHER_DEFAULT = 145;
    private static final int RTP_INDEX_ALARM_WEATHER_RAIN = 149;
    private static final int RTP_INDEX_ALARM_WEATHER_SMOG = 147;
    private static final int RTP_INDEX_ALARM_WEATHER_SNOW = 148;
    private static final int RTP_INDEX_ALARM_WEATHER_SUNNY = 146;
    private static final int RTP_INDEX_ALARM_WEATHER_THUNDERSTORM = 144;
    private static final int RTP_INDEX_ALARM_WEATHER_WIND = 142;
    private static final int RTP_INDEX_ATTACH_TO_MIDDLE = 54;
    private static final int RTP_INDEX_BREATHE_SPREAD_OUT = 55;
    private static final int RTP_INDEX_ERROR_MESSAGE = 46;
    private static final int RTP_INDEX_HEARTBEAT = 43;
    private static final int RTP_INDEX_LONG_VIBRATE = 56;
    private static final int RTP_INDEX_NOTIFICATION_CRYSTALCLEAR = 128;
    private static final int RTP_INDEX_NOTIFICATION_EMERGE = 123;
    private static final int RTP_INDEX_NOTIFICATION_FUN = 13;
    private static final int RTP_INDEX_NOTIFICATION_GRANULES = 9;
    private static final int RTP_INDEX_NOTIFICATION_HARP = 131;
    private static final int RTP_INDEX_NOTIFICATION_INGENIOUS = 12;
    private static final int RTP_INDEX_NOTIFICATION_INSTANT = 2;
    private static final int RTP_INDEX_NOTIFICATION_JOY = 129;
    private static final int RTP_INDEX_NOTIFICATION_OVERTONE = 132;
    private static final int RTP_INDEX_NOTIFICATION_PERCUSSION = 126;
    private static final int RTP_INDEX_NOTIFICATION_RECEIVE = 15;
    private static final int RTP_INDEX_NOTIFICATION_RIPPLES = 127;
    private static final int RTP_INDEX_NOTIFICATION_SIMPLE = 133;
    private static final int RTP_INDEX_NOTIFICATION_SPLASH = 16;
    private static final int RTP_INDEX_NOTIFICATION_TUNES = 125;
    private static final int RTP_INDEX_NOTIFICATION_TWINKLE = 130;
    private static final int RTP_INDEX_RAPID = 45;
    private static final int RTP_INDEX_REALME_ITSREALME = 500;
    private static final int RTP_INDEX_REALME_JINGLE = 502;
    private static final int RTP_INDEX_REALME_TUNE = 501;
    private static final int RTP_INDEX_REMIND = 44;
    private static final int RTP_INDEX_RINGTONE_BLISS = 138;
    private static final int RTP_INDEX_RINGTONE_CALM = 140;
    private static final int RTP_INDEX_RINGTONE_CHILDHOOD = 19;
    private static final int RTP_INDEX_RINGTONE_CLASSIC = 135;
    private static final int RTP_INDEX_RINGTONE_COMMUTE = 20;
    private static final int RTP_INDEX_RINGTONE_DANCE = 30;
    private static final int RTP_INDEX_RINGTONE_DAZZLE = 28;
    private static final int RTP_INDEX_RINGTONE_DELIGHT = 141;
    private static final int RTP_INDEX_RINGTONE_DREAM = 139;
    private static final int RTP_INDEX_RINGTONE_FAIRVIEWS = 121;
    private static final int RTP_INDEX_RINGTONE_FIREFLY = 22;
    private static final int RTP_INDEX_RINGTONE_GAZINGOUT = 24;
    private static final int RTP_INDEX_RINGTONE_LAKESIDE = 25;
    private static final int RTP_INDEX_RINGTONE_LONGING = 124;
    private static final int RTP_INDEX_RINGTONE_MEMORIES = 27;
    private static final int RTP_INDEX_RINGTONE_NIGHT = 29;
    private static final int RTP_INDEX_RINGTONE_NOSTALGIC = 134;
    private static final int RTP_INDEX_RINGTONE_PLAYPARK = 31;
    private static final int RTP_INDEX_RINGTONE_PURE = 49;
    private static final int RTP_INDEX_RINGTONE_RELAX = 32;
    private static final int RTP_INDEX_RINGTONE_ROMANCE = 137;
    private static final int RTP_INDEX_RINGTONE_SCHOOL = 17;
    private static final int RTP_INDEX_RINGTONE_SILENCE = 35;
    private static final int RTP_INDEX_RINGTONE_SOLITUDE = 34;
    private static final int RTP_INDEX_RINGTONE_STARS = 36;
    private static final int RTP_INDEX_RINGTONE_SUMMER = 37;
    private static final int RTP_INDEX_RINGTONE_TRINKETS = 38;
    private static final int RTP_INDEX_RINGTONE_VISIONS = 136;
    private static final int RTP_INDEX_SGAME_DOUBLE_KILL = 61;
    private static final int RTP_INDEX_SGAME_FIRST_BLOOD = 60;
    private static final int RTP_INDEX_SGAME_GODLIKE = 68;
    private static final int RTP_INDEX_SGAME_KILLING_SPREE = 65;
    private static final int RTP_INDEX_SGAME_LEGENDARY = 69;
    private static final int RTP_INDEX_SGAME_PANTA_KILL = 64;
    private static final int RTP_INDEX_SGAME_RAMPAGE = 66;
    private static final int RTP_INDEX_SGAME_TRIBLE_KILL = 62;
    private static final int RTP_INDEX_SGAME_ULTRA_KILL = 63;
    private static final int RTP_INDEX_SGAME_UNSTOPPABLE = 67;
    private static final int RTP_INDEX_SPREAD_OUT = 47;
    private static final int RTP_INDEX_STREAK = 42;
    private static final int RTP_INDEX_STRONG_ONE_SENCOND = 58;
    private static final int RTP_INDEX_STRONG_POINTFOUR_SENCOND = 57;
    private static final int RTP_INDEX_SYMPHONIC = 41;
    public static final int STRENGTH_LIGHT = 0;
    public static final int STRENGTH_MEDIUM = 1;
    public static final int STRENGTH_STRONG = 2;
    private static final String TAG = "WaveformEffect";
    private static final SparseIntArray WAVEFORMINDEX_EFFECTS = new SparseIntArray();
    private static final SparseLongArray WAVEFORM_EFFECT_DURATION = new SparseLongArray();
    private static final int WAVEFORM_INDEX_MODERATE_SHORT = 3;
    private static final int WAVEFORM_INDEX_RUSH_LEFT_TO_RIGHT = 5;
    private static final int WAVEFORM_INDEX_STRONG_GRANULAR = 6;
    private static final int WAVEFORM_INDEX_THREE_DIMENSION_TOUCH = 4;
    private static final int WAVEFORM_INDEX_WEAKEST_SHORT = 1;
    private static final int WAVEFORM_INDEX_WEAK_GRANULAR = 7;
    private static final int WAVEFORM_INDEX_WEAK_SHORT = 2;
    public static final int WAVEFORM_NODE_INVALID = -1;
    public static final int WAVEFORM_NODE_RAM = 1;
    public static final int WAVEFORM_NODE_RTP = 2;
    /* access modifiers changed from: private */
    public boolean mAsynchronous;
    /* access modifiers changed from: private */
    public boolean mEffectLoop;
    /* access modifiers changed from: private */
    public int mEffectStrength;
    /* access modifiers changed from: private */
    public int mEffectType;
    /* access modifiers changed from: private */
    public boolean mIsRingtoneCustomized;
    /* access modifiers changed from: private */
    public String mRingtoneFilePath;
    /* access modifiers changed from: private */
    public int mRingtoneVibrateType;
    /* access modifiers changed from: private */
    public boolean mStrengthSettingEnabled;
    /* access modifiers changed from: private */
    public int mUsageHint;

    static {
        WAVEFORMINDEX_EFFECTS.put(0, 1);
        WAVEFORMINDEX_EFFECTS.put(1, 2);
        WAVEFORMINDEX_EFFECTS.put(2, 3);
        WAVEFORMINDEX_EFFECTS.put(49, 4);
        WAVEFORMINDEX_EFFECTS.put(53, 5);
        WAVEFORMINDEX_EFFECTS.put(69, 6);
        WAVEFORMINDEX_EFFECTS.put(68, 7);
        RTPINDEX_EFFECTS.put(3, 46);
        RTPINDEX_EFFECTS.put(50, 47);
        RTPINDEX_EFFECTS.put(70, 56);
        RTPINDEX_EFFECTS.put(71, 57);
        RTPINDEX_EFFECTS.put(72, 58);
        RTPINDEX_EFFECTS.put(73, 54);
        RTPINDEX_EFFECTS.put(74, 55);
        RTPINDEX_EFFECTS.put(4, 41);
        RTPINDEX_EFFECTS.put(5, 42);
        RTPINDEX_EFFECTS.put(6, 43);
        RTPINDEX_EFFECTS.put(7, 44);
        RTPINDEX_EFFECTS.put(8, 45);
        RTPINDEX_EFFECTS.put(54, 60);
        RTPINDEX_EFFECTS.put(55, 61);
        RTPINDEX_EFFECTS.put(56, 62);
        RTPINDEX_EFFECTS.put(57, 63);
        RTPINDEX_EFFECTS.put(58, 64);
        RTPINDEX_EFFECTS.put(59, 65);
        RTPINDEX_EFFECTS.put(60, 66);
        RTPINDEX_EFFECTS.put(61, 67);
        RTPINDEX_EFFECTS.put(62, 68);
        RTPINDEX_EFFECTS.put(63, 69);
        RTPINDEX_EFFECTS.put(75, 100);
        RTPINDEX_EFFECTS.put(76, 101);
        RTPINDEX_EFFECTS.put(77, 102);
        RTPINDEX_EFFECTS.put(78, 103);
        RTPINDEX_EFFECTS.put(79, 104);
        RTPINDEX_EFFECTS.put(80, 501);
        RTPINDEX_EFFECTS.put(81, 502);
        RTPINDEX_EFFECTS.put(82, 500);
        RTPINDEX_EFFECTS.put(100, 140);
        RTPINDEX_EFFECTS.put(101, 134);
        RTPINDEX_EFFECTS.put(102, 137);
        RTPINDEX_EFFECTS.put(103, 139);
        RTPINDEX_EFFECTS.put(104, 136);
        RTPINDEX_EFFECTS.put(105, 124);
        RTPINDEX_EFFECTS.put(106, 138);
        RTPINDEX_EFFECTS.put(107, 141);
        RTPINDEX_EFFECTS.put(108, 135);
        RTPINDEX_EFFECTS.put(109, 31);
        RTPINDEX_EFFECTS.put(110, 30);
        RTPINDEX_EFFECTS.put(111, 38);
        RTPINDEX_EFFECTS.put(112, 22);
        RTPINDEX_EFFECTS.put(113, 36);
        RTPINDEX_EFFECTS.put(114, 35);
        RTPINDEX_EFFECTS.put(115, 27);
        RTPINDEX_EFFECTS.put(116, 29);
        RTPINDEX_EFFECTS.put(117, 25);
        RTPINDEX_EFFECTS.put(118, 24);
        RTPINDEX_EFFECTS.put(119, 28);
        RTPINDEX_EFFECTS.put(120, 32);
        RTPINDEX_EFFECTS.put(121, 19);
        RTPINDEX_EFFECTS.put(122, 17);
        RTPINDEX_EFFECTS.put(123, 20);
        RTPINDEX_EFFECTS.put(124, 37);
        RTPINDEX_EFFECTS.put(125, 121);
        RTPINDEX_EFFECTS.put(126, 34);
        RTPINDEX_EFFECTS.put(127, 49);
        RTPINDEX_EFFECTS.put(128, 133);
        RTPINDEX_EFFECTS.put(129, 128);
        RTPINDEX_EFFECTS.put(130, 125);
        RTPINDEX_EFFECTS.put(131, 123);
        RTPINDEX_EFFECTS.put(132, 127);
        RTPINDEX_EFFECTS.put(133, 131);
        RTPINDEX_EFFECTS.put(134, 132);
        RTPINDEX_EFFECTS.put(135, 126);
        RTPINDEX_EFFECTS.put(136, 129);
        RTPINDEX_EFFECTS.put(137, 130);
        RTPINDEX_EFFECTS.put(138, 2);
        RTPINDEX_EFFECTS.put(139, 13);
        RTPINDEX_EFFECTS.put(140, 15);
        RTPINDEX_EFFECTS.put(141, 16);
        RTPINDEX_EFFECTS.put(142, 12);
        RTPINDEX_EFFECTS.put(143, 9);
        RTPINDEX_EFFECTS.put(144, 142);
        RTPINDEX_EFFECTS.put(145, 143);
        RTPINDEX_EFFECTS.put(146, 144);
        RTPINDEX_EFFECTS.put(147, 145);
        RTPINDEX_EFFECTS.put(148, 146);
        RTPINDEX_EFFECTS.put(149, 147);
        RTPINDEX_EFFECTS.put(150, 148);
        RTPINDEX_EFFECTS.put(151, 149);
        RINGTONE_FILENAME_TITLE.put("ringtone_000", "it's realme");
        RINGTONE_FILENAME_TITLE.put("ringtone_0001", "realme Tune");
        RINGTONE_FILENAME_TITLE.put("ringtone_001", "Calm");
        RINGTONE_FILENAME_TITLE.put("ringtone_002", "Nostalgic");
        RINGTONE_FILENAME_TITLE.put("ringtone_003", "Romance");
        RINGTONE_FILENAME_TITLE.put("ringtone_004", "Dream");
        RINGTONE_FILENAME_TITLE.put("ringtone_005", "Visions");
        RINGTONE_FILENAME_TITLE.put("ringtone_006", "Longing");
        RINGTONE_FILENAME_TITLE.put("ringtone_007", "Bliss");
        RINGTONE_FILENAME_TITLE.put("ringtone_008", "Delight");
        RINGTONE_FILENAME_TITLE.put("ringtone_009", "Classic");
        RINGTONE_FILENAME_TITLE.put("ringtone_010", "Playpark");
        RINGTONE_FILENAME_TITLE.put("ringtone_011", "Dance");
        RINGTONE_FILENAME_TITLE.put("ringtone_012", "Trinkets");
        RINGTONE_FILENAME_TITLE.put("ringtone_013", "Firefly");
        RINGTONE_FILENAME_TITLE.put("ringtone_014", "Stars");
        RINGTONE_FILENAME_TITLE.put("ringtone_015", "Silence");
        RINGTONE_FILENAME_TITLE.put("ringtone_016", "Memories");
        RINGTONE_FILENAME_TITLE.put("ringtone_017", "Night");
        RINGTONE_FILENAME_TITLE.put("ringtone_018", "Lakeside");
        RINGTONE_FILENAME_TITLE.put("ringtone_019", "Gazingout");
        RINGTONE_FILENAME_TITLE.put("ringtone_020", "Dazzle");
        RINGTONE_FILENAME_TITLE.put("ringtone_021", "Relax");
        RINGTONE_FILENAME_TITLE.put("ringtone_022", "Childhood");
        RINGTONE_FILENAME_TITLE.put("ringtone_023", "School");
        RINGTONE_FILENAME_TITLE.put("ringtone_024", "Commute");
        RINGTONE_FILENAME_TITLE.put("ringtone_025", "Summer");
        RINGTONE_FILENAME_TITLE.put("ringtone_026", "Fairviews");
        RINGTONE_FILENAME_TITLE.put("ringtone_027", "Solitude");
        RINGTONE_FILENAME_TITLE.put("ringtone_028", "Pure");
        RINGTONE_FILENAME_TITLE.put("ringtone_wind", "Wind");
        RINGTONE_FILENAME_TITLE.put("ringtone_cloud", "Cloudy");
        RINGTONE_FILENAME_TITLE.put("ringtone_thunderstorm", "ThunderStorm");
        RINGTONE_FILENAME_TITLE.put("ringtone_weather_default", "WeatherDefault");
        RINGTONE_FILENAME_TITLE.put("ringtone_sun", "Sunny");
        RINGTONE_FILENAME_TITLE.put("ringtone_haze", "Haze");
        RINGTONE_FILENAME_TITLE.put("ringtone_snow", "Snow");
        RINGTONE_FILENAME_TITLE.put("ringtone_rain", "Rain");
        RINGTONE_FILENAME_TITLE.put("notification_000", "realme Jingle");
        RINGTONE_FILENAME_TITLE.put("notification_001", "Simple");
        RINGTONE_FILENAME_TITLE.put("notification_002", "CrystalClear");
        RINGTONE_FILENAME_TITLE.put("notification_003", "Tunes");
        RINGTONE_FILENAME_TITLE.put("notification_004", "Emerge");
        RINGTONE_FILENAME_TITLE.put("notification_005", "Ripples");
        RINGTONE_FILENAME_TITLE.put("notification_006", "Harp");
        RINGTONE_FILENAME_TITLE.put("notification_007", "Overtone");
        RINGTONE_FILENAME_TITLE.put("notification_008", "Percussion");
        RINGTONE_FILENAME_TITLE.put("notification_009", "Joy");
        RINGTONE_FILENAME_TITLE.put("notification_010", "Twinkle");
        RINGTONE_FILENAME_TITLE.put("notification_011", "Instant");
        RINGTONE_FILENAME_TITLE.put("notification_012", "Fun");
        RINGTONE_FILENAME_TITLE.put("notification_013", "Receive");
        RINGTONE_FILENAME_TITLE.put("notification_014", "Splash");
        RINGTONE_FILENAME_TITLE.put("notification_015", "Ingenious");
        RINGTONE_FILENAME_TITLE.put("notification_016", "Granules");
        RINGTONE_TITLE_EFFECTS.put("Calm", 100);
        RINGTONE_TITLE_EFFECTS.put("Nostalgic", 101);
        RINGTONE_TITLE_EFFECTS.put("Romance", 102);
        RINGTONE_TITLE_EFFECTS.put("Dream", 103);
        RINGTONE_TITLE_EFFECTS.put("Visions", 104);
        RINGTONE_TITLE_EFFECTS.put("Longing", 105);
        RINGTONE_TITLE_EFFECTS.put("Bliss", 106);
        RINGTONE_TITLE_EFFECTS.put("Delight", 107);
        RINGTONE_TITLE_EFFECTS.put("Classic", 108);
        RINGTONE_TITLE_EFFECTS.put("Playpark", 109);
        RINGTONE_TITLE_EFFECTS.put("Dance", 110);
        RINGTONE_TITLE_EFFECTS.put("Trinkets", 111);
        RINGTONE_TITLE_EFFECTS.put("Firefly", 112);
        RINGTONE_TITLE_EFFECTS.put("Stars", 113);
        RINGTONE_TITLE_EFFECTS.put("Silence", 114);
        RINGTONE_TITLE_EFFECTS.put("Memories", 115);
        RINGTONE_TITLE_EFFECTS.put("Night", 116);
        RINGTONE_TITLE_EFFECTS.put("Lakeside", 117);
        RINGTONE_TITLE_EFFECTS.put("Gazingout", 118);
        RINGTONE_TITLE_EFFECTS.put("Dazzle", 119);
        RINGTONE_TITLE_EFFECTS.put("Relax", 120);
        RINGTONE_TITLE_EFFECTS.put("Childhood", 121);
        RINGTONE_TITLE_EFFECTS.put("School", 122);
        RINGTONE_TITLE_EFFECTS.put("Commute", 123);
        RINGTONE_TITLE_EFFECTS.put("Summer", 124);
        RINGTONE_TITLE_EFFECTS.put("Fairviews", 125);
        RINGTONE_TITLE_EFFECTS.put("Solitude", 126);
        RINGTONE_TITLE_EFFECTS.put("Pure", 127);
        RINGTONE_TITLE_EFFECTS.put("Simple", 128);
        RINGTONE_TITLE_EFFECTS.put("CrystalClear", 129);
        RINGTONE_TITLE_EFFECTS.put("Tunes", 130);
        RINGTONE_TITLE_EFFECTS.put("Emerge", 131);
        RINGTONE_TITLE_EFFECTS.put("Ripples", 132);
        RINGTONE_TITLE_EFFECTS.put("Harp", 133);
        RINGTONE_TITLE_EFFECTS.put("Overtone", 134);
        RINGTONE_TITLE_EFFECTS.put("Percussion", 135);
        RINGTONE_TITLE_EFFECTS.put("Joy", 136);
        RINGTONE_TITLE_EFFECTS.put("Twinkle", 137);
        RINGTONE_TITLE_EFFECTS.put("Instant", 138);
        RINGTONE_TITLE_EFFECTS.put("Fun", 139);
        RINGTONE_TITLE_EFFECTS.put("Receive", 140);
        RINGTONE_TITLE_EFFECTS.put("Splash", 141);
        RINGTONE_TITLE_EFFECTS.put("Ingenious", 142);
        RINGTONE_TITLE_EFFECTS.put("Granules", 143);
        RINGTONE_TITLE_EFFECTS.put("Wind", 144);
        RINGTONE_TITLE_EFFECTS.put("Cloudy", 145);
        RINGTONE_TITLE_EFFECTS.put("ThunderStorm", 146);
        RINGTONE_TITLE_EFFECTS.put("WeatherDefault", 147);
        RINGTONE_TITLE_EFFECTS.put("Sunny", 148);
        RINGTONE_TITLE_EFFECTS.put("Haze", 149);
        RINGTONE_TITLE_EFFECTS.put("Snow", 150);
        RINGTONE_TITLE_EFFECTS.put("Rain", 151);
        RINGTONE_TITLE_EFFECTS.put("realme Tune", 80);
        RINGTONE_TITLE_EFFECTS.put("realme Jingle", 81);
        RINGTONE_TITLE_EFFECTS.put("it's realme", 82);
        WAVEFORM_EFFECT_DURATION.put(0, 14);
        WAVEFORM_EFFECT_DURATION.put(1, 14);
        WAVEFORM_EFFECT_DURATION.put(2, 70);
        WAVEFORM_EFFECT_DURATION.put(49, 80);
        WAVEFORM_EFFECT_DURATION.put(53, 100);
        WAVEFORM_EFFECT_DURATION.put(69, 20);
        WAVEFORM_EFFECT_DURATION.put(68, 12);
        WAVEFORM_EFFECT_DURATION.put(4, 2729);
        WAVEFORM_EFFECT_DURATION.put(5, 2081);
        WAVEFORM_EFFECT_DURATION.put(6, 3968);
        WAVEFORM_EFFECT_DURATION.put(7, 6000);
        WAVEFORM_EFFECT_DURATION.put(8, 4516);
        WAVEFORM_EFFECT_DURATION.put(3, 280);
        WAVEFORM_EFFECT_DURATION.put(50, 280);
        WAVEFORM_EFFECT_DURATION.put(73, 200);
        WAVEFORM_EFFECT_DURATION.put(74, 620);
        WAVEFORM_EFFECT_DURATION.put(70, 350);
        WAVEFORM_EFFECT_DURATION.put(51, 100);
        WAVEFORM_EFFECT_DURATION.put(52, 60);
        WAVEFORM_EFFECT_DURATION.put(71, 400);
        WAVEFORM_EFFECT_DURATION.put(72, 1000);
        WAVEFORM_EFFECT_DURATION.put(54, 220);
        WAVEFORM_EFFECT_DURATION.put(55, 210);
        WAVEFORM_EFFECT_DURATION.put(56, 410);
        WAVEFORM_EFFECT_DURATION.put(57, 430);
        WAVEFORM_EFFECT_DURATION.put(58, 790);
        WAVEFORM_EFFECT_DURATION.put(59, 520);
        WAVEFORM_EFFECT_DURATION.put(60, 470);
        WAVEFORM_EFFECT_DURATION.put(61, 590);
        WAVEFORM_EFFECT_DURATION.put(62, 630);
        WAVEFORM_EFFECT_DURATION.put(63, 1020);
        WAVEFORM_EFFECT_DURATION.put(75, 230);
        WAVEFORM_EFFECT_DURATION.put(76, 290);
        WAVEFORM_EFFECT_DURATION.put(77, 430);
        WAVEFORM_EFFECT_DURATION.put(78, 610);
        WAVEFORM_EFFECT_DURATION.put(79, 750);
        WAVEFORM_EFFECT_DURATION.put(82, 26157);
        WAVEFORM_EFFECT_DURATION.put(80, 21551);
        WAVEFORM_EFFECT_DURATION.put(81, 1731);
        WAVEFORM_EFFECT_DURATION.put(100, 31014);
        WAVEFORM_EFFECT_DURATION.put(101, 26328);
        WAVEFORM_EFFECT_DURATION.put(102, 26098);
        WAVEFORM_EFFECT_DURATION.put(103, 20018);
        WAVEFORM_EFFECT_DURATION.put(104, 28260);
        WAVEFORM_EFFECT_DURATION.put(105, 14015);
        WAVEFORM_EFFECT_DURATION.put(106, 26909);
        WAVEFORM_EFFECT_DURATION.put(107, 20875);
        WAVEFORM_EFFECT_DURATION.put(108, 1975);
        WAVEFORM_EFFECT_DURATION.put(109, 17214);
        WAVEFORM_EFFECT_DURATION.put(110, 18000);
        WAVEFORM_EFFECT_DURATION.put(111, 18925);
        WAVEFORM_EFFECT_DURATION.put(112, 11954);
        WAVEFORM_EFFECT_DURATION.put(113, 21600);
        WAVEFORM_EFFECT_DURATION.put(114, 26666);
        WAVEFORM_EFFECT_DURATION.put(115, 22169);
        WAVEFORM_EFFECT_DURATION.put(116, 9546);
        WAVEFORM_EFFECT_DURATION.put(117, 16172);
        WAVEFORM_EFFECT_DURATION.put(118, 12675);
        WAVEFORM_EFFECT_DURATION.put(119, 17650);
        WAVEFORM_EFFECT_DURATION.put(120, 8202);
        WAVEFORM_EFFECT_DURATION.put(121, 18866);
        WAVEFORM_EFFECT_DURATION.put(122, 10575);
        WAVEFORM_EFFECT_DURATION.put(123, 26942);
        WAVEFORM_EFFECT_DURATION.put(124, 15301);
        WAVEFORM_EFFECT_DURATION.put(125, 32571);
        WAVEFORM_EFFECT_DURATION.put(126, 27712);
        WAVEFORM_EFFECT_DURATION.put(127, 10445);
        WAVEFORM_EFFECT_DURATION.put(128, 1140);
        WAVEFORM_EFFECT_DURATION.put(129, 382);
        WAVEFORM_EFFECT_DURATION.put(130, 1036);
        WAVEFORM_EFFECT_DURATION.put(131, 822);
        WAVEFORM_EFFECT_DURATION.put(132, 879);
        WAVEFORM_EFFECT_DURATION.put(133, 1928);
        WAVEFORM_EFFECT_DURATION.put(134, 830);
        WAVEFORM_EFFECT_DURATION.put(135, 941);
        WAVEFORM_EFFECT_DURATION.put(136, 866);
        WAVEFORM_EFFECT_DURATION.put(137, 1947);
        WAVEFORM_EFFECT_DURATION.put(138, 1044);
        WAVEFORM_EFFECT_DURATION.put(139, 1876);
        WAVEFORM_EFFECT_DURATION.put(140, 3958);
        WAVEFORM_EFFECT_DURATION.put(141, 2515);
        WAVEFORM_EFFECT_DURATION.put(142, 877);
        WAVEFORM_EFFECT_DURATION.put(143, 2387);
        WAVEFORM_EFFECT_DURATION.put(144, 19226);
        WAVEFORM_EFFECT_DURATION.put(145, 21036);
        WAVEFORM_EFFECT_DURATION.put(146, 15967);
        WAVEFORM_EFFECT_DURATION.put(147, 30336);
        WAVEFORM_EFFECT_DURATION.put(148, 21434);
        WAVEFORM_EFFECT_DURATION.put(149, 16759);
        WAVEFORM_EFFECT_DURATION.put(150, 18201);
        WAVEFORM_EFFECT_DURATION.put(151, 16533);
    }

    private WaveformEffect() {
        this.mEffectType = -1;
        this.mEffectStrength = 1;
        this.mEffectLoop = false;
        this.mRingtoneVibrateType = -1;
        this.mIsRingtoneCustomized = false;
        this.mRingtoneFilePath = "";
        this.mStrengthSettingEnabled = false;
        this.mAsynchronous = false;
        this.mUsageHint = 0;
    }

    public int getEffectType() {
        return this.mEffectType;
    }

    public int[] getWaveFormIndexArray() {
        int waveFormindex;
        if (WAVEFORMINDEX_EFFECTS.indexOfKey(this.mEffectType) >= 0) {
            waveFormindex = WAVEFORMINDEX_EFFECTS.get(this.mEffectType);
        } else if (RTPINDEX_EFFECTS.indexOfKey(this.mEffectType) >= 0) {
            waveFormindex = RTPINDEX_EFFECTS.get(this.mEffectType);
        } else {
            waveFormindex = -1;
        }
        return new int[]{waveFormindex};
    }

    public int[] getWaveFormIndexArray(int effectType) {
        int waveFormindex;
        if (WAVEFORMINDEX_EFFECTS.indexOfKey(effectType) >= 0) {
            waveFormindex = WAVEFORMINDEX_EFFECTS.get(effectType);
        } else if (RTPINDEX_EFFECTS.indexOfKey(effectType) >= 0) {
            waveFormindex = RTPINDEX_EFFECTS.get(effectType);
        } else {
            waveFormindex = -1;
        }
        int[] waveFormIndexArray = {waveFormindex};
        Slog.d(TAG, "getWaveFormDurationArray effectType=" + effectType + " waveFormIndexArray=" + waveFormindex);
        return waveFormIndexArray;
    }

    public long[] getWaveFormDurationArray() {
        long duration = 0;
        if (WAVEFORM_EFFECT_DURATION.indexOfKey(this.mEffectType) >= 0) {
            duration = WAVEFORM_EFFECT_DURATION.get(this.mEffectType);
        }
        long[] durationArray = {duration};
        Slog.d(TAG, "getWaveFormDurationArray mEffectType=" + this.mEffectType + " duration=" + duration + " indexOfKey=" + WAVEFORM_EFFECT_DURATION.indexOfKey(this.mEffectType));
        return durationArray;
    }

    public long[] getWaveFormDurationArray(int effectType) {
        long duration = 0;
        if (WAVEFORM_EFFECT_DURATION.indexOfKey(effectType) >= 0) {
            duration = WAVEFORM_EFFECT_DURATION.get(effectType);
        }
        Slog.d(TAG, "getWaveFormDurationArray effectType=" + effectType + " duration=" + duration);
        return new long[]{duration};
    }

    public int getEffectStrength() {
        return 2;
    }

    public boolean getEffectLoop() {
        return this.mEffectLoop;
    }

    public int getRingtoneVibrateType() {
        return this.mRingtoneVibrateType;
    }

    public boolean getIsRingtoneCustomized() {
        return this.mIsRingtoneCustomized;
    }

    public String getRingtoneFilePath() {
        return this.mRingtoneFilePath;
    }

    public boolean getStrengthSettingEnabled() {
        return this.mStrengthSettingEnabled;
    }

    public boolean getAsynchronous() {
        return this.mAsynchronous;
    }

    public int getUsageHint() {
        return this.mUsageHint;
    }

    public int getWaveFormNodeType() {
        if (this.mRingtoneVibrateType != -1) {
            return -1;
        }
        if (WAVEFORMINDEX_EFFECTS.indexOfKey(this.mEffectType) >= 0) {
            return 1;
        }
        if (RTPINDEX_EFFECTS.indexOfKey(this.mEffectType) >= 0) {
            return 2;
        }
        return -1;
    }

    public static String getRingtoneTitle(String ringtoneName) {
        if (RINGTONE_FILENAME_TITLE.containsKey(ringtoneName)) {
            return (String) RINGTONE_FILENAME_TITLE.get(ringtoneName);
        }
        return "";
    }

    public static int getRingtoneWaveFormEffect(String ringtoneTitle) {
        if (RINGTONE_TITLE_EFFECTS.containsKey(ringtoneTitle)) {
            return ((Integer) RINGTONE_TITLE_EFFECTS.get(ringtoneTitle)).intValue();
        }
        return -1;
    }

    public static class Builder {
        private boolean mAsynchronous = false;
        private boolean mEffectLoop = false;
        private int mEffectStrength = 1;
        private int mEffectType = -1;
        private boolean mIsRingtoneCustomized = false;
        private String mRingtoneFilePath = "";
        private int mRingtoneVibrateType = -1;
        private boolean mStrengthSettingEnabled = false;
        private int mUsageHint = 0;

        public Builder() {
        }

        public Builder(WaveformEffect we) {
            this.mEffectType = we.mEffectType;
        }

        public WaveformEffect build() {
            WaveformEffect we = new WaveformEffect();
            int unused = we.mEffectType = this.mEffectType;
            int unused2 = we.mEffectStrength = this.mEffectStrength;
            boolean unused3 = we.mEffectLoop = this.mEffectLoop;
            int unused4 = we.mRingtoneVibrateType = this.mRingtoneVibrateType;
            boolean unused5 = we.mIsRingtoneCustomized = this.mIsRingtoneCustomized;
            String unused6 = we.mRingtoneFilePath = this.mRingtoneFilePath;
            boolean unused7 = we.mStrengthSettingEnabled = this.mStrengthSettingEnabled;
            boolean unused8 = we.mAsynchronous = this.mAsynchronous;
            int unused9 = we.mUsageHint = this.mUsageHint;
            return we;
        }

        /* JADX WARNING: Removed duplicated region for block: B:5:0x0010 A[FALL_THROUGH] */
        public Builder setEffectType(int type) {
            switch (type) {
                default:
                    switch (type) {
                        default:
                            switch (type) {
                                default:
                                    switch (type) {
                                        case 100:
                                        case 101:
                                        case 102:
                                        case 103:
                                        case 104:
                                        case 105:
                                        case 106:
                                        case 107:
                                        case 108:
                                        case 109:
                                        case 110:
                                        case 111:
                                        case 112:
                                        case 113:
                                        case 114:
                                        case 115:
                                        case 116:
                                        case 117:
                                        case 118:
                                        case 119:
                                        case 120:
                                        case 121:
                                        case 122:
                                        case 123:
                                        case 124:
                                        case 125:
                                        case 126:
                                        case 127:
                                        case 128:
                                        case 129:
                                        case 130:
                                        case 131:
                                        case 132:
                                        case 133:
                                        case 134:
                                        case 135:
                                        case 136:
                                        case 137:
                                        case 138:
                                        case 139:
                                        case 140:
                                        case 141:
                                        case 142:
                                        case 143:
                                        case 144:
                                        case 145:
                                        case 146:
                                        case 147:
                                        case 148:
                                        case 149:
                                        case 150:
                                        case 151:
                                            break;
                                        default:
                                            this.mEffectType = -1;
                                            break;
                                    }
                                case 68:
                                case 69:
                                case 70:
                                case 71:
                                case 72:
                                case 73:
                                case 74:
                                case 75:
                                case 76:
                                case 77:
                                case 78:
                                case 79:
                                    this.mEffectType = type;
                                    break;
                            }
                        case 49:
                        case 50:
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                        case 58:
                        case 59:
                        case 60:
                        case 61:
                        case 62:
                        case 63:
                            break;
                    }
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    break;
            }
            return this;
        }

        public Builder setEffectStrength(int strength) {
            if (strength == 0 || strength == 1 || strength == 2) {
                this.mEffectStrength = strength;
            } else {
                this.mEffectStrength = 1;
            }
            return this;
        }

        public Builder setEffectLoop(boolean loop) {
            this.mEffectLoop = loop;
            return this;
        }

        public Builder setRingtoneVibrateType(int type) {
            if (!(type == 64 || type == 67)) {
                switch (type) {
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        break;
                    default:
                        this.mRingtoneVibrateType = -1;
                        break;
                }
                return this;
            }
            this.mRingtoneVibrateType = type;
            return this;
        }

        public Builder setIsRingtoneCustomized(boolean customized) {
            this.mIsRingtoneCustomized = customized;
            return this;
        }

        public Builder setRingtoneFilePath(String path) {
            this.mRingtoneFilePath = path;
            return this;
        }

        public Builder setStrengthSettingEnabled(boolean enabled) {
            this.mStrengthSettingEnabled = enabled;
            return this;
        }

        public Builder setAsynchronous(boolean async) {
            this.mAsynchronous = async;
            return this;
        }

        public Builder setUsageHint(int usageHint) {
            this.mUsageHint = usageHint;
            return this;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEffectType);
        dest.writeInt(this.mEffectStrength);
        dest.writeBoolean(this.mEffectLoop);
        dest.writeInt(this.mRingtoneVibrateType);
        dest.writeBoolean(this.mIsRingtoneCustomized);
        dest.writeString(this.mRingtoneFilePath);
        dest.writeBoolean(this.mStrengthSettingEnabled);
        dest.writeBoolean(this.mAsynchronous);
        dest.writeInt(this.mUsageHint);
    }

    private WaveformEffect(Parcel in) {
        this.mEffectType = -1;
        this.mEffectStrength = 1;
        this.mEffectLoop = false;
        this.mRingtoneVibrateType = -1;
        this.mIsRingtoneCustomized = false;
        this.mRingtoneFilePath = "";
        this.mStrengthSettingEnabled = false;
        this.mAsynchronous = false;
        this.mUsageHint = 0;
        this.mEffectType = in.readInt();
        this.mEffectStrength = in.readInt();
        this.mEffectLoop = in.readBoolean();
        this.mRingtoneVibrateType = in.readInt();
        this.mIsRingtoneCustomized = in.readBoolean();
        this.mRingtoneFilePath = in.readString();
        this.mStrengthSettingEnabled = in.readBoolean();
        this.mAsynchronous = in.readBoolean();
        this.mUsageHint = in.readInt();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaveformEffect that = (WaveformEffect) o;
        if (this.mEffectType == that.getEffectType() && this.mEffectStrength == that.getEffectStrength() && this.mEffectLoop == that.getEffectLoop() && this.mRingtoneVibrateType == that.getRingtoneVibrateType() && this.mIsRingtoneCustomized == that.getIsRingtoneCustomized() && this.mRingtoneFilePath == that.getRingtoneFilePath() && this.mStrengthSettingEnabled == that.getStrengthSettingEnabled() && this.mAsynchronous == that.getAsynchronous() && this.mUsageHint == that.getUsageHint()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[0]);
    }

    public String toString() {
        return new String("WaveformEffect: effect type=" + this.mEffectType + " effect strength=" + this.mEffectStrength + " effect loop=" + this.mEffectLoop + " effect ringtone vibrate type=" + this.mRingtoneVibrateType + " effect is ringtone customized=" + this.mIsRingtoneCustomized + " effect ringtone filepath=" + this.mRingtoneFilePath + " effect strength settings enabled=" + this.mStrengthSettingEnabled + " effect asynchronous=" + this.mAsynchronous + " effect usageHint=" + this.mUsageHint);
    }
}

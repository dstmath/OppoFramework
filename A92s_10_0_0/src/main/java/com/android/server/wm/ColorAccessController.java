package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorAccessController {
    public static final String ALIPAY_TRANSPROCESSPAYACTIVITY_ACTIVITY_FILTER = "com.alipay.android.app.TransProcessPayActivity";
    public static final String ANDROID_DIALER_EMERGENCYCALL = "com.android.dialer.dialpad.EmergencyCall";
    private static final String APP_PROTECT_RUS_CONFIG_FILE_FORMAT = ".xml";
    private static final String APP_PROTECT_RUS_CONFIG_FILE_NAME = "app_protect_rus_config.xml";
    private static final String APP_PROTECT_RUS_CONFIG_FILE_PATH;
    private static final String APP_PROTECT_RUS_CONFIG_NAME = "app_protect_rus_config";
    private static final String APP_PROTECT_TAG = "app_protect";
    public static final String CONTACTS_EMERGENCYCALL = "com.android.contacts.dialpad.EmergencyCall";
    private static final String DIR = (File.separator + "data" + File.separator + "oppo" + File.separator + "coloros" + File.separator + "safecenter" + File.separator);
    public static final String FACEBOOK_SHARE_PLATFORM_ACTIVITY_FILTER = "com.facebook.platform.common.activity.PlatformWrapperActivity";
    public static final ArrayList<String> FILTER_PROTECT_ACTIVITY_LIST = new ArrayList<>(Arrays.asList(OPPO_SMS_SPECIAL_ACTIVITY_FILTER, OPPO_SMS_SPECIAL2_ACTIVITY_FILTER, OPPO_SMS_SPECIAL3_ACTIVITY_FILTER, OPPO_LOCK_CHOOSE1_ACTIVITY_FILTER, OPPO_LOCK_CHOOSE2_ACTIVITY_FILTER, OPPO_LOCK_CHOOSE3_ACTIVITY_FILTER, OPPO_LOCK_COMFIRM1_ACTIVITY_FILTER, OPPO_LOCK_COMFIRM2_ACTIVITY_FILTER, OPPO_LOCK_FINGER1_ACTIVITY_FILTER, OPPO_LOCK_FINGER2_ACTIVITY_FILTER, OPPO_LOCK_FINGER3_ACTIVITY_FILTER, OPPO_LOCK_FINGER4_ACTIVITY_FILTER, OPPO_LOCK_FINGER5_ACTIVITY_FILTER, OPPO_LOCK_RESET_ACTIVITY_FILTER, OPPO_LOCK_RESET_ACTIVITY_FILTER2, OPPO_USER_CENTER_ACTIVITY_FILTER, OPPO_FILEMANAGER_RING0_ACTIVITY_FILTER, OPPO_FILEMANAGER_RING1_ACTIVITY_FILTER, OPPO_FILEMANAGER_RING2_ACTIVITY_FILTER, TRANSPARENT_DIDIDACHE_ACTIVITY_FILTER, QQ2MM_CALLBACK_ACTIVITY_FILTER, FACEBOOK_SHARE_PLATFORM_ACTIVITY_FILTER, QQ_AV_UI_VACHATACTIVITY_FILTER, OPPO_SETTINGS_PRIV_PWD_CHOOSEGENERIC_FILTER, OPPO_SETTINGS_PRIV_PWD_CHOOSEPATTERN_FILTER, OPPO_SETTINGS_PRIV_PWD_CHOOSENUMBER_FILTER, OPPO_SETTINGS_PRIV_PWD_CHOOSECOMPLEX_FILTER, OPPO_SETTINGS_PRIV_PWD_SAFEQUESTION_FILTER, OPPO_SETTINGS_PRIV_PWD_CONFIRMCENERIC_FILTER, OPPO_SETTINGS_PRIV_PWD_CONFIRMPATTERN_FILTER, OPPO_SETTINGS_PRIV_PWD_CONFIRMNUMBER_FILTER, OPPO_SETTINGS_PRIV_PWD_CONFIRMCOMPLEX_FILTER, OPPO_SETTINGS_PRIV_PWD_QUESTIONSELECT_FILTER, OPPO_SETTINGS_PRIV_PWD_CHECKANSWER_FILTER, OPPO_SETTINGS_PRIV_PWD_RESETGENERIC_FILTER, SETTING_DEVICEADMINADD_ACTIVITY_FILTER, SETTINGS_DEVICEADMINADD_FILTER, SETTINGS_CONFIRMLOCKPASSWORD_FILTER, SETTINGS_CONFIRMLOCKPASSWORD_INTERNALACTIVITY_FILTER, SETTINGS_CHOOSELOCKPASSWORD_FILTER, SETTINGS_CHOOSELOCKPATTERN_FILTER, SETTINGS_CHOOSELOCKGENERIC_FILTER, SETTINGS_CHOOSELOCKGENERIC_INTERNALACTIVITY_FILTER, SETTINGS_CONFIRMLOCKPATTERN_FILTER, SETTINGS_CHOOSELOCKPATTERN_INTERNALACTIVITY_FILTER, SETTINGS_CHOOSELOCKPASSWORD_INTERNALACTIVITY_FILTER, SETTINGS_CONFIRMLOCKPATTERN_INTERNALACTIVITY_FILTER, SETTINGS_SAFEQUESTIONACTIVITY_FILTER, SETTINGS_RESETGENERICPRIVACY_FILTER, SETTINGS_CHECKANSWERACTIVITY_FILTER, SETTINGS_CHOOSEPATTERNPRIVACY_FILTER, SETTINGS_CONFIRMCOMPLEXPRIVACY_FILTER, SETTINGS_CHOOSEGENERICPRIVACY_FILTER, SETTINGS_CHOOSECOMPLEXPRIVACY_FILTER, SETTINGS_CONFIRMNUMBERPRIVACY_FILTER, SETTINGS_QUESTIONSELECTACTIVITY_FILTER, SETTINGS_CONFIRMGENERICPRIVACY_FILTER, SETTINGS_CHOOSENUMBERPRIVACY_FILTER, SETTINGS_FINGERPRINTRESETPASSWORD_FILTER, SETTINGS_CONFIRMPATTERNPRIVACY_FILTER, SETTINGS_RESETGENERICACTIVITY_FILTER, MM_PLUGIN_WALLET_PAY_UI_WALLETPAYUI_ACTIVITY_FILTER, MM_PLUGIN_WALLET_CORE_UI_WALLETORDERINFOUI_ACTIVITY_FILTER, MM_PLUGIN_FINGERPRINT_UI_ACTIVITY_FILTER, ALIPAY_TRANSPROCESSPAYACTIVITY_ACTIVITY_FILTER, NETEASE_AWAKE_AWAKEACTIVITY_FILTER, RECENT_APP, ANDROID_DIALER_EMERGENCYCALL, CONTACTS_EMERGENCYCALL, MMS_UI_EXTERUSEEXPACTIVITY, MMS_SMSTIMERACTIVITY, SETTINGS_APN_SETTING, SETTINGS_VPN_SETTING, SETTINGS_PUSH_SERVICE, SETTINGS_POWER_OFFACTIVITY, ITMER_POWER_OFF, ITMER_POWER_OFF_LOCKED, SSWO_SSWOACTIVITY_FILTER));
    public static final ArrayList<String> HIDE_PROTECT_ACTIVITY_LIST = new ArrayList<>(Arrays.asList(QQ_LS_ACTIVITY_HIDE));
    private static final String ITEM_TAG = "item";
    public static final String ITMER_POWER_OFF = "com.oppo.settings.timepower.PowerOffPromptActivity";
    public static final String ITMER_POWER_OFF_LOCKED = "com.oppo.settings.timepower.PowerOffPromptActivity$NewStylePowerOffPromptActivity";
    public static final String MMS_SMSTIMERACTIVITY = "com.android.mms.ui.MmsOppoSmsTimerActivity";
    public static final String MMS_UI_EXTERUSEEXPACTIVITY = "com.oppo.mms.ui.MmsOppoExterUseExpActivity";
    public static final String MM_PLUGIN_FINGERPRINT_UI_ACTIVITY_FILTER = "com.tencent.mm.plugin.fingerprint.ui.FingerPrintAuthTransparentUI";
    public static final String MM_PLUGIN_WALLET_CORE_UI_WALLETORDERINFOUI_ACTIVITY_FILTER = "com.tencent.mm.plugin.wallet_core.ui.WalletOrderInfoUI";
    public static final String MM_PLUGIN_WALLET_PAY_UI_WALLETPAYUI_ACTIVITY_FILTER = "com.tencent.mm.plugin.wallet.pay.ui.WalletPayUI";
    private static final String NAME_TAG = "name";
    public static final String NETEASE_AWAKE_AWAKEACTIVITY_FILTER = "com.netease.awake.AwakeActivity";
    public static final String OPPO_FILEMANAGER_RING0_ACTIVITY_FILTER = "com.coloros.filemanager.view.picker.music.MusicFilePickerActivity";
    public static final String OPPO_FILEMANAGER_RING1_ACTIVITY_FILTER = "com.coloros.filemanager.view.ringtone.RingtoneSettingsActivity";
    public static final String OPPO_FILEMANAGER_RING2_ACTIVITY_FILTER = "com.coloros.filemanager.view.external.ringtone.RingtoneSettingsActivity";
    public static final String OPPO_LOCK_CHOOSE1_ACTIVITY_FILTER = "com.android.settings.ChooseLockPattern$InternalActivity";
    public static final String OPPO_LOCK_CHOOSE2_ACTIVITY_FILTER = "com.android.settings.ChooseLockPassword$InternalActivity";
    public static final String OPPO_LOCK_CHOOSE3_ACTIVITY_FILTER = "com.android.settings.ChooseLockGeneric$InternalActivity";
    public static final String OPPO_LOCK_COMFIRM1_ACTIVITY_FILTER = "com.android.settings.ConfirmLockPattern$InternalActivity";
    public static final String OPPO_LOCK_COMFIRM2_ACTIVITY_FILTER = "com.android.settings.ConfirmLockPassword$InternalActivity";
    public static final String OPPO_LOCK_FINGER1_ACTIVITY_FILTER = "com.android.settings.ChooseLockPattern";
    public static final String OPPO_LOCK_FINGER2_ACTIVITY_FILTER = "com.android.settings.ChooseLockPassword";
    public static final String OPPO_LOCK_FINGER3_ACTIVITY_FILTER = "com.android.settings.ChooseLockGeneric";
    public static final String OPPO_LOCK_FINGER4_ACTIVITY_FILTER = "com.android.settings.ConfirmLockPattern";
    public static final String OPPO_LOCK_FINGER5_ACTIVITY_FILTER = "com.android.settings.ConfirmLockPassword";
    public static final String OPPO_LOCK_RESET_ACTIVITY_FILTER = "com.oppo.settings.fingerprint.FingerprintResetPassword";
    public static final String OPPO_LOCK_RESET_ACTIVITY_FILTER2 = "com.oppo.settings.fingerprint.ResetGenericActivity";
    public static final String OPPO_SETTINGS_PRIV_PWD_CHECKANSWER_FILTER = "com.oppo.settings.privacy.CheckAnswerActivity";
    public static final String OPPO_SETTINGS_PRIV_PWD_CHOOSECOMPLEX_FILTER = "com.oppo.settings.privacy.ChooseComplexPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CHOOSEGENERIC_FILTER = "com.oppo.settings.privacy.ChooseGenericPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CHOOSENUMBER_FILTER = "com.oppo.settings.privacy.ChooseNumberPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CHOOSEPATTERN_FILTER = "com.oppo.settings.privacy.ChoosePatternPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CONFIRMCENERIC_FILTER = "com.oppo.settings.privacy.ConfirmGenericPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CONFIRMCOMPLEX_FILTER = "com.oppo.settings.privacy.ConfirmComplexPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CONFIRMNUMBER_FILTER = "com.oppo.settings.privacy.ConfirmNumberPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_CONFIRMPATTERN_FILTER = "com.oppo.settings.privacy.ConfirmPatternPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_QUESTIONSELECT_FILTER = "com.oppo.settings.privacy.QuestionSelectActivity";
    public static final String OPPO_SETTINGS_PRIV_PWD_RESETGENERIC_FILTER = "com.oppo.settings.privacy.ResetGenericPrivacy";
    public static final String OPPO_SETTINGS_PRIV_PWD_SAFEQUESTION_FILTER = "com.oppo.settings.privacy.SafeQuestionActivity";
    public static final String OPPO_SMS_SPECIAL2_ACTIVITY_FILTER = "com.ted.sdk.mms.ui.TedAboutActivity";
    public static final String OPPO_SMS_SPECIAL3_ACTIVITY_FILTER = "com.android.mms.ui.ClassZeroActivity";
    public static final String OPPO_SMS_SPECIAL_ACTIVITY_FILTER = "com.ted.sdk.mms.ui.TedUpdateSettingActivity";
    public static final String OPPO_USER_CENTER_ACTIVITY_FILTER = "com.oppo.usercenter.open.UserCenterContainerActivity";
    public static final String PROTECT_FILTER_USERCENTER_ACTION = "oppo.usercenter.intent.action.retrieve_password_verificarion";
    public static final String PROTECT_FILTER_USERCENTER_EXTRA_KEY = "current_package_name";
    public static final String PROTECT_FILTER_USERCENTER_EXTRA_VALUE = "com.android.keyguard";
    public static final String QQ2MM_CALLBACK_ACTIVITY_FILTER = "com.tencent.mm.plugin.webview.ui.tools.QQCallbackUI";
    public static final String QQ_AV_UI_VACHATACTIVITY_FILTER = "com.tencent.av.ui.VChatActivity";
    private static final String QQ_LS_ACTIVITY_HIDE = "com.tencent.mobileqq.activity.QQLSActivity";
    public static final String RECENT_APP = "com.android.internal.policy.impl.RecentApplicationsActivity";
    private static final String ROOMUPDATE_FILTER_ATIVITY_FILE_NAME = "filter_app_protect_data_update.txt";
    private static final String ROOMUPDATE_HIDE_ATIVITY_FILE_NAME = "hide_app_protect_data_update.txt";
    private static final int RUS_TYPE_IGNORE_SWITCH_APP = 2;
    public static final String SETTINGS_APN_SETTING = "com.android.settings.ApnSettings";
    public static final String SETTINGS_CHECKANSWERACTIVITY_FILTER = "com.coloros.settings.privacy.CheckAnswerActivity";
    public static final String SETTINGS_CHOOSECOMPLEXPRIVACY_FILTER = "com.coloros.settings.privacy.ChooseComplexPrivacy";
    public static final String SETTINGS_CHOOSEGENERICPRIVACY_FILTER = "com.coloros.settings.privacy.ChooseGenericPrivacy";
    public static final String SETTINGS_CHOOSELOCKGENERIC_FILTER = "com.android.settings.password.ChooseLockGeneric";
    public static final String SETTINGS_CHOOSELOCKGENERIC_INTERNALACTIVITY_FILTER = "com.android.settings.password.ChooseLockGeneric$InternalActivity";
    public static final String SETTINGS_CHOOSELOCKPASSWORD_FILTER = "com.android.settings.password.ChooseLockPassword";
    public static final String SETTINGS_CHOOSELOCKPASSWORD_INTERNALACTIVITY_FILTER = "com.android.settings.password.ChooseLockPassword$InternalActivity";
    public static final String SETTINGS_CHOOSELOCKPATTERN_FILTER = "com.android.settings.password.ChooseLockPattern";
    public static final String SETTINGS_CHOOSELOCKPATTERN_INTERNALACTIVITY_FILTER = "com.android.settings.password.ChooseLockPattern$InternalActivity";
    public static final String SETTINGS_CHOOSENUMBERPRIVACY_FILTER = "com.coloros.settings.privacy.ChooseNumberPrivacy";
    public static final String SETTINGS_CHOOSEPATTERNPRIVACY_FILTER = "com.coloros.settings.privacy.ChoosePatternPrivacy";
    public static final String SETTINGS_CONFIRMCOMPLEXPRIVACY_FILTER = "com.coloros.settings.privacy.ConfirmComplexPrivacy";
    public static final String SETTINGS_CONFIRMGENERICPRIVACY_FILTER = "com.coloros.settings.privacy.ConfirmGenericPrivacy";
    public static final String SETTINGS_CONFIRMLOCKPASSWORD_FILTER = "com.android.settings.password.ConfirmLockPassword";
    public static final String SETTINGS_CONFIRMLOCKPASSWORD_INTERNALACTIVITY_FILTER = "com.android.settings.password.ConfirmLockPassword$InternalActivity";
    public static final String SETTINGS_CONFIRMLOCKPATTERN_FILTER = "com.android.settings.password.ConfirmLockPattern";
    public static final String SETTINGS_CONFIRMLOCKPATTERN_INTERNALACTIVITY_FILTER = "com.android.settings.password.ConfirmLockPattern$InternalActivity";
    public static final String SETTINGS_CONFIRMNUMBERPRIVACY_FILTER = "com.coloros.settings.privacy.ConfirmNumberPrivacy";
    public static final String SETTINGS_CONFIRMPATTERNPRIVACY_FILTER = "com.coloros.settings.privacy.ConfirmPatternPrivacy";
    public static final String SETTINGS_DEVICEADMINADD_FILTER = "com.android.settings.applications.specialaccess.deviceadmin.DeviceAdminAdd";
    public static final String SETTINGS_FINGERPRINTRESETPASSWORD_FILTER = "com.coloros.settings.feature.fingerprint.FingerprintResetPassword";
    public static final String SETTINGS_POWER_OFFACTIVITY = "com.android.settings.PowerOffActivity";
    public static final String SETTINGS_PUSH_SERVICE = "com.android.settings.PushServiceSetting";
    public static final String SETTINGS_QUESTIONSELECTACTIVITY_FILTER = "com.coloros.settings.privacy.QuestionSelectActivity";
    public static final String SETTINGS_RESETGENERICACTIVITY_FILTER = "com.coloros.settings.feature.password.ResetGenericActivity";
    public static final String SETTINGS_RESETGENERICPRIVACY_FILTER = "com.coloros.settings.privacy.ResetGenericPrivacy";
    public static final String SETTINGS_SAFEQUESTIONACTIVITY_FILTER = "com.coloros.settings.privacy.SafeQuestionActivity";
    public static final String SETTINGS_VPN_SETTING = "com.android.settings.vpn.VpnSettings";
    public static final String SETTING_DEVICEADMINADD_ACTIVITY_FILTER = "com.android.settings.DeviceAdminAdd";
    public static final String SSWO_SSWOACTIVITY_FILTER = "com.ss.android.message.sswo.SswoActivity";
    private static final String TAG = "ColorAccessController";
    public static final String TRANSPARENT_DIDIDACHE_ACTIVITY_FILTER = "com.igexin.sdk.GActivity";
    private static final String TYPE_TAG = "type";
    private static final String VERSION_TAG = "version";
    private Context mContext;
    private HashSet<String> mFilterActivitys = new HashSet<>();
    private HashSet<String> mHideActivitys = new HashSet<>();
    private HashSet<String> mIgnoreAppSwitchActivitys = new HashSet<>();

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(DIR);
        sb.append(APP_PROTECT_RUS_CONFIG_FILE_NAME);
        APP_PROTECT_RUS_CONFIG_FILE_PATH = sb.toString();
    }

    public ColorAccessController(Context context, Looper looper) {
        this.mContext = context;
        initWhiteList();
    }

    private void initWhiteList() {
        this.mFilterActivitys.addAll(FILTER_PROTECT_ACTIVITY_LIST);
        this.mHideActivitys.addAll(HIDE_PROTECT_ACTIVITY_LIST);
        readFilteActivityFromFile();
        readHideActivitysFromFile();
        readIgnoreActivitysFromFile(APP_PROTECT_RUS_CONFIG_FILE_PATH);
    }

    private void readFilteActivityFromFile() {
        ArrayList<String> list = readUpdateData(ROOMUPDATE_FILTER_ATIVITY_FILE_NAME);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (!this.mFilterActivitys.contains(list.get(i))) {
                    this.mFilterActivitys.add(list.get(i));
                }
            }
        }
    }

    private void readHideActivitysFromFile() {
        ArrayList<String> list = readUpdateData(ROOMUPDATE_HIDE_ATIVITY_FILE_NAME);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (!this.mHideActivitys.contains(list.get(i))) {
                    this.mHideActivitys.add(list.get(i));
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0072 A[Catch:{ Exception -> 0x00a0, all -> 0x009e }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0085 A[SYNTHETIC] */
    private void readIgnoreActivitysFromFile(String configFilePath) {
        int type;
        String tagName;
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            Slog.e(TAG, "No config file in path:" + configFilePath);
            return;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(configFile);
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && (tagName = parser.getName()) != null && !tagName.isEmpty()) {
                    char c = 65535;
                    int hashCode = tagName.hashCode();
                    if (hashCode != -1562491215) {
                        if (hashCode == 351608024 && tagName.equals("version")) {
                            c = 0;
                            if (c == 0) {
                                continue;
                            } else if (c != 1) {
                                continue;
                            } else if (Integer.parseInt(parser.getAttributeValue(null, TYPE_TAG)) == 2) {
                                parseSecondStageRUSFile(parser);
                                continue;
                            } else {
                                continue;
                            }
                        }
                    } else if (tagName.equals(APP_PROTECT_TAG)) {
                        c = 1;
                        if (c == 0) {
                        }
                    }
                    if (c == 0) {
                    }
                }
            } while (type != 1);
            try {
                if (parser instanceof Closeable) {
                    ((Closeable) parser).close();
                }
                stream2.close();
            } catch (IOException e) {
                Slog.e(TAG, "Failed to close!");
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 instanceof Closeable) {
                null.close();
            }
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            try {
                if (0 instanceof Closeable) {
                    null.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e3) {
                Slog.e(TAG, "Failed to close!");
                e3.printStackTrace();
            }
            throw th;
        }
    }

    public void parseSecondStageRUSFile(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(ITEM_TAG))) {
                this.mIgnoreAppSwitchActivitys.add(parser.getAttributeValue(null, "name"));
            }
        }
    }

    private ArrayList<String> readUpdateData(String fileName) {
        ArrayList<String> switchList = new ArrayList<>();
        FileReader fileReader = null;
        BufferedReader switchBufferedReader = null;
        try {
            File file = new File(DIR + fileName);
            if (!file.exists()) {
                if (switchBufferedReader != null) {
                    try {
                        switchBufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                return null;
            }
            if (file.exists()) {
                fileReader = new FileReader(file);
                switchBufferedReader = new BufferedReader(fileReader);
                while (true) {
                    String lineSwitchString = switchBufferedReader.readLine();
                    if (TextUtils.isEmpty(lineSwitchString)) {
                        break;
                    }
                    switchList.add(lineSwitchString);
                }
            }
            if (switchBufferedReader != null) {
                try {
                    switchBufferedReader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            return switchList;
        } catch (Exception e5) {
            e5.printStackTrace();
            if (switchBufferedReader != null) {
                try {
                    switchBufferedReader.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (Throwable th) {
            if (switchBufferedReader != null) {
                try {
                    switchBufferedReader.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
                }
            }
            throw th;
        }
    }

    public void updateRusList(int type, List<String> addList, List<String> deleteList) {
        HashSet<String> targetList = null;
        if (type == 0) {
            targetList = this.mFilterActivitys;
        } else if (type == 1) {
            targetList = this.mHideActivitys;
        } else if (type == 2) {
            this.mIgnoreAppSwitchActivitys.clear();
            this.mIgnoreAppSwitchActivitys.addAll(addList);
        }
        if (targetList != null) {
            synchronized (this) {
                if (addList != null) {
                    for (int i = 0; i < addList.size(); i++) {
                        if (!targetList.contains(addList.get(i))) {
                            targetList.add(addList.get(i));
                        }
                    }
                }
                if (deleteList != null) {
                    for (int i2 = 0; i2 < deleteList.size(); i2++) {
                        if (targetList.contains(deleteList.get(i2))) {
                            targetList.remove(deleteList.get(i2));
                        }
                    }
                }
            }
        }
    }

    public boolean isSkipCheckActivity(Intent intent) {
        synchronized (this) {
            if (intent != null) {
                try {
                    ComponentName componentName = intent.getComponent();
                    if (componentName != null) {
                        String packageName = componentName.getPackageName();
                        String activity = componentName.getClassName();
                        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activity)) {
                            boolean contains = this.mFilterActivitys.contains(activity);
                            return contains;
                        }
                    }
                } catch (Throwable e) {
                    Slog.e(TAG, "isSkipCheckActivity error" + e);
                }
            }
        }
        return false;
    }

    public boolean isHideActivity(Intent intent) {
        synchronized (this) {
            if (intent != null) {
                try {
                    ComponentName componentName = intent.getComponent();
                    if (componentName != null) {
                        String packageName = componentName.getPackageName();
                        String activity = componentName.getClassName();
                        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activity)) {
                            boolean contains = this.mHideActivitys.contains(activity);
                            return contains;
                        }
                    }
                } catch (Throwable e) {
                    Slog.e(TAG, "isHideActivity error" + e);
                }
            }
        }
        return false;
    }

    public boolean isIgnoreAppSwitchActivity(Intent intent) {
        synchronized (this) {
            Slog.i(TAG, "enter isIgnoreAppSwitchActivity");
            if (intent != null) {
                try {
                    Slog.i(TAG, "enter isIgnoreAppSwitchActivity and intent != null");
                    ComponentName componentName = intent.getComponent();
                    if (componentName != null) {
                        Slog.i(TAG, "enter isIgnoreAppSwitchActivity and componentName != null");
                        String packageName = componentName.getPackageName();
                        String activity = componentName.getClassName();
                        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activity)) {
                            Slog.i(TAG, "enter isIgnoreAppSwitchActivity and packageName&activity is not Empty");
                            boolean contains = this.mIgnoreAppSwitchActivitys.contains(activity);
                            return contains;
                        }
                    }
                } catch (Throwable e) {
                    Slog.e(TAG, "mIgnoreAppSwitchActivitys error" + e);
                }
            }
        }
        return false;
    }

    public boolean isFilterAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action) || !PROTECT_FILTER_USERCENTER_ACTION.equals(action)) {
            return false;
        }
        String extra = intent.getStringExtra(PROTECT_FILTER_USERCENTER_EXTRA_KEY);
        if (TextUtils.isEmpty(action) || !PROTECT_FILTER_USERCENTER_EXTRA_VALUE.endsWith(extra)) {
            return false;
        }
        return true;
    }

    public void dump(PrintWriter pw) {
        int N = this.mFilterActivitys.size();
        if (N > 0) {
            pw.println("There are " + N + " Filter Activitys");
            Iterator<String> it = this.mFilterActivitys.iterator();
            while (it.hasNext()) {
                pw.print("  ");
                pw.println(it.next());
            }
        }
        int M = this.mHideActivitys.size();
        if (M > 0) {
            pw.println("There are " + M + " Hide Activitys");
            Iterator<String> it2 = this.mHideActivitys.iterator();
            while (it2.hasNext()) {
                pw.print("  ");
                pw.println(it2.next());
            }
        }
    }
}

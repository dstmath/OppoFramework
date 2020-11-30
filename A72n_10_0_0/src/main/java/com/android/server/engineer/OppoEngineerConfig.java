package com.android.server.engineer;

import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.engineer.RomUpdateHelper;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public class OppoEngineerConfig extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "/data/engineermode/engineer_config_list.xml";
    private static final String FILTER_NAME = "sys_engineer_config_list";
    private static final String SYS_FILE_DIR = "/system/etc/engineermode/engineer_config_list.xml";
    private static final String TAG = OppoEngineerConfig.class.getSimpleName();
    private final ISwitchUpdateListener mISwitchUpdateListener;

    public interface ISwitchUpdateListener {
        void onEngineerConfigUpdate();

        void onEngineerConfigUpdateFromProvider();
    }

    /* access modifiers changed from: private */
    public class OppoEningeerConfigUpdateInfo extends RomUpdateHelper.UpdateInfo {
        private static final String ATTR_CLASSNAME = "className";
        private static final String ATTR_ORDER_COMMAND = "order";
        private static final String ATTR_PACKAGE = "packageName";
        private static final String ATTR_SHELL_COMMAND = "command";
        private static final String ATTR_SWITCH_NAME = "switchName";
        private static final String ATTR_SWITCH_STATE = "switchState";
        private static final String SWITCH_ACTIVITY = "activity_sw";
        private static final String SWITCH_ENGINEER_ORDER = "engineer_order_sw";
        private static final String SWITCH_RESET_ATM = "reset_atm_sw";
        private static final String SWITCH_SERVICE = "service_sw";
        private static final String SWITCH_SHELL_COMMAND = "shell_command_sw";
        private final String TAG_ACTIVITY = IColorAppStartupManager.TYPE_ACTIVITY;
        private final String TAG_ENGINEER_ORDER = "engineer_order";
        private final String TAG_SERVICE = IColorAppStartupManager.TYPE_SERVICE;
        private final String TAG_SHELL_COMMAND = "shell_command";
        private final String TAG_SWITCH = "switch";
        private List<ComponentName> mActivityBlackList = new ArrayList();
        private boolean mActivitySwitch;
        private List<String> mEngineerOrderBlackList = new ArrayList();
        private boolean mEngineerOrderSwitch;
        private final ISwitchUpdateListener mISwitchUpdateListener;
        private final Object mLock = new Object();
        private boolean mResetAtmSwitch;
        private List<ComponentName> mServiceBlackList = new ArrayList();
        private boolean mServiceSwitch;
        private List<String> mShellCommandBlackList = new ArrayList();
        private boolean mShellCommandSwitch;

        /* access modifiers changed from: package-private */
        public boolean isActivityInBlackList(ComponentName componentName) {
            if (!this.mActivitySwitch || componentName == null) {
                return false;
            }
            for (ComponentName cp : this.mActivityBlackList) {
                if (OppoEngineerUtils.isComponentEquals(cp, componentName)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean isServiceInBlackList(ComponentName componentName) {
            if (!this.mServiceSwitch || componentName == null) {
                return false;
            }
            for (ComponentName cp : this.mServiceBlackList) {
                if (OppoEngineerUtils.isComponentEquals(cp, componentName)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean isShellCommandInBlackList(String command) {
            if (!this.mShellCommandSwitch || command == null) {
                return false;
            }
            for (String cmd : this.mShellCommandBlackList) {
                if (command.equals(cmd)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean isEngineerOrderInBlackList(String command) {
            if (!this.mEngineerOrderSwitch || command == null) {
                return false;
            }
            for (String cmd : this.mEngineerOrderBlackList) {
                if (command.equals(cmd)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean needResetAtm() {
            return this.mResetAtmSwitch;
        }

        OppoEningeerConfigUpdateInfo(ISwitchUpdateListener switchUpdateListener) {
            super();
            this.mISwitchUpdateListener = switchUpdateListener;
        }

        private void parseComponentName(XmlPullParser parser, List<ComponentName> componentNameList) {
            String packageName = parser.getAttributeValue(null, "packageName");
            String className = parser.getAttributeValue(null, ATTR_CLASSNAME);
            ComponentName item = new ComponentName(packageName, className);
            synchronized (this.mLock) {
                componentNameList.add(item);
            }
            String str = OppoEngineerConfig.TAG;
            Slog.d(str, "parseComponentName, packageName = " + packageName + ", className = " + className);
        }

        private void parseShellCommand(XmlPullParser parser, List<String> commandList) {
            String command = parser.getAttributeValue(null, ATTR_SHELL_COMMAND);
            synchronized (this.mLock) {
                commandList.add(command);
            }
            String str = OppoEngineerConfig.TAG;
            Slog.d(str, "parseShellCommand, command = " + command);
        }

        private void parseEngineerOrder(XmlPullParser parser, List<String> orderList) {
            String order = parser.getAttributeValue(null, ATTR_ORDER_COMMAND);
            synchronized (this.mLock) {
                orderList.add(order);
            }
            String str = OppoEngineerConfig.TAG;
            Slog.d(str, "parseEngineerOrder, order = " + order);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:56:0x0116, code lost:
            if (0 == 0) goto L_0x0119;
         */
        @Override // com.android.server.engineer.RomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            ISwitchUpdateListener iSwitchUpdateListener;
            if (content != null) {
                StringReader strReader = null;
                boolean updated = false;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    strReader = new StringReader(content);
                    parser.setInput(strReader);
                    synchronized (this.mLock) {
                        this.mActivityBlackList.clear();
                        this.mServiceBlackList.clear();
                    }
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0 && eventType == 2) {
                            String tmp = parser.getName();
                            if ("switch".equals(tmp)) {
                                String switchName = parser.getAttributeValue(null, ATTR_SWITCH_NAME);
                                String switchState = parser.getAttributeValue(null, ATTR_SWITCH_STATE);
                                if (!TextUtils.isEmpty(switchName) && !TextUtils.isEmpty(switchState)) {
                                    Slog.d(OppoEngineerConfig.TAG, String.format(Locale.US, "parse switch %s = %s", switchName, switchState));
                                    if (SWITCH_ACTIVITY.equals(switchName)) {
                                        this.mActivitySwitch = Boolean.parseBoolean(switchState);
                                    } else if (SWITCH_SERVICE.equals(switchName)) {
                                        this.mServiceSwitch = Boolean.parseBoolean(switchState);
                                    } else if (SWITCH_SHELL_COMMAND.equals(switchName)) {
                                        this.mShellCommandSwitch = Boolean.parseBoolean(switchState);
                                    } else if (SWITCH_ENGINEER_ORDER.equals(switchName)) {
                                        this.mEngineerOrderSwitch = Boolean.parseBoolean(switchState);
                                    } else if (SWITCH_RESET_ATM.equals(switchName)) {
                                        this.mResetAtmSwitch = Boolean.parseBoolean(switchState);
                                    }
                                }
                            } else if (IColorAppStartupManager.TYPE_ACTIVITY.equals(tmp)) {
                                parseComponentName(parser, this.mActivityBlackList);
                                updated = true;
                            } else if (IColorAppStartupManager.TYPE_SERVICE.equals(tmp)) {
                                parseComponentName(parser, this.mServiceBlackList);
                                updated = true;
                            } else if ("shell_command".equals(tmp)) {
                                parseShellCommand(parser, this.mShellCommandBlackList);
                                updated = true;
                            } else if ("engineer_order".equals(tmp)) {
                                parseEngineerOrder(parser, this.mEngineerOrderBlackList);
                                updated = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    Slog.e(OppoEngineerConfig.TAG, "Got execption parsing permissions.", e);
                } catch (Throwable th) {
                    if (0 != 0) {
                        strReader.close();
                    }
                    throw th;
                }
                strReader.close();
                if (updated && (iSwitchUpdateListener = this.mISwitchUpdateListener) != null) {
                    iSwitchUpdateListener.onEngineerConfigUpdate();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isActivityInBlackList(ComponentName componentName) {
        return ((OppoEningeerConfigUpdateInfo) getUpdateInfo(true)).isActivityInBlackList(componentName);
    }

    /* access modifiers changed from: package-private */
    public boolean isServiceInBlackList(ComponentName componentName) {
        return ((OppoEningeerConfigUpdateInfo) getUpdateInfo(true)).isServiceInBlackList(componentName);
    }

    /* access modifiers changed from: package-private */
    public boolean isShellCommandInBlackList(String command) {
        return ((OppoEningeerConfigUpdateInfo) getUpdateInfo(true)).isShellCommandInBlackList(command);
    }

    /* access modifiers changed from: package-private */
    public boolean isEngineerOrderInBlackList(String order) {
        return ((OppoEningeerConfigUpdateInfo) getUpdateInfo(true)).isEngineerOrderInBlackList(order);
    }

    /* access modifiers changed from: package-private */
    public boolean needResetAtm() {
        return ((OppoEningeerConfigUpdateInfo) getUpdateInfo(true)).needResetAtm();
    }

    OppoEngineerConfig(Context context, ISwitchUpdateListener switchUpdateListener) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new OppoEningeerConfigUpdateInfo(switchUpdateListener), new OppoEningeerConfigUpdateInfo(switchUpdateListener));
        this.mISwitchUpdateListener = switchUpdateListener;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.android.server.engineer.RomUpdateHelper
    public void getUpdateFromProvider() {
        super.getUpdateFromProvider();
        ISwitchUpdateListener iSwitchUpdateListener = this.mISwitchUpdateListener;
        if (iSwitchUpdateListener != null) {
            iSwitchUpdateListener.onEngineerConfigUpdateFromProvider();
        }
        Slog.v(TAG, "update SecrecySwitchHelper config");
    }
}

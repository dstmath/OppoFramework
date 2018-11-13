package com.color.sau;

import com.color.widget.ColorSAUAlertDialog;
import com.color.widget.ColorSAUAlertDialog.OnButtonClickListener;

public class SauUpgradeAlertDialogFunc {
    private static ISauUpgradeDialog sListener;

    public static void setDialogListener(ISauUpgradeDialog listener) {
        sListener = listener;
    }

    public static void processDialogFunc(ColorSAUAlertDialog mUpgradeDialog, String packageName, boolean forceUpgrade, boolean downloaded, boolean wifiConnected, String verName, String size, String description) {
        if (mUpgradeDialog != null) {
            if (forceUpgrade) {
                mUpgradeDialog.setCancelable(false);
            } else {
                mUpgradeDialog.setCancelable(true);
            }
            if (downloaded) {
                if (forceUpgrade) {
                    mUpgradeDialog.setButtonType(7);
                    mUpgradeDialog.setOnButtonClickListener(new OnButtonClickListener() {
                        public void onClick(int whichButton) {
                            switch (whichButton) {
                                case -2:
                                    SauUpgradeAlertDialogFunc.sListener.exitUpgrade();
                                    return;
                                case -1:
                                    SauUpgradeAlertDialogFunc.sListener.installNow();
                                    return;
                                default:
                                    return;
                            }
                        }
                    });
                } else {
                    mUpgradeDialog.setButtonType(6);
                    mUpgradeDialog.setOnButtonClickListener(new OnButtonClickListener() {
                        public void onClick(int whichButton) {
                            switch (whichButton) {
                                case -2:
                                    SauUpgradeAlertDialogFunc.sListener.upgradeLater();
                                    return;
                                case -1:
                                    SauUpgradeAlertDialogFunc.sListener.installNow();
                                    return;
                                default:
                                    return;
                            }
                        }
                    });
                }
            } else if (forceUpgrade) {
                mUpgradeDialog.setButtonType(9);
                mUpgradeDialog.setOnButtonClickListener(new OnButtonClickListener() {
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case -2:
                                SauUpgradeAlertDialogFunc.sListener.exitUpgrade();
                                return;
                            case -1:
                                SauUpgradeAlertDialogFunc.sListener.upgradeNow();
                                return;
                            default:
                                return;
                        }
                    }
                });
            } else {
                mUpgradeDialog.setButtonType(8);
                mUpgradeDialog.setOnButtonClickListener(new OnButtonClickListener() {
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case -2:
                                SauUpgradeAlertDialogFunc.sListener.upgradeLater();
                                return;
                            case -1:
                                SauUpgradeAlertDialogFunc.sListener.upgradeNow();
                                return;
                            default:
                                return;
                        }
                    }
                });
            }
            if (downloaded) {
                mUpgradeDialog.setNetworkPrompt(2);
            } else if (wifiConnected) {
                mUpgradeDialog.setNetworkPrompt(0);
            } else {
                mUpgradeDialog.setNetworkPrompt(1);
            }
            mUpgradeDialog.setVersionName(verName);
            mUpgradeDialog.setSizeStr(size);
            mUpgradeDialog.setUpdateDescription(description);
        }
    }
}

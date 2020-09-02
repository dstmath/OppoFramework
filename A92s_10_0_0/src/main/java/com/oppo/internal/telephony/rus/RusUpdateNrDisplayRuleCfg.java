package com.oppo.internal.telephony.rus;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.oppo.internal.telephony.explock.RegionLockConstant;
import java.util.HashMap;

public final class RusUpdateNrDisplayRuleCfg extends RusBase {
    private static final int EVENT_DISPLAY_RULE_DONE = 110;
    private static final String TAG = "RusUpdateNrDisplayRuleCfg";
    private final int CONFIG_LEN_DEFAULT = 3;
    private final int MAX_SLEEP_COUNT = 10;
    String[] mCfgData = new String[3];
    String[] mConfigParaNameArray = {"nr_display_rule", "lte_con_stat_delay_timer", "scg_con_stat_delay_timer"};
    /* access modifiers changed from: private */
    public Handler mHandler;

    public RusUpdateNrDisplayRuleCfg() {
        createHandler();
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("nr_display_rule")) {
            rusData.get("nr_display_rule");
            for (int index = 0; index < 3; index++) {
                String[] strArr = this.mConfigParaNameArray;
                if (index >= strArr.length) {
                    break;
                }
                String str = rusData.get(strArr[index]);
                if (str != null) {
                    this.mCfgData[index] = str;
                }
            }
            String[] strArr2 = this.mCfgData;
            if (strArr2 != null && strArr2.length == 3) {
                printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",nr_display_rule:" + this.mCfgData[0] + ",lte_con_stat_delay_timer:" + this.mCfgData[1] + ",scg_con_stat_delay_timer:" + this.mCfgData[2]);
                if ("3".equals(this.mCfgData[0]) || RegionLockConstant.TEST_OP_CUANDCMCC.equals(this.mCfgData[0]) || "1".equals(this.mCfgData[0]) || "0".equals(this.mCfgData[0])) {
                    String updateCmd = "UPDATE_DISPLAY_RULE, " + this.mCfgData[0] + ", " + this.mCfgData[1] + ", " + this.mCfgData[2];
                    printLog(TAG, "updateCmd = " + updateCmd);
                    sendDisplayRule(updateCmd);
                    printLog(TAG, "update display rule!");
                }
            }
        }
    }

    private void sendDisplayRule(String cmdLine) {
        try {
            byte[] rawData = cmdLine.getBytes();
            byte[] cmdByte = new byte[(rawData.length + 1)];
            System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
            cmdByte[cmdByte.length - 1] = 0;
            for (int i = 0; i < 10 && this.mHandler == null; i++) {
                Thread.sleep(5);
                printLog(TAG, "thread sleep count = " + i);
            }
            this.mPhone.invokeOemRilRequestRaw(cmdByte, this.mHandler.obtainMessage(110));
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void createHandler() {
        printLog(TAG, "enter createHandler fuction!");
        if (this.mHandler == null) {
            new Thread(new Runnable() {
                /* class com.oppo.internal.telephony.rus.RusUpdateNrDisplayRuleCfg.AnonymousClass1 */

                public void run() {
                    try {
                        Looper.prepare();
                        final Looper looper = Looper.myLooper();
                        RusUpdateNrDisplayRuleCfg.this.printLog(RusUpdateNrDisplayRuleCfg.TAG, "create handler now!");
                        Handler unused = RusUpdateNrDisplayRuleCfg.this.mHandler = new Handler() {
                            /* class com.oppo.internal.telephony.rus.RusUpdateNrDisplayRuleCfg.AnonymousClass1.AnonymousClass1 */

                            public void handleMessage(Message msg) {
                                AsyncResult asyncResult = (AsyncResult) msg.obj;
                                if (msg.what == 110) {
                                    if (asyncResult == null || asyncResult.exception != null || asyncResult.result == null) {
                                        RusUpdateNrDisplayRuleCfg.this.printLog(RusUpdateNrDisplayRuleCfg.TAG, "EVENT_DISPLAY_RULE_DONE failed.");
                                    } else {
                                        RusUpdateNrDisplayRuleCfg rusUpdateNrDisplayRuleCfg = RusUpdateNrDisplayRuleCfg.this;
                                        rusUpdateNrDisplayRuleCfg.printLog(RusUpdateNrDisplayRuleCfg.TAG, "EVENT_DISPLAY_RULE_DONE succeed,result = " + ((int) ((byte[]) asyncResult.result)[0]));
                                    }
                                }
                                looper.quitSafely();
                                RusUpdateNrDisplayRuleCfg.this.printLog(RusUpdateNrDisplayRuleCfg.TAG, "looper quit!");
                            }
                        };
                        RusUpdateNrDisplayRuleCfg.this.printLog(RusUpdateNrDisplayRuleCfg.TAG, "already create handler !");
                        Looper.loop();
                    } catch (Exception e) {
                        e.printStackTrace();
                        RusUpdateNrDisplayRuleCfg.this.printLog(RusUpdateNrDisplayRuleCfg.TAG, "RusUpdateNrDisplayRuleCfg exception.");
                    }
                }
            }).start();
        }
    }
}

package com.oppo.internal.telephony.rus;

import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import java.util.HashMap;

public final class RusUpdateMdLog extends RusBase {
    private static final int MD_DUMP_TYPE_CLOSE = 0;
    private static final int MD_DUMP_TYPE_MINI = 1;
    private static final int MD_DUMP_TYPE_SSR = 2;
    private static final String MTKLOGGER_ADB_CMD = "com.mediatek.mtklogger.ADB_CMD";
    private static final String PERSIST_SYS_MDLOG_DUMPBACK = "persist.sys.mdlog_dumpback";
    private static final String TAG = "RusUpdateMdLog";

    /* access modifiers changed from: protected */
    @Override // com.oppo.internal.telephony.rus.RusBase
    public void executeRusCommand(HashMap<String, String> rusData, boolean isReboot) {
        if (rusData == null) {
            printLog(TAG, "defaultValue is null ");
        } else if (rusData.containsKey("dump_pb")) {
            String value = rusData.get("dump_pb");
            printLog(TAG, "executeRusCommand() isReboot:" + isReboot + ",dump_pb:" + value);
            setcmdMdLog(value);
        }
    }

    private void setcmdMdLog(String strdump_pb) {
        try {
            int dump_pb = Integer.parseInt(strdump_pb);
            if (dump_pb == 2 || dump_pb == 0 || dump_pb == 1) {
                int oldValue = SystemProperties.getInt(PERSIST_SYS_MDLOG_DUMPBACK, -1);
                Log.d(TAG, "oldValue:" + oldValue + ",dump_pb:" + dump_pb);
                if (oldValue != dump_pb) {
                    SystemProperties.set(PERSIST_SYS_MDLOG_DUMPBACK, "" + dump_pb);
                    Intent intent1 = new Intent(MTKLOGGER_ADB_CMD);
                    if (dump_pb > 0) {
                        intent1.putExtra("cmd_name", "SSR_start,1");
                    } else {
                        intent1.putExtra("cmd_name", "SSR_stop,1");
                    }
                    intent1.putExtra("cmd_target", 2);
                    this.mPhone.getContext().sendBroadcast(intent1);
                }
            }
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }
}

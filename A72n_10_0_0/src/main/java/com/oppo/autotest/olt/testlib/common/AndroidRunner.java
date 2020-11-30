package com.oppo.autotest.olt.testlib.common;

import android.os.Bundle;
import androidx.test.runner.AndroidJUnitRunner;
import com.oppo.autotest.olt.testlib.utils.ConfigUtils;
import com.oppo.autotest.olt.testlib.utils.LogUtils;

public class AndroidRunner extends AndroidJUnitRunner {
    @Override // androidx.test.runner.MonitoringInstrumentation, androidx.test.runner.AndroidJUnitRunner
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        LogUtils.logInfo("create for android api tests");
        BasicData.setTestCaseName(arguments.getString("test_case_name"));
        BasicData.setReportTime(arguments.getString("report_time"));
        ConfigUtils.parser(BasicData.CONFIG_PATH + arguments.getString("test_config_name"));
    }
}

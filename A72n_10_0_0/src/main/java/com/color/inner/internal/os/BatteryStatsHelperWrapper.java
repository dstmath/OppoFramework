package com.color.inner.internal.os;

import android.content.Context;
import android.os.Bundle;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import java.util.ArrayList;
import java.util.List;

public class BatteryStatsHelperWrapper {
    private BatteryStatsHelper mHelper;

    public BatteryStatsHelperWrapper(Context context) {
        this.mHelper = new BatteryStatsHelper(context);
    }

    public static String makemAh(double val) {
        return BatteryStatsHelper.makemAh(val);
    }

    public void create(Bundle bundle) {
        this.mHelper.create(bundle);
    }

    public void refreshStats(int tag, int userId) {
        this.mHelper.refreshStats(tag, userId);
    }

    public List<BatterySipperWrapper> getUsageList() {
        List<BatterySipper> origin = this.mHelper.getUsageList();
        List<BatterySipperWrapper> list = new ArrayList<>();
        if (origin != null) {
            for (BatterySipper sipper : origin) {
                list.add(new BatterySipperWrapper(sipper));
            }
        }
        return list;
    }
}

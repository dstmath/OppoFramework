package com.mediatek.apm.test.service;

import android.content.Context;
import com.android.server.SystemService;
import com.mediatek.apm.frc.FocusRelationshipChainPolicy;
import com.mediatek.apm.suppression.SuppressionPolicy;
import com.mediatek.apm.test.service.ITestAmPolicyMakerService.Stub;
import java.util.List;

public class TestAmPolicyMakerService extends Stub {
    private FocusRelationshipChainPolicy B = FocusRelationshipChainPolicy.getInstance();
    private SuppressionPolicy C = SuppressionPolicy.getInstance();
    private final String TAG = "TestAmPolicyMakerService";

    public static final class LifeCycle extends SystemService {
        private final TestAmPolicyMakerService D = new TestAmPolicyMakerService();

        public LifeCycle(Context context) {
            super(context);
        }

        public void onStart() {
            publishBinderService("TestAmPolicyMakerService", this.D);
        }
    }

    public void startFrc(String str, int i, List<String> list) {
        this.B.startFrc(str, i, list);
    }

    public void stopFrc(String str) {
        this.B.stopFrc(str);
    }

    public List<String> getFrcPackageList(String str) {
        return this.B.getFrcPackageList(str);
    }

    public void updateFrcExtraAllowList(String str, List<String> list) {
        this.B.updateFrcExtraAllowList(str, list);
    }

    public void startSuppression(String str, int i, int i2, String str2, List<String> list) {
        this.C.startSuppression(str, i, i2, str2, list);
    }

    public void stopSuppression(String str) {
        this.C.stopSuppression(str);
    }

    public void updateSuppressionExtraAllowList(String str, List<String> list) {
        this.C.updateExtraAllowList(str, list);
    }

    public List<String> getSuppressionList() {
        return this.C.getSuppressionList();
    }

    public boolean isPackageInSuppression(String str, String str2, int i) {
        return this.C.isPackageInSuppression(str, str2, i);
    }
}

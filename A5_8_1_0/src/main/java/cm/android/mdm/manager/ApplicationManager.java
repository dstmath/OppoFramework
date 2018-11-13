package cm.android.mdm.manager;

import android.app.ActivityManager;
import android.content.Context;
import cm.android.mdm.interfaces.IApplicationManager;
import cm.android.mdm.util.CustomizeServiceManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class ApplicationManager implements IApplicationManager {
    private static final String TAG = "ApplicationManager";
    private ActivityManager mAm = ((ActivityManager) this.mContext.getSystemService("activity"));
    private Context mContext;

    public ApplicationManager(Context context) {
        this.mContext = context;
    }

    public void addPersistentApp(List<String> packageNames) {
        if (packageNames != null && packageNames.size() > 0) {
            for (String packageName : packageNames) {
                CustomizeServiceManager.addProtectApplication(packageName);
            }
        }
    }

    public void removePersistentApp(List<String> packageNames) {
        if (packageNames != null && packageNames.size() > 0) {
            for (String packageName : packageNames) {
                CustomizeServiceManager.removeProtectApplication(packageName);
            }
        }
    }

    public List<String> getPersistentApp() {
        return CustomizeServiceManager.getProtectApplicationList();
    }

    public void addDisallowedRunningApp(List<String> packageNames) {
        if (this.mAm != null) {
            this.mAm.addDisallowedRunningApp(packageNames);
        }
    }

    public void removeDisallowedRunningApp(List<String> packageNames) {
        if (this.mAm != null) {
            this.mAm.removeDisallowedRunningApp(packageNames);
        }
    }

    public void removeDisallowedRunningApp() {
        if (this.mAm != null) {
            List<String> currentList = this.mAm.getDisallowedRunningApp();
            if (currentList != null) {
                this.mAm.removeDisallowedRunningApp(currentList);
            }
        }
    }

    public List<String> getDisallowedRunningApp() {
        if (this.mAm != null) {
            return this.mAm.getDisallowedRunningApp();
        }
        return null;
    }

    public void killProcess(String packageName) {
        CustomizeServiceManager.killProcess(packageName);
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(ApplicationManager.class);
    }
}

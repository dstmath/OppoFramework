package cm.android.mdm.interfaces;

import java.util.List;

public interface IApplicationManager {
    void addDisallowedRunningApp(List<String> list);

    void addPersistentApp(List<String> list);

    List<String> getDisallowedRunningApp();

    List<String> getPersistentApp();

    List<String> getSupportMethods();

    void killProcess(String str);

    void removeDisallowedRunningApp();

    void removeDisallowedRunningApp(List<String> list);

    void removePersistentApp(List<String> list);
}

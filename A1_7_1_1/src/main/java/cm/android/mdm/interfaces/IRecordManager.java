package cm.android.mdm.interfaces;

import java.util.List;

public interface IRecordManager {
    List<String> getSupportMethods();

    void startRecord(String str);

    void startRecordPolicy(String str);

    void stopRecord();

    void stopRecordPolicy();
}

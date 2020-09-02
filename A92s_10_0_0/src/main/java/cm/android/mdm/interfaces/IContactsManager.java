package cm.android.mdm.interfaces;

import android.content.ComponentName;
import java.util.List;

public interface IContactsManager {
    int addMdmNumberList(ComponentName componentName, List<String> list, int i);

    int getMdmBlockPattern(ComponentName componentName);

    int getMdmMatchPattern(ComponentName componentName);

    List<String> getMdmNumberList(ComponentName componentName, int i);

    int getMdmOutgoOrIncomePattern(ComponentName componentName);

    boolean removeMdmAllNumber(ComponentName componentName, int i);

    int removeMdmNumberList(ComponentName componentName, List<String> list, int i);

    boolean setMdmBlockPattern(ComponentName componentName, int i);

    boolean setMdmMatchPattern(ComponentName componentName, int i);

    boolean setMdmOutgoOrIncomePattern(ComponentName componentName, int i);
}

package cm.android.mdm.interfaces;

import java.util.List;

public interface IContactsManager {
    void addContactsRestriction(int i, List<String> list);

    List<String> getSupportMethods();

    void removeContactsRestriction(int i);

    void removeContactsRestriction(int i, List<String> list);

    void setContactsRestriction(int i);
}

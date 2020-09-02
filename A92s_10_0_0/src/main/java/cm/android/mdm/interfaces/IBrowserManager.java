package cm.android.mdm.interfaces;

import android.provider.BaseColumns;
import java.util.List;

public interface IBrowserManager {

    public static class BrowserColumns implements BaseColumns {
        public static final String CREATED = "created";
        public static final String DATE = "date";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String VISITS = "visits";
    }

    void addBrowserRestriction(int i, List<String> list);

    List<String> getSupportMethods();

    void removeBrowserRestriction(int i);

    void removeBrowserRestriction(int i, List<String> list);

    void setBrowserRestriction(int i);
}

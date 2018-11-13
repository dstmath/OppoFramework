package android.view;

public interface IColorWindow extends IColorLongshotWindow {
    public static final int COLOR_CALL_TRANSACTION_INDEX = 10000;
    public static final int COLOR_FIRST_CALL_TRANSACTION = 10001;
    public static final String DESCRIPTOR = "android.view.IWindow";
    public static final int GET_LONGSHOT_VIEW_INFO = 10002;
    public static final int GET_LONGSHOT_VIEW_INFO_ASYNC = 10009;
    public static final int LONGSHOT_ANALYSIS_VIEW = 10003;
    public static final int LONGSHOT_COLLECT_ROOT = 10004;
    public static final int LONGSHOT_COLLECT_WINDOW = 10011;
    public static final int LONGSHOT_DUMP = 10010;
    public static final int LONGSHOT_END_SCROLL = 10006;
    public static final int LONGSHOT_INJECT_SCROLL = 10005;
    public static final int LONGSHOT_NOTIFY_CONNECTED = 10007;
    public static final int LONGSHOT_SET_SCROLL_MODE = 10008;
}

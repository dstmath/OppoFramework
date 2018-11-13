package com.android.server;

import android.annotation.IntDef;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.OnHardKeyboardStatusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnectionInspector;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManagerInternal;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSubtype.InputMethodSubtypeBuilder;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController.ImeSubtypeListItem;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager.Stub;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputBindResult;
import com.android.internal.view.InputMethodClient;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.lights.OppoLightsService.ButtonLight;
import com.android.server.oppo.IElsaManager;
import com.android.server.statusbar.StatusBarManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class InputMethodManagerService extends Stub implements ServiceConnection, Callback {
    static boolean DEBUG = false;
    static boolean DEBUG_IME_ACTIVE = false;
    static boolean DEBUG_IME_ACTIVE_LIGHT = false;
    static boolean DEBUG_RESTORE = false;
    static boolean DEBUG_SHOW_SOFTINPUT = false;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_AUTO_SWITCH_IME = 3070;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HARD_KEYBOARD_SWITCH_CHANGED = 4000;
    static final int MSG_HIDE_CURRENT_INPUT_METHOD = 1035;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_RECONNECT_TIMEOUT = 3060;
    static final int MSG_RESTART_INPUT = 2010;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 3040;
    static final int MSG_SHOW_IM_CONFIG = 3;
    static final int MSG_SHOW_IM_SUBTYPE_ENABLER = 2;
    static final int MSG_SHOW_IM_SUBTYPE_PICKER = 1;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_SWITCH_IME = 3050;
    static final int MSG_SYSTEM_UNLOCK_USER = 5000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    private static final String PROP_IMELOG = "persist.sys.assert.imelog";
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    static final String TAG = "InputMethodManagerService";
    private static final String TAG_TRY_SUPPRESSING_IME_SWITCHER = "TrySuppressingImeSwitcher";
    static final long TIME_TO_RECONNECT = 3000;
    private int index1;
    private int index2;
    private boolean mAccessibilityRequestingNoSoftKeyboard;
    private final AppOpsManager mAppOpsManager;
    private String[] mArgs;
    int mBackDisposition;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients;
    final Context mContext;
    EditorInfo mCurAttribute;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurUserActionNotificationSequenceNumber;
    private InputMethodSubtype mCurrentSubtype;
    private Builder mDialogBuilder;
    SessionState mEnabledSession;
    private InputMethodFileManager mFileManager;
    final Handler mHandler;
    private final int mHardKeyboardBehavior;
    private final HardKeyboardListener mHardKeyboardListener;
    final boolean mHasFeature;
    boolean mHaveConnection;
    private final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    private final boolean mImeSelectedOnBoot;
    private PendingIntent mImeSwitchPendingIntent;
    private Notification.Builder mImeSwitcherNotification;
    int mImeWindowVis;
    private InputMethodInfo[] mIms;
    boolean mInputShown;
    boolean mIsInteractive;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList;
    final HashMap<String, InputMethodInfo> mMethodMap;
    private final MyPackageMonitor mMyPackageMonitor;
    private int mNextArg;
    final InputBindResult mNoBinding;
    private NotificationManager mNotificationManager;
    private boolean mNotificationShown;
    private ReconnectWatchDog mReconnectWatchDog;
    final Resources mRes;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans;
    final InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    boolean mShowForcedFromKey;
    private boolean mShowImeWithHardKeyboard;
    private boolean mShowOngoingImeSwitcherForPhones;
    boolean mShowRequested;
    private final String mSlotIme;
    private StatusBarManagerService mStatusBar;
    private int[] mSubtypeIds;
    private Toast mSubtypeSwitchedByShortCutToast;
    private final InputMethodSubtypeSwitchingController mSwitchingController;
    private AlertDialog mSwitchingDialog;
    private View mSwitchingDialogTitleView;
    boolean mSystemReady;
    private Timer mTimer;
    private final UserManager mUserManager;
    boolean mVisibleBound;
    final ServiceConnection mVisibleConnection;
    final WindowManagerInternal mWindowManagerInternal;

    static final class ClientState {
        final InputBinding binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        final IInputMethodClient client;
        SessionState curSession;
        final IInputContext inputContext;
        final int pid;
        boolean sessionRequested;
        final int uid;

        public String toString() {
            return "ClientState{" + Integer.toHexString(System.identityHashCode(this)) + " uid " + this.uid + " pid " + this.pid + "}";
        }

        ClientState(IInputMethodClient _client, IInputContext _inputContext, int _uid, int _pid) {
            this.client = _client;
            this.inputContext = _inputContext;
            this.uid = _uid;
            this.pid = _pid;
        }
    }

    @IntDef({0, 1})
    @Retention(RetentionPolicy.SOURCE)
    private @interface HardKeyboardBehavior {
        public static final int WIRED_AFFORDANCE = 1;
        public static final int WIRELESS_AFFORDANCE = 0;
    }

    private class HardKeyboardListener implements OnHardKeyboardStatusChangeListener {
        /* synthetic */ HardKeyboardListener(InputMethodManagerService this$0, HardKeyboardListener hardKeyboardListener) {
            this();
        }

        private HardKeyboardListener() {
        }

        public void onHardKeyboardStatusChange(boolean available) {
            InputMethodManagerService.this.mHandler.sendMessage(InputMethodManagerService.this.mHandler.obtainMessage(InputMethodManagerService.MSG_HARD_KEYBOARD_SWITCH_CHANGED, Integer.valueOf(available ? 1 : 0)));
        }

        public void handleHardKeyboardStatusChange(boolean available) {
            if (InputMethodManagerService.DEBUG) {
                Slog.w(InputMethodManagerService.TAG, "HardKeyboardStatusChanged: available=" + available);
            }
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (!(InputMethodManagerService.this.mSwitchingDialog == null || InputMethodManagerService.this.mSwitchingDialogTitleView == null || !InputMethodManagerService.this.mSwitchingDialog.isShowing())) {
                    InputMethodManagerService.this.mSwitchingDialogTitleView.findViewById(16909194).setVisibility(available ? 0 : 8);
                }
            }
        }
    }

    private static class ImeSubtypeListAdapter extends ArrayAdapter<ImeSubtypeListItem> {
        public int mCheckedItem;
        private final LayoutInflater mInflater;
        private final List<ImeSubtypeListItem> mItemsList;
        private final int mTextViewResourceId;

        public ImeSubtypeListAdapter(Context context, int textViewResourceId, List<ImeSubtypeListItem> itemsList, int checkedItem) {
            super(context, textViewResourceId, itemsList);
            this.mTextViewResourceId = textViewResourceId;
            this.mItemsList = itemsList;
            this.mCheckedItem = checkedItem;
            this.mInflater = (LayoutInflater) context.getSystemService(LayoutInflater.class);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            boolean z = false;
            if (convertView != null) {
                view = convertView;
            } else {
                view = this.mInflater.inflate(this.mTextViewResourceId, null);
            }
            if (position < 0 || position >= this.mItemsList.size()) {
                return view;
            }
            ImeSubtypeListItem item = (ImeSubtypeListItem) this.mItemsList.get(position);
            CharSequence imeName = item.mImeName;
            CharSequence subtypeName = item.mSubtypeName;
            TextView firstTextView = (TextView) view.findViewById(16908308);
            TextView secondTextView = (TextView) view.findViewById(16908309);
            if (TextUtils.isEmpty(subtypeName)) {
                firstTextView.setText(imeName);
                secondTextView.setVisibility(8);
            } else {
                firstTextView.setText(subtypeName);
                secondTextView.setText(imeName);
                secondTextView.setVisibility(0);
            }
            RadioButton radioButton = (RadioButton) view.findViewById(16909196);
            if (position == this.mCheckedItem) {
                z = true;
            }
            radioButton.setChecked(z);
            return view;
        }
    }

    class ImmsBroadcastReceiver extends BroadcastReceiver {
        ImmsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                InputMethodManagerService.this.hideInputMethodMenu();
            } else if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                InputMethodManagerService.this.updateCurrentProfileIds();
            } else {
                if ("android.os.action.SETTING_RESTORED".equals(action)) {
                    if ("enabled_input_methods".equals(intent.getStringExtra("setting_name"))) {
                        InputMethodManagerService.restoreEnabledInputMethods(InputMethodManagerService.this.mContext, intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"));
                    }
                } else if (!"android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                    Slog.w(InputMethodManagerService.TAG, "Unexpected intent " + intent);
                } else if (InputMethodManagerService.this.mInputShown) {
                    InputMethodManagerService.this.hideCurrentInputLocked(0, null);
                    Slog.i(InputMethodManagerService.TAG, "IPO shutdown");
                }
            }
        }
    }

    private static class InputMethodFileManager {
        private static final String ADDITIONAL_SUBTYPES_FILE_NAME = "subtypes.xml";
        private static final String ATTR_ICON = "icon";
        private static final String ATTR_ID = "id";
        private static final String ATTR_IME_SUBTYPE_EXTRA_VALUE = "imeSubtypeExtraValue";
        private static final String ATTR_IME_SUBTYPE_ID = "subtypeId";
        private static final String ATTR_IME_SUBTYPE_LANGUAGE_TAG = "languageTag";
        private static final String ATTR_IME_SUBTYPE_LOCALE = "imeSubtypeLocale";
        private static final String ATTR_IME_SUBTYPE_MODE = "imeSubtypeMode";
        private static final String ATTR_IS_ASCII_CAPABLE = "isAsciiCapable";
        private static final String ATTR_IS_AUXILIARY = "isAuxiliary";
        private static final String ATTR_LABEL = "label";
        private static final String INPUT_METHOD_PATH = "inputmethod";
        private static final String NODE_IMI = "imi";
        private static final String NODE_SUBTYPE = "subtype";
        private static final String NODE_SUBTYPES = "subtypes";
        private static final String SYSTEM_PATH = "system";
        private final AtomicFile mAdditionalInputMethodSubtypeFile;
        private final HashMap<String, List<InputMethodSubtype>> mAdditionalSubtypesMap = new HashMap();
        private final HashMap<String, InputMethodInfo> mMethodMap;

        public InputMethodFileManager(HashMap<String, InputMethodInfo> methodMap, int userId) {
            if (methodMap == null) {
                throw new NullPointerException("methodMap is null");
            }
            File systemDir;
            this.mMethodMap = methodMap;
            if (userId == 0) {
                systemDir = new File(Environment.getDataDirectory(), SYSTEM_PATH);
            } else {
                systemDir = Environment.getUserSystemDirectory(userId);
            }
            File inputMethodDir = new File(systemDir, INPUT_METHOD_PATH);
            if (!(inputMethodDir.exists() || inputMethodDir.mkdirs())) {
                Slog.w(InputMethodManagerService.TAG, "Couldn't create dir.: " + inputMethodDir.getAbsolutePath());
            }
            File subtypeFile = new File(inputMethodDir, ADDITIONAL_SUBTYPES_FILE_NAME);
            this.mAdditionalInputMethodSubtypeFile = new AtomicFile(subtypeFile);
            if (subtypeFile.exists()) {
                readAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile);
            } else {
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, methodMap);
            }
        }

        private void deleteAllInputMethodSubtypes(String imiId) {
            synchronized (this.mMethodMap) {
                this.mAdditionalSubtypesMap.remove(imiId);
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, this.mMethodMap);
            }
        }

        public void addInputMethodSubtypes(InputMethodInfo imi, InputMethodSubtype[] additionalSubtypes) {
            synchronized (this.mMethodMap) {
                ArrayList<InputMethodSubtype> subtypes = new ArrayList();
                for (InputMethodSubtype subtype : additionalSubtypes) {
                    if (subtypes.contains(subtype)) {
                        Slog.w(InputMethodManagerService.TAG, "Duplicated subtype definition found: " + subtype.getLocale() + ", " + subtype.getMode());
                    } else {
                        subtypes.add(subtype);
                    }
                }
                this.mAdditionalSubtypesMap.put(imi.getId(), subtypes);
                writeAdditionalInputMethodSubtypes(this.mAdditionalSubtypesMap, this.mAdditionalInputMethodSubtypeFile, this.mMethodMap);
            }
        }

        public HashMap<String, List<InputMethodSubtype>> getAllAdditionalInputMethodSubtypes() {
            HashMap<String, List<InputMethodSubtype>> hashMap;
            synchronized (this.mMethodMap) {
                hashMap = this.mAdditionalSubtypesMap;
            }
            return hashMap;
        }

        private static void writeAdditionalInputMethodSubtypes(HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile, HashMap<String, InputMethodInfo> methodMap) {
            boolean isSetMethodMap = methodMap != null && methodMap.size() > 0;
            FileOutputStream fos = null;
            try {
                fos = subtypesFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, NODE_SUBTYPES);
                for (String imiId : allSubtypes.keySet()) {
                    if (!isSetMethodMap || methodMap.containsKey(imiId)) {
                        out.startTag(null, NODE_IMI);
                        out.attribute(null, "id", imiId);
                        List<InputMethodSubtype> subtypesList = (List) allSubtypes.get(imiId);
                        int N = subtypesList.size();
                        for (int i = 0; i < N; i++) {
                            InputMethodSubtype subtype = (InputMethodSubtype) subtypesList.get(i);
                            out.startTag(null, NODE_SUBTYPE);
                            if (subtype.hasSubtypeId()) {
                                out.attribute(null, ATTR_IME_SUBTYPE_ID, String.valueOf(subtype.getSubtypeId()));
                            }
                            out.attribute(null, ATTR_ICON, String.valueOf(subtype.getIconResId()));
                            out.attribute(null, ATTR_LABEL, String.valueOf(subtype.getNameResId()));
                            out.attribute(null, ATTR_IME_SUBTYPE_LOCALE, subtype.getLocale());
                            out.attribute(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG, subtype.getLanguageTag());
                            out.attribute(null, ATTR_IME_SUBTYPE_MODE, subtype.getMode());
                            out.attribute(null, ATTR_IME_SUBTYPE_EXTRA_VALUE, subtype.getExtraValue());
                            out.attribute(null, ATTR_IS_AUXILIARY, String.valueOf(subtype.isAuxiliary() ? 1 : 0));
                            out.attribute(null, ATTR_IS_ASCII_CAPABLE, String.valueOf(subtype.isAsciiCapable() ? 1 : 0));
                            out.endTag(null, NODE_SUBTYPE);
                        }
                        out.endTag(null, NODE_IMI);
                    } else {
                        Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + imiId);
                    }
                }
                out.endTag(null, NODE_SUBTYPES);
                out.endDocument();
                subtypesFile.finishWrite(fos);
            } catch (IOException e) {
                Slog.w(InputMethodManagerService.TAG, "Error writing subtypes", e);
                if (fos != null) {
                    subtypesFile.failWrite(fos);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A:{Splitter: B:23:0x0064, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A:{Splitter: B:23:0x0064, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:25:0x0065, code:
            r6 = move-exception;
     */
        /* JADX WARNING: Missing block: B:26:0x0066, code:
            android.util.Slog.w(com.android.server.InputMethodManagerService.TAG, "Error reading subtypes", r6);
     */
        /* JADX WARNING: Missing block: B:27:0x0073, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void readAdditionalInputMethodSubtypes(HashMap<String, List<InputMethodSubtype>> allSubtypes, AtomicFile subtypesFile) {
            Throwable th;
            FileInputStream fis;
            Throwable th2;
            if (allSubtypes != null && subtypesFile != null) {
                allSubtypes.clear();
                th = null;
                fis = null;
                try {
                    fis = subtypesFile.openRead();
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis, StandardCharsets.UTF_8.name());
                    int eventType = parser.getEventType();
                    do {
                        eventType = parser.next();
                        if (eventType == 2) {
                            break;
                        }
                    } while (eventType != 1);
                    if (NODE_SUBTYPES.equals(parser.getName())) {
                        int depth = parser.getDepth();
                        Object currentImiId = null;
                        ArrayList<InputMethodSubtype> tempSubtypesArray = null;
                        while (true) {
                            eventType = parser.next();
                            if ((eventType != 3 || parser.getDepth() > depth) && eventType != 1) {
                                if (eventType == 2) {
                                    String nodeName = parser.getName();
                                    if (NODE_IMI.equals(nodeName)) {
                                        currentImiId = parser.getAttributeValue(null, "id");
                                        if (TextUtils.isEmpty(currentImiId)) {
                                            Slog.w(InputMethodManagerService.TAG, "Invalid imi id found in subtypes.xml");
                                        } else {
                                            tempSubtypesArray = new ArrayList();
                                            allSubtypes.put(currentImiId, tempSubtypesArray);
                                        }
                                    } else if (NODE_SUBTYPE.equals(nodeName)) {
                                        if (TextUtils.isEmpty(currentImiId) || tempSubtypesArray == null) {
                                            Slog.w(InputMethodManagerService.TAG, "IME uninstalled or not valid.: " + currentImiId);
                                        } else {
                                            int icon = Integer.parseInt(parser.getAttributeValue(null, ATTR_ICON));
                                            int label = Integer.parseInt(parser.getAttributeValue(null, ATTR_LABEL));
                                            String imeSubtypeLocale = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LOCALE);
                                            String languageTag = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_LANGUAGE_TAG);
                                            String imeSubtypeMode = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_MODE);
                                            String imeSubtypeExtraValue = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_EXTRA_VALUE);
                                            boolean isAuxiliary = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_AUXILIARY)));
                                            InputMethodSubtypeBuilder builder = new InputMethodSubtypeBuilder().setSubtypeNameResId(label).setSubtypeIconResId(icon).setSubtypeLocale(imeSubtypeLocale).setLanguageTag(languageTag).setSubtypeMode(imeSubtypeMode).setSubtypeExtraValue(imeSubtypeExtraValue).setIsAuxiliary(isAuxiliary).setIsAsciiCapable(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(String.valueOf(parser.getAttributeValue(null, ATTR_IS_ASCII_CAPABLE))));
                                            String subtypeIdString = parser.getAttributeValue(null, ATTR_IME_SUBTYPE_ID);
                                            if (subtypeIdString != null) {
                                                builder.setSubtypeId(Integer.parseInt(subtypeIdString));
                                            }
                                            tempSubtypesArray.add(builder.build());
                                        }
                                    }
                                }
                            }
                        }
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        if (th != null) {
                            throw th;
                        }
                        return;
                    }
                    throw new XmlPullParserException("Xml doesn't start with subtypes");
                } catch (Throwable th4) {
                    Throwable th5 = th4;
                    th4 = th2;
                    th2 = th5;
                }
            } else {
                return;
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable th6) {
                    if (th4 == null) {
                        th4 = th6;
                    } else if (th4 != th6) {
                        th4.addSuppressed(th6);
                    }
                }
            }
            if (th4 != null) {
                try {
                    throw th4;
                } catch (Exception e) {
                }
            } else {
                throw th2;
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private InputMethodManagerService mService;

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-12-25 : Modify for ColorOS Service", property = OppoRomType.ROM)
        public Lifecycle(Context context) {
            super(context);
            this.mService = new ColorInputMethodManagerService(context);
        }

        public void onStart() {
            LocalServices.addService(InputMethodManagerInternal.class, new LocalServiceImpl(this.mService.mHandler));
            publishBinderService("input_method", this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemRunning((StatusBarManagerService) ServiceManager.getService("statusbar"));
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.mHandler.sendMessage(this.mService.mHandler.obtainMessage(InputMethodManagerService.MSG_SYSTEM_UNLOCK_USER, userHandle, 0));
        }
    }

    private static final class LocalServiceImpl implements InputMethodManagerInternal {
        private final Handler mHandler;

        LocalServiceImpl(Handler handler) {
            this.mHandler = handler;
        }

        public void setInteractive(boolean interactive) {
            int i;
            Handler handler = this.mHandler;
            Handler handler2 = this.mHandler;
            if (interactive) {
                i = 1;
            } else {
                i = 0;
            }
            handler.sendMessage(handler2.obtainMessage(InputMethodManagerService.MSG_SET_INTERACTIVE, i, 0));
        }

        public void switchInputMethod(boolean forwardDirection) {
            int i;
            Handler handler = this.mHandler;
            Handler handler2 = this.mHandler;
            if (forwardDirection) {
                i = 1;
            } else {
                i = 0;
            }
            handler.sendMessage(handler2.obtainMessage(3050, i, 0));
        }

        public void hideCurrentInputMethod() {
            this.mHandler.removeMessages(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
            this.mHandler.sendEmptyMessage(InputMethodManagerService.MSG_HIDE_CURRENT_INPUT_METHOD);
        }
    }

    private static final class MethodCallback extends IInputSessionCallback.Stub {
        private final InputChannel mChannel;
        private final IInputMethod mMethod;
        private final InputMethodManagerService mParentIMMS;

        MethodCallback(InputMethodManagerService imms, IInputMethod method, InputChannel channel) {
            this.mParentIMMS = imms;
            this.mMethod = method;
            this.mChannel = channel;
        }

        public void sessionCreated(IInputMethodSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mParentIMMS.onSessionCreated(this.mMethod, session, this.mChannel);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        private boolean isChangingPackagesOfCurrentUser() {
            int userId = getChangingUserId();
            boolean retval = userId == InputMethodManagerService.this.mSettings.getCurrentUserId();
            if (InputMethodManagerService.DEBUG && !retval) {
                Slog.d(InputMethodManagerService.TAG, "--- ignore this call back from a background user: " + userId);
            }
            return retval;
        }

        /* JADX WARNING: Missing block: B:26:0x0061, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            if (!isChangingPackagesOfCurrentUser()) {
                return false;
            }
            synchronized (InputMethodManagerService.this.mMethodMap) {
                String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                int N = InputMethodManagerService.this.mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i = 0; i < N; i++) {
                        InputMethodInfo imi = (InputMethodInfo) InputMethodManagerService.this.mMethodList.get(i);
                        if (imi.getId().equals(curInputMethodId)) {
                            int length = packages.length;
                            int i2 = 0;
                            while (i2 < length) {
                                if (!imi.getPackageName().equals(packages[i2])) {
                                    i2++;
                                } else if (doit) {
                                    InputMethodManagerService.this.resetSelectedInputMethodAndSubtypeLocked(IElsaManager.EMPTY_PACKAGE);
                                    InputMethodManagerService.this.chooseNewDefaultIMELocked();
                                    return true;
                                } else {
                                    return true;
                                }
                            }
                            continue;
                        }
                    }
                }
            }
        }

        public void onSomePackagesChanged() {
            if (isChangingPackagesOfCurrentUser()) {
                synchronized (InputMethodManagerService.this.mMethodMap) {
                    int change;
                    InputMethodInfo inputMethodInfo = null;
                    String curInputMethodId = InputMethodManagerService.this.mSettings.getSelectedInputMethod();
                    int N = InputMethodManagerService.this.mMethodList.size();
                    if (curInputMethodId != null) {
                        for (int i = 0; i < N; i++) {
                            InputMethodInfo imi = (InputMethodInfo) InputMethodManagerService.this.mMethodList.get(i);
                            String imiId = imi.getId();
                            if (imiId.equals(curInputMethodId)) {
                                inputMethodInfo = imi;
                            }
                            change = isPackageDisappearing(imi.getPackageName());
                            if (isPackageModified(imi.getPackageName())) {
                                InputMethodManagerService.this.mFileManager.deleteAllInputMethodSubtypes(imiId);
                            }
                            if (change == 2 || change == 3) {
                                Slog.i(InputMethodManagerService.TAG, "Input method uninstalled, disabling: " + imi.getComponent());
                                InputMethodManagerService.this.setInputMethodEnabledLocked(imi.getId(), false);
                            }
                        }
                    }
                    InputMethodManagerService.this.buildInputMethodListLocked(false);
                    boolean changed = false;
                    if (inputMethodInfo != null) {
                        change = isPackageDisappearing(inputMethodInfo.getPackageName());
                        if (change == 2 || change == 3) {
                            ServiceInfo si = null;
                            try {
                                si = InputMethodManagerService.this.mIPackageManager.getServiceInfo(inputMethodInfo.getComponent(), 0, InputMethodManagerService.this.mSettings.getCurrentUserId());
                            } catch (RemoteException e) {
                            }
                            if (si == null) {
                                Slog.i(InputMethodManagerService.TAG, "Current input method removed: " + curInputMethodId);
                                InputMethodManagerService.this.updateSystemUiLocked(InputMethodManagerService.this.mCurToken, 0, InputMethodManagerService.this.mBackDisposition);
                                if (!InputMethodManagerService.this.chooseNewDefaultIMELocked()) {
                                    changed = true;
                                    inputMethodInfo = null;
                                    Slog.i(InputMethodManagerService.TAG, "Unsetting current input method");
                                    InputMethodManagerService.this.resetSelectedInputMethodAndSubtypeLocked(IElsaManager.EMPTY_PACKAGE);
                                }
                            }
                        }
                    }
                    if (inputMethodInfo == null) {
                        changed = InputMethodManagerService.this.chooseNewDefaultIMELocked();
                    } else if (!changed) {
                        if (isPackageModified(inputMethodInfo.getPackageName())) {
                            changed = true;
                        }
                    }
                    if (changed) {
                        InputMethodManagerService.this.updateFromSettingsLocked(false);
                    }
                }
            }
        }
    }

    private class ReconnectWatchDog {
        private static final int AUTO_SWITCH_IME_DELAY = 200;
        private static final boolean DEBUG_DET = false;
        private static final int MAX_DECT_COUNT = 3;
        private static final int RECONNECT_TIMEOUT = 6000;
        private Handler mHandler = null;
        private boolean mIsWaitingForAutoSwitch = false;
        private int mReconnectCount = 0;

        public ReconnectWatchDog(Handler handler) {
            this.mHandler = handler;
        }

        public void sendReconnectTimeoutMsg() {
            if (!this.mIsWaitingForAutoSwitch && this.mHandler != null && this.mReconnectCount < 3) {
                this.mReconnectCount++;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3060, this.mReconnectCount, 0), ButtonLight.TIMEOUT_DEFAULT);
            }
        }

        private void cancelReconnectTimeoutMsg() {
            if (this.mHandler != null && this.mReconnectCount > 0) {
                this.mReconnectCount = 0;
                this.mIsWaitingForAutoSwitch = false;
                this.mHandler.removeMessages(3060);
            }
        }

        public void onServiceConnected(ComponentName cmpName) {
            cancelReconnectTimeoutMsg();
        }

        public void handleReconnectTimeoutLocked(int reconnectTimeIndex, InputMethodInfo curImeInfo) {
            if (reconnectTimeIndex >= 3) {
                Slog.w(InputMethodManagerService.TAG, "reconnect time out for current ime for times " + reconnectTimeIndex);
                switchToNextImeLocked(curImeInfo);
            }
        }

        private void switchToNextImeLocked(InputMethodInfo curImeInfo) {
            InputMethodInfo nextEnabledIme = getMostApplicableEnabledIME(InputMethodManagerService.this.mSettings.getEnabledInputMethodListLocked(), curImeInfo);
            if (nextEnabledIme != null) {
                String nextImeId = nextEnabledIme.getId();
                Slog.i(InputMethodManagerService.TAG, "switchToNextIme:" + nextImeId);
                sendAutoSwitchImeMsg(nextImeId);
                return;
            }
            justSayGoodByeToCurIme(curImeInfo);
        }

        private InputMethodInfo getMostApplicableEnabledIME(List<InputMethodInfo> enabledImes, InputMethodInfo curImeInfo) {
            if (enabledImes == null || enabledImes.isEmpty()) {
                return null;
            }
            String curImeId;
            int i = enabledImes.size();
            int j = i;
            if (curImeInfo != null) {
                curImeId = curImeInfo.getId();
            } else {
                curImeId = null;
            }
            boolean needToCmpId = curImeId != null && curImeId.length() > 0;
            while (i > 0) {
                i--;
                InputMethodInfo imi = (InputMethodInfo) enabledImes.get(i);
                if (!imi.isAuxiliaryIme() && ((!needToCmpId || !curImeId.equals(imi.getId())) && InputMethodUtils.isSystemIme(imi))) {
                    return imi;
                }
            }
            while (j > 0) {
                j--;
                InputMethodInfo imei = (InputMethodInfo) enabledImes.get(j);
                if (!imei.isAuxiliaryIme() && (!needToCmpId || !curImeId.equals(imei.getId()))) {
                    return imei;
                }
            }
            return null;
        }

        private void justSayGoodByeToCurIme(InputMethodInfo curImeInfo) {
            Slog.w(InputMethodManagerService.TAG, "No applicable enabled IME to switch!");
        }

        private void sendAutoSwitchImeMsg(String nextImeId) {
            if (this.mHandler != null) {
                this.mIsWaitingForAutoSwitch = true;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3070, nextImeId), 200);
            }
        }

        public void finishAutoSwitchIme() {
            this.mIsWaitingForAutoSwitch = false;
            this.mReconnectCount = 0;
        }
    }

    static class SessionState {
        InputChannel channel;
        final ClientState client;
        final IInputMethod method;
        IInputMethodSession session;

        public String toString() {
            return "SessionState{uid " + this.client.uid + " pid " + this.client.pid + " method " + Integer.toHexString(System.identityHashCode(this.method)) + " session " + Integer.toHexString(System.identityHashCode(this.session)) + " channel " + this.channel + "}";
        }

        SessionState(ClientState _client, IInputMethod _method, IInputMethodSession _session, InputChannel _channel) {
            this.client = _client;
            this.method = _method;
            this.session = _session;
            this.channel = _channel;
        }
    }

    class SettingsObserver extends ContentObserver {
        String mLastEnabled = IElsaManager.EMPTY_PACKAGE;
        boolean mRegistered = false;
        int mUserId;

        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void registerContentObserverLocked(int userId) {
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = InputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    InputMethodManagerService.this.mContext.getContentResolver().unregisterContentObserver(this);
                    this.mRegistered = false;
                }
                if (this.mUserId != userId) {
                    this.mLastEnabled = IElsaManager.EMPTY_PACKAGE;
                    this.mUserId = userId;
                }
                resolver.registerContentObserver(Secure.getUriFor("default_input_method"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("enabled_input_methods"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("selected_input_method_subtype"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("show_ime_with_hard_keyboard"), false, this, userId);
                resolver.registerContentObserver(Secure.getUriFor("accessibility_soft_keyboard_mode"), false, this, userId);
                this.mRegistered = true;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean z = true;
            Uri showImeUri = Secure.getUriFor("show_ime_with_hard_keyboard");
            Uri accessibilityRequestingNoImeUri = Secure.getUriFor("accessibility_soft_keyboard_mode");
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (showImeUri.equals(uri)) {
                    InputMethodManagerService.this.updateKeyboardFromSettingsLocked();
                } else if (accessibilityRequestingNoImeUri.equals(uri)) {
                    InputMethodManagerService inputMethodManagerService = InputMethodManagerService.this;
                    if (Secure.getIntForUser(InputMethodManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, this.mUserId) != 1) {
                        z = false;
                    }
                    inputMethodManagerService.mAccessibilityRequestingNoSoftKeyboard = z;
                    if (InputMethodManagerService.this.mAccessibilityRequestingNoSoftKeyboard) {
                        boolean showRequested = InputMethodManagerService.this.mShowRequested;
                        InputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        InputMethodManagerService.this.mShowRequested = showRequested;
                    } else if (InputMethodManagerService.this.mShowRequested) {
                        InputMethodManagerService.this.showCurrentInputLocked(1, null);
                    }
                } else {
                    boolean enabledChanged = false;
                    String newEnabled = InputMethodManagerService.this.mSettings.getEnabledInputMethodsStr();
                    if (!this.mLastEnabled.equals(newEnabled)) {
                        this.mLastEnabled = newEnabled;
                        enabledChanged = true;
                    }
                    InputMethodManagerService.this.updateInputMethodsFromSettingsLocked(enabledChanged);
                }
            }
        }

        public String toString() {
            return "SettingsObserver{mUserId=" + this.mUserId + " mRegistered=" + this.mRegistered + " mLastEnabled=" + this.mLastEnabled + "}";
        }
    }

    private class SwitchImeTask extends TimerTask {
        /* synthetic */ SwitchImeTask(InputMethodManagerService this$0, SwitchImeTask switchImeTask) {
            this();
        }

        private SwitchImeTask() {
        }

        /* JADX WARNING: Missing block: B:11:0x0042, code:
            return;
     */
        /* JADX WARNING: Missing block: B:37:0x014d, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (InputMethodManagerService.this.mMethodMap) {
                if (InputMethodManagerService.this.index2 == InputMethodManagerService.this.index1) {
                    List<ImeSubtypeListItem> imList = InputMethodManagerService.this.mSwitchingController.getSortedInputMethodAndSubtypeListLocked(false, false);
                    InputMethodInfo currentMethod = (InputMethodInfo) InputMethodManagerService.this.mMethodMap.get(InputMethodManagerService.this.mCurMethodId);
                    if (imList.size() > 1) {
                        int listSize = imList.size();
                        int currentSubtypeId;
                        if (InputMethodManagerService.this.mCurrentSubtype != null) {
                            currentSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(currentMethod, InputMethodManagerService.this.mCurrentSubtype.hashCode());
                        } else {
                            currentSubtypeId = -1;
                        }
                        if (InputMethodManagerService.DEBUG) {
                            Slog.d(InputMethodManagerService.TAG, "ImeSubtypeListItem size : " + listSize);
                        }
                        int i = 0;
                        while (i < listSize) {
                            ImeSubtypeListItem isli = (ImeSubtypeListItem) imList.get(i);
                            if (isli.mImi.equals(currentMethod) && isli.mSubtypeId == currentSubtypeId) {
                                if (InputMethodManagerService.DEBUG) {
                                    Slog.d(InputMethodManagerService.TAG, "index2: " + InputMethodManagerService.this.index2 + ",i: " + i + ",listSize: " + listSize);
                                }
                                InputMethodManagerService inputMethodManagerService = InputMethodManagerService.this;
                                inputMethodManagerService.index2 = inputMethodManagerService.index2 + i;
                                InputMethodManagerService.this.index2 = InputMethodManagerService.this.index2 % listSize;
                                if (InputMethodManagerService.this.index2 < 0) {
                                    inputMethodManagerService = InputMethodManagerService.this;
                                    inputMethodManagerService.index2 = inputMethodManagerService.index2 + listSize;
                                }
                                ImeSubtypeListItem item = (ImeSubtypeListItem) imList.get(InputMethodManagerService.this.index2);
                                if (InputMethodManagerService.DEBUG) {
                                    Slog.d(InputMethodManagerService.TAG, "set input method in runnable! index2: " + InputMethodManagerService.this.index2 + ",item: " + item.mImi.getId());
                                }
                                InputMethodManagerService.this.setInputMethodLocked(item.mImi.getId(), item.mSubtypeId);
                                InputMethodManagerService.this.index2 = 0;
                                InputMethodManagerService.this.index1 = 0;
                            } else {
                                i++;
                            }
                        }
                        InputMethodManagerService.this.index2 = 0;
                        InputMethodManagerService.this.index1 = 0;
                    } else if (InputMethodManagerService.DEBUG) {
                        Slog.w(InputMethodManagerService.TAG, "Only one IME within list, ignored");
                    }
                } else {
                    InputMethodManagerService.this.index2 = InputMethodManagerService.this.index1;
                    Slog.d(InputMethodManagerService.TAG, "schedule switch task after 500ms! index2: " + InputMethodManagerService.this.index2);
                    InputMethodManagerService.this.mTimer.purge();
                    InputMethodManagerService.this.mTimer = new Timer();
                    InputMethodManagerService.this.mTimer.schedule(new SwitchImeTask(), 500);
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.InputMethodManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.InputMethodManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.InputMethodManagerService.<clinit>():void");
    }

    static void restoreEnabledInputMethods(Context context, String prevValue, String newValue) {
        if (DEBUG_RESTORE) {
            Slog.i(TAG, "Restoring enabled input methods:");
            Slog.i(TAG, "prev=" + prevValue);
            Slog.i(TAG, " new=" + newValue);
        }
        ArrayMap<String, ArraySet<String>> prevMap = InputMethodUtils.parseInputMethodsAndSubtypesString(prevValue);
        for (Entry<String, ArraySet<String>> entry : InputMethodUtils.parseInputMethodsAndSubtypesString(newValue).entrySet()) {
            String imeId = (String) entry.getKey();
            ArraySet<String> prevSubtypes = (ArraySet) prevMap.get(imeId);
            if (prevSubtypes == null) {
                prevSubtypes = new ArraySet(2);
                prevMap.put(imeId, prevSubtypes);
            }
            prevSubtypes.addAll((ArraySet) entry.getValue());
        }
        String mergedImesAndSubtypesString = InputMethodUtils.buildInputMethodsAndSubtypesString(prevMap);
        if (DEBUG_RESTORE) {
            Slog.i(TAG, "Merged IME string:");
            Slog.i(TAG, "     " + mergedImesAndSubtypesString);
        }
        Secure.putString(context.getContentResolver(), "enabled_input_methods", mergedImesAndSubtypesString);
    }

    /* JADX WARNING: Missing block: B:26:0x00bf, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onUnlockUser(int userId) {
        boolean z = false;
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (DEBUG) {
                Slog.d(TAG, "onUnlockUser: userId=" + userId + " curUserId=" + currentUserId);
            }
            if (userId != currentUserId) {
                return;
            }
            InputMethodSettings inputMethodSettings = this.mSettings;
            if (!this.mSystemReady) {
                z = true;
            }
            inputMethodSettings.switchCurrentUser(currentUserId, z);
            buildInputMethodListLocked(false);
            updateInputMethodsFromSettingsLocked(true);
            if (!this.mImeSelectedOnBoot) {
                String preInstalledImeName = SystemProperties.get("ro.mtk_default_ime");
                if (preInstalledImeName != null) {
                    InputMethodInfo preInstalledImi = null;
                    for (InputMethodInfo imi : this.mMethodList) {
                        Slog.i(TAG, "mMethodList service info : " + imi.getServiceName());
                        if (preInstalledImeName.equals(imi.getServiceName())) {
                            preInstalledImi = imi;
                            break;
                        }
                    }
                    Slog.i(TAG, "preInstalledImi= " + preInstalledImi);
                    if (preInstalledImi != null) {
                        setInputMethodEnabledLocked(preInstalledImi.getId(), true);
                        setInputMethodLocked(preInstalledImi.getId(), -1);
                    } else {
                        Slog.w(TAG, "Set preinstall ime as default fail.");
                        resetDefaultImeLocked(this.mContext);
                    }
                }
            }
        }
    }

    void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public InputMethodManagerService(Context context) {
        this.mNoBinding = new InputBindResult(null, null, null, -1, -1);
        this.mMethodList = new ArrayList();
        this.mMethodMap = new HashMap();
        this.mSecureSuggestionSpans = new LruCache(20);
        this.mVisibleConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };
        this.mVisibleBound = false;
        this.mClients = new HashMap();
        this.mShortcutInputMethodsAndSubtypes = new HashMap();
        this.mIsInteractive = true;
        this.mCurUserActionNotificationSequenceNumber = 0;
        this.mBackDisposition = 0;
        this.mMyPackageMonitor = new MyPackageMonitor();
        this.mReconnectWatchDog = null;
        this.index1 = 0;
        this.index2 = 0;
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mContext = context;
        this.mRes = context.getResources();
        this.mHandler = new Handler(this);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mCaller = new HandlerCaller(context, null, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                InputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mHardKeyboardListener = new HardKeyboardListener(this, null);
        this.mHasFeature = context.getPackageManager().hasSystemFeature("android.software.input_methods");
        this.mSlotIme = this.mContext.getString(17039389);
        this.mHardKeyboardBehavior = this.mContext.getResources().getInteger(17694888);
        Bundle extras = new Bundle();
        extras.putBoolean("android.allowDuringSetup", true);
        this.mImeSwitcherNotification = new Notification.Builder(this.mContext).setSmallIcon(17302539).setWhen(0).setOngoing(true).addExtras(extras).setCategory("sys").setColor(17170523);
        this.mImeSwitchPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent("android.settings.SHOW_INPUT_METHOD_PICKER"), 0);
        this.mShowOngoingImeSwitcherForPhones = false;
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        broadcastFilter.addAction("android.intent.action.USER_ADDED");
        broadcastFilter.addAction("android.intent.action.USER_REMOVED");
        broadcastFilter.addAction("android.os.action.SETTING_RESTORED");
        broadcastFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        this.mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
        this.mNotificationShown = false;
        int userId = 0;
        try {
            userId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        this.mSettings = new InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, this.mMethodList, userId, !this.mSystemReady);
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, userId);
        synchronized (this.mMethodMap) {
            this.mSwitchingController = InputMethodSubtypeSwitchingController.createInstanceLocked(this.mSettings, context);
        }
        String defaultImiId = this.mSettings.getSelectedInputMethod();
        if (DEBUG) {
            Slog.d(TAG, "Initial default ime = " + defaultImiId);
        }
        this.mImeSelectedOnBoot = !TextUtils.isEmpty(defaultImiId);
        synchronized (this.mMethodMap) {
            buildInputMethodListLocked(!this.mImeSelectedOnBoot);
        }
        this.mSettings.enableAllIMEsIfThereIsNoEnabledIME();
        if (!this.mImeSelectedOnBoot) {
            Slog.w(TAG, "No IME selected. Choose the most applicable IME.");
            synchronized (this.mMethodMap) {
                resetDefaultImeLocked(context);
            }
        }
        synchronized (this.mMethodMap) {
            this.mSettingsObserver.registerContentObserverLocked(userId);
            updateFromSettingsLocked(true);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (InputMethodManagerService.this.mMethodMap) {
                    InputMethodManagerService.this.resetStateIfCurrentLocaleChangedLocked();
                }
            }
        }, filter);
        this.mReconnectWatchDog = new ReconnectWatchDog(this.mHandler);
    }

    private void resetDefaultImeLocked(Context context) {
        if (this.mCurMethodId == null || InputMethodUtils.isSystemIme((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId))) {
            List<InputMethodInfo> suitableImes = InputMethodUtils.getDefaultEnabledImes(context, this.mSystemReady, this.mSettings.getEnabledInputMethodListLocked());
            if (suitableImes.isEmpty()) {
                Slog.i(TAG, "No default found");
                return;
            }
            InputMethodInfo defIm = (InputMethodInfo) suitableImes.get(0);
            Slog.i(TAG, "Default found, using " + defIm.getId());
            setSelectedInputMethodAndSubtypeLocked(defIm, -1, false);
        }
    }

    private void resetAllInternalStateLocked(boolean updateOnlyWhenLocaleChanged, boolean resetDefaultEnabledIme) {
        if (this.mSystemReady) {
            LocaleList newLocales = this.mRes.getConfiguration().getLocales();
            if (!(updateOnlyWhenLocaleChanged && (newLocales == null || newLocales.equals(this.mLastSystemLocales)))) {
                if (!updateOnlyWhenLocaleChanged) {
                    hideCurrentInputLocked(0, null);
                    resetCurrentMethodAndClient(6);
                }
                if (DEBUG) {
                    Slog.i(TAG, "LocaleList has been changed to " + newLocales);
                }
                buildInputMethodListLocked(resetDefaultEnabledIme);
                if (updateOnlyWhenLocaleChanged) {
                    resetDefaultImeLocked(this.mContext);
                } else if (TextUtils.isEmpty(this.mSettings.getSelectedInputMethod())) {
                    resetDefaultImeLocked(this.mContext);
                }
                updateFromSettingsLocked(true);
                this.mLastSystemLocales = newLocales;
                if (!updateOnlyWhenLocaleChanged) {
                    try {
                        startInputInnerLocked();
                    } catch (RuntimeException e) {
                        Slog.w(TAG, "Unexpected exception", e);
                    }
                }
            }
        }
    }

    private void resetStateIfCurrentLocaleChangedLocked() {
        resetAllInternalStateLocked(true, true);
    }

    private void switchUserLocked(int newUserId) {
        if (DEBUG) {
            Slog.d(TAG, "Switching user stage 1/3. newUserId=" + newUserId + " currentUserId=" + this.mSettings.getCurrentUserId());
        }
        this.mSettingsObserver.registerContentObserverLocked(newUserId);
        boolean useCopyOnWriteSettings = (this.mSystemReady && this.mUserManager.isUserUnlockingOrUnlocked(newUserId)) ? false : true;
        this.mSettings.switchCurrentUser(newUserId, useCopyOnWriteSettings);
        updateCurrentProfileIds();
        this.mFileManager = new InputMethodFileManager(this.mMethodMap, newUserId);
        String defaultImiId = this.mSettings.getSelectedInputMethod();
        if (DEBUG) {
            Slog.d(TAG, "Switching user stage 2/3. newUserId=" + newUserId + " defaultImiId=" + defaultImiId);
        }
        boolean initialUserSwitch = TextUtils.isEmpty(defaultImiId);
        resetAllInternalStateLocked(false, initialUserSwitch);
        if (initialUserSwitch) {
            InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), newUserId, this.mContext.getBasePackageName());
        }
        if (DEBUG) {
            Slog.d(TAG, "Switching user stage 3/3. newUserId=" + newUserId + " selectedIme=" + this.mSettings.getSelectedInputMethod());
        }
    }

    void updateCurrentProfileIds() {
        this.mSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(this.mSettings.getCurrentUserId()));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        synchronized (this.mMethodMap) {
            if (DEBUG) {
                Slog.d(TAG, "--- systemReady");
            }
            if (!this.mSystemReady) {
                boolean z;
                this.mSystemReady = true;
                int currentUserId = this.mSettings.getCurrentUserId();
                InputMethodSettings inputMethodSettings = this.mSettings;
                if (this.mUserManager.isUserUnlockingOrUnlocked(currentUserId)) {
                    z = false;
                } else {
                    z = true;
                }
                inputMethodSettings.switchCurrentUser(currentUserId, z);
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                this.mShowOngoingImeSwitcherForPhones = this.mRes.getBoolean(17956870);
                if (this.mShowOngoingImeSwitcherForPhones) {
                    this.mWindowManagerInternal.setOnHardKeyboardStatusChangeListener(this.mHardKeyboardListener);
                }
                buildInputMethodListLocked(!this.mImeSelectedOnBoot);
                if (!this.mImeSelectedOnBoot) {
                    Slog.w(TAG, "Reset the default IME as \"Resource\" is ready here.");
                    String preInstalledImeName = SystemProperties.get("ro.mtk_default_ime");
                    if (preInstalledImeName != null) {
                        InputMethodInfo preInstalledImi = null;
                        for (InputMethodInfo imi : this.mMethodList) {
                            Slog.i(TAG, "mMethodList service info : " + imi.getServiceName());
                            if (preInstalledImeName.equals(imi.getServiceName())) {
                                preInstalledImi = imi;
                                break;
                            }
                        }
                        if (preInstalledImi != null) {
                            setInputMethodLocked(preInstalledImi.getId(), -1);
                        } else {
                            Slog.w(TAG, "Set preinstall ime as default fail.");
                            resetDefaultImeLocked(this.mContext);
                        }
                    }
                    resetStateIfCurrentLocaleChangedLocked();
                    InputMethodUtils.setNonSelectedSystemImesDisabledUntilUsed(this.mIPackageManager, this.mSettings.getEnabledInputMethodListLocked(), this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
                }
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
                try {
                    startInputInnerLocked();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Unexpected exception", e);
                }
            }
        }
        return;
    }

    private void setImeWindowVisibilityStatusHiddenLocked() {
        this.mImeWindowVis = 0;
        updateImeWindowStatusLocked();
    }

    public void refreshImeWindowVisibilityLocked() {
        boolean haveHardKeyboard;
        int i = 2;
        int i2 = 1;
        Configuration conf = this.mRes.getConfiguration();
        if (conf.keyboard != 1) {
            haveHardKeyboard = true;
        } else {
            haveHardKeyboard = false;
        }
        boolean hardKeyShown = haveHardKeyboard ? conf.hardKeyboardHidden != 2 : false;
        boolean inputActive = !isKeyguardLocked() ? !this.mInputShown ? hardKeyShown : true : false;
        boolean inputVisible = inputActive && !hardKeyShown;
        if (!inputActive) {
            i2 = 0;
        }
        if (!inputVisible) {
            i = 0;
        }
        this.mImeWindowVis = i | i2;
        updateImeWindowStatusLocked();
    }

    private void updateImeWindowStatusLocked() {
        setImeWindowStatus(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
    }

    /* JADX WARNING: Missing block: B:6:0x0072, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (DEBUG) {
            Slog.d(TAG, "--- calledFromForegroundUserOrSystemProcess ? calling uid = " + uid + " system uid = " + 1000 + " calling userId = " + userId + ", foreground user id = " + this.mSettings.getCurrentUserId() + ", calling pid = " + Binder.getCallingPid() + InputMethodUtils.getApiCallStack());
        }
        if (uid == 1000 || this.mSettings.isCurrentProfile(userId) || OppoMultiAppManager.getInstance().isCurrentProfile(userId)) {
            return true;
        }
        if (this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            if (DEBUG) {
                Slog.d(TAG, "--- Access granted because the calling process has the INTERACT_ACROSS_USERS_FULL permission");
            }
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return false;
    }

    private boolean calledWithValidToken(IBinder token) {
        if (token == null || this.mCurToken != token) {
            return false;
        }
        return true;
    }

    private boolean bindCurrentInputMethodService(Intent service, ServiceConnection conn, int flags) {
        if (DEBUG_SHOW_SOFTINPUT) {
            Slog.d(TAG, "bindCurrentInputMethodService, service:" + service);
        }
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return false;
    }

    public List<InputMethodInfo> getInputMethodList() {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        List arrayList;
        synchronized (this.mMethodMap) {
            arrayList = new ArrayList(this.mMethodList);
        }
        return arrayList;
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        List enabledInputMethodListLocked;
        synchronized (this.mMethodMap) {
            enabledInputMethodListLocked = this.mSettings.getEnabledInputMethodListLocked();
        }
        return enabledInputMethodListLocked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0020  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo imi;
            if (imiId == null) {
                if (this.mCurMethodId != null) {
                    imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                    List<InputMethodSubtype> emptyList;
                    if (imi != null) {
                        emptyList = Collections.emptyList();
                        return emptyList;
                    }
                    emptyList = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, allowsImplicitlySelectedSubtypes);
                    return emptyList;
                }
            }
            imi = (InputMethodInfo) this.mMethodMap.get(imiId);
            if (imi != null) {
            }
        }
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int uid, int pid) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                this.mClients.put(client.asBinder(), new ClientState(client, inputContext, uid, pid));
            }
        }
    }

    public void removeClient(IInputMethodClient client) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                ClientState cs = (ClientState) this.mClients.remove(client.asBinder());
                if (cs != null) {
                    clearClientSessionLocked(cs);
                    if (this.mCurClient == cs) {
                        this.mCurClient = null;
                    }
                    if (this.mCurFocusedWindowClient == cs) {
                        this.mCurFocusedWindowClient = null;
                    }
                }
            }
        }
    }

    void executeOrSendMessage(IInterface target, Message msg) {
        if (target.asBinder() instanceof Binder) {
            this.mCaller.sendMessage(msg);
            return;
        }
        handleMessage(msg);
        msg.recycle();
    }

    void unbindCurrentClientLocked(int unbindClientReason) {
        if (this.mCurClient != null) {
            if (DEBUG) {
                Slog.v(TAG, "unbindCurrentInputLocked: client=" + this.mCurClient.client.asBinder());
            }
            if (this.mBoundToMethod) {
                this.mBoundToMethod = false;
                if (this.mCurMethod != null) {
                    executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(1000, this.mCurMethod));
                }
            }
            if (DEBUG_IME_ACTIVE) {
                Slog.d(TAG, "unbindCurrentClient send MSG_SET_ACTIVE active: false , to client:" + this.mCurClient);
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(3000, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = false;
            this.mCurClient = null;
            hideInputMethodMenuLocked();
        }
    }

    private int getImeShowFlags() {
        if (this.mShowForcedFromKey) {
            return 5;
        }
        if (this.mShowForced) {
            return 3;
        }
        if (this.mShowExplicitlyRequested) {
            return 1;
        }
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 2;
        }
        if (this.mShowExplicitlyRequested) {
            return 0;
        }
        return 1;
    }

    InputBindResult attachNewInputLocked(boolean initial) {
        InputChannel inputChannel = null;
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_BIND_INPUT, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        SessionState session = this.mCurClient.curSession;
        if (initial) {
            executeOrSendMessage(session.method, this.mCaller.obtainMessageIOOO(MSG_START_INPUT, this.mCurInputContextMissingMethods, session, this.mCurInputContext, this.mCurAttribute));
        } else {
            executeOrSendMessage(session.method, this.mCaller.obtainMessageIOOO(MSG_RESTART_INPUT, this.mCurInputContextMissingMethods, session, this.mCurInputContext, this.mCurAttribute));
        }
        if (this.mShowRequested) {
            if (DEBUG) {
                Slog.v(TAG, "Attach new input asks to show input");
            }
            showCurrentInputLocked(getAppShowFlags(), null);
        }
        IInputMethodSession iInputMethodSession = session.session;
        if (session.channel != null) {
            inputChannel = session.channel.dup();
        }
        return new InputBindResult(iInputMethodSession, inputChannel, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    InputBindResult startInputLocked(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        ClientState cs = (ClientState) this.mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client " + client.asBinder());
        } else if (attribute == null) {
            Slog.w(TAG, "Ignoring startInput with null EditorInfo. uid=" + cs.uid + " pid=" + cs.pid);
            return null;
        } else {
            try {
                if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                    Slog.w(TAG, "Starting input on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                    return null;
                }
            } catch (RemoteException e) {
            }
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
        }
    }

    InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        boolean z = true;
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, cs.uid, attribute.packageName)) {
            if (this.mCurClient != cs) {
                this.mCurClientInKeyguard = isKeyguardLocked();
                unbindCurrentClientLocked(1);
                if (DEBUG) {
                    Slog.v(TAG, "switching to client: client=" + cs.client.asBinder() + " keyguard=" + this.mCurClientInKeyguard);
                }
                if (this.mIsInteractive) {
                    if (DEBUG_IME_ACTIVE) {
                        Slog.d(TAG, "startInputUnchecked send MSG_SET_ACTIVE active:" + this.mIsInteractive + ", to client:" + this.mCurClient);
                    }
                    executeOrSendMessage(cs.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, cs));
                }
            }
            this.mCurSeq++;
            if (this.mCurSeq <= 0) {
                this.mCurSeq = 1;
            }
            this.mCurClient = cs;
            this.mCurInputContext = inputContext;
            this.mCurInputContextMissingMethods = missingMethods;
            this.mCurAttribute = attribute;
            if (this.mCurId != null && this.mCurId.equals(this.mCurMethodId)) {
                if (cs.curSession != null) {
                    if ((controlFlags & 256) == 0) {
                        z = false;
                    }
                    return attachNewInputLocked(z);
                } else if (this.mHaveConnection) {
                    if (this.mCurMethod != null) {
                        requestClientSessionLocked(cs);
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else if (SystemClock.uptimeMillis() < this.mLastBindTime + TIME_TO_RECONNECT) {
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else {
                        Object[] objArr = new Object[3];
                        objArr[0] = this.mCurMethodId;
                        objArr[1] = Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime);
                        objArr[2] = Integer.valueOf(0);
                        EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, objArr);
                    }
                }
            }
            return startInputInnerLocked();
        }
        Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + cs.uid + " package=" + attribute.packageName);
        return this.mNoBinding;
    }

    InputBindResult startInputInnerLocked() {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (this.mSystemReady) {
            InputMethodInfo info = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (info == null) {
                Slog.w(TAG, "Unknown id: " + this.mCurMethodId + ", rebuild the method list and reset to the default Ime.");
                buildInputMethodListLocked(true);
                info = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                if (info == null) {
                    throw new IllegalArgumentException("Unknown id: " + this.mCurMethodId);
                }
            }
            unbindCurrentMethodLocked(true);
            this.mCurIntent = new Intent("android.view.InputMethod");
            this.mCurIntent.setComponent(info.getComponent());
            this.mCurIntent.putExtra("android.intent.extra.client_label", 17040506);
            this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
            if (bindCurrentInputMethodService(this.mCurIntent, this, 1610612741)) {
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mHaveConnection = true;
                this.mCurId = info.getId();
                this.mCurToken = new Binder();
                try {
                    Slog.v(TAG, "Adding window token: " + this.mCurToken);
                    this.mIWindowManager.addWindowToken(this.mCurToken, 2011);
                } catch (RemoteException e) {
                }
                return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            }
            this.mCurIntent = null;
            Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
            return null;
        }
        return new InputBindResult(null, null, this.mCurMethodId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    private InputBindResult startInput(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (!calledFromValidUser()) {
            return null;
        }
        InputBindResult startInputLocked;
        synchronized (this.mMethodMap) {
            if (DEBUG) {
                Slog.v(TAG, "startInput: reason=" + InputMethodClient.getStartInputReason(startInputReason) + " client = " + client.asBinder() + " inputContext=" + inputContext + " missingMethods=" + InputConnectionInspector.getMissingMethodFlagsAsString(missingMethods) + " attribute=" + attribute + " controlFlags=#" + Integer.toHexString(controlFlags));
            }
            long ident = Binder.clearCallingIdentity();
            try {
                startInputLocked = startInputLocked(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return startInputLocked;
    }

    public void finishInput(IInputMethodClient client) {
    }

    /* JADX WARNING: Missing block: B:26:0x00a0, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                if (this.mReconnectWatchDog != null) {
                    this.mReconnectWatchDog.onServiceConnected(name);
                }
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(false);
                    return;
                }
                if (DEBUG) {
                    Slog.v(TAG, "Initiating attach with token: " + this.mCurToken);
                }
                if (DEBUG_SHOW_SOFTINPUT) {
                    Slog.d(TAG, "onServiceConnected, name:" + name + ", token:" + this.mCurToken);
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_ATTACH_TOKEN, this.mCurMethod, this.mCurToken));
                if (this.mCurClient != null) {
                    clearClientSessionLocked(this.mCurClient);
                    requestClientSessionLocked(this.mCurClient);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0046, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x0048, code:
            r9.dispose();
     */
    /* JADX WARNING: Missing block: B:17:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod == null || method == null || this.mCurMethod.asBinder() != method.asBinder() || this.mCurClient == null) {
            } else {
                clearClientSessionLocked(this.mCurClient);
                this.mCurClient.curSession = new SessionState(this.mCurClient, method, session, channel);
                InputBindResult res = attachNewInputLocked(true);
                if (res.method != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(3010, this.mCurClient.client, res));
                }
            }
        }
    }

    void unbindCurrentMethodLocked(boolean savePosition) {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = false;
        }
        if (this.mCurToken != null) {
            try {
                if (DEBUG) {
                    Slog.v(TAG, "Removing window token: " + this.mCurToken);
                }
                if ((this.mImeWindowVis & 1) != 0 && savePosition) {
                    this.mWindowManagerInternal.saveLastInputMethodWindowForTransition();
                }
                this.mIWindowManager.removeWindowToken(this.mCurToken);
            } catch (RemoteException e) {
            }
            this.mCurToken = null;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(false);
        unbindCurrentClientLocked(unbindClientReason);
    }

    void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            if (DEBUG) {
                Slog.v(TAG, "Creating new session for client " + cs);
            }
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[1], new MethodCallback(this, this.mCurMethod, channels[0])));
        }
    }

    void clearClientSessionLocked(ClientState cs) {
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = false;
    }

    private void finishSessionLocked(SessionState sessionState) {
        if (sessionState != null) {
            if (sessionState.session != null) {
                try {
                    sessionState.session.finishSession();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Session failed to close due to remote exception", e);
                    updateSystemUiLocked(this.mCurToken, 0, this.mBackDisposition);
                }
                sessionState.session = null;
            }
            if (sessionState.channel != null) {
                sessionState.channel.dispose();
                sessionState.channel = null;
            }
        }
    }

    void clearCurMethodLocked() {
        if (this.mCurMethod != null) {
            for (ClientState cs : this.mClients.values()) {
                clearClientSessionLocked(cs);
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        if (this.mStatusBar != null) {
            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (DEBUG) {
                Slog.v(TAG, "Service disconnected: " + name + " mCurIntent=" + this.mCurIntent);
            }
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = false;
                if (this.mCurClient != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(3000, 3, this.mCurSeq, this.mCurClient.client));
                }
            }
        }
    }

    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (iconId == 0) {
                        if (DEBUG) {
                            Slog.d(TAG, "hide the small icon for the input method");
                        }
                        if (this.mStatusBar != null) {
                            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                        }
                    } else if (packageName != null) {
                        if (DEBUG) {
                            Slog.d(TAG, "show a small icon for the input method");
                        }
                        try {
                            CharSequence contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                        } catch (RemoteException e) {
                        }
                        if (this.mStatusBar != null) {
                            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                Slog.e(TAG, "Ignoring updateStatusIcon due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        if (!this.mShowOngoingImeSwitcherForPhones) {
            return false;
        }
        if (this.mSwitchingDialog != null) {
            return false;
        }
        if (isScreenLocked()) {
            return false;
        }
        if ((visibility & 1) == 0) {
            return false;
        }
        if (this.mWindowManagerInternal.isHardKeyboardAvailable()) {
            if (this.mHardKeyboardBehavior == 0) {
                return true;
            }
        } else if ((visibility & 2) == 0) {
            return false;
        }
        List<InputMethodInfo> imis = this.mSettings.getEnabledInputMethodListLocked();
        int N = imis.size();
        if (N > 2) {
            return true;
        }
        if (N < 1) {
            return false;
        }
        int nonAuxCount = 0;
        int auxCount = 0;
        InputMethodSubtype nonAuxSubtype = null;
        InputMethodSubtype inputMethodSubtype = null;
        for (int i = 0; i < N; i++) {
            List<InputMethodSubtype> subtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, (InputMethodInfo) imis.get(i), true);
            int subtypeCount = subtypes.size();
            if (subtypeCount == 0) {
                nonAuxCount++;
            } else {
                for (int j = 0; j < subtypeCount; j++) {
                    InputMethodSubtype subtype = (InputMethodSubtype) subtypes.get(j);
                    if (subtype.isAuxiliary()) {
                        auxCount++;
                    } else {
                        nonAuxCount++;
                    }
                    nonAuxSubtype = subtype;
                }
            }
        }
        if (nonAuxCount > 1 || auxCount > 1) {
            return true;
        }
        if (nonAuxCount != 1 || auxCount != 1) {
            return false;
        }
        if (nonAuxSubtype == null || inputMethodSubtype == null || ((!nonAuxSubtype.getLocale().equals(inputMethodSubtype.getLocale()) && !inputMethodSubtype.overridesImplicitlyEnabledSubtype() && !nonAuxSubtype.overridesImplicitlyEnabledSubtype()) || !nonAuxSubtype.containsExtraValueKey(TAG_TRY_SUPPRESSING_IME_SWITCHER))) {
            return true;
        }
        return false;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null ? this.mKeyguardManager.isKeyguardLocked() : false;
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            synchronized (this.mMethodMap) {
                this.mImeWindowVis = vis;
                this.mBackDisposition = backDisposition;
                updateSystemUiLocked(token, vis, backDisposition);
            }
            return;
        }
        Slog.e(TAG, "Ignoring setImeWindowStatus due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    private void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            long ident = Binder.clearCallingIdentity();
            if (vis != 0) {
                try {
                    if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
                        vis = 0;
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            boolean needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis);
            if (this.mStatusBar != null) {
                this.mStatusBar.setImeWindowStatus(token, vis, backDisposition, needsToShowImeSwitcher);
            }
            InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (imi != null && needsToShowImeSwitcher) {
                CharSequence title = this.mRes.getText(17040436);
                CharSequence summary = InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, imi, this.mCurrentSubtype);
                this.mImeSwitcherNotification.setContentTitle(title).setContentText(summary).setContentIntent(this.mImeSwitchPendingIntent);
                try {
                    if (!(this.mNotificationManager == null || this.mIWindowManager.hasNavigationBar())) {
                        if (DEBUG) {
                            Slog.d(TAG, "--- show notification: label =  " + summary);
                        }
                        this.mNotificationManager.notifyAsUser(null, 17040436, this.mImeSwitcherNotification.build(), UserHandle.ALL);
                        this.mNotificationShown = true;
                    }
                } catch (RemoteException e) {
                }
            } else if (this.mNotificationShown && this.mNotificationManager != null) {
                if (DEBUG) {
                    Slog.d(TAG, "--- hide notification");
                }
                this.mNotificationManager.cancelAsUser(null, 17040436, UserHandle.ALL);
                this.mNotificationShown = false;
            }
            Binder.restoreCallingIdentity(ident);
            return;
        }
        Slog.e(TAG, "Ignoring updateSystemUiLocked due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                for (SuggestionSpan ss : spans) {
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x001f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo targetImi = (InputMethodInfo) this.mSecureSuggestionSpans.get(span);
            if (targetImi != null) {
                String[] suggestions = span.getSuggestions();
                if (index < 0 || index >= suggestions.length) {
                } else {
                    String className = span.getNotificationTargetClassName();
                    Intent intent = new Intent();
                    intent.setClassName(targetImi.getPackageName(), className);
                    intent.setAction("android.text.style.SUGGESTION_PICKED");
                    intent.putExtra("before", originalString);
                    intent.putExtra("after", suggestions[index]);
                    intent.putExtra("hashcode", span.hashCode());
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                        Binder.restoreCallingIdentity(ident);
                        return true;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            } else {
                return false;
            }
        }
    }

    void updateFromSettingsLocked(boolean enabledMayChange) {
        updateInputMethodsFromSettingsLocked(enabledMayChange);
        updateKeyboardFromSettingsLocked();
    }

    void updateInputMethodsFromSettingsLocked(boolean enabledMayChange) {
        if (enabledMayChange) {
            List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
            for (int i = 0; i < enabled.size(); i++) {
                InputMethodInfo imm = (InputMethodInfo) enabled.get(i);
                try {
                    ApplicationInfo ai = this.mIPackageManager.getApplicationInfo(imm.getPackageName(), 32768, this.mSettings.getCurrentUserId());
                    if (ai != null && ai.enabledSetting == 4) {
                        if (DEBUG) {
                            Slog.d(TAG, "Update state(" + imm.getId() + "): DISABLED_UNTIL_USED -> DEFAULT");
                        }
                        this.mIPackageManager.setApplicationEnabledSetting(imm.getPackageName(), 0, 1, this.mSettings.getCurrentUserId(), this.mContext.getBasePackageName());
                    }
                } catch (RemoteException e) {
                }
            }
        }
        String id = this.mSettings.getSelectedInputMethod();
        if (TextUtils.isEmpty(id) && chooseNewDefaultIMELocked()) {
            id = this.mSettings.getSelectedInputMethod();
        }
        if (TextUtils.isEmpty(id)) {
            resetCurrentMethodAndClient(4);
        } else {
            try {
                setInputMethodLocked(id, this.mSettings.getSelectedInputMethodSubtypeId(id));
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Unknown input method from prefs: " + id, e2);
                resetCurrentMethodAndClient(5);
            }
            this.mShortcutInputMethodsAndSubtypes.clear();
        }
        this.mSwitchingController.resetCircularListLocked(this.mContext);
    }

    public void updateKeyboardFromSettingsLocked() {
        this.mShowImeWithHardKeyboard = this.mSettings.isShowImeWithHardKeyboardEnabled();
        if (this.mSwitchingDialog != null && this.mSwitchingDialogTitleView != null && this.mSwitchingDialog.isShowing()) {
            ((Switch) this.mSwitchingDialogTitleView.findViewById(16909195)).setChecked(this.mShowImeWithHardKeyboard);
        }
    }

    private void notifyInputMethodSubtypeChanged(int userId, InputMethodInfo inputMethodInfo, InputMethodSubtype subtype) {
        InputManagerInternal inputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        if (inputManagerInternal != null) {
            inputManagerInternal.onInputMethodSubtypeChanged(userId, inputMethodInfo, subtype);
        }
    }

    void setInputMethodLocked(String id, int subtypeId) {
        InputMethodInfo info = (InputMethodInfo) this.mMethodMap.get(id);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + id);
        } else if (id.equals(this.mCurMethodId)) {
            int subtypeCount = info.getSubtypeCount();
            if (subtypeCount > 0) {
                InputMethodSubtype newSubtype;
                InputMethodSubtype oldSubtype = this.mCurrentSubtype;
                if (subtypeId < 0 || subtypeId >= subtypeCount) {
                    newSubtype = getCurrentInputMethodSubtypeLocked();
                } else {
                    newSubtype = info.getSubtypeAt(subtypeId);
                }
                if (newSubtype == null || oldSubtype == null) {
                    Slog.w(TAG, "Illegal subtype state: old subtype = " + oldSubtype + ", new subtype = " + newSubtype);
                    return;
                }
                if (newSubtype != oldSubtype) {
                    setSelectedInputMethodAndSubtypeLocked(info, subtypeId, true);
                    if (this.mCurMethod != null) {
                        try {
                            updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                            this.mCurMethod.changeInputMethodSubtype(newSubtype);
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Failed to call changeInputMethodSubtype");
                            return;
                        }
                    }
                    notifyInputMethodSubtypeChanged(this.mSettings.getCurrentUserId(), info, newSubtype);
                }
            }
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                setSelectedInputMethodAndSubtypeLocked(info, subtypeId, false);
                this.mCurMethodId = id;
                if (ActivityManagerNative.isSystemReady()) {
                    Intent intent = new Intent("android.intent.action.INPUT_METHOD_CHANGED");
                    intent.addFlags(536870912);
                    intent.putExtra("input_method_id", id);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                }
                unbindCurrentClientLocked(2);
                notifyInputMethodSubtypeChanged(this.mSettings.getCurrentUserId(), info, getCurrentInputMethodSubtypeLocked());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0057, code:
            if (r9.mCurClient.client.asBinder() == r10.asBinder()) goto L_0x0059;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                }
                try {
                    if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                        Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                    }
                    if (DEBUG) {
                        Slog.v(TAG, "Client requesting input be shown");
                    }
                    boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                    Binder.restoreCallingIdentity(ident);
                    return showCurrentInputLocked;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return false;
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mShowRequested = true;
        if (this.mAccessibilityRequestingNoSoftKeyboard) {
            return false;
        }
        if ((flags & 2) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForced = true;
        } else if ((flags & 1) == 0) {
            this.mShowExplicitlyRequested = true;
        }
        if ((flags & 4) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForcedFromKey = true;
        }
        if (this.mSystemReady) {
            boolean res = false;
            if (this.mCurMethod != null) {
                if (DEBUG) {
                    Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(MSG_SHOW_SOFT_INPUT, getImeShowFlags(), this.mCurMethod, resultReceiver));
                this.mInputShown = true;
                if (this.mHaveConnection && !this.mVisibleBound) {
                    bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, 201326593);
                    this.mVisibleBound = true;
                }
                res = true;
            } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + TIME_TO_RECONNECT) {
                Object[] objArr = new Object[3];
                objArr[0] = this.mCurMethodId;
                objArr[1] = Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime);
                objArr[2] = Integer.valueOf(1);
                EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, objArr);
                Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
                if (this.mReconnectWatchDog != null) {
                    this.mReconnectWatchDog.sendReconnectTimeoutMsg();
                }
                this.mContext.unbindService(this);
                bindCurrentInputMethodService(this.mCurIntent, this, 1073741825);
            } else if (DEBUG) {
                Slog.d(TAG, "Can't show input: connection = " + this.mHaveConnection + ", time = " + ((this.mLastBindTime + TIME_TO_RECONNECT) - SystemClock.uptimeMillis()));
            }
            return res;
        }
        if (DEBUG_SHOW_SOFTINPUT) {
            Slog.d(TAG, "showCurrentInputLocked return for system not ready!");
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:22:0x005b, code:
            if (r9.mCurClient.client.asBinder() == r10.asBinder()) goto L_0x005d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                }
                try {
                    if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                        if (DEBUG) {
                            Slog.w(TAG, "Ignoring hideSoftInput of uid " + uid + ": " + client);
                        }
                    }
                    if (DEBUG) {
                        Slog.v(TAG, "Client requesting input be hidden");
                    }
                    boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                    Binder.restoreCallingIdentity(ident);
                    return hideCurrentInputLocked;
                } catch (RemoteException e) {
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return false;
    }

    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        boolean shouldHideSoftInput = true;
        if ((flags & 1) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            if (DEBUG) {
                Slog.v(TAG, "Not hiding: explicit show not cancelled by non-explicit hide");
            }
            return false;
        } else if (!this.mShowForced || (flags & 2) == 0) {
            boolean res;
            if (this.mCurMethod == null) {
                shouldHideSoftInput = false;
            } else if (!this.mInputShown && (this.mImeWindowVis & 1) == 0) {
                shouldHideSoftInput = false;
            }
            if (shouldHideSoftInput) {
                hideInputMethodMenu();
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_HIDE_SOFT_INPUT, this.mCurMethod, resultReceiver));
                res = true;
            } else {
                res = false;
            }
            if (this.mHaveConnection && this.mVisibleBound) {
                try {
                    this.mContext.unbindService(this.mVisibleConnection);
                } catch (IllegalArgumentException e) {
                    if (DEBUG) {
                        Slog.v(TAG, e.toString());
                    }
                }
                this.mVisibleBound = false;
            }
            this.mInputShown = false;
            this.mShowRequested = false;
            this.mShowExplicitlyRequested = false;
            this.mShowForced = false;
            this.mShowForcedFromKey = false;
            return res;
        } else {
            if (DEBUG) {
                Slog.v(TAG, "Not hiding: forced show not cancelled by not-always hide");
            }
            return false;
        }
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (windowToken != null) {
            return windowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
        }
        return startInput(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
    }

    private InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult res = null;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (DEBUG) {
                    Slog.v(TAG, "windowGainedFocus: reason=" + InputMethodClient.getStartInputReason(startInputReason) + " client=" + client.asBinder() + " inputContext=" + inputContext + " missingMethods=" + InputConnectionInspector.getMissingMethodFlagsAsString(missingMethods) + " attribute=" + attribute + " controlFlags=#" + Integer.toHexString(controlFlags) + " softInputMode=#" + Integer.toHexString(softInputMode) + " windowFlags=#" + Integer.toHexString(windowFlags));
                }
                ClientState cs = (ClientState) this.mClients.get(client.asBinder());
                if (cs == null) {
                    throw new IllegalArgumentException("unknown client " + client.asBinder());
                }
                try {
                    if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                        Slog.w(TAG, "Focus gain on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                    }
                } catch (RemoteException e) {
                }
                if (!calledFromValidUser) {
                    Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                    Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                    hideCurrentInputLocked(0, null);
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } else if (this.mCurFocusedWindow == windowToken) {
                    Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client + " attribute=" + attribute + ", token = " + windowToken);
                    if (attribute != null) {
                        InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                        Binder.restoreCallingIdentity(ident);
                        return startInputUncheckedLocked;
                    }
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } else {
                    this.mCurFocusedWindow = windowToken;
                    this.mCurFocusedWindowClient = cs;
                    boolean doAutoShow;
                    if ((softInputMode & 240) != 16) {
                        doAutoShow = this.mRes.getConfiguration().isLayoutSizeAtLeast(3);
                    } else {
                        doAutoShow = true;
                    }
                    boolean isTextEditor = (controlFlags & 2) != 0;
                    boolean didStart = false;
                    switch (softInputMode & 15) {
                        case 0:
                            if (!isTextEditor || !doAutoShow) {
                                if (LayoutParams.mayUseInputMethod(windowFlags)) {
                                    if (DEBUG) {
                                        Slog.v(TAG, "Unspecified window will hide input");
                                    }
                                    hideCurrentInputLocked(2, null);
                                    break;
                                }
                            } else if (isTextEditor && doAutoShow && (softInputMode & 256) != 0) {
                                if (DEBUG) {
                                    Slog.v(TAG, "Unspecified window will show input");
                                }
                                if (attribute != null) {
                                    res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                    didStart = true;
                                }
                                showCurrentInputLocked(1, null);
                                break;
                            }
                            break;
                        case 2:
                            if ((softInputMode & 256) != 0) {
                                if (DEBUG) {
                                    Slog.v(TAG, "Window asks to hide input going forward");
                                }
                                hideCurrentInputLocked(0, null);
                                break;
                            }
                            break;
                        case 3:
                            if (DEBUG) {
                                Slog.v(TAG, "Window asks to hide input");
                            }
                            hideCurrentInputLocked(0, null);
                            break;
                        case 4:
                            if ((softInputMode & 256) != 0) {
                                if (DEBUG) {
                                    Slog.v(TAG, "Window asks to show input going forward");
                                }
                                if (attribute != null) {
                                    res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                    didStart = true;
                                }
                                showCurrentInputLocked(1, null);
                                break;
                            }
                            break;
                        case 5:
                            if (DEBUG) {
                                Slog.v(TAG, "Window asks to always show input");
                            }
                            if (attribute != null) {
                                res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                didStart = true;
                            }
                            showCurrentInputLocked(1, null);
                            break;
                    }
                    if (!(didStart || attribute == null)) {
                        res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                    }
                    Binder.restoreCallingIdentity(ident);
                    return res;
                }
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Missing block: B:15:0x0053, code:
            if (r4.mCurClient.client.asBinder() != r5.asBinder()) goto L_0x0010;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!(this.mCurClient == null || client == null)) {
                }
                Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid " + Binder.getCallingUid() + ": " + client);
                this.mHandler.sendMessage(this.mCaller.obtainMessageI(1, auxiliarySubtypeMode));
            }
        }
    }

    public void setInputMethod(IBinder token, String id) {
        if (calledFromValidUser()) {
            setInputMethodWithSubtypeId(token, id, -1);
        }
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (subtype != null) {
                    setInputMethodWithSubtypeIdLocked(token, id, InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(id), subtype.hashCode()));
                } else {
                    setInputMethod(token, id);
                }
            }
        }
    }

    public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(2, inputMethodId));
            }
        }
    }

    public boolean switchToLastInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            InputMethodInfo lastImi;
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme != null) {
                lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
            } else {
                lastImi = null;
            }
            String targetLastImiId = null;
            int subtypeId = -1;
            if (!(lastIme == null || lastImi == null)) {
                boolean imiIdIsSame = lastImi.getId().equals(this.mCurMethodId);
                int lastSubtypeHash = Integer.parseInt((String) lastIme.second);
                int currentSubtypeHash;
                if (this.mCurrentSubtype == null) {
                    currentSubtypeHash = -1;
                } else {
                    currentSubtypeHash = this.mCurrentSubtype.hashCode();
                }
                if (!(imiIdIsSame && lastSubtypeHash == currentSubtypeHash)) {
                    targetLastImiId = lastIme.first;
                    subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, lastSubtypeHash);
                }
            }
            if (TextUtils.isEmpty(targetLastImiId) && !InputMethodUtils.canAddToLastInputMethod(this.mCurrentSubtype)) {
                List<InputMethodInfo> enabled = this.mSettings.getEnabledInputMethodListLocked();
                if (enabled != null) {
                    String locale;
                    int N = enabled.size();
                    if (this.mCurrentSubtype == null) {
                        locale = this.mRes.getConfiguration().locale.toString();
                    } else {
                        locale = this.mCurrentSubtype.getLocale();
                    }
                    for (int i = 0; i < N; i++) {
                        InputMethodInfo imi = (InputMethodInfo) enabled.get(i);
                        if (imi.getSubtypeCount() > 0 && InputMethodUtils.isSystemIme(imi)) {
                            InputMethodSubtype keyboardSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, InputMethodUtils.getSubtypes(imi), "keyboard", locale, true);
                            if (keyboardSubtype != null) {
                                targetLastImiId = imi.getId();
                                subtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, keyboardSubtype.hashCode());
                                if (keyboardSubtype.getLocale().equals(locale)) {
                                    break;
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(targetLastImiId)) {
                return false;
            }
            if (DEBUG) {
                Slog.d(TAG, "Switch to: " + lastImi.getId() + ", " + ((String) lastIme.second) + ", from: " + this.mCurMethodId + ", " + subtypeId);
            }
            setInputMethodWithSubtypeIdLocked(token, targetLastImiId, subtypeId);
            return true;
        }
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (calledWithValidToken(token)) {
                ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(onlyCurrentIme, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true);
                if (nextSubtype == null) {
                    return false;
                }
                setInputMethodWithSubtypeIdLocked(token, nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
                return true;
            }
            Slog.e(TAG, "Ignoring switchToNextInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
            return false;
        }
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (!calledWithValidToken(token)) {
                Slog.e(TAG, "Ignoring shouldOfferSwitchingToNextInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                return false;
            } else if (this.mSwitchingController.getNextInputMethodLocked(false, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, true) == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0028, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:25:0x004c, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null || TextUtils.isEmpty((CharSequence) lastIme.first) || TextUtils.isEmpty((CharSequence) lastIme.second)) {
            } else {
                InputMethodInfo lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
                if (lastImi == null) {
                    return null;
                }
                try {
                    int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                    if (lastSubtypeId < 0 || lastSubtypeId >= lastImi.getSubtypeCount()) {
                    } else {
                        InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                        return subtypeAt;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:44:0x0065, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
        if (calledFromValidUser() && !TextUtils.isEmpty(imiId) && subtypes != null) {
            synchronized (this.mMethodMap) {
                InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(imiId);
                if (imi == null) {
                    return;
                }
                long ident;
                try {
                    String[] packageInfos = this.mIPackageManager.getPackagesForUid(Binder.getCallingUid());
                    if (packageInfos != null) {
                        for (String equals : packageInfos) {
                            if (equals.equals(imi.getPackageName())) {
                                this.mFileManager.addInputMethodSubtypes(imi, subtypes);
                                ident = Binder.clearCallingIdentity();
                                buildInputMethodListLocked(false);
                                Binder.restoreCallingIdentity(ident);
                                return;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to get package infos");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight();
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        if (calledFromValidUser()) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mMethodMap) {
                    if (calledWithValidToken(token)) {
                        this.mWindowManagerInternal.clearLastInputMethodWindowForTransition();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    Slog.e(TAG, "Ignoring clearLastInputMethodWindowForTransition due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0051, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x0066, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyUserAction(int sequenceNumber) {
        if (DEBUG) {
            Slog.d(TAG, "Got the notification of a user action. sequenceNumber:" + sequenceNumber);
        }
        synchronized (this.mMethodMap) {
            if (this.mCurUserActionNotificationSequenceNumber == sequenceNumber) {
                InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                if (imi != null) {
                    this.mSwitchingController.onUserActionLocked(imi, this.mCurrentSubtype);
                }
            } else if (DEBUG) {
                Slog.d(TAG, "Ignoring the user action notification due to the sequence number mismatch. expected:" + this.mCurUserActionNotificationSequenceNumber + " actual: " + sequenceNumber);
            }
        }
    }

    private void setInputMethodWithSubtypeId(IBinder token, String id, int subtypeId) {
        synchronized (this.mMethodMap) {
            setInputMethodWithSubtypeIdLocked(token, id, subtypeId);
        }
    }

    private void setInputMethodWithSubtypeIdLocked(IBinder token, String id, int subtypeId) {
        if (token == null) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Using null token requires permission android.permission.WRITE_SECURE_SETTINGS");
            }
        } else if (this.mCurToken != token) {
            Slog.w(TAG, "Ignoring setInputMethod of uid " + Binder.getCallingUid() + " token: " + token);
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            setInputMethodLocked(id, subtypeId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void hideMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        hideCurrentInputLocked(flags, null);
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    Slog.e(TAG, "Ignoring hideInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            }
        }
    }

    public void showMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        showCurrentInputLocked(flags, null);
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    Slog.e(TAG, "Ignoring showMySoftInput due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            }
        }
    }

    void setEnabledSessionInMainThread(SessionState session) {
        if (this.mEnabledSession != session) {
            if (!(this.mEnabledSession == null || this.mEnabledSession.session == null)) {
                try {
                    if (DEBUG) {
                        Slog.v(TAG, "Disabling: " + this.mEnabledSession);
                    }
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            this.mEnabledSession = session;
            if (this.mEnabledSession != null && this.mEnabledSession.session != null) {
                try {
                    if (DEBUG) {
                        Slog.v(TAG, "Enabling: " + this.mEnabledSession);
                    }
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, true);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    public boolean handleMessage(Message msg) {
        SomeArgs args;
        int missingMethods;
        SessionState session;
        switch (msg.what) {
            case 1:
                boolean showAuxSubtypes;
                switch (msg.arg1) {
                    case 0:
                        showAuxSubtypes = this.mInputShown;
                        break;
                    case 1:
                        showAuxSubtypes = true;
                        break;
                    case 2:
                        showAuxSubtypes = false;
                        break;
                    default:
                        Slog.e(TAG, "Unknown subtype picker mode = " + msg.arg1);
                        return false;
                }
                showInputMethodMenu(showAuxSubtypes);
                return true;
            case 2:
                showInputMethodAndSubtypeEnabler((String) msg.obj);
                return true;
            case 3:
                showConfigureInputMethods();
                return true;
            case 1000:
                try {
                    ((IInputMethod) msg.obj).unbindInput();
                } catch (RemoteException e) {
                }
                return true;
            case MSG_BIND_INPUT /*1010*/:
                args = msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                }
                args.recycle();
                return true;
            case MSG_SHOW_SOFT_INPUT /*1020*/:
                args = (SomeArgs) msg.obj;
                try {
                    if (DEBUG) {
                        Slog.v(TAG, "Calling " + args.arg1 + ".showSoftInput(" + msg.arg1 + ", " + args.arg2 + ")");
                    }
                    ((IInputMethod) args.arg1).showSoftInput(msg.arg1, (ResultReceiver) args.arg2);
                } catch (RemoteException e3) {
                }
                args.recycle();
                return true;
            case MSG_HIDE_SOFT_INPUT /*1030*/:
                args = (SomeArgs) msg.obj;
                try {
                    if (DEBUG) {
                        Slog.v(TAG, "Calling " + args.arg1 + ".hideSoftInput(0, " + args.arg2 + ")");
                    }
                    ((IInputMethod) args.arg1).hideSoftInput(0, (ResultReceiver) args.arg2);
                } catch (RemoteException e4) {
                }
                args.recycle();
                return true;
            case MSG_HIDE_CURRENT_INPUT_METHOD /*1035*/:
                synchronized (this.mMethodMap) {
                    hideCurrentInputLocked(0, null);
                }
                return true;
            case MSG_ATTACH_TOKEN /*1040*/:
                args = (SomeArgs) msg.obj;
                try {
                    if (DEBUG) {
                        Slog.v(TAG, "Sending attach of token: " + args.arg2);
                    }
                    if (DEBUG_SHOW_SOFTINPUT) {
                        Slog.d(TAG, "MSG_ATTACH_TOKEN, Sending attach of token: " + args.arg2);
                    }
                    ((IInputMethod) args.arg1).attachToken((IBinder) args.arg2);
                } catch (RemoteException e5) {
                }
                args.recycle();
                return true;
            case MSG_CREATE_SESSION /*1050*/:
                args = (SomeArgs) msg.obj;
                IInputMethod method = args.arg1;
                InputChannel channel = args.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback) args.arg3);
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                } catch (RemoteException e6) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                } catch (Throwable th) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case MSG_START_INPUT /*2000*/:
                missingMethods = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    session = args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.startInput((IInputContext) args.arg2, missingMethods, (EditorInfo) args.arg3);
                } catch (RemoteException e7) {
                }
                args.recycle();
                return true;
            case MSG_RESTART_INPUT /*2010*/:
                missingMethods = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    session = (SessionState) args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.restartInput((IInputContext) args.arg2, missingMethods, (EditorInfo) args.arg3);
                } catch (RemoteException e8) {
                }
                args.recycle();
                return true;
            case 3000:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e9) {
                }
                return true;
            case 3010:
                args = (SomeArgs) msg.obj;
                IInputMethodClient client = args.arg1;
                InputBindResult res = args.arg2;
                try {
                    client.onBindMethod(res);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (RemoteException e10) {
                    Slog.w(TAG, "Client died receiving input method " + args.arg2);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (Throwable th2) {
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case MSG_SET_ACTIVE /*3020*/:
                try {
                    if (DEBUG_IME_ACTIVE) {
                        Slog.d(TAG, "case MSG_SET_ACTIVE, active:" + (msg.arg1 != 0));
                    }
                    ((ClientState) msg.obj).client.setActive(msg.arg1 != 0);
                } catch (RemoteException e11) {
                    Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid " + ((ClientState) msg.obj).pid + " uid " + ((ClientState) msg.obj).uid);
                }
                return true;
            case MSG_SET_INTERACTIVE /*3030*/:
                handleSetInteractive(msg.arg1 != 0);
                return true;
            case 3040:
                int sequenceNumber = msg.arg1;
                ClientState clientState = msg.obj;
                try {
                    clientState.client.setUserActionNotificationSequenceNumber(sequenceNumber);
                } catch (RemoteException e12) {
                    Slog.w(TAG, "Got RemoteException sending setUserActionNotificationSequenceNumber(" + sequenceNumber + ") notification to pid " + clientState.pid + " uid " + clientState.uid);
                }
                return true;
            case 3050:
                handleSwitchInputMethod(msg.arg1 != 0);
                return true;
            case 3060:
                synchronized (this.mMethodMap) {
                    InputMethodInfo curImeInfo = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                    if (this.mReconnectWatchDog != null) {
                        this.mReconnectWatchDog.handleReconnectTimeoutLocked(msg.arg1, curImeInfo);
                    }
                }
                return true;
            case 3070:
                String nextImeId = msg.obj;
                if (nextImeId == null || nextImeId.length() <= 0) {
                    Slog.w(TAG, "Unknow ime id when auto switch!");
                    return true;
                }
                synchronized (this.mMethodMap) {
                    try {
                        int subtypeId = this.mSettings.getSelectedInputMethodSubtypeId(nextImeId);
                        Slog.i(TAG, "auto switch to next IME:" + nextImeId);
                        setInputMethodLocked(nextImeId, subtypeId);
                        if (this.mReconnectWatchDog != null) {
                            this.mReconnectWatchDog.finishAutoSwitchIme();
                        }
                    } catch (IllegalArgumentException e13) {
                        Slog.w(TAG, "Unknown input method when auto switch: " + nextImeId, e13);
                        unbindCurrentMethodLocked(false);
                        if (this.mReconnectWatchDog != null) {
                            this.mReconnectWatchDog.finishAutoSwitchIme();
                        }
                    }
                }
                return true;
            case MSG_HARD_KEYBOARD_SWITCH_CHANGED /*4000*/:
                this.mHardKeyboardListener.handleHardKeyboardStatusChange(msg.arg1 == 1);
                return true;
            case MSG_SYSTEM_UNLOCK_USER /*5000*/:
                onUnlockUser(msg.arg1);
                return true;
            default:
                return false;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        int i = 0;
        synchronized (this.mMethodMap) {
            int i2;
            this.mIsInteractive = interactive;
            IBinder iBinder = this.mCurToken;
            if (interactive) {
                i2 = this.mImeWindowVis;
            } else {
                i2 = 0;
            }
            updateSystemUiLocked(iBinder, i2, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                IInterface iInterface = this.mCurClient.client;
                HandlerCaller handlerCaller = this.mCaller;
                if (this.mIsInteractive) {
                    i = 1;
                }
                executeOrSendMessage(iInterface, handlerCaller.obtainMessageIO(MSG_SET_ACTIVE, i, this.mCurClient));
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0054, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleSwitchInputMethod(boolean forwardDirection) {
        synchronized (this.mMethodMap) {
            ImeSubtypeListItem nextSubtype = this.mSwitchingController.getNextInputMethodLocked(false, (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), this.mCurrentSubtype, forwardDirection);
            if (nextSubtype == null) {
                return;
            }
            setInputMethodLocked(nextSubtype.mImi.getId(), nextSubtype.mSubtypeId);
            InputMethodInfo newInputMethodInfo = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (newInputMethodInfo == null) {
                return;
            }
            CharSequence toastText = InputMethodUtils.getImeAndSubtypeDisplayName(this.mContext, newInputMethodInfo, this.mCurrentSubtype);
            if (!TextUtils.isEmpty(toastText)) {
                if (this.mSubtypeSwitchedByShortCutToast == null) {
                    this.mSubtypeSwitchedByShortCutToast = Toast.makeText(this.mContext, toastText, 0);
                } else {
                    this.mSubtypeSwitchedByShortCutToast.setText(toastText);
                }
                this.mSubtypeSwitchedByShortCutToast.show();
            }
        }
    }

    private boolean chooseNewDefaultIMELocked() {
        InputMethodInfo imi = InputMethodUtils.getMostApplicableDefaultIME(this.mSettings.getEnabledInputMethodListLocked());
        if (imi == null) {
            return false;
        }
        if (DEBUG) {
            Slog.d(TAG, "New default IME was selected: " + imi.getId());
        }
        resetSelectedInputMethodAndSubtypeLocked(imi.getId());
        return true;
    }

    void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        int i;
        int N;
        if (DEBUG) {
            Slog.d(TAG, "--- re-buildInputMethodList reset = " + resetDefaultEnabledIme + " \n ------ caller=" + Debug.getCallers(10));
        }
        this.mMethodList.clear();
        this.mMethodMap.clear();
        List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, this.mSettings.getCurrentUserId());
        HashMap<String, List<InputMethodSubtype>> additionalSubtypes = this.mFileManager.getAllAdditionalInputMethodSubtypes();
        for (i = 0; i < services.size(); i++) {
            ResolveInfo ri = (ResolveInfo) services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if ("android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                if (DEBUG) {
                    Slog.d(TAG, "Checking " + compName);
                }
                try {
                    InputMethodInfo p = new InputMethodInfo(this.mContext, ri, additionalSubtypes);
                    this.mMethodList.add(p);
                    this.mMethodMap.put(p.getId(), p);
                    if (DEBUG) {
                        Slog.d(TAG, "Found an input method " + p);
                    }
                } catch (Exception e) {
                    Slog.wtf(TAG, "Unable to load input method " + compName, e);
                }
            } else {
                Slog.w(TAG, "Skipping input method " + compName + ": it does not require the permission " + "android.permission.BIND_INPUT_METHOD");
            }
        }
        if (!resetDefaultEnabledIme) {
            boolean enabledImeFound = false;
            List<InputMethodInfo> enabledImes = this.mSettings.getEnabledInputMethodListLocked();
            N = enabledImes.size();
            for (i = 0; i < N; i++) {
                if (this.mMethodList.contains((InputMethodInfo) enabledImes.get(i))) {
                    enabledImeFound = true;
                    break;
                }
            }
            if (!enabledImeFound) {
                Slog.i(TAG, "All the enabled IMEs are gone. Reset default enabled IMEs.");
                resetDefaultEnabledIme = true;
                resetSelectedInputMethodAndSubtypeLocked(IElsaManager.EMPTY_PACKAGE);
            }
        }
        if (resetDefaultEnabledIme) {
            InputMethodInfo imi;
            ArrayList<InputMethodInfo> defaultEnabledIme = InputMethodUtils.getDefaultEnabledImes(this.mContext, this.mSystemReady, this.mMethodList);
            N = defaultEnabledIme.size();
            for (i = 0; i < N; i++) {
                imi = (InputMethodInfo) defaultEnabledIme.get(i);
                if (DEBUG) {
                    Slog.d(TAG, "--- enable ime = " + imi);
                }
                setInputMethodEnabledLocked(imi.getId(), true);
            }
            if (TextUtils.isEmpty(this.mSettings.getEnabledInputMethodsStr())) {
                for (i = 0; i < this.mMethodList.size(); i++) {
                    imi = (InputMethodInfo) this.mMethodList.get(i);
                    if (InputMethodUtils.isSystemIme(imi)) {
                        setInputMethodEnabledLocked(imi.getId(), true);
                        break;
                    }
                }
            }
        }
        String defaultImiId = this.mSettings.getSelectedInputMethod();
        if (!TextUtils.isEmpty(defaultImiId)) {
            if (this.mMethodMap.containsKey(defaultImiId)) {
                setInputMethodEnabledLocked(defaultImiId, true);
            } else {
                Slog.w(TAG, "Default IME is uninstalled. Choose new default IME.");
                if (chooseNewDefaultIMELocked()) {
                    updateInputMethodsFromSettingsLocked(true);
                }
            }
        }
        this.mSwitchingController.resetCircularListLocked(this.mContext);
    }

    private void showInputMethodAndSubtypeEnabler(String inputMethodId) {
        int userId;
        Intent intent = new Intent("android.settings.INPUT_METHOD_SUBTYPE_SETTINGS");
        intent.setFlags(337641472);
        if (!TextUtils.isEmpty(inputMethodId)) {
            intent.putExtra("input_method_id", inputMethodId);
        }
        synchronized (this.mMethodMap) {
            userId = this.mSettings.getCurrentUserId();
        }
        this.mContext.startActivityAsUser(intent, null, UserHandle.of(userId));
    }

    private void showConfigureInputMethods() {
        hideInputMethodMenu();
        Intent intent = new Intent("android.settings.INPUT_METHOD_SETTINGS");
        intent.setFlags(337641472);
        this.mContext.startActivityAsUser(intent, null, UserHandle.CURRENT);
    }

    private boolean isScreenLocked() {
        if (this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardLocked()) {
            return false;
        }
        return this.mKeyguardManager.isKeyguardSecure();
    }

    /* JADX WARNING: Missing block: B:14:0x0088, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK, : Modify for inputmothod select", property = OppoRomType.ROM)
    private void showInputMethodMenu(boolean showAuxSubtypes) {
        if (DEBUG) {
            Slog.v(TAG, "Show switching menu. showAuxSubtypes=" + showAuxSubtypes);
        }
        Context context = this.mContext;
        boolean isScreenLocked = isScreenLocked();
        String lastInputMethodId = this.mSettings.getSelectedInputMethod();
        int lastInputMethodSubtypeId = this.mSettings.getSelectedInputMethodSubtypeId(lastInputMethodId);
        if (DEBUG) {
            Slog.v(TAG, "Current IME: " + lastInputMethodId);
        }
        synchronized (this.mMethodMap) {
            HashMap<InputMethodInfo, List<InputMethodSubtype>> immis = this.mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(this.mContext);
            if (immis == null || immis.size() == 0) {
            } else {
                hideInputMethodMenuLocked();
                List<ImeSubtypeListItem> imList = this.mSwitchingController.getSortedInputMethodAndSubtypeListLocked(showAuxSubtypes, isScreenLocked);
                if (lastInputMethodSubtypeId == -1) {
                    InputMethodSubtype currentSubtype = getCurrentInputMethodSubtypeLocked();
                    if (currentSubtype != null) {
                        lastInputMethodSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), currentSubtype.hashCode());
                    }
                }
                int N = imList.size();
                this.mIms = new InputMethodInfo[N];
                this.mSubtypeIds = new int[N];
                int checkedItem = 0;
                boolean mNeedfindInputMethod = true;
                for (int i = 0; i < N; i++) {
                    ImeSubtypeListItem item = (ImeSubtypeListItem) imList.get(i);
                    this.mIms[i] = item.mImi;
                    this.mSubtypeIds[i] = item.mSubtypeId;
                    if (this.mIms[i].getId().equals(lastInputMethodId)) {
                        if (mNeedfindInputMethod) {
                            checkedItem = i;
                        }
                        int subtypeId = this.mSubtypeIds[i];
                        if (subtypeId == -1 || ((lastInputMethodSubtypeId == -1 && subtypeId == 0) || subtypeId == lastInputMethodSubtypeId)) {
                            checkedItem = i;
                            mNeedfindInputMethod = false;
                        }
                    }
                }
                this.mDialogBuilder = new Builder(new ContextThemeWrapper(context, 201523202));
                this.mDialogBuilder.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        InputMethodManagerService.this.hideInputMethodMenu();
                    }
                });
                Context dialogContext = this.mDialogBuilder.getContext();
                TypedArray a = dialogContext.obtainStyledAttributes(null, R.styleable.DialogPreference, 16842845, 0);
                Drawable dialogIcon = a.getDrawable(2);
                a.recycle();
                this.mDialogBuilder.setIcon(dialogIcon);
                View tv = ((LayoutInflater) dialogContext.getSystemService(LayoutInflater.class)).inflate(201917490, null);
                this.mDialogBuilder.setCustomTitle(tv);
                this.mSwitchingDialogTitleView = tv;
                this.mSwitchingDialogTitleView.findViewById(16909194).setVisibility(this.mWindowManagerInternal.isHardKeyboardAvailable() ? 0 : 8);
                Switch hardKeySwitch = (Switch) this.mSwitchingDialogTitleView.findViewById(16909195);
                hardKeySwitch.setChecked(this.mShowImeWithHardKeyboard);
                hardKeySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        InputMethodManagerService.this.mSettings.setShowImeWithHardKeyboard(isChecked);
                        InputMethodManagerService.this.hideInputMethodMenu();
                    }
                });
                final ImeSubtypeListAdapter adapter = new ImeSubtypeListAdapter(dialogContext, 17367152, imList, checkedItem);
                this.mDialogBuilder.setSingleChoiceItems(adapter, checkedItem, new OnClickListener() {
                    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return;
     */
                    /* JADX WARNING: Missing block: B:22:0x005c, code:
            return;
     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized (InputMethodManagerService.this.mMethodMap) {
                            if (InputMethodManagerService.this.mIms != null && InputMethodManagerService.this.mIms.length > which) {
                                if (InputMethodManagerService.this.mSubtypeIds != null && InputMethodManagerService.this.mSubtypeIds.length > which) {
                                    InputMethodInfo im = InputMethodManagerService.this.mIms[which];
                                    int subtypeId = InputMethodManagerService.this.mSubtypeIds[which];
                                    adapter.mCheckedItem = which;
                                    adapter.notifyDataSetChanged();
                                    InputMethodManagerService.this.hideInputMethodMenu();
                                    if (im != null) {
                                        if (subtypeId < 0 || subtypeId >= im.getSubtypeCount()) {
                                            subtypeId = -1;
                                        }
                                        InputMethodManagerService.this.setInputMethodLocked(im.getId(), subtypeId);
                                    }
                                }
                            }
                        }
                    }
                });
                if (!isScreenLocked) {
                    this.mDialogBuilder.setPositiveButton(201590133, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            InputMethodManagerService.this.showConfigureInputMethods();
                        }
                    });
                }
                this.mSwitchingDialog = this.mDialogBuilder.create();
                this.mSwitchingDialog.setCanceledOnTouchOutside(true);
                this.mSwitchingDialog.getWindow().setType(2012);
                LayoutParams attributes = this.mSwitchingDialog.getWindow().getAttributes();
                attributes.privateFlags |= 16;
                this.mSwitchingDialog.getWindow().getAttributes().setTitle("Select input method");
                updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                this.mSwitchingDialog.show();
            }
        }
    }

    void hideInputMethodMenu() {
        synchronized (this.mMethodMap) {
            hideInputMethodMenuLocked();
        }
    }

    void hideInputMethodMenuLocked() {
        if (DEBUG) {
            Slog.v(TAG, "Hide switching menu");
        }
        if (this.mSwitchingDialog != null) {
            this.mSwitchingDialog.dismiss();
            this.mSwitchingDialog = null;
        }
        updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
        this.mDialogBuilder = null;
        this.mIms = null;
    }

    public boolean setInputMethodEnabled(String id, boolean enabled) {
        if (!calledFromValidUser()) {
            return false;
        }
        boolean inputMethodEnabledLocked;
        synchronized (this.mMethodMap) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Requires permission android.permission.WRITE_SECURE_SETTINGS");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                inputMethodEnabledLocked = setInputMethodEnabledLocked(id, enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return inputMethodEnabledLocked;
    }

    boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        if (((InputMethodInfo) this.mMethodMap.get(id)) == null) {
            throw new IllegalArgumentException("Unknown id: " + this.mCurMethodId);
        }
        List<Pair<String, ArrayList<String>>> enabledInputMethodsList = this.mSettings.getEnabledInputMethodsAndSubtypeListLocked();
        if (enabled) {
            for (Pair<String, ArrayList<String>> pair : enabledInputMethodsList) {
                if (((String) pair.first).equals(id)) {
                    return true;
                }
            }
            this.mSettings.appendAndPutEnabledInputMethodLocked(id, false);
            return false;
        }
        if (!this.mSettings.buildAndPutEnabledInputMethodsStrRemovingIdLocked(new StringBuilder(), enabledInputMethodsList, id)) {
            return false;
        }
        if (id.equals(this.mSettings.getSelectedInputMethod()) && !chooseNewDefaultIMELocked()) {
            Slog.i(TAG, "Can't find new IME, unsetting the current input method.");
            resetSelectedInputMethodAndSubtypeLocked(IElsaManager.EMPTY_PACKAGE);
        }
        return true;
    }

    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId, boolean setSubtypeOnly) {
        this.mSettings.saveCurrentInputMethodAndSubtypeToHistory(this.mCurMethodId, this.mCurrentSubtype);
        this.mCurUserActionNotificationSequenceNumber = Math.max(this.mCurUserActionNotificationSequenceNumber + 1, 1);
        if (DEBUG) {
            Slog.d(TAG, "Bump mCurUserActionNotificationSequenceNumber:" + this.mCurUserActionNotificationSequenceNumber);
        }
        if (!(this.mCurClient == null || this.mCurClient.client == null)) {
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(3040, this.mCurUserActionNotificationSequenceNumber, this.mCurClient));
        }
        if (imi == null || subtypeId < 0) {
            this.mSettings.putSelectedSubtype(-1);
            this.mCurrentSubtype = null;
        } else if (subtypeId < imi.getSubtypeCount()) {
            InputMethodSubtype subtype = imi.getSubtypeAt(subtypeId);
            this.mSettings.putSelectedSubtype(subtype.hashCode());
            this.mCurrentSubtype = subtype;
        } else {
            this.mSettings.putSelectedSubtype(-1);
            this.mCurrentSubtype = getCurrentInputMethodSubtypeLocked();
        }
        if (!setSubtypeOnly) {
            String id;
            InputMethodSettings inputMethodSettings = this.mSettings;
            if (imi != null) {
                id = imi.getId();
            } else {
                id = IElsaManager.EMPTY_PACKAGE;
            }
            inputMethodSettings.putSelectedInputMethod(id);
        }
    }

    private void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(newDefaultIme);
        int lastSubtypeId = -1;
        if (!(imi == null || TextUtils.isEmpty(newDefaultIme))) {
            String subtypeHashCode = this.mSettings.getLastSubtypeForInputMethodLocked(newDefaultIme);
            if (subtypeHashCode != null) {
                try {
                    lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(imi, Integer.parseInt(subtypeHashCode));
                } catch (NumberFormatException e) {
                    Slog.w(TAG, "HashCode for subtype looks broken: " + subtypeHashCode, e);
                }
            }
        }
        setSelectedInputMethodAndSubtypeLocked(imi, lastSubtypeId, false);
    }

    private Pair<InputMethodInfo, InputMethodSubtype> findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        InputMethodInfo mostApplicableIMI = null;
        InputMethodSubtype mostApplicableSubtype = null;
        boolean foundInSystemIME = false;
        for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
            String imiId = imi.getId();
            if (!foundInSystemIME || imiId.equals(this.mCurMethodId)) {
                ArrayList<InputMethodSubtype> subtypesForSearch;
                InputMethodSubtype subtype = null;
                List<InputMethodSubtype> enabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, mode, this.mCurrentSubtype.getLocale(), false);
                }
                if (subtype == null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, enabledSubtypes, mode, null, true);
                }
                ArrayList<InputMethodSubtype> overridingImplicitlyEnabledSubtypes = InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, mode);
                if (overridingImplicitlyEnabledSubtypes.isEmpty()) {
                    subtypesForSearch = InputMethodUtils.getSubtypes(imi);
                } else {
                    subtypesForSearch = overridingImplicitlyEnabledSubtypes;
                }
                if (subtype == null && this.mCurrentSubtype != null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, mode, this.mCurrentSubtype.getLocale(), false);
                }
                if (subtype == null) {
                    subtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, subtypesForSearch, mode, null, true);
                }
                if (subtype == null) {
                    continue;
                } else if (imiId.equals(this.mCurMethodId)) {
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    break;
                } else if (!foundInSystemIME) {
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    if ((imi.getServiceInfo().applicationInfo.flags & 1) != 0) {
                        foundInSystemIME = true;
                    }
                }
            }
        }
        if (DEBUG && mostApplicableIMI != null) {
            Slog.w(TAG, "Most applicable shortcut input method was:" + mostApplicableIMI.getId());
            if (mostApplicableSubtype != null) {
                Slog.w(TAG, "Most applicable shortcut input method subtype was:," + mostApplicableSubtype.getMode() + "," + mostApplicableSubtype.getLocale());
            }
        }
        if (mostApplicableIMI != null) {
            return new Pair(mostApplicableIMI, mostApplicableSubtype);
        }
        return null;
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        InputMethodSubtype currentInputMethodSubtypeLocked;
        synchronized (this.mMethodMap) {
            currentInputMethodSubtypeLocked = getCurrentInputMethodSubtypeLocked();
        }
        return currentInputMethodSubtypeLocked;
    }

    private InputMethodSubtype getCurrentInputMethodSubtypeLocked() {
        if (this.mCurMethodId == null) {
            return null;
        }
        boolean subtypeIsSelected = this.mSettings.isSubtypeSelected();
        InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
        if (imi == null || imi.getSubtypeCount() == 0) {
            return null;
        }
        if (!(subtypeIsSelected && this.mCurrentSubtype != null && InputMethodUtils.isValidSubtypeId(imi, this.mCurrentSubtype.hashCode()))) {
            int subtypeId = this.mSettings.getSelectedInputMethodSubtypeId(this.mCurMethodId);
            if (subtypeId == -1) {
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypes = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                if (explicitlyOrImplicitlyEnabledSubtypes.size() == 1) {
                    this.mCurrentSubtype = (InputMethodSubtype) explicitlyOrImplicitlyEnabledSubtypes.get(0);
                } else if (explicitlyOrImplicitlyEnabledSubtypes.size() > 1) {
                    this.mCurrentSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, explicitlyOrImplicitlyEnabledSubtypes, "keyboard", null, true);
                    if (this.mCurrentSubtype == null) {
                        this.mCurrentSubtype = InputMethodUtils.findLastResortApplicableSubtypeLocked(this.mRes, explicitlyOrImplicitlyEnabledSubtypes, null, null, true);
                    }
                }
            } else {
                this.mCurrentSubtype = (InputMethodSubtype) InputMethodUtils.getSubtypes(imi).get(subtypeId);
            }
        }
        return this.mCurrentSubtype;
    }

    /* JADX WARNING: Missing block: B:9:0x0024, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList();
            if (this.mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
            } else {
                for (InputMethodInfo imi : this.mShortcutInputMethodsAndSubtypes.keySet()) {
                    ret.add(imi);
                    for (InputMethodSubtype subtype : (ArrayList) this.mShortcutInputMethodsAndSubtypes.get(imi)) {
                        ret.add(subtype);
                    }
                }
                return ret;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x002f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (subtype != null) {
                if (this.mCurMethodId != null) {
                    int subtypeId = InputMethodUtils.getSubtypeIdFromHashCode((InputMethodInfo) this.mMethodMap.get(this.mCurMethodId), subtype.hashCode());
                    if (subtypeId != -1) {
                        setInputMethodLocked(this.mCurMethodId, subtypeId);
                        return true;
                    }
                }
            }
        }
    }

    private static String imeWindowStatusToString(int imeWindowVis) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if ((imeWindowVis & 1) != 0) {
            sb.append("Active");
            first = false;
        }
        if ((imeWindowVis & 2) != 0) {
            if (!first) {
                sb.append("|");
            }
            sb.append("Visible");
        }
        return sb.toString();
    }

    public IInputContentUriToken createInputContentUriToken(IBinder token, Uri contentUri, String packageName) {
        if (!calledFromValidUser()) {
            return null;
        }
        if (token == null) {
            throw new NullPointerException("token");
        } else if (packageName == null) {
            throw new NullPointerException("packageName");
        } else if (contentUri == null) {
            throw new NullPointerException("contentUri");
        } else {
            if ("content".equals(contentUri.getScheme())) {
                synchronized (this.mMethodMap) {
                    int uid = Binder.getCallingUid();
                    if (this.mCurMethodId == null) {
                        return null;
                    } else if (this.mCurToken != token) {
                        Slog.e(TAG, "Ignoring createInputContentUriToken mCurToken=" + this.mCurToken + " token=" + token);
                        return null;
                    } else if (TextUtils.equals(this.mCurAttribute.packageName, packageName)) {
                        InputContentUriTokenHandler inputContentUriTokenHandler = new InputContentUriTokenHandler(contentUri, uid, packageName, UserHandle.getUserId(uid), UserHandle.getUserId(this.mCurClient.uid));
                        return inputContentUriTokenHandler;
                    } else {
                        Slog.e(TAG, "Ignoring createInputContentUriToken mCurAttribute.packageName=" + this.mCurAttribute.packageName + " packageName=" + packageName);
                        return null;
                    }
                }
            }
            throw new InvalidParameterException("contentUri must have content scheme");
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump InputMethodManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (!dynamicallyConfigImeLogTag(fd, pw, args)) {
            ClientState client;
            ClientState focusedWindowClient;
            IInputMethod method;
            this.mArgs = args;
            this.mNextArg = 1;
            if (args != null && args.length > 0) {
                String option = args[0];
                if (option != null && option.length() > 0 && option.charAt(0) == '-') {
                    handleDebugCmd(fd, pw, option);
                    return;
                }
            }
            Printer p = new PrintWriterPrinter(pw);
            synchronized (this.mMethodMap) {
                p.println("Current Input Method Manager state:");
                int N = this.mMethodList.size();
                p.println("  Input Methods:");
                for (int i = 0; i < N; i++) {
                    InputMethodInfo info = (InputMethodInfo) this.mMethodList.get(i);
                    p.println("  InputMethod #" + i + ":");
                    info.dump(p, "    ");
                }
                p.println("  Clients:");
                for (ClientState ci : this.mClients.values()) {
                    p.println("  Client " + ci + ":");
                    p.println("    client=" + ci.client);
                    p.println("    inputContext=" + ci.inputContext);
                    p.println("    sessionRequested=" + ci.sessionRequested);
                    p.println("    curSession=" + ci.curSession);
                }
                p.println("  mCurMethodId=" + this.mCurMethodId);
                client = this.mCurClient;
                p.println("  mCurClient=" + client + " mCurSeq=" + this.mCurSeq);
                p.println("  mCurFocusedWindow=" + this.mCurFocusedWindow);
                focusedWindowClient = this.mCurFocusedWindowClient;
                p.println("  mCurFocusedWindowClient=" + focusedWindowClient);
                p.println("  mCurId=" + this.mCurId + " mHaveConnect=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod);
                p.println("  mCurToken=" + this.mCurToken);
                p.println("  mCurIntent=" + this.mCurIntent);
                method = this.mCurMethod;
                p.println("  mCurMethod=" + this.mCurMethod);
                p.println("  mEnabledSession=" + this.mEnabledSession);
                p.println("  mImeWindowVis=" + imeWindowStatusToString(this.mImeWindowVis));
                p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
                p.println("  mCurUserActionNotificationSequenceNumber=" + this.mCurUserActionNotificationSequenceNumber);
                p.println("  mSystemReady=" + this.mSystemReady + " mInteractive=" + this.mIsInteractive);
                p.println("  mSettingsObserver=" + this.mSettingsObserver);
                p.println("  mSwitchingController:");
                this.mSwitchingController.dump(p);
                p.println("  mSettings:");
                this.mSettings.dumpLocked(p, "    ");
            }
            p.println(" ");
            if (client != null) {
                pw.flush();
                try {
                    client.client.asBinder().dump(fd, args);
                } catch (RemoteException e) {
                    p.println("Input method client dead: " + e);
                }
            } else {
                p.println("No input method client.");
            }
            if (!(focusedWindowClient == null || client == focusedWindowClient)) {
                p.println(" ");
                p.println("Warning: Current input method client doesn't match the last focused. window.");
                p.println("Dumping input method client in the last focused window just in case.");
                p.println(" ");
                pw.flush();
                try {
                    focusedWindowClient.client.asBinder().dump(fd, args);
                } catch (RemoteException e2) {
                    p.println("Input method client in focused window dead: " + e2);
                }
            }
            p.println(" ");
            if (method != null) {
                pw.flush();
                try {
                    method.asBinder().dump(fd, args);
                } catch (RemoteException e22) {
                    p.println("Input method service dead: " + e22);
                }
            } else {
                p.println("No input method service.");
            }
        }
    }

    private void printUsage(PrintWriter pw) {
        pw.println("Input method manager service dump options:");
        pw.println("  [-d] [-h] [cmd] [option] ...");
        pw.println("  -d enable <zone>          enable the debug zone");
        pw.println("  -d disable <zone>         disable the debug zone");
        pw.println("       zone list:");
        pw.println("         0 : InputMethodManagerService");
        pw.println("         1 : InputMethodService");
        pw.println("         2 : InputMethodManager");
        pw.println("  -h                        print the dump usage");
    }

    private void handleDebugCmd(FileDescriptor fd, PrintWriter pw, String option) {
        if ("-d".equals(option)) {
            String action = nextArg();
            if ("enable".equals(action)) {
                runDebug(fd, pw, true);
            } else if ("disable".equals(action)) {
                runDebug(fd, pw, false);
            } else {
                printUsage(pw);
            }
        } else if ("-h".equals(option)) {
            printUsage(pw);
        } else {
            pw.println("Unknown argument: " + option + "; use -h for help");
        }
    }

    private void runDebug(FileDescriptor fd, PrintWriter pw, boolean enable) {
        String[] args = new String[1];
        while (true) {
            String type = nextArg();
            if (type == null) {
                return;
            }
            if ("0".equals(type)) {
                DEBUG = enable;
            } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(type)) {
                args[0] = enable ? "enable" : "disable";
                runInputMethodServiceDebug(fd, pw, args);
            } else if ("2".equals(type)) {
                args[0] = enable ? "enable" : "disable";
                runInputMethodManagerDebug(fd, pw, args);
            } else {
                printUsage(pw);
                return;
            }
        }
    }

    private void runInputMethodServiceDebug(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mCurMethod != null) {
            try {
                this.mCurMethod.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                pw.println("Input method client dead: " + e);
            }
        }
    }

    private void runInputMethodManagerDebug(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mCurClient != null) {
            try {
                this.mCurClient.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                pw.println("Input method client dead: " + e);
            }
        }
    }

    private String nextArg() {
        if (this.mNextArg >= this.mArgs.length) {
            return null;
        }
        String[] strArr = this.mArgs;
        int i = this.mNextArg;
        this.mNextArg = i + 1;
        return strArr[i];
    }

    public void switchInputMethodFromWindowManager(boolean isForward) {
        if (DEBUG) {
            Slog.d(TAG, "switch input method from WindowManager: " + isForward);
        }
        synchronized (this.mMethodMap) {
            if (isForward) {
                this.index1++;
            } else {
                this.index1--;
            }
            if (this.index1 == 1 || this.index1 == -1) {
                this.index2 = this.index1;
                if (this.mTimer != null) {
                    this.mTimer.purge();
                }
                this.mTimer = new Timer();
                this.mTimer.schedule(new SwitchImeTask(this, null), 500);
            }
        }
    }

    public void sendCharacterToCurClient(int unicode) {
        if (this.mCurClient != null && this.mCurClient.client != null) {
            try {
                this.mCurClient.client.sendCharacter(unicode);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected boolean dynamicallyConfigImeLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length < 1) {
            return false;
        }
        String cmd = args[0];
        boolean isImeLog = "log".equals(cmd);
        boolean isDebugStateQuery = "debug_switch".equals(cmd);
        if (isImeLog) {
            if (args.length != 3) {
                pw.println("Invalid argument! Get detail help as bellow:");
                logOutImeLogTagHelp(pw);
                return true;
            }
            doConfigImeLog(fd, pw, args);
            return true;
        } else if (!isDebugStateQuery) {
            return false;
        } else {
            dumpDynamicallyLogSwitch(pw, args);
            return true;
        }
    }

    private void dumpDynamicallyLogSwitch(PrintWriter pw, String[] args) {
        pw.println("  all=" + DEBUG);
    }

    private void doConfigImeLog(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("doConfigImeLog, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("doConfigImeLog, args[" + index + "]:" + args[index]);
        }
        String logCategoryTag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("doConfigImeLog, logCategoryTag:" + logCategoryTag + ", on:" + on);
        Slog.d(TAG, "doConfigImeLog, logCategoryTag:" + logCategoryTag + ", on:" + on);
        if ("all".equals(logCategoryTag)) {
            DEBUG = on;
            DEBUG_RESTORE = DEBUG;
            DEBUG_IME_ACTIVE = DEBUG;
            DEBUG_IME_ACTIVE_LIGHT = DEBUG;
            DEBUG_SHOW_SOFTINPUT = DEBUG;
            SystemProperties.set(PROP_IMELOG, on ? "true" : "false");
            configInputMehtodServiceLogTag(fd, pw, args);
            configImeClientLogTag(fd, pw, args);
        } else if ("imms".equals(logCategoryTag)) {
            DEBUG = on;
            DEBUG_RESTORE = DEBUG;
            DEBUG_IME_ACTIVE = DEBUG;
            DEBUG_IME_ACTIVE_LIGHT = DEBUG;
            DEBUG_SHOW_SOFTINPUT = DEBUG;
            SystemProperties.set(PROP_IMELOG, on ? "true" : "false");
        } else if ("ims".equals(logCategoryTag)) {
            SystemProperties.set(PROP_IMELOG, on ? "true" : "false");
            configInputMehtodServiceLogTag(fd, pw, args);
        } else if ("client".equals(logCategoryTag)) {
            SystemProperties.set(PROP_IMELOG, on ? "true" : "false");
            configImeClientLogTag(fd, pw, args);
        } else if (!"testrecon".equals(logCategoryTag)) {
            pw.println("Invalid log tag argument! Get detail help as bellow:");
            logOutImeLogTagHelp(pw);
        } else if (this.mReconnectWatchDog != null) {
            Slog.i(TAG, "testrecon...");
            this.mReconnectWatchDog.sendReconnectTimeoutMsg();
        }
    }

    private void configInputMehtodServiceLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mCurMethod != null) {
            try {
                this.mCurMethod.asBinder().dump(fd, args);
                return;
            } catch (RemoteException e) {
                pw.println("Input method client dead: " + e);
                return;
            }
        }
        pw.println("No input methd service.");
    }

    private void configImeClientLogTag(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mCurClient == null) {
            pw.println("No input client");
        } else if (this.mCurClient.client != null) {
            try {
                this.mCurClient.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                pw.println("Input client dead: " + e);
            }
        } else {
            pw.println("Current input client has error:mCurClient.client null");
        }
    }

    protected void logOutImeLogTagHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1 All ime log:IMMS, IMS, Ime2App");
        pw.println("cmd: dumpsys input_method log all 0/1");
        pw.println("----------------------------------");
        pw.println("2 IMMS(InputMethodManagerService) log");
        pw.println("cmd: dumpsys input_method log imms 0/1");
        pw.println("----------------------------------");
        pw.println("3 IMS(InputMethdService)");
        pw.println("cmd: dumpsys input_method log ims 0/1");
        pw.println("----------------------------------");
        pw.println("4 log between ime and app");
        pw.println("cmd: dumpsys input_method log client 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }
}

package android.telephony;

import android.os.SystemProperties;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import com.android.i18n.phonenumbers.AsYouTypeFormatter;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import java.util.Locale;

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
public class PhoneNumberFormattingTextWatcher implements TextWatcher {
    private static final int TEST_MODE_CTA = 1;
    private static final int TEST_MODE_FTA = 2;
    private static final int TEST_MODE_IOT = 3;
    private static String TEST_MODE_PROPERTY_KEY = null;
    private static final int TEST_MODE_UNKNOWN = -1;
    private AsYouTypeFormatter mFormatter;
    private boolean mSelfChange;
    private boolean mStopFormatting;
    private int testMode;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberFormattingTextWatcher.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.PhoneNumberFormattingTextWatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberFormattingTextWatcher.<clinit>():void");
    }

    public PhoneNumberFormattingTextWatcher() {
        this(Locale.getDefault().getCountry());
    }

    public PhoneNumberFormattingTextWatcher(String countryCode) {
        this.mSelfChange = false;
        this.testMode = 0;
        getTestMode();
        if (countryCode == null) {
            throw new IllegalArgumentException();
        }
        this.mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!this.mSelfChange && !this.mStopFormatting && count > 0 && count > after && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!this.mSelfChange && !this.mStopFormatting && count > 0 && count > before && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    public synchronized void afterTextChanged(Editable s) {
        boolean z = true;
        synchronized (this) {
            if (this.testMode == 2) {
            } else if (this.mStopFormatting) {
                if (s.length() == 0) {
                    z = false;
                }
                this.mStopFormatting = z;
            } else if (this.mSelfChange) {
            } else {
                String formatted = reformat(s, Selection.getSelectionEnd(s));
                if (formatted != null) {
                    int rememberedPos = this.mFormatter.getRememberedPosition();
                    this.mSelfChange = true;
                    s.replace(0, s.length(), formatted, 0, formatted.length());
                    if (formatted.equals(s.toString())) {
                        Selection.setSelection(s, rememberedPos);
                    }
                    this.mSelfChange = false;
                }
                PhoneNumberUtils.ttsSpanAsPhoneNumber(s, 0, s.length());
            }
        }
    }

    private String reformat(CharSequence s, int cursor) {
        int curIndex = cursor - 1;
        String formatted = null;
        this.mFormatter.clear();
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == curIndex) {
                hasCursor = true;
            }
        }
        if (lastNonSeparator != 0) {
            return getFormattedNumber(lastNonSeparator, hasCursor);
        }
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        if (hasCursor) {
            return this.mFormatter.inputDigitAndRememberPosition(lastNonSeparator);
        }
        return this.mFormatter.inputDigit(lastNonSeparator);
    }

    private void stopFormatting() {
        this.mStopFormatting = true;
        this.mFormatter.clear();
    }

    private boolean hasSeparator(CharSequence s, int start, int count) {
        for (int i = start; i < start + count; i++) {
            if (!PhoneNumberUtils.isNonSeparator(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void getTestMode() {
        try {
            this.testMode = Integer.valueOf(SystemProperties.get(TEST_MODE_PROPERTY_KEY)).intValue();
        } catch (Exception e) {
            Rlog.d("getTestMode", "Invalid property value");
            this.testMode = -1;
        }
        Rlog.d("getTestMode", "Test mode is " + this.testMode);
    }
}

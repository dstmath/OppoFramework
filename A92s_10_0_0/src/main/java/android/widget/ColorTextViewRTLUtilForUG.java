package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.text.Layout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import com.coloros.deepthinker.AlgorithmBinderCode;
import java.util.Arrays;
import java.util.Locale;

public class ColorTextViewRTLUtilForUG implements IColorTextViewRTLUtilForUG {
    private static volatile ColorTextViewRTLUtilForUG sInstance = null;
    public boolean hasInit;
    public boolean mForceAnyRtl;
    public boolean mForceViewStart;
    private Locale mLastUpdateLocale;
    public boolean mSupportRtl;

    public static ColorTextViewRTLUtilForUG getInstance() {
        if (sInstance == null) {
            synchronized (ColorTextViewRTLUtilForUG.class) {
                if (sInstance == null) {
                    sInstance = new ColorTextViewRTLUtilForUG();
                }
            }
        }
        return sInstance;
    }

    ColorTextViewRTLUtilForUG() {
        this.mLastUpdateLocale = Locale.ENGLISH;
        this.hasInit = false;
        this.mSupportRtl = true;
        this.mForceAnyRtl = false;
        this.mForceViewStart = false;
        this.mLastUpdateLocale = Locale.getDefault();
    }

    public void initRtlParameter(Resources res) {
        String[] locales;
        if (!this.hasInit && res != null && res.getConfiguration() != null && res.getConfiguration().locale != null) {
            String sysLocale = SystemProperties.get("persist.sys.locale", "zh_CN");
            String appLocale = res.getConfiguration().locale.toString();
            if (sysLocale != null && sysLocale.equalsIgnoreCase("ug-CN")) {
                this.mForceAnyRtl = true;
                if (!(appLocale == null || !appLocale.equalsIgnoreCase("ug_CN") || (locales = res.getAssets().getNonSystemLocales()) == null)) {
                    if (locales.length <= 0) {
                        this.mSupportRtl = false;
                    } else if (Arrays.asList(locales).contains("ug-CN")) {
                        this.mForceViewStart = true;
                    } else {
                        this.mSupportRtl = false;
                    }
                }
            }
            this.hasInit = true;
        }
    }

    public boolean getOppoSupportRtl() {
        return this.mSupportRtl;
    }

    public boolean getDirectionAnyRtl() {
        return this.mForceAnyRtl;
    }

    public boolean getTextViewStart() {
        return this.mForceViewStart;
    }

    public Layout.Alignment getLayoutAlignmentForTextView(Layout.Alignment alignment, Context context, TextView textView) {
        Layout.Alignment alignment2;
        boolean forceViewStart = getTextViewStart();
        switch (textView.getTextAlignment()) {
            case 1:
                int gravity = textView.getGravity() & 8388615;
                if (gravity == 1) {
                    return Layout.Alignment.ALIGN_CENTER;
                }
                if (gravity == 3) {
                    return Layout.Alignment.ALIGN_LEFT;
                }
                if (gravity == 5) {
                    return Layout.Alignment.ALIGN_RIGHT;
                }
                if (gravity == 8388611) {
                    return forceViewStart ? Layout.Alignment.ALIGN_RIGHT : Layout.Alignment.ALIGN_NORMAL;
                } else if (gravity != 8388613) {
                    return forceViewStart ? Layout.Alignment.ALIGN_RIGHT : Layout.Alignment.ALIGN_NORMAL;
                } else {
                    return forceViewStart ? Layout.Alignment.ALIGN_LEFT : Layout.Alignment.ALIGN_OPPOSITE;
                }
            case 2:
                return Layout.Alignment.ALIGN_NORMAL;
            case 3:
                return Layout.Alignment.ALIGN_OPPOSITE;
            case 4:
                return Layout.Alignment.ALIGN_CENTER;
            case AlgorithmBinderCode.BIND_EVENT_HANDLE /*{ENCODED_INT: 5}*/:
                return textView.getLayoutDirection() == 1 ? Layout.Alignment.ALIGN_RIGHT : Layout.Alignment.ALIGN_LEFT;
            case 6:
                return textView.getLayoutDirection() == 1 ? Layout.Alignment.ALIGN_LEFT : Layout.Alignment.ALIGN_RIGHT;
            default:
                if (forceViewStart) {
                    alignment2 = Layout.Alignment.ALIGN_RIGHT;
                } else {
                    alignment2 = Layout.Alignment.ALIGN_NORMAL;
                }
                return alignment2;
        }
    }

    public TextDirectionHeuristic getTextDirectionHeuristicForTextView(boolean defaultIsRtl) {
        if (getDirectionAnyRtl()) {
            return TextDirectionHeuristics.ANYRTL_LTR;
        }
        if (defaultIsRtl) {
            return TextDirectionHeuristics.FIRSTSTRONG_RTL;
        }
        return TextDirectionHeuristics.FIRSTSTRONG_LTR;
    }

    public boolean hasRtlSupportForView(Context context) {
        return getOppoSupportRtl() && context.getApplicationInfo().hasRtlSupport();
    }

    public void updateRtlParameterForUG(Resources res, Configuration newConfig) {
        if (res != null && res.getAssets() != null) {
            updateRtlParameterForUG(res.getAssets().getNonSystemLocales(), newConfig);
        }
    }

    public void updateRtlParameterForUG(String[] availableLocales, Configuration newConfig) {
        if (this.hasInit && availableLocales != null && newConfig != null && newConfig.locale != null && !newConfig.locale.equals(this.mLastUpdateLocale)) {
            this.mForceAnyRtl = false;
            this.mForceViewStart = false;
            this.mSupportRtl = true;
            String newLocale = newConfig.locale.toLanguageTag();
            if (newLocale != null && newLocale.equalsIgnoreCase("ug-CN")) {
                this.mForceAnyRtl = true;
                if (availableLocales != null) {
                    if (availableLocales.length <= 0) {
                        this.mSupportRtl = false;
                    } else if (Arrays.asList(availableLocales).contains("ug-CN")) {
                        this.mForceViewStart = true;
                    } else {
                        this.mSupportRtl = false;
                    }
                }
            }
            this.mLastUpdateLocale = newConfig.locale;
            this.hasInit = true;
        }
    }
}

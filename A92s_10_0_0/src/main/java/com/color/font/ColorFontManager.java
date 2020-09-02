package com.color.font;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.widget.TextView;
import com.color.util.OppoFontUtils;
import java.io.File;

public class ColorFontManager implements IColorFontManager {
    private static final String TAG = "ColorFontManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static volatile ColorFontManager sInstance = null;
    private boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private boolean mDynamicDebug = false;

    public static ColorFontManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorFontManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorFontManager();
                }
            }
        }
        return sInstance;
    }

    private ColorFontManager() {
    }

    public void createFontLink(String pkgName) {
        printLog("createFontLink");
        OppoFontUtils.createFontLink(pkgName);
    }

    public void deleteFontLink(String pkgName) {
        printLog("deleteFontLink");
        OppoFontUtils.deleteFontLink(pkgName);
    }

    public void handleFactoryReset() {
        printLog("handleFactoryReset");
        OppoFontUtils.handleFactoryReset();
    }

    public Typeface flipTypeface(Typeface typeface) {
        return OppoFontUtils.flipTypeface(typeface);
    }

    public boolean isFlipFontUsed() {
        return OppoFontUtils.isFlipFontUsed;
    }

    public void setCurrentAppName(String pkgName) {
        OppoFontUtils.setAppTypeFace(pkgName);
    }

    public void setFlipFont(Configuration config, int changes) {
        if ((33554432 & changes) != 0) {
            OppoFontUtils.setFlipFont(config);
        }
    }

    public void setFlipFontWhenUserChange(Configuration config, int changes) {
        if ((536870912 & changes) != 0) {
            OppoFontUtils.setFlipFontWhenUserChange(config);
        }
    }

    public void replaceFakeBoldToColorMedium(TextView textView, Typeface typeface, int style) {
        OppoFontUtils.replaceFakeBoldToColorMedium(textView, typeface, style);
    }

    public void updateTypefaceInCurrProcess(Configuration config) {
        OppoFontUtils.updateTypefaceInCurrProcess(config);
    }

    private void printLog(String msg) {
        if (this.DEBUG_SWITCH) {
            Slog.d(TAG, "[impl] " + msg);
        }
    }

    public void onCleanupUserForFont(int userId) {
        if (userId != 0) {
            File fontFileForUser = new File(OppoFontUtils.DATA_FONT_DIRECTORY + userId);
            if (fontFileForUser.exists()) {
                boolean cleanUserFontResult = FileUtils.deleteContentsAndDir(fontFileForUser);
                if (this.DEBUG_SWITCH) {
                    Log.v(TAG, "onCleanupUserForFont result :" + cleanUserFontResult);
                }
            }
        }
    }
}

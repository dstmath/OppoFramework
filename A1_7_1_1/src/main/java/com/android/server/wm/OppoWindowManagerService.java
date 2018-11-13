package com.android.server.wm;

import android.app.IColorKeyguardSessionCallback;
import android.app.IColorKeyguardSessionCallback.Stub;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.MagnificationSpec;
import android.view.OppoWindowManagerImplHelper;
import android.view.OppoWindowManagerPolicy;
import android.view.WindowManagerInternal;
import com.android.server.LocalServices;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.input.InputManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoWindowManagerService extends WindowManagerService {
    private static final int EXPAND_HEIGHT = 96;
    private static final String FACEBOOK_PKG_NAME = "com.facebook.katana";
    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String TAG = "OppoWindowManagerService";
    private Context mContext;
    private final ColorLongshotHelper mLongshotHelper;
    private OppoKeyguardListHelper mOppoKeyguardListHelper = OppoKeyguardListHelper.getInstance();

    OppoWindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        super(context, inputManager, haveInputMethods, showBootMsgs, onlyCore);
        this.mLongshotHelper = new ColorLongshotHelper(context, this);
        this.mContext = context;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mLongshotHelper.onTransact(code, data, reply, flags)) {
            return true;
        }
        boolean result;
        switch (code) {
            case 10007:
                data.enforceInterface("android.view.IWindowManager");
                showStatusBar();
                reply.writeNoException();
                return true;
            case 10008:
                data.enforceInterface("android.view.IWindowManager");
                rm_add_StatusBarRunnable(data.readInt());
                reply.writeNoException();
                return true;
            case 10010:
                data.enforceInterface("android.view.IWindowManager");
                result = isInputShow();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10011:
                data.enforceInterface("android.view.IWindowManager");
                result = isFullScreen();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10012:
                data.enforceInterface("android.view.IWindowManager");
                result = isStatusBarVisible();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10013:
                data.enforceInterface("android.view.IWindowManager");
                result = isRotatingLw();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10014:
                data.enforceInterface("android.view.IWindowManager");
                result = checkIsFloatWindowForbidden(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10015:
                MagnificationSpec spec;
                data.enforceInterface("android.view.IWindowManager");
                if (data.readInt() != 0) {
                    spec = (MagnificationSpec) MagnificationSpec.CREATOR.createFromParcel(data);
                } else {
                    spec = null;
                }
                setMagnificationSpecEx(spec);
                reply.writeNoException();
                return true;
            case 10017:
                data.enforceInterface("android.view.IWindowManager");
                requestDismissKeyguard();
                reply.writeNoException();
                return true;
            case 10018:
                data.enforceInterface("android.view.IWindowManager");
                result = isWindowShownForUid(data.readInt());
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10019:
                data.enforceInterface("android.view.IWindowManager");
                requestKeyguard(data.readString());
                reply.writeNoException();
                return true;
            case 10020:
                data.enforceInterface("android.view.IWindowManager");
                result = openKeyguardSession(Stub.asInterface(data.readStrongBinder()), data.readStrongBinder(), data.readString());
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 10022:
                data.enforceInterface("android.view.IWindowManager");
                removeWindowShownOnKeyguard();
                reply.writeNoException();
                return true;
            case 10025:
                data.enforceInterface("android.view.IWindowManager");
                String currentFocus = getCurrentFocus();
                reply.writeNoException();
                reply.writeString(currentFocus);
                return true;
            case 10030:
                data.enforceInterface("android.view.IWindowManager");
                Rect result2 = getFloatWindowRect(data.readInt());
                reply.writeNoException();
                if (result2 != null) {
                    reply.writeInt(1);
                    result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mLongshotHelper.setFileDescriptor(fd);
        super.dump(fd, pw, args);
        this.mLongshotHelper.setFileDescriptor(null);
    }

    boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        if (this.mLongshotHelper.dumpWindows(pw, name)) {
            return true;
        }
        return super.dumpWindows(pw, name, args, opti, dumpAll);
    }

    public boolean isInputShow() {
        if (this.mInputMethodWindow != null) {
            return this.mInputMethodWindow.mHasSurface;
        }
        return false;
    }

    public boolean isFullScreen() {
        if (this.mCurrentFocus != null) {
            String pids = getActiveAudioPids();
            if (pids == null || !pids.contains(Integer.toString(this.mCurrentFocus.mSession.mPid))) {
                return false;
            }
            WindowList list = this.mCurrentFocus.mChildWindows;
            if (list != null) {
                DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
                int screenWidth = dm.widthPixels;
                int screenHeight = dm.heightPixels;
                if (list.size() <= 0) {
                    return this.mCurrentFocus.mHasSurface && this.mCurrentFocus.getContentFrameLw().width() == screenWidth && this.mCurrentFocus.getContentFrameLw().height() == screenHeight;
                } else {
                    int i = 0;
                    while (i < list.size()) {
                        if (((WindowState) list.get(i)).mHasSurface && ((WindowState) list.get(i)).getContentFrameLw().width() == screenWidth && ((WindowState) list.get(i)).getContentFrameLw().height() == screenHeight) {
                            return true;
                        }
                        i++;
                    }
                }
            }
        }
    }

    private String getActiveAudioPids() {
        String pids = ((AudioManager) this.mContext.getSystemService("audio")).getParameters("get_pid");
        if (pids == null || pids.length() == 0) {
            return null;
        }
        return pids;
    }

    public boolean checkIsFloatWindowForbidden(String packageName, int type) {
        return OppoWindowManagerImplHelper.checkIsFloatWindowForbidden(packageName, type);
    }

    public void setMagnificationSpecEx(MagnificationSpec spec) {
        if (spec != null) {
            ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).setMagnificationSpec(spec);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0040, code:
            r30 = r29.getDisplayInfo();
            r31 = r30.logicalWidth;
            r28 = r30.logicalHeight;
     */
    /* JADX WARNING: Missing block: B:13:0x0050, code:
            if (r31 == 0) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:14:0x0052, code:
            if (r28 != 0) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:16:0x0056, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x008e;
     */
    /* JADX WARNING: Missing block: B:17:0x0058, code:
            android.util.Slog.i("WindowManager", "Screenshot of " + r54 + ": returning null. logical widthxheight=" + r31 + "x" + r28);
     */
    /* JADX WARNING: Missing block: B:19:0x008f, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:23:0x0093, code:
            r41 = 0;
            r34 = new android.graphics.Rect();
            r46 = new android.graphics.Rect();
     */
    /* JADX WARNING: Missing block: B:24:0x00a5, code:
            if (getDockedStackSide() != -1) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:26:0x00ab, code:
            if (r53.dismissDockedStackFromHome != false) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:27:0x00ad, code:
            r38 = r53.mSplitFormBack;
     */
    /* JADX WARNING: Missing block: B:28:0x00b3, code:
            if (r54 != null) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:29:0x00b5, code:
            if (r61 == false) goto L_0x014b;
     */
    /* JADX WARNING: Missing block: B:30:0x00b7, code:
            r45 = false;
            r8 = Integer.MAX_VALUE;
     */
    /* JADX WARNING: Missing block: B:31:0x00bc, code:
            r7 = r53.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:32:0x00c2, code:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:35:0x00c7, code:
            if (r53.mInputMethodTarget == null) goto L_0x0150;
     */
    /* JADX WARNING: Missing block: B:36:0x00c9, code:
            r36 = r53.mInputMethodTarget.mAppToken;
     */
    /* JADX WARNING: Missing block: B:37:0x00d1, code:
            if (r36 == null) goto L_0x0157;
     */
    /* JADX WARNING: Missing block: B:39:0x00d7, code:
            if (r36.appToken == null) goto L_0x0157;
     */
    /* JADX WARNING: Missing block: B:41:0x00e3, code:
            if (r36.appToken.asBinder() != r54) goto L_0x0157;
     */
    /* JADX WARNING: Missing block: B:43:0x00ed, code:
            if (r53.mInputMethodTarget.isInMultiWindowMode() == false) goto L_0x0154;
     */
    /* JADX WARNING: Missing block: B:44:0x00ef, code:
            r37 = false;
     */
    /* JADX WARNING: Missing block: B:45:0x00f1, code:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:46:0x00f2, code:
            r20 = ((r53.mPolicy.windowTypeToLayerLw(2) + 1) * 10000) + 1000;
            r14 = r53.mWindowMap;
     */
    /* JADX WARNING: Missing block: B:47:0x0107, code:
            monitor-enter(r14);
     */
    /* JADX WARNING: Missing block: B:48:0x0108, code:
            r22 = null;
     */
    /* JADX WARNING: Missing block: B:50:?, code:
            r51 = r29.getWindowList();
            r11 = getDefaultDisplayContentLocked().getDisplay().getRotation();
     */
    /* JADX WARNING: Missing block: B:51:0x011b, code:
            if (r11 == 1) goto L_0x0120;
     */
    /* JADX WARNING: Missing block: B:53:0x011e, code:
            if (r11 != 3) goto L_0x0124;
     */
    /* JADX WARNING: Missing block: B:55:0x0121, code:
            if (r11 != 1) goto L_0x015d;
     */
    /* JADX WARNING: Missing block: B:56:0x0123, code:
            r11 = 3;
     */
    /* JADX WARNING: Missing block: B:57:0x0124, code:
            r26 = r53.mStatusBarHeight;
            r27 = 0;
            r35 = r51.size() - 1;
     */
    /* JADX WARNING: Missing block: B:58:0x0132, code:
            if (r35 < 0) goto L_0x01cb;
     */
    /* JADX WARNING: Missing block: B:59:0x0134, code:
            r52 = (com.android.server.wm.WindowState) r51.get(r35);
     */
    /* JADX WARNING: Missing block: B:60:0x0142, code:
            if (r52.mHasSurface != false) goto L_0x015f;
     */
    /* JADX WARNING: Missing block: B:61:0x0144, code:
            r35 = r35 - 1;
     */
    /* JADX WARNING: Missing block: B:62:0x0147, code:
            r38 = true;
     */
    /* JADX WARNING: Missing block: B:63:0x014b, code:
            r45 = true;
            r8 = 0;
     */
    /* JADX WARNING: Missing block: B:64:0x0150, code:
            r36 = null;
     */
    /* JADX WARNING: Missing block: B:65:0x0154, code:
            r37 = true;
     */
    /* JADX WARNING: Missing block: B:66:0x0157, code:
            r37 = false;
     */
    /* JADX WARNING: Missing block: B:70:0x015d, code:
            r11 = 1;
     */
    /* JADX WARNING: Missing block: B:73:0x0165, code:
            if (r52.mLayer >= r20) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:74:0x0167, code:
            if (r61 == false) goto L_0x016f;
     */
    /* JADX WARNING: Missing block: B:76:0x016d, code:
            if (r52.mIsWallpaper == false) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:78:0x0173, code:
            if (r52.mIsImWindow == false) goto L_0x01f2;
     */
    /* JADX WARNING: Missing block: B:79:0x0175, code:
            if (r37 == false) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:80:0x0177, code:
            r50 = r52.mWinAnimator;
            r39 = r50.mSurfaceController.getLayer();
     */
    /* JADX WARNING: Missing block: B:81:0x0189, code:
            if (r41 >= r39) goto L_0x018d;
     */
    /* JADX WARNING: Missing block: B:82:0x018b, code:
            r41 = r39;
     */
    /* JADX WARNING: Missing block: B:84:0x018f, code:
            if (r8 <= r39) goto L_0x0193;
     */
    /* JADX WARNING: Missing block: B:85:0x0191, code:
            r8 = r39;
     */
    /* JADX WARNING: Missing block: B:87:0x0197, code:
            if (r52.mIsWallpaper != false) goto L_0x019f;
     */
    /* JADX WARNING: Missing block: B:89:0x019d, code:
            if (r52.mIsImWindow == false) goto L_0x0216;
     */
    /* JADX WARNING: Missing block: B:91:0x01a3, code:
            if (r52.mAppToken == null) goto L_0x039b;
     */
    /* JADX WARNING: Missing block: B:93:0x01ad, code:
            if (r52.mAppToken.token != r54) goto L_0x039b;
     */
    /* JADX WARNING: Missing block: B:94:0x01af, code:
            r33 = true;
     */
    /* JADX WARNING: Missing block: B:95:0x01b1, code:
            if (r33 == false) goto L_0x01c1;
     */
    /* JADX WARNING: Missing block: B:97:0x01b7, code:
            if (r52.isDisplayedLw() == false) goto L_0x01c1;
     */
    /* JADX WARNING: Missing block: B:99:0x01bd, code:
            if (r50.getShown() == false) goto L_0x01c1;
     */
    /* JADX WARNING: Missing block: B:100:0x01bf, code:
            r45 = true;
     */
    /* JADX WARNING: Missing block: B:102:0x01c9, code:
            if (r52.isObscuringFullscreen(r30) == false) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:103:0x01cb, code:
            if (r54 == null) goto L_0x03a5;
     */
    /* JADX WARNING: Missing block: B:104:0x01cd, code:
            if (r22 != null) goto L_0x03a5;
     */
    /* JADX WARNING: Missing block: B:106:0x01d1, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x01ef;
     */
    /* JADX WARNING: Missing block: B:107:0x01d3, code:
            android.util.Slog.i("WindowManager", "Screenshot: Couldn't find a surface matching " + r54);
     */
    /* JADX WARNING: Missing block: B:109:0x01f0, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:110:0x01f1, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:113:0x01f6, code:
            if (r52.mIsWallpaper == false) goto L_0x0200;
     */
    /* JADX WARNING: Missing block: B:114:0x01f8, code:
            if (r61 == false) goto L_0x01fc;
     */
    /* JADX WARNING: Missing block: B:115:0x01fa, code:
            r22 = r52;
     */
    /* JADX WARNING: Missing block: B:116:0x01fc, code:
            if (r22 != null) goto L_0x0177;
     */
    /* JADX WARNING: Missing block: B:117:0x0200, code:
            if (r54 == null) goto L_0x0177;
     */
    /* JADX WARNING: Missing block: B:119:0x0206, code:
            if (r52.mAppToken == null) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:121:0x0210, code:
            if (r52.mAppToken.token != r54) goto L_0x0144;
     */
    /* JADX WARNING: Missing block: B:122:0x0212, code:
            r22 = r52;
     */
    /* JADX WARNING: Missing block: B:123:0x0216, code:
            if (r38 != false) goto L_0x0225;
     */
    /* JADX WARNING: Missing block: B:125:0x0223, code:
            if (FACEBOOK_PKG_NAME.equals(r52.getOwningPackage()) == false) goto L_0x0301;
     */
    /* JADX WARNING: Missing block: B:126:0x0225, code:
            r48 = r52.mFrame;
            r25 = r52.mContentInsets;
            r34.union(r48.left + r25.left, r48.top + r25.top, r48.right - r25.right, r48.bottom - r25.bottom);
     */
    /* JADX WARNING: Missing block: B:127:0x0266, code:
            r52.getVisibleBounds(r46);
     */
    /* JADX WARNING: Missing block: B:128:0x026f, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x02ed;
     */
    /* JADX WARNING: Missing block: B:129:0x0271, code:
            android.util.Slog.d("WindowManager", "Screenshot: ws:" + r52 + " stackBounds:" + r46 + " frame:" + r34 + " systemUiVisibility:" + java.lang.Integer.toBinaryString(r52.getSystemUiVisibility()) + " ws.mAttrs.flags:" + java.lang.Integer.toBinaryString(r52.mAttrs.flags) + " ws.mFrame:" + r52.mFrame + " ws.mContentInsets:" + r52.mContentInsets);
     */
    /* JADX WARNING: Missing block: B:130:0x02ed, code:
            if (r38 == false) goto L_0x019f;
     */
    /* JADX WARNING: Missing block: B:132:0x02f7, code:
            if (android.graphics.Rect.intersects(r34, r46) != false) goto L_0x019f;
     */
    /* JADX WARNING: Missing block: B:133:0x02f9, code:
            r34.setEmpty();
     */
    /* JADX WARNING: Missing block: B:137:0x0301, code:
            r43 = r31;
            r23 = r28;
     */
    /* JADX WARNING: Missing block: B:140:0x030e, code:
            if (r53.mPolicy.getNavigationBarStatus() == 2) goto L_0x0329;
     */
    /* JADX WARNING: Missing block: B:142:0x0314, code:
            if (r31 >= r28) goto L_0x0375;
     */
    /* JADX WARNING: Missing block: B:143:0x0316, code:
            r23 = r53.mPolicy.getNonDecorDisplayHeight(r31, r28, 0, r53.mCurConfiguration.uiMode);
     */
    /* JADX WARNING: Missing block: B:145:0x0333, code:
            if ((r52.mAttrs.flags & 1024) != 1024) goto L_0x0344;
     */
    /* JADX WARNING: Missing block: B:147:0x0337, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x0342;
     */
    /* JADX WARNING: Missing block: B:148:0x0339, code:
            android.util.Slog.d("WindowManager", "Full screen window, do not crop status bar");
     */
    /* JADX WARNING: Missing block: B:149:0x0342, code:
            r26 = 0;
     */
    /* JADX WARNING: Missing block: B:151:0x034c, code:
            if ((r52.getSystemUiVisibility() & 16384) != 16384) goto L_0x0363;
     */
    /* JADX WARNING: Missing block: B:153:0x0350, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x035b;
     */
    /* JADX WARNING: Missing block: B:154:0x0352, code:
            android.util.Slog.d("WindowManager", "16:9 window on 18:9 device, crop expand area");
     */
    /* JADX WARNING: Missing block: B:156:0x035f, code:
            if (r31 >= r28) goto L_0x0389;
     */
    /* JADX WARNING: Missing block: B:157:0x0361, code:
            r26 = 96;
     */
    /* JADX WARNING: Missing block: B:159:0x0364, code:
            if (r11 != 1) goto L_0x038c;
     */
    /* JADX WARNING: Missing block: B:160:0x0366, code:
            r34.union(r31 - r43, r26, r31 - r27, r23);
     */
    /* JADX WARNING: Missing block: B:161:0x0375, code:
            r43 = r53.mPolicy.getNonDecorDisplayWidth(r31, r28, 1, r53.mCurConfiguration.uiMode);
     */
    /* JADX WARNING: Missing block: B:162:0x0389, code:
            r27 = 96;
     */
    /* JADX WARNING: Missing block: B:163:0x038c, code:
            r34.union(r27, r26, r43, r23);
     */
    /* JADX WARNING: Missing block: B:164:0x039b, code:
            if (r22 == null) goto L_0x03a1;
     */
    /* JADX WARNING: Missing block: B:165:0x039d, code:
            r33 = r61;
     */
    /* JADX WARNING: Missing block: B:166:0x03a1, code:
            r33 = false;
     */
    /* JADX WARNING: Missing block: B:167:0x03a5, code:
            if (r45 != false) goto L_0x03f7;
     */
    /* JADX WARNING: Missing block: B:168:0x03a7, code:
            r7 = "WindowManager";
            r9 = new java.lang.StringBuilder().append("Failed to capture screenshot of ").append(r54).append(" appWin=");
     */
    /* JADX WARNING: Missing block: B:169:0x03c3, code:
            if (r22 != null) goto L_0x03d6;
     */
    /* JADX WARNING: Missing block: B:170:0x03c5, code:
            r6 = "null";
     */
    /* JADX WARNING: Missing block: B:171:0x03c8, code:
            android.util.Slog.i(r7, r9.append(r6).toString());
     */
    /* JADX WARNING: Missing block: B:173:0x03d4, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:174:0x03d5, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:176:?, code:
            r6 = r22 + " drawState=" + r22.mWinAnimator.mDrawState;
     */
    /* JADX WARNING: Missing block: B:177:0x03f7, code:
            if (r41 != 0) goto L_0x0429;
     */
    /* JADX WARNING: Missing block: B:179:0x03fb, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x0426;
     */
    /* JADX WARNING: Missing block: B:180:0x03fd, code:
            android.util.Slog.i("WindowManager", "Screenshot of " + r54 + ": returning null maxLayer=" + r41);
     */
    /* JADX WARNING: Missing block: B:182:0x0427, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:183:0x0428, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:187:0x0435, code:
            if (r34.intersect(0, 0, r31, r28) != false) goto L_0x043a;
     */
    /* JADX WARNING: Missing block: B:188:0x0437, code:
            r34.setEmpty();
     */
    /* JADX WARNING: Missing block: B:190:0x043e, code:
            if (r34.isEmpty() == false) goto L_0x0443;
     */
    /* JADX WARNING: Missing block: B:192:0x0441, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:193:0x0442, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:194:0x0443, code:
            if (r56 >= 0) goto L_0x044f;
     */
    /* JADX WARNING: Missing block: B:196:?, code:
            r56 = (int) (((float) r34.width()) * r59);
     */
    /* JADX WARNING: Missing block: B:197:0x044f, code:
            r57 = (r34.height() * r56) / r34.width();
            r5 = new android.graphics.Rect(r34);
            convertCropForSurfaceFlinger(r5, r11, r31, r28);
     */
    /* JADX WARNING: Missing block: B:198:0x046b, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x0565;
     */
    /* JADX WARNING: Missing block: B:199:0x046d, code:
            android.util.Slog.i("WindowManager", "Screenshot: " + r31 + "x" + r28 + " from " + r8 + " to " + r41 + " appToken=" + r54 + " crop=" + r5 + " width=" + r56 + " height=" + r57 + " isInMultiWindowMode=" + r38 + ",rot:" + r11);
            r35 = 0;
     */
    /* JADX WARNING: Missing block: B:201:0x0500, code:
            if (r35 >= r51.size()) goto L_0x0565;
     */
    /* JADX WARNING: Missing block: B:202:0x0502, code:
            r49 = (com.android.server.wm.WindowState) r51.get(r35);
            r24 = r49.mWinAnimator.mSurfaceController;
            r7 = "WindowManager";
            r9 = new java.lang.StringBuilder().append(r49).append(": ").append(r49.mLayer).append(" animLayer=").append(r49.mWinAnimator.mAnimLayer).append(" surfaceLayer=");
     */
    /* JADX WARNING: Missing block: B:203:0x0549, code:
            if (r24 != null) goto L_0x055c;
     */
    /* JADX WARNING: Missing block: B:204:0x054b, code:
            r6 = "null";
     */
    /* JADX WARNING: Missing block: B:205:0x054e, code:
            android.util.Slog.i(r7, r9.append(r6).toString());
            r35 = r35 + 1;
     */
    /* JADX WARNING: Missing block: B:206:0x055c, code:
            r6 = java.lang.Integer.valueOf(r24.getLayer());
     */
    /* JADX WARNING: Missing block: B:207:0x0565, code:
            r44 = r53.mAnimator.getScreenRotationAnimationLocked(0);
     */
    /* JADX WARNING: Missing block: B:208:0x056e, code:
            if (r44 == null) goto L_0x05dd;
     */
    /* JADX WARNING: Missing block: B:209:0x0570, code:
            r10 = r44.isAnimating();
     */
    /* JADX WARNING: Missing block: B:211:0x0576, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x0583;
     */
    /* JADX WARNING: Missing block: B:212:0x0578, code:
            if (r10 == false) goto L_0x0583;
     */
    /* JADX WARNING: Missing block: B:213:0x057a, code:
            android.util.Slog.v("WindowManager", "Taking screenshot while rotating");
     */
    /* JADX WARNING: Missing block: B:214:0x0583, code:
            android.view.SurfaceControl.openTransaction();
            android.view.SurfaceControl.closeTransactionSync();
     */
    /* JADX WARNING: Missing block: B:215:0x0589, code:
            if (r22 == null) goto L_0x05df;
     */
    /* JADX WARNING: Missing block: B:217:0x0596, code:
            if (QQ_PACKAGE_NAME.equals(r22.getOwningPackage()) == false) goto L_0x05df;
     */
    /* JADX WARNING: Missing block: B:218:0x0598, code:
            r12 = android.view.SurfaceControl.screenshot(r5, r56, r57, r8, r41 + 5, r10, r11);
     */
    /* JADX WARNING: Missing block: B:219:0x05a2, code:
            if (r12 != null) goto L_0x05ea;
     */
    /* JADX WARNING: Missing block: B:220:0x05a4, code:
            android.util.Slog.w("WindowManager", "Screenshot failure taking screenshot for (" + r31 + "x" + r28 + ") to layer " + r41);
     */
    /* JADX WARNING: Missing block: B:222:0x05db, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:223:0x05dc, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:224:0x05dd, code:
            r10 = false;
     */
    /* JADX WARNING: Missing block: B:227:?, code:
            r12 = android.view.SurfaceControl.screenshot(r5, r56, r57, r8, r41, r10, r11);
     */
    /* JADX WARNING: Missing block: B:228:0x05ea, code:
            monitor-exit(r14);
     */
    /* JADX WARNING: Missing block: B:230:0x05ed, code:
            if (com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT == false) goto L_0x0682;
     */
    /* JADX WARNING: Missing block: B:231:0x05ef, code:
            r13 = new int[(r12.getWidth() * r12.getHeight())];
            r12.getPixels(r13, 0, r12.getWidth(), 0, 0, r12.getWidth(), r12.getHeight());
            r21 = true;
            r32 = r13[0];
            r35 = 0;
     */
    /* JADX WARNING: Missing block: B:233:0x0618, code:
            if (r35 >= r13.length) goto L_0x0622;
     */
    /* JADX WARNING: Missing block: B:235:0x061e, code:
            if (r13[r35] == r32) goto L_0x068c;
     */
    /* JADX WARNING: Missing block: B:236:0x0620, code:
            r21 = false;
     */
    /* JADX WARNING: Missing block: B:237:0x0622, code:
            if (r21 == false) goto L_0x0682;
     */
    /* JADX WARNING: Missing block: B:238:0x0624, code:
            r7 = "WindowManager";
            r9 = new java.lang.StringBuilder().append("Screenshot ").append(r22).append(" was monochrome(").append(java.lang.Integer.toHexString(r32)).append(")! mSurfaceLayer=");
     */
    /* JADX WARNING: Missing block: B:239:0x064f, code:
            if (r22 == null) goto L_0x068f;
     */
    /* JADX WARNING: Missing block: B:240:0x0651, code:
            r6 = java.lang.Integer.valueOf(r22.mWinAnimator.mSurfaceController.getLayer());
     */
    /* JADX WARNING: Missing block: B:241:0x065f, code:
            android.util.Slog.i(r7, r9.append(r6).append(" minLayer=").append(r8).append(" maxLayer=").append(r41).toString());
     */
    /* JADX WARNING: Missing block: B:242:0x0682, code:
            r42 = r12.createAshmemBitmap(r60);
            r12.recycle();
     */
    /* JADX WARNING: Missing block: B:243:0x068b, code:
            return r42;
     */
    /* JADX WARNING: Missing block: B:244:0x068c, code:
            r35 = r35 + 1;
     */
    /* JADX WARNING: Missing block: B:245:0x068f, code:
            r6 = "null";
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Bitmap screenshotApplicationsInner(IBinder appToken, int displayId, int width, int height, boolean includeFullDisplay, float frameScale, Config config, boolean wallpaperOnly) {
        synchronized (this.mWindowMap) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent == null) {
                if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                    Slog.i("WindowManager", "Screenshot of " + appToken + ": returning null. No Display for displayId=" + displayId);
                }
                return null;
            }
        }
    }

    private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        int tmp;
        if (rot == 1) {
            tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            tmp = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp;
            tmp = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp;
        } else if (rot == 3) {
            tmp = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp;
        }
    }

    public String getCurrentFocus() {
        return getFocusedWindowPkg();
    }

    public boolean isWindowShownForUid(int uid) {
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int winNdx = windowList.size() - 1;
                while (winNdx >= 0) {
                    WindowState w = (WindowState) windowList.get(winNdx);
                    if (w.mOwnerUid != uid || w.mWinAnimator == null || w.mWinAnimator.mSurfaceController == null || !w.mWinAnimator.mSurfaceController.getShown()) {
                        winNdx--;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isStatusBarVisible() {
        return ((OppoWindowManagerPolicy) this.mPolicy).isStatusBarVisible();
    }

    public void showStatusBar() {
    }

    public void rm_add_StatusBarRunnable(int type) {
    }

    public boolean isRotatingLw() {
        boolean z = false;
        ScreenRotationAnimation mScreenRotationAnimation = null;
        if (this.mAnimator != null) {
            mScreenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(0);
        }
        if (mScreenRotationAnimation == null) {
            return false;
        }
        if (mScreenRotationAnimation.isRotating()) {
            z = mScreenRotationAnimation.isAnimating();
        }
        return z;
    }

    public void requestDismissKeyguard() {
        if (this.mPolicy != null) {
            ((OppoWindowManagerPolicy) this.mPolicy).requestDismissKeyguard();
        }
    }

    public boolean openKeyguardSession(IColorKeyguardSessionCallback callback, IBinder token, String module) {
        if (this.mPolicy != null) {
            return ((OppoWindowManagerPolicy) this.mPolicy).openKeyguardSession(callback, token, module);
        }
        return false;
    }

    public void requestKeyguard(String command) {
        if (this.mPolicy != null) {
            ((OppoWindowManagerPolicy) this.mPolicy).requestKeyguard(command);
        }
    }

    public void removeWindowShownOnKeyguard() {
        WindowState w;
        ArrayList<WindowState> mWaitingToRemove = new ArrayList();
        synchronized (this.mWindowMap) {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                for (int winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                    w = (WindowState) windowList.get(winNdx);
                    boolean appWindow = w.mAttrs.type >= 1 && w.mAttrs.type < 2000;
                    if (appWindow && w.mAttachedWindow == null && (w.mAttrs.flags & DumpState.DUMP_FROZEN) != 0) {
                        if (!isInKeyguardRemoveWhiteList(w.getOwningPackage())) {
                            if (!isSystemApp(w.getOwningPackage())) {
                                mWaitingToRemove.add(w);
                                Map<String, String> staticEventMap = new HashMap();
                                staticEventMap.put("pkgname", w.getOwningPackage());
                                Slog.d(TAG, "remove window OwningPackage = " + w.getOwningPackage());
                                OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "keyguard_intercept_unlock_control", staticEventMap, false);
                            }
                        }
                    }
                }
            }
        }
        if (mWaitingToRemove.size() > 0) {
            for (WindowState w2 : mWaitingToRemove) {
                IBinder binder = w2.mAppToken == null ? w2.mToken == null ? null : w2.mToken.token : w2.mAppToken.token;
                if (binder != null) {
                    try {
                        boolean result = this.mActivityManager.finishActivity(binder, 0, null, 0);
                        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                            Slog.i(TAG, "removeWindowShownOnKeyguard finishActivity ok, result =" + result + ", w= " + w2);
                        }
                    } catch (RemoteException e) {
                        Slog.w(TAG, "removeWindowShownOnKeyguard finishActivity fail, e=" + e.getMessage());
                    }
                }
            }
        }
    }

    void checkDrawnWindowsLocked() {
        if (!(this.mWaitingForDrawn.isEmpty() || this.mWaitingForDrawnCallback == null)) {
            for (int j = this.mWaitingForDrawn.size() - 1; j >= 0; j--) {
                WindowState win = (WindowState) this.mWaitingForDrawn.get(j);
                if (!(win == null || !this.mPolicy.isKeyguardHostWindow(win.mAttrs) || win.mAnimatingExit)) {
                    if (win.mRemoved || !win.mHasSurface || !win.mPolicyVisibility) {
                        this.mWaitingForDrawn.remove(win);
                    } else if (win.mWinAnimator.mSurfaceController != null && win.mWinAnimator.mSurfaceController.getShown()) {
                        this.mWaitingForDrawn.remove(win);
                    } else if (!(this.mPolicy == null || ((OppoWindowManagerPolicy) this.mPolicy).doesNeedWaitingKeyguard())) {
                        this.mWaitingForDrawn.remove(win);
                    }
                }
            }
            if (this.mWaitingForDrawn.isEmpty()) {
                if (WindowManagerDebugConfig.DEBUG_SCREEN_ON) {
                    Slog.d(TAG, "oppo checkDrawnWindowsLocked All windows drawn!");
                }
                this.mH.removeMessages(24);
                this.mH.sendEmptyMessage(33);
            }
        }
        super.checkDrawnWindowsLocked();
    }

    public void disableKeyguard(IBinder token, String tag) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (token == null) {
            throw new IllegalArgumentException("token == null");
        } else if (this.mContext.getPackageManager().isFullFunctionMode() || isSystemApp(Binder.getCallingUid())) {
            super.disableKeyguard(token, tag);
        }
    }

    public void reenableKeyguard(IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
            throw new SecurityException("Requires DISABLE_KEYGUARD permission");
        } else if (token == null) {
            throw new IllegalArgumentException("token == null");
        } else if (this.mContext.getPackageManager().isFullFunctionMode() || isSystemApp(Binder.getCallingUid())) {
            super.reenableKeyguard(token);
        }
    }

    private boolean isSystemApp(int uid) {
        if (uid < 10000) {
            if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.d(TAG, "isSystemApp system app, uid =" + uid);
            }
            return true;
        }
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.d(TAG, "isSystemApp getPackagesForUid = null, uid =" + uid);
            }
            return true;
        }
        for (int i = 0; i < packages.length; i++) {
            String packageName = packages[i];
            if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.d(TAG, "isSystemApp - i =" + i + ",  packages[i] = " + packageName);
            }
            if (isSystemApp(packageName)) {
                return true;
            }
        }
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d(TAG, "isSystemApp end, return false, uid =" + uid);
        }
        return false;
    }

    private boolean isSystemApp(String packageName) {
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d(TAG, "isSystemApp start, packageName =" + packageName);
        }
        if (packageName != null) {
            try {
                ApplicationInfo info = this.mContext.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
                if (!(info == null || (info.flags & 1) == 0)) {
                    if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                        Slog.d(TAG, "isSystemApp system app");
                    }
                    return true;
                }
            } catch (NameNotFoundException e) {
                if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
                    Slog.d(TAG, "isSystemApp NameNotFoundException,  return false");
                }
                return false;
            }
        }
        return false;
    }

    private boolean isInKeyguardRemoveWhiteList(String packageName) {
        List<String> nameList = this.mOppoKeyguardListHelper.getSkipNameList();
        if (WindowManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.d(TAG, "getSkipNameList =" + nameList);
        }
        if (packageName != null) {
            for (String pkg : nameList) {
                if (packageName.equalsIgnoreCase(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:22:0x0037, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Rect getFloatWindowRect(int displayId) {
        synchronized (this.mWindowMap) {
            WindowList list = getWindowListLocked(displayId);
            Rect r = new Rect();
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    WindowState win = (WindowState) list.get(i);
                    if (win != null && win.mHasSurface && win.mAppOpVisibility && (24 == win.mAppOp || 45 == win.mAppOp)) {
                        r = win.mVisibleFrame;
                        break;
                    }
                }
            } else {
                return r;
            }
        }
    }
}

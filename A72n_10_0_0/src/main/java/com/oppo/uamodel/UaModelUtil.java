package com.oppo.uamodel;

import android.app.OppoMirrorActivityThread;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.logging.nano.MetricsProto;
import com.oppo.luckymoney.LMManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UaModelUtil {
    private static final String CUSTOMIZED_THEME_EXTENSION = ".theme";
    private static final String CUSTOMIZED_THEME_PREFIX = "Customized_";
    private static final String CUSTOMIZED_THEME_ROOT_PATH = "data/theme_bak/customized_theme/";
    private static final String TAG = "UAmodel";
    private static final String UUID = "FA2A296A2A6842A6A9E34B75384EC45A";
    private static List<String> uaModelList = Arrays.asList(LMManager.QQ_PACKAGENAME, "com.sina.weibo", "com.sinamobile.uagenerator");
    private String customized_theme_uuid_from_file = null;
    private String mBarceCustom = SystemProperties.get("ro.product.oppo.custom.Barce");
    private String mColorTheme = SystemProperties.get("ro.hw.phone.color");
    private String mLbTheme = SystemProperties.get("persist.sys.oppo.theme");
    private String mOppoModel = SystemProperties.get("ro.product.oppo_model");
    private boolean uuidMatch = false;

    public boolean UaModelOk(String uaPackageName) {
        boolean noOppoModel = this.mOppoModel.equals("");
        boolean uaWhiteList = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            uaWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_REQUEST_RECEIVE_MMS), uaPackageName).booleanValue();
        }
        if (noOppoModel || !uaWhiteList || !this.mLbTheme.equals("lb_theme")) {
            return false;
        }
        return true;
    }

    public boolean BarceCustom_UaModel_Ok(String uaPackageName) {
        boolean noOppoModel = this.mOppoModel.equals("");
        boolean uaWhiteList = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            uaWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_REQUEST_RECEIVE_MMS), uaPackageName).booleanValue();
        }
        if (noOppoModel || !uaWhiteList || !this.mBarceCustom.equals("Barce")) {
            return false;
        }
        return true;
    }

    public boolean renoZ_UaModel_Ok(String uaPackageName) {
        boolean noOppoModel = this.mOppoModel.equals("");
        boolean uaWhiteList = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            uaWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_REQUEST_RECEIVE_MMS), uaPackageName).booleanValue();
        }
        if (noOppoModel || !uaWhiteList || !this.mColorTheme.equals("FFFF3000")) {
            return false;
        }
        return true;
    }

    public boolean FindX2_LBTheme_UaModel_Ok(String uaPackageName) {
        boolean noOppoModel = this.mOppoModel.equals("");
        boolean uaWhiteList = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            uaWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_REQUEST_RECEIVE_MMS), uaPackageName).booleanValue();
        }
        if (noOppoModel || !uaWhiteList || !this.mColorTheme.equals("00FFF002")) {
            return false;
        }
        return true;
    }

    public boolean RenoACE2_EVA_UaModel_Ok(String uaPackageName) {
        boolean noOppoModel = this.mOppoModel.equals("");
        boolean uaWhiteList = false;
        if (OppoMirrorActivityThread.inCptWhiteList != null) {
            uaWhiteList = OppoMirrorActivityThread.inCptWhiteList.call(null, Integer.valueOf((int) MetricsProto.MetricsEvent.ACTION_PERMISSION_REQUEST_RECEIVE_MMS), uaPackageName).booleanValue();
        }
        if (noOppoModel || !uaWhiteList || !this.mColorTheme.equals("00FFF003")) {
            return false;
        }
        return true;
    }

    public void isLbTheme() {
        String customized_theme_path = scanCustomizedTheme();
        this.customized_theme_uuid_from_file = getThemeUuidFromThemeFile(CUSTOMIZED_THEME_ROOT_PATH + customized_theme_path);
        String str = this.customized_theme_uuid_from_file;
        if (str != null) {
            this.uuidMatch = str.equals(UUID);
        }
        if (this.uuidMatch) {
            SystemProperties.set("persist.sys.oppo.theme", "lb_theme");
        } else {
            SystemProperties.set("persist.sys.oppo.theme", "nlb_theme");
        }
    }

    public void changeToSpecialModel() {
        try {
            Field field = Build.class.getField("MODEL");
            field.setAccessible(true);
            field.set(Build.class, this.mOppoModel);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private String getThemeUuidFromThemeFile(String file) {
        String uuid = null;
        if (new File(file).exists()) {
            InputStream in = null;
            ZipInputStream zin = null;
            ZipFile zf = null;
            try {
                ZipFile zf2 = new ZipFile(file);
                InputStream in2 = new BufferedInputStream(new FileInputStream(file));
                ZipInputStream zin2 = new ZipInputStream(in2);
                while (true) {
                    ZipEntry ze = zin2.getNextEntry();
                    if (ze != null) {
                        if (!ze.isDirectory()) {
                            String file_name = ze.getName();
                            Log.i(TAG, "file_name = " + file_name);
                            if (file_name.startsWith("previews")) {
                                Log.i(TAG, "ignore preview directory to avoid preview picture file name length maybe 32 characters");
                            } else {
                                try {
                                    uuid = file_name.substring(file_name.lastIndexOf("/") + 1, file_name.lastIndexOf("."));
                                } catch (IndexOutOfBoundsException e) {
                                    Log.i(TAG, e.getMessage());
                                }
                                if (uuid == null) {
                                    continue;
                                } else if (uuid.length() != 32) {
                                }
                            }
                        }
                    }
                    try {
                        zin2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    try {
                        in2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                    try {
                        zf2.close();
                        break;
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            } catch (IOException e1) {
                Log.i(TAG, e1.getMessage());
                if (0 != 0) {
                    try {
                        zin.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                if (0 != 0) {
                    zf.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        zin.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                }
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                if (0 != 0) {
                    try {
                        zf.close();
                    } catch (IOException e9) {
                        e9.printStackTrace();
                    }
                }
                throw th;
            }
        } else {
            Log.e(TAG, "theme file not exists : " + file);
        }
        return uuid;
    }

    private String scanCustomizedTheme() {
        File theme_root = new File(CUSTOMIZED_THEME_ROOT_PATH);
        if (!theme_root.exists() || !theme_root.isDirectory() || theme_root.list().length <= 0) {
            return null;
        }
        Log.i(TAG, "theme_root " + theme_root.toString() + " not empty...");
        String[] list = theme_root.list();
        for (String file : list) {
            if (file.startsWith(CUSTOMIZED_THEME_PREFIX) && file.endsWith(CUSTOMIZED_THEME_EXTENSION)) {
                Log.i(TAG, "theme found : data/theme_bak/customized_theme/" + file);
                return file;
            }
        }
        return null;
    }
}

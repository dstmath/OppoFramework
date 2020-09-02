package com.android.server.pm;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class OppoMarketHelper {
    public static final String EXTRA_APPLICATION_DIR_PATH = (File.separator + "data" + File.separator + "oppo" + File.separator + "common" + File.separator + "pm");
    public static final String EXTRA_APPLICATION_FILE_PATH = (File.separator + "data" + File.separator + "oppo" + File.separator + "common" + File.separator + "pm" + File.separator + "extra_app_info.xml");
    public static final String EXTRA_APPLICATION_FILE_TMP_PATH = (File.separator + "data" + File.separator + "oppo" + File.separator + "common" + File.separator + "pm" + File.separator + "extra_app_info_tmp.xml");
    public static final int Limit_Length = 1024;
    static final String TAG = "OppoMarketHelper";
    public static boolean isFirstReadXml = true;
    /* access modifiers changed from: private */
    public static ArrayList<ExtraApplicationInfo> mLocalExtraApplicationInfoList = null;

    public static class ExtraApplicationInfoManager {
        private ExtraApplicationInfo extraAppParams;
        final ArrayList<ExtraApplicationInfo> mPendingUpdates = new ArrayList<>();

        public void handleMessage(Message msg) {
            if (msg.what == 199) {
                this.extraAppParams = (ExtraApplicationInfo) msg.obj;
                if (PackageManagerService.DEBUG_INSTALL) {
                    Slog.d(OppoMarketHelper.TAG, "update_extra_app_info");
                }
                if (OppoMarketHelper.isFirstReadXml) {
                    ArrayList unused = OppoMarketHelper.mLocalExtraApplicationInfoList = OppoMarketHelper.readExtraAppInfoFromXml();
                    if (OppoMarketHelper.mLocalExtraApplicationInfoList == null) {
                        ArrayList unused2 = OppoMarketHelper.mLocalExtraApplicationInfoList = OppoMarketHelper.readExtraAppInfoFromXml();
                    }
                    OppoMarketHelper.isFirstReadXml = false;
                }
                if (PackageManagerService.DEBUG_INSTALL) {
                    Slog.d(OppoMarketHelper.TAG, "extraAppParams.getPackageName(): " + this.extraAppParams.getPackageName());
                }
                ExtraApplicationInfo extraApplicationInfo = this.extraAppParams;
                if (extraApplicationInfo == null || extraApplicationInfo.getPackageName() == null) {
                    Slog.d(OppoMarketHelper.TAG, "The element is Empty");
                    return;
                }
                if (OppoMarketHelper.mLocalExtraApplicationInfoList != null) {
                    boolean searchResult = false;
                    Iterator it = OppoMarketHelper.mLocalExtraApplicationInfoList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ExtraApplicationInfo extraAppInfo = (ExtraApplicationInfo) it.next();
                        if (TextUtils.equals(extraAppInfo.getPackageName(), this.extraAppParams.getPackageName())) {
                            OppoMarketHelper.mLocalExtraApplicationInfoList.remove(extraAppInfo);
                            if (TextUtils.equals("add", this.extraAppParams.getOperateFlag())) {
                                OppoMarketHelper.mLocalExtraApplicationInfoList.add(this.extraAppParams);
                            }
                            searchResult = true;
                        }
                    }
                    if (PackageManagerService.DEBUG_INSTALL) {
                        Slog.d(OppoMarketHelper.TAG, "searchResult: " + searchResult);
                    }
                    if (!searchResult && TextUtils.equals("add", this.extraAppParams.getOperateFlag())) {
                        OppoMarketHelper.mLocalExtraApplicationInfoList.add(this.extraAppParams);
                    }
                } else {
                    ArrayList unused3 = OppoMarketHelper.mLocalExtraApplicationInfoList = new ArrayList();
                    if (TextUtils.equals("add", this.extraAppParams.getOperateFlag())) {
                        OppoMarketHelper.mLocalExtraApplicationInfoList.add(this.extraAppParams);
                    }
                }
                boolean writeResult = OppoMarketHelper.writeExtraAppInfoToXml(OppoMarketHelper.mLocalExtraApplicationInfoList);
                if (writeResult) {
                    OppoMarketHelper.renameFile(OppoMarketHelper.EXTRA_APPLICATION_FILE_TMP_PATH, OppoMarketHelper.EXTRA_APPLICATION_FILE_PATH);
                }
                if (PackageManagerService.DEBUG_INSTALL) {
                    Slog.d(OppoMarketHelper.TAG, "writeResult: " + writeResult);
                    Iterator it2 = OppoMarketHelper.mLocalExtraApplicationInfoList.iterator();
                    while (it2.hasNext()) {
                        ExtraApplicationInfo extraAppInfo2 = (ExtraApplicationInfo) it2.next();
                        Slog.d(OppoMarketHelper.TAG, "extraAppInfo.getPackageName() " + extraAppInfo2.getPackageName() + " extraAppInfo.getInstallerName() " + extraAppInfo2.getInstallerName() + " extraAppInfo.getExtraAppInfo() " + extraAppInfo2.getExtraAppInfo() + " extraAppInfo.getOperateFlag() " + extraAppInfo2.getOperateFlag());
                    }
                }
            }
        }
    }

    public static class ExtraApplicationInfo {
        private String mExtraAppInfo;
        private String mInstallerName;
        private String mOperateFlag;
        private String mPackage;

        public ExtraApplicationInfo() {
            this.mPackage = "";
            this.mInstallerName = "";
            this.mExtraAppInfo = "";
            this.mOperateFlag = "";
        }

        public ExtraApplicationInfo(String packageName, String installerName, String mExtraAppInfo2, String operateFlag) {
            this.mPackage = packageName;
            this.mInstallerName = installerName;
            this.mExtraAppInfo = mExtraAppInfo2;
            this.mOperateFlag = operateFlag;
        }

        public String getPackageName() {
            return this.mPackage;
        }

        public String getInstallerName() {
            return this.mInstallerName;
        }

        public String getExtraAppInfo() {
            return this.mExtraAppInfo;
        }

        public String getOperateFlag() {
            return this.mOperateFlag;
        }

        public void setPackageName(String packageName) {
            this.mPackage = packageName;
        }

        public void setInstallerName(String installerName) {
            this.mInstallerName = installerName;
        }

        public void setExtraAppInfo(String extraAppInfo) {
            this.mExtraAppInfo = extraAppInfo;
        }

        public void setOperateFlag(String operateFlag) {
            this.mOperateFlag = operateFlag;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x01cf A[Catch:{ IOException -> 0x01d6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:117:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:118:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:120:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x019f A[Catch:{ IOException -> 0x01a6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01b3  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01c2  */
    public static ArrayList<ExtraApplicationInfo> readExtraAppInfoFromXml() {
        XmlPullParser parse;
        Throwable th;
        ArrayList<ExtraApplicationInfo> extraAppInfoList = new ArrayList<>();
        XmlPullParser parse2 = null;
        ExtraApplicationInfo extraAppInfo = null;
        String installerName = null;
        String installerName2 = null;
        File dir = new File(EXTRA_APPLICATION_DIR_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
            dir.setReadable(true, false);
            dir.setExecutable(true, false);
        }
        if (new File(EXTRA_APPLICATION_FILE_TMP_PATH).exists()) {
            renameFile(EXTRA_APPLICATION_FILE_TMP_PATH, EXTRA_APPLICATION_FILE_PATH);
        }
        File file = new File(EXTRA_APPLICATION_FILE_PATH);
        if (!file.exists()) {
            Log.i(TAG, "readExtraAppInfoFromXml file is not exist");
            return null;
        }
        try {
            FileInputStream in = new FileInputStream(file);
            parse2 = XmlPullParserFactory.newInstance().newPullParser();
            parse2.setInput(in, "utf-8");
            String extra_info = null;
            String installerName3 = null;
            ExtraApplicationInfo extraAppInfo2 = null;
            int event = parse2.getEventType();
            for (int i = 1; event != i; i = 1) {
                if (event != 0) {
                    if (event == 2) {
                        if (parse2.getName().equals("package_name")) {
                            extraAppInfo2 = new ExtraApplicationInfo();
                            int attrCount = parse2.getAttributeCount();
                            if (attrCount > 0) {
                                int i2 = 0;
                                while (true) {
                                    if (i2 >= attrCount) {
                                        break;
                                    }
                                    String attrName = parse2.getAttributeName(i2);
                                    String attrValue = parse2.getAttributeValue(i2);
                                    if ("value".equals(attrName)) {
                                        extraAppInfo2.setPackageName(attrValue);
                                        break;
                                    }
                                    i2++;
                                }
                            } else {
                                String attrName2 = parse2.getAttributeName(0);
                                String attrValue2 = parse2.getAttributeValue(0);
                                if ("value".equals(attrName2)) {
                                    extraAppInfo2.setPackageName(attrValue2);
                                }
                            }
                        }
                        if (extraAppInfo2 != null) {
                            if (parse2.getName().equals("installer_name")) {
                                installerName3 = parse2.nextText();
                                if (PackageManagerService.DEBUG_INSTALL) {
                                    Log.i(TAG, "readExtraAppInfoFromXml-installerName " + installerName3);
                                }
                                extraAppInfo2.setInstallerName(installerName3);
                            } else if (parse2.getName().equals("extra_info")) {
                                String extra_info2 = parse2.nextText();
                                try {
                                    if (PackageManagerService.DEBUG_INSTALL) {
                                        Log.i(TAG, "readExtraAppInfoFromXml-extra_info " + extra_info2);
                                    }
                                    extraAppInfo2.setExtraAppInfo(extra_info2);
                                    extra_info = extra_info2;
                                } catch (XmlPullParserException e) {
                                    e = e;
                                    e.printStackTrace();
                                    if (!(parse2 instanceof Closeable)) {
                                    }
                                } catch (IOException e2) {
                                    e = e2;
                                    e.printStackTrace();
                                    if (!(parse2 instanceof Closeable)) {
                                    }
                                } catch (Exception e3) {
                                    e = e3;
                                    extraAppInfo = extraAppInfo2;
                                    installerName = installerName3;
                                    installerName2 = extra_info2;
                                    e.printStackTrace();
                                    if (!(parse2 instanceof Closeable)) {
                                    }
                                } catch (Throwable th2) {
                                    parse = parse2;
                                    th = th2;
                                    if (parse instanceof Closeable) {
                                    }
                                    throw th;
                                }
                            }
                        }
                    } else if (event == 3) {
                        try {
                            if (parse2.getName().equals("package_name")) {
                                extraAppInfoList.add(extraAppInfo2);
                                extraAppInfo2 = null;
                            }
                        } catch (XmlPullParserException e4) {
                            e = e4;
                            e.printStackTrace();
                            if (!(parse2 instanceof Closeable)) {
                            }
                        } catch (IOException e5) {
                            e = e5;
                            e.printStackTrace();
                            if (!(parse2 instanceof Closeable)) {
                            }
                        } catch (Exception e6) {
                            e = e6;
                            extraAppInfo = extraAppInfo2;
                            installerName = installerName3;
                            installerName2 = extra_info;
                            try {
                                e.printStackTrace();
                                try {
                                    if (!(parse2 instanceof Closeable)) {
                                    }
                                } catch (IOException e7) {
                                    e7.printStackTrace();
                                    return null;
                                }
                            } catch (Throwable th3) {
                                parse = parse2;
                                th = th3;
                                try {
                                    if (parse instanceof Closeable) {
                                        ((Closeable) parse).close();
                                    }
                                } catch (IOException e8) {
                                    e8.printStackTrace();
                                }
                                throw th;
                            }
                        } catch (Throwable th4) {
                            parse = parse2;
                            th = th4;
                            if (parse instanceof Closeable) {
                            }
                            throw th;
                        }
                    }
                }
                event = parse2.next();
            }
            try {
                if (parse2 instanceof Closeable) {
                    ((Closeable) parse2).close();
                }
            } catch (IOException e9) {
                e9.printStackTrace();
            }
            return extraAppInfoList;
        } catch (XmlPullParserException e10) {
            e = e10;
            e.printStackTrace();
            if (!(parse2 instanceof Closeable)) {
                return null;
            }
            ((Closeable) parse2).close();
            return null;
        } catch (IOException e11) {
            e = e11;
            e.printStackTrace();
            if (!(parse2 instanceof Closeable)) {
                return null;
            }
            ((Closeable) parse2).close();
            return null;
        } catch (Exception e12) {
            e = e12;
            e.printStackTrace();
            if (!(parse2 instanceof Closeable)) {
                return null;
            }
            ((Closeable) parse2).close();
            return null;
        }
    }

    public static boolean writeExtraAppInfoToXml(List<ExtraApplicationInfo> extraAppInfoList) {
        File dir = new File(EXTRA_APPLICATION_DIR_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
            dir.setReadable(true, false);
            dir.setExecutable(true, false);
        }
        try {
            File tmpFile = new File(EXTRA_APPLICATION_FILE_TMP_PATH);
            if (!tmpFile.createNewFile()) {
                tmpFile.delete();
                if (!tmpFile.createNewFile()) {
                    Log.d(TAG, "Create file failed!");
                    return false;
                }
            }
            tmpFile.setReadable(true, false);
            FileOutputStream out = new FileOutputStream(tmpFile);
            XmlSerializer serializer = Xml.newSerializer();
            String newLine = System.getProperty("line.separator");
            serializer.setOutput(out, "UTF-8");
            serializer.startDocument("UTF-8", true);
            serializer.text(newLine);
            for (ExtraApplicationInfo extraAppInfo : extraAppInfoList) {
                if (!(extraAppInfo == null || extraAppInfo.getPackageName() == null)) {
                    serializer.startTag(null, "package_name");
                    serializer.attribute(null, "value", extraAppInfo.getPackageName());
                    serializer.startTag(null, "installer_name");
                    serializer.text(extraAppInfo.getInstallerName());
                    serializer.endTag(null, "installer_name");
                    serializer.startTag(null, "extra_info");
                    serializer.text(extraAppInfo.getExtraAppInfo());
                    serializer.endTag(null, "extra_info");
                    serializer.endTag(null, "package_name");
                    serializer.text(newLine);
                }
            }
            serializer.endDocument();
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean renameFile(String fromFilePath, String toFilePath) {
        try {
            File fromFile = new File(fromFilePath);
            File toFile = new File(toFilePath);
            boolean isExists = false;
            if (!fromFile.exists()) {
                return false;
            }
            if (toFile.exists()) {
                toFile.delete();
                isExists = toFile.exists();
                int i = 0;
                while (true) {
                    if (i >= 10) {
                        break;
                    } else if (!isExists) {
                        break;
                    } else {
                        isExists = toFile.exists();
                        i++;
                    }
                }
            }
            if (isExists) {
                return false;
            }
            return fromFile.renameTo(toFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

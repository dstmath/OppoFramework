package com.android.server.pm;

import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.os.BackgroundThread;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class UserUninstallRecorder {
    private static final String ATTR_NAME = "name";
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String MANUAL_PACKAGE_OPERATION_FILE = "/data/oppo/coloros/dataupdate/manual_package_operation.xml";
    private static final int MSG_WHAT = 20190829;
    private static final String TAG = "UserUninstallRecorder";
    private static final String TAG_MANUAL_STATE = "manual-state";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PACKAGE_UNINSTALLED = "package-uninstalled";
    private static UserUninstallRecorder instance = null;
    private Handler mHandler = new MyHandler();
    private List<String> mUninstalledPackagesByUser = new ArrayList();

    public static UserUninstallRecorder getInstance() {
        if (instance == null) {
            synchronized (UserUninstallRecorder.class) {
                if (instance == null) {
                    instance = new UserUninstallRecorder();
                }
            }
        }
        return instance;
    }

    public UserUninstallRecorder() {
        loadManualPackageOperation();
        if (DEBUG) {
            Iterator<String> it = this.mUninstalledPackagesByUser.iterator();
            while (it.hasNext()) {
                Slog.i(TAG, "package " + it.next() + " removed by user");
            }
        }
    }

    public void persistenceAsync() {
        Handler handler = this.mHandler;
        if (handler == null) {
            Slog.e(TAG, "mHandler is null, exit persistenceAsync");
            return;
        }
        if (handler.hasMessages(MSG_WHAT)) {
            this.mHandler.removeMessages(MSG_WHAT);
        }
        this.mHandler.sendEmptyMessage(MSG_WHAT);
    }

    public List<String> getUninstalledList() {
        List<String> result = new ArrayList<>();
        List<String> list = this.mUninstalledPackagesByUser;
        if (list != null && list.size() > 0) {
            result.addAll(this.mUninstalledPackagesByUser);
        }
        return result;
    }

    public boolean isPkgUninstalledByUser(String pkg) {
        return this.mUninstalledPackagesByUser.contains(pkg);
    }

    public void addManualOperatedPackage(String pkg, boolean installed) {
        if (TextUtils.isEmpty(pkg)) {
            Slog.e(TAG, "pkg is null!");
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "addManualOperatedPackage::pkg[" + pkg + "], installed[" + installed + "].");
        }
        if (installed) {
            if (this.mUninstalledPackagesByUser.contains(pkg)) {
                this.mUninstalledPackagesByUser.remove(pkg);
            } else {
                Slog.d(TAG, "not care " + pkg + " installed.");
            }
        } else if (!this.mUninstalledPackagesByUser.contains(pkg)) {
            this.mUninstalledPackagesByUser.add(pkg);
        } else {
            Slog.e(TAG, "duplicate uninstall " + pkg + ", please check it.");
        }
        persistenceAsync();
    }

    private void loadManualPackageOperation() {
        StringBuilder sb;
        File operationFile = new File(MANUAL_PACKAGE_OPERATION_FILE);
        if (!operationFile.exists()) {
            Slog.i(TAG, "manual_package_operation.xml not exist.");
            return;
        }
        try {
            FileInputStream in = new AtomicFile(operationFile).openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, null);
                parseManualPackageOperation(parser);
                IoUtils.closeQuietly(in);
                if (1 == 0) {
                    sb = new StringBuilder();
                    sb.append("parse ");
                    sb.append(operationFile.getPath());
                    sb.append(" failed, delete it.");
                    Slog.e(TAG, sb.toString());
                    operationFile.delete();
                }
            } catch (IOException | XmlPullParserException e) {
                Slog.e(TAG, "Failed parsing file: " + operationFile, e);
                IoUtils.closeQuietly(in);
                if (0 == 0) {
                    sb = new StringBuilder();
                }
            } catch (Throwable th) {
                IoUtils.closeQuietly(in);
                if (1 == 0) {
                    Slog.e(TAG, "parse " + operationFile.getPath() + " failed, delete it.");
                    operationFile.delete();
                }
                throw th;
            }
        } catch (FileNotFoundException e2) {
            Slog.i(TAG, "No package operation state.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004d  */
    private void parseManualPackageOperation(XmlPullParser parser) throws IOException, XmlPullParserException {
        boolean z;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String name = parser.getName();
                int hashCode = name.hashCode();
                if (hashCode != 1435800058) {
                    if (hashCode == 1827165130 && name.equals(TAG_MANUAL_STATE)) {
                        z = DEBUG;
                        if (!z) {
                            Slog.d(TAG, "parse manual-state tag.");
                        } else if (z) {
                            parsePackage(parser);
                        }
                    }
                } else if (name.equals(TAG_PACKAGE_UNINSTALLED)) {
                    z = true;
                    if (!z) {
                    }
                }
                z = true;
                if (!z) {
                }
            }
        }
    }

    private void parsePackage(XmlPullParser parser) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String name = parser.getName();
                char c = 65535;
                if (name.hashCode() == -807062458 && name.equals("package")) {
                    c = 0;
                }
                if (c == 0) {
                    String name2 = parser.getAttributeValue(null, "name");
                    if (DEBUG) {
                        Slog.d(TAG, "parsePackage::name[" + name2 + "].");
                    }
                    this.mUninstalledPackagesByUser.add(name2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void persistence() {
        AtomicFile destination = new AtomicFile(new File(MANUAL_PACKAGE_OPERATION_FILE));
        FileOutputStream out = null;
        try {
            out = destination.startWrite();
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, StandardCharsets.UTF_8.name());
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_MANUAL_STATE);
            serializer.startTag(null, TAG_PACKAGE_UNINSTALLED);
            for (String pkg : this.mUninstalledPackagesByUser) {
                serializer.startTag(null, "package");
                serializer.attribute(null, "name", pkg);
                serializer.endTag(null, "package");
            }
            serializer.endTag(null, TAG_PACKAGE_UNINSTALLED);
            serializer.endTag(null, TAG_MANUAL_STATE);
            serializer.endDocument();
            destination.finishWrite(out);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(out);
    }

    private final class MyHandler extends Handler {
        public MyHandler() {
            super(BackgroundThread.getHandler().getLooper());
        }

        public void handleMessage(Message msg) {
            if (msg.what == UserUninstallRecorder.MSG_WHAT) {
                UserUninstallRecorder.this.persistence();
            }
        }
    }
}

package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OppoLocModeRecord {
    private static final String ACTION_LOCATION_MODE_CHANGED = "android.location.MODE_CHANGED";
    private static final long DO_MONITOR_DELAY_TIME = 1000;
    private static final long FILTER_TIME_WINDOW = 1000;
    private static final int MSG_LOC_MODE_CHANGE = 101;
    private static final int MSG_READ_SECURE_SETTINGS = 102;
    private static final String TAG = "OppoLocModeRecord";
    private static OppoLocModeRecord mInstall = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoLocModeRecord.AnonymousClass1 */

        /* JADX WARNING: Removed duplicated region for block: B:19:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(OppoLocModeRecord.TAG, "receive broadcast intent, action: " + action);
            if (action != null) {
                synchronized (OppoLocModeRecord.this.mLock) {
                    char c = 65535;
                    int hashCode = action.hashCode();
                    if (hashCode != -511271086) {
                        if (hashCode == 959232034 && action.equals("android.intent.action.USER_SWITCHED")) {
                            c = 1;
                            if (c == 0) {
                                OppoLocModeRecord.this.mHandler.sendEmptyMessage(OppoLocModeRecord.MSG_LOC_MODE_CHANGE);
                            } else if (c == 1) {
                                OppoLocModeRecord.this.onUserChanged(intent.getIntExtra("android.intent.extra.user_handle", 0));
                            }
                        }
                    } else if (action.equals(OppoLocModeRecord.ACTION_LOCATION_MODE_CHANGED)) {
                        c = 0;
                        if (c == 0) {
                        }
                    }
                    if (c == 0) {
                    }
                }
            }
        }
    };
    private Context mContext = null;
    private int mCurrentUserId = 0;
    private Handler mHandler = new Handler() {
        /* class com.android.server.location.OppoLocModeRecord.AnonymousClass2 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == OppoLocModeRecord.MSG_LOC_MODE_CHANGE) {
                sendEmptyMessageDelayed(OppoLocModeRecord.MSG_READ_SECURE_SETTINGS, 1000);
            } else if (i == OppoLocModeRecord.MSG_READ_SECURE_SETTINGS) {
                OppoLocModeRecord.this.refreshLocSwitchRec();
            }
        }
    };
    private boolean mHasFiltered = false;
    private List<String> mLocModeChangeHis = new ArrayList();
    private int mLocationMode = 0;
    private final Object mLock = new Object();

    public static OppoLocModeRecord getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoLocModeRecord(context);
        }
        return mInstall;
    }

    private OppoLocModeRecord(Context context) {
        this.mContext = context;
        initValue();
    }

    private void initValue() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOCATION_MODE_CHANGED);
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshLocSwitchRec() {
        parseSettiingsXml("data/system/users/" + this.mCurrentUserId + "/settings_secure.xml");
    }

    private String readFromFile(File path) {
        if (path == null) {
            return null;
        }
        InputStream inputStrm = null;
        try {
            InputStream inputStrm2 = new FileInputStream(path);
            BufferedReader inputRdr = new BufferedReader(new InputStreamReader(inputStrm2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = inputRdr.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line + StringUtils.LF);
            }
            String stringBuffer = buffer.toString();
            try {
                inputStrm2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (0 != 0) {
                inputStrm.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (0 != 0) {
                try {
                    inputStrm.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStrm.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
        return null;
    }

    @GuardedBy({"mLock"})
    private void parseSettiingsXml(String FileName) {
        String content = readFromFile(new File(FileName));
        if (content != null) {
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(new StringReader(content));
                for (int type = parser.getEventType(); type != 1; type = parser.next()) {
                    if (type != 2) {
                        if (type != 3) {
                        }
                    } else if (parser.getName().equals("setting") && parser.getAttributeValue(null, "name").equals("location_mode")) {
                        this.mLocationMode = Integer.parseInt(parser.getAttributeValue(null, "value"));
                        long time = System.currentTimeMillis() - 1000;
                        if (this.mLocationMode != 0) {
                            List<String> list = this.mLocModeChangeHis;
                            list.add(StringUtils.EMPTY + time + StringUtils.SPACE + parser.getAttributeValue(null, "package") + " open");
                            StringBuilder sb = new StringBuilder();
                            sb.append(parser.getAttributeValue(null, "package"));
                            sb.append(" open location");
                            Log.d(TAG, sb.toString());
                            return;
                        }
                        List<String> list2 = this.mLocModeChangeHis;
                        list2.add(StringUtils.EMPTY + time + StringUtils.SPACE + parser.getAttributeValue(null, "package") + " close");
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(parser.getAttributeValue(null, "package"));
                        sb2.append(" close location");
                        Log.d(TAG, sb2.toString());
                        correctLocationMode();
                        return;
                    }
                }
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Got XmlPullParser exception parsing!");
            } catch (IOException e2) {
                Log.e(TAG, "Got IO exception parsing!!");
            }
        }
    }

    @GuardedBy({"mLock"})
    private void filterLmChangeRec() {
        List<String> tempRec = new ArrayList<>();
        int Length = this.mLocModeChangeHis.size();
        if (Length > 1) {
            int flag = 1;
            for (int i = 1; i < Length; i++) {
                if (Long.valueOf(this.mLocModeChangeHis.get(i).split(StringUtils.SPACE)[0]).longValue() - Long.valueOf(this.mLocModeChangeHis.get(i - 1).split(StringUtils.SPACE)[0]).longValue() < 1000) {
                    flag++;
                } else {
                    if (flag % 2 == 1) {
                        tempRec.add(this.mLocModeChangeHis.get(i - 1));
                    }
                    flag = 1;
                }
            }
            if (flag % 2 == 1) {
                tempRec.add(this.mLocModeChangeHis.get(Length - 1));
            }
            this.mLocModeChangeHis = tempRec;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM-dd_HH:mm:ss");
        for (int i2 = 0; i2 < this.mLocModeChangeHis.size(); i2++) {
            String newEle = simpleDateFormat.format(new Date(Long.valueOf(this.mLocModeChangeHis.get(i2).split(StringUtils.SPACE)[0]).longValue()));
            this.mLocModeChangeHis.set(i2, newEle + StringUtils.SPACE + this.mLocModeChangeHis.get(i2).split(StringUtils.SPACE)[1] + StringUtils.SPACE + this.mLocModeChangeHis.get(i2).split(StringUtils.SPACE)[2]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void onUserChanged(int userId) {
        if (this.mCurrentUserId != userId) {
            Log.d(TAG, "foreground user is changing to " + userId);
            this.mCurrentUserId = userId;
        }
    }

    @GuardedBy({"mLock"})
    private void correctLocationMode() {
        List<String> list = this.mLocModeChangeHis;
        if (list != null && list.size() != 0) {
            List<String> list2 = this.mLocModeChangeHis;
            String lastRec = list2.get(list2.size() - 1);
            Log.d(TAG, "mLocModeChangeHis size = " + this.mLocModeChangeHis.size() + " lastOperation--" + lastRec + " mLocationMode=" + this.mLocationMode);
            if (this.mLocationMode == 0 && lastRec.contains("com.google.android.gms")) {
                Log.d(TAG, "Open location when GMS closed it abnormally");
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", 3, UserHandle.CURRENT.getIdentifier());
            }
        }
    }

    @GuardedBy({"mLock"})
    public List<String> getAllRec() {
        if (!this.mHasFiltered) {
            filterLmChangeRec();
            this.mHasFiltered = true;
        }
        return this.mLocModeChangeHis;
    }

    @GuardedBy({"mLock"})
    public int getLocMode() {
        return this.mLocationMode;
    }

    @GuardedBy({"mLock"})
    public void resetRec() {
        List<String> list = this.mLocModeChangeHis;
        if (list != null) {
            list.clear();
        }
        this.mHasFiltered = false;
    }
}

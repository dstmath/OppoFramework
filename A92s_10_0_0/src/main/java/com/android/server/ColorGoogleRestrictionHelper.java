package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorGoogleRestrictionHelper {
    /* access modifiers changed from: private */
    public static final boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG_PANIC);
    private static final String TAG = "ColorGoogleRestrictionHelper";
    private static ColorGoogleRestrictionHelper mInstance = null;
    ArrayList<IColorGoogleRestrictCallback> mCallbacks = new ArrayList<>();
    private Context mContext = null;
    /* access modifiers changed from: private */
    public ArrayList<String> mGoogleRestrictList = new ArrayList<>();
    /* access modifiers changed from: private */
    public volatile boolean mGoogleRestricted = DEBUG_PANIC;
    private BroadcastReceiver mGoogleRestriction = new BroadcastReceiver() {
        /* class com.android.server.ColorGoogleRestrictionHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            boolean restrictlistChange = intent.getBooleanExtra("restrict_list_change", ColorGoogleRestrictionHelper.DEBUG_PANIC);
            boolean restrict = intent.getBooleanExtra("restrict_enable", ColorGoogleRestrictionHelper.this.mGoogleRestricted);
            ArrayList<String> restrictList = intent.getStringArrayListExtra("restrict_list");
            if (ColorGoogleRestrictionHelper.DEBUG_PANIC) {
                Slog.d(ColorGoogleRestrictionHelper.TAG, "receive broadcast, old restrict " + ColorGoogleRestrictionHelper.this.mGoogleRestricted + ", new restrict " + restrict + ", whitelistChange = " + restrictlistChange);
            }
            boolean valueChange = ColorGoogleRestrictionHelper.DEBUG_PANIC;
            boolean listChange = ColorGoogleRestrictionHelper.DEBUG_PANIC;
            synchronized (ColorGoogleRestrictionHelper.this.mLock) {
                if (restrict != ColorGoogleRestrictionHelper.this.mGoogleRestricted) {
                    if (!restrictlistChange && ColorGoogleRestrictionHelper.this.mGoogleRestrictList.size() == 0 && restrictList != null && restrictList.size() > 0) {
                        ColorGoogleRestrictionHelper.this.mGoogleRestrictList.addAll(restrictList);
                    }
                    boolean unused = ColorGoogleRestrictionHelper.this.mGoogleRestricted = restrict;
                    valueChange = true;
                }
                if (restrictlistChange) {
                    ColorGoogleRestrictionHelper.this.mGoogleRestrictList.clear();
                    if (restrictList != null && restrictList.size() > 0) {
                        ColorGoogleRestrictionHelper.this.mGoogleRestrictList.addAll(restrictList);
                    }
                    listChange = ColorGoogleRestrictionHelper.this.mGoogleRestricted;
                }
            }
            if (valueChange || listChange) {
                synchronized (ColorGoogleRestrictionHelper.this.mCallbacks) {
                    Iterator<IColorGoogleRestrictCallback> it = ColorGoogleRestrictionHelper.this.mCallbacks.iterator();
                    while (it.hasNext()) {
                        IColorGoogleRestrictCallback callback = it.next();
                        if (valueChange) {
                            callback.restrictChange();
                        }
                        if (listChange) {
                            callback.restrictListChange();
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Object mLock = new Object();

    public static ColorGoogleRestrictionHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ColorGoogleRestrictionHelper.class) {
                if (mInstance == null) {
                    mInstance = new ColorGoogleRestrictionHelper(context);
                }
            }
        }
        return mInstance;
    }

    public ColorGoogleRestrictionHelper(Context context) {
        this.mContext = context;
        context.registerReceiver(this.mGoogleRestriction, new IntentFilter("oppo.intent.action.google_restrict_change"), "oppo.permission.OPPO_COMPONENT_SAFE", null);
    }

    public void addCallback(IColorGoogleRestrictCallback callback) {
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.contains(callback)) {
                this.mCallbacks.add(callback);
            }
        }
    }

    public void removeCallback(IColorGoogleRestrictCallback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    public boolean isGoogleRestrct() {
        return this.mGoogleRestricted;
    }

    public ArrayList<String> getGoogleRestrictList() {
        ArrayList<String> arrayList;
        synchronized (this.mLock) {
            arrayList = this.mGoogleRestrictList;
        }
        return arrayList;
    }
}

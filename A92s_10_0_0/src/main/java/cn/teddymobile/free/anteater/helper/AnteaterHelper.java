package cn.teddymobile.free.anteater.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import cn.teddymobile.free.anteater.helper.logger.Logger;
import cn.teddymobile.free.anteater.helper.view.HierarchyView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnteaterHelper {
    private static final String TAG = AnteaterHelper.class.getSimpleName();
    private static volatile AnteaterHelper sInstance = null;

    public static AnteaterHelper getInstance() {
        if (sInstance == null) {
            synchronized (AnteaterHelper.class) {
                if (sInstance == null) {
                    sInstance = new AnteaterHelper();
                }
            }
        }
        return sInstance;
    }

    private AnteaterHelper() {
    }

    public void showHierarchyDialog(View decorView) {
        HierarchyView hierarchyView = new HierarchyView(decorView.getContext());
        hierarchyView.updateModel(decorView, 0);
        new AlertDialog.Builder(decorView.getContext()).setView(hierarchyView).create().show();
    }

    public void logViewInfo(View view) {
        logResourceName(view);
        logViewHierarchy(view);
    }

    private void logResourceName(View view) {
        int id = view.getId();
        String resName = null;
        if (id != -1) {
            try {
                resName = view.getResources().getResourceName(id);
            } catch (Exception e) {
            }
        }
        String str = TAG;
        Logger.i(str, "id = " + id);
        String str2 = TAG;
        Logger.i(str2, "resName = " + resName);
    }

    private void logViewHierarchy(View view) {
        try {
            List<String> classList = new ArrayList<>();
            List<Integer> indexList = new ArrayList<>();
            View currentView = view;
            while (!currentView.getClass().getName().endsWith("DecorView")) {
                ViewGroup parent = (ViewGroup) currentView.getParent();
                classList.add(currentView.getClass().getName());
                indexList.add(Integer.valueOf(parent.indexOfChild(currentView)));
                currentView = parent;
            }
            StringBuilder info = new StringBuilder("[");
            for (int i = classList.size() - 1; i >= 0; i--) {
                info.append(String.format(Locale.getDefault(), "{\"class_name\": \"%s\",\"child_index\": %d}", classList.get(i), Integer.valueOf(indexList.get(i).intValue())));
                if (i != 0) {
                    info.append(SmsManager.REGEX_PREFIX_DELIMITER);
                }
            }
            info.append("]");
            Logger.i(TAG, "onClick ViewHierarchy = \n" + info.toString());
        } catch (Exception e) {
        }
    }

    public void logActivityInfo(Activity activity) {
        String str = TAG;
        Logger.i(str, "onCreate " + activity.getClass().getSimpleName());
        try {
            Intent intent = activity.getIntent();
            Bundle extra = intent.getExtras();
            if (extra != null) {
                Logger.i(TAG, "Intent extra :");
                for (String key : extra.keySet()) {
                    String str2 = TAG;
                    Logger.i(str2, key + " = " + extra.get(key));
                }
            } else {
                Logger.i(TAG, "Intent extra is null.");
            }
            Uri uri = intent.getData();
            if (uri != null) {
                String str3 = TAG;
                Logger.i(str3, "Intent data = " + uri.toString());
                return;
            }
            Logger.i(TAG, "Intent data is null.");
        } catch (Exception e) {
            Logger.w(TAG, e.getMessage(), e);
        }
    }
}

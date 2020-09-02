package android.view;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.TextView;
import com.color.direct.ColorDirectFindCmd;
import com.color.direct.ColorDirectFindCmds;
import com.color.direct.ColorDirectFindResult;
import com.color.direct.ColorDirectUtils;
import com.color.direct.IColorDirectFindCallback;
import com.color.favorite.ColorFavoriteCallback;
import com.color.favorite.ColorFavoriteData;
import com.color.favorite.ColorFavoriteHelper;
import com.color.favorite.ColorFavoriteResult;
import com.color.favorite.IColorFavoriteConstans;
import com.color.favorite.IColorFavoriteManager;
import com.color.util.ColorLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorDirectViewDump implements IColorFavoriteConstans {
    private static final boolean DBG = ColorDirectUtils.DBG;
    private static final String TAG = "DirectService";

    public void findCmd(ViewRootImpl viewAncestor, ColorDirectFindCmd findCmd) {
        ColorDirectFindCmds cmd = findCmd.getCommand();
        boolean z = DBG;
        ColorLog.d(z, TAG, "findCmd : " + cmd);
        int i = AnonymousClass1.$SwitchMap$com$color$direct$ColorDirectFindCmds[cmd.ordinal()];
        if (i == 1) {
            findText(viewAncestor, findCmd.getCallback(), findCmd.getBundle().getStringArrayList("id_names"));
        } else if (i == 2) {
            findFavorite(viewAncestor, findCmd.getCallback());
        } else if (i != 3) {
            ColorDirectUtils.onFindFailed(findCmd.getCallback(), "unknown_cmd");
        } else {
            saveFavorite(viewAncestor, findCmd.getCallback());
        }
    }

    /* renamed from: android.view.ColorDirectViewDump$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$color$direct$ColorDirectFindCmds = new int[ColorDirectFindCmds.values().length];

        static {
            try {
                $SwitchMap$com$color$direct$ColorDirectFindCmds[ColorDirectFindCmds.FIND_TEXT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$color$direct$ColorDirectFindCmds[ColorDirectFindCmds.FIND_FAVORITE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$color$direct$ColorDirectFindCmds[ColorDirectFindCmds.SAVE_FAVORITE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private String getPackageName(String idName, String packageName) {
        String[] s = idName.toString().split(":");
        if (s == null || s.length != 2) {
            return packageName;
        }
        return s[0];
    }

    private String getIdName(String idName) {
        String[] s = idName.toString().split(":");
        if (s == null || s.length != 2) {
            return idName;
        }
        return s[1].replace("id/", "");
    }

    private CharSequence getTextFromView(ViewRootImpl viewAncestor, Resources resources, String idName, String packageName) {
        int id;
        View view;
        boolean z = DBG;
        ColorLog.d(z, TAG, "getTextFromView : [" + packageName + ":id/" + idName + "]");
        View root = viewAncestor.getView();
        if (root != null && (id = resources.getIdentifier(idName, "id", packageName)) > 0 && (view = root.findViewById(id)) != null && (view instanceof TextView)) {
            return ((TextView) view).getText();
        }
        return null;
    }

    private void findText(ViewRootImpl viewAncestor, IColorDirectFindCallback callback, ArrayList<String> idNames) {
        if (idNames == null || idNames.size() <= 0) {
            ColorDirectUtils.onFindFailed(callback, "no_idnames");
            return;
        }
        ColorLog.d(DBG, TAG, "findText");
        Context context = viewAncestor.mContext;
        Resources resources = context.getResources();
        String currPkgName = context.getPackageName();
        Iterator<String> it = idNames.iterator();
        while (it.hasNext()) {
            String idName = it.next();
            CharSequence text = getTextFromView(viewAncestor, resources, getIdName(idName), getPackageName(idName, currPkgName));
            if (!TextUtils.isEmpty(text)) {
                boolean z = DBG;
                ColorLog.d(z, TAG, "findText : text=" + ((Object) text));
                Bundle result = new Bundle();
                result.putCharSequence("result_text", text);
                ColorDirectUtils.onFindSuccess(callback, result);
                return;
            }
        }
        ColorDirectUtils.onFindFailed(callback, "no_text");
    }

    private void findFavorite(ViewRootImpl viewAncestor, IColorDirectFindCallback callback) {
        ColorLog.d(DBG, TAG, "findFavorite");
        OppoFeatureCache.getOrCreate(IColorFavoriteManager.DEFAULT, new Object[0]).processCrawl(viewAncestor.getView(), new FavoriteCallback(callback));
    }

    private void saveFavorite(ViewRootImpl viewAncestor, IColorDirectFindCallback callback) {
        ColorLog.d(DBG, TAG, "saveFavorite");
        OppoFeatureCache.getOrCreate(IColorFavoriteManager.DEFAULT, new Object[0]).processSave(viewAncestor.getView(), new FavoriteCallback(callback));
    }

    private static class FavoriteCallback extends ColorFavoriteCallback {
        private final WeakReference<IColorDirectFindCallback> mCallback;

        public FavoriteCallback(IColorDirectFindCallback callback) {
            this.mCallback = new WeakReference<>(callback);
        }

        public boolean isSettingOn(Context context) {
            return ColorFavoriteHelper.isSettingOn(context);
        }

        public void onFavoriteStart(Context context, ColorFavoriteResult result) {
            IColorDirectFindCallback callback = this.mCallback.get();
            if (callback != null) {
                try {
                    ColorDirectFindResult findResult = new ColorDirectFindResult();
                    Bundle bundle = findResult.getBundle();
                    String packageName = result.getPackageName();
                    String str = this.TAG;
                    ColorLog.d(false, str, "onFavoriteStart : packageName=" + packageName);
                    if (!TextUtils.isEmpty(packageName)) {
                        bundle.putString("package_name", packageName);
                    }
                    callback.onDirectInfoFound(findResult);
                } catch (RemoteException e) {
                    ColorLog.e(this.TAG, e.toString());
                } catch (Exception e2) {
                    ColorLog.e(this.TAG, e2.toString());
                }
            }
        }

        public void onFavoriteFinished(Context context, ColorFavoriteResult result) {
            IColorDirectFindCallback callback = this.mCallback.get();
            if (callback != null) {
                try {
                    ColorDirectFindResult findResult = new ColorDirectFindResult();
                    Bundle bundle = findResult.getBundle();
                    ArrayList<ColorFavoriteData> data = result.getData();
                    if (!data.isEmpty()) {
                        String str = this.TAG;
                        ColorLog.d(false, str, "onFavoriteFinished : data=" + data.size() + ":" + data.get(0));
                        bundle.putParcelableArrayList("result_data", extractData(data));
                        bundle.putStringArrayList("result_titles", extractTitles(data));
                    }
                    String error = result.getError();
                    if (!TextUtils.isEmpty(error)) {
                        String str2 = this.TAG;
                        ColorLog.d(false, str2, "onFavoriteFinished : error=" + error);
                        bundle.putString("result_error", error);
                    }
                    callback.onDirectInfoFound(findResult);
                } catch (RemoteException e) {
                    ColorLog.e(this.TAG, e.toString());
                } catch (Exception e2) {
                    ColorLog.e(this.TAG, e2.toString());
                }
            }
        }

        private ArrayList<Bundle> extractData(ArrayList<ColorFavoriteData> data) {
            ArrayList<Bundle> bundles = new ArrayList<>();
            synchronized (data) {
                Iterator<ColorFavoriteData> it = data.iterator();
                while (it.hasNext()) {
                    ColorFavoriteData d = it.next();
                    Bundle bundle = new Bundle();
                    bundle.putString("data_title", d.getTitle());
                    bundle.putString("data_url", d.getUrl());
                    bundles.add(bundle);
                }
            }
            return bundles;
        }

        private ArrayList<String> extractTitles(ArrayList<ColorFavoriteData> data) {
            ArrayList<String> titles = new ArrayList<>();
            synchronized (data) {
                Iterator<ColorFavoriteData> it = data.iterator();
                while (it.hasNext()) {
                    titles.add(it.next().getTitle());
                }
            }
            return titles;
        }
    }
}

package com.color.favorite;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import cn.teddymobile.free.anteater.AnteaterClient;
import cn.teddymobile.free.anteater.helper.AnteaterHelper;
import cn.teddymobile.free.anteater.resources.RuleResourcesClient;
import com.coloros.deepthinker.AlgorithmBinderCode;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorEngineTeddy extends ColorFavoriteEngine {
    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onInit() {
        AnteaterClient.getInstance().init();
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onRelease() {
        AnteaterClient.getInstance().release();
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onProcessClick(View clickView, ColorFavoriteCallback callback) {
        startSave(clickView, callback, "click");
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onProcessCrawl(View rootView, ColorFavoriteCallback callback) {
        if (rootView != null) {
            rootView = rootView.findViewById(16908290);
        }
        startCrawl(rootView, callback, "display");
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onProcessSave(View rootView, ColorFavoriteCallback callback) {
        if (rootView != null) {
            rootView = rootView.findViewById(16908290);
        }
        startSave(rootView, callback, "display");
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onLogActivityInfo(Activity activity) {
        AnteaterHelper.getInstance().logActivityInfo(activity);
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onLogViewInfo(View view) {
        AnteaterHelper.getInstance().logViewInfo(view);
    }

    /* access modifiers changed from: protected */
    @Override // com.color.favorite.ColorFavoriteEngine
    public void onLoadRule(Context context, String data, ColorFavoriteCallback callback) {
        AnteaterClient.getInstance().loadRule(context, data, new AntaterLoadCallback(callback));
    }

    @Override // com.color.favorite.IColorFavoriteEngine
    public Handler getWorkHandler() {
        return AnteaterClient.getInstance().getWorkHandler();
    }

    private void startSave(View view, ColorFavoriteCallback callback, String action) {
        AnteaterClient.getInstance().save(action, view, new AnteaterSaveCallback(callback));
    }

    private void startCrawl(View view, ColorFavoriteCallback callback, String action) {
        AnteaterClient.getInstance().process(action, view, new AnteaterProcessCallback(callback));
    }

    private static class AnteaterCallback {
        final WeakReference<ColorFavoriteCallback> mCallback;

        public AnteaterCallback(ColorFavoriteCallback callback) {
            this.mCallback = new WeakReference<>(callback);
        }

        /* access modifiers changed from: protected */
        public boolean isSettingOn(Context context) {
            ColorFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                return callback.isSettingOn(context);
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public Handler onCreateWorkHandler(Context context, String name, int priority) {
            ColorFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                return callback.createWorkHandler(context, name, priority);
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onHandleFinished(Context context, AnteaterClient.ResultData result) {
            ColorFavoriteCallback callback = this.mCallback.get();
            if (callback != null) {
                ColorFavoriteResult favoriteResult = new ColorFavoriteResult();
                favoriteResult.setData(getQueryList(result));
                favoriteResult.setError(getErrorMessage(result.getError()));
                callback.onFavoriteFinished(context, favoriteResult);
            }
        }

        private ArrayList<ColorFavoriteData> getQueryList(AnteaterClient.ResultData result) {
            ArrayList<ColorFavoriteData> data = new ArrayList<>();
            ArrayList<AnteaterClient.QueryData> queryList = result.getQueryList();
            synchronized (queryList) {
                Iterator<AnteaterClient.QueryData> it = queryList.iterator();
                while (it.hasNext()) {
                    AnteaterClient.QueryData queryData = it.next();
                    ColorFavoriteData d = new ColorFavoriteData();
                    d.setTitle(queryData.getTitle());
                    d.setUrl(queryData.getUrl());
                    data.add(d);
                }
            }
            return data;
        }

        private String getErrorMessage(AnteaterClient.ErrorCode errorCode) {
            switch (AnonymousClass1.$SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[errorCode.ordinal()]) {
                case 1:
                    return "No view";
                case 2:
                    return "Not init";
                case 3:
                    return "Not found";
                case 4:
                    return "Unsupported";
                case AlgorithmBinderCode.BIND_EVENT_HANDLE /*{ENCODED_INT: 5}*/:
                    return "Setting off";
                case 6:
                    return "Save failed";
                default:
                    return null;
            }
        }
    }

    /* renamed from: com.color.favorite.ColorEngineTeddy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode = new int[AnteaterClient.ErrorCode.values().length];

        static {
            try {
                $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[AnteaterClient.ErrorCode.NO_VIEW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[AnteaterClient.ErrorCode.NOT_INIT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[AnteaterClient.ErrorCode.NOT_FOUND.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[AnteaterClient.ErrorCode.UNSUPPORT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[AnteaterClient.ErrorCode.SETTING_OFF.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$cn$teddymobile$free$anteater$AnteaterClient$ErrorCode[AnteaterClient.ErrorCode.SAVE_FAILED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private static class AntaterLoadCallback extends AnteaterCallback implements RuleResourcesClient.LoadCallback {
        public AntaterLoadCallback(ColorFavoriteCallback callback) {
            super(callback);
        }

        public void onLoadResult(Context context, boolean noRule, boolean emptyRule, ArrayList<String> sceneList) {
            ColorFavoriteCallback callback = (ColorFavoriteCallback) this.mCallback.get();
            if (callback != null) {
                callback.onLoadFinished(context, noRule, emptyRule, sceneList);
            }
        }
    }

    private static class AnteaterSaveCallback extends AnteaterCallback implements AnteaterClient.SaveCallback {
        public AnteaterSaveCallback(ColorFavoriteCallback callback) {
            super(callback);
        }

        public Handler createWorkHandler(Context context, String name, int priority) {
            return onCreateWorkHandler(context, name, priority);
        }

        public boolean isSettingOff(Context context) {
            return !isSettingOn(context);
        }

        public void onSaveResult(Context context, AnteaterClient.ResultData result) {
            onHandleFinished(context, result);
        }
    }

    private static class AnteaterProcessCallback extends AnteaterCallback implements AnteaterClient.ProcessCallback {
        public AnteaterProcessCallback(ColorFavoriteCallback callback) {
            super(callback);
        }

        public Handler createWorkHandler(Context context, String name, int priority) {
            return onCreateWorkHandler(context, name, priority);
        }

        public boolean isSettingOff(Context context) {
            return !isSettingOn(context);
        }

        public void onProcessResult(Context context, AnteaterClient.ResultData result) {
            onHandleFinished(context, result);
        }
    }
}

package com.nearme.romupdate.bugly;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import com.nearme.romupdate.bugly.IBuglyControl.Stub;
import java.util.List;

public class Client {
    private static final int MSG_INIT_BUGLY = 100;
    private static Client mIns;
    private CallBack mCallBack;
    private ServiceConnection mConn = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            Client.this.mService = null;
            Client.this.mContext = null;
            Client.this.mPackageName = null;
            Client.this.mCallBack = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Client.this.mService = Stub.asInterface(service);
            boolean result = false;
            try {
                result = Client.this.mService.isEnableBuglyLog(Client.this.mPackageName);
            } catch (RemoteException e) {
            }
            Client.this.mCallBack.isEnableBugly(result);
            Client.this.mContext.unbindService(Client.this.mConn);
        }
    };
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Client.MSG_INIT_BUGLY /*100*/:
                    Client.mIns.bindToService(Client.this.mContext, Client.this.mPackageName, Client.this.mCallBack);
                    return;
                default:
                    return;
            }
        }
    };
    private String mPackageName;
    private IBuglyControl mService;

    public interface CallBack {
        void isEnableBugly(boolean z);
    }

    private static Client getInstance() {
        if (mIns == null) {
            mIns = new Client();
        }
        return mIns;
    }

    public static void setBuglyCallBack(Context context, CallBack callBack) {
        setBuglyCallBack(context, context.getPackageName(), callBack);
    }

    public static void setBuglyCallBack(Context context, String packageName, CallBack callBack) {
        getInstance();
        mIns.mContext = context;
        mIns.mPackageName = packageName;
        mIns.mCallBack = callBack;
        mIns.mHandler.sendMessageDelayed(mIns.mHandler.obtainMessage(MSG_INIT_BUGLY), 2000);
    }

    private void bindToService(Context context, String packageName, CallBack callBack) {
        Intent implicitIntent = new Intent("action.com.nearme.romupdate.bugly.BuglyService");
        this.mPackageName = packageName;
        if (callBack != null && context != null) {
            this.mCallBack = callBack;
            this.mContext = context;
            Intent explicitIntent = wrapIntent(context, implicitIntent);
            if (explicitIntent != null) {
                context.bindService(explicitIntent, this.mConn, 1);
            }
        }
    }

    public static Intent wrapIntent(Context context, Intent implicitIntent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}

package com.color.antivirus.qihoo;

import android.app.ColorUxIconConstants;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.text.TextUtils;
import android.view.WindowManager;
import com.color.antivirus.AntivirusLog;
import com.coloros.deepthinker.AlgorithmBinderCode;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BinderStubClient implements IBinder {
    private static final String APPLICATION_THREAD_SERVICE_NAME = "applicationthread";
    private static final String TAG = "BinderStubClient";
    private static final String bidRule = "262144,phone,com.android.internal.telephony.ITelephony,TRANSACTION_getLine1NumberForDisplay,NULL\n263168,phone,com.android.internal.telephony.ITelephony,TRANSACTION_getImeiForSlot,NULL\n264192,phone,com.android.internal.telephony.ITelephony,TRANSACTION_getDeviceId,NULL\n265216,phone,com.android.internal.telephony.ITelephony,TRANSACTION_getActivePhoneTypeForSlot,NULL\n524288,isms,com.android.internal.telephony.ISms,TRANSACTION_sendTextForSubscriber,NULL\n525312,isms,com.android.internal.telephony.ISms,TRANSACTION_sendMultipartTextForSubscriber,NULL\n526336,isms,com.android.internal.telephony.ISms,TRANSACTION_sendDataForSubscriber,NULL\n786432,isub,com.android.internal.telephony.ISub,TRANSACTION_getActiveSubscriptionInfoList,NULL\n787456,isub,com.android.internal.telephony.ISub,TRANSACTION_getActiveSubscriptionInfoForSimSlotIndex,NULL\n1048576,iphonesubinfo,com.android.internal.telephony.IPhoneSubInfo,TRANSACTION_getVoiceMailNumberForSubscriber,NULL\n1049600,iphonesubinfo,com.android.internal.telephony.IPhoneSubInfo,TRANSACTION_getVoiceMailAlphaTagForSubscriber,NULL\n1050624,iphonesubinfo,com.android.internal.telephony.IPhoneSubInfo,TRANSACTION_getSubscriberIdForSubscriber,NULL\n1051648,iphonesubinfo,com.android.internal.telephony.IPhoneSubInfo,TRANSACTION_getLine1NumberForSubscriber,NULL\n1052672,iphonesubinfo,com.android.internal.telephony.IPhoneSubInfo,TRANSACTION_getIccSerialNumberForSubscriber,NULL\n1053696,iphonesubinfo,com.android.internal.telephony.IPhoneSubInfo,TRANSACTION_getGroupIdLevel1ForSubscriber,NULL\n1310720,wifi,android.net.wifi.IWifiManager,TRANSACTION_startScan,NULL\n1311744,wifi,android.net.wifi.IWifiManager,TRANSACTION_setWifiEnabled,NULL\n1313792,wifi,android.net.wifi.IWifiManager,TRANSACTION_removeNetwork,NULL\n1314816,wifi,android.net.wifi.IWifiManager,TRANSACTION_reconnect,NULL\n1315840,wifi,android.net.wifi.IWifiManager,TRANSACTION_reassociate,NULL\n1316864,wifi,android.net.wifi.IWifiManager,TRANSACTION_isScanAlwaysAvailable,NULL\n1317888,wifi,android.net.wifi.IWifiManager,TRANSACTION_getWifiEnabledState,NULL\n1318912,wifi,android.net.wifi.IWifiManager,TRANSACTION_getScanResults,NULL\n1319936,wifi,android.net.wifi.IWifiManager,TRANSACTION_getDhcpInfo,NULL\n1320960,wifi,android.net.wifi.IWifiManager,TRANSACTION_getConnectionInfo,NULL\n1321984,wifi,android.net.wifi.IWifiManager,TRANSACTION_getConfiguredNetworks,NULL\n1323008,wifi,android.net.wifi.IWifiManager,TRANSACTION_enableNetwork,NULL\n1324032,wifi,android.net.wifi.IWifiManager,TRANSACTION_disconnect,NULL\n1325056,wifi,android.net.wifi.IWifiManager,TRANSACTION_disableNetwork,NULL\n1326080,wifi,android.net.wifi.IWifiManager,TRANSACTION_addOrUpdateNetwork,NULL\n1572864,usagestats,android.app.usage.IUsageStatsManager,TRANSACTION_queryUsageStats,NULL\n1835008,restrictions,android.content.IRestrictionsManager,TRANSACTION_requestPermission,NULL\n1836032,restrictions,android.content.IRestrictionsManager,TRANSACTION_notifyPermissionResponse,NULL\n2097152,device_policy,android.app.admin.IDevicePolicyManager,TRANSACTION_uninstallCaCerts,NULL\n2098176,device_policy,android.app.admin.IDevicePolicyManager,TRANSACTION_removeActiveAdmin,NULL\n2099200,device_policy,android.app.admin.IDevicePolicyManager,TRANSACTION_installCaCert,NULL\n2100224,device_policy,android.app.admin.IDevicePolicyManager,TRANSACTION_createAndManageUser,NULL\n2101248,device_policy,android.app.admin.IDevicePolicyManager,TRANSACTION_resetPassword,NULL\n2359296,window,android.view.IWindowManager,TRANSACTION_reenableKeyguard,NULL\n2360320,window,android.view.IWindowManager,TRANSACTION_lockNow,NULL\n2361344,window,android.view.IWindowManager,TRANSACTION_isKeyguardSecure,NULL\n2362368,window,android.view.IWindowManager,TRANSACTION_isKeyguardLocked,NULL\n2363392,window,android.view.IWindowManager,TRANSACTION_exitKeyguardSecurely,NULL\n2364416,window,android.view.IWindowManager,TRANSACTION_dismissKeyguard,NULL\n2365440,window,android.view.IWindowManager,TRANSACTION_disableKeyguard,NULL\n2366464,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1\n2366465,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2\n2366466,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*3\n2366467,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*4\n2366468,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*99\n2366469,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1000\n2366470,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1001\n2366471,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1002\n2366472,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1003\n2366473,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1004\n2366474,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*1005\n2366475,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2000\n2366476,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2001\n2366477,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2002\n2366478,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2003\n2366479,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2004\n2366480,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2005\n2366481,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2006\n2366482,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2007\n2366483,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2008\n2366484,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2009\n2366485,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2010\n2366486,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2011\n2366487,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2012\n2366488,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2013\n2366489,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2014\n2366490,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2030\n2366491,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2032\n2366492,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|-1*-1*2038\n2621440,wallpaper,android.app.IWallpaperManager,TRANSACTION_setWallpaper,NULL\n2883584,vibrator,android.os.IVibratorService,TRANSACTION_vibrate,NULL\n2884608,vibrator,android.os.IVibratorService,TRANSACTION_cancelVibrate,NULL\n3145728,telephony.registry,com.android.internal.telephony.ITelephonyRegistry,TRANSACTION_listenForSubscriber,NULL\n3407872,statusbar,com.android.internal.statusbar.IStatusBarService,TRANSACTION_setSystemUiVisibility,NULL\n3408896,statusbar,com.android.internal.statusbar.IStatusBarService,TRANSACTION_setIconVisibility,NULL\n3409920,statusbar,com.android.internal.statusbar.IStatusBarService,TRANSACTION_setIcon,NULL\n3410944,statusbar,com.android.internal.statusbar.IStatusBarService,TRANSACTION_disableForUser,NULL\n3670016,user,android.os.IUserManager,TRANSACTION_getApplicationRestrictions,NULL\n3932160,package,android.content.pm.IPackageManager,TRANSACTION_setComponentEnabledSetting,cn|NULL;i|0\n3932161,package,android.content.pm.IPackageManager,TRANSACTION_setComponentEnabledSetting,cn|NULL;i|1\n3932162,package,android.content.pm.IPackageManager,TRANSACTION_setComponentEnabledSetting,cn|NULL;i|2\n3932163,package,android.content.pm.IPackageManager,TRANSACTION_setComponentEnabledSetting,cn|NULL;i|3\n3933184,package,android.content.pm.IPackageManager,TRANSACTION_setApplicationEnabledSetting,NULL\n3934208,package,android.content.pm.IPackageManager,TRANSACTION_getPermissionInfo,NULL\n3935232,package,android.content.pm.IPackageManager,TRANSACTION_getInstalledPackages,NULL\n3936256,package,android.content.pm.IPackageManager,TRANSACTION_getInstalledApplications,NULL\n3937280,package,android.content.pm.IPackageManager,TRANSACTION_clearPackagePreferredActivities,NULL\n3938304,package,android.content.pm.IPackageManager,TRANSACTION_addPreferredActivity,NULL\n4456448,launcherapps,android.content.pm.ILauncherApps,TRANSACTION_startShortcut,NULL\n4457472,launcherapps,android.content.pm.ILauncherApps,TRANSACTION_pinShortcuts,NULL\n4458496,launcherapps,android.content.pm.ILauncherApps,TRANSACTION_getLauncherActivities,NULL\n4459520,launcherapps,android.content.pm.ILauncherApps,TRANSACTION_addOnAppsChangedListener,NULL\n4718592,notification,android.app.INotificationManager,TRANSACTION_enqueueToast,NULL\n4719616,notification,android.app.INotificationManager,TRANSACTION_enqueueNotificationWithTag,NULL\n4720640,notification,android.app.INotificationManager,TRANSACTION_cancelNotificationWithTag,NULL\n4980736,network_management,android.os.INetworkManagementService,TRANSACTION_registerObserver,NULL\n4981760,network_management,android.os.INetworkManagementService,TRANSACTION_addRoute,NULL\n5242880,netpolicy,android.net.INetworkPolicyManager,TRANSACTION_registerListener,NULL\n5505024,imms,com.android.internal.telephony.IMms,TRANSACTION_sendMessage,NULL\n5767168,media_session,android.media.session.ISessionManager,TRANSACTION_dispatchAdjustVolume,NULL\n6029312,lock_settings,com.android.internal.widget.ILockSettings,TRANSACTION_setString,NULL\n6030336,lock_settings,com.android.internal.widget.ILockSettings,TRANSACTION_setLong,NULL\n6291456,location,android.location.ILocationManager,TRANSACTION_sendExtraCommand,NULL\n6292480,location,android.location.ILocationManager,TRANSACTION_requestLocationUpdates,NULL\n6293504,location,android.location.ILocationManager,TRANSACTION_requestGeofence,NULL\n6294528,location,android.location.ILocationManager,TRANSACTION_removeUpdates,NULL\n6295552,location,android.location.ILocationManager,TRANSACTION_registerGnssStatusCallback,NULL\n6296576,location,android.location.ILocationManager,TRANSACTION_isProviderEnabledForUser,NULL\n6297600,location,android.location.ILocationManager,TRANSACTION_getProviders,NULL\n6298624,location,android.location.ILocationManager,TRANSACTION_getLastLocation,NULL\n6299648,location,android.location.ILocationManager,TRANSACTION_getBestProvider,NULL\n6553600,jobscheduler,android.app.job.IJobScheduler,TRANSACTION_schedule,NULL\n6815744,country_detector,android.location.ICountryDetector,TRANSACTION_detectCountry,NULL\n7077888,consumer_ir,android.hardware.IConsumerIrService,TRANSACTION_transmit,NULL\n7340032,connectivity,android.net.IConnectivityManager,TRANSACTION_requestNetwork,NULL\n7341056,connectivity,android.net.IConnectivityManager,TRANSACTION_prepareVpn,NULL\n7342080,connectivity,android.net.IConnectivityManager,TRANSACTION_isActiveNetworkMetered,NULL\n7343104,connectivity,android.net.IConnectivityManager,TRANSACTION_getNetworkInfo,NULL\n7344128,connectivity,android.net.IConnectivityManager,TRANSACTION_getNetworkCapabilities,NULL\n7345152,connectivity,android.net.IConnectivityManager,TRANSACTION_getLinkProperties,NULL\n7346176,connectivity,android.net.IConnectivityManager,TRANSACTION_getAllNetworks,NULL\n7347200,connectivity,android.net.IConnectivityManager,TRANSACTION_getAllNetworkInfo,NULL\n7348224,connectivity,android.net.IConnectivityManager,TRANSACTION_getActiveNetworkInfo,NULL\n7602176,content,android.content.IContentService,TRANSACTION_registerContentObserver,uri|content://call_log/\n7602177,content,android.content.IContentService,TRANSACTION_registerContentObserver,uri|content://browser/\n7602178,content,android.content.IContentService,TRANSACTION_registerContentObserver,uri|content://com.android.contacts/\n7602179,content,android.content.IContentService,TRANSACTION_registerContentObserver,uri|content://com.android.calendar/\n7602180,content,android.content.IContentService,TRANSACTION_registerContentObserver,uri|content://telephony/\n7602181,content,android.content.IContentService,TRANSACTION_registerContentObserver,uri|content://sms/\n7864320,clipboard,android.content.IClipboard,TRANSACTION_getPrimaryClip,NULL\n7865344,clipboard,android.content.IClipboard,TRANSACTION_addPrimaryClipChangedListener,NULL\n8126464,audio,android.media.IAudioService,TRANSACTION_setStreamVolume,NULL\n8127488,audio,android.media.IAudioService,TRANSACTION_setSpeakerphoneOn,NULL\n8128512,audio,android.media.IAudioService,TRANSACTION_setMode,NULL\n8129536,audio,android.media.IAudioService,TRANSACTION_setMicrophoneMute,NULL\n8130560,audio,android.media.IAudioService,TRANSACTION_setMasterMute,NULL\n8131584,audio,android.media.IAudioService,TRANSACTION_requestAudioFocus,NULL\n8132608,audio,android.media.IAudioService,TRANSACTION_playSoundEffectVolume,NULL\n8133632,audio,android.media.IAudioService,TRANSACTION_adjustStreamVolume,NULL\n8388608,appops,com.android.internal.app.IAppOpsService,TRANSACTION_setMode,NULL\n8650752,batterystats,com.android.internal.app.IBatteryStats,TRANSACTION_takeUidSnapshots,NULL\n8912896,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.SEND*~\n8912897,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.WEB_SEARCH*~\n8912898,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.EDIT*~\n8912899,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.app.action.ADD_DEVICE_ADMIN*~\n8912900,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.ATTACH_DATA*~\n8912901,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.CALL*~\n8912902,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.DELETE*~\n8912903,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.SEARCH*~\n8912904,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.PICK*~\n8912905,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.PICK_ACTIVITY*~\n8912906,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.SENDTO*~\n8912907,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.GET_CONTENT*~\n8912908,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.DIAL*~\n8912909,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.MAIN*~\n8912910,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.INSERT*~\n8912911,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.htmlviewer\n8912912,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.mms\n8912913,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.phone\n8912914,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.dialer\n8912915,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.coloros.gallery3d\n8912916,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.vending\n8912917,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.heytap.browser\n8912918,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.coloros.calendar\n8912919,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.contacts\n8912920,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.gallery\n8912921,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.heytap.market\n8912922,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.oppo.music\n8912923,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.packageinstaller\n8912924,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.calendar2\n8912925,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.chrome\n8912926,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*com.android.settings\n8912927,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|android.intent.action.VIEW*~\n8912928,activity_task,android.app.IActivityTaskManager,TRANSACTION_startActivity,sbn|NULL;s|NULL;acn|~*~\n9175040,activity,android.app.IActivityManager,TRANSACTION_updateConfiguration,NULL\n9176064,activity,android.app.IActivityManager,TRANSACTION_unregisterReceiver,NULL\n9177088,activity,android.app.IActivityManager,TRANSACTION_unbindService,NULL\n9178112,activity_task,android.app.IActivityTaskManager,TRANSACTION_setTaskDescription,NULL\n9179136,activity_task,android.app.IActivityTaskManager,TRANSACTION_setRequestedOrientation,NULL\n9180160,activity_task,android.app.IActivityTaskManager,TRANSACTION_navigateUpTo,NULL\n9181184,activity_task,android.app.IActivityTaskManager,TRANSACTION_moveTaskToFront,NULL\n9182208,activity,android.app.IActivityManager,TRANSACTION_killBackgroundProcesses,NULL\n9183232,activity,android.app.IActivityManager,TRANSACTION_getRunningAppProcesses,NULL\n9184256,activity_task,android.app.IActivityTaskManager,TRANSACTION_getRecentTasks,NULL\n9185280,activity,android.app.IActivityManager,TRANSACTION_getProcessMemoryInfo,NULL\n9186304,activity,android.app.IActivityManager,TRANSACTION_getProcessesInErrorState,NULL\n9187328,activity_task,android.app.IActivityTaskManager,TRANSACTION_getAppTasks,NULL\n9188352,activity_task,android.app.IActivityTaskManager,TRANSACTION_finishActivity,NULL\n9189376,activity,android.app.IActivityManager,TRANSACTION_closeSystemDialogs,NULL\n9190400,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.intent.action.PACKAGE_CHANGED\n9190401,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.intent.action.PACKAGE_REMOVED\n9190402,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.intent.action.TIME_TICK\n9190403,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.intent.action.TIME_SET\n9190404,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.intent.action.BATTERY_CHANGED\n9190405,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.intent.action.PACKAGE_ADDED\n9190406,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|android.launcher.action.INSTALL_SHORTCUT\n9190407,activity,android.app.IActivityManager,TRANSACTION_broadcastIntent,sbn|NULL;act|~\n9191424,activity,android.app.IActivityManager,TRANSACTION_bindIsolatedService,NULL\n9699328,account,android.accounts.IAccountManager,TRANSACTION_removeAccountExplicitly,NULL\n9700352,account,android.accounts.IAccountManager,TRANSACTION_getAccounts,NULL\n9701376,account,android.accounts.IAccountManager,TRANSACTION_addAccountExplicitlyWithVisibility,NULL\n9702400,account,android.accounts.IAccountManager,TRANSACTION_addAccount,NULL\n9961472,appwidget,com.android.internal.appwidget.IAppWidgetService,TRANSACTION_bindAppWidgetId,NULL\n9962496,appwidget,com.android.internal.appwidget.IAppWidgetService,TRANSACTION_createAppWidgetConfigIntentSender,NULL\n10223616,contentprovider,android.content.IContentProvider,QUERY_TRANSACTION,s|NULL;uri|content://call_log/\n10223617,contentprovider,android.content.IContentProvider,QUERY_TRANSACTION,s|NULL;uri|content://contacts/\n10223618,contentprovider,android.content.IContentProvider,QUERY_TRANSACTION,s|NULL;uri|content://com.android.contacts/\n10223619,contentprovider,android.content.IContentProvider,QUERY_TRANSACTION,s|NULL;uri|content://com.coloros.calendar/\n10223620,contentprovider,android.content.IContentProvider,QUERY_TRANSACTION,s|NULL;uri|content://telephony/\n10223621,contentprovider,android.content.IContentProvider,QUERY_TRANSACTION,s|NULL;uri|content://sms/\n10224640,contentprovider,android.content.IContentProvider,UPDATE_TRANSACTION,s|NULL;uri|content://call_log/\n10224641,contentprovider,android.content.IContentProvider,UPDATE_TRANSACTION,s|NULL;uri|content://contacts/\n10224642,contentprovider,android.content.IContentProvider,UPDATE_TRANSACTION,s|NULL;uri|content://com.android.contacts/\n10224643,contentprovider,android.content.IContentProvider,UPDATE_TRANSACTION,s|NULL;uri|content://com.coloros.calendar/\n10224644,contentprovider,android.content.IContentProvider,UPDATE_TRANSACTION,s|NULL;uri|content://telephony/\n10224645,contentprovider,android.content.IContentProvider,UPDATE_TRANSACTION,s|NULL;uri|content://sms/\n10225664,contentprovider,android.content.IContentProvider,INSERT_TRANSACTION,s|NULL;uri|content://call_log/\n10225665,contentprovider,android.content.IContentProvider,INSERT_TRANSACTION,s|NULL;uri|content://contacts/\n10225666,contentprovider,android.content.IContentProvider,INSERT_TRANSACTION,s|NULL;uri|content://com.android.contacts/\n10225667,contentprovider,android.content.IContentProvider,INSERT_TRANSACTION,s|NULL;uri|content://com.coloros.calendar/\n10225668,contentprovider,android.content.IContentProvider,INSERT_TRANSACTION,s|NULL;uri|content://telephony/\n10225669,contentprovider,android.content.IContentProvider,INSERT_TRANSACTION,s|NULL;uri|content://sms/\n10226688,contentprovider,android.content.IContentProvider,DELETE_TRANSACTION,s|NULL;uri|content://call_log/\n10226689,contentprovider,android.content.IContentProvider,DELETE_TRANSACTION,s|NULL;uri|content://contacts/\n10226690,contentprovider,android.content.IContentProvider,DELETE_TRANSACTION,s|NULL;uri|content://com.android.contacts/\n10226691,contentprovider,android.content.IContentProvider,DELETE_TRANSACTION,s|NULL;uri|content://com.coloros.calendar/\n10226692,contentprovider,android.content.IContentProvider,DELETE_TRANSACTION,s|NULL;uri|content://telephony/\n10226693,contentprovider,android.content.IContentProvider,DELETE_TRANSACTION,s|NULL;uri|content://sms/\n9439232,activity,android.app.IActivityManager,TRANSACTION_finishReceiver,sbn|NULL;i|NULL;s|NULL;bd|NULL;b|true\n9439233,activity,android.app.IActivityManager,TRANSACTION_finishReceiver,sbn|NULL;i|NULL;s|NULL;bd|NULL;b|false\n2366493,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1\n2366494,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2\n2366495,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*3\n2366496,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*4\n2366497,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*99\n2366498,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1000\n2366499,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1001\n2366500,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1002\n2366501,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1003\n2366502,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1004\n2366503,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*1005\n2366504,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2000\n2366505,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2001\n2366506,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2002\n2366507,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2003\n2366508,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2004\n2366509,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2005\n2366510,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2006\n2366511,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2007\n2366512,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2008\n2366513,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2009\n2366514,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2010\n2366515,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2011\n2366516,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2012\n2366517,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2013\n2366518,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2014\n2366519,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2030\n2366520,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2032\n2366521,windowsession,android.view.IWindowSession,TRANSACTION_addToDisplay,sbn|NULL;i|NULL;lp|~*~*2038\n4194304,packageinstaller,android.content.pm.IPackageInstaller,TRANSACTION_uninstall,NULL\n";
    private static Map<IBinder, BinderStubClient> sOriBinder2FakeBinderMap = new ConcurrentHashMap();
    private static Set<String> sReplaceServiceNameSet = new HashSet();
    private static Map<String, Map> sSvc2FakeBinderMap = new ConcurrentHashMap();
    private static Map<String, Map> sSvcName_SvcNameDescMap_Map = new HashMap();
    private static Map<String, Map> sSvcName_TCodeTNameMap_Map = new HashMap();
    private static Map<String, Map> sSvcName_TNameBidMap_Map = new HashMap();
    private static Map<String, Map> sSvcName_TNameParamPatternMap_Map = new HashMap();
    private BinderStubDeathRecipient mAppThrDeathRecipient = null;
    private IBinder mOrigBinder = null;
    private String mReplacedServiceName = null;
    private Map<String, String> mSvcNameDescMap = new HashMap();
    private Map<Integer, String> mTCodeTNameMap = new HashMap();
    private Map<String, Object> mTNameBidMap = new HashMap();
    private Map<String, String> mTNameParamPatternMap = new HashMap();

    static {
        sReplaceServiceNameSet.add(APPLICATION_THREAD_SERVICE_NAME);
        sReplaceServiceNameSet.add("phone");
        sReplaceServiceNameSet.add("isms");
        sReplaceServiceNameSet.add("isub");
        sReplaceServiceNameSet.add("iphonesubinfo");
        sReplaceServiceNameSet.add("wifi");
        sReplaceServiceNameSet.add("usagestats");
        sReplaceServiceNameSet.add("restrictions");
        sReplaceServiceNameSet.add("device_policy");
        sReplaceServiceNameSet.add("window");
        sReplaceServiceNameSet.add("windowsession");
        sReplaceServiceNameSet.add("wallpaper");
        sReplaceServiceNameSet.add("vibrator");
        sReplaceServiceNameSet.add("telephony.registry");
        sReplaceServiceNameSet.add("statusbar");
        sReplaceServiceNameSet.add("package");
        sReplaceServiceNameSet.add("launcherapps");
        sReplaceServiceNameSet.add("notification");
        sReplaceServiceNameSet.add("network_management");
        sReplaceServiceNameSet.add("netpolicy");
        sReplaceServiceNameSet.add("imms");
        sReplaceServiceNameSet.add("media_session");
        sReplaceServiceNameSet.add("lock_settings");
        sReplaceServiceNameSet.add("location");
        sReplaceServiceNameSet.add("jobscheduler");
        sReplaceServiceNameSet.add("country_detector");
        sReplaceServiceNameSet.add("consumer_ir");
        sReplaceServiceNameSet.add("connectivity");
        sReplaceServiceNameSet.add("content");
        sReplaceServiceNameSet.add("clipboard");
        sReplaceServiceNameSet.add("audio");
        sReplaceServiceNameSet.add("appops");
        sReplaceServiceNameSet.add("batterystats");
        sReplaceServiceNameSet.add("activity");
        sReplaceServiceNameSet.add("activity_task");
        sReplaceServiceNameSet.add("account");
        sReplaceServiceNameSet.add("appwidget");
        sReplaceServiceNameSet.add("contentprovider");
        sReplaceServiceNameSet.add("user");
        sReplaceServiceNameSet.add("packageinstaller");
    }

    public BinderStubClient(IBinder orig, String name) {
        this.mOrigBinder = orig;
        this.mReplacedServiceName = name;
        initInObj();
    }

    private static void initStatic() {
        Map<String, String> tmpSvcNameDescMap;
        Map<Integer, String> tmpTCodeTNameMap;
        Map<Integer, String> tmpTCodeTNameMap2;
        Map<String, Object> tmpTNameBidMap;
        Map<String, String> paramBidMap;
        try {
            String[] lines = bidRule.split("\n");
            int length = lines.length;
            char c = 0;
            Map<String, String> tmpTNameParamPatternMap = null;
            int i = 0;
            while (i < length) {
                String[] elements = lines[i].split(",");
                String elementBid = elements[c];
                String elementSvcName = elements[1];
                String elementDescriptor = elements[2];
                String elementTName = elements[3];
                String elementParamPattern = elements[4];
                if (sSvcName_SvcNameDescMap_Map.containsKey(elementSvcName)) {
                    tmpSvcNameDescMap = sSvcName_SvcNameDescMap_Map.get(elementSvcName);
                } else {
                    tmpSvcNameDescMap = new HashMap<>();
                }
                if (!tmpSvcNameDescMap.containsKey(elementSvcName)) {
                    tmpSvcNameDescMap.put(elementSvcName, elementDescriptor);
                }
                sSvcName_SvcNameDescMap_Map.put(elementSvcName, tmpSvcNameDescMap);
                if (sSvcName_TCodeTNameMap_Map.containsKey(elementSvcName)) {
                    tmpTCodeTNameMap = sSvcName_TCodeTNameMap_Map.get(elementSvcName);
                } else {
                    tmpTCodeTNameMap = new HashMap<>();
                }
                int tCode = getTransactionCode(elementDescriptor, elementTName);
                if (!tmpTCodeTNameMap.containsKey(Integer.valueOf(tCode))) {
                    tmpTCodeTNameMap.put(Integer.valueOf(tCode), elementTName);
                }
                sSvcName_TCodeTNameMap_Map.put(elementSvcName, tmpTCodeTNameMap);
                if (!elementParamPattern.equals("NULL")) {
                    if (sSvcName_TNameParamPatternMap_Map.containsKey(elementSvcName)) {
                        tmpTNameParamPatternMap = sSvcName_TNameParamPatternMap_Map.get(elementSvcName);
                    } else {
                        tmpTNameParamPatternMap = new HashMap<>();
                    }
                    if (!tmpTNameParamPatternMap.containsKey(elementTName)) {
                        tmpTNameParamPatternMap.put(elementTName, elementParamPattern);
                    }
                    tmpTCodeTNameMap2 = tmpTCodeTNameMap;
                    sSvcName_TNameParamPatternMap_Map.put(elementSvcName, tmpTNameParamPatternMap);
                } else {
                    tmpTCodeTNameMap2 = tmpTCodeTNameMap;
                    tmpTNameParamPatternMap = tmpTNameParamPatternMap;
                }
                if (sSvcName_TNameBidMap_Map.containsKey(elementSvcName)) {
                    tmpTNameBidMap = sSvcName_TNameBidMap_Map.get(elementSvcName);
                } else {
                    tmpTNameBidMap = new HashMap<>();
                }
                if (!elementParamPattern.equals("NULL")) {
                    if (tmpTNameBidMap.containsKey(elementTName)) {
                        paramBidMap = (Map) tmpTNameBidMap.get(elementTName);
                    } else {
                        paramBidMap = new HashMap<>();
                    }
                    paramBidMap.put(elementParamPattern, elementBid);
                    tmpTNameBidMap.put(elementTName, paramBidMap);
                } else if (!tmpTNameBidMap.containsKey(elementTName)) {
                    tmpTNameBidMap.put(elementTName, elementBid);
                }
                sSvcName_TNameBidMap_Map.put(elementSvcName, tmpTNameBidMap);
                i++;
                c = 0;
            }
        } catch (Exception e) {
            AntivirusLog.e(TAG, "initStatic error : " + e.getMessage());
        }
    }

    private void initInObj() {
        try {
            this.mTNameParamPatternMap = sSvcName_TNameParamPatternMap_Map.get(this.mReplacedServiceName);
            this.mTCodeTNameMap = sSvcName_TCodeTNameMap_Map.get(this.mReplacedServiceName);
            this.mTNameBidMap = sSvcName_TNameBidMap_Map.get(this.mReplacedServiceName);
            this.mSvcNameDescMap = sSvcName_SvcNameDescMap_Map.get(this.mReplacedServiceName);
        } catch (Exception e) {
            AntivirusLog.e(TAG, "initInObj error : " + e.getMessage());
        }
    }

    private void debugPrintMapKV(Map debugMap) {
        if (debugMap != null) {
            for (Object itemEntry : debugMap.entrySet()) {
                Object key = ((Map.Entry) itemEntry).getKey();
                Object value = ((Map.Entry) itemEntry).getValue();
                if (value instanceof Map) {
                    AntivirusLog.d(TAG, "key " + key + " map:");
                    debugPrintMapKV((Map) value);
                } else {
                    AntivirusLog.d(TAG, key + " " + value);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        return r5;
     */
    public static synchronized IBinder getOrCreateFakeBinder(IBinder origBinder, String svcName) {
        synchronized (BinderStubClient.class) {
            if (BinderStubClient.class.isInstance(origBinder)) {
                return origBinder;
            }
            try {
                if (sSvc2FakeBinderMap.containsKey(svcName)) {
                    sOriBinder2FakeBinderMap = sSvc2FakeBinderMap.get(svcName);
                    if (sOriBinder2FakeBinderMap != null) {
                        if (sOriBinder2FakeBinderMap.containsKey(origBinder)) {
                            IBinder t = sOriBinder2FakeBinderMap.get(origBinder);
                            if (t != null) {
                                return t;
                            }
                        } else {
                            BinderStubClient tmpBinder = new BinderStubClient(origBinder, svcName);
                            sOriBinder2FakeBinderMap.put(origBinder, tmpBinder);
                            sSvc2FakeBinderMap.replace(svcName, sOriBinder2FakeBinderMap);
                            return tmpBinder;
                        }
                    }
                } else {
                    BinderStubClient tmpBinder2 = new BinderStubClient(origBinder, svcName);
                    Map<IBinder, BinderStubClient> tmpFakeBinderMap = new ConcurrentHashMap<>();
                    tmpFakeBinderMap.put(origBinder, tmpBinder2);
                    sSvc2FakeBinderMap.put(svcName, tmpFakeBinderMap);
                    return tmpBinder2;
                }
            } catch (Exception e) {
                AntivirusLog.e(TAG, "getOrCreateFakeBinder error : " + e.getMessage());
                return origBinder;
            }
        }
    }

    private void checkAndSendInvocation(String svcName, int transactionCode, Parcel parcel) {
        try {
            if (this.mTCodeTNameMap.containsKey(Integer.valueOf(transactionCode))) {
                BehaviorBidSender.getInstance().pushId(convertOrigInvocationToBid(svcName, this.mTCodeTNameMap.get(Integer.valueOf(transactionCode)), parcel));
            }
        } catch (Exception e) {
            AntivirusLog.e(TAG, e.getMessage());
        }
    }

    public static void checkAndSendReceiverInvocation(Intent intent, boolean dy) {
        String act;
        if (checkNeedReplace(APPLICATION_THREAD_SERVICE_NAME) && (act = intent.getAction()) != null) {
            int bid = 0;
            char c = 65535;
            switch (act.hashCode()) {
                case -2128145023:
                    if (act.equals("android.intent.action.SCREEN_OFF")) {
                        c = 5;
                        break;
                    }
                    break;
                case -1454123155:
                    if (act.equals("android.intent.action.SCREEN_ON")) {
                        c = 6;
                        break;
                    }
                    break;
                case -1173745501:
                    if (act.equals("android.intent.action.CALL")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1173708363:
                    if (act.equals("android.intent.action.DIAL")) {
                        c = 2;
                        break;
                    }
                    break;
                case 798292259:
                    if (act.equals("android.intent.action.BOOT_COMPLETED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1901012141:
                    if (act.equals("android.intent.action.NEW_OUTGOING_CALL")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1948416196:
                    if (act.equals("android.intent.action.CREATE_SHORTCUT")) {
                        c = 3;
                        break;
                    }
                    break;
                case 2142067319:
                    if (act.equals("android.intent.action.DATA_SMS_RECEIVED")) {
                        c = 7;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    bid = dy ? 9438208 : 9437184;
                    break;
                case 1:
                    bid = dy ? 9438209 : 9437185;
                    break;
                case 2:
                    bid = dy ? 9438210 : 9437186;
                    break;
                case 3:
                    bid = dy ? 9438211 : 9437187;
                    break;
                case 4:
                    bid = dy ? 9438212 : 9437188;
                    break;
                case AlgorithmBinderCode.BIND_EVENT_HANDLE /*{ENCODED_INT: 5}*/:
                    bid = dy ? 9438213 : 9437189;
                    break;
                case 6:
                    bid = dy ? 9438214 : 9437190;
                    break;
                case 7:
                    bid = dy ? 9438215 : 9437191;
                    break;
            }
            sendInvocation(bid);
        }
    }

    public static void sendInvocation(int bid) {
        BehaviorBidSender.getInstance().pushId(bid);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int convertOrigInvocationToBid(String svcName, String transactionName, Parcel parcel) {
        char c;
        String pimpPkg;
        String tmpAction;
        String pimpPkg2;
        String str = svcName;
        if (!this.mTNameBidMap.containsKey(transactionName)) {
            return 0;
        }
        Map<String, String> map = this.mTNameParamPatternMap;
        if (map == null || !map.containsKey(transactionName)) {
            return Integer.parseInt((String) this.mTNameBidMap.get(transactionName));
        }
        String paramPattern = this.mTNameParamPatternMap.get(transactionName);
        String str2 = ";";
        String[] paramPatterns = paramPattern.split(str2);
        List<String> keyList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        for (String param : paramPatterns) {
            String[] params = param.split("\\|");
            keyList.add(params[0]);
            valueList.add(params[1]);
        }
        int position = parcel.dataPosition();
        parcel.setDataPosition(0);
        if (this.mSvcNameDescMap.containsKey(str)) {
            parcel.enforceInterface(this.mSvcNameDescMap.get(str));
        } else {
            AntivirusLog.d(TAG, str + ": get desc failed");
        }
        List<String> paramsList = new ArrayList<>();
        List<String> bufList = new ArrayList<>();
        int i = 0;
        while (i < keyList.size()) {
            String str3 = keyList.get(i);
            switch (str3.hashCode()) {
                case 98:
                    if (str3.equals("b")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 99:
                    if (str3.equals("c")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 100:
                    if (str3.equals("d")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 102:
                    if (str3.equals("f")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 105:
                    if (str3.equals("i")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 108:
                    if (str3.equals("l")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 115:
                    if (str3.equals("s")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 3138:
                    if (str3.equals("bd")) {
                        c = 13;
                        break;
                    }
                    c = 65535;
                    break;
                case 3179:
                    if (str3.equals("cn")) {
                        c = 9;
                        break;
                    }
                    c = 65535;
                    break;
                case 3460:
                    if (str3.equals("lp")) {
                        c = 8;
                        break;
                    }
                    c = 65535;
                    break;
                case 96396:
                    if (str3.equals("acn")) {
                        c = 12;
                        break;
                    }
                    c = 65535;
                    break;
                case 96402:
                    if (str3.equals("act")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case 113663:
                    if (str3.equals("sbn")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 116076:
                    if (str3.equals("uri")) {
                        c = 10;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    pimpPkg = str2;
                    int intVal = parcel.readInt();
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("i|" + intVal);
                        break;
                    } else {
                        paramsList.add("i|NULL");
                        break;
                    }
                case 1:
                    pimpPkg = str2;
                    String strVal = parcel.readString();
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("s|" + strVal);
                        break;
                    } else {
                        paramsList.add("s|NULL");
                        break;
                    }
                case 2:
                    pimpPkg = str2;
                    long longVal = parcel.readLong();
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("l|" + longVal);
                        break;
                    } else {
                        paramsList.add("l|NULL");
                        break;
                    }
                case 3:
                    pimpPkg = str2;
                    float floatVal = parcel.readFloat();
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("f|" + floatVal);
                        break;
                    } else {
                        paramsList.add("f|NULL");
                        break;
                    }
                case 4:
                    pimpPkg = str2;
                    double doubleVal = parcel.readDouble();
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("d|" + doubleVal);
                        break;
                    } else {
                        paramsList.add("d|NULL");
                        break;
                    }
                case AlgorithmBinderCode.BIND_EVENT_HANDLE /*{ENCODED_INT: 5}*/:
                    pimpPkg = str2;
                    break;
                case 6:
                    pimpPkg = str2;
                    boolean boolVal = parcel.readInt() != 0;
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("b|" + boolVal);
                        break;
                    } else {
                        paramsList.add("b|NULL");
                        break;
                    }
                case 7:
                    pimpPkg = str2;
                    IBinder iBinder = parcel.readStrongBinder();
                    if (!"NULL".equals(valueList.get(i))) {
                        paramsList.add("sbn|" + iBinder);
                        break;
                    } else {
                        paramsList.add("sbn|NULL");
                        break;
                    }
                case ColorUxIconConstants.IconTheme.THEME_MATERIAL_RADIUS_PX:
                    pimpPkg = str2;
                    if (parcel.readInt() == 0) {
                        paramsList.add("lp|NULL");
                        break;
                    } else {
                        WindowManager.LayoutParams lparams = (WindowManager.LayoutParams) WindowManager.LayoutParams.CREATOR.createFromParcel(parcel);
                        if (!"NULL".equals(valueList.get(i))) {
                            if (lparams == null) {
                                break;
                            } else {
                                paramsList.add("lp|" + lparams.width + "*" + lparams.height + "*" + lparams.type);
                                break;
                            }
                        } else {
                            paramsList.add("lp|NULL");
                            break;
                        }
                    }
                case 9:
                    pimpPkg = str2;
                    if (parcel.readInt() == 0) {
                        paramsList.add("cn|NULL");
                        break;
                    } else {
                        ComponentName.CREATOR.createFromParcel(parcel);
                        if (!"NULL".equals(valueList.get(i))) {
                            AntivirusLog.e(TAG, "Please check cn parameter rule");
                            break;
                        } else {
                            paramsList.add("cn|NULL");
                            break;
                        }
                    }
                case 10:
                    pimpPkg = str2;
                    if (!str.equals("contentprovider") && parcel.readInt() == 0) {
                        paramsList.add("uri|NULL");
                        break;
                    } else {
                        Uri uri = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        String tmpValue = valueList.get(i);
                        String tmpUriString = uri.toString();
                        String tmpUriMatchStr = "NULL";
                        try {
                            try {
                                tmpUriMatchStr = tmpUriString.substring(0, tmpUriString.indexOf(ColorUxIconConstants.IconLoader.FILE_SEPARATOR, "content://".length()) + 1);
                            } catch (Exception e) {
                            }
                        } catch (Exception e2) {
                        }
                        if (!"NULL".equals(tmpValue)) {
                            paramsList.add("uri|" + tmpUriMatchStr);
                            break;
                        } else {
                            paramsList.add("uri|NULL");
                            break;
                        }
                    }
                case 11:
                    pimpPkg = str2;
                    if (parcel.readInt() == 0) {
                        paramsList.add("act|NULL");
                        break;
                    } else {
                        Intent intent = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        if (!"NULL".equals(valueList.get(i))) {
                            if (intent == null) {
                                break;
                            } else {
                                paramsList.add("act|" + intent.getAction());
                                break;
                            }
                        } else {
                            paramsList.add("act|NULL");
                            break;
                        }
                    }
                case ColorUxIconConstants.IconTheme.ICON_RADIUS_BIT_LENGTH:
                    if (parcel.readInt() == 0) {
                        pimpPkg = str2;
                        paramsList.add("acn|NULL");
                        break;
                    } else {
                        Intent intentAcn = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        String tmpAction2 = intentAcn.getAction();
                        if (TextUtils.isEmpty(tmpAction2)) {
                            tmpAction = "~";
                        } else {
                            tmpAction = tmpAction2;
                        }
                        ComponentName tmpCn = intentAcn.getComponent();
                        if (tmpCn == null) {
                            pimpPkg2 = "~";
                        } else if (TextUtils.isEmpty(tmpCn.getPackageName())) {
                            pimpPkg2 = "~";
                        } else {
                            pimpPkg2 = tmpCn.getPackageName();
                        }
                        pimpPkg = str2;
                        if (!"NULL".equals(valueList.get(i))) {
                            paramsList.add("acn|" + tmpAction + "*" + pimpPkg2);
                            bufList.add(tmpAction);
                            bufList.add(pimpPkg2);
                            break;
                        } else {
                            paramsList.add("acn|NULL");
                            break;
                        }
                    }
                case 13:
                    if (parcel.readInt() == 0) {
                        paramsList.add("bd|NULL");
                        pimpPkg = str2;
                        break;
                    } else {
                        Bundle bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        if (!"NULL".equals(valueList.get(i))) {
                            paramsList.add("bd|" + bundle);
                            pimpPkg = str2;
                            break;
                        } else {
                            paramsList.add("bd|NULL");
                            pimpPkg = str2;
                            break;
                        }
                    }
                default:
                    pimpPkg = str2;
                    AntivirusLog.e(TAG, "Unknow param pattern: " + keyList.get(i));
                    break;
            }
            i++;
            str = svcName;
            paramPattern = paramPattern;
            paramPatterns = paramPatterns;
            str2 = pimpPkg;
        }
        parcel.setDataPosition(position);
        String paramKey = String.join(str2, paramsList);
        Map<String, String> bidObject = (Map) this.mTNameBidMap.get(transactionName);
        if (bidObject.containsKey(paramKey)) {
            return Integer.parseInt(bidObject.get(paramKey));
        }
        if (transactionName.equals("TRANSACTION_startActivity")) {
            String tmpAction3 = bufList.get(0);
            bufList.get(1);
            paramsList.set(2, "acn|" + tmpAction3 + "*~");
            String paramKey2 = String.join(str2, paramsList);
            if (bidObject.containsKey(paramKey2)) {
                return Integer.parseInt(bidObject.get(paramKey2));
            }
            paramsList.set(2, "acn|~*~");
            String paramKey3 = String.join(str2, paramsList);
            if (bidObject.containsKey(paramKey3)) {
                return Integer.parseInt(bidObject.get(paramKey3));
            }
            return 0;
        } else if (transactionName.equals("TRANSACTION_broadcastIntent")) {
            paramsList.set(1, "act|~");
            String paramKey4 = String.join(str2, paramsList);
            if (bidObject.containsKey(paramKey4)) {
                return Integer.parseInt(bidObject.get(paramKey4));
            }
            return 0;
        } else if (!transactionName.equals("TRANSACTION_addToDisplay")) {
            return 0;
        } else {
            String lpParam = paramsList.get(2);
            String type = "";
            if (lpParam.contains("lp")) {
                type = lpParam.split("\\*")[2];
            }
            paramsList.set(2, "lp|~*~*" + type);
            String paramKey5 = String.join(str2, paramsList);
            if (bidObject.containsKey(paramKey5)) {
                return Integer.parseInt(bidObject.get(paramKey5));
            }
            return 0;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0036, code lost:
        if (r1 != null) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0038, code lost:
        r1.setAccessible(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
        if (r1 == null) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005d, code lost:
        return r3;
     */
    private static int getTransactionCode(String interfaceDescriptor, String transactionName) {
        String clazzName = "android.content.IContentProvider";
        Field field = null;
        int tcode = 0;
        try {
            if (!clazzName.equals(interfaceDescriptor)) {
                clazzName = interfaceDescriptor + "$Stub";
            }
            Class clazz = Class.forName(clazzName);
            if (clazz != null) {
                field = clazz.getDeclaredField(transactionName);
                field.setAccessible(true);
                tcode = field.getInt(clazz);
            }
        } catch (Exception e) {
            AntivirusLog.e(TAG, "Fail to get transaction code: " + e.getMessage());
            tcode = 0;
        } catch (Throwable th) {
            if (field != null) {
                field.setAccessible(false);
            }
            throw th;
        }
    }

    public static boolean checkNeedReplace(String name) {
        return checkNeedReplace(name, Process.myUid());
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0020 A[Catch:{ all -> 0x001b }] */
    public static synchronized boolean checkNeedReplace(String name, int callingUid) {
        boolean res;
        synchronized (BinderStubClient.class) {
            if (callingUid > 10000) {
                try {
                    if (sReplaceServiceNameSet.contains(name) && BehaviorBidSender.getInstance().checkUidInMonitorSet(callingUid)) {
                        res = true;
                        if (res) {
                            initStatic();
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            res = false;
            if (res) {
            }
        }
        return res;
    }

    @Override // android.os.IBinder
    public String getInterfaceDescriptor() throws RemoteException {
        IBinder iBinder = this.mOrigBinder;
        if (iBinder != null) {
            return iBinder.getInterfaceDescriptor();
        }
        return "BinderStubClientDesc";
    }

    public boolean pingBinder() {
        try {
            if (this.mOrigBinder != null) {
                return this.mOrigBinder.pingBinder();
            }
            AntivirusLog.e(TAG, "pingBinder mOrigBinder is null");
            return false;
        } catch (Exception e) {
            AntivirusLog.e(TAG, "pingBinder: " + e.getMessage());
            return false;
        }
    }

    public boolean isBinderAlive() {
        try {
            if (this.mOrigBinder != null) {
                return this.mOrigBinder.isBinderAlive();
            }
            AntivirusLog.e(TAG, "isBinderAlive mOrigBinder is null");
            return false;
        } catch (Exception e) {
            AntivirusLog.e(TAG, "isBinderAlive: " + e.getMessage());
            return false;
        }
    }

    public IInterface queryLocalInterface(String s) {
        try {
            if (this.mOrigBinder != null) {
                return this.mOrigBinder.queryLocalInterface(s);
            }
            AntivirusLog.e(TAG, "queryLocalInterface mOrigBinder is null");
            return null;
        } catch (Exception e) {
            AntivirusLog.e(TAG, "queryLocalInterface: " + e.getMessage());
            return null;
        }
    }

    @Override // android.os.IBinder
    public void dump(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {
        IBinder iBinder = this.mOrigBinder;
        if (iBinder != null) {
            iBinder.dump(fileDescriptor, strings);
        } else {
            AntivirusLog.e(TAG, "dump mOrigBinder is null");
        }
    }

    @Override // android.os.IBinder
    public void dumpAsync(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {
        IBinder iBinder = this.mOrigBinder;
        if (iBinder != null) {
            iBinder.dumpAsync(fileDescriptor, strings);
        } else {
            AntivirusLog.e(TAG, "dumpAsync mOrigBinder is null");
        }
    }

    @Override // android.os.IBinder
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mOrigBinder != null) {
            checkAndSendInvocation(this.mReplacedServiceName, code, data);
            return this.mOrigBinder.transact(code, data, reply, flags);
        }
        AntivirusLog.e(TAG, "transact mOrigBinder is null!");
        return false;
    }

    private class BinderStubDeathRecipient implements IBinder.DeathRecipient {
        private int mPid = 0;
        private int mUid = 0;
        private IBinder.DeathRecipient origDeathRecipient = null;

        public BinderStubDeathRecipient(IBinder.DeathRecipient rec, int uid, int pid) {
            this.origDeathRecipient = rec;
            this.mUid = uid;
            this.mPid = pid;
        }

        public void binderDied() {
            this.origDeathRecipient.binderDied();
            BehaviorBidSender.getInstance().notifyProcessDied(this.mUid, this.mPid);
        }
    }

    @Override // android.os.IBinder
    public void linkToDeath(IBinder.DeathRecipient deathRecipient, int i) throws RemoteException {
        if (this.mOrigBinder == null) {
            AntivirusLog.e(TAG, "linkToDeath mOrigBinder is null");
        } else if (!APPLICATION_THREAD_SERVICE_NAME.equals(this.mReplacedServiceName) || this.mAppThrDeathRecipient != null) {
            this.mOrigBinder.linkToDeath(deathRecipient, i);
        } else {
            this.mAppThrDeathRecipient = new BinderStubDeathRecipient(deathRecipient, Process.myUid(), Process.myPid());
            this.mOrigBinder.linkToDeath(this.mAppThrDeathRecipient, i);
        }
    }

    public boolean unlinkToDeath(IBinder.DeathRecipient deathRecipient, int i) {
        IBinder iBinder = this.mOrigBinder;
        if (iBinder != null) {
            return iBinder.unlinkToDeath(deathRecipient, i);
        }
        AntivirusLog.e(TAG, "unlinkToDeath mOrigBinder is null");
        return false;
    }

    public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback shellCallback, ResultReceiver resultReceiver) throws RemoteException {
        IBinder iBinder = this.mOrigBinder;
        if (iBinder != null) {
            iBinder.shellCommand(in, out, err, args, shellCallback, resultReceiver);
        } else {
            AntivirusLog.e(TAG, "shellCommand mOrigBinder is null");
        }
    }
}

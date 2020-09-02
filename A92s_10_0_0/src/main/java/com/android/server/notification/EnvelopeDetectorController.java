package com.android.server.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.color.util.ColorNavigationBarUtil;

public class EnvelopeDetectorController {
    private static final long CANCEL_DELAY = 1000;
    private static final String CANCEL_ENVELOPE = "com.android.systemui.envelope.cancel_envelope";
    static final boolean DEBUG_OPPO_ENVELOPE = false;
    public static final String ENVELOPE_ASSISTANT_ENABLE = "envelope_assistant_enable";
    private static final int ENVELOPE_ASSISTANT_ENABLED = 1;
    private static final String ENVELOPE_CATEGORY = "envelope_category";
    public static final String ENVELOPE_NOTICE_SOUND_ENABLE = "envelope_notice_sound_enable";
    private static final String ENVELOPE_NOTIFICATION = "com.android.systemui.envelope.envelope_notification";
    private static final String ENVELOPE_RELATED_UID = "envelope_related_uid";
    public static final String ENVELOPE_TAG = "envelope";
    public static final String LOCK_SCREEN_VISIBILITY = "lock_screen_visibility";
    private static final int NONE_ENVELOPE = 0;
    public static final String NOTIFICATION_SOURCE = "notification_source";
    private static final int QQ_ENVELOPE = 2;
    private static final String QQ_ENVELOPE_ID = "[QQ红包]";
    private static final String QQ_PACKAGE = "com.tencent.mobileqq";
    private static final String RELATED_KEY = "related_key";
    private static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    static final String TAG = "EnvelopeDetectorController";
    private static final String TRANSITIVE_ENVELOPE = "transitive_envelope";
    private static final int WECHAT_CANCEL_ID = 4097;
    private static final int WECHAT_ENVELOPE = 1;
    private static final int WECHAT_ENVELOPE_ID = 436207665;
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private ContentObserver mAssistantEnableObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.notification.EnvelopeDetectorController.AnonymousClass1 */

        public void onChange(boolean b) {
            if (EnvelopeDetectorController.this.mContext != null) {
                EnvelopeDetectorController envelopeDetectorController = EnvelopeDetectorController.this;
                int unused = envelopeDetectorController.mEnvelopeAssistantStatus = Settings.Secure.getIntForUser(envelopeDetectorController.mContext.getContentResolver(), EnvelopeDetectorController.ENVELOPE_ASSISTANT_ENABLE, 1, -2);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mEnvelopeAssistantStatus;
    /* access modifiers changed from: private */
    public int mEnvelopeSoundStatus;
    private Handler mHandler;
    private boolean mIsExpVersion = DEBUG_OPPO_ENVELOPE;
    private QqDetector mQqDetector;
    private ContentObserver mSoundEnableObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.notification.EnvelopeDetectorController.AnonymousClass2 */

        public void onChange(boolean b) {
            if (EnvelopeDetectorController.this.mContext != null) {
                EnvelopeDetectorController envelopeDetectorController = EnvelopeDetectorController.this;
                int unused = envelopeDetectorController.mEnvelopeSoundStatus = Settings.Secure.getIntForUser(envelopeDetectorController.mContext.getContentResolver(), EnvelopeDetectorController.ENVELOPE_NOTICE_SOUND_ENABLE, 1, -2);
            }
        }
    };
    private WeChatDetector mWeChatDetector;

    public EnvelopeDetectorController(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mIsExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
        this.mWeChatDetector = new WeChatDetector();
        this.mQqDetector = new QqDetector();
        initStatus();
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(ENVELOPE_ASSISTANT_ENABLE), true, this.mAssistantEnableObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(ENVELOPE_NOTICE_SOUND_ENABLE), true, this.mSoundEnableObserver, -1);
    }

    public void detectEnvelope(Context context, Notification notification, String pkg, String relatedKey, int uid, int lockScreenVisibility) {
        if (this.mIsExpVersion || this.mEnvelopeAssistantStatus != 1) {
            return;
        }
        if (notification == null) {
            Log.w(TAG, "notification is null");
        } else if (notification.extras.getCharSequence("android.text") == null) {
            Log.w(TAG, "notification's content is null");
        } else if ("com.tencent.mm".equals(pkg)) {
            this.mHandler.post(new Runnable(context, notification, relatedKey, uid, lockScreenVisibility) {
                /* class com.android.server.notification.$$Lambda$EnvelopeDetectorController$bFagjyY0Z9PlVO9XTb9HdS7n05M */
                private final /* synthetic */ Context f$1;
                private final /* synthetic */ Notification f$2;
                private final /* synthetic */ String f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ int f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run() {
                    EnvelopeDetectorController.this.lambda$detectEnvelope$0$EnvelopeDetectorController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
        } else if (QQ_PACKAGE.equals(pkg)) {
            this.mHandler.post(new Runnable(context, notification, relatedKey, uid, lockScreenVisibility) {
                /* class com.android.server.notification.$$Lambda$EnvelopeDetectorController$mar0B6dqtKbyk7_4kzgeejs1vGE */
                private final /* synthetic */ Context f$1;
                private final /* synthetic */ Notification f$2;
                private final /* synthetic */ String f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ int f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run() {
                    EnvelopeDetectorController.this.lambda$detectEnvelope$1$EnvelopeDetectorController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
        }
    }

    public /* synthetic */ void lambda$detectEnvelope$0$EnvelopeDetectorController(Context context, Notification notification, String relatedKey, int uid, int lockScreenVisibility) {
        this.mWeChatDetector.detectEnvelope(context, notification, relatedKey, uid, lockScreenVisibility);
    }

    public /* synthetic */ void lambda$detectEnvelope$1$EnvelopeDetectorController(Context context, Notification notification, String relatedKey, int uid, int lockScreenVisibility) {
        this.mQqDetector.detectEnvelope(context, notification, relatedKey, uid, lockScreenVisibility);
    }

    public void detectCancelAction(Context context, int id, String pkg, int uid) {
        if (this.mIsExpVersion || this.mEnvelopeAssistantStatus != 1 || pkg == null) {
            return;
        }
        if (pkg.equals("com.tencent.mm")) {
            this.mHandler.postDelayed(new Runnable(context, id, uid) {
                /* class com.android.server.notification.$$Lambda$EnvelopeDetectorController$vjZ_B2deSSm6yxGStInBTtC14Y */
                private final /* synthetic */ Context f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    EnvelopeDetectorController.this.lambda$detectCancelAction$2$EnvelopeDetectorController(this.f$1, this.f$2, this.f$3);
                }
            }, CANCEL_DELAY);
        } else if (pkg.equals(QQ_PACKAGE)) {
            this.mQqDetector.detectCancelAction(context, id, uid);
        }
    }

    public /* synthetic */ void lambda$detectCancelAction$2$EnvelopeDetectorController(Context context, int id, int uid) {
        this.mWeChatDetector.detectCancelAction(context, id, uid);
    }

    /* access modifiers changed from: private */
    public void setTag(Notification notification) {
        notification.extras.putBoolean(ENVELOPE_TAG, true);
    }

    public boolean notificationFromEnvelopeAssistant(Notification notification) {
        if (this.mIsExpVersion || notification == null || notification.extras == null || notification.extras.getString(NOTIFICATION_SOURCE) == null) {
            return DEBUG_OPPO_ENVELOPE;
        }
        return true;
    }

    public boolean interceptBuzzBeepBlink(Notification notification) {
        if (this.mIsExpVersion || notification.extras.getString(NOTIFICATION_SOURCE) == null || this.mEnvelopeAssistantStatus != 1 || this.mEnvelopeSoundStatus != 0) {
            return DEBUG_OPPO_ENVELOPE;
        }
        Log.w(TAG, "interceptBuzzBeepBlink because the envelope sound is unable");
        return true;
    }

    public boolean useEnvelopeSound(Notification notification) {
        if (this.mIsExpVersion || notification.extras.getString(NOTIFICATION_SOURCE) == null) {
            return DEBUG_OPPO_ENVELOPE;
        }
        Log.d(TAG, "Envelope notification use envelope sound");
        return true;
    }

    abstract class Detector {
        /* access modifiers changed from: package-private */
        public abstract void detectCancelAction(Context context, int i, int i2);

        /* access modifiers changed from: package-private */
        public abstract void detectEnvelope(Context context, Notification notification, String str, int i, int i2);

        Detector() {
        }
    }

    private class WeChatDetector extends Detector {
        private WeChatDetector() {
            super();
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.notification.EnvelopeDetectorController.Detector
        public void detectEnvelope(Context context, Notification notification, String relatedKey, int uid, int lockScreenVisibility) {
            PendingIntent intent = notification.contentIntent;
            if (intent == null) {
                Log.d(EnvelopeDetectorController.TAG, "contentIntent is null");
                return;
            }
            String wechatEnvelopeString = ColorNavigationBarUtil.getInstance().getEnvelopeInfo("envelope_filter_value");
            if (wechatEnvelopeString == null || wechatEnvelopeString.isEmpty()) {
                Log.d(EnvelopeDetectorController.TAG, "wechatEnvelopeString is null");
                return;
            }
            if (intent.getIntent().getIntExtra(ColorNavigationBarUtil.getInstance().getEnvelopeInfo("envelope_filter_field"), -1) == Integer.parseInt(wechatEnvelopeString)) {
                EnvelopeDetectorController.this.setTag(notification);
                notifyEnvelopeAssistant(context, notification, relatedKey, uid, lockScreenVisibility);
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyEnvelopeAssistant(Context context, Notification notification, String relatedKey, int uid, int lockScreenVisibility) {
            Intent intent = new Intent();
            intent.setAction(EnvelopeDetectorController.ENVELOPE_NOTIFICATION);
            intent.setPackage("com.android.systemui");
            intent.putExtra(EnvelopeDetectorController.ENVELOPE_CATEGORY, 1);
            intent.putExtra(EnvelopeDetectorController.RELATED_KEY, relatedKey);
            intent.putExtra(EnvelopeDetectorController.ENVELOPE_RELATED_UID, uid);
            intent.putExtra(EnvelopeDetectorController.LOCK_SCREEN_VISIBILITY, lockScreenVisibility);
            intent.putExtra(EnvelopeDetectorController.TRANSITIVE_ENVELOPE, notification.clone());
            context.sendBroadcast(intent);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.notification.EnvelopeDetectorController.Detector
        public void detectCancelAction(Context context, int id, int uid) {
            if (id == EnvelopeDetectorController.WECHAT_CANCEL_ID) {
                cancelAllEnvelope(context, uid);
            }
        }

        /* access modifiers changed from: package-private */
        public void cancelAllEnvelope(Context context, int uid) {
            Intent intent = new Intent();
            intent.setAction(EnvelopeDetectorController.CANCEL_ENVELOPE);
            intent.setPackage("com.android.systemui");
            intent.putExtra(EnvelopeDetectorController.ENVELOPE_CATEGORY, 1);
            intent.putExtra(EnvelopeDetectorController.ENVELOPE_RELATED_UID, uid);
            context.sendBroadcast(intent);
        }
    }

    private class QqDetector extends Detector {
        private QqDetector() {
            super();
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.notification.EnvelopeDetectorController.Detector
        public void detectEnvelope(Context context, Notification notification, String relatedKey, int uid, int lockScreenVisibility) {
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.notification.EnvelopeDetectorController.Detector
        public void detectCancelAction(Context context, int id, int uid) {
        }

        /* access modifiers changed from: package-private */
        public void notifyEnvelopeAssistant(Context context, Notification notification, String relatedKey, int uid, int lockScreenVisibility) {
            Intent intent = new Intent();
            intent.setAction(EnvelopeDetectorController.ENVELOPE_NOTIFICATION);
            intent.setPackage("com.android.systemui");
            intent.putExtra(EnvelopeDetectorController.ENVELOPE_CATEGORY, 2);
            intent.putExtra(EnvelopeDetectorController.TRANSITIVE_ENVELOPE, notification.clone());
            context.sendBroadcast(intent);
        }
    }

    public void initStatus() {
        this.mEnvelopeAssistantStatus = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), ENVELOPE_ASSISTANT_ENABLE, 1, -2);
        this.mEnvelopeSoundStatus = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), ENVELOPE_NOTICE_SOUND_ENABLE, 1, -2);
    }
}

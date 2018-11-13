package android.widget;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.QuickContact;
import android.telecom.PhoneAccount;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.internal.R;

public class QuickContactBadge extends ImageView implements OnClickListener {
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final String[] EMAIL_LOOKUP_PROJECTION = new String[]{"contact_id", ContactsColumns.LOOKUP_KEY};
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;
    private static final String EXTRA_URI_CONTENT = "uri_content";
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final String[] PHONE_LOOKUP_PROJECTION = new String[]{"_id", ContactsColumns.LOOKUP_KEY};
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;
    private static final int TOKEN_EMAIL_LOOKUP = 0;
    private static final int TOKEN_EMAIL_LOOKUP_AND_TRIGGER = 2;
    private static final int TOKEN_PHONE_LOOKUP = 1;
    private static final int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;
    private String mContactEmail;
    private String mContactPhone;
    private Uri mContactUri;
    private Drawable mDefaultAvatar;
    protected String[] mExcludeMimes;
    private Bundle mExtras;
    private Drawable mOverlay;
    private String mPrioritizedMimeType;
    private QueryHandler mQueryHandler;

    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* JADX WARNING: Missing block: B:4:0x000c, code:
            if (r18 == null) goto L_0x0011;
     */
        /* JADX WARNING: Missing block: B:5:0x000e, code:
            r18.close();
     */
        /* JADX WARNING: Missing block: B:6:0x0011, code:
            android.widget.QuickContactBadge.-set0(r15.this$0, r8);
            android.widget.QuickContactBadge.-wrap0(r15.this$0);
     */
        /* JADX WARNING: Missing block: B:7:0x001b, code:
            if (r9 == false) goto L_0x00a5;
     */
        /* JADX WARNING: Missing block: B:9:0x0023, code:
            if (android.widget.QuickContactBadge.-get0(r15.this$0) == null) goto L_0x00a5;
     */
        /* JADX WARNING: Missing block: B:10:0x0025, code:
            android.provider.ContactsContract.QuickContact.showQuickContact(r15.this$0.getContext(), r15.this$0, android.widget.QuickContactBadge.-get0(r15.this$0), r15.this$0.mExcludeMimes, android.widget.QuickContactBadge.-get1(r15.this$0));
     */
        /* JADX WARNING: Missing block: B:15:0x0057, code:
            if (r18 == null) goto L_0x000c;
     */
        /* JADX WARNING: Missing block: B:17:0x005d, code:
            if (r18.moveToFirst() == false) goto L_0x000c;
     */
        /* JADX WARNING: Missing block: B:18:0x005f, code:
            r8 = android.provider.ContactsContract.Contacts.getLookupUri(r18.getLong(0), r18.getString(1));
     */
        /* JADX WARNING: Missing block: B:20:0x0082, code:
            if (r18 == null) goto L_0x000c;
     */
        /* JADX WARNING: Missing block: B:22:0x0088, code:
            if (r18.moveToFirst() == false) goto L_0x000c;
     */
        /* JADX WARNING: Missing block: B:23:0x008a, code:
            r8 = android.provider.ContactsContract.Contacts.getLookupUri(r18.getLong(0), r18.getString(1));
     */
        /* JADX WARNING: Missing block: B:28:0x00a5, code:
            if (r4 == null) goto L_?;
     */
        /* JADX WARNING: Missing block: B:29:0x00a7, code:
            r6 = new android.content.Intent("com.android.contacts.action.SHOW_OR_CREATE_CONTACT", r4);
     */
        /* JADX WARNING: Missing block: B:30:0x00af, code:
            if (r5 == null) goto L_0x00ba;
     */
        /* JADX WARNING: Missing block: B:31:0x00b1, code:
            r5.remove(android.widget.QuickContactBadge.EXTRA_URI_CONTENT);
            r6.putExtras(r5);
     */
        /* JADX WARNING: Missing block: B:32:0x00ba, code:
            r15.this$0.getContext().startActivity(r6);
     */
        /* JADX WARNING: Missing block: B:33:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:34:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:35:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;
            Bundle extras = cookie != null ? (Bundle) cookie : new Bundle();
            switch (token) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    trigger = true;
                    createUri = Uri.fromParts("mailto", extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                    break;
                case 3:
                    trigger = true;
                    try {
                        createUri = Uri.fromParts(PhoneAccount.SCHEME_TEL, extras.getString(QuickContactBadge.EXTRA_URI_CONTENT), null);
                        break;
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
            }
        }
    }

    public QuickContactBadge(Context context) {
        this(context, null);
    }

    public QuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public QuickContactBadge(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mExtras = null;
        this.mExcludeMimes = null;
        TypedArray styledAttributes = this.mContext.obtainStyledAttributes(R.styleable.Theme);
        this.mOverlay = styledAttributes.getDrawable(316);
        styledAttributes.recycle();
        setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            this.mQueryHandler = new QueryHandler(this.mContext.getContentResolver());
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable overlay = this.mOverlay;
        if (overlay != null && overlay.isStateful() && overlay.setState(getDrawableState())) {
            invalidateDrawable(overlay);
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mOverlay != null) {
            this.mOverlay.setHotspot(x, y);
        }
    }

    public void setMode(int size) {
    }

    public void setPrioritizedMimeType(String prioritizedMimeType) {
        this.mPrioritizedMimeType = prioritizedMimeType;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEnabled() && this.mOverlay != null && this.mOverlay.getIntrinsicWidth() != 0 && this.mOverlay.getIntrinsicHeight() != 0) {
            this.mOverlay.setBounds(0, 0, getWidth(), getHeight());
            if (this.mPaddingTop == 0 && this.mPaddingLeft == 0) {
                this.mOverlay.draw(canvas);
            } else {
                int saveCount = canvas.getSaveCount();
                canvas.save();
                canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
                this.mOverlay.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    private boolean isAssigned() {
        return (this.mContactUri == null && this.mContactEmail == null && this.mContactPhone == null) ? false : true;
    }

    public void setImageToDefault() {
        if (this.mDefaultAvatar == null) {
            this.mDefaultAvatar = this.mContext.getDrawable(R.drawable.ic_contact_picture);
        }
        setImageDrawable(this.mDefaultAvatar);
    }

    public void assignContactUri(Uri contactUri) {
        this.mContactUri = contactUri;
        this.mContactEmail = null;
        this.mContactPhone = null;
        onContactUriChanged();
    }

    public void assignContactFromEmail(String emailAddress, boolean lazyLookup) {
        assignContactFromEmail(emailAddress, lazyLookup, null);
    }

    public void assignContactFromEmail(String emailAddress, boolean lazyLookup, Bundle extras) {
        this.mContactEmail = emailAddress;
        this.mExtras = extras;
        if (lazyLookup || this.mQueryHandler == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        this.mQueryHandler.startQuery(0, null, Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
    }

    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        assignContactFromPhone(phoneNumber, lazyLookup, new Bundle());
    }

    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup, Bundle extras) {
        this.mContactPhone = phoneNumber;
        this.mExtras = extras;
        if (lazyLookup || this.mQueryHandler == null) {
            this.mContactUri = null;
            onContactUriChanged();
            return;
        }
        this.mQueryHandler.startQuery(1, null, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
    }

    public void setOverlay(Drawable overlay) {
        this.mOverlay = overlay;
    }

    private void onContactUriChanged() {
        setEnabled(isAssigned());
    }

    public void onClick(View v) {
        Bundle extras = this.mExtras == null ? new Bundle() : this.mExtras;
        if (this.mContactUri != null) {
            QuickContact.showQuickContact(getContext(), (View) this, this.mContactUri, this.mExcludeMimes, this.mPrioritizedMimeType);
        } else if (this.mContactEmail != null && this.mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, this.mContactEmail);
            this.mQueryHandler.startQuery(2, extras, Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(this.mContactEmail)), EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else if (this.mContactPhone != null && this.mQueryHandler != null) {
            extras.putString(EXTRA_URI_CONTENT, this.mContactPhone);
            this.mQueryHandler.startQuery(3, extras, Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, this.mContactPhone), PHONE_LOOKUP_PROJECTION, null, null, null);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return QuickContactBadge.class.getName();
    }

    public void setExcludeMimes(String[] excludeMimes) {
        this.mExcludeMimes = excludeMimes;
    }
}

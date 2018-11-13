package com.qrd.wappush;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Threads;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.SAXException;

public class WapPushHandler implements IWapPushHandler {
    private static final String SPACE = " ";
    public String TAG = "WapPushHandler";
    private String mAction = null;
    private String mContent = null;
    private String mLink = null;
    private long mThreadID = 0;

    public Uri handleWapPush(InputStream inputstream, String mime, Context context, int slotID, String address) throws SAXException, IOException {
        boolean bIsSI = mime.equals("application/vnd.wap.sic");
        boolean bIsSL = mime.equals("application/vnd.wap.slc");
        if (bIsSI || (bIsSL ^ 1) == 0) {
            int pushType = bIsSI ? 1 : 2;
            WapPushParser parser = new WapPushParser();
            parser.parse(inputstream, pushType);
            this.mAction = parser.getAction();
            this.mContent = parser.getContent() == null ? "" : parser.getContent();
            this.mLink = parser.getHyperLink() == null ? "" : parser.getHyperLink();
            Uri pushUri = storeWapPushMessage(context, this.mContent + SPACE + this.mLink, slotID, address);
            if (bIsSL && (this.mLink.isEmpty() ^ 1) != 0) {
                context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(this.mLink)).addFlags(268435456));
            }
            return pushUri;
        }
        throw new SAXException("Error: can not handler unsupported type");
    }

    public long getThreadID() {
        return this.mThreadID;
    }

    private Uri storeWapPushMessage(Context context, String pushContent, int subscription, String address) {
        ContentValues values = new ContentValues();
        values.put("address", address);
        Log.d(this.TAG, "storeWapPushMessage : ADDRESS " + address + ", subscription " + subscription + ", Content " + pushContent);
        values.put("date", new Long(System.currentTimeMillis()));
        values.put("read", Integer.valueOf(0));
        values.put("seen", Integer.valueOf(0));
        values.put("error_code", Integer.valueOf(0));
        values.put("body", pushContent);
        values.put("address", address);
        this.mThreadID = Threads.getOrCreateThreadId(context, address);
        values.put("thread_id", Long.valueOf(this.mThreadID));
        return SqliteWrapper.insert(context, context.getContentResolver(), Inbox.CONTENT_URI, values);
    }
}

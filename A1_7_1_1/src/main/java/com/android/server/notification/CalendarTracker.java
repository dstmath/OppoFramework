package com.android.server.notification;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.service.notification.ZenModeConfig.EventInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class CalendarTracker {
    private static final String[] ATTENDEE_PROJECTION = null;
    private static final String ATTENDEE_SELECTION = "event_id = ? AND attendeeEmail = ?";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_ATTENDEES = false;
    private static final int EVENT_CHECK_LOOKAHEAD = 86400000;
    private static final String INSTANCE_ORDER_BY = "begin ASC";
    private static final String[] INSTANCE_PROJECTION = null;
    private static final String TAG = "ConditionProviders.CT";
    private Callback mCallback;
    private final ContentObserver mObserver;
    private boolean mRegistered;
    private final Context mSystemContext;
    private final Context mUserContext;

    public interface Callback {
        void onChanged();
    }

    public static class CheckEventResult {
        public boolean inEvent;
        public long recheckAt;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.CalendarTracker.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.CalendarTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.CalendarTracker.<clinit>():void");
    }

    public CalendarTracker(Context systemContext, Context userContext) {
        this.mObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange, Uri u) {
                if (CalendarTracker.DEBUG) {
                    Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange + " uri=" + u + " u=" + CalendarTracker.this.mUserContext.getUserId());
                }
                CalendarTracker.this.mCallback.onChanged();
            }

            public void onChange(boolean selfChange) {
                if (CalendarTracker.DEBUG) {
                    Log.d(CalendarTracker.TAG, "onChange selfChange=" + selfChange);
                }
            }
        };
        this.mSystemContext = systemContext;
        this.mUserContext = userContext;
    }

    public void setCallback(Callback callback) {
        if (this.mCallback != callback) {
            this.mCallback = callback;
            setRegistered(this.mCallback != null);
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mCallback=");
        pw.println(this.mCallback);
        pw.print(prefix);
        pw.print("mRegistered=");
        pw.println(this.mRegistered);
        pw.print(prefix);
        pw.print("u=");
        pw.println(this.mUserContext.getUserId());
    }

    private ArraySet<Long> getPrimaryCalendars() {
        long start = System.currentTimeMillis();
        ArraySet<Long> rt = new ArraySet();
        String primary = "\"primary\"";
        String[] projection = new String[2];
        projection[0] = "_id";
        projection[1] = "(account_name=ownerAccount) AS \"primary\"";
        String selection = "\"primary\" = 1";
        Cursor cursor = null;
        try {
            cursor = this.mUserContext.getContentResolver().query(Calendars.CONTENT_URI, projection, "\"primary\" = 1", null, null);
            while (cursor != null && cursor.moveToNext()) {
                rt.add(Long.valueOf(cursor.getLong(0)));
            }
            if (cursor != null) {
                cursor.close();
            }
            if (DEBUG) {
                Log.d(TAG, "getPrimaryCalendars took " + (System.currentTimeMillis() - start));
            }
            return rt;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public CheckEventResult checkEvent(EventInfo filter, long time) {
        Builder uriBuilder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(uriBuilder, time);
        ContentUris.appendId(uriBuilder, 86400000 + time);
        Cursor cursor = this.mUserContext.getContentResolver().query(uriBuilder.build(), INSTANCE_PROJECTION, null, null, INSTANCE_ORDER_BY);
        CheckEventResult result = new CheckEventResult();
        result.recheckAt = 86400000 + time;
        try {
            ArraySet<Long> primaryCalendars = getPrimaryCalendars();
            while (cursor != null && cursor.moveToNext()) {
                boolean meetsCalendar;
                long begin = cursor.getLong(0);
                long end = cursor.getLong(1);
                String title = cursor.getString(2);
                boolean calendarVisible = cursor.getInt(3) == 1;
                int eventId = cursor.getInt(4);
                String name = cursor.getString(5);
                String owner = cursor.getString(6);
                long calendarId = cursor.getLong(7);
                int availability = cursor.getInt(8);
                boolean calendarPrimary = primaryCalendars.contains(Long.valueOf(calendarId));
                if (DEBUG) {
                    String str = TAG;
                    Object[] objArr = new Object[10];
                    objArr[0] = title;
                    objArr[1] = new Date(begin);
                    objArr[2] = new Date(end);
                    objArr[3] = Boolean.valueOf(calendarVisible);
                    objArr[4] = availabilityToString(availability);
                    objArr[5] = Integer.valueOf(eventId);
                    objArr[6] = name;
                    objArr[7] = owner;
                    objArr[8] = Long.valueOf(calendarId);
                    objArr[9] = Boolean.valueOf(calendarPrimary);
                    Log.d(str, String.format("%s %s-%s v=%s a=%s eid=%s n=%s o=%s cid=%s p=%s", objArr));
                }
                boolean meetsTime = time >= begin && time < end;
                if (!calendarVisible || !calendarPrimary) {
                    meetsCalendar = false;
                } else if (filter.calendar == null || Objects.equals(filter.calendar, owner)) {
                    meetsCalendar = true;
                } else {
                    meetsCalendar = Objects.equals(filter.calendar, name);
                }
                boolean meetsAvailability = availability != 1;
                if (meetsCalendar && meetsAvailability) {
                    if (DEBUG) {
                        Log.d(TAG, "  MEETS CALENDAR & AVAILABILITY");
                    }
                    if (meetsAttendee(filter, eventId, owner)) {
                        if (DEBUG) {
                            Log.d(TAG, "    MEETS ATTENDEE");
                        }
                        if (meetsTime) {
                            if (DEBUG) {
                                Log.d(TAG, "      MEETS TIME");
                            }
                            result.inEvent = true;
                        }
                        if (begin > time && begin < result.recheckAt) {
                            result.recheckAt = begin;
                        } else if (end > time) {
                            if (end < result.recheckAt) {
                                result.recheckAt = end;
                            }
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return result;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean meetsAttendee(EventInfo filter, int eventId, String email) {
        long start = System.currentTimeMillis();
        String selection = ATTENDEE_SELECTION;
        String[] selectionArgs = new String[2];
        selectionArgs[0] = Integer.toString(eventId);
        selectionArgs[1] = email;
        Cursor cursor = this.mUserContext.getContentResolver().query(Attendees.CONTENT_URI, ATTENDEE_PROJECTION, selection, selectionArgs, null);
        try {
            if (cursor.getCount() == 0) {
                if (DEBUG) {
                    Log.d(TAG, "No attendees found");
                }
                cursor.close();
                if (DEBUG) {
                    Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
                }
                return true;
            }
            boolean rt = false;
            while (cursor.moveToNext()) {
                long rowEventId = cursor.getLong(0);
                String rowEmail = cursor.getString(1);
                int status = cursor.getInt(2);
                boolean meetsReply = meetsReply(filter.reply, status);
                if (DEBUG) {
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append(IElsaManager.EMPTY_PACKAGE);
                    Object[] objArr = new Object[2];
                    objArr[0] = attendeeStatusToString(status);
                    objArr[1] = Boolean.valueOf(meetsReply);
                    Log.d(str, append.append(String.format("status=%s, meetsReply=%s", objArr)).toString());
                }
                boolean eventMeets = (rowEventId == ((long) eventId) && Objects.equals(rowEmail, email)) ? meetsReply : false;
                rt |= eventMeets;
            }
            cursor.close();
            if (DEBUG) {
                Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
            }
            return rt;
        } catch (Throwable th) {
            cursor.close();
            if (DEBUG) {
                Log.d(TAG, "meetsAttendee took " + (System.currentTimeMillis() - start));
            }
        }
    }

    private void setRegistered(boolean registered) {
        if (this.mRegistered != registered) {
            ContentResolver cr = this.mSystemContext.getContentResolver();
            int userId = this.mUserContext.getUserId();
            if (this.mRegistered) {
                if (DEBUG) {
                    Log.d(TAG, "unregister content observer u=" + userId);
                }
                cr.unregisterContentObserver(this.mObserver);
            }
            this.mRegistered = registered;
            if (DEBUG) {
                Log.d(TAG, "mRegistered = " + registered + " u=" + userId);
            }
            if (this.mRegistered) {
                if (DEBUG) {
                    Log.d(TAG, "register content observer u=" + userId);
                }
                cr.registerContentObserver(Instances.CONTENT_URI, true, this.mObserver, userId);
                cr.registerContentObserver(Events.CONTENT_URI, true, this.mObserver, userId);
                cr.registerContentObserver(Calendars.CONTENT_URI, true, this.mObserver, userId);
            }
        }
    }

    private static String attendeeStatusToString(int status) {
        switch (status) {
            case 0:
                return "ATTENDEE_STATUS_NONE";
            case 1:
                return "ATTENDEE_STATUS_ACCEPTED";
            case 2:
                return "ATTENDEE_STATUS_DECLINED";
            case 3:
                return "ATTENDEE_STATUS_INVITED";
            case 4:
                return "ATTENDEE_STATUS_TENTATIVE";
            default:
                return "ATTENDEE_STATUS_UNKNOWN_" + status;
        }
    }

    private static String availabilityToString(int availability) {
        switch (availability) {
            case 0:
                return "AVAILABILITY_BUSY";
            case 1:
                return "AVAILABILITY_FREE";
            case 2:
                return "AVAILABILITY_TENTATIVE";
            default:
                return "AVAILABILITY_UNKNOWN_" + availability;
        }
    }

    private static boolean meetsReply(int reply, int attendeeStatus) {
        boolean z = true;
        switch (reply) {
            case 0:
                if (attendeeStatus == 2) {
                    z = false;
                }
                return z;
            case 1:
                if (!(attendeeStatus == 1 || attendeeStatus == 4)) {
                    z = false;
                }
                return z;
            case 2:
                if (attendeeStatus != 1) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }
}

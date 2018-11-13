package android.provider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorEntityIterator;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.RemoteException;
import android.provider.SyncStateContract.Columns;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class CalendarContract {
    public static final String ACCOUNT_TYPE_LOCAL = "LOCAL";
    public static final String ACTION_EVENT_REMINDER = "android.intent.action.EVENT_REMINDER";
    public static final String ACTION_HANDLE_CUSTOM_EVENT = "android.provider.calendar.action.HANDLE_CUSTOM_EVENT";
    public static final String AUTHORITY = "com.android.calendar";
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";
    public static final Uri CONTENT_URI = null;
    public static final String EXTRA_CUSTOM_APP_URI = "customAppUri";
    public static final String EXTRA_EVENT_ALL_DAY = "allDay";
    public static final String EXTRA_EVENT_BEGIN_TIME = "beginTime";
    public static final String EXTRA_EVENT_END_TIME = "endTime";
    private static final String TAG = "Calendar";

    protected interface AttendeesColumns {
        public static final String ATTENDEE_EMAIL = "attendeeEmail";
        public static final String ATTENDEE_IDENTITY = "attendeeIdentity";
        public static final String ATTENDEE_ID_NAMESPACE = "attendeeIdNamespace";
        public static final String ATTENDEE_NAME = "attendeeName";
        public static final String ATTENDEE_RELATIONSHIP = "attendeeRelationship";
        public static final String ATTENDEE_STATUS = "attendeeStatus";
        public static final int ATTENDEE_STATUS_ACCEPTED = 1;
        public static final int ATTENDEE_STATUS_DECLINED = 2;
        public static final int ATTENDEE_STATUS_INVITED = 3;
        public static final int ATTENDEE_STATUS_NONE = 0;
        public static final int ATTENDEE_STATUS_TENTATIVE = 4;
        public static final String ATTENDEE_TYPE = "attendeeType";
        public static final String EVENT_ID = "event_id";
        public static final int RELATIONSHIP_ATTENDEE = 1;
        public static final int RELATIONSHIP_NONE = 0;
        public static final int RELATIONSHIP_ORGANIZER = 2;
        public static final int RELATIONSHIP_PERFORMER = 3;
        public static final int RELATIONSHIP_SPEAKER = 4;
        public static final int TYPE_NONE = 0;
        public static final int TYPE_OPTIONAL = 2;
        public static final int TYPE_REQUIRED = 1;
        public static final int TYPE_RESOURCE = 3;
    }

    protected interface EventsColumns {
        public static final int ACCESS_CONFIDENTIAL = 1;
        public static final int ACCESS_DEFAULT = 0;
        public static final String ACCESS_LEVEL = "accessLevel";
        public static final int ACCESS_PRIVATE = 2;
        public static final int ACCESS_PUBLIC = 3;
        public static final String ALL_DAY = "allDay";
        public static final String AVAILABILITY = "availability";
        public static final int AVAILABILITY_BUSY = 0;
        public static final int AVAILABILITY_FREE = 1;
        public static final int AVAILABILITY_TENTATIVE = 2;
        public static final String CALENDAR_ID = "calendar_id";
        public static final String CAN_INVITE_OTHERS = "canInviteOthers";
        public static final String CUSTOM_APP_PACKAGE = "customAppPackage";
        public static final String CUSTOM_APP_URI = "customAppUri";
        public static final String DESCRIPTION = "description";
        public static final String DISPLAY_COLOR = "displayColor";
        public static final String DTEND = "dtend";
        public static final String DTSTART = "dtstart";
        public static final String DURATION = "duration";
        public static final String EVENT_COLOR = "eventColor";
        public static final String EVENT_COLOR_KEY = "eventColor_index";
        public static final String EVENT_END_TIMEZONE = "eventEndTimezone";
        public static final String EVENT_LOCATION = "eventLocation";
        public static final String EVENT_TIMEZONE = "eventTimezone";
        public static final String EXDATE = "exdate";
        public static final String EXRULE = "exrule";
        public static final String GUESTS_CAN_INVITE_OTHERS = "guestsCanInviteOthers";
        public static final String GUESTS_CAN_MODIFY = "guestsCanModify";
        public static final String GUESTS_CAN_SEE_GUESTS = "guestsCanSeeGuests";
        public static final String HAS_ALARM = "hasAlarm";
        public static final String HAS_ATTENDEE_DATA = "hasAttendeeData";
        public static final String HAS_EXTENDED_PROPERTIES = "hasExtendedProperties";
        public static final String IS_ORGANIZER = "isOrganizer";
        public static final String LAST_DATE = "lastDate";
        public static final String LAST_SYNCED = "lastSynced";
        public static final String ORGANIZER = "organizer";
        public static final String ORIGINAL_ALL_DAY = "originalAllDay";
        public static final String ORIGINAL_ID = "original_id";
        public static final String ORIGINAL_INSTANCE_TIME = "originalInstanceTime";
        public static final String ORIGINAL_SYNC_ID = "original_sync_id";
        public static final String RDATE = "rdate";
        public static final String RRULE = "rrule";
        public static final String SELF_ATTENDEE_STATUS = "selfAttendeeStatus";
        public static final String STATUS = "eventStatus";
        public static final int STATUS_CANCELED = 2;
        public static final int STATUS_CONFIRMED = 1;
        public static final int STATUS_TENTATIVE = 0;
        public static final String SYNC_DATA1 = "sync_data1";
        public static final String SYNC_DATA10 = "sync_data10";
        public static final String SYNC_DATA2 = "sync_data2";
        public static final String SYNC_DATA3 = "sync_data3";
        public static final String SYNC_DATA4 = "sync_data4";
        public static final String SYNC_DATA5 = "sync_data5";
        public static final String SYNC_DATA6 = "sync_data6";
        public static final String SYNC_DATA7 = "sync_data7";
        public static final String SYNC_DATA8 = "sync_data8";
        public static final String SYNC_DATA9 = "sync_data9";
        public static final String TITLE = "title";
        public static final String UID_2445 = "uid2445";
    }

    public static final class Attendees implements BaseColumns, AttendeesColumns, EventsColumns {
        private static final String ATTENDEES_WHERE = "event_id=?";
        public static final Uri CONTENT_URI = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Attendees.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Attendees.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.Attendees.<clinit>():void");
        }

        private Attendees() {
        }

        public static final Cursor query(ContentResolver cr, long eventId, String[] projection) {
            String[] attArgs = new String[1];
            attArgs[0] = Long.toString(eventId);
            return cr.query(CONTENT_URI, projection, ATTENDEES_WHERE, attArgs, null);
        }
    }

    protected interface CalendarAlertsColumns {
        public static final String ALARM_TIME = "alarmTime";
        public static final String BEGIN = "begin";
        public static final String CREATION_TIME = "creationTime";
        public static final String DEFAULT_SORT_ORDER = "begin ASC,title ASC";
        public static final String END = "end";
        public static final String EVENT_ID = "event_id";
        public static final String MINUTES = "minutes";
        public static final String NOTIFY_TIME = "notifyTime";
        public static final String RECEIVED_TIME = "receivedTime";
        public static final String STATE = "state";
        public static final int STATE_DISMISSED = 2;
        public static final int STATE_FIRED = 1;
        public static final int STATE_SCHEDULED = 0;
    }

    protected interface CalendarColumns {
        public static final String ALLOWED_ATTENDEE_TYPES = "allowedAttendeeTypes";
        public static final String ALLOWED_AVAILABILITY = "allowedAvailability";
        public static final String ALLOWED_REMINDERS = "allowedReminders";
        public static final String CALENDAR_ACCESS_LEVEL = "calendar_access_level";
        public static final String CALENDAR_COLOR = "calendar_color";
        public static final String CALENDAR_COLOR_KEY = "calendar_color_index";
        public static final String CALENDAR_DISPLAY_NAME = "calendar_displayName";
        public static final String CALENDAR_TIME_ZONE = "calendar_timezone";
        public static final int CAL_ACCESS_CONTRIBUTOR = 500;
        public static final int CAL_ACCESS_EDITOR = 600;
        public static final int CAL_ACCESS_FREEBUSY = 100;
        public static final int CAL_ACCESS_NONE = 0;
        public static final int CAL_ACCESS_OVERRIDE = 400;
        public static final int CAL_ACCESS_OWNER = 700;
        public static final int CAL_ACCESS_READ = 200;
        public static final int CAL_ACCESS_RESPOND = 300;
        public static final int CAL_ACCESS_ROOT = 800;
        public static final String CAN_MODIFY_TIME_ZONE = "canModifyTimeZone";
        public static final String CAN_ORGANIZER_RESPOND = "canOrganizerRespond";
        public static final String IS_PRIMARY = "isPrimary";
        public static final String MAX_REMINDERS = "maxReminders";
        public static final String OWNER_ACCOUNT = "ownerAccount";
        public static final String SYNC_EVENTS = "sync_events";
        public static final String VISIBLE = "visible";
    }

    public static final class CalendarAlerts implements BaseColumns, CalendarAlertsColumns, EventsColumns, CalendarColumns {
        public static final Uri CONTENT_URI = null;
        public static final Uri CONTENT_URI_BY_INSTANCE = null;
        private static final boolean DEBUG = false;
        private static final String SORT_ORDER_ALARMTIME_ASC = "alarmTime ASC";
        public static final String TABLE_NAME = "CalendarAlerts";
        private static final String WHERE_ALARM_EXISTS = "event_id=? AND begin=? AND alarmTime=?";
        private static final String WHERE_FINDNEXTALARMTIME = "alarmTime>=?";
        private static final String WHERE_RESCHEDULE_MISSED_ALARMS = "state=0 AND alarmTime<? AND alarmTime>? AND end>=?";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarAlerts.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarAlerts.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.CalendarAlerts.<clinit>():void");
        }

        private CalendarAlerts() {
        }

        public static final Uri insert(ContentResolver cr, long eventId, long begin, long end, long alarmTime, int minutes) {
            ContentValues values = new ContentValues();
            values.put("event_id", Long.valueOf(eventId));
            values.put("begin", Long.valueOf(begin));
            values.put("end", Long.valueOf(end));
            values.put(CalendarAlertsColumns.ALARM_TIME, Long.valueOf(alarmTime));
            values.put(CalendarAlertsColumns.CREATION_TIME, Long.valueOf(System.currentTimeMillis()));
            values.put(CalendarAlertsColumns.RECEIVED_TIME, Integer.valueOf(0));
            values.put(CalendarAlertsColumns.NOTIFY_TIME, Integer.valueOf(0));
            values.put("state", Integer.valueOf(0));
            values.put("minutes", Integer.valueOf(minutes));
            return cr.insert(CONTENT_URI, values);
        }

        public static final long findNextAlarmTime(ContentResolver cr, long millis) {
            String selection = "alarmTime>=" + millis;
            String[] projection = new String[1];
            projection[0] = CalendarAlertsColumns.ALARM_TIME;
            Uri uri = CONTENT_URI;
            String str = WHERE_FINDNEXTALARMTIME;
            String[] strArr = new String[1];
            strArr[0] = Long.toString(millis);
            Cursor cursor = cr.query(uri, projection, str, strArr, SORT_ORDER_ALARMTIME_ASC);
            long alarmTime = -1;
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        alarmTime = cursor.getLong(0);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return alarmTime;
        }

        public static final void rescheduleMissedAlarms(ContentResolver cr, Context context, AlarmManager manager) {
            long now = System.currentTimeMillis();
            long ancient = now - AlarmManager.INTERVAL_DAY;
            String[] projection = new String[1];
            projection[0] = CalendarAlertsColumns.ALARM_TIME;
            Uri uri = CONTENT_URI;
            String str = WHERE_RESCHEDULE_MISSED_ALARMS;
            String[] strArr = new String[3];
            strArr[0] = Long.toString(now);
            strArr[1] = Long.toString(ancient);
            strArr[2] = Long.toString(now);
            Cursor cursor = cr.query(uri, projection, str, strArr, SORT_ORDER_ALARMTIME_ASC);
            if (cursor != null) {
                long alarmTime = -1;
                while (cursor.moveToNext()) {
                    try {
                        long newAlarmTime = cursor.getLong(0);
                        if (alarmTime != newAlarmTime) {
                            scheduleAlarm(context, manager, newAlarmTime);
                            alarmTime = newAlarmTime;
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }

        public static void scheduleAlarm(Context context, AlarmManager manager, long alarmTime) {
            if (manager == null) {
                manager = (AlarmManager) context.getSystemService("alarm");
            }
            Intent intent = new Intent(CalendarContract.ACTION_EVENT_REMINDER);
            intent.setData(ContentUris.withAppendedId(CalendarContract.CONTENT_URI, alarmTime));
            intent.putExtra(CalendarAlertsColumns.ALARM_TIME, alarmTime);
            manager.setExactAndAllowWhileIdle(0, alarmTime, PendingIntent.getBroadcast(context, 0, intent, 0));
        }

        public static final boolean alarmExists(ContentResolver cr, long eventId, long begin, long alarmTime) {
            String[] projection = new String[1];
            projection[0] = CalendarAlertsColumns.ALARM_TIME;
            Uri uri = CONTENT_URI;
            String str = WHERE_ALARM_EXISTS;
            String[] strArr = new String[3];
            strArr[0] = Long.toString(eventId);
            strArr[1] = Long.toString(begin);
            strArr[2] = Long.toString(alarmTime);
            Cursor cursor = cr.query(uri, projection, str, strArr, null);
            boolean found = false;
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        found = true;
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return found;
        }
    }

    protected interface CalendarCacheColumns {
        public static final String KEY = "key";
        public static final String VALUE = "value";
    }

    public static final class CalendarCache implements CalendarCacheColumns {
        public static final String KEY_TIMEZONE_INSTANCES = "timezoneInstances";
        public static final String KEY_TIMEZONE_INSTANCES_PREVIOUS = "timezoneInstancesPrevious";
        public static final String KEY_TIMEZONE_TYPE = "timezoneType";
        public static final String TIMEZONE_TYPE_AUTO = "auto";
        public static final String TIMEZONE_TYPE_HOME = "home";
        public static final Uri URI = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarCache.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarCache.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.CalendarCache.<clinit>():void");
        }

        private CalendarCache() {
        }
    }

    protected interface CalendarSyncColumns {
        public static final String CAL_SYNC1 = "cal_sync1";
        public static final String CAL_SYNC10 = "cal_sync10";
        public static final String CAL_SYNC2 = "cal_sync2";
        public static final String CAL_SYNC3 = "cal_sync3";
        public static final String CAL_SYNC4 = "cal_sync4";
        public static final String CAL_SYNC5 = "cal_sync5";
        public static final String CAL_SYNC6 = "cal_sync6";
        public static final String CAL_SYNC7 = "cal_sync7";
        public static final String CAL_SYNC8 = "cal_sync8";
        public static final String CAL_SYNC9 = "cal_sync9";
    }

    protected interface SyncColumns extends CalendarSyncColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String CAN_PARTIALLY_UPDATE = "canPartiallyUpdate";
        public static final String DELETED = "deleted";
        public static final String DIRTY = "dirty";
        public static final String MUTATORS = "mutators";
        public static final String _SYNC_ID = "_sync_id";
    }

    public static final class CalendarEntity implements BaseColumns, SyncColumns, CalendarColumns {
        public static final Uri CONTENT_URI = null;

        private static class EntityIteratorImpl extends CursorEntityIterator {
            public EntityIteratorImpl(Cursor cursor) {
                super(cursor);
            }

            public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
                long calendarId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                ContentValues cv = new ContentValues();
                cv.put("_id", Long.valueOf(calendarId));
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "account_name");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "account_type");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "_sync_id");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "dirty");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, SyncColumns.MUTATORS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC1);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC2);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC3);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC4);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC5);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC6);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC7);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC8);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC9);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC10);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "name");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "calendar_displayName");
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.CALENDAR_COLOR);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarColumns.CALENDAR_COLOR_KEY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.CALENDAR_ACCESS_LEVEL);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.VISIBLE);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.SYNC_EVENTS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, Calendars.CALENDAR_LOCATION);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarColumns.CALENDAR_TIME_ZONE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarColumns.OWNER_ACCOUNT);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.CAN_ORGANIZER_RESPOND);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.CAN_MODIFY_TIME_ZONE);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CalendarColumns.MAX_REMINDERS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, SyncColumns.CAN_PARTIALLY_UPDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarColumns.ALLOWED_REMINDERS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, "deleted");
                Entity entity = new Entity(cv);
                cursor.moveToNext();
                return entity;
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarEntity.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarEntity.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.CalendarEntity.<clinit>():void");
        }

        private CalendarEntity() {
        }

        public static EntityIterator newEntityIterator(Cursor cursor) {
            return new EntityIteratorImpl(cursor);
        }
    }

    protected interface CalendarMetaDataColumns {
        public static final String LOCAL_TIMEZONE = "localTimezone";
        public static final String MAX_EVENTDAYS = "maxEventDays";
        public static final String MAX_INSTANCE = "maxInstance";
        public static final String MIN_EVENTDAYS = "minEventDays";
        public static final String MIN_INSTANCE = "minInstance";
    }

    public static final class CalendarMetaData implements CalendarMetaDataColumns, BaseColumns {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarMetaData.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private CalendarMetaData() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.CalendarMetaData.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.CalendarMetaData.<init>():void");
        }
    }

    public static final class Calendars implements BaseColumns, SyncColumns, CalendarColumns {
        public static final String CALENDAR_LOCATION = "calendar_location";
        public static final Uri CONTENT_URI = null;
        public static final String DEFAULT_SORT_ORDER = "calendar_displayName";
        public static final String NAME = "name";
        public static final String[] SYNC_WRITABLE_COLUMNS = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Calendars.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Calendars.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.Calendars.<clinit>():void");
        }

        private Calendars() {
        }
    }

    protected interface ColorsColumns extends Columns {
        public static final String COLOR = "color";
        public static final String COLOR_KEY = "color_index";
        public static final String COLOR_TYPE = "color_type";
        public static final int TYPE_CALENDAR = 0;
        public static final int TYPE_EVENT = 1;
    }

    public static final class Colors implements ColorsColumns {
        public static final Uri CONTENT_URI = null;
        public static final String TABLE_NAME = "Colors";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Colors.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Colors.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.Colors.<clinit>():void");
        }

        private Colors() {
        }
    }

    protected interface EventDaysColumns {
        public static final String ENDDAY = "endDay";
        public static final String STARTDAY = "startDay";
    }

    public static final class EventDays implements EventDaysColumns {
        public static final Uri CONTENT_URI = null;
        private static final String SELECTION = "selected=1";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventDays.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventDays.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.EventDays.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventDays.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private EventDays() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventDays.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.EventDays.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.CalendarContract.EventDays.query(android.content.ContentResolver, int, int, java.lang.String[]):android.database.Cursor, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public static final android.database.Cursor query(android.content.ContentResolver r1, int r2, int r3, java.lang.String[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.CalendarContract.EventDays.query(android.content.ContentResolver, int, int, java.lang.String[]):android.database.Cursor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.EventDays.query(android.content.ContentResolver, int, int, java.lang.String[]):android.database.Cursor");
        }
    }

    public static final class Events implements BaseColumns, SyncColumns, EventsColumns, CalendarColumns {
        public static final Uri CONTENT_EXCEPTION_URI = null;
        public static final Uri CONTENT_URI = null;
        private static final String DEFAULT_SORT_ORDER = "";
        public static String[] PROVIDER_WRITABLE_COLUMNS;
        public static final String[] SYNC_WRITABLE_COLUMNS = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Events.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Events.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.Events.<clinit>():void");
        }

        private Events() {
        }
    }

    public static final class EventsEntity implements BaseColumns, SyncColumns, EventsColumns {
        public static final Uri CONTENT_URI = null;

        private static class EntityIteratorImpl extends CursorEntityIterator {
            private static final String[] ATTENDEES_PROJECTION = null;
            private static final int COLUMN_ATTENDEE_EMAIL = 1;
            private static final int COLUMN_ATTENDEE_IDENTITY = 5;
            private static final int COLUMN_ATTENDEE_ID_NAMESPACE = 6;
            private static final int COLUMN_ATTENDEE_NAME = 0;
            private static final int COLUMN_ATTENDEE_RELATIONSHIP = 2;
            private static final int COLUMN_ATTENDEE_STATUS = 4;
            private static final int COLUMN_ATTENDEE_TYPE = 3;
            private static final int COLUMN_ID = 0;
            private static final int COLUMN_METHOD = 1;
            private static final int COLUMN_MINUTES = 0;
            private static final int COLUMN_NAME = 1;
            private static final int COLUMN_VALUE = 2;
            private static final String[] EXTENDED_PROJECTION = null;
            private static final String[] REMINDERS_PROJECTION = null;
            private static final String WHERE_EVENT_ID = "event_id=?";
            private final ContentProviderClient mProvider;
            private final ContentResolver mResolver;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventsEntity.EntityIteratorImpl.<clinit>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            static {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventsEntity.EntityIteratorImpl.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.EventsEntity.EntityIteratorImpl.<clinit>():void");
            }

            public EntityIteratorImpl(Cursor cursor, ContentResolver resolver) {
                super(cursor);
                this.mResolver = resolver;
                this.mProvider = null;
            }

            public EntityIteratorImpl(Cursor cursor, ContentProviderClient provider) {
                super(cursor);
                this.mResolver = null;
                this.mProvider = provider;
            }

            public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
                ContentResolver contentResolver;
                Uri uri;
                String[] strArr;
                String str;
                String[] strArr2;
                Cursor subCursor;
                ContentProviderClient contentProviderClient;
                long eventId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                ContentValues cv = new ContentValues();
                cv.put("_id", Long.valueOf(eventId));
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.CALENDAR_ID);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "title");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "description");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_LOCATION);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.STATUS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.SELF_ATTENDEE_STATUS);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.DTSTART);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.DTEND);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "duration");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_TIMEZONE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_END_TIMEZONE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "allDay");
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.ACCESS_LEVEL);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.AVAILABILITY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_COLOR);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_COLOR_KEY);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.HAS_ALARM);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.HAS_EXTENDED_PROPERTIES);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.RRULE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.RDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EXRULE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EXDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_SYNC_ID);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_ID);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_INSTANCE_TIME);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_ALL_DAY);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.LAST_DATE);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.HAS_ATTENDEE_DATA);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.GUESTS_CAN_INVITE_OTHERS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.GUESTS_CAN_MODIFY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.GUESTS_CAN_SEE_GUESTS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.CUSTOM_APP_PACKAGE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "customAppUri");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.UID_2445);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.ORGANIZER);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.IS_ORGANIZER);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, "_sync_id");
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, "dirty");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, SyncColumns.MUTATORS);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.LAST_SYNCED);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, "deleted");
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA1);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA2);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA3);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA4);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA5);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA6);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA7);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA8);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA9);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.SYNC_DATA10);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC1);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC2);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC3);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC4);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC5);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC6);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC7);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC8);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC9);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarSyncColumns.CAL_SYNC10);
                Entity entity = new Entity(cv);
                if (this.mResolver != null) {
                    contentResolver = this.mResolver;
                    uri = Reminders.CONTENT_URI;
                    strArr = REMINDERS_PROJECTION;
                    str = WHERE_EVENT_ID;
                    strArr2 = new String[1];
                    strArr2[0] = Long.toString(eventId);
                    subCursor = contentResolver.query(uri, strArr, str, strArr2, null);
                } else {
                    contentProviderClient = this.mProvider;
                    uri = Reminders.CONTENT_URI;
                    strArr = REMINDERS_PROJECTION;
                    str = WHERE_EVENT_ID;
                    strArr2 = new String[1];
                    strArr2[0] = Long.toString(eventId);
                    subCursor = contentProviderClient.query(uri, strArr, str, strArr2, null);
                }
                while (subCursor.moveToNext()) {
                    try {
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put("minutes", Integer.valueOf(subCursor.getInt(0)));
                        reminderValues.put("method", Integer.valueOf(subCursor.getInt(1)));
                        entity.addSubValue(Reminders.CONTENT_URI, reminderValues);
                    } finally {
                        subCursor.close();
                    }
                }
                if (this.mResolver != null) {
                    contentResolver = this.mResolver;
                    uri = Attendees.CONTENT_URI;
                    strArr = ATTENDEES_PROJECTION;
                    str = WHERE_EVENT_ID;
                    strArr2 = new String[1];
                    strArr2[0] = Long.toString(eventId);
                    subCursor = contentResolver.query(uri, strArr, str, strArr2, null);
                } else {
                    contentProviderClient = this.mProvider;
                    uri = Attendees.CONTENT_URI;
                    strArr = ATTENDEES_PROJECTION;
                    str = WHERE_EVENT_ID;
                    strArr2 = new String[1];
                    strArr2[0] = Long.toString(eventId);
                    subCursor = contentProviderClient.query(uri, strArr, str, strArr2, null);
                }
                while (subCursor.moveToNext()) {
                    try {
                        ContentValues attendeeValues = new ContentValues();
                        attendeeValues.put(AttendeesColumns.ATTENDEE_NAME, subCursor.getString(0));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_EMAIL, subCursor.getString(1));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_RELATIONSHIP, Integer.valueOf(subCursor.getInt(2)));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_TYPE, Integer.valueOf(subCursor.getInt(3)));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_STATUS, Integer.valueOf(subCursor.getInt(4)));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_IDENTITY, subCursor.getString(5));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_ID_NAMESPACE, subCursor.getString(6));
                        entity.addSubValue(Attendees.CONTENT_URI, attendeeValues);
                    } finally {
                        subCursor.close();
                    }
                }
                if (this.mResolver != null) {
                    contentResolver = this.mResolver;
                    uri = ExtendedProperties.CONTENT_URI;
                    strArr = EXTENDED_PROJECTION;
                    str = WHERE_EVENT_ID;
                    strArr2 = new String[1];
                    strArr2[0] = Long.toString(eventId);
                    subCursor = contentResolver.query(uri, strArr, str, strArr2, null);
                } else {
                    contentProviderClient = this.mProvider;
                    uri = ExtendedProperties.CONTENT_URI;
                    strArr = EXTENDED_PROJECTION;
                    str = WHERE_EVENT_ID;
                    strArr2 = new String[1];
                    strArr2[0] = Long.toString(eventId);
                    subCursor = contentProviderClient.query(uri, strArr, str, strArr2, null);
                }
                while (subCursor.moveToNext()) {
                    try {
                        ContentValues extendedValues = new ContentValues();
                        extendedValues.put("_id", subCursor.getString(0));
                        extendedValues.put("name", subCursor.getString(1));
                        extendedValues.put("value", subCursor.getString(2));
                        entity.addSubValue(ExtendedProperties.CONTENT_URI, extendedValues);
                    } finally {
                        subCursor.close();
                    }
                }
                cursor.moveToNext();
                return entity;
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventsEntity.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventsEntity.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.EventsEntity.<clinit>():void");
        }

        private EventsEntity() {
        }

        public static EntityIterator newEntityIterator(Cursor cursor, ContentResolver resolver) {
            return new EntityIteratorImpl(cursor, resolver);
        }

        public static EntityIterator newEntityIterator(Cursor cursor, ContentProviderClient provider) {
            return new EntityIteratorImpl(cursor, provider);
        }
    }

    protected interface EventsRawTimesColumns {
        public static final String DTEND_2445 = "dtend2445";
        public static final String DTSTART_2445 = "dtstart2445";
        public static final String EVENT_ID = "event_id";
        public static final String LAST_DATE_2445 = "lastDate2445";
        public static final String ORIGINAL_INSTANCE_TIME_2445 = "originalInstanceTime2445";
    }

    public static final class EventsRawTimes implements BaseColumns, EventsRawTimesColumns {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventsRawTimes.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private EventsRawTimes() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.EventsRawTimes.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.EventsRawTimes.<init>():void");
        }
    }

    protected interface ExtendedPropertiesColumns {
        public static final String EVENT_ID = "event_id";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }

    public static final class ExtendedProperties implements BaseColumns, ExtendedPropertiesColumns, EventsColumns {
        public static final Uri CONTENT_URI = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.ExtendedProperties.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.ExtendedProperties.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.ExtendedProperties.<clinit>():void");
        }

        private ExtendedProperties() {
        }
    }

    public static final class Instances implements BaseColumns, EventsColumns, CalendarColumns {
        public static final String BEGIN = "begin";
        public static final Uri CONTENT_BY_DAY_URI = null;
        public static final Uri CONTENT_SEARCH_BY_DAY_URI = null;
        public static final Uri CONTENT_SEARCH_URI = null;
        public static final Uri CONTENT_URI = null;
        private static final String DEFAULT_SORT_ORDER = "begin ASC";
        public static final String END = "end";
        public static final String END_DAY = "endDay";
        public static final String END_MINUTE = "endMinute";
        public static final String EVENT_ID = "event_id";
        public static final String START_DAY = "startDay";
        public static final String START_MINUTE = "startMinute";
        private static final String[] WHERE_CALENDARS_ARGS = null;
        private static final String WHERE_CALENDARS_SELECTED = "visible=?";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Instances.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Instances.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.Instances.<clinit>():void");
        }

        private Instances() {
        }

        public static final Cursor query(ContentResolver cr, String[] projection, long begin, long end) {
            Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, end);
            return cr.query(builder.build(), projection, WHERE_CALENDARS_SELECTED, WHERE_CALENDARS_ARGS, DEFAULT_SORT_ORDER);
        }

        public static final Cursor query(ContentResolver cr, String[] projection, long begin, long end, String searchQuery) {
            Builder builder = CONTENT_SEARCH_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, end);
            return cr.query(builder.appendPath(searchQuery).build(), projection, WHERE_CALENDARS_SELECTED, WHERE_CALENDARS_ARGS, DEFAULT_SORT_ORDER);
        }
    }

    protected interface RemindersColumns {
        public static final String EVENT_ID = "event_id";
        public static final String METHOD = "method";
        public static final int METHOD_ALARM = 4;
        public static final int METHOD_ALERT = 1;
        public static final int METHOD_DEFAULT = 0;
        public static final int METHOD_EMAIL = 2;
        public static final int METHOD_SMS = 3;
        public static final String MINUTES = "minutes";
        public static final int MINUTES_DEFAULT = -1;
    }

    public static final class Reminders implements BaseColumns, RemindersColumns, EventsColumns {
        public static final Uri CONTENT_URI = null;
        private static final String REMINDERS_WHERE = "event_id=?";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Reminders.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.Reminders.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.Reminders.<clinit>():void");
        }

        private Reminders() {
        }

        public static final Cursor query(ContentResolver cr, long eventId, String[] projection) {
            String[] remArgs = new String[1];
            remArgs[0] = Long.toString(eventId);
            return cr.query(CONTENT_URI, projection, REMINDERS_WHERE, remArgs, null);
        }
    }

    public static final class SyncState implements Columns {
        private static final String CONTENT_DIRECTORY = "syncstate";
        public static final Uri CONTENT_URI = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.SyncState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.SyncState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.SyncState.<clinit>():void");
        }

        private SyncState() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.CalendarContract.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.CalendarContract.<clinit>():void");
    }

    private CalendarContract() {
    }
}

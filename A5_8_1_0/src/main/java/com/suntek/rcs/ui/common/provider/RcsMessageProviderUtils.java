package com.suntek.rcs.ui.common.provider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import com.suntek.mway.rcs.client.aidl.common.RcsColumns.SmsRcsColumns;
import com.suntek.mway.rcs.client.aidl.common.RcsColumns.ThreadColumns;
import com.suntek.mway.rcs.client.aidl.constant.Parameter;

public class RcsMessageProviderUtils {
    private static final String NO_SUCH_COLUMN_EXCEPTION_MESSAGE = "no such column";
    private static final String NO_SUCH_TABLE_EXCEPTION_MESSAGE = "no such table";
    private static final String TAG = "RcsMessageProviderUtils";

    public static void upgradeDatabaseToVersion65(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE threads RENAME TO threads_old;");
        createRcsThreadsTable(db);
        db.execSQL("INSERT INTO threads (_id,date,message_count,recipient_ids,snippet,snippet_cs,read,archived,type,error,has_attachment,rcs_top,rcs_top_time)SELECT _id,date,message_count,recipient_ids,snippet,snippet_cs,read,archived,type,error,has_attachment,top,top_time  FROM threads_old;");
        db.execSQL("update threads set msg_chat_type = 1;");
        db.execSQL("DROP TABLE threads_old;");
        db.execSQL("ALTER TABLE sms RENAME TO sms_old;");
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_on_insert;");
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_date_subject_on_update;");
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_read_on_update;");
        db.execSQL("DROP TRIGGER IF EXISTS update_threads_error_on_update_sms;");
        db.execSQL("DROP TRIGGER IF EXISTS sms_words_update;");
        db.execSQL("DROP TRIGGER IF EXISTS sms_words_delete;");
        createRcsSmsTable(db);
        createSmsTrigger65(db);
        db.execSQL("INSERT INTO sms (_id,thread_id,address,person,date,date_sent,protocol,read,status,type,reply_path_present,subject,body,service_center,locked,sub_id, phone_id, error_code,creator,seen,priority,favourite,rcs_message_id,rcs_file_name,rcs_mime_type,rcs_msg_type,rcs_msg_state,rcs_chat_type,rcs_conversation_id,rcs_contribution_id,rcs_file_selector,rcs_file_transfered,rcs_file_transfer_id, rcs_file_icon,rcs_burn,rcs_header,rcs_file_path,rcs_is_download,rcs_file_size,rcs_thumb_path,rcs_burn_body )SELECT _id,thread_id,address,person,date,date_sent,protocol,read,status,type,reply_path_present,subject,body,service_center,locked,sub_id, phone_id, error_code,creator,seen,priority,favourite,rcs_message_id,rcs_filename,rcs_mime_type,rcs_msg_type,rcs_msg_state,rcs_chat_type,rcs_conversation_id,rcs_contribution_id,rcs_file_selector,rcs_file_transfer_ext,rcs_file_transfer_id,rcs_file_icon,rcs_is_burn,rcs_header,rcs_path,rcs_is_download,rcs_file_size,rcs_thumb_path,rcs_burn_body  FROM sms_old;");
        db.execSQL("DROP TABLE sms_old;");
        db.execSQL("update sms set rcs_chat_type = 1;");
        createRcsOneToManyMesageStatusTable(db);
        createRcsThreadUpdateTriggers(db);
        createDeviceApiSqlView(db);
        createRcsDeleteGroupStatesTriggers(db);
    }

    public static void createRcsOneToManyMesageStatusTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE group_status (_id INTEGER PRIMARY KEY,msg_id INTEGER DEFAULT -1,date INTEGER DEFAULT 0,number TEXT,status INTEGER DEFAULT 0 );");
    }

    public static void createRcsThreadUpdateTriggers(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER sms_update_thread_rcs_message_info_on_insert  AFTER INSERT ON sms BEGIN   UPDATE threads SET msg_chat_type =     (CASE WHEN msg_chat_type != -1     THEN msg_chat_type WHEN msg_chat_type= -1 AND new.rcs_chat_type!= -1 THEN new.rcs_chat_type ELSE msg_chat_type END),rcs_number =     (CASE WHEN new.rcs_chat_type= 1    or new.rcs_chat_type = 2    or new.rcs_chat_type= 4     THEN new.address ELSE rcs_number    END)     WHERE threads._id = new.thread_id    AND new.rcs_msg_type != -1;   UPDATE threads SET last_msg_id =     new._id,last_msg_type    = new.rcs_msg_type    WHERE threads . _id = new . thread_id; END;");
        createSmsDeleteDuplicateRecordBeforeInsertTriggers(db);
    }

    public static void createRcsDeleteGroupStatesTriggers(SQLiteDatabase db) {
        db.execSQL("create trigger delete_group_status_after_delete_sms AFTER DELETE ON sms BEGIN delete from group_status where msg_id = old._id; END");
    }

    public static void createSmsDeleteDuplicateRecordBeforeInsertTriggers(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER sms_delete_duplicate_record_before_insert BEFORE INSERT ON sms when new.type= 1 and new.rcs_message_id!= -1 and new.rcs_message_id IS NOT NULL  BEGIN  select raise(rollback,'')  where (select _id from sms where rcs_message_id = new.rcs_message_id and type= 1 and sub_id = new.sub_id) is not null; END;");
    }

    public static void createRcsThreadsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE threads (_id INTEGER PRIMARY KEY AUTOINCREMENT,date INTEGER DEFAULT 0,message_count INTEGER DEFAULT 0,recipient_ids TEXT,snippet TEXT,snippet_cs INTEGER DEFAULT 0,read INTEGER DEFAULT 1,archived INTEGER DEFAULT 0,type INTEGER DEFAULT 0,error INTEGER DEFAULT 0,has_attachment INTEGER DEFAULT 0,rcs_top INTEGER DEFAULT 0,rcs_top_time INTEGER DEFAULT 0,rcs_number TEXT,last_msg_id INTEGER  DEFAULT -1,last_msg_type INTEGER  DEFAULT -1,msg_chat_type INTEGER  DEFAULT -1 );");
    }

    public static void createRcsSmsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sms (_id INTEGER PRIMARY KEY,thread_id INTEGER,address TEXT,person INTEGER,date INTEGER,date_sent INTEGER DEFAULT 0,protocol INTEGER,read INTEGER DEFAULT 0,status INTEGER DEFAULT -1,type INTEGER,reply_path_present INTEGER,subject TEXT,body TEXT,service_center TEXT,locked INTEGER DEFAULT 0,sub_id INTEGER DEFAULT -1, phone_id INTEGER DEFAULT -1, error_code INTEGER DEFAULT 0,creator TEXT,seen INTEGER DEFAULT 0,priority INTEGER DEFAULT -1,favourite INTEGER DEFAULT 0,rcs_message_id TEXT,rcs_file_name TEXT,rcs_mime_type TEXT,rcs_msg_type INTEGER DEFAULT -1,rcs_msg_state INTEGER,rcs_chat_type INTEGER DEFAULT -1,rcs_conversation_id TEXT,rcs_contribution_id TEXT,rcs_file_selector TEXT,rcs_file_transfered TEXT,rcs_file_transfer_id TEXT,rcs_file_icon TEXT,rcs_burn INTEGER  DEFAULT -1,rcs_header TEXT,rcs_file_path TEXT,rcs_is_download INTEGER DEFAULT 0,rcs_file_size INTEGER DEFAULT 0,rcs_thumb_path TEXT,rcs_burn_body TEXT,rcs_media_played INTEGER DEFAULT 0,rcs_ext_contact TEXT,rcs_file_record INTEGER );");
    }

    public static void createSmsTrigger65(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER sms_update_thread_on_insert AFTER INSERT ON sms BEGIN  UPDATE threads SET    date = (strftime('%s','now') * 1000),     snippet = new.body,     snippet_cs = 0  WHERE threads._id = new.thread_id;   UPDATE threads SET message_count =      (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads       ON threads._id = thread_id      WHERE thread_id = new.thread_id        AND sms.type != 3) +      (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads       ON threads._id = thread_id      WHERE thread_id = new.thread_id        AND (m_type=132 OR m_type=130 OR m_type=128)        AND msg_box != 3)   WHERE threads._id = new.thread_id;   UPDATE threads SET read =     CASE (SELECT COUNT(*)          FROM sms          WHERE read = 0            AND thread_id = threads._id)      WHEN 0 THEN 1      ELSE 0    END  WHERE threads._id = new.thread_id; END;");
        db.execSQL("CREATE TRIGGER sms_update_thread_date_subject_on_update AFTER  UPDATE OF date, body, type  ON sms BEGIN  UPDATE threads SET    date = (strftime('%s','now') * 1000),     snippet = new.body,     snippet_cs = 0  WHERE threads._id = new.thread_id;   UPDATE threads SET message_count =      (SELECT COUNT(sms._id) FROM sms LEFT JOIN threads       ON threads._id = thread_id      WHERE thread_id = new.thread_id        AND sms.type != 3) +      (SELECT COUNT(pdu._id) FROM pdu LEFT JOIN threads       ON threads._id = thread_id      WHERE thread_id = new.thread_id        AND (m_type=132 OR m_type=130 OR m_type=128)        AND msg_box != 3)   WHERE threads._id = new.thread_id;   UPDATE threads SET read =     CASE (SELECT COUNT(*)          FROM sms          WHERE read = 0            AND thread_id = threads._id)      WHEN 0 THEN 1      ELSE 0    END  WHERE threads._id = new.thread_id; END;");
        db.execSQL("CREATE TRIGGER sms_update_thread_read_on_update AFTER  UPDATE OF read  ON sms BEGIN   UPDATE threads SET read =     CASE (SELECT COUNT(*)          FROM sms          WHERE read = 0            AND thread_id = threads._id)      WHEN 0 THEN 1      ELSE 0    END  WHERE threads._id = new.thread_id; END;");
        db.execSQL("CREATE TRIGGER update_threads_error_on_update_sms   AFTER UPDATE OF type ON sms  WHEN (OLD.type != 5 AND NEW.type = 5)    OR (OLD.type = 5 AND NEW.type != 5) BEGIN   UPDATE threads SET error =     CASE      WHEN NEW.type = 5 THEN error + 1      ELSE error - 1    END   WHERE _id = NEW.thread_id; END;");
        db.execSQL("CREATE TRIGGER sms_words_update AFTER UPDATE ON sms BEGIN UPDATE words  SET index_text = NEW.body WHERE (source_id=NEW._id AND table_to_use=1);  END;");
        db.execSQL("CREATE TRIGGER sms_words_delete AFTER DELETE ON sms BEGIN DELETE FROM   words WHERE source_id = OLD._id AND table_to_use = 1; END;");
    }

    public static void upgradeDatabaseToVersion66(SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS sms_update_thread_rcs_message_info_on_insert");
        createRcsThreadUpdateTriggers(db);
        createDeviceApiSqlView(db);
        createRcsDeleteGroupStatesTriggers(db);
    }

    public static void createDeviceApiSqlView(SQLiteDatabase db) {
        db.execSQL("create view device_api_messages as select sms._id as CHATMESSAGE_MESSAGE_ID,(CASE WHEN sms.rcs_chat_type = 3 THEN (select a.address from sms a where a.thread_id = sms.thread_id AND a.rcs_msg_type = 7) ELSE NULL END)  as CHATMESSAGE_CHAT_ID,sms.date as CHATMESSAGE_TIMESTAMP,sms.thread_id as CHATMESSAGE_CONVERSATION,sms.rcs_chat_type as CHATMESSAGE_FLAG, 0  as CHATMESSAGE_ISBLOCKED,(CASE WHEN sms.type = 1 THEN 0 ELSE 1 END) as CHATMESSAGE_DIRECTION,(CASE WHEN sms.rcs_chat_type = 3 THEN  (select a.address from sms a where a._id = sms._id AND a.rcs_msg_type <> 7 )  ELSE sms.address END)  as CHATMESSAGE_CONTACT_NUMBER,(CASE WHEN sms.rcs_file_transfer_id >= 0 THEN sms.rcs_file_transfer_id ELSE (CASE WHEN sms.rcs_message_id  is not null THEN sms.body ELSE NULL END)END) as CHATMESSAGE_BODY,(CASE WHEN sms.rcs_file_transfer_id >= 0 THEN 5 ELSE 3 END) as CHATMESSAGE_TYPE, (CASE WHEN sms.type = 1 AND sms.read = 0 THEN 0 ELSE (CASE WHEN sms.type = 1 AND sms.read = 1 THEN 2 ELSE (CASE WHEN sms.rcs_message_id is null AND sms.type = 6 THEN 3 WHEN sms.rcs_message_id is null AND sms.type = 2 THEN 4 WHEN sms.rcs_message_id is null AND sms.type = 5 THEN 5 ELSE (CASE WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = 64 THEN 3 WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = 32 THEN 4 WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = 128 THEN 5 WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = -1 THEN 7 ELSE 6 END)END)END)END) as CHATMESSAGE_MESSAGE_STATUS from sms WHERE sms.rcs_chat_type in (1,2,3) and sms.rcs_msg_type <> 7");
        db.execSQL("create view message_conversation as select threads.snippet as CHATMESSAGE_BODY,threads.date as CHATMESSAGE_TIMESTAMP,threads.message_count as CHATMESSAGE_MESSAGE_COUNT,(select COUNT(*) FROM (select sms.read FROM sms inner join threads on sms.thread_id = threads._id WHERE sms.read = 0  UNION ALL SELECT pdu.read FROM pdu inner join threads on pdu.thread_id = threads._id WHERE pdu.read = 0)) as CHATMESSAGE_UNREAD_COUNT,threads.msg_chat_type as CHATMESSAGE_FLAG,(CASE WHEN threads.msg_chat_type = 4 then NULL ELSE threads._id END ) as CHATMESSAGE_CONVERSATION_ID,(CASE WHEN threads.msg_chat_type = 3 THEN (select sms.address from sms where threads._id = sms.thread_id AND sms.rcs_chat_type = 3 AND sms.rcs_msg_type = 7) ELSE sms.address END) as CHATMESSAGE_RECIPIENTS,(CASE WHEN sms.rcs_file_transfer_id >= 0 AND threads.last_msg_id=sms._id THEN 5 ELSE (CASE WHEN threads.msg_chat_type = 4 THEN 4 ELSE (CASE WHEN threads.msg_chat_type = 1 OR threads.msg_chat_type = 2 OR threads.msg_chat_type = 3 THEN 3 ELSE 0 END)END)END) as CHATMESSAGE_TYPE,(CASE WHEN sms.rcs_file_transfer_id >= 0 AND threads.last_msg_id=sms._id THEN sms.rcs_mime_type ELSE NULL END) as CHATMESSAGE_MIME_TYPE from threads inner join sms on threads.last_msg_id = sms._id and sms.rcs_msg_type <> 7");
        db.execSQL("create view public_account_messages as select sms.address as PUBLICACCOUNTSERVICE_ACCOUNT,sms.body as PUBLICACCOUNTSERVICE_BODY,sms.date as PUBLICACCOUNTSERVICE_TIMESTAMP,(CASE WHEN sms.type = 1 THEN 0 ELSE 1 END) as PUBLICACCOUNTSERVICE_DIRECTION,sms.rcs_mime_type as PUBLICACCOUNTSERVICE_MIME_TYPE,(CASE WHEN sms.rcs_file_transfer_id >= 0 THEN 5 ELSE 4 END) as PUBLICACCOUNTSERVICE_TYPE, (CASE WHEN sms.type = 1 AND sms.read = 0 THEN 0 ELSE (CASE WHEN sms.type = 1 AND sms.read = 1 THEN 2 ELSE (CASE WHEN sms.rcs_message_id is null AND sms.type = 6 THEN 3 WHEN sms.rcs_message_id is null AND sms.type = 2 THEN 4 WHEN sms.rcs_message_id is null AND sms.type = 5 THEN 5 ELSE (CASE WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = 64 THEN 3 WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = 32 THEN 4 WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = 128 THEN 5 WHEN sms.rcs_message_id is not null AND sms.rcs_msg_state = -1 THEN 7 ELSE 6 END)END)END)END) as PUBLICACCOUNTSERVICE_MESSAGE_STATUS,(CASE WHEN sms.rcs_file_transfer_id >= 0 THEN sms.rcs_file_transfer_id ELSE sms._id END) as PUBLICACCOUNTSERVICE_MESSAGE_ID from sms WHERE sms.rcs_chat_type = 4");
        db.execSQL("create view file_transfer_message as select sms.date as TIMESTAMP,sms.address as CONTACT_NUMBER,sms.rcs_file_transfer_id as FT_ID,sms.rcs_file_name as FILENAME,sms.rcs_thumb_path as FILEICON,sms.rcs_file_size as FILE_SIZE,sms.rcs_file_transfered as TRANSFERRED,sms.rcs_mime_type as TYPE,(CASE WHEN sms.type = 1 THEN 0 ELSE 1 END) as DIRECTION,(CASE WHEN type = 2 AND rcs_msg_state = 64 THEN 2 WHEN type = 2 AND rcs_msg_state = 32 OR rcs_msg_state = -1 THEN 3 WHEN (type = 2 OR type = 5) AND rcs_msg_state = 128 THEN 5 WHEN type = 1 AND (rcs_msg_type = 1 OR rcs_msg_type = 3 ) AND rcs_file_transfered = 0  THEN 0 WHEN type = 1 AND (rcs_msg_type = 1 OR rcs_msg_type = 3 ) AND rcs_file_transfered = rcs_file_size THEN 3 WHEN type = 1 AND (rcs_msg_type = 1 OR rcs_msg_type = 3 ) AND (rcs_file_transfered < rcs_file_size AND rcs_file_transfered > 0 ) THEN 2 WHEN type = 1 AND (rcs_msg_type <> 1 AND rcs_msg_type <> 3 ) THEN 3 ELSE 0 END) as STATE from sms WHERE sms.rcs_file_transfer_id >= 0 ");
        createDeviceApiIndex(db);
    }

    public static void createDeviceApiBlockedCallogView(SQLiteDatabase db) {
        db.execSQL("create view blocked_callog_view as select  _id as BLACKLIST_CALL_ID, contact as BLACKLIST_PHONE_NUMBER, name as BLACKLIST_NAME from blockrecorditems where block_type = 0");
    }

    public static void createDeviceApiIndex(SQLiteDatabase db) {
        db.execSQL("create index rcsChatTypeThreadIdIndex on sms (rcs_chat_type,thread_id)");
        db.execSQL("create index rcsFileTransferIdIndex on sms (rcs_file_transfer_id)");
        db.execSQL("create index rcsMessageIdIndex on sms (rcs_message_id)");
        db.execSQL("create index threadIdIndex on sms(thread_id)");
    }

    public static Cursor getConvasatonUnreadCount(SQLiteDatabase db, Uri uri) {
        String threadId = uri.getQueryParameter(Parameter.EXTRA_THREAD_ID);
        return db.rawQuery("     SELECT COUNT(*) FROM (SELECT read FROM      sms WHERE read = 0 AND thread_id = " + threadId + "     UNION ALL SELECT read FROM pdu WHERE read = 0 " + "     AND thread_id = " + threadId + ") ;", new String[0]);
    }

    public static void checkAndUpdateThreadTable(SQLiteDatabase db) {
        try {
            db.query(RcsMessageProviderConstants.TABLE_THREADS, null, null, null, null, null, null);
            checkAndUpdateRcsThreadTable(db);
        } catch (SQLiteException e) {
            Log.e(TAG, "onOpen: ex. ", e);
            if (e.getMessage().startsWith(NO_SUCH_TABLE_EXCEPTION_MESSAGE)) {
                createRcsThreadsTable(db);
            }
        }
    }

    public static void checkAndUpdateRcsThreadTable(SQLiteDatabase db) {
        try {
            db.query(RcsMessageProviderConstants.TABLE_THREADS, new String[]{ThreadColumns.RCS_TOP, ThreadColumns.RCS_TOP_TIME, "rcs_number", ThreadColumns.RCS_MSG_ID, ThreadColumns.RCS_MSG_TYPE, ThreadColumns.RCS_CHAT_TYPE}, null, null, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "checkAndUpgradeSmsTable: ex. ", e);
            if (e.getMessage().startsWith(NO_SUCH_COLUMN_EXCEPTION_MESSAGE)) {
                db.execSQL("ALTER TABLE threads ADD COLUMN rcs_top INTEGER DEFAULT 0 ");
                db.execSQL("ALTER TABLE threads ADD COLUMN rcs_top_time INTEGER DEFAULT 0 ");
                db.execSQL("ALTER TABLE threads ADD COLUMN rcs_number TEXT");
                db.execSQL("ALTER TABLE threads ADD COLUMN last_msg_id INTEGER  DEFAULT -1 ");
                db.execSQL("ALTER TABLE threads ADD COLUMN last_msg_type INTEGER  DEFAULT -1");
                db.execSQL("ALTER TABLE threads ADD COLUMN msg_chat_type INTEGER  DEFAULT -1 ");
            }
        }
    }

    public static void checkAndUpdateRcsSmsTable(SQLiteDatabase db) {
        try {
            db.query(RcsMessageProviderConstants.TABLE_SMS, new String[]{"favourite", "rcs_message_id", "rcs_file_name", "rcs_mime_type", "rcs_msg_type", SmsRcsColumns.RCS_MSG_STATE, "rcs_chat_type", "rcs_conversation_id", "rcs_contribution_id", "rcs_file_selector", "rcs_file_transfered", "rcs_file_transfer_id", "rcs_file_icon", "rcs_burn", SmsRcsColumns.RCS_HEADER, SmsRcsColumns.RCS_PATH, "rcs_is_download", "rcs_file_size", "rcs_thumb_path", SmsRcsColumns.RCS_BURN_BODY, SmsRcsColumns.RCS_MEDIA_PLAYED, SmsRcsColumns.RCS_EXT_CONTACT, "rcs_file_record"}, null, null, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, "checkAndUpgradeSmsTable: ex. ", e);
            if (e.getMessage().startsWith(NO_SUCH_COLUMN_EXCEPTION_MESSAGE)) {
                db.execSQL("ALTER TABLE sms ADD COLUMN favourite INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_message_id TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_name TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_mime_type TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_msg_type INTEGER DEFAULT -1");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_msg_state INTEGER");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_chat_type INTEGER DEFAULT -1");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_conversation_id TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_contribution_id TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_selector TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_transfered TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_transfer_id TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_icon TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_burn INTEGER  DEFAULT -1");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_header TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_path TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_is_download INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_size INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_thumb_path TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_burn_body TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_media_played INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_ext_contact TEXT");
                db.execSQL("ALTER TABLE sms ADD COLUMN rcs_file_record INTEGER");
            }
        }
    }

    public static void createGroupStatusUpdateTriggers(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER sms_update_group_on_delete AFTER DELETE ON sms BEGIN DELETE FROM group_status WHERE msg_id = old._id; END");
    }
}

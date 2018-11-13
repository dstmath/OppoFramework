package android.provider;

import android.R;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public class Contacts {
    @Deprecated
    public static final String AUTHORITY = "contacts";
    @Deprecated
    public static final Uri CONTENT_URI = null;
    @Deprecated
    public static final int KIND_EMAIL = 1;
    @Deprecated
    public static final int KIND_IM = 3;
    @Deprecated
    public static final int KIND_ORGANIZATION = 4;
    @Deprecated
    public static final int KIND_PHONE = 5;
    @Deprecated
    public static final int KIND_POSTAL = 2;
    private static final String TAG = "Contacts";

    @Deprecated
    public interface ContactMethodsColumns {
        @Deprecated
        public static final String AUX_DATA = "aux_data";
        @Deprecated
        public static final String DATA = "data";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
        @Deprecated
        public static final String KIND = "kind";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final int MOBILE_EMAIL_TYPE_INDEX = 2;
        @Deprecated
        public static final String MOBILE_EMAIL_TYPE_NAME = "_AUTO_CELL";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_HOME = 1;
        @Deprecated
        public static final int TYPE_OTHER = 3;
        @Deprecated
        public static final int TYPE_WORK = 2;
    }

    @Deprecated
    public interface PeopleColumns {
        @Deprecated
        public static final String CUSTOM_RINGTONE = "custom_ringtone";
        @Deprecated
        public static final String DISPLAY_NAME = "display_name";
        @Deprecated
        public static final String LAST_TIME_CONTACTED = "last_time_contacted";
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String NOTES = "notes";
        @Deprecated
        public static final String PHONETIC_NAME = "phonetic_name";
        @Deprecated
        public static final String PHOTO_VERSION = "photo_version";
        @Deprecated
        public static final String SEND_TO_VOICEMAIL = "send_to_voicemail";
        @Deprecated
        public static final String SORT_STRING = "sort_string";
        @Deprecated
        public static final String STARRED = "starred";
        @Deprecated
        public static final String TIMES_CONTACTED = "times_contacted";
    }

    @Deprecated
    public static final class ContactMethods implements BaseColumns, ContactMethodsColumns, PeopleColumns {
        @Deprecated
        public static final String CONTENT_EMAIL_ITEM_TYPE = "vnd.android.cursor.item/email";
        @Deprecated
        public static final String CONTENT_EMAIL_TYPE = "vnd.android.cursor.dir/email";
        @Deprecated
        public static final Uri CONTENT_EMAIL_URI = null;
        @Deprecated
        public static final String CONTENT_IM_ITEM_TYPE = "vnd.android.cursor.item/jabber-im";
        @Deprecated
        public static final String CONTENT_POSTAL_ITEM_TYPE = "vnd.android.cursor.item/postal-address";
        @Deprecated
        public static final String CONTENT_POSTAL_TYPE = "vnd.android.cursor.dir/postal-address";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact-methods";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String POSTAL_LOCATION_LATITUDE = "data";
        @Deprecated
        public static final String POSTAL_LOCATION_LONGITUDE = "aux_data";
        @Deprecated
        public static final int PROTOCOL_AIM = 0;
        @Deprecated
        public static final int PROTOCOL_GOOGLE_TALK = 5;
        @Deprecated
        public static final int PROTOCOL_ICQ = 6;
        @Deprecated
        public static final int PROTOCOL_JABBER = 7;
        @Deprecated
        public static final int PROTOCOL_MSN = 1;
        @Deprecated
        public static final int PROTOCOL_QQ = 4;
        @Deprecated
        public static final int PROTOCOL_SKYPE = 3;
        @Deprecated
        public static final int PROTOCOL_YAHOO = 2;

        interface ProviderNames {
            public static final String AIM = "AIM";
            public static final String GTALK = "GTalk";
            public static final String ICQ = "ICQ";
            public static final String JABBER = "JABBER";
            public static final String MSN = "MSN";
            public static final String QQ = "QQ";
            public static final String SKYPE = "SKYPE";
            public static final String XMPP = "XMPP";
            public static final String YAHOO = "Yahoo";
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.ContactMethods.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.ContactMethods.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.ContactMethods.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private ContactMethods() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.ContactMethods.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.decodeImProtocol(java.lang.String):java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static java.lang.Object decodeImProtocol(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.decodeImProtocol(java.lang.String):java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.decodeImProtocol(java.lang.String):java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.encodeCustomImProtocol(java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static java.lang.String encodeCustomImProtocol(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.encodeCustomImProtocol(java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.encodeCustomImProtocol(java.lang.String):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.encodePredefinedImProtocol(int):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static java.lang.String encodePredefinedImProtocol(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.encodePredefinedImProtocol(int):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.encodePredefinedImProtocol(int):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.getDisplayLabel(android.content.Context, int, int, java.lang.CharSequence):java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static final java.lang.CharSequence getDisplayLabel(android.content.Context r1, int r2, int r3, java.lang.CharSequence r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.getDisplayLabel(android.content.Context, int, int, java.lang.CharSequence):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.getDisplayLabel(android.content.Context, int, int, java.lang.CharSequence):java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.addPostalLocation(android.content.Context, long, double, double):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public void addPostalLocation(android.content.Context r1, long r2, double r4, double r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.ContactMethods.addPostalLocation(android.content.Context, long, double, double):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.ContactMethods.addPostalLocation(android.content.Context, long, double, double):void");
        }

        @Deprecated
        public static String lookupProviderNameFromId(int protocol) {
            switch (protocol) {
                case 0:
                    return ProviderNames.AIM;
                case 1:
                    return ProviderNames.MSN;
                case 2:
                    return ProviderNames.YAHOO;
                case 3:
                    return ProviderNames.SKYPE;
                case 4:
                    return ProviderNames.QQ;
                case 5:
                    return ProviderNames.GTALK;
                case 6:
                    return ProviderNames.ICQ;
                case 7:
                    return ProviderNames.JABBER;
                default:
                    return null;
            }
        }
    }

    @Deprecated
    public interface ExtensionsColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String VALUE = "value";
    }

    @Deprecated
    public static final class Extensions implements BaseColumns, ExtensionsColumns {
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_extensions";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_extensions";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "person, name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Extensions.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Extensions.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Extensions.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Extensions.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Extensions() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Extensions.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Extensions.<init>():void");
        }
    }

    @Deprecated
    public interface GroupsColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String NOTES = "notes";
        @Deprecated
        public static final String SHOULD_SYNC = "should_sync";
        @Deprecated
        public static final String SYSTEM_ID = "system_id";
    }

    @Deprecated
    public static final class GroupMembership implements BaseColumns, GroupsColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "groupmembership";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contactsgroupmembership";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contactsgroupmembership";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "group_id ASC";
        @Deprecated
        public static final String GROUP_ID = "group_id";
        @Deprecated
        public static final String GROUP_SYNC_ACCOUNT = "group_sync_account";
        @Deprecated
        public static final String GROUP_SYNC_ACCOUNT_TYPE = "group_sync_account_type";
        @Deprecated
        public static final String GROUP_SYNC_ID = "group_sync_id";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final Uri RAW_CONTENT_URI = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.GroupMembership.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.GroupMembership.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.GroupMembership.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.GroupMembership.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private GroupMembership() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.GroupMembership.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.GroupMembership.<init>():void");
        }
    }

    @Deprecated
    public static final class Groups implements BaseColumns, GroupsColumns {
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contactsgroup";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contactsgroup";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final Uri DELETED_CONTENT_URI = null;
        @Deprecated
        public static final String GROUP_ANDROID_STARRED = "Starred in Android";
        @Deprecated
        public static final String GROUP_MY_CONTACTS = "Contacts";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Groups.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Groups.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Groups.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Groups.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Groups() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Groups.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Groups.<init>():void");
        }
    }

    @Deprecated
    public static final class Intents {
        @Deprecated
        public static final String ATTACH_IMAGE = "com.android.contacts.action.ATTACH_IMAGE";
        @Deprecated
        public static final String EXTRA_CREATE_DESCRIPTION = "com.android.contacts.action.CREATE_DESCRIPTION";
        @Deprecated
        public static final String EXTRA_FORCE_CREATE = "com.android.contacts.action.FORCE_CREATE";
        @Deprecated
        public static final String EXTRA_TARGET_RECT = "target_rect";
        @Deprecated
        public static final String SEARCH_SUGGESTION_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CLICKED";
        @Deprecated
        public static final String SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED";
        @Deprecated
        public static final String SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED = "android.provider.Contacts.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED";
        @Deprecated
        public static final String SHOW_OR_CREATE_CONTACT = "com.android.contacts.action.SHOW_OR_CREATE_CONTACT";

        @Deprecated
        public static final class Insert {
            @Deprecated
            public static final String ACTION = "android.intent.action.INSERT";
            @Deprecated
            public static final String COMPANY = "company";
            @Deprecated
            public static final String EMAIL = "email";
            @Deprecated
            public static final String EMAIL_ISPRIMARY = "email_isprimary";
            @Deprecated
            public static final String EMAIL_TYPE = "email_type";
            @Deprecated
            public static final String FULL_MODE = "full_mode";
            @Deprecated
            public static final String IM_HANDLE = "im_handle";
            @Deprecated
            public static final String IM_ISPRIMARY = "im_isprimary";
            @Deprecated
            public static final String IM_PROTOCOL = "im_protocol";
            @Deprecated
            public static final String JOB_TITLE = "job_title";
            @Deprecated
            public static final String NAME = "name";
            @Deprecated
            public static final String NOTES = "notes";
            @Deprecated
            public static final String PHONE = "phone";
            @Deprecated
            public static final String PHONETIC_NAME = "phonetic_name";
            @Deprecated
            public static final String PHONE_ISPRIMARY = "phone_isprimary";
            @Deprecated
            public static final String PHONE_TYPE = "phone_type";
            @Deprecated
            public static final String POSTAL = "postal";
            @Deprecated
            public static final String POSTAL_ISPRIMARY = "postal_isprimary";
            @Deprecated
            public static final String POSTAL_TYPE = "postal_type";
            @Deprecated
            public static final String SECONDARY_EMAIL = "secondary_email";
            @Deprecated
            public static final String SECONDARY_EMAIL_TYPE = "secondary_email_type";
            @Deprecated
            public static final String SECONDARY_PHONE = "secondary_phone";
            @Deprecated
            public static final String SECONDARY_PHONE_TYPE = "secondary_phone_type";
            @Deprecated
            public static final String TERTIARY_EMAIL = "tertiary_email";
            @Deprecated
            public static final String TERTIARY_EMAIL_TYPE = "tertiary_email_type";
            @Deprecated
            public static final String TERTIARY_PHONE = "tertiary_phone";
            @Deprecated
            public static final String TERTIARY_PHONE_TYPE = "tertiary_phone_type";

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Intents.Insert.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            @java.lang.Deprecated
            public Insert() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Intents.Insert.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Intents.Insert.<init>():void");
            }
        }

        @Deprecated
        public static final class UI {
            @Deprecated
            public static final String FILTER_CONTACTS_ACTION = "com.android.contacts.action.FILTER_CONTACTS";
            @Deprecated
            public static final String FILTER_TEXT_EXTRA_KEY = "com.android.contacts.extra.FILTER_TEXT";
            @Deprecated
            public static final String GROUP_NAME_EXTRA_KEY = "com.android.contacts.extra.GROUP";
            @Deprecated
            public static final String LIST_ALL_CONTACTS_ACTION = "com.android.contacts.action.LIST_ALL_CONTACTS";
            @Deprecated
            public static final String LIST_CONTACTS_WITH_PHONES_ACTION = "com.android.contacts.action.LIST_CONTACTS_WITH_PHONES";
            @Deprecated
            public static final String LIST_DEFAULT = "com.android.contacts.action.LIST_DEFAULT";
            @Deprecated
            public static final String LIST_FREQUENT_ACTION = "com.android.contacts.action.LIST_FREQUENT";
            @Deprecated
            public static final String LIST_GROUP_ACTION = "com.android.contacts.action.LIST_GROUP";
            @Deprecated
            public static final String LIST_STARRED_ACTION = "com.android.contacts.action.LIST_STARRED";
            @Deprecated
            public static final String LIST_STREQUENT_ACTION = "com.android.contacts.action.LIST_STREQUENT";
            @Deprecated
            public static final String TITLE_EXTRA_KEY = "com.android.contacts.extra.TITLE_EXTRA";

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Intents.UI.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            @java.lang.Deprecated
            public UI() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Intents.UI.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Intents.UI.<init>():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Intents.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        @java.lang.Deprecated
        public Intents() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Intents.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Intents.<init>():void");
        }
    }

    @Deprecated
    public interface OrganizationColumns {
        @Deprecated
        public static final String COMPANY = "company";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String TITLE = "title";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_OTHER = 2;
        @Deprecated
        public static final int TYPE_WORK = 1;
    }

    @Deprecated
    public static final class Organizations implements BaseColumns, OrganizationColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "organizations";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "company, title, isprimary ASC";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Organizations.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Organizations.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Organizations.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Organizations.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Organizations() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Organizations.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Organizations.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Organizations.getDisplayLabel(android.content.Context, int, java.lang.CharSequence):java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static final java.lang.CharSequence getDisplayLabel(android.content.Context r1, int r2, java.lang.CharSequence r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Organizations.getDisplayLabel(android.content.Context, int, java.lang.CharSequence):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Organizations.getDisplayLabel(android.content.Context, int, java.lang.CharSequence):java.lang.CharSequence");
        }
    }

    @Deprecated
    public interface PhonesColumns {
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String NUMBER = "number";
        @Deprecated
        public static final String NUMBER_KEY = "number_key";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_FAX_HOME = 5;
        @Deprecated
        public static final int TYPE_FAX_WORK = 4;
        @Deprecated
        public static final int TYPE_HOME = 1;
        @Deprecated
        public static final int TYPE_MOBILE = 2;
        @Deprecated
        public static final int TYPE_OTHER = 7;
        @Deprecated
        public static final int TYPE_PAGER = 6;
        @Deprecated
        public static final int TYPE_WORK = 3;
    }

    @Deprecated
    public interface PresenceColumns {
        public static final int AVAILABLE = 5;
        public static final int AWAY = 2;
        public static final int DO_NOT_DISTURB = 4;
        public static final int IDLE = 3;
        @Deprecated
        public static final String IM_ACCOUNT = "im_account";
        @Deprecated
        public static final String IM_HANDLE = "im_handle";
        @Deprecated
        public static final String IM_PROTOCOL = "im_protocol";
        public static final int INVISIBLE = 1;
        public static final int OFFLINE = 0;
        public static final String PRESENCE_CUSTOM_STATUS = "status";
        public static final String PRESENCE_STATUS = "mode";
        public static final String PRIORITY = "priority";
    }

    @Deprecated
    public static final class People implements BaseColumns, PeopleColumns, PhonesColumns, PresenceColumns {
        @Deprecated
        public static final Uri CONTENT_FILTER_URI = null;
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/person";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/person";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final Uri DELETED_CONTENT_URI = null;
        private static final String[] GROUPS_PROJECTION = null;
        @Deprecated
        public static final String PRIMARY_EMAIL_ID = "primary_email";
        @Deprecated
        public static final String PRIMARY_ORGANIZATION_ID = "primary_organization";
        @Deprecated
        public static final String PRIMARY_PHONE_ID = "primary_phone";
        @Deprecated
        public static final Uri WITH_EMAIL_OR_IM_FILTER_URI = null;

        @Deprecated
        public static final class ContactMethods implements BaseColumns, ContactMethodsColumns, PeopleColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "contact_methods";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "data ASC";

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.ContactMethods.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private ContactMethods() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.ContactMethods.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.ContactMethods.<init>():void");
            }
        }

        @Deprecated
        public static class Extensions implements BaseColumns, ExtensionsColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "extensions";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "name ASC";
            @Deprecated
            public static final String PERSON_ID = "person";

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.Extensions.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private Extensions() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.Extensions.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.Extensions.<init>():void");
            }
        }

        @Deprecated
        public static final class Phones implements BaseColumns, PhonesColumns, PeopleColumns {
            @Deprecated
            public static final String CONTENT_DIRECTORY = "phones";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "number ASC";

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.Phones.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private Phones() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.Phones.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.Phones.<init>():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private People() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.People.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.addToGroup(android.content.ContentResolver, long, long):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static android.net.Uri addToGroup(android.content.ContentResolver r1, long r2, long r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.addToGroup(android.content.ContentResolver, long, long):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.addToGroup(android.content.ContentResolver, long, long):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.addToGroup(android.content.ContentResolver, long, java.lang.String):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static android.net.Uri addToGroup(android.content.ContentResolver r1, long r2, java.lang.String r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.addToGroup(android.content.ContentResolver, long, java.lang.String):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.addToGroup(android.content.ContentResolver, long, java.lang.String):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.createPersonInMyContactsGroup(android.content.ContentResolver, android.content.ContentValues):android.net.Uri, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static android.net.Uri createPersonInMyContactsGroup(android.content.ContentResolver r1, android.content.ContentValues r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.createPersonInMyContactsGroup(android.content.ContentResolver, android.content.ContentValues):android.net.Uri, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.createPersonInMyContactsGroup(android.content.ContentResolver, android.content.ContentValues):android.net.Uri");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.loadContactPhoto(android.content.Context, android.net.Uri, int, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static android.graphics.Bitmap loadContactPhoto(android.content.Context r1, android.net.Uri r2, int r3, android.graphics.BitmapFactory.Options r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.loadContactPhoto(android.content.Context, android.net.Uri, int, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.loadContactPhoto(android.content.Context, android.net.Uri, int, android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.loadPlaceholderPhoto(int, android.content.Context, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private static android.graphics.Bitmap loadPlaceholderPhoto(int r1, android.content.Context r2, android.graphics.BitmapFactory.Options r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.loadPlaceholderPhoto(int, android.content.Context, android.graphics.BitmapFactory$Options):android.graphics.Bitmap, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.loadPlaceholderPhoto(int, android.content.Context, android.graphics.BitmapFactory$Options):android.graphics.Bitmap");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.markAsContacted(android.content.ContentResolver, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static void markAsContacted(android.content.ContentResolver r1, long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.markAsContacted(android.content.ContentResolver, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.markAsContacted(android.content.ContentResolver, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.openContactPhotoInputStream(android.content.ContentResolver, android.net.Uri):java.io.InputStream, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static java.io.InputStream openContactPhotoInputStream(android.content.ContentResolver r1, android.net.Uri r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.openContactPhotoInputStream(android.content.ContentResolver, android.net.Uri):java.io.InputStream, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.openContactPhotoInputStream(android.content.ContentResolver, android.net.Uri):java.io.InputStream");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.queryGroups(android.content.ContentResolver, long):android.database.Cursor, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static android.database.Cursor queryGroups(android.content.ContentResolver r1, long r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.queryGroups(android.content.ContentResolver, long):android.database.Cursor, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.queryGroups(android.content.ContentResolver, long):android.database.Cursor");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.setPhotoData(android.content.ContentResolver, android.net.Uri, byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static void setPhotoData(android.content.ContentResolver r1, android.net.Uri r2, byte[] r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.People.setPhotoData(android.content.ContentResolver, android.net.Uri, byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.setPhotoData(android.content.ContentResolver, android.net.Uri, byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.tryGetMyContactsGroupId(android.content.ContentResolver):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static long tryGetMyContactsGroupId(android.content.ContentResolver r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.People.tryGetMyContactsGroupId(android.content.ContentResolver):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.People.tryGetMyContactsGroupId(android.content.ContentResolver):long");
        }

        @Deprecated
        public static Uri addToMyContactsGroup(ContentResolver resolver, long personId) {
            long groupId = tryGetMyContactsGroupId(resolver);
            if (groupId != 0) {
                return addToGroup(resolver, personId, groupId);
            }
            throw new IllegalStateException("Failed to find the My Contacts group");
        }
    }

    @Deprecated
    public static final class Phones implements BaseColumns, PhonesColumns, PeopleColumns {
        @Deprecated
        public static final Uri CONTENT_FILTER_URL = null;
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Phones.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Phones.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Phones.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Phones.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Phones() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Phones.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Phones.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Phones.getDisplayLabel(android.content.Context, int, java.lang.CharSequence, java.lang.CharSequence[]):java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static final java.lang.CharSequence getDisplayLabel(android.content.Context r1, int r2, java.lang.CharSequence r3, java.lang.CharSequence[] r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Phones.getDisplayLabel(android.content.Context, int, java.lang.CharSequence, java.lang.CharSequence[]):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Phones.getDisplayLabel(android.content.Context, int, java.lang.CharSequence, java.lang.CharSequence[]):java.lang.CharSequence");
        }

        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type, CharSequence label) {
            return getDisplayLabel(context, type, label, null);
        }
    }

    @Deprecated
    public interface PhotosColumns {
        @Deprecated
        public static final String DATA = "data";
        @Deprecated
        public static final String DOWNLOAD_REQUIRED = "download_required";
        @Deprecated
        public static final String EXISTS_ON_SERVER = "exists_on_server";
        @Deprecated
        public static final String LOCAL_VERSION = "local_version";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String SYNC_ERROR = "sync_error";
    }

    @Deprecated
    public static final class Photos implements BaseColumns, PhotosColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "photo";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "person ASC";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Photos.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Photos.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Photos.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Photos.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Photos() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Photos.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Photos.<init>():void");
        }
    }

    @Deprecated
    public static final class Presence implements BaseColumns, PresenceColumns, PeopleColumns {
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String PERSON_ID = "person";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Presence.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Presence.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Presence.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Presence.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public Presence() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Presence.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Presence.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Presence.setPresenceIcon(android.widget.ImageView, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static final void setPresenceIcon(android.widget.ImageView r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Presence.setPresenceIcon(android.widget.ImageView, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Presence.setPresenceIcon(android.widget.ImageView, int):void");
        }

        @Deprecated
        public static final int getPresenceIconResourceId(int status) {
            switch (status) {
                case 1:
                    return R.drawable.presence_invisible;
                case 2:
                case 3:
                    return R.drawable.presence_away;
                case 4:
                    return R.drawable.presence_busy;
                case 5:
                    return R.drawable.presence_online;
                default:
                    return R.drawable.presence_offline;
            }
        }
    }

    @Deprecated
    public interface SettingsColumns {
        @Deprecated
        public static final String KEY = "key";
        @Deprecated
        public static final String VALUE = "value";
        @Deprecated
        public static final String _SYNC_ACCOUNT = "_sync_account";
        @Deprecated
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
    }

    @Deprecated
    public static final class Settings implements BaseColumns, SettingsColumns {
        @Deprecated
        public static final String CONTENT_DIRECTORY = "settings";
        @Deprecated
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "key ASC";
        @Deprecated
        public static final String SYNC_EVERYTHING = "syncEverything";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Settings.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Settings.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Settings.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Settings.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private Settings() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.Settings.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Settings.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.Settings.getSetting(android.content.ContentResolver, java.lang.String, java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static java.lang.String getSetting(android.content.ContentResolver r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.provider.Contacts.Settings.getSetting(android.content.ContentResolver, java.lang.String, java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Settings.getSetting(android.content.ContentResolver, java.lang.String, java.lang.String):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Settings.setSetting(android.content.ContentResolver, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        @java.lang.Deprecated
        public static void setSetting(android.content.ContentResolver r1, java.lang.String r2, java.lang.String r3, java.lang.String r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.provider.Contacts.Settings.setSetting(android.content.ContentResolver, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.Settings.setSetting(android.content.ContentResolver, java.lang.String, java.lang.String, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.Contacts.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.Contacts.<clinit>():void");
    }

    private Contacts() {
    }
}

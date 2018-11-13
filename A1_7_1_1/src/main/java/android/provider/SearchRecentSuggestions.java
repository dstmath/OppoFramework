package android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.Semaphore;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SearchRecentSuggestions {
    private static final String LOG_TAG = "SearchSuggestions";
    private static final int MAX_HISTORY_COUNT = 250;
    public static final String[] QUERIES_PROJECTION_1LINE = null;
    public static final String[] QUERIES_PROJECTION_2LINE = null;
    public static final int QUERIES_PROJECTION_DATE_INDEX = 1;
    public static final int QUERIES_PROJECTION_DISPLAY1_INDEX = 3;
    public static final int QUERIES_PROJECTION_DISPLAY2_INDEX = 4;
    public static final int QUERIES_PROJECTION_QUERY_INDEX = 2;
    private static final Semaphore sWritesInProgress = null;
    private final String mAuthority;
    private final Context mContext;
    private final Uri mSuggestionsUri;
    private final boolean mTwoLineDisplay;

    private static class SuggestionColumns implements BaseColumns {
        public static final String DATE = "date";
        public static final String DISPLAY1 = "display1";
        public static final String DISPLAY2 = "display2";
        public static final String QUERY = "query";

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.SearchRecentSuggestions.SuggestionColumns.<init>():void, dex: 
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
        private SuggestionColumns() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.SearchRecentSuggestions.SuggestionColumns.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.SearchRecentSuggestions.SuggestionColumns.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.provider.SearchRecentSuggestions.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.provider.SearchRecentSuggestions.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.SearchRecentSuggestions.<clinit>():void");
    }

    public SearchRecentSuggestions(Context context, String authority, int mode) {
        boolean z = false;
        if (TextUtils.isEmpty(authority) || (mode & 1) == 0) {
            throw new IllegalArgumentException();
        }
        if ((mode & 2) != 0) {
            z = true;
        }
        this.mTwoLineDisplay = z;
        this.mContext = context;
        this.mAuthority = new String(authority);
        this.mSuggestionsUri = Uri.parse("content://" + this.mAuthority + "/suggestions");
    }

    public void saveRecentQuery(final String queryString, final String line2) {
        if (!TextUtils.isEmpty(queryString)) {
            if (this.mTwoLineDisplay || TextUtils.isEmpty(line2)) {
                new Thread("saveRecentQuery") {
                    public void run() {
                        SearchRecentSuggestions.this.saveRecentQueryBlocking(queryString, line2);
                        SearchRecentSuggestions.sWritesInProgress.release();
                    }
                }.start();
                return;
            }
            throw new IllegalArgumentException();
        }
    }

    void waitForSave() {
        do {
            sWritesInProgress.acquireUninterruptibly();
        } while (sWritesInProgress.availablePermits() > 0);
    }

    private void saveRecentQueryBlocking(String queryString, String line2) {
        ContentResolver cr = this.mContext.getContentResolver();
        long now = System.currentTimeMillis();
        try {
            ContentValues values = new ContentValues();
            values.put(SuggestionColumns.DISPLAY1, queryString);
            if (this.mTwoLineDisplay) {
                values.put(SuggestionColumns.DISPLAY2, line2);
            }
            values.put("query", queryString);
            values.put("date", Long.valueOf(now));
            cr.insert(this.mSuggestionsUri, values);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "saveRecentQuery", e);
        }
        truncateHistory(cr, 250);
    }

    public void clearHistory() {
        truncateHistory(this.mContext.getContentResolver(), 0);
    }

    protected void truncateHistory(ContentResolver cr, int maxEntries) {
        if (maxEntries < 0) {
            throw new IllegalArgumentException();
        }
        String selection = null;
        if (maxEntries > 0) {
            try {
                selection = "_id IN (SELECT _id FROM suggestions ORDER BY date DESC LIMIT -1 OFFSET " + String.valueOf(maxEntries) + ")";
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "truncateHistory", e);
                return;
            }
        }
        cr.delete(this.mSuggestionsUri, selection, null);
    }
}

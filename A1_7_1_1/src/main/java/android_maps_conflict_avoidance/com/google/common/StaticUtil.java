package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

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
public final class StaticUtil {
    private static boolean IS_REGISTER_OUT_OF_MEMORY_HANDLER;
    private static byte[] emergencyMemory;
    private static final Vector outOfMemoryHandlers = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.StaticUtil.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.StaticUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.StaticUtil.<clinit>():void");
    }

    private static void allocateEmergencyMemory() {
        if (emergencyMemory != null) {
            System.gc();
            try {
                emergencyMemory = new byte[4096];
            } catch (OutOfMemoryError e) {
            }
        }
    }

    private static PersistentStore getPersistentStore() {
        return Config.getInstance().getPersistentStore();
    }

    public static void savePreferenceAsString(String preference, String value) {
        savePreferenceAsObject(preference, value);
    }

    private static void savePreferenceAsObject(String preference, Object object) {
        PersistentStore store = Config.getInstance().getPersistentStore();
        if (object == null) {
            store.setPreference(preference, null);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput dataOut = new DataOutputStream(baos);
        try {
            if (object instanceof Boolean) {
                dataOut.writeBoolean(((Boolean) object).booleanValue());
            } else if (object instanceof String) {
                dataOut.writeUTF((String) object);
            } else if (object instanceof Integer) {
                dataOut.writeInt(((Integer) object).intValue());
            } else if (object instanceof Long) {
                dataOut.writeLong(((Long) object).longValue());
            } else {
                throw new IllegalArgumentException("Bad type: " + object.getClass() + " for " + preference);
            }
            store.setPreference(preference, baos.toByteArray());
        } catch (IOException e) {
            Log.logQuietThrowable("Writing: " + preference, e);
        }
    }

    public static String readPreferenceAsString(String preference) {
        return (String) readPreferenceAsObject(preference, 3);
    }

    private static Object readPreferenceAsObject(String preference, int type) {
        DataInput input = readPreferenceAsDataInput(preference);
        if (input == null) {
            return null;
        }
        switch (type) {
            case LayoutParams.MODE_MAP /*0*/:
                return new Boolean(input.readBoolean());
            case 1:
                return new Integer(input.readInt());
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                return new Long(input.readLong());
            case LayoutParams.LEFT /*3*/:
                return input.readUTF();
            default:
                try {
                    throw new RuntimeException("Bad class: " + type + " for " + preference);
                } catch (IOException e) {
                    return null;
                }
        }
    }

    public static DataInput readPreferenceAsDataInput(String preference) {
        byte[] data = getPersistentStore().readPreference(preference);
        if (data == null) {
            return null;
        }
        return new DataInputStream(new ByteArrayInputStream(data));
    }

    public static void registerOutOfMemoryHandler(OutOfMemoryHandler handler) {
        if (IS_REGISTER_OUT_OF_MEMORY_HANDLER) {
            outOfMemoryHandlers.addElement(handler);
        }
    }

    public static void removeOutOfMemoryHandler(OutOfMemoryHandler handler) {
        outOfMemoryHandlers.removeElement(handler);
    }

    public static void handleOutOfMemory() {
        handleOutOfMemory(false);
    }

    private static void handleOutOfMemory(boolean warning) {
        emergencyMemory = null;
        System.err.println(warning ? "LowOnMemory" : "OutOfMemory");
        for (int i = 0; i < outOfMemoryHandlers.size(); i++) {
            ((OutOfMemoryHandler) outOfMemoryHandlers.elementAt(i)).handleOutOfMemory(warning);
        }
    }
}

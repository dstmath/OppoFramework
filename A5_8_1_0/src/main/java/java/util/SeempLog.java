package java.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class SeempLog {
    private static Method seemp_record_method = null;
    private static boolean seemp_record_method_looked_up = false;

    private SeempLog() {
    }

    public static int record_str(int api, String msg) {
        if (seemp_record_method == null) {
            if (!seemp_record_method_looked_up) {
                try {
                    Class c = Class.forName("android.util.SeempLog");
                    if (c != null) {
                        seemp_record_method = c.getDeclaredMethod("record_str", Integer.TYPE, String.class);
                    }
                } catch (ClassNotFoundException e) {
                    seemp_record_method = null;
                } catch (NoSuchMethodException e2) {
                    seemp_record_method = null;
                }
            }
            seemp_record_method_looked_up = true;
        }
        if (seemp_record_method == null) {
            return 0;
        }
        try {
            return ((Integer) seemp_record_method.invoke(null, Integer.valueOf(api), msg)).intValue();
        } catch (IllegalAccessException e3) {
            return 0;
        } catch (InvocationTargetException e4) {
            return 0;
        }
    }
}

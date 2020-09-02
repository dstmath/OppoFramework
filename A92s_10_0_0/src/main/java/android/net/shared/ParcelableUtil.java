package android.net.shared;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public final class ParcelableUtil {
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.reflect.Array.newInstance(java.lang.Class<?>, int):java.lang.Object throws java.lang.NegativeArraySizeException}
     arg types: [java.lang.Class<ParcelableType>, int]
     candidates:
      ClspMth{java.lang.reflect.Array.newInstance(java.lang.Class<?>, int[]):java.lang.Object VARARG throws java.lang.IllegalArgumentException, java.lang.NegativeArraySizeException}
      ClspMth{java.lang.reflect.Array.newInstance(java.lang.Class<?>, int):java.lang.Object throws java.lang.NegativeArraySizeException} */
    public static <ParcelableType, BaseType> ParcelableType[] toParcelableArray(Collection<BaseType> base, Function<BaseType, ParcelableType> conv, Class<ParcelableType> parcelClass) {
        ParcelableType[] out = (Object[]) Array.newInstance((Class<?>) parcelClass, base.size());
        int i = 0;
        for (BaseType b : base) {
            out[i] = conv.apply(b);
            i++;
        }
        return out;
    }

    public static <ParcelableType, BaseType> ArrayList<BaseType> fromParcelableArray(ParcelableType[] parceled, Function<ParcelableType, BaseType> conv) {
        ArrayList<BaseType> out = new ArrayList<>(parceled.length);
        for (ParcelableType t : parceled) {
            out.add(conv.apply(t));
        }
        return out;
    }
}

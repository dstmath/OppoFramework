package com.android.internal.telephony.common;

import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoCommonFactory {
    boolean isValid(int i);

    default <T extends IOppoCommonFeature> T getFeature(T def, Object... vars) {
        verityParams(def);
        return def;
    }

    default <T extends IOppoCommonFeature> void verityParams(T def) {
        if (def == null) {
            throw new IllegalArgumentException("def can not be null");
        } else if (def.index() == OppoFeatureList.OppoIndex.End) {
            throw new IllegalArgumentException(((Object) def) + "must override index() method");
        }
    }

    default void verityParamsType(String key, Object[] vars, int num, Class... types) {
        if (vars == null || types == null || vars.length != num || types.length != num) {
            throw new IllegalArgumentException(key + " need +" + num + " params");
        }
        int i = 0;
        while (i < num) {
            if (types[i].isInstance(vars[i])) {
                i++;
            } else {
                throw new IllegalArgumentException(types[i].getName() + " is not instance " + vars[i]);
            }
        }
    }
}

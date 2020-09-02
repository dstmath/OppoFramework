package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;

public class ConfigurationBoundResourceCache<T> extends ThemedResourceCache<ConstantState<T>> {
    @Override // android.content.res.ThemedResourceCache
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ void onConfigurationChange(int i) {
        super.onConfigurationChange(i);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(android.content.res.ConstantState, int):boolean
     arg types: [android.content.res.ConstantState<T>, int]
     candidates:
      android.content.res.ConfigurationBoundResourceCache.shouldInvalidateEntry(java.lang.Object, int):boolean
      MutableMD:(java.lang.Object, int):boolean
      MutableMD:(android.content.res.ConstantState, int):boolean */
    @Override // android.content.res.ThemedResourceCache
    public /* bridge */ /* synthetic */ boolean shouldInvalidateEntry(Object obj, int i) {
        return shouldInvalidateEntry((ConstantState) ((ConstantState) obj), i);
    }

    public T getInstance(long key, Resources resources, Resources.Theme theme) {
        ConstantState<T> entry = get(key, theme);
        if (entry != null) {
            return entry.newInstance(resources, theme);
        }
        return null;
    }

    public boolean shouldInvalidateEntry(ConstantState<T> entry, int configChanges) {
        return Configuration.needNewResources(configChanges, entry.getChangingConfigurations());
    }
}

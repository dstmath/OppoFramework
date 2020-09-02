package cn.teddymobile.free.anteater.rule.attribute.intent;

import android.util.Pair;
import cn.teddymobile.free.anteater.logger.Logger;
import cn.teddymobile.free.anteater.rule.utils.RegularExpressionUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Attribute {
    private static final String ALIAS_CAPTURE_PATTERN = "<alias_(.*?)>";
    private static final String ALIAS_FORMAT = "<alias_%s>";
    private static final String JSON_FIELD_ALIAS = "alias";
    private static final String JSON_FIELD_ATTRIBUTE_PATTERN = "attribute_pattern";
    private static final String TAG = Attribute.class.getSimpleName();
    private final String mAlias;
    private final String mAttributePattern;

    public Attribute(JSONObject attributeObject) throws JSONException {
        this.mAttributePattern = attributeObject.getString(JSON_FIELD_ATTRIBUTE_PATTERN);
        this.mAlias = attributeObject.getString("alias");
    }

    public String toString() {
        return "AttributePattern = " + this.mAttributePattern + "\nAlias = " + this.mAlias;
    }

    public Pair<String, String> getResult(Map<String, String> extraMap) {
        Pair<String, String> result = null;
        List<String> aliasList = RegularExpressionUtils.captureAll(this.mAttributePattern, ALIAS_CAPTURE_PATTERN);
        String attribute = this.mAttributePattern;
        boolean missingAttribute = false;
        Iterator<String> it = aliasList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String alias = it.next();
            if (!extraMap.containsKey(alias)) {
                Logger.w(TAG, "Missing attribute.");
                String str = TAG;
                Logger.w(str, "Alias = " + alias);
                missingAttribute = true;
                break;
            }
            String value = extraMap.get(alias);
            if (value != null) {
                attribute = attribute.replaceAll(String.format(ALIAS_FORMAT, alias), value);
            }
        }
        if (!missingAttribute) {
            result = new Pair<>(this.mAlias, attribute);
        }
        String str2 = TAG;
        Logger.i(str2, getClass().getSimpleName() + " Result = " + result);
        return result;
    }
}

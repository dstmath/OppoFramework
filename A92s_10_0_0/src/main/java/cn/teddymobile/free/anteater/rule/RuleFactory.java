package cn.teddymobile.free.anteater.rule;

import cn.teddymobile.free.anteater.rule.attribute.AttributeRule;
import cn.teddymobile.free.anteater.rule.attribute.intent.IntentRule;
import cn.teddymobile.free.anteater.rule.attribute.reflection.ReflectionRule;
import cn.teddymobile.free.anteater.rule.attribute.webview.WebViewRule;
import cn.teddymobile.free.anteater.rule.html.HtmlRule;
import cn.teddymobile.free.anteater.rule.html.javascript.JavaScriptRule;
import cn.teddymobile.free.anteater.rule.trigger.TriggerRule;
import cn.teddymobile.free.anteater.rule.trigger.context.ContextRule;
import cn.teddymobile.free.anteater.rule.trigger.hierarchy.ViewHierarchyRule;
import cn.teddymobile.free.anteater.rule.trigger.resource.ResourceNameRule;
import cn.teddymobile.free.anteater.rule.trigger.text.TextRule;

public class RuleFactory {
    private static final String ATTRIBUTE_METHOD_INTENT = "intent";
    private static final String ATTRIBUTE_METHOD_REFLECTION = "reflection";
    private static final String ATTRIBUTE_METHOD_WEB_VIEW = "web_view";
    private static final String TRIGGER_METHOD_CONTEXT = "context";
    private static final String TRIGGER_METHOD_RESOURCE_NAME = "resource_name";
    private static final String TRIGGER_METHOD_TEXT = "text";
    private static final String TRIGGER_METHOD_VIEW_HIERARCHY = "view_hierarchy";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static TriggerRule createTriggerRule(String triggerMethod) throws IllegalArgumentException {
        char c;
        switch (triggerMethod.hashCode()) {
            case 3556653:
                if (triggerMethod.equals("text")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 951530927:
                if (triggerMethod.equals(TRIGGER_METHOD_CONTEXT)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 979421212:
                if (triggerMethod.equals(TRIGGER_METHOD_RESOURCE_NAME)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1420918043:
                if (triggerMethod.equals(TRIGGER_METHOD_VIEW_HIERARCHY)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return new TextRule();
        }
        if (c == 1) {
            return new ResourceNameRule();
        }
        if (c == 2) {
            return new ViewHierarchyRule();
        }
        if (c == 3) {
            return new ContextRule();
        }
        throw new IllegalArgumentException("Trigger method " + triggerMethod + " is undefined");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0065  */
    public static AttributeRule createAttributeRule(String attributeMethod) throws IllegalArgumentException {
        char c;
        int hashCode = attributeMethod.hashCode();
        if (hashCode != -1366299605) {
            if (hashCode != -1183762788) {
                if (hashCode == -718398288 && attributeMethod.equals(ATTRIBUTE_METHOD_WEB_VIEW)) {
                    c = 1;
                    if (c != 0) {
                        return new IntentRule();
                    }
                    if (c == 1) {
                        return new WebViewRule();
                    }
                    if (c == 2) {
                        return new ReflectionRule();
                    }
                    throw new IllegalArgumentException("Attribute method " + attributeMethod + " is undefined");
                }
            } else if (attributeMethod.equals("intent")) {
                c = 0;
                if (c != 0) {
                }
            }
        } else if (attributeMethod.equals(ATTRIBUTE_METHOD_REFLECTION)) {
            c = 2;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    public static HtmlRule createDecodeRule() {
        return new JavaScriptRule();
    }
}

package cn.teddymobile.free.anteater.rule.trigger.hierarchy;

import android.net.wifi.WifiEnterpriseConfig;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import cn.teddymobile.free.anteater.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewHierarchyNode {
    private static final String CHILD_INDEX_ARBITRARY = "?";
    private static final String CHILD_INDEX_FIRST = "first";
    private static final String CHILD_INDEX_LAST = "last";
    private static final String CLASS_NAME_ARBITRARY = "?";
    private static final String JSON_FIELD_CHILD_INDEX = "child_index";
    private static final String JSON_FIELD_CLASS_NAME = "class_name";
    private static final String JSON_FIELD_CLASS_NAME_OBFUSCATED = "class_name_obfuscated";
    private static final String TAG = ViewHierarchyNode.class.getSimpleName();
    private final String mChildIndex;
    private final boolean mChildIndexArbitrary = this.mChildIndex.equals("?");
    private final String mClassName;
    private final boolean mClassNameArbitrary = this.mClassName.equals("?");
    private final boolean mClassNameObfuscated;

    public ViewHierarchyNode(JSONObject nodeObject) throws JSONException {
        this.mClassName = nodeObject.getString(JSON_FIELD_CLASS_NAME);
        this.mChildIndex = nodeObject.getString(JSON_FIELD_CHILD_INDEX);
        this.mClassNameObfuscated = nodeObject.optBoolean(JSON_FIELD_CLASS_NAME_OBFUSCATED, false);
    }

    public String toString() {
        return "ClassName = " + this.mClassName + "\nClassNameObfuscated = " + this.mClassNameObfuscated + "\nChildIndex = " + this.mChildIndex;
    }

    public boolean fitView(View view) {
        boolean result = false;
        if (view != null && (view.getParent() instanceof ViewGroup)) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (!this.mClassNameObfuscated && !this.mClassNameArbitrary && !view.getClass().getName().equals(this.mClassName)) {
                Logger.w(TAG, "Child class is incorrect.");
                String str = TAG;
                Logger.w(str, "Expected = " + this.mClassName + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mClassNameObfuscated);
                String str2 = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Actual = ");
                sb.append(view.getClass().getName());
                Logger.w(str2, sb.toString());
            } else if (this.mChildIndexArbitrary) {
                result = true;
            } else {
                Integer indexNumber = null;
                if (this.mChildIndex.equals(CHILD_INDEX_FIRST)) {
                    indexNumber = 0;
                } else if (this.mChildIndex.equals(CHILD_INDEX_LAST)) {
                    indexNumber = Integer.valueOf(parent.getChildCount() - 1);
                } else if (TextUtils.isDigitsOnly(this.mChildIndex)) {
                    indexNumber = Integer.valueOf(Integer.parseInt(this.mChildIndex));
                } else {
                    String str3 = TAG;
                    Logger.w(str3, "Unknown child index " + this.mChildIndex);
                }
                if (indexNumber != null) {
                    if (indexNumber.intValue() < 0 || indexNumber.intValue() >= parent.getChildCount()) {
                        Logger.w(TAG, "Child index is out of bounds.");
                        String str4 = TAG;
                        Logger.w(str4, "Parent = " + parent.getClass().getName());
                        String str5 = TAG;
                        Logger.w(str5, "Child = " + view.getClass().getName());
                        String str6 = TAG;
                        Logger.w(str6, "Index = " + indexNumber + "/" + parent.getChildCount());
                    } else if (parent.getChildAt(indexNumber.intValue()).equals(view)) {
                        result = true;
                    } else {
                        Logger.w(TAG, "Child is incorrect.");
                        String str7 = TAG;
                        Logger.w(str7, "Parent = " + parent.getClass().getName());
                        String str8 = TAG;
                        Logger.w(str8, "Expected = " + view);
                        String str9 = TAG;
                        Logger.w(str9, "Actual = " + parent.getChildAt(indexNumber.intValue()));
                    }
                }
            }
        } else if (view != null) {
            String str10 = TAG;
            Logger.w(str10, view.getClass().getName() + " parent is not ViewGroup.");
        } else {
            Logger.w(TAG, "View is null.");
        }
        String str11 = TAG;
        Logger.i(str11, getClass().getSimpleName() + " Result = " + result);
        return result;
    }

    public View getView(View view) {
        View result = null;
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            Integer indexNumber = null;
            if (this.mChildIndex.equals(CHILD_INDEX_FIRST)) {
                indexNumber = 0;
            } else if (this.mChildIndex.equals(CHILD_INDEX_LAST)) {
                indexNumber = Integer.valueOf(parent.getChildCount() - 1);
            } else if (TextUtils.isDigitsOnly(this.mChildIndex)) {
                indexNumber = Integer.valueOf(Integer.parseInt(this.mChildIndex));
            } else {
                String str = TAG;
                Logger.w(str, "Unknown child index " + this.mChildIndex);
            }
            if (indexNumber != null) {
                if (indexNumber.intValue() < 0 || indexNumber.intValue() >= parent.getChildCount()) {
                    Logger.w(TAG, "Child index is out of bounds.");
                    String str2 = TAG;
                    Logger.w(str2, "Parent = " + parent.getClass().getName());
                    String str3 = TAG;
                    Logger.w(str3, "Child = " + this.mClassName);
                    String str4 = TAG;
                    Logger.w(str4, "Index = " + this.mChildIndex + "/" + parent.getChildCount());
                } else {
                    View child = parent.getChildAt(indexNumber.intValue());
                    if (this.mClassNameObfuscated) {
                        return child;
                    }
                    if (child.getClass().getName().equals(this.mClassName)) {
                        result = child;
                    } else {
                        Integer autoFixIndex = autoFix(parent, this.mClassName);
                        if (autoFixIndex != null) {
                            Logger.w(TAG, "Child class is incorrect.");
                            String str5 = TAG;
                            Logger.w(str5, "Parent = " + parent.getClass().getName());
                            String str6 = TAG;
                            Logger.w(str6, "Index = " + this.mChildIndex);
                            String str7 = TAG;
                            Logger.w(str7, "Expected = " + this.mClassName);
                            String str8 = TAG;
                            Logger.w(str8, "Actual = " + child.getClass().getName());
                            String str9 = TAG;
                            Logger.w(str9, "Auto fix index to " + autoFixIndex);
                            result = parent.getChildAt(autoFixIndex.intValue());
                        } else {
                            Logger.w(TAG, "Child class is incorrect.");
                            String str10 = TAG;
                            Logger.w(str10, "Parent = " + parent.getClass().getName());
                            String str11 = TAG;
                            Logger.w(str11, "Index = " + this.mChildIndex);
                            String str12 = TAG;
                            Logger.w(str12, "Expected = " + this.mClassName);
                            String str13 = TAG;
                            Logger.w(str13, "Actual = " + child.getClass().getName());
                        }
                    }
                }
            }
        } else if (view != null) {
            String str14 = TAG;
            Logger.w(str14, view.getClass().getName() + " parent is not ViewGroup.");
        } else {
            Logger.w(TAG, "View is null.");
        }
        String str15 = TAG;
        Logger.i(str15, getClass().getSimpleName() + " Result = " + result);
        return result;
    }

    private Integer autoFix(ViewGroup parent, String className) {
        if (parent == null || className == null) {
            return null;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i).getClass().getName().equals(className)) {
                return Integer.valueOf(i);
            }
        }
        return null;
    }
}

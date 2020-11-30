package cn.teddymobile.free.anteater.helper.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiEnterpriseConfig;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.teddymobile.free.anteater.helper.logger.Logger;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class AttributeView extends ScrollView {
    private static final String TAG = AttributeView.class.getSimpleName();
    private Object mAttribute = null;
    private Class<?> mClass = null;
    private LinearLayout mContainer = null;

    public AttributeView(Context context) {
        super(context);
        initView();
    }

    public void updateModel(Object attribute, Class<?> clazz) {
        if (attribute != null && clazz != null) {
            this.mAttribute = attribute;
            this.mClass = clazz;
            addSuperClassTextView();
            addMethodTextView();
            addContextTextView();
            addIntentTextView();
            addListenerInfoTextView();
            addOnClickListenerTextView();
            addArrayTextView();
            addFieldTextView();
        }
    }

    private void initView() {
        this.mContainer = new LinearLayout(getContext());
        this.mContainer.setOrientation(1);
        addView(this.mContainer);
    }

    private void addSuperClassTextView() {
        if (this.mClass.getSuperclass() != null) {
            final Class<?> superClazz = this.mClass.getSuperclass();
            Context context = getContext();
            TextView textView = HierarchyView.createTextView(context, "Super = " + superClazz.getName());
            textView.setTextColor(Color.MAGENTA);
            textView.setOnClickListener(new View.OnClickListener() {
                /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass1 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                    attributeView.updateModel(AttributeView.this.mAttribute, superClazz);
                    new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                }
            });
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass2 */

                @Override // android.view.View.OnLongClickListener
                public boolean onLongClick(View v) {
                    String info = String.format("{\"field_name\": \"[super]\", \"class_name\": \"%s\", \"leaf\": []}", superClazz.getName());
                    String str = AttributeView.TAG;
                    Logger.i(str, "Attribute = \n" + info);
                    return true;
                }
            });
            this.mContainer.addView(textView);
        }
    }

    private void addMethodTextView() {
        TextView textView = HierarchyView.createTextView(getContext(), "Methods");
        textView.setTextColor(Color.MAGENTA);
        textView.setOnClickListener(new View.OnClickListener() {
            /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass3 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MethodView methodView = new MethodView(AttributeView.this.getContext());
                methodView.updateModel(AttributeView.this.mAttribute, AttributeView.this.mClass);
                new AlertDialog.Builder(AttributeView.this.getContext()).setView(methodView).create().show();
            }
        });
        this.mContainer.addView(textView);
    }

    private void addContextTextView() {
        Object obj = this.mAttribute;
        if (obj instanceof View) {
            View view = (View) obj;
            try {
                Method method = View.class.getDeclaredMethod("getContext", new Class[0]);
                method.setAccessible(true);
                final Object contextObject = method.invoke(view, new Object[0]);
                Context context = getContext();
                TextView textView = HierarchyView.createTextView(context, "Context = " + contextObject.getClass().getName());
                textView.setTextColor(Color.MAGENTA);
                textView.setOnClickListener(new View.OnClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass4 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                        Object obj = contextObject;
                        attributeView.updateModel(obj, obj.getClass());
                        new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                    }
                });
                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass5 */

                    @Override // android.view.View.OnLongClickListener
                    public boolean onLongClick(View v) {
                        String info = String.format("{\"field_name\": \"[context]\", \"class_name\": \"%s\", \"leaf\": []}", contextObject.getClass().getName());
                        String str = AttributeView.TAG;
                        Logger.i(str, "Attribute = \n" + info);
                        return true;
                    }
                });
                this.mContainer.addView(textView);
            } catch (Exception e) {
                Logger.w(TAG, e.getMessage(), e);
            }
        }
    }

    private void addIntentTextView() {
        Object obj = this.mAttribute;
        if (obj instanceof View) {
            View view = (View) obj;
            try {
                Method method = View.class.getDeclaredMethod("getContext", new Class[0]);
                method.setAccessible(true);
                Object contextObject = method.invoke(view, new Object[0]);
                if (contextObject instanceof Activity) {
                    final Intent intent = ((Activity) contextObject).getIntent();
                    Context context = getContext();
                    TextView textView = HierarchyView.createTextView(context, "Intent = " + intent);
                    textView.setTextColor(Color.MAGENTA);
                    textView.setOnClickListener(new View.OnClickListener() {
                        /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass6 */

                        @Override // android.view.View.OnClickListener
                        public void onClick(View v) {
                            if (intent != null) {
                                AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                                Intent intent = intent;
                                attributeView.updateModel(intent, intent.getClass());
                                new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                            }
                        }
                    });
                    this.mContainer.addView(textView);
                }
            } catch (Exception e) {
                Logger.w(TAG, e.getMessage(), e);
            }
        }
    }

    private void addListenerInfoTextView() {
        Object obj = this.mAttribute;
        if (obj instanceof View) {
            View view = (View) obj;
            try {
                Field field = View.class.getDeclaredField("mListenerInfo");
                field.setAccessible(true);
                final Object listenerInfo = field.get(view);
                Context context = getContext();
                TextView textView = HierarchyView.createTextView(context, "ListenerInfo = " + listenerInfo);
                textView.setTextColor(Color.MAGENTA);
                textView.setOnClickListener(new View.OnClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass7 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        if (listenerInfo != null) {
                            AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                            Object obj = listenerInfo;
                            attributeView.updateModel(obj, obj.getClass());
                            new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                        }
                    }
                });
                this.mContainer.addView(textView);
            } catch (Exception e) {
                Logger.w(TAG, e.getMessage(), e);
            }
        }
    }

    private void addOnClickListenerTextView() {
        final Object onClickListener;
        Object obj = this.mAttribute;
        if (obj instanceof View) {
            View view = (View) obj;
            try {
                Field field = View.class.getDeclaredField("mListenerInfo");
                field.setAccessible(true);
                Object listenerInfo = field.get(view);
                if (listenerInfo != null) {
                    Field field2 = listenerInfo.getClass().getDeclaredField("mOnClickListener");
                    field2.setAccessible(true);
                    onClickListener = field2.get(listenerInfo);
                } else {
                    onClickListener = null;
                }
                Context context = getContext();
                TextView textView = HierarchyView.createTextView(context, "OnClickListener = " + onClickListener);
                textView.setTextColor(Color.MAGENTA);
                textView.setOnClickListener(new View.OnClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass8 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        if (onClickListener != null) {
                            AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                            Object obj = onClickListener;
                            attributeView.updateModel(obj, obj.getClass());
                            new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                        }
                    }
                });
                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass9 */

                    @Override // android.view.View.OnLongClickListener
                    public boolean onLongClick(View v) {
                        String info = String.format(Locale.getDefault(), "{\"field_name\": \"[onClickListener]\", \"class_name\": \"%s\", \"leaf\": []}", View.OnClickListener.class.getName());
                        String str = AttributeView.TAG;
                        Logger.i(str, "Attribute = \n" + info);
                        return true;
                    }
                });
                this.mContainer.addView(textView);
            } catch (Exception e) {
                Logger.w(TAG, e.getMessage(), e);
            }
        }
    }

    private void addArrayTextView() {
        if (this.mClass.isArray()) {
            for (final int i = 0; i < Array.getLength(this.mAttribute); i++) {
                final Object arrayItem = Array.get(this.mAttribute, i);
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                sb.append(i);
                sb.append("] = ");
                sb.append(arrayItem);
                sb.append(arrayItem != null ? WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + arrayItem.getClass().getSimpleName() : "");
                TextView textView = HierarchyView.createTextView(getContext(), sb.toString());
                textView.setOnClickListener(new View.OnClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass10 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        if (arrayItem != null) {
                            ((TextView) v).setTextColor(-65536);
                            AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                            Object obj = arrayItem;
                            attributeView.updateModel(obj, obj.getClass());
                            new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                        }
                    }
                });
                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass11 */

                    @Override // android.view.View.OnLongClickListener
                    public boolean onLongClick(View v) {
                        Locale locale = Locale.getDefault();
                        Object[] objArr = new Object[2];
                        objArr[0] = Integer.valueOf(i);
                        Object obj = arrayItem;
                        objArr[1] = obj != null ? obj.getClass().getName() : "?";
                        String info = String.format(locale, "{\"field_name\": \"[%d]\", \"class_name\": \"%s\", \"leaf\": []}", objArr);
                        String str = AttributeView.TAG;
                        Logger.i(str, "Attribute = \n" + info);
                        return true;
                    }
                });
                this.mContainer.addView(textView);
            }
        }
    }

    private void addFieldTextView() {
        if (!this.mClass.isArray()) {
            final Field[] fields = this.mClass.getDeclaredFields();
            for (final int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                try {
                    field.setAccessible(true);
                    final Object value = field.get(this.mAttribute);
                    StringBuilder sb = new StringBuilder();
                    sb.append(field.getName());
                    sb.append(" = ");
                    sb.append(value);
                    sb.append(value != null ? WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + value.getClass().getSimpleName() : "");
                    TextView textView = HierarchyView.createTextView(getContext(), sb.toString());
                    textView.setOnClickListener(new View.OnClickListener() {
                        /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass12 */

                        @Override // android.view.View.OnClickListener
                        public void onClick(View v) {
                            if (value != null) {
                                ((TextView) v).setTextColor(-65536);
                                AttributeView attributeView = new AttributeView(AttributeView.this.getContext());
                                Object obj = value;
                                attributeView.updateModel(obj, obj.getClass());
                                new AlertDialog.Builder(AttributeView.this.getContext()).setView(attributeView).create().show();
                            }
                        }
                    });
                    textView.setOnLongClickListener(new View.OnLongClickListener() {
                        /* class cn.teddymobile.free.anteater.helper.view.AttributeView.AnonymousClass13 */

                        @Override // android.view.View.OnLongClickListener
                        public boolean onLongClick(View v) {
                            Locale locale = Locale.getDefault();
                            Object[] objArr = new Object[4];
                            objArr[0] = field.getName();
                            Object obj = value;
                            objArr[1] = obj != null ? obj.getClass().getName() : "?";
                            objArr[2] = Integer.valueOf(i);
                            objArr[3] = Integer.valueOf(fields.length);
                            String info = String.format(locale, "{\"field_name\": \"%s\", \"class_name\": \"%s\", \"field_index\": %d, \"parent_field_count\": %d, \"leaf\": []}", objArr);
                            String str = AttributeView.TAG;
                            Logger.i(str, "Attribute = \n" + info);
                            return true;
                        }
                    });
                    this.mContainer.addView(textView);
                } catch (Exception e) {
                    Logger.w(TAG, e.getMessage(), e);
                }
            }
        }
    }
}

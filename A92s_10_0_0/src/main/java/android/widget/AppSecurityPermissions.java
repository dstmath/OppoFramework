package android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

@Deprecated
public class AppSecurityPermissions {
    public static View getPermissionItemView(Context context, CharSequence grpName, CharSequence description, boolean dangerous) {
        return getPermissionItemViewOld(context, (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), grpName, description, dangerous, context.getDrawable(dangerous ? R.drawable.ic_bullet_key_permission : R.drawable.ic_text_dot));
    }

    private static View getPermissionItemViewOld(Context context, LayoutInflater inflater, CharSequence grpName, CharSequence permList, boolean dangerous, Drawable icon) {
        View permView = inflater.inflate(R.layout.app_permission_item_old, (ViewGroup) null);
        TextView permGrpView = (TextView) permView.findViewById(R.id.permission_group);
        TextView permDescView = (TextView) permView.findViewById(R.id.permission_list);
        ((ImageView) permView.findViewById(R.id.perm_icon)).setImageDrawable(icon);
        if (grpName != null) {
            permGrpView.setText(grpName);
            permDescView.setText(permList);
        } else {
            permGrpView.setText(permList);
            permDescView.setVisibility(8);
        }
        return permView;
    }
}

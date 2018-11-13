package android.accounts;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class ChooseAccountTypeActivity extends Activity {
    private static final String TAG = "AccountChooser";
    private ArrayList<AuthInfo> mAuthenticatorInfosToDisplay;
    private HashMap<String, AuthInfo> mTypeToAuthenticatorInfo = new HashMap();

    private static class AccountArrayAdapter extends ArrayAdapter<AuthInfo> {
        private ArrayList<AuthInfo> mInfos;
        private LayoutInflater mLayoutInflater;

        public AccountArrayAdapter(Context context, int textViewResourceId, ArrayList<AuthInfo> infos) {
            super(context, textViewResourceId, infos);
            this.mInfos = infos;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = this.mLayoutInflater.inflate(17367108, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(16908656);
                holder.icon = (ImageView) convertView.findViewById(16908655);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text.setText(((AuthInfo) this.mInfos.get(position)).name);
            holder.icon.setImageDrawable(((AuthInfo) this.mInfos.get(position)).drawable);
            return convertView;
        }
    }

    private static class AuthInfo {
        final AuthenticatorDescription desc;
        final Drawable drawable;
        final String name;

        AuthInfo(AuthenticatorDescription desc, String name, Drawable drawable) {
            this.desc = desc;
            this.name = name;
            this.drawable = drawable;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView text;

        /* synthetic */ ViewHolder(ViewHolder -this0) {
            this();
        }

        private ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        String type;
        super.onCreate(savedInstanceState);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "ChooseAccountTypeActivity.onCreate(savedInstanceState=" + savedInstanceState + ")");
        }
        Set setOfAllowableAccountTypes = null;
        String[] validAccountTypes = getIntent().getStringArrayExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY);
        if (validAccountTypes != null) {
            setOfAllowableAccountTypes = new HashSet(validAccountTypes.length);
            for (String type2 : validAccountTypes) {
                setOfAllowableAccountTypes.add(type2);
            }
        }
        buildTypeToAuthDescriptionMap();
        this.mAuthenticatorInfosToDisplay = new ArrayList(this.mTypeToAuthenticatorInfo.size());
        for (Entry<String, AuthInfo> entry : this.mTypeToAuthenticatorInfo.entrySet()) {
            type2 = (String) entry.getKey();
            AuthInfo info = (AuthInfo) entry.getValue();
            if (setOfAllowableAccountTypes == null || (setOfAllowableAccountTypes.contains(type2) ^ 1) == 0) {
                this.mAuthenticatorInfosToDisplay.add(info);
            }
        }
        if (this.mAuthenticatorInfosToDisplay.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "no allowable account types");
            setResult(-1, new Intent().putExtras(bundle));
            finish();
        } else if (this.mAuthenticatorInfosToDisplay.size() == 1) {
            setResultAndFinish(((AuthInfo) this.mAuthenticatorInfosToDisplay.get(0)).desc.type);
        } else {
            setContentView(17367109);
            ListView list = (ListView) findViewById(R.id.list);
            list.setAdapter(new AccountArrayAdapter(this, R.layout.simple_list_item_1, this.mAuthenticatorInfosToDisplay));
            list.setChoiceMode(0);
            list.setTextFilterEnabled(false);
            list.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    ChooseAccountTypeActivity.this.setResultAndFinish(((AuthInfo) ChooseAccountTypeActivity.this.mAuthenticatorInfosToDisplay.get(position)).desc.type);
                }
            });
        }
    }

    private void setResultAndFinish(String type) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, type);
        setResult(-1, new Intent().putExtras(bundle));
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "ChooseAccountTypeActivity.setResultAndFinish: selected account type " + type);
        }
        finish();
    }

    private void buildTypeToAuthDescriptionMap() {
        for (AuthenticatorDescription desc : AccountManager.get(this).getAuthenticatorTypes()) {
            String str = null;
            Drawable icon = null;
            try {
                Context authContext = createPackageContext(desc.packageName, 0);
                icon = authContext.getDrawable(desc.iconId);
                CharSequence sequence = authContext.getResources().getText(desc.labelId);
                if (sequence != null) {
                    str = sequence.toString();
                }
                str = sequence.toString();
            } catch (NameNotFoundException e) {
                if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "No icon name for account type " + desc.type);
                }
            } catch (NotFoundException e2) {
                if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "No icon resource for account type " + desc.type);
                }
            }
            this.mTypeToAuthenticatorInfo.put(desc.type, new AuthInfo(desc, str, icon));
        }
    }
}

package com.android.internal.app;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import com.android.internal.R;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocaleStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocalePickerWithRegion extends ListFragment implements SearchView.OnQueryTextListener {
    private static final String PARENT_FRAGMENT_NAME = "localeListEditor";
    private SuggestedLocaleAdapter mAdapter;
    private int mFirstVisiblePosition = 0;
    private LocaleSelectedListener mListener;
    private Set<LocaleStore.LocaleInfo> mLocaleList;
    private LocaleStore.LocaleInfo mParentLocale;
    private CharSequence mPreviousSearch = null;
    private boolean mPreviousSearchHadFocus = false;
    private SearchView mSearchView = null;
    private int mTopDistance = 0;
    private boolean mTranslatedOnly = false;

    public interface LocaleSelectedListener {
        void onLocaleSelected(LocaleStore.LocaleInfo localeInfo);
    }

    private static LocalePickerWithRegion createCountryPicker(Context context, LocaleSelectedListener listener, LocaleStore.LocaleInfo parent, boolean translatedOnly) {
        LocalePickerWithRegion localePicker = new LocalePickerWithRegion();
        if (localePicker.setListener(context, listener, parent, translatedOnly)) {
            return localePicker;
        }
        return null;
    }

    public static LocalePickerWithRegion createLanguagePicker(Context context, LocaleSelectedListener listener, boolean translatedOnly) {
        LocalePickerWithRegion localePicker = new LocalePickerWithRegion();
        localePicker.setListener(context, listener, null, translatedOnly);
        return localePicker;
    }

    private boolean setListener(Context context, LocaleSelectedListener listener, LocaleStore.LocaleInfo parent, boolean translatedOnly) {
        this.mParentLocale = parent;
        this.mListener = listener;
        this.mTranslatedOnly = translatedOnly;
        setRetainInstance(true);
        HashSet<String> langTagsToIgnore = new HashSet<>();
        if (!translatedOnly) {
            Collections.addAll(langTagsToIgnore, LocalePicker.getLocales().toLanguageTags().split(SmsManager.REGEX_PREFIX_DELIMITER));
        }
        if (parent != null) {
            this.mLocaleList = LocaleStore.getLevelLocales(context, langTagsToIgnore, parent, translatedOnly);
            if (this.mLocaleList.size() <= 1) {
                if (listener == null || this.mLocaleList.size() != 1) {
                    return false;
                }
                listener.onLocaleSelected(this.mLocaleList.iterator().next());
                return false;
            }
        } else {
            this.mLocaleList = LocaleStore.getLevelLocales(context, langTagsToIgnore, null, translatedOnly);
        }
        return true;
    }

    private void returnToParentFrame() {
        getFragmentManager().popBackStack(PARENT_FRAGMENT_NAME, 1);
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean countryMode = true;
        setHasOptionsMenu(true);
        if (this.mLocaleList == null) {
            returnToParentFrame();
            return;
        }
        if (this.mParentLocale == null) {
            countryMode = false;
        }
        Locale sortingLocale = countryMode ? this.mParentLocale.getLocale() : Locale.getDefault();
        this.mAdapter = new SuggestedLocaleAdapter(this.mLocaleList, countryMode);
        this.mAdapter.sort(new LocaleHelper.LocaleInfoComparator(sortingLocale, countryMode));
        setListAdapter(this.mAdapter);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        getFragmentManager().popBackStack();
        return true;
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.mParentLocale != null) {
            getActivity().setTitle(this.mParentLocale.getFullNameNative());
        } else {
            getActivity().setTitle(R.string.language_selection_title);
        }
        getListView().requestFocus();
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        SearchView searchView = this.mSearchView;
        int i = 0;
        if (searchView != null) {
            this.mPreviousSearchHadFocus = searchView.hasFocus();
            this.mPreviousSearch = this.mSearchView.getQuery();
        } else {
            this.mPreviousSearchHadFocus = false;
            this.mPreviousSearch = null;
        }
        ListView list = getListView();
        View firstChild = list.getChildAt(0);
        this.mFirstVisiblePosition = list.getFirstVisiblePosition();
        if (firstChild != null) {
            i = firstChild.getTop() - list.getPaddingTop();
        }
        this.mTopDistance = i;
    }

    @Override // android.app.ListFragment
    public void onListItemClick(ListView l, View v, int position, long id) {
        LocaleStore.LocaleInfo locale = (LocaleStore.LocaleInfo) getListAdapter().getItem(position);
        if (locale.getParent() != null) {
            LocaleSelectedListener localeSelectedListener = this.mListener;
            if (localeSelectedListener != null) {
                localeSelectedListener.onLocaleSelected(locale);
            }
            returnToParentFrame();
            return;
        }
        LocalePickerWithRegion selector = createCountryPicker(getContext(), this.mListener, locale, this.mTranslatedOnly);
        if (selector != null) {
            getFragmentManager().beginTransaction().setTransition(4097).replace(getId(), selector).addToBackStack(null).commit();
        } else {
            returnToParentFrame();
        }
    }

    @Override // android.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mParentLocale == null) {
            inflater.inflate(R.menu.language_selection_list, menu);
            MenuItem searchMenuItem = menu.findItem(R.id.locale_search_menu);
            this.mSearchView = (SearchView) searchMenuItem.getActionView();
            this.mSearchView.setQueryHint(getText(R.string.search_language_hint));
            this.mSearchView.setOnQueryTextListener(this);
            if (!TextUtils.isEmpty(this.mPreviousSearch)) {
                searchMenuItem.expandActionView();
                this.mSearchView.setIconified(false);
                this.mSearchView.setActivated(true);
                if (this.mPreviousSearchHadFocus) {
                    this.mSearchView.requestFocus();
                }
                this.mSearchView.setQuery(this.mPreviousSearch, true);
            } else {
                this.mSearchView.setQuery(null, false);
            }
            getListView().setSelectionFromTop(this.mFirstVisiblePosition, this.mTopDistance);
        }
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextChange(String newText) {
        SuggestedLocaleAdapter suggestedLocaleAdapter = this.mAdapter;
        if (suggestedLocaleAdapter == null) {
            return false;
        }
        suggestedLocaleAdapter.getFilter().filter(newText);
        return false;
    }
}

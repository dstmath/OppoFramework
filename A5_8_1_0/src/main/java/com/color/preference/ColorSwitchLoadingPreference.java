package com.color.preference;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Switch;

public class ColorSwitchLoadingPreference extends SwitchPreference {
    private final Listener mListener;
    private ProgressBar mProgressBar;
    View mSwitch;

    private class Listener implements OnCheckedChangeListener {
        /* synthetic */ Listener(ColorSwitchLoadingPreference this$0, Listener -this1) {
            this();
        }

        private Listener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (ColorSwitchLoadingPreference.this.callChangeListener(Boolean.valueOf(isChecked))) {
                ColorSwitchLoadingPreference.this.setChecked(isChecked);
            } else {
                buttonView.setChecked(isChecked ^ 1);
            }
        }
    }

    public ColorSwitchLoadingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mListener = new Listener(this, null);
        setCanRecycleLayout(true);
    }

    public ColorSwitchLoadingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorSwitchLoadingPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 201393332);
    }

    public ColorSwitchLoadingPreference(Context context) {
        this(context, null);
    }

    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(getLayoutResource(), parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        int widgetLayoutResource = getWidgetLayoutResource();
        if (widgetFrame != null) {
            if (widgetLayoutResource != 0) {
                layoutInflater.inflate(widgetLayoutResource, widgetFrame);
                this.mProgressBar = (ProgressBar) widgetFrame.findViewById(201458961);
                this.mSwitch = (Switch) layout.findViewById(201458962);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        return layout;
    }

    protected void onBindView(View view) {
        this.mSwitch = view.findViewById(201458962);
        if (this.mSwitch != null && (this.mSwitch instanceof Checkable)) {
            if (this.mSwitch instanceof Switch) {
                this.mSwitch.setOnCheckedChangeListener(null);
            }
            ((Checkable) this.mSwitch).setChecked(isChecked());
            if (this.mSwitch instanceof Switch) {
                Switch switchView = (Switch) this.mSwitch;
                switchView.setTextOn(getSwitchTextOn());
                switchView.setTextOff(getSwitchTextOff());
                switchView.setOnCheckedChangeListener(this.mListener);
            }
        }
        super.onBindView(view);
    }

    public void showProgressBar(boolean show) {
        if (this.mProgressBar != null && this.mSwitch != null) {
            if (show) {
                this.mProgressBar.setVisibility(0);
                this.mSwitch.setVisibility(8);
                return;
            }
            this.mProgressBar.setVisibility(8);
            this.mSwitch.setVisibility(0);
        }
    }

    public ProgressBar getProgressBar() {
        return this.mProgressBar;
    }

    public Switch getSwitch() {
        if (this.mSwitch == null || !(this.mSwitch instanceof Switch)) {
            return null;
        }
        return (Switch) this.mSwitch;
    }
}

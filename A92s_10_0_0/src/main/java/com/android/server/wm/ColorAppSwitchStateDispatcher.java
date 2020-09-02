package com.android.server.wm;

import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorAppSwitchStateDispatcher {
    private static final String TAG = "ColorAppSwitchStateDispatcher";
    public boolean inBlackList = false;
    public final List<ColorAppSwitchRuleInfo> mDynamicRules = new ArrayList();
    public ColorAppSwitchRuleInfo mStaticIntecepter;
    public String pkgName;

    public ColorAppSwitchStateDispatcher(String pkgName2) {
        this.pkgName = pkgName2;
    }

    public void setStaticRule(ColorAppSwitchRuleInfo rule) {
        this.mStaticIntecepter = rule;
    }

    public boolean addDynamicListener(ColorAppSwitchRuleInfo info) {
        Slog.i(TAG, "registerAppSwitchObserver addDynamicListener OK, info = " + info + " rule size =" + this.mDynamicRules.size());
        clearNullElement();
        synchronized (this.mDynamicRules) {
            this.mDynamicRules.remove(info);
            this.mDynamicRules.add(info);
        }
        return true;
    }

    private void clearNullElement() {
        synchronized (this.mDynamicRules) {
            Iterator<ColorAppSwitchRuleInfo> it = this.mDynamicRules.iterator();
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                }
            }
        }
    }

    public boolean removeDynamicListener(ColorAppSwitchRuleInfo info) {
        clearNullElement();
        synchronized (this.mDynamicRules) {
            int index = this.mDynamicRules.indexOf(info);
            if (index >= 0) {
                Slog.i(TAG, "unregisterAppSwitchObserver ok, pkgName = " + this.pkgName + " info = " + info);
                ColorAppSwitchRuleInfo ruleInfo = this.mDynamicRules.get(index);
                this.mDynamicRules.remove(info);
                ruleInfo.observer.asBinder().unlinkToDeath(ruleInfo.deathRecipient, 0);
                return true;
            }
            Slog.i(TAG, "unregisterAppSwitchObserver failed, info = " + info);
            return false;
        }
    }

    public void setDefaultMatchConfig(boolean defaultMatchApp, boolean defaultMatchActivity) {
        synchronized (this.mDynamicRules) {
            for (ColorAppSwitchRuleInfo rule : this.mDynamicRules) {
                if (rule != null) {
                    rule.setDefaultMatchConfig(defaultMatchApp, defaultMatchActivity);
                }
            }
        }
        ColorAppSwitchRuleInfo colorAppSwitchRuleInfo = this.mStaticIntecepter;
        if (colorAppSwitchRuleInfo != null) {
            colorAppSwitchRuleInfo.setDefaultMatchConfig(defaultMatchApp, defaultMatchActivity);
        }
    }

    public void setBlackItem(boolean black) {
        this.inBlackList = black;
    }

    public boolean notifyActivityEnter(ActivityRecord enter, boolean firstStart) {
        boolean match;
        if (this.inBlackList) {
            return false;
        }
        synchronized (this.mDynamicRules) {
            match = false;
            for (ColorAppSwitchRuleInfo dynamicRule : this.mDynamicRules) {
                match |= dynamicRule.notifyActivityEnter(enter, firstStart);
            }
            if (this.mDynamicRules.size() == 0 && this.mStaticIntecepter != null && this.mStaticIntecepter.notifyActivityEnter(enter, firstStart)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "notifyActivityEnter static listener handled = " + this.pkgName + " enter = " + enter);
                }
                match |= true;
            }
        }
        return match;
    }

    public boolean notifyAppExit(String pkgName2, ActivityRecord nextResuming, boolean nextFirstStart) {
        boolean match;
        if (this.inBlackList) {
            return false;
        }
        synchronized (this.mDynamicRules) {
            match = false;
            for (ColorAppSwitchRuleInfo dynamicRule : this.mDynamicRules) {
                match |= dynamicRule.notifyAppExit(pkgName2, nextResuming, nextFirstStart);
            }
            if (this.mDynamicRules.size() == 0 && this.mStaticIntecepter != null && this.mStaticIntecepter.notifyAppExit(pkgName2, nextResuming, nextFirstStart)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "notifyAppExit static listener handled = " + this.pkgName + " pkg = " + pkgName2);
                }
                match = true;
            }
        }
        return match;
    }

    public boolean notifyActivityExit(String className, ActivityRecord nextResuming, boolean nextFirstStart) {
        boolean match;
        if (this.inBlackList) {
            return false;
        }
        synchronized (this.mDynamicRules) {
            match = false;
            for (ColorAppSwitchRuleInfo dynamicRule : this.mDynamicRules) {
                match |= dynamicRule.notifyActivityExit(className, nextResuming, nextFirstStart);
            }
            if (this.mDynamicRules.size() == 0 && this.mStaticIntecepter != null && this.mStaticIntecepter.notifyActivityExit(className, nextResuming, nextFirstStart)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "notifyActivityExit static listener handled = " + this.pkgName + " className = " + className);
                }
                match = true;
            }
        }
        return match;
    }

    public boolean notifyAppEnter(ActivityRecord enter, boolean firstStart) {
        boolean match;
        if (this.inBlackList) {
            return false;
        }
        synchronized (this.mDynamicRules) {
            match = false;
            for (ColorAppSwitchRuleInfo dynamicRule : this.mDynamicRules) {
                match |= dynamicRule.notifyAppEnter(enter, firstStart);
            }
            if (this.mDynamicRules.size() == 0 && this.mStaticIntecepter != null && this.mStaticIntecepter.notifyAppEnter(enter, firstStart)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "notifyActivityExit static listener handled = " + this.pkgName + " enter = " + enter);
                }
                match = true;
            }
        }
        return match;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ColorAppSwitchRuleInfo = { ");
        sb.append(" pkgName = " + this.pkgName);
        sb.append("}");
        return sb.toString();
    }
}

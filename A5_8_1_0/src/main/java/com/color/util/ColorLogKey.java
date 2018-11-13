package com.color.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface ColorLogKey {
    public static final String PREFIX = "log.key";

    @ColorLogKey(owner = "ROM.SDK")
    public static class ActionBar {
        public static final String BASE = "log.key.action_bar";
        public static final String DISP = "log.key.action_bar.disp";
        public static final String MODE = "log.key.action_bar.mode";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class BootMessage {
        public static final String BASE = "log.key.boot_message";
        public static final String DISP = "log.key.boot_message.disp";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class BottomMenu {
        public static final String ANIM = "log.key.bottom_menu.anim";
        public static final String ANIM2 = "log.key.bottom_menu.anim2";
        public static final String BASE = "log.key.bottom_menu";
        public static final String DRAW = "log.key.bottom_menu.draw";
        public static final String ITEM = "log.key.bottom_menu.item";
        public static final String PRESS = "log.key.bottom_menu.press";
        public static final String TOUCH = "log.key.bottom_menu.touch";
        public static final String UPDATE = "log.key.bottom_menu.update";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class ListView {
        public static final String BASE = "log.key.list_view";
        public static final String REMOVE = "log.key.list_view.remove";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class LockPattern {
        public static final String BASE = "log.key.lock_pattern";
        public static final String SERVICE = "log.key.lock_pattern.service";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class LongShot {
        public static final String ANALYSIS = "log.key.long_shot.analysis";
        public static final String ANALYSIS_ALL = "log.key.long_shot.analysis.all";
        public static final String BASE = "log.key.long_shot";
        public static final String BORDERING = "log.key.long_shot.bordering";
        public static final String BORDERING_ALL = "log.key.long_shot.bordering.all";
        public static final String CONFIG = "log.key.long_shot.config";
        public static final String INIT = "log.key.long_shot.init";
        public static final String INPUT = "log.key.long_shot.input";
        public static final String INTERCEPT = "log.key.long_shot.intercept";
        public static final String INTERCEPT_ALL = "log.key.long_shot.intercept.all";
        public static final String SCROLL = "log.key.long_shot.scroll";
        public static final String SCROLL_ALL = "log.key.long_shot.scroll.all";
        public static final String SERVICE = "log.key.long_shot.service";
        public static final String SPEND = "log.key.long_shot.spend";
        public static final String TAKE = "log.key.long_shot.take";
        public static final String VIEW = "log.key.long_shot.view";
        public static final String WINDOW = "log.key.long_shot.window";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class MultiChoice {
        public static final String ANIM = "log.key.multi_choice.anim";
        public static final String ANIM2 = "log.key.multi_choice.anim2";
        public static final String BASE = "log.key.multi_choice";
        public static final String DISP = "log.key.multi_choice.disp";
    }

    @ColorLogKey(owner = "ROM.SDK")
    @Deprecated
    public static class MultiSelect {
        public static final String ANIM = "log.key.multi_select.anim";
        public static final String BASE = "log.key.multi_select";
        public static final String DISP = "log.key.multi_select.disp";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class ProgressBar {
        public static final String BASE = "log.key.progress_bar";
        public static final String DRAW = "log.key.progress_bar.draw";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class ScrollBar {
        public static final String BASE = "log.key.scroll_bar";
        public static final String EFFECT = "log.key.scroll_bar.effect";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class SearchView {
        public static final String ANIM = "log.key.search_view.anim";
        public static final String BASE = "log.key.search_view";
    }

    @ColorLogKey(owner = "ROM.SDK")
    public static class ViewPager {
        public static final String BASE = "log.key.view_pager";
        public static final String SELECT = "log.key.view_pager.select";
    }

    String owner() default "";
}

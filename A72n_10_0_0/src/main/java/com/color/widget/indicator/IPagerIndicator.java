package com.color.widget.indicator;

public interface IPagerIndicator {
    void onPageScrollStateChanged(int i);

    void onPageScrolled(int i, float f, int i2);

    void onPageSelected(int i);
}

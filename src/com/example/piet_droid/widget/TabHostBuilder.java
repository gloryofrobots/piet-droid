package com.example.piet_droid.widget;

import java.util.ArrayList;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class TabHostBuilder {

    int mActiveTabColor;
    int mPassiveTabColor;
    int mTextColor;
    int mTabWidth;
    int mTabHeight;
    float mTextSize;

    public final int DEFAULT_DIMENSION = -1;
    ArrayList<TabHost.TabSpec> mTabs;
    TabHost mTabHost;

    private TabHostBuilder(TabHost tabs) {
        mTabHost = tabs;
        mTabHost.setup();
        mTabWidth = DEFAULT_DIMENSION;
        mTabHeight = DEFAULT_DIMENSION;
        mTextSize = 12;
    }

    public static TabHostBuilder create(TabHost tabs) {
        TabHostBuilder helper = new TabHostBuilder(tabs);
        return helper;
    }

    public TabHostBuilder setActiveTabColor(int activeTabColor) {
        this.mActiveTabColor = activeTabColor;
        return this;
    }

    public TabHostBuilder setTextSize(float size) {
        this.mTextSize = size;
        return this;
    }

    public TabHostBuilder setPassiveTabColor(int passiveTabColor) {
        this.mPassiveTabColor = passiveTabColor;
        return this;
    }

    public TabHostBuilder setTabWidth(int width) {
        mTabWidth = width;
        return this;
    }

    public TabHostBuilder setTabHeight(int height) {
        mTabHeight = height;
        return this;
    }

    public TabHostBuilder setTextColor(int color) {
        mTextColor = color;
        return this;
    }

    public TabHostBuilder addTab(int id, String tag, String indicator) {
        TabHost.TabSpec spec = mTabHost.newTabSpec(tag);
        spec.setContent(id);
        spec.setIndicator(indicator);
        mTabHost.addTab(spec);
        return this;
    }

    public void build(int currentTabIndex) {
        mTabHost.setCurrentTab(currentTabIndex);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setTabColors(mTabHost);
            }
        });

        setTabColors(mTabHost);
    }

    private void setTabColors(TabHost tabHost) {
        TabWidget tabWidget = tabHost.getTabWidget();
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            View tab = tabWidget.getChildAt(i);
            setTabAttributes(tab, mPassiveTabColor, mTextColor);
        }

        View tab = tabWidget.getChildAt(tabHost.getCurrentTab());
        setTabAttributes(tab, mActiveTabColor, mTextColor);
    }

    private void setTabAttributes(View tab, int backgroundColor, int textColor) {

        tab.setBackgroundColor(backgroundColor); // unselected
        TextView tv = (TextView) tab.findViewById(android.R.id.title); // Unselected
                                                                       // Tabs
        tv.setTextColor(textColor);
        tv.setBackgroundColor(backgroundColor);
        tv.setTextSize(mTextSize);

        if (mTabHeight != DEFAULT_DIMENSION) {
            tab.getLayoutParams().height = mTabHeight;
        }
        if (mTabWidth != DEFAULT_DIMENSION) {
            tab.getLayoutParams().width = mTabWidth;
        }
    }

}

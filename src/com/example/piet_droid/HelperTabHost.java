package com.example.piet_droid;

import java.util.ArrayList;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class HelperTabHost {
    
   
    int mActiveTabColor;
    int mPassiveTabColor;
    int mTextColor;
    int mTabWidth;
    int mTabHeight;
    
    public final int DEFAULT_DIMENSION = -1;
    ArrayList<TabHost.TabSpec> mTabs;
    TabHost mTabHost; 
    private HelperTabHost(TabHost tabs) {
        // TODO Auto-generated constructor stub
        mTabHost = tabs;
        mTabHost.setup();
        mTabWidth = DEFAULT_DIMENSION;
        mTabHeight = DEFAULT_DIMENSION;
    }
    
    public static HelperTabHost  create(TabHost tabs) {
        HelperTabHost helper = new HelperTabHost(tabs);
        return helper;
    }
 
    public HelperTabHost setActiveTabColor(int activeTabColor) {
        this.mActiveTabColor = activeTabColor;
        return this;
    }

    public HelperTabHost setPassiveTabColor(int passiveTabColor) {
        this.mPassiveTabColor = passiveTabColor;
        return this;
    }
    

    public HelperTabHost setTabWidth(int width) {
        mTabWidth = width;
        return this;
    }
    

    public HelperTabHost setTabHeight(int height) {
        mTabHeight = height;
        return this;
    }
    
    public HelperTabHost setTextColor(int color) {
        mTextColor = color;
        return this;
    }
    
    public HelperTabHost addTab(int id, String tag, String indicator) {
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
                // TODO Auto-generated method stub
                setTabColors(mTabHost);
             }
        });
        
        setTabColors(mTabHost);
    }
    
    private void setTabColors(TabHost tabHost) {
        TabWidget tabWidget = tabHost.getTabWidget();
        for(int i=0; i < tabWidget.getChildCount();i++)
        {
            View tab = tabWidget.getChildAt(i);
            setTabAttributes(tab, mPassiveTabColor, mTextColor);
        }
        
        View tab = tabWidget.getChildAt(tabHost.getCurrentTab());
        setTabAttributes(tab, mActiveTabColor, mTextColor);
    }
    
    private void setTabAttributes(View tab, int backgroundColor, int textColor) {
        
        tab.setBackgroundColor(backgroundColor); //unselected
        TextView tv = (TextView) tab.findViewById(android.R.id.title); //Unselected Tabs
        tv.setTextColor(textColor);
        tv.setBackgroundColor(backgroundColor);
        
        if(mTabHeight != DEFAULT_DIMENSION) {
            tab.getLayoutParams().height = 50;
        }
        if(mTabWidth != DEFAULT_DIMENSION) {
            tab.getLayoutParams().width = 50;
        }
    }
    
   
    

}

package com.example.piet_droid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;

public class AccordeonTabHost extends TabHost {
    
    public AccordeonTabHost(Context context) {
        super(context);
    }

    public AccordeonTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public void setCurrentTab(int index) {
        FrameLayout contentView = getTabContentView();
        
        if (index == getCurrentTab()) {
            if(contentView.getVisibility() == View.VISIBLE) {
                contentView.setVisibility(View.GONE);
            } else {
                contentView.setVisibility(View.VISIBLE);
            }
        } else {
            contentView.setVisibility(View.VISIBLE);
            super.setCurrentTab(index);
        }
    }
}

package com.example.piet_droid.widget;

import com.example.piet_droid.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;

public class AccordionTabHost extends TabHost {

    public AccordionTabHost(Context context) {
        super(context);
    }

    public AccordionTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setup() {
        super.setup();
        //Create fake tab to unselect all possibility
        TabSpec spec = newTabSpec("hidden_tab").setIndicator("").setContent(R.id.fakeAccordeonTabHostTab);
        this.addTab(spec);
        this.getTabWidget().getChildTabViewAt(0).setVisibility(View.GONE);
    }

    @Override
    public void setCurrentTab(int index) {
        if(index == 0) {
            super.setCurrentTab(0);
            return;
        }
        
        final FrameLayout contentView = getTabContentView();
        //Toggle view and switch to the fake tab
        if (index == getCurrentTab()) {
            if (contentView.getVisibility() == View.VISIBLE) {
                contentView.setVisibility(View.GONE);
                super.setCurrentTab(0);
            } else {
                contentView.setVisibility(View.VISIBLE);
            }
        } else {
            contentView.setVisibility(View.VISIBLE);
            super.setCurrentTab(index);
        }
    }

    public void setCurrentTabForce(int resultTab) {
        //Skip accordion behaviour by setting tabwidget to fake tab first
        setCurrentTab(0);
        
        if(resultTab == 0) {
            return;
        }
        
        setCurrentTab(resultTab);
    }
}

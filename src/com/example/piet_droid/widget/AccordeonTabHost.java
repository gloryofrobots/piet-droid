package com.example.piet_droid.widget;

import com.example.piet_droid.R;

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
    public void setup() {
        super.setup();
        //Create fake tab to unselect all possibility
        TabSpec spec = newTabSpec("hiddenTab").setIndicator("").setContent(R.id.fakeAccordeonTabHostTab);
        this.addTab(spec);
        this.getTabWidget().getChildTabViewAt(0).setVisibility(View.GONE);
    }

    @Override
    public void setCurrentTab(int index) {
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
}

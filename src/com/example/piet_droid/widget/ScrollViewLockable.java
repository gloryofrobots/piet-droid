package com.example.piet_droid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ScrollViewLockable extends ScrollView {

    public ScrollViewLockable(Context context) {
        super(context);
    }

    public ScrollViewLockable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewLockable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mScrollable == false) {
            return false;
        }
        
        return super.onTouchEvent(ev);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isScrollable() == false) {
            return false;
        }
        
        return super.onInterceptTouchEvent(ev);
    }
}

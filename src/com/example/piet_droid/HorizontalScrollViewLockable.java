package com.example.piet_droid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HorizontalScrollViewLockable extends HorizontalScrollView {

    public HorizontalScrollViewLockable(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public HorizontalScrollViewLockable(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public HorizontalScrollViewLockable(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
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
        //return super.onInterceptTouchEvent(ev);
//        if (!mScrollable) return false;
//        else return super.onInterceptTouchEvent(ev);
    }
}

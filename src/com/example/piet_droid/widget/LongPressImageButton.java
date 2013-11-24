package com.example.piet_droid.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

public class LongPressImageButton extends ImageButton {
    private Handler mHandler;
    private boolean mIsLongPressed;
    private Runnable mRunnable;
    public long mDelay = 1000;
    
    private LongPressListener mLongPressListener;
    private CancelLongPressListener mCancelLongPressListener;
    
    public interface CancelLongPressListener{
        public void onCancel();
    }
    
    public interface LongPressListener{
        public void onPress();
    }
    
    public LongPressImageButton(Context context) {
        super(context);
        init();
    }

    public LongPressImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LongPressImageButton(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        mHandler = new Handler();
        
        mRunnable = new Runnable() {
            public void run() {
                if (mIsLongPressed) {
                    if(mLongPressListener == null) {
                        return;
                    }
                    
                    mLongPressListener.onPress();
                    mHandler.postDelayed(this, mDelay);
                }
            }
        };
        
        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mIsLongPressed = true;
                mHandler.post(mRunnable);
                return true;
            }
        });
    }

    public void setDelay(long delay) {
        mDelay = delay;
    }
    

    public void setOnCancelLongPressListener(CancelLongPressListener listener) {
        mCancelLongPressListener = listener;
    }
    
    public void setOnLongPressListener(LongPressListener listener) {
        mLongPressListener = listener;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        cancelLongpressIfRequired(event);
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        cancelLongpressIfRequired(event);
        return super.onTrackballEvent(event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                || (keyCode == KeyEvent.KEYCODE_ENTER)) {
            cancelLongpress();
        }
        return super.onKeyUp(keyCode, event);
    }
    
    private void cancelLongpressIfRequired(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_CANCEL)
                || (event.getAction() == MotionEvent.ACTION_UP)) {
            cancelLongpress();
        }
    }
    
    public boolean isLongPressed() {
        return mIsLongPressed;
    }
    
    public void cancelLongpress() {        
        if (mCancelLongPressListener != null) {
            mCancelLongPressListener.onCancel();
        }
        
        setPressed(false);
        mIsLongPressed = false;
    }    
}

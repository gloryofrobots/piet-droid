package com.example.piet_droid.widget;


import com.example.piet_droid.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class ZoomView extends LinearLayout {
    public interface ZoomListener {
        public void onChangeZoom(int count);
    }
    
    private int mCurrentStep;
    private int mLimit;
    public int DEFAULT_MAX_STEP = 10;
    private ZoomListener mListener;
    
    LongPressImageButton mButtonZoomIncrease;
    LongPressImageButton mButtonZoomDecrease;
    
    public ZoomView(Context context) {
        super(context);
        init(context);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        mCurrentStep = 1;
        mLimit = 10;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.zoom_view, this);

        
        mButtonZoomIncrease = (LongPressImageButton) getChildAt(0);
        mButtonZoomDecrease = (LongPressImageButton) getChildAt(1);
        mButtonZoomIncrease.setDelay(50);
        mButtonZoomDecrease.setDelay(50);
        
        mButtonZoomDecrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               performDecrease();
            }
        });
        
        mButtonZoomIncrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               performIncrease();
            }
        });
        
        mButtonZoomDecrease.setOnLongPressListener(new LongPressImageButton.LongPressListener() {
            @Override
            public void onPress() {
                performDecrease();
            }
        } );
        
        mButtonZoomIncrease.setOnLongPressListener(new LongPressImageButton.LongPressListener() {
            @Override
            public void onPress() {
                performIncrease();
            }
        } );
    }
    
    private void performDecrease() {
        mCurrentStep--;
        updateDecrease();
        notifyListener();
    }
    
    private void performIncrease() {
        mCurrentStep++;
        updateIncrease();
        notifyListener();
    }
    
    private void updateDecrease() {
        if(mCurrentStep <= 1) {
            mButtonZoomDecrease.setEnabled(false);
            if(mButtonZoomDecrease.isLongPressed()) {
                mButtonZoomDecrease.cancelLongpress();
            }
        }
        
        if(mCurrentStep < mLimit && mButtonZoomIncrease.isEnabled() == false) {
            mButtonZoomIncrease.setEnabled(true);
        }
    }
    
    private void updateIncrease() {
        if(mCurrentStep >= mLimit) {
            mButtonZoomIncrease.setEnabled(false);
            if(mButtonZoomIncrease.isLongPressed()) {
                mButtonZoomIncrease.cancelLongpress();
            }
        }
        
        if(mCurrentStep > 1 && mButtonZoomDecrease.isEnabled() == false) {
            mButtonZoomDecrease.setEnabled(true);
        }
        
    }
    
    private void notifyListener() {
        if(mListener == null) {
            return;
        }
        
        mListener.onChangeZoom(mCurrentStep);
    }
    
    public void setListener(ZoomListener listener) {
        mListener = listener;
    }
    
    public boolean set(int step, int limit) {
        if(limit < step) {
            return false;
        }
        
        mLimit = limit;
        
        if(step <= 0) {
            mCurrentStep = 1;
        } else {
            mCurrentStep = step;
        }
       
        updateIncrease();
        updateDecrease();
        notifyListener();
        return true;
    }
}




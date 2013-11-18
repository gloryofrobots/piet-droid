package com.example.piet_droid.widget;


import com.example.piet_droid.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ZoomView extends LinearLayout {
  
  
    public interface ZoomListener {
        public void onIncrease(int count);
        public void onDecrease(int count);
    }
    
    private int mCurrentStep;
    private int mMaxStep;
    public int DEFAULT_MAX_STEP = 10;
    private ZoomListener mListener;
    
    ImageButton mButtonZoomIncrease;
    ImageButton mButtonZoomDecrease;
    
    public ZoomView(Context context) {
        super(context);
        init(context);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        mCurrentStep = 0;
        mMaxStep = 10;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.zoom_view, this);

        mButtonZoomDecrease = (ImageButton) getChildAt(0);
        mButtonZoomIncrease = (ImageButton) getChildAt(1);
      

        mButtonZoomDecrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCurrentStep--;
                updateDecrease();
            }
        });
        
        mButtonZoomIncrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCurrentStep++;
                updateIncrease();
            }
        });
    }
    private void updateDecrease() {
        if(mCurrentStep <= 0) {
            mButtonZoomDecrease.setEnabled(false);
        }
        
        if(mCurrentStep < mMaxStep || mButtonZoomIncrease.isEnabled() == false) {
            mButtonZoomIncrease.setEnabled(true);
        }
        
        if(mListener == null) {
            return;
        }
        
        mListener.onDecrease(mCurrentStep);
    }
    
    private void updateIncrease() {
        if(mCurrentStep >= mMaxStep) {
            mButtonZoomIncrease.setEnabled(false);
        }
        
        if(mCurrentStep > 0 || mButtonZoomDecrease.isEnabled() == false) {
            mButtonZoomDecrease.setEnabled(true);
        }
        
        if(mListener == null) {
            return;
        }
        
        mListener.onIncrease(mCurrentStep);
    }
    
    public void setListener(ZoomListener listener) {
        mListener = listener;
    }
    
    public void setCurrentStep(int step) {
        if(step < 0 || step > mMaxStep) {
            return;
        }
        
        if(mCurrentStep == 0) {
            mButtonZoomDecrease.setEnabled(false);
        }
        
        if(mCurrentStep == mMaxStep) {
            mButtonZoomIncrease.setEnabled(false);
        }
    }
}
//ImageButton mButtonRun;
//ImageButton mButtonStep;
//ImageButton mButtonPause;
//ImageButton mButtonStop;
//
//public interface InteractionListener {
//    public void onInteractionRun();
//
//    public void onInteractionStep();
//
//    public void onInteractionPause();
//
//    public void onInteractionStop();
//    // /
//}
//
//private InteractionListener mInteractionListener;
//
//public ControlToolboxView(Context context) {
//    super(context);
//    init(context);
//}
//
//public ControlToolboxView(Context context, AttributeSet attrs) {
//    super(context, attrs);
//    init(context);
//}
//
//
//private void init(Context context) {
//    
//    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    inflater.inflate(R.layout.control_toolbox_view, this);
//
//    mButtonRun = (ImageButton) getChildAt(0);
//    mButtonStep = (ImageButton) getChildAt(1);
//    mButtonPause = (ImageButton) getChildAt(2);
//    mButtonStop = (ImageButton) getChildAt(3);
//
//    setControlsToDefaultState();
//
//    mButtonRun.setOnClickListener(new View.OnClickListener() {
//        public void onClick(View v) {
//            mButtonPause.setEnabled(true);
//            mButtonStop.setEnabled(true);
//            mButtonStep.setEnabled(false);
//            mButtonRun.setEnabled(false);
//
//            mInteractionListener.onInteractionRun();
//        }
//    });
//
//    mButtonStep.setOnClickListener(new View.OnClickListener() {
//        public void onClick(View v) {
//            mButtonStop.setEnabled(true);
//            mInteractionListener.onInteractionStep();
//        }
//    });
//
//    mButtonPause.setOnClickListener(new View.OnClickListener() {
//        public void onClick(View v) {
//            mButtonPause.setEnabled(false);
//            mButtonStop.setEnabled(true);
//            mButtonStep.setEnabled(true);
//            mButtonRun.setEnabled(true);
//
//            mInteractionListener.onInteractionPause();
//
//        }
//    });
//
//    mButtonStop.setOnClickListener(new View.OnClickListener() {
//        public void onClick(View v) {
//            setControlsToDefaultState();
//            mInteractionListener.onInteractionStop();
//
//        }
//    });
//}
//
//public void setControlsToDefaultState() {
//    mButtonRun.setEnabled(true);
//    mButtonStep.setEnabled(true);
//    mButtonPause.setEnabled(false);
//    mButtonStop.setEnabled(false);
//}
//
//public void setInteractionListener(InteractionListener listener) {
//    mInteractionListener = listener;
//}
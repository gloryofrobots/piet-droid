package com.example.piet_droid;

import android.content.Context;
import android.view.View;

import com.example.jpiet.Piet;
import com.example.piet_droid.widget.ColorFieldView;


public class PietFile {
    private String mPath;
    private PietFileActor mActor;
    private PietFileRunner mRunner;
    private Context mContext;
    private ColorFieldView mView;
    boolean mTouched;
    Piet mPiet;
    
    public PietFile(ColorFieldView view, Piet piet, Context context) {
        mContext = context;
        mView = view;
        mPiet = piet;
        mActor = new PietFileActor(this);
        mRunner = new PietFileRunner(this);
    }
    
    public Context getContext() {
        return mContext;
    }
    
    public ColorFieldView getView() {
        return mView;
    }
    
    public Piet getPiet() {
        return mPiet;
    }
    
    public void finalise() {
        mActor.finalise();
        mActor = null;
    }
    
    public void setPath(String path) {
        mPath = path;
    }
    
    public boolean hasPath() {
        return mPath != null;
    }
    
    public String getPath() {
        return mPath;
    }
    
    public PietFileActor getActor() {
        return mActor;
    }
    
    public PietFileRunner getRunner() {
        return mRunner;
    }
    
    public void touch() {
        mTouched = true;
    }
    
    public void untouch() {
        mTouched = false;
    }
    
    public boolean isTouched() {
        return mTouched;
    }
    
    public int getWidth() {
        return mPiet.getModel().getWidth();
    }
    
    public int getHeight() {
        return mPiet.getModel().getHeight();
    }
}

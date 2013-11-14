package com.example.piet_droid;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.example.jpiet.Piet;
import com.example.piet_droid.widget.ColorFieldView;


public class PietFile {
    private String mPath;
    private PietFileActor mActor;
    private PietFileRunner mRunner;
    private Activity mActivity;
    private ColorFieldView mView;
    boolean mTouched = false;
    boolean mIsTemporary = false;
    Piet mPiet;
    
    public PietFile(ColorFieldView view, Piet piet, Activity activity) {
        mActivity = activity;
        mView = view;
        mPiet = piet;
        mActor = new PietFileActor(this);
        mRunner = new PietFileRunner(this);
    }
    
    public Activity getActivity() {
        return mActivity;
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
    
    public void setTemporary(boolean value) {
        mIsTemporary = value;
    }
    
    public boolean isTemporary() {
        return mIsTemporary;
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

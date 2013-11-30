package com.example.piet_droid;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.example.jpiet.Piet;
import com.example.piet_droid.widget.ColorFieldView;

public class PietFile {
    private String mPath;
//    private String mTag;
    private PietFileActor mActor;
    private PietFileRunner mRunner;
    private PietFileSaver mSaver;
    private PietFileLoader mLoader;

    private Activity mActivity;
    private ColorFieldView mView;
    boolean mTouched = false;
    boolean mIsTemporary = false;
    Piet mPiet;
    boolean mIsValid;

    public PietFile(ColorFieldView view, Piet piet, Activity activity) {
        mActivity = activity;
        mView = view;
        mPiet = piet;
        mIsValid = true;
        mActor = new PietFileActor(this);
        mRunner = new PietFileRunner(this);
        mLoader = new PietFileLoader(this);
        mSaver = new PietFileSaver(this);
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

        mRunner.finalise();
        mRunner = null;

        mLoader.finalise();
        mLoader = null;

        mSaver.finalise();
        mSaver = null;

        mIsValid = false;
    }

    public boolean isValid() {
        return mIsValid;
    }

//    public void setTag(String tag) {
//        mTag = tag;
//    }
//
//    public String getTag() {
//        return mTag;
//    }

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

    public PietFileLoader getLoader() {
        return mLoader;
    }

    public PietFileSaver getSaver() {
        return mSaver;
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

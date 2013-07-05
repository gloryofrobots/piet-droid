package com.example.pieteditor;

public class CodelChoser {

    public final static int RIGHT = 0;
    public final static int LEFT = 1;

    private int mState;

    CodelChoser(){
        mState = RIGHT;
    }

    public void roll( int _countSteps ){
        mState = (mState + _countSteps) % 2;
    }

    public void switchState(){
        roll(1);
    }
}

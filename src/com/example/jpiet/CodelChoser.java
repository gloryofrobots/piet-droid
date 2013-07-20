package com.example.jpiet;
/*
 * Representation of CodelChoser
 *
 */

public class CodelChoser {

    /*
     * CAREFUL Values is Important if you change it for example to
     * RIGHT = 4
     * LEFT = 5
     * then roll method will be broken
     */
    public final static int RIGHT = 0;
    public final static int LEFT = 1;
    private int mState;

    CodelChoser() {
        mState = LEFT;
    }

    public boolean isLeft() {
        return mState == LEFT;
    }
   
    public boolean isRight() {
        return mState == RIGHT;
    }
    
    public void roll(int _countSteps) {
        mState = (mState + _countSteps) % 2;
    }

    public void switchState() {
        roll(1);
    }
    
    public String toString() {
		String repr = "";

		switch (mState) {
		case LEFT:
			repr = "LEFT";
			break;
		case RIGHT:
			repr = "RIGHT";
			break;
		}

		return repr;
	}
}

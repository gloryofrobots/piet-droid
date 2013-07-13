package com.example.jpiet;

/**
 * Representation of Direction Pointer
 * 
 */
public class DirectionPointer {

	/**
	 * CAREFUL Values is Important if you change it for example to RIGHT = 4
	 * LEFT = 5 then roll method will be broken
	 */
	final static public int RIGHT = 0;
	final static public int BOTTOM = 1;
	final static public int LEFT = 2;
	final static public int TOP = 3;
	private int mState;

	DirectionPointer() {
		mState = RIGHT;
	}

	public boolean isRight() {
		return mState == RIGHT;
	}

	public boolean isLeft() {
		return mState == LEFT;
	}

	public boolean isBottom() {
		return mState == BOTTOM;
	}

	public boolean isTop() {
		return mState == TOP;
	}

	public void roll(int _step) {
		mState = (mState + _step) % 4;
	}

	public void rollClockWise() {
		roll(1);
	}

	public int getState() {
		return mState;
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
		case TOP:
			repr = "TOP";
			break;
		case BOTTOM:
			repr = "BOTTOM";
			break;
		}

		return repr;
	}
}

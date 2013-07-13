package com.example.piet_droid;

import java.io.IOException;

import android.widget.EditText;
import android.widget.TextView;

import com.example.jpiet.InOutSystem;

public class InOutSystemEditText implements InOutSystem {
	private EditText mIn;
	private TextView mOut;
	private CharSequence mCurrentData;
	private int mCurrentIndex;
	private String mBuffer;
	
	public InOutSystemEditText(EditText in, TextView out) {
		mIn = in;
		mOut = out;
	}
	
	public void prepare(){
		mBuffer = "";
		mCurrentData = mIn.getText();
		mCurrentIndex = 0;
		mOut.setText("");
	}
	//flush must called from ui thread
	public void flush(){
	    CharSequence txt = mOut.getText();
		mOut.setText(txt.toString() + mBuffer);
		mBuffer = "";
	}
	
	@Override
	public int read() throws IOException {
		if( mCurrentIndex >= mCurrentData.length()) {
			throw new IOException();
		}
		
		char input = mCurrentData.charAt(mCurrentIndex);
		int result = Character.getNumericValue(input);
		
		mCurrentIndex++;
		return result;
	}

	@Override
	public void write(int output) {
		mBuffer += (char) output;
	}

	@Override
	public void write(String output) {
		mBuffer += output;
	}

}

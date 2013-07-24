package com.example.piet_droid;

import java.io.IOException;
import java.util.ArrayList;

import android.widget.EditText;
import android.widget.TextView;

import com.example.jpiet.InOutSystem;

public class InOutSystemEditText implements InOutSystem {
	private EditText mIn;
	private TextView mOut;
	private ArrayList<Integer> mCurrentData;
	private int mCurrentIndex;
	private String mBuffer;
	
	public InOutSystemEditText(EditText in, TextView out) {
		mIn = in;
		mOut = out;
	}
	
	public void prepare(){
		mBuffer = "";
		mCurrentData = new ArrayList<Integer>();
		
		mCurrentIndex = 0;
		mOut.setText("");
		
		String data = mIn.getText().toString();
        String [] tokens = data.split(" ");
        
        for(String token : tokens) {
            int value = 0;
            try {
                value = Integer.valueOf(token);
            } catch(NumberFormatException e) {
                value = Character.getNumericValue(value);
            } 
            
            mCurrentData.add(value);
        }
	}
	//flush must called from ui thread
	public void flush(){
	    CharSequence txt = mOut.getText();
		mOut.setText(txt.toString() + mBuffer);
		mBuffer = "";
	}
	
	@Override
	public int read() throws IOException {
		if( mCurrentIndex >= mCurrentData.size()) {
			throw new IOException();
		}
		
		int result = mCurrentData.get(mCurrentIndex);
		
		mCurrentIndex++;
		return result;
	}

	@Override
	public void write(int output) {
		mBuffer += String.valueOf(output);
	}

	@Override
	public void write(String output) {
		mBuffer += output;
	}

}
